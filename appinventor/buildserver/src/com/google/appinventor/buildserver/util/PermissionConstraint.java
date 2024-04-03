// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class PermissionConstraint<T> {
  private final String permission;
  private final String attribute;
  private final T value;

  public interface Reducer<T> {
    void accept(T value);
  }

  public static class MaxReducer implements Reducer<Integer> {
    private int value = -1;

    public void accept(Integer value) {
      if (value > this.value) {
        this.value = value;
      }
    }

    public int getResult() {
      return value;
    }

    public String toString() {
      return "" + value;
    }
  }

  public static class UnionReducer<T> implements Reducer<T> {
    private Set<T> items = new LinkedHashSet<>();

    public void accept(T item) {
      items.add(item);
    }

    public String getResult() {
      StringBuilder sb = new StringBuilder();
      String sep = "";
      for (T item : items) {
        sb.append(sep);
        sb.append(item);
        sep = "|";
      }
      return sb.toString();
    }

    public String toString() {
      return getResult();
    }
  }

  public PermissionConstraint(String permission, String attribute, T value) {
    this.permission = permission;
    this.attribute = attribute;
    this.value = value;
  }

  public String getAttribute() {
    return attribute;
  }

  public void apply(Reducer<T> reducer) {
    reducer.accept(value);
  }

  /**
   * Reinterpret the PermissionConstraint value as type {@code U} if and only if {@code value} is
   * an instance of {@code U}. If {@code value} is not in {@code U}, a {@code ClassCastException}
   * will be raised.
   *
   * @param clazz the class of the desired type
   * @return this object with a specialization on {@code U}
   * @param <U> the desired type to cast to
   * @throws ClassCastException if the value of the constraint is not an instance of {@code U}
   */
  @SuppressWarnings("unchecked")
  public <U> PermissionConstraint<U> as(Class<U> clazz) {
    if (clazz.isInstance(value)) {
      return (PermissionConstraint<U>) this;
    } else {
      throw new ClassCastException();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermissionConstraint<?> that = (PermissionConstraint<?>) o;
    return permission.equals(that.permission) && attribute.equals(that.attribute)
        && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permission, attribute, value);
  }

  @Override
  public String toString() {
    return permission + "(" + attribute + "=" + value + ")";
  }
}
