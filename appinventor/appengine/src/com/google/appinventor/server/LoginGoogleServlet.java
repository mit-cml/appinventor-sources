// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appinventor.server.flags.Flag;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

import com.google.appinventor.server.util.PasswordHash;
import com.google.appinventor.server.util.UriBuilder;

import com.google.appinventor.shared.rpc.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * LoginServlet -- Handle logging someone in using an email address for a login
 * name and a password, which is stored hashed (and salted). Facilities are
 * provided to e-mail a password to an e-mail address both to set one up the
 * first time and to recover a lost password.
 *
 * This implementation uses a helper server to send mail. It does a webservices
 * transaction (REST/POST) to the server with the email address and reset url.
 * The helper server then formats the e-mail message and sends it. The source
 * code is in misc/passwordmail/...
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
@SuppressWarnings("unchecked")
public class LoginGoogleServlet extends BaseLoginServlet {
  protected static final Logger LOG = Logger.getLogger(LoginGoogleServlet.class.getName());
  
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
    // resp.setContentType("text/html; charset=utf-8");

    // Check if Google login is enabled
    if (!useGoogle.get()) {
      req.setAttribute("title", "Google authentication is disabled");
      req.setAttribute("reason", "Please contact the server admin to require logging in with Google account.");
      req.getRequestDispatcher("/error.jsp").forward(req, resp);
      return;
    }
    
    if (DEBUG) {
      LOG.info("requestURI = " + req.getRequestURI());
    }

    // Decode common parameters
    Map<String, String> commonParams = getCommonParams(req);
    String locale = commonParams.get("locale");
    String repo = commonParams.get("repo");
    String galleryId = commonParams.get("galleryId");
    String redirect = commonParams.get("redirect");

    // Common method variables
    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));
    PrintWriter out;
    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);

    if (DEBUG) {
      LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    }

    // We get here after we have gone through the Google Login page
    // This is arranged via a security-constraint setup in web.xml
    com.google.appengine.api.users.User apiUser = userService.getCurrentUser();
    if (apiUser == null) {  // Hmmm. I don't think this should happen
      fail(req, resp, "Google Authentication Failed"); // Not sure what else to do
      return;
    }
    String email = apiUser.getEmail();
    String userId = apiUser.getUserId();
    User user = storageIo.getUser(userId, email);

    userInfo = new OdeAuthFilter.UserInfo(); // Create a new userInfo object

    userInfo.setUserId(user.getUserId()); // This effectively logs us in!
    userInfo.setIsAdmin(user.getIsAdmin() || userService.isUserAdmin()); // If we are a developer, we are always an admin

    String newCookie = userInfo.buildCookie(false);
    if (DEBUG) {
      LOG.info("newCookie = " + newCookie);
    }
    if (newCookie != null) {
      Cookie cook = new Cookie("AppInventor", newCookie);
      cook.setPath("/");
      resp.addCookie(cook);
    }
    // Remove the ACSID Cookie used by Google for Authentication
    Cookie cook = new Cookie("ACSID", null);
    cook.setPath("/");
    cook.setMaxAge(0);
    resp.addCookie(cook);
    String uri = "/";
    if (redirect != null) {
      uri = redirect;
    }
    uri = new UriBuilder(uri)
      .add("locale", locale)
      .add("repo", repo)
      .add("galleryId", galleryId).build();
    resp.sendRedirect(uri);
  }
}
