package com.google.appinventor.client.editor.blocks;

import jsinterop.annotations.JsType;

@JsType(namespace = "Blockly", isNative = true)
public class Connection {
  @SuppressWarnings("unused")  // will be used by native constructor
  public Connection(Block block, int type) {}

  public native void connect(Connection other);

  public native Block targetBlock();

  public Connection targetConnection;
}
