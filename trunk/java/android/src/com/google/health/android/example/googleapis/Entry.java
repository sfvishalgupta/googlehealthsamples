package com.google.health.android.example.googleapis;

import java.io.IOException;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DataUtil;
import com.google.api.client.util.Key;
import com.google.api.client.xml.atom.AtomContent;

public class Entry implements Cloneable {

  @Key
  public String title;

  @Key
  public String content;

  @Override
  protected Entry clone() {
    return DataUtil.clone(this);
  }

  Entry executeInsert(HttpTransport transport, HealthUrl url)
      throws IOException {
    HttpRequest request = transport.buildPostRequest();
    request.url = url;
    AtomContent content = new AtomContent();
    content.namespaceDictionary = Namespace.DICTIONARY;
    content.entry = this;
    request.content = content;
    return request.execute().parseAs(getClass());
  }
}
