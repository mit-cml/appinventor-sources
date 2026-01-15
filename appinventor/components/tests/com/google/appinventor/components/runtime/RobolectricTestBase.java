// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.view.View;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.tileprovider.util.ShadowStorageUtils;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import org.junit.Before;
import org.junit.BeforeClass; // Added this tool to run code only once
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.util.Scheduler.IdleState;

import java.util.concurrent.TimeUnit;

/**
 * Base class for tests run with Robolectric.
 * Optimized to reuse a single Form instance across all tests.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest="tests/AndroidManifest.xml",
    shadows = {ShadowStorageUtils.class, ShadowEventDispatcher.class, ShadowAsynchUtil.class})
public class RobolectricTestBase {

  // This variable now stays in memory for the whole test run
  private static Form sharedForm;

  private static class FakeForm extends Form {
    @Override
    protected void $define() {}

    @Override
    public void dispatchErrorOccurredEvent(Component component, String functionName, int errorCode, Object... args) {
      String message = ErrorMessages.formatMessage(errorCode, args);
      ShadowEventDispatcher.dispatchEvent(this.$form(), "ErrorOccurred", component, functionName, errorCode, message);
    }
  }

  private static class FakeReplForm extends ReplForm {
    @Override
    protected void $define() {}

    @Override
    public void dispatchErrorOccurredEvent(Component component, String functionName, int errorCode, Object... args) {
      String message = ErrorMessages.formatMessage(errorCode, args);
      ShadowEventDispatcher.dispatchEvent(this.$form(), "ErrorOccurred", component, functionName, errorCode, message);
    }
  }

  public Form getForm() {
    return sharedForm;
  }

  /**
   * This method runs ONLY ONCE at the very beginning.
   * It builds the "Fake Phone" (Form) that all tests will share.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    if (sharedForm == null) {
      shadowOf(Looper.getMainLooper()).getScheduler().setIdleState(IdleState.PAUSED);
      // Build the activity once
      ActivityController<FakeForm> activityController = Robolectric.buildActivity(FakeForm.class).setup();
      sharedForm = activityController.get();
      sharedForm.DefaultFileScope(FileScope.Legacy);
      
      // Simulate layout once
      View v = sharedForm.getFrameLayout();
      v.layout(0, 0, 240, 320);
      v.measure(240, 320);
      v.invalidate();
      v.requestLayout();
      shadowOf(Looper.getMainLooper()).getScheduler().setIdleState(IdleState.UNPAUSED);
    }
  }

  /**
   * This method runs before EVERY test.
   * It no longer builds a phone; it just clears old messages (Light Cleaning).
   */
  @Before
  public void setUp() {
    ShadowEventDispatcher.clearEvents(); // Start with a clean slate
  }

  public void setUpAsRepl() {
    // Note: If a test specifically needs the REPL form, 
    // it will still use the existing logic, but most use FakeForm.
    setUpForm(FakeReplForm.class);
  }

  private <T extends Form> void setUpForm(Class<T> clazz) {
    // Keep this for specialized setup if needed
    shadowOf(Looper.getMainLooper()).getScheduler().setIdleState(IdleState.PAUSED);
    ActivityController<T> activityController = Robolectric.buildActivity(clazz).setup();
    sharedForm = activityController.get();
    sharedForm.DefaultFileScope(FileScope.Legacy);
    View v = sharedForm.getFrameLayout();
    v.layout(0, 0, 240, 320);
    v.measure(240, 320);
    v.invalidate();
    v.requestLayout();
    shadowOf(Looper.getMainLooper()).getScheduler().setIdleState(IdleState.UNPAUSED);
    ShadowEventDispatcher.clearEvents();
  }

  protected void runAllEvents() {
    shadowOf(Looper.getMainLooper()).getScheduler().advanceToLastPostedRunnable();
  }

  protected void runAllAsynchronousCommandsAndEvents() {
    ShadowAsynchUtil.runAllPendingRunnables();
    runAllEvents();
  }

  protected void advance(int millis) {
    shadowOf(Looper.getMainLooper()).getScheduler().advanceBy(millis, TimeUnit.MILLISECONDS);
  }
}