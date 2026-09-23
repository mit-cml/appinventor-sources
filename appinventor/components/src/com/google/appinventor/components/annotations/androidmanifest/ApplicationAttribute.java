// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a single attribute-value pair to be injected into the
 * {@code <application>} tag of the Android Manifest.
 *
 * @author https://github.com/jewelshkjony (Jewel Shikder Jony)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplicationAttribute {

  /**
   * The name of the Android attribute.
   * <p>
   * This should exclude the {@code android:} namespace prefix, as it is
   * automatically prepended by the compiler. For example: {@code "largeHeap"}
   * or {@code "appCategory"}.
   * </p>
   * See valid attributes <a href="https://developer.android.com/guide/topics/manifest/application-element">from here</a
   *
   * @return the name of the attribute
   */
  String name();

  /**
   * The value to be assigned to the attribute.
   * <p>
   * For example: {@code "true"}, {@code "social"}, or a resource identifier
   * like {@code "@style/AppTheme"}.
   * </p>
   *
   * @return the value of the attribute
   */
  String value();
}
