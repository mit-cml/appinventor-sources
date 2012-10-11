/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
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
 * @fileoverview Generating Python for math blocks.
 * @author fraser@google.com (Neil Fraser)
 */

Blockly.Python = Blockly.Generator.get('Python');

if (!Blockly.Python.RESERVED_WORDS_) {
  Blockly.Python.RESERVED_WORDS_ = '';
}
Blockly.Python.RESERVED_WORDS_ += 'math,random,';

Blockly.Python.math_number = function() {
  // Numeric value.
  var code = window.parseFloat(this.getTitleValue('NUM'));
  return [code, Blockly.Python.ORDER_UNARY_SIGN];
};

Blockly.Python.math_arithmetic = function() {
  // Basic arithmetic operators, and power.
  var mode = this.getTitleValue('OP');
  var tuple = Blockly.Python.math_arithmetic.OPERATORS[mode];
  var operator = tuple[0];
  var order = tuple[1];
  var argument0 = Blockly.Python.valueToCode(this, 'A', order) || '0';
  var argument1 = Blockly.Python.valueToCode(this, 'B', order) || '0';
  var code = argument0 + operator + argument1;
  return [code, order];
  // In case of 'DIVIDE', division between integers returns different results
  // in Python 2 and 3. However, is not an issue since Blockly does not
  // guarantee identical results in all languages.  To do otherwise would
  // require every operator to be wrapped in a function call.  This would kill
  // legibility of the generated code.  See:
  // http://code.google.com/p/blockly/wiki/Language
};

Blockly.Python.math_arithmetic.OPERATORS = {
  ADD: [' + ', Blockly.Python.ORDER_ADDITIVE],
  MINUS: [' - ', Blockly.Python.ORDER_ADDITIVE],
  MULTIPLY: [' * ', Blockly.Python.ORDER_MULTIPLICATIVE],
  DIVIDE: [' / ', Blockly.Python.ORDER_MULTIPLICATIVE],
  POWER: [' ** ', Blockly.Python.ORDER_EXPONENTIATION]
};

Blockly.Python.math_change = function() {
  // Add to a variable in place.
  var argument0 = Blockly.Python.valueToCode(this, 'DELTA',
      Blockly.Python.ORDER_ADDITIVE) || '0';
  var varName = Blockly.Python.variableDB_.getName(this.getTitleValue('VAR'),
      Blockly.Variables.NAME_TYPE);
  return varName + ' = (' + varName + ' if type(' + varName +
      ') in (int, float) else 0) + ' + argument0 + '\n';
};

Blockly.Python.math_single = function() {
  // Math operators with single operand.
  var operator = this.getTitleValue('OP');
  var code;
  var arg;
  if (operator == 'NEG') {
    // Negation is a special case given its different operator precedence.
    var code = Blockly.Python.valueToCode(this, 'NUM',
        Blockly.Python.ORDER_UNARY_SIGN) || '0';
    return ['-' + code, Blockly.Python.ORDER_UNARY_SIGN];
  }
  Blockly.Python.definitions_['import_math'] = 'import math';
  if (operator == 'SIN' || operator == 'COS' || operator == 'TAN') {
    arg = Blockly.Python.valueToCode(this, 'NUM',
        Blockly.Python.ORDER_MULTIPLICATIVE) || '0';
  } else {
    arg = Blockly.Python.valueToCode(this, 'NUM',
        Blockly.Python.ORDER_NONE) || '0';
  }
  // First, handle cases which generate values that don't need parentheses
  // wrapping the code.
  switch (operator) {
    case 'ABS':
      code = 'math.fabs(' + arg + ')';
      break;
    case 'ROOT':
      code = 'math.sqrt(' + arg + ')';
      break;
    case 'LN':
      code = 'math.log(' + arg + ')';
      break;
    case 'LOG10':
      code = 'math.log10(' + arg + ')';
      break;
    case 'EXP':
      code = 'math.exp(' + arg + ')';
      break;
    case 'POW10':
      code = 'math.pow(10,' + arg + ')';
      break;
    case 'ROUND':
      code = 'round(' + arg + ')';
      break;
    case 'ROUNDUP':
      code = 'math.ceil(' + arg + ')';
      break;
    case 'ROUNDDOWN':
      code = 'math.floor(' + arg + ')';
      break;
    case 'SIN':
      code = 'math.sin(' + arg + ' / 180.0 * math.pi)';
      break;
    case 'COS':
      code = 'math.cos(' + arg + ' / 180.0 * math.pi)';
      break;
    case 'TAN':
      code = 'math.tan(' + arg + ' / 180.0 * math.pi)';
      break;
  }
  if (code) {
    return [code, Blockly.Python.ORDER_FUNCTION_CALL];
  }
  // Second, handle cases which generate values that may need parentheses
  // wrapping the code.
  switch (operator) {
    case 'ASIN':
      code = 'math.asin(' + arg + ') / math.pi * 180';
      break;
    case 'ACOS':
      code = 'math.acos(' + arg + ') / math.pi * 180';
      break;
    case 'ATAN':
      code = 'math.atan(' + arg + ') / math.pi * 180';
      break;
    default:
      throw 'Unknown math operator: ' + operator;
  }
  return [code, Blockly.Python.ORDER_MULTIPLICATIVE];
};

// Rounding functions have a single operand.
Blockly.Python.math_round = Blockly.Python.math_single;
// Trigonometry functions have a single operand.
Blockly.Python.math_trig = Blockly.Python.math_single;

Blockly.Python.math_on_list = function() {
  // Math functions for lists.
  func = this.getTitleValue('OP');
  list = Blockly.Python.valueToCode(this, 'LIST',
      Blockly.Python.ORDER_NONE) || '[]';
  var code;
  switch (func) {
    case 'SUM':
      code = 'sum(' + list + ')';
      break;
    case 'MIN':
      code = 'min(' + list + ')';
      break;
    case 'MAX':
      code = 'max(' + list + ')';
      break;
    case 'AVERAGE':
      if (!Blockly.Python.definitions_['math_mean']) {
        // This operation exclude null and values that are not int or float:
        //   math_mean([null,null,"aString",1,9]) == 5.0.
        var functionName = Blockly.Python.variableDB_.getDistinctName(
            'math_mean', Blockly.Generator.NAME_TYPE);
        Blockly.Python.math_on_list.math_mean = functionName;
        var func = [];
        func.push('def ' + functionName + '(myList):');
        func.push('  localList = [e for e in myList ' +
            'if type(e) in [int, float]]');
        func.push('  if not localList: return');
        func.push('  return float(sum(localList)) / len(localList)');
        Blockly.Python.definitions_['math_mean'] = func.join('\n');
      }
      code = Blockly.Python.math_on_list.math_mean + '(' + list + ')';
      break;
    case 'MEDIAN':
      if (!Blockly.Python.definitions_['math_median']) {
        // This operation exclude null values:
        //   math_median([null,null,1,3]) == 2.0.
        var functionName = Blockly.Python.variableDB_.getDistinctName(
            'math_median', Blockly.Generator.NAME_TYPE);
        Blockly.Python.math_on_list.math_median = functionName;
        var func = [];
        func.push('def ' + functionName + '(myList):');
        func.push('  localList = sorted([e for e in myList ' +
            'if type(e) in [int, float]])');
        func.push('  if not localList: return');
        func.push('  if len(localList) % 2 == 0:');
        func.push('    return (localList[len(localList) / 2 - 1] + ' +
            'localList[len(localList) / 2]) / 2.0');
        func.push('  else:');
        func.push('    return localList[(len(localList) - 1) / 2]');
        Blockly.Python.definitions_['math_median'] = func.join('\n');
      }
      code = Blockly.Python.math_on_list.math_median + '(' + list + ')';
      break;
    case 'MODE':
      if (!Blockly.Python.definitions_['math_modes']) {
        // As a list of numbers can contain more than one mode,
        // the returned result is provided as an array.
        // Mode of [3, 'x', 'x', 1, 1, 2, '3'] -> ['x', 1].
        var functionName = Blockly.Python.variableDB_.getDistinctName(
            'math_modes', Blockly.Generator.NAME_TYPE);
        Blockly.Python.math_on_list.math_modes = functionName;
        var func = [];
        func.push('def ' + functionName + '(some_list):');
        func.push('  modes = []');
        func.push('  # Using a lists of [item, count] to keep count rather ' +
                  'than dict');
        func.push('  # to avoid "unhashable" errors when the counted item is ' +
                  'itself a list or dict.');
        func.push('  counts = []');
        func.push('  maxCount = 1');
        func.push('  for item in some_list:');
        func.push('    found = False');
        func.push('    for count in counts:');
        func.push('      if count[0] == item:');
        func.push('        count[1] += 1');
        func.push('        maxCount = max(maxCount, count[1])');
        func.push('        found = True');
        func.push('    if not found:');
        func.push('      counts.append([item, 1])');
        func.push('  for counted_item, item_count in counts:');
        func.push('    if item_count == maxCount:');
        func.push('      modes.append(counted_item)');
        func.push('  return modes');
        Blockly.Python.definitions_['math_modes'] = func.join('\n');
      }
      code = Blockly.Python.math_on_list.math_modes + '(' + list + ')';
      break;
    case 'STD_DEV':
      Blockly.Python.definitions_['import_math'] = 'import math';
      if (!Blockly.Python.definitions_['math_standard_deviation']) {
        var functionName = Blockly.Python.variableDB_.getDistinctName(
            'math_standard_deviation', Blockly.Generator.NAME_TYPE);
        Blockly.Python.math_on_list.math_standard_deviation = functionName;
        var func = [];
        func.push('def ' + functionName + '(numbers):');
        func.push('  n = len(numbers)');
        func.push('  if n == 0: return');
        func.push('  mean = float(sum(numbers)) / n');
        func.push('  variance = sum((x - mean) ** 2 for x in numbers) / n');
        func.push('  standard_dev = math.sqrt(variance)');
        func.push('  return standard_dev');
        Blockly.Python.definitions_['math_standard_deviation'] =
            func.join('\n');
      }
      code = Blockly.Python.math_on_list.math_standard_deviation +
          '(' + list + ')';
      break;
    case 'RANDOM':
      Blockly.Python.definitions_['import_random'] = 'import random';
      code = 'random.choice(' + list + ')';
      break;
    default:
      throw 'Unknown operator: ' + func;
  }
  return [code, Blockly.Python.ORDER_FUNCTION_CALL];
};

