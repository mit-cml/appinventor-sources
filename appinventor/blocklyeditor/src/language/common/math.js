/**
.setAlign(Blockly.ALIGN_RIGHT) * Visual Blocks Language
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

// TODO(andrew): Change addition, multiplication, min, and max to take multiple arguments.
// TODO(andrew): Add appropriate helpurls for each block.

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.math_number = {
  // Numeric value.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : Blockly.LANG_MATH_NUMBER_HELPURL,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.appendDummyInput().appendTitle(
        new Blockly.FieldTextInput('0', Blockly.Language.math_number.validator), 'NUM');
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.setTooltip(Blockly.LANG_MATH_NUMBER_TOOLTIP);
    this.appendCollapsedInput().appendTitle('0', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER }],
  prepareCollapsedText: function(){
    var textToDisplay = this.getTitleValue('NUM');
    if (textToDisplay.length > 8 ) // 8 is a length of 5 plus 3 dots
      textToDisplay = textToDisplay.substring(0, 5) + '...';
    this.getTitle_('COLLAPSED_TEXT').setText(textToDisplay, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.math_number.validator = function(text) {
  // Ensure that only a number may be entered.
  // TODO: Handle cases like 'o', 'ten', '1,234', '3,14', etc.
  var n = window.parseFloat(text || 0);
  return window.isNaN(n) ? null : String(n);
};

Blockly.Language.math_compare = {
  // Basic arithmetic operator.
  // TODO(Andrew): equality block needs to have any on the sockets.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_compare.HELPURLS[mode];},
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('A').setCheck(null);
    this.appendValueInput('B').setCheck(null).appendTitle(new Blockly.FieldDropdown(this.OPERATORS,Blockly.Language.math_compare.onchange), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_compare.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle('=', 'COLLAPSED_TEXT');
  },
  //TODO (user) compare has not been internationalized yet
  // Potential clash with logic equal, using '=' for now
  typeblock: [{
    translatedName: '=',
    dropDown: {
      titleName: 'OP',
      value: 'EQ'
    }
  },{
    translatedName: '\u2260',
    dropDown: {
      titleName: 'OP',
      value: 'NEQ'
    }
  },{
    translatedName: '<',
    dropDown: {
      titleName: 'OP',
      value: 'LT'
    }
  },{
    translatedName: '\u2264',
    dropDown: {
      titleName: 'OP',
      value: 'LTE'
    }
  },{
    translatedName: '>',
    dropDown: {
      titleName: 'OP',
      value: 'GT'
    }
  },{
    translatedName: '\u2265',
    dropDown: {
      titleName: 'OP',
      value: 'GTE'
    }
  }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.math_compare.onchange = function(value){
  if(!this.sourceBlock_){return;}
  if(value == "EQ" || value == "NEQ") {
    this.sourceBlock_.getInput("A").setCheck(null);
    this.sourceBlock_.getInput("B").setCheck(null);
  } else {
    this.sourceBlock_.getInput("A").setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    this.sourceBlock_.getInput("B").setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
  }
};

Blockly.Language.math_compare.OPERATORS =
  [['=', 'EQ'],
   ['\u2260', 'NEQ'],
   ['<', 'LT'],
   ['\u2264', 'LTE'],
   ['>', 'GT'],
   ['\u2265', 'GTE']];

Blockly.Language.math_compare.TOOLTIPS = {
  EQ: Blockly.LANG_MATH_COMPARE_TOOLTIP_EQ,
  NEQ: Blockly.LANG_MATH_COMPARE_TOOLTIP_NEQ,
  LT: Blockly.LANG_MATH_COMPARE_TOOLTIP_LT,
  LTE: Blockly.LANG_MATH_COMPARE_TOOLTIP_LTE,
  GT: Blockly.LANG_MATH_COMPARE_TOOLTIP_GT,
  GTE: Blockly.LANG_MATH_COMPARE_TOOLTIP_GTE
};

Blockly.Language.math_compare.HELPURLS = {
  EQ: Blockly.LANG_MATH_COMPARE_HELPURL_EQ,
  NEQ: Blockly.LANG_MATH_COMPARE_HELPURL_NEQ,
  LT: Blockly.LANG_MATH_COMPARE_HELPURL_LT,
  LTE: Blockly.LANG_MATH_COMPARE_HELPURL_LTE,
  GT: Blockly.LANG_MATH_COMPARE_HELPURL_GT,
  GTE: Blockly.LANG_MATH_COMPARE_HELPURL_GTE
};

Blockly.Language.math_add = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ARITHMETIC_HELPURL_ADD,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM0').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    // append the title on a separate line to avoid overly long lines
    this.appendValueInput('NUM1').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
        .appendTitle("+");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_ADD;
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
    this.appendCollapsedInput().appendTitle('+', 'COLLAPSED_TEXT');
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'math_mutator_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    var input = this.appendDummyInput(this.emptyInputName);
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    if(inputNum !== 0){
      input.appendTitle("+");
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setTitleValue("+","CONTAINER_TEXT");
  },
  //TODO (user) add has not been internationalized yet
  // Using '+' for now
  typeblock: [{ translatedName: '+' }]
};

Blockly.Language.math_mutator_item = {
  // Add items.
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.appendDummyInput()
        //.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE);
        .appendTitle("number");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    //this.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP_1);
    this.contextMenu = false;
  }
};


Blockly.Language.math_subtract = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ARITHMETIC_HELPURL_MINUS ,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
        .appendTitle("-");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS;
    });
    this.appendCollapsedInput().appendTitle('-', 'COLLAPSED_TEXT');
  },
  //TODO (user) subtract has not been internationalized yet
  // Using '-' for now
  typeblock: [{ translatedName: '-' }]
};

Blockly.Language.math_multiply = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ARITHMETIC_HELPURL_MULTIPLY,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM0').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    this.appendValueInput('NUM1').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
        .appendTitle(Blockly.Language.times_symbol);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY ;
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
    this.appendCollapsedInput().appendTitle(Blockly.Language.times_symbol, 'COLLAPSED_TEXT');
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'math_mutator_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    var input = this.appendDummyInput(this.emptyInputName);
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    if(inputNum !== 0){
      input.appendTitle(Blockly.Language.times_symbol);
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setTitleValue(Blockly.Language.times_symbol,"CONTAINER_TEXT");
  },
  //TODO (user) multiply has not been internationalized yet
  // Using '*' for now
  typeblock: [{ translatedName:  '*' }]
};

Blockly.Language.math_division = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ARITHMETIC_HELPURL_DIVIDE,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
        .appendTitle('/');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE;
    });
    this.appendCollapsedInput().appendTitle('/', 'COLLAPSED_TEXT');
  },
  //TODO (user) division has not been internationalized yet
  // Using '/' for now
  typeblock: [{ translatedName: '/' }]
};

Blockly.Language.math_power = {
  // Basic arithmetic operator.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_ARITHMETIC_HELPURL_POWER,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
        .appendTitle("^");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      //var mode = thisBlock.getTitleValue('OP');
      return Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_POWER;
    });
    this.appendCollapsedInput().appendTitle('^', 'COLLAPSED_TEXT');
  },
  //TODO (user) power has not been internationalized yet
  // Using '^' for now
  typeblock: [{ translatedName: '^' }]
};

Blockly.Language.math_random_int = {
  // Random integer between [X] and [Y].
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_RANDOM_INT_HELPURL,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('FROM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('random integer').appendTitle('from');
    this.appendValueInput('TO').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('to');
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_MATH_RANDOM_INT_TOOLTIP );
    this.appendCollapsedInput().appendTitle(Blockly.LANG_MATH_RANDOM_INT_TITLE_RANDOM, 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_MATH_RANDOM_INT_TITLE_RANDOM }]
};

Blockly.Language.math_random_float = {
  // Random fraction between 0 and 1.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_RANDOM_FLOAT_HELPURL,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendDummyInput().appendTitle('random fraction');
    this.setTooltip(Blockly.LANG_MATH_RANDOM_FLOAT_TOOLTIP);
    this.appendCollapsedInput().appendTitle(Blockly.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM, 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM }]
};

Blockly.Language.math_random_set_seed = {
  // Set the seed of the radom number generator
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: Blockly.LANG_MATH_RANDOM_SEED_HELPURL,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(false, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('random set seed').appendTitle('to');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_MATH_RANDOM_SEED_TOOLTIP);
    this.appendCollapsedInput().appendTitle(Blockly.LANG_MATH_RANDOM_SEED_TITLE_RANDOM, 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_MATH_RANDOM_SEED_TITLE_RANDOM }]
};

Blockly.Language.math_on_list = {
  // Evaluate a list of numbers to return sum, average, min, max, etc.
  // Some functions also work on text (min, max, mode, median).
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM0').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.appendValueInput('NUM1').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    this.setInputsInline(false);
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_on_list.TOOLTIPS[mode];
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.itemCount_ = 2;
    this.valuesToSave = {'OP':null};
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.appendCollapsedInput().appendTitle(this.getTitleValue('OP'), 'COLLAPSED_TEXT');

  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'math_mutator_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    var input = this.appendDummyInput(this.emptyInputName);
    input.appendTitle(new Blockly.FieldDropdown(this.OPERATORS),'OP');
    this.setTitleValue(this.valuesToSave['OP'],'OP');
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT));
    if(inputNum == 0){
      input.appendTitle(new Blockly.FieldDropdown(this.OPERATORS),'OP');
      this.setTitleValue(this.valuesToSave['OP'],'OP');
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {

    for(var i=0;i<Blockly.Language.math_on_list.OPERATORS.length;i++){
      if(Blockly.Language.math_on_list.OPERATORS[i][1] == this.getTitleValue("OP") ){
        containerBlock.setTitleValue(Blockly.Language.math_on_list.OPERATORS[i][0],"CONTAINER_TEXT");
      }
    }

  },
  typeblock: [{
    translatedName: Blockly.LANG_MATH_ONLIST_OPERATOR_MIN,
    dropDown: {
      titleName: 'OP',
      value: 'MIN'
    }
  },{
    translatedName: Blockly.LANG_MATH_ONLIST_OPERATOR_MAX,
    dropDown: {
      titleName: 'OP',
      value: 'MAX'
    }
  }],
  prepareCollapsedText: function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.getTitleValue('OP').toLowerCase(), 'COLLAPSED_TEXT');
  }
};

Blockly.Language.math_on_list.OPERATORS =
  [['min', 'MIN'],
   ['max', 'MAX']];

Blockly.Language.math_on_list.TOOLTIPS = {
  MIN: 'Return the smallest of its arguments..',
  MAX: 'Return the largest of its arguments..'
};


Blockly.Language.math_single = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle(this.getTitleValue('OP'), 'COLLAPSED_TEXT');
  },
  typeblock: [{
    translatedName: Blockly.LANG_MATH_SINGLE_OP_ROOT,
    dropDown: {
      titleName: 'OP',
      value: 'ROOT'
    }
  },{
    translatedName: Blockly.LANG_MATH_SINGLE_OP_ABSOLUTE,
    dropDown: {
      titleName: 'OP',
      value: 'ABS'
    }
  },{
    translatedName: 'neg',
    dropDown: {
      titleName: 'OP',
      value: 'NEG'
    }
  },{
    translatedName: 'log',
    dropDown: {
      titleName: 'OP',
      value: 'LN'
    }
  },{
    translatedName: 'e^',
    dropDown: {
      titleName: 'OP',
      value: 'EXP'
    }
  },{
    translatedName: Blockly.LANG_MATH_ROUND_OPERATOR_ROUND,
    dropDown: {
      titleName: 'OP',
      value: 'ROUND'
    }
  },{
    translatedName: Blockly.LANG_MATH_ROUND_OPERATOR_CEILING,
    dropDown: {
      titleName: 'OP',
      value: 'CEILING'
    }
  },{
    translatedName: Blockly.LANG_MATH_ROUND_OPERATOR_FLOOR,
    dropDown: {
      titleName: 'OP',
      value: 'FLOOR'
    }
  }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.math_single.OPERATORS =
  [['sqrt', 'ROOT'],
   ['abs', 'ABS'],
   ['-', 'NEG'],
   ['log', 'LN'],
   ['e^', 'EXP'],
   ['round', 'ROUND'],
   ['ceiling', 'CEILING'],
   ['floor', 'FLOOR']];

Blockly.Language.math_single.TOOLTIPS = {
  ROOT: Blockly.LANG_MATH_SINGLE_TOOLTIP_ROOT,
  ABS: Blockly.LANG_MATH_SINGLE_TOOLTIP_ABS,
  NEG: Blockly.LANG_MATH_SINGLE_TOOLTIP_NEG,
  LN: Blockly.LANG_MATH_SINGLE_TOOLTIP_LN,
  EXP: Blockly.LANG_MATH_SINGLE_TOOLTIP_EXP,
  ROUND : Blockly.LANG_MATH_ROUND_TOOLTIP_ROUND,
  CEILING : Blockly.LANG_MATH_ROUND_TOOLTIP_CEILING,
  FLOOR : Blockly.LANG_MATH_ROUND_TOOLTIP_FLOOR
};
Blockly.Language.math_single.HELPURLS = {
  ROOT: Blockly.LANG_MATH_SINGLE_HELPURL_ROOT,
  ABS: Blockly.LANG_MATH_SINGLE_HELPURL_ABS,
  NEG: Blockly.LANG_MATH_SINGLE_HELPURL_NEG,
  LN: Blockly.LANG_MATH_SINGLE_HELPURL_LN,
  EXP: Blockly.LANG_MATH_SINGLE_HELPURL_EXP,
  ROUND : Blockly.LANG_MATH_ROUND_HELPURL_ROUND,
  CEILING : Blockly.LANG_MATH_ROUND_HELPURL_CEILING,
  FLOOR : Blockly.LANG_MATH_ROUND_HELPURL_FLOOR
};

Blockly.Language.math_abs = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.math_single.OPERATORS), 'OP');
    this.setTitleValue('ABS',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_neg = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.math_single.OPERATORS), 'OP');
    this.setTitleValue('NEG',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
  }
};


Blockly.Language.math_round = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.math_single.OPERATORS), 'OP');
    this.setTitleValue('ROUND',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
  }
};


Blockly.Language.math_ceiling = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.math_single.OPERATORS), 'OP');
    this.setTitleValue('CEILING',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_floor = {
  // Advanced math operators with single operand.
  category: Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.math_single.OPERATORS), 'OP');
    this.setTitleValue('FLOOR',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_single.TOOLTIPS[mode];
    });
  }
};


Blockly.Language.math_divide = {
  // Remainder or quotient of a division.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_divide.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('DIVIDEND').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.appendValueInput('DIVISOR').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('\u00F7');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_divide.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle(this.getTitleValue('OP'), 'COLLAPSED_TEXT');
  },
  typeblock: [{
    translatedName: Blockly.LANG_MATH_DIVIDE_OPERATOR_MODULO,
    dropDown: {
      titleName: 'OP',
      value: 'MODULO'
    }
  },{
    translatedName: Blockly.LANG_MATH_DIVIDE_OPERATOR_REMAINDER,
    dropDown: {
      titleName: 'OP',
      value: 'REMAINDER'
    }
  },{
    translatedName: Blockly.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT,
    dropDown: {
      titleName: 'OP',
      value: 'QUOTIENT'
    }
  }],
  prepareCollapsedText: function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.getTitleValue('OP').toLowerCase(), 'COLLAPSED_TEXT');
  }
};

Blockly.Language.math_divide.OPERATORS =
  [['modulo of', 'MODULO'],
   ['remainder of', 'REMAINDER'],
   [ 'quotient of', 'QUOTIENT' ]];

Blockly.Language.math_divide.TOOLTIPS = {
  MODULO: Blockly.LANG_MATH_DIVIDE_TOOLTIP_MODULO,
  REMAINDER: Blockly.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER,
  QUOTIENT: Blockly.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT
};
Blockly.Language.math_divide.HELPURLS = {
  MODULO: Blockly.LANG_MATH_DIVIDE_HELPURL_MODULO,
  REMAINDER: Blockly.LANG_MATH_DIVIDE_HELPURL_REMAINDER,
  QUOTIENT: Blockly.LANG_MATH_DIVIDE_HELPURL_QUOTIENT
};

Blockly.Language.math_trig = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_trig.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_trig.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle(this.getTitleValue('OP'), 'COLLAPSED_TEXT');
  },
  //TODO (user) sine has not been internationalized yet
  //Using 'sine' for now
  typeblock: [{
    translatedName: 'sin',
    dropDown: {
      titleName: 'OP',
      value: 'SIN'
    }
  },{
    translatedName: 'cos',
    dropDown: {
      titleName: 'OP',
      value: 'COS'
    }
  },{
    translatedName: 'tan',
    dropDown: {
      titleName: 'OP',
      value: 'TAN'
    }
  },{
    translatedName: 'asin',
    dropDown: {
      titleName: 'OP',
      value: 'ASIN'
    }
  },{
    translatedName: 'acos',
    dropDown: {
      titleName: 'OP',
      value: 'ACOS'
    }
  },{
    translatedName: 'atan',
    dropDown: {
      titleName: 'OP',
      value: 'ATAN'
    }
  }],
  prepareCollapsedText: function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.getTitleValue('OP').toLowerCase(), 'COLLAPSED_TEXT');
  }
};

Blockly.Language.math_trig.OPERATORS =
  [[ 'sin', 'SIN' ],
   [ 'cos', 'COS' ],
   [ 'tan', 'TAN' ],
   [ 'asin', 'ASIN' ],
   [ 'acos', 'ACOS' ],
   [ 'atan', 'ATAN' ]];

Blockly.Language.math_trig.TOOLTIPS = {
  SIN : Blockly.LANG_MATH_TRIG_TOOLTIP_SIN,
  COS : Blockly.LANG_MATH_TRIG_TOOLTIP_COS,
  TAN : Blockly.LANG_MATH_TRIG_TOOLTIP_TAN,
  ASIN : Blockly.LANG_MATH_TRIG_TOOLTIP_ASIN,
  ACOS : Blockly.LANG_MATH_TRIG_TOOLTIP_ACOS,
  ATAN : Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN
};

Blockly.Language.math_trig.HELPURLS = {
  SIN : Blockly.LANG_MATH_TRIG_HELPURL_SIN,
  COS : Blockly.LANG_MATH_TRIG_HELPURL_COS,
  TAN : Blockly.LANG_MATH_TRIG_HELPURL_TAN,
  ASIN : Blockly.LANG_MATH_TRIG_HELPURL_ASIN,
  ACOS : Blockly.LANG_MATH_TRIG_HELPURL_ACOS,
  ATAN : Blockly.LANG_MATH_TRIG_HELPURL_ATAN
};

Blockly.Language.math_cos = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_trig.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.math_trig.OPERATORS), 'OP');
    this.setTitleValue('COS',"OP");
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_trig.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_tan = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_trig.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle(new Blockly.FieldDropdown(Blockly.Language.math_trig.OPERATORS), 'OP');
    this.setTitleValue('TAN',"OP");
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_trig.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.math_atan2 = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : Blockly.LANG_MATH_TRIG_HELPURL_ATAN2,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendDummyInput().appendTitle('atan2');
    this.appendValueInput('Y').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('y').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('X').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('x').setAlign(Blockly.ALIGN_RIGHT);
    this.setInputsInline(false);
    this.setTooltip(Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN2);
    this.appendCollapsedInput().appendTitle('atan2', 'COLLAPSED_TEXT');
  },
  //TODO (user) atan2 has not been internationalized yet
  // Using 'atan2' for now
  typeblock: [{ translatedName: 'atan2' }]
};

Blockly.Language.math_convert_angles = {
  // Trigonometry operators.
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.math_convert_angles.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('convert').appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.math_convert_angles.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle(this.getTitleValue('OP'), 'COLLAPSED_TEXT');
  },
  typeblock: [{
    translatedName: Blockly.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT +
        ' ' + Blockly.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG,
    dropDown: {
      titleName: 'OP',
      value: 'RADIANS_TO_DEGREES'
    }
  },{
    translatedName: Blockly.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT+
        ' ' + Blockly.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD,
    dropDown: {
      titleName: 'OP',
      value: 'DEGREES_TO_RADIANS'
    }
  }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.math_convert_angles.OPERATORS =
  [[ 'radians to degrees', 'RADIANS_TO_DEGREES' ],
   [ 'degrees to radians', 'DEGREES_TO_RADIANS' ]];

Blockly.Language.math_convert_angles.TOOLTIPS = {
  RADIANS_TO_DEGREES : Blockly.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG,
  DEGREES_TO_RADIANS : Blockly.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD
};

Blockly.Language.math_convert_angles.HELPURLS = {
  RADIANS_TO_DEGREES : Blockly.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG,
  DEGREES_TO_RADIANS : Blockly.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD
};

Blockly.Language.math_format_as_decimal = {
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : Blockly.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendDummyInput().appendTitle('format as decimal');
    this.appendValueInput('NUM').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('number').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('PLACES').setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT)).appendTitle('places').setAlign(Blockly.ALIGN_RIGHT);
    this.setInputsInline(false);
    this.setTooltip(Blockly.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP);
    this.appendCollapsedInput().appendTitle('format decimal', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_MATH_FORMAT_AS_DECIMAL_TITLE }]
};

Blockly.Language.math_is_a_number = {
  category : Blockly.LANG_CATEGORY_MATH,
  helpUrl : Blockly.LANG_MATH_IS_A_NUMBER_HELPURL,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('NUM').appendTitle('is a number?');
    this.setTooltip(function() {
      return Blockly.LANG_MATH_IS_A_NUMBER_TOOLTIP;
    });
    this.appendCollapsedInput().appendTitle('number?', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_MATH_IS_A_NUMBER_INPUT_NUM }]
};
