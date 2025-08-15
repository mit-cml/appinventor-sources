// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @license
 * @fileoverview Matrices blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author jackyc@mit.edu (Jacky Chen)
 */

'use strict';

goog.provide('AI.Yail.matrices');

AI.Yail['matrices_create'] = function() {
  var rows = this.getFieldValue('ROWS') || 2;
  var cols = this.getFieldValue('COLS') || 2;

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-matrix" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += rows + AI.Yail.YAIL_SPACER + cols;

  for (var i = 0; i < rows; i++) {
    for (var j = 0; j < cols; j++) {
      var fieldName = 'MATRIX_' + i + '_' + j;
      var value = this.getFieldValue(fieldName) || 0;
      code += AI.Yail.YAIL_SPACER + value;
    }
  }

  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code += "number number";
  
  for (var i = 0; i < rows * cols; i++) {
    code += " number";
  }

  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "create a matrix" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;

  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['matrices_create_multidim'] = function() {
  var dims = AI.Yail.valueToCode(this, 'DIM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var initial = AI.Yail.valueToCode(this, 'INITIAL', AI.Yail.ORDER_NONE) || '0';
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-matrix-multidim" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += dims + AI.Yail.YAIL_SPACER + initial;
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE;
  code += AI.Yail.YAIL_OPEN_COMBINATION + "list number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "create multidimensional matrix" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['matrices_get_row'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var argument1 = AI.Yail.valueToCode(this, 'ROW', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-row" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix row" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['matrices_get_column'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var argument1 = AI.Yail.valueToCode(this, 'COLUMN', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-column" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix column" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['matrices_get_cell'] = function() {
  var matrix = AI.Yail.valueToCode(this, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-cell" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += matrix;
  for (var i = 0; i < this.itemCount_; i++) {
    var idx = AI.Yail.valueToCode(this, 'DIM' + i, AI.Yail.ORDER_NONE) || '1';
    code += AI.Yail.YAIL_SPACER + idx;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION + "matrix";
  for (var i = 0; i < this.itemCount_; i++) {
    code += " number";
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix cell" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['matrices_set_cell'] = function() {
  var matrix = AI.Yail.valueToCode(this, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var value  = AI.Yail.valueToCode(this, 'VALUE',  AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-set-cell!" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += matrix + AI.Yail.YAIL_SPACER + value;
  for (var i = 0; i < this.itemCount_; i++) {
    var idx = AI.Yail.valueToCode(this, 'DIM' + i, AI.Yail.ORDER_NONE) || '1';
    code += AI.Yail.YAIL_SPACER + idx;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION + "matrix";
  for (var i = 0; i < this.itemCount_ + 1; i++) {
    code += " number";
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "set matrix cell" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code; 
};

AI.Yail['matrices_operations'] = function() {
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.matrices_operations.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = AI.Yail.valueToCode(this, 'MATRIX', order);
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "matrix"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.matrices_operations.OPERATORS = {
  INVERSE: ['yail-matrix-inverse', 'inverse', AI.Yail.ORDER_NONE],
  TRANSPOSE: ['yail-matrix-transpose', 'transpose', AI.Yail.ORDER_NONE],
  ROTATE_LEFT: ['yail-matrix-rotate-left', 'rotate_left', AI.Yail.ORDER_NONE],
  ROTATE_RIGHT: ['yail-matrix-rotate-right', 'rotate_right', AI.Yail.ORDER_NONE]
};

AI.Yail['matrices_transpose'] = function() {
  return AI.Yail.matrices_operations.call(this);
};

AI.Yail['matrices_rotate_left'] = function() {
  return AI.Yail.matrices_operations.call(this);
};

AI.Yail['matrices_rotate_right'] = function() {
  return AI.Yail.matrices_operations.call(this);
};

AI.Yail['matrices_arithmetic'] = function(mode, block) {
  var tuple = AI.Yail.matrices_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var argument0 = AI.Yail.valueToCode(block, 'A', order) || 0;
  var argument1 = AI.Yail.valueToCode(block, 'B', order) || 0;

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1;
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  if (mode === "POWER") {
    code += "matrix number";
  } else if (mode === "MULTIPLY") {
    code += "matrix any";
  } else {
    code += "matrix matrix";
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + operator + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['matrices_subtract'] = function() {
  return AI.Yail.matrices_arithmetic("MINUS", this);
};

AI.Yail['matrices_power'] = function() {
  return AI.Yail.matrices_arithmetic("POWER", this);
};

AI.Yail['matrices_add'] = function() {
  return AI.Yail.matrices_arithmetic_list("ADD", this);
};

AI.Yail['matrices_multiply'] = function() {
  return AI.Yail.matrices_arithmetic_list("MULTIPLY", this);
};

AI.Yail['matrices_arithmetic_list'] = function(mode, block) {
  var tuple = AI.Yail.matrices_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var argCodes = [];
  for (var i = 0; i < block.itemCount_; i++) {
    var c = AI.Yail.valueToCode(block, 'MAT' + i, order) || 0;
    if (c) argCodes.push(c);
  }

  if (mode === 'ADD') {
    if (argCodes.length === 0) return ['0', order];
    if (argCodes.length === 1) return [argCodes[0], order];
  }
  if (mode === 'MULTIPLY') {
    if (block.itemCount_ === 0) return ['1', order];
    if (argCodes.length === 0) return ['0', order];
    if (argCodes.length === 1) return [argCodes[0], order];
  }

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argCodes.join(AI.Yail.YAIL_SPACER) +  AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  if (mode === "ADD") {
    for (var i = 0; i < argCodes.length; i++) {
      code += "matrix" + AI.Yail.YAIL_SPACER;
    }
  } else if (mode === "MULTIPLY") {
    code += "matrix" + AI.Yail.YAIL_SPACER;
    for (var i = 1; i < argCodes.length; i++) {
      code += "any" + AI.Yail.YAIL_SPACER;
    }
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + operator + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.matrices_arithmetic.OPERATORS = {
  ADD: ['yail-matrix-add', AI.Yail.ORDER_NONE],
  MINUS: ['yail-matrix-subtract', AI.Yail.ORDER_NONE],
  MULTIPLY: ['yail-matrix-multiply', AI.Yail.ORDER_NONE],
  POWER: ['yail-matrix-power', AI.Yail.ORDER_NONE]
};
