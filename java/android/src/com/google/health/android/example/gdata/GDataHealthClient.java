/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.health.android.example.gdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class GDataHealthClient implements HealthClient {
  private HealthService service;
  private String profileId;
  private String authToken;

  public enum HealthService {
    HEALTH("health", "https://www.google.com/health/feeds"), H9("weaver",
        "https://www.google.com/h9/feeds");

    private String name;
    private String baseUrl;

    private HealthService(String name, String baseUrl) {
      this.name = name;
      this.baseUrl = baseUrl;
    }

    public String getBaseURL() {
      return baseUrl;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Matches the profile name and id in the Atom results from the Health profile
   * feed.
   */
  static final Pattern PROFILE_PATTERN = Pattern
      .compile("<title type='text'>([^<]*)</title><content type='text'>([\\w\\.]*)</content>");

  static final String ATOM_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
      + "<entry xmlns=\"http://www.w3.org/2005/Atom\">";
  static final String ATOM_FOOTER = "</entry>";

  /** Params: title, content */
  static final String NOTICE = "<title type=\"text\">%s</title><content type=\"text\">%s</content>";

  static final String CCR_HEADER = "<ContinuityOfCareRecord xmlns=\"urn:astm-org:CCR\">";
  static final String CCR_FOOTER = "</ContinuityOfCareRecord>";

  public GDataHealthClient(HealthService service) {
    this.service = service;
  }

  public GDataHealthClient(String serviceName) {
    if (HealthService.H9.getName().equals(serviceName)) {
      this.service = HealthService.H9;
    } else if (HealthService.HEALTH.getName().equals(serviceName)) {
      this.service = HealthService.HEALTH;
    } else {
      throw new IllegalArgumentException("Invalid service name. Expecting 'weaver' or 'health'.");
    }
  }

  @Override
  public String getProfileId() {
    return profileId;
  }

  @Override
  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  @Override
  public String getAuthToken() {
    return authToken;
  }

  @Override
  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  @Override
  public Map<String, String> retrieveProfiles() throws AuthenticationException,
      InvalidProfileException, ServiceException {

    if (authToken == null) {
      throw new IllegalStateException("authToken must not be null.");
    }

    Map<String, String> profiles = new LinkedHashMap<String, String>();
    String data;
    InputStream istream = null;

    try {
      istream = retreiveData(service.getBaseURL() + "/profile/list");
      data = bufferData(istream);
    } catch (IOException e) {
      throw new ServiceException(e);
    } finally {
      if (istream != null) {
        try {
          istream.close();
        } catch (IOException e) {
          throw new ServiceException(e);
        }
      }
    }

    // Find the profile name/id pairs in the XML.
    Matcher matcher = PROFILE_PATTERN.matcher(data);
    while (matcher.find()) {
      // TODO Un-escape XML escape sequences (e.g. &amp;).
      // e.g. commons-lang StringEscapeUtils.unescapeXml(matcher.group(1))
      profiles.put(matcher.group(2), matcher.group(1));
    }

    return profiles;
  }

  @Override
  public List<Result> retrieveResults() throws AuthenticationException, InvalidProfileException,
      ServiceException {

    if (authToken == null) {
      throw new IllegalStateException("authToken must not be null");
    }

    if (profileId == null) {
      throw new IllegalStateException("profileId must not be null.");
    }

    String url = service.getBaseURL() + "/profile/ui/" + profileId + "/-/labtest";
    InputStream istream = retreiveData(url);

    HealthGDataContentHandler ccrHandler = new HealthGDataContentHandler();
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();

      XMLReader xr = sp.getXMLReader();
      xr.setContentHandler(ccrHandler);
      xr.parse(new InputSource(istream));
    } catch (ParserConfigurationException e) {
      throw new ServiceException(e);
    } catch (SAXException e) {
      throw new ServiceException(e);
    } catch (IOException e) {
      throw new ServiceException(e);
    } finally {
      if (istream != null) {
        try {
          istream.close();
        } catch (IOException e) {
          throw new ServiceException(e);
        }
      }
    }

    return ccrHandler.getResults();
  }

  @Override
  public Result createResult(Result result) throws AuthenticationException,
      InvalidProfileException, ServiceException {

    if (authToken == null) {
      throw new IllegalStateException("authToken must not be null");
    }

    if (profileId == null) {
      throw new IllegalStateException("profileId must not be null.");
    }

    String ccr = CCR_HEADER + "<Body><Results>" + result.toCCR() + "</Results></Body>" + CCR_FOOTER;
    String notice = String.format(NOTICE, "Health Android Example App data posted",
        "The Health Android Example App posted the following data to your profile:");
    String atom = ATOM_HEADER + notice + ccr + ATOM_FOOTER;

    String url = service.getBaseURL() + "/register/ui/" + profileId;
    // TODO Parse and return results from service.
    postData(url, atom);

    return result;
  }

  @Override
  public void deleteResult(Result result) throws AuthenticationException, InvalidProfileException,
      ServiceException {

    if (authToken == null) {
      throw new IllegalStateException("authToken must not be null");
    }

    if (profileId == null) {
      throw new IllegalStateException("profileId must not be null.");
    }

    if (result.getId() == null) {
      throw new IllegalArgumentException("Result must have an id.");
    }

    String url = service.getBaseURL() + "/profile/ui/" + profileId + "/" + result.getId();

    HttpClient httpclient = new DefaultHttpClient();
    HttpDelete httpdelete = new HttpDelete(url);
    httpdelete.setHeader("Authorization", "GoogleLogin auth=" + authToken);
    httpdelete.setHeader("Content-Type", "application/atom+xml");

    try {
      getResponseStream(httpclient, httpdelete);
    } catch (ServiceException e) {
      // If the result has already been deleted, ignore exception.
      if (e.getCode() != 404) {
        throw e;
      }
    }

  }

  private InputStream retreiveData(String requestUrl) throws AuthenticationException,
      InvalidProfileException, ServiceException {

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpget = new HttpGet(requestUrl);
    httpget.addHeader("Authorization", "GoogleLogin auth=" + authToken);

    return getResponseStream(httpclient, httpget);
  }

  private InputStream postData(String requestUrl, String atom) throws AuthenticationException,
      InvalidProfileException, ServiceException {

    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost = new HttpPost(requestUrl);
    httppost.setHeader("Authorization", "GoogleLogin auth=" + authToken);
    httppost.setHeader("Content-Type", "application/atom+xml");

    try {
      httppost.setEntity(new StringEntity(atom));
    } catch (UnsupportedEncodingException e) {
      throw new ServiceException(e);
    }

    return getResponseStream(httpclient, httppost);
  }

  private InputStream getResponseStream(HttpClient client, HttpUriRequest request)
      throws AuthenticationException, InvalidProfileException, ServiceException {

    try {
      HttpResponse response = client.execute(request);
      HttpEntity entity = response.getEntity();

      int code = response.getStatusLine().getStatusCode();
      String message = response.getStatusLine().getReasonPhrase();

      switch (code) {
      case 200:
      case 201:
        break;

      case 401:
        throw new AuthenticationException(code, message, bufferData(entity.getContent()));

      case 403:
        throw new InvalidProfileException();

      case 404:
        // Not found... returned when a result is deleted, but it no longer exists.
      default:
        throw new ServiceException(code, message, bufferData(entity.getContent()));
      }

      return entity.getContent();
    } catch (IOException e) {
      throw new ServiceException(e);
    }
  }

  private String bufferData(InputStream istream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(istream));

    StringBuilder sb = new StringBuilder();
    int read;
    char[] buffer = new char[1024];

    try {
      while ((read = reader.read(buffer)) != -1) {
        sb.append(buffer, 0, read);
      }
    } finally {
      istream.close();
    }

    return sb.toString();
  }
}
