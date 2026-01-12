// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Math blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Blocks.math');

goog.require('AI.BlockUtils');

Blockly.Blocks['math_number'] = {
  // Numeric value.
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_NUMBER_HELPURL,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.appendDummyInput().appendField(
        new Blockly.FieldTextInput('0', Blockly.Blocks.math_number.validator), 'NUM');
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER}]
};

/**
 * Ensures that only a number may be entered into the text field. Supports
 * decimal, binary, octal, and hex.
 * @param {string} text The text to check the validity of.
 * @return {string} Validated text.
 */
Blockly.Blocks.math_number.validator = function (text) {
  // TODO: Handle cases like 'o', 'ten', '1,234', '3,14', etc.
  var n = Number(text || 0);  // Number() supports binary, hex, etc.
  if (window.isNaN(n)) {
    // Fall back to old method. This is just UI-Behavior that doesn't affect
    // the generated code, but we might as well keep it the same.
    n = window.parseFloat(text || 0);
    return window.isNaN(n) ? null : String(n);
  }
  // Don't convert n to string, because that always returns decimal.
  return text;
};

Blockly.Blocks['math_number_radix'] = {
  category:'Math',

  helpUrl: Blockly.Msg.LANG_MATH_NUMBER_RADIX_HELPURL,

  init: function() {
    this.dropdown = new Blockly.FieldDropdown([
        [Blockly.Msg.LANG_MATH_DECIMAL_FORMAT, 'DEC'],
        [Blockly.Msg.LANG_MATH_BINARY_FORMAT, 'BIN'],
        [Blockly.Msg.LANG_MATH_OCTAL_FORMAT, 'OCT'],
        [Blockly.Msg.LANG_MATH_HEXADECIMAL_FORMAT, 'HEX'],
    ], this.dropdownListener);
    this.numberField = new Blockly.FieldTextInput('0', this.numberValidator);
    this.currentPrefix = Blockly.Blocks.math_number_radix.PREFIX['DEC'];

    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(this.dropdown, 'OP')
        .appendField(this.numberField, 'NUM');
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType(
        "number", AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_MATH_NUMBER_RADIX_TOOLTIP);
  },

  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_NUMBER_RADIX_TITLE}],

  dropdownListener: function(newValue) {
    this.getSourceBlock().currentPrefix = Blockly.Blocks.math_number_radix.PREFIX[newValue];
    var numberField = this.sourceBlock_.numberField;
    var oldPrefix = Blockly.Blocks.math_number_radix.PREFIX[this.getValue()];
    var oldValue = Number(oldPrefix + numberField.getValue());
    var newRadix = Blockly.Blocks.math_number_radix.RADIX[newValue];
    AI.Blockly.multiselect.withoutMultiFieldUpdates(function() {
        numberField.setValue(oldValue.toString(newRadix));
    });
  },

  numberValidator: function(text) {
    if (!text) {
      return 0;
    }
    var n = Number(this.getSourceBlock().currentPrefix + text);
    // Do not convert n to string, because that always returns decimal.
    return window.isNaN(n) ? null : text;
  }
};

// Maps dropdown value to radix prefix.
Blockly.Blocks.math_number_radix.PREFIX = {
  'DEC': '',
  'BIN': '0b',
  'OCT': '0o',
  'HEX': '0x'
};

// Maps dropdown value to radix.
Blockly.Blocks.math_number_radix.RADIX = {
  'DEC': 10,
  'BIN': 2,
  'OCT': 8,
  'HEX': 16
};

