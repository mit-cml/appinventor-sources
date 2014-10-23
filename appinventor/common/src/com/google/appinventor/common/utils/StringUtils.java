// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.common.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Set;

/**
 * Helper class for working with strings.
 *
 */
public final class StringUtils {
  private StringUtils() {
  }

  /**
   * A {@link CharMatcher} that matches valid filename characters:
   * [0-9],[a-z],[A-Z],'_','.', and '-'
   */
  public static final CharMatcher VALID_FILENAME_CHARS =
      CharMatcher.inRange('0', '9')
      .or(CharMatcher.inRange('a', 'z'))
      .or(CharMatcher.inRange('A', 'Z'))
      .or(CharMatcher.is('_'))
      .or(CharMatcher.is('.'))
      .or(CharMatcher.is('-'));

  /**
   * Returns the given string enclosed with quotation marks.
   *
   * @param str string to quote
   * @return quoted string
   * @throws NullPointerException  if {@code str} is {@code null}
   */
  public static String quote(String str) {
    return '"' + str.toString() + '"';
  }

  /**
   * Returns the given quoted string without quotation marks.
   *
   * @param str quoted string
   * @return string without quotation marks
   * @throws IllegalArgumentException  if {@code str} doesn't have a leading or
   *                                   a trailing quotation mark
   * @throws NullPointerException  if {@code str} is {@code null}
   */
  public static String unquote(String str) {
    int lastIndex = str.length() - 1;
    if (lastIndex <= 0 || str.charAt(0) != '"' || str.charAt(lastIndex) != '"') {
      throw new IllegalArgumentException("Attempting to unquote string without quotes!");
    }

    return str.substring(1, lastIndex);
  }

  /**
   * Creates new string to display nicely in HTML.
   *
   * @param str string to escape
   * @return escaped string or {@code null} if {@code str} was {@code null}
   */
  public static String escape(String str) {
    if (str != null) {
      str = str.replaceAll("&", "&amp;").
          replaceAll("<", "&lt;").
          replaceAll(">", "&gt;").
          replaceAll("\"", "&quot;").
          replaceAll("\n", "<br>");
    }
    return str;
  }

