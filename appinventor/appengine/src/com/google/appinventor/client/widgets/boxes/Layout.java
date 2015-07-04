// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.boxes;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of layouts for boxes on a work area panel.
 *
 */
public abstract class Layout {

  // List of listeners for any changes in an editor.
  private final List<LayoutChangeListener> layoutChangeListeners;

  // Name of layout
  private String name;

  /**
   * Creates a new layout.
   */
  protected Layout(String name) {
    layoutChangeListeners = new ArrayList<LayoutChangeListener>();

    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * Applies the given layout to the work area.
   *
   * @param workArea  work area to apply layout to
   */
  public abstract void apply(WorkAreaPanel workArea);

  /**
   * Invoked upon resizing of the work area panel.
   *
   * @param width  new work width in pixel
   * @param height  new work height in pixel
   */
  public abstract void onResize(int width, int height);

  /**
   * Return layout converted into JSON format.
   *
   * @return  layout in JSON format
   */
  public abstract String toJson();

  /**
   * Adds an {@link LayoutChangeListener} to the listener list.
   *
   * @param listener  the {@code LayoutChangeListener} to be added
   */
  public void addLayoutChangeListener(LayoutChangeListener listener) {
    layoutChangeListeners.add(listener);
  }

  /**
   * Removes an {@link LayoutChangeListener} from the listener list.
   *
   * @param listener  the {@code LayoutChangeListener} to be removed
   */
  public void removeLayoutChangeListener(LayoutChangeListener listener) {
    layoutChangeListeners.remove(listener);
  }

  /**
   * Triggers a change event to be sent to the listener on the listener list.
   */
  protected void fireLayoutChange() {
    for (LayoutChangeListener listener : layoutChangeListeners) {
      listener.onLayoutChange(this);
    }
  }
}
