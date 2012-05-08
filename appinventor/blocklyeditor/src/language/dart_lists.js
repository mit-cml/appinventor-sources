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
 * @fileoverview Generating Dart for list blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Dart = Blockly.Generator.get('Dart');

Blockly.Dart.lists_length = function(opt_dropParens) {
  // Testing the length of a list is the same as for a string.
  return Blockly.Dart.text_length.call(this, opt_dropParens);
};

Blockly.Dart.lists_isEmpty = function(opt_dropParens) {
  // Testing a list for being empty is the same as for a string.
  return Blockly.Dart.text_isEmpty.call(this, opt_dropParens);
};

Blockly.Dart.lists_contains = function(opt_dropParens) {
  // Testing a list for a value is the same as search for a substring.
  return Blockly.Dart.text_contains.call(this, opt_dropParens);
};

Blockly.Dart.lists_getIndex = function(opt_dropParens) {
  // Indexing into a list is the same as indexing into a string.
  return Blockly.Dart.text_charAt.call(this, opt_dropParens);
};
