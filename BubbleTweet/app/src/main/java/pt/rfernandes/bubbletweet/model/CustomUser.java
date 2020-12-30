package pt.rfernandes.bubbletweet.model;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CustomUser {
  @PrimaryKey
  private long id;
  private String userSecret;
  private String name;
  private String email;
  private String photoUrl;
  private String token;
  private String uid;
  private String providerId;
  private String username;

  public CustomUser() {
  }

  public CustomUser(long id, String username, String userSecret, String name, String email,
                    String photoUri,
                    String token,
                    String uid, String providerId) {
    this.id = id;
    this.username = username;
    this.userSecret = userSecret;
    this.name = name;
    this.email = email;
    this.photoUrl = photoUri;
    this.token = token;
    this.uid = uid;
    this.providerId = providerId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
