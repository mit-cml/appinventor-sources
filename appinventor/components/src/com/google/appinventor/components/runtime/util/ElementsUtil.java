// -*- Mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;

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

  public static YailList elements(YailList itemList, String componentName){
    Object[] objects = itemList.toStringArray();
    for (int i = 0; i < objects.length; i++) {
      if (!(objects[i] instanceof String)) {
        throw new YailRuntimeError("Items passed to " + componentName + " must be Strings",
            "Error");
      }
    }
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
    if (index == 0)
      return "";
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

}
