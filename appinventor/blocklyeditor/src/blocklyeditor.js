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

/**
 * Add a "Generate Yail" option to the context menu for every block. The generated yail will go in
 * the block's comment (if it has one) for now. 
 * TODO: eventually create a separate kind of bubble for the generated yail, which can morph into 
 * the bubble for "do it" output once we hook up to the REPL.
 */ 
Blockly.Block.prototype.customContextMenu = function(options) {
  var yailOption = {enabled: true};
  yailOption.text = "Generate Yail";
  var myBlock = this;
  yailOption.callback = function() {
    myBlock.setCommentText(Blockly.Yail.blockToCode1(myBlock));
  };
  options.push(yailOption);
};