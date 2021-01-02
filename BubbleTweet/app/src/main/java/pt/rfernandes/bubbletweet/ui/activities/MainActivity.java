package pt.rfernandes.bubbletweet.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import de.hdodenhof.circleimageview.CircleImageView;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.service.FloatingService;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;
import pt.rfernandes.bubbletweet.data.local.SharedPreferencesManager;
import pt.rfernandes.bubbletweet.model.CustomUser;
import pt.rfernandes.bubbletweet.model.viewmodels.MainActivityViewModel;
import pt.rfernandes.bubbletweet.ui.goodies.GoodiesActivity;

import static pt.rfernandes.bubbletweet.ui.goodies.GoodiesActivity.FROM_MAIN_LOGIN;

public class MainActivity extends AppCompatActivity {

  private static final int APP_OVERLAY_PERMISSION = 1000;
  private static final int ADS_LOGIN = 656;
  private Context context;
  private MainActivityViewModel mMainActivityViewModel;
  private CircleImageView imageView;
  private TextView textViewDisplayName;
  private TextView textViewUsername;
  private TwitterLoginButton mTwitterBtn;
  private Button buttonActivateService;
  private Button buttonLogout;
  private FirebaseAuth mFirebaseAuth;
  private LinearLayout linearLayoutUserInfo;
  private ProgressBar progressBar;
  private TwitterSession mTwitterSession;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(UtilsClass.getInstance().setStatusBarDark(this));
    super.onCreate(savedInstanceState);

    mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    if (Constants.sTweetCreds != null) {
      TwitterAuthConfig mTwitterAuthConfig = new TwitterAuthConfig(Constants.sTweetCreds.getTweetConsumerKey(),
          Constants.sTweetCreds.getTweetConsumerSecret());
      TwitterConfig twitterConfig = new TwitterConfig.Builder(MainActivity.this)
          .twitterAuthConfig(mTwitterAuthConfig)
          .debug(Constants.TW_DEBUGGER)
          .build();
      Twitter.initialize(twitterConfig);
    }
    setContentView(R.layout.activity_main);
    initViewModel();
    mMainActivityViewModel.getLoggedInUser();


    mFirebaseAuth = FirebaseAuth.getInstance();
    imageView = findViewById(R.id.imageView);
    textViewDisplayName = findViewById(R.id.textViewDisplayName);
    textViewUsername = findViewById(R.id.textViewUsername);
    mTwitterBtn = findViewById(R.id.createBtn);
    buttonActivateService = findViewById(R.id.buttonActivateService);
    linearLayoutUserInfo = findViewById(R.id.linearLayoutUserInfo);
    buttonLogout = findViewById(R.id.buttonLogout);
    progressBar = findViewById(R.id.progressBar);
    SwitchMaterial switchMaterial = findViewById(R.id.switch1);
    boolean toShowEnding = SharedPreferencesManager.getInstance(getApplication()).getTweetEndingValue();
    switchMaterial.setChecked(toShowEnding);

    switchMaterial.setOnCheckedChangeListener(
        (buttonView, isChecked)
            -> {
          SharedPreferencesManager
              .getInstance(getApplication())
              .setTweetEndingValue(isChecked);
        }
    );

    context = this;

    mTwitterBtn.setOnClickListener(v -> progressBar.setVisibility(View.VISIBLE));

    mTwitterBtn.setCallback(new Callback<TwitterSession>() {
      @Override
      public void success(Result<TwitterSession> result) {
        mTwitterSession = result.data;
        Intent intent = new Intent(MainActivity.this, GoodiesActivity.class);
        intent.putExtra(FROM_MAIN_LOGIN, true);
        startActivityForResult(intent, ADS_LOGIN);

        mTwitterBtn.setVisibility(View.VISIBLE);
      }

      @Override
      public void failure(TwitterException exception) {
        progressBar.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
            getResources().getString(R.string.login_failed), Snackbar.LENGTH_LONG);
        if (exception.getMessage() != null && exception.getMessage().contains("Failed to get " +
            "request token")) {
          snackbar = Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
              getResources().getString(R.string.login_failed), Snackbar.LENGTH_LONG);
          snackbar.setAction(R.string.get_twitter, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(Intent.ACTION_VIEW);
              intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.twitter.android&hl=pt_PT&gl=US"));
              startActivity(intent);
            }
          });
        }

        snackbar.show();

      }
    });

    mTwitterBtn.setVisibility(checkIfOverlayPermissionGranted() ? View.VISIBLE : View.GONE);

    buttonActivateService.setOnClickListener(v -> {
      // Asking for permission from user...
      if (!Settings.canDrawOverlays(context)) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, APP_OVERLAY_PERMISSION);
      } else {
        startService(new Intent(context, FloatingService.class));
        finish();
      }

    });

    buttonLogout.setOnClickListener(v -> {
      progressBar.setVisibility(View.VISIBLE);
      mMainActivityViewModel.logout();
    });

  }

  private void signInToFirebaseWithTwitterSession(TwitterSession session) {
    AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,
        session.getAuthToken().secret);

    mFirebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, task -> {
          if (!task.isSuccessful()) {
            progressBar.setVisibility(View.GONE);
            Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
                getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show();
          } else {
            if (task.getResult() != null) {
              mMainActivityViewModel.authTwitter(session, task.getResult().getUser());
            }
          }
        });
  }

  private void initViewModel() {
    mMainActivityViewModel.authLiveData.observe(this, authResult -> {
      progressBar.setVisibility(View.GONE);
      if (authResult != null) {
        setViews(authResult, true);
      } else {
        setViews(null, false);
      }

    });

    mMainActivityViewModel.mFirebaseUserMutableLiveData.observe(this, firebaseUser -> {
      if (firebaseUser != null) {
        setViews(firebaseUser, true);
      } else {
        setViews(null, false);
      }

    });

  }

  private void startService() {
    startService(new Intent(context, FloatingService.class));
    finish();
  }

  private void setViews(@Nullable CustomUser user, boolean show) {
    buttonLogout.setVisibility(show ? View.VISIBLE : View.GONE);
    linearLayoutUserInfo.setVisibility(show ? View.VISIBLE : View.GONE);
    mTwitterBtn.setVisibility(show ? View.GONE : View.VISIBLE);
    buttonActivateService.setVisibility(show ? View.VISIBLE : View.GONE);
    if (user != null) {
      textViewUsername.setText(String.format("@%s", user.getUsername()));
      Glide.with(MainActivity.this).load(user.getPhotoUrl()).into(imageView);
      textViewDisplayName.setText(user.getName());
    }

  }

  private Boolean checkIfOverlayPermissionGranted() {
    return Settings.canDrawOverlays(context);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (Settings.canDrawOverlays(context)) {
      // Permission was already granted..starting service for creating the Floating Button UI...
      startService(new Intent(context, FloatingService.class));
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == APP_OVERLAY_PERMISSION) {
      showMessage(checkIfOverlayPermissionGranted() ? getString(R.string.overlay_granted) : getString(R.string.overlay_denied));
      if (Settings.canDrawOverlays(context)) {
        startService(new Intent(context, FloatingService.class));
        finish();
      }
      
    } else if( requestCode == ADS_LOGIN){
      if(resultCode == RESULT_OK){

        Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
            getResources().getString(R.string.signed_in_success), Snackbar.LENGTH_LONG).show();

        signInToFirebaseWithTwitterSession(mTwitterSession);
      } else {
        Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
            getString(R.string.watch_ad_please), Snackbar.LENGTH_LONG).show();
      }
    }else {
      super.onActivityResult(requestCode, resultCode, data);
      mTwitterBtn.onActivityResult(requestCode, resultCode, data);
    }
  }

  // Shows message to the user...
  void showMessage(String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

}
