package com.google.health.android.example.googleapis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

public class HealthFeed extends Feed {

  @Key("entry")
  public List<HealthEntry> entries = new ArrayList<HealthEntry>();

  public static HealthFeed executeGet(HttpTransport transport, HealthUrl url) throws IOException {
    return (HealthFeed) Feed.executeGet(transport, url, HealthFeed.class);
  }
}