Blockly.Blocks['math_compare'] = {
  // Basic arithmetic operator.
  // TODO(Andrew): equality block needs to have any on the sockets.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_compare.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("boolean", AI.BlockUtils.OUTPUT));
    this.appendValueInput('A')
        .setCheck(null);
    this.appendValueInput('B')
        .setCheck(null)
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_compare.OPERATORS(), Blockly.Blocks.math_compare.onchange), 'OP');
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_compare.TOOLTIPS()[mode];
    });
  },
  // Potential clash with logic equal, using '=' for now
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_COMPARE_EQ,
    dropDown: {
      titleName: 'OP',
      value: 'EQ'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_COMPARE_NEQ,
    dropDown: {
      titleName: 'OP',
      value: 'NEQ'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_COMPARE_LT,
    dropDown: {
      titleName: 'OP',
      value: 'LT'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_COMPARE_LTE,
    dropDown: {
      titleName: 'OP',
      value: 'LTE'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_COMPARE_GT,
    dropDown: {
      titleName: 'OP',
      value: 'GT'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_COMPARE_GTE,
    dropDown: {
      titleName: 'OP',
      value: 'GTE'
    }
  }]
};

Blockly.Blocks.math_compare.onchange = function (value) {
  if (!this.sourceBlock_) {
    return;
  }
  if (value == "EQ" || value == "NEQ") {
    this.sourceBlock_.getInput("A").setCheck(null);
    this.sourceBlock_.getInput("B").setCheck(null);
  } else {
    this.sourceBlock_.getInput("A")
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    this.sourceBlock_.getInput("B")
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
  }
};

Blockly.Blocks.math_compare.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATH_COMPARE_EQ, 'EQ'],
    [Blockly.Msg.LANG_MATH_COMPARE_NEQ, 'NEQ'],
    [Blockly.Msg.LANG_MATH_COMPARE_LT, 'LT'],
    [Blockly.Msg.LANG_MATH_COMPARE_LTE, 'LTE'],
    [Blockly.Msg.LANG_MATH_COMPARE_GT, 'GT'],
    [Blockly.Msg.LANG_MATH_COMPARE_GTE, 'GTE']];
};

Blockly.Blocks.math_compare.TOOLTIPS = function () {
  return {
    EQ: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ,
    NEQ: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ,
    LT: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT,
    LTE: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE,
    GT: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT,
    GTE: Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE
  }
};

Blockly.Blocks.math_compare.HELPURLS = function () {
  return {
    EQ: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ,
    NEQ: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ,
    LT: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT,
    LTE: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE,
    GT: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT,
    GTE: Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE
  }
};

Blockly.Blocks['math_add'] = {
  // Basic arithmetic operator.
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_ADD,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    // append the title on a separate line to avoid overly long lines
    this.appendValueInput('NUM1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      return Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD;
    });
    this.setMutator(new Blockly.icons.MutatorIcon(['math_mutator_item'], this));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: function(container) {
    Blockly.domToMutation.call(this, container);

    // If we only have one input, put the + operator before it
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('0 ' + Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    }
  },
  decompose: function (workspace) {
    return Blockly.decompose(workspace, 'math_mutator_item', this);
  },
  compose: function(containerBlock) {
    Blockly.compose.call(this, containerBlock);

    // If we only have one input, put the + operator before it
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('0 ' + Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    }
  },
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function () {
    this.appendDummyInput(this.emptyInputName)
      .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
  },
  addInput: function (inputNum) {
    var input = this.appendValueInput(this.repeatingInputName + inputNum)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    if (inputNum !== 0) {
      input.appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_ADD);
    }
    return input;
  },
  updateContainerBlock: function (containerBlock) {
    containerBlock.setFieldValue("+", "CONTAINER_TEXT");
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_ADD}]
};

Blockly.Blocks['math_mutator_item'] = {
  // Add items.
  init: function () {
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
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MINUS,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('A')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    this.appendValueInput('B')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    this.setTooltip(Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS}]
};

Blockly.Blocks['math_multiply'] = {
  // Basic arithmetic operator.
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MULTIPLY,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    this.appendValueInput('NUM1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(AI.BlockUtils.times_symbol);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      return Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY;
    });
    this.setMutator(new Blockly.icons.MutatorIcon(['math_mutator_item'], this));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: function(container) {
    Blockly.domToMutation.call(this, container);
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('1 ' + AI.BlockUtils.times_symbol);
    }
  },
  decompose: function (workspace) {
    return Blockly.decompose(workspace, 'math_mutator_item', this);
  },
  compose: function(containerBlock) {
    Blockly.compose.call(this, containerBlock);
    if (this.itemCount_ === 1) {
      this.inputList[0].appendField('1 ' + AI.BlockUtils.times_symbol);
    }
  },
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function () {
    this.appendDummyInput(this.emptyInputName)
      .appendField(AI.BlockUtils.times_symbol);
  },
  addInput: function (inputNum) {
    var input = this.appendValueInput(this.repeatingInputName + inputNum)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    if (inputNum !== 0) {
      input.appendField(AI.BlockUtils.times_symbol);
    }
    return input;
  },
  updateContainerBlock: function (containerBlock) {
    containerBlock.setFieldValue(AI.BlockUtils.times_symbol, "CONTAINER_TEXT");
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY}]
};

