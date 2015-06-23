// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.AlarmHandler;

import android.os.Handler;

/**
 * Helper class for components containing timers, such as Timer and Sprite.
 *
 */
public final class TimerInternal implements Runnable {

  // Android message handler used as a timer
  private Handler handler;

  // Indicates whether the timer is running or not
  private boolean enabled;  // set in constructor

  // Interval between timer events in ms
  private int interval;  // set in constructor

  // Component that should be called by timer
  private AlarmHandler component;

  /**
   * Timer constructor
   *
   * @param component the component whose {@link AlarmHandler#alarm()} method
   *        should be called on timer intervals
   * @param enabled whether it is initially enabled
   * @param interval time in ms
   */
  public TimerInternal(AlarmHandler component, boolean enabled, int interval) {
    this(component, enabled, interval, new Handler());
  }

  /**
   * Timer constructor allowing injection of a mock Handler for test purposes
   *
   * @param component the component whose {@link AlarmHandler#alarm()} method
   *        should be called on timer intervals
   * @param enabled whether it is initially enabled
   * @param interval time in ms
   * @param handler the handler whose {@link
   *        android.os.Handler#postDelayed(Runnable, long)}
   *        method is called to request calls of this after the delay
   *        specified via {@link #Interval(int)}
   */
  public TimerInternal(AlarmHandler component, boolean enabled, int interval, Handler handler) {
    this.handler = handler;
    this.component = component;

    // Set properties to default values specified by caller.
    this.enabled = enabled;
    this.interval = interval;
    if (enabled) {
      handler.postDelayed(this, interval);
    }
  }

  /**
   * Interval getter.
   *
   * @return  timer interval in ms
   */
  public int Interval() {
    return interval;
  }

  /**
   * Interval property setter method: sets the interval between timer events.
   *
   * @param interval  timer interval in ms
   */
  public void Interval(int interval) {
    this.interval = interval;
    if (enabled) {
      handler.removeCallbacks(this);
      handler.postDelayed(this, interval);
    }
  }

  /**
   * Enabled property getter method.
   *
   * @return  {@code true} indicates a running timer, {@code false} a stopped
   *          timer
   */
  public boolean Enabled() {
    return enabled;
  }

  /**
   * Enabled property setter method: starts or stops the timer.
   *
   * @param enabled  {@code true} starts the timer, {@code false} stops it
   */
  public void Enabled(boolean enabled) {
    if (this.enabled) {
      handler.removeCallbacks(this);
    }

    this.enabled = enabled;

    if (enabled) {
      handler.postDelayed(this, interval);
    }
  }

  // Runnable implementation

  public void run() {
    if (enabled) {
      component.alarm();

      // During the call to component.alarm, the enabled field may have changed.
      // We need to make sure that enabled is still true before we call handler.postDelayed.
      if (enabled) {
        handler.postDelayed(this, interval);
      }
    }
  }
}
