// -*- Mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import org.json.JSONObject;

import java.util.List;
import java.util.Arrays;  
import java.util.ArrayList;  

/**
 * Utilities for Components that display a number of options on Screen such as ListPicker,
 * Spinner, and ListView.
 */
public class ElementsUtil {

  public static YailList elementsFromString(String itemString){
    YailList items = new YailList();
    if (itemString.length() > 0) {
      items = YailList.makeList((Object[]) itemString.split(" *, *"));
    }

    return items;
  }

  /** Check a Yail list of items to verify that they are all strings
   *
   * @param itemList
   * @param componentName
   * @return the original list
   */
  public static List<String> elementsStrings(YailList itemList, String componentName){
    Object[] objects = itemList.toStringArray();
    for (int i = 0; i < objects.length; i++) {
      if (!(objects[i] instanceof String)) {
        throw new YailRuntimeError("Items passed to " + componentName + " must be Strings",
            "Error");
      }
    }
    // this is not changing itemlist.  it's just checking that the items are strings
    String[] strings = (String[]) objects;
    List<String> ans = new ArrayList<>(Arrays.asList(strings));
    return ans;
  }

  /**
   * Returns a list of string from a comma-separated string
   * @param itemString
   * @return items
   */
  public static List<String> elementsListFromString(String itemString){
    List<String> items;
    if (itemString.length() > 0) {
      String[] words = itemString.split(" *, *");
      items = new ArrayList<>(Arrays.asList(words));
    } else {
      items = new ArrayList();
    }
    return items;
  }

  /**
   * Converts a List of string to YailList
   * @param stringItems
   * @return YailList
   */
  public static YailList makeYailListFromList(List<String> stringItems) {
    if (stringItems == null || stringItems.size() == 0) return YailList.makeEmptyList();
    return YailList.makeList(stringItems);
  }

  public static int selectionIndexInStringList(int index, List<String> items) {
    if (index < 1 || index > items.size()) {
      return 0;
    } else {
      return index;
    }
  }

  public static String setSelectionFromIndexInStringList(int index, List<String> items) {
    if (index < 1 || index > items.size()) {
      return "";
    }
    return items.get(index - 1);
  }

  public static int setSelectedIndexFromValueInStringList(String value, List<String> items) {
    // Now, we need to change SelectionIndex to correspond to Selection.
    // If multiple Selections have the same SelectionIndex, use the first.
    // If none do, arbitrarily set the SelectionIndex to its default value
    // of 0.
    for (int i = 0; i < items.size(); i++) {
      // The comparison is case-sensitive to be consistent with yail-equal?.
      if (items.get(i).equals(value)) {
        return i + 1;
      }
    }
    return 0;
  }

 /** Check a Yail list of items to verify that they are all strings and
  *
  * @param itemList
  * @param componentName
  * @return the original list
  */

  public static YailList elements(YailList itemList, String componentName){
    Object[] objects = itemList.toStringArray();
    for (int i = 0; i < objects.length; i++) {
      if (!(objects[i] instanceof String)) {
        throw new YailRuntimeError("Items passed to " + componentName + " must be Strings",
            "Error");
      }
    }
    // this is not changing itemlist.  it's just checking that the items are strings
    return itemList;
  }

  public static int selectionIndex(int index, YailList items){
    if (index <= 0 || index > items.size()) {
      return 0;
    } else {
      return index;
    }
  }

  public static String setSelectionFromIndex(int index, YailList items){
    if (index == 0 || index > items.size()) {
      return "";
    }
    // YailLists are 0-based, but we want to be 1-based.
    return items.getString(index - 1);
  }

  public static int setSelectedIndexFromValue(String value, YailList items){
    // Now, we need to change SelectionIndex to correspond to Selection.
    // If multiple Selections have the same SelectionIndex, use the first.
    // If none do, arbitrarily set the SelectionIndex to its default value
    // of 0.
    for (int i = 0; i < items.size(); i++) {
      // The comparison is case-sensitive to be consistent with yail-equal?.
      if (items.getString(i).equals(value)) {
        return i + 1;
      }
    }
    return 0;
  }

  public static String toStringEmptyIfNull(Object o) {
    return o == null ? "" : o.toString();
  }
}
