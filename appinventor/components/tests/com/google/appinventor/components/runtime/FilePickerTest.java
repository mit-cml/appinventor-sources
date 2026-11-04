// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2026 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FilePicker}.
 */
public class FilePickerTest extends RobolectricTestBase {

  private FilePicker filePicker;

  @Before
  public void setUp() {
    super.setUp();
    filePicker = new FilePicker(getForm());
    // Simulate user clicking to register requestCode
    filePicker.requestCode = 1001;
  }

  @Test
  public void testResultReturnedCanceledWithNullData() {
    filePicker.resultReturned(1001, Activity.RESULT_CANCELED, null);
    ShadowEventDispatcher.assertEventNotFired(filePicker, "AfterPicking");
    assertEquals("", filePicker.Selection());
    try {
      ShadowEventDispatcher.assertErrorOccurred();
      fail("Expected no error on cancellation");
    } catch (AssertionError e) {
      // Expected: no ErrorOccurred event
    }
  }

  @Test
  public void testResultReturnedCanceledWithEmptyIntent() {
    filePicker.resultReturned(1001, Activity.RESULT_CANCELED, new Intent());
    ShadowEventDispatcher.assertEventNotFired(filePicker, "AfterPicking");
    assertEquals("", filePicker.Selection());
    try {
      ShadowEventDispatcher.assertErrorOccurred();
      fail("Expected no error on cancellation");
    } catch (AssertionError e) {
      // Expected: no ErrorOccurred event
    }
  }

  @Test
  public void testResultReturnedOkWithoutUri() {
    filePicker.resultReturned(1001, Activity.RESULT_OK, new Intent());
    ShadowEventDispatcher.assertEventNotFired(filePicker, "AfterPicking");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_FILEPICKER_NO_URI_RETURNED);
  }

  @Test
  public void testResultReturnedWrongRequestCode() {
    filePicker.resultReturned(9999, Activity.RESULT_OK, new Intent());
    ShadowEventDispatcher.assertEventNotFired(filePicker, "AfterPicking");
    try {
      ShadowEventDispatcher.assertErrorOccurred();
      fail("Expected no error when requestCode does not match");
    } catch (AssertionError e) {
      // Expected: no ErrorOccurred event
    }
  }

  @Test
  public void testResultReturnedSuccess() {
    Intent data = new Intent();
    Uri uri = Uri.parse("content://com.android.providers.media.documents/document/image%3A123");
    data.setData(uri);
    data.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    filePicker.resultReturned(1001, Activity.RESULT_OK, data);
    ShadowEventDispatcher.assertEventFired(filePicker, "AfterPicking");
    assertEquals(uri.toString(), filePicker.Selection());
  }
}
