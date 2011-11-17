// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.server.util;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for dealing with If-Modified-Since and Last-Modified headers
 * for servlet requests and responses.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class ModifiedHeaders {
  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();
  // length of time to cache responses

  private static long buildDate = 0;

  /**
   * Check the "If-Modified-Since" header in req (if it exists). If it exists
   * and the date is after our build date then return true (=>not modified by
   * this build). Otherwise, return false.
   * @param req
   * @return true iff we're sure that req is for a resource that wasn't
   *   modified by our build.
   */
  public static boolean notModified(HttpServletRequest req) {
    long ifModDate = req.getDateHeader("If-Modified-Since");
    if (buildDate > 0 && ifModDate > 0) {
      if (ifModDate >= buildDate) {
        return true;
      }
    }
    return false;
  }

  /**
   * If we know our build date, set the "Last-Modified" header in resp to be
   * our build date. Sets cache control to be cacheable-private.
   * @param resp
   */
  public static void setHeaders(HttpServletResponse resp) {
    if (getBuildTimestamp() != 0) {
      resp.setDateHeader("Last-Modified", buildDate);
      // TODO(user): is setCacheablePrivate with a duration of 0 the
      // same as setNotCacheable? I'm guessing it is (or that notCacheable
      // will do). Check this. If we need setCacheablePrivate we'll need to
      // implement it.
      // CACHE_HEADERS.setCacheablePrivate(resp, new Duration(0), null);
      CACHE_HEADERS.setNotCacheable(resp);
    }
  }

  private static long getBuildTimestamp() {
    if (buildDate != 0) return buildDate;
    buildDate = BuildData.getTimestamp();
    return buildDate;
  }
}
