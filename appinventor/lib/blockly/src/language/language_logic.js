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
    this.setColour('green');
    this.setOutput(true);
    this.addInput('', '', Blockly.INPUT_VALUE);
    var dropdown = new Blockly.FieldDropdown(thisBlock.MSG_EQ, function() {
      return [thisBlock.MSG_EQ,
              thisBlock.MSG_NEQ,
              thisBlock.MSG_LT,
              thisBlock.MSG_LTE,
              thisBlock.MSG_GT,
              thisBlock.MSG_GTE];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
    this.setTooltip(function() {
      switch (thisBlock.getValueLabel(1)) {
        case thisBlock.MSG_EQ:
          return 'Return true if both inputs equal each other.';
        case thisBlock.MSG_NEQ:
          return 'Return true if both inputs are not equal to each other.';
        case thisBlock.MSG_LT:
          return 'Return true if the first input is smaller\nthan the second input.';
        case thisBlock.MSG_LTE:
          return 'Return true if the first input is smaller\nthan or equal to the second input.';
        case thisBlock.MSG_GT:
          return 'Return true if the first input is greater\nthan the second input.';
        case thisBlock.MSG_GTE:
          return 'Return true if the first input is greater\nthan or equal to the second input.';
      }
      return '';
    });
  },
  MSG_EQ: '=',
  MSG_NEQ: '\u2260',
  MSG_LT: '<',
  MSG_LTE: '\u2264',
  MSG_GT: '>',
  MSG_GTE: '\u2265'
};

Blockly.Language.logic_operation = {
  // Logical operations: 'and', 'or'.
  category: 'Logic',
  helpUrl: 'http://en.wikipedia.org/wiki/Logical_disjunction',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour('green');
    this.setOutput(true);
    this.addInput('', '', Blockly.INPUT_VALUE);
    var dropdown = new Blockly.FieldDropdown(thisBlock.MSG_AND, function() {
      return [thisBlock.MSG_AND, thisBlock.MSG_OR];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
    this.setTooltip(function() {
      switch (thisBlock.getValueLabel(1)) {
        case thisBlock.MSG_AND:
          return 'Return true if both inputs are true.';
        case thisBlock.MSG_OR:
          return 'Return true if either inputs are true.';
      }
      return '';
    });
  },
  MSG_AND: 'and',
  MSG_OR: 'or'
};

Blockly.Language.logic_negate = {
  // Negation.
  category: 'Logic',
  helpUrl: 'http://en.wikipedia.org/wiki/Logical_disjunction',
  init: function() {
    this.setColour('green');
    this.setOutput(true);
    this.addInput('not', '', Blockly.INPUT_VALUE);
    this.setTooltip('Returns true if the input is false.\nReturns false if the input is true.');
  }
};

Blockly.Language.logic_boolean = {
  // Boolean data type: true and false.
  category: 'Logic',
  helpUrl: 'http://en.wikipedia.org/wiki/Boolean_data_type',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour('green');
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(thisBlock.MSG_TRUE, function() {
      return [thisBlock.MSG_TRUE, thisBlock.MSG_FALSE];
    });
    this.addTitle(dropdown);
    this.setTooltip('Returns either true or false.');
  },
  MSG_TRUE: 'true',
  MSG_FALSE: 'false'
};
