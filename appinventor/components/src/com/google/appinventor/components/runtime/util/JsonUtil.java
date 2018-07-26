// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Environment;

import android.util.Base64;
import android.util.Log;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import gnu.lists.FString;

import gnu.math.IntFraction;

import java.io.File;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Provides utility functions to convert between Java object and JSON.
 *
 *
 */
public class JsonUtil {

  private static final String BINFILE_DIR = "/AppInventorBinaries";
  private static final String LOG_TAG = "JsonUtil";

  /**
   * Prevent instantiation.
   */
  private JsonUtil() {
  }

  /**
   * Returns a list of String objects from a JSONArray. This
   * does not do any kind of recursive unpacking of the array.
   * Thus, if the array includes other JSON arrays or JSON objects
   * their string representation will be a single item in the
   * returned list.
   *
   * @param jArray The JSONArray to convert.
   * @return A List of the String representation of each item in
   * the JSON array.
   * @throws JSONException if an element of jArray cannot be
   * converted to a String.
   */
  public static List<String> getStringListFromJsonArray(JSONArray jArray) throws JSONException {
    List<String> returnList = new ArrayList<String>();
    for (int i = 0; i < jArray.length(); i++) {
      String val = jArray.getString(i);
      returnList.add(val);
    }
    return returnList;
  }

  /**
   * Returns a Java Object list of a JSONArray with each item in
   * the array converted using convertJsonItem().
   *
   * @param jArray The JSONArray to convert.
   * @return A List of Strings and more Object lists.
   * @throws JSONException if an element in jArray cannot be
   * converted properly.
   */
  public static List<Object> getListFromJsonArray(JSONArray jArray) throws JSONException {
    List<Object> returnList = new ArrayList<Object>();
    for (int i = 0; i < jArray.length(); i++) {
      returnList.add(convertJsonItem(jArray.get(i)));
    }
    return returnList;
  }

  /**
   * Returns a list containing one two item list per key in jObject.
   * Each two item list has the key String as its first element and
   * the result of calling convertJsonItem() on its value as the
   * second element. The sub-lists in the returned list will appear
   * in alphabetical order by key.
   *
   * @param jObject The JSONObject to convert.
   * @return A list of two item lists: [String key, Object value].
   * @throws JSONException if an element in jObject cannot be
   * converted properly.
   */
  // TODO(hal): If we implement dictionaries, we'll need to decode Json
  // objects to dictionaires instead.
  public static List<Object> getListFromJsonObject(JSONObject jObject) throws JSONException {
    List<Object> returnList = new ArrayList<Object>();
    Iterator<String> keys = jObject.keys();

    List<String> keysList = new ArrayList<String>();
    while (keys.hasNext()) {
      keysList.add(keys.next());
    }
    Collections.sort(keysList);

    for (String key : keysList) {
      List<Object> nestedList = new ArrayList<Object>();
      nestedList.add(key);
      nestedList.add(convertJsonItem(jObject.get(key)));
      returnList.add(nestedList);
    }

    return returnList;
  }

  /**
   * Returns a Java object representation of objects that are
   * encountered inside of JSON created using the org.json package.
   * JSON arrays and objects are transformed into their list
   * representations using getListFromJsonArray and
   * getListFromJsonObject respectively.
   *
   * Java Boolean values and the Strings "true" and "false" (case
   * insensitive) are inserted as Booleans. Java Numbers are
   * inserted without modification and all other values are inserted
   * as their toString(). value.
   *
   * @param o An item in a JSON array or JSON object to convert.
   * @return A Java Object representing o or the String "null"
   * if o is null.
   * @throws JSONException if o fails to parse.
   */
  public static Object convertJsonItem(Object o) throws JSONException {
    if (o == null) {
      return "null";
    }

    if (o instanceof JSONObject) {
      return getListFromJsonObject((JSONObject) o);
    }

    if (o instanceof JSONArray) {
      return getListFromJsonArray((JSONArray) o);
    }

    if (o.equals(Boolean.FALSE) || (o instanceof String &&
        ((String) o).equalsIgnoreCase("false"))) {
      return false;
    }

    if (o.equals(Boolean.TRUE) || (o instanceof String && ((String) o).equalsIgnoreCase("true"))) {
      return true;
    }

    if (o instanceof Number) {
      return o;
    }

    return o.toString();
  }

