package pt.rfernandes.bubbletweet.custom;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.widget.Toast;

import com.bugsnag.android.Bugsnag;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.utils.CheckInternet;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.model.TweetCreds;
import pt.rfernandes.bubbletweet.ui.activities.MainActivity;
import pt.rfernandes.bubbletweet.ui.activities.SplashActivity;

public class MyApp extends Application implements CallBackNetwork{
  public boolean networkAvailable = false;
  private ConnectivityManager mConnectivityManager;
  private ConnectivityManager.NetworkCallback mNetworkCallback;
  private CallBackNetwork mCallBackNetwork;
  @Override
  public void onCreate() {
    super.onCreate();
    Bugsnag.start(this);

    mConnectivityManager
        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    networkChangedCallBack(this);

  }

  public void networkChangedCallBack(CallBackNetwork callBackNetwork) {

    mNetworkCallback = new ConnectivityManager.NetworkCallback() {

      @Override
      public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);

        callBackNetwork.networkAvailable(true);
      }

      @Override
      public void onLost(@NonNull Network network) {
        super.onLost(network);

        callBackNetwork.networkAvailable(false);
      }

      @Override
      public void onUnavailable() {
        super.onUnavailable();

        callBackNetwork.networkAvailable(false);

      }

      @Override
      public void onLosing(@NonNull Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
        callBackNetwork.networkAvailable(false);

      }

      @Override
      public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);

      }

      @Override
      public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
        super.onBlockedStatusChanged(network, blocked);
      }
    };

    mConnectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(),
        mNetworkCallback);

  }

  @Override
  public void networkAvailable(boolean isAvailable) {
    networkAvailable = isAvailable;
  }
}
