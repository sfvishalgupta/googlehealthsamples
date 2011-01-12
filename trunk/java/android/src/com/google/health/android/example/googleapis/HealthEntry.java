package com.google.health.android.example.googleapis;

import java.io.IOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

public class HealthEntry extends Entry {

  @Key("ContinuityOfCareRecord")
  public ContinuityOfCareRecord ccr;

  @Override
  public HealthEntry clone() {
    return (HealthEntry) super.clone();
  }

  @Override
  public HealthEntry executeInsert(HttpTransport transport, HealthUrl url) throws IOException {
    return (HealthEntry) super.executeInsert(transport, url);
  }
}
