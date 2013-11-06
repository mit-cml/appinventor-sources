// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

/**
 * Marker interface for sprites
 *
 */
// spertus and lizlooney agreed this is the best place for the constants.
@SuppressWarnings("InterfaceIsType")
public interface MockSprite {
  static final double DEFAULT_Z_LAYER = 1.0;
  static final String PROPERTY_NAME_Z = "Z";
  static final String PROPERTY_NAME_X = "X";
  static final String PROPERTY_NAME_Y = "Y";
}
