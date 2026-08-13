// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Looper;
import android.view.View;

import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;

import org.junit.Test;

import static org.robolectric.Shadows.shadowOf;

public class ListViewTest extends RobolectricTestBase {
  /**
   * Test that different ListView components respond to different touch events. This
   * tests the fix for issue #2544 where multiple ListViews trigger the last ListView's
   * AfterPicking event.
   */
  @Test
  public void testMultipleListViewEvents() {
    ListView listView1 = new ListView(getForm());
    listView1.ElementsFromString("1,2,3");
    listView1.Height(100);
    listView1.Width(320);
    initialize(listView1);
    ListView listView2 = new ListView(getForm());
    listView2.ElementsFromString("4,5,6");
    listView2.Height(100);
    listView2.Width(320);
    initialize(listView2);
    ListView listView3 = new ListView(getForm());
    listView3.ElementsFromString("7,8,9");
    listView3.Height(100);
    listView3.Width(320);
    initialize(listView3);

    // Click on the first list view
    assertTrue(getViewForPosition(listView1, 0).performClick());
    ShadowEventDispatcher.assertEventFired(listView1, "AfterPicking");
    ShadowEventDispatcher.assertEventNotFired(listView2, "AfterPicking");
    ShadowEventDispatcher.assertEventNotFired(listView3, "AfterPicking");
    ShadowEventDispatcher.clearEvents();

    // Click on the second list view
    assertTrue(getViewForPosition(listView2, 1).performClick());
    ShadowEventDispatcher.assertEventNotFired(listView1, "AfterPicking");
    ShadowEventDispatcher.assertEventFired(listView2, "AfterPicking");
    ShadowEventDispatcher.assertEventNotFired(listView3, "AfterPicking");
    ShadowEventDispatcher.clearEvents();

    // Click on the third list view
    assertTrue(getViewForPosition(listView3, 2).performClick());
    ShadowEventDispatcher.assertEventNotFired(listView1, "AfterPicking");
    ShadowEventDispatcher.assertEventNotFired(listView2, "AfterPicking");
    ShadowEventDispatcher.assertEventFired(listView3, "AfterPicking");
  }

  /**
   * Test the filtering capability of the ListView.
   *
   * <p>In nb187 there was an issue (#2550) where filtering would throw a NullPointerException,
   * causing the app to crash.
   */
  @Test
  public void testFilter() throws InterruptedException {
    ListView listView1 = new ListView(getForm());
    listView1.ElementsFromString("apple,banana,cantaloupe,date");
    listView1.Height(200);
    listView1.Width(320);
    EditText filterBox = (EditText) ((LinearLayout) listView1.getView()).getChildAt(0);
    filterBox.setText("an");
    Thread.sleep(100);  // Filtering runs on a separate thread for performance reasons
    runAllEvents();

    LinearLayout listlayout = (LinearLayout) ((LinearLayout) listView1.getView()).getChildAt(1);
    RecyclerView rv = (RecyclerView) listlayout.getChildAt(0);
    int count = 0;
    for (int i = 0; i < rv.getLayoutManager().getChildCount(); i++) {
      if (rv.getLayoutManager().getChildAt(i).getVisibility() == View.VISIBLE) {
        count++;
      }
    }
    assertEquals(2, count);
  }

  /**
   * Test removal of the selection for a list containing dictionary based elements.
   *
   * <p>In nb195 there was an issue (#3008) where setting the SelectionIndex to 0 to remove
   * the selection instead resulted in an ArrayIndexOutOfBoundsException.
   */
  @Test
  public void testSelectionRemovalWithDictBasedElements() {
    ListView listView1 = new ListView(getForm());
    Object listItem = listView1.CreateElement("main", "detail", "image");
    YailList list = YailList.makeList(new Object[]{listItem});
    listView1.Elements(list);
    listView1.Height(200);
    listView1.Width(320);

    listView1.SelectionIndex(1);  // select the 1st element in the list.
    assertEquals(1, listView1.SelectionIndex());

    listView1.SelectionIndex(0);  // clear the selected element.
    assertEquals(0, listView1.SelectionIndex());
  }

  /**
   * With MultiSelect enabled, tapping rows builds up SelectedItems instead of replacing it, and
   * tapping a selected row again removes it. Selection and SelectionIndex report the row that was
   * touched last either way, including when that touch was the one that deselected it.
   */
  @Test
  public void testMultiSelectAccumulatesTappedRows() {
    ListView listView = new ListView(getForm());
    listView.ElementsFromString("apple,banana,cantaloupe");
    listView.Height(200);
    listView.Width(320);
    listView.MultiSelect(true);
    initialize(listView);

    assertTrue(getViewForPosition(listView, 0).performClick());
    assertTrue(getViewForPosition(listView, 2).performClick());

    assertEquals(2, listView.SelectedItems().size());
    assertEquals("apple", listView.SelectedItems().getObject(0));
    assertEquals("cantaloupe", listView.SelectedItems().getObject(1));
    // The last row touched is what the singular blocks report.
    assertEquals(3, listView.SelectionIndex());
    assertEquals("cantaloupe", listView.Selection());

    // Tapping an already selected row takes it back out of the set.
    assertTrue(getViewForPosition(listView, 0).performClick());
    assertEquals(1, listView.SelectedItems().size());
    assertEquals("cantaloupe", listView.SelectedItems().getObject(0));
    assertEquals(1, listView.SelectionIndex());
  }

  /**
   * Without MultiSelect, each tap replaces the previous selection, so SelectedItems never holds
   * more than the one element the user last picked.
   */
  @Test
  public void testSingleSelectReplacesTheSelection() {
    ListView listView = new ListView(getForm());
    listView.ElementsFromString("apple,banana,cantaloupe");
    listView.Height(200);
    listView.Width(320);
    initialize(listView);

    assertTrue(getViewForPosition(listView, 0).performClick());
    assertTrue(getViewForPosition(listView, 2).performClick());

    assertEquals(1, listView.SelectedItems().size());
    assertEquals("cantaloupe", listView.SelectedItems().getObject(0));
    assertEquals(3, listView.SelectionIndex());
  }

  /**
   * Filtering must not throw away a set the user is building with MultiSelect: an item the filter
   * hides stays selected and is still there once the query is cleared.
   */
  @Test
  public void testMultiSelectKeepsSelectionThroughFiltering() throws InterruptedException {
    ListView listView = new ListView(getForm());
    listView.ElementsFromString("apple,banana,cantaloupe,date");
    listView.Height(200);
    listView.Width(320);
    listView.MultiSelect(true);
    initialize(listView);

    assertTrue(getViewForPosition(listView, 0).performClick());  // apple
    assertTrue(getViewForPosition(listView, 3).performClick());  // date
    assertEquals(2, listView.SelectedItems().size());

    EditText filterBox = (EditText) ((LinearLayout) listView.getView()).getChildAt(0);
    filterBox.setText("an");  // hides both apple and date
    Thread.sleep(100);  // Filtering runs on a separate thread for performance reasons
    runAllEvents();
    assertEquals(2, listView.SelectedItems().size());

    filterBox.setText("");
    Thread.sleep(100);
    runAllEvents();
    assertEquals(2, listView.SelectedItems().size());
    assertEquals("apple", listView.SelectedItems().getObject(0));
    assertEquals("date", listView.SelectedItems().getObject(1));
  }

  /**
   * Removing an item above the selected one keeps the selection on the item the user picked,
   * rather than leaving the index pointing at whatever slid into its place.
   */
  @Test
  public void testRemoveItemKeepsSelectionOnTheSameItem() {
    ListView listView = new ListView(getForm());
    listView.ElementsFromString("apple,banana,cantaloupe,date");
    listView.Height(200);
    listView.Width(320);

    listView.SelectionIndex(4);  // date
    listView.RemoveItemAtIndex(2);  // banana goes, so date moves from 4 to 3

    assertEquals(3, listView.SelectionIndex());
    assertEquals("date", listView.Selection());

    listView.RemoveItemAtIndex(3);  // removing date itself clears what the blocks report
    assertEquals(0, listView.SelectionIndex());
    assertEquals("", listView.Selection());
  }

  /**
   * An index outside the list selects nothing, so SelectionIndex reports 0 rather than keeping a
   * number that points at no row — which is what its documentation has always promised.
   */
  @Test
  public void testOutOfRangeSelectionIndexReportsNothingSelected() {
    ListView listView = new ListView(getForm());
    listView.ElementsFromString("apple,banana,cantaloupe");
    listView.Height(200);
    listView.Width(320);

    listView.SelectionIndex(2);
    listView.SelectionIndex(99);

    assertEquals(0, listView.SelectionIndex());
    assertEquals("", listView.Selection());
    assertEquals(0, listView.SelectedItems().size());
  }

  /**
   * Replacing an item leaves the rows around it alone, but the replaced row stops being selected:
   * a different item occupies that position afterwards, so a selection pointing there would no
   * longer refer to what the user picked.
   */
  @Test
  public void testUpdateItemAtIndexClearsThatRowsSelection() {
    ListView listView = new ListView(getForm());
    listView.ElementsFromString("apple,banana,cantaloupe");
    listView.Height(200);
    listView.Width(320);
    listView.SelectionIndex(3);  // cantaloupe

    listView.UpdateItemAtIndex(1, "apricot", "", "");  // a row the user did not pick
    assertEquals("apricot", listView.Elements().get(0));
    assertEquals(3, listView.SelectionIndex());
    assertEquals("cantaloupe", listView.Selection());

    listView.UpdateItemAtIndex(3, "cherry", "", "");  // the selected row
    assertEquals("cherry", listView.Elements().get(2));
    assertEquals(0, listView.SelectionIndex());
    assertEquals("", listView.Selection());
  }

  /** An index outside the list is reported as an error and leaves the items untouched. */
  @Test
  public void testUpdateItemAtIndexOutOfBoundsLeavesTheListAlone() {
    ListView listView = new ListView(getForm());
    listView.ElementsFromString("apple,banana");
    listView.Height(200);
    listView.Width(320);

    listView.UpdateItemAtIndex(3, "cherry", "", "");

    assertEquals(2, listView.Elements().size());
    assertEquals("apple", listView.Elements().get(0));
    assertEquals("banana", listView.Elements().get(1));
  }

  private View getViewForPosition(ListView listView, int position) {
    LinearLayout listLayout = (LinearLayout) ((LinearLayout) listView.getView()).getChildAt(1);
    RecyclerView rv = (RecyclerView) listLayout.getChildAt(0);
    rv.scrollToPosition(position);
    shadowOf(Looper.getMainLooper()).idle();
    RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
    assertNotNull(vh);
    return vh.itemView;
  }

  private void initialize(AndroidViewComponent component) {
    View v = component.getView();
    v.measure(
        View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY));
    v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
    shadowOf(Looper.getMainLooper()).idle();
  }
}
