// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Container block for all mutators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Blocks.mutators');

//container block for all mutators
Blockly.Blocks['mutator_container'] = {
  // Container.
  init: function() {
    this.setColour(210);
    this.appendDummyInput()
        //.appendField(Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD);
        .appendField("","CONTAINER_TEXT");
    this.appendStatementInput('STACK');
    //this.setTooltip(Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.mutationToDom = function(workspace) {
    var container = document.createElement('mutation');
    container.setAttribute('items', this.itemCount_);
    return container;
}

Blockly.domToMutation = function(container) {
  if(this.valuesToSave != null){
    for (var name in this.valuesToSave) {
      this.valuesToSave[name] = this.getFieldValue(name);
    }
  }

  for (var x = 0; x < this.itemCount_; x++) {
    this.removeInput(this.repeatingInputName + x);
  }
  this.itemCount_ = window.parseInt(container.getAttribute('items'), 10);
  for (var x = 0; x < this.itemCount_; x++) {
    this.addInput(x);
  }
  if (this.itemCount_ == 0) {
    this.addEmptyInput();
  }
}

Blockly.decompose =  function(workspace,itemBlockName,block) {
  var containerBlockName = 'mutator_container';
  //var itemBlockName = 'mutator_item';
  var containerBlock = new Blockly.Block.obtain(workspace,containerBlockName);
  containerBlock.setColour(block.getColour());
  if(block.updateContainerBlock != null){
    block.updateContainerBlock(containerBlock);
  }
  containerBlock.initSvg();
  var connection = containerBlock.getInput('STACK').connection;
  for (var x = 0; x < block.itemCount_; x++) {
    var itemBlock = new Blockly.Block.obtain(workspace, itemBlockName);
    itemBlock.initSvg();
    connection.connect(itemBlock.previousConnection);
    connection = itemBlock.nextConnection;
  }
  return containerBlock;
}

Blockly.compose = function(containerBlock) {
  if(this.valuesToSave != null){
    for (var name in this.valuesToSave) {
      this.valuesToSave[name] = this.getFieldValue(name);
    }
  }
  // Disconnect all input blocks and destroy all inputs.
  if (this.itemCount_ == 0) {
    if(this.emptyInputName != null) {
      this.removeInput(this.emptyInputName);
    }
  } else {
    for (var x = this.itemCount_ - 1; x >= 0; x--) {
      this.removeInput(this.repeatingInputName + x);
    }
  }
  this.itemCount_ = 0;
  // Rebuild the block's inputs.
  var itemBlock = containerBlock.getInputTargetBlock('STACK');
  while (itemBlock) {

    var input = this.addInput(this.itemCount_)

    // Reconnect any child blocks.
    if (itemBlock.valueConnection_) {
      input.connection.connect(itemBlock.valueConnection_);
    }
    this.itemCount_++;
    itemBlock = itemBlock.nextConnection &&
      itemBlock.nextConnection.targetBlock();
  }
  if (this.itemCount_ == 0) {

    this.addEmptyInput();
  }
}

Blockly.saveConnections = function(containerBlock) {
  // Store a pointer to any connected child blocks.
  var itemBlock = containerBlock.getInputTargetBlock('STACK');
  var x = 0;
  while (itemBlock) {
    var input = this.getInput(this.repeatingInputName + x);
    itemBlock.valueConnection_ = input && input.connection.targetConnection;
    x++;
    itemBlock = itemBlock.nextConnection &&
      itemBlock.nextConnection.targetBlock();
  }
}
