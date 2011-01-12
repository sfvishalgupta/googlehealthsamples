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

package com.google.health.android.example.googleapis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.xml.atom.AtomParser;
import com.google.health.android.example.HealthClient;
import com.google.health.android.example.gdata.CCRResultsHandler;
import com.google.health.android.example.gdata.Result;

public class HealthGoogleApiClient implements HealthClient {
  private String profileId;
  private String authToken;

  private HttpTransport transport;

  /** Matches the profile name and id in the Atom results from the Health profile feed. */
  static final Pattern PROFILE_PATTERN = Pattern
      .compile("<title type='text'>([^<]*)</title><content type='text'>([\\w\\.]*)</content>");

  static final String ATOM_HEADER =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?><entry xmlns=\"http://www.w3.org/2005/Atom\">";
  static final String ATOM_FOOTER = "</entry>";

  /** Params: title, content */
  static final String NOTICE = "<title type=\"text\">%s</title><content type=\"text\">%s</content>";

  static final String CCR_HEADER = "<ContinuityOfCareRecord xmlns=\"urn:astm-org:CCR\">";
  static final String CCR_FOOTER = "</ContinuityOfCareRecord>";

  private HealthUrl url;

  public HealthGoogleApiClient(String applicationName, String serviceName) {
    if (applicationName == null) {
      throw new IllegalArgumentException("Appliation name cannot be null");
    }

    transport = GoogleTransport.create();
    GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
    headers.setApplicationName(applicationName);
    headers.gdataVersion = "2";
    AtomParser parser = new AtomParser();
    parser.namespaceDictionary = Namespace.DICTIONARY;
    transport.addParser(parser);

    if (HEALTH_SERVICE.equals(serviceName)) {
      this.url = new HealthUrl(HealthUrl.HEALTH_SERVICE_URL);
    } else if (H9_SERVICE.equals(serviceName)) {
      this.url = new HealthUrl(HealthUrl.H9_SERVICE_URL);
    } else {
      throw new IllegalArgumentException("Invalid service name. Expecting 'weaver' or 'health'.");
    }
  }

  public String getProfileId() {
    return profileId;
  }

  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
    ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
  }

  public Map<String, String> retrieveProfiles() throws AuthenticationException,
      InvalidProfileException {

    if (authToken == null) {
      throw new IllegalStateException("authToken must not be null.");
    }

    Map<String, String> profiles = new LinkedHashMap<String, String>();

    HealthFeed feed = null;
    try {
      feed = HealthFeed.executeGet(transport, url.profileListFeed());
    } catch (IOException e) {
      // TODO Handle exception
      e.printStackTrace();
    }

    if (feed.entries != null) {
      for (HealthEntry entry : feed.entries) {
        profiles.put(entry.content, entry.title);
      }
    }

    return profiles;
  }

  public List<Result> retrieveResults() throws AuthenticationException, InvalidProfileException {
    if (authToken == null) {
      throw new IllegalStateException("authToken must not be null");
    }

    if (profileId == null) {
      throw new IllegalStateException("profileId must not be null.");
    }

    HealthFeed feed = null;
    try {
      feed = HealthFeed.executeGet(transport, url.profileFeed(profileId));
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<Result> results = new LinkedList<Result>();

    if (feed.entries != null) {
      for (HealthEntry entry : feed.entries) {
        CCRResultsHandler ccrHandler = new CCRResultsHandler();
        try {
          SAXParserFactory spf = SAXParserFactory.newInstance();
          SAXParser sp = spf.newSAXParser();
          XMLReader xr = sp.getXMLReader();
          xr.setContentHandler(ccrHandler);
          xr.parse(new InputSource(new ByteArrayInputStream(entry.ccr.toString().getBytes())));
        } catch (ParserConfigurationException e) {
          return null;
        } catch (SAXException e) {
          return null;
        } catch (IOException e) {
          return null;
        }

        results.addAll(ccrHandler.getResults());
      }
    }

    return results;
  }

  public Result createResult(Result result) throws AuthenticationException,
      InvalidProfileException {

    if (authToken == null) {
      throw new IllegalStateException("authToken must not be null");
    }

    if (profileId == null) {
      throw new IllegalStateException("profileId must not be null.");
    }

    // XXX
    return new Result();
  }
}
