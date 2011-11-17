// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark Simple event definitions.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimpleEvent {
  /**
   * If non-empty, description to use in user-level documentation in place of
   * Javadoc, which is meant for developers.
   */
  String description() default "";

  /**
   * If false, this event should not be accessible through Codeblocks.
   */
  boolean userVisible() default true;
}
