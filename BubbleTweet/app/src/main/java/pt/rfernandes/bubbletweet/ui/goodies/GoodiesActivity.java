package pt.rfernandes.bubbletweet.ui.goodies;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;

import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;

import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.service.FloatingService;
import pt.rfernandes.bubbletweet.data.local.SharedPreferencesManager;

import static pt.rfernandes.bubbletweet.custom.utils.UtilsClass.md5;

public class GoodiesActivity extends AppCompatActivity {
  private RewardedAd rewardedGoodie;
  private ProgressBar progressBar2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_goodies);


//    MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
    if (Constants.APP_DEBUG) {
      String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
      String deviceId = md5(android_id).toUpperCase();
      RequestConfiguration configuration =
          new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(deviceId)).build();
      MobileAds.setRequestConfiguration(configuration);
    }
    progressBar2 = findViewById(R.id.progressBar2);
    progressBar2.setVisibility(View.VISIBLE);
    rewardedGoodie = new RewardedAd(this,
        getResources().getString(R.string.interstitial_ad_unit_id));

    RewardedAdLoadCallback goodieLoadCallback = new RewardedAdLoadCallback() {
      @Override
      public void onRewardedAdLoaded() {
        progressBar2.setVisibility(View.GONE);
        // Ad successfully loaded.
        if (rewardedGoodie.isLoaded()) {
          Activity activityContext = GoodiesActivity.this;
          RewardedAdCallback adCallback = new RewardedAdCallback() {
            @Override
            public void onRewardedAdOpened() {
              // Ad opened.
            }

            @Override
            public void onRewardedAdClosed() {
              // Ad closed.
              Toast.makeText(GoodiesActivity.this, getResources().getString(R.string.sorry_ads),
                  Toast.LENGTH_SHORT).show();
              resumeService();
            }


            @Override
            public void onUserEarnedReward(@NonNull RewardItem reward) {
              // User earned reward.
              SharedPreferencesManager.getInstance(getApplication()).setTokenKey(reward.getAmount());
              Toast.makeText(GoodiesActivity.this, getResources().getString(R.string.thank_you_ad),
                  Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedAdFailedToShow(AdError adError) {
              // Ad failed to display.
              Toast.makeText(GoodiesActivity.this, getResources().getString(R.string.no_ads_to_show), Toast.LENGTH_SHORT).show();
              resumeService();
            }
          };
          rewardedGoodie.show(activityContext, adCallback);
        }
      }

      @Override
      public void onRewardedAdFailedToLoad(LoadAdError adError) {
        // Ad failed to load.
        Toast.makeText(GoodiesActivity.this, getResources().getString(R.string.no_ads_to_show), Toast.LENGTH_SHORT).show();
        resumeService();
      }
    };

    rewardedGoodie.loadAd(new AdRequest.Builder().build(), goodieLoadCallback);

  }

  private void resumeService(){
    finish();
    progressBar2.setVisibility(View.GONE);
    if (Settings.canDrawOverlays(GoodiesActivity.this)) {
      // Permission was already granted..starting service for creating the Floating Button UI...
      startService(new Intent(GoodiesActivity.this, FloatingService.class));
    }
  }


}