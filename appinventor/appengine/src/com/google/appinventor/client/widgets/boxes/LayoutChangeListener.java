// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
