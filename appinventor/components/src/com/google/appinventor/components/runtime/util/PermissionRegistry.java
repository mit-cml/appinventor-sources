// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Build.VERSION;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for permissions that are needed by the App Inventor components.
 *
 * <p>Permissions can be constrained by the minimum and maximum SDK versions. If a permission is not
 * constrained, it is assumed to be needed by all versions of Android. Use of a permission on a
 * version of Android outside the specified range will be assumed to succeed.
 */
public class PermissionRegistry {
  private static final String LOG_TAG = PermissionRegistry.class.getSimpleName();

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

  /**
   * Records a permission that is in effect between the given minimum and maximum SDK versions.
   *
   * @param permission the Android permission of interest
   * @param minSdk the minimum SDK version that requires the permission
   * @param maxSdk the maximum SDK version that requires the permission
   * @return the PermissionRegistry instance for chaining
   */
  public PermissionRegistry record(String permission, int minSdk, int maxSdk) {
    if (permissions.containsKey(permission)) {
      Log.w(LOG_TAG, "Overwriting permission rules for " + permission);
    }
    permissions.put(permission, new SdkConstraint(minSdk, maxSdk));
    return this;
  }

  /**
   * Records a permission that is in effect for all SDK versions greater than or equal to the given
   * minimum SDK version.
   *
   * @param permission the Android permission of interest
   * @param minSdk the minimum SDK version that requires the permission
   * @return the PermissionRegistry instance for chaining
   */
  public PermissionRegistry recordMinSdk(String permission, int minSdk) {
    if (permissions.containsKey(permission)) {
      permissions.put(permission, new SdkConstraint(minSdk, permissions.get(permission).maxSdk));
    } else {
      permissions.put(permission, SdkConstraint.minSdk(minSdk));
    }
    return this;
  }

  /**
   * Records a permission that is in effect for all SDK versions less than the given maximum SDK
   * version.
   *
   * @param permission the Android permission of interest
   * @param maxSdk the maximum SDK version that requires the permission
   * @return the PermissionRegistry instance for chaining
   */
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
