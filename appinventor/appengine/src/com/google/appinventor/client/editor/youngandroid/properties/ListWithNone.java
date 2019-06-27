// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * List where item 0 represents the empty string, but it is displayed as
 * "None" (configurable). Item 0 is always present.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class ListWithNone {
  interface ListBoxWrapper {
    void addItem(String item);
    String getItem(int index);
    void removeItem(int index);
    void setSelectedIndex(int index);
  }

  private final ListBoxWrapper listBoxWrapper;
  private final String noneDisplayItem;
  private final List<String> values;

  /**
   * Creates a ListWithNone instance with the given "None" display item.
   * After construction, the list contains just the "None" item.
   *
   * @param noneDisplayItem the display item for "None"
   * @param listBoxWrapper a wrapper around the list box
   */
  ListWithNone(String noneDisplayItem, ListBoxWrapper listBoxWrapper) {
    this.noneDisplayItem = noneDisplayItem;
    this.listBoxWrapper = listBoxWrapper;
    values = new ArrayList<String>();

    values.add("");
    listBoxWrapper.addItem(noneDisplayItem);
  }

  /**
   * Adds a value to the list. The display item will be the same as the value.
   *
   * @param value the value to add to the list
   */
  void addItem(String value) {
    addItem(value, value);
  }

  /**
   * Adds a value and its corresponding display item to the list.
   *
   * @param value the value to add to the list
   * @param displayItem the item to be displayed
   */
  void addItem(String value, String displayItem) {
    values.add(value);
    listBoxWrapper.addItem(displayItem);
  }

  /**
   * Selects the item in the list that corresponds to the given value.
   *
   * @param value the value to select.
   */
  void selectValue(String value) {
    int index = values.indexOf(value);
    if (index != -1) {
      listBoxWrapper.setSelectedIndex(index);
    }
  }

  /**
   * Removes a value and its corresponding display item from the list
   *
   * @param value the value to remove from the list
   */
  void removeValue(String value) {
    int index = values.indexOf(value);
    if (index != -1) {
      values.remove(index);
      listBoxWrapper.removeItem(index);
    }
  }

  /**
   * Returns the value at the given index.
   *
   * @param index the index
   * @return the value at the given index
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  String getValueAtIndex(int index) throws IndexOutOfBoundsException {
    if (index >= 0 && index < values.size()) {
      return values.get(index);
    }
    throw new IndexOutOfBoundsException("" + index);
  }

  /**
   * Returns the index of the given value or -1 if the value isn't in the list.
   *
   * @param value the value
   * @return the index of the given value or -1 if the value isn't in the list
   */
  int indexOfValue(String value) {
    return values.indexOf(value);
  }

  /**
   * Returns true if the given value is in the list
   *
   * @param value the value
   * @return true if the given value is in the list, false otherwise
   */
  boolean containsValue(String value) {
    return values.contains(value);
  }

  /**
   * Returns the display item for the given value.
   *
   * @param value the value
   * @return the display item for the given value
   * @throws IllegalArgumentException if the given value is not in the list
   */
  String getDisplayItemForValue(String value) {
    int index = values.indexOf(value);
    if (index != -1) {
      return listBoxWrapper.getItem(index);
    }
    throw new IllegalArgumentException("Illegal value: " + value);
  }
}
