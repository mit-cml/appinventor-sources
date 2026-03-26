// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * PoC DTO for project-wide permission metadata.
 */
public class PermissionMetadata implements Serializable {
  private long projectId;
  private List<ComponentPermission> components = new ArrayList<>();
  private long timestamp;

  /**
   * No-arg constructor for GWT serialization.
   */
  public PermissionMetadata() {}

  public PermissionMetadata(long projectId, List<ComponentPermission> components) {
    this.projectId = projectId;
    this.components = components;
    this.timestamp = System.currentTimeMillis();
  }

  public long getProjectId() {
    return projectId;
  }

  public List<ComponentPermission> getComponents() {
    return components;
  }

  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Helper for reverse lookup.
   */
  public Set<String> getComponentsRequiringPermission(String permissionName) {
    Set<String> componentNames = new HashSet<>();
    for (ComponentPermission cp : components) {
      if (cp.getPermissions().contains(permissionName)) {
        componentNames.add(cp.getComponentName());
      }
    }
    return componentNames;
  }
}
