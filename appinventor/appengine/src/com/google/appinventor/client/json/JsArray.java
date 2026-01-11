// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

package com.google.appinventor.client.json;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;

public class JsArray<E> extends JavaScriptObject implements List<E> {

  protected JsArray() {}

  public static native <E> JsArray<E> cast(JavaScriptObject obj)/*-{
    return obj instanceof Array ? obj : null;
  }-*/;

  public static native <E> JsArray<E> create()/*-{
    return [];
  }-*/;

  private static class ArrayIterator<E> implements ListIterator<E> {
    private int i = 0;
    private final JsArray<E> this$static;

    ArrayIterator(JsArray<E> outer) {
      this$static = outer;
    }

    @Override
    public boolean hasNext() {
      return i < this$static.size();
    }

    @Override
    public E next() {
      return this$static.get(i++);
    }

    @Override
    public boolean hasPrevious() {
      return i > 0;
    }

    @Override
    public E previous() {
      return this$static.get(--i);
    }

    @Override
    public int nextIndex() {
      return i + 1;
    }

    @Override
    public int previousIndex() {
      return i - 1;
    }

    @Override
    public void remove() {
      this$static.splice(i, 1);
    }

    @Override
    public void set(E e) {
      this$static.set(i, e);
    }

    @Override
    public void add(E e) {
      this$static.splice(i, 0, e);
    }
  }

  @Override
  public final native int size()/*-{
    return this.length;
  }-*/;

  @Override
  public final boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public final native boolean contains(Object o)/*-{
    return this.indexOf(o) >= 0;
  }-*/;

  @Override
  public final Iterator<E> iterator() {
    return new ArrayIterator<>(this);
  }

  @Override
  public final Object[] toArray() {
    Object[] result = new Object[this.size()];
    for (int i = 0; i < size(); i++) {
      result[i] = get(i);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final <T> T[] toArray(@Nonnull T[] a) {
    for (int i = 0; i < size(); i++) {
      a[i] = (T) get(i);
    }
    return a;
  }

  @Override
  public final native boolean add(E t)/*-{
    this.push(t);
    return true;
  }-*/;

  @Override
  public final native boolean remove(Object o)/*-{
    var i = this.indexOf(o);
    if (i >= 0) {
      this.splice(i, 1);
    }
    return false;
  }-*/;

  @Override
  public final boolean containsAll(@Nonnull Collection<?> c) {
    if (c == this) {
      return true;
    }
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final boolean addAll(Collection<? extends E> c) {
    for (E o : c) {
      this.add(o);
    }
    return true;
  }

  @Override
  public final boolean addAll(int index, @Nonnull Collection<? extends E> c) {
    JsArray<E> items = JsArray.create();
    items.addAll(c);
    addAllInternal(index, items);
    return true;
  }

  public final native void addAllInternal(int index, JsArray<E> objects)/*-{
    objects.unshift(0);
    objects.unshift(index);
    this.splice.apply(this, objects);
  }-*/;

  @SuppressWarnings("ConstantConditions")
  @Override
  public final boolean removeAll(@Nonnull Collection<?> c) {
    if (c == this) {
      clear();
      return true;
    }
    boolean modified = false;
    for (Object o : c) {
      if (remove(o)) {
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public final boolean retainAll(@Nonnull Collection<?> c) {
    if (c == this) {
      return true;
    }
    boolean modified = false;
    for (int i = size() - 1; i >= 0; i--) {
      if (c.contains(get(i))) {
        splice(i, 1);
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public final native void clear()/*-{
    this.length = 0;
  }-*/;

  @Override
  public final native E get(int index)/*-{
    return this[index];
  }-*/;

  @Override
  public final native E set(int index, E element)/*-{
    var x = this[index];
    this[index] = element;
    return x;
  }-*/;

  @Override
  public final native void add(int index, E element)/*-{
    this.splice(index, 0, element);
  }-*/;

  @Override
  public final native E remove(int index)/*-{
    var x = this[index];
    this.splice(index, 1);
    return x;
  }-*/;

  @Override
  public final native int indexOf(Object o)/*-{
    return Array.prototype.indexOf.call(this, o);
  }-*/;

  @Override
  public final native int lastIndexOf(Object o)/*-{
    return Array.prototype.lastIndexOf.call(this, o);
  }-*/;

  @Override
  public final ListIterator<E> listIterator() {
    return new ArrayIterator<>(this);
  }

  @Override
  public final ListIterator<E> listIterator(int index) {
    ArrayIterator<E> it = new ArrayIterator<>(this);
    it.i = index;
    return it;
  }

  @Override
  public final native List<E> subList(int fromIndex, int toIndex)/*-{
    return this.splice(fromIndex, toIndex);
  }-*/;

  @SafeVarargs
  public final native JsArray<E> splice(int startIndex, int deletions, E... insertions)/*-{
    return Array.prototype.splice.apply(this, [startIndex, deletions].concat(insertions));
  }-*/;
}
