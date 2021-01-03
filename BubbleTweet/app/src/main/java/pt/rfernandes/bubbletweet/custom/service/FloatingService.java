package pt.rfernandes.bubbletweet.custom.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;
import pt.rfernandes.bubbletweet.data.Repository;
import pt.rfernandes.bubbletweet.data.local.DBCallBack;
import pt.rfernandes.bubbletweet.data.local.SharedPreferencesManager;
import pt.rfernandes.bubbletweet.data.remote.RequestCallBack;
import pt.rfernandes.bubbletweet.model.CustomUser;
import pt.rfernandes.bubbletweet.model.TweetBody;
import pt.rfernandes.bubbletweet.model.TweetCreds;
import pt.rfernandes.bubbletweet.ui.activities.MainActivity;
import pt.rfernandes.bubbletweet.ui.goodies.GoodiesActivity;

import static pt.rfernandes.bubbletweet.custom.Constants.MAX_LENGTH_MENTION;
import static pt.rfernandes.bubbletweet.custom.Constants.MAX_LENGTH_NO_MENTION;

public class FloatingService extends Service implements
    View.OnTouchListener {

  private final static float CLICK_DRAG_TOLERANCE = 35; // Often, there will be a slight,
  // unintentional, drag when the user taps the FAB, so we need to account for this.
  private float downRawX, downRawY;

  private Repository mRepository;
  private UtilsClass.SIDE mSIDE = UtilsClass.SIDE.RIGHT;
  private long startClickTime;
  private WindowManager mWindowManager;
  private View mFloatingView;
  private FloatingActionButton mainButton;
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
  private boolean showEnding = false;
  private ImageButton imageButtonDiscard;
  private TextInputLayout textInputLayoutLeft;
  private TextInputLayout textInputLayoutRight;
  private int maxTweetLength = 0;
  private Vibrator myVib;

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
    myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
    //Inflate the floating view layout we created
    mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
    r1 = mFloatingView.findViewById(R.id.r1);

    //Add the view to the window
    mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    mWindowManager.addView(mFloatingView, getParams());

    mainButton = (FloatingActionButton) mFloatingView.findViewById(R.id.mainButton);
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
    textInputLayoutRight = (TextInputLayout) mFloatingView.findViewById(R.id.textInputLayoutRight);
    textInputLayoutLeft = (TextInputLayout) mFloatingView.findViewById(R.id.textInputLayoutLeft);
    tintViews(SharedPreferencesManager.getInstance(getApplication()).getActiveColor());
//    imageButtonDiscard = (ImageButton) mFloatingView.findViewById(R.id.imageButtonDiscard);
    showEnding = SharedPreferencesManager.getInstance(getApplication()).getTweetEndingValue();

    maxTweetLength = showEnding ? MAX_LENGTH_MENTION : MAX_LENGTH_NO_MENTION;

    textInputLayoutLeft.setCounterMaxLength(maxTweetLength);
    textInputLayoutRight.setCounterMaxLength(maxTweetLength);

    mRepository.getUserLoggedIn(user -> {
      if(user != null) {
        textViewUserAtLeft.setText(String.format("%s @%s", getString(R.string.as_at), user.getUsername()));
        textViewUserAtRight.setText(String.format("%s @%s", getString(R.string.as_at), user.getUsername()));
      }

    });

    imageButtonDefsLeft.setOnClickListener(defsClick);
    imageButtonDefsRight.setOnClickListener(defsClick);

    imageButtonCancelRight.setOnClickListener(cancelClick);
    imageButtonCancelLeft.setOnClickListener(cancelClick);

    editTextTextLeft.setImeOptions(EditorInfo.IME_ACTION_SEND);
    editTextTextLeft.setRawInputType(InputType.TYPE_CLASS_TEXT);
    editTextTextRight.setImeOptions(EditorInfo.IME_ACTION_SEND);
    editTextTextRight.setRawInputType(InputType.TYPE_CLASS_TEXT);
    editTextTextLeft.setOnEditorActionListener(mOnEditorActionListener);
    editTextTextRight.setOnEditorActionListener(mOnEditorActionListener);

    mainButton.setOnClickListener(mainButtonClick);

    buttonLeft.setOnClickListener(sendTweetClick);
    buttonRight.setOnClickListener(sendTweetClick);

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

//    // Set the position to the top right corner of the screen
//    params.gravity = Gravity.TOP | Gravity.LEFT;
    params.x = width - 50;
    params.y = 50;

    return params;
  }

  private void tintViews(int color){
    int[][] states = new int[][] {
        new int[] { android.R.attr.state_enabled},
    };

    int[] colors = new int[] {
        color
    };

    ColorStateList myList = new ColorStateList(states, colors);
    mainButton.setBackgroundTintList(myList);
    buttonRight.getBackground().setTint(color);
    buttonLeft.getBackground().setTint(color);
    editTextTextRight.setHintTextColor(color);
    editTextTextLeft.setHintTextColor(color);
    imageButtonCancelLeft.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);
    imageButtonCancelRight.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);
    imageButtonDefsLeft.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);
    imageButtonDefsRight.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);
    textViewUserAtRight.setTextColor(color);
    textViewUserAtLeft.setTextColor(color);

    textInputLayoutRight.setHintTextColor(myList);
    textInputLayoutLeft.setHintTextColor(myList);
    textInputLayoutLeft.setCounterTextColor(myList);
    textInputLayoutRight.setCounterTextColor(myList);

  }

  private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if(actionId == EditorInfo.IME_ACTION_SEND) {
        sendTweetClick.onClick(v);
        return true;
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
        if (Constants.ADS_DEBUGGER) {
          startAds();
        } else {
          if (tweetContent.length() > maxTweetLength) {
            showMessage(getString(R.string.tweet_too_big, maxTweetLength));
            disableKeyboard();
          } else {
            if (SharedPreferencesManager.getInstance(getApplication()).getAvailableTokens() == 0) {
              startAds();
            } else {
              if (showEnding) {
                tweetContent =
                    tweetContent + getResources().getString(R.string.tweet_sent_from_bubble);
              }
              sendTweet(tweetContent);
            }
          }
        }

      }
    }
  };

  private void startAds() {
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
      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getApplicationContext())
          .setMessage(getResources().getString(R.string.discard_bubble_question))
          .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            FloatingService.this.stopSelf();
          })
          .setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
          });
      AlertDialog alertDialog = alertBuilder.create();
      alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
      alertDialog.show();
    }
  };

  private final View.OnClickListener defsClick = v -> {
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    if (wasOpen) {
      double heightPercentage = height * 0.15;
      if (params.y > (int) heightPercentage) {
        params.y = (int) heightPercentage;
      }
      mWindowManager.updateViewLayout(mFloatingView, params);
      enableKeyboard();
    } else {
      disableKeyboard();
    }

  }

  private void sendTweet(String status) {
    mRepository.getUserLoggedIn(object -> mRepository.getTweetCreds(credsObbject -> {
      TweetBody tweetBody =
          new TweetBody(credsObbject.getTweetConsumerKey(),
              credsObbject.getTweetConsumerSecret(),
              object.getToken(), status, object.getUserSecret());

      mRepository.sendTweet(tweetBody, new RequestCallBack() {
        @Override
        public void success() {
          int availableTokens =
              SharedPreferencesManager.getInstance(getApplication()).getAvailableTokens();
          SharedPreferencesManager.getInstance(getApplication()).setTokenKey(availableTokens - 1);
          new Handler(getMainLooper()).post(() -> showMessage(getString(R.string.tweet_success)));
        }

        @Override
        public void failure(String error) {
//            looper.
          new Handler(getMainLooper()).post(() -> showMessage(error));
        }
      });
    }));
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

        downRawX = motionEvent.getRawX();
        downRawY = motionEvent.getRawY();
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

        return true;
      case MotionEvent.ACTION_UP:

        float upRawX = motionEvent.getRawX();
        float upRawY = motionEvent.getRawY();

        float upDX = upRawX - downRawX;
        float upDY = upRawY - downRawY;

        if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
          // Click event has occurred
          view.performClick();
          myVib.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.EFFECT_TICK));
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

//  private boolean isViewOverlapping(View firstView, View secondView) {
//    if (firstView != null && secondView != null) {
//      int[] firstPosition = new int[2];
//      int[] secondPosition = new int[2];
//
//      firstView.getLocationOnScreen(firstPosition);
//      secondView.getLocationOnScreen(secondPosition);
//
//      // Rect constructor parameters: left, top, right, bottom
//      Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
//          firstPosition[0] + firstView.getMeasuredWidth(), firstPosition[1] + firstView.getMeasuredHeight());
//      Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
//          secondPosition[0] + secondView.getMeasuredWidth(), secondPosition[1] + secondView.getMeasuredHeight());
//      return rectFirstView.intersect(rectSecondView);
//
//    } else {
//
//      return false;
//    }
//
//  }

}