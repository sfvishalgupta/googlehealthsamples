package com.google.health.examples.appengine.oauth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.health.examples.appengine.Profile;
import com.google.health.examples.appengine.ProfileDao;
import com.google.health.examples.appengine.gdata.GoogleService;
import com.google.health.examples.appengine.oauth.OAuthService.OAuthVersion;

public class OAuthFilter implements Filter {

  // FilterConfig parameters.
  private String OAUTH_CONSUMER_KEY = "oauthConsumerKey";
  private String KEYSTORE_CERTIFICATE_ALIAS = "keystoreCertificateAlias";
  private String KEYSTORE_FILE = "keystoreFile";
  private String KEYSTORE_PASSWORD = "keystorePassword";
  private String SERVICE_NAME = "serviceName";

  // Request parameters.
  public static final String PROFILE = "profile";
  public static final String GOOGLE_SERVICE = "googleService";
  public static final String OAUTH_SERVICE = "oauthService";

  private static final UserService userService = UserServiceFactory.getUserService();

  @SuppressWarnings("unused")
  private FilterConfig filterConfig;

  private ProfileDao profileDao = new ProfileDao();

  private GoogleService service;

  private OAuthService oauthService;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;

    try {
      oauthService = new OAuthService(OAuthVersion.v1_0, filterConfig
          .getInitParameter(OAUTH_CONSUMER_KEY), filterConfig.getInitParameter(KEYSTORE_FILE),
          filterConfig.getInitParameter(KEYSTORE_PASSWORD), filterConfig
              .getInitParameter(KEYSTORE_CERTIFICATE_ALIAS));
    } catch (Exception e) {
      throw new ServletException(e);
    }

    service = GoogleService.getServiceByName(filterConfig.getInitParameter(SERVICE_NAME));
  }

  public void destroy() {
    filterConfig = null;
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    User user = userService.getCurrentUser();

    // User is not logged in. Send them to the login page.
    if (user == null) {
      resp.sendRedirect(userService.createLoginURL(req.getRequestURL().toString()));
      return;
    }

    Profile profile = profileDao.retrieveProfileByEmail(user.getEmail());
    if (profile == null) {
      profile = new Profile();
      profile.setEmail(user.getEmail());

      profile = profileDao.createOrUpdateProfile(profile);
    }

    // The user has requested to unlink their account.
    if (req.getParameter("unlink") != null) {
      try {
        oauthService.revokeToken(profile.getOAuthToken(), profile.getOAuthTokenSecret());
      } catch (OAuthException e) {
        throw new ServletException(e);
      }

      // Reset the profile's credentials.
      profile.setOAuthToken(null);
      profile.setOAuthTokenSecret(null);
      profileDao.createOrUpdateProfile(profile);

      // Send them to the welcome page.
      resp.sendRedirect("/");
      return;
    }

    // If user doesn't have their OAuth token yet, they're somewhere in the
    // process.
    if (profile.getOAuthToken() == null) {
      String requestToken = req.getParameter("oauth_token");

      // Check to see if we've received a request token in a callback.
      // If not, start the OAuth dance.
      if (requestToken == null) {
        // OAuthGetRequestToken: The first leg is to get an unauthorized request
        // token.
        String[] tokens;
        try {
          tokens = oauthService.getRequestTokens(service.getBaseURL());
        } catch (OAuthException e) {
          throw new ServletException(e);
        }

        // Store the token secret for re-use in third leg.
        profile.setOAuthTokenSecret(tokens[1]);
        profileDao.createOrUpdateProfile(profile);

        // OAuthAuthorizeToken: The second leg is to authorize the request
        // token.
        String link = oauthService.getAuthorizationLink(tokens[0], req.getRequestURL().toString());

        // Give the user the link for authorizing the linkage.
        // This could be an automatic redirect, but we'd have to append the
        // "permission" parameter to the URL.
        req.setAttribute("link", link);
        req.getRequestDispatcher("WEB-INF/jsp/link.jsp").forward(req, resp);

        return;
      } else {
        // We have the request token, so we've received the callback form the
        // OAuth service.

        // OAuthGetAccessToken: The third leg is to get an access token.
        String tokenSecret = profile.getOAuthTokenSecret();

        // If the token secret is null, the user needs to re-do the first leg.
        if (tokenSecret == null) {
          resp.sendRedirect(req.getRequestURL().toString());
          return;
        }

        String[] tokens;
        try {
          tokens = oauthService.getAccessTokens(tokenSecret, req.getQueryString());
        } catch (OAuthException e) {
          throw new ServletException(e);
        }

        // Set the tokens and persist the profile.
        profile.setOAuthToken(tokens[0]);
        profile.setOAuthTokenSecret(tokens[1]);

        profileDao.createOrUpdateProfile(profile);

        // Authorization complete.
        // Redirect the user to the current base url to clear oauth parameters.
        resp.sendRedirect(req.getRequestURL().toString());
        return;
      }
    } else if (profile.getOAuthTokenSecret() == null) {
      // If the profile has a token but no token secret, something's wrong...
      profile.setOAuthToken(null);
      profile.setOAuthTokenSecret(null);

      profileDao.createOrUpdateProfile(profile);

      resp.sendRedirect(req.getRequestURL().toString());
      return;
    }

    // Pass the following classes to the servlet so they don't have to be
    // re-created or re-configured.
    req.setAttribute(GOOGLE_SERVICE, service);
    req.setAttribute(PROFILE, profile);
    req.setAttribute(OAUTH_SERVICE, oauthService);

    // Authorization complete... continue to the resource.
    chain.doFilter(req, resp);
  }
}