Blockly.Blocks['math_division'] = {
  // Basic arithmetic operator.
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_DIVIDE,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('A')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    this.appendValueInput('B')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    this.setTooltip(Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE}]
};

Blockly.Blocks['math_power'] = {
  // Basic arithmetic operator.
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_POWER,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('A')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    this.appendValueInput('B')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_ARITHMETIC_POWER);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ARITHMETIC_POWER}]
};


Blockly.Blocks['math_bitwise'] = {
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_bitwise.HELPURLS()[mode];
  },
  init: function () {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_bitwise.OPERATORS), 'OP');
    this.appendValueInput('NUM1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    this.setInputsInline(false);
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_bitwise.TOOLTIPS()[mode];
    });
    this.setMutator(new Blockly.icons.MutatorIcon(['math_mutator_item'], this));
    this.itemCount_ = 2;
    this.valuesToSave = {'OP': null};
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function (workspace) {
    return Blockly.decompose(workspace, 'math_mutator_item', this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function () {
    var input = this.appendDummyInput(this.emptyInputName);
    input.appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_bitwise.OPERATORS), 'OP');
    this.setFieldValue(this.valuesToSave['OP'], 'OP');
  },
  addInput: function (inputNum) {
    var input = this.appendValueInput(this.repeatingInputName + inputNum)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    if (inputNum == 0) {
      input.appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_bitwise.OPERATORS), 'OP');
      this.setFieldValue(this.valuesToSave['OP'], 'OP');
    }
    return input;
  },
  updateContainerBlock: function (containerBlock) {

    for (var i = 0; i < Blockly.Blocks.math_bitwise.OPERATORS.length; i++) {
      if (Blockly.Blocks.math_bitwise.OPERATORS[i][1] == this.getFieldValue("OP")) {
        containerBlock.setFieldValue(Blockly.Blocks.math_bitwise.OPERATORS[i][0], "CONTAINER_TEXT");
      }
    }

  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_BITWISE_AND,
    dropDown: {
      titleName: 'OP',
      value: 'BITAND'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_BITWISE_IOR,
    dropDown: {
      titleName: 'OP',
      value: 'BITIOR'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_BITWISE_XOR,
    dropDown: {
      titleName: 'OP',
      value: 'BITXOR'
    }
  }]
};

Blockly.Blocks.math_bitwise.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATH_BITWISE_AND, 'BITAND'],
    [Blockly.Msg.LANG_MATH_BITWISE_IOR, 'BITIOR'],
    [Blockly.Msg.LANG_MATH_BITWISE_XOR, 'BITXOR']]
};

Blockly.Blocks.math_bitwise.TOOLTIPS = function () {
  return {
    BITAND: Blockly.Msg.LANG_MATH_BITWISE_TOOLTIP_AND,
    BITIOR: Blockly.Msg.LANG_MATH_BITWISE_TOOLTIP_IOR,
    BITXOR: Blockly.Msg.LANG_MATH_BITWISE_TOOLTIP_XOR
  }
};

Blockly.Blocks.math_bitwise.HELPURLS = function () {
  return {
    BITAND: Blockly.Msg.LANG_MATH_BITWISE_HELPURL_AND,
    BITIOR: Blockly.Msg.LANG_MATH_BITWISE_HELPURL_IOR,
    BITXOR: Blockly.Msg.LANG_MATH_BITWISE_HELPURL_XOR
  }
};

