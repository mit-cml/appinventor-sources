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
 * @fileoverview Generating JavaScript for math blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.JavaScript = Blockly.Generator.get('JavaScript');

Blockly.JavaScript.math_number = function() {
  // Numeric value.
  return window.parseFloat(this.getTitleText(0));
};

Blockly.JavaScript.math_arithmetic = function(opt_dropParens) {
  // Basic arithmetic operators, and power.
  var argument0 = Blockly.JavaScript.valueToCode(this, 0) || '0';
  var argument1 = Blockly.JavaScript.valueToCode(this, 1) || '0';
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

Blockly.JavaScript.math_change = function() {
  // Add to a variable in place.
  var argument0 = Blockly.JavaScript.valueToCode(this, 0) || '0';
  var varName = Blockly.JavaScript.variableDB_.getName(this.getTitleText(1),
      Blockly.Variables.NAME_TYPE);
  return varName + ' = (typeof ' + varName + ' == \'number\' ? ' + varName + 
      ' : 0) + ' + argument0 + ';\n';
};

Blockly.JavaScript.math_single = function(opt_dropParens) {
  // Math operators with single operand.
  var argNaked = Blockly.JavaScript.valueToCode(this, 0, true) || '0';
  var argParen = Blockly.JavaScript.valueToCode(this, 0, false) || '0';
  var operator = this.getValueLabel(0);
  var code;
  // First, handle cases which generate values that don't need parentheses wrapping the code.
  switch (operator) {
    case this.MSG_ABS:
      code = 'Math.abs(' + argNaked + ')';
      break;
    case this.MSG_ROOT:
      code = 'Math.sqrt(' + argNaked + ')';
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
    case this.MSG_ROUND:
      code = 'Math.round(' + argNaked + ')';
      break;
    case this.MSG_ROUNDUP:
      code = 'Math.ceil(' + argNaked + ')';
      break;
    case this.MSG_ROUNDDOWN:
      code = 'Math.floor(' + argNaked + ')';
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
  }
  if (code) {
    return code;
  }
  // Second, handle cases which generate values that may need parentheses wrapping the code.
  switch (operator) {
    case this.MSG_NEG:
      code = '-' + argParen;
      break;
    case this.MSG_LOG10:
      code = 'Math.log(' + argNaked + ') / Math.log(10)';
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
    default:
      throw 'Unknown math operator.';
  }
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

// Rounding functions have a single operand.
Blockly.JavaScript.math_round = Blockly.JavaScript.math_single;
// Trigonometry functions have a single operand.
Blockly.JavaScript.math_trig = Blockly.JavaScript.math_single;

Blockly.JavaScript.math_on_list = function() {
  // Rounding functions.
  func = this.getTitleText(0);
  list = Blockly.JavaScript.valueToCode(this, 0, true) || '[]';
  var code;
  switch (func) {
    case this.MSG_SUM:
      code = list + '.reduce(function(x, y) {return x + y;})';
      break;
    case this.MSG_MIN:
      code = 'Math.min.apply(null,' + list + ')';
      break;
    case this.MSG_MAX:
      code = 'Math.max.apply(null,' + list + ')';
      break;
    case this.MSG_AVERAGE:
      code = '(' + list + '.reduce(function(x, y) {return x + y;})/' + list +
      '.length)';
      break;
    case this.MSG_MEDIAN:
      if (!Blockly.JavaScript.definitions_['math_median']) {
        var functionName = Blockly.JavaScript.variableDB_.getDistinctName(
            'math_median', Blockly.Generator.NAME_TYPE);
        Blockly.JavaScript.math_on_list.math_median = functionName;
        // Median is not a native JavaScript function.  Define one.
        // May need to handle null. 
        // Currently math_median([null,null,1,3]) == 0.5.
        var func = [];
        func.push('function ' + functionName + '(myList) {');
        func.push('  var localList = myList.filter(function (x) {return typeof x == \'number\';});');
        func.push('  if (!localList.length) return null;');
        func.push('  localList.sort(function(a, b) {return b - a;});');
        func.push('  if (localList.length % 2 == 0) {');
        func.push('    return (localList[localList.length / 2 - 1] + localList[localList.length / 2]) / 2;');
        func.push('  } else {');
        func.push('    return localList[(localList.length - 1) / 2];');
        func.push('  }');
        func.push('}');
        Blockly.JavaScript.definitions_['math_median'] = func.join('\n');
      }
      code = Blockly.JavaScript.math_on_list.math_median + '(' + list + ')';
      break;
    case this.MSG_MODE:
      if (!Blockly.JavaScript.definitions_['math_modes']) {
        var functionName = Blockly.JavaScript.variableDB_.getDistinctName(
            'math_modes', Blockly.Generator.NAME_TYPE);
        Blockly.JavaScript.math_on_list.math_modes = functionName;
        // As a list of numbers can contain more than one mode,
        // the returned result is provided as an array.
        // Mode of [3, 'x', 'x', 1, 1, 2, '3'] -> ['x', 1].
        var func = [];
        func.push('function ' + functionName + '(values) {');
        func.push('  var modes = [];');
        func.push('  var counts = [];');
        func.push('  var maxCount = 0;');
        func.push('  for (var i = 0; i < values.length; i++) {');
        func.push('    var value = values[i];');
        func.push('    var found = false;');
        func.push('    var thisCount;');
        func.push('    for (var j = 0; j < counts.length; j++) {');
        func.push('      if (counts[j][0] === value) {');
        func.push('        thisCount = ++counts[j][1];');
        func.push('        found = true;');
        func.push('        break;');
        func.push('      }');
        func.push('    }');
        func.push('    if (!found) {');
        func.push('      counts.push([value, 1]);');
        func.push('      thisCount = 1;');
        func.push('    }');
        func.push('    maxCount = Math.max(thisCount, maxCount);');
        func.push('  }');
        func.push('  for (var j = 0; j < counts.length; j++) {');
        func.push('    if (counts[j][1] == maxCount) {');
        func.push('        modes.push(counts[j][0]);');
        func.push('    }');
        func.push('  }');
        func.push('  return modes;');
        func.push('}');
        Blockly.JavaScript.definitions_['math_modes'] = func.join('\n');
      }
      code = Blockly.JavaScript.math_on_list.math_modes + '(' + list + ')';
      break;
    case this.MSG_STD_DEV:
      if (!Blockly.JavaScript.definitions_['math_standard_deviation']) {
        var functionName = Blockly.JavaScript.variableDB_.getDistinctName(
            'math_standard_deviation', Blockly.Generator.NAME_TYPE);
        Blockly.JavaScript.math_on_list.math_standard_deviation = functionName;
        var func = [];
        func.push('function ' + functionName + '(numbers) {');
        func.push('  var n = numbers.length;');
        func.push('  if (!n) return null;');
        func.push('  var mean = numbers.reduce(function(x, y) {return x + y;}) / n;');
        func.push('  var variance = 0;');
        func.push('  for (var j = 0; j < n; j++) {');
        func.push('    variance += Math.pow(numbers[j] - mean, 2);');
        func.push('  }');
        func.push('  variance = variance / n;');
        func.push('  standard_dev = Math.sqrt(variance);');
        func.push('  return standard_dev;');
        func.push('}');
        Blockly.JavaScript.definitions_['math_standard_deviation'] = func.join('\n');
      }
      code = Blockly.JavaScript.math_on_list.math_standard_deviation + '(' + list + ')';
      break;
    case this.MSG_RANDOM_ITEM:
      code = list + '[Math.floor(Math.random() * ' + list + '.length)]';
      break;
    default:
      throw 'Unknown operator.';
  }
  return code;
};

Blockly.JavaScript.math_constrain = function() {
  // Constrain a number between two limits.
  var argument0 = Blockly.JavaScript.valueToCode(this, 0, true) || '0';
  var argument1 = Blockly.JavaScript.valueToCode(this, 1, true) || '0';
  var argument2 = Blockly.JavaScript.valueToCode(this, 2, true) || '0';
  return 'Math.min(Math.max(' + argument0 + ', ' + argument1 + '), ' + argument2 + ')';
};

Blockly.JavaScript.math_modulo = function(opt_dropParens) {
  // Remainder computation.
  var argument0 = Blockly.JavaScript.valueToCode(this, 0) || '0';
  var argument1 = Blockly.JavaScript.valueToCode(this, 1) || '0';
  var code = argument0 + ' % ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.JavaScript.math_random_float = function() {
  return 'Math.random()';
};

Blockly.JavaScript.math_random_int = function() {
  var argument0 = Blockly.JavaScript.valueToCode(this, 0) || '0';
  var argument1 = Blockly.JavaScript.valueToCode(this, 1) || '0';
  var rand1 = 'Math.floor(Math.random() * (' + argument1 + ' - ' + argument0 + ' + 1' + ') + ' + argument0 + ')';
  var rand2 = 'Math.floor(Math.random() * (' + argument0 + ' - ' + argument1 + ' + 1' + ') + ' + argument1 + ')';
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
