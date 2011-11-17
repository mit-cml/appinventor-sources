// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.widgets.boxes;

/**
 * Listener interface for changes to a layout.
 *
 */
public interface LayoutChangeListener {

  /**
   * Invoked upon changes to the layout listened to.
   *
   * @param layout  layout that changed
   */
  void onLayoutChange(Layout layout);
}
