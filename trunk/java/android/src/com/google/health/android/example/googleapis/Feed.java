package com.google.health.android.example.googleapis;

import java.io.IOException;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;

public class Feed {

  static Feed executeGet(HttpTransport transport, HealthUrl url, Class<? extends Feed> feedClass)
      throws IOException {
    HttpRequest request = transport.buildGetRequest();
    request.url = url;
    return request.execute().parseAs(feedClass);
  }
}
