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
  console.log('got here');

  // Make sure visibility matches input.
  var block = childConnection.getSourceBlock();
  var visible = this.getSourceBlock().getInputWithBlock(block).isVisible();
  console.log('visible: ', visible);
  block.getSvgRoot().style.display = visible ? 'block' : 'none';
  block.rendered = visible;
};
