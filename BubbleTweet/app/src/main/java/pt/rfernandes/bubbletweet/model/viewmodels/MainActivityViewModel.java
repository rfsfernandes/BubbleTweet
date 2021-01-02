package pt.rfernandes.bubbletweet.model.viewmodels;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import pt.rfernandes.bubbletweet.data.local.DBCallBack;
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

  }

  public void getTokens(FirebaseRTDBCallBack callBack){

  }

  public void getLoggedInUser(){
    mRepository.getUserLoggedIn(object -> mFirebaseUserMutableLiveData.postValue(object));
  }

  private void setLoggedInUser(TwitterSession session, FirebaseUser firebaseUser) {
    CustomUser customUser = new CustomUser(session.getUserId(), session.getUserName(),
        session.getAuthToken().secret,
        firebaseUser.getDisplayName(),
        firebaseUser.getEmail(), firebaseUser.getPhotoUrl().toString().replace("normal", "400x400"),
        session.getAuthToken().token,
        firebaseUser.getUid(),
        firebaseUser.getProviderId());

    this.mRepository.setUserLoggedIn(customUser);
    authLiveData.postValue(customUser);
  }

  public void authTwitter(TwitterSession session, FirebaseUser firebaseUser) {
    setLoggedInUser(session, firebaseUser);
  }

  public void logout(){
    this.mRepository.logout(new DBCallBack<Boolean>() {
      @Override
      public void returnDB(Boolean object) {
        authLiveData.postValue(null);
      }
    });
  }

}