/* -*- mode: javascript; js-indent-level: 2; -*- */
/**
 * @license
 * Copyright © 2016-2017 Massachusetts Institute of Technology. All rights reserved.
 */

/**
 * @fileoverview A database for tracking user-defined procedures.
 * @author Evan W. Patton <ewpatton@mit.edu>
 */

'use strict';

goog.provide('AI.Blockly.ProcedureDatabase');
goog.require('goog.object');

/**
 * ProcedureDatabase provides a per-workspace data store for manipulating procedure definitions in
 * the Blocks editor.
 *
 * @param workspace The workspace containing procedures indexed by the ProcedureDatabase.
 * @constructor
 */
Blockly.ProcedureDatabase = function(workspace) {
  /**
   * The source workspace for the ProcedureDatabase.
   * @type {!Blockly.WorkspaceSvg}
   * @private
   */
  this.workspace_ = workspace;

  /**
   * Procedure definition map from block ID to block. This is a subset of
   * {@link Blockly.Workspace.blockDB}.
   * @type {{string: !Blockly.BlockSvg}}
   * @private
   */
  this.procedures_ = {};

  /**
   * Procedure definition map for procedures that return values. This is a subset of
   * {@link #procedures_}.
   * @type {{string: !Blockly.BlockSvg}}
   * @private
   */
  this.returnProcedures_ = {};

  /**
   * Procedure definition map for procedures that do not return values. This is a subset of
   * {@link #procedures_}.
   * @type {{string: !Blockly.BlockSvg}}
   * @private
   */
  this.voidProcedures_ = {};

  /**
   * Number of procedures in the database.
   * @type {number}
   */
  this.length = 0;

  /**
   * Number of procedure definitions in the database that return a value.
   * @type {number}
   */
  this.returnProcedures = 0;

  /**
   * Number of procedure definitions in the database that do not return a value.
   * @type {number}
   */
  this.voidProcedures = 0;
};

Blockly.ProcedureDatabase.defaultValue = ['', 'none'];

/**
 * Get a list of names for procedures in the database.
 *
 * @param {Boolean=false} returnValue Return names of procedures with return values (true) or without return values (false)
 * @returns {!string[]}
 */
Blockly.ProcedureDatabase.prototype.getNames = function(returnValue) {
  return AI.Blockly.AIProcedure.getProcedureNames(returnValue, this.workspace_).map(function(v) { return v[0]; });
};

/**
 * Get a list of (name, id) tuples for showing procedure names in a dropdown field.
 *
 * @returns {!Array.<Array.<string>>}
 */
Blockly.ProcedureDatabase.prototype.getMenuItems = function(returnValue) {
  return AI.Blockly.AIProcedure.getProcedureNames(returnValue, this.workspace_);
};

/**
 * Get a list of procedure definition blocks.
 *
 * @param {Boolean=false} returnValue Return procedure definition blocks with return values (true) or without return values (false)
 * @returns {!Blockly.Block[]}
 */
Blockly.ProcedureDatabase.prototype.getDeclarationBlocks = function(returnValue) {
  return goog.object.getValues(returnValue ? this.returnProcedures_: this.voidProcedures_);
};

Blockly.ProcedureDatabase.prototype.getDeclarationsBlocksExcept = function(block) {
  var blockArray = [];
  goog.object.forEach(this.procedures_, function(b) {
    if (b !== block) blockArray.push(b);
  });
  return blockArray;
};

Blockly.ProcedureDatabase.prototype.getAllDeclarationNames = function() {
  return goog.object.getValues(this.procedures_)
    .map(function(block) { return block.getFieldValue('NAME'); });
};

/**
 * Add a procedure to the database.
 *
 * @param {!string} name
 * @param {!Blockly.Block} block
 * @returns {boolean} true if the definition was added, otherwise false.
 */
Blockly.ProcedureDatabase.prototype.addProcedure = function(name, block) {
  if (block.type != 'procedures_defnoreturn' && block.type != 'procedures_defreturn') {
    // not a procedure block!
    console.warn('Attempt to addProcedure with block type ' + block.type);
    return false;
  }
  var id = block.id;
  if (id in this.procedures_) {
    return false;
  }
  this.procedures_[id] = block;
  this.length++;
  if (block.type == 'procedures_defnoreturn') {
    this.voidProcedures_[id] = block;
    this.voidProcedures++;
  } else {
    this.returnProcedures_[id] = block;
    this.returnProcedures++;
  }
  return true;
};

/**
 * Remove a procedure from the database.
 *
 * @param {!string} id
 */
Blockly.ProcedureDatabase.prototype.removeProcedure = function(id) {
  if (id in this.procedures_) {
    var block = this.procedures_[id];
    if (block.type == 'procedures_defnoreturn') {
      delete this.voidProcedures_[id];
      this.voidProcedures--;
    } else {
      delete this.returnProcedures_[id];
      this.returnProcedures--;
    }
    delete this.procedures_[id];
    this.length--;
  }
  return true;
};

/**
 * Rename a procedure in the database with the given oldNmae to newName.
 *
 * @param {!string} procId
 * @param {!string} oldName
 * @param {!string} newName
 * @returns {boolean} true if the procedure was renamed in the database, otherwise false.
 */
Blockly.ProcedureDatabase.prototype.renameProcedure = function(procId, oldName, newName) {
  /*
  if (newName in this.procedures_) {
    return false;
  }
  if (oldName in this.procedures_) {
    var block = this.procedures_[oldName];
    if (block.type == 'procedures_defnoreturn') {
      this.voidProcedures_[newName] = block;
      delete this.voidProcedures_[oldName];
    } else {
      this.returnProcedures_[newName] = block;
      delete this.returnProcedures_[oldName];
    }
    this.procedures_[newName] = block;
    delete this.procedures_[oldName];
    return true;
  } else {
    console.warn('Attempt to renameProcedure "' + oldName + '" not in the database.');
    return false;
  }
  */
};

/**
 * Get the procedure identified by {@link #id}. If the id does not identify a procedure, undefined
 * will be returned.
 *
 * @param {?string} id The procedure's id.
 * @returns {?Blockly.BlockSvg} The procedure block defining the procedure identified by {@link #id}
 */
Blockly.ProcedureDatabase.prototype.getProcedure = function(id) {
  var proc = this.procedures_[id];
  return proc ? proc : this.getProcedureByName(id);
};

Blockly.ProcedureDatabase.prototype.getProcedureByName = function(name) {
  for (var id in this.procedures_) {
    if (this.procedures_[id].getFieldValue('NAME') === name) {
      return this.procedures_[id];
    }
  }
  return undefined;
};

Blockly.ProcedureDatabase.prototype.clear = function() {
  this.procedures_ = {};
  this.returnProcedures_ = {};
  this.voidProcedures_ = {};
  this.length = 0;
  this.returnProcedures = 0;
  this.voidProcedures = 0;
};
