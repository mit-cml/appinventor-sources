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
 * @fileoverview Generating Python for text blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Python = Blockly.Generator.get('Python');

Blockly.Python.text = function() {
  // Text value.
  return Blockly.Python.quote_(this.getTitleText('TEXT'));
};

Blockly.Python.text_join = function(opt_dropParens) {
  // Create a string made up of any number of elements of any type.
  //Should we allow joining by '-' or ',' or any other characters?
  if (this.itemCount_ == 0) {
    return '\'\'';
  } else if (this.itemCount_ == 1) {
    return 'str(' + (Blockly.Python.valueToCode(this, 'ADD0', opt_dropParens) || '\'\'') + ')';
  } else if (this.itemCount_ == 2) {
    var argument0 = Blockly.Python.valueToCode(this, 'ADD0', true) || '\'\'';
    var argument1 = Blockly.Python.valueToCode(this, 'ADD1', true) || '\'\'';
    var code = 'str(' + argument0 + ') + str(' + argument1 + ')';
    if (!opt_dropParens) {
      code = '(' + code + ')';
    }
    return code;
  } else {
    var code = [];
    for (n = 0; n < this.itemCount_; n++) {
      code[n] = Blockly.Python.valueToCode(this, 'ADD' + n, true) || '\'\'';
    }
    var tempVar = Blockly.Python.variableDB_.getDistinctName('temp_value',
        Blockly.Variables.NAME_TYPE);
    code = '\'\'.join([str(' + tempVar + ') for ' + tempVar + ' in [' + code.join(', ') + ']])';
    if (!opt_dropParens) {
      code = '(' + code + ')';
    }
    return code;
  }
};

Blockly.Python.text_length = function() {
  // String length.
  var argument0 = Blockly.Python.valueToCode(this, 'VALUE', true) || '\'\'';
  return 'len(' + argument0 + ')';
};

Blockly.Python.text_isEmpty = function(opt_dropParens) {
  // Is the string null?
  var argument0 = Blockly.Python.valueToCode(this, 'VALUE', true) || '\'\'';
  var code = 'not len(' + argument0 + ')';
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Python.text_endString = function() {
  // Return a leading or trailing substring.
  // Do we need to prevent 'List index out of range' ERROR by checking
  // if argument 0 > len(argument1)? Or will ALL error be handled systematically?
  var first = this.getInputLabelValue('NUM') == 'FIRST';
  var argument0 = Blockly.Python.valueToCode(this, 'NUM', true) || '1';
  var argument1 = Blockly.Python.valueToCode(this, 'TEXT') || '\'\'';
  var code = argument1 + '[' +
      (first ? ':' + argument0 : '-' + argument0 + ':') + ']';
  return code;
};

Blockly.Python.text_indexOf = function(opt_dropParens) {
  // Search the text for a substring.
  // Should we allow for non-case sensitive???
  var operator = this.getTitleValue('END') == 'FIRST' ? 'find' : 'rfind';
  var argument0 = Blockly.Python.valueToCode(this, 'FIND') || '\'\'';
  var argument1 = Blockly.Python.valueToCode(this, 'VALUE') || '\'\'';
  var code = argument1 + '.' + operator + '(' + argument0 + ') + 1';
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Python.text_charAt = function() {
  // Get letter at index.
  var argument0 = Blockly.Python.valueToCode(this, 'AT', true) || '1';
  var argument1 = Blockly.Python.valueToCode(this, 'VALUE') || '[]';
  // Blockly uses one-based indicies.
  if (argument0.match(/^\d+$/)) {
    // If the index is a naked number, decrement it right now.
    // Except not allowing negative index by constraining at 0.
    argument0 = Math.max(0, parseInt(argument0, 10) - 1);
  } else {
    // If the index is dynamic, decrement it in code.
    argument0 += ' - 1';
  }
  return argument1 + '[' + argument0 + ']';
};

Blockly.Python.text_changeCase = function() {
  // Change capitalization.
  var mode = this.getInputLabelValue('TEXT');
  var operator = Blockly.JavaScript.text_changeCase.OPERATORS[mode];
  var argument0 = Blockly.Python.valueToCode(this, 'TEXT', true) || '\'\'';
  var code = argument0 + operator;
  return code;
};

Blockly.Python.text_changeCase.OPERATORS = {
  UPPERCASE: '.upper()',
  LOWERCASE: '.lower()',
  TITLECASE: '.title()'
};

Blockly.Python.text_trim = function() {
  // Trim spaces.
  var mode = this.getTitleValue('MODE');
  var operator = Blockly.JavaScript.text_trim.OPERATORS[mode];
  var argument0 = Blockly.Python.valueToCode(this, 'TEXT') || '\'\'';
  return argument0 + operator;
};

Blockly.Python.text_trim.OPERATORS = {
  LEFT: '.lstrip()',
  RIGHT: '.rstrip()',
  BOTH: '.strip()'
};

Blockly.Python.text_print = function() {
  // Print statement.
  var argument0 = Blockly.Python.valueToCode(this, 'TEXT', true) || '\'\'';
  return 'print ' + argument0 + '\n';
};
