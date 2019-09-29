// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Interface for sprites
 *
 */
// spertus and lizlooney agreed this is the best place for the constants.
@SuppressWarnings("InterfaceIsType")
public interface MockSprite {
  static final String PROPERTY_NAME_X = "X";
  static final String PROPERTY_NAME_Y = "Y";
  static final String PROPERTY_NAME_Z = "Z";
  static final double DEFAULT_Z_LAYER = 1.0;

  /**
   * Gets the x-coordinate of the left edge of this sprite.
   *
   * @return the x-coordinate of the left edge of this sprite
   */
  int getLeftX();

  /**
   * Gets the y-coordinate of the top edge of this sprite.
   *
   * @return the y-coordinate of the top edge of this sprite
   */
  int getTopY();

  /**
   * Gets the difference between the X property and the x-coordinate of the left edge of this sprite.
   *
   * @return the difference between the X property and the x-coordinate of the left edge of this sprite
   */
  int getXOffset();

  /**
   * Gets the difference between the Y property and the y-coordinate of the top of this sprite.
   *
   * @return the difference between the Y property and the y-coordinate of the top of this sprite
   */
  int getYOffset();
}
