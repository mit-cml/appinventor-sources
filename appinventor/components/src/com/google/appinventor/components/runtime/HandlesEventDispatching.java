// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * Interface indicating that this object can handle event dispatching.
 *
 * @author markf@google.com (Mark Friedman)
 */

public interface HandlesEventDispatching {
  public boolean canDispatchEvent(Component component, String eventName);

  public boolean dispatchEvent(Component component, String componentName, String eventName,
      Object[] args);

  void dispatchErrorOccurredEvent(Component component, String functionName, int errorCode,
      Object... args);

  /**
   * Request that the entity that handles the event send a generic
   * event for corresponding component class, event name pair.
   *
   * @param component the component originating the event
   * @param eventName the name of the event to fire
   * @param notAlreadyHandled true if the event was not handled by an event handler on the component, otherwise false
   * @param args any event-specific arguments to pass to the event handler block
   */
  void dispatchGenericEvent(Component component, String eventName, boolean notAlreadyHandled, Object[] args);
}
