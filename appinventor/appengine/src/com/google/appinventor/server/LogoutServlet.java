// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Logout handler
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class LogoutServlet extends OdeServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.sendRedirect(UserServiceFactory.getUserService().createLogoutURL("/"));
    res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
  }
}
