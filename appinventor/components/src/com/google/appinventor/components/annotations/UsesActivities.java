// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate any additional activities required by
 * a component so that corresponding <activity> elements can be added
 * to AndroidManifest.xml.
 *
 * @author will2596@gmail.com (William Byrne)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesActivities {

  /**
   * An array containing each {@link ActivityElement}
   * that is required by the component.
   *
   * @return  the array containing the relevant activities
   */
  ActivityElement[] activities();
}