  public static String getJsonRepresentation(Object value) throws JSONException {
    if (value == null || value.equals(null)) {
      return "null";
    }
    if (value instanceof FString) {
      return JSONObject.quote(value.toString());
    }
    if (value instanceof YailList) {
      return ((YailList) value).toJSONString();
    }
    // The Json tokener used in getObjectFromJson cannot handle
    // fractions.  So we Json encode fractions by first converting
    // them to doubles. This is an example of value with Kawa type any
    // being exposed to the rest of App Inventor by the value being
    // passed to a component method, in this case TinyDB or TinyWebDB
    // StoreValue.  See the "warning" comment in runtime.scm at
    // call-component-method.
    if (value instanceof IntFraction) {
      return JSONObject.numberToString((Number) ((IntFraction)value).doubleValue());
    }
    if (value instanceof Number) {
      return JSONObject.numberToString((Number) value);
    }
    if (value instanceof Boolean) {
      return value.toString();
    }
    if (value instanceof List) {
      value = ((List)value).toArray();
    }
    if (value.getClass().isArray()) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      String separator = "";
      for (Object o: (Object[]) value) {
        sb.append(separator).append(getJsonRepresentation(o));
        separator = ",";
      }
      sb.append("]");
      return sb.toString();
    }
    return JSONObject.quote(value.toString());
  }

  public static Object getObjectFromJson(String jsonString) throws JSONException {
      if ((jsonString == null) || jsonString.equals("")) {
      // We'd like the empty string to decode to the empty string.  Form.java
      // relies on this for the case where there's an activity result with no intent data.
      // We handle this case explicitly since nextValue() appears to throw an error
      // when given the empty string.
      return "";
    } else {
      final Object value = (new JSONTokener(jsonString)).nextValue();
      // Note that the JSONTokener may return a value equals() to null.
      if (value == null || value.equals(null)) {
        return null;
      } else if ((value instanceof String) ||
          (value instanceof Number) ||
          (value instanceof Boolean)) {
        return value;
      } else if (value instanceof JSONArray) {
        return getListFromJsonArray((JSONArray)value);
      } else if (value instanceof JSONObject) {
        return getListFromJsonObject((JSONObject)value);
      }
      throw new JSONException("Invalid JSON string.");
    }
  }

  /**
   * Written by joymitro@gmail.com (Joydeep Mitra)
   * This method converts a file path to a JSON representation.
   * The code in the method was part of GetValue. For better modularity and reusability
   * the logic is now part of this method, which can be invoked from wherever and
   * whenever required.
   *
   * 07/06/2018 (jis): Currently this routine is called by the CloudDB component
   *                   When called from GetValue, it is handed a String (the raw
   *                   data from Jedis) to parse. When called from the
   *                   CloudDBJedisListener it is handed an input that has
   *                   already been converted into a List (because the
   *                   CloudDBJedisListener is processing a list of changes,
   *                   not just one. Rather then having two versions of
   *                   this function, we just do the initial JSON parsing
   *                   if we are handed a string.
   *
   * @param file path
   * @return JSON representation
   */
  public static String getJsonRepresentationIfValueFileName(Object value){
    try {
      List<String> valueList;
      if (value instanceof String) {
        JSONArray valueJsonList = new JSONArray((String)value);
        valueList = getStringListFromJsonArray(valueJsonList);
      } else if (value instanceof List) {
        valueList = (List<String>) value;
      } else {
        throw new YailRuntimeError("getJsonRepresentationIfValueFileName called on unknown type",
          value.getClass().getName());
      }
      if (valueList.size() == 2) {
        if (valueList.get(0).startsWith(".")) {
          String filename = writeFile(valueList.get(1), valueList.get(0).substring(1));
          System.out.println("Filename Written: " + filename);
          filename = filename.replace("file:/", "file:///");
          return getJsonRepresentation(filename);
        } else {
          return null;
        }
      } else {
        return null;
      }
    } catch(JSONException e) {
      Log.e(LOG_TAG, "JSONException", e);
      return null;
    }
  }

  /**
   * Accepts a base64 encoded string and a file extension (which must be three characters).
   * Decodes the string into a binary and saves it to a file on external storage and returns
   * the filename assigned.
   *
   * Written by Jeff Schiller (jis) for the BinFile Extension
   *
   * @param input Base64 input string
   * @param fileExtension three character file extension
   * @return the name of the created file
   */
  private static String writeFile(String input, String fileExtension) {
    try {
      if (fileExtension.length() != 3 && fileExtension.length() != 4) {
        throw new YailRuntimeError("File Extension must be three or four characters", "Write Error");
      }
      byte [] content = Base64.decode(input, Base64.DEFAULT);
      String fullDirName = Environment.getExternalStorageDirectory() + BINFILE_DIR;
      File destDirectory = new File(fullDirName);
      destDirectory.mkdirs();
      File dest = File.createTempFile("BinFile", "." + fileExtension, destDirectory);
      FileOutputStream outStream = new FileOutputStream(dest);
      outStream.write(content);
      outStream.close();
      String retval = dest.toURI().toASCIIString();
      trimDirectory(20, destDirectory);
      return retval;
    } catch (Exception e) {
      throw new YailRuntimeError(e.getMessage(), "Write");
    }
  }

  // keep only the last N files, where N = maxSavedFiles
  // Written by Jeff Schiller (jis) for the BinFile Extension
  private static void trimDirectory(int maxSavedFiles, File directory) {

    File [] files = directory.listFiles();

    Arrays.sort(files, new Comparator<File>(){
      public int compare(File f1, File f2)
      {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      } });

    int excess = files.length - maxSavedFiles;
    for (int i = 0; i < excess; i++) {
      files[i].delete();
    }
  }
}

