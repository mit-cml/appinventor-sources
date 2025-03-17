// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests JsonUtil class.
 *
 *
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest="tests/AndroidManifest.xml")
public class JsonUtilTest {

  // The elements in this next test were commented out due to a change in the
  // Json.javq library that makes geStringListFromJsonArray throw an error
  // if the item being operated on is not a string.
  @Test
  public void testGetStringListFromJsonArray() throws JSONException {
//   Object[] firstArray = {"Houston", "we", "have", "a", "problem"};
//   Object[] secondArray = {"China", "we", "have", "an", "ultimatum"};

    List<String> mixedList = new ArrayList<String>();
    mixedList.add("Hello.");
    mixedList.add("O hi.");
    mixedList.add(Integer.toString(9));
//    mixedList.add(new JSONArray(Arrays.asList(firstArray)).toString());
//    mixedList.add(new JSONArray(Arrays.asList(secondArray)).toString());

    String jsonInput =
      "[" +
      "\"Hello.\"," +
      "\"O hi.\"," +
      "\"9\""  +
      //    "[\"Houston\",\"we\",\"have\",\"a\",\"problem\"]," +
      //   "[\"China\",\"we\",\"have\",\"an\",\"ultimatum\"]" +
      "]";
    List<String> testList;
    testList = JsonUtil.getStringListFromJsonArray(new JSONArray(jsonInput));
    for (int i = 0; i < testList.size(); i++) {
      assertEquals(testList.get(i), mixedList.get(i));
    }
  }

  @Test
  public void testGetListFromJsonArray() throws JSONException {
    Object[] firstArray = {"Houston", "we", "have", "a", "problem"};
    List<Object> firstList = Arrays.asList(firstArray);
    Object[] secondArray = {"China", "we", "have", "an", "ultimatum"};
    List<Object> secondList = Arrays.asList(secondArray);
    List<Object> mixedList = new ArrayList<Object>();
    mixedList.add("Hello.");
    mixedList.add("O hi.");
    mixedList.add(9);
    mixedList.add(firstList);
    mixedList.add(secondList);
    mixedList.add(9.5);
    mixedList.add(true);
    mixedList.add(false);

    String jsonInput =
      "[" +
      "\"Hello.\"," +
      "\"O hi.\"," +
      "9," +
      "[\"Houston\",\"we\",\"have\",\"a\",\"problem\"]," +
      "[\"China\",\"we\",\"have\",\"an\",\"ultimatum\"]," +
      "9.5," +
      "true," +
      "\"faLse\"" +
      "]";
    List<Object> testList;
    testList = JsonUtil.getListFromJsonArray(new JSONArray(jsonInput));
    for (int i = 0; i < testList.size(); i++) {
      assertEquals(testList.get(i), mixedList.get(i));
    }
  }

  @Test
  public void testGetListFromJsonObject() throws JSONException {
    String jsonInput = "{\"a\": 1, \"c\": [\"a\", \"b\", \"c\"], " +
        "\"b\": \"boo\", \"d\": {\"e\": \"f\"}}";
    JSONObject object = new JSONObject(jsonInput);
    YailDictionary returnDict = JsonUtil.getDictionaryFromJsonObject(object);
    assertNotNull(returnDict);
    assertEquals(1, returnDict.get("a"));
    assertEquals("boo", returnDict.get("b"));
    assertEquals(YailList.makeList(Arrays.asList("a", "b", "c")), returnDict.get("c"));
    assertEquals(YailDictionary.makeDictionary("e", "f"), returnDict.get("d"));
  }

  @Test
  public void testConvertBoolean() throws JSONException {
    assertEquals(true, JsonUtil.convertJsonItem("true"));
    assertEquals(true, JsonUtil.convertJsonItem(true));
    assertEquals(false, JsonUtil.convertJsonItem("false"));
    assertEquals(false, JsonUtil.convertJsonItem(false));
    assertEquals(true, JsonUtil.convertJsonItem("tRue"));
    assertEquals(false, JsonUtil.convertJsonItem("faLse"));
  }

  @Test
  public void testConvertNumber() throws JSONException {
    String jsonInput = "[9.5,-9.5,9,-9,123456789101112,0xF]";
    JSONArray array = new JSONArray(jsonInput);
    assertEquals(9.5, JsonUtil.convertJsonItem(array.get(0)));
    assertEquals(-9.5, JsonUtil.convertJsonItem(array.get(1)));
    assertEquals(9, JsonUtil.convertJsonItem(array.get(2)));
    assertEquals(-9, JsonUtil.convertJsonItem(array.get(3)));
    assertEquals(123456789101112L, JsonUtil.convertJsonItem(array.get(4)));
    assertEquals(15, JsonUtil.convertJsonItem(array.get(5)));
  }

  @Test
  public void testConvertEmpty() throws JSONException {
    Object shouldBeEmpty = JsonUtil.getObjectFromJson("");
    assertEquals("", JsonUtil.getObjectFromJson(""));
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * from an element that is not a List returns an empty List.
   */
  @Test
  public void testGetColumnsFromJsonNotList() throws JSONException {
    String json = "1";

    YailList result = JsonUtil.getColumnsFromJson(json);
    YailList expected = new YailList();

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing one Array entry with multiple elements
   * returns a List containing the appropriate column.
   */
  @Test
  public void testGetColumnsFromJsonArrayMultipleEntries() throws JSONException {
    String json = "{"
        + "\"array\": "
        + "[1,2,3]"
        + "}";

    YailList expectedColumn = YailList.makeList(Arrays.asList("array", "1", "2", "3"));
    YailList expected = YailList.makeList(Collections.singletonList(expectedColumn));

    YailList result = JsonUtil.getColumnsFromJson(json);
    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing one Array entry with a single element
   * returns a List containing the appropriate column.
   */
  @Test
  public void testGetColumnsFromJsonArraySingleEntry() throws JSONException {
    String json = "{"
        + "\"array\": "
        + "[7]"
        + "}";

    YailList expectedColumn = YailList.makeList(Arrays.asList("array", "7"));
    YailList expected = YailList.makeList(Collections.singletonList(expectedColumn));

    YailList result = JsonUtil.getColumnsFromJson(json);
    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing one non-Array entry returns a List containing
   * the appropriate column.
   */
  @Test
  public void testGetColumnsFromJsonElement() throws JSONException {
    String json = "{"
        + "\"value\": "
        + "\"test-value\""
        + "}";

    YailList expectedColumn = YailList.makeList(Arrays.asList("value", "test-value"));
    YailList expected = YailList.makeList(Collections.singletonList(expectedColumn));

    YailList result = JsonUtil.getColumnsFromJson(json);
    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing mixed entries (both Array and non-array)
   * returns a List of the appropriate columns.
   */
  @Test
  public void testGetColumnsFromJsonMixedEntries() throws JSONException {
    String json = "{"
        + "\"array\": [1,2,3],"
        + "\"values\": [4,7,2,1],"
        + "\"test-value\": \"element\""
        + "}";

    YailList expected1 = YailList.makeList(Arrays.asList("array", "1", "2", "3"));
    YailList expected2 = YailList.makeList(Arrays.asList("values", "4", "7", "2", "1"));
    YailList expected3 = YailList.makeList(Arrays.asList("test-value", "element"));

    YailList result = JsonUtil.getColumnsFromJson(json);

    // Since JSON stores it's properties in Sets, order cannot be guaranteed here, hence
    // contains is checked instaed.
    assertTrue(result.contains(expected1));
    assertTrue(result.contains(expected2));
    assertTrue(result.contains(expected3));
  }
}
