// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Editable procedure name field with flydown containing procedure caller block.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */

'use strict';

goog.provide('Blockly.FieldProcedureFlydown');

goog.require('Blockly.FieldFlydown');

/**
 * Class for a clickable parameter field.
 * @param {string} text The initial parameter name in the field.
 * @extends {Blockly.Field}
 * @constructor
 */
Blockly.FieldProcedureFlydown = function(name, displayLocation) {
  Blockly.FieldProcedureFlydown.superClass_.constructor.call(this, name, true, displayLocation);
};

goog.inherits(Blockly.FieldProcedureFlydown, Blockly.FieldFlydown);

Blockly.FieldProcedureFlydown.prototype.fieldCSSClassName = 'blocklyFieldProcedure'

Blockly.FieldProcedureFlydown.prototype.flyoutCSSClassName = 'blocklyFieldProcedureFlydown'

 /**
 * Returns a list of two XML elements: a getter block for name and a setter block for this parameter field.
 *  @return {!Array.<string>} List of two XML elements.
 **/
Blockly.FieldProcedureFlydown.prototype.createBlocks_ = function() {
  var name = this.getText(); // name in this parameter field.
  var getterBlock = new Blockly.Block.obtain(Blockly.mainWorkspace, 'variables_get');
  getterBlock.setFieldValue(name, 'VAR');
  var setterBlock = new Blockly.Block.obtain(Blockly.mainWorkspace, 'variables_set');
  setterBlock.setFieldValue(name, 'VAR');
  return [getterBlock, setterBlock];
}

