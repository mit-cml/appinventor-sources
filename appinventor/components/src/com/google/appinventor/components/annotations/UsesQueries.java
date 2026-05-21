// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.ProviderElement;

/**
 * Annotation to describe a &lt;queries&gt; entry required by Android SDK 30.
 */
public @interface UsesQueries {
  /**
   * An array of intents that will be included in the &lt;queries&gt; element.
   *
   * @return the array of intents of interest
   */
  IntentFilterElement[] intents() default {};

  /**
   * A package name that will be included in the &lt;queries&gt; element.
   *
   * @return the array containing package names of interest
   */
  String[] packageNames() default {};

  /**
   * A provider element that will be included in the &lt;queries&gt;
   *
   * @return the array containing provider elements of interest
   */
  ProviderElement[] providers() default {};
}
