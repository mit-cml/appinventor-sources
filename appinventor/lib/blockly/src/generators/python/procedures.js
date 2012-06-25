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
 * @fileoverview Generating Python for variable blocks.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

Blockly.Python = Blockly.Generator.get('Python');

Blockly.Python.procedures_defreturn = function() {
  // Define a procedure with a return value.
  // First, add a 'global' statement for every variable that is assigned.
  var globals = Blockly.Variables.allVariables(this);
  for (var i = 0, varName; varName = globals[i]; i++) {
    globals[i] = Blockly.Python.variableDB_.getName(varName,
        Blockly.Variables.NAME_TYPE);
  }
  globals = globals.length ? '  global ' + globals.join(', ') + '\n' : '';
  var funcName = Blockly.Python.variableDB_.getName(this.getTitleText('NAME'),
      Blockly.Procedures.NAME_TYPE);
  var branch = Blockly.Python.statementToCode(this, 'STACK');
  var returnValue = Blockly.Python.valueToCode(this, 'RETURN', true) || '';
  if (returnValue) {
    returnValue = '  return ' + returnValue + '\n';
  } else if (!branch) {
    branch = '  pass';
  }
  var code = 'def ' + funcName + '():\n' + globals + branch + returnValue + '\n';
  code = Blockly.Python.scrub_(this, code);
  Blockly.Python.definitions_[funcName] = code;
  return null;
};

// Defining a procedure without a return value uses the same generator as
// a procedure with a return value.
Blockly.Python.procedures_defnoreturn =
    Blockly.Python.procedures_defreturn;

Blockly.Python.procedures_callreturn = function() {
  // Call a procedure with a return value.
  var funcName = Blockly.Python.variableDB_.getName(this.getTitleText('NAME'),
      Blockly.Procedures.NAME_TYPE);
  var code = funcName + '()';
  return code;
};

Blockly.Python.procedures_callnoreturn = function() {
  // Call a procedure with no return value.
  var funcName = Blockly.Python.variableDB_.getName(this.getTitleText('NAME'),
      Blockly.Procedures.NAME_TYPE);
  var code = funcName + '()\n';
  return code;
};
