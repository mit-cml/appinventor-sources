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
 * Annotation to mark Simple functions.
 *
 * <p>Note that the Simple compiler will only recognize Java methods marked
 * with this annotation. All other methods will be ignored.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimpleFunction {
  /**
   * If non-empty, description to use in user-level documentation in place of
   * Javadoc, which is meant for developers.
   */
  String description() default "";

  /**
   * If false, this property should not be accessible through Codeblocks.
   * This was added (1) by analogy to {@link SimpleProperty#userVisible()}
   * and (2) to temporarily hide functions for opening additional screens
   * in {@link com.google.appinventor.components.runtime.Form}.
   */
  boolean userVisible() default true;
}
