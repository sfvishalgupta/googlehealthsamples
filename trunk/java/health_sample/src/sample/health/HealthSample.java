/*
 * Copyright (c) 2007 Google Inc.
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
package sample.health;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextContent;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.XmlBlob;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Class wraps all operations necessary to interact with the Google Health API.
 * 
 * @author api.jfisher@google.com (Jeff Fisher)
 */
public class HealthSample {

  // the AuthSub token associated with this instance.
  private String authSubToken = null;

  /**
   * Retrieves the associated AuthSub token.
   * 
   * @return string representing an AuthSub token for the Health API
   */
  public String getAuthSubToken() {
    return this.authSubToken;
  }

  /**
   * Exchanges the single use token provided by the AuthSub redirect for a
   * session token
   * 
   * @param singleUseToken token query parameter
   * @return session token
   * @throws HealthSampleException
   */
  public String exchangeAuthSubToken(String singleUseToken)
      throws HealthSampleException {

    try {
      this.authSubToken = AuthSubUtil.exchangeForSessionToken(
          URLDecoder.decode(singleUseToken, "UTF-8"),
          null);
    } catch (Exception e) {
      throw new HealthSampleException(
          "Problem while exchanging AuthSub token.", e);
    } 
    
    return this.authSubToken;

  }

  /**
   * Sets the AuthSub token associated with this object.
   * 
   * @param token an AuthSub token string
   */
  public void setAuthSubToken(String token) {
    this.authSubToken = token;
  }

  /**
   * Requests a single use token that can be upgraded to a session token. Note
   * that it does not use request signing.
   * 
   * @param nextUrl The webpage that will receive the single use token
   * @return The URL to have the user visit in order to authenticate.
   */
  public static String getAuthSubUrl(String nextUrl) {

    String authSubLink = AuthSubUtil.getRequestUrl(nextUrl,
        "https://www.google.com/h9/feeds/", false, true);
    
    authSubLink += "&permission=1";

    // we have a custom AuthSub handler for Health.
    return authSubLink.replaceFirst("/accounts/AuthSubRequest", "/h9/authsub");

  }

  /**
   * Retrieves the profile associated with the current AuthSub token.
   * 
   * @return A string representing all the CCRg data in a profile.
   * @throws HealthSampleException
   */
  public String getProfile() throws HealthSampleException {
    if (this.authSubToken == null) {
      throw new HealthSampleException(
          "Need to supply a token before retrieving profile.");
    }

    GoogleService service = new GoogleService("weaver", "HealthSample");
    service.setAuthSubToken(this.authSubToken);
    Query query = null;
    try {
      query = new Query(new URL(
          "https://www.google.com/h9/feeds/profile/default?digest=true"));
    } catch (MalformedURLException e) {
      throw new HealthSampleException("Bad profile URL!", e);
    }

    try {
      Feed result = service.query(query, Feed.class);
      //there should be only one entry that contains
      //the CCRg document for the profile.
      for (Entry entry : result.getEntries()) {
        return prettifyXmlBlob(entry.getXmlBlob());
      }
    } catch (Exception e) {
      throw new HealthSampleException("Error retrieving profile", e);
    }

    // If we don't find any profile data, return nothing.
    // This probably means the sample app wasn't granted full access.
    return "";
  }

  /**
   * Posts a notice to the profile determined by the current AuthSub token.
   * 
   * @param subject The subject of the new notice.
   * @param message The body of the new notice.
   * @param ccrg An optional XML blob specifying CCRg data.
   * @throws HealthSampleException
   */
  public void postNotice(String subject, String message, String ccrg)
      throws HealthSampleException {

    if (this.authSubToken == null) {
      throw new HealthSampleException(
          "Need to supply a token before retrieving notices.");
    }

    GoogleService service = new GoogleService("weaver", "HealthSample");
    service.setAuthSubToken(this.authSubToken);

    Entry newNotice = new Entry();
    newNotice.setTitle(new PlainTextConstruct(subject));
    newNotice.setContent(new PlainTextConstruct(message));
    if (ccrg != null) {
      XmlBlob ccrgElement = new XmlBlob();
      ccrgElement.setBlob(ccrg);
      newNotice.setXmlBlob(ccrgElement);
    }


    try {
      Entry createdNotice = service.insert(new URL(
          "https://www.google.com/h9/feeds/register/default"), newNotice);
    } catch (MalformedURLException e) {
      throw new HealthSampleException("Error posting notice", e);
    } catch (IOException e) {
      throw new HealthSampleException("Error posting notice", e);
    } catch (ServiceException e) {
      throw new HealthSampleException("Error posting notice", e);
    }
  }

  /**
   * Cleans up the CCRg data enclosed in an Atom entry to make it more human
   * readable.
   * 
   * @param blob The XmlBlob containing the extension data.
   * @return A formatted string representing the CCRg XML data.
   * @throws TransformerException
   */
  private String prettifyXmlBlob(XmlBlob blob) throws TransformerException {

    String xmlString = blob.getBlob();

    StreamSource sourceXml = new StreamSource(new StringReader(xmlString));
    StringWriter sw = new StringWriter();

    TransformerFactory tf = TransformerFactory.newInstance();
    tf.setAttribute("indent-number", new Integer(2));
    Transformer trans;

    trans = tf.newTransformer();
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    trans.transform(sourceXml, new StreamResult(sw));
    xmlString = sw.toString();

    return xmlString;
  }

}
