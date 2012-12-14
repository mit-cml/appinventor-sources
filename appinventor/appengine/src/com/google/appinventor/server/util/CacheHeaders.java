// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
   * @param resp the response
   */
  void setNotCacheable(HttpServletResponse resp);

  /**
   * Marks the given response as only cacheable for the current user and
   * needing to be revalidated.
   *
   * @param resp the response
   */
  void setCacheablePrivate(HttpServletResponse resp);
}
