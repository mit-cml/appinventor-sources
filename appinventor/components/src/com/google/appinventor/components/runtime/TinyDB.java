// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

/**
 * `TinyDB` is a non-visible component that stores data for an app.
 *
 * Apps created with App Inventor are initialized each time they run. This means that if an app
 * sets the value of a variable and the user then quits the app, the value of that variable will
 * not be remembered the next time the app is run. In contrast, TinyDB is a persistent data store
 * for the app. The data stored in a `TinyDB` will be available each time the app is run. An
 * example might be a game that saves the high score and retrieves it each time the game is played.
 *
 * Data items consist of tags and values. To store a data item, you specify the tag it should be
 * stored under. The tag must be a text block, giving the data a name. Subsequently, you can
 * retrieve the data that was stored under a given tag.
 *
 * You cannot use the `TinyDB` to pass data between two different apps on the phone, although you
 * can use the `TinyDB` to share data between the different screens of a multi-screen app.
 *
 * When you are developing apps using the AI Companion, all the apps using that Companion will
 * share the same `TinyDB`. That sharing will disappear once the apps are packaged and installed on
 * the phone. During development you should be careful to clear the Companion app's data each time
 * you start working on a new app.
 *
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.TINYDB_COMPONENT_VERSION,
    description = "TinyDB is a non-visible component that stores data for an app. "
        + "<p> Apps created with App Inventor are initialized each time they run: "
        + "If an app sets the value of a variable and the user then quits the app, "
        + "the value of that variable will not be remembered the next time the app is run. "
        + "In contrast, TinyDB is a <em> persistent </em> data store for the app, "
        + "that is, the data stored there will be available each time the app is "
        + "run. An example might be a game that saves the high score and "
        + "retrieves it each time the game is played. </<p> "
        + "<p> Data items are strings stored under <em>tags</em> . To store a data "
        + "item, you specify the tag it should be stored under.  Subsequently, you "
        + "can retrieve the data that was stored under a given tag. </p>"
        + "<p> There is only one data store per app. Even if you have multiple TinyDB "
        + "components, they will use the same data store. To get the effect of "
        + "separate stores, use different keys. Also each app has its own data "
        + "store. You cannot use TinyDB to pass data between two different apps on "
        + "the phone, although you <em>can</em> use TinyDb to shares data between the "
        + "different screens of a multi-screen app. </p> "
        + "<p>When you are developing apps using the AI Companion, all the apps "
        + "using that companion will share the same TinyDb.  That sharing will disappear "
        + "once the apps are packaged.  But, during development, you should be careful to clear "
        + "the TinyDb each time you start working on a new app.</p>",
    category = ComponentCategory.STORAGE,
    nonVisible = true,
    iconName = "images/tinyDB.png")

@SimpleObject
public class TinyDB extends AndroidNonvisibleComponent implements Component, Deleteable,
    ObservableDataSource<String, YailList> {

  public static final String DEFAULT_NAMESPACE = "TinyDB1";

  private SharedPreferences sharedPreferences;
  private String namespace;

  private Context context;  // this was a local in constructor and final not private

  // Set of observers
  private HashSet<DataSourceChangeListener> dataSourceObservers = new HashSet<>();

  // SharedPreferences listener used to notify observers
  private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;


  /**
   * Creates a new TinyDB component.
   *
   * @param container the Form that this component is contained in.
   */
  public TinyDB(ComponentContainer container) {
    super(container.$form());
    context = (Context) container.$context();

    sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Upon value change, notify the observers with the key and the value
        notifyDataObservers(key, GetValue(key, null));
      }
    };

    Namespace(DEFAULT_NAMESPACE);
  }

  /**
   * Namespace for storing data. All `TinyDB` components in the same app with the same `Namespace`
   * property access the same data.
   *
   *   Each `Namespace` represents a single data store that is shared by the entire app. If you
   * have multiple `TinyDB` components with the same `Namespace` within an app, they use the same
   * data store, even if they are on different screens. If you only need one data store for your
   * app, it's not necessary to set a `Namespace`.
   *
   * @param namespace the alternate namespace to use for the TinyDB
   */
  @SimpleProperty(description = "Namespace for storing data.", category = PropertyCategory.BEHAVIOR)
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = DEFAULT_NAMESPACE)
  public void Namespace(String namespace) {
    this.namespace = namespace;

    // SharedPreferences previously defined; Unregister the change listener.
    if (sharedPreferences != null) {
      sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    sharedPreferences = context.getSharedPreferences(namespace, Context.MODE_PRIVATE);

    // Register the SharedPreferences change listener
    sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
  }

  @SimpleProperty(description = "Namespace for storing data.")
  public String Namespace() {
    return namespace;
  }

  /**
   * Store the given `valueToStore`{:.variable.block} under the given `tag`{:.text.block}.
   * The storage persists on the phone when the app is restarted.
   *
   * @param tag          The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   *                     number, text, boolean or list).
   */
  @SimpleFunction(description = "Store the given value under the given tag.  The storage persists "
      + "on the phone when the app is restarted.")
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
   * Retrieve the value stored under the given `tag`{:.text.block}.  If there's no such tag, then
   * return `valueIfTagNotThere`{:.variable.block}.
   *
   * @param tag                The tag to use
   * @param valueIfTagNotThere The value returned if tag in not in TinyDB
   * @return The value stored under the tag. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction(description = "Retrieve the value stored under the given tag. If there's no "
      + "such tag, then return valueIfTagNotThere.")
  public Object GetValue(final String tag, final Object valueIfTagNotThere) {
    try {
      String value = sharedPreferences.getString(tag, "");
      // If there's no entry with tag as a key then return the empty string.
      //    was  return (value.length() == 0) ? "" : JsonUtil.getObjectFromJson(value);
      return (value.length() == 0) ? valueIfTagNotThere : JsonUtil.getObjectFromJson(value, true);
    } catch (JSONException e) {
      throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Creation Error.");
    }
  }

   /**
   * Return a list of all the tags in the data store.
   *
   * @return a list of all keys.
   */
  @SimpleFunction(description = "Return a list of all the tags in the data store.")
  public Object GetTags() {
    List<String> keyList = new ArrayList<String>();
    Map<String, ?> keyValues = sharedPreferences.getAll();
    // here is the simple way to get keys
    keyList.addAll(keyValues.keySet());
    java.util.Collections.sort(keyList);
    return keyList;
  }

  /**
   * Clear the entire data store.
   *
   */
  @SimpleFunction(description = "Clear the entire data store.")
  public void ClearAll() {
    final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
    sharedPrefsEditor.clear();
    sharedPrefsEditor.commit();
    notifyDataObservers(null, null); // Notify observers with null value to be interpreted as clear
  }

  /**
   * Clear the entry with the given `tag`{:.text.block}.
   *
   * @param tag The tag to remove.
   */
  @SimpleFunction(description = "Clear the entry with the given tag.")
  public void ClearTag(final String tag) {
    final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
    sharedPrefsEditor.remove(tag);
    sharedPrefsEditor.commit();
  }

  @Override
  public void onDelete() {
    final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
    sharedPrefsEditor.clear();
    sharedPrefsEditor.commit();
    notifyDataObservers(null, null); // Notify observers with null value to be interpreted as clear
  }

  /**
   * Returns the specified List object identified by the key. If the
   * value is not a List object, or it does not exist, an empty List
   * is returned.
   *
   * @param key Key of the value to retrieve
   * @return value as a List object, or empty List if not applicable
   */
  @Override
  public YailList getDataValue(String key) {
    // Get the value from the TinyDB data with the specified key
    Object value = GetValue(key, YailList.makeEmptyList());

    // Check if value is of type List, and return it if that is the case.
    if (value instanceof YailList) {
      return (YailList) value;
    }

    // Default option (could not parse data): return empty YailList
    return YailList.makeEmptyList();
  }

  @Override
  public void addDataObserver(DataSourceChangeListener dataComponent) {
    dataSourceObservers.add(dataComponent);
  }

  @Override
  public void removeDataObserver(DataSourceChangeListener dataComponent) {
    dataSourceObservers.remove(dataComponent);
  }

  @Override
  public void notifyDataObservers(String key, Object newValue) {
    Log.i("Tag", "Notified: " + dataSourceObservers.size() + " observers.");

    // Notify each Chart Data observer component of the Data value change
    for (DataSourceChangeListener dataComponent : dataSourceObservers) {
      dataComponent.onDataSourceValueChange(this, key, newValue);
    }
  }

  /**
   * Method to get all data in form of Dictionary
   * @return Dictionary of tags and their associated values
   */
  @SimpleFunction(description = "Retrieves all data entries of TinyDB in form of Dictionaries")
  public YailDictionary GetEntries() {
    Map<String, ?> keyValues = sharedPreferences.getAll();
    List<String> keyList = new ArrayList<>(keyValues.keySet());

    java.util.Collections.sort(keyList);

    YailDictionary dictionary = new YailDictionary();

    for (String aKey : keyList) {
      dictionary.put(aKey, GetValue(aKey, ""));
    }

    return dictionary;
  }

}
