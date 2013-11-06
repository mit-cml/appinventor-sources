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
 * @fileoverview Clickable field with flydown menu of global getter and setter blocks.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */
'use strict';

goog.provide('Blockly.FieldGlobalFlydown');

goog.require('Blockly.FieldFlydown');

/**
 * Class for a clickable global variable declaration field.
 * @param {string} text The initial parameter name in the field.
 * @extends {Blockly.Field}
 * @constructor
 */
Blockly.FieldGlobalFlydown = function(name, displayLocation) {
  Blockly.FieldGlobalFlydown.superClass_.constructor.call(this, name, true, displayLocation,
      // rename all references to this global variable
      Blockly.LexicalVariable.renameGlobal)
};
goog.inherits(Blockly.FieldGlobalFlydown, Blockly.FieldFlydown);

Blockly.FieldGlobalFlydown.prototype.fieldCSSClassName = 'blocklyFieldParameter'

Blockly.FieldGlobalFlydown.prototype.flyoutCSSClassName = 'blocklyFieldParameterFlydown'

/**
 * Block creation menu for global variables
 * Returns a list of two XML elements: a getter block for name and a setter block for this parameter field.
 *  @return {!Array.<string>} List of two XML elements.
 **/
/* Blockly.FieldGlobalFlydown.prototype.createBlocks_ = function() {
  var name = Blockly.globalNamePrefix + " " + this.getText(); // global name for this parameter field.
  var getterBlock = new Blockly.Block(Blockly.mainWorkspace, 'lexical_variable_get');
  getterBlock.setTitleValue(name, 'VAR');
  var setterBlock = new Blockly.Block(Blockly.mainWorkspace, 'lexical_variable_set');
  setterBlock.setTitleValue(name, 'VAR');
  return [getterBlock, setterBlock];
}
*/
Blockly.FieldGlobalFlydown.prototype.flydownBlocksXML_ = function() {
  var name = Blockly.globalNamePrefix + " " + this.getText(); // global name for this parameter field.
  var getterSetterXML =
      '<xml>' +
        '<block type="lexical_variable_get">' +
          '<title name="VAR">' +
            name +
          '</title>' +
        '</block>' +
        '<block type="lexical_variable_set">' +
          '<title name="VAR">' +
            name +
          '</title>' +
        '</block>' +
      '</xml>';
  return getterSetterXML;
}


