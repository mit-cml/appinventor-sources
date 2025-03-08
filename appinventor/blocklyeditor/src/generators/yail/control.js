/**
 * Visual Blocks Language
 *
 * Copyright 2012 Massachusetts Institute of Technology. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview List generators for Blockly, modified for App Inventor
 * @author fraser@google.com (Neil Fraser)
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

/**
 * Lyn's History:
 * [lyn, 10/27/13] Modified for loop index variables to begin with YAIL_LOCAL_VAR_TAG (currently '$').
 *     At least on Kawa-legal first character is necessary to ensure AI identifiers
 *     satisfy Kawa's identifier rules.
 * [lyn, 01/15/2013] Added do_then_return, eval_but_ignore, and nothing.
 * [lyn, 12/27/2012] Made code generation of forRange and forEach consistent with parameter change.
 */

'use strict';

goog.provide('AI.Yail.control');

AI.Yail['controls_if'] = function() {

  var code = "";
  for(var i=0;i<this.elseifCount_ + 1;i++){
    var argument = AI.Yail.valueToCode(this, 'IF'+ i, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
    var branch = AI.Yail.statementToCode(this, 'DO'+ i) || AI.Yail.YAIL_FALSE;
    if(i != 0) {
      code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_BEGIN;
    }
    code += AI.Yail.YAIL_IF + argument + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_BEGIN
      + branch + AI.Yail.YAIL_CLOSE_COMBINATION;
  }
  if(this.elseCount_ == 1){
    var branch = AI.Yail.statementToCode(this, 'ELSE') || AI.Yail.YAIL_FALSE;
    code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_BEGIN + branch + AI.Yail.YAIL_CLOSE_COMBINATION;
  }

  for(var i=0;i<this.elseifCount_;i++){
    code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_CLOSE_COMBINATION;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// [lyn, 01/15/2013] Edited to make consistent with removal of "THEN-DO" and "ELSE-DO"
AI.Yail['controls_choose'] = function() {
  // Choose.
  var test = AI.Yail.valueToCode(this, 'TEST', AI.Yail.ORDER_NONE)  || AI.Yail.YAIL_FALSE;
  var thenReturn = AI.Yail.valueToCode(this, 'THENRETURN', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var elseReturn = AI.Yail.valueToCode(this, 'ELSERETURN', AI.Yail.ORDER_NONE)  || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_IF + test
             + AI.Yail.YAIL_SPACER +  thenReturn
             + AI.Yail.YAIL_SPACER +  elseReturn
             + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code,AI.Yail.ORDER_ATOMIC];
};

// [lyn, 12/27/2012]
AI.Yail['controls_forEach'] = function() {
  // For each loop.
  var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;

  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
  emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;


  var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR');
  var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
  var bodyCode = AI.Yail.statementToCode(this, 'DO', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
  return AI.Yail.YAIL_FOREACH + loopIndexName + AI.Yail.YAIL_SPACER
         + AI.Yail.YAIL_BEGIN + bodyCode + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER
         + listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
};

AI.Yail['controls_for_each_dict'] = function() {
  var yail = AI.Yail;
  var generator = AI.Yail['controls_for_each_dict'];

  var prefix = Blockly.usePrefixInYail ? 'local_' : '';
  var keyName = yail.YAIL_LOCAL_VAR_TAG + prefix + this.getFieldValue('KEY');
  var valueName = yail.YAIL_LOCAL_VAR_TAG + prefix + this.getFieldValue('VALUE');

  var loopIndexName = 'item';
  var loopIndexCommandAndName = yail.getVariableCommandAndName(loopIndexName);
  loopIndexName = loopIndexCommandAndName[1];

  var getListCode = loopIndexCommandAndName[0] + loopIndexName +
      yail.YAIL_CLOSE_COMBINATION;
  var getKeyCode = generator.generateGetListItemCode(getListCode, 1);
  var getValueCode = generator.generateGetListItemCode(getListCode, 2);
  var setKeyCode = generator.generateSetVarCode(keyName, getKeyCode);
  var setValueCode = generator.generateSetVarCode(valueName, getValueCode);
  var letCode = yail.YAIL_LET + yail.YAIL_OPEN_COMBINATION + yail.YAIL_SPACER
      + setKeyCode + yail.YAIL_SPACER + setValueCode
      + yail.YAIL_CLOSE_COMBINATION;
  var bodyCode = yail.statementToCode(this, 'DO') || yail.YAIL_FALSE;
  var dictionaryCode = yail.valueToCode(this, 'DICT', yail.ORDER_NONE)
      || yail.YAIL_EMPTY_DICT;

  return yail.YAIL_FOREACH + loopIndexName + yail.YAIL_SPACER
      + letCode + bodyCode + yail.YAIL_CLOSE_COMBINATION
      + yail.YAIL_SPACER + dictionaryCode + yail.YAIL_CLOSE_COMBINATION;
};

AI.Yail['controls_for_each_dict'].generateGetListItemCode =
  function(getListCode, index) {
    var yail = AI.Yail;
    return yail.YAIL_CALL_YAIL_PRIMITIVE + 'yail-list-get-item' + yail.YAIL_SPACER
        + yail.YAIL_OPEN_COMBINATION + yail.YAIL_LIST_CONSTRUCTOR
        + yail.YAIL_SPACER + getListCode
        + yail.YAIL_SPACER + index.toString() + yail.YAIL_CLOSE_COMBINATION
        + yail.YAIL_SPACER + yail.YAIL_QUOTE + yail.YAIL_OPEN_COMBINATION
        + 'list number' + yail.YAIL_CLOSE_COMBINATION + yail.YAIL_SPACER
        + yail.YAIL_DOUBLE_QUOTE + 'select list item' + yail.YAIL_DOUBLE_QUOTE
        + yail.YAIL_CLOSE_COMBINATION;
  };

AI.Yail['controls_for_each_dict'].generateSetVarCode =
    function(varName, getVarCode) {
      var yail = AI.Yail;
      return yail.YAIL_OPEN_COMBINATION
          + varName + yail.YAIL_SPACER + getVarCode
          + yail.YAIL_CLOSE_COMBINATION;
    };

// In general break could take a value to return from the loop, but
// none of our block language loops return values, so we won't use that capability.

// [hal, 1/20/2018]
AI.Yail['controls_break'] = function() {
  // generates the literal string: (break #f)
  // which if evaluated inside the body of a loop will call
  // the "break" function passed to the loop macro
  var code = AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_BREAK + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_FALSE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};


// [lyn, 12/27/2012]
AI.Yail['controls_forRange'] = function() {
  // For range loop.
  var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR');
  var startCode = AI.Yail.valueToCode(this, 'START', AI.Yail.ORDER_NONE) || 0;
  var endCode = AI.Yail.valueToCode(this, 'END', AI.Yail.ORDER_NONE) || 0;
  var stepCode = AI.Yail.valueToCode(this, 'STEP', AI.Yail.ORDER_NONE) || 0;
  var bodyCode = AI.Yail.statementToCode(this, 'DO', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  return AI.Yail.YAIL_FORRANGE + loopIndexName + AI.Yail.YAIL_SPACER
         + AI.Yail.YAIL_BEGIN + bodyCode + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER
         + startCode + AI.Yail.YAIL_SPACER
         + endCode + AI.Yail.YAIL_SPACER
         + stepCode + AI.Yail.YAIL_CLOSE_COMBINATION;
};

AI.Yail['for_lexical_variable_get'] = function() {
  return AI.Yail.lexical_variable_get.call(this);
}

AI.Yail['controls_while'] = function() {
  // While condition.
  var test = AI.Yail.valueToCode(this, 'TEST', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var toDo = AI.Yail.statementToCode(this, 'DO') || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_WHILE + test + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_BEGIN + toDo + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// [lyn, 01/15/2013] Added
AI.Yail['controls_do_then_return'] = function() {
  var stm = AI.Yail.statementToCode(this, 'STM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var value = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_BEGIN + stm + AI.Yail.YAIL_SPACER + value + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

 // [lyn, 01/15/2013] Added
// adding 'ignored' here is only for the printout in Do-It.  The value will be ignored because the block shape
// has no output
AI.Yail['controls_eval_but_ignore'] = function() {
  var toEval = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_BEGIN + toEval + AI.Yail.YAIL_SPACER + '"ignored"' + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// [lyn, 01/15/2013] Added
AI.Yail['controls_nothing'] = function() {
  return ['*the-null-value*', AI.Yail.ORDER_NONE];
};

AI.Yail['controls_openAnotherScreen'] = function() {
  // Open another screen
  var argument0 = AI.Yail.valueToCode(this, 'SCREEN', AI.Yail.ORDER_NONE) || null;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "open-another-screen" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "open another screen" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['controls_openAnotherScreenWithStartValue'] = function() {
  // Open another screen with start value
  var argument0 = AI.Yail.valueToCode(this, 'SCREENNAME', AI.Yail.ORDER_NONE) || null;
  var argument1 = AI.Yail.valueToCode(this, 'STARTVALUE', AI.Yail.ORDER_NONE) || null;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "open-another-screen-with-start-value" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "open another screen with start value" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['controls_getStartValue'] = function() {
  // Get start value
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "get-start-value" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "get start value" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['controls_closeScreen'] = function() {
  // Close screen
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-screen" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "close screen" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['controls_closeScreenWithValue'] = function() {
  // Close screen with value
  var argument0 = AI.Yail.valueToCode(this, 'SCREEN', AI.Yail.ORDER_NONE) || null;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-screen-with-value" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "close screen with value" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['controls_closeApplication'] = function() {
  // Close application
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-application" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "close application" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['controls_getPlainStartText'] = function() {
  // Get plain start text
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "get-plain-start-text" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "get plain start text" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['controls_closeScreenWithPlainText'] = function() {
  // Close screen with plain text
  var argument0 = AI.Yail.valueToCode(this, 'TEXT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-screen-with-plain-text" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "close screen with plain text" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};
