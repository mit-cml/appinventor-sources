package com.google.appinventor.client.editor.blocks;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Scope {
  public Block block;
  public WorkspaceSvg workspace;
  public Object comment;
  public Object focusedNode;
}
