package pt.rfernandes.bubbletweet.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.data.local.DBCallBack;
import pt.rfernandes.bubbletweet.model.TweetCreds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

public class SplashActivity extends AppCompatActivity {
  private DatabaseReference mDatabaseReference;
  private Repository mRepository;
  private Activity mActivity;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    mRepository = Repository.getInstance(getApplication());
    mDatabaseReference = FirebaseDatabase.getInstance().getReference()
        .child("ttkeys");
    mActivity = this;

    mRepository.getTweetCreds(object -> {
      if(object == null) {
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
            Toast.makeText(mActivity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
          }
        };
        mDatabaseReference.addValueEventListener(postListener);
      } else {
        new Handler(getMainLooper()).postDelayed(() -> {
          Constants.sTweetCreds = object;
          startActivity(new Intent(mActivity, MainActivity.class));
          mActivity.finish();
        }, 2000);
      }
    });



  }
}