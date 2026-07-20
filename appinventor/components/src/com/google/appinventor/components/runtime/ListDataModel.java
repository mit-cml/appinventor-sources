// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Single owner of the {@link ListView}'s non-visual list data.
 *
 * <p>This is the first step of consolidating the ListView's scattered state into one
 * source of truth. In this step the model owns the canonical list of items and every
 * mutation to it; {@link ListView} and the row adapters read and mutate the list through
 * this model instead of each keeping their own copy.
 *
 * <p>Selection and filtering are still handled by the adapter for now and move into this
 * model in a later step, at which point the mutation methods below become the single place
 * where a data change also updates the filtered view and the selection.
 */
public class ListDataModel {
  private final List<Object> items = new ArrayList<>();

  /**
   * The live backing list. The adapter holds this same reference as its data source, so
   * callers must not swap it out — mutate through the methods below.
   */
  public List<Object> getItems() {
    return items;
  }

  public int size() {
    return items.size();
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  public Object get(int index) {
    return items.get(index);
  }

  /** Replaces all items, keeping the same backing list the adapter already references. */
  public void setItems(Collection<?> newItems) {
    items.clear();
    items.addAll(newItems);
  }

  public void add(Object item) {
    items.add(item);
  }

  public void addAt(int index, Object item) {
    items.add(index, item);
  }

  public void addAll(Collection<?> newItems) {
    items.addAll(newItems);
  }

  public void addAllAt(int index, Collection<?> newItems) {
    items.addAll(index, newItems);
  }

  public void remove(int index) {
    items.remove(index);
  }

  public void clear() {
    items.clear();
  }
}
