package com.google.appinventor.client.editor.blocks;

public class BlocklyUtil {
  public static native void setMutationFromXmlString(Block block, String text)/*-{
    if (!block.domToMutation) {
      return;  // This block does not support mutations
    }
    var dom = Blockly.Xml.textToDom('<xml>' + text + '</xml>');
    block.domToMutation(dom.firstChild);
  }-*/;
}
