package com.google.health.android.example;

import java.util.List;
import java.util.Map;

import com.google.health.android.example.gdata.Result;

public interface HealthClient {
  public static final String HEALTH_SERVICE = "health";
  public static final String H9_SERVICE = "weaver";

  public abstract String getProfileId();

  public abstract void setProfileId(String profileId);

  public abstract String getAuthToken();

  public abstract void setAuthToken(String authToken);

  public abstract Map<String, String> retrieveProfiles() throws AuthenticationException,
      InvalidProfileException, ServiceException;

  public abstract List<Result> retrieveResults() throws AuthenticationException,
      InvalidProfileException, ServiceException;

  public abstract Result createResult(Result result) throws AuthenticationException,
      InvalidProfileException, ServiceException;

  @SuppressWarnings("serial")
  public class AuthenticationException extends ServiceException {
    public AuthenticationException(int code, String message, String content) {
      super(code, message, content);
    }
  }

  @SuppressWarnings("serial")
  public class InvalidProfileException extends Exception {
  }

  @SuppressWarnings("serial")
  public class ServiceException extends Exception {
    private int code;
    private String message;
    private String content;

    public ServiceException(int code, String message, String content) {
      this.code = code;
      this.message = message;
      this.content = content;
    }

    public int getCode() {
      return code;
    }

    public String getMessage() {
      return message;
    }

    public String getContent() {
      return content;
    }
  }
}
