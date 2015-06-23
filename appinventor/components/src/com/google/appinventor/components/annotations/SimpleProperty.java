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
 * Annotation to mark Simple properties.
 *
 * <p>Both the getter and the setter method of the property need to be marked
 * with this annotation.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimpleProperty {
  /**
   * If non-empty, description to use in user-level documentation.
   */
  String description() default "";

  /**
   * Category of property for user-level documentation.  This only needs
   * to be specified in the setter or the getter, not both.
   */
  PropertyCategory category() default PropertyCategory.UNSET;

  /**
   * If false, this property should not be accessible through Codeblocks.
   * This was added to support the Row and Column properties, so they could
   * be indirectly set in the Designer but not accessed in Codeblocks.
   */
  boolean userVisible() default true;
}
