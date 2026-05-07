// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2022-2025 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

import com.google.appinventor.client.json.JsArray;
import com.google.appinventor.client.json.JsonUtil;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

@JsType(namespace = "Blockly", isNative = true)
public class Block {
  protected Block() {}

  public String type;

  public JsArray<Input> inputList;

  public Connection previousConnection;

  public Connection nextConnection;

  public Connection outputConnection;

  public native String getFieldValue(String name);

  public native Input getInput(String name);

  public native Block getInputTargetBlock(String name);

  public native void dispose(boolean healStack);

  public native void moveBy(double dx, double dy);

  public native Coordinate getRelativeToSurfaceXY();

  @JsOverlay
  public final JsArray<Block> getStatementBlocks(String name) {
    JsArray<Block> result = JsArray.create();
    Block child = getInputTargetBlock(name);
    while (child != null) {
      result.add(child);
      if (child.nextConnection == null) {
        return result;
      }
      child = child.nextConnection.targetBlock();
    }
    return result;
  }

  @JsOverlay
  public final String getStringProperty(String name) {
    return (String) JsonUtil.getProperty(this, name);
  }

  @JsOverlay
  public final void setMutationFromXmlString(String text) {
    BlocklyUtil.setMutationFromXmlString(this, text);
  }
}
