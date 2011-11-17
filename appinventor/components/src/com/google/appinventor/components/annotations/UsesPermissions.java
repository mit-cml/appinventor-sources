// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate Android permissions required by components.
 *
 * @author markf@google.com (Mark Friedman)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesPermissions {

  /**
   * The names of the permissions separated by commas.
   *
   * @return  the permission name
   */
  String permissionNames() default "";
}
