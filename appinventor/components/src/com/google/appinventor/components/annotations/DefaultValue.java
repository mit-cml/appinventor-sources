// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation lets us attach a default block to a component's parameter
 * directly in the Blocks Drawer. Instead of dragging out an empty block and
 * then searching for a math, text, etc block to fill it, the block will already
 * come with these defaults attached when you see it in the drawer. These are
 * real, draggable blocks that help users understand what kind of data is
 * expected.
 *
 * @author https://github.com/jewelshkjony (Jewel Shikder Jony)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface DefaultValue {

  /**
   * The initial value that goes inside the attached block.
   * * Follow these formatting rules:
   * - For colors: Start with a # (e.g., "#FFFFFF").
   * - For lists: Use commas to separate items (e.g., "item1, item2, item3").
   * - For dictionaries: Use key:value pairs (e.g., "key1:val1, key2:val2").
   * - For simple types: Just use the value (e.g., "10" or "hello").
   */
  String value() default "";

  /**
   * The type of block to attach.
   * * This is optional. Usually, the system figures out the block type from
   * the parameter itself. You only need to set this manually if the parameter
   * is an Object (any) and you want a specific block type to show up.
   * * Allowed types: text, number, boolean, color, list, dictionary.
   */
  String type() default "";
}
