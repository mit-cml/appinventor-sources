// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Logic blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.logic');

AI.Yail['logic_boolean'] = function() {
  // Boolean values true and false.
  var code = (this.getFieldValue('BOOL') == 'TRUE') ? AI.Yail.YAIL_TRUE
      : AI.Yail.YAIL_FALSE;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['logic_false'] = function() {
  return AI.Yail.logic_boolean.call(this);
}

AI.Yail['logic_negate'] = function() {
  // negate operation
  var argument = AI.Yail
      .valueToCode(this, 'BOOL', AI.Yail.ORDER_NONE)
      || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-not"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "boolean"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "not"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['logic_operation'] = function() {
  // The and, or logic operations
  var mode = this.opField.getValue();
  var tuple = AI.Yail.logic_operation.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var defaultValue = tuple[2];
  var argument0 = AI.Yail.valueToCode(this, 'A', order) || defaultValue;
  var argument1 = AI.Yail.valueToCode(this, 'B', order) || defaultValue;
  var code = AI.Yail.YAIL_OPEN_COMBINATION + operator
      + AI.Yail.YAIL_SPACER + argument0 + AI.Yail.YAIL_SPACER
      + argument1;
  for (var i = 2; i < this.itemCount_; i++) {
    var arg = AI.Yail.valueToCode(this, this.repeatingInputName + i, order) || AI.Yail.YAIL_FALSE;
    code += AI.Yail.YAIL_SPACER + arg;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.logic_operation.OPERATORS = {
  AND : [ 'and-delayed', AI.Yail.ORDER_NONE, AI.Yail.YAIL_TRUE ],
  OR : [ 'or-delayed', AI.Yail.ORDER_NONE, AI.Yail.YAIL_FALSE ]
};

AI.Yail['logic_or'] = function() {
  return AI.Yail.logic_operation.call(this);
}

AI.Yail['logic_compare'] = function() {
  // Basic logic compare operators
  // // TODO: (Hal) handle any type?
  var argument0 = AI.Yail.valueToCode(this, 'A', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = AI.Yail.valueToCode(this, 'B', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var yailCommand = (this.getFieldValue('OP') == "NEQ" ? 'yail-not-equal?' : "yail-equal?" );
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + yailCommand
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "any" + AI.Yail.YAIL_SPACER
      + "any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "="
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};
