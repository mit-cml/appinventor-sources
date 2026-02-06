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

AI.Yail.forBlock['matrices_create'] = function(block, generator) {
  var rows = block.getFieldValue('ROWS') || 2;
  var cols = block.getFieldValue('COLS') || 2;

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-matrix" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += rows + AI.Yail.YAIL_SPACER + cols;

  for (var i = 0; i < rows; i++) {
    for (var j = 0; j < cols; j++) {
      var fieldName = 'MATRIX_' + i + '_' + j;
      var value = block.getFieldValue(fieldName) || 0;
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

AI.Yail.forBlock['matrices_create_multidim'] = function(block, generator) {
  var dims = generator.valueToCode(block, 'DIM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var initial = generator.valueToCode(block, 'INITIAL', AI.Yail.ORDER_NONE) || '0';
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-matrix-multidim" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += dims + AI.Yail.YAIL_SPACER + initial;
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE;
  code += AI.Yail.YAIL_OPEN_COMBINATION + "list number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "create multidimensional matrix" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['matrices_get_row'] = function(block, generator) {
  var argument0 = generator.valueToCode(block, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var argument1 = generator.valueToCode(block, 'ROW', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-row" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix row" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['matrices_get_column'] = function(block, generator) {
  var argument0 = generator.valueToCode(block, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var argument1 = generator.valueToCode(block, 'COLUMN', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-column" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix column" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['matrices_get_cell'] = function(block, generator) {
  var matrix = generator.valueToCode(block, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-cell" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += matrix;
  for (var i = 0; i < block.itemCount_; i++) {
    var idx = generator.valueToCode(block, 'DIM' + i, AI.Yail.ORDER_NONE) || '1';
    code += AI.Yail.YAIL_SPACER + idx;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION + "matrix";
  for (var i = 0; i < block.itemCount_; i++) {
    code += " number";
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix cell" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['matrices_set_cell'] = function(block, generator) {
  var matrix = generator.valueToCode(block, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var value  = generator.valueToCode(block, 'VALUE',  AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-set-cell!" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += matrix + AI.Yail.YAIL_SPACER + value;
  for (var i = 0; i < block.itemCount_; i++) {
    var idx = generator.valueToCode(block, 'DIM' + i, AI.Yail.ORDER_NONE) || '1';
    code += AI.Yail.YAIL_SPACER + idx;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION + "matrix";
  for (var i = 0; i < block.itemCount_ + 1; i++) {
    code += " number";
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "set matrix cell" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code; 
};

AI.Yail.forBlock['matrices_get_dims'] = function(block, generator) {
  var argument = generator.valueToCode(block, 'MATRIX', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-dims" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION + "matrix" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "get matrix dimensions" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['matrices_operations'] = function(block, generator) {
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['matrices_operations'].OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = generator.valueToCode(block, 'MATRIX', order);
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

AI.Yail.forBlock['matrices_operations'].OPERATORS = {
  INVERSE: ['yail-matrix-inverse', 'inverse', AI.Yail.ORDER_NONE],
  TRANSPOSE: ['yail-matrix-transpose', 'transpose', AI.Yail.ORDER_NONE],
  ROTATE_LEFT: ['yail-matrix-rotate-left', 'rotate_left', AI.Yail.ORDER_NONE],
  ROTATE_RIGHT: ['yail-matrix-rotate-right', 'rotate_right', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['matrices_transpose'] = function(block, generator) {
  return AI.Yail.forBlock['matrices_operations'].call(block, block, generator);
};

AI.Yail.forBlock['matrices_rotate_left'] = function(block, generator) {
  return AI.Yail.forBlock['matrices_operations'].call(block, block, generator);
};

AI.Yail.forBlock['matrices_rotate_right'] = function(block, generator) {
  return AI.Yail.forBlock['matrices_operations'].call(block, block, generator);
};

AI.Yail.forBlock['matrices_arithmetic'] = function(block, generator, mode) {
  var tuple = AI.Yail.forBlock['matrices_arithmetic'].OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var argument0 = generator.valueToCode(block, 'A', order) || 0;
  var argument1 = generator.valueToCode(block, 'B', order) || 0;

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

AI.Yail.forBlock['matrices_subtract'] = function(block, generator) {
  return AI.Yail.forBlock['matrices_arithmetic'](block, generator, "MINUS");
};

AI.Yail.forBlock['matrices_power'] = function(block, generator) {
  return AI.Yail.forBlock['matrices_arithmetic'](block, generator, "POWER");
};

AI.Yail.forBlock['matrices_add'] = function(block, generator) {
  return AI.Yail.forBlock['matrices_arithmetic_list'](block, generator, "ADD");
};

AI.Yail.forBlock['matrices_multiply'] = function(block, generator) {
  return AI.Yail.forBlock['matrices_arithmetic_list'](block, generator, "MULTIPLY");
};

AI.Yail.forBlock['matrices_arithmetic_list'] = function(block, generator, mode) {
  var tuple = AI.Yail.forBlock['matrices_arithmetic'].OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var argCodes = [];
  for (var i = 0; i < block.itemCount_; i++) {
    var c = generator.valueToCode(block, 'MAT' + i, order) || 0;
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

AI.Yail.forBlock['matrices_arithmetic'].OPERATORS = {
  ADD: ['yail-matrix-add', AI.Yail.ORDER_NONE],
  MINUS: ['yail-matrix-subtract', AI.Yail.ORDER_NONE],
  MULTIPLY: ['yail-matrix-multiply', AI.Yail.ORDER_NONE],
  POWER: ['yail-matrix-power', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['matrices_is_matrix'] = function(block, generator) {
  var argument = generator.valueToCode(block, 'VALUE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix?" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION + "any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "is matrix?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};
