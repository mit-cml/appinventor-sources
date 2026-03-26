// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * PoC DTO for component permission metadata.
 */
public class ComponentPermission implements Serializable {
  private String componentName;
  private Set<String> permissions = new HashSet<>();
  private int minSdkVersion;

  /**
   * No-arg constructor for GWT serialization.
   */
  public ComponentPermission() {}

  public ComponentPermission(String componentName, Set<String> perms, int sdk) {
    this.componentName = componentName;
    this.permissions = perms;
    this.minSdkVersion = sdk;
  }

  public String getComponentName() {
    return componentName;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public int getMinSdkVersion() {
    return minSdkVersion;
  }
}
