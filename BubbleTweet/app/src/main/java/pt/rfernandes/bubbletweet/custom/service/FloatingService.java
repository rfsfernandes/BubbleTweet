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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.Nullable;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.data.local.SharedPreferencesManager;
import pt.rfernandes.bubbletweet.data.remote.RequestCallBack;
import pt.rfernandes.bubbletweet.model.TweetBody;
import pt.rfernandes.bubbletweet.ui.activities.MainActivity;
import pt.rfernandes.bubbletweet.ui.goodies.GoodiesActivity;

public class FloatingService extends Service implements
    View.OnTouchListener, View.OnFocusChangeListener {
  private static final int MAX_CLICK_DURATION = 200;
  private static final int MAX_CLICK_DISTANCE = 15;
  private Repository mRepository;
  private UtilsClass.SIDE mSIDE = UtilsClass.SIDE.RIGHT;
  private long startClickTime;
  private WindowManager mWindowManager;
  private View mFloatingView;
  private ImageButton mainButton;
  private ImageButton imageButtonCancelLeft;
  private ImageButton imageButtonCancelRight;
  private ImageButton imageButtonDefsLeft;
  private ImageButton imageButtonDefsRight;
  private LinearLayout showLinLeft;
  private LinearLayout showLinRight;
  private TextView textViewUserAtRight;
  private TextView textViewUserAtLeft;
  private TextInputEditText editTextTextRight;
  private TextInputEditText editTextTextLeft;
  private Button buttonLeft;
  private Button buttonRight;
  private RelativeLayout r1;
  private WindowManager.LayoutParams params;
  private int width = 0;
  private int height = 0;
  private int initialX = 0;
  private int initialY = 0;
  private float initialTouchX = 0;
  private float initialTouchY = 0;
  private boolean wasOpen = false;
  private float pressedX;
  private float pressedY;

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
    height = size.y;

    //Inflate the floating view layout we created
    mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
    r1 = mFloatingView.findViewById(R.id.r1);

    //Add the view to the window
    mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    mWindowManager.addView(mFloatingView, getParams());

    mainButton = (ImageButton) mFloatingView.findViewById(R.id.mainButton);
    buttonLeft = (Button) mFloatingView.findViewById(R.id.buttonLeft);
    buttonRight = (Button) mFloatingView.findViewById(R.id.buttonRight);
    showLinLeft = (LinearLayout) mFloatingView.findViewById(R.id.showLinLeft);
    showLinRight = (LinearLayout) mFloatingView.findViewById(R.id.showLinRight);
    editTextTextRight = (TextInputEditText) mFloatingView.findViewById(R.id.editTextTextRight);
    editTextTextLeft = (TextInputEditText) mFloatingView.findViewById(R.id.editTextTextLeft);
    imageButtonCancelLeft = (ImageButton) mFloatingView.findViewById(R.id.imageButtonCancelLeft);
    imageButtonCancelRight = (ImageButton) mFloatingView.findViewById(R.id.imageButtonCancelRight);
    imageButtonDefsLeft = (ImageButton) mFloatingView.findViewById(R.id.imageButtonDefsLeft);
    imageButtonDefsRight = (ImageButton) mFloatingView.findViewById(R.id.imageButtonDefsRight);
    textViewUserAtRight = (TextView) mFloatingView.findViewById(R.id.textViewUserAtRight);
    textViewUserAtLeft = (TextView) mFloatingView.findViewById(R.id.textViewUserAtLeft);

    mRepository.getUserLoggedIn(user -> {
      textViewUserAtLeft.setText(String.format("%s @%s", getString(R.string.as_at) ,user.getUsername()));
      textViewUserAtRight.setText(String.format("%s @%s", getString(R.string.as_at),user.getUsername()));
    });

    editTextTextRight.setOnFocusChangeListener(this);
    editTextTextLeft.setOnFocusChangeListener(this);
//    editTextTextRight.setOnTouchListener(editTextTouchListener);
//    editTextTextLeft.setOnTouchListener(editTextTouchListener);

    imageButtonDefsLeft.setOnClickListener(defsClick);
    imageButtonDefsRight.setOnClickListener(defsClick);

    imageButtonCancelRight.setOnClickListener(cancelClick);
    imageButtonCancelLeft.setOnClickListener(cancelClick);

    mainButton.setOnClickListener(mainButtonClick);

    buttonLeft.setOnClickListener(sendTweetClick);
    buttonRight.setOnClickListener(sendTweetClick);
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

  private final View.OnTouchListener editTextTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_UP) {
        enableKeyboard();
      }
      if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
        v.performClick();
      }
      return false;
    }
  };

  private final View.OnClickListener mainButtonClick = v -> toggleTweetWindow();

  private final View.OnClickListener sendTweetClick = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
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
        if(Constants.ADS_DEBUGGER){
          startAds();
        } else {
          if (tweetContent.length() > 280) {
            showMessage(getString(R.string.tweet_too_big));
            disableKeyboard();
          } else {
            if (SharedPreferencesManager.getInstance(getApplication()).getAvailableTokens() == 0) {
              startAds();
            } else {
              sendTweet(tweetContent);
            }
          }
        }

      }
    }
  };

  private void startAds(){
    Intent intent = new Intent(FloatingService.this, GoodiesActivity.class);
    startActivity(intent);
    FloatingService.this.stopSelf();
  }

  private final View.OnClickListener cancelClick = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      editTextTextLeft.setText("");
      editTextTextRight.setText("");
      toggleTweetWindow();
    }
  };

  private final View.OnClickListener defsClick = v -> {
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(intent);
    toggleTweetWindow();
    FloatingService.this.stopSelf();
  };


  private void toggleTweetWindow() {

    switch (mSIDE) {
      case LEFT:
        showLinRight.setVisibility(View.GONE);
        showLinLeft.setVisibility(showLinLeft.getVisibility() == View.VISIBLE ? View.GONE :
            View.VISIBLE);
        wasOpen = showLinLeft.getVisibility() == View.VISIBLE;
        break;
      case RIGHT:
        showLinLeft.setVisibility(View.GONE);
        showLinRight.setVisibility(showLinRight.getVisibility() == View.VISIBLE ? View.GONE :
            View.VISIBLE);
        wasOpen = showLinRight.getVisibility() == View.VISIBLE;
        break;
    }

  }

  private void sendTweet(String status) {
    mRepository.getUserLoggedIn(object -> {

      TweetBody tweetBody =
          new TweetBody(Constants.KEY,
              Constants.SECRET,
              object.getToken(), status, object.getUserSecret());

      mRepository.sendTweet(tweetBody, new RequestCallBack() {
        @Override
        public void success() {
          int availableTokens =
              SharedPreferencesManager.getInstance(getApplication()).getAvailableTokens();
          SharedPreferencesManager.getInstance(getApplication()).setTokenKey(availableTokens - 1);
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
          new Handler(getMainLooper()).post(() -> showMessage(error));
        }
      });
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
      case MotionEvent.ACTION_BUTTON_PRESS:
        int n = 0;
        break;
      case MotionEvent.ACTION_DOWN:
        startClickTime = System.currentTimeMillis();
        //remember the initial position.
        initialX = params.x;
        initialY = params.y;
        pressedX = motionEvent.getX();
        pressedY = motionEvent.getY();
        //get the touch location
        initialTouchX = view.getX() - motionEvent.getRawX();
        initialTouchY = view.getY() - motionEvent.getRawY();
        return true;
      case MotionEvent.ACTION_MOVE:
//        //Calculate the X and Y coordinates of the view.
        params.x = (int) (motionEvent.getRawX() - initialTouchX) - initialX - view.getWidth();
        params.y = (int) (motionEvent.getRawY() - initialTouchY) - initialY - view.getHeight() * 2;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //Update the layout with new X & Y coordinate
        mWindowManager.updateViewLayout(mFloatingView, params);

        showLinRight.setVisibility(View.GONE);
        showLinLeft.setVisibility(View.GONE);

        return true;
      case MotionEvent.ACTION_UP:
        long pressDuration = System.currentTimeMillis() - startClickTime;

        if (pressDuration < MAX_CLICK_DURATION && distance(pressedX, pressedY, motionEvent.getX(), motionEvent.getY()) < MAX_CLICK_DISTANCE) {
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
          params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
          mWindowManager.updateViewLayout(mFloatingView, params);

          if (wasOpen)
            toggleTweetWindow();

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
    return px / getApplicationContext().getResources().getDisplayMetrics().density;
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
    double heightPercentage = height * 0.15;
    if (params.y > (int) heightPercentage) {
      params.y = (int) heightPercentage;
    }
    mWindowManager.updateViewLayout(mFloatingView, params);
    UtilsClass.getInstance().openKeyboard(getApplication(), mFloatingView, true);

  }

  private void disableKeyboard() {
    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    mWindowManager.updateViewLayout(mFloatingView, params);
    UtilsClass.getInstance().openKeyboard(getApplication(), mFloatingView, false);
  }

}