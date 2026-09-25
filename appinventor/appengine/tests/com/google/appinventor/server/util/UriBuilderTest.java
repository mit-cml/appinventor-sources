// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import junit.framework.TestCase;

/**
 * Tests that UriBuilder starts the query string with a "?" when the base has none, but joins with
 * an "&" when the base already carries a query, so it never emits a second "?" that corrupts the
 * URL.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class UriBuilderTest extends TestCase {

  /** With no query in the base, the first parameter opens the query with "?". */
  public void testFirstParamOpensQuery() {
    assertEquals("http://example.org/path?a=1&b=2",
        new UriBuilder("http://example.org/path").add("a", "1").add("b", "2").build());
  }

  /** With a query already in the base, the first parameter joins it with "&", not a second "?". */
  public void testBaseWithExistingQueryJoinsWithAmpersand() {
    assertEquals("http://example.org/path?fixed=1&a=2",
        new UriBuilder("http://example.org/path?fixed=1").add("a", "2").build());
  }

  /** A null value is skipped, so an absent optional parameter does not appear in the URL. */
  public void testNullValueIsSkipped() {
    assertEquals("http://example.org/path?a=1",
        new UriBuilder("http://example.org/path").add("a", "1").add("b", null).build());
  }
}
