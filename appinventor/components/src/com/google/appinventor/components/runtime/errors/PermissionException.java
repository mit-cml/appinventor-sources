// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

/**
 * PermissionException is thrown when App Inventor needs a certain permission granted by the user but does not
 * have that permissions according to the Android permissions API.
 *
 * This exception will only be thrown on SDK 23 (Android 6.0 Marshmallow) or higher.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class PermissionException extends RuntimeException {

  private final String permissionNeeded;

  /**
   * Construct a new PermissionException to report that the given permission is needed.
   * @param permissionNeeded The permission needed at the point the exception is thrown.
   */
  public PermissionException(String permissionNeeded) {
    this.permissionNeeded = permissionNeeded;
  }

  /**
   * Get the name of the needed permission.
   * @return The permission name, e.g. android.permission.WRITE_EXTERNAL_STORAGE
   */
  public String getPermissionNeeded() {
    return permissionNeeded;
  }

  @Override
  public String getMessage() {
    return "Unable to complete the operation because the user denied permission: " + permissionNeeded;
  }
}
