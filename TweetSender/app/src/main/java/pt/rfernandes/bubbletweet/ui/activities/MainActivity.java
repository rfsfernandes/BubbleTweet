package pt.rfernandes.bubbletweet.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.service.FloatingService;
import pt.rfernandes.bubbletweet.model.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

  private static final int APP_OVERLAY_PERMISSION = 1000;
  private Context context;
  private MainActivityViewModel mMainActivityViewModel;
  private ImageView imageView;
  private TextView textViewUsername;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

    imageView = findViewById(R.id.imageView);
    textViewUsername = findViewById(R.id.textViewUsername);

    context = this;
    initViewModel();

    // Asking for permission from user...
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, APP_OVERLAY_PERMISSION);
    }

    findViewById(R.id.createBtn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context))) {
          // Permission was already granted..starting service for creating the Floating Button UI...
          mMainActivityViewModel.authTwitter(MainActivity.this);
        }
      }
    });

    findViewById(R.id.createBtn).setVisibility(checkIfOverlayPermissionGranted() ? View.VISIBLE : View.GONE);
  }

  private void initViewModel(){
    mMainActivityViewModel.authLiveData.observe(this, authResult -> {
//      startService(new Intent(context, FloatingService.class));
//      finish();
      if(authResult != null) {
        setViews(authResult);
      }

    });

    mMainActivityViewModel.mFirebaseUserMutableLiveData.observe(this, firebaseUser -> {
      if(firebaseUser != null) {
        setViews(firebaseUser);
      }
    });

  }

  private void setViews(FirebaseUser user) {
    findViewById(R.id.createBtn).setVisibility(View.GONE);
    Glide.with(MainActivity.this).load(user.getPhotoUrl()).into(imageView);
    textViewUsername.setText(user.getDisplayName());
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
    }
  }

  // Shows message to the user...
  void showMessage(String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }
}
