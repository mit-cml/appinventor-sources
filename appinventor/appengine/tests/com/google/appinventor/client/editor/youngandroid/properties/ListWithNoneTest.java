// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link ListWithNone}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ListWithNoneTest extends TestCase {
  static class FakeListBox implements ListWithNone.ListBoxWrapper {
    private final List<String> items = new ArrayList<String>();
    private int selectedIndex = -1;

    @Override
    public void addItem(String item) {
      items.add(item);
    }

    @Override
    public String getItem(int index) {
      return items.get(index);
    }

    @Override
    public void removeItem(int index) {
      items.remove(index);
    }

    @Override
    public void setSelectedIndex(int index) {
      selectedIndex = index;
    }
  };

  private static final String NONE_DISPLAY_ITEM = "Nada";
  private final FakeListBox fakeListBox = new FakeListBox();

  private ListWithNone listWithNone;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    listWithNone = new ListWithNone(NONE_DISPLAY_ITEM, fakeListBox);
  }

  public void testNew() {
    // The listbox should just contain the none item.
    assertEquals(1, fakeListBox.items.size());
    assertEquals(NONE_DISPLAY_ITEM, fakeListBox.items.get(0));
    assertEquals(-1, fakeListBox.selectedIndex);
  }

  public void testAddItem() {
    listWithNone.addItem("one");
    listWithNone.addItem("two");
    listWithNone.addItem("three");
    // The listbox should just contain 4 items.
    assertEquals(4, fakeListBox.items.size());
    assertEquals(NONE_DISPLAY_ITEM, fakeListBox.items.get(0));
    assertEquals("one", fakeListBox.items.get(1));
    assertEquals("two", fakeListBox.items.get(2));
    assertEquals("three", fakeListBox.items.get(3));
  }

  public void testAddItemWithDifferentDisplayItem() {
    listWithNone.addItem("one", "1");
    listWithNone.addItem("two", "2");
    listWithNone.addItem("three", "3");
    // The listbox should just contain 4 items matching the display items, not the values.
    assertEquals(4, fakeListBox.items.size());
    assertEquals(NONE_DISPLAY_ITEM, fakeListBox.items.get(0));
    assertEquals("1", fakeListBox.items.get(1));
    assertEquals("2", fakeListBox.items.get(2));
    assertEquals("3", fakeListBox.items.get(3));
  }

  public void testSelectValue() {
    listWithNone.addItem("one");
    listWithNone.addItem("two");
    listWithNone.addItem("three");
    // Select an item.
    listWithNone.selectValue("two");
    assertEquals(2, fakeListBox.selectedIndex);
  }

  public void testRemoveValue() {
    listWithNone.addItem("one");
    listWithNone.addItem("two");
    listWithNone.addItem("three");
    // Remove an item.
    listWithNone.removeValue("two");
    // The listbox should just contain 3 items.
    assertEquals(3, fakeListBox.items.size());
    assertEquals(NONE_DISPLAY_ITEM, fakeListBox.items.get(0));
    assertEquals("one", fakeListBox.items.get(1));
    assertEquals("three", fakeListBox.items.get(2));
  }

  public void testGetValueAtIndex() {
    listWithNone.addItem("one", "1");
    listWithNone.addItem("two", "2");
    listWithNone.addItem("three", "3");
    assertEquals("", listWithNone.getValueAtIndex(0));
    assertEquals("one", listWithNone.getValueAtIndex(1));
    assertEquals("two", listWithNone.getValueAtIndex(2));
    assertEquals("three", listWithNone.getValueAtIndex(3));
    try {
      listWithNone.getValueAtIndex(4);
      fail();
    } catch (IndexOutOfBoundsException e) {
      // expected
    }
  }

  public void testIndexOfValue() {
    listWithNone.addItem("one", "1");
    listWithNone.addItem("two", "2");
    listWithNone.addItem("three", "3");
    assertEquals(0, listWithNone.indexOfValue(""));
    assertEquals(1, listWithNone.indexOfValue("one"));
    assertEquals(2, listWithNone.indexOfValue("two"));
    assertEquals(3, listWithNone.indexOfValue("three"));
    assertEquals(-1, listWithNone.indexOfValue("four"));
  }

  public void testContainsValue() {
    listWithNone.addItem("one", "1");
    listWithNone.addItem("two", "2");
    listWithNone.addItem("three", "3");
    assertTrue(listWithNone.containsValue(""));
    assertTrue(listWithNone.containsValue("one"));
    assertTrue(listWithNone.containsValue("two"));
    assertTrue(listWithNone.containsValue("three"));
    assertFalse(listWithNone.containsValue("four"));
  }

  public void testGetDisplayItemForValue() {
    listWithNone.addItem("one", "1");
    listWithNone.addItem("two", "2");
    listWithNone.addItem("three", "3");
    assertEquals(NONE_DISPLAY_ITEM, listWithNone.getDisplayItemForValue(""));
    assertEquals("1", listWithNone.getDisplayItemForValue("one"));
    assertEquals("2", listWithNone.getDisplayItemForValue("two"));
    assertEquals("3", listWithNone.getDisplayItemForValue("three"));
    try {
      listWithNone.getDisplayItemForValue("four");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
