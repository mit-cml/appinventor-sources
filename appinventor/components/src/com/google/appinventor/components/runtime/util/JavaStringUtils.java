// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2020  MIT, All rights reserve
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;

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
 * Java implementation of string utility methods for use in Scheme calls.
 * Used for ease of reasoning about the solution or to address issues
 * with Kawa (e.g. memory problems).
 * See runtime.scm
 */
public class JavaStringUtils {
  /**
   * Auxiliary class for ReplaceWithMappings that defines
   * the mapping application order for a given List of keys.
   * Default option is to do nothing with the order, which represents
   * the dictionary order.
   */
  private static class MappingOrder {
    /**
     * Changes the order of the specified key list
     * @param keys  List of keys
     * @param text  Text in which to search for keys
     */
    public void changeOrder(List<String> keys, String text) {
      // Default option: Do nothing (dictionary order)
    }
  }

  /**
   * Changes the key order in descending order of key length.
   */
  private static class MappingLongestStringFirstOrder extends MappingOrder {
    @Override
    public void changeOrder(List<String> keys, String text) {
      Collections.sort(keys, new Comparator<String>() {
        @Override
        public int compare(String s, String t1) {
          // Sort in descending order of string length
          return Integer.compare(t1.length(), s.length());
        }
      });
    }
  }

  /**
   * Changes the key order based on the earliest occurrences in the original
   * text string.
   */
  private static class MappingEarliestOccurrenceFirstOrder extends MappingOrder {
    @Override
    public void changeOrder(List<String> keys, String text) {
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
  }

  public static final String LOG_TAG_JOIN_STRINGS = "JavaJoinListOfStrings";
  private static final boolean DEBUG = false;


  // Implements the following operation

  // (define join-strings (strings separator)
  //    (JavaJoinListOfStrings:joinStrings strings separator))

  // I'm writing this in Java, rather than using Kawa in runtime.scm
  // because Kawa seems to blow out memory (or stack?) on small-memory systems
  // and large lists.

  /**
   * Java implementation of join-strings since the Kawa version appears to run of space.
   * See runtime.scm
   *
   * The elements in listOString are Kawa strings, but these are
   * not necessarily Java Strings.   They might be FStrings.   So we
   * accept a list of Objects and use toString to do a conversion.
   *
   *
   * @author halabelson@google.com (Hal Abelson)
   */
  public static String joinStrings(List<Object> listOfStrings, String separator) {
    // We would use String.join, but that is Java 8
    if (DEBUG) {
      Log.i(LOG_TAG_JOIN_STRINGS, "calling joinStrings");
    }
    return join(listOfStrings, separator);
  }

  private static String join(List<Object> list, String separator)
  {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Object item : list)
    {
      if (first)
        first = false;
      else
        sb.append(separator);
      sb.append(item.toString());
    }
    return sb.toString();
  }

  /**
   * Replaces the specified text string with the specified mappings in
   * dictionary element order.
   *
   * @see #replaceWithMappings(String, Map, MappingOrder)
   * @see MappingOrder
   * @param text      Text to apply mappings to
   * @param mappings  Map containing mappings
   * @return Text with the mappings applied
   */
  public static String replaceWithMappingsDictionaryOrder(String text, Map<Object, Object> mappings) {
    return replaceWithMappings(text, mappings, new MappingOrder());
  }

  /**
   * Replaces the specified text string with the specified mappings in
   * longest string first order.
   *
   * @see #replaceWithMappings(String, Map, MappingOrder)
   * @see MappingLongestStringFirstOrder
   * @param text      Text to apply mappings to
   * @param mappings  Map containing mappings
   * @return Text with the mappings applied
   */
  public static String replaceWithMappingsLongestStringOrder(String text, Map<Object, Object> mappings) {
    return replaceWithMappings(text, mappings, new MappingLongestStringFirstOrder());
  }

  /**
   * Replaces the specified text string with the specified mappings in
   * earliest occurrence first order.
   *
   * @see #replaceWithMappings(String, Map, MappingOrder)
   * @see MappingEarliestOccurrenceFirstOrder
   * @param text      Text to apply mappings to
   * @param mappings  Map containing mappings
   * @return Text with the mappings applied
   */
  public static String replaceWithMappingsEarliestOccurrenceOrder(String text, Map<Object, Object> mappings) {
    return replaceWithMappings(text, mappings, new MappingEarliestOccurrenceFirstOrder());
  }

  /**
   * Replaces the specified text string with the specified mappings,
   * which is a map containing Key Value pairs where the key is the
   * substring to replace, and the value is the value to replace the substring with.
   * An order parameter is specified to indicate the order of mapping applications.
   *
   * TODO: Might be better to re-implement this in Scheme/Kawa in the future.
   *
   * @param text     Text to apply mappings to
   * @param mappings Map containing mappings
   * @param order    Order to use for replacing mappings
   * @return Text with the mappings applied
   */
  public static String replaceWithMappings(String text, Map<Object, Object> mappings, MappingOrder order) {
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

    // Change the order of the keys based on the given Order object
    order.changeOrder(keys, text);

    // Construct pattern from the constructed pattern string
    // We will then create a matcher from the constructed pattern.
    String patternString = buildUnionFromStrings(keys);
    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(text);

    return applyMappings(matcher, stringMappings);
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

  /**
   * Auxiliary function to build a String of keys combined via
   * regex union operators.
   * The keys are escaped to prevent them being interpreted as regex.
   *
   * @param keys  List of keys to union together
   * @return  Single Regex String of keys combined by unions
   */
  private static String buildUnionFromStrings(List<String> keys) {
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

    return patternBuilder.toString();
  }
}
