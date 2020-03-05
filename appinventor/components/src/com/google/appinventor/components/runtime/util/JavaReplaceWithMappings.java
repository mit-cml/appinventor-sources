// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020  MIT, All rights reserve
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java implementation of replace-with-mappings. Used for ease
 * of reasoning about the solution.
 * See runtime.scm
 *
 * TODO: Might be better to re-implement this in Scheme/Kawa in the future.
 * TODO: Add unit tests
 */
public final class JavaReplaceWithMappings {
  /**
   * Replaces the specified text string with the specified mappings,
   * which is a map containing Key Value pairs where the key is the
   * substring to replace, and the value is the value to replace the substring with.
   *
   * @param text      Text to apply mappings to
   * @param mappings  Map containing mappings
   * @return Text with the mappings applied
   */
  public static String replaceWithMappings(String text, Map<Object, Object> mappings)
  {
    // We will construct a regex pattern
    StringBuilder patternBuilder = new StringBuilder();

    // Iterate over all the mappings
    Iterator<Map.Entry<Object, Object>> it = mappings.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry<Object, Object> current = it.next();

      // Append mapping that we want to replace to the regex pattern
      patternBuilder.append(current.getKey().toString());

      // If there is still another mapping, then we append the union (OR) operator
      if (it.hasNext()) {
        patternBuilder.append("|");
      }
    }

    // Construct pattern from the constructed pattern string
    // We will then create a matcher from the constructed pattern.
    String patternString = patternBuilder.toString();
    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(text);

    StringBuffer sb = new StringBuffer();

    // Iterate until no more regex matches exist
    while (matcher.find()) {
      // Found mapping
      String found = matcher.group();

      // Get the string to replace with (probably default not needed here,
      // but we add it as a safe-guard just in case something goes wrong)
      String replace = found;

      if (mappings.containsKey(found)) {
        replace = mappings.get(found).toString();
      }

      // Replace the found pattern with the mapped string
      matcher.appendReplacement(sb, replace);
    }

    // Append remainder
    matcher.appendTail(sb);

    // Return result of mappings applied
    return sb.toString();
  }
}
