// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.server.util;

import javax.servlet.http.HttpServletResponse;

/**
 * Interface for setting HTTP cache-control headers on responses.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public interface CacheHeaders {

  /**
   * Marks the given response as not cacheable.
   *
   * @param resp HTTP response
   */
  void setNotCacheable(HttpServletResponse resp);
}

