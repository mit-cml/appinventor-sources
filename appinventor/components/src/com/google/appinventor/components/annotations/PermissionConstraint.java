// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PermissionConstraint describes additional constraints that are applied to a
 * &lt;uses-permission&gt; element in the Android Manifest. This can sometimes
 * be helpful if a particular component needs finer control over when a
 * permission applies to a given app.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PermissionConstraint {
  /**
   * The name of the permission modified by this constraint.
   *
   * @return the name of the permission
   */
  String name();

  /**
   * The maximum SDK version for the given permission.
   *
   * @return the maximum SDK applicable, or -1 if unbounded
   */
  int maxSdkVersion() default -1;

  /**
   * Additional flags that describe the use of the permission to Android.
   * This feature was added in SDK 31.
   *
   * @return any flags, or the empty string if none are needed
   */
  String usesPermissionFlags() default "";
}
