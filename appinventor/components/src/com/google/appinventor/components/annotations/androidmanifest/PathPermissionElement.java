// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the path and required permissions for a specific subset of data within a 
 * <provider>. This element can be specified multiple times to supply multiple paths.
 * 
 * Note: Most of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 * 
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/path-permission-element">
 *              https://developer.android.com/guide/topics/manifest/path-permission-element
 *            </a>}.
 * 
 * @author https://github.com/ShreyashSaitwal (Shreyash Saitwal)
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PathPermissionElement {

  /**
   * A complete URI path for a subset of content provider data. Permission can be granted only 
   * to the particular data identified by this path. When used to provide search suggestion 
   * content, it must be appended with "/search_suggest_query".
   * 
   * @return  the path permission path attribute
   */
  String path() default "";

  /**
   * The initial part of a URI path for a subset of content provider data. Permission can be 
   * granted to all data subsets with paths that share this initial part.
   * 
   * @return  the path permission pathPrefix attribute
   */
  String pathPrefix() default "";

  /**
   * A complete URI path for a subset of content provider data, but one that can use the following 
   * wildcards:
   *  - An asterisk ('*'). This matches a sequence of 0 to many occurrences of the immediately 
   *    preceding character.
   *  - A period followed by an asterisk (".*"). This matches any sequence of 0 or more characters.
   * 
   * Because '\' is used as an escape character when the string is read from XML (before it is parsed 
   * as a pattern), you will need to double-escape. For example, a literal '*' would be written as "\\*" 
   * and a literal '\' would be written as "\\". This is basically the same as what you would need to 
   * write if constructing the string in Java code.
   * 
   * @return  the path permission pathPattern attribute
   */
  String pathPattern() default "";

  /**
   * The name of a permission that clients must have in order to read or write the content provider's 
   * data. This attribute is a convenient way of setting a single permission for both reading and 
   * writing. However, the {@link #readPermission()} and {@link #writePermission()} attributes take 
   * precedence over this one.
   * 
   * @return  the path permission permission attribute
   */
  String permission() default "";

  /**
   * A permission that clients must have in order to query the content provider.
   * 
   * @return  the path permission readPermission attribute
   */
  String readPermission() default "";

  /**
   * A permission that clients must have in order to make changes to the data controlled by the 
   * content provider.
   * 
   * @return  the path permission writePermission attribute
   */
  String writePermission() default "";
}
