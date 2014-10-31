// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.user.User;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An authentication filter that uses Google Accounts for logged-in users.
 *
 * @author markf@google.com (Mark Friedman)
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class OdeAuthFilter implements Filter {

  public OdeAuthFilter() {}

  private static final Logger LOG = Logger.getLogger(OdeAuthFilter.class.getName());

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  private static final UserService userService = UserServiceFactory.getUserService();

  // Note that if no whitelist exists, then no whitelist will be used.
//  private static final Whitelist whitelist = new Whitelist();
  private static final IdMap idmap = IdMap.getInstance();

  // Whether this server should use a whitelist to determine who can
  // access it. Value is specified in the <system-properties> section
  // of appengine-web.xml.
  @VisibleForTesting
  static final Flag<Boolean> useWhitelist = Flag.createFlag("use.whitelist", false);

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

    // Use Google Accounts authentication
    if (httpRequest.getUserPrincipal() == null) {
      return;   // if no principal, block the request
    }

    doMyFilter(httpRequest, httpResponse, chain);
  }

  @VisibleForTesting
  void doMyFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!setUser(request)) {
      // can't get the user info, so block further request processing
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
      if (!localUser.getUserTosAccepted() &&
          !request.getRequestURI().endsWith(ServerLayout.ACCEPT_TOS_SERVLET)) {
        // This indicates to the client side code that the user needs to accept
        // the terms of service. We don't send the redirect here because
        // it isn't understood properly by GWT RPC
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
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

  @VisibleForTesting
  boolean setUser(HttpServletRequest request) {
    com.google.appengine.api.users.User apiUser = userService.getCurrentUser();
    if (apiUser != null) {
      String userId = apiUser.getUserId();
      String email = apiUser.getEmail();
      email = idmap.get(email);	// Map the user.
      User user = storageIo.getUser(userId, email);
      user.setIsAdmin(userService.isUserAdmin());
      if (!email.equals(user.getUserEmail())) {
        user.setUserEmail(email);
      }
      localUser.set(user);
      return true;
    } else {
      return false;
    }
  }

  /*
   * Sets the user for the current thread according to the given userId.
   *
   * <p>This method is called from {@link WebStartFileServlet} with the userId
   * that was encrypted in the URL.
   */
  void setUserFromUserId(String userId) {
    User user = storageIo.getUser(userId);
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
}
