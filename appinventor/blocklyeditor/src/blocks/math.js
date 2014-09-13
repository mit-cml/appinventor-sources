// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Math blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Blocks.math');

goog.require('Blockly.Blocks.Utilities');

Blockly.Blocks['math_number'] = {
  // Numeric value.
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl : Blockly.Msg.LANG_MATH_NUMBER_HELPURL,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.appendDummyInput().appendField(
        new Blockly.FieldTextInput('0', Blockly.Blocks.math_number.validator), 'NUM');
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER }]
};

Blockly.Blocks.math_number.validator = function(text) {
  // Ensure that only a number may be entered.
  // TODO: Handle cases like 'o', 'ten', '1,234', '3,14', etc.
  var n = window.parseFloat(text || 0);
  return window.isNaN(n) ? null : String(n);
};

Blockly.Blocks['math_compare'] = {
  // Basic arithmetic operator.
  // TODO(Andrew): equality block needs to have any on the sockets.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_compare.HELPURLS[mode];},
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A').setCheck(null);
    this.appendValueInput('B').setCheck(null).appendField(new Blockly.FieldDropdown(this.OPERATORS,Blockly.Blocks.math_compare.onchange), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_compare.TOOLTIPS[mode];
    });
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
  }]
};

Blockly.Blocks.math_compare.onchange = function(value){
  if(!this.sourceBlock_){return;}
  if(value == "EQ" || value == "NEQ") {
    this.sourceBlock_.getInput("A").setCheck(null);
    this.sourceBlock_.getInput("B").setCheck(null);
  } else {
    this.sourceBlock_.getInput("A").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    this.sourceBlock_.getInput("B").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
  }
};

Blockly.Blocks.math_compare.OPERATORS =
  [['=', 'EQ'],
   ['\u2260', 'NEQ'],
   ['<', 'LT'],
   ['\u2264', 'LTE'],
   ['>', 'GT'],
   ['\u2265', 'GTE']];

Blockly.Blocks.math_compare.TOOLTIPS = {
  EQ: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ,
  NEQ: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ,
  LT: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT,
  LTE: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE,
  GT: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT,
  GTE: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE
};

Blockly.Blocks.math_compare.HELPURLS = {
  EQ: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ,
  NEQ: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ,
  LT: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT,
  LTE: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE,
  GT: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT,
  GTE: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE
};

Blockly.Blocks['math_add'] = {
  // Basic arithmetic operator.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_ADD,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM0').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    // append the title on a separate line to avoid overly long lines
    this.appendValueInput('NUM1').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT))
        .appendField("+");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD;
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
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
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    if(inputNum !== 0){
      input.appendField("+");
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setFieldValue("+","CONTAINER_TEXT");
  },
  //TODO (user) add has not been internationalized yet
  // Using '+' for now
  typeblock: [{ translatedName: '+' }]
};

Blockly.Blocks['math_mutator_item'] = {
  // Add items.
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.appendDummyInput()
        //.appendField(Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE);
        .appendField("number");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    //this.setTooltip(Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP_1);
    this.contextMenu = false;
  }
};


Blockly.Blocks['math_subtract'] = {
  // Basic arithmetic operator.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MINUS ,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT))
        .appendField("-");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    this.setTooltip(Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS);
  },
  //TODO (user) subtract has not been internationalized yet
  // Using '-' for now
  typeblock: [{ translatedName: '-' }]
};

Blockly.Blocks['math_multiply'] = {
  // Basic arithmetic operator.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MULTIPLY,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM0').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('NUM1').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Blocks.Utilities.times_symbol);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      return Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY ;
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
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
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    if(inputNum !== 0){
      input.appendField(Blockly.Blocks.Utilities.times_symbol);
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.setFieldValue(Blockly.Blocks.Utilities.times_symbol,"CONTAINER_TEXT");
  },
  //TODO (user) multiply has not been internationalized yet
  // Using '*' for now
  typeblock: [{ translatedName:  '*' }]
};

Blockly.Blocks['math_division'] = {
  // Basic arithmetic operator.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_DIVIDE,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT))
        .appendField('/');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    this.setTooltip(Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE);
  },
  //TODO (user) division has not been internationalized yet
  // Using '/' for now
  typeblock: [{ translatedName: '/' }]
};

Blockly.Blocks['math_power'] = {
  // Basic arithmetic operator.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_POWER,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('A').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    this.appendValueInput('B').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT))
        .appendField("^");
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER);
  },
  //TODO (user) power has not been internationalized yet
  // Using '^' for now
  typeblock: [{ translatedName: '^' }]
};

