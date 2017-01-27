// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

/**
 * Annotation to describe an <intent-filter> element required by an <activity>
 * or a <receiver> element so that it can be added to AndroidManifest.xml.
 * <intent-filter> element attributes that are not set explicitly default
 * to "" or {} and are ignored when the element is created in the manifest.
 * In order for an <intent-filter> element to work properly, it must include
 * at least one <action> element in the {@link #actionElements()} attribute.
 *
 * See {@link ActionElement} for more information.
 *
 * Note: Some of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 *
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/intent-filter-element.html">
 *              https://developer.android.com/guide/topics/manifest/intent-filter-element.html
 *            </a>}.
 *
 * @author will2596@gmail.com (William Byrne)
 */
public @interface IntentFilterElement {

  /**
   * The array of actions accepted by this <intent-filter>. By construction,
   * <intent-filter> elements must have at least one <action> subelement.
   * Thus, this attribute of @IntentFilterElement is required and has no default.
   *
   * @return  the array of actions accepted by this <intent-filter>
   */
  ActionElement[] actionElements();

  /**
   * The array of categories accepted by this <intent-filter>. According to
   * the AndroidMainfest.xml specification, these subelements are optional.
   *
   * @return  the array of categories accepted by this <intent-filter>
   */
  CategoryElement[] categoryElements() default {};

  /**
   * The array of data specifications accepted by this <intent-filter>. According to
   * the AndroidMainfest.xml specification, these subelements are optional.
   *
   * @return  the array of data URIs accepted by this <intent-filter>
   */
  DataElement[] dataElements() default {};

  /**
   * A reference to a drawable resource representing the parent activity
   * or broadcast receiver when that component is presented to
   * the user as having the capability described by the filter.
   *
   * @return  a reference to the drawable resource for the filter's parent
   */
  String icon() default "";

  /**
   * A user-readable label for the parent component specified as a reference
   * to a string resource. If this attribute is left unspecified, the label
   * will default to the label set by the parent component
   *
   * @return  a reference to the string resource to be used as a label
   */
  String label() default "";

  /**
   * The priority that should be given to the parent activity or broadcast
   * receiver with regard to handling intents of the type described by the
   * filter. This must be specified as an integer in the interval
   * (-1000, 1000). If the priority is not set, it will default to 0.
   *
   * @return  the priority of the parent activity or broadcast receiver w.r.t.
   *          handling intents described by this filter
   */
  String priority() default "";

}
