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
public class LoginServlet extends HttpServlet {

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
  private static final Flag<String> mailServer = Flag.createFlag("localauth.mailserver", "");
  private static final Flag<String> password = Flag.createFlag("localauth.mailserver.password", "");
  private static final Flag<Boolean> useGoogle = Flag.createFlag("auth.usegoogle", true);
  private static final Flag<Boolean> useLocal = Flag.createFlag("auth.uselocal", false);
  private static final UserService userService = UserServiceFactory.getUserService();

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html; charset=utf-8");

    PrintWriter out;
    String [] components = req.getRequestURI().split("/");
    LOG.info("requestURI = " + req.getRequestURI());
    String page = getPage(req);

    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);

    String queryString = req.getQueryString();
    HashMap<String, String> params = getQueryMap(queryString);
    // These params are passed around so they can take effect even if we
    // were not logged in.
    String locale = params.get("locale");
    if (locale == null) {
      locale = "en";
    }
    String repo = params.get("repo");
    String galleryId = params.get("galleryId");

    LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));

    if (page.equals("google")) {
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
      userInfo.setIsAdmin(user.getIsAdmin());
      if (userService.isUserAdmin()) { // If we are a developer, we are always an admin
        userInfo.setIsAdmin(true);
      }

      String newCookie = userInfo.buildCookie(false);
      LOG.info("newCookie = " + newCookie);
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
      String uri = buildUri("/", locale, repo, galleryId);
      resp.sendRedirect(uri);
      return;
    } else {
      if (useLocal.get() == false) {
        if (useGoogle.get() == false) {
          out = setCookieOutput(userInfo, resp);
          out.println("<html><head><title>Error</title></head>\n");
          out.println("<body><h1>App Inventor is Mis-Configured</h1>\n");
          out.println("<p>This instance of App Inventor has no authentication mechanism configured.</p>\n");
          out.println("</body>\n");
          out.println("</html>\n");
          return;
        }
        String uri = buildUri("/login/google", locale, repo, galleryId);
        resp.sendRedirect(uri);
        return;
      }
    }

    // If we get here, local accounts are supported

    if (page.equals("setpw")) {
      String uid = getParam(req);
      if (uid == null) {
        fail(req, resp, "Invalid Set Password Link");
        return;
      }
      PWData data = storageIo.findPWData(uid);
      if (data == null) {
        fail(req, resp, "Invalid Set Password Link");
        return;
      }
      LOG.info("setpw email = " + data.email);
      User user = storageIo.getUserFromEmail(data.email);
      userInfo = new OdeAuthFilter.UserInfo(); // Create new userInfo object
      userInfo.setUserId(user.getUserId()); // This effectively logs us in!
      out = setCookieOutput(userInfo, resp);
//      req.getSession().setAttribute("userid", user.getUserId()); // This effectively logs us in!
      out.println("<html><head><title>Set Your Password</title>\n");
      out.println("</head>\n<body>\n");
      out.println("<h1>" + bundle.getString("setyourpassword") + "</h1>\n");
      out.println("<form method=POST action=\"" + req.getRequestURI() + "\">");
      out.println("<input type=password name=password value=\"\" size=\"35\"><br />\n");
      out.println("<p></p>");
      out.println("<input type=Submit value=\"" + bundle.getString("setpassword") + "\" style=\"font-size: 300%;\">\n");
      out.println("</form>\n");
      storageIo.cleanuppwdata();
      return;
    } else if (page.equals("linksent")) {
      out = setCookieOutput(userInfo, resp);
      out.println("<html><head><title>" + bundle.getString("linksent") + "</title></head>\n");
      out.println("<body>\n");
      out.println("<h1>" + bundle.getString("linksent") + "</h1>\n");
      out.println("<p>" + bundle.getString("checkemail") + "</p>\n");
      return;
    } else if (page.equals("sendlink")) {
      out = setCookieOutput(userInfo, resp);
      out.println("<head><title>" + bundle.getString("requestreset") + "</title></head>\n");
      out.println("<body>\n");
      out.println("<h1>" + bundle.getString("requestlink") + "</h1>\n");
      out.println("<p>" + bundle.getString("requestinstructions") + "</p>\n");
      out.println("<form method=POST action=\"" + req.getRequestURI() + "\">\n");
      out.println(bundle.getString("enteremailaddress") + ":&nbsp;<input type=text name=email value=\"\" size=\"35\"><br />\n");
      out.println("<p></p>");
      out.println("<input type=submit value=\"" + bundle.getString("sendlink") + "\" style=\"font-size: 300%;\">\n");
      out.println("</form>\n");
      return;
    }

    String emailAddress = bundle.getString("emailaddress");
    String password = bundle.getString("password");
    String login = bundle.getString("login");
    String passwordclickhere = bundle.getString("passwordclickhere");

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
    try {
      req.getRequestDispatcher("/login.jsp").forward(req, resp);
    } catch (ServletException e) {
      throw new IOException(e);
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(req.getInputStream()));
    String queryString = input.readLine();

    PrintWriter out;

    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);

    if (userInfo == null) {
      userInfo = new OdeAuthFilter.UserInfo();
    }

    if (queryString == null) {
      out = setCookieOutput(userInfo, resp);
      out.println("queryString is null");
      return;
    }

    HashMap<String, String> params = getQueryMap(queryString);
    String page = getPage(req);
    String locale = params.get("locale");
    if (locale == null) {
      locale = "en";
    }

    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));

    String repo = params.get("repo");
    String galleryId = params.get("galleryId");
    LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    if (page.equals("sendlink")) {
      String email = params.get("email");
      if (email == null) {
        fail(req, resp, "No Email Address Provided");
        return;
      }
      // Send email here, for now we put it in the error string and redirect
      PWData pwData = storageIo.createPWData(email);
      if (pwData == null) {
        fail(req, resp, "Internal Error");
        return;
      }
      String link = trimPage(req) + pwData.id + "/setpw";
      sendmail(email, link, locale);
      resp.sendRedirect("/login/linksent/");
      storageIo.cleanuppwdata();
      return;
    } else if (page.equals("setpw")) {
      if (userInfo == null || userInfo.getUserId().equals("")) {
        fail(req, resp, "Session Timed Out");
        return;
      }
      User user = storageIo.getUser(userInfo.getUserId());
      String password = params.get("password");
      if (password == null || password.equals("")) {
        fail(req, resp, bundle.getString("nopassword"));
        return;
      }
      String hashedPassword;
      try {
        hashedPassword = PasswordHash.createHash(password);
      } catch (NoSuchAlgorithmException e) {
        fail(req, resp, "System Error hashing password");
        return;
      } catch (InvalidKeySpecException e) {
        fail(req, resp, "System Error hashing password");
        return;
      }

      storageIo.setUserPassword(user.getUserId(),  hashedPassword);
      String uri = buildUri("/", locale, repo, galleryId);
      resp.sendRedirect(uri);   // Logged in, go to service
      return;
    }

    String email = params.get("email");
    String password = params.get("password"); // We don't check it now
    User user = storageIo.getUserFromEmail(email);
    boolean validLogin = false;

    String hash = user.getPassword();
    if ((hash == null) || hash.equals("")) {
      fail(req, resp, "No Password Set for User");
      return;
    }

    try {
      validLogin = PasswordHash.validatePassword(password, hash);
    } catch (NoSuchAlgorithmException e) {
    } catch (InvalidKeySpecException e) {
    }

    if (!validLogin) {
      fail(req, resp, bundle.getString("invalidpassword"));
      return;
    }

    LOG.info("userInfo = " + userInfo + " user = " + user);
    userInfo.setUserId(user.getUserId());
    userInfo.setIsAdmin(user.getIsAdmin());
    String newCookie = userInfo.buildCookie(false);
    LOG.info("newCookie = " + newCookie);
    if (newCookie != null) {
      Cookie cook = new Cookie("AppInventor", newCookie);
      cook.setPath("/");
      resp.addCookie(cook);
    }

    String uri = buildUri("/", locale, repo, galleryId);
    resp.sendRedirect(uri);
  }

  public void destroy() {
    super.destroy();
  }

  private static HashMap<String, String> getQueryMap(String query)  {
    HashMap<String, String> map = new HashMap<String, String>();
    if (query == null || query.equals("")) {
      return map;               // Empty map
    }
    String[] params = query.split("&");
    for (String param : params)  {
      String [] nvpair = param.split("=");
      if (nvpair.length <= 1) {
        map.put(nvpair[0], "");
      } else
        map.put(nvpair[0], URLDecoder.decode(nvpair[1]));
    }
    return map;
  }

  // Note: Urls in this servlet are of the form /login/<param>/<page>
  // The page identifier is *after* the parameter, if there is one.

  private String getPage(HttpServletRequest req) {
    String [] components = req.getRequestURI().split("/");
    return components[components.length-1];
  }

  private String getParam(HttpServletRequest req) {
    String [] components = req.getRequestURI().split("/");
    if (components.length < 2)
      return null;
    return components[components.length-2];
  }

  private String trimPage(HttpServletRequest req) {
    String [] components = req.getRequestURL().toString().split("/");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < components.length-1; i++)
      sb.append(components[i] + "/");
    return sb.toString();
  }

  private void fail(HttpServletRequest req, HttpServletResponse resp, String error) throws IOException {
    resp.sendRedirect("/login/?error=" + error);
    return;
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
      stream.write("email=" + URLEncoder.encode(email) + "&url=" + URLEncoder.encode(url) +
          "&pass=" + password.get() + "&locale=" + locale);
      stream.flush();
      stream.close();
      int responseCode = 0;
      responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        LOG.warning("mailserver responded with code = " + responseCode);
        // Nothing else we can do here...
      }
    } catch (MalformedURLException e) {
    } catch (IOException e) {
    }
  }

  private PrintWriter setCookieOutput(OdeAuthFilter.UserInfo userInfo, HttpServletResponse resp)
    throws IOException {
    if (userInfo != null) {     // if we never had logged in, this will be null!
      String newCookie = userInfo.buildCookie(true);
      if (newCookie != null) {
        Cookie cook = new Cookie("AppInventor", newCookie);
        cook.setPath("/");
        resp.addCookie(cook);
      }
    }
    resp.setContentType("text/html; charset=utf-8");
    PrintWriter out = resp.getWriter();
    return out;
  }

  private String buildUri(String uri, String locale, String repo, String galleryId) {
    String separator = "?";
    if (locale != null && !locale.equals("")) {
      uri += separator + "locale=" + locale;
      separator = "&";
    }
    if (repo != null && !repo.equals("")) {
      uri += separator + "repo=" + repo;
      separator = "&";
    }
    if (galleryId != null && !galleryId.equals("")) {
      uri += separator + "galleryId=" + galleryId;
    }
    return (uri);
  }

}
