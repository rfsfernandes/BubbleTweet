package pt.rfernandes.bubbletweet.model;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;


@Entity
public class TweetCreds {
  @PrimaryKey
  @NonNull
  private String tweetConsumerKey;
  private String tweetConsumerSecret;
  private String password;

  public TweetCreds(@NonNull String tweetConsumerKey, String tweetConsumerSecret, String password) {
    this.password = password;
    try {
      IvParameterSpec ivParameterSpec = UtilsClass.getInstance().generateIv();
      SecretKey secretKey = UtilsClass.getInstance().generateKey(password, tweetConsumerKey);
      this.tweetConsumerKey = UtilsClass.getInstance().encrypt(tweetConsumerKey, secretKey, ivParameterSpec);
      this.tweetConsumerSecret = UtilsClass.getInstance().encrypt(tweetConsumerSecret, secretKey, ivParameterSpec);

    } catch (Exception e) {
      this.tweetConsumerKey = tweetConsumerKey;
      this.tweetConsumerSecret = tweetConsumerSecret;
      e.printStackTrace();
    }
    System.out.println(this.tweetConsumerKey + " " + this.tweetConsumerSecret);
  }

  public @NonNull String getTweetConsumerKey() {
    try {
      return UtilsClass.getInstance().decrypt(this.tweetConsumerKey,
          UtilsClass.getInstance().getsSecretKey(),UtilsClass.getInstance().getsIvParameterSpec());
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    }
    return this.tweetConsumerKey;
  }

  public String getTweetConsumerSecret() {

    try {
      return UtilsClass.getInstance().decrypt(this.tweetConsumerSecret,
          UtilsClass.getInstance().getsSecretKey(),UtilsClass.getInstance().getsIvParameterSpec());
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    }
    return this.tweetConsumerSecret;
  }

  public void setTweetConsumerKey(@NonNull String tweetConsumerKey) {
    this.tweetConsumerKey = tweetConsumerKey;
  }

  public void setTweetConsumerSecret(String tweetConsumerSecret) {
    this.tweetConsumerSecret = tweetConsumerSecret;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }



}
