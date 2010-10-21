package com.google.health.examples.appengine;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Profile {
  @SuppressWarnings("unused")
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private String email;
  
  @Persistent
  private String oauthTokenSecret;

  @Persistent
  private String oauthToken;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getOAuthTokenSecret() {
    return oauthTokenSecret;
  }

  public void setOAuthTokenSecret(String oauthTokenSecret) {
    this.oauthTokenSecret = oauthTokenSecret;
  }

  public String getOAuthToken() {
    return oauthToken;
  }

  public void setOAuthToken(String oauthToken) {
    this.oauthToken = oauthToken;
  }
}
