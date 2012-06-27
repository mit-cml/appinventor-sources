// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Initialize the blocks editor workspace.
 * 
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.BlocklyEditor = {};

Blockly.BlocklyEditor.startup = function(documentBody) {
  Blockly.inject(documentBody);
  Blockly.Drawer.createDom();
  Blockly.Drawer.init();
};