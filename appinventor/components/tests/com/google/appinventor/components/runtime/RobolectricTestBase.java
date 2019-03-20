// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.tileprovider.util.ShadowStorageUtils;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.Scheduler.IdleState;

import java.util.concurrent.TimeUnit;

/**
 * Base class for tests run with Robolectric.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest="tests/AndroidManifest.xml",
    shadows = {ShadowStorageUtils.class, ShadowEventDispatcher.class, ShadowAsynchUtil.class})
public class RobolectricTestBase {

  private Form form;

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
    return form;
  }

  @Before
  public void setUp() {
    setUpForm(FakeForm.class);
  }

  public void setUpAsRepl() {
    setUpForm(FakeReplForm.class);
  }

  private <T extends Form> void setUpForm(Class<T> clazz) {
    ShadowLooper.getShadowMainLooper().getScheduler().setIdleState(IdleState.PAUSED);
    ActivityController<T> activityController = Robolectric.buildActivity(clazz)
        .create().start().resume().visible();
    form = activityController.get();
    // Unfortunately Robolectric won't handle laying out the view hierarchy and because of how
    // we use runOnUiThread in the Initialize() method, tests will enter an infinite loop. This
    // code simulates enough of the layout process so that we don't loop forever.
    View v = ((ViewGroup) form.findViewById(android.R.id.content)).getChildAt(0);
    v = ((ViewGroup) v).getChildAt(1);  // frameLayoutWithTitle in Form
    v = ((ViewGroup) v).getChildAt(0);  // frameLayout in Form
    v.layout(0, 0, 240, 320);
    v.measure(240, 320);
    v.invalidate();
    v.requestLayout();
    ShadowLooper.getShadowMainLooper().getScheduler().setIdleState(IdleState.UNPAUSED);
    ShadowEventDispatcher.clearEvents();  // start with a clean slate
  }

  protected void runAllEvents() {
    ShadowLooper.getShadowMainLooper().getScheduler().advanceToLastPostedRunnable();
  }

  protected void advance(int millis) {
    ShadowLooper.getShadowMainLooper().getScheduler().advanceBy(millis, TimeUnit.MILLISECONDS);
  }
}
