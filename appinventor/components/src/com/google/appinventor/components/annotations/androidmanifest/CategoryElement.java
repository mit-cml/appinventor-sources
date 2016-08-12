// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe a <category> element, which is an optional subelement
 * of an <intent-filter>. A <category> element is a string containing
 * additional information about the kind of component that should handle the
 * intent. Any number of category descriptions can be placed in an intent, but
 * most intents do not require a category.
 *
 * Note: Some of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 *
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/category-element.html">
 *              https://developer.android.com/guide/topics/manifest/category-element.html
 *            </a>}.
 *
 * @author will2596@gmail.com (William Byrne)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CategoryElement {

  /**
   * The name of the category. Standard categories are defined in the
   * {@link android.content.Intent} class as CATEGORY_name constants. The
   * name assigned here can be derived from those constants by prefixing
   * "android.intent.category." to the name that follows CATEGORY_. For
   * example, the string value for {@link android.content.Intent#CATEGORY_LAUNCHER}
   * is "android.intent.category.LAUNCHER".
   *
   * Note: In order to receive implicit intents, you must include the
   * CATEGORY_DEFAULT category in the intent filter.
   *
   * Custom categories should use the package name as a prefix, to ensure that
   * they are unique.
   *
   * The name attribute is required in any @CategoryElement annotation and
   * hence has no default value.
   *
   * @return  the name of the category specified by this <category> element
   */
  String name();
}
