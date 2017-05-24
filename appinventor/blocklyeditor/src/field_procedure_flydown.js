// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Editable procedure name field with flydown containing procedure caller block.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */

'use strict';

goog.provide('AI.Blockly.FieldProcedureFlydown');

goog.require('AI.Blockly.FieldFlydown');

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
  var getterBlock = Blockly.mainWorkspace.newBlock('variables_get');
  getterBlock.setFieldValue(name, 'VAR');
  var setterBlock = Blockly.mainWorkspace.newBlock('variables_set');
  setterBlock.setFieldValue(name, 'VAR');
  return [getterBlock, setterBlock];
};

