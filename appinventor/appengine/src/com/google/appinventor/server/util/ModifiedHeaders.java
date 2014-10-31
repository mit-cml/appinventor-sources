// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for dealing with "If-Modified-Since" and
 * "Last-Modified" headers for servlet requests and responses.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class ModifiedHeaders {
  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();

  /**
   * Compares the "If-Modified-Since" header in this request (ir present) to
   * the last build date (if known) in order to determine whether the requested
   * data has been modified since the prior request.
   *
   * @param req the request
   * @return {@code true} iff we're sure that request is for a resource that
   *         has not been modified since the prior request
   */
  public static boolean notModified(HttpServletRequest req) {
    long ifModDate = req.getDateHeader("If-Modified-Since");
    if (BuildData.getTimestamp() > 0 && ifModDate > 0) {
      if (ifModDate >= BuildData.getTimestamp()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets headers such that, if the last build time is known, it is included in
   * the "Last-Modified" header and validation is required for reuse.  If the
   * build time is not known, this makes the response uncacheable.
   *
   * @param resp the response
   */
  public static void setHeaders(HttpServletResponse resp) {
    if (BuildData.getTimestamp() == 0) {
      CACHE_HEADERS.setNotCacheable(resp);
    } else {
      resp.setDateHeader("Last-Modified", BuildData.getTimestamp());
      CACHE_HEADERS.setCacheablePrivate(resp);
    }
  }
}
