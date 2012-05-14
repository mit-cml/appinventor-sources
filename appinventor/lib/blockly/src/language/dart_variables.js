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
 * @fileoverview Generating Dart for variable blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Dart = Blockly.Generator.get('Dart');

Blockly.Dart.variables_get = function() {
  // Variable getter.
  return Blockly.Dart.variableDB_.getVariable(this.getTitleText(1));
};

Blockly.Dart.variables_set = function() {
  // Variable setter.
  var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || '0';
  var varName = Blockly.Dart.variableDB_.getVariable(this.getTitleText(1));
  return varName + ' = ' + argument0 + ';\n';
};
