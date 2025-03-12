// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.gwt.user.client.ui.Image;

/**
 * ComponentFactory defines a high-level interface for objects that create new mock components.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public interface ComponentFactory {
  /**
   * Instantiates mock component by name.
   */
  MockComponent createMockComponent(String name, String type);

  /**
   *
   * @param name
   * @param type
   * @return
   */
  Image getImage(String name, String type);

  Image getImage(String name);

  String getLicense(String name, String type);
}
