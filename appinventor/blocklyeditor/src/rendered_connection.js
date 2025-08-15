// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Add additional "class methods" to Blockly.RenderedConnection
 */

var oldConnect = Blockly.RenderedConnection.prototype.connect_;

/**
 * Connect two connections together.  This is the connection on the superior
 * block.  Rerender blocks as needed.
 *
 * This function is overridden so that we can properly update the visibility
 * of the child block to match the input (if this connection is an input
 * connection).
 * @param {!Blockly.Connection} childConnection Connection on inferior block.
 * @private
 */
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

Blockly.RenderedConnection.prototype.input_ = undefined;

/**
 * Returns the input this connection belongs to, or null if this connection
 * does not belong to an input.
 * @return {Blockly.Input} The input this block is connected to.
 */
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
/**
 * Disconnect two blocks that are connected by this connection.
 *
 * This function is overridden so that we can set the block to visible
 * when it is disconnected. This handles the case where the block was "inside"
 * of a collapsed block.
 * @param {!Blockly.Block} parentBlock The superior block.
 * @param {!Blockly.Block} childBlock The inferior block.
 * @private
 */
Blockly.RenderedConnection.prototype.disconnectInternal_ = function(parentBlock,
    childBlock) {
  oldDisconnectInternal.call(this, parentBlock, childBlock);

  // Reset visibility, since this block is now a top block.
  childBlock.getSvgRoot().style.display = 'block';
};

