package com.google.appinventor.client.editor.blocks;

import com.google.appinventor.client.json.JsArray;
import jsinterop.annotations.JsType;

@JsType(namespace = "Blockly", isNative = true)
public class Input {
  public String name;

  public JsArray<Field> fieldRow;

  public Connection connection;
}
