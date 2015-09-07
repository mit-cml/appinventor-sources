// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;

import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A helper class that allows a palette category to have its components in a defined order.
 */
abstract class OrderedPaletteHelper implements PaletteHelper {
  private final List<String> orderedComponentNames;

  private final Comparator<SimplePaletteItem> componentsComparator =
      new Comparator<SimplePaletteItem>() {
    @Override
    public int compare(SimplePaletteItem spi1, SimplePaletteItem spi2) {
      int value1 = orderedComponentNames.indexOf(spi1.getName());
      int value2 = orderedComponentNames.indexOf(spi2.getName());
      return Integer.signum(value1 - value2);
    }
  };

  private final List<SimplePaletteItem> componentsAddedSoFar =
      new ArrayList<SimplePaletteItem>();

  protected OrderedPaletteHelper(List<String> orderedComponentNames) {
    this.orderedComponentNames = orderedComponentNames;
  }

  @Override
  public final void addPaletteItem(VerticalPanel panel, SimplePaletteItem component) {
    int index = Collections.binarySearch(componentsAddedSoFar, component, componentsComparator);
    int insertionPos = - index - 1;
    componentsAddedSoFar.add(insertionPos, component);
    panel.insert(component, insertionPos);
  }
}
