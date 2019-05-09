// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.shadows;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;
import org.junit.Assert;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ewpatton on 7/1/17.
 */
@Implements(EventDispatcher.class)
public class ShadowEventDispatcher {
  private static class EventWithArgs {
    String eventName;
    Object[] args;

    EventWithArgs(String eventName, Object[] args) {
      this.eventName = eventName;
      this.args = args;
    }
  }

  private static Map<Component, Set<EventWithArgs>> firedEvents = new HashMap<>();

  public static void clearEvents() {
    firedEvents.clear();
  }

  @Implementation
  public static boolean dispatchEvent(Component component, String eventName, Object... args) {
    if (!firedEvents.containsKey(component)) {
      firedEvents.put(component, new HashSet<EventWithArgs>());
    }
    firedEvents.get(component).add(new EventWithArgs(eventName, args));
    return true;
  }

  public static void assertEventFired(Component component, String eventName, Object... args) {
    Set<EventWithArgs> events = firedEvents.get(component);
    if (events != null) {
      for (EventWithArgs e : events) {
        if (e.eventName.equals(eventName) && Arrays.deepEquals(e.args, args)) {
          return;  // the event fired, assertion passed
        }
      }
    }
    // the event didn't fire, assertion failed.
    throw new AssertionError(String.format("Component %s did not receive event %s", component, eventName));
  }

  /**
   * Checks whether or not the given {@code eventName} has fired for {@code component}. If so, the
   * test fails.
   * @param component The component to check for events
   * @param eventName An event name to check that should not have fired
   */
  public static void assertEventNotFired(Component component, String eventName) {
    Set<EventWithArgs> events = firedEvents.get(component);
    if (events != null) {
      for (EventWithArgs e : events) {
        if (e.eventName.equals(eventName)) {
          Assert.fail("Expected " + eventName + " of " + component + " to not fire, but it did.");
        }
      }
    }
  }

  public static void assertEventFiredAny(Component component, String eventName) {
    Set<EventWithArgs> events = firedEvents.get(component);
    if (events != null) {
      for (EventWithArgs e : events) {
        if (e.eventName.equals(eventName)) {
          return;  // event fired, assertion passed
        }
      }
    }
    // the event didn't fire, assertion failed.
    throw new AssertionError(String.format("Component %s did not receive event %s", component, eventName));
  }

  public static void assertErrorOccurred() {
    for (Set<EventWithArgs> events : firedEvents.values()) {
      for (EventWithArgs event : events) {
        if ("ErrorOccurred".equals(event.eventName)) {
          return;
        }
      }
    }
    throw new AssertionError("Form did not receive ErrorOccurred event.");
  }

  public static void assertErrorOccurred(int errorCode) {
    for (Set<EventWithArgs> events: firedEvents.values()) {
      for (EventWithArgs event : events) {
        if ("ErrorOccurred".equals(event.eventName) && errorCode == (Integer) event.args[2]) {
          return;
        }
      }
    }
    throw new AssertionError(String.format("Form did not receive ErrorOccurred event with code %d.", errorCode));
  }

  /**
   * Asserts that the EventDispatcher saw a PermissionDenied event for the given permission name.
   *
   * @param permission the permission to test for denial
   */
  public static void assertPermissionDenied(String permission) {
    if (permission.startsWith("android.permission.")) {
      permission = permission.replace("android.permission.", "");
    }
    for (Set<EventWithArgs> events: firedEvents.values()) {
      for (EventWithArgs event : events) {
        if ("PermissionDenied".equals(event.eventName) && event.args[2].equals(permission)) {
          return;
        }
      }
    }
    throw new AssertionError(String.format("Form did not receive PermissionDenied event for permission %s.", permission));
  }
}
