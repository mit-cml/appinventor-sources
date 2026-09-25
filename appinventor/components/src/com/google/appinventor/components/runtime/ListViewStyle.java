// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * The visual state of a {@link ListView}'s rows: colors, fonts, sizes, alignments and image
 * dimensions.
 *
 * <p>{@link ListView} owns one instance and the row adapters hold a reference to it rather than
 * copies of its values, so an appearance change is picked up the next time a row is bound. That is
 * what lets the appearance setters refresh the visible rows in place instead of building a whole
 * new adapter, which used to reset the user's scroll position.
 *
 * <p>Deliberately a plain holder with no behaviour: it must not know about the RecyclerView, and
 * the non-visual state (items, filter, selection) stays in {@link ListDataModel}.
 */
public class ListViewStyle {
  // Unlike the other properties, ElementColor has no setter call in the ListView constructor, so
  // its default has to live here.
  public int elementColor = Component.COLOR_NONE;
  public int selectionColor;
  public int radius;

  public int textColor;
  public int detailTextColor;

  public float fontSizeMain;
  public float fontSizeDetail;
  public String fontTypeface;
  public String fontTypeDetail;

  public int textAlignmentMain;
  public int textAlignmentDetail;

  public int imageWidth;
  public int imageHeight;
}
