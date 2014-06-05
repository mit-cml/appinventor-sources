// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Math blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Yail.math');

Blockly.Yail['math_number'] = function() {
  // Numeric value.
  var code = window.parseFloat(this.getFieldValue('NUM'));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['math_compare'] = function() {
  // Basic compare operators
  var mode = this.getFieldValue('OP');
  var prim = Blockly.Yail.math_compare.OPERATORS[mode];
  var operator1 = prim[0];
  var operator2 = prim[1];
  var order = prim[2];
  var argument0 = Blockly.Yail.valueToCode(this, 'A', order) || 0;
  var argument1 = Blockly.Yail.valueToCode(this, 'B', order) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + (mode == "EQ" || mode == "NEQ" ? "any any" : "number number" )
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator2
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail.math_compare.OPERATORS = {
  EQ: ['yail-equal?', '=', Blockly.Yail.ORDER_NONE],
  NEQ: ['yail-not-equal?', 'not =', Blockly.Yail.ORDER_NONE],
  LT: ['<', '<', Blockly.Yail.ORDER_NONE],
  LTE: ['<=', '<=', Blockly.Yail.ORDER_NONE],
  GT: ['>', '>', Blockly.Yail.ORDER_NONE],
  GTE: ['>=', '>=', Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['math_arithmetic'] = function(mode,block) {
  // Basic arithmetic operators.
  var tuple = Blockly.Yail.math_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1]; 
  var argument0 = Blockly.Yail.valueToCode(block, 'A', order) || 0;
  var argument1 = Blockly.Yail.valueToCode(block, 'B', order) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code += (mode == "EQ" || mode == "NEQ" ? "any any" : "number number" )
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['math_subtract'] = function() {
  return Blockly.Yail.math_arithmetic("MINUS",this);
};

Blockly.Yail['math_division'] = function() {
  return Blockly.Yail.math_arithmetic("DIVIDE",this);
};

Blockly.Yail['math_power'] = function() {
  return Blockly.Yail.math_arithmetic("POWER",this);
};

Blockly.Yail['math_add'] = function() {
  return Blockly.Yail.math_arithmetic_list("ADD",this);
};

Blockly.Yail['math_multiply'] = function() {
  return Blockly.Yail.math_arithmetic_list("MULTIPLY",this);
};

Blockly.Yail['math_arithmetic_list'] = function(mode,block) {
  // Basic arithmetic operators.
  //var mode = this.getFieldValue('OP');
  var tuple = Blockly.Yail.math_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];

  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  for(var i=0;i<block.itemCount_;i++) {
    var argument = Blockly.Yail.valueToCode(block, 'NUM' + i, order) || 0;
    code += argument + Blockly.Yail.YAIL_SPACER;
  }
  code += Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION;
  for(var i=0;i<block.itemCount_;i++) {
    code += "number" + Blockly.Yail.YAIL_SPACER;
  }
  code += Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail.math_arithmetic.OPERATORS = {
  ADD: ['+', Blockly.Yail.ORDER_NONE],
  MINUS: ['-', Blockly.Yail.ORDER_NONE],
  MULTIPLY: ['*', Blockly.Yail.ORDER_NONE],
  DIVIDE: ['/', Blockly.Yail.ORDER_NONE],
  POWER: ['expt', Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['math_single'] = function() {
  // Basic arithmetic operators.
  var mode = this.getFieldValue('OP');
  var tuple = Blockly.Yail.math_single.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = Blockly.Yail.valueToCode(this, 'NUM', order) || 1;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator2
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail.math_single.OPERATORS = {
  ROOT: ['sqrt', 'sqrt', Blockly.Yail.ORDER_NONE],
  ABS: ['abs', 'abs', Blockly.Yail.ORDER_NONE],
  NEG: ['-', 'negate', Blockly.Yail.ORDER_NONE],
  LN: ['log', 'log', Blockly.Yail.ORDER_NONE],
  EXP: ['exp', 'exp', Blockly.Yail.ORDER_NONE],
  ROUND: ['yail-round', 'round', Blockly.Yail.ORDER_NONE],
  CEILING: ['yail-ceiling', 'ceiling', Blockly.Yail.ORDER_NONE],
  FLOOR: ['yail-floor', 'floor', Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['math_abs'] = function() {
  return Blockly.Yail.math_single.call(this);
};

Blockly.Yail['math_neg'] = function() {
  return Blockly.Yail.math_single.call(this);
};

Blockly.Yail['math_round'] = function() {
  return Blockly.Yail.math_single.call(this);
};

Blockly.Yail['math_ceiling'] = function() {
  return Blockly.Yail.math_single.call(this);
};

Blockly.Yail['math_floor'] = function() {
  return Blockly.Yail.math_single.call(this);
};


Blockly.Yail['math_random_int'] = function() {
  // Random integer between [X] and [Y].
  var argument0 = Blockly.Yail.valueToCode(this, 'FROM',
    Blockly.Yail.ORDER_NONE) || 0;
  var argument1 = Blockly.Yail.valueToCode(this, 'TO',
    Blockly.Yail.ORDER_NONE) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "random-integer"
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "random integer"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['math_random_float'] = function() {
  // Random fraction between 0 and 1.
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "random-fraction"
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + 
  Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + 
  Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + "random fraction"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['math_random_set_seed'] = function() {
  // Basic is_a_number.
  var argument = Blockly.Yail.valueToCode(this, 'NUM', Blockly.Yail.ORDER_NONE) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "random-set-seed"
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "random set seed"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail['math_on_list'] = function() {
  // Min and Max operators.
  var mode = this.getFieldValue('OP');
  var tuple = Blockly.Yail.math_on_list.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1]; 
  var args = "";
  var typeString = "";
  for(var i=0;i<this.itemCount_;i++) {
    args += (Blockly.Yail.valueToCode(this, 'NUM' + i, order) || 0) + Blockly.Yail.YAIL_SPACER;
    typeString += "number" + Blockly.Yail.YAIL_SPACER;
  }
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + args//argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + typeString
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.math_on_list.OPERATORS = {
  MIN: ['min', Blockly.Yail.ORDER_NONE],
  MAX: ['max', Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['math_divide'] = function() {
  // divide operators.
  var mode = this.getFieldValue('OP');
  var tuple = Blockly.Yail.math_divide.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1]; 
  var argument0 = Blockly.Yail.valueToCode(this, 'DIVIDEND', order) || 0;
  var argument1 = Blockly.Yail.valueToCode(this, 'DIVISOR', order) || 1;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.math_divide.OPERATORS = {
  MODULO: ['modulo', Blockly.Yail.ORDER_NONE],
  REMAINDER: ['remainder', Blockly.Yail.ORDER_NONE],
  QUOTIENT: ['quotient', Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['math_trig'] = function() {
  // Basic trig operators.
  var mode = this.getFieldValue('OP');
  var tuple = Blockly.Yail.math_trig.OPERATORS[mode];
  var operator1 = tuple[1];
  var operator2 = tuple[0];
  var order = tuple[2];
  var argument = Blockly.Yail.valueToCode(this, 'NUM', order) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator2
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.math_trig.OPERATORS = {
  SIN: ['sin', 'sin-degrees', Blockly.Yail.ORDER_NONE],
  COS: ['cos', 'cos-degrees', Blockly.Yail.ORDER_NONE],
  TAN: ['tan', 'tan-degrees', Blockly.Yail.ORDER_NONE],
  ASIN: ['asin', 'asin-degrees', Blockly.Yail.ORDER_NONE],
  ACOS: ['acos', 'acos-degrees', Blockly.Yail.ORDER_NONE],
  ATAN: ['atan', 'atan-degrees', Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['math_cos'] = function() {
  return Blockly.Yail.math_trig.call(this);
};

Blockly.Yail['math_tan'] = function() {
  return Blockly.Yail.math_trig.call(this);
};

Blockly.Yail['math_atan2'] = function() {
  // atan2 operators.
  var argument0 = Blockly.Yail.valueToCode(this, 'Y', Blockly.Yail.ORDER_NONE) || 1;
  var argument1 = Blockly.Yail.valueToCode(this, 'X', Blockly.Yail.ORDER_NONE) || 1;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "atan2-degrees"
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "atan2"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['math_convert_angles'] = function() {
  // Basic arithmetic operators.
  var mode = this.getFieldValue('OP');
  var tuple = Blockly.Yail.math_convert_angles.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = Blockly.Yail.valueToCode(this, 'NUM', order) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator2
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.math_convert_angles.OPERATORS = {
  RADIANS_TO_DEGREES: ['radians->degrees', 'convert radians to degrees', Blockly.Yail.ORDER_NONE],
  DEGREES_TO_RADIANS: ['degrees->radians', 'convert degrees to radians', Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['math_format_as_decimal'] = function() {
  // format_as_decimal.
  var argument0 = Blockly.Yail.valueToCode(this, 'NUM', Blockly.Yail.ORDER_NONE) || 0;
  var argument1 = Blockly.Yail.valueToCode(this, 'PLACES', Blockly.Yail.ORDER_NONE) || 0;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "format-as-decimal"
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "format as decimal"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['math_is_a_number'] = function() {
  // Basic is_a_number.
  var argument = Blockly.Yail.valueToCode(this, 'NUM', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "is-number?"
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "any"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "is a number?"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};
