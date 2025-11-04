// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
   * Returns the image associated with a component.
   *
   * @param name The unqualified component name, e.g., Button
   * @param type The fully qualified component type, e.g.,
   *             com.google.appinventor.components.runtime.Button
   * @return The image associated with the component
   */
  Image getImage(String name, String type);

  /**
   * Returns the image associated with a component.
   *
   * @param name The unqualified component name, e.g., Button
   * @return The image associated with the component
   */
  Image getImage(String name);

  /**
   * Returns the license associated with a component.
   *
   * @param name The unqualified component name, e.g., Button
   * @param type The fully qualified component type, e.g.,
   *             com.google.appinventor.components.runtime.Button
   * @return The license associated with the component
   */
  String getLicense(String name, String type);
}