Blockly.Blocks['math_random_int'] = {
  // Random integer between [X] and [Y].
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));

    var checkTypeNumber = AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT,
        ['FROM', checkTypeNumber, Blockly.inputs.Align.RIGHT],
        ['TO', checkTypeNumber, Blockly.inputs.Align.RIGHT],
        Blockly.inputs.Align.RIGHT)
    /*this.appendValueInput('FROM')
     .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number",AI.BlockUtils.INPUT))
     .appendField(Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM)
     .appendField(Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM);
     this.appendValueInput('TO')
     .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number",AI.BlockUtils.INPUT))
     .appendField(Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO);*/
    this.setInputsInline(true);
    this.setTooltip(Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM}]
};

Blockly.Blocks['math_random_float'] = {
  // Random fraction between 0 and 1.
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM);
    this.setTooltip(Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM}]
};

Blockly.Blocks['math_random_set_seed'] = {
  // Set the seed of the radom number generator
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(false, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM)
        .appendField(Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM}]
};

Blockly.Blocks['math_on_list'] = {
  // Evaluate a list of numbers to return sum, average, min, max, etc.
  // Some functions also work on text (min, max, mode, median).
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_on_list.HELPURLS()[mode];
  },
  init: function () {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_on_list.OPERATORS()), 'OP');
    this.appendValueInput('NUM1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    this.setInputsInline(false);
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_on_list.TOOLTIPS()[mode];
    });
    this.setMutator(new Blockly.icons.MutatorIcon(['math_mutator_item'], this));
    this.itemCount_ = 2;
    this.valuesToSave = {'OP': null};
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'NUM';
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function (workspace) {
    return Blockly.decompose(workspace, 'math_mutator_item', this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function () {
    var input = this.appendDummyInput(this.emptyInputName);
    input.appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_on_list.OPERATORS()), 'OP');
    this.setFieldValue(this.valuesToSave['OP'], 'OP');
  },
  addInput: function (inputNum) {
    var input = this.appendValueInput(this.repeatingInputName + inputNum)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT));
    if (inputNum == 0) {
      input.appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_on_list.OPERATORS()), 'OP');
      this.setFieldValue(this.valuesToSave['OP'], 'OP');
    }
    return input;
  },
  updateContainerBlock: function (containerBlock) {
    var ops = Blockly.Blocks.math_on_list.OPERATORS();
    for (var i = 0; i < ops.length; i++) {
      if (ops[i][1] == this.getFieldValue("OP")) {
        containerBlock.setFieldValue(ops[i][0], "CONTAINER_TEXT");
      }
    }

  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN,
    dropDown: {
      titleName: 'OP',
      value: 'MIN'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX,
    dropDown: {
      titleName: 'OP',
      value: 'MAX'
    }
  }]
};

Blockly.Blocks.math_on_list.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN, 'MIN'],
    [Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX, 'MAX']]
};

Blockly.Blocks.math_on_list.TOOLTIPS = function () {
  return {
    MIN: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN,
    MAX: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX
  }
};

Blockly.Blocks.math_on_list.HELPURLS = function () {
  return {
    MIN: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MIN,
    MAX: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MAX
  }
};


Blockly.Blocks['math_on_list2'] = {
  // Evaluate a list of numbers to return average, min, max, etc.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_on_list2.HELPURLS()[mode];
  },
  init: function () {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('LIST')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_on_list2.OPERATORS()), 'OP');
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_on_list2.TOOLTIPS()[mode];
    });
  },

  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_AVG,
    dropDown: {
      titleName: 'OP',
      value: 'AVG'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN_LIST,
    dropDown: {
      titleName: 'OP',
      value: 'MIN'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX_LIST,
    dropDown: {
      titleName: 'OP',
      value: 'MAX'
    }
  },//{
   // translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MODE,
   // dropDown: {
   //   titleName: 'OP',
   //   value: 'MODE'
   // }
  //},
    {
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_GM,
    dropDown: {
      titleName: 'OP',
      value: 'GM'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_SD,
    dropDown: {
      titleName: 'OP',
      value: 'SD'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_SE,
    dropDown: {
      titleName: 'OP',
      value: 'SE'
    }
  }]
};

Blockly.Blocks.math_on_list2.OPERATORS = function () {
  return [
      [Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_AVG,'AVG'],
      [Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN_LIST, 'MIN'],
      [Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX_LIST, 'MAX'],
      //[Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MODE,'MODE'],
      [Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_GM,'GM'],
      [Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_SD,'SD'],
      [Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_SE,'SE']
  ]
};

Blockly.Blocks.math_on_list2.TOOLTIPS = function () {
  return {
    AVG: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_AVG,
    MIN: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN_LIST,
    MAX: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX_LIST,
    //MODE: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MODE,
    GM: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_GM,
    SD: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_SD,
    SE: Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_SE
  }
};

Blockly.Blocks.math_on_list2.HELPURLS = function () {
  return {
    AVG: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_AVG,
    MIN: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MIN_LIST,
    MAX: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MAX_LIST,
    //MODE: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MODE,
    GM: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_GM,
    SD: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_SD,
    SE: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_SE
  }
};

Blockly.Blocks['math_mode_of_list'] = {
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MODE,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.appendValueInput('LIST')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_LIST_MODE_TITLE)
    this.setTooltip(Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MODE);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MODE}]
};

