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
 * @fileoverview Generating Yail for logic blocks.
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 */

Blockly.Yail = Blockly.Generator.get('Yail');

Blockly.Yail.logic_boolean = function() {
  // Boolean values true and false.
  var code = (this.getTitleValue('BOOL') == 'TRUE') ? Blockly.Yail.YAIL_TRUE
      : Blockly.Yail.YAIL_FALSE;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.logic_false = function() {
  return Blockly.Yail.logic_boolean.call(this);
}

Blockly.Yail.logic_negate = function() {
  // negate operation
  var argument = Blockly.Yail
      .valueToCode(this, 'BOOL', Blockly.Yail.ORDER_NONE)
      || Blockly.Yail.YAIL_FALSE;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-not"
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "boolean"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "not"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.logic_operation = function() {
  // The and, or logic operations
  // TODO: (Andrew) Make these take multiple arguments.
  var mode = this.getTitleValue('OP');
  var tuple = Blockly.Yail.logic_operation.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var argument0 = Blockly.Yail.valueToCode(this, 'A', order) || Blockly.Yail.YAIL_FALSE;
  var argument1 = Blockly.Yail.valueToCode(this, 'B', order) || Blockly.Yail.YAIL_FALSE;
  var code = Blockly.Yail.YAIL_OPEN_COMBINATION + operator
      + Blockly.Yail.YAIL_SPACER + argument0 + Blockly.Yail.YAIL_SPACER
      + argument1 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail.logic_operation.OPERATORS = {
  AND : [ 'and-delayed', Blockly.Yail.ORDER_NONE ],
  OR : [ 'or-delayed', Blockly.Yail.ORDER_NONE ]
};

Blockly.Yail.logic_or = function() {
  return Blockly.Yail.logic_operation.call(this);
}

Blockly.Yail.logic_compare = function() {
  // Basic logic compare operators
  // // TODO: (Hal) handle any type?
  var argument0 = Blockly.Yail.valueToCode(this, 'A', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var argument1 = Blockly.Yail.valueToCode(this, 'B', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var yailCommand = (this.getTitleValue('OP') == "NEQ" ? 'yail-not-equal?' : "yail-equal?" );
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + yailCommand
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_SPACER + argument1
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "any" + Blockly.Yail.YAIL_SPACER
      + "any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + "="
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};