Blockly.Blocks['math_random_int'] = {
  // Random integer between [X] and [Y].
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('FROM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('random integer').appendField('from');
    this.appendValueInput('TO').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('to');
    this.setInputsInline(true);
    this.setTooltip(Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP );
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM }]
};

Blockly.Blocks['math_random_float'] = {
  // Random fraction between 0 and 1.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput().appendField('random fraction');
    this.setTooltip(Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM }]
};

Blockly.Blocks['math_random_set_seed'] = {
  // Set the seed of the radom number generator
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL,
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(false, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('random set seed').appendField('to');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM }]
};

Blockly.Blocks['math_on_list'] = {
  // Evaluate a list of numbers to return sum, average, min, max, etc.
  // Some functions also work on text (min, max, mode, median).
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: '',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM0').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.appendValueInput('NUM1').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    this.setInputsInline(false);
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_on_list.TOOLTIPS[mode];
    });
    this.setMutator(new Blockly.Mutator(['math_mutator_item']));
    this.itemCount_ = 2;
    this.valuesToSave = {'OP':null};
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
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
    input.appendField(new Blockly.FieldDropdown(this.OPERATORS),'OP');
    this.setFieldValue(this.valuesToSave['OP'],'OP');
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT));
    if(inputNum == 0){
      input.appendField(new Blockly.FieldDropdown(this.OPERATORS),'OP');
      this.setFieldValue(this.valuesToSave['OP'],'OP');
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {

    for(var i=0;i<Blockly.Blocks.math_on_list.OPERATORS.length;i++){
      if(Blockly.Blocks.math_on_list.OPERATORS[i][1] == this.getFieldValue("OP") ){
        containerBlock.setFieldValue(Blockly.Blocks.math_on_list.OPERATORS[i][0],"CONTAINER_TEXT");
      }
    }

  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN,
    dropDown: {
      titleName: 'OP',
      value: 'MIN'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX,
    dropDown: {
      titleName: 'OP',
      value: 'MAX'
    }
  }]
};

Blockly.Blocks.math_on_list.OPERATORS =
  [['min', 'MIN'],
   ['max', 'MAX']];

Blockly.Blocks.math_on_list.TOOLTIPS = {
  MIN: 'Return the smallest of its arguments..',
  MAX: 'Return the largest of its arguments..'
};


Blockly.Blocks['math_single'] = {
  // Advanced math operators with single operand.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT,
    dropDown: {
      titleName: 'OP',
      value: 'ROOT'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE,
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
    translatedName: Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND,
    dropDown: {
      titleName: 'OP',
      value: 'ROUND'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING,
    dropDown: {
      titleName: 'OP',
      value: 'CEILING'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR,
    dropDown: {
      titleName: 'OP',
      value: 'FLOOR'
    }
  }]
};

Blockly.Blocks.math_single.OPERATORS =
  [['sqrt', 'ROOT'],
   ['abs', 'ABS'],
   ['-', 'NEG'],
   ['log', 'LN'],
   ['e^', 'EXP'],
   ['round', 'ROUND'],
   ['ceiling', 'CEILING'],
   ['floor', 'FLOOR']];

Blockly.Blocks.math_single.TOOLTIPS = {
  ROOT: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT,
  ABS: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS,
  NEG: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG,
  LN: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN,
  EXP: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP,
  ROUND : Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND,
  CEILING : Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING,
  FLOOR : Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR
};
Blockly.Blocks.math_single.HELPURLS = {
  ROOT: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT,
  ABS: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS,
  NEG: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG,
  LN: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN,
  EXP: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP,
  ROUND : Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND,
  CEILING : Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING,
  FLOOR : Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR
};

Blockly.Blocks['math_abs'] = {
  // Advanced math operators with single operand.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('ABS',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS[mode];
    });
  }
};

Blockly.Blocks['math_neg'] = {
  // Advanced math operators with single operand.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('NEG',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS[mode];
    });
  }
};


Blockly.Blocks['math_round'] = {
  // Advanced math operators with single operand.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('ROUND',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS[mode];
    });
  }
};


Blockly.Blocks['math_ceiling'] = {
  // Advanced math operators with single operand.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('CEILING',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS[mode];
    });
  }
};

Blockly.Blocks['math_floor'] = {
  // Advanced math operators with single operand.
  category: Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_single.HELPURLS[mode];
    },
  init: function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('FLOOR',"OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS[mode];
    });
  }
};


