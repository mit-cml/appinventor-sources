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

public final class ComponentManager {
  private final List<Component> components = new ArrayList<Component>();

  public ComponentManager() {
    pullComponents();
  }

  /**
   * Returns a list of all components already retrived by this manager
   */
  public List<Component> getRetrivedComponents() {
    return components;
  }

  public void pullComponents() {
    Ode.getInstance().getComponentService().getComponents(
      new OdeAsyncCallback<List<Component>>() {
        @Override
        public void onSuccess(List<Component> components) {
          ComponentManager.this.components.clear();
          ComponentManager.this.components.addAll(components);
        }
      });
  }
}
