// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * A component for measuring elapsed time, with nanosecond accuracy, for an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ELAPSED_TIME_COMPONENT_VERSION,
    description = "A component for measuring elapsed time, with nanosecond accuracy, for an " +
    "FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftcElapsedTime.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcElapsedTime extends AndroidNonvisibleComponent
    implements Component, OnDestroyListener, Deleteable {

  private volatile ElapsedTime elapsedTime;

  /**
   * Creates a new FtcElapsedTime component.
   */
  public FtcElapsedTime(ComponentContainer container) {
    super(container.$form());

    elapsedTime = new ElapsedTime(0);

    form.registerForOnDestroy(this);
  }

  @SimpleFunction(description = "Reset the start time to now.")
  public void Reset() {
    try {
      elapsedTime.reset();
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Reset",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  /**
   * StartTime property getter.
   */
  @SimpleProperty(description = "Get the relative start time.",
      category = PropertyCategory.BEHAVIOR)
  public double StartTime() {
    try {
      return elapsedTime.startTime();
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "StartTime",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  /**
   * Time property getter.
   */
  @SimpleProperty(description = "Get the number of seconds since the start time, with " +
      "nanosecond accuracy.",
      category = PropertyCategory.BEHAVIOR)
  public double Time() {
    try {
      return elapsedTime.time();
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Time",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Log a message stating how long the timer has been running.")
  public void Log(String label) {
    try {
      elapsedTime.log(label);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Log",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Return text stating the number of seconds that have passed.")
  public String ToString() {
    try {
      String s = elapsedTime.toString();
      if (s != null) {
        return s;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ToString",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return "";
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    elapsedTime = null;
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    elapsedTime = null;
  }
}