Blockly.Python.math_constrain = function() {
  // Constrain a number between two limits.
  var argument0 = Blockly.Python.valueToCode(this, 'VALUE',
      Blockly.Python.ORDER_NONE) || '0';
  var argument1 = Blockly.Python.valueToCode(this, 'LOW',
      Blockly.Python.ORDER_NONE) || '0';
  var argument2 = Blockly.Python.valueToCode(this, 'HIGH',
      Blockly.Python.ORDER_NONE) || '0';
  code = 'min(max(' + argument0 + ', ' + argument1 + '), ' + argument2 + ')';
  return [code, Blockly.Python.ORDER_FUNCTION_CALL];
};

Blockly.Python.math_modulo = function() {
  // Remainder computation.
  var argument0 = Blockly.Python.valueToCode(this, 'DIVIDEND',
      Blockly.Python.ORDER_MULTIPLICATIVE) || '0';
  var argument1 = Blockly.Python.valueToCode(this, 'DIVISOR',
      Blockly.Python.ORDER_MULTIPLICATIVE) || '0';
  var code = argument0 + ' % ' + argument1;
  return [code, Blockly.Python.ORDER_MULTIPLICATIVE];
};

Blockly.Python.math_random_int = function() {
  // Random integer between [X] and [Y].
  Blockly.Python.definitions_['import_random'] = 'import random';
  var argument0 = Blockly.Python.valueToCode(this, 'FROM',
      Blockly.Python.ORDER_NONE) || '0';
  var argument1 = Blockly.Python.valueToCode(this, 'TO',
      Blockly.Python.ORDER_NONE) || '0';
  code = 'random.randint(' + argument0 + ', ' + argument1 + ')';
  return [code, Blockly.Python.ORDER_FUNCTION_CALL];
};

Blockly.Python.math_random_float = function() {
  // Random fraction between 0 and 1.
  Blockly.Python.definitions_['import_random'] = 'import random';
  return ['random.random()', Blockly.Python.ORDER_FUNCTION_CALL];
};
