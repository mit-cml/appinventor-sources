// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

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
  var argument0 = AI.Yail.valueToCode(this, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var argument1 = AI.Yail.valueToCode(this, 'ROW', AI.Yail.ORDER_NONE) || 1;
  var argument2 = AI.Yail.valueToCode(this, 'COLUMN', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-cell" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix cell" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['matrices_set_cell'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'MATRIX', AI.Yail.ORDER_NONE);
  var argument1 = AI.Yail.valueToCode(this, 'ROW', AI.Yail.ORDER_NONE);
  var argument2 = AI.Yail.valueToCode(this, 'COLUMN', AI.Yail.ORDER_NONE);
  var argument3 = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE);
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-set-cell!" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_SPACER + argument3 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number number number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
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
};

AI.Yail['matrices_transpose'] = function() {
  return AI.Yail.matrices_operations.call(this);
};

AI.Yail['matrices_arithmetic'] = function(mode, block) {
  var tuple = AI.Yail.matrices_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var argument0 = AI.Yail.valueToCode(block, 'A', order);
  var argument1 = AI.Yail.valueToCode(block, 'B', order);

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

AI.Yail['matrices_add'] = function() {
  return AI.Yail.matrices_arithmetic("ADD", this);
};

AI.Yail['matrices_subtract'] = function() {
  return AI.Yail.matrices_arithmetic("MINUS", this);
};

AI.Yail['matrices_multiply'] = function() {
  return AI.Yail.matrices_arithmetic("MULTIPLY", this);
};

AI.Yail['matrices_power'] = function() {
  return AI.Yail.matrices_arithmetic("POWER", this);
};

AI.Yail.matrices_arithmetic.OPERATORS = {
  ADD: ['yail-matrix-add', AI.Yail.ORDER_NONE],
  MINUS: ['yail-matrix-subtract', AI.Yail.ORDER_NONE],
  MULTIPLY: ['yail-matrix-multiply', AI.Yail.ORDER_NONE],
  POWER: ['yail-matrix-power', AI.Yail.ORDER_NONE]
};