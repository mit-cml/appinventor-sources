/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
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
 * @fileoverview Utility functions for handling variables and procedure names.
 * Note that variables and procedures share the same name space, meaning that
 * one can't have a variable and a procedure of the same name.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a database of variables.
 * @param {Array.<string>} reservedWords An array of words that are illegal for
 *     use as variable names in a language (e.g. ['new', 'if', 'this', ...]).
 * @constructor
 */
Blockly.Variables = {};

Blockly.Variables.NAME_TYPE = 'variable';

/**
 * Find all user-created variables.
 * @return {!Array.<string>} Array of variable names.
 */
Blockly.Variables.allVariables = function() {
  var blocks = Blockly.mainWorkspace.getAllBlocks();
  var variableHash = {};
  // Iterate through every block and add each variable to the hash.
  for (var x = 0; x < blocks.length; x++) {
    var func = blocks[x].getVars;
    if (func) {
      var blockVariables = func.call(blocks[x]);
      for (var y = 0; y < blockVariables.length; y++) {
        var varName = blockVariables[y];
        // Variable name may be null if the block is only half-built.
        if (varName) {
          variableHash[Blockly.Names.PREFIX_ +
              varName.toLowerCase()] = varName;
        }
      }
    }
  }
  // Flatten the hash into a list.
  var variableList = [];
  for (var name in variableHash) {
    variableList.push(variableHash[name]);
  }
  return variableList;
};

/**
 * Return a sorted list of variable names for variable dropdown menus.
 * Include a special option at the end for creating a new variable name.
 * @return {!Array.<string>} Array of variable names.
 */
Blockly.Variables.dropdownCreate = function() {
  var variableList = Blockly.Variables.allVariables();
  variableList.sort(Blockly.caseInsensitiveComparator);
  variableList.push(Blockly.MSG_RENAME_VARIABLE);
  variableList.push(Blockly.MSG_NEW_VARIABLE);
  return variableList;
};

/**
 * Event handler for a change in variable name.
 * Special case the 'New variable...' and 'Rename variable...' options.
 * In both of these special cases, prompt the user for a new name.
 * @param {string} text The selected dropdown menu option.
 */
Blockly.Variables.dropdownChange = function(text) {
  function promptName(promptText, defaultText) {
    Blockly.hideChaff();
    var newVar = window.prompt(promptText, defaultText);
    // Strip leading and trailing whitespace.  Beyond this, all names are legal.
    return newVar && newVar.replace(/^[\s\xa0]+|[\s\xa0]+$/g, '');
  }
  if (text == Blockly.MSG_RENAME_VARIABLE) {
    var oldVar = this.getText();
    text = promptName(Blockly.MSG_RENAME_VARIABLE_TITLE.replace('%1', oldVar),
                      oldVar);
    if (text) {
      Blockly.Variables.renameVariable(oldVar, text);
    }
  } else {
    if (text == Blockly.MSG_NEW_VARIABLE) {
      text = promptName(Blockly.MSG_NEW_VARIABLE_TITLE, '');
      // Since variables are case-insensitive, ensure that if the new variable
      // matches with an existing variable, the new case prevails throughout.
      Blockly.Variables.renameVariable(text, text);
    }
    if (text) {
      this.setText(text);
    }
  }
};

/**
 * Find all instances of the specified variable and rename them.
 * @param {string} oldName Variable to rename.
 * @param {string} newName New variable name.
 */
Blockly.Variables.renameVariable = function(oldName, newName) {
  var blocks = Blockly.mainWorkspace.getAllBlocks();
  // Iterate through every block.
  for (var x = 0; x < blocks.length; x++) {
    var func = blocks[x].renameVar;
    if (func) {
      func.call(blocks[x], oldName, newName);
    }
  }
};

/**
 * Construct the blocks required by the flyout for the variable category.
 * @param {!Array.<!Blockly.Block>} blocks List of blocks to show.
 * @param {!Array.<number>} gaps List of widths between blocks.
 * @param {number} margin Standard margin width for calculating gaps.
 * @param {!Blockly.Workspace} workspace The flyout's workspace.
 */
Blockly.Variables.flyoutCategory = function(blocks, gaps, margin, workspace) {
  var variableList = Blockly.Variables.allVariables();
  variableList.sort(Blockly.caseInsensitiveComparator);
  // In addition to the user's variables, we also want to display the default
  // variable name at the top.  We also don't want this duplicated if the
  // user has created a variable of the same name.
  variableList.unshift(null);
  var defaultVariable = undefined;
  for (var i = 0; i < variableList.length; i++) {
    if (variableList[i] === defaultVariable) {
      continue;
    }
    var getBlock = Blockly.Language.variables_get ?
        new Blockly.Block(workspace, 'variables_get') : null;
    getBlock && getBlock.initSvg();
    var setBlock = Blockly.Language.variables_set ?
        new Blockly.Block(workspace, 'variables_set') : null;
    setBlock && setBlock.initSvg();
    if (variableList[i] === null) {
      defaultVariable = (getBlock || setBlock).getVars()[0];
    } else {
      getBlock && getBlock.setTitleText(variableList[i], 1);
      setBlock && setBlock.setTitleText(variableList[i], 1);
    }
    setBlock && blocks.push(setBlock);
    getBlock && blocks.push(getBlock);
    if (getBlock && setBlock) {
      gaps.push(margin, margin * 3);
    } else {
      gaps.push(margin * 2);
    }
  }
};
