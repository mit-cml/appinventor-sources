// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;

/**
 * Tests for the Sharing component.
 */
public class SharingTest extends RobolectricTestBase {

  @Rule
  public final TemporaryFolder tempFolder = TemporaryFolder.builder().assureDeletion().build();

  private Sharing sharing;

  @Before
  public void setUp() {
    super.setUp();
    sharing = new Sharing(getForm());
  }

  @Test
  public void testMissingFileReportsInvokedBlock() {
    sharing.ShareFile("does_not_exist.png");
    assertFileNotFound("ShareFile", getForm().getDefaultPath("does_not_exist.png"));

    ShadowEventDispatcher.clearEvents();
    sharing.ShareFileWithMessage("does_not_exist.png", "hi");
    assertFileNotFound("ShareFileWithMessage", getForm().getDefaultPath("does_not_exist.png"));
  }

  @Test
  public void testEmptyMessageOnMissingFileReportsShareFileWithMessage() {
    sharing.ShareFileWithMessage("does_not_exist.png", "");
    assertFileNotFound("ShareFileWithMessage", getForm().getDefaultPath("does_not_exist.png"));
  }

  @Test
  public void testMissingPathsReportInvokedBlock() {
    sharing.ShareFile("does_not_exist.png");
    assertFileNotFound("ShareFile", getForm().getDefaultPath("does_not_exist.png"));

    ShadowEventDispatcher.clearEvents();
    sharing.ShareFileWithMessage("/does_not_exist.png", "hi");
    assertFileNotFound("ShareFileWithMessage", "file:///does_not_exist.png");

    ShadowEventDispatcher.clearEvents();
    sharing.ShareFile("file:///does_not_exist.png");
    assertFileNotFound("ShareFile", "file:///does_not_exist.png");
  }

  @Test
  public void testExistingFileStartsSendIntent() throws IOException {
    ResolveInfo info = new ResolveInfo();
    info.activityInfo = new ActivityInfo();
    info.activityInfo.name = "ShareActivity";
    info.activityInfo.packageName = "com.example";
    info.activityInfo.applicationInfo = new ApplicationInfo();
    info.activityInfo.applicationInfo.packageName = "com.example";
    Shadows.shadowOf(getForm().getPackageManager()).addResolveInfoForIntent(
        new Intent(Intent.ACTION_SEND), info);

    File file = tempFolder.newFile("share.png");
    String path = file.getAbsolutePath();

    sharing.ShareFile(path);
    assertSendIntent(null);

    sharing.ShareFileWithMessage(path, "");
    assertSendIntent(null);

    sharing.ShareFileWithMessage(path, "hi");
    assertSendIntent("hi");

    ShadowEventDispatcher.assertEventNotFired(getForm(), "ErrorOccurred");
  }

  private void assertFileNotFound(String methodName, String file) {
    Object[] args = ShadowEventDispatcher.getArgumentsForEventFired(getForm(), "ErrorOccurred");
    assertEquals(sharing, args[0]);
    assertEquals(methodName, args[1]);
    assertEquals(ErrorMessages.ERROR_FILE_NOT_FOUND_FOR_SHARING, args[2]);
    assertEquals(ErrorMessages.formatMessage(ErrorMessages.ERROR_FILE_NOT_FOUND_FOR_SHARING,
        new Object[] {file}), args[3]);
  }

  private void assertSendIntent(String message) {
    ShadowActivity.IntentForResult started =
        Shadows.shadowOf(getForm()).getNextStartedActivityForResult();
    assertNotNull(started);
    Intent intent = started.intent;
    assertEquals(Intent.ACTION_SEND, intent.getAction());
    assertNotNull(intent.getParcelableExtra(Intent.EXTRA_STREAM));
    if (message == null) {
      assertFalse(intent.hasExtra(Intent.EXTRA_TEXT));
    } else {
      assertEquals(message, intent.getStringExtra(Intent.EXTRA_TEXT));
    }
  }
}
