// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BuildType {
  /**
   * Indicates that the annotated task is used for apk builds.
   */
  boolean apk() default false;

  /**
   * Indicates that the annotated task is used for aab builds.
   */
  boolean aab() default false;

  /**
   * Indicates that the annotated task is used for ipa builds.
   */
  boolean ipa() default false;

  /**
   * Indicates that the annotated task is used for App Store Connect builds.
   */
  boolean asc() default false;

  String APK_EXTENSION = "apk";
  String AAB_EXTENSION = "aab";
  String IPA_EXTENSION = "ipa";
  String ASC_EXTENSION = "asc";
  Collection<String> ALLOWED = Collections.unmodifiableCollection(Arrays.asList(
      APK_EXTENSION, AAB_EXTENSION, IPA_EXTENSION, ASC_EXTENSION));
}
