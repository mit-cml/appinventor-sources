// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate native library files required by components.
 *
 * @author trevorbadams@gmail.com (Trevor Adams)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesNativeLibraries {

  /**
   * The names of the libraries separated by commas. Filenames of native libraries targeted at
   * Armeabi-v7A must end (after name but before the file extension) with a suffix
   * defined in Compiler.java ("-v7a") the same goes for v8a libraries ("-v8a").
   */
  String libraries() default "";
  String v7aLibraries() default "";
  String v8aLibraries() default "";
  String x86_64Libraries() default "";
}
