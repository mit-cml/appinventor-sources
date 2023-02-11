// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020  MIT, All rights reserve
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static junit.framework.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * Test class for JavaStringUtils utility methods.
 */
public class JavaStringUtilsTest {
  /**
   * Test case for replacing a String with no mappings.
   * The String should not be modified.
   */
  @Test
  public void testreplaceAllMappingsNone() {
    final String text = "this is a test string";
    Map<Object, Object> mappings = new LinkedHashMap<>();

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);

    assertEquals(result, text);
  }

  @Test 
  public void testSplitString() {
    YailList text = JavaStringUtils.split("abcde", Pattern.quote(""));
    assertEquals(5, text.size());
  }

  /**
   * Test case for replacing a String with mappings, but none
   * of the mappings are found in the target String.
   * The String should not be modified.
   */
  @Test
  public void testreplaceAllMappingsNoMatches() {
    final String text = "this is a test string";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("abc", "def");
    mappings.put("tset", "test");
    mappings.put("strxng", "string");

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);

    assertEquals(result, text);
  }

  /**
   * Test case for a replacing a String with mappings, where
   * only a single mapping is given.
   * The String should be modified to replace all occurrences
   * of that single mapping.
   */
  @Test
  public void testreplaceAllMappingsSingleMapping() {
    final String text = "this is a tset string, testing, tseting";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("tset", "test");

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "this is a test string, testing, testing";

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that upon applying mappings, they
   * are not applied transitively, but rather only once.
   */
  @Test
  public void testReplaceNoTransitiveMappings() {
    final String text = "Substitute item1 to item2, and then item2 to item3";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("item1", "item2");
    mappings.put("item2", "item3");

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "Substitute item2 to item3, and then item3 to item3";

    // Note how in the expected result, item1 -> item2, rather than
    // item1 -> item2 -> item3
    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that specifying mappings as integers
   * replaces the String correctly.
   */
  @Test
  public void testReplaceIntegers() {
    final String text = "1, 2, 3 and 5, 7, 6";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put(5, 4);
    mappings.put(7,5);

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "1, 2, 3 and 4, 5, 6";

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that specifying mappings as floats
   * replaces the String correctly.
   */
  @Test
  public void testReplaceFloats() {
    final String text = "Pi: 3.14159";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put(3.14159f, 3.14f);

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "Pi: 3.14";

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that specifying mappings where some
   * of the mappings are substrings of each other, and using
   * the longest-string-first order will apply the mappings
   * in the correct order.
   */
  @Test
  public void testReplaceLongestStringFirst() {
    final String text = "ab ba a b";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("a", "d");
    mappings.put("b", "e");
    mappings.put("ab", "bc");
    mappings.put("ba", "cb");

    final String result = JavaStringUtils.replaceAllMappingsLongestStringOrder(text, mappings);
    final String expected = "bc cb d e";

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that specifying mappings where some
   * of the mappings are substrings of each other, and using
   * the earliest occurrence order will apply the mappings
   * in the correct order.
   */
  @Test
  public void testReplaceEarliestOccurrence() {
    final String text = "ab ba a b";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("b", "e");
    mappings.put("a", "d");
    mappings.put("ab", "bc");
    mappings.put("ba", "cb");

    final String result = JavaStringUtils.replaceAllMappingsEarliestOccurrenceOrder(text, mappings);
    final String expected = "bc ed d e";

    assertEquals(expected, result);
  }

  /**
   * Test case to verify that the union character gets escaped
   * rather than interpreted in the regex pattern from the mapping
   * when replacing a String with mappings.
   */
  @Test
  public void testEscapeUnionCharacter() {
    final String text = "a b c d";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("a|b", "x");
    mappings.put("c", "y");

    final String result = JavaStringUtils.replaceAllMappingsLongestStringOrder(text, mappings);
    final String expected = "a b y d";

    assertEquals(expected, result);
  }

  /**
   * Test case to verify that conflicting orders are treated properly
   * upon replacing a string with mappings in longest string first order
   * for which we have keys that share some part of a string.
   */
  @Test
  public void testReplaceLongestStringFirstOrderTest() {
    final String text = "that you were good";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("you were", "I was");
    mappings.put("you", "I");
    mappings.put("at you", "at me");

    final String result = JavaStringUtils.replaceAllMappingsLongestStringOrder(text, mappings);
    final String expected = "that I was good";

    assertEquals(expected, result);
  }

  /**
   * Test case to verify that conflicting orders are treated properly
   * upon replacing a string with mappings in dictionary order
   * for which we have keys that share some part of a string.
   */
  @Test
  public void testReplaceDictionaryOrderOrderTest() {
    final String text = "abcdef";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("cd", "g");
    mappings.put("abc", "h");
    mappings.put("ab", "i");
    mappings.put("g", "x");

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "igef";

    assertEquals(expected, result);
  }

  /**
   * Test case to verify that replacing mappings in an empty String
   * with nothing to replace causes no issues.
   */
  @Test
  public void testReplaceMappingsEmptyString() {
    final String text = "";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("x", "y");
    mappings.put("a", "d");

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "";

    assertEquals(expected, result);
  }

  /**
   * Test case to verify that replacing mappings in an empty String
   * with a mapping that maps the empty String to some substring
   * correctly replaces it.
   */
  @Test
  public void testReplaceMappingsEmptyStringReplaced() {
    final String text = "";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("abc", "d");
    mappings.put("", "abc");

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "abc";

    assertEquals(expected, result);
  }

  /**
   * Test case to verify that replacing mappings in a non-empty
   * String with a mapping that maps an empty string to some String
   * replaces all gaps in the String.
   */
  @Test
  public void testReplaceMappingsEmptyStringMapping() {
    final String text = "ax";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("", "_");

    final String result = JavaStringUtils.replaceAllMappingsDictionaryOrder(text, mappings);
    final String expected = "_a_x_";

    assertEquals(expected, result);
  }
}
