// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Initialize the blocks editor workspace.
 * 
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.BlocklyEditor = {};

Blockly.BlocklyEditor.startup = function(documentBody, formName) {
  Blockly.inject(documentBody);
  Blockly.Drawer.createDom();
  Blockly.Drawer.init();
  Blockly.BlocklyEditor.formName_ = formName;
  Blockly.bindEvent_(Blockly.mainWorkspace.getCanvas(), 'blocklyWorkspaceChange', this,
      function() { 
        window.parent.BlocklyPanel_blocklyWorkspaceChanged(Blockly.BlocklyEditor.formName_); 
      });
};
