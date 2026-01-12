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
 * Annotation to indicate library files required by components.
 *
 * @author ralph.morelli@trincoll.edu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesLibraries {

  /**
   * The names of the libraries separated by commas.
   *
   * @return  the library name
   */
  String libraries() default "";

  /**
   * The names of the libraries (as an array)
   *
   * @return  the array of library names
   */
  String[] value() default {};
}
