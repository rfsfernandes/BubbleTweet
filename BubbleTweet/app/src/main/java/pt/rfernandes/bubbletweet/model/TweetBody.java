package pt.rfernandes.bubbletweet.model;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Random;
import java.util.UUID;

import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.custom.utils.UtilsClass;

public class TweetBody {
  private String oauth_callback = "https://bubbletweet.firebaseapp.com/__/auth/handler";
  private String oauth_consumer_key;
  private String oauth_nonce;
  private String oauth_signature;
  private String oauth_signature_method = "HMAC-SHA1";
  private String oauth_timestamp;
  private String oauth_token;
  private String oauth_version = "1.0";
  private String secret;
  private String status;


  public TweetBody(String oauth_consumer_key, String oauth_consumer_secret,
                   String oauth_token,
                   String status, String secret) {
    this.oauth_consumer_key = oauth_consumer_key;
//    this.oauth_nonce = UUID.randomUUID().toString().replaceAll("-", "");
    this.oauth_nonce = getNonce();
    this.oauth_token = oauth_token;
    this.status = status;
    this.oauth_timestamp = (Long.valueOf(System.currentTimeMillis()/1000)).toString();
    this.secret = secret;
    String parameter_string =
        "status=" + status + "&oauth_consumer_key=" + oauth_consumer_key +
            "&oauth_nonce=" + oauth_nonce + "&oauth_signature_method=" + oauth_signature_method +
        "&oauth_timestamp=" + oauth_timestamp + "&oauth_token=" + UtilsClass.getInstance().encode(oauth_token) + "&oauth_version=1.0";

    String twitter_endpoint = "https://api.twitter.com/1.1/statuses/update.json";
    String signature_base_string =
        "post" + "&" + UtilsClass.getInstance().encode(twitter_endpoint) + "&" + UtilsClass.getInstance().encode(parameter_string);

    try {
      oauth_signature = UtilsClass.getInstance().computeSignature(signature_base_string,
          this.oauth_consumer_key + "&" + UtilsClass.getInstance().encode(oauth_consumer_secret));  // note
      // the & at
      // the end. Normally the user access_token would go here, but we don't know it yet for request_token
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

  }

  private String getNonce() {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    Random random = new Random();

    String generatedString = random.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    return generatedString;

  }

  public String getSecret() {
    return secret;
  }

  public String getOauth_callback() {
    return oauth_callback;
  }

  public void setOauth_callback(String oauth_callback) {
    this.oauth_callback = oauth_callback;
  }

  public String getOauth_consumer_key() {
    return oauth_consumer_key;
  }

  public void setOauth_consumer_key(String oauth_consumer_key) {
    this.oauth_consumer_key = oauth_consumer_key;
  }

  public String getOauth_nonce() {
    return oauth_nonce;
  }

  public void setOauth_nonce(String oauth_nonce) {
    this.oauth_nonce = oauth_nonce;
  }

  public String getOauth_signature() {
    return oauth_signature;
  }

  public void setOauth_signature(String oauth_signature) {
    this.oauth_signature = oauth_signature;
  }

  public String getOauth_signature_method() {
    return oauth_signature_method;
  }

  public void setOauth_signature_method(String oauth_signature_method) {
    this.oauth_signature_method = oauth_signature_method;
  }

  public String getOauth_timestamp() {
    return oauth_timestamp;
  }

  public void setOauth_timestamp(String oauth_timestamp) {
    this.oauth_timestamp = oauth_timestamp;
  }

  public String getOauth_token() {
    return oauth_token;
  }

  public void setOauth_token(String oauth_token) {
    this.oauth_token = oauth_token;
  }

  public String getOauth_version() {
    return oauth_version;
  }

  public void setOauth_version(String oauth_version) {
    this.oauth_version = oauth_version;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
