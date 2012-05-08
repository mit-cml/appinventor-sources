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
    this.setColour('green');
    this.setOutput(true);
    this.addInput('', '', Blockly.INPUT_VALUE);
    var dropdown = new Blockly.FieldDropdown(Blockly.Language.logic_compare.MSG_EQ, function() {
      return [Blockly.Language.logic_compare.MSG_EQ,
              Blockly.Language.logic_compare.MSG_NEQ,
              Blockly.Language.logic_compare.MSG_LT,
              Blockly.Language.logic_compare.MSG_LTE,
              Blockly.Language.logic_compare.MSG_GT,
              Blockly.Language.logic_compare.MSG_GTE];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};

Blockly.Language.logic_compare.MSG_EQ = '=';
Blockly.Language.logic_compare.MSG_NEQ = '\u2260';
Blockly.Language.logic_compare.MSG_LT = '<';
Blockly.Language.logic_compare.MSG_LTE = '\u2264';
Blockly.Language.logic_compare.MSG_GT = '>';
Blockly.Language.logic_compare.MSG_GTE = '\u2265';

Blockly.Language.logic_operation = {
  // Logical operations: 'and', 'or'.
  category: 'Logic',
  helpUrl: 'http://en.wikipedia.org/wiki/Logical_disjunction',
  init: function() {
    this.setColour('green');
    this.setOutput(true);
    this.addInput('', '', Blockly.INPUT_VALUE);
    var dropdown = new Blockly.FieldDropdown(Blockly.Language.logic_operation.MSG_AND, function() {
      return [Blockly.Language.logic_operation.MSG_AND,
              Blockly.Language.logic_operation.MSG_OR];
    });
    this.addInput(dropdown, '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
  }
};

Blockly.Language.logic_operation.MSG_AND = 'and';
Blockly.Language.logic_operation.MSG_OR = 'or';

Blockly.Language.logic_negate = {
  // Negation.
  category: 'Logic',
  helpUrl: 'http://en.wikipedia.org/wiki/Logical_disjunction',
  init: function() {
    this.setColour('green');
    this.setOutput(true);
    this.addInput('not', '', Blockly.INPUT_VALUE);
  }
};

Blockly.Language.logic_boolean = {
  // Boolean data type: true and false.
  category: 'Logic',
  helpUrl: 'http://en.wikipedia.org/wiki/Boolean_data_type',
  init: function() {
    this.setColour('green');
    this.setOutput(true);
    var dropdown = new Blockly.FieldDropdown(Blockly.Language.logic_boolean.MSG_TRUE, function() {
      return [Blockly.Language.logic_boolean.MSG_TRUE,
              Blockly.Language.logic_boolean.MSG_FALSE];
    });
    this.addTitle(dropdown);
  }
};

Blockly.Language.logic_boolean.MSG_TRUE = 'true';
Blockly.Language.logic_boolean.MSG_FALSE = 'false';
