package pt.rfernandes.bubbletweet.model;

import android.net.Uri;

public class CustomUser {
  private String userSecret;
  private String name;
  private String email;
  private Uri photoUrl;
  private String token;
  private String uid;
  private String providerId;


  public CustomUser() {
  }

  public CustomUser(String userSecret, String name, String email, Uri photoUrl, String token, String uid, String providerId) {
    this.userSecret = userSecret;
    this.name = name;
    this.email = email;
    this.photoUrl = photoUrl;
    this.token = token;
    this.uid = uid;
    this.providerId = providerId;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getUserSecret() {
    return userSecret;
  }

  public void setUserSecret(String userSecret) {
    this.userSecret = userSecret;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Uri getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(Uri photoUrl) {
    this.photoUrl = photoUrl;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
