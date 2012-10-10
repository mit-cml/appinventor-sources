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
    this.appendDummyInput().appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'BOOL');
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
    this.appendValueInput('BOOL').setCheck(Boolean).appendTitle('not');
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
    this.setOutput(true, Boolean);
    this.appendValueInput('A');
    this.appendValueInput('B').appendTitle('=');
    this.setInputsInline(true);
    this.setTooltip('Tests whether two things are equal. \n' +
        'The things being compared can be any thing, not only numbers.');
  }
};

Blockly.Language.logic_operation = {
    // Logical operations: 'and', 'or'.
    category: Blockly.LANG_CATEGORY_LOGIC,
    helpUrl: Blockly.LANG_LOGIC_OPERATION_HELPURL,
    init: function() {
      this.setColour(120);
      this.setOutput(true, Boolean);
      this.appendValueInput('A').setCheck(Boolean);
      this.appendValueInput('B').setCheck(Boolean).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
      this.setInputsInline(true);
      // Assign 'this' to a variable for use in the tooltip closure below.
      var thisBlock = this;
      this.setTooltip(function() {
        var op = thisBlock.getTitleValue('OP');
        return Blockly.Language.logic_operation.TOOLTIPS[op];
      });
    }
  };

Blockly.Language.logic_operation.OPERATORS =
    [[Blockly.LANG_LOGIC_OPERATION_AND, 'AND'],
     [Blockly.LANG_LOGIC_OPERATION_OR, 'OR']];

Blockly.Language.logic_operation.TOOLTIPS = {
  AND : 'Returns true if all inputs are true.',
  OR : 'Returns true if any input is true.'
};


