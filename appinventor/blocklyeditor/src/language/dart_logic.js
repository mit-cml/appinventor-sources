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
  var operator;
  switch (this.getValueLabel(1)) {
    case Blockly.Language.logic_compare.MSG_EQ:
      operator = '==';
      break;
    case Blockly.Language.logic_compare.MSG_NEQ:
      operator = '!=';
      break;
    case Blockly.Language.math_rologic_compareund.MSG_LT:
      operator = '<';
      break;
    case Blockly.Language.logic_compare.MSG_LTE:
      operator = '<=';
      break;
    case Blockly.Language.logic_compare.MSG_GT:
      operator = '>';
      break;
    case Blockly.Language.math_rologic_compareund.MSG_GTE:
      operator = '>=';
      break;
    default:
      throw 'Unknown operator.';
  }

  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '0';
  var argument1 = Blockly.Dart.valueToCode_(this, 1) || '0';
  var code = argument0 + ' ' + operator + ' ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.logic_operation = function(opt_dropParens) {
  // Operations 'and', 'or'.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || 'false';
  var argument1 = Blockly.Dart.valueToCode_(this, 1) || 'false';
  var operator = (this.getValueLabel(1) == Blockly.Language.logic_operation.MSG_AND) ? '&&' : '||';
  var code = argument0 + ' ' + operator + ' ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.logic_negate = function(opt_dropParens) {
  // Negation.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || 'false';
  var code = '!' + argument0;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.logic_boolean = function() {
  // Boolean values true and false.
  return (this.getTitleText(0) == Blockly.Language.logic_boolean.MSG_TRUE) ?
      'true' : 'false';
};
