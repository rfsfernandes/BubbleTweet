package pt.rfernandes.bubbletweet.model.viewmodels;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.model.CustomUser;


public class MainActivityViewModel extends AndroidViewModel {
  private final Application mApplication;
  private final Repository mRepository;

  public MutableLiveData<CustomUser> authLiveData = new MutableLiveData<>();
  public MutableLiveData<CustomUser> mFirebaseUserMutableLiveData = new MutableLiveData<>();


  /**
   * This méthod creates a new instance of the ViewModel.
   *
   * @param application Use getApplicationContext
   */

  public MainActivityViewModel(@NonNull Application application) {
    super(application);
    this.mApplication = application;
    this.mRepository = Repository.getInstance(application);


    try {
      InputStream inputStream = application.getResources().getAssets().open("twitter_consumer_key" +
          ".txt");
      Constants.KEY = assetFiletoString(inputStream);
      inputStream.close();

      inputStream = application.getResources().getAssets().open("twitter_consumer_secret" +
          ".txt");
      Constants.SECRET = assetFiletoString(inputStream);

      inputStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    mRepository.getUserLoggedIn(object -> mFirebaseUserMutableLiveData.postValue(object));

  }

  private String assetFiletoString(InputStream is) throws IOException {
    StringBuilder buf = new StringBuilder();
    BufferedReader in =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    String str;

    while ((str = in.readLine()) != null) {
      buf.append(str);
    }

    in.close();

    return buf.toString();
  }

  private void setLoggedInUser(TwitterSession session, FirebaseUser firebaseUser) {
    CustomUser customUser = new CustomUser(session.getAuthToken().secret, firebaseUser.getDisplayName(),
        firebaseUser.getEmail(), firebaseUser.getPhotoUrl().toString(), session.getAuthToken().token,
        firebaseUser.getUid(),
        firebaseUser.getProviderId());

    this.mRepository.setUserLoggedIn(customUser);
    authLiveData.postValue(customUser);
  }

  public void authTwitter(TwitterSession session, FirebaseUser firebaseUser) {
    setLoggedInUser(session, firebaseUser);

  }


}