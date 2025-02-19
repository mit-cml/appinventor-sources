// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dispatches events to component event handlers.
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 */
public class EventDispatcher {
  private static final class EventClosure {
    private final String componentId;
    private final String eventName;

    private EventClosure(String componentId, String eventName) {
      this.componentId = componentId;
      this.eventName = eventName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      EventClosure that = (EventClosure) o;

      if (!componentId.equals(that.componentId)) {
        return false;
      }
      if (!eventName.equals(that.eventName)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 31 * eventName.hashCode() + componentId.hashCode();
    }
  }

  /*
   * Each EventRegistry is associated with one dispatchDelegate.
   * It contains all the event closures for a single form.
   */
  private static final class EventRegistry {
    private final HandlesEventDispatching dispatchDelegate;

    // Mapping of event names to a set of event closures.
    // Note that by using a Set here, we'll only have one closure corresponding to a
    // given componentId-eventName.  We do not support invoking multiple handlers for a
    // single event.
    private final HashMap<String, Set<EventClosure>> eventClosuresMap =
        new HashMap<String, Set<EventClosure>>();

    EventRegistry(HandlesEventDispatching dispatchDelegate) {
      this.dispatchDelegate = dispatchDelegate;
    }
  }

  private static final boolean DEBUG = false;

  private static final Map<HandlesEventDispatching, EventRegistry>
      mapDispatchDelegateToEventRegistry = new HashMap<HandlesEventDispatching, EventRegistry>();

  private EventDispatcher() {
  }

  private static EventRegistry getEventRegistry(HandlesEventDispatching dispatchDelegate) {
    EventRegistry er = mapDispatchDelegateToEventRegistry.get(dispatchDelegate);
    if (er == null) {
      er = new EventRegistry(dispatchDelegate);
      mapDispatchDelegateToEventRegistry.put(dispatchDelegate, er);
    }
    return er;
  }

  private static EventRegistry removeEventRegistry(HandlesEventDispatching dispatchDelegate) {
    return mapDispatchDelegateToEventRegistry.remove(dispatchDelegate);
  }


  /**
   * Registers a dispatchDelegate for handling event dispatching for the event with the specified
   * component id and event name.
   *
   * @param dispatchDelegate  object responsible for dispatching the event
   * @param componentId  id of component associated with event handler
   * @param eventName  name of event
   */
  // Don't delete this method. It's called from runtime.scm.
  public static synchronized void registerEventForDelegation(HandlesEventDispatching dispatchDelegate,
                                                String componentId, String eventName) {
    EventRegistry er = getEventRegistry(dispatchDelegate);
    Set<EventClosure> eventClosures = er.eventClosuresMap.get(eventName);
    Set<EventClosure> newEventClosures = eventClosures == null
        ? new HashSet<EventClosure>() : new HashSet<>(eventClosures);
    newEventClosures.add(new EventClosure(componentId, eventName));
    er.eventClosuresMap.put(eventName, newEventClosures);

    if (DEBUG) {
      Log.i("EventDispatcher", "Registered event closure for " +
          componentId + "." + eventName);
    }
  }

  /**
   * Unregisters a dispatchDelegate for handling event dispatching for the event with the specified
   * component id and event name.
   *
   * @param dispatchDelegate  object responsible for dispatching the event
   * @param componentId  id of component associated with event handler
   * @param eventName  name of event
   */
  // Don't delete this method. It's called from runtime.scm.
  public static synchronized void unregisterEventForDelegation(HandlesEventDispatching dispatchDelegate,
                                                  String componentId, String eventName) {
    EventRegistry er = getEventRegistry(dispatchDelegate);
    Set<EventClosure> eventClosures = er.eventClosuresMap.get(eventName);
    if (eventClosures == null || eventClosures.isEmpty()) {
      return;
    }
    Set<EventClosure> updated = new HashSet<>();
    for (EventClosure eventClosure : eventClosures) {
      if (eventClosure.componentId.equals(componentId)) {
        if (DEBUG) {
          Log.i("EventDispatcher", "Deleting event closure for " +
              eventClosure.componentId + "." + eventClosure.eventName);
        }
      } else {
        updated.add(eventClosure);
      }
    }
    er.eventClosuresMap.put(eventName, updated);
  }

