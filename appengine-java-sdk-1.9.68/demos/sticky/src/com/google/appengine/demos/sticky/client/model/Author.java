/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.sticky.client.model;

import java.io.Serializable;

/**
 * A client-side data object representing an author.
 *
 */
@SuppressWarnings("serial")
public class Author implements Serializable {

  /**
   * Returns a shorter name for an author. For names of the form a@company.com,
   * this returns "a". For names of the form First Last, this returns "First".
   *
   * @return
   */
  public static String getShortName(String name) {
    final int atIndex = name.indexOf('@');
    if (atIndex > 0) {
      return name.substring(0, atIndex);
    }

    final int spIndex = name.indexOf(' ');
    if (spIndex > 0) {
      return name.substring(0, spIndex);
    }

    return name;
  }

  /**
   * The author's email address.
   */
  private String email;

  /**
   * A nickname for the user. If the author has no nick name this will be set to
   * the author's email address, as well.
   */
  private String name;

  /**
   * @param email
   *          the authors email
   * @param name
   *          a nick name for the user
   */
  public Author(String email, String name) {
    this.email = email;
    this.name = name;
  }

  /**
   * Need for RPC serialization.
   */
  @SuppressWarnings("unused")
  private Author() {
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  /**
   * Convenience method that calls through to
   * {@link Author#getShortName(String)}.
   *
   * @return
   */
  public String getShortName() {
    return getShortName(name);
  }
}
