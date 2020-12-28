package pt.rfernandes.bubbletweet.custom.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class UtilsClass {
  public enum SIDE {
    RIGHT,
    LEFT
  }

  private static UtilsClass instance;

  public static UtilsClass getInstance() {
    if (instance != null) {
      return instance;
    } else {
      return instance = new UtilsClass();
    }
  }

  public void setStatusBarDark(Activity activity, boolean isWhite) {
    setWindowFlag(activity, true);
    activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
    if (isWhite) {
      activity.getWindow().getDecorView().setSystemUiVisibility(0x00002000
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    } else {
      activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    setWindowFlag(activity, false);
  }

  private void setWindowFlag(Activity activity, boolean on) {
    Window win = activity.getWindow();
    WindowManager.LayoutParams winParams = win.getAttributes();
    if (on) {
      winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    } else {
      winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    }
    win.setAttributes(winParams);
  }

}
