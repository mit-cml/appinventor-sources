// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests JsonUtil class.
 *
 *
 */
public class JsonUtilTest extends TestCase {

  // The elements in this next test were commented out due to a change in the
  // Json.javq library that makes geStringListFromJsonArray throw an error
  // if the item being operated on is not a string.
  public void testGetStringListFromJsonArray() throws JSONException {
//   Object[] firstArray = {"Houston", "we", "have", "a", "problem"};
//   Object[] secondArray = {"China", "we", "have", "an", "ultimatum"};

    List<String> mixedList = new ArrayList<String>();
    mixedList.add("Hello.");
    mixedList.add("O hi.");
    mixedList.add(new Integer(9).toString());
//    mixedList.add(new JSONArray(Arrays.asList(firstArray)).toString());
//    mixedList.add(new JSONArray(Arrays.asList(secondArray)).toString());

    String jsonInput =
      "[" +
      "\"Hello.\"," +
      "\"O hi.\"," +
      "\"9\","  +
      //    "[\"Houston\",\"we\",\"have\",\"a\",\"problem\"]," +
      //   "[\"China\",\"we\",\"have\",\"an\",\"ultimatum\"]" +
      "]";
    List<String> testList;
    testList = JsonUtil.getStringListFromJsonArray(new JSONArray(jsonInput));
    for (int i = 0; i < testList.size(); i++) {
      assertEquals(testList.get(i), mixedList.get(i));
    }
  }

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
      "\"faLse\"," +
      "]";
    List<Object> testList;
    testList = JsonUtil.getListFromJsonArray(new JSONArray(jsonInput));
    for (int i = 0; i < testList.size(); i++) {
      assertEquals(testList.get(i), mixedList.get(i));
    }
  }

  public void testGetListFromJsonObject() throws JSONException {
    String jsonInput = "{\"a\": 1, \"c\": [\"a\", \"b\", \"c\"], " +
        "\"b\": \"boo\", \"d\": {\"e\": \"f\"}}";
    JSONObject object = new JSONObject(jsonInput);

    List<Object> returnList = JsonUtil.getListFromJsonObject(object);
    List<Object> aList = Arrays.asList(new Object[] {"a", 1});
    List<Object> bList = Arrays.asList(new Object[] {"b", "boo"});
    List<Object> cList = Arrays.asList(new Object[] {"c",
       Arrays.asList(new Object[] {"a", "b", "c"})});
    List<Object> dList = Arrays.asList(new Object[] {"d",
       Arrays.asList(new Object[] {Arrays.asList(new Object[] {"e", "f"})})});

    assertEquals(returnList.get(0), aList);
    assertEquals(returnList.get(1), bList);
    assertEquals(returnList.get(2), cList);
    assertEquals(returnList.get(3), dList);
  }

  public void testConvertBoolean() throws JSONException {
    assertEquals(true, JsonUtil.convertJsonItem("true"));
    assertEquals(true, JsonUtil.convertJsonItem(true));
    assertEquals(false, JsonUtil.convertJsonItem("false"));
    assertEquals(false, JsonUtil.convertJsonItem(false));
    assertEquals(true, JsonUtil.convertJsonItem("tRue"));
    assertEquals(false, JsonUtil.convertJsonItem("faLse"));
  }

  public void testConvertNumber() throws JSONException {
    String jsonInput = "[9.5,-9.5,9,-9,123456789101112,0xF]";
    JSONArray array = new JSONArray(jsonInput);
    assertEquals(9.5, JsonUtil.convertJsonItem(array.get(0)));
    assertEquals(-9.5, JsonUtil.convertJsonItem(array.get(1)));
    assertEquals(9, JsonUtil.convertJsonItem(array.get(2)));
    assertEquals(-9, JsonUtil.convertJsonItem(array.get(3)));
    assertEquals(123456789101112L, JsonUtil.convertJsonItem(array.get(4)));
//    assertEquals(15, JsonUtil.convertJsonItem(array.get(5)));
//    The above line used to work before the JSON library was changed.
    assertEquals("0xF", JsonUtil.convertJsonItem(array.get(5)));
  }
  
  public void testConvertEmpty() throws JSONException {
    Object shouldBeEmpty = JsonUtil.getObjectFromJson("");
    assertEquals("", JsonUtil.getObjectFromJson(""));
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * from an element that is not a List returns an empty List.
   */
  public void testGetColumnsFromJSONNotList() {
    String json = "1";

    YailList result = JsonUtil.getColumnsFromJSON(json);
    YailList expected = new YailList();

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing one Array entry with multiple elements
   * returns a List containing the appropriate column.
   */
  public void testGetColumnsFromJSONArrayMultipleEntries() {
    String json = "{" +
        "\"array\": " +
        "[1,2,3]" +
        "}";

    YailList expectedColumn = YailList.makeList(Arrays.asList("array", "1", "2", "3"));
    YailList expected = YailList.makeList(Collections.singletonList(expectedColumn));

    YailList result = JsonUtil.getColumnsFromJSON(json);
    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing one Array entry with a single element
   * returns a List containing the appropriate column.
   */
  public void testGetColumnsFromJSONArraySingleEntry() {
    String json = "{" +
        "\"array\": " +
        "[7]" +
        "}";

    YailList expectedColumn = YailList.makeList(Arrays.asList("array", "7"));
    YailList expected = YailList.makeList(Collections.singletonList(expectedColumn));

    YailList result = JsonUtil.getColumnsFromJSON(json);
    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing one non-Array entry returns a List containing
   * the appropriate column.
   */
  public void testGetColumnsFromJSONElement() {
    String json = "{" +
        "\"value\": " +
        "\"test-value\"" +
        "}";

    YailList expectedColumn = YailList.makeList(Arrays.asList("value", "test-value"));
    YailList expected = YailList.makeList(Collections.singletonList(expectedColumn));

    YailList result = JsonUtil.getColumnsFromJSON(json);
    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that retrieving columns from JSON
   * containing mixed entries (both Array and non-array)
   * returns a List of the appropriate columns.
   */
  public void testGetColumnsFromJSONMixedEntries() {
    String json = "{" +
        "\"array\": [1,2,3]," +
        "\"values\": [4,7,2,1]," +
        "\"test-value\": \"element\"" +
        "}";

    YailList expected1 = YailList.makeList(Arrays.asList("array", "1", "2", "3"));
    YailList expected2 = YailList.makeList(Arrays.asList("values", "4", "7", "2", "1"));
    YailList expected3 = YailList.makeList(Arrays.asList("test-value", "element"));

    YailList result = JsonUtil.getColumnsFromJSON(json);

    // Since JSON stores it's properties in Sets, order cannot be guaranteed here, hence
    // contains is checked instaed.
    assertTrue(result.contains(expected1));
    assertTrue(result.contains(expected2));
    assertTrue(result.contains(expected3));
  }
}
