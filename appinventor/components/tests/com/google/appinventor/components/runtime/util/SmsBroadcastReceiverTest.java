// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.google.appinventor.components.runtime.RobolectricTestBase;
import org.junit.Test;
import org.robolectric.shadows.ShadowPackageManager;

/**
 * Tests how {@link SmsBroadcastReceiver} finds the app's main activity when the application
 * package differs from the package of the form classes.
 */
public class SmsBroadcastReceiverTest extends RobolectricTestBase {
  private static final String SCREEN1 = "appinventor.ai_test.SmsApp.Screen1";

  @Test
  public void testMainActivityFromLaunchIntent() throws Exception {
    Context context = getForm();
    ComponentName screen1 = new ComponentName(context.getPackageName(), SCREEN1);
    ShadowPackageManager packageManager = shadowOf(context.getPackageManager());
    packageManager.addActivityIfNotPresent(screen1);
    IntentFilter launcher = new IntentFilter(Intent.ACTION_MAIN);
    launcher.addCategory(Intent.CATEGORY_LAUNCHER);
    packageManager.addIntentFilterForActivity(screen1, launcher);

    assertEquals(SCREEN1, SmsBroadcastReceiver.getMainActivityClassName(context));
  }

  @Test
  public void testMainActivityWithoutLaunchIntent() {
    Context context = getForm();
    shadowOf(context.getPackageManager()).addActivityIfNotPresent(
        new ComponentName(context.getPackageName(), SCREEN1));

    assertEquals(SCREEN1, SmsBroadcastReceiver.getMainActivityClassName(context));
  }
}