Blockly.Blocks['math_single'] = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_single.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS()), 'OP');
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT,
    dropDown: {
      titleName: 'OP',
      value: 'ROOT'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE,
    dropDown: {
      titleName: 'OP',
      value: 'ABS'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_SINGLE_OP_NEG,
    dropDown: {
      titleName: 'OP',
      value: 'NEG'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_SINGLE_OP_LN,
    dropDown: {
      titleName: 'OP',
      value: 'LN'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_SINGLE_OP_EXP,
    dropDown: {
      titleName: 'OP',
      value: 'EXP'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND,
    dropDown: {
      titleName: 'OP',
      value: 'ROUND'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING,
    dropDown: {
      titleName: 'OP',
      value: 'CEILING'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR,
    dropDown: {
      titleName: 'OP',
      value: 'FLOOR'
    }
  }]
};

Blockly.Blocks.math_single.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT, 'ROOT'],
    [Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE, 'ABS'],
    [Blockly.Msg.LANG_MATH_SINGLE_OP_NEG, 'NEG'],
    [Blockly.Msg.LANG_MATH_SINGLE_OP_LN, 'LN'],
    [Blockly.Msg.LANG_MATH_SINGLE_OP_EXP, 'EXP'],
    [Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND, 'ROUND'],
    [Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING, 'CEILING'],
    [Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR, 'FLOOR']];
};

Blockly.Blocks.math_single.TOOLTIPS = function () {
  return {
    ROOT: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT,
    ABS: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS,
    NEG: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG,
    LN: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN,
    EXP: Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP,
    ROUND: Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND,
    CEILING: Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING,
    FLOOR: Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR
  }
};

Blockly.Blocks.math_single.HELPURLS = function () {
  return {
    ROOT: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT,
    ABS: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS,
    NEG: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG,
    LN: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN,
    EXP: Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP,
    ROUND: Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND,
    CEILING: Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING,
    FLOOR: Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR
  }
};

Blockly.Blocks['math_abs'] = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_single.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('ABS', "OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['math_neg'] = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_single.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('NEG', "OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['math_round'] = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_single.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('ROUND', "OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['math_ceiling'] = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_single.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('CEILING', "OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['math_floor'] = {
  // Advanced math operators with single operand.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_single.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_single.OPERATORS), 'OP');
    this.setFieldValue('FLOOR', "OP");
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_single.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['math_divide'] = {
  // Remainder or quotient of a division.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_divide.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('DIVIDEND')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_divide.OPERATORS()), 'OP');
    this.appendValueInput('DIVISOR')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_DIVIDE);
    this.setInputsInline(true);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_divide.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO,
    dropDown: {
      titleName: 'OP',
      value: 'MODULO'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER,
    dropDown: {
      titleName: 'OP',
      value: 'REMAINDER'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT,
    dropDown: {
      titleName: 'OP',
      value: 'QUOTIENT'
    }
  }]
};

Blockly.Blocks.math_divide.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO, 'MODULO'],
    [Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER, 'REMAINDER'],
    [Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT, 'QUOTIENT']];
};

