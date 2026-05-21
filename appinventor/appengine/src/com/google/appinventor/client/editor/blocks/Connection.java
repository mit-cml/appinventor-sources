// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2025 Massachusetts Institute of Technology. All Rights Reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
