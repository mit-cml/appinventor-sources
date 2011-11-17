// Copyright 2010 Google Inc. All Rights Reserved.
package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.JsonUtil;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;

/**
 * Persistently store YAIL values on the phone using tags to store and retrieve.
 *
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.TINYDB_COMPONENT_VERSION,
    description = "Non-visible component that persistently stores values on the phone.",
    category = ComponentCategory.BASIC,
    nonVisible = true,
    iconName = "images/tinyDB.png")
@SimpleObject
public class TinyDB extends AndroidNonvisibleComponent implements Component, Deleteable {

  private SharedPreferences sharedPreferences;

  /**
   * Creates a new TinyDB component.
   *
   * @param container the Form that this component is contained in.
   */
  public TinyDB(ComponentContainer container) {
    super(container.$form());
    final Context context = (Context) container.$context();
    sharedPreferences = context.getSharedPreferences("TinyDB", Context.MODE_PRIVATE);
  }

  /**
   * Store the given value under the given tag.  The storage persists on the
   * phone when the app is restarted.
   *
   * @param tag The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction
  public void StoreValue(final String tag, final Object valueToStore) {
    final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
    try {
      sharedPrefsEditor.putString(tag, JsonUtil.getJsonRepresentation(valueToStore));
      sharedPrefsEditor.commit();
    } catch (JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }
  }

  /**
   * Retrieve the value stored under the given tag.
   *
   * @param tag The tag to use
   * @return The value stored under the tag. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction
  public Object GetValue(final String tag) {
    try {
      String value = sharedPreferences.getString(tag, "");
      // If there's no entry with tag as a key then return the empty string.
      return (value.length() == 0) ? "" : JsonUtil.getObjectFromJson(value);
    } catch (JSONException e) {
      throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Creation Error.");
    }
  }

  @Override
  public void onDelete() {
    final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
    sharedPrefsEditor.clear();
    sharedPrefsEditor.commit();
  }
}
