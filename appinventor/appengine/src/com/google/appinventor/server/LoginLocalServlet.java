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
public class LoginLocalServlet extends BaseLoginServlet {
  protected static final Logger LOG = Logger.getLogger(LoginLocalServlet.class.getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    // resp.setContentType("text/html; charset=utf-8");

    if (DEBUG) {
      LOG.info("requestURI = " + req.getRequestURI());
    }

    // Check if local authentication is enabled
    if (!useLocal.get()) {
      req.setAttribute("title", "Local authentication is disabled");
      req.setAttribute("reason", "Please contact the server admin to require logging in with local datastore account.");
      req.getRequestDispatcher("/error.jsp").forward(req, resp);
      return;
    }

    // Decode common parameters
    Map<String, String> commonParams = getCommonParams(req);
    String locale = commonParams.get("locale");
    String repo = commonParams.get("repo");
    String galleryId = commonParams.get("galleryId");
    String redirect = commonParams.get("redirect");

    // Common method variables
    String path = req.getRequestURI();
    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));

    if (DEBUG) {
      LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    }

    // The GET route never manipulate user credentials.
    // Never do any means of user authentication here.

    if (path.equals("/login/setpw")) {
      // Decode extra parameters
      String pwid = req.getParameter("pwid");

      // Check pwid paramter existence
      if (pwid == null) {
        fail(req, resp, "Bad request");
        return;
      }

      req.setAttribute("pwid", pwid);
      req.setAttribute("setYourPassword", bundle.getString("setyourpassword"));
      req.setAttribute("setPassword", bundle.getString("setpassword"));
      req.getRequestDispatcher("/setpw.jsp").forward(req, resp);
      return;

    } else if (path.equals("/login/linksent")) {
      req.setAttribute("linksent", bundle.getString("linksent"));
      req.setAttribute("checkemail", bundle.getString("checkemail"));
      req.getRequestDispatcher("/linksent.jsp").forward(req, resp);
      return;

    } else if (path.equals("/login/sendlink")) {
      req.setAttribute("requestreset", bundle.getString("requestreset"));
      req.setAttribute("requestlink", bundle.getString("requestlink"));
      req.setAttribute("requestinstructions", bundle.getString("requestinstructions"));
      req.setAttribute("enteremailaddress", bundle.getString("enteremailaddress"));
      req.setAttribute("sendlink", bundle.getString("sendlink"));
      req.getRequestDispatcher("/sendlink.jsp").forward(req, resp);
      return;
    } else {
      // Bad request
      assert false : String.format("The path \"%s\" should never reach this servlet. Is your web.xml correctly configured?", path);
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Decode common parameters
    Map<String, String> commonParams = getCommonParams(req);
    String locale = commonParams.get("locale");
    String repo = commonParams.get("repo");
    String galleryId = commonParams.get("galleryId");
    String redirect = commonParams.get("redirect");

    // Common method variables
    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));
    String path = req.getRequestURI();

    if (DEBUG) {
      LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    }

    if (path.equals("/login/sendlink")) {
      // Decode additional params
      String email = req.getParameter("email");

      // Kick out bad requests
      if (email == null) {
        fail(req, resp, "No Email Address Provided");
        return;
      }

      // Create email entry in datastore
      PWData pwData = storageIo.createPWData(email);
      if (pwData == null) {
        fail(req, resp, "Internal Error");
        return;
      }
      String pwid = pwData.id;

      // Send email here, for now we put it in the error string and redirect
      String link = new UriBuilder("/login/setpw")
        .add("pwid", pwid)
        .build();
      sendmail(email, link, locale);
      resp.sendRedirect("/login/linksent");

      storageIo.cleanuppwdata();  // TODO move it to cron job
      return;

    } else if (path.equals("/login/setpw")) {
      // Decode additional params
      String pwid = req.getParameter("pwid");
      String password = req.getParameter("password");

      if (pwid == null || password == null || pwid.equals("") || password.equals("")) {
        fail(req, resp, "Bad request");
        return;
      }

      // TODO strong password validation
      // It is suggested for better security.

      // Load email session
      PWData data = storageIo.findPWData(pwid);
      if (data == null) {
        fail(req, resp, "Invalid set password link");
        return;
      }

      if (DEBUG) {
        LOG.info("setpw email = " + data.email);
      }

      // Update password
      User user = storageIo.getOrCreateUserFromEmail(data.email);
      if (user == null) {
        fail(req, resp, "Invalid set password link");
        return;
      }
      String uid = user.getUserId();
      storageIo.setUserPassword(uid,  password);

      // Automatic login for user
      OdeAuthFilter.UserInfo userInfo = new OdeAuthFilter.UserInfo();
      userInfo.setUserId(uid); // This effectively logs us in!

      String newCookie = userInfo.buildCookie(true);
      if (newCookie != null) {
        Cookie cook = new Cookie("AppInventor", newCookie);
        cook.setPath("/");
        resp.addCookie(cook);
      }

      // Redirect
      String uri = new UriBuilder("/")
        .add("locale", locale)
        .add("repo", repo)
        .add("galleryId", galleryId).build();
      resp.sendRedirect(uri);
      return;

    } else {
      // Bad request
      assert false : String.format("The path \"%s\" should never reach this servlet. Is your web.xml correctly configured?", path);
    }
  }

  private void sendmail(String email, String url, String locale) {
    try {
      String tmailServer = mailServer.get();
      if (tmailServer.equals("")) { // No mailserver = no mail!
        return;
      }
      URL mailServerUrl = new URL(tmailServer);
      HttpURLConnection connection = (HttpURLConnection) mailServerUrl.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      PrintWriter stream = new PrintWriter(connection.getOutputStream());
      stream.write("email=" + URLEncoder.encode(email) +
                   "&url=" + URLEncoder.encode(url) +
                   "&pass=" + URLEncoder.encode(password.get()) +
                   "&locale=" + URLEncoder.encode(locale));
      stream.flush();
      stream.close();
      int responseCode = 0;
      responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        LOG.warning("mailserver responded with code = " + responseCode);
        // Nothing else we can do here...
      }
    } catch (MalformedURLException e) {
      // TODO
    } catch (IOException e) {
      // TODO
    }
  }

}
