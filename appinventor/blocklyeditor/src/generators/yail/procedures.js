// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Procedure yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.procedures');

/**
 * Lyn's History:
 * [lyn, 10/29/13] Fixed bug in handling parameters of zero-arg procedures.
 * [lyn, 10/27/13] Modified procedure names to begin with YAIL_PROC_TAG (currently 'p$')
 *     and parameters to begin with YAIL_LOCAL_VAR_TAG (currently '$').
 *     At least on Kawa-legal first character is necessary to ensure AI identifiers
 *     satisfy Kawa's identifier rules. And the procedure 'p$' tag is necessary to
 *     distinguish procedures from globals (which use the 'g$' tag).
 * [lyn, 01/15/2013] Edited to remove STACK (no longer necessary with DO-THEN-RETURN)
 */

AI.Yail.YAIL_PROC_TAG = 'p$'; // See notes on this in generators/yail/variables.js

// Generator code for procedure call with return
// [lyn, 01/15/2013] Edited to remove STACK (no longer necessary with DO-THEN-RETURN)
AI.Yail['procedures_defreturn'] = function() {
  var argPrefix = AI.Yail.YAIL_LOCAL_VAR_TAG
                  + (Blockly.usePrefixInYail && this.arguments_.length != 0 ? "param_" : "");
  var args = this.arguments_.map(function (arg) {return argPrefix + arg;}).join(' ');
  var procName = AI.Yail.YAIL_PROC_TAG + this.getFieldValue('NAME');
  var returnVal = AI.Yail.valueToCode(this, 'RETURN', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_DEFINE + AI.Yail.YAIL_OPEN_COMBINATION + procName
      + AI.Yail.YAIL_SPACER + args + AI.Yail.YAIL_CLOSE_COMBINATION 
      + AI.Yail.YAIL_SPACER + returnVal + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// Generator code for procedure call with return
AI.Yail['procedures_defnoreturn'] = function() {
  var argPrefix = AI.Yail.YAIL_LOCAL_VAR_TAG
                  + (Blockly.usePrefixInYail && this.arguments_.length != 0 ? "param_" : "");
  var args = this.arguments_.map(function (arg) {return argPrefix + arg;}).join(' ');
  var procName = AI.Yail.YAIL_PROC_TAG + this.getFieldValue('NAME');
  var body = AI.Yail.statementToCode(this, 'STACK', AI.Yail.ORDER_NONE)  || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_DEFINE + AI.Yail.YAIL_OPEN_COMBINATION + procName
      + AI.Yail.YAIL_SPACER + args + AI.Yail.YAIL_CLOSE_COMBINATION + body
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// Generator code for yail procedure
AI.Yail['procedures_defanonnoreturn'] = function() {
  var argPrefix = AI.Yail.YAIL_LOCAL_VAR_TAG
                  + (Blockly.usePrefixInYail && this.arguments_.length != 0 ? "param_" : "");
  var args = this.getVars().map(function (arg) {return argPrefix + arg;}).join(' ');
  var body = AI.Yail.statementToCode(this, 'STACK', AI.Yail.ORDER_NONE)  || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YailCallYialPrimitive(
    "create-yail-procedure",
    AI.Yail.YAIL_LAMBDA
      + AI.Yail.YAIL_OPEN_COMBINATION + args + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER
      + body + AI.Yail.YAIL_CLOSE_COMBINATION,
    "any", "create procedure");
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

// Generator code for yail procedure (with return)
AI.Yail['procedures_defanonreturn'] = function() {
  var argPrefix = AI.Yail.YAIL_LOCAL_VAR_TAG
                  + (Blockly.usePrefixInYail && this.arguments_.length != 0 ? "param_" : "");
  var args = this.getVars().map(function (arg) {return argPrefix + arg;}).join(' ');
  var returnVal = AI.Yail.valueToCode(this, 'RETURN', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YailCallYialPrimitive(
    "create-yail-procedure",
    AI.Yail.YAIL_LAMBDA
      + AI.Yail.YAIL_OPEN_COMBINATION + args + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER
      + returnVal + AI.Yail.YAIL_CLOSE_COMBINATION,
    "any", "create procedure");
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['procedure_lexical_variable_get'] = function() {
  return AI.Yail.lexical_variable_get.call(this);
}

//call the do return in control category
AI.Yail['procedures_do_then_return'] = function() {
  return AI.Yail.controls_do_then_return.call(this);
}

// Generator code for procedure call with return
AI.Yail['procedures_callnoreturn'] = function() {
  var procName = AI.Yail.YAIL_PROC_TAG + this.getFieldValue('PROCNAME');
  var argCode = [];
  for ( var x = 0;this.getInput("ARG" + x); x++) {
    argCode[x] = AI.Yail.valueToCode(this, 'ARG' + x, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  }
  var code = AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_GET_VARIABLE + procName
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + argCode.join(' ')
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// Generator code for procedure call with return
AI.Yail['procedures_callreturn'] = function() {
  var procName = AI.Yail.YAIL_PROC_TAG + this.getFieldValue('PROCNAME');
  var argCode = [];
  for ( var x = 0; this.getInput("ARG" + x); x++) {
    argCode[x] = AI.Yail.valueToCode(this, 'ARG' + x, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  }
  var code = AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_GET_VARIABLE + procName
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + argCode.join(' ')
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

// Generator code for yail procedure call with no return
AI.Yail['procedures_callanonnoreturn'] = function() {
  var argCode = [];
  var argTypes = [];
  argCode.push(AI.Yail.valueToCode(this, 'PROCEDURE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE);
  argTypes.push("any");
  for (var x=0; this.getInput("ARG" + x); x++) {
    argCode.push(AI.Yail.valueToCode(this, 'ARG' + x, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE);
    argTypes.push('any');
  }
  var code = AI.Yail.YailCallYialPrimitive("call-yail-procedure", argCode, argTypes, "call procedure");
  return code;
};

// Generator code for yail procedure call with return
AI.Yail['procedures_callanonreturn'] = function() {
  var argCode = [];
  var argTypes = [];
  argCode.push(AI.Yail.valueToCode(this, 'PROCEDURE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE);
  argTypes.push("any");
  for (var x=0; this.getInput("ARG" + x); x++) {
    argCode.push(AI.Yail.valueToCode(this, 'ARG' + x, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE);
    argTypes.push('any');
  }
  var code = AI.Yail.YailCallYialPrimitive("call-yail-procedure", argCode, argTypes, "call procedure");
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

// Generator code for yail procedure call with no return (input list version)
AI.Yail['procedures_callanonnoreturn_inputlist'] = function() {
  var code = AI.Yail.YailCallYialPrimitive(
      "call-yail-procedure-input-list",
      [ AI.Yail.valueToCode(this, 'PROCEDURE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE,
        AI.Yail.valueToCode(this, 'INPUTLIST', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE ], 
      ["any","any"],
      "call procedure(with input list)");
  return code;
};

// Generator code for yail procedure call with return (input list version)
AI.Yail['procedures_callanonreturn_inputlist'] = function() {
  var code = AI.Yail.YailCallYialPrimitive(
      "call-yail-procedure-input-list",
      [ AI.Yail.valueToCode(this, 'PROCEDURE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE,
        AI.Yail.valueToCode(this, 'INPUTLIST', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE ], 
      ["any","any"],
      "call procedure(with input list)");
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['procedures_numArgs'] = function() {
  var code = AI.Yail.YailCallYialPrimitive(
      "num-args-yail-procedure",
      AI.Yail.valueToCode(this, 'PROCEDURE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE,
      "any", "get number of arguments");
  return [ code, AI.Yail.ORDER_ATOMIC ];
}

AI.Yail['procedures_getWithName'] = function() {
  var procName = AI.Yail.valueToCode(this, 'PROCEDURENAME', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YailCallYialPrimitive(
      "create-yail-procedure-with-name", procName, "any", "get procedure");
  return [ code, AI.Yail.ORDER_ATOMIC ];
}

AI.Yail['procedures_getWithDropdown'] = function() {
  var procedure = AI.Yail.YAIL_GET_VARIABLE +
      AI.Yail.YAIL_PROC_TAG + this.getFieldValue('PROCNAME') + AI.Yail.YAIL_CLOSE_COMBINATION;
  var code = AI.Yail.YailCallYialPrimitive(
    "create-yail-procedure", procedure, "any", "get procedure");
  return [ code, AI.Yail.ORDER_ATOMIC ];
}