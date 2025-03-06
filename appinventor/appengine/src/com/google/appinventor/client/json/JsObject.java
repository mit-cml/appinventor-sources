// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

package com.google.appinventor.client.json;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class JsObject<E> extends JavaScriptObject implements Map<String, E> {

  protected JsObject() {}

  public static native <E> JsObject<E> cast(JavaScriptObject obj)/*-{
    return obj instanceof Object && !(obj instanceof Array) ? obj : null;
  }-*/;

  public static native <E> JsObject<E> create()/*-{
    return {};
  }-*/;

  public static native <E> JsArray<String> keys(JsObject<E> object)/*-{
    return Object.keys(object);
  }-*/;

  public static native <E> JsArray<Object> values(JsObject<E> object)/*-{
    return Object.values(object);
  }-*/;

  public static native <E> JsArray<JsArray<Object>> entries(JsObject<E> object)/*-{
    return Object.entries(object);
  }-*/;

  @Override
  public final int size() {
    return keys(this).size();
  }

  @Override
  public final native boolean isEmpty()/*-{
    return Object.keys(this).length === 0;
  }-*/;

  @Override
  public final boolean containsKey(Object key) {
    if (key instanceof String) {
      return JsonUtil.containsKey(this, (String) key);
    }
    return false;
  }

  @Override
  public final boolean containsValue(Object value) {
    return false;
  }

  @Override
  public final native E get(Object key)/*-{
    return this[key];
  }-*/;

  @Override
  public final native E put(String key, E value)/*-{
    var old = this[key];
    this[key] = value;
    return old;
  }-*/;

  @Override
  public final native E remove(Object key)/*-{
    var old = this[key];
    delete this[key];
    return old;
  }-*/;

  @SuppressWarnings("unchecked")
  @Override
  public final void putAll(@Nonnull Map<? extends String, ? extends E> m) {
    for (Map.Entry<?, ?> entry : m.entrySet()) {
      put((String) entry.getKey(), (E) entry.getValue());
    }
  }

  @Override
  public final void clear() {

  }

  @Override
  @Nonnull
  public final Set<String> keySet() {
    final JsArray<String> keys = keys(this);
    return new Set<String>() {
      @Override
      public int size() {
        return keys.size();
      }

      @Override
      public boolean isEmpty() {
        return keys.size() == 0;
      }

      @Override
      public boolean contains(Object o) {
        return keys.contains(o);
      }

      @Override
      public Iterator<String> iterator() {
        return keys.iterator();
      }

      @Override
      public Object[] toArray() {
        return new Object[0];
      }

      @Override
      public <T> T[] toArray(T[] a) {
        return null;
      }

      @Override
      public boolean add(String s) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
        return false;
      }

      @Override
      public boolean containsAll(@Nonnull Collection<?> c) {
        return false;
      }

      @Override
      public boolean addAll(@Nonnull Collection<? extends String> c) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(@Nonnull Collection<?> c) {
        return false;
      }

      @Override
      public boolean removeAll(@Nonnull Collection<?> c) {
        return false;
      }

      @Override
      public void clear() {

      }
    };
  }

  @Override
  @Nonnull
  public final native Collection<E> values()/*-{
    return Object.values(this);
  }-*/;

  @Override
  @Nonnull
  public final Set<Entry<String, E>> entrySet() {
    return null;
  }

  public final native String stringify()/*-{
    return JSON.stringify(this);
  }-*/;
}
