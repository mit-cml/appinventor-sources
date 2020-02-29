// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Add additional "class methods" to Blockly.RenderedConnection
 */

var oldConnect = Blockly.RenderedConnection.prototype.connect_;
Blockly.RenderedConnection.prototype.connect_ = function(childConnection) {
  oldConnect.call(this, childConnection);

  var input = this.getInput();
  if (!input) {
    return;
  }

  var visible = input.isVisible();
  var block = childConnection.getSourceBlock();
  block.getSvgRoot().style.display = visible ? 'block' : 'none';
};

Blockly.RenderedConnection.prototype.input_;

Blockly.RenderedConnection.prototype.getInput = function() {
  if (this.input_ !== undefined) {
    return this.input_;
  }

  var inputs = this.sourceBlock_.inputList;
  for (var i = 0, input; (input = inputs[i]); i++) {
    if (input.connection == this) {
      this.input_ = input;
      return input;
    }
  }
  this.input_ = null;
  return null;
};

var oldDisconnectInternal = Blockly.RenderedConnection.
    prototype.disconnectInternal_;
Blockly.RenderedConnection.prototype.disconnectInternal_ = function(parentBlock,
    childBlock) {
  oldDisconnectInternal.call(this, parentBlock, childBlock);
  console.log('called');

  // Reset visibility, since this block is now a top block.
  childBlock.getSvgRoot().style.display = 'block';
};
