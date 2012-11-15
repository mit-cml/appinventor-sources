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

if (!Blockly.Yail) Blockly.Yail = {};

Blockly.Yail = Blockly.Generator.get('Yail');

Blockly.Yail.controls_if = function() {

  var code = "";
  for(var i=0;i<this.elseifCount_ + 1;i++){
    var argument = Blockly.Yail.valueToCode(this, 'IF'+ i, Blockly.Yail.ORDER_NONE) || 'false';
    var branch = Blockly.Yail.statementToCode(this, 'DO'+ i);
    if(i != 0) {
      code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_BEGIN;
    }
    code += Blockly.Yail.YAIL_IF + argument + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_BEGIN
      + branch + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  }
  if(this.elseCount_ == 1){
    var branch = Blockly.Yail.statementToCode(this, 'ELSE');
    code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_BEGIN + branch + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  }
  for(var i=0;i<this.elseifCount_ + this.elseCount_ + 1;i++){
    code += Blockly.Yail.YAIL_CLOSE_COMBINATION;
  }

  return code;
};

Blockly.Yail.controls_ifelse = function() {
  // If-elseif-else condition.
  var argument = Blockly.Yail.valueToCode(this, 'IF', Blockly.Yail.ORDER_NONE) || 'false';
  var branch0 = Blockly.Yail.statementToCode(this, 'DO0');
  var branch1 = Blockly.Yail.statementToCode(this, 'DO1');
  var code = Blockly.Yail.YAIL_IF + argument + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_BEGIN
      + branch0 + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_BEGIN
      + branch1 + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_choose = function() {
  // Choose.
  var test = Blockly.Yail.valueToCode(this, 'CHOOSE', Blockly.Yail.ORDER_NONE) || 'false';
  var thenDo = Blockly.Yail.statementToCode(this, 'THEN', Blockly.Yail.ORDER_NONE) || 'null';
  var thenReturn = Blockly.Yail.valueToCode(this, 'THENRETURN', Blockly.Yail.ORDER_NONE) || 'null';
  var elseDo = Blockly.Yail.statementToCode(this, 'ELSE', Blockly.Yail.ORDER_NONE) || 'null';
  var elseReturn = Blockly.Yail.valueToCode(this, 'ELSERETURN', Blockly.Yail.ORDER_NONE) || 'null';
  var code = Blockly.Yail.YAIL_IF + test;
  code = code + Blockly.Yail.YAIL_SPACER +  Blockly.Yail.YAIL_BEGIN + thenDo + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER + thenReturn + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER +  Blockly.Yail.YAIL_BEGIN + elseDo + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER + elseReturn + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_forEach = function() {
  // For each loop.
  //TODO:(Andrew) When variables are implemented
  return "Waiting for variables to be implemented";
};

Blockly.Yail.controls_forRange = function() {
  // For range.
  //TODO:(Andrew) When variables are implemented
  return "Waiting for variables to be implemented";
};

Blockly.Yail.controls_while = function() {
  // While condition.
  var test = Blockly.Yail.valueToCode(this, 'TEST', Blockly.Yail.ORDER_NONE) || 'false';
  var toDo = Blockly.Yail.statementToCode(this, 'DO');
  var code = Blockly.Yail.YAIL_WHILE + test + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_BEGIN + toDo + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_openAnotherScreen = function() {
  // Open another screen
  var argument0 = Blockly.Yail.valueToCode(this, 'SCREEN', Blockly.Yail.ORDER_NONE) || null;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "open-another-screen" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "open another screen" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_openAnotherScreenWithStartValue = function() {
  // Open another screen with start value
  var argument0 = Blockly.Yail.valueToCode(this, 'SCREENNAME', Blockly.Yail.ORDER_NONE) || null;
  var argument1 = Blockly.Yail.valueToCode(this, 'STARTVALUE', Blockly.Yail.ORDER_NONE) || null;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "open-another-screen-with-start-value" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "open another screen with start value" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_getStartValue = function() {
  // Get start value
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "get-start-value" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "get start value" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.controls_closeScreen = function() {
  // Close screen
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-screen" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "close screen" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_closeScreenWithValue = function() {
  // Close screen with value
  var argument0 = Blockly.Yail.valueToCode(this, 'SCREEN', Blockly.Yail.ORDER_NONE) || null;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-screen-with-value" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "close screen with value" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_closeApplication = function() {
  // Close application
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-application" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "close application" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.controls_getPlainStartText = function() {
  // Get plain start text
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "get-plain-start-text" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "get plain start text" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.controls_closeScreenWithPlainText = function() {
  // Close screen with plain text
  var argument0 = Blockly.Yail.valueToCode(this, 'TEXT', Blockly.Yail.ORDER_NONE) || null;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "close-screen-with-plain-text" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "close screen with plain text" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};