Blockly.Blocks.math_divide.TOOLTIPS = function () {
  return {
    MODULO: Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO,
    REMAINDER: Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER,
    QUOTIENT: Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT
  }
};

Blockly.Blocks.math_divide.HELPURLS = function () {
  return {
    MODULO: Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO,
    REMAINDER: Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER,
    QUOTIENT: Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT
  }
};

Blockly.Blocks['math_trig'] = {
  // Trigonometry operators.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_trig.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_trig.OPERATORS()), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_trig.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_TRIG_SIN,
    dropDown: {
      titleName: 'OP',
      value: 'SIN'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_TRIG_COS,
    dropDown: {
      titleName: 'OP',
      value: 'COS'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_TRIG_TAN,
    dropDown: {
      titleName: 'OP',
      value: 'TAN'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_TRIG_ASIN,
    dropDown: {
      titleName: 'OP',
      value: 'ASIN'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_TRIG_ACOS,
    dropDown: {
      titleName: 'OP',
      value: 'ACOS'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_TRIG_ATAN,
    dropDown: {
      titleName: 'OP',
      value: 'ATAN'
    }
  }]
};

Blockly.Blocks.math_trig.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATH_TRIG_SIN, 'SIN'],
    [Blockly.Msg.LANG_MATH_TRIG_COS, 'COS'],
    [Blockly.Msg.LANG_MATH_TRIG_TAN, 'TAN'],
    [Blockly.Msg.LANG_MATH_TRIG_ASIN, 'ASIN'],
    [Blockly.Msg.LANG_MATH_TRIG_ACOS, 'ACOS'],
    [Blockly.Msg.LANG_MATH_TRIG_ATAN, 'ATAN']];
}

Blockly.Blocks.math_trig.TOOLTIPS = function () {
  return {
    SIN: Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN,
    COS: Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS,
    TAN: Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN,
    ASIN: Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN,
    ACOS: Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS,
    ATAN: Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN
  }
};

Blockly.Blocks.math_trig.HELPURLS = function () {
  return {
    SIN: Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN,
    COS: Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS,
    TAN: Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN,
    ASIN: Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN,
    ACOS: Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS,
    ATAN: Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN
  }
};

Blockly.Blocks['math_cos'] = {
  // Trigonometry operators.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_trig.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_trig.OPERATORS), 'OP');
    this.setFieldValue('COS', "OP");
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_trig.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['math_tan'] = {
  // Trigonometry operators.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_trig.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_trig.OPERATORS), 'OP');
    this.setFieldValue('TAN', "OP");
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_trig.TOOLTIPS()[mode];
    });
  }
};

Blockly.Blocks['math_atan2'] = {
  // Trigonometry operators.
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendDummyInput().appendField(Blockly.Msg.LANG_MATH_TRIG_ATAN2);
    this.appendValueInput('Y')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('X')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_TRIG_ATAN2_X)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.setInputsInline(false);
    this.setTooltip(Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_TRIG_ATAN2}]
};

Blockly.Blocks['math_convert_angles'] = {
  // Trigonometry operators.
  category: 'Math',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.math_convert_angles.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT)
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_convert_angles.OPERATORS()), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_convert_angles.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT +
    ' ' + Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG,
    dropDown: {
      titleName: 'OP',
      value: 'RADIANS_TO_DEGREES'
    }
  }, {
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT +
    ' ' + Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD,
    dropDown: {
      titleName: 'OP',
      value: 'DEGREES_TO_RADIANS'
    }
  }]
};

Blockly.Blocks.math_convert_angles.OPERATORS = function () {
  return [[Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG, 'RADIANS_TO_DEGREES'],
    [Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD, 'DEGREES_TO_RADIANS']]
};

Blockly.Blocks.math_convert_angles.TOOLTIPS = function () {
  return {
    RADIANS_TO_DEGREES: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG,
    DEGREES_TO_RADIANS: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD
  }
};

Blockly.Blocks.math_convert_angles.HELPURLS = function () {
  return {
    RADIANS_TO_DEGREES: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG,
    DEGREES_TO_RADIANS: Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD
  }
};

