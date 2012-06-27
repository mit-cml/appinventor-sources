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
 * @fileoverview Generating Python for list blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Python = Blockly.Generator.get('Python');

Blockly.Python.lists_create_empty = function() {
  // Create an empty list.
  return '[]';
};

Blockly.Python.lists_create_with = function() {
  // Create a list with any number of elements of any type.
  var code = new Array(this.itemCount_);
  for (n = 0; n < this.itemCount_; n++) {
    code[n] = Blockly.Python.valueToCode(this, 'ADD' + n, true) || 'None';
  }
  return '[' + code.join(', ') + ']';
};

Blockly.Python.lists_repeat = function(opt_dropParens) {
  // Create a list with one element repeated.
  var argument0 = Blockly.Python.valueToCode(this, 'ITEM', true) || 'None';
  var argument1 = Blockly.Python.valueToCode(this, 'NUM') || '0';
  var code = '[' + argument0 + '] * ' + argument1;
  if (!opt_dropParens) {
    code = '(' + code + ')';
  }
  return code;
};

Blockly.Python.lists_length = function(opt_dropParens) {
  // Testing the length of a list is the same as for a string.
  return Blockly.Python.text_length.call(this, opt_dropParens);
};

Blockly.Python.lists_isEmpty = function(opt_dropParens) {
  // Testing a list for being empty is the same as for a string.
  return Blockly.Python.text_isEmpty.call(this, opt_dropParens);
};

Blockly.Python.lists_indexOf = function(opt_dropParens) {
  // Searching a list for a value is the same as search for a substring.
  return Blockly.Python.text_indexOf.call(this, opt_dropParens);
};

Blockly.Python.lists_getIndex = function(opt_dropParens) {
  // Indexing into a list is the same as indexing into a string.
  return Blockly.Python.text_charAt.call(this, opt_dropParens);
};

Blockly.Python.lists_setIndex = function() {
  // Set element at index.
  var argument0 = Blockly.Python.valueToCode(this, 'AT', true) || '1';
  var argument1 = Blockly.Python.valueToCode(this, 'LIST') || '[]';
  var argument2 = Blockly.Python.valueToCode(this, 'TO', true) || 'None';
  // Blockly uses one-based indicies.
  if (argument0.match(/^\d+$/)) {
    // If the index is a naked number, decrement it right now.
    // Except we don't allow negative index like in Python.
    argument0 = Math.max(0, parseInt(argument0, 10) - 1);
  } else {
    // If the index is dynamic, decrement it in code.
    argument0 += ' - 1';
  }
  return argument1 + '[' + argument0 + '] = ' + argument2 + '\n';
};
