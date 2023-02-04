// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Listener interface for receiving events when the Design Preview
 * is changed.
 *
 * Classes interested in knowing then the Design Preview is changed
 * should listen for onDesignPreviewChanged. In particular components
 * that need to draw different svg elements for different previews
 * should listen and act on these change notices.
 *
 * @author  jis@mit.edu (Jeffrey I. Schiller)
 */
public interface DesignPreviewChangeListener {

  /**
   * Invoked when the DesignPreview is Changed
   *
   */
  void onDesignPreviewChanged();

}
