// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Matrices blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author jackyc@mit.edu (Jacky Chen)
 */

'use strict';

goog.provide('Blockly.Yail.matrices');

Blockly.Yail['matrices_create'] = function() {
  var rows = this.getFieldValue('ROWS') || 2;
  var cols = this.getFieldValue('COLS') || 2;
  var matrixValues = this.matrixValues.flat();

  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-matrix" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code += rows + Blockly.Yail.YAIL_SPACER + cols;
  
  for (var x = 0; x < matrixValues.length; x++) {
    code += Blockly.Yail.YAIL_SPACER + matrixValues[x];
  }

  code += Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code += "number number";
  for (var x = 0; x < matrixValues.length; x++) {
    code += " number";
  }
  code += Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_DOUBLE_QUOTE + "create a matrix" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;

  return [code, Blockly.Yail.ORDER_ATOMIC];
};


Blockly.Yail['matrices_get_row'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'MATRIX', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var argument1 = Blockly.Yail.valueToCode(this, 'ROW', Blockly.Yail.ORDER_NONE) || 1;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-row" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code += argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_DOUBLE_QUOTE + "get matrix row" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['matrices_get_column'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'MATRIX', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var argument1 = Blockly.Yail.valueToCode(this, 'COLUMN', Blockly.Yail.ORDER_NONE) || 1;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-column" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code += argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_DOUBLE_QUOTE + "get matrix column" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['matrices_get_cell'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'MATRIX', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var argument1 = Blockly.Yail.valueToCode(this, 'ROW', Blockly.Yail.ORDER_NONE) || 1;
  var argument2 = Blockly.Yail.valueToCode(this, 'COLUMN', Blockly.Yail.ORDER_NONE) || 1;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-get-cell" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code += argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + argument2 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number number" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_DOUBLE_QUOTE + "get matrix cell" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['matrices_set_cell'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'MATRIX', Blockly.Yail.ORDER_NONE);
  var argument1 = Blockly.Yail.valueToCode(this, 'ROW', Blockly.Yail.ORDER_NONE);
  var argument2 = Blockly.Yail.valueToCode(this, 'COLUMN', Blockly.Yail.ORDER_NONE);
  var argument3 = Blockly.Yail.valueToCode(this, 'VALUE', Blockly.Yail.ORDER_NONE);
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-matrix-set-cell!" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code += argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + argument2 + Blockly.Yail.YAIL_SPACER + argument3 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code += "matrix number number number" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_DOUBLE_QUOTE + "set matrix cell" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};
