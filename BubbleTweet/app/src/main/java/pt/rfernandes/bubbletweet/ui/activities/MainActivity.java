package pt.rfernandes.bubbletweet.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import de.hdodenhof.circleimageview.CircleImageView;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.service.FloatingService;
import pt.rfernandes.bubbletweet.data.local.SharedPreferencesManager;
import pt.rfernandes.bubbletweet.model.CustomUser;
import pt.rfernandes.bubbletweet.model.TweetCreds;
import pt.rfernandes.bubbletweet.model.viewmodels.FirebaseRTDBCallBack;
import pt.rfernandes.bubbletweet.model.viewmodels.MainActivityViewModel;

import static pt.rfernandes.bubbletweet.custom.Constants.FROM_DEFS;
import static pt.rfernandes.bubbletweet.custom.Constants.FROM_WIDGET;

public class MainActivity extends AppCompatActivity {

  private static final int APP_OVERLAY_PERMISSION = 1000;
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


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    TwitterAuthConfig mTwitterAuthConfig = new TwitterAuthConfig(Constants.sTweetCreds.getTweetConsumerKey(),
        Constants.sTweetCreds.getTweetConsumerSecret());
    TwitterConfig twitterConfig = new TwitterConfig.Builder(MainActivity.this)
        .twitterAuthConfig(mTwitterAuthConfig)
        .debug(Constants.TW_DEBUGGER)
        .build();
    Twitter.initialize(twitterConfig);

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


    // Asking for permission from user...
    if (!Settings.canDrawOverlays(context)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, APP_OVERLAY_PERMISSION);
    }

    mTwitterBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
      }
    });
    mTwitterBtn.setCallback(new Callback<TwitterSession>() {
      @Override
      public void success(Result<TwitterSession> result) {

        Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
            getResources().getString(R.string.signed_in_success), Snackbar.LENGTH_LONG).show();

        signInToFirebaseWithTwitterSession(result.data);

        mTwitterBtn.setVisibility(View.VISIBLE);
      }

      @Override
      public void failure(TwitterException exception) {
        Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
            getResources().getString(R.string.login_failed), Snackbar.LENGTH_LONG).show();
      }
    });

    mTwitterBtn.setVisibility(checkIfOverlayPermissionGranted() ? View.VISIBLE : View.GONE);

    buttonActivateService.setOnClickListener(v -> {
      startService(new Intent(context, FloatingService.class));
      finish();
    });

    buttonLogout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        mMainActivityViewModel.logout();
      }
    });

  }

  private void signInToFirebaseWithTwitterSession(TwitterSession session) {
    AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,
        session.getAuthToken().secret);


    mFirebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (!task.isSuccessful()) {
              Snackbar.make(MainActivity.this, findViewById(android.R.id.content),
                  getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show();
            } else {
              if (Settings.canDrawOverlays(context)) {
                // Permission was already granted..starting service for creating the Floating Button UI...
                if (task.getResult() != null) {
                  mMainActivityViewModel.authTwitter(session, task.getResult().getUser());
                }
              }
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
      if(firebaseUser != null) {

        if(getIntent().getExtras() != null) {
          Bundle extras = getIntent().getExtras();
          if(extras != null) {
            boolean fromWidget = extras.getBoolean(FROM_WIDGET);
            boolean fromDefs = extras.getBoolean(FROM_DEFS);
            if(fromWidget) {
             startService();
            } else if(fromDefs) {

              if (firebaseUser != null) {
                setViews(firebaseUser, true);
              } else {
                setViews(null, false);
              }

            } else {
              startService();
            }
          }
        } else {
          if (firebaseUser != null) {
            setViews(firebaseUser, true);
          } else {
            setViews(null, false);
          }
        }
      }

    });

  }

  private void startService(){
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
