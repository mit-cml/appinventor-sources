// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.editor.simple.components.MockComponent;

/**
 * An interface that a palette can implement in order to support showing Simple
 * components which can be dropped onto a visual designer panel.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface SimplePalettePanel {
  /**
   * Loads all components to be shown on the palette.
   *
   * @param dropTargetProvider  provider of targets that palette items can be
   *                            dropped on
   */
  void loadComponents(DropTargetProvider dropTargetProvider);

  /**
   * Configure a mock component.
   *
   * @param mockComponent  mock component to configure
   */
  void configureComponent(MockComponent mockComponent);
}
