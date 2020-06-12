// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Text blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Blocks.text');

goog.require('Blockly.Blocks.Utilities');

Blockly.Blocks['text'] = {
  // Text value.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_TEXT_HELPURL,
  init: function () {
    var textInput = new Blockly.FieldTextInput('');
    textInput.onFinishEditing_ = Blockly.Blocks.text
        .bumpBlockOnFinishEdit.bind(this);

    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE)
        .appendField(textInput, 'TEXT')
        .appendField(Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE);
    this.setOutput(true, [Blockly.Blocks.text.connectionCheck]);
    this.setTooltip(Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP);
  },
  errors: [{name:"checkInvalidNumber"}],
  typeblock: [{translatedName: Blockly.Msg.LANG_CATEGORY_TEXT}]
};

Blockly.Blocks.text.connectionCheck = function (myConnection, otherConnection, opt_value) {
  var otherTypeArray = otherConnection.check_;
  if (!otherTypeArray) {  // Other connection accepts everything.
    return true;
  }

  var block = myConnection.sourceBlock_;
  var shouldIgnoreError = Blockly.mainWorkspace.isLoading;
  var value = opt_value || block.getFieldValue('TEXT');

  for (var i = 0; i < otherTypeArray.length; i++) {
    if (otherTypeArray[i] == "String") {
      return true;
    } else if (otherTypeArray[i] == "Number") {
      if (shouldIgnoreError) {
        // Error may be noted by WarningHandler's checkInvalidNumber
        return true;
      } else if (Blockly.Blocks.Utilities.NUMBER_REGEX.test(value)) {
        // Value passes a floating point regex
        return !isNaN(parseFloat(value));
      }
    } else if (otherTypeArray[i] == "Key") {
      return true;
    }
  }
  return false;
};

/**
 * Bumps the text block out of its connection iff it is connected to a number
 * input and it no longer contains a number.
 * @param {string} finalValue The final value typed into the text input.
 * @this Blockly.Block
 */
Blockly.Blocks.text.bumpBlockOnFinishEdit = function(finalValue) {
  var connection = this.outputConnection.targetConnection;
  if (!connection) {
    return;
  }
  // If the connections are no longer compatible.
  if (!Blockly.Blocks.text.connectionCheck(
      this.outputConnection, connection, finalValue)) {
    connection.disconnect();
    connection.sourceBlock_.bumpNeighbours_();
  }
}

Blockly.Blocks['text_join'] = {
  // Create a string made up of any number of elements of any type.
  // TODO: (Andrew) Make this handle multiple arguments.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_JOIN_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('ADD0')
        .appendField(Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN);
    this.appendValueInput('ADD1');
    this.setTooltip(Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP);
    this.setMutator(new Blockly.Mutator(['text_join_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'ADD';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function (workspace) {
    return Blockly.decompose(workspace, 'text_join_item', this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function () {
    this.appendDummyInput(this.emptyInputName)
        .appendField(Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN);
  },
  addInput: function (inputNum) {
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT));
    if (inputNum === 0) {
      input.appendField(Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN);
    }
    return input;
  },
  updateContainerBlock: function (containerBlock) {
    containerBlock.inputList[0].fieldRow[0].setText(Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN}]

};

Blockly.Blocks['text_join_item'] = {
  // Add items.
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP);
    this.contextMenu = false;
  }
};

Blockly.Blocks['text_length'] = {
  // String length.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_LENGTH_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('VALUE')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH);
    this.setTooltip(Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH}]
};

Blockly.Blocks['text_isEmpty'] = {
  // Is the string null?
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('VALUE')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY);
    this.setTooltip(Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY}]
};

Blockly.Blocks['text_compare'] = {
  // Compare two texts
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_COMPARE_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT1')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE);
    this.appendValueInput('TEXT2')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.text_compare.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE + Blockly.Msg.LANG_TEXT_COMPARE_LT,
    dropDown: {
      titleName: 'OP',
      value: 'LT'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE + Blockly.Msg.LANG_TEXT_COMPARE_EQUAL,
    dropDown: {
      titleName: 'OP',
      value: 'EQUAL'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE + Blockly.Msg.LANG_TEXT_COMPARE_NEQ,
    dropDown: {
      titleName: 'OP',
      value: 'NEQ'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE + Blockly.Msg.LANG_TEXT_COMPARE_GT,
    dropDown: {
      titleName: 'OP',
      value: 'GT'
    }
  }]
};

Blockly.Blocks.text_compare.OPERATORS = function () {
  return [
    [Blockly.Msg.LANG_TEXT_COMPARE_LT, 'LT'], [Blockly.Msg.LANG_TEXT_COMPARE_EQUAL, 'EQUAL'], [Blockly.Msg.LANG_TEXT_COMPARE_NEQ, 'NEQ'], [Blockly.Msg.LANG_TEXT_COMPARE_GT, 'GT']
  ]
};

Blockly.Blocks.text_compare.TOOLTIPS = function () {
  return {
    LT: Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT,
    EQUAL: Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL,
    NEQ: Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_NEQ,
    GT: Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT
  }
};

Blockly.Blocks['text_trim'] = {
  // trim string
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_TRIM_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM);
    this.setTooltip(Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM}]
};

