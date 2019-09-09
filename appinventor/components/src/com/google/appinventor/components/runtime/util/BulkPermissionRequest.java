// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Components can use BulkPermissionRequest to request multiple permissions in a single check
 * by {@link Form#askPermission(BulkPermissionRequest)} rather than chaining multiple
 * PermissionRequestCallbacks.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class BulkPermissionRequest {
  private Component source;
  private String caller;
  private String[] permissions;

  /**
   * Construct a new BulkPermissionRequest.
   *
   * @param source the component requesting the permissions, for error reporting
   * @param caller the method requesting the permissions, for error reporting
   * @param permissions the list of permissions to request
   */
  protected BulkPermissionRequest(Component source, String caller, String... permissions) {
    this.source = source;
    this.caller = caller;
    this.permissions = permissions;
  }

  /**
   * Subclasses must implement onGranted to provide behavior for when all permissions are granted.
   * This method is called either because all permissions were previously granted or because the
   * user was prompted and answered affirmatively.
   */
  public abstract void onGranted();

  /**
   * Handles the scenario where one or more permissions in the request has been rejected. Subclasses
   * may override this method to provide their own behavior.
   *
   * @param permissions the array of permissions that were denied
   */
  public void onDenied(String[] permissions) {
    Form form = (Form) source.getDispatchDelegate();
    for (String permission : permissions) {
      form.dispatchPermissionDeniedEvent(source, caller, permission);
    }
  }

  /**
   * Gets a mutable copy of the permissions requested.
   *
   * @return the list of desired permissions
   */
  public final List<String> getPermissions() {
    List<String> result = new ArrayList<>(permissions.length);
    Collections.addAll(result, permissions);
    return result;
  }
}