Blockly.Blocks['math_divide'] = {
  // Remainder or quotient of a division.
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_divide.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('DIVIDEND').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.appendValueInput('DIVISOR').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('\u00F7');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_divide.TOOLTIPS[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO,
    dropDown: {
      titleName: 'OP',
      value: 'MODULO'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER,
    dropDown: {
      titleName: 'OP',
      value: 'REMAINDER'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT,
    dropDown: {
      titleName: 'OP',
      value: 'QUOTIENT'
    }
  }]
};

Blockly.Blocks.math_divide.OPERATORS =
  [['modulo of', 'MODULO'],
   ['remainder of', 'REMAINDER'],
   [ 'quotient of', 'QUOTIENT' ]];

Blockly.Blocks.math_divide.TOOLTIPS = {
  MODULO: Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO,
  REMAINDER: Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER,
  QUOTIENT: Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT
};
Blockly.Blocks.math_divide.HELPURLS = {
  MODULO: Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO,
  REMAINDER: Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER,
  QUOTIENT: Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT
};

Blockly.Blocks['math_trig'] = {
  // Trigonometry operators.
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_trig.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_trig.TOOLTIPS[mode];
    });
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
  }]
};

Blockly.Blocks.math_trig.OPERATORS =
  [[ 'sin', 'SIN' ],
   [ 'cos', 'COS' ],
   [ 'tan', 'TAN' ],
   [ 'asin', 'ASIN' ],
   [ 'acos', 'ACOS' ],
   [ 'atan', 'ATAN' ]];

Blockly.Blocks.math_trig.TOOLTIPS = {
  SIN : Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN,
  COS : Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS,
  TAN : Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN,
  ASIN : Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN,
  ACOS : Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS,
  ATAN : Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN
};

Blockly.Blocks.math_trig.HELPURLS = {
  SIN : Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN,
  COS : Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS,
  TAN : Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN,
  ASIN : Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN,
  ACOS : Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS,
  ATAN : Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN
};

Blockly.Blocks['math_cos'] = {
  // Trigonometry operators.
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_trig.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_trig.OPERATORS), 'OP');
    this.setFieldValue('COS',"OP");
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_trig.TOOLTIPS[mode];
    });
  }
};

Blockly.Blocks['math_tan'] = {
  // Trigonometry operators.
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_trig.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_trig.OPERATORS), 'OP');
    this.setFieldValue('TAN',"OP");
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_trig.TOOLTIPS[mode];
    });
  }
};

Blockly.Blocks['math_atan2'] = {
  // Trigonometry operators.
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl : Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput().appendField('atan2');
    this.appendValueInput('Y').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('y').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('X').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('x').setAlign(Blockly.ALIGN_RIGHT);
    this.setInputsInline(false);
    this.setTooltip(Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2);
  },
  //TODO (user) atan2 has not been internationalized yet
  // Using 'atan2' for now
  typeblock: [{ translatedName: 'atan2' }]
};

Blockly.Blocks['math_convert_angles'] = {
  // Trigonometry operators.
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_convert_angles.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('convert').appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_convert_angles.TOOLTIPS[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT +
        ' ' + Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG,
    dropDown: {
      titleName: 'OP',
      value: 'RADIANS_TO_DEGREES'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT+
        ' ' + Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD,
    dropDown: {
      titleName: 'OP',
      value: 'DEGREES_TO_RADIANS'
    }
  }]
};

Blockly.Blocks.math_convert_angles.OPERATORS =
  [[ 'radians to degrees', 'RADIANS_TO_DEGREES' ],
   [ 'degrees to radians', 'DEGREES_TO_RADIANS' ]];

Blockly.Blocks.math_convert_angles.TOOLTIPS = {
  RADIANS_TO_DEGREES : Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG,
  DEGREES_TO_RADIANS : Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD
};

Blockly.Blocks.math_convert_angles.HELPURLS = {
  RADIANS_TO_DEGREES : Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG,
  DEGREES_TO_RADIANS : Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD
};

Blockly.Blocks['math_format_as_decimal'] = {
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl : Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendDummyInput().appendField('format as decimal');
    this.appendValueInput('NUM').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('number').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('PLACES').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT)).appendField('places').setAlign(Blockly.ALIGN_RIGHT);
    this.setInputsInline(false);
    this.setTooltip(Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE }]
};

Blockly.Blocks['math_is_a_number'] = {
  category : Blockly.Msg.LANG_CATEGORY_MATH,
  helpUrl : Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL,
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('NUM').appendField('is a number?');
    this.setTooltip(function() {
      return Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP;
    });
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM }]
};
