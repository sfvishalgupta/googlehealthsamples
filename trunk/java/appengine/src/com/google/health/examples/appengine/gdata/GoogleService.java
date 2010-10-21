package com.google.health.examples.appengine.gdata;

import java.util.HashMap;
import java.util.Map;

public enum GoogleService {
  HEALTH("health", "https://www.google.com/health/feeds"),
  H9("weaver", "https://www.google.com/h9/feeds");
  
  private static Map<String, GoogleService> services = new HashMap<String, GoogleService>();
  
  static {
    for (GoogleService service : GoogleService.values()) {
      services.put(service.getServiceName(), service);
    }
  }
  
  private String name;
  private String baseUrl;

  private GoogleService(String name, String baseUrl) {
    this.name = name;
    this.baseUrl = baseUrl;
  }

  public String getBaseURL() {
    return baseUrl;
  }

  public String getServiceName() {
    return name;
  }
  
  public static GoogleService getServiceByName(String name) {
    return services.get(name);
  }
}
