// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Build.VERSION;
import java.util.HashMap;
import java.util.Map;

public class PermissionRegistry {
  private static class SdkConstraint {
    private final int minSdk;
    private final int maxSdk;

    SdkConstraint(int minSdk, int maxSdk) {
      this.minSdk = minSdk;
      this.maxSdk = maxSdk;
    }

    static SdkConstraint minSdk(int minSdk) {
      return new SdkConstraint(minSdk, Integer.MAX_VALUE);
    }

    static SdkConstraint maxSdk(int maxSdk) {
      return new SdkConstraint(1, maxSdk);
    }
  }

  private final Map<String, SdkConstraint> permissions = new HashMap<>();

  public PermissionRegistry() {

  }

  public PermissionRegistry record(String permission, int minSdk, int maxSdk) {
    permissions.put(permission, new SdkConstraint(minSdk, maxSdk));
    return this;
  }

  public PermissionRegistry recordMinSdk(String permission, int minSdk) {
    if (permissions.containsKey(permission)) {
      permissions.put(permission, new SdkConstraint(minSdk, permissions.get(permission).maxSdk));
    } else {
      permissions.put(permission, SdkConstraint.minSdk(minSdk));
    }
    return this;
  }

  public PermissionRegistry recordMaxSdk(String permission, int maxSdk) {
    if (permissions.containsKey(permission)) {
      permissions.put(permission, new SdkConstraint(permissions.get(permission).minSdk, maxSdk));
    } else {
      permissions.put(permission, SdkConstraint.maxSdk(maxSdk));
    }
    return this;
  }

  /**
   * Checks whether the given {@code permission} is needed by the current Android version. The
   * lack of constraints implies that the permission must be requested.
   *
   * @param permission the permission to check
   * @return true if the system should check for the permission, otherwise false
   */
  public boolean needsPermission(String permission) {
    SdkConstraint constraint = permissions.get(permission);
    if (constraint == null) {
      return true;
    }
    return constraint.minSdk <= VERSION.SDK_INT && VERSION.SDK_INT < constraint.maxSdk;
  }
}
