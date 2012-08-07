// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle serialization of the blocks workspace
 * 
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.SaveFile = {};

Blockly.SaveFile.load = function(blocksContent) {
  // TODO(sharon): deal with errors
  if (blocksContent.length != 0) {
    var xml = Blockly.Xml.textToDom(blocksContent);
    var firstChild = xml.childNodes[0];
    if (firstChild.nodeName && firstChild.nodeName == 'YACodeBlocks') {
      console.log("Warning: don't know how to convert old codeblocks blocks  yet. Ignoring!");
    }
    Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
  }
};

Blockly.SaveFile.get = function() {
  return Blockly.Xml.domToPrettyText(Blockly.Xml.workspaceToDom(Blockly.mainWorkspace));
};

