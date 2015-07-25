// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.component;

import com.google.appinventor.shared.rpc.component.Component;

/**
 * Listener interface for receiving component manager events.
 *
 * <p>Classes interested in processing component manager events must implement
 * this interface, and instances of that class must be registered with the
 * {@link ComponentManager} instance using its
 * {@link ComponentManager#addEventListener(ComponentManagerEventListener)}
 * method. When a component is added to the component manager, the listeners'
 * {@link #onComponentAdded(Component)} methods will be invoked. When a component is
 * removed the listeners'
 * {@link #onComponentRemoved(Component)} methods will be invoked.
 *
 */
public interface ComponentManagerEventListener {

  /**
   * Invoked after a component was added to the ComponentManager
   *
   * @param component  component added
   */
  void onComponentAdded(Component component);

  /**
   * Invoked after a component was removed.
   *
   * @param component  component removed
   */
  void onComponentRemoved(Component component);
}
