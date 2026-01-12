// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Math blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.math');

AI.Yail['math_number'] = function() {
  // Use Number() instead of parseFloat because it automatically
  // converts hex, binary, and octal to decimal.
  var code = Number(this.getFieldValue('NUM'));
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['math_number_radix'] = function() {
  var prefix = Blockly.Blocks.math_number_radix.PREFIX[this.getFieldValue('OP')];
  var code = Number(prefix + this.getFieldValue('NUM'));
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['math_compare'] = function() {
  // Basic compare operators
  var mode = this.getFieldValue('OP');
  var prim = AI.Yail.math_compare.OPERATORS[mode];
  var operator1 = prim[0];
  var operator2 = prim[1];
  var order = prim[2];
  var argument0 = AI.Yail.valueToCode(this, 'A', order) || 0;
  var argument1 = AI.Yail.valueToCode(this, 'B', order) || 0;
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

AI.Yail.math_compare.OPERATORS = {
  EQ: ['yail-equal?', '=', AI.Yail.ORDER_NONE],
  NEQ: ['yail-not-equal?', 'not =', AI.Yail.ORDER_NONE],
  LT: ['<', '<', AI.Yail.ORDER_NONE],
  LTE: ['<=', '<=', AI.Yail.ORDER_NONE],
  GT: ['>', '>', AI.Yail.ORDER_NONE],
  GTE: ['>=', '>=', AI.Yail.ORDER_NONE]
};

AI.Yail['math_arithmetic'] = function(mode,block) {
  // Basic arithmetic operators.
  var tuple = AI.Yail.math_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var argument0 = AI.Yail.valueToCode(block, 'A', order) || 0;
  var argument1 = AI.Yail.valueToCode(block, 'B', order) || 0;
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

AI.Yail['math_subtract'] = function() {
  return AI.Yail.math_arithmetic("MINUS",this);
};

AI.Yail['math_division'] = function() {
  return AI.Yail.math_arithmetic("DIVIDE",this);
};

AI.Yail['math_power'] = function() {
  return AI.Yail.math_arithmetic("POWER",this);
};

AI.Yail['math_add'] = function() {
  return AI.Yail.math_arithmetic_list("ADD",this);
};

AI.Yail['math_multiply'] = function() {
  return AI.Yail.math_arithmetic_list("MULTIPLY",this);
};

AI.Yail['math_arithmetic_list'] = function(mode,block) {
  // Basic arithmetic operators.
  //var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  for(var i=0;i<block.itemCount_;i++) {
    var argument = AI.Yail.valueToCode(block, 'NUM' + i, order) || 0;
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

AI.Yail.math_arithmetic.OPERATORS = {
  ADD: ['+', AI.Yail.ORDER_NONE],
  MINUS: ['-', AI.Yail.ORDER_NONE],
  MULTIPLY: ['*', AI.Yail.ORDER_NONE],
  DIVIDE: ['yail-divide', AI.Yail.ORDER_NONE],
  POWER: ['expt', AI.Yail.ORDER_NONE]
};

AI.Yail['math_bitwise'] = function() {
  // Bitwise and, inclusive or, and exclusive or. All can take variable number of arguments.
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_bitwise.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var args = "";
  var typeString = "";
  for(var i=0;i<this.itemCount_;i++) {
    args += (AI.Yail.valueToCode(this, 'NUM' + i, order) || 0) + AI.Yail.YAIL_SPACER;
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

AI.Yail.math_bitwise.OPERATORS = {
    BITAND: ['bitwise-and', AI.Yail.ORDER_NONE],
    BITIOR: ['bitwise-ior', AI.Yail.ORDER_NONE],
    BITXOR: ['bitwise-xor', AI.Yail.ORDER_NONE]
};

AI.Yail['math_single'] = function() {
  // Basic arithmetic operators.
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_single.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = AI.Yail.valueToCode(this, 'NUM', order) || 1;
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

AI.Yail.math_single.OPERATORS = {
  ROOT: ['sqrt', 'sqrt', AI.Yail.ORDER_NONE],
  ABS: ['abs', 'abs', AI.Yail.ORDER_NONE],
  NEG: ['-', 'negate', AI.Yail.ORDER_NONE],
  LN: ['log', 'log', AI.Yail.ORDER_NONE],
  EXP: ['exp', 'exp', AI.Yail.ORDER_NONE],
  ROUND: ['yail-round', 'round', AI.Yail.ORDER_NONE],
  CEILING: ['yail-ceiling', 'ceiling', AI.Yail.ORDER_NONE],
  FLOOR: ['yail-floor', 'floor', AI.Yail.ORDER_NONE]
};

AI.Yail['math_abs'] = function() {
  return AI.Yail.math_single.call(this);
};

AI.Yail['math_neg'] = function() {
  return AI.Yail.math_single.call(this);
};

AI.Yail['math_round'] = function() {
  return AI.Yail.math_single.call(this);
};

AI.Yail['math_ceiling'] = function() {
  return AI.Yail.math_single.call(this);
};

AI.Yail['math_floor'] = function() {
  return AI.Yail.math_single.call(this);
};


AI.Yail['math_random_int'] = function() {
  // Random integer between [X] and [Y].
  var argument0 = AI.Yail.valueToCode(this, 'FROM',
    AI.Yail.ORDER_NONE) || 0;
  var argument1 = AI.Yail.valueToCode(this, 'TO',
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

AI.Yail['math_random_float'] = function() {
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

AI.Yail['math_random_set_seed'] = function() {
  // Basic is_a_number.
  var argument = AI.Yail.valueToCode(this, 'NUM', AI.Yail.ORDER_NONE) || 0;
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

AI.Yail['math_on_list'] = function() {
  // Min and Max operators.
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_on_list.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var identity = tuple[2];
  var args = "";
  var typeString = "";
  for(var i=0;i<this.itemCount_;i++) {
    args += (AI.Yail.valueToCode(this, 'NUM' + i, order) || 0) + AI.Yail.YAIL_SPACER;
    typeString += "number" + AI.Yail.YAIL_SPACER;
  }
  if (this.itemCount_ === 0) {
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

AI.Yail.math_on_list.OPERATORS = {
  MIN: ['min', AI.Yail.ORDER_NONE, '+inf.0'],
  MAX: ['max', AI.Yail.ORDER_NONE, '-inf.0']
};

AI.Yail['math_on_list2'] = function() {
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_on_list2.OPERATORS[mode];
  var operator = tuple[0];
  var args = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_YAIL_LIST;

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

AI.Yail.math_on_list2.OPERATORS = {
  AVG: ['avg', AI.Yail.ORDER_NONE],
  MIN: ['minl', AI.Yail.ORDER_NONE],
  MAX: ['maxl', AI.Yail.ORDER_NONE],
  //MODE: ['mode', AI.Yail.ORDER_NONE],
  GM: ['gm', AI.Yail.ORDER_NONE],
  SD: ['std-dev', AI.Yail.ORDER_NONE],
  SE: ['std-err', AI.Yail.ORDER_NONE]
};

AI.Yail['math_mode_of_list'] = function() {
  var args = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_YAIL_LIST;

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

AI.Yail['math_divide'] = function() {
  // divide operators.
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_divide.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var argument0 = AI.Yail.valueToCode(this, 'DIVIDEND', order) || 0;
  var argument1 = AI.Yail.valueToCode(this, 'DIVISOR', order) || 1;
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

AI.Yail.math_divide.OPERATORS = {
  MODULO: ['modulo', AI.Yail.ORDER_NONE],
  REMAINDER: ['remainder', AI.Yail.ORDER_NONE],
  QUOTIENT: ['quotient', AI.Yail.ORDER_NONE]
};

AI.Yail['math_trig'] = function() {
  // Basic trig operators.
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_trig.OPERATORS[mode];
  var operator1 = tuple[1];
  var operator2 = tuple[0];
  var order = tuple[2];
  var argument = AI.Yail.valueToCode(this, 'NUM', order) || 0;
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

AI.Yail.math_trig.OPERATORS = {
  SIN: ['sin', 'sin-degrees', AI.Yail.ORDER_NONE],
  COS: ['cos', 'cos-degrees', AI.Yail.ORDER_NONE],
  TAN: ['tan', 'tan-degrees', AI.Yail.ORDER_NONE],
  ASIN: ['asin', 'asin-degrees', AI.Yail.ORDER_NONE],
  ACOS: ['acos', 'acos-degrees', AI.Yail.ORDER_NONE],
  ATAN: ['atan', 'atan-degrees', AI.Yail.ORDER_NONE]
};

AI.Yail['math_cos'] = function() {
  return AI.Yail.math_trig.call(this);
};

AI.Yail['math_tan'] = function() {
  return AI.Yail.math_trig.call(this);
};

AI.Yail['math_atan2'] = function() {
  // atan2 operators.
  var argument0 = AI.Yail.valueToCode(this, 'Y', AI.Yail.ORDER_NONE) || 1;
  var argument1 = AI.Yail.valueToCode(this, 'X', AI.Yail.ORDER_NONE) || 1;
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

AI.Yail['math_convert_angles'] = function() {
  // Basic arithmetic operators.
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_convert_angles.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = AI.Yail.valueToCode(this, 'NUM', order) || 0;
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

AI.Yail.math_convert_angles.OPERATORS = {
  RADIANS_TO_DEGREES: ['radians->degrees', 'convert radians to degrees', AI.Yail.ORDER_NONE],
  DEGREES_TO_RADIANS: ['degrees->radians', 'convert degrees to radians', AI.Yail.ORDER_NONE]
};

AI.Yail['math_format_as_decimal'] = function() {
  // format_as_decimal.
  var argument0 = AI.Yail.valueToCode(this, 'NUM', AI.Yail.ORDER_NONE) || 0;
  var argument1 = AI.Yail.valueToCode(this, 'PLACES', AI.Yail.ORDER_NONE) || 0;
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

AI.Yail['math_is_a_number'] = function() {
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_is_a_number.OPERATORS[mode];
  var yailname = tuple[0];
  var description = tuple[1];
  var argument = AI.Yail.valueToCode(this, 'NUM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code ="(call-yail-primitive " + yailname + " (*list-for-runtime* " + argument +") " + description + ")";
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.math_is_a_number.OPERATORS = {
  NUMBER: ['is-number?', "'(text) \"is a number?\""],
  BASE10: ['is-base10?', "'(text) \"is base10?\""],
  HEXADECIMAL: ['is-hexadecimal?', "'(text) \"is hexadecimal?\""],
  BINARY: ['is-binary?', "'(text) \"is binary?\""]
};

AI.Yail['math_convert_number'] = function() {
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.math_convert_number.OPERATORS[mode];
  var yailname = tuple[0];
  var description = tuple[1];
  var argument0 = AI.Yail.valueToCode(this, 'NUM', AI.Yail.ORDER_NONE) || 0;
  var code ="(call-yail-primitive " + yailname + " (*list-for-runtime* " + argument0 +") " + description + ")";
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.math_convert_number.OPERATORS = {
  DEC_TO_HEX: ['math-convert-dec-hex', "'(text) \"convert Dec to Hex\""],
  HEX_TO_DEC: ['math-convert-hex-dec', "'(text) \"convert Hex to Dec\""],
  DEC_TO_BIN: ['math-convert-dec-bin', "'(text) \"convert Dec to Hex\""],
  BIN_TO_DEC: ['math-convert-bin-dec', "'(text) \"convert Hex to Dec\""]
};
