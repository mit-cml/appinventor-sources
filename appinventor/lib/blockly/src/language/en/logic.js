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
 * @fileoverview Logic blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.logic_compare = {
  // Comparison operator.
  category: 'Logic',
  helpUrl: 'http://en.wikipedia.org/wiki/Inequality_(mathematics)',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(120);
    this.setOutput(true);
    this.appendInput('', Blockly.INPUT_VALUE, 'A');
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.logic_compare.OPERATORS;
    });
    this.appendInput(dropdown, Blockly.INPUT_VALUE, 'B');
    this.setInputsInline(true);
    this.setTooltip(function() {
      var op = thisBlock.getInputLabelValue('B');
      return Blockly.Language.logic_compare.TOOLTIPS[op];
    });
  }
};

Blockly.Language.logic_compare.OPERATORS =
    [['=', 'EQ'],
     ['\u2260', 'NEQ'],
     ['<', 'LT'],
     ['\u2264', 'LTE'],
     ['>', 'GT'],
     ['\u2265', 'GTE']];

Blockly.Language.logic_compare.TOOLTIPS = {
  EQ: 'Return true if both inputs equal each other.',
  NEQ: 'Return true if both inputs are not equal to each other.',
  LT: 'Return true if the first input is smaller\n' +
      'than the second input.',
  LTE: 'Return true if the first input is smaller\n' +
       'than or equal to the second input.',
  GT: 'Return true if the first input is greater\n' +
      'than the second input.',
  GTE: 'Return true if the first input is greater\n' +
       'than or equal to the second input.'
};

Blockly.Language.logic_operation = {
  // Logical operations: 'and', 'or'.
  category: 'Logic',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/And_Or',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(120);
    this.setOutput(true);
    this.appendInput('', Blockly.INPUT_VALUE, 'A');
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.logic_operation.OPERATORS;
    });
    this.appendInput(dropdown, Blockly.INPUT_VALUE, 'B');
    this.setInputsInline(true);
    this.setTooltip(function() {
      var op = thisBlock.getInputLabelValue('B');
      return Blockly.Language.logic_operation.TOOLTIPS[op];
    });
  }
};

Blockly.Language.logic_operation.OPERATORS = [['and', 'AND'], ['or', 'OR']];

Blockly.Language.logic_operation.TOOLTIPS = {
  AND: 'Return true if both inputs are true.',
  OR: 'Return true if either inputs are true.'
};

Blockly.Language.logic_negate = {
  // Negation.
  category: 'Logic',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/Not',
  init: function() {
    this.setColour(120);
    this.setOutput(true);
    this.appendInput('not', Blockly.INPUT_VALUE, 'BOOL');
    this.setTooltip('Returns true if the input is false.\n' +
                    'Returns false if the input is true.');
  }
};

Blockly.Language.logic_boolean = {
  // Boolean data type: true and false.
  category: 'Logic',
  helpUrl: 'http://code.google.com/p/google-blockly/wiki/True_False',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(120);
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(function() {
      return Blockly.Language.logic_boolean.OPERATORS;
    });
    this.appendTitle(dropdown, 'BOOL');
    this.setTooltip('Returns either true or false.');
  }
};

Blockly.Language.logic_boolean.OPERATORS =
    [['true', 'TRUE'], ['false', 'FALSE']];
