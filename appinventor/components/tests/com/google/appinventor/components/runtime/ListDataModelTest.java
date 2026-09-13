// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.appinventor.components.runtime.util.YailDictionary;

import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link ListDataModel}, the single owner of the ListView's data, filter, and
 * selection. Because the model no longer lives inside the adapter, these exercise the
 * filter-position mapping and the selection policy directly, without a RecyclerView. Filtering
 * is applied synchronously here via {@link ListDataModel#computeFilter}/{@code commitFilter},
 * the same two steps the adapter's Filter runs across its background and UI threads.
 */
public class ListDataModelTest extends RobolectricTestBase {

  private static ListDataModel modelOf(Object... items) {
    ListDataModel model = new ListDataModel();
    model.setItems(Arrays.asList(items));
    return model;
  }

  private static void applyFilter(ListDataModel model, String query) {
    model.commitFilter(model.computeFilter(query));
  }

  private static YailDictionary dictItem(String main, String detail) {
    YailDictionary dict = new YailDictionary();
    dict.put(Component.LISTVIEW_KEY_MAIN_TEXT, main);
    dict.put(Component.LISTVIEW_KEY_DESCRIPTION, detail);
    return dict;
  }

  /** With no filter the visible view is the full list and positions map to themselves. */
  @Test
  public void testUnfilteredIsIdentity() {
    ListDataModel model = modelOf("apple", "banana", "cantaloupe", "date");
    assertEquals(4, model.visibleSize());
    assertEquals("apple", model.getVisibleItem(0));
    assertEquals(2, model.toOriginalPosition(2));
    assertEquals(2, model.toDisplayPosition(2));
    assertTrue(model.isVisible(3));
  }

  /** A filter narrows the visible view and maps display rows back to original indexes. */
  @Test
  public void testFilterMapsDisplayRowsToOriginalPositions() {
    ListDataModel model = modelOf("apple", "banana", "cantaloupe", "date");
    applyFilter(model, "an");  // matches banana (1) and cantaloupe (2)

    assertEquals("an", model.getLastQuery());
    assertEquals(2, model.visibleSize());
    assertEquals("banana", model.getVisibleItem(0));
    assertEquals("cantaloupe", model.getVisibleItem(1));
    // display 0 -> original 1, display 1 -> original 2
    assertEquals(1, model.toOriginalPosition(0));
    assertEquals(2, model.toOriginalPosition(1));
    // original 1 is shown at display 0; original 0 is hidden (-1)
    assertEquals(0, model.toDisplayPosition(1));
    assertEquals(-1, model.toDisplayPosition(0));
    assertTrue(model.isVisible(1));
    assertFalse(model.isVisible(0));
  }

  /** Clearing the filter restores the full list. */
  @Test
  public void testClearingFilterRestoresAllItems() {
    ListDataModel model = modelOf("apple", "banana");
    applyFilter(model, "ban");
    assertEquals(1, model.visibleSize());
    applyFilter(model, "");
    assertEquals(2, model.visibleSize());
    assertEquals("apple", model.getVisibleItem(0));
  }

  /** Filtering matches against both the main and the detail text of dictionary rows. */
  @Test
  public void testFilterMatchesMainAndDetailText() {
    ListDataModel model = modelOf(
        dictItem("apple", "red fruit"),
        dictItem("banana", "yellow fruit"));

    applyFilter(model, "red");  // only appears in the first row's detail text
    assertEquals(1, model.visibleSize());
    assertEquals("apple",
        ((YailDictionary) model.getVisibleItem(0)).get(Component.LISTVIEW_KEY_MAIN_TEXT));

    applyFilter(model, "fruit");  // appears in both details
    assertEquals(2, model.visibleSize());
  }

  /**
   * Filtering never changes the selection. It only decides which rows are on screen, so an item
   * the filter hides is still the item the user picked and is still selected once the query is
   * cleared. Whether to drop a selection the user can no longer see is {@link ListView}'s policy
   * call, not the model's.
   */
  @Test
  public void testFilteringLeavesTheSelectionAlone() {
    ListDataModel model = modelOf("apple", "banana", "cantaloupe", "date");
    model.selectSingle(1);  // banana, by original index

    applyFilter(model, "an");  // banana (1) still visible
    assertTrue(model.isSelected(1));

    applyFilter(model, "date");  // banana is hidden now, but still the user's pick
    assertFalse(model.isVisible(1));
    assertTrue(model.isSelected(1));

    applyFilter(model, "");  // and it is still there when the filter clears
    assertTrue(model.isSelected(1));
    assertEquals(1, model.firstSelection());
  }

  /** Removing an item drops its selection and moves the later ones down with their rows. */
  @Test
  public void testRemoveKeepsSelectionOnTheSameItems() {
    ListDataModel model = modelOf("apple", "banana", "cantaloupe", "date");
    model.toggleSelection(1);  // banana
    model.toggleSelection(3);  // date

    model.remove(1);  // banana goes, so date slides from index 3 to index 2

    assertFalse(model.isSelected(1));
    assertTrue(model.isSelected(2));
    assertEquals("date", model.get(2));
  }

  /** Inserting ahead of a selected item carries the selection along with it. */
  @Test
  public void testInsertShiftsSelectionThatMovedDown() {
    ListDataModel model = modelOf("apple", "banana");
    model.toggleSelection(1);  // banana

    model.addAt(0, "apricot");  // banana slides from 1 to 2
    assertTrue(model.isSelected(2));
    assertEquals("banana", model.get(2));

    model.addAllAt(0, Arrays.asList("acai", "almond"));  // and down another two
    assertTrue(model.isSelected(4));
    assertEquals("banana", model.get(4));
  }

  /** Appending leaves existing rows where they are, so the selection does not move. */
  @Test
  public void testAppendLeavesSelectionInPlace() {
    ListDataModel model = modelOf("apple", "banana");
    model.toggleSelection(0);

    model.add("cantaloupe");
    model.addAll(Arrays.asList("date", "elderberry"));

    assertTrue(model.isSelected(0));
    assertEquals(0, model.firstSelection());
  }

  /** Replacing or emptying the items drops the selection, because those items are gone. */
  @Test
  public void testReplacingItemsClearsSelection() {
    ListDataModel model = modelOf("apple", "banana");
    model.toggleSelection(1);

    model.setItems(Arrays.asList("cherry", "damson"));
    assertEquals(-1, model.firstSelection());

    model.toggleSelection(0);
    model.clear();
    assertEquals(-1, model.firstSelection());
  }

  /** Single-selection replaces; toggle adds/removes for MultiSelect; clear empties. */
  @Test
  public void testSelectionSemantics() {
    ListDataModel model = modelOf("apple", "banana", "cantaloupe");

    model.selectSingle(0);
    assertEquals(0, model.firstSelection());
    model.selectSingle(2);  // replaces the previous selection
    assertFalse(model.isSelected(0));
    assertTrue(model.isSelected(2));

    model.toggleSelection(1);  // now 1 and 2 are selected
    assertTrue(model.isSelected(1));
    assertTrue(model.isSelected(2));
    model.toggleSelection(2);  // removes 2
    assertFalse(model.isSelected(2));

    model.clearSelection();
    assertFalse(model.isSelected(1));
    assertEquals(-1, model.firstSelection());
  }
}
