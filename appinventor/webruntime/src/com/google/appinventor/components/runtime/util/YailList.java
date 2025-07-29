package com.google.appinventor.components.runtime.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class YailList implements List<Object> {

  public static YailList makeList(Object[] objects) {
    YailList yailList = new YailList();
    yailList.addAll(Arrays.asList(objects));
    return yailList;
  }

  public static YailList makeEmptyList() {
    return new YailList();
  }

  public static YailList makeList(Collection<?> vals) {
    YailList yailList = new YailList();
    yailList.addAll(vals);
    return yailList;
  }

  private final JsArray<?> contents;

  public YailList() {
    contents = JavaScriptObject.createArray().cast();
    addYailHeader(contents);
  }

  private static native void addYailHeader(JsArray<?> contents) /*-{
    contents.splice(0, 0, $wnd.yailListHeader);
  }-*/;

  public String[] toStringArray() {
    return new String[0];
  }

  @Override
  public native int size() /*-{
    return this.length - 1;
  }-*/;

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public native boolean contains(Object o) /*-{
    return this.@com.google.appinventor.components.runtime.util.YailList::contents.indexOf(o) >= 0;
  }-*/;

  @Override
  public Iterator<Object> iterator() {
    return null;
  }

  @Override
  public void forEach(Consumer<? super Object> action) {
    List.super.forEach(action);
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
  public boolean add(Object o) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(int index, Collection<?> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean removeIf(Predicate<? super Object> filter) {
    return List.super.removeIf(filter);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  @Override
  public void replaceAll(UnaryOperator<Object> operator) {
    List.super.replaceAll(operator);
  }

  @Override
  public void sort(Comparator<? super Object> c) {
    List.super.sort(c);
  }

  @Override
  public void clear() {

  }

  @Override
  public Object get(int index) {
    return null;
  }

  @Override
  public Object set(int index, Object element) {
    return null;
  }

  @Override
  public void add(int index, Object element) {

  }

  @Override
  public Object remove(int index) {
    return null;
  }

  @Override
  public int indexOf(Object o) {
    return 0;
  }

  @Override
  public int lastIndexOf(Object o) {
    return 0;
  }

  @Override
  public ListIterator<Object> listIterator() {
    return null;
  }

  @Override
  public ListIterator<Object> listIterator(int index) {
    return null;
  }

  @Override
  public List<Object> subList(int fromIndex, int toIndex) {
    return null;
  }

  @Override
  public Spliterator<Object> spliterator() {
    return List.super.spliterator();
  }

  @Override
  public Stream<Object> stream() {
    return List.super.stream();
  }

  @Override
  public Stream<Object> parallelStream() {
    return List.super.parallelStream();
  }

  public String getString(int index) {
    return contents.get(index).toString();
  }

  public Object getObject(int index) {
    return contents.get(index);
  }
}
