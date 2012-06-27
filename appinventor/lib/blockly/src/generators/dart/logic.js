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
 * @fileoverview Generating Dart for logic blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Dart = Blockly.Generator.get('Dart');

Blockly.Dart.logic_compare = function(opt_dropParens) {
  // Comparison operator.
  var mode = this.getInputLabelValue('B');
  var operator = Blockly.Dart.logic_compare.OPERATORS[mode];
  var argument0 = Blockly.Dart.valueToCode(this, 'A') || '0';
  var argument1 = Blockly.Dart.valueToCode(this, 'B') || '0';
  var code = argument0 + ' ' + operator + ' ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.logic_compare.OPERATORS = {
  EQ: '==',
  NEQ: '!=',
  LT: '<',
  LTE: '<=',
  GT: '>',
  GTE: '>='
};

Blockly.Dart.logic_operation = function(opt_dropParens) {
  // Operations 'and', 'or'.
  var argument0 = Blockly.Dart.valueToCode(this, 'A') || 'false';
  var argument1 = Blockly.Dart.valueToCode(this, 'B') || 'false';
  var operator = (this.getInputLabelValue('B') == 'AND') ? '&&' : '||';
  var code = argument0 + ' ' + operator + ' ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.logic_negate = function(opt_dropParens) {
  // Negation.
  var argument0 = Blockly.Dart.valueToCode(this, 'BOOL') || 'false';
  var code = '!' + argument0;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.logic_boolean = function() {
  // Boolean values true and false.
  return (this.getTitleValue('BOOL') == 'TRUE') ? 'true' : 'false';
};
