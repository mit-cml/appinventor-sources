// Copyright 2008 Google Inc. All Rights Reserved.
package com.google.appengine.demos.helloorm;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Max Ross <maxr@google.com>
 */
public class UpdatePersistenceStandard extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceStandard ps = PersistenceStandard.valueOf(req.getParameter("persistenceStandard"));
    PersistenceStandard.set(ps);
    resp.sendRedirect("/");
  }
}
