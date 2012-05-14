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
 * @fileoverview Generating Dart for math blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Dart = Blockly.Generator.get('Dart');

Blockly.Dart.math_number = function() {
  // Numeric value.
  return window.parseFloat(this.getTitleText(0));
};

Blockly.Dart.math_arithmetic = function(opt_dropParens) {
  // Basic arithmetic operators, and power.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '0';
  var argument1 = Blockly.Dart.valueToCode_(this, 1) || '0';
  var code;
  
  if (this.getValueLabel(1) == this.MSG_POW) {
    code = 'Math.pow(' + argument0 + ', ' + argument1 + ')';
  } else {
    var map = {};
    map[this.MSG_ADD] = '+';
    map[this.MSG_MINUS] = '-';
    map[this.MSG_MULTIPLY] = '*';
    map[this.MSG_DIVIDE] = '/';
    var operator = map[this.getValueLabel(1)];
    code = argument0 + ' ' + operator + ' ' + argument1;
    if (!opt_dropParens) {
      code = '(' + code + ')';
    }
  }
  return code;
};

Blockly.Dart.math_change = function() {
  // Add to a variable in place.
  var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || '0';
  var varName = Blockly.Dart.variableDB_.getVariable(this.getTitleText(1));
  return varName + ' += ' + argument0 + ';\n';
};

Blockly.Dart.math_single = function(opt_dropParens) {
  // Advanced math operators with single operand.
  var argNaked = Blockly.Dart.valueToCode_(this, 0, true) || '0';
  var argParen = Blockly.Dart.valueToCode_(this, 0, false) || '0';
  var operator = this.getValueLabel(0);
  var code;
  // First, handle cases which generate values that don't need parentheses.
  switch (operator) {
    case this.MSG_ABS:
      if (!argNaked.match(/^[\w\.]+$/)) {
        // -4.abs() returns -4 in Dart due to strange order of operation choices.
        // Need to wrap non-trivial numbers in parentheses: (-4).abs()
        argNaked = '(' + argNaked + ')';
      }
      code = argNaked + '.abs()';
      break;
    case this.MSG_ROOT:
      code = 'Math.sqrt(' + argNaked + ')';
      break;
    case this.MSG_SIN:
      code = 'Math.sin(' + argParen + ' / 180 * Math.PI)';
      break;
    case this.MSG_COS:
      code = 'Math.cos(' + argParen + ' / 180 * Math.PI)';
      break;
    case this.MSG_TAN:
      code = 'Math.tan(' + argParen + ' / 180 * Math.PI)';
      break;
    case this.MSG_LN:
      code = 'Math.log(' + argNaked + ')';
      break;
    case this.MSG_EXP:
      code = 'Math.exp(' + argNaked + ')';
      break;
    case this.MSG_10POW:
      code = 'Math.pow(10,' + argNaked + ')';
      break;
  }
  if (code) {
    return code;
  }
  // Second, handle cases which generate values that may need parentheses.
  switch (operator) {
    case this.MSG_NEG:
      code = '-' + argParen;
      break;
    case this.MSG_ASIN:
      code = 'Math.asin(' + argNaked + ') / Math.PI * 180';
      break;
    case this.MSG_ACOS:
      code = 'Math.acos(' + argNaked + ') / Math.PI * 180';
      break;
    case this.MSG_ATAN:
      code = 'Math.atan(' + argNaked + ') / Math.PI * 180';
      break;
    case this.MSG_LOG10:
      code = 'Math.log(' + argNaked + ') / Math.log(10)';
      break;
    default:
      throw 'Unknown math operator.';
  }
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.math_modulo = function(opt_dropParens) {
  // Remainder computation.
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '0';
  var argument1 = Blockly.Dart.valueToCode_(this, 1) || '0';
  var code = argument0 + ' % ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.math_round = function() {
  // Rounding functions.
  var operator;
  switch (this.getValueLabel(0)) {
    case this.MSG_ROUND:
      operator = 'round';
      break;
    case this.MSG_ROUNDUP:
      operator = 'ceil';
      break;
    case this.MSG_ROUNDDOWN:
      operator = 'floor';
      break;
    default:
      throw 'Unknown operator.';
  }
  var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || '0';
  if (operator != 'round' && !argument0.match(/^[\w\.]+$/)) {
    // -1.49.ceil() returns -2 in Dart due to strange order of operation choices.
    // Need to wrap non-trivial numbers in parentheses: (-1.49).ceil().
    // Not needed in case of round().
    argument0 = '(' + argument0 + ')';
  }
  return argument0 + '.' + operator + '()';
};

Blockly.Dart.math_random_float = function() {
  return 'Math.random()';
};

Blockly.Dart.math_random_int = function() {
  var argument0 = Blockly.Dart.valueToCode_(this, 0) || '0';
  var argument1 = Blockly.Dart.valueToCode_(this, 1) || '0';
  var rand1 = '(Math.random()*(' + argument1 + '-' + argument0 + '+1' + ')+' + argument0 + ').floor()';
  var rand2 = '(Math.random()*(' + argument0 + '-' + argument1 + '+1' + ')+' + argument1 + ').floor()';
  var code;
  if (argument0.match(/^[\d\.]+$/) && argument1.match(/^[\d\.]+$/)) {
    if (parseFloat(argument0) < parseFloat(argument1)) {
      code = rand1;
    } else {
      code = rand2;
    }
  } else {
    code = argument0 + ' < ' + argument1 + ' ? ' + rand1 + ' : ' + rand2;
  }
  return code;
};
