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
 * @fileoverview Unit test blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.unittest_main = {
  // Container for unit tests.
  category: 'Unit test',
  init: function() {
    this.setColour(65);
    this.appendDummyInput()
        .appendTitle('run tests');
    this.appendStatementInput('DO');
    this.setTooltip('Executes the enclosed unit tests,\n' +
                    'then prints a summary.');
  },
  getVars: function() {
    return ['unittestResults'];
  }
};

Blockly.Language.unittest_assertequals = {
  // Asserts that a value equals another value.
  category: 'Unit test',
  init: function() {
    this.setColour(65);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldTextInput('test name'), 'MESSAGE');
    this.appendValueInput('ACTUAL', null)
        .appendTitle('actual');
    this.appendValueInput('EXPECTED', null)
        .appendTitle('expected');
    this.setTooltip('Tests that "actual == expected".');
  }
};

Blockly.Language.unittest_asserttrue = {
  // Asserts that a value is true.
  category: 'Unit test',
  init: function() {
    this.setColour(65);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldTextInput('test name'), 'MESSAGE')
    this.appendValueInput('ACTUAL', Boolean)
        .appendTitle('assert true');
    this.setTooltip('Tests that the value is true.');
  }
};

Blockly.Language.unittest_assertfalse = {
  // Asserts that a value is false.
  category: 'Unit test',
  init: function() {
    this.setColour(65);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldTextInput('test name'), 'MESSAGE')
    this.appendValueInput('ACTUAL', Boolean)
        .appendTitle('assert false');
    this.setTooltip('Tests that the value is false.');
  }
};

Blockly.Language.unittest_fail = {
  // Always assert an error.
  category: 'Unit test',
  init: function() {
    this.setColour(65);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldTextInput('test name'), 'MESSAGE')
        .appendTitle('fail');
    this.setTooltip('Records an error.');
  }
};
