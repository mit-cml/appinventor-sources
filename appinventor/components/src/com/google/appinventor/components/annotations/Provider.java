// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a parameter as accepting a provider
 * string. This can be used to upgrade old components (ie components
 * that were released before this was released). It can also be
 * included in new components.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Provider {
  /**
   * If specified, a list of extensions used to filter the asset list by.
   *
   * @return an empty array (the default)
   */
  String[] value() default {};
}
