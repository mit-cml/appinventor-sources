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
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendDummyInput().appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'BOOL');
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('BOOL');
      return Blockly.Language.logic_boolean.TOOLTIPS[op];
    });
    this.appendCollapsedInput().appendTitle(Blockly.LANG_LOGIC_BOOLEAN_TRUE, 'COLLAPSED_TEXT');
  },
  helpUrl : function() {
    var op = this.getTitleValue('BOOL');
    return Blockly.Language.logic_boolean.HELPURLS[op];},
  typeblock: [{
    translatedName: Blockly.LANG_LOGIC_BOOLEAN_TRUE,
    dropDown: {
      titleName: 'BOOL',
      value: 'TRUE'
    }
  },{
    translatedName: Blockly.LANG_LOGIC_BOOLEAN_FALSE,
    dropDown: {
      titleName: 'BOOL',
      value: 'FALSE'
    }
  }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('BOOL'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.logic_boolean.OPERATORS = [
    [ Blockly.LANG_LOGIC_BOOLEAN_TRUE, 'TRUE' ],
    [ Blockly.LANG_LOGIC_BOOLEAN_FALSE, 'FALSE' ]];

Blockly.Language.logic_boolean.TOOLTIPS = {
  TRUE : Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE,
  FALSE : Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE
};

Blockly.Language.logic_boolean.HELPURLS = {
  TRUE : Blockly.LANG_LOGIC_BOOLEAN_TRUE_HELPURL,
  FALSE : Blockly.LANG_LOGIC_BOOLEAN_FALSE_HELPURL
};

Blockly.Language.logic_false = {
  // Boolean data type: true and false.
  category : Blockly.LANG_CATEGORY_LOGIC,
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendDummyInput().appendTitle(new Blockly.FieldDropdown(Blockly.Language.logic_boolean.OPERATORS), 'BOOL');
    this.setTitleValue('FALSE','BOOL');
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('BOOL');
      return Blockly.Language.logic_boolean.TOOLTIPS[op];
    });
    this.appendCollapsedInput().appendTitle(Blockly.LANG_LOGIC_BOOLEAN_FALSE, 'COLLAPSED_TEXT');
  },
  helpUrl : function() {
    var op = this.getTitleValue('BOOL');
    return Blockly.Language.logic_boolean.HELPURLS[op];},
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(
        Blockly.Language.logic_boolean.OPERATORS, this.getTitleValue('BOOL'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.logic_negate = {
  // Negation.
  category : Blockly.LANG_CATEGORY_LOGIC,
  helpUrl : Blockly.LANG_LOGIC_NEGATE_HELPURL,
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('BOOL').setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT)).appendTitle('not');
    this.setTooltip(Blockly.LANG_LOGIC_NEGATE_TOOLTIP);
    this.appendCollapsedInput().appendTitle('not', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName:Blockly.LANG_LOGIC_NEGATE_INPUT_NOT }]
};

Blockly.Language.logic_compare = {
  // Comparison operator.
  category : Blockly.LANG_CATEGORY_LOGIC,
  helpUrl : function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.logic_compare.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('A');
    this.appendValueInput('B').appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.logic_compare.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle(
        Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP')), 'COLLAPSED_TEXT');
  },
  //TODO (user) compare has not been internationalized yet
  // Potential clash with Math =, so using 'logic equal' for now
  typeblock: [{ translatedName: 'logic equal' }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.logic_compare.TOOLTIPS = {
  EQ: 'Tests whether two things are equal. \n' +
        'The things being compared can be any thing, not only numbers.',
  NEQ: 'Tests whether two things are not equal. \n' +
        'The things being compared can be any thing, not only numbers.'
};

Blockly.Language.logic_compare.HELPURLS = {
  EQ: Blockly.LANG_LOGIC_COMPARE_HELPURL_EQ,
  NEQ: Blockly.LANG_LOGIC_COMPARE_HELPURL_NEQ
};

Blockly.Language.logic_compare.OPERATORS =
  [['=', 'EQ'],
   ['\u2260', 'NEQ']];

Blockly.Language.logic_operation = {
  // Logical operations: 'and', 'or'.
  category: Blockly.LANG_CATEGORY_LOGIC,
  init: function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('OP');
      return Blockly.Language.logic_operation.TOOLTIPS[op];
    });
    this.appendCollapsedInput().appendTitle('and', 'COLLAPSED_TEXT');
  },
  helpUrl: function() {
      var op = this.getTitleValue('OP');
      return Blockly.Language.logic_operation.HELPURLS[op];
    },
  typeblock: [{
    translatedName: Blockly.LANG_LOGIC_OPERATION_AND,
    dropDown: {
      titleName: 'OP',
      value: 'AND'
    }
  },{
    translatedName: Blockly.LANG_LOGIC_OPERATION_OR,
    dropDown: {
      titleName: 'OP',
      value: 'OR'
    }
  }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.logic_operation.OPERATORS =
    [[Blockly.LANG_LOGIC_OPERATION_AND, 'AND'],
     [Blockly.LANG_LOGIC_OPERATION_OR, 'OR']];

Blockly.Language.logic_operation.HELPURLS = {
  AND : Blockly.LANG_LOGIC_OPERATION_HELPURL_AND,
  OR : Blockly.LANG_LOGIC_OPERATION_HELPURL_OR
};
Blockly.Language.logic_operation.TOOLTIPS = {
  AND : Blockly.LANG_LOGIC_OPERATION_TOOLTIP_AND,
  OR : Blockly.LANG_LOGIC_OPERATION_TOOLTIP_OR
};


Blockly.Language.logic_or = {
  // Logical operations: 'and', 'or'.
  category: Blockly.LANG_CATEGORY_LOGIC,
  init: function() {
    this.setColour(Blockly.LOGIC_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.logic_operation.OPERATORS), 'OP');
    this.setTitleValue('OR','OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var op = thisBlock.getTitleValue('OP');
      return Blockly.Language.logic_operation.TOOLTIPS[op];
    });
    this.appendCollapsedInput().appendTitle('or', 'COLLAPSED_TEXT');
  },
  helpUrl: function() {
      var op = this.getTitleValue('OP');
      return Blockly.Language.logic_operation.HELPURLS[op];
    },
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(
        Blockly.Language.logic_operation.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

