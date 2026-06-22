// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * Tests for {@link LmsHttp}. Only the pure {@code jsonField} parser is unit
 * tested here; the networked methods are exercised by the live handshake.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsHttpTest extends TestCase {

  public void testJsonFieldReturnsValue() throws Exception {
    assertEquals("ya29.abc",
        LmsHttp.jsonField("{\"access_token\":\"ya29.abc\",\"expires_in\":3599}", "access_token"));
  }

  public void testJsonFieldAbsentReturnsNull() throws Exception {
    assertNull(LmsHttp.jsonField("{\"access_token\":\"ya29.abc\"}", "refresh_token"));
  }

  public void testJsonFieldEmptyReturnsNull() throws Exception {
    // optString yields "" for a present-but-empty field; jsonField normalizes that to null.
    assertNull(LmsHttp.jsonField("{\"id\":\"\"}", "id"));
  }

  public void testJsonFieldMalformedThrows() {
    try {
      LmsHttp.jsonField("not json", "id");
      fail();
    } catch (IOException e) {
      // expected
    }
  }
}
