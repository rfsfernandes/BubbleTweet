package pt.rfernandes.bubbletweet.data.local;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.TwitterSession;

import pt.rfernandes.bubbletweet.model.CustomUser;

public class SharedPreferencesManager {
  private static final String SHARED_PREFS = "SHARED_PREFS";
  private static final String TOKEN_KEY = "TOKEN:KEY";
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

  public void setTokenKey(int tokens){
    if (sharedPrefs == null || sharedPrefsEditor == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
      sharedPrefsEditor = sharedPrefs.edit();

    }
    sharedPrefsEditor.putInt(TOKEN_KEY, tokens);
    sharedPrefsEditor.apply();
  }

  public int getAvailableTokens() {
    if (sharedPrefs == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }
    return sharedPrefs.getInt(TOKEN_KEY, 3);
  }

}
