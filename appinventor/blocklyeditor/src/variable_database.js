/* -*- mode: javascript; js-indent-level: 2; -*- */
/**
 * @license
 * Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.
 */

/**
 * @fileoverview A database for tracking variables.
 * @author Evan W. Patton <ewpatton@mit.edu>
 */

goog.provide('AI.Blockly.VariableDatabase');

Blockly.VariableDatabase = function() {
  this.globals_ = {};
};

Blockly.VariableDatabase.prototype.getGlobalNames = function(opt_excludedBlock) {
  var globals = [];
  for (var name in this.globals_) {
    if (this.globals_.hasOwnProperty(name) && this.globals_[name] != opt_excludedBlock) {
      globals.push(name);
    }
  }
  return globals;
};

Blockly.VariableDatabase.prototype.getLexicalNamesInScope = function(block) {
  var child = block,
      parent;
  
};

Blockly.VariableDatabase.prototype.getNamesInScope = function(block) {
};

