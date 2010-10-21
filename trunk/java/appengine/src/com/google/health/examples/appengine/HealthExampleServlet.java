package com.google.health.examples.appengine;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.health.examples.appengine.gdata.GoogleService;
import com.google.health.examples.appengine.gdata.HealthClient;
import com.google.health.examples.appengine.gdata.Result;
import com.google.health.examples.appengine.gdata.TestResult;
import com.google.health.examples.appengine.oauth.AuthenticationException;
import com.google.health.examples.appengine.oauth.OAuthFilter;
import com.google.health.examples.appengine.oauth.OAuthService;

@SuppressWarnings("serial")
public class HealthExampleServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(HealthExampleServlet.class.getName());

  private static ProfileDao pm = new ProfileDao();

  private static UserService userService = UserServiceFactory.getUserService();

  private HealthClient client;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {

    initHealthClient(req);
    Profile profile = (Profile) req.getAttribute(OAuthFilter.PROFILE);

    List<Result> results = null;
    try {
      results = client.retrieveResults();
    } catch (AuthenticationException e) {
      handleAuthException(profile, req, resp);
      return;
    }

    // Collect the Tests from the Results and order them chronologically.
    Set<TestResult> tests = new TreeSet<TestResult>(new Comparator<TestResult>() {
      public int compare(TestResult t1, TestResult t2) {
        // TODO Check for null dates and names
        int x = t1.getDate().compareTo(t2.getDate());

        if (x != 0) {
          return x;
        }

        x = t1.getName().compareTo(t2.getName());

        if (x != 0) {
          return x;
        }

        return -1;
      }
    });

    for (Result result : results) {
      tests.addAll(result.getTests());

      // If the Test is missing it's date, then assign the Result date.
      for (TestResult test : result.getTests()) {
        if (test.getDate() == null) {
          test.setDate(result.getDate());
        }
      }
    }

    req.setAttribute("tests", tests);
    req.setAttribute("logoutLink", userService.createLogoutURL(req.getRequestURL().toString()));

    req.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {

    initHealthClient(req);
    Profile profile = (Profile) req.getAttribute(OAuthFilter.PROFILE);

    TestResult test = new TestResult();
    test.setName(req.getParameter("name"));
    test.setValue(req.getParameter("value"));
    test.setUnits(req.getParameter("units"));
    test.setDate(req.getParameter("date"));

    Result result = new Result();
    result.addTest(test);

    try {
      client.createResult(result);
    } catch (AuthenticationException e) {
      handleAuthException(profile, req, resp);
      return;
    }

    // Re-display the results with the added entry.
    doGet(req, resp);
  }

  private void initHealthClient(HttpServletRequest req) {
    if (client == null) {
      client = new HealthClient((GoogleService) req.getAttribute(OAuthFilter.GOOGLE_SERVICE));
      client.setOAuthAuthenticator((OAuthService) req.getAttribute(OAuthFilter.OAUTH_SERVICE));
    }

    client.setProfile((Profile) req.getAttribute(OAuthFilter.PROFILE));
  }

  private void handleAuthException(Profile profile, HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    
    log.info("User has unlinked account.");

    // The user has unlinked their account. Reset the tokens with the profile
    // as start over.
    profile.setOAuthToken(null);
    profile.setOAuthTokenSecret(null);

    profile = pm.createOrUpdateProfile(profile);

    resp.sendRedirect(req.getRequestURL().toString());
  }
}
