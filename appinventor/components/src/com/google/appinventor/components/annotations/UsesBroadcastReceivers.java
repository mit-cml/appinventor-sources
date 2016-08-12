// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import com.google.appinventor.components.annotations.androidmanifest.ReceiverElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate any broadcast receivers used by
 * a component so that corresponding <receiver> elements can be
 * created in AndroidManifest.xml.
 *
 * @author will2596@gmail.com (William Byrne)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesBroadcastReceivers {

  /**
   * An array containing each {@link ReceiverElement}
   * that is required by the component.
   *
   * @return  the array containing the relevant receivers
   */
  ReceiverElement[] receivers();
}
