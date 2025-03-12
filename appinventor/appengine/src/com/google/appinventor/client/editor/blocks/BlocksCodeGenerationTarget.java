// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

package com.google.appinventor.client.editor.blocks;

/**
 * Enumeration of possible targets for blocks code generation.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public enum BlocksCodeGenerationTarget {
  YAIL("Yail"),
  IOTVM("IOT"),
  ALEXA("Alexa");

  private final String target;

  BlocksCodeGenerationTarget(String target) {
    this.target = target;
  }

  public String getTarget() {
    return target;
  }
}
