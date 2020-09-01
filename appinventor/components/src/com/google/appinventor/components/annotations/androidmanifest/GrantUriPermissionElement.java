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
 * Specifies the subsets of app data that the parent content provider has permission to access. 
 * Data subsets are indicated by the path part of a content: URI. (The authority part of the URI 
 * identifies the content provider.) Granting permission is a way of enabling clients of the <provider> 
 * that don't normally have permission to access its data to overcome that restriction on a one-time 
 * basis.
 * 
 * If a content provider's grantUriPermissions attribute is "true", permission can be granted for 
 * any the data under the provider's purview. However, if that attribute is "false", permission can 
 * be granted only to data subsets that are specified by this element. A provider can contain any 
 * number of <grant-uri-permission> elements. Each one can specify only one path (only one of the 
 * three possible attributes).
 * 
 * Note: Most of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 * 
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/grant-uri-permission-element">
 *              https://developer.android.com/guide/topics/manifest/grant-uri-permission-element
 *            </a>}.
 * 
 * @author https://github.com/ShreyashSaitwal (Shreyash Saitwal)
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GrantUriPermissionElement {

  /**
   * A complete URI path for a subset of content provider data. Permission can be granted only 
   * to the particular data identified by this path. When used to provide search suggestion 
   * content, it must be appended with "/search_suggest_query".
   * 
   * @return  the grant uri permission path attribute
   */
  String path() default "";

  /**
   * The initial part of a URI path for a subset of content provider data. Permission can be 
   * granted to all data subsets with paths that share this initial part.
   * 
   * @return  the grant uri permission pathPrefix attribute
   */
  String pathPrefix() default "";

  /**
   * A path identifying the data subset or subsets that permission can be granted for. The path 
   * attribute specifies a complete path; permission can be granted only to the particular data 
   * subset identified by that path. The pathPrefix attribute specifies the initial part of a path; 
   * permission can be granted to all data subsets with paths that share that initial part. The 
   * pathPattern attribute specifies a complete path, but one that can contain the following wildcards:
   *   - An asterisk ('*') matches a sequence of 0 to many occurrences of the immediately preceding 
   *     character.
   *   - A period followed by an asterisk (".*") matches any sequence of 0 to many characters.

   * Because '\' is used as an escape character when the string is read from XML (before it is parsed 
   * as a pattern), you will need to double-escape: For example, a literal '*' would be written as "\\*" 
   * and a literal '\' would be written as "\\\\". This is basically the same as what you would need to 
   * write if constructing the string in Java code.
   * 
   * @return the grant uri permission pathPattern attribute respectively
   */
  String pathPattern() default "";
}
