// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import com.google.common.annotations.VisibleForTesting;

import javax.servlet.http.HttpServletResponse;

/**
 * Methods for setting HTTP cache-control headers on responses.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class CacheHeadersImpl implements CacheHeaders {

  @VisibleForTesting
  static final String TIME_IN_THE_PAST = "Fri, 01 Jan 1990 00:00:00 GMT";

  @Override
  public void setNotCacheable(HttpServletResponse resp) {
    resp.setHeader("Cache-Control","no-cache, no-store, max-age=0, must-revalidate");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", TIME_IN_THE_PAST);
  }

  @Override
  public void setCacheablePrivate(HttpServletResponse resp) {
    long nowMillis =  System.currentTimeMillis();
    resp.setDateHeader("Date", nowMillis);
    resp.setDateHeader("Expires", nowMillis);  // Forces HTTP/1.0 not to cache
    resp.setHeader("Cache-Control", "private,max-age=0");
  }
}
