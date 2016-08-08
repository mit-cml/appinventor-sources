// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
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

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  // Whether this server should use a whitelist to determine who can
  // access it. Value is specified in the <system-properties> section
  // of appengine-web.xml.
  @VisibleForTesting
  static final Flag<Boolean> useWhitelist = Flag.createFlag("use.whitelist", false);
  static final Flag<String> sessionKeyFile = Flag.createFlag("session.keyfile", "WEB-INF/authkey");
  static final Flag<Integer> idleTimeout = Flag.createFlag("session.idletimeout", 120);
  static final Flag<Integer> renewTime = Flag.createFlag("session.renew", 30);

  private final LocalUser localUser = LocalUser.getInstance();

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
    // String userid = (String) httpRequest.getSession().getAttribute("userid");
    // Object isReadOnlyObject = httpRequest.getSession().getAttribute("readonly");
    // boolean isReadOnly = false;
    // if (isReadOnlyObject != null) {
    //   isReadOnly = (boolean) isReadOnlyObject;
    // }
    // LOG.info("isReadOnly = " + isReadOnly);
    // if (userid == null) {        // Invalid Login
    //   LOG.info("userid is null on login.");
    //   httpResponse.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
    //   return;
    // }

    // Use Local Authentication
    UserInfo userInfo = getUserInfo(httpRequest);
    if (userInfo == null) {        // Invalid Login
      LOG.info("uinfo is null on login.");
      httpResponse.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
      return;
    }

    String userId = userInfo.userId;
    boolean isAdmin = userInfo.isAdmin;
    boolean isReadOnly = userInfo.isReadOnly;

//    Object oIsAdmin = httpRequest.getSession().getAttribute("isadmin");
//    if (oIsAdmin != null) {
//      isAdmin = (boolean) oIsAdmin;
//    }

    doMyFilter(userInfo, isAdmin, isReadOnly, httpRequest, httpResponse, chain);
  }

  @VisibleForTesting
  void doMyFilter(UserInfo userInfo, boolean isAdmin, boolean isReadOnly,
    HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {

    // Setup the user object for OdeRemoteServiceServlet
    setUserFromUserId(userInfo.userId, isAdmin, isReadOnly);

    // If using local login, we *must* have an email address because that is how we
    // find the UserData object.
    String lemail = localUser.getUserEmail();
    if (lemail.equals("")) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
        LOG.info("Renewing the authentication Cookie");
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
  void setUserFromUserId(String userId, boolean isAdmin, boolean isReadOnly) {
    User user = storageIo.getUser(userId);
    if (!user.getIsAdmin() && isAdmin) {
      user.setIsAdmin(true);    // If session says they are an admin (which is the case
                                // if they are a Google Account with Developer access
    }
    user.setReadOnly(isReadOnly);
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
            LOG.info("getUserInfo: rawCookie = " + rawData);
            Crypter crypter = getCrypter();
            CookieAuth.cookie cookieToken = CookieAuth.cookie.parseFrom(
              crypter.decrypt(Base64Coder.decode(rawData)));
            UserInfo uInfo = new UserInfo();
            uInfo.userId = cookieToken.getUuid();
            uInfo.ts = cookieToken.getTs();
            uInfo.isAdmin = cookieToken.getIsAdmin();
            uInfo.isReadOnly = cookieToken.getIsReadOnly();
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
