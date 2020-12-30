package pt.rfernandes.bubbletweet.custom;

import android.app.Application;

import com.bugsnag.android.Bugsnag;

public class MyApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Bugsnag.start(this);
  }
}
