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
 * Annotation to describe an <action> element required by an <intent-filter>
 * element so that it can be added to AndroidManifest.xml.
 *
 * Note: Some of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 *
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/action-element.html">
 *              https://developer.android.com/guide/topics/manifest/action-element.html
 *            </a>}.
 *
 * @author will2596@gmail.com (William Byrne)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ActionElement {

  /**
   * The fully qualified name of the action. For standard actions defined in
   * the {@link android.content.Intent} class, prepend "android.intent.action" to
   * the "string" in each ACTION_string constant. For example, to specify
   * ACTION_MAIN the fully qualified name would be "android.intent.action.MAIN".
   * Custom defined actions are conventionally prepended with the package name
   * of their containing class, e.g. "com.example.project.ACTION". The name attribute
   * is required in any @ActionElement annotation and hence has no default value.
   *
   * @return the fully qualified name of the action
   */
  String name();
}
