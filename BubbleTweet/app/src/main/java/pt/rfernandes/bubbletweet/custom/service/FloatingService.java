package pt.rfernandes.bubbletweet.custom.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.data.local.DBCallBack;
import pt.rfernandes.bubbletweet.data.remote.RequestCallBack;
import pt.rfernandes.bubbletweet.model.CustomUser;
import pt.rfernandes.bubbletweet.model.TweetBody;

public class FloatingService extends Service implements View.OnClickListener,
    View.OnTouchListener, View.OnFocusChangeListener {
  private static final int MAX_CLICK_DURATION = 200;
  private static final int MAX_CLICK_DISTANCE = 15;
  private Repository mRepository;
  private UtilsClass.SIDE mSIDE = UtilsClass.SIDE.RIGHT;
  private long startClickTime;
  private WindowManager mWindowManager;
  private View mFloatingView;
  private ImageView mainButton;
  private LinearLayout showLinLeft;
  private LinearLayout showLinRight;
  private EditText editTextTextRight;
  private EditText editTextTextLeft;
  private Button buttonLeft;
  private Button buttonRight;
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
    mRepository = Repository.getInstance(getApplication());
    Display display = getDisplay();
    Point size = new Point();
    display.getSize(size);
    width = size.x;


    //Inflate the floating view layout we created
    mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
    r1 = mFloatingView.findViewById(R.id.r1);

    //Add the view to the window
    mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    mWindowManager.addView(mFloatingView, getParams());

    mainButton = (ImageView) mFloatingView.findViewById(R.id.mainButton);
    buttonLeft = (Button) mFloatingView.findViewById(R.id.buttonLeft);
    buttonRight = (Button) mFloatingView.findViewById(R.id.buttonRight);
    showLinLeft = (LinearLayout) mFloatingView.findViewById(R.id.showLinLeft);
    showLinRight = (LinearLayout) mFloatingView.findViewById(R.id.showLinRight);
    editTextTextRight = (EditText) mFloatingView.findViewById(R.id.editTextTextRight);
    editTextTextLeft = (EditText) mFloatingView.findViewById(R.id.editTextTextLeft);

    editTextTextRight.setOnFocusChangeListener(this);
    editTextTextLeft.setOnFocusChangeListener(this);

    mainButton.setOnClickListener(this);
    buttonLeft.setOnClickListener(this);
    buttonRight.setOnClickListener(this);
//    btnClose.setOnClickListener(this);

    //Drag and move floating view using user's touch action.
    mainButton.setOnTouchListener(this);
  }

  private WindowManager.LayoutParams getParams() {

    params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT);

    params.gravity = Gravity.START | Gravity.TOP;

    // Set the position to the top right corner of the screen
    params.gravity = Gravity.TOP | Gravity.LEFT;
    params.x = width - 50;
    params.y = 50;

    return params;
  }

  @Override
  public void onClick(View view) {
    if (view == mainButton) {
      switch (mSIDE) {
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

    } else if (view == buttonLeft || view == buttonRight) {
      String tweetContent = "";
      switch (mSIDE) {
        case LEFT:
          tweetContent = editTextTextLeft.getText().toString();
          break;
        case RIGHT:
          tweetContent = editTextTextRight.getText().toString();
          break;
      }

      if (tweetContent.isEmpty()) {

        showMessage(getString(R.string.empty_tweet));

      } else {
        sendTweet(tweetContent);

      }

    }
  }

  private void sendTweet(String status) {
    String finalTweetContent = status;
    mRepository.getUserLoggedIn(new DBCallBack<CustomUser>() {
      @Override
      public void returnDB(CustomUser object) {

        TweetBody tweetBody =
            new TweetBody(Constants.KEY,
                Constants.SECRET,
                object.getToken(), finalTweetContent, object.getUserSecret());

        mRepository.sendTweet(tweetBody, new RequestCallBack() {
          @Override
          public void success() {
            new Handler(getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                showMessage(getString(R.string.tweet_success));
              }
            });
          }

          @Override
          public void failure(String error) {
//            looper.
            new Handler(getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                showMessage(error);
              }
            });
          }
        });
      }
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (null != mFloatingView && null != mWindowManager)
      mWindowManager.removeView(mFloatingView);
  }

  // Shows message to the user...
  void showMessage(String message) {
    UtilsClass.getInstance().openKeyboard(getApplication(), mFloatingView, false);
    editTextTextRight.setText("");
    editTextTextLeft.setText("");
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

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

          RelativeLayout.LayoutParams lp =
              new RelativeLayout.LayoutParams((RelativeLayout.LayoutParams) r1.getLayoutParams());
          if (params.x < (width / 2)) {
            mSIDE = UtilsClass.SIDE.LEFT;
            params.x = 0;
            lp.removeRule(RelativeLayout.ALIGN_PARENT_END);
            lp.addRule(RelativeLayout.ALIGN_PARENT_START);
            editTextTextLeft.requestFocus();
          } else if (params.x > (width / 2)) {
            params.x = width;
            mSIDE = UtilsClass.SIDE.RIGHT;
            lp.removeRule(RelativeLayout.ALIGN_PARENT_START);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);
            editTextTextRight.requestFocus();
          }
          r1.setLayoutParams(lp);

          mWindowManager.updateViewLayout(mFloatingView, params);
        }
        return true;

    }
    return false;
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (hasFocus) {
      enableKeyboard();
    } else {
      disableKeyboard();
    }
  }

  private void enableKeyboard() {

    params.flags = 0;
    mWindowManager.updateViewLayout(mFloatingView, params);
    UtilsClass.getInstance().openKeyboard(getApplication(), mFloatingView, true);

  }

  private void disableKeyboard() {

    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    mWindowManager.updateViewLayout(mFloatingView, params);
    UtilsClass.getInstance().openKeyboard(getApplication(), mFloatingView, false);

  }

}