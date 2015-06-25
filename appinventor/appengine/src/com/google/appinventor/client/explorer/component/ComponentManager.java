// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.component;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.component.ComponentInfo;

import java.util.ArrayList;
import java.util.List;

public final class ComponentManager {
  private final List<ComponentInfo> compInfos = new ArrayList<ComponentInfo>();

  public ComponentManager() {
    pullComponentInfos();
  }

  /**
   * Returns a list of all component info already retrived by this manager
   */
  public List<ComponentInfo> getRetrivedComponentInfos() {
    return compInfos;
  }

  public void pullComponentInfos() {
    Ode.getInstance().getComponentService().getComponentInfos(
      new OdeAsyncCallback<List<ComponentInfo>>() {
        @Override
        public void onSuccess(List<ComponentInfo> compInfos) {
          ComponentManager.this.compInfos.clear();
          ComponentManager.this.compInfos.addAll(compInfos);
        }
      });
  }
}
