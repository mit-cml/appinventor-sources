// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
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
  var getterBlock = new Blockly.Block.obtain(Blockly.mainWorkspace, 'lexical_variable_get');
  getterBlock.setFieldValue(name, 'VAR');
  var setterBlock = new Blockly.Block.obtain(Blockly.mainWorkspace, 'lexical_variable_set');
  setterBlock.setFieldValue(name, 'VAR');
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