Blockly.Blocks['math_format_as_decimal'] = {
  category: 'Math',
  helpUrl: Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL,
  init: function () {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));

    var checkTypeNumber = AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT,
        ['NUM', checkTypeNumber, Blockly.inputs.Align.RIGHT],
        ['PLACES', checkTypeNumber, Blockly.inputs.Align.RIGHT],
        Blockly.inputs.Align.RIGHT);
    /*this.appendDummyInput()
      .appendField('format as decimal');
    this.appendValueInput('NUM')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number",AI.BlockUtils.INPUT))
      .appendField('number')
      .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('PLACES')
      .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number",AI.BlockUtils.INPUT))
      .appendField('places')
      .setAlign(Blockly.inputs.Align.RIGHT);*/
    this.setInputsInline(false);
    this.setTooltip(Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE}]
};

Blockly.Blocks['math_is_a_number'] = {
  category : 'Math',
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_is_a_number.HELPURLS()[mode];
  },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("boolean",AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_is_a_number.OPERATORS()), 'OP');
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_is_a_number.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM,
    dropDown: {
      titleName: 'OP',
      value: 'NUMBER'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM,
    dropDown: {
      titleName: 'OP',
      value: 'BASE10'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM,
    dropDown: {
      titleName: 'OP',
      value: 'HEXADECIMAL'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM,
    dropDown: {
      titleName: 'OP',
      value: 'BINARY'
    }
  }]
};

Blockly.Blocks.math_is_a_number.OPERATORS = function() {
  return [[Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM, 'NUMBER'],
    [Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM, 'BASE10'],
    [Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM, 'HEXADECIMAL'],
    [Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM, 'BINARY']]
}

Blockly.Blocks.math_is_a_number.TOOLTIPS = function() {
  return {
    NUMBER: Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP,
    BASE10: Blockly.Msg.LANG_MATH_IS_A_DECIMAL_TOOLTIP,
    HEXADECIMAL: Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_TOOLTIP,
    BINARY: Blockly.Msg.LANG_MATH_IS_A_BINARY_TOOLTIP
  };
};

Blockly.Blocks.math_is_a_number.HELPURLS = function() {
  return {
    NUMBER: Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL,
    BASE10: Blockly.Msg.LANG_MATH_IS_A_DECIMAL_HELPURL,
    HEXADECIMAL: Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_HELPURL,
    BINARY: Blockly.Msg.LANG_MATH_IS_A_BINARY_HELPURL
  };
};

Blockly.Blocks['math_convert_number'] = {
  category : 'Math',
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.math_convert_number.HELPURLS()[mode];
  },
  init : function() {
    this.setColour(Blockly.MATH_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text",AI.BlockUtils.OUTPUT));
    this.appendValueInput('NUM')
        .appendField(Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TITLE_CONVERT)
        .appendField(new Blockly.FieldDropdown(Blockly.Blocks.math_convert_number.OPERATORS()), 'OP');
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.math_convert_number.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX,
    dropDown: {
      titleName: 'OP',
      value: 'DEC_TO_HEX'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC,
    dropDown: {
      titleName: 'OP',
      value: 'HEX_TO_DEC'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN,
    dropDown: {
      titleName: 'OP',
      value: 'DEC_TO_BIN'
    }
  },{
    translatedName: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC,
    dropDown: {
      titleName: 'OP',
      value: 'BIN_TO_DEC'
    }
  }]
};

Blockly.Blocks.math_convert_number.OPERATORS = function() {
  return [[Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX, 'DEC_TO_HEX'],
      [Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC, 'HEX_TO_DEC'],
      [Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN, 'DEC_TO_BIN'],
      [Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC, 'BIN_TO_DEC']];
}

Blockly.Blocks.math_convert_number.TOOLTIPS = function() {
  return {
    DEC_TO_HEX: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_HEX,
    HEX_TO_DEC: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_HEX_TO_DEC,
    DEC_TO_BIN: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_BIN,
    BIN_TO_DEC: Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_BIN_TO_DEC
  };
}

Blockly.Blocks.math_convert_number.HELPURLS = function() {
  return {
    DEC_TO_HEX : Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_HEX,
    HEX_TO_DEC : Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_HEX_TO_DEC,
    DEC_TO_BIN : Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_BIN,
    BIN_TO_DEC : Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_BIN_TO_DEC
  };
}