  /**
   * Indicates whether an array contains the given string.
   *
   * @param array  array to check
   * @param string  string to look for
   * @return  {@code true} if the string was found in the array, {@code false}
   *          otherwise
   * @throws NullPointerException  if either {@code array} or {@code string} is
   *                               {@code null}
   */
  public static boolean contains(String[] array, String string) {
    for (String s : array) {
      if (string.equals(s)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a string consisting of the joined string array elements,
   * separated by the delimiter.
   *
   * @param delimiter  separates individual strings in the joined string
   * @param strings  strings to join
   * @return  string resulting from joining the individual strings
   */
  public static String join(String delimiter, final String[] strings) {
    return join(delimiter, Arrays.asList(strings));
  }

  /**
   * Returns a string consisting of the joined strings, separated by the
   * delimiter.
   *
   * @param delimiter  separates array elements in created string
   * @param strings  string elements to join
   * @return  string created of joined string array elements
   */
  public static String join(String delimiter, Iterable<String> strings) {
    Preconditions.checkNotNull(delimiter);
    Preconditions.checkNotNull(strings);

    StringBuilder sb = new StringBuilder();
    String separator = "";
    for (String string : strings) {
      sb.append(separator);
      sb.append(string);
      separator = delimiter;
    }
    return sb.toString();
  }

  /**
   * Returns a semi-unique legal package name for a user.
   *
   * @param email the user's email address
   * @return  package name
   */
  public static String userToPackageName(String email) {
    StringBuilder sb = new StringBuilder("appinventor.ai_");
    int length = email.length();
    for (int i = 0; i < length; i++) {
      char ch = email.charAt(i);
      if (ch == '@') {
        break;
      }
      if ((ch >= 'a' && ch <= 'z') ||
          (ch >= 'A' && ch <= 'Z') ||
          (ch >= '0' && ch <= '9') ||
          (ch == '_')) {
        sb.append(ch);
      } else if (ch == '.') {
        sb.append('_');
      }
    }
    return sb.toString();
  }

  /**
   * Return the package for project, given the user's email address and the project name.
   *
   * @param userEmail the user's email address
   * @param projectName the project name
   * @return  package name
   */
  public static String getProjectPackage(String userEmail, String projectName) {
    return userToPackageName(userEmail) + "." + projectName;
  }

  /**
   * Return the qualified name of Screen1 in a project, given the user's email address and the
   * project name.
   *
   * @param userEmail the user's email address
   * @param projectName the project name
   * @return  qualified form name
   */
  public static String getQualifiedFormName(String userEmail, String projectName) {
    return getProjectPackage(userEmail, projectName) + ".Screen1";
  }

  /**
   * Returns a new String resulting from replacing the last occurrence of
   * target in string with replacement. If target does not occur in string,
   * string is returned.
   *
   * @param string the original string
   * @param target the value to be replaced
   * @param replacement the replacement value
   * @return the resulting string
   */
  public static String replaceLastOccurrence(String string, String target, String replacement) {
    if (string.length() > 0 && target.length() > 0) {
      int lastIndexOfTarget = string.lastIndexOf(target);
      if (lastIndexOfTarget != -1) {
        return string.substring(0, lastIndexOfTarget) +
            replacement +
            string.substring(lastIndexOfTarget + target.length());
      }
    }
    return string;
  }

  /**
   * Autogenerates a projectname and verifies it does not already exist in
   * {@code existingProjectNames}
   */
  public static String createProjectName(Set<String> existingProjectNames) {
    String prefix = "project";
    int highIndex = 0;
    int prefixLength = prefix.length();
    for (String name : existingProjectNames) {
      try {
        if (name.startsWith(prefix)) {
          highIndex = Math.max(highIndex, Integer.parseInt(name.substring(prefixLength)));
        }
      } catch (NumberFormatException e) {
        continue;
      }
    }
    return prefix + (highIndex + 1);
  }

  /**
   * Create a name safe for use in file paths from the provided String {@code
   * str}. It is fairly conservative to attempt, not guarantee, maximum
   * compatability for most operating systems.
   */
  public static String normalizeForFilename(String str) {
    String normalized = VALID_FILENAME_CHARS.retainFrom(str);
    if (!normalized.isEmpty()) {
      while (normalized.length() > 2 &&
             !CharMatcher.JAVA_LETTER.matches(normalized.charAt(0))) {
        normalized = normalized.substring(1);
      }
      if (CharMatcher.JAVA_LETTER.matches(normalized.charAt(0))) {
        return normalized;
      }
    }
    return null;
  }


  /**
   * Converts a String to a JSON String.
   * Returns null if the String is null.
   */
  public static String toJson(String s) {
    if (s != null) {
      StringBuilder sb = new StringBuilder();
      sb.append('"');
      int len = s.length();
      for (int i = 0; i < len; i++) {
        char c = s.charAt(i);
        switch (c) {
          case '\\':
          case '"':
          case '/':
            sb.append('\\').append(c);
            break;
          case '\b':
            sb.append("\\b");
            break;
          case '\f':
            sb.append("\\f");
            break;
          case '\n':
            sb.append("\\n");
            break;
          case '\r':
            sb.append("\\r");
            break;
          case '\t':
            sb.append("\\t");
            break;
          default:
            if (c < ' ' || c > '~') {
              // Replace any special chars with \u1234 unicode
              String hex = "000" + Integer.toHexString(c);
              hex = hex.substring(hex.length() - 4);
              sb.append("\\u" + hex);
            } else {
              sb.append(c);
            }
            break;
        }
      }
      sb.append('"');
      return sb.toString();
    } else {
      return null;
    }
  }
}
