// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2025 Massachusetts Institute of Technology. All Rights Reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

public class BlocklyUtil {
  public static native void setMutationFromXmlString(Block block, String text)/*-{
    if (!block.domToMutation) {
      return;  // This block does not support mutations
    }
    var dom = Blockly.utils.xml.textToDom('<xml>' + text + '</xml>');
    block.domToMutation(dom.firstChild);
  }-*/;
}
