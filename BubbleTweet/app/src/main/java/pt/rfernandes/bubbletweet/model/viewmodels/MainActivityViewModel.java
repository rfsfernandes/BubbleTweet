package pt.rfernandes.bubbletweet.model.viewmodels;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.TwitterSession;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.data.local.DBCallBack;
import pt.rfernandes.bubbletweet.data.local.SharedPreferencesManager;
import pt.rfernandes.bubbletweet.model.CustomUser;


public class MainActivityViewModel extends AndroidViewModel {
  private final Application mApplication;
  private final Repository mRepository;

  public MutableLiveData<CustomUser> authLiveData = new MutableLiveData<>();
  public MutableLiveData<CustomUser> mFirebaseUserMutableLiveData = new MutableLiveData<>();
  public MutableLiveData<Integer> mColorMutableLiveData = new MutableLiveData<>();

  /**
   * This méthod creates a new instance of the ViewModel.
   *
   * @param application Use getApplicationContext
   */

  public MainActivityViewModel(@NonNull Application application) {
    super(application);
    this.mApplication = application;
    this.mRepository = Repository.getInstance(application);
    mColorMutableLiveData.setValue(SharedPreferencesManager.getInstance(getApplication()).getActiveColor());
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

  public void setColor(int color){
    mColorMutableLiveData.setValue(color);
    SharedPreferencesManager.getInstance(getApplication()).setActiveColor(color);
  }

}