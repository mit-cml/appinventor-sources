// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.server.util;

import com.google.common.annotations.VisibleForTesting;

import javax.servlet.http.HttpServletResponse;

/**
 * Methods for setting HTTP cache-control headers on responses.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class CacheHeadersImpl implements CacheHeaders {

  @VisibleForTesting
  static final String TIME_IN_THE_PAST = "Fri, 01 Jan 1990 00:00:00 GMT";

  /* (non-Javadoc)
   * @see com.google.appinventor.server.util.CacheHeaders#setNotCacheable(javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void setNotCacheable(HttpServletResponse resp) {
    // TODO(user): check that this works on App Engine
    resp.setHeader("Cache-Control","no-cache, no-store, max-age=0, must-revalidate");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", TIME_IN_THE_PAST);
  }

}
