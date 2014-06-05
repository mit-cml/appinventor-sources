// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Editable parameter field with flydown menu of a getter and setter block.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */

'use strict';

goog.provide('Blockly.FieldParameterFlydown');

goog.require('Blockly.FieldFlydown');

/**
 * Class for a parameter declaration field with flyout menu of getter/setter blocks on mouse over
 * @param {string} text The initial parameter name in the field.
 * @param {boolean} isEditable Indicates whether the the name in the flydown is editable.
 * @param {opt_additionalChangeHandler} function A one-arg function indicating what to do in addition to
 *   renaming lexical variables. May be null/undefined to indicate nothing extra to be done.
 * @extends {Blockly.FieldFlydown}
 * @constructor
 */
// [lyn, 10/26/13] Added opt_additionalChangeHandler to handle propagation of renaming
//    of proc decl params
Blockly.FieldParameterFlydown = function(name, isEditable, displayLocation, opt_additionalChangeHandler) {
  // default change handler renames all references to this lexical variable
  var changeHandler = Blockly.LexicalVariable.renameParam;
  if (opt_additionalChangeHandler) {
    changeHandler = function (text) {
      // changeHandler is invoked as method on field, so "this" will be the field.
      // Need to pass correct "this" to both functions!
      var possiblyRenamedText = Blockly.LexicalVariable.renameParam.call(this, text);
      opt_additionalChangeHandler.call(this, possiblyRenamedText);
      return possiblyRenamedText;
    }
  }
  Blockly.FieldParameterFlydown.superClass_.constructor.call(this, name, isEditable, displayLocation, changeHandler);
};
goog.inherits(Blockly.FieldParameterFlydown, Blockly.FieldFlydown);

Blockly.FieldParameterFlydown.prototype.fieldCSSClassName = 'blocklyFieldParameter'

Blockly.FieldParameterFlydown.prototype.flyoutCSSClassName = 'blocklyFieldParameterFlydown'

 /**
  * Method for creating blocks
  * Returns a list of two XML elements: a getter block for name and a setter block for this parameter field.
  *  @return {!Array.<string>} List of two XML elements.
  */
Blockly.FieldParameterFlydown.prototype.flydownBlocksXML_ = function() {
  var name = this.getText(); // name in this parameter field.
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

/**
 * [lyn, 10/24/13]
 * Add an option for toggling horizontal vs. vertical placement of parameter lists
 * on the given block. Put before "Collapse Block in uncollapsed block"
 * [lyn, 10/27/13] Also remove any "Inline Inputs" option, since vertical params
 * doesn't interact well with it (in procedures_defreturn).
 */
Blockly.FieldParameterFlydown.addHorizontalVerticalOption = function (block, options) {
  var numParams = 0;
  if (block.getParameters) {
    numParams = block.getParameters().length;
  }
  if (Blockly.collapse && ! this.collapsed && numParams > 0) {
    var horizVertOption =
        { enabled: true,
             text: block.horizontalParameters ? Blockly.MSG_VERTICAL_PARAMETERS : Blockly.MSG_HORIZONTAL_PARAMETERS,
         callback: function () { block.setParameterOrientation(!block.horizontalParameters); }
        };

    // Find the index of "Collapse Block" option and inset horizonta/vertical option before it
    var insertionIndex = 0;
    for (var option = null; option = options[insertionIndex]; insertionIndex++) {
      if (option.text === Blockly.MSG_COLLAPSE_BLOCK) {
        break; // Stop loop when insertion point found
      }
    }
    if (insertionIndex < options.length) { // If didn't find "Collapse Block" option, something's wrong.
      options.splice(insertionIndex, 0, horizVertOption);
    }

    // Remove an "Inline Inputs" option (if there is one)
    var removalIndex = -1;
    for (var i = 0, option = null; option = options[i]; i++) {
      if (option.text === Blockly.MSG_INLINE_INPUTS) {
        removalIndex = i;
        break; // Stop loop when insertion point found
      }
    }
    if (removalIndex >= 0) {
      options.splice(removalIndex, 1);
    }
  }
}




