package pt.rfernandes.bubbletweet.data.local;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import pt.rfernandes.bubbletweet.model.CustomUser;

public class SharedPreferencesManager {
  private static final String SHARED_PREFS = "SHARED_PREFS";
  private static final String USER_KEY = "USER:KEY";
  private static SharedPreferencesManager instance;
  private static SharedPreferences sharedPrefs;
  private static SharedPreferences.Editor sharedPrefsEditor;

  private static Application mApplication;
  /**
   * Creates the PreferencesManager singleton
   *
   * @return SharedPreferences instance, whether it's a new one or the one that already exists.
   */
  public static SharedPreferencesManager getInstance(Application application) {
    mApplication = application;
    if (instance != null) {
      return instance;
    } else {
      instance = new SharedPreferencesManager();
      return instance;
    }
  }

  public void setUserLoggedIn(FirebaseUser firebaseUser){
    CustomUser customUser = new CustomUser("", firebaseUser.getDisplayName(),
        firebaseUser.getEmail(), firebaseUser.getPhotoUrl(), "", firebaseUser.getUid(),
        firebaseUser.getProviderId())
    String user = new Gson().toJson(firebaseUser);

    if (sharedPrefs == null || sharedPrefsEditor == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
      sharedPrefsEditor = sharedPrefs.edit();

    }
    sharedPrefsEditor.putString(USER_KEY, user);
    sharedPrefsEditor.apply();
  }

  public CustomUser getUserLoggedIn() {
    if (sharedPrefs == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }
    return new Gson().fromJson(sharedPrefs.getString(USER_KEY, ""), CustomUser.class);
  }

}