  /**
   * Removes all event closures previously registered via
   * {@link EventDispatcher#registerEventForDelegation}.
   */
  // Don't delete this method. It's called from runtime.scm.
  public static synchronized void unregisterAllEventsForDelegation() {
    for (EventRegistry er : mapDispatchDelegateToEventRegistry.values()) {
      er.eventClosuresMap.clear();
    }
  }

  /**
   * Removes event handlers previously registered with the given
   * dispatchDelegate and clears all references to the dispatchDelegate in
   * this class.
   *
   * Called when a Form's onDestroy method is called.
   */
  public static synchronized void removeDispatchDelegate(HandlesEventDispatching dispatchDelegate) {
    EventRegistry er = removeEventRegistry(dispatchDelegate);
    if (er != null) {
      er.eventClosuresMap.clear();
    }
  }

  /**
   * Dispatches an event based on its name to any registered handlers.
   *
   * @param component  the component raising the event
   * @param eventName  name of event being raised
   * @param args  arguments to the event handler
   */
  public static synchronized boolean dispatchEvent(Component component, String eventName, Object...args) {
    return Boolean.TRUE == dispatchFallibleEvent(component, eventName, args);
  }

  /**
   * Dispatches an event based on its name to any registered handlers.
   *
   * @param component  the component raising the event
   * @param eventName  name of event being raised
   * @param args  arguments to the event handler
   * @return `Boolean.TRUE` if the event was dispatched, `Boolean.FALSE` if no event handler was
   * found, or an Exception if the event handler threw an Exception
   */
  public static synchronized Object dispatchFallibleEvent(Component component, String eventName, Object... args) {
    if (DEBUG) {
      Log.i("EventDispatcher", "Trying to dispatch event " + eventName);
    }
    boolean dispatched = false;
    try {
      args = OptionHelper.optionListsFromValues(component, eventName, args);
      HandlesEventDispatching dispatchDelegate = component.getDispatchDelegate();
      if (dispatchDelegate.canDispatchEvent(component, eventName)) {
        EventRegistry er = getEventRegistry(dispatchDelegate);
        Set<EventClosure> eventClosures = er.eventClosuresMap.get(eventName);
        if (eventClosures != null && eventClosures.size() > 0) {
          dispatched = delegateDispatchEvent(dispatchDelegate, eventClosures, component, args);
        }
        dispatchDelegate.dispatchGenericEvent(component, eventName, !dispatched, args);
      }
    } catch (Exception e) {
      return e;
    }
    return dispatched;
  }

  /**
   * Delegates the dispatch of an event to the dispatch delegate.
   *
   * @param eventClosures set of event closures matching the event name
   * @param component the component that generated the event
   * @param args  arguments to event handler
   */
  private static synchronized boolean delegateDispatchEvent(HandlesEventDispatching dispatchDelegate,
                                               Set<EventClosure> eventClosures,
                                               Component component, Object... args) {
    // The event closures set will contain all event closures matching the event name.
    // We depend on the delegate's dispatchEvent method to check the registered event closure and
    // only dispatch the event if the registered component matches the component that generated the
    // event.  This should only be true for one (or zero) of the closures.
    boolean dispatched = false;
    for (EventClosure eventClosure : eventClosures) {
      if (dispatchDelegate.dispatchEvent(component,
                                         eventClosure.componentId,
                                         eventClosure.eventName,
                                         args)) {
        if (DEBUG) {
          Log.i("EventDispatcher", "Successfully dispatched event " +
              eventClosure.componentId + "." + eventClosure.eventName);
        }
        dispatched = true;  // break here or keep iterating through loop?
      }
    }
    return dispatched;
  }

  // Don't delete this method. It's called from runtime.scm.
  public static String makeFullEventName(String componentId, String eventName) {
    if (DEBUG) {
      Log.i("EventDispatcher", "makeFullEventName componentId=" + componentId + ", " +
          "eventName=" + eventName);
    }
    return componentId + '$' + eventName;
  }
}
