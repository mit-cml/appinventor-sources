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
 * @fileoverview Utility functions for handling procedures.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 */
Blockly.Procedures = {};

Blockly.Procedures.NAME_TYPE = 'procedure';

/**
 * Find all user-created procedure definitions.
 * @return {!Array.<string>} Array of variable names.
 */
Blockly.Procedures.allProcedures = function() {
  var blocks = Blockly.mainWorkspace.getAllBlocks(false);
  var proceduresReturn = [];
  var proceduresNoReturn = [];
  for (var x = 0; x < blocks.length; x++) {
    var func = blocks[x].getProcedureDef;
    if (func) {
      var tuple = func.call(blocks[x]);
      if (tuple) {
        if (tuple[1]) {
          proceduresReturn.push(tuple[0]);
        } else {
          proceduresNoReturn.push(tuple[0]);
        }
      }
    }
  }
  proceduresNoReturn.sort(Blockly.caseInsensitiveComparator);
  proceduresReturn.sort(Blockly.caseInsensitiveComparator);
  return [proceduresNoReturn, proceduresReturn];
};

/**
 * Ensure two identically-named procedures don't exist.
 * @param {string} name Proposed procedure name.
 * @param {!Blockly.Block} block Block to disambiguate.
 * @return {string} Non-colliding name.
 */
Blockly.Procedures.findLegalName = function(name, block) {
  if (!block.workspace.editable) {
    return name;
  }
  while (!Blockly.Procedures.isLegalName(name, block.workspace, block)) {
    // Collision with another procedure.
    var r = name.match(/^(.*?)(\d+)$/);
    if (!r) {
      name += '2';
    } else {
      name = r[1] + (parseInt(r[2], 10) + 1);
    }
  }
  return name;
};

/**
 * Does this procedure have a legal name?  Illegal names include names of
 * procedures already defined.
 * @param {string} name The questionable name.
 * @param {!Blockly.Workspace} workspace The workspace to scan for collisions.
 * @param {Blockly.Block} opt_exclude Optional block to exclude from
 *     comparisons (one doesn't want to collide with oneself).
 * @return {boolean} True if the name is legal.
 */
Blockly.Procedures.isLegalName = function(name, workspace, opt_exclude) {
  name = name.toLowerCase();
  var blocks = workspace.getAllBlocks(false);
  // Iterate through every block and check the name.
  for (var x = 0; x < blocks.length; x++) {
    if (blocks[x] == opt_exclude) {
      continue;
    }
    var func = blocks[x].getProcedureDef;
    if (func) {
      var procName = func.call(blocks[x]);
      if (procName[0].toLowerCase() == name) {
        return false;
      }
    }
  }
  return true;
};

/**
 * Rename a procedure.  Called by the editable field.
 * @param {string} text The proposed new name.
 * @return {?string} The accepted name, or null if rejected.
 */
Blockly.Procedures.rename = function(text) {
  if (!this.sourceBlock_.editable) {
    return text;
  }
  // Strip leading and trailing whitespace.  Beyond this, all names are legal.
  text = text.replace(/^[\s\xa0]+|[\s\xa0]+$/g, '');
  if (!text) {
    return null;
  }
  // Ensure two identically-named procedures don't exist.
  text = Blockly.Procedures.findLegalName(text, this.sourceBlock_);
  // Rename any callers.
  var blocks = this.sourceBlock_.workspace.getAllBlocks(false);
  for (var x = 0; x < blocks.length; x++) {
    var func = blocks[x].renameProcedure;
    if (func) {
      func.call(blocks[x], this.text_, text);
    }
  }
  return text;
};

/**
 * Construct the blocks required by the flyout for the procedure category.
 * @param {!Array.<!Blockly.Block>} blocks List of blocks to show.
 * @param {!Array.<number>} gaps List of widths between blocks.
 * @param {number} margin Standard margin width for calculating gaps.
 * @param {!Blockly.Workspace} workspace The flyout's workspace.
 */
Blockly.Procedures.flyoutCategory = function(blocks, gaps, margin, workspace) {
  if (Blockly.Language.procedures_defnoreturn) {
    var block = new Blockly.Block(workspace, 'procedures_defnoreturn');
    block.initSvg();
    blocks.push(block);
    gaps.push(margin * 2);
  }
  if (Blockly.Language.procedures_defreturn) {
    var block = new Blockly.Block(workspace, 'procedures_defreturn');
    block.initSvg();
    blocks.push(block);
    gaps.push(margin * 2);
  }

  var tuple = Blockly.Procedures.allProcedures();
  var proceduresNoReturn = tuple[0];
  var proceduresReturn = tuple[1];
  if (Blockly.Language.procedures_callnoreturn) {
    for (var x = 0; x < proceduresNoReturn.length; x++) {
      var block = new Blockly.Block(workspace, 'procedures_callnoreturn');
      block.setTitleText(proceduresNoReturn[x], 'NAME');
      block.initSvg();
      blocks.push(block);
      gaps.push(margin * 2);
    }
  }
  if (Blockly.Language.procedures_callreturn) {
    for (var x = 0; x < proceduresReturn.length; x++) {
      var block = new Blockly.Block(workspace, 'procedures_callreturn');
      block.setTitleText(proceduresReturn[x], 'NAME');
      block.initSvg();
      blocks.push(block);
      gaps.push(margin * 2);
    }
  }
};

/**
 * When a procedure definition is destroyed, find and destroy all its callers.
 * @param {string} name Name of deleted procedure definition.
 * @param {!Blockly.Workspace} workspace The workspace to delete callers from.
 */
Blockly.Procedures.destroyCallers = function(name, workspace) {
  name = name.toLowerCase();
  var blocks = workspace.getAllBlocks(false);
  // Iterate through every block and check the name.
  for (var x = 0; x < blocks.length; x++) {
    var func = blocks[x].getProcedureCall;
    if (func) {
      var procName = func.call(blocks[x]);
      // Procedure name may be null if the block is only half-built.
      if (procName && procName.toLowerCase() == name) {
        blocks[x].destroy(true);
      }
    }
  }
};
