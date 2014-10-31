// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import gnu.lists.FString;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Tests YailList class.
 *
 */
public class YailListTest extends TestCase {

  public void testEmptyList() {
    YailList yailList = new YailList();
    assertEquals(0, yailList.size());
    assertEquals(0, yailList.toArray().length);
    assertEquals(0, yailList.toStringArray().length);
    try {
      yailList.getString(0);
      fail();
    } catch (IndexOutOfBoundsException e) {
      // this is the intended behavior
    }
    try {
      yailList.getString(1);
      fail();
    } catch (IndexOutOfBoundsException e) {
      // this is the intended behavior
    }
  }

  public void testToString() {
    Object[] object = {"Houston", "we", "have", "a", "problem"};
    YailList yailList = YailList.makeList(object);
    assertEquals("(Houston we have a problem)", yailList.toString());
    ArrayList<String> testList = new ArrayList<String>();
    testList.add("one");
    testList.add("two");
    testList.add("three");
    ArrayList<Object> testList2 = new ArrayList<Object>();
    testList2.add("4");
    testList2.add(testList);
    testList2.add("6");
    yailList = YailList.makeList(testList2);
    assertEquals("(4 [one, two, three] 6)", yailList.toString());
  }

  public void testToStringArray() {
    Object[] object = {"Houston", "we", "have", "a", "problem"};
    YailList yailList = YailList.makeList(object);
    String[] listArray = yailList.toStringArray();
    for (int i = 0; i < object.length; i++) {
      assertEquals(object[i].toString(), listArray[i]);
    }
  }

  public void testEmptyJsonStringOutput() {
    YailList yailList = new YailList();
    assertEquals("[]", yailList.toJSONString());
  }

  public void testJsonStringOutput() {
    Object[] object = {"Houston", "we", "have", "a", "problem"};
    YailList yailList = YailList.makeList(object);
    assertEquals("[\"Houston\",\"we\",\"have\",\"a\",\"problem\"]", yailList.toJSONString());
  }

  public void testJsonStringOutputOfFString() {
    Object[] object = {new FString("Houston"), new FString("we"), new FString("have"),
        new FString("a"), new FString("problem")};
    YailList yailList = YailList.makeList(object);
    assertEquals("[\"Houston\",\"we\",\"have\",\"a\",\"problem\"]", yailList.toJSONString());
  }

  public void testJsonStringOutputOfNumber() {
    Object[] object = {new Integer(8), 9, 8.5};
    YailList yailList = YailList.makeList(object);
    assertEquals("[8,9,8.5]", yailList.toJSONString());
  }

  public void testJsonStringOutputOfHeterogenousItems() {
    Object[] firstList = {"Houston", "we", "have", "a", "problem"};
    Object[] secondList = {"China", "we", "have", "an", "ultimatum"};
    Object[] mixedList = {firstList, secondList, "Hello.", new FString("O, hi."), 9};
    YailList yailList = YailList.makeList(mixedList);
    String correctOutput =
      "[" +
      "[\"Houston\",\"we\",\"have\",\"a\",\"problem\"]," +
      "[\"China\",\"we\",\"have\",\"an\",\"ultimatum\"]," +
      "\"Hello.\"," +
      "\"O, hi.\"," +
      "9" +
      "]";
    assertEquals(correctOutput, yailList.toJSONString());
  }

  public void testJsonStringOutputOfDeepNestedList() {
    Object [] firstList = {"a"};
    Object [] secondList = {firstList, "b"};
    Object [] thirdList = {secondList, "c"};
    Object [] fourthList = {thirdList, "d"};
    YailList yailList = YailList.makeList(fourthList);
    String correctOutput = "[[[[\"a\"],\"b\"],\"c\"],\"d\"]";
    assertEquals(correctOutput, yailList.toJSONString());
  }

  public void testJsonStringOutputOfNestedYailList() {
    Object [] firstList = {"a", "b"};
    Object [] secondList = {"a", "b"};
    YailList yailListOne = YailList.makeList(firstList);
    Object [] thirdList = {yailListOne, secondList};
    YailList yailListTwo = YailList.makeList(thirdList);
    String correctOutput = "[[\"a\",\"b\"],[\"a\",\"b\"]]";
    assertEquals(correctOutput, yailListTwo.toJSONString());
  }

  public void testCreationFromJavaList() {
    ArrayList<String> testList = new ArrayList<String>();
    testList.add("tom");
    testList.add("dick");
    testList.add("harry");
    YailList yailList = YailList.makeList(testList);
    assertEquals(3, yailList.size());
    Object[] objects = yailList.toArray();
    String[] strings = yailList.toStringArray();
    assertEquals(3, objects.length);
    assertEquals(3, strings.length);
    for (int i = 0; i < objects.length; i++) {
      assertEquals(testList.get(i), String.valueOf(objects[i]));
    }
    for (int i = 0; i < strings.length; i++) {
      assertEquals(testList.get(i), strings[i]);
    }
    for (int i = 0; i < testList.size(); i++) {
      assertEquals(testList.get(i), yailList.getString(i));
    }
    try {
      yailList.getString(3);
      fail();
    } catch (IndexOutOfBoundsException e) {
      // this is the intended behavior
    }
  }

  public void testCreationFromJavaCollection() {
    HashSet<String> testSet = new HashSet<String>();
    testSet.add("blind mouse #1");
    testSet.add("blind mouse #2");
    testSet.add("blind mouse #3");
    YailList yailList = YailList.makeList(testSet);
    assertEquals(3, yailList.size());
    Object[] objects = yailList.toArray();
    String[] strings = yailList.toStringArray();
    assertEquals(3, objects.length);
    assertEquals(3, strings.length);
    for (int i = 0; i < objects.length; i++) {
      assertTrue(testSet.contains(String.valueOf(objects[i])));
    }
    for (int i = 0; i < strings.length; i++) {
      assertTrue(testSet.contains(strings[i]));
    }
    for (int i = 0; i < testSet.size(); i++) {
      assertTrue(testSet.contains(yailList.getString(i)));
    }
    try {
      yailList.getString(3);
      fail();
    } catch (IndexOutOfBoundsException e) {
      // this is the intended behavior
    }
  }

  public void testCreationFromArray() {
    String[] testArray = {"Mahmoud Ahmadinejad", "Alvin Stardust", "The Hamburglar"};
    YailList yailList = YailList.makeList(testArray);
    assertEquals(3, yailList.size());
    Object[] objects = yailList.toArray();
    String[] strings = yailList.toStringArray();
    assertEquals(3, objects.length);
    assertEquals(3, strings.length);
    for (int i = 0; i < objects.length; i++) {
      assertEquals(testArray[i], String.valueOf(objects[i]));
    }
    for (int i = 0; i < strings.length; i++) {
      assertEquals(testArray[i], strings[i]);
    }
    for (int i = 0; i < testArray.length; i++) {
      assertEquals(testArray[i], yailList.getString(i));
    }
    try {
      yailList.getString(3);
      fail();
    } catch (IndexOutOfBoundsException e) {
      // this is the intended behavior
    }
  }
}
