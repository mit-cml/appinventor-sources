// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.errors.DispatchableError;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import gnu.lists.FString;
import gnu.lists.LList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;

/**
 * The YailList is a wrapper around the gnu.list.Pair class used
 * by the Kawa framework. YailList is the main list primitive used
 * by App Inventor components.
 *
 * @author data1013@mit.edu (Danny Tang)
 */
public class YailDictionary extends LinkedHashMap<Object, Object>
    implements YailObject<YailList> {

  private static final String LOG_TAG = "YailDictionary";
  public static final Object ALL = new Object() {
    @Override
    public String toString() {
      return "ALL_ITEMS";
    }
  };

  public interface KeyTransformer {
    Object transform(Object key);
  }

  private static final KeyTransformer IDENTITY = new KeyTransformer() {
    @Override
    public Object transform(Object key) {
      return key;
    }
  };

  private final KeyTransformer keyTransformer;

  /**
   * Create an empty YailDictionary.
   */
  public YailDictionary() {
    super();
    keyTransformer = IDENTITY;
  }

  @SuppressWarnings("UseBulkOperation")  // Use of put handles type casting
  public YailDictionary(Map<?, ?> prevMap) {
    this(prevMap, IDENTITY);
  }

  public YailDictionary(Map<?, ?> prevMap, KeyTransformer keyTransformer) {
    this.keyTransformer = keyTransformer;
    for (Map.Entry<?, ?> entry : prevMap.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Create an empty YailDictionary.
   */
  @SuppressWarnings("WeakerAccess")  // Called from runtime.scm
  public static YailDictionary makeDictionary() {
    return new YailDictionary();
  }

  /**
   * Create a YailDictionary containing the contents of the given Map. The
   * caller is responsible for ensuring that the contents of the map are
   * coercible to types in YAIL.
   *
   * @param prevMap The existing collection to use in the YailDictionary.
   * @return A new YailDictionary that is a shallow copy of {@code prevMap}.
   */
  @SuppressWarnings("WeakerAccess")  // Called from runtime.scm
  public static YailDictionary makeDictionary(Map<Object, Object> prevMap) {
    return new YailDictionary(prevMap);
  }

  /**
   * Create a YailDictionary from the given list of keys and values. The list
   * must have an even number of entries with the key first and value second
   * for each pair of values.
   *
   * <p>For example:
   *
   * <code>
   *   YailDictionary.makeDictionary("foo", 1, "bar", 2);
   * </code>
   *
   * <p>will yield a YailDictionary object equivalent to the JSON:
   *
   * <code>
   *   {
   *     "foo": 1,
   *     "bar": 2
   *   }
   * </code>
   *
   * @param keysAndValues The list of key-value pairs to be added to the
   *     YailDictionary.
   * @return A newly constructed YailDictionary object.
   */
  @SuppressWarnings("WeakerAccess")  // Called from runtime.scm
  public static YailDictionary makeDictionary(Object... keysAndValues) {
    if (keysAndValues.length % 2 == 1) {
      throw new IllegalArgumentException("Expected an even number of key-value entries.");
    }
    YailDictionary dict = new YailDictionary();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      dict.put(keysAndValues[i], keysAndValues[i + 1]);
    }
    return dict;
  }

  /**
   * Construct a new dictionary from a list of pairs stored in {@code YailList}s.
   *
   * @param pairs The list of pairs used to populate the dictionary.
   * @return A newly constructed dictionary from the pairs.
   * @throws IndexOutOfBoundsException if any of the lists isn't a pair.
   */
  @SuppressWarnings({"unused", "WeakerAccess"})  // Called from runtime.scm
  public static YailDictionary makeDictionary(List<YailList> pairs) {
    Map<Object, Object> map = new LinkedHashMap<>();

    for (YailList currentYailList : pairs) {
      Object currentKey = currentYailList.getObject(0);
      Object currentValue = currentYailList.getObject(1);
      map.put(currentKey, currentValue);
    }

    return new YailDictionary(map);
  }

  private static Boolean isAlist(YailList yailList) {
    boolean hadPair = false;

    for (Object currentPair : ((LList) yailList.getCdr())) {
      if (!(currentPair instanceof YailList)) {
        return false;
      }

      if (((YailList) currentPair).size() != 2) {
        return false;
      }

      hadPair = true;
    }

    return hadPair;
  }

  @SuppressWarnings("WeakerAccess")  // Called from runtime.scm
  public static YailDictionary alistToDict(YailList alist) {
    YailDictionary map = new YailDictionary();

    for (Object o : ((LList) alist.getCdr())) {
      YailList currentPair = (YailList) o;

      Object currentKey = currentPair.getObject(0);
      Object currentValue = currentPair.getObject(1);

      if (currentValue instanceof YailList && isAlist((YailList) currentValue)) {
        map.put(currentKey, alistToDict((YailList) currentValue));
      } else {
        if (currentValue instanceof YailList) {
          map.put(currentKey, checkList((YailList) currentValue));
        } else {
          map.put(currentKey, currentValue);
        }
      }
    }

    return map;
  }

  private static YailList checkList(YailList list) {
    Object[] checked = new Object[list.size()];
    int i = 0;
    Iterator<?> it = list.iterator();
    it.next();  // skip *list* symbol
    boolean processed = false;
    while (it.hasNext()) {
      Object o = it.next();
      if (o instanceof YailList) {
        if (isAlist((YailList) o)) {
          checked[i] = alistToDict((YailList) o);
          processed = true;
        } else {
          checked[i] = checkList((YailList) o);
          if (checked[i] != o) {  // identity is used to determine whether the list contained an alist
            processed = true;
          }
        }
      } else {
        checked[i] = o;
      }
      i++;
    }
    if (processed) {
      return YailList.makeList(checked);
    } else {
      return list;  // nothing has changed
    }
  }

  private static YailList checkListForDicts(YailList list) {
    List<Object> copy = new ArrayList<>();
    for (Object o : (LList) list.getCdr()) {
      if (o instanceof YailDictionary) {
        copy.add(dictToAlist((YailDictionary) o));
      } else if (o instanceof YailList) {
        copy.add(checkListForDicts((YailList) o));
      } else {
        copy.add(o);
      }
    }
    return YailList.makeList(copy);
  }

  @SuppressWarnings({"unused", "WeakerAccess"})  // Called from runtime.scm
  public static YailList dictToAlist(YailDictionary dict) {
    List<Object> list = new ArrayList<>();
    for (Map.Entry<Object, Object> entry : dict.entrySet()) {
      list.add(YailList.makeList(new Object[] {entry.getKey(), entry.getValue()}));
    }
    return YailList.makeList(list);
  }

  public void setPair(YailList pair) {
    this.put(pair.getObject(0), pair.getObject(1));
  }

  private Object getFromList(List<?> target, Object currentKey) {
    int offset = target instanceof YailList ? 0 : 1;
    try {
      if (currentKey instanceof FString) {
        return target.get(Integer.parseInt(currentKey.toString()) - offset);
      } else if (currentKey instanceof String) {
        return target.get(Integer.parseInt((String) currentKey) - offset);
      } else if (currentKey instanceof Number) {
        return target.get(((Number) currentKey).intValue() - offset);
      }
    } catch (NumberFormatException e) {
      Log.w(LOG_TAG, "Unable to parse key as integer: " + currentKey, e);
      throw new YailRuntimeError("Unable to parse key as integer: " + currentKey,
          "NumberParseException");
    } catch (IndexOutOfBoundsException e) {
      Log.w(LOG_TAG, "Requested too large of an index: " + currentKey, e);
      throw new YailRuntimeError("Requested too large of an index: " + currentKey,
          "IndexOutOfBoundsException");
    }
    return null;
  }

  @SuppressWarnings("WeakerAccess")  // Called from runtime.scm
  public Object getObjectAtKeyPath(List<?> keysOrIndices) {
    Object target = this;

    for (Object currentKey : keysOrIndices) {
      if (target instanceof Map) {
        target = ((Map<?, ?>) target).get(currentKey);
      } else if (target instanceof YailList && isAlist((YailList) target)) {
        target = alistToDict((YailList) target).get(currentKey);
      } else if (target instanceof List) {
        target = getFromList((List<?>) target, currentKey);
      } else {
        return null;
      }
    }

    return target;
  }

  private static Collection<Object> allOf(Map<Object, Object> map) {
    return map.values();
  }

  @SuppressWarnings("unchecked")  // Kawa is compiled without generics for Java 5
  private static Collection<Object> allOf(List<Object> list) {
    if (list instanceof YailList) {
      if (isAlist((YailList) list)) {
        ArrayList<Object> result = new ArrayList<>();
        for (Object o : (LList) ((YailList) list).getCdr()) {
          result.add(((YailList) o).getObject(1));
        }
        return result;
      } else {
        return (Collection<Object>) ((YailList) list).getCdr();
      }
    }
    return list;
  }

  @SuppressWarnings("unchecked")  // everything extends Object
  private static Collection<Object> allOf(Object object) {
    if (object instanceof Map) {
      return allOf((Map<Object, Object>) object);
    } else if (object instanceof List) {
      return allOf((List<Object>) object);
    } else {
      return Collections.emptyList();
    }
  }

  private static Object alistLookup(YailList alist, Object target) {
    for (Object o : (LList) alist.getCdr()) {
      if (o instanceof YailList) {
        Object key = ((YailList) o).getObject(0);
        if (key.equals(target)) {
          return ((YailList) o).getObject(1);
        }
      } else {
        return null;
      }
    }
    return null;
  }

  private static <T> List<Object> walkKeyPath(Object root, List<T> keysOrIndices,
      List<Object> result) {
    if (keysOrIndices.isEmpty()) {
      if (root != null) {
        result.add(root);
      }
      return result;
    } else if (root == null) {
      return result;
    }

    Object currentKey = keysOrIndices.get(0);
    List<T> childKeys = keysOrIndices.subList(1, keysOrIndices.size());
    if (currentKey == ALL) {
      for (Object child : allOf(root)) {
        walkKeyPath(child, childKeys, result);
      }
    } else if (root instanceof Map) {
      walkKeyPath(((Map<?, ?>) root).get(currentKey), childKeys, result);
    } else if (root instanceof YailList && isAlist((YailList) root)) {
      Object value = alistLookup((YailList) root, currentKey);
      if (value != null) {
        walkKeyPath(value, childKeys, result);
      }
    } else if (root instanceof List) {
      int index = keyToIndex((List<?>) root, currentKey);
      try {
        walkKeyPath(((List<?>) root).get(index), childKeys, result);
      } catch (Exception e) {
        // Suppressed, as we are walking the tree and other paths might match.
      }
    }
    return result;
  }

  @SuppressWarnings("WeakerAccess")  // called from runtime.scm
  public static <T> List<Object> walkKeyPath(YailObject<?> object, List<T> keysOrIndices) {
    return walkKeyPath(object, keysOrIndices, new ArrayList<>());
  }

  private static int keyToIndex(List<?> target, Object key) {
    int offset = target instanceof YailList ? 0 : 1;
    int index;
    if (key instanceof Number) {
      index = ((Number) key).intValue();
    } else {
      try {
        index = Integer.parseInt(key.toString());
      } catch (NumberFormatException e) {
        throw new DispatchableError(ErrorMessages.ERROR_NUMBER_FORMAT_EXCEPTION,
            key.toString());
      }
    }
    index -= offset;
    if (index < 0 || index >= target.size() + 1 - offset) {
      try {
        throw new DispatchableError(ErrorMessages.ERROR_INDEX_MISSING_IN_LIST,
            index + offset, JsonUtil.getJsonRepresentation(target));
      } catch (JSONException e) {
        // We just parsed this...
        Log.e(LOG_TAG, "Unable to serialize object as JSON", e);
        throw new YailRuntimeError(e.getMessage(), "JSON Error");
      }
    }
    return index;
  }

  private Object lookupTargetForKey(Object target, Object key) {
    if (target instanceof YailDictionary) {
      return ((YailDictionary) target).get(key);
    } else if (target instanceof List) {
      return ((List<?>) target).get(keyToIndex((List<?>) target, key));
    } else {
      throw new DispatchableError(ErrorMessages.ERROR_INVALID_VALUE_IN_PATH,
          target == null ? "null" : target.getClass().getSimpleName());
    }
  }

  /**
   * Sets the value for the given key path.
   *
   * Key traversal occurs for both dictionaries and arrays. Consider the
   * following JSON data structure:
   *
   * <pre><code>
   *   {
   *     "data": [
   *       {
   *         "id": 1,
   *         "name": "Foo",
   *         "address": {
   *           "number": 77,
   *           "street": "Massachusetts Avenue"
   *         }
   *       }, {
   *         "id": 2,
   *         "name": "Bar"
   *         "address": {
   *           "number": 32,
   *           "street": "Vassar Street"
   *         }
   *       }
   *     ]
   *   }
   * </code></pre>
   *
   * Calling <code>setValueForKeyPath</code> with the path ["data",  1,  "name"]
   * and the value "Baz" will update name of the first value from "Foo" to
   * "Baz". This method will work so long as the path represented by the n-1
   * elements in the path exist in the data structure. Following the previous
   * example, if one used the path ["data", 1, "course"] with the value 6, the
   * first data element would have the course field added with the value 6:
   *
   * <pre><code>
   *   {
   *     "id": 1,
   *     "name": "Baz",
   *     "address": {
   *       "number": 77,
   *       "street": "Massachusetts Avenue"
   *     },
   *     "course": 6
   *   }
   * </code></pre>
   *
   * If the last item in the path is a list and the given path index is a
   * number, the item at the given index in the list will be replaced.
   *
   * There are X error conditions that can occur when performing this operation.
   * If an error occurs, it is thrown as a {@link DispatchableError}, which
   * will result in the Screen's ErrorOccurred event to run.
   *
   * 1. The item at a given point in the path is not a list or a dictionary
   * 2. The key in the path is mismatched to the data type at that location
   *    in the data structure. For example, a string is given as the key but the
   *    data structure at that point in the traversal is a list.
   * 3. The data structure at a given point in the traversal is a list and the
   *    key is a number, but the value is out of bounds for the list.
   *
   * @param keys a list of keys to traverse in the dictionary
   * @param value the value to set at the end of the key path if the first n-1
   *              keys map to a valid object.
   */
  @SuppressWarnings("WeakerAccess")   // Called from runtime.scm
  public void setValueForKeyPath(List<?> keys, Object value) {
    Object target = this;
    Iterator<?> it = keys.iterator();

    // Updating with an empty key path is a no-op as there isn't a path to update.
    if (keys.isEmpty()) {
      return;
    }

    while (it.hasNext()) {
      Object key = it.next();
      if (it.hasNext()) {
        // More keys to go
        target = lookupTargetForKey(target, key);
      } else {
        if (target instanceof YailDictionary) {
          ((YailDictionary) target).put(key, value);
        } else if (target instanceof YailList) {
          LList l = (LList) target;
          l.getIterator(keyToIndex((List<?>) target, key)).set(value);
        } else if (target instanceof List) {
          //noinspection unchecked
          ((List) target).set(keyToIndex((List<?>) target, key), value);
        } else {
          throw new DispatchableError(ErrorMessages.ERROR_INVALID_VALUE_IN_PATH);
        }
      }
    }
  }

  @Override
  public boolean containsKey(Object key) {
    if (key instanceof FString) {
      key = key.toString();
    }
    return super.containsKey(keyTransformer.transform(key));
  }

  @Override
  public boolean containsValue(Object value) {
    if (value instanceof FString) {
      return super.containsValue(value.toString());
    }
    return super.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    if (key instanceof FString) {
      key = key.toString();
    }
    key = keyTransformer.transform(key);
    return super.get(key);
  }

  @Override
  public Object put(Object key, Object value) {
    if (key instanceof FString) {
      key = key.toString();
    }
    key = keyTransformer.transform(key);
    if (value instanceof FString) {
      value = value.toString();
    }
    return super.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    if (key instanceof FString) {
      key = key.toString();
    }
    key = keyTransformer.transform(key);
    return super.remove(key);
  }

  @Override
  public String toString() {
    try {
      return JsonUtil.getJsonRepresentation(this);
    } catch (JSONException e) {
      throw new YailRuntimeError(e.getMessage(), "JSON Error");
    }
  }

  @Override
  public Object getObject(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException();
    }
    int i = index;
    for (Map.Entry<Object, Object> e : entrySet()) {
      if (i == 0) {
        return Lists.newArrayList(e.getKey(), e.getValue());
      }
      i--;
    }
    // We shouldn't get here as the check at the beginning of the function
    // should have already covered this (and we aren't concurrently editable).
    throw new IndexOutOfBoundsException();
  }

  @NonNull
  @Override
  public Iterator<YailList> iterator() {
    return new DictIterator(entrySet().iterator());
  }

  private static class DictIterator implements Iterator<YailList> {

    final Iterator<Map.Entry<Object, Object>> it;

    DictIterator(Iterator<Map.Entry<Object, Object>> it) {
      this.it = it;
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public YailList next() {
      Map.Entry<Object, Object> e = it.next();
      return YailList.makeList(new Object[] { e.getKey(), e.getValue() });
    }

    @Override
    public void remove() {
      it.remove();
    }
  }
}
