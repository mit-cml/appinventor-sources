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
 * @fileoverview Utility functions for handling variables.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a database of variables.
 * @param {Array.<string>} reservedWords An array of words that are illegal for
 *     use as variable names in a language (e.g. ['new', 'if', 'this', ...]).
 * @constructor
 */
Blockly.Variables = function(reservedWords) {
  this.reservedDict_ = {};
  if (reservedWords) {
    for (var x = 0; x < reservedWords.length; x++) {
      this.reservedDict_[Blockly.Variables.PREFIX_ + reservedWords[x]] = true;
    }
  }
  this.reset();
};

/**
 * JavaScript doesn't have a true hashtable, it uses object properties.
 * Since even clean objects have a few properties, prepend this prefix onto
 * names so that they don't collide with any builtins.
 * @private
 */
Blockly.Variables.PREFIX_ = 'x_';

/**
 * Empty the database and start from scratch.  The reserved words are kept.
 */
Blockly.Variables.prototype.reset = function() {
  this.db_ = {};
  this.dbReverse_ = {};
};

/**
 * Convert a Blockly variable name to a legal exportable variable name.
 * @param {string} name The Blockly variable name (no constraints).
 * @return {string} A variable name legal for the exported language.
 */
Blockly.Variables.prototype.getVariable = function(name) {
  var normalized = Blockly.Variables.PREFIX_ +
      (Blockly.caseSensitiveVariables ? name : name.toLowerCase());
  if (normalized in this.db_) {
    return this.db_[normalized];
  } else {
    return this.getDistinctVariable(name);
  }
};

/**
 * Convert a Blockly variable name to a legal exportable variable name.
 * Ensure that this is a new variable not overlapping any previously defined
 * variable.
 * @param {string} name The Blockly variable name (no constraints).
 * @return {string} A variable name legal for the exported language.
 */
Blockly.Variables.prototype.getDistinctVariable = function(name) {
  var safeName = this.safeName_(name);
  if (this.dbReverse_[Blockly.Variables.PREFIX_ + safeName]) {
    // Collision with existing variable.  Create a unique name.
    var testName;
    var i = 1;
    do {
      i++;
      testName = safeName + i;
    } while (this.dbReverse_[Blockly.Variables.PREFIX_ + testName]);
    safeName = testName;
  }
  this.db_[Blockly.Variables.PREFIX_ + name.toLowerCase()] = safeName;
  this.dbReverse_[Blockly.Variables.PREFIX_ + safeName] = true;
  return safeName;
};

/**
 * Given a proposed variable name, generate a name that conforms to the
 * [_A-Za-z][_A-Za-z0-9]* format that most languages consider legal for
 * variables.
 * Also check against list of reserved words for the current language and
 * ensure variable doesn't collide.
 * @param {string} name Potentially illegal variable name.
 * @return {string} Safe variable name.
 * @private
 */
Blockly.Variables.prototype.safeName_ = function(name) {
  if (!name) {
    name = 'var_unnamed';
  } else {
    // Unfortunately names in non-latin characters will all be sequences of _s.
    // TODO: Make friendlier names for non-latin variables.
    name = name.replace(/[^\w]/g, '_');
    if ('0123456789'.indexOf(name.charAt(0)) != -1) {
      name = 'var_' + name;
    }
  }
  // Ensure no collsion with reserved word list.
  while ((Blockly.Variables.PREFIX_ + name) in this.reservedDict_) {
    name = 'var_' + name;
  }
  return name;
};

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
        variableHash[Blockly.Variables.PREFIX_ +
            (Blockly.caseSensitiveVariables ?
            varName : varName.toLowerCase())] = varName;
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
      // If variables are case-insensitive, ensure that if the new variable
      // matches with an existing variable, the new case prevails throughout.
      if (!Blockly.caseSensitiveVariables) {
        Blockly.Variables.renameVariable(text, text);
      }
    }
    if (text) {
      this.setText(text);
    }
  }
};

/**
 * Do the given two variable names refer to the same variable?
 * Blockly has a mode where variables are case-insensitive.
 * @param {string} name1 First variable name.
 * @param {string} name2 Second variable name.
 * @return {boolean} True if names are the same.
 */
Blockly.Variables.nameEquals = function(name1, name2) {
  if (!Blockly.caseSensitiveVariables) {
    name1 = name1.toLowerCase();
    name2 = name2.toLowerCase();
  }
  return name1 == name2;
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
