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
import java.util.TreeSet;
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
   * Auxiliary class for replaceAllMappings that defines
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
   *
   * TODO: If we do re-implement earliest occurrence at any time, it should
   * TODO: probably take the regex order (so that the keys are replaced in
   * TODO: order of actual occurrence variably) rather than the current
   * TODO: static order implemented here.
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

  /**
   * Auxiliary Range class to store the index range (substring indices)
   * of some original String to replace with the given text.
   */
  private static class Range {
    int start;   // Inclusive start index
    int end;     // Exclusive end index
    String text; // Replacement text

    /**
     * Create a new Range object instance.
     *
     * @param start  start index of the range (inclusive)
     * @param end    end index of the range (exclusive)
     * @param text   text to replace range with
     */
    public Range(int start, int end, String text) {
      this.start = start;
      this.end = end;
      this.text = text;
    }
  }
  /*
   * Range comparator that sorts ranges in descending order of range
   * end times. Overlapping ranges are considered equal by the comparator.
   */
  private static class RangeComparator implements Comparator<Range> {
    @Override
    public int compare(Range r1, Range r2) {
      // First, test for overlap. We do this by taking the maximum
      // start point of a range, and the minimum end point of a range.
      int maxStart = Math.max(r1.start, r2.start);
      int minEnd = Math.min(r1.end, r2.end);

      // If maxStart <= minEnd, the ranges overlap (or touch). Since the
      // min end index is exclusive, we take the strictly less < instead,
      // since the ranges could touch (due to the end ID being overlapping)
      if (maxStart < minEnd) {
        // Ranges overlap. Consider them equal, thus not inserting a new range.
        return 0;
      } else {
        // Ranges unequal. Sort by endpoint in descending order to get the last
        // range first in the TreeSet.
        return Integer.compare(r2.end, r1.end);
      }
    }
  }

  public static final String LOG_TAG_JOIN_STRINGS = "JavaJoinListOfStrings";
  private static final boolean DEBUG = false;

  /**
   * Since mapping orders do not have state, we initialize
   * fixed final MappingOrders to use for replaceAllMappings.
   */
  private static final MappingOrder mappingOrderDictionary = new MappingOrder();
  private static final MappingOrder mappingOrderLongestStringFirst = new MappingLongestStringFirstOrder();
  private static final MappingOrder mappingOrderEarliestOccurrence = new MappingEarliestOccurrenceFirstOrder();
  private static final Comparator<Range> rangeComparator = new RangeComparator();

  /**
   * Java implementation of join-strings since the Kawa version appears to run of space.
   * See runtime.scm
   *
   * The elements in listOString are Kawa strings, but these are
   * not necessarily Java Strings.   They might be FStrings.   So we
   * accept a list of Objects and use toString to do a conversion.
   *
   * Implements the following operation
   *
   * (define join-strings (strings separator)
   *    (JavaJoinListOfStrings:joinStrings strings separator))
   *
   * I'm writing this in Java, rather than using Kawa in runtime.scm
   * because Kawa seems to blow out memory (or stack?) on small-memory systems
   * and large lists.
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

  /**
   * Splits the given {@code text} into one or more parts separated by {@code at}. This version
   * attempts to match the behavior of Java 8+ where:
   *
   * <p><quote>
   *   When there is a positive-width match at the beginning of this string then an empty leading
   *   substring is included at the beginning of the resulting array. A zero-width match at the
   *   beginning however never produces such empty leading substring.
   * </quote></p>
   *
   * @param text the string to split
   * @param at the substring to split on
   * @return a YailList of one or more substrings
   */
  public static YailList split(String text, String at) {
    List<String> parts = new ArrayList<>();
    Collections.addAll(parts, text.split(at));
    if (Pattern.quote("").equals(at) && parts.get(0).equals("")) {
      parts.remove(0);
    }
    return YailList.makeList(parts);
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
   * @see #replaceAllMappings(String, Map, MappingOrder)
   * @see MappingOrder
   * @param text      Text to apply mappings to
   * @param mappings  Map containing mappings
   * @return Text with the mappings applied
   */
  public static String replaceAllMappingsDictionaryOrder(String text, Map<Object, Object> mappings) {
    return replaceAllMappings(text, mappings, mappingOrderDictionary);
  }

  /**
   * Replaces the specified text string with the specified mappings in
   * longest string first order.
   *
   * @see #replaceAllMappings(String, Map, MappingOrder)
   * @see MappingLongestStringFirstOrder
   * @param text      Text to apply mappings to
   * @param mappings  Map containing mappings
   * @return Text with the mappings applied
   */
  public static String replaceAllMappingsLongestStringOrder(String text, Map<Object, Object> mappings) {
    return replaceAllMappings(text, mappings, mappingOrderLongestStringFirst);
  }

  /**
   * Replaces the specified text string with the specified mappings in
   * earliest occurrence first order.
   *
   * @see #replaceAllMappings(String, Map, MappingOrder)
   * @see MappingEarliestOccurrenceFirstOrder
   * @param text      Text to apply mappings to
   * @param mappings  Map containing mappings
   * @return Text with the mappings applied
   */
  public static String replaceAllMappingsEarliestOccurrenceOrder(String text, Map<Object, Object> mappings) {
    return replaceAllMappings(text, mappings, mappingOrderEarliestOccurrence);
  }

  /**
   * Replaces the specified text string with the specified mappings,
   * which is a map containing Key Value pairs where the key is the
   * substring to replace, and the value is the value to replace the substring with.
   * An order parameter is specified to indicate the order of mapping applications.
   *
   * @param text     Text to apply mappings to
   * @param mappings Map containing mappings
   * @param order    Order to use for replacing mappings
   * @return Text with the mappings applied
   */
  public static String replaceAllMappings(String text, Map<Object, Object> mappings, MappingOrder order) {
    // Iterate over all the mappings
    Iterator<Map.Entry<Object, Object>> it = mappings.entrySet().iterator();

    // Construct a new map for <String, String> mappings in order to support
    // look-ups for non-pure String values (e.g. numbers)
    Map<String, String> stringMappings = new HashMap<>();

    // Construct a new List to store the Map's keys
    List<String> keys = new ArrayList<>();

    while (it.hasNext()) {
      Map.Entry<Object, Object> current = it.next();

      // Get Key & Value as strings
      // This is needed to convert any non-String values to a String,
      // e.g. numbers to String literals
      String key = current.getKey().toString();
      String value = current.getValue().toString();

      // Add key only if it was not added before (to reduce potential
      // redundancy on potentially duplicate keys)
      if (!stringMappings.containsKey(key)) {
        keys.add(key);
      }

      // Update map
      stringMappings.put(key, value);
    }

    // Change the order of the keys based on the given Order object
    // TODO: If we have stream support, we can sort the stringMappings map
    // TODO: directly provided we initialize it to a LinkedHashMap.
    // TODO: It could save some memory in the long run.
    order.changeOrder(keys, text);

    // Apply mappings owith re-ordered keys and mappings
    return applyMappings(text, stringMappings, keys);
  }

  /**
   * Auxiliary function to apply the mappings provided in the form of a map
   * to the given text string. A supplementary keys list is provided to specify
   * key order (the first element in the list is the first key to replace,
   * the second is the second and so on).
   *
   * The method applies mappings by making use of a range set that keeps track of
   * which indices have been replaced to avoid conflicts. Each key is traversed
   * one by one, and the ranges of replacement are updated, provided that the
   * range is not already overlapped/enclosed by other ranges. Finally, once
   * we have all the ranges, the ranges are replaced in order of descending
   * end point of the range so as to keep the previous range end points invariable
   * (if we replace the largest end point, and there are no overlaps, no range
   * will ever hit the start point of the last range, therefore all indices
   * of the previous range remain unaffected)
   *
   * TODO: By optimizing the way strings are replaced (reducing substrings
   * TODO: to use on smaller strings), we can achieve better runtime complexity
   * TODO: on this end.
   *
   * @param text      Text to apply mappings to
   * @param mappings  Mappings in the form {String -> String}
   * @param keys      List of keys to replace (all values must exist in the mappings map)
   * @return  String with the mappings applied
   */
  private static String applyMappings(String text, Map<String, String> mappings, List<String> keys) {
    // Create a set of ranges to keep track of which index ranges in the
    // original text string are already set for replacement, together with the
    // text to replace with. We sort this TreeSet in descending order to preserve
    // indices of all ranges after replacement.
    TreeSet<Range> ranges = new TreeSet<Range>(rangeComparator);

    // Range construction step: Iterate through all the keys,
    // and fill in ranges set & replacements map.
    for (String key : keys) {
      // Convert key to pattern, and create a matcher to find all
      // occurrences of the current string.
      Pattern keyPattern = Pattern.compile(Pattern.quote(key));
      Matcher matcher =  keyPattern.matcher(text);

      // Keep track of the String to replace key with
      String replacement = mappings.get(key);

      // Iterate until the key can no longer be found in text.
      while (matcher.find()) {
        // Get start & end indices of the string to be replaced.
        int startId = matcher.start();
        int endId = matcher.end();

        // Create a closed open range (closed since startId is inclusive,
        // and open because endId is exclusive), and add the range
        // to our TreeSet of ranges. If the range is already covered (i.e.
        // there exists an overlapping range), it is simply not added.
        Range range = new Range(startId, endId, replacement);
        ranges.add(range);
      }
    }

    // Go through each entry that we want to replace. Since we used
    // a TreeSet, we have an order that will not break things;
    // We first replace the substring with the largest end index, which,
    // because of overlap, will not affect the previous range indices
    // because no ranges overlap in our range set.
    // If we did not have this order, then we would have to update all indices
    // of all ranges upon replacement.
    for (Range range : ranges) {
      // Combine strings: L + M + R, where:
      // L - substring from start of string until endpoint
      // M - middle string (the one that we use as replacement)
      // R - remainder of the string after replacement
      String left = text.substring(0, range.start);
      String middle = range.text;
      String end = text.substring(range.end);
      text = left + middle + end;
    }

    return text;
  }
}
