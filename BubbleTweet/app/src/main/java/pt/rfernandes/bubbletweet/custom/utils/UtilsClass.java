package pt.rfernandes.bubbletweet.custom.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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

  public void openKeyboard(Application application, View view, boolean show){
    InputMethodManager inputMethodManager =
        (InputMethodManager)application.getSystemService(Context.INPUT_METHOD_SERVICE);

    if(show) {
      inputMethodManager.showSoftInputFromInputMethod(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED);
    } else {
      inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED);
    }
  }

  public String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException
  {
    SecretKey secretKey = null;

    byte[] keyBytes = keyString.getBytes();
    secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(secretKey);

    byte[] text = baseString.getBytes();

    return new String(Base64.getEncoder().encode(mac.doFinal(text))).trim();
  }

  public String encode(String value)
  {
    String encoded = null;
    try {
      encoded = URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException ignore) {
    }
    StringBuilder buf = new StringBuilder(encoded.length());
    char focus;
    for (int i = 0; i < encoded.length(); i++) {
      focus = encoded.charAt(i);
      if (focus == '*') {
        buf.append("%2A");
      } else if (focus == '+') {
        buf.append("%20");
      } else if (focus == '%' && (i + 1) < encoded.length()
          && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
        buf.append('~');
        i += 2;
      } else {
        buf.append(focus);
      }
    }
    return buf.toString();
  }

  public static final String md5(final String s) {
    try {
      // Create MD5 Hash
      MessageDigest digest = java.security.MessageDigest
          .getInstance("MD5");
      digest.update(s.getBytes());
      byte messageDigest[] = digest.digest();

      // Create Hex String
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < messageDigest.length; i++) {
        String h = Integer.toHexString(0xFF & messageDigest[i]);
        while (h.length() < 2)
          h = "0" + h;
        hexString.append(h);
      }
      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return "";
  }

  private SecretKey sSecretKey;
  private IvParameterSpec sIvParameterSpec;

  public SecretKey getsSecretKey() {
    return sSecretKey;
  }

  public IvParameterSpec getsIvParameterSpec() {
    return sIvParameterSpec;
  }

  public SecretKey generateKey(String password, String salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    if(sSecretKey != null) {
      return sSecretKey;
    }
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 500, 256);
    SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
        .getEncoded(), "AES");
    sSecretKey = secret;
    return secret;
  }

  public IvParameterSpec generateIv() {
    if(sIvParameterSpec != null) {
      return sIvParameterSpec;
    }
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    sIvParameterSpec = new IvParameterSpec(iv);
    return sIvParameterSpec;
  }

  public String encrypt(String input, SecretKey key,
                         IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
      InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    byte[] cipherText = cipher.doFinal(input.getBytes());
    return Base64.getEncoder()
        .encodeToString(cipherText);
  }

  public String decrypt(String cipherText, SecretKey key,
                         IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
      InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, key, iv);
    byte[] plainText = cipher.doFinal(Base64.getDecoder()
        .decode(cipherText));
    return new String(plainText);
  }

}
