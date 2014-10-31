// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

import junit.framework.TestCase;

/**
 * Unit tests for {@link UploadResponse}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class UploadResponseTest extends TestCase {
  public void testExtractUploadResponse() {
    String results =
        "<pre>[UPLOAD RESPONSE BEGIN]SUCCESS#DELIM#0#DELIM#1304103761370#DELIM#user project info" +
        "[UPLOAD RESPONSE END]</pre>";
    UploadResponse response = UploadResponse.extractUploadResponse(results);
    assertNotNull(response);
    assertEquals(UploadResponse.Status.SUCCESS, response.getStatus());
    assertEquals(0, response.getCount());
    assertEquals(1304103761370L, response.getModificationDate());
    assertEquals("user project info", response.getInfo());
  }

  public void testExtractUploadResponseWithBlankInfo() {
    String results =
        "<pre>[UPLOAD RESPONSE BEGIN]SUCCESS#DELIM#0#DELIM#1304103761370#DELIM#" +
        "[UPLOAD RESPONSE END]</pre>";
    UploadResponse response = UploadResponse.extractUploadResponse(results);
    assertNotNull(response);
    assertEquals(UploadResponse.Status.SUCCESS, response.getStatus());
    assertEquals(0, response.getCount());
    assertEquals(1304103761370L, response.getModificationDate());
    assertEquals("", response.getInfo());
  }

  public void testExtractUploadResponseWithWhoDatExtension() {
    String results = "<div id=\"whodiv\" style=\"display:none;\"></div>" +
        "<pre>[UPLOAD RESPONSE BEGIN]SUCCESS#DELIM#0#DELIM#1304103761370#DELIM#user project info" +
        "[UPLOAD RESPONSE END]</pre>";
    UploadResponse response = UploadResponse.extractUploadResponse(results);
    assertNotNull(response);
    assertEquals(UploadResponse.Status.SUCCESS, response.getStatus());
    assertEquals(0, response.getCount());
    assertEquals(1304103761370L, response.getModificationDate());
    assertEquals("user project info", response.getInfo());
  }

  public void testExtractUploadResponseWithInternetExplorerInterference() {
    // Internet Explorer sometimes changes <pre> to <PRE> and </pre> to </PRE>.
    String results =
        "<PRE>[UPLOAD RESPONSE BEGIN]SUCCESS#DELIM#0#DELIM#1304103761370#DELIM#user project info" +
        "[UPLOAD RESPONSE END]</PRE>";
    UploadResponse response = UploadResponse.extractUploadResponse(results);
    assertNotNull(response);
    assertEquals(UploadResponse.Status.SUCCESS, response.getStatus());
    assertEquals(0, response.getCount());
    assertEquals(1304103761370L, response.getModificationDate());
    assertEquals("user project info", response.getInfo());
  }

}
