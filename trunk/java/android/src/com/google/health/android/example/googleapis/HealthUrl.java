package com.google.health.android.example.googleapis;

import com.google.api.client.googleapis.GoogleUrl;

public class HealthUrl extends GoogleUrl {

  public static final String H9_SERVICE_URL = "https://www.google.com/h9/feeds";
  public static final String HEALTH_SERVICE_URL = "https://www.google.com/health/feeds";

  public HealthUrl(String url) {
    super(url);
  }

  private HealthUrl root() {
    return (HealthUrl)super.clone();
  }

  public HealthUrl profileFeed() {
    HealthUrl result = root();
    result.pathParts.add("profile");
    return result;
  }

  public HealthUrl profileFeed(String pid) {
    HealthUrl result = profileFeed();
    result.pathParts.add("ui");
    result.pathParts.add(pid);
    return result;
  }

  public HealthUrl profileListFeed() {
    HealthUrl result = profileFeed();
    result.pathParts.add("list");
    return result;
  }

  public HealthUrl registerFeed() {
    HealthUrl result = root();
    result.pathParts.add("register");
    return result;
  }

  public HealthUrl registerFeed(String pid) {
    HealthUrl result = registerFeed();
    result.pathParts.add("ui");
    result.pathParts.add(pid);
    return result;
  }
}
