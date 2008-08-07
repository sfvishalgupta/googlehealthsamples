/* Copyright (c) 2007 Mt. Tabor OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mttaboros.health.feed;

import com.mttaboros.health.authsub.AuthSubUtil;
import com.mttaboros.health.authsub.AuthenticationException;
import com.mttaboros.util.IOUtils;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility functions to support retrieval and creation of AuthSub secured atom feeds.
 * <p/>
 * This version of FeedClient is java 1.4.2 compatible.
 *
 * @author cb@mttaboros.com
 * @author az@mttaboros.com
 */
public class FeedClient {

    // The regular expression used for matching xml declarations
    private static final Pattern REGEX_PATTERN_XML_DECLARATION = Pattern.compile("[\\s]*<\\?xml.*?\\?>");

    private FeedClient() {
        //class only has static methods
    }

    /**
     * Removes the xml declaration at the top of an xml file
     *
     * @param string The xml contents to remove the declaration from
     * @return the xml without an xml declaration at the top
     */
    public static synchronized String removeXmlDeclaration(String string) {
        return REGEX_PATTERN_XML_DECLARATION.matcher(string).replaceFirst("");
    }

    /**
     * Return an atom feed from the specified url using an AuthSub token and PrivateKey.
     *
     * @param authSubToken the AuthSub token to use in the request
     * @param urlHref      the url of the atom feed
     * @param privateKey   the private key to sign the request
     * @return the atom Feed from the specified url
     * @throws AuthenticationException if the token is rejected
     */
    public static Feed getAtomFeed(String authSubToken, String urlHref, PrivateKey privateKey) throws AuthenticationException {
        return getAtomFeed(authSubToken, urlHref, privateKey, true);
    }

