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
import com.google.appinventor.server.storage.StoredData.PWData;

import com.google.appinventor.server.util.PasswordHash;
import com.google.appinventor.server.util.UriBuilder;

import com.google.appinventor.shared.rpc.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

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
public class LoginEntryServlet extends BaseLoginServlet {
  protected static final Logger LOG = Logger.getLogger(LoginEntryServlet.class.getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    resp.setContentType("text/html; charset=utf-8");

    if (DEBUG) {
      LOG.info("requestURI = " + req.getRequestURI());
    }

    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);

    // Decode parameters
    Map<String, String> commonParams = this.getCommonParams(req);
    String locale = commonParams.get("locale");
    String repo = commonParams.get("repo");
    String galleryId = commonParams.get("galleryId");
    String redirect = commonParams.get("redirect");

    if (DEBUG) {
      LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    }

    // i18n
    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));

    String emailAddress = bundle.getString("emailaddress");
    String password = bundle.getString("password");
    String login = bundle.getString("login");
    String passwordclickhere = bundle.getString("passwordclickhere");

    // Render page
    req.setCharacterEncoding("UTF-8");
    if (useGoogle.get()) {
      req.setAttribute("useGoogleLabel", "true");
    } else {
      req.setAttribute("useGoogleLabel", "false");
    }
    req.setAttribute("emailAddressLabel", emailAddress);
    req.setAttribute("passwordLabel", password);
    req.setAttribute("loginLabel", login);
    req.setAttribute("passwordclickhereLabel", passwordclickhere);
    req.setAttribute("localeLabel", locale);
    req.setAttribute("pleaselogin", bundle.getString("pleaselogin"));
    req.setAttribute("login", bundle.getString("login"));
    req.setAttribute("repo", repo);
    req.setAttribute("locale", locale);
    req.setAttribute("galleryId", galleryId);

    req.getRequestDispatcher("/login.jsp").forward(req, resp);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);

    if (userInfo == null) {
      userInfo = new OdeAuthFilter.UserInfo();
    }

    // Decode common parameters
    Map<String, String> commonParams = getCommonParams(req);
    String locale = commonParams.get("locale");
    String repo = commonParams.get("repo");
    String galleryId = commonParams.get("galleryId");
    String redirect = commonParams.get("redirect");

    // Decode user/pass parameters
    String email = req.getParameter("email");
    String password = req.getParameter("password");

    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));

    if (DEBUG) {
      LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    }

    // Verify parameters
    if (email == null || password == null) {
      fail(req, resp, "Bad login request");
      return;
    }

    // Verify credential
    User user = storageIo.getOrCreateUserFromEmail(email);
    boolean validLogin = false;

    String hash = user.getPassword();
    if ((hash == null) || hash.equals("")) {
      fail(req, resp, bundle.getString("invalidpassword"));
      return;
    }

    try {
      validLogin = PasswordHash.validatePassword(password, hash);
    } catch (NoSuchAlgorithmException e) {
      // TODO
    } catch (InvalidKeySpecException e) {
      // TODO
    }

    if (!validLogin) {
      fail(req, resp, bundle.getString("invalidpassword"));
      return;
    }

    if (DEBUG) {
      LOG.info("userInfo = " + userInfo + " user = " + user);
    }

    // Login
    userInfo.setUserId(user.getUserId());
    userInfo.setIsAdmin(user.getIsAdmin());

    String newCookie = userInfo.buildCookie(false);
    if (DEBUG) {
      LOG.info("newCookie = " + newCookie);
    }
    if (newCookie != null) {
      Cookie cook = new Cookie("AppInventor", newCookie);
      cook.setPath("/");
      resp.addCookie(cook);
    }

    // Redirect to home page
    String uri = "/";
    if (redirect != null && !redirect.equals("")) {
      uri = redirect;
    }
    uri = new UriBuilder(uri)
      .add("locale", locale)
      .add("repo", repo)
      .add("galleryId", galleryId).build();
    resp.sendRedirect(uri);
  }
}
