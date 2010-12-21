package com.google.health.examples.appengine.oauth;

@SuppressWarnings("serial")
public class AuthenticationException extends Exception {
  public AuthenticationException() {
    super();
  }

  public AuthenticationException(Exception e) {
    super(e);
  }
}
