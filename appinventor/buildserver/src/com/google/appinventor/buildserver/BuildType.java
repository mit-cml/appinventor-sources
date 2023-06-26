// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BuildType {
  /**
   * Indicates that the annotated task is used for apk builds.
   */
  boolean apk() default false;

  /**
   * Indicates that the annotated task is used for aab builds.
   * @return
   */
  boolean aab() default false;

  String APK_EXTENSION = "apk";
  String AAB_EXTENSION = "aab";
}
