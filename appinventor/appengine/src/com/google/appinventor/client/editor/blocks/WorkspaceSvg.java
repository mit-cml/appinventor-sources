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
}
