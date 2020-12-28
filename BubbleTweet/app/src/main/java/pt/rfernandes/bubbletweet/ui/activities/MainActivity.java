package pt.rfernandes.bubbletweet.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.service.FloatingService;
import pt.rfernandes.bubbletweet.model.CustomUser;
import pt.rfernandes.bubbletweet.model.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

  private static final int APP_OVERLAY_PERMISSION = 1000;
  private Context context;
  private MainActivityViewModel mMainActivityViewModel;
  private ImageView imageView;
  private TextView textViewUsername;
  private TwitterLoginButton mTwitterBtn;
  private FirebaseAuth mFirebaseAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //This code must be entering before the setContentView to make the twitter login work...
    TwitterAuthConfig mTwitterAuthConfig = new TwitterAuthConfig(getString(R.string.twitter_consumer_key),
        getString(R.string.twitter_consumer_secret));
    TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
        .twitterAuthConfig(mTwitterAuthConfig)
        .debug(true)
        .build();
    Twitter.initialize(twitterConfig);
    setContentView(R.layout.activity_main);

    mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    mFirebaseAuth = FirebaseAuth.getInstance();
    imageView = findViewById(R.id.imageView);
    textViewUsername = findViewById(R.id.textViewUsername);
    mTwitterBtn = findViewById(R.id.createBtn);
    context = this;
    initViewModel();

    // Asking for permission from user...
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, APP_OVERLAY_PERMISSION);
    }

    mTwitterBtn.setCallback(new Callback<TwitterSession>() {
      @Override
      public void success(Result<TwitterSession> result) {
        Toast.makeText(MainActivity.this, "Signed in to twitter successful", Toast.LENGTH_LONG).show();
        signInToFirebaseWithTwitterSession(result.data);
        mTwitterBtn.setVisibility(View.VISIBLE);
      }

      @Override
      public void failure(TwitterException exception) {
        Toast.makeText(MainActivity.this, "Login failed. No internet or No Twitter app found on your phone",
            Toast.LENGTH_LONG).show();
      }
    });

    mTwitterBtn.setVisibility(checkIfOverlayPermissionGranted() ? View.VISIBLE : View.GONE);
  }

  private void signInToFirebaseWithTwitterSession(TwitterSession session) {
    AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,
        session.getAuthToken().secret);

    mFirebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (!task.isSuccessful()) {
              Toast.makeText(MainActivity.this, "Auth firebase twitter failed", Toast.LENGTH_LONG).show();
            } else {
              if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context))) {
                // Permission was already granted..starting service for creating the Floating Button UI...
                if(task.getResult() != null) {
                  mMainActivityViewModel.authTwitter(session, task.getResult().getUser());
                }
              }
            }
          }
        });
  }

  private void initViewModel() {
    mMainActivityViewModel.authLiveData.observe(this, authResult -> {

      if (authResult != null) {
        setViews(authResult);
        startService(new Intent(context, FloatingService.class));
        finish();
      }

    });

    mMainActivityViewModel.mFirebaseUserMutableLiveData.observe(this, firebaseUser -> {
      if (firebaseUser != null) {
        setViews(firebaseUser);
        startService(new Intent(context, FloatingService.class));
        finish();
      }
    });

  }

  private void setViews(CustomUser user) {
//    findViewById(R.id.createBtn).setVisibility(View.GONE);
    Glide.with(MainActivity.this).load(user.getPhotoUrl()).into(imageView);
    textViewUsername.setText(user.getName());
  }

  private Boolean checkIfOverlayPermissionGranted() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context))) {
      // Permission was already granted..starting service for creating the Floating Button UI...
      startService(new Intent(context, FloatingService.class));
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == APP_OVERLAY_PERMISSION) {
      showMessage(checkIfOverlayPermissionGranted() ? "Overlay Permission Granted :)" : "Overlay Permission Denied :(");
      findViewById(R.id.createBtn).setVisibility(checkIfOverlayPermissionGranted() ? View.VISIBLE : View.GONE);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
      mTwitterBtn.onActivityResult(requestCode, resultCode, data);
    }
  }

  // Shows message to the user...
  void showMessage(String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }
}
