/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
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
 * @fileoverview Generating Python for logic blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Python = Blockly.Generator.get('Python');

Blockly.Python.logic_compare = function(opt_dropParens) {
  // Comparison operator.
  var mode = this.getInputLabelValue('B');
  var operator = Blockly.Python.logic_compare.OPERATORS[mode];
  var argument0 = Blockly.Python.valueToCode(this, 'A') || '0';
  var argument1 = Blockly.Python.valueToCode(this, 'B') || '0';
  var code = argument0 + ' ' + operator + ' ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Python.logic_compare.OPERATORS = {
  EQ: '==',
  NEQ: '!=',
  LT: '<',
  LTE: '<=',
  GT: '>',
  GTE: '>='
};

Blockly.Python.logic_operation = function(opt_dropParens) {
  // Operations 'and', 'or'.
  var argument0 = Blockly.Python.valueToCode(this, 'A') || 'False';
  var argument1 = Blockly.Python.valueToCode(this, 'B') || 'False';
  var operator = (this.getInputLabelValue('B') == 'AND') ? 'and' : 'or';
  var code = argument0 + ' ' + operator + ' ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Python.logic_negate = function(opt_dropParens) {
  // Negation.
  var argument0 = Blockly.Python.valueToCode(this, 'BOOL') || 'False';
  var code = 'not ' + argument0;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Python.logic_boolean = function() {
  // Boolean values true and false.
  return (this.getTitleValue('BOOL') == 'TRUE') ? 'True' : 'False';
};
