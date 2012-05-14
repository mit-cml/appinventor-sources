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
 * @fileoverview Generating Dart for control blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Dart = Blockly.Generator.get('Dart');

Blockly.Dart.controls_if = function() {
  // If/elseif/else condition.
  var n = 0;
  var argument = Blockly.Dart.valueToCode_(this, n, true) || 'false';
  var branch = Blockly.Dart.statementToCode_(this, n);
  var code = 'if (' + argument + ') {\n' + branch + '}';
  for (n = 1; n <= this.elseifCount_; n++) {
    argument = Blockly.Dart.valueToCode_(this, n, true) || 'false';
    branch = Blockly.Dart.statementToCode_(this, n);
    code += ' else if (' + argument + ') {\n' + branch + '}';
  }
  if (this.elseCount_) {
    branch = Blockly.Dart.statementToCode_(this, n);
    code += ' else {\n' + branch + '}';
  }
  return code + '\n';
};

Blockly.Dart.controls_whileUntil = function() {
  // Do while/until loop.
  var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || 'false';
  var branch0 = Blockly.Dart.statementToCode_(this, 0);
  if (this.getTitleText(1) == this.MSG_UNTIL) {
    argument0 = '!(' + argument0 + ')';
  }
  return 'while (' + argument0 + ') {\n' + branch0 + '}\n';
};

Blockly.Dart.controls_for = function() {
  // For loop.
  var variable0 = Blockly.Dart.variableDB_.getVariable(
      this.getVariableInput(0));
  var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || '0';
  var argument1 = Blockly.Dart.valueToCode_(this, 1, true) || '0';
  var branch0 = Blockly.Dart.statementToCode_(this, 0);
  var code;
  if (argument1.match(/^\w+$/)) {
    code = 'for (' + variable0 + ' = ' + argument0 + '; ' + variable0 + ' <= ' + argument1 + '; ' + variable0 + '++) {\n' +
        branch0 + '}\n';
  } else {
    // The end value appears to be more complicated than a simple variable.
    // Cache it to a variable to prevent repeated look-ups.
    var endVar = Blockly.Dart.variableDB_.getDistinctVariable(
        variable0 + '_end');
    code = 'var ' + endVar + ' = ' + argument1 + ';\n' +
        'for (' + variable0 + ' = ' + argument0 + '; ' + variable0 + ' <= ' + endVar + '; ' + variable0 + '++) {\n' +
        branch0 + '}\n';
  }
  return code;
};

Blockly.Dart.controls_forEach = function() {
  // For each loop.
  var variable0 = Blockly.Dart.variableDB_.getVariable(
      this.getVariableInput(0));
  var argument0 = Blockly.Dart.valueToCode_(this, 0, true) || '[]';
  var branch0 = Blockly.Dart.statementToCode_(this, 0);
  var code;
  var indexVar = Blockly.Dart.variableDB_.getDistinctVariable(
      variable0 + '_index');
  if (argument0.match(/^\w+$/)) {
    branch0 = '  ' + variable0 + ' = ' + argument0 + '[' + indexVar + '];\n' + branch0;
    code = 'for (var ' + indexVar + ' in  ' + argument0 + ') {\n' +
        branch0 + '}\n';
  } else {
    // The list appears to be more complicated than a simple variable.
    // Cache it to a variable to prevent repeated look-ups.
    var listVar = Blockly.Dart.variableDB_.getDistinctVariable(
        variable0 + '_list');
    branch0 = '  ' + variable0 + ' = ' + listVar + '[' + indexVar + '];\n' + branch0;
    code = 'var ' + listVar + ' = ' + argument0 + ';\n' +
        'for (var ' + indexVar + ' in ' + listVar + ') {\n' +
        branch0 + '}\n';
  }
  return code;
};
