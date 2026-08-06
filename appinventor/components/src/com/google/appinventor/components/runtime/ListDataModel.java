// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.YailDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Single owner of the {@link ListView}'s non-visual state: the item list, the search filter,
 * and the selection.
 *
 * <p>The model holds the canonical list of items and every mutation to it; {@link ListView} and
 * the row adapters read and mutate through this model instead of each keeping their own copy. It
 * also owns the active search {@link #computeFilter query} and which rows survive it, plus the
 * current {@link #selectSingle selection} (stored against original item indexes so it survives
 * filtering).
 *
 * <p>Filtering is split into a pure {@link #computeFilter} that a background thread can run
 * safely and a {@link #commitFilter} that the UI thread applies, so the filtered view is only
 * ever swapped in on the UI thread and the row count never disagrees with the data — see the
 * Filter in {@link ListAdapterWithRecyclerView}.
 */
public class ListDataModel {
  private final List<Object> items = new ArrayList<>();

  // Filtering. While lastQuery is empty the list is unfiltered and filteredItems/originalPositions
  // are unused; getVisibleItems() then returns the full list directly.
  private List<Object> filteredItems = new ArrayList<>();
  private List<Integer> originalPositions = new ArrayList<>();
  private String lastQuery = "";

  // Selection, stored by ORIGINAL item index (a list so MultiSelect can hold several).
  private final List<Integer> selectedItems = new ArrayList<>();

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

  // ---------------------------------------------------------------------------
  // Filtering
  // ---------------------------------------------------------------------------

  /** The current lowercased search query, or the empty string when unfiltered. */
  public String getLastQuery() {
    return lastQuery;
  }

  /**
   * Computes, but does not apply, the filtered view for the given query. Reads only the item
   * list and returns a fresh, self-contained {@link FilterResult}, so it is safe to run off the
   * UI thread; {@link #commitFilter} then applies the result on the UI thread.
   */
  public FilterResult computeFilter(String query) {
    String normalized = query == null ? "" : query.toLowerCase();
    List<Object> visible = new ArrayList<>();
    List<Integer> positions = new ArrayList<>();
    if (!normalized.isEmpty()) {
      for (int index = 0; index < items.size(); index++) {
        Object item = items.get(index);
        if (filterTextOf(item).toLowerCase().contains(normalized)) {
          visible.add(item);
          positions.add(index);
        }
      }
    }
    return new FilterResult(normalized, visible, positions);
  }

  /**
   * Applies a result from {@link #computeFilter}. Call on the UI thread: it swaps in the new
   * filtered view and drops any selection the filter now hides, so the caller can notify the
   * adapter immediately afterwards without the row count ever disagreeing with the data.
   */
  public void commitFilter(FilterResult result) {
    lastQuery = result.query;
    filteredItems = result.visibleItems;
    originalPositions = result.originalPositions;
    if (!lastQuery.isEmpty()) {
      // Selection is kept by original index, so it survives filtering; only drop entries the
      // filter now hides.
      selectedItems.retainAll(originalPositions);
    }
  }

  /** Re-runs the active filter against the current items, e.g. after the data changed. */
  public void refreshFilter() {
    commitFilter(computeFilter(lastQuery));
  }

  /** The text an item contributes to filtering: main text plus detail for dictionary rows. */
  private static String filterTextOf(Object item) {
    if (item instanceof YailDictionary
        && ((YailDictionary) item).containsKey(Component.LISTVIEW_KEY_MAIN_TEXT)) {
      YailDictionary dict = (YailDictionary) item;
      String text = dict.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
      Object detail = dict.get(Component.LISTVIEW_KEY_DESCRIPTION);
      if (detail != null) {
        text += " " + detail;
      }
      return text;
    }
    return item.toString();
  }

  /** The rows currently shown: the filtered subset while a filter is active, else every item. */
  public List<Object> getVisibleItems() {
    return lastQuery.isEmpty() ? items : filteredItems;
  }

  public Object getVisibleItem(int displayPosition) {
    return getVisibleItems().get(displayPosition);
  }

  public int visibleSize() {
    return getVisibleItems().size();
  }

  /** Display row showing the given original index, or -1 when that item is filtered out. */
  public int toDisplayPosition(int originalPosition) {
    return lastQuery.isEmpty() ? originalPosition : originalPositions.indexOf(originalPosition);
  }

  /** The original item index behind the given display row. */
  public int toOriginalPosition(int displayPosition) {
    return lastQuery.isEmpty() ? displayPosition : originalPositions.get(displayPosition);
  }

  /** Whether the item at the given original index survives the active filter. */
  public boolean isVisible(int originalPosition) {
    return lastQuery.isEmpty() || originalPositions.contains(originalPosition);
  }

  // ---------------------------------------------------------------------------
  // Selection (by original item index)
  // ---------------------------------------------------------------------------

  public boolean isSelected(int originalPosition) {
    return selectedItems.contains(originalPosition);
  }

  /** The first selected original index, or -1 when nothing is selected. */
  public int firstSelection() {
    return selectedItems.isEmpty() ? -1 : selectedItems.get(0);
  }

  /** Selects only the given original index, replacing any previous selection. */
  public void selectSingle(int originalPosition) {
    selectedItems.clear();
    selectedItems.add(originalPosition);
  }

  /** Adds or removes the given original index from the selection (used by MultiSelect). */
  public void toggleSelection(int originalPosition) {
    if (!selectedItems.remove(Integer.valueOf(originalPosition))) {
      selectedItems.add(originalPosition);
    }
  }

  public void clearSelection() {
    selectedItems.clear();
  }

  /**
   * A computed filtered view produced by {@link #computeFilter} and applied by
   * {@link #commitFilter}. Immutable so it can pass safely from a background thread to the UI
   * thread.
   */
  public static final class FilterResult {
    private final String query;
    private final List<Object> visibleItems;
    private final List<Integer> originalPositions;

    FilterResult(String query, List<Object> visibleItems, List<Integer> originalPositions) {
      this.query = query;
      this.visibleItems = visibleItems;
      this.originalPositions = originalPositions;
    }

    int size() {
      return visibleItems.size();
    }
  }
}
