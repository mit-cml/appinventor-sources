// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import javax.servlet.http.HttpSession;

/**
 * A Singleton providing access to the Java HttpSession Object
 * This permits us to set fields in the session from the AdminInfoService
 * (and others if the need arises).
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class LocalSession {

  private ThreadLocal<HttpSession> session = new ThreadLocal<HttpSession>();

  /**
   * Returns the singleton LocalSession instance.
   *
   * @return localSession instance
   */
  public static LocalSession getInstance() {
    return LocalSessionInstanceHolder.INSTANCE;
  }

  private static class LocalSessionInstanceHolder {
    private LocalSessionInstanceHolder() {} // not to be instantiated
    private static final LocalSession INSTANCE = new LocalSession();
  }

  public void set(HttpSession session) {
    this.session.set(session);
  }

  public HttpSession getSession() {
    return session.get();
  }
}