Blockly.Blocks['text_changeCase'] = {
  // Change capitalization.
  category: 'Text',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.text_changeCase.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    var thisBlock = this;
    this.setTooltip(function () {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.text_changeCase.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE,
    dropDown: {
      titleName: 'OP',
      value: 'UPCASE'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE,
    dropDown: {
      titleName: 'OP',
      value: 'DOWNCASE'
    }
  }]
};

Blockly.Blocks.text_changeCase.OPERATORS = function () {
  return [
    [Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE, 'UPCASE'], [Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE, 'DOWNCASE']
  ]
};

Blockly.Blocks.text_changeCase.TOOLTIPS = function () {
  return {
    UPCASE: Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE,
    DOWNCASE: Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE
  }
};

Blockly.Blocks.text_changeCase.HELPURLS = function () {
  return {
    UPCASE: Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE,
    DOWNCASE: Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE
  }
};

Blockly.Blocks['text_starts_at'] = {
  // return index of first occurrence.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.OUTPUT));
    var checkTypeText = Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT,
        ['TEXT', checkTypeText, Blockly.ALIGN_RIGHT],
        ['PIECE', checkTypeText, Blockly.ALIGN_RIGHT],
        Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT}]
};

Blockly.Blocks['text_contains'] = {
  // Is text contained in
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    var checkTypeText = Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_TEXT_CONTAINS_INPUT,
        ['TEXT', checkTypeText, Blockly.ALIGN_RIGHT],
        ['PIECE', checkTypeText, Blockly.ALIGN_RIGHT],
        Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS}]
};

Blockly.Blocks['text_split'] = {
  // This includes all four split variants (modes). The name and type of the 'AT' arg
  // changes to match the selected mode.
  category: 'Text',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.text_split.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(new Blockly.FieldDropdown(this.OPERATORS, Blockly.Blocks.text_split.dropdown_onchange), 'OP')
        .appendField(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT);
    this.appendValueInput('AT')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME')
        .setAlign(Blockly.ALIGN_RIGHT);
  },
  // adjust for the mode when the block is read in
  domToMutation: function (xmlElement) {
    var mode = xmlElement.getAttribute('mode');
    Blockly.Blocks.text_split.adjustToMode(mode, this);
  },
  // put the mode in the DOM so it can be read in by domToMutation
  // WARNING:  Note that the 'mode' tag below is lowercase.  It would not work
  // to make it uppercase ('MODE').  There's a bug somewhere (in the browser?) that
  // writes tags as lowercase.  So writing the date with tag 'MODE' and attempting
  // to read with tag 'MODE' wil not work.
  mutationToDom: function () {
    var container = document.createElement('mutation');
    var savedMode = this.getFieldValue('OP');
    container.setAttribute('mode', savedMode);
    return container;
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT,
    dropDown: {
      titleName: 'OP',
      value: 'SPLIT'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATFIRST'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATANY'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATFIRSTOFANY'
    }
  }]
};

// Change the name and type of ARG2 and set tooltop depending on mode
Blockly.Blocks.text_split.adjustToMode = function (mode, block) {
  if (mode == 'SPLITATFIRST' || mode == 'SPLIT') {
    block.getInput("AT").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT));
    block.setFieldValue(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME');
  } else if (mode == 'SPLITATFIRSTOFANY' || mode == 'SPLITATANY') {
    block.getInput("AT").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.INPUT));
    block.setFieldValue(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST, 'ARG2_NAME');
  }
  ;
  block.setTooltip(Blockly.Blocks.text_split.TOOLTIPS()[mode]);
};

Blockly.Blocks.text_split.dropdown_onchange = function (mode) {
  Blockly.Blocks.text_split.adjustToMode(mode, this.sourceBlock_)
};

// The order here determines the order in the dropdown
Blockly.Blocks.text_split.OPERATORS = function () {
  return [
    [Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT, 'SPLIT'],
    [Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST, 'SPLITATFIRST'],
    [Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY, 'SPLITATANY'],
    [Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY, 'SPLITATFIRSTOFANY']
  ]
};

Blockly.Blocks.text_split.TOOLTIPS = function () {
  return {
    SPLITATFIRST: Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST,
    SPLITATFIRSTOFANY: Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY,
    SPLIT: Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT,
    SPLITATANY: Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY
  }
};

Blockly.Blocks.text_split.HELPURLS = function () {
  return {
    SPLITATFIRST: Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST,
    SPLITATFIRSTOFANY: Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY,
    SPLIT: Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT,
    SPLITATANY: Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY
  }
};

Blockly.Blocks['text_split_at_spaces'] = {
  // Split at spaces
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE);
    this.setTooltip(Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE}]
};

