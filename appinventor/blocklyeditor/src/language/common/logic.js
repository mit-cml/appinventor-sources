/**
 * Visual Blocks Language
 *
 * Copyright 2012 Massachusetts Institute of Technology. All rights reserved.
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
 * @fileoverview Logic blocks for Blockly, modified for App Inventor
 * @author fraser@google.com (Neil Fraser)
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.logic_boolean = {
  // Boolean data type: true and false.
  category : Blockly.LANG_CATEGORY_LOGIC,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.setOutput(true, Boolean);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendTitle(dropdown, 'BOOL');
 // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('BOOL');
      return Blockly.Language.logic_boolean.TOOLTIPS[op];
    });
  }
};

Blockly.Language.logic_boolean.OPERATORS = [
    [ Blockly.LANG_LOGIC_BOOLEAN_TRUE, 'TRUE' ],
    [ Blockly.LANG_LOGIC_BOOLEAN_FALSE, 'FALSE' ]];

Blockly.Language.logic_boolean.TOOLTIPS = {
  TRUE : 'Reports the boolean true.',
  FALSE : 'Reports the boolean false.'
};

Blockly.Language.logic_negate = {
  // Negation.
  category : Blockly.LANG_CATEGORY_LOGIC,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.setOutput(true, Boolean);
    this.appendInput('not', Blockly.INPUT_VALUE, 'BOOL', Boolean);
    this.setTooltip('Returns true if the input is false.\n' +
        'Returns false if the input is true.');
  }
};

Blockly.Language.logic_compare = {
  // Comparison operator.
  category : Blockly.LANG_CATEGORY_LOGIC,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.appendInput('', Blockly.INPUT_VALUE, 'A', null);
    this.appendInput('=', Blockly.INPUT_VALUE, 'B', null);
    this.setInputsInline(true);
    this.setOutput(true, Boolean);
    this.setTooltip('Tests whether two things are equal. \n' +
        'The things being compared can be any thing, not only numbers.');
  }
};

Blockly.Language.logic_operation = {
  // Logical operations: 'and', 'or'.
  // TODO: (Andrew) Make these take multiple arguments.
  category : Blockly.LANG_CATEGORY_LOGIC,
  helpUrl : '',
  init : function() {
    this.setColour(120);
    this.setOutput(true, Boolean);
    this.appendInput('', Blockly.INPUT_VALUE, 'A', Boolean);
    var dropdown = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendInput([ dropdown, 'OP' ], Blockly.INPUT_VALUE, 'B', Boolean);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('OP');
      return Blockly.Language.logic_operation.TOOLTIPS[op];
    });
  }
};

Blockly.Language.logic_operation.OPERATORS = [
    [ Blockly.LANG_LOGIC_OPERATION_AND, 'AND' ],
    [ Blockly.LANG_LOGIC_OPERATION_OR, 'OR' ] ];

Blockly.Language.logic_operation.TOOLTIPS = {
  AND : 'Returns true if all inputs are true.',
  OR : 'Returns true if any input is true.'
};


