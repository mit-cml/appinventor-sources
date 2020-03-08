// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020  MIT, All rights reserve
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java implementation of replace-with-mappings. Used for ease
 * of reasoning about the solution.
 * See runtime.scm
 * <p>
 * TODO: Might be better to re-implement this in Scheme/Kawa in the future.
 * TODO: Add unit tests
 */
public final class JavaReplaceWithMappings {
  /**
   * Replaces the specified text string with the specified mappings,
   * which is a map containing Key Value pairs where the key is the
   * substring to replace, and the value is the value to replace the substring with.
   * A mode parameter is also specified to indicate the order of mapping application:
   * 0 - longest string first
   * 1 - dictionary order
   * 2 - earliest string first
   *
   * @param text     Text to apply mappings to
   * @param mappings Map containing mappings
   * @param mode     Mode to use for replacing mappings
   * @return Text with the mappings applied
   */
  public static String replaceWithMappings(String text, Map<Object, Object> mappings, int mode) {
    // Iterate over all the mappings
    Iterator<Map.Entry<Object, Object>> it = mappings.entrySet().iterator();

    // Construct a new map for <String, String> mappings in order to support
    // look-ups for non-pure String values (e.g. numbers)
    Map<String, String> stringMappings = new HashMap<>();

    // Construct a new List to store the Map's keys
    List<String> keys = new ArrayList<>();

    while (it.hasNext()) {
      Map.Entry<Object, Object> current = it.next();

      // Get Key & Value, and update string mappings map
      String key = current.getKey().toString();
      String value = current.getValue().toString();
      stringMappings.put(key, value);

      // Add key
      keys.add(key);
    }

    // TODO: Should probably refactor this somehow so it's less hardcoded.
    // TODO: Maybe create new function defs in scheme instead, and map mode -> function call?
    // Note: We do not check mode: 1 since that's the default order
    // that the keys were inserted in (the dictionary order)
    if (mode == 0) {
      sortKeysOnLargestSizeFirst(keys);
    } else if (mode == 2) {
      sortKeysOnEarliestOccurrenceFirst(text, keys);
    }

    // We will construct a union regex pattern from the keys
    StringBuilder patternBuilder = new StringBuilder();

    for (int i = 0; i < keys.size(); ++i) {
      // Escape the key string so that any regex characters not get
      // interpreted directly.
      String key = Pattern.quote(keys.get(i));

      // Append mapping that we want to replace to the regex pattern
      patternBuilder.append(key);

      // If there is still another mapping, then we append the union (OR) operator
      if ((i+1) < keys.size()) {
        patternBuilder.append("|");
      }
    }

    // Construct pattern from the constructed pattern string
    // We will then create a matcher from the constructed pattern.
    String patternString = patternBuilder.toString();
    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(text);

    return applyMappings(matcher, stringMappings);
  }

  /**
   * Sort the given List of keys in order of largest length first.
   * @param keys  Keys (List of Strings) to sort
   */
  private static void sortKeysOnLargestSizeFirst(final List<String> keys) {
    Collections.sort(keys, new Comparator<String>() {
      @Override
      public int compare(String s, String t1) {
        // Sort in descending order of string length
        return Integer.compare(t1.length(), s.length());
      }
    });
  }

  /**
   * Sort the given List of keys in order of earliest occurrence first in the
   * text string.
   * @param text  Text string
   * @param keys  Keys 9List of Strings) to sort
   */
  private static void sortKeysOnEarliestOccurrenceFirst(final String text, final List<String> keys) {
    // Construct a map for first index of occurrence for String
    final Map<String, Integer> occurrenceIndices = new HashMap<>();

    // TODO: Can we optimize the O(mn) loop with m = length of text,
    // TODO: n = number of keys?
    for (String key : keys) {
      int firstIndex = text.indexOf(key);

      // No first index; Key should gain less priority than
      // other occurrences (this value can be arbitrary)
      if (firstIndex == -1) {
        firstIndex = text.length() + occurrenceIndices.size();
      }

      // Map key to first index of occurrence
      occurrenceIndices.put(key, firstIndex);
    }

    Collections.sort(keys, new Comparator<String>() {
      @Override
      public int compare(String s, String t1) {
        // Sort in ascending order by first index in String
        int id1 = occurrenceIndices.get(s);
        int id2 = occurrenceIndices.get(t1);

        if (id1 == id2) {
          // Use longer string instead if indices equal
          return Integer.compare(t1.length(), s.length());
        } else {
          // Take smaller index first
          return Integer.compare(id1, id2);
        }
      }
    });
  }

  /**
   * Auxiliary function to apply the mappings provided to the given
   * matcher that has some regex pattern loaded.
   *
   * @param matcher  Matcher representing matcher of Pattern on text
   * @param mappings String -> String mappings to replace from Key to Value
   * @return String with mappings applied
   */
  private static String applyMappings(Matcher matcher, Map<String, String> mappings) {
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