Blockly.Blocks['text_segment'] = {
  // Create text segment
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.OUTPUT));
    var checkTypeText = Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT);
    var checkTypeNumber = Blockly.Blocks.Utilities.YailTypeToBlocklyType("number", Blockly.Blocks.Utilities.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_TEXT_SEGMENT_INPUT,
        ['TEXT', checkTypeText, Blockly.ALIGN_RIGHT],
        ['START', checkTypeNumber, Blockly.ALIGN_RIGHT],
        ['LENGTH', checkTypeNumber, Blockly.ALIGN_RIGHT],
        Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT}]
};

Blockly.Blocks['text_replace_all'] = {
  // Replace all occurrences of text
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.OUTPUT));
    var checkTypeText = Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT,
        ['TEXT', checkTypeText, Blockly.ALIGN_RIGHT],
        ['SEGMENT', checkTypeText, Blockly.ALIGN_RIGHT],
        ['REPLACEMENT', checkTypeText, Blockly.ALIGN_RIGHT],
        Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL}]
};

Blockly.Blocks['obfuscated_text'] = {
  // Text value.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    var label = Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE + " " +
        Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE
    var textInput = new Blockly.FieldTextBlockInput('');
    textInput.onFinishEditing_ = Blockly.Blocks.text
        .bumpBlockOnFinishEdit.bind(this);
    this.appendDummyInput()
        .appendField(label)
        .appendField(textInput,'TEXT')
        .appendField(Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE);
    this.setOutput(true, [Blockly.Blocks.text.connectionCheck]);
    this.setTooltip(Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_TOOLTIP);
    this.confounder = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 8);
  },
  domToMutation: function(xmlElement) {
    var confounder = xmlElement.getAttribute('confounder');
    this.confounder = confounder;
  },
  mutationToDom: function() {
    var container = document.createElement('mutation')
    container.setAttribute('confounder', this.confounder);
    return container;
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE}]
};

Blockly.Blocks['text_is_string'] = {
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_HELPURL,
  init: function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendValueInput('ITEM')
      .appendField(Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_TITLE)
      .appendField(Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_INPUT_THING);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean", Blockly.Blocks.Utilities.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_TITLE}]
};

Blockly.Blocks['text_reverse'] = {
  // String reverse.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_REVERSE_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('VALUE')
        .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_REVERSE_INPUT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_REVERSE_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_REVERSE_INPUT}]
};

Blockly.Blocks['text_replace_mappings'] = {
  // Replace all occurrences in mappings with their corresponding replacement
  category: 'Text',
  helpUrl: function () {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.text_replace_mappings.HELPURLS()[mode];
  },
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.OUTPUT));
    var checkTypeText = Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT);
    var checkTypeMap = Blockly.Blocks.Utilities.YailTypeToBlocklyType("dictionary", Blockly.Blocks.Utilities.INPUT);

    this.appendValueInput('MAPPINGS')
      .setCheck(checkTypeMap)
      .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_TITLE)
      .setAlign(Blockly.ALIGN_RIGHT)

    this.appendValueInput('TEXT')
      .setCheck(checkTypeText)
      .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_TEXT)
      .setAlign(Blockly.ALIGN_RIGHT)

    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_ORDER_PREFIX)
        .appendField(new Blockly.FieldDropdown(this.OPERATORS, Blockly.Blocks.text_replace_mappings.onchange), 'OP')
        .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_ORDER)
        .setAlign(Blockly.ALIGN_RIGHT)

    this.setInputsInline(false);

    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.text_replace_mappings.TOOLTIPS()[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_OPERATOR_LONGEST_STRING_FIRST,
    dropDown: {
      titleName: 'OP',
      value: 'LONGEST_STRING_FIRST'
    }
  }, {
    translatedName: Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_OPERATOR_DICTIONARY_ORDER,
    dropDown: {
      titleName: 'OP',
      value: 'DICTIONARY_ORDER'
    }
  }
  /*{
    translatedName : Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST,
    dropDown: {
        titleName: 'OP',
        value: 'EARLIEST_OCCURRENCE'
    }
  }*/
  ]
};

// The order here determines the order in the dropdown
Blockly.Blocks.text_replace_mappings.OPERATORS = function () {
  return [
    [Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_OPERATOR_LONGEST_STRING_FIRST, 'LONGEST_STRING_FIRST'],
    [Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_OPERATOR_DICTIONARY_ORDER, 'DICTIONARY_ORDER']
    //['earliest occurrence', 'EARLIEST_OCCURRENCE']
  ]
};

Blockly.Blocks.text_replace_mappings.TOOLTIPS = function () {
  return {
    LONGEST_STRING_FIRST : Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_TOOLTIP_LONGEST_STRING_FIRST,
    DICTIONARY_ORDER : Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_TOOLTIP_DICTIONARY_ORDER
    //EARLIEST_OCCURRENCE : "tooltip"
  }
};

Blockly.Blocks.text_replace_mappings.HELPURLS = function () {
  return {
    LONGEST_STRING_FIRST : Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_HELPURL_LONGEST_STRING_FIRST,
    DICTIONARY_ORDER : Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_HELPURL_DICTIONARY_ORDER
    //EARLIEST_OCCURRENCE : "help"
  }
}
