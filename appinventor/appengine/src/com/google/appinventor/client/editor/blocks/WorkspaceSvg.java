// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012-2025 Massachusetts Institute of Technology. All Rights Reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

import com.google.appinventor.client.json.JsArray;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

@JsType(namespace="Blockly", isNative = true)
public class WorkspaceSvg {
  public native JsArray<Block> getAllBlocks();

  public native JsArray<Block> getTopBlocks();

  public native Block newBlock(String type, String opt_id);

  @JsOverlay
  public final Block newBlock(String type) {
    return newBlock(type, null);
  }

  public native void setVisible(boolean visible);
}
