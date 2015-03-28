// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import gnu.lists.LList;
import gnu.lists.Pair;

import org.json.JSONException;

import java.util.Collection;
import java.util.List;

import android.util.Log;

/**
 * The YailList is a wrapper around the gnu.list.Pair class used
 * by the Kawa framework. YailList is the main list primitive used
 * by App Inventor components.
 *
 */
public class YailList extends Pair {

  private static final String LOG_TAG = "YailList";

  // Component writers take note!
  // If you want to pass back a list to the blocks language, the
  // straightforward way to do this is simply to pass
  // back an ArrayList.  If you construct a YailList to return
  // to codeblocks, you must guarantee that the elements of the list
  // are "sanitized".  That is, you must pass back a tree whose
  // subtrees are themselves YailLists, and whose leaves are all
  // legitimate Yail data types.  See the definition of sanitization
  // in runtime.scm.

  /**
   * Create an empty YailList.
   */
  public YailList() {
    super(YailConstants.YAIL_HEADER, LList.Empty);
  }

  private YailList(Object cdrval) {
    super(YailConstants.YAIL_HEADER, cdrval);
  }

  /**
   * Create an empty YailList YailList from an array.
   */
  public static YailList makeEmptyList() {
    return new YailList();
  }

  /**
   * Create a YailList from an array.
   */
  public static YailList makeList(Object[] objects) {
    LList newCdr = Pair.makeList(objects, 0);
    return new YailList(newCdr);
  }

  /**
   * Create a YailList from a List.
   */
  public static YailList makeList(List vals) {
    LList newCdr = Pair.makeList(vals);
    return new YailList(newCdr);
  }

  /**
   * Create a YailList from a Collection.
   */
  public static YailList makeList(Collection vals) {
    LList newCdr = Pair.makeList(vals.toArray(), 0);
    return new YailList(newCdr);
  }

  /**
   * Return this YailList as an array.
   */
  @Override
  public Object[] toArray() {
    if (cdr instanceof Pair) {
      return ((Pair) cdr).toArray();
    } else if (cdr instanceof LList) {
      return ((LList) cdr).toArray();
    } else {
      throw new YailRuntimeError("YailList cannot be represented as an array", "YailList Error.");
    }
  }

  /**
   * Return this YailList as an array of Strings.
   * In the case of numbers, we convert to strings using
   * YailNumberToString for consistency with the
   * other places where we convert Yail numbers for printing.
   */

  public String[] toStringArray() {
    int size = this.size();
    String[] objects = new String[size];
    for (int i = 1; i <= size; i++) {
      objects[i - 1] = YailListElementToString(get(i));
    }
    return objects;
  }

  /**
   * Convert a YailList element to a string.  This is the same as
   * toString except in the case of numbers, which we convert to strings using
   * YailNumberToString for consistency with the
   * other places where we convert Yail numbers for printing.
   * @param element
   * @return the string
   */
  public static String YailListElementToString(Object element) {
    if (Number.class.isInstance(element)) {
      return YailNumberToString.format(((Number) element).doubleValue());
    } else {
      return String.valueOf(element);
    }
  }

  /**
   * Return a strictly syntactically correct JSON text
   * representation of this YailList. Only supports String, Number,
   * Boolean, YailList, FString and arrays containing these types.
   */
  public String toJSONString() {
    try {
      StringBuilder json = new StringBuilder();
      String separator = "";
      json.append('[');
      int size = this.size();
      for (int i = 1; i <= size; i++) {
        Object value = get(i);
        json.append(separator).append(JsonUtil.getJsonRepresentation(value));
        separator = ",";
      }
      json.append(']');

      return json.toString();

    } catch (JSONException e) {
      throw new YailRuntimeError("List failed to convert to JSON.", "JSON Creation Error.");
    }
  }

  /**
   * Return the size of this YailList.
   */
  @Override
  public int size() {
    return super.size() - 1;
  }

  /**
   * Return a String representation of this YailList.
   */
  @Override
  public String toString() {
    if (cdr instanceof Pair) {
      return ((Pair) cdr).toString();
    } else if (cdr instanceof LList) {
      return ((LList) cdr).toString();
    } else {
      throw new RuntimeException("YailList cannot be represented as a String");
    }
  }

  /**
   * Return the String at the given index.
   */
  public String getString(int index) {
    return get(index + 1).toString();
  }

  /**
   * Return the Object at the given index.
   */
  public Object getObject(int index) {
    return get(index + 1);
  }
}
