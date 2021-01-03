package pt.rfernandes.bubbletweet.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.CallBackNetwork;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.MyApp;
import pt.rfernandes.bubbletweet.custom.utils.CheckInternet;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.model.TweetCreds;

public class SplashActivity extends AppCompatActivity {
  private DatabaseReference mDatabaseReference;
  private Repository mRepository;
  private Activity mActivity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      final WindowInsetsController controller = getWindow().getInsetsController();

      if (controller != null)
        controller.hide(WindowInsets.Type.statusBars());
    } else {
      //noinspection deprecation
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    setTheme(UtilsClass.getInstance().setStatusBarDark(this));
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_splash);

    new CheckInternet(internet -> {
      if (!internet) {
        Snackbar.make(SplashActivity.this, findViewById(android.R.id.content), getString(R.string.no_internet), Snackbar.LENGTH_LONG).show();
      }
    });

    MyApp myApp = (MyApp) getApplication();

    myApp.networkChangedCallBack(isAvailable -> {

      mRepository = Repository.getInstance(getApplication());
      mDatabaseReference = FirebaseDatabase.getInstance().getReference()
          .child("ttkeys");
      mActivity = SplashActivity.this;

      mRepository.getTweetCreds(object -> {
        if (object == null) {
          new CheckInternet(internet -> {
            if (internet && isAvailable) {
              ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                  String key = dataSnapshot.child("tt_c_key").getValue().toString();

                  String secret = dataSnapshot.child("tt_c_secret").getValue().toString();

                  String password = dataSnapshot.child("password").getValue().toString();

                  TweetCreds tweetCreds = new TweetCreds(key, secret, password);

                  mRepository.setTweetCreds(mActivity, tweetCreds, object -> new Handler(getMainLooper()).postDelayed(() -> {
                    Constants.sTweetCreds = tweetCreds;
                    startActivity(new Intent(mActivity, MainActivity.class));
                    mActivity.finish();
                  }, 3000));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                  Toast.makeText(mActivity, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
              };
              mDatabaseReference.addValueEventListener(postListener);
            } else {
              //error message
              Snackbar.make(SplashActivity.this, findViewById(android.R.id.content), getString(R.string.no_internet), Snackbar.LENGTH_LONG).show();
            }
          });

        } else {
          new Handler(getMainLooper()).postDelayed(() -> {
            Constants.sTweetCreds = object;
            startActivity(new Intent(mActivity, MainActivity.class));
            mActivity.finish();
          }, 2000);
        }
      });

    });

  }

}