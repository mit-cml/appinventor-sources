// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a file is a BroadcastReceiver and will need to be written to the Android Manifest in
 * Compiler.java
 * For each receiver <i>android:exported</i> will default to true if any actions are declared, false if no actions are passed.
 * <i>android:enabled</i> will default to true because the same attribute in the <i>application</i> tag is not specified.
 * If any other attributes are ever needed for the receiver, the annotation can be extended.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface SimpleBroadcastReceiver {

  /**
   * The class name of the Broadcast Receiver
   */
  String className() default "";

  /**
   * The names of the actions for the receiver's intent filter, separated by commas.
   */
  String actions() default "";
}
