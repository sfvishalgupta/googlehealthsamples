package com.google.health.examples.appengine.oauth;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;

public class OAuthService {
  private String consumerKey;
  private String consumerSecret;

  private OAuthVersion version;

  private GoogleOAuthHelper helper;

  private final Logger log = Logger.getLogger(this.getClass().getName());

  public enum OAuthVersion {
    v1_0, v1_0a;
  }

  /**
   * Anonymous, HMAC-SHA1 signing
   * 
   * @param version
   */
  public OAuthService(OAuthVersion version) {
    this.version = version;
    this.consumerKey = "anonymous";
    this.consumerSecret = "anonymous";

    this.helper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
  }

  /**
   * HMAC-SHA1 signing
   * 
   * @param version
   * @param consumerKey
   * @param consumerSecret
   */
  public OAuthService(OAuthVersion version, String consumerKey, String consumerSecret) {
    this.version = version;
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;

    this.helper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
  }

  /**
   * RSA-SHA1 signing
   * 
   * @param version
   * @param consumerKey
   * @param keystore
   * @param keystorePassword
   */
  public OAuthService(OAuthVersion version, String consumerKey, String keystore,
      String keystorePassword, String alias) {
    this.version = version;
    this.consumerKey = consumerKey;

    PrivateKey pk;
    try {
      pk = loadPrivateKey(keystore, keystorePassword, alias);
      log.info("Loaded private key: " + pk.toString());

      this.helper = new GoogleOAuthHelper(new OAuthRsaSha1Signer(pk));
    } catch (Exception e) {
      log.severe(e.getMessage());
      return;
      // TODO not enough...
    }
  }

  /**
   * First leg OAuth 1.0
   * 
   * @param scope
   * @return Array containing token (index = 0) and token secret (index = 1).
   */
  public String[] getRequestTokens(String scope) {
    if (version != OAuthVersion.v1_0)
      throw new IllegalStateException();

    return _getRequestTokens(scope, null);
  }

  /**
   * First leg OAuth 1.0a
   * 
   * @param scope
   * @param callback
   * @return Array containing token (index = 0) and token secret (index = 1).
   */
  public String[] getRequestTokens(String scope, String callback) {
    if (version != OAuthVersion.v1_0a || Strings.isNullOrEmpty(callback))
      throw new IllegalStateException();

    return _getRequestTokens(scope, callback);
  }

  public String[] _getRequestTokens(String scope, String callback) {
    GoogleOAuthParameters params = new GoogleOAuthParameters();
    params.setOAuthConsumerKey(consumerKey);
    params.setScope(scope);
    params.setOAuthCallback(callback);

    // Will be null with RSA-SHA1, but the API doesn't seem to care.
    params.setOAuthConsumerSecret(consumerSecret);

    log.info("Retrieving request tokens.");
    logParameters(params);

    try {
      helper.getUnauthorizedRequestToken(params);
    } catch (OAuthException e) {
      log.severe(e.getMessage());
    }

    log.info("Retrieved request tokens.");
    logParameters(params);

    return new String[] { params.getOAuthToken(), params.getOAuthTokenSecret() };
  }

  /**
   * Second leg OAuth 1.0
   * 
   * @param token
   * @param callback
   * @return
   */
  public String getAuthorizationLink(String token, String callback) {
    if (version != OAuthVersion.v1_0 || Strings.isNullOrEmpty(callback))
      throw new IllegalStateException();

    return _getAuthorizationLink(token, callback);
  }

  /**
   * Second leg OAuth 1.0a
   * 
   * @param token
   * @return
   */
  public String getAuthorizationLink(String token) {
    if (version != OAuthVersion.v1_0a)
      throw new IllegalStateException();

    return _getAuthorizationLink(token, null);
  }

  private String _getAuthorizationLink(String token, String callback) {
    if (version != OAuthVersion.v1_0)
      throw new IllegalStateException();

    GoogleOAuthParameters params = new GoogleOAuthParameters();
    params.setOAuthConsumerKey(consumerKey);
    params.setOAuthConsumerSecret(consumerSecret);
    params.setOAuthToken(token);
    params.setOAuthCallback(callback);

    log.info("Generating authorization link.");
    logParameters(params);

    return helper.createUserAuthorizationUrl(params);
  }

  /**
   * Third leg
   * 
   * @param tokenSecret
   * @param queryString
   * @return
   */
  public String[] getAccessTokens(String tokenSecret, String queryString) {
    GoogleOAuthParameters params = new GoogleOAuthParameters();
    params.setOAuthConsumerKey(consumerKey);
    params.setOAuthConsumerSecret(consumerSecret);
    // Adding the token secret from the unauthorized request token response.
    // Doesn't seem to be documented.
    params.setOAuthTokenSecret(tokenSecret);

    helper.getOAuthParametersFromCallback(queryString, params);

    log.info("Retrieving access tokens.");
    logParameters(params);

    // Upgrade to access token.
    String accessToken = "";
    try {
      accessToken = helper.getAccessToken(params);
    } catch (OAuthException e) {
      log.severe(e.getMessage());
    }

    log.info("Retrieved access tokens.");
    logParameters(params);

    return new String[] { accessToken, params.getOAuthTokenSecret() };
  }

  /**
   * Generate an OAuth HTTP request parameter value.
   * 
   * @param url
   * @param method
   * @param token
   * @param tokenSecret
   * @return
   */
  public String getHttpAuthorizationHeader(String url, String method, String token,
      String tokenSecret) {
    log.info("Generating authorization header.");

    GoogleOAuthParameters params = new GoogleOAuthParameters();
    params.setOAuthConsumerKey(consumerKey);
    params.setOAuthConsumerSecret(consumerSecret);
    params.setOAuthToken(token);
    params.setOAuthTokenSecret(tokenSecret);

    logParameters(params);

    String header = null;
    try {
      header = helper.getAuthorizationHeader(url.toString(), method, params);
    } catch (OAuthException e) {
      log.severe(e.getMessage());
    }

    return header;
  }
  
  public void revokeToken(String token, String tokenSecret) {
    GoogleOAuthParameters params = new GoogleOAuthParameters();
    params.setOAuthConsumerKey(consumerKey);
    params.setOAuthConsumerSecret(consumerSecret);
    params.setOAuthToken(token);
    params.setOAuthTokenSecret(tokenSecret);
    
    try {
      helper.revokeToken(params);
    } catch (OAuthException e) {
      log.severe(e.getMessage());
    }
  }
  
  private static PrivateKey loadPrivateKey(String keystore, String keystorePassword, String alias)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
      UnrecoverableKeyException {

    InputStream in = OAuthService.class.getClassLoader().getResourceAsStream(keystore);

    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(in, keystorePassword.toCharArray());
    in.close();

    PrivateKey pk = (PrivateKey) ks.getKey(alias, keystorePassword.toCharArray());

    return pk;
  }

  private void logParameters(GoogleOAuthParameters params) {
    if (!log.isLoggable(Level.INFO))
      return;

    StringBuffer sb = new StringBuffer("Base Parameters: ");
    for (String key : params.getBaseParameters().keySet())
      sb.append("[" + key + " : " + params.getBaseParameters().get(key) + "]");

    log.info(sb.toString());

    sb = new StringBuffer("Extra Parameters: ");
    for (String key : params.getExtraParameters().keySet())
      sb.append("[" + key + " : " + params.getExtraParameters().get(key) + "]");

    log.info(sb.toString());
  }
}
