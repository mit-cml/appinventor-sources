// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020  MIT, All rights reserve
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for the JavaReplaceWithMappings utility.
 */
public class JavaReplaceWithMappingsTest {
  /**
   * Test case for replacing a String with no mappings.
   * The String should not be modified.
   */
  @Test
  public void testReplaceWithMappingsNone() {
    final String text = "this is a test string";
    Map<Object, Object> mappings = new LinkedHashMap<>();

    final String result = JavaReplaceWithMappings.replaceWithMappings(text, mappings);

    assertEquals(result, text);
  }

  /**
   * Test case for replacing a String with mappings, but none
   * of the mappings are found in the target String.
   * The String should not be modified.
   */
  @Test
  public void testReplaceWithMappingsNoMatches() {
    final String text = "this is a test string";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("abc", "def");
    mappings.put("tset", "test");
    mappings.put("strxng", "string");

    final String result = JavaReplaceWithMappings.replaceWithMappings(text, mappings);

    assertEquals(result, text);
  }

  /**
   * Test case for a replacing a String with mappings, where
   * only a single mapping is given.
   * The String should be modified to replace all occurrences
   * of that single mapping.
   */
  @Test
  public void testReplaceWithMappingsSingleMapping() {
    final String text = "this is a tset string, testing, tseting";
    Map<Object, Object> mappings = new LinkedHashMap<>();
    mappings.put("tset", "test");

    final String result = JavaReplaceWithMappings.replaceWithMappings(text, mappings);
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

    final String result = JavaReplaceWithMappings.replaceWithMappings(text, mappings);
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

    final String result = JavaReplaceWithMappings.replaceWithMappings(text, mappings);
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

    final String result = JavaReplaceWithMappings.replaceWithMappings(text, mappings);
    final String expected = "Pi: 3.14";

    assertEquals(expected, result);
  }

  // TODO: Add conflicting replacement tests
}