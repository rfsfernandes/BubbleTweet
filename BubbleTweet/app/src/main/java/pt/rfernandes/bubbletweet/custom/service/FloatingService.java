package pt.rfernandes.bubbletweet.custom.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;

public class FloatingService extends Service implements View.OnClickListener, View.OnTouchListener {
  private static final int MAX_CLICK_DURATION = 200;
  private static final int MAX_CLICK_DISTANCE = 15;
  private UtilsClass.SIDE mSIDE = UtilsClass.SIDE.RIGHT;
  private long startClickTime;
  private WindowManager mWindowManager;
  private View mFloatingView;
  private ImageView mainButton;
  private LinearLayout showLinLeft;
  private LinearLayout showLinRight;
  private Button button;
  private RelativeLayout r1;
  private WindowManager.LayoutParams params;
  private int width = 0;

  int initialX = 0;
  int initialY = 0;
  float initialTouchX = 0;
  float initialTouchY = 0;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    Display display = window.getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    width = size.x;


    //Inflate the floating view layout we created
    mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
    r1 = mFloatingView.findViewById(R.id.r1);
    //Add the view to the window.

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      params = new WindowManager.LayoutParams(
          WindowManager.LayoutParams.WRAP_CONTENT,
          WindowManager.LayoutParams.WRAP_CONTENT,
          WindowManager.LayoutParams.TYPE_PHONE,
          WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
          PixelFormat.TRANSLUCENT);


    } else {
      params = new WindowManager.LayoutParams(
          WindowManager.LayoutParams.WRAP_CONTENT,
          WindowManager.LayoutParams.WRAP_CONTENT,
          WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
          WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
          PixelFormat.TRANSLUCENT);
    }
    params.gravity = Gravity.START | Gravity.TOP;

    // Set the position to the top right corner of the screen
    params.gravity = Gravity.TOP | Gravity.LEFT;
    params.x = width - 50;
    params.y = 50;

    //Add the view to the window
    mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    mWindowManager.addView(mFloatingView, params);

    mainButton = (ImageView) mFloatingView.findViewById(R.id.mainButton);
    button = (Button) mFloatingView.findViewById(R.id.buttonLeft);
    showLinLeft = (LinearLayout) mFloatingView.findViewById(R.id.showLinLeft);
    showLinRight = (LinearLayout) mFloatingView.findViewById(R.id.showLinRight);

    mainButton.setOnClickListener(this);
    button.setOnClickListener(this);
//    btnClose.setOnClickListener(this);

    //Drag and move floating view using user's touch action.
    mainButton.setOnTouchListener(this);
  }

  @Override
  public void onClick(View view) {
    if (view == mainButton) {
      switch (mSIDE){
        case LEFT:
          showLinRight.setVisibility(View.GONE);
          showLinLeft.setVisibility(showLinLeft.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
          break;
        case RIGHT:
          showLinLeft.setVisibility(View.GONE);
          showLinRight.setVisibility(showLinRight.getVisibility() == View.VISIBLE ? View.GONE :
              View.VISIBLE);
          break;
      }

    }

    if (view == button) {
//      Intent intent = new Intent(FloatingService.this, MainActivity.class);
//      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//      startActivity(intent);
//
//      // Stop the service and Remove the Floating Button when our app opens...
//      stopSelf();
      showMessage("Tweet sent");
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (null != mFloatingView && null != mWindowManager)
      mWindowManager.removeView(mFloatingView);
  }

  // Shows message to the user...
  void showMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {

    switch (motionEvent.getAction()) {
      case MotionEvent.ACTION_DOWN:
        startClickTime = System.currentTimeMillis();
        //remember the initial position.
        initialX = params.x;
        initialY = params.y;

        //get the touch location
        initialTouchX = view.getX() - motionEvent.getRawX();
//        Toast.makeText(this, "X: " + view.getX() + " " + "Y: " + view.getY(), Toast.LENGTH_LONG).show();
        initialTouchY = view.getY() - motionEvent.getRawY();
        return true;
      case MotionEvent.ACTION_MOVE:
//        //Calculate the X and Y coordinates of the view.
        params.x = (int) (motionEvent.getRawX() - initialTouchX) - initialX - view.getWidth();
        params.y = (int) (motionEvent.getRawY() - initialTouchY) - initialY - view.getHeight() * 2;

        //Update the layout with new X & Y coordinate
        mWindowManager.updateViewLayout(mFloatingView, params);

        return true;
      case MotionEvent.ACTION_UP:
        long pressDuration = System.currentTimeMillis() - startClickTime;
        if (pressDuration < MAX_CLICK_DURATION) {
          // Click event has occurred
          view.performClick();
        } else {
          Toast.makeText(this, "x: " + params.x + " width/2: " + (width / 2),
              Toast.LENGTH_SHORT).show();

          RelativeLayout.LayoutParams lp =
              new RelativeLayout.LayoutParams((RelativeLayout.LayoutParams)r1.getLayoutParams());
          if (params.x < (width / 2)) {
            mSIDE = UtilsClass.SIDE.LEFT;
            params.x = 0;
            lp.removeRule(RelativeLayout.ALIGN_PARENT_END);
            lp.addRule(RelativeLayout.ALIGN_PARENT_START);
          } else if (params.x > (width / 2)) {
            params.x = width;
            mSIDE = UtilsClass.SIDE.RIGHT;
            lp.removeRule(RelativeLayout.ALIGN_PARENT_START);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);
          }
          r1.setLayoutParams(lp);

          mWindowManager.updateViewLayout(mFloatingView, params);
        }
        return true;

    }
    return false;
  }

  private float distance(float x1, float y1, float x2, float y2) {
    float dx = x1 - x2;
    float dy = y1 - y2;
    float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
    return pxToDp(distanceInPx);
  }

  private float pxToDp(float px) {
    return px / getResources().getDisplayMetrics().density;
  }

}