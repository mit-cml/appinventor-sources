// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;

import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;

import org.junit.Test;

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

  private View getViewForPosition(ListView listView, int position) {
    LinearLayout listLayout = (LinearLayout) ((LinearLayout) listView.getView()).getChildAt(1);
    RecyclerView rv = (RecyclerView) listLayout.getChildAt(0);
    RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
    assertNotNull(vh);
    return vh.itemView;
  }

  private void initialize(AndroidViewComponent component) {
    component.getView().invalidate();
    component.getView().forceLayout();
    component.getView().measure(0, 0);
  }
}
