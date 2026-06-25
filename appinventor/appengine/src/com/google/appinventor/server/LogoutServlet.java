// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Logout handler
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class LogoutServlet extends OdeServlet {

  private static final Flag<String> logoutUrl = Flag.createFlag("logout.url", "");

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

    Cookie cookie = new Cookie("AppInventor", null);
    cookie.setPath("/");
    cookie.setMaxAge(0);        // This should cause it to be tossed immediately
    res.addCookie(cookie);

    if (!logoutUrl.get().isEmpty()) {
      res.sendRedirect(logoutUrl.get());
    } else {
      res.sendRedirect("/");
    }

  }

}
