// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle serialization of the blocks workspace
 * 
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.SaveFile = {};

Blockly.SaveFile.load = function(blocksContent) {
  // We leave it to our caller to catch JavaScriptException and deal with
  // errors loading the block space.
  if (blocksContent.length != 0) {
    var xml = Blockly.Xml.textToDom(blocksContent);
    Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
  }
};

Blockly.SaveFile.get = function() {
  return Blockly.Xml.domToPrettyText(Blockly.Xml.workspaceToDom(Blockly.mainWorkspace));
};
