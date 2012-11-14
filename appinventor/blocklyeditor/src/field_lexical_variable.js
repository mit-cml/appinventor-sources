/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
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
 * @fileoverview Drop-down chooser of variables in the current lexical scope for App Inventor
 * @author fturbak@wellesley.com (Lyn Turbak)
 */
'use strict';

// Get all global names 

/**
 * Class for a variable's dropdown field.
 * @param {!string} varname The default name for the variable.  If null,
 *     a unique variable name will be generated.
 * @extends Blockly.FieldDropdown
 * @constructor
 */
Blockly.FieldLexicalVariable = function(varname) {
 // Call parent's constructor.
  Blockly.FieldDropdown.call(this, Blockly.FieldLexicalVariable.dropdownCreate,
                                   Blockly.FieldLexicalVariable.dropdownChange);
  if (varname) {
    this.setText(varname);
  } else {
    this.setText(Blockly.Variables.generateUniqueName());
  }
};

// FieldLexicalVariable is a subclass of FieldDropdown.
goog.inherits(Blockly.FieldLexicalVariable, Blockly.FieldDropdown);

/**
 * Get the variable's name (use a variableDB to convert into a real name).
 * Unline a regular dropdown, variables are literal and have no neutral value.
 * @return {string} Current text.
 */
Blockly.FieldLexicalVariable.prototype.getValue = function() {
  return this.getText();
};

/**
 * Set the variable name.
 * @param {string} text New text.
 */
Blockly.FieldLexicalVariable.prototype.setValue = function(text) {
  this.setText(text);
};

/**
 * Get the block holding this drop-down variable chooser
 * @return {string} Block holding this drop-down variable chooser. 
 */
Blockly.FieldLexicalVariable.prototype.getBlock = function() {
  return this.block_; 
};

/**
 * Set the block holding this drop-down variable chooser. Also initializes the cachedParent.
 * @param {string} block Block holding this drop-down variable chooser
 */
Blockly.FieldLexicalVariable.prototype.setBlock = function(block) {
  this.block_ = block;
  this.setCachedParent(block.getParent());
};

/**
 * Get the cached parent of the block holding this drop-down variable chooser
 * @return {string} Cached parent of the block holding this drop-down variable chooser. 
 */
Blockly.FieldLexicalVariable.prototype.getCachedParent = function() {
  return this.cachedParent_; 
};

/**
 * Set the cached parent of the block holding this drop-down variable chooser. 
 * This is used for detecting when the parent has changed in the onchange event handler. 
 * @param {string} Parent of the block holding this drop-down variable chooser
 */
Blockly.FieldLexicalVariable.prototype.setCachedParent = function(parent) {
  this.cachedParent_ = parent;
};



// [lyn, 11/10/12]
// Returns the names of all global definitions as a list of strings,
// each of which is prefixed with the name "global". 
Blockly.FieldLexicalVariable.prototype.getGlobalNames = function () {
  var globals = [];
  if (Blockly.mainWorkspace) {
    var blocks = Blockly.mainWorkspace.getAllBlocks(); // [lyn, 11/10/12] Is there a better way to get workspace? 
    for (var i = 0; i < blocks.length; i++) {
      var block = blocks[i];
      if (block.type === 'global_declaration') {
          globals.push("global " + block.getTitleValue('NAME'));
      }
    }
  }
  return globals;
}

// [lyn, 11/10/12]
// Returns the names of all names in lexical scope for the block associated with this menu. 
// including global variable names. Each name has a prefix: 
// * "global " for global names
// * "param " for procedure/function params, event handler params, and loop params
// * "local " for local variable declaration names
Blockly.FieldLexicalVariable.prototype.getNamesInScope = function () {
  var names = this.getGlobalNames();
  // names.push("---");
  var child = this.block_;
  if (child) {
    var parent = child.getParent();
    if (parent) {
      while (parent) {
          if ((parent.type === "procedures_defnoreturn") || (parent.type === "procedures_defreturn")) {
            var params = parent.arguments_; 
            for (var i = 0; i < params.length; i++) {
              names.push("param " + params[i]);
            }
          } else if (((parent.type === "controls_for") || (parent.type === "controls_forEach") )
                     && (parent.getInputTargetBlock('DO') == child)) {// Only DO is in scope, not FROM and TO!

              names.push("param " + parent.getTitleValue('VAR'));
          }
          child = parent;
          parent = parent.getParent(); // keep moving up the chain.
      }
    }
  }
  return names;
}

/**
 * Return a sorted list of variable names for variable dropdown menus.
 * @return {!Array.<string>} Array of variable names.
 * @this {!Blockly.FieldLexicalVariable}
 */
Blockly.FieldLexicalVariable.dropdownCreate = function() {
  var variableList = this.getNamesInScope(); // [lyn, 11/10/12] Get all global, parameter, and local names
  // Variables are not language-specific, use the name as both the user-facing
  // text and the internal representation.
  var options = [];
  // [lyn, 11/10/12] Ensure variable list isn't empty
  if (variableList.length == 0) variableList = ["???"];
  for (var x = 0; x < variableList.length; x++) {
    options[x] = [variableList[x], variableList[x]];
  }
  return options;
};

/**
 * Event handler for a change in variable name.
 * // [lyn, 11/10/12] *** Not clear this needs to do anything for lexically scoped variables. 
 * Special case the 'New variable...' and 'Rename variable...' options.
 * In both of these special cases, prompt the user for a new name.
 * @param {string} text The selected dropdown menu option.
 * @this {!Blockly.FieldLexicalVariable}
 */
Blockly.FieldLexicalVariable.dropdownChange = function(text) {
  if (text) {
    this.setText(text);
  }
  // window.setTimeout(Blockly.Variables.refreshFlyoutCategory, 1);
};