    /**
     * Return an atom feed from the specified url using an AuthSub token and PrivateKey.
     *
     * @param removeXmlnsFromEntries TRUE enables stripping xmlns attributes from the entry elements, FALSE disables it.
     * @param authSubToken           the AuthSub token to use in the request
     * @param urlHref                the url of the atom feed
     * @param privateKey             the private key to sign the request
     * @return the atom Feed from the specified url
     * @throws AuthenticationException if the token is rejected
     */
    public static Feed getAtomFeed(String authSubToken, String urlHref, PrivateKey privateKey, boolean removeXmlnsFromEntries) throws AuthenticationException {
        try {
            return getAtomFeed(getResponseAsInputStream(authSubToken, new URL(urlHref).toExternalForm(), privateKey), removeXmlnsFromEntries);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return an atom feed from the specified inputStream
     *
     * @param inputStream            the InputStream of the atom feed xml
     * @param removeXmlnsFromEntries TRUE enables stripping xmlns attributes from the entry elements, FALSE disables it.
     * @return the atom Feed from the specified inputStream
     */
    public static Feed getAtomFeed(InputStream inputStream, boolean removeXmlnsFromEntries) {
        try {
            return (Feed) new WireFeedInput().build(getDocument(inputStream, removeXmlnsFromEntries));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return an atom feed from the specified inputStream
     *
     * @param inputStream the InputStream of the atom feed xml
     * @return the atom Feed from the specified inputStream
     */
    public static Feed getAtomFeed(InputStream inputStream) {
        return getAtomFeed(inputStream, true);
    }

    /**
     * Gets an XML string representation of an Element
     *
     * @param stucturedProductTypeNode a JDom Element
     * @return the xml of the Jdom Element
     * @throws IOException if error in writing/reading the request
     */
    public static String getXmlFromNode(Element stucturedProductTypeNode) throws IOException {
        StringWriter stringWriter = new StringWriter();
        new XMLOutputter().output(stucturedProductTypeNode, stringWriter);
        return stringWriter.getBuffer().toString();
    }

    /**
     * Converts an xml string into a JDom Element
     *
     * @param xml a valid xml string that can be converted into a jdom element
     * @return a Jdom element created from the provided xml
     */
    private static Element toJdomElement(String xml) {
        try {
            return new SAXBuilder().build(new StringReader(xml)).detachRootElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds an atom Entry to specified remote atom feed
     *
     * @param authSubToken the AuthSub token to use in the request
     * @param feedHref     the url of the atom feed
     * @param title        the title/subject of the new entry
     * @param ccr          the ccr or ccrg xml
     * @return an atom feed Entry
     */
    public static Entry addEntry(String authSubToken, String feedHref, String title, String ccr) {
        Feed feed = buildFeed(title, ccr);
        try {
            String entryXml = getXmlFromNode(getEntryFromFeed(feed));
            return sendEntryToServer(authSubToken, feedHref, entryXml);
        } catch (Exception e) {
            throw new RuntimeException("Could not add new atom ccr entry", e);
        }
    }

    /**
     * Creates an atom feed with one entry that contains the provided ccr
     *
     * @param title the title/subject of the new entry
     * @param ccr   the ccr or ccrg xml
     * @return the atom Feed from the specified url
     */
    public static Feed buildFeed(String title, String ccr) {
        Feed feed = new Feed();
        feed.setFeedType("atom_1.0");
        Entry entry = new Entry();
        entry.setPublished(new Date());
        ((Collection) entry.getForeignMarkup()).add(toJdomElement(ccr));
        entry.setTitle(title);
        feed.getEntries().add(entry);
        return feed;
    }

    /**
     * Gets the entry from an atom feed that only has only has one entry.
     *
     * @param feed the atom feed to retrieve the entry Element from
     * @return an element node of the entry in the feed
     * @throws FeedException if there is an error in parsing feed
     * @throws JDOMException if there is an error in building a Document
     * @throws IOException   if error in writing/reading the request
     */
    public static Element getEntryFromFeed(Feed feed) throws FeedException, JDOMException, IOException {
        String xml = new WireFeedOutput().outputString(feed);
        Document document = new SAXBuilder(false).build(new ByteArrayInputStream(xml.getBytes()));
        List nodes = document.getRootElement().getChildren();
        if (nodes.size() != 1) {
            throw new RuntimeException("Found more than one entry on that feed. Cannot add CCR.");
        }
        return (Element) nodes.get(0);
    }

    /**
     * Creates an atom feed from an atom entry xml string and posts the feed to a specified atom url.
     *
     * @param authSubToken the AuthSub token to use in the request
     * @param feedHref     the url of the atom feed
     * @param entryXml     a valid xml string that can be converted into a jdom entry
     * @return an atom Entry
     * @throws GeneralSecurityException if error in signing the request
     * @throws AuthenticationException  if the token is rejected
     * @throws FeedException            if there is an error in parsing feed
     */
    private static Entry sendEntryToServer(String authSubToken, String feedHref, String entryXml) throws FeedException, AuthenticationException, GeneralSecurityException {
        InputStream stream = postDataAndGetResponseAsInputStream(authSubToken, feedHref, null, entryXml);
        return (Entry) buildAtomFeedFromEntryXml(IOUtils.toString(stream)).getEntries().get(0);
    }

    /**
     * Wraps an xml entry with the atom feed namespace and creates a new atom Feed from it.
     *
     * @param entryXml     a valid xml string that can be converted into a jdom entry
     * @return an atom Entry
     * @throws GeneralSecurityException if error in signing the request
     * @throws AuthenticationException  if the token is rejected
     * @throws FeedException            if there is an error in parsing feed
     */
    public static Feed buildAtomFeedFromEntryXml(String entryXml) throws FeedException, AuthenticationException, GeneralSecurityException {
        return (Feed) new WireFeedInput().build(new StringReader("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ccr=\"urn:astm-org:CCR\">" + removeXmlDeclaration(entryXml) + "</feed>"));
    }

    /**
     * Gets the response of the http request for an AuthSub secured atom feed as a string.
     *
     * @param authSubToken the AuthSub token to use in the request
     * @param url          the url of the atom feed
     * @param privateKey   the private key to sign the request
     * @return the atom Feed from the specified url as a string
     * @throws AuthenticationException if the token is rejected
     */
    public String getResponseAsString(String authSubToken, String url, PrivateKey privateKey) throws AuthenticationException {
        return IOUtils.toString(getResponseAsInputStream(authSubToken, url, privateKey));
    }

    /**
     * Gets the response of the http request for an AuthSub secured atom feed as an InputStream.
     *
     * @param authSubToken the AuthSub token to use in the request
     * @param url          the url of the atom feed
     * @param privateKey   the private key to sign the request
     * @return the response from the specified url as an InputStream
     * @throws AuthenticationException if the token is rejected
     */
    public static InputStream getResponseAsInputStream(String authSubToken, String url, PrivateKey privateKey) throws AuthenticationException {
        try {
            String header = AuthSubUtil.formAuthorizationHeader(authSubToken, privateKey, new URL(url), "GET");
            HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
            GetMethod method = new GetMethod(url);
            method.setRequestHeader(new Header("Authorization", header));
            executeMethod(client, method, HttpURLConnection.HTTP_OK);
            return method.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a method on the provided httpClient and throws an exception
     * if the response code is not what is expected.
     *
     * @param client                the HttpClient to execute the method on
     * @param method                the Get method to execute
     * @param necessaryResponseCode the required response code
     * @throws IOException             if error in writing/reading the request
     * @throws AuthenticationException if the token is rejected or the necessaryResponseCode is not received
     */
    private static void executeMethod(HttpClient client, HttpMethodBase method, int necessaryResponseCode) throws IOException, AuthenticationException {
        int responseCode = client.executeMethod(method);
        if (responseCode != necessaryResponseCode) {
            throw new AuthenticationException(String.valueOf(responseCode));
        }
    }

    /**
     * Posts data to a specifed AuthSub protected url and gets the repsonse as an InputStream.
     *
     * @param authSubToken the AuthSub token to use in the request
     * @param urlHref      the url of the atom feed
     * @param privateKey   the private key to sign the request
     * @param data         the data to post to the feed url
     * @return the response from the specified feed url as an InputStream
     * @throws AuthenticationException  if the token is rejected
     * @throws GeneralSecurityException if error in signing the request
     */
    public static InputStream postDataAndGetResponseAsInputStream(String authSubToken, String urlHref, PrivateKey privateKey, String data) throws AuthenticationException, GeneralSecurityException {
        try {
            URL url = new URL(urlHref);
            String header = AuthSubUtil.formAuthorizationHeader(authSubToken, privateKey, url, "POST");
            HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
            PostMethod method = new PostMethod(url.toExternalForm());
            method.setRequestHeader(new Header("Authorization", header));
            method.setRequestHeader(new Header("Content-Type", "application/atom+xml;charset=UTF-8"));
            method.setRequestHeader(new Header("Accept-Encoding", "gzip"));
            method.setRequestHeader(new Header("Accept", "text/html, image/ gif, image/ jpeg, *; q=.2, */*; q=.2"));
            method.setRequestHeader(new Header("Cache-Control", "no-cache"));
            method.setRequestHeader(new Header("Pragma", "no-cache"));
            method.setRequestHeader(new Header("Connection", "keep-alive"));
            //noinspection deprecation
            method.setRequestBody(data);
            executeMethod(client, method, HttpURLConnection.HTTP_CREATED);
            return method.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses an InputStream into an org.w3c.dom.Document and optionally removes xmlns attributes from entry nodes
     *
     * @param inputStream            the xml inputStream to convert to a document
     * @param removeXmlnsFromEntries whether or not remove xmlns attributes from the entry nodes of this document
     * @return the org.w3c.dom.Document created from parsing the InputStream
     * @throws ParserConfigurationException if error in creating the DocumentBuilder
     * @throws SAXException                 if error in parsing the InputStream
     */
    private static org.w3c.dom.Document getDocument(InputStream inputStream, boolean removeXmlnsFromEntries) throws ParserConfigurationException, SAXException {
        org.w3c.dom.Document document = getDocument(inputStream);
        if (removeXmlnsFromEntries) {
            removeXmlnsFromEntries(document);
        }
        return document;
    }

    /**
     * Parses an InputStream into an org.w3c.dom.Document
     *
     * @param inputStream the xml inputStream to convert to a document
     * @return the org.w3c.dom.Document created from parsing the InputStream
     * @throws ParserConfigurationException if error in creating the DocumentBuilder
     * @throws SAXException                 if error in parsing the InputStream
     */
    private static org.w3c.dom.Document getDocument(InputStream inputStream) throws ParserConfigurationException, SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try {
            return documentBuilder.parse(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removed xmlns attributes from entry nodes
     *
     * @param document the org.w3c.dom.Document with entries to remove xmlns attributes from
     */
    private static void removeXmlnsFromEntries(org.w3c.dom.Document document) {
        NodeList nodes = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if ("entry".equals(node.getNodeName())) {
                if (node.getAttributes().getNamedItem("xmlns") != null) {
                    node.getAttributes().removeNamedItem("xmlns");
                }
            }
        }
    }
}