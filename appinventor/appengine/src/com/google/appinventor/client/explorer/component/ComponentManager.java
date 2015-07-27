// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.component;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.component.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages {@link com.google.appinventor.shared.rpc.component.Component}.
 *
 */
public final class ComponentManager {
  private final List<Component> components;
  private final List<ComponentManagerEventListener> eventListeners;

  public ComponentManager() {
    components = new ArrayList<Component>();
    eventListeners = new ArrayList<ComponentManagerEventListener>();

    Ode.getInstance().getComponentService().getComponents(
      new OdeAsyncCallback<List<Component>>() {
        @Override
        public void onSuccess(List<Component> components) {
          ComponentManager.this.components.addAll(components);
          fireComponentsLoaded();
        }
      });
  }

  /**
   * Returns a list of all components
   */
  public List<Component> getComponents() {
    return components;
  }

  /**
   * Adds a new component to this component manager.
   *
   * @param component the component to be added
   */
  public void addComponent(Component component) {
    components.add(component);
    fireComponentAdded(component);
  }

  /**
   * Removes the given component.
   *
   * @param component the component to be removed
   */
  public void removeComponent(Component component) {
    components.remove(component);
    fireComponentRemoved(component);
  }

  /**
   * Adds a {@link ComponentManagerEventListener} to the listener list.
   *
   * @param listener  the {@code ComponentManagerEventListener} to be added
   */
  public void addEventListener(ComponentManagerEventListener listener) {
    eventListeners.add(listener);
  }

  private void fireComponentsLoaded() {
    for (ComponentManagerEventListener listener : eventListeners) {
      listener.onComponentsLoaded();
    }
  }

  private void fireComponentAdded(Component component) {
    for (ComponentManagerEventListener listener : eventListeners) {
      listener.onComponentAdded(component);
    }
  }

  private void fireComponentRemoved(Component component) {
    for (ComponentManagerEventListener listener : eventListeners) {
      listener.onComponentRemoved(component);
    }
  }
}
