// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2026 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests Sharing.java.
 *
 */
public class SharingTest extends RobolectricTestBase {

  private Sharing sharing;

  @Before
  public void setUp() {
    super.setUp();
    sharing = new Sharing(getForm());
  }

  @Test
  public void testShareFileReportsShareFile() {
    sharing.ShareFile("/nonexistent/file.txt");
    Object[] args = ShadowEventDispatcher.getArgumentsForEventFired(getForm(), "ErrorOccurred");
    assertEquals("ShareFile", args[1]);
    assertEquals(ErrorMessages.ERROR_FILE_NOT_FOUND_FOR_SHARING, args[2]);
  }

  @Test
  public void testShareFileWithMessageReportsShareFileWithMessage() {
    sharing.ShareFileWithMessage("/nonexistent/file.txt", "hello");
    Object[] args = ShadowEventDispatcher.getArgumentsForEventFired(getForm(), "ErrorOccurred");
    assertEquals("ShareFileWithMessage", args[1]);
    assertEquals(ErrorMessages.ERROR_FILE_NOT_FOUND_FOR_SHARING, args[2]);
  }

  @Test
  public void testShareFileWithMessageEmptyMessageReportsShareFileWithMessage() {
    sharing.ShareFileWithMessage("/nonexistent/file.txt", "");
    Object[] args = ShadowEventDispatcher.getArgumentsForEventFired(getForm(), "ErrorOccurred");
    assertEquals("ShareFileWithMessage", args[1]);
    assertEquals(ErrorMessages.ERROR_FILE_NOT_FOUND_FOR_SHARING, args[2]);
  }
}
