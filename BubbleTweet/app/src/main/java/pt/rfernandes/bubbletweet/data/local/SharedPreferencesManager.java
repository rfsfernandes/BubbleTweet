package pt.rfernandes.bubbletweet.data.local;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.TwitterSession;

import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.model.CustomUser;

import static pt.rfernandes.bubbletweet.custom.Constants.TWEET_SPAN_TILL_AD;

public class SharedPreferencesManager {
  private static final String SHARED_PREFS = "SHARED_PREFS";
  private static final String TOKEN_KEY = "TOKEN:KEY";
  private static final String TWEET_ENDING_KEY = "TWEET_ENDING:KEY";
  private static final String ACTIVE_COLOR = "ACTIVE_COLOR";
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
    return sharedPrefs.getInt(TOKEN_KEY, TWEET_SPAN_TILL_AD);
  }

  public void setTweetEndingValue(boolean checked){
    if (sharedPrefs == null || sharedPrefsEditor == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
      sharedPrefsEditor = sharedPrefs.edit();

    }
    sharedPrefsEditor.putBoolean(TWEET_ENDING_KEY, checked);
    sharedPrefsEditor.commit();
  }

  public boolean getTweetEndingValue() {
    if (sharedPrefs == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }
    return sharedPrefs.getBoolean(TWEET_ENDING_KEY, true);
  }


  public void setActiveColor(int color){
    if (sharedPrefs == null || sharedPrefsEditor == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
      sharedPrefsEditor = sharedPrefs.edit();

    }
    sharedPrefsEditor.putInt(ACTIVE_COLOR, color);
    sharedPrefsEditor.apply();
  }

  public int getActiveColor() {
    if (sharedPrefs == null) {
      sharedPrefs = mApplication.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }
    return sharedPrefs.getInt(ACTIVE_COLOR, mApplication.getColor(R.color.colorAccent));
  }

}
