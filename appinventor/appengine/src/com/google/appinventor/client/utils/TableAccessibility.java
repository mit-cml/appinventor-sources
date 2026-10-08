// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 Massachusetts Institute of Technology. All Rights Reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility methods for tables that are only used for layout.
 *
 * <p>Some GWT widgets build their markup out of {@code <table>} elements, for example the
 * frame of a {@code DialogBox} and every {@code TreeItem} that has children. Screen readers
 * announce these as data tables. Marking them with {@code role="presentation"} tells screen
 * readers to ignore the table structure and read only its contents.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Tree tree = new Tree();
 * // ... add items ...
 * TableAccessibility.setLayoutTables(tree);
 * }</pre>
 */
public class TableAccessibility {

  /**
   * Marks a table element as used only for layout.
   *
   * @param table The {@code <table>} element
   */
  public static void setLayoutTable(Element table) {
    if (table != null) {
      table.setAttribute("role", "presentation");
    }
  }

  /**
   * Marks the widget's element, if it is a table, and every table inside it as used only for
   * layout. Use this only for widgets that contain no data tables.
   *
   * @param widget The GWT widget
   */
  public static void setLayoutTables(Widget widget) {
    if (widget == null) {
      return;
    }
    Element element = widget.getElement();
    if ("table".equalsIgnoreCase(element.getTagName())) {
      setLayoutTable(element);
    }
    NodeList<Element> tables = element.getElementsByTagName("table");
    for (int i = 0; i < tables.getLength(); i++) {
      setLayoutTable(tables.getItem(i));
    }
  }
}
