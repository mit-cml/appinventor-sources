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
public class BaseLoginServlet extends HttpServlet {

  protected final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  protected static final Flag<String> mailServer = Flag.createFlag("localauth.mailserver", "");
  protected static final Flag<String> password = Flag.createFlag("localauth.mailserver.password", "");
  protected static final Flag<Boolean> useGoogle = Flag.createFlag("auth.usegoogle", true);
  protected static final Flag<Boolean> useLocal = Flag.createFlag("auth.uselocal", false);
  protected static final UserService userService = UserServiceFactory.getUserService();
  protected static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    if (!useGoogle.get() && !useLocal.get()) {
      throw new ServletException("No authentication method is enabled. Please check either one of auth.usegoogle, auth.uselocal, auth.useldap is enabled.");
    }
  }

  protected void fail(HttpServletRequest req, HttpServletResponse resp, String error) throws IOException {
    String uri = new UriBuilder("/login")
      .add("error", error)
      .build();
    resp.sendRedirect(uri);
    return;
  }

  protected Map<String, String> getCommonParams(HttpServletRequest req) {
    // Decode params
    String repo = req.getParameter("repo");
    String galleryId = req.getParameter("galleryId");
    String redirect = req.getParameter("redirect");
    String locale = req.getParameter("locale");

    // Set default values
    locale = locale != null ? locale : "en";

    Map<String, String> ret = new HashMap<>();
    ret.put("repo", repo);
    ret.put("galleryId", galleryId);
    ret.put("redirect", redirect);
    ret.put("locale", locale);
    return ret;
  }

  protected PrintWriter setCookieOutput(OdeAuthFilter.UserInfo userInfo, HttpServletResponse resp)
    throws IOException {
    if (userInfo != null) {     // if we never had logged in, this will be null!
      String newCookie = userInfo.buildCookie(true);
      if (newCookie != null) {
        Cookie cook = new Cookie("AppInventor", newCookie);
        cook.setPath("/");
        resp.addCookie(cook);
      }
    }
    PrintWriter out = resp.getWriter();
    return out;
  }
}
