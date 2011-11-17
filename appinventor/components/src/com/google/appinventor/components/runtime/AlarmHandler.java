// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

/**
 * Interface for components that can be woken up by an outside alarm.  This is
 * typically used in conjunction with TimerInternal().
 */

public interface AlarmHandler {
  public void alarm();
}
