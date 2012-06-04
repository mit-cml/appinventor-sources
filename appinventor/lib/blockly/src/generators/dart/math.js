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
  var argument0 = Blockly.Dart.valueToCode(this, 0) || '0';
  var argument1 = Blockly.Dart.valueToCode(this, 1) || '0';
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
  var argument0 = Blockly.Dart.valueToCode(this, 0) || '0';
  var varName = Blockly.Dart.variableDB_.getName(this.getTitleText(1),
			Blockly.Variables.NAME_TYPE);
  return varName + ' = (' + varName + ' is num ? ' + varName + ' : 0) + ' + 
      argument0 + ';\n';
};

Blockly.Dart.math_single = function(opt_dropParens) {
  // Math operators with single operand.
  var argNaked = Blockly.Dart.valueToCode(this, 0, true) || '0';
  var argParen = Blockly.Dart.valueToCode(this, 0, false) || '0';
	var argDartSafe = argNaked;
	if (!argDartSafe.match(/^[\w\.]+$/)) {
    // -4.abs() returns -4 in Dart due to strange order of operation choices.
    // Need to wrap non-trivial numbers in parentheses: (-4).abs()
    argDartSafe = '(' + argDartSafe + ')';
  }
  var operator = this.getValueLabel(0);
  var code;
  // First, handle cases which generate values that don't need parentheses.
  switch (operator) {
    case this.MSG_ABS:
      code = argDartSafe + '.abs()';
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
    case this.MSG_ROUND:
			// Dart-safe parens not needed since -4.2.round() == (-4.2).round() 
      code = argParen + '.round()';
      break;
    case this.MSG_ROUNDUP:
      code = argDartSafe + '.ceil()';
      break;
    case this.MSG_ROUNDDOWN:
      operator = argDartSafe + '.floor()';
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
  // Second, handle cases which generate values that may need parentheses.
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
Blockly.Dart.math_round = Blockly.Dart.math_single;
// Trigonometry functions have a single operand.
Blockly.Dart.math_trig = Blockly.Dart.math_single;

Blockly.Dart.math_on_list = function() {
  // Rounding functions.
  func = this.getTitleText(0);
  list = Blockly.Dart.valueToCode(this, 0, true) || '[]';
  var code;
  switch (func) {
    case this.MSG_SUM:
      if (!Blockly.Dart.definitions_['math_sum']) {
        var functionName = Blockly.Dart.variableDB_.getDistinctName(
            'math_sum', Blockly.Generator.NAME_TYPE);
        Blockly.Dart.math_on_list.math_sum = functionName;
        var func = [];
        func.push('Dynamic ' + functionName + '(List myList) {');
        func.push('  var sumVal = 0;');
        func.push('  myList.forEach((num entry) {sumVal += entry;});');
        func.push('  return sumVal;');
        func.push('}');
        Blockly.Dart.definitions_['math_sum'] = func.join('\n');
      }
      code = Blockly.Dart.math_on_list.math_sum + '(' + list + ')';
      break;
    case this.MSG_MIN:
      if (!Blockly.Dart.definitions_['math_min']) {
        var functionName = Blockly.Dart.variableDB_.getDistinctName(
            'math_min', Blockly.Generator.NAME_TYPE);
        Blockly.Dart.math_on_list.math_min = functionName;
        var func = [];
        func.push('Dynamic ' + functionName + '(List myList) {');
        func.push('  if (myList.isEmpty()) return;');
        func.push('  var minVal = myList[0];');
        func.push('  myList.forEach((num entry) {minVal = Math.min(minVal, entry);});');
        func.push('  return minVal;');
        func.push('}');
        Blockly.Dart.definitions_['math_min'] = func.join('\n');
      }
      code = Blockly.Dart.math_on_list.math_min + '(' + list + ')';
      break;
    case this.MSG_MAX:
      if (!Blockly.Dart.definitions_['math_max']) {
        var functionName = Blockly.Dart.variableDB_.getDistinctName(
            'math_max', Blockly.Generator.NAME_TYPE);
        Blockly.Dart.math_on_list.math_max = functionName;
        var func = [];
        func.push('Dynamic ' + functionName + '(List myList) {');
        func.push('  if (myList.isEmpty()) return;');
        func.push('  var maxVal = myList[0];');
        func.push('  myList.forEach((num entry) {maxVal = Math.max(maxVal, entry);});');
        func.push('  return maxVal;');
        func.push('}');
        Blockly.Dart.definitions_['math_max'] = func.join('\n');
      }
      code = Blockly.Dart.math_on_list.math_max + '(' + list + ')';
      break;
    case this.MSG_AVERAGE:
      if (!Blockly.Dart.definitions_['math_average']) {
        var functionName = Blockly.Dart.variableDB_.getDistinctName(
            'math_average', Blockly.Generator.NAME_TYPE);
        Blockly.Dart.math_on_list.math_average = functionName;
        var func = [];
        func.push('Dynamic ' + functionName + '(List myList) {');
        func.push('  if (myList.isEmpty()) return;');
        func.push('  var sumVal = 0;');
        func.push('  myList.forEach((num entry) {sumVal += entry;});');
        func.push('  return sumVal / myList.length;');
        func.push('}');
        Blockly.Dart.definitions_['math_average'] = func.join('\n');
      }
      code = Blockly.Dart.math_on_list.math_average + '(' + list + ')';
      break;
    case this.MSG_MEDIAN:
      if (!Blockly.Dart.definitions_['math_median']) {
        var functionName = Blockly.Dart.variableDB_.getDistinctName(
            'math_median', Blockly.Generator.NAME_TYPE);
        Blockly.Dart.math_on_list.math_median = functionName;
        var func = [];
        func.push('Dynamic ' + functionName + '(List myList) {');
        func.push('  // First filter list for numbers only, then sort, then return middle value');
        func.push('  // or the average of two middle values if list has an even number of elements.');
        func.push('  List localList = myList.filter((a) => a is num);');
        func.push('  if (localList.isEmpty()) return;');
        func.push('  localList.sort((a, b) => (a - b));');
        func.push('  int index = (localList.length / 2).toInt();');
        func.push('  if (localList.length.isOdd()) {');
        func.push('    return localList[index];');
        func.push('  } else {');
        func.push('    return (localList[index - 1] + localList[index]) / 2;');
        func.push('  }');
        func.push('}');
        Blockly.Dart.definitions_['math_median'] = func.join('\n');
      }
      code = Blockly.Dart.math_on_list.math_median + '(' + list + ')';
      break;
    case this.MSG_MODE:
      if (!Blockly.Dart.definitions_['math_modes']) {
        var functionName = Blockly.Dart.variableDB_.getDistinctName(
            'math_modes', Blockly.Generator.NAME_TYPE);
        Blockly.Dart.math_on_list.math_modes = functionName;
        // As a list of numbers can contain more than one mode,
        // the returned result is provided as an array.
        // Mode of [3, 'x', 'x', 1, 1, 2, '3'] -> ['x', 1].
        var func = [];
        func.push('Dynamic ' + functionName + '(values) {');
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
        func.push('      counts.add([value, 1]);');
        func.push('      thisCount = 1;');
        func.push('    }');
        func.push('    maxCount = Math.max(thisCount, maxCount);');
        func.push('  }');
        func.push('  for (var j = 0; j < counts.length; j++) {');
        func.push('    if (counts[j][1] == maxCount) {');
        func.push('        modes.add(counts[j][0]);');
        func.push('    }');
        func.push('  }');
        func.push('  return modes;');
        func.push('}');
        Blockly.Dart.definitions_['math_modes'] = func.join('\n');
      }
      code = Blockly.Dart.math_on_list.math_modes + '(' + list + ')';
      break;
    case this.MSG_STD_DEV:
      if (!Blockly.Dart.definitions_['math_standard_deviation']) {
        var functionName = Blockly.Dart.variableDB_.getDistinctName(
            'math_standard_deviation', Blockly.Generator.NAME_TYPE);
        Blockly.Dart.math_on_list.math_standard_deviation = functionName;
        var func = [];
        func.push('Dynamic ' + functionName + '(myList) {');
        func.push('  List numbers = myList.filter((a) => a is num);');
        func.push('  if (numbers.isEmpty()) return;');
        func.push('  var n = numbers.length;');
        func.push('  var sum = 0;');
        func.push('  numbers.forEach((x) => sum += x);');
        func.push('  var mean = sum / n;');
        func.push('  var sumSquare = 0;');
        func.push('  numbers.forEach((x) => sumSquare += Math.pow(x - mean, 2));');
        func.push('  var standard_dev = Math.sqrt(sumSquare / n);');
        func.push('  return standard_dev;');
        func.push('}');
        Blockly.Dart.definitions_['math_standard_deviation'] = func.join('\n');
      }
      code = Blockly.Dart.math_on_list.math_standard_deviation + '(' + list + ')';
      break;
    case this.MSG_RANDOM_ITEM:
      code = list + '[(Math.random() * ' + list + '.length).floor().toInt()]';
      break;
    default:
      throw 'Unknown operator.';
  }
  return code;
};

Blockly.Dart.math_constrain = function() {
  // Constrain a number between two limits.
  var argument0 = Blockly.Dart.valueToCode(this, 0, true) || '0';
  var argument1 = Blockly.Dart.valueToCode(this, 1, true) || '0';
  var argument2 = Blockly.Dart.valueToCode(this, 2, true) || '0';
  return 'Math.min(Math.max(' + argument0 + ', ' + argument1 + '), ' + argument2 + ')';
};

Blockly.Dart.math_modulo = function(opt_dropParens) {
  // Remainder computation.
  var argument0 = Blockly.Dart.valueToCode(this, 0) || '0';
  var argument1 = Blockly.Dart.valueToCode(this, 1) || '0';
  var code = argument0 + ' % ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Dart.math_random_float = function() {
  return 'Math.random()';
};

Blockly.Dart.math_random_int = function() {
  var argument0 = Blockly.Dart.valueToCode(this, 0) || '0';
  var argument1 = Blockly.Dart.valueToCode(this, 1) || '0';
  var rand1 = '(Math.random() * (' + argument1 + ' - ' + argument0 + ' + 1' + 
      ') + ' + argument0 + ').floor()';
  var rand2 = '(Math.random() * (' + argument0 + ' - ' + argument1 + ' + 1' + 
      ') + ' + argument1 + ').floor()';
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
