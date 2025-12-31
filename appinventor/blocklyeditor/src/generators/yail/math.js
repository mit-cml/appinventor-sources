// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Math blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.math');

AI.Yail.forBlock['math_number'] = function(block, generator) {
  // Use Number() instead of parseFloat because it automatically
  // converts hex, binary, and octal to decimal.
  var code = Number(block.getFieldValue('NUM'));
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['math_number_radix'] = function(block, generator) {
  var prefix = Blockly.Blocks.math_number_radix.PREFIX[block.getFieldValue('OP')];
  var code = Number(prefix + block.getFieldValue('NUM'));
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['math_compare'] = function(block, generator) {
  // Basic compare operators
  var mode = block.getFieldValue('OP');
  var prim = AI.Yail.forBlock['math_compare'].OPERATORS[mode];
  var operator1 = prim[0];
  var operator2 = prim[1];
  var order = prim[2];
  var argument0 = generator.valueToCode(block, 'A', order) || 0;
  var argument1 = generator.valueToCode(block, 'B', order) || 0;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + (mode == "EQ" || mode == "NEQ" ? "any any" : "number number" )
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['math_compare'].OPERATORS = {
  EQ: ['yail-equal?', '=', AI.Yail.ORDER_NONE],
  NEQ: ['yail-not-equal?', 'not =', AI.Yail.ORDER_NONE],
  LT: ['<', '<', AI.Yail.ORDER_NONE],
  LTE: ['<=', '<=', AI.Yail.ORDER_NONE],
  GT: ['>', '>', AI.Yail.ORDER_NONE],
  GTE: ['>=', '>=', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_arithmetic'] = function(mode,block, generator) {
  // Basic arithmetic operators.
  var tuple = AI.Yail.forBlock['math_arithmetic'].OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var argument0 = generator.valueToCode(block, 'A', order) || 0;
  var argument1 = generator.valueToCode(block, 'B', order) || 0;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION;
  code += (mode == "EQ" || mode == "NEQ" ? "any any" : "number number" )
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['math_subtract'] = function(block, generator) {
  return AI.Yail.forBlock['math_arithmetic']("MINUS",block, generator);
};

AI.Yail.forBlock['math_division'] = function(block, generator) {
  return AI.Yail.forBlock['math_arithmetic']("DIVIDE",block, generator);
};

AI.Yail.forBlock['math_power'] = function(block, generator) {
  return AI.Yail.forBlock['math_arithmetic']("POWER",block, generator);
};

AI.Yail.forBlock['math_add'] = function(block, generator) {
  return AI.Yail.forBlock['math_arithmetic_list']("ADD",block, generator);
};

AI.Yail.forBlock['math_multiply'] = function(block, generator) {
  return AI.Yail.forBlock['math_arithmetic_list']("MULTIPLY",block, generator);
};

AI.Yail.forBlock['math_arithmetic_list'] = function(mode,block, generator) {
  // Basic arithmetic operators.
  //var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_arithmetic'].OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  for(var i=0;i<block.itemCount_;i++) {
    var argument = generator.valueToCode(block, 'NUM' + i, order) || 0;
    code += argument + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION;
  for(var i=0;i<block.itemCount_;i++) {
    code += "number" + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['math_arithmetic'].OPERATORS = {
  ADD: ['+', AI.Yail.ORDER_NONE],
  MINUS: ['-', AI.Yail.ORDER_NONE],
  MULTIPLY: ['*', AI.Yail.ORDER_NONE],
  DIVIDE: ['yail-divide', AI.Yail.ORDER_NONE],
  POWER: ['expt', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_bitwise'] = function(block, generator) {
  // Bitwise and, inclusive or, and exclusive or. All can take variable number of arguments.
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_bitwise'].OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var args = "";
  var typeString = "";
  for(var i=0;i<block.itemCount_;i++) {
    args += (generator.valueToCode(block, 'NUM' + i, order) || 0) + AI.Yail.YAIL_SPACER;
    typeString += "number" + AI.Yail.YAIL_SPACER;
  }
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + args
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + typeString
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_bitwise'].OPERATORS = {
    BITAND: ['bitwise-and', AI.Yail.ORDER_NONE],
    BITIOR: ['bitwise-ior', AI.Yail.ORDER_NONE],
    BITXOR: ['bitwise-xor', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_single'] = function(block, generator) {
  // Basic arithmetic operators.
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_single'].OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = generator.valueToCode(block, 'NUM', order) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['math_single'].OPERATORS = {
  ROOT: ['sqrt', 'sqrt', AI.Yail.ORDER_NONE],
  ABS: ['abs', 'abs', AI.Yail.ORDER_NONE],
  NEG: ['-', 'negate', AI.Yail.ORDER_NONE],
  LN: ['log', 'log', AI.Yail.ORDER_NONE],
  EXP: ['exp', 'exp', AI.Yail.ORDER_NONE],
  ROUND: ['yail-round', 'round', AI.Yail.ORDER_NONE],
  CEILING: ['yail-ceiling', 'ceiling', AI.Yail.ORDER_NONE],
  FLOOR: ['yail-floor', 'floor', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_abs'] = function(block, generator) {
  return AI.Yail.forBlock['math_single'](block, generator);
};

AI.Yail.forBlock['math_neg'] = function(block, generator) {
  return AI.Yail.forBlock['math_single'](block, generator);
};

AI.Yail.forBlock['math_round'] = function(block, generator) {
  return AI.Yail.forBlock['math_single'](block, generator);
};

AI.Yail.forBlock['math_ceiling'] = function(block, generator) {
  return AI.Yail.forBlock['math_single'](block, generator);
};

AI.Yail.forBlock['math_floor'] = function(block, generator) {
  return AI.Yail.forBlock['math_single'](block, generator);
};


AI.Yail.forBlock['math_random_int'] = function(block, generator) {
  // Random integer between [X] and [Y].
  var argument0 = generator.valueToCode(block, 'FROM',
    AI.Yail.ORDER_NONE) || 0;
  var argument1 = generator.valueToCode(block, 'TO',
    AI.Yail.ORDER_NONE) || 0;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "random-integer"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "random integer"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_random_float'] = function(block, generator) {
  // Random fraction between 0 and 1.
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "random-fraction"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR +
  AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE +
  AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER
      + AI.Yail.YAIL_DOUBLE_QUOTE + "random fraction"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_random_set_seed'] = function(block, generator) {
  // Basic is_a_number.
  var argument = generator.valueToCode(block, 'NUM', AI.Yail.ORDER_NONE) || 0;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "random-set-seed"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "random set seed"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail.forBlock['math_on_list'] = function(block, generator) {
  // Min and Max operators.
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_on_list'].OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var identity = tuple[2];
  var args = "";
  var typeString = "";
  for(var i=0;i<block.itemCount_;i++) {
    args += (generator.valueToCode(block, 'NUM' + i, order) || 0) + AI.Yail.YAIL_SPACER;
    typeString += "number" + AI.Yail.YAIL_SPACER;
  }
  if (block.itemCount_ === 0) {
    args += identity + AI.Yail.YAIL_SPACER;
    typeString += "number" + AI.Yail.YAIL_SPACER;
  }
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + args//argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + typeString
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_on_list'].OPERATORS = {
  MIN: ['min', AI.Yail.ORDER_NONE, '+inf.0'],
  MAX: ['max', AI.Yail.ORDER_NONE, '-inf.0']
};

AI.Yail.forBlock['math_on_list2'] = function(block, generator) {
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_on_list2'].OPERATORS[mode];
  var operator = tuple[0];
  var args = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_YAIL_LIST;

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + args
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "list-of-number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_on_list2'].OPERATORS = {
  AVG: ['avg', AI.Yail.ORDER_NONE],
  MIN: ['minl', AI.Yail.ORDER_NONE],
  MAX: ['maxl', AI.Yail.ORDER_NONE],
  //MODE: ['mode', AI.Yail.ORDER_NONE],
  GM: ['gm', AI.Yail.ORDER_NONE],
  SD: ['std-dev', AI.Yail.ORDER_NONE],
  SE: ['std-err', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_mode_of_list'] = function(block, generator) {
  var args = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_YAIL_LIST;

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + 'mode'
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + args
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "list-of-number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + 'mode'
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_divide'] = function(block, generator) {
  // divide operators.
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_divide'].OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var argument0 = generator.valueToCode(block, 'DIVIDEND', order) || 0;
  var argument1 = generator.valueToCode(block, 'DIVISOR', order) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_divide'].OPERATORS = {
  MODULO: ['modulo', AI.Yail.ORDER_NONE],
  REMAINDER: ['remainder', AI.Yail.ORDER_NONE],
  QUOTIENT: ['quotient', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_trig'] = function(block, generator) {
  // Basic trig operators.
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_trig'].OPERATORS[mode];
  var operator1 = tuple[1];
  var operator2 = tuple[0];
  var order = tuple[2];
  var argument = generator.valueToCode(block, 'NUM', order) || 0;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_trig'].OPERATORS = {
  SIN: ['sin', 'sin-degrees', AI.Yail.ORDER_NONE],
  COS: ['cos', 'cos-degrees', AI.Yail.ORDER_NONE],
  TAN: ['tan', 'tan-degrees', AI.Yail.ORDER_NONE],
  ASIN: ['asin', 'asin-degrees', AI.Yail.ORDER_NONE],
  ACOS: ['acos', 'acos-degrees', AI.Yail.ORDER_NONE],
  ATAN: ['atan', 'atan-degrees', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_cos'] = function(block, generator) {
  return AI.Yail.forBlock['math_trig'](block, generator);
};

AI.Yail.forBlock['math_tan'] = function(block, generator) {
  return AI.Yail.forBlock['math_trig'](block, generator);
};

AI.Yail.forBlock['math_atan2'] = function(block, generator) {
  // atan2 operators.
  var argument0 = generator.valueToCode(block, 'Y', AI.Yail.ORDER_NONE) || 1;
  var argument1 = generator.valueToCode(block, 'X', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "atan2-degrees"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "atan2"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_convert_angles'] = function(block, generator) {
  // Basic arithmetic operators.
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_convert_angles'].OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = generator.valueToCode(block, 'NUM', order) || 0;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_convert_angles'].OPERATORS = {
  RADIANS_TO_DEGREES: ['radians->degrees', 'convert radians to degrees', AI.Yail.ORDER_NONE],
  DEGREES_TO_RADIANS: ['degrees->radians', 'convert degrees to radians', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['math_format_as_decimal'] = function(block, generator) {
  // format_as_decimal.
  var argument0 = generator.valueToCode(block, 'NUM', AI.Yail.ORDER_NONE) || 0;
  var argument1 = generator.valueToCode(block, 'PLACES', AI.Yail.ORDER_NONE) || 0;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "format-as-decimal"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "format as decimal"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_is_a_number'] = function(block, generator) {
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_is_a_number'].OPERATORS[mode];
  var yailname = tuple[0];
  var description = tuple[1];
  var argument = generator.valueToCode(block, 'NUM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code ="(call-yail-primitive " + yailname + " (*list-for-runtime* " + argument +") " + description + ")";
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_is_a_number'].OPERATORS = {
  NUMBER: ['is-number?', "'(text) \"is a number?\""],
  BASE10: ['is-base10?', "'(text) \"is base10?\""],
  HEXADECIMAL: ['is-hexadecimal?', "'(text) \"is hexadecimal?\""],
  BINARY: ['is-binary?', "'(text) \"is binary?\""]
};

AI.Yail.forBlock['math_convert_number'] = function(block, generator) {
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['math_convert_number'].OPERATORS[mode];
  var yailname = tuple[0];
  var description = tuple[1];
  var argument0 = generator.valueToCode(block, 'NUM', AI.Yail.ORDER_NONE) || 0;
  var code ="(call-yail-primitive " + yailname + " (*list-for-runtime* " + argument0 +") " + description + ")";
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['math_convert_number'].OPERATORS = {
  DEC_TO_HEX: ['math-convert-dec-hex', "'(text) \"convert Dec to Hex\""],
  HEX_TO_DEC: ['math-convert-hex-dec', "'(text) \"convert Hex to Dec\""],
  DEC_TO_BIN: ['math-convert-dec-bin', "'(text) \"convert Dec to Hex\""],
  BIN_TO_DEC: ['math-convert-bin-dec', "'(text) \"convert Hex to Dec\""]
};
