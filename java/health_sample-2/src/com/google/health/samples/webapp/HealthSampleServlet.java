/*
 * Copyright (C) 2007 Google Inc.
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
package com.google.health.samples.webapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet class to demonstrate use of the Google Health API.
 * 
 * @author api.jfisher@google.com Jeff Fisher
 */
public class HealthSampleServlet extends javax.servlet.http.HttpServlet
    implements Servlet {

  public static final long serialVersionUID = 1;

  // the name of the session variable that holds our AuthSub token
  public static final String HEALTH_TOKEN = "healthToken";

  /**
   * Process GET requests by calling the doPost method
   * 
   * @param request The request
   * @param response The response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  /**
   * Process all requests to this servlet.
   * 
   * @param request The request
   * @param response The response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    /*
     * Determine action attempted
     */
    String action = (request.getParameter("action")) == null
        ? "outputIntroPage" : request.getParameter("action");

    if ("outputIntroPage".equals(action)) {
      processOutputIntroPage(request, response);
    } else if ("handleToken".equals(action)) {
      processAcceptAuthSubToken(request, response);
    } else if ("outputProfile".equals(action)) {
      processOutputProfile(request, response);
    } else if ("submitNotice".equals(action)) {
      processSubmittedNotice(request, response);
    } else if ("logout".equals(action)) {
      processLogout(request, response);
    } else {
      throw new ServletException("Error! Bad Action!'" + action + "'");
    }
  }


  /**
   * Returns URI of the current page
   * 
   * @param request the request
   * @return the URI the user is currently requesting.
   * @throws MalformedURLException When the Java URL constructor gets angry.
   */
  private String getCurrentUrl(HttpServletRequest request)
      throws MalformedURLException {
    URL currentUrl = new URL(request.getScheme(), request.getServerName(),
        request.getServerPort(), request.getRequestURI());
    return currentUrl.toString();
  }

  /**
   * Save AuthSubToken into session.
   * 
   * @param request The request
   * @param response The response
   * @throws IOException
   */
  private void processAcceptAuthSubToken(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    /*
     * Request is caused by a user being redirected back from AuthSub login
     */
    if (request.getParameter("token") != null) {
      HealthSample sp = new HealthSample();
      try {
        String token = sp.exchangeAuthSubToken(request.getParameter("token"));
        request.getSession().setAttribute(HEALTH_TOKEN, token);
        response.sendRedirect("?action=outputProfile");
      } catch (HealthSampleException e) {
        System.err.println("Authentication exception: " + e.getMessage());
      }
    }
  }

  /**
   * Logs the user out by clearing the session.
   * 
   * @param request The request
   * @param response The response
   * @throws IOException
   */
  private void processLogout(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    request.getSession().removeAttribute(HEALTH_TOKEN);
    response.sendRedirect("?action=outputIntroPage");
  }

  /**
   * Default action - output intro page.
   * 
   * @param request The request
   * @param response The response
   * @throws ServletException
   * @throws IOException
   */
  private void processOutputIntroPage(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {

    String authUrl = HealthSample.getAuthSubUrl(getCurrentUrl(request)
        + "?action=handleToken");
    request.setAttribute("authUrl", authUrl);
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
        "/WEB-INF/jsp/outputIntroPage.jsp");
    dispatcher.forward(request, response);
  }

  /**
   * Output profile visible from the given token.
   * 
   * @param request The request
   * @param response The response
   * @throws ServletException
   * @throws IOException
   */
  private void processOutputProfile(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    HealthSample sp = new HealthSample();
    try {
      String token = (String) request.getSession().getAttribute(HEALTH_TOKEN);
      if (token == null) {
        response.sendRedirect("?action=outputIntroPage");
        return;
      }
      sp.setAuthSubToken(token);

      request.setAttribute("profile", sp.getProfile());
    } catch (HealthSampleException e) {
      System.err.println("Profile retrieval exception: " + e.getMessage());
    }
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
        "/WEB-INF/jsp/outputProfile.jsp");
    dispatcher.forward(request, response);
  }

  /**
   * Action: User submitted a new notice to post to the profile.
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  private void processSubmittedNotice(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    HealthSample sp = new HealthSample();
    try {
      String token = (String) request.getSession().getAttribute(HEALTH_TOKEN);
      if (token == null) {
        response.sendRedirect("?action=outputIntroPage");
        return;
      }
      sp.setAuthSubToken(token);

      String subject = request.getParameter("subject");
      String message = request.getParameter("message");
      String ccrg = request.getParameter("ccrg");

      sp.postNotice(subject, message, ccrg);

      response.sendRedirect("?action=outputProfile");

    } catch (HealthSampleException e) {
      System.err.println("Exception posting notice: " + e.getMessage());
    }
  }



}
