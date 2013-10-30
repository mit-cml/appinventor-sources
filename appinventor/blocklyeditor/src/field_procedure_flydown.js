/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * http://blockly.googlecode.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
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
  var getterBlock = new Blockly.Block(Blockly.mainWorkspace, 'variables_get');
  getterBlock.setTitleValue(name, 'VAR');
  var setterBlock = new Blockly.Block(Blockly.mainWorkspace, 'variables_set');
  setterBlock.setTitleValue(name, 'VAR');
  return [getterBlock, setterBlock];
}

