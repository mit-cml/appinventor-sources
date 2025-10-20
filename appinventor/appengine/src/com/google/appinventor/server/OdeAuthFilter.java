// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.cookieauth.CookieAuth;
import java.io.Serializable;

import com.google.appinventor.server.flags.Flag;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.user.User;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

import org.keyczar.util.Base64Coder;


/**
 * An authentication filter that uses Google Accounts for logged-in users.
 *
 * @author markf@google.com (Mark Friedman)
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class OdeAuthFilter implements Filter {

  public OdeAuthFilter() {}

  private static final Logger LOG = Logger.getLogger(OdeAuthFilter.class.getName());

  private static Crypter crypter = null; // accessed through getCrypter only
  private static final Object crypterSync = new Object();

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  // Whether this server should use a whitelist to determine who can
  // access it. Value is specified in the <system-properties> section
  // of appengine-web.xml.
  @VisibleForTesting
  static final Flag<Boolean> useWhitelist = Flag.createFlag("use.whitelist", false);
  static final Flag<String> sessionKeyFile = Flag.createFlag("session.keyfile", "WEB-INF/authkey");
  static final Flag<Integer> idleTimeout = Flag.createFlag("session.idletimeout", 120);
  static final Flag<Integer> renewTime = Flag.createFlag("session.renew", 30);

  private final LocalUser localUser = LocalUser.getInstance();
  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  /**
   * Filters using Google Accounts
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      throw new ServletException("Unsupported request type.");
    }

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Use Local Authentication
    UserInfo userInfo = getUserInfo(httpRequest);
    if (userInfo == null) {        // Invalid Login
      if (DEBUG) {
        LOG.info("uinfo is null on login.");
      }
      // If the URI starts with /ode, then we are being invoked through
      // the App Inventor client. In that case we are in an XMLHttpRequest
      // (aka ajax) so we cannot send a redirect to the login page
      // instead we return SC_PRECONDITION_FAILED which tips off the
      // client that it needs to reload itself to the login page.
      String uri = httpRequest.getRequestURI();
      if (DEBUG) {
        LOG.info("Not Logged In: uri = " + uri);
      }
      if (uri.startsWith("/ode")) {
        httpResponse.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
      } else {
        httpResponse.sendRedirect("/login?redirect=" + uri);
      }
      return;
    }

    String userId = userInfo.userId;
    boolean isAdmin = userInfo.isAdmin;
    boolean isReadOnly = userInfo.isReadOnly;
    long oneProjectId = userInfo.oneProjectId;
    String fauxProjectName = userInfo.fauxProjectName;
    String fauxAccountName = userInfo.fauxAccountName;

//    Object oIsAdmin = httpRequest.getSession().getAttribute("isadmin");
//    if (oIsAdmin != null) {
//      isAdmin = (boolean) oIsAdmin;
//    }

    doMyFilter(userInfo, isAdmin, isReadOnly, oneProjectId, fauxProjectName, fauxAccountName, httpRequest, httpResponse, chain);
  }

  @VisibleForTesting
  void doMyFilter(UserInfo userInfo, boolean isAdmin, boolean isReadOnly,
    long oneProjectId, String fauxProjectName, String fauxAccountName,
    HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {

    // Setup the user object for OdeRemoteServiceServlet
    setUserFromUserId(userInfo.userId, isAdmin, isReadOnly, oneProjectId, fauxProjectName, fauxAccountName);

    // If using local login, we *must* have an email address because that is how we
    // find the UserData object.
    String lemail = localUser.getUserEmail();
    if (lemail.equals("")) {
      // We send a SC_PRECONDITION_FAILED which will cause the login page to
      // be displayed (or the use of Google Authentication if that is the only
      // mechanism enabled). This should *not* happen in production. However
      // it happens all the time in development when people do an "ant clean"
      // followed by an "ant". This results in their development datastore being
      // erased. But their browser still contains a valid authentication cookie,
      // but the userId no longer exists. It is then automatically created
      // in code called before here, but the e-mail address is not set. So
      // we error out here.
      response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
      return;
    }

    try {
      if (useWhitelist.get() && !isUserWhitelisted()) {
        writeWhitelistErrorMessage(response);
        // This indicates to the client side code that the user is not on the whitelist.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;  // This blocks further processing of the request.
      }
      // If user hasn't accepted terms of service, redirect them,
      // unless they're submitting the acceptance request.
      if (!localUser.getUserTosAccepted() && !isReadOnly &&
          !request.getRequestURI().endsWith(ServerLayout.ACCEPT_TOS_SERVLET)) {
        // This indicates to the client side code that the user needs to accept
        // the terms of service. We don't send the redirect here because
        // it isn't understood properly by GWT RPC
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      String newCookie = userInfo.buildCookie(true);
      if (newCookie != null) {  // If we get a value here, it is time to renew
                                // the Cookie
        if (DEBUG) {
          LOG.info("Renewing the authentication Cookie");
        }
        Cookie cook = new Cookie("AppInventor", newCookie);
        cook.setPath("/");
        response.addCookie(cook);
      }
      chain.doFilter(request, response);
    } finally {
      removeUser();
    }
  }

  @VisibleForTesting
  boolean isUserWhitelisted() {
    //return whitelist.isInWhitelist(localUser);
    return storageIo.checkWhiteList(localUser.getUserEmail());
  }

  @VisibleForTesting
  void writeWhitelistErrorMessage(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain; charset=utf-8");
    PrintWriter out = response.getWriter();
    out.print("You are attempting to connect to this App Inventor service with the login ID:\n\n" +
        localUser.getUserEmail() + "\n\nThat ID has not been authorized to use this service.  " +
        "If you believe that you were in fact given authorization, you should contact the " +
        "service operator.");
  }

  /*
   * Sets the user for the current thread according to the given userId.
   *
   * <p>This method is called from {@link WebStartFileServlet} with the userId
   * that was encrypted in the URL.
   */
  void setUserFromUserId(String userId, boolean isAdmin, boolean isReadOnly, long oneProjectId,
    String projectName, String displayAccountName) {
    User user = storageIo.getUser(userId);
    if (!user.getIsAdmin() && isAdmin) {
      user.setIsAdmin(true);    // If session says they are an admin (which is the case
                                // if they are a Google Account with Developer access
    }
    user.setReadOnly(isReadOnly);
    user.setOneProjectId(oneProjectId);
    user.setFauxProjectName(projectName);
    LOG.severe("OdeAuthFilter: displayAccountName = " + displayAccountName);
    if (displayAccountName != null && !displayAccountName.isEmpty()) {
      user.setUserEmail(displayAccountName);
    }
    localUser.set(user);
  }

  /*
   * Clears the user for the current thread.
   *
   * <p>This method is called from {@link #doMyFilter} above.
   *
   * <p>This method is called from {@link WebStartFileServlet}, a non-filtered
   * servlet.
   */
  @VisibleForTesting
  void removeUser() {
    localUser.set(null);
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }

  // --- Support Routines for encrypted cookies --- //

  public static class UserInfo implements Serializable {
    String userId = "";
    boolean isAdmin = false;
    boolean isReadOnly = false;
    long ts;
    long oneProjectId = 0;
    String fauxProjectName = "";
    String fauxAccountName = "";

    transient boolean modified = false;

    public UserInfo() {
      this.ts = System.currentTimeMillis();
    }

    public boolean getReadOnly() {
      return this.isReadOnly;
    }

    public UserInfo(String userId, boolean isAdmin) {
      this.userId = userId;
      this.isAdmin = isAdmin;
      this.ts = System.currentTimeMillis();
    }

    public void setUserId(String userId) {
      this.userId = userId;
      modified = true;
    }

    public void setReadOnly(boolean value) {
      this.isReadOnly = value;
      modified = true;
    }

    public String getUserId() {
      return userId;
    }

    public boolean getIsAdmin() {
      return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
      this.isAdmin = isAdmin;
      modified = true;
    }

    public void setOneProjectId(long projectId) {
      this.oneProjectId = projectId;
    }

    public long getOneProjectId() {
      return oneProjectId;
    }

    public void setFauxProjectName(String fauxProjectName) {
      this.fauxProjectName = fauxProjectName;
    }

    public String getFauxProjectName() {
      return fauxProjectName;
    }

    public void setFauxAccountName(String fauxAccountName) {
      this.fauxAccountName = fauxAccountName;
    }

    public String getFauxAccountName() {
      return fauxAccountName;
    }

    public String buildCookie(boolean ifNeeded) {
      try {
        long offset = System.currentTimeMillis() - this.ts;
        offset /= 1000;
        if (offset > (60*renewTime.get())) {    // Renew if it is time
          modified = true;
          ts = System.currentTimeMillis();
        }
        if (!ifNeeded || modified) {
          Crypter crypter = getCrypter();
          CookieAuth.cookie cookie = CookieAuth.cookie.newBuilder()
            .setUuid(this.userId)
            .setTs(this.ts)
            .setIsAdmin(this.isAdmin)
            .setOneProjectId(this.oneProjectId)
            .setDisplayprojectname(this.fauxProjectName)
            .setDisplayaccountname(this.fauxAccountName)
            .setIsReadOnly(this.isReadOnly).build();
          return Base64Coder.encode(crypter.encrypt(cookie.toByteArray()));
        } else {
          return null;
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    // Verify the timestamp
    boolean isValid() {
      long offset = System.currentTimeMillis() - this.ts;
      offset /= 1000;

      // Reject if older then idleTimeout (minutes) or if greater then
      // 60 seconds in the future. We allow for 60 seconds in the
      // future to deal with potential clock skew between app inventor
      // servers

      if (offset < -60 || offset > (60*idleTimeout.get())) {
        return false;
      } else {
        return true;
      }
    }
  }

  public static UserInfo getUserInfo(HttpServletRequest request) {
    try {
      Cookie [] cookies = request.getCookies();
      if (cookies != null)
        for (Cookie cookie : cookies) {
          if ("AppInventor".equals(cookie.getName())) {
            String rawData = cookie.getValue();
            if (DEBUG) {
              LOG.info("getUserInfo: rawCookie = " + rawData);
            }
            Crypter crypter = getCrypter();
            CookieAuth.cookie cookieToken = CookieAuth.cookie.parseFrom(
              crypter.decrypt(Base64Coder.decode(rawData)));
            UserInfo uInfo = new UserInfo();
            uInfo.userId = cookieToken.getUuid();
            uInfo.ts = cookieToken.getTs();
            uInfo.isAdmin = cookieToken.getIsAdmin();
            uInfo.isReadOnly = cookieToken.getIsReadOnly();
            uInfo.oneProjectId = cookieToken.getOneProjectId();
            uInfo.fauxProjectName = cookieToken.getDisplayprojectname();
            uInfo.fauxAccountName = cookieToken.getDisplayaccountname();
            if (uInfo.isValid()) {
              return uInfo;
            } else {
              return null;
            }
          }
        }
      return null;
    } catch (KeyczarException e) {
      LOG.log(Level.SEVERE, "Error parsing provided cookie", e);
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Crypter getCrypter() throws KeyczarException {
    synchronized(crypterSync) {
      if (crypter != null) {
        return crypter;
      } else {
        crypter = new Crypter(sessionKeyFile.get());
        return crypter;
      }
    }
  }
}
