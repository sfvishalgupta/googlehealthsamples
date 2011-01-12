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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.health.android.example.HealthClient;

public class HealthGDataClient implements HealthClient {
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

  public HealthGDataClient(HealthService service) {
    this.service = service;
  }

  public HealthGDataClient(String serviceName) {
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

    String data = retreiveData(service.getBaseURL() + "/profile/list");

    // Find the profile name/id pairs in the XML.
    Matcher matcher = PROFILE_PATTERN.matcher(data);

    while (matcher.find()) {
      // TODO Unescape XML escape sequences (e.g. &amp;).
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

    String url = service.getBaseURL() + "/profile/ui/" + profileId + "/-/labtest?digest=true";
    String data = retreiveData(url);

    CCRResultsHandler ccrHandler = new CCRResultsHandler();
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();

      XMLReader xr = sp.getXMLReader();
      xr.setContentHandler(ccrHandler);
      // TODO Parse stream... no need to buffer results
      xr.parse(new InputSource(new ByteArrayInputStream(data.getBytes())));
    } catch (ParserConfigurationException e) {
      return null;
    } catch (SAXException e) {
      return null;
    } catch (IOException e) {
      return null;
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

  private String retreiveData(String requestUrl) throws AuthenticationException,
      InvalidProfileException, ServiceException {

    BufferedReader reader = null;
    StringBuilder sb = new StringBuilder();

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpget = new HttpGet(requestUrl);
    httpget.addHeader("Authorization", "GoogleLogin auth=" + authToken);

    try {
      HttpResponse response = httpclient.execute(httpget);

      HttpEntity entity = response.getEntity();

      // Buffer the response.
      if (entity != null) {
        reader = new BufferedReader(new InputStreamReader(entity.getContent()));
        int read;
        char[] buff = new char[1024];
        while ((read = reader.read(buff)) != -1) {
          sb.append(buff, 0, read);
        }
      }

      int code = response.getStatusLine().getStatusCode();
      String message = response.getStatusLine().getReasonPhrase();
      switch (code) {
      case 401:
        throw new AuthenticationException(code, message, sb.toString());

      case 403:
        throw new InvalidProfileException();

      case 200:
      case 201:
        break;

      default:
        throw new ServiceException(code, message, sb.toString());
      }
    } catch (IOException e) {
      throw new ServiceException(e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          throw new ServiceException(e);
        }
      }
    }

    return sb.toString();
  }

  private String postData(String requestUrl, String atom) throws AuthenticationException,
      InvalidProfileException, ServiceException {

    BufferedReader reader = null;
    StringBuilder sb = new StringBuilder();

    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost = new HttpPost(requestUrl);
    httppost.setHeader("Authorization", "GoogleLogin auth=" + authToken);
    httppost.setHeader("Content-Type", "application/atom+xml");

    try {
      httppost.setEntity(new StringEntity(atom));

      // TODO The following is repeated code (retrieveData)... refactor
      HttpResponse response = httpclient.execute(httppost);
      HttpEntity entity = response.getEntity();

      // Buffer the response.
      if (entity != null) {
        reader = new BufferedReader(new InputStreamReader(entity.getContent()));
        int read;
        char[] buff = new char[1024];
        while ((read = reader.read(buff)) != -1) {
          sb.append(buff, 0, read);
        }
      }

      int code = response.getStatusLine().getStatusCode();
      String message = response.getStatusLine().getReasonPhrase();
      switch (code) {
      case 401:
        throw new AuthenticationException(code, message, sb.toString());

      case 403:
        throw new InvalidProfileException();

      case 200:
      case 201:
        break;

      default:
        throw new ServiceException(code, message, sb.toString());
      }
    } catch (IOException e) {
      throw new ServiceException(e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          throw new ServiceException(e);
        }
      }
    }

    return sb.toString();
  }
}
