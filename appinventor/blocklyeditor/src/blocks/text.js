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

goog.provide('AI.Blocks.text');

goog.require('AI.BlockUtils');
goog.require('AI.FieldTextBlockInput');

Blockly.Blocks['text'] = {
  // Text value.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_TEXT_HELPURL,
  init: function () {
    const textInput = new Blockly.FieldTextInput('', this.inputValidator);
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE)
        .appendField(textInput, 'TEXT')
        .appendField(Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP);
  },
  inputValidator: function (newVal) {
    /*
     * Note that it would be nice to be able to just use this input validator to alwaysset the output type
     * but the problem is that the validator is called during the editing of the input field.
     * So if the text block is in the input of another block's numeric input, and the user
     * just happens to mistype a number, the block will be bumped out of the connection. Therefore
     * we only set the output stye in the case that the workspace is loading, in which case we know
     * the validator is only called once, with the entire value of the test input field.
     */
    const block = this.getSourceBlock();
    if (block.workspace.isLoading) {
      Blockly.Blocks.text.setOutputOnFinishEdit.call(block,newVal);
    }
  },
  onchange: function(event) {
    /*
     * Note that it would be nice to be able to just use this onchange event to always set the output
     * type, but the neither the BLOCK_CHANGE nor BLOCK_CREATE events are fired when the block is loaded.
     * Nor is any other block related event. Therefore we use the text input field validator to set the
     * output type during loading.
     */
    if (event.blockId === this.id &&
        ((event.type === Blockly.Events.BLOCK_CHANGE) ||
        event.type === Blockly.Events.BLOCK_CREATE)) {
      const text = this.getFieldValue('TEXT');
      Blockly.Blocks.text.setOutputOnFinishEdit.call(this, text);
    }
  },
  errors: [{name:"checkInvalidNumber"}],
  typeblock: [{translatedName: Blockly.Msg.LANG_CATEGORY_TEXT}]
};

/**
 * Sets the output type of the text block based on the final value typed into the text input.
 * @param {string} newValue The new value typed into the text input.
 * @this Blockly.Block
 */
Blockly.Blocks.text.setOutputOnFinishEdit = function(newValue) {
  if (AI.BlockUtils.NUMBER_REGEX.test(newValue) && !isNaN(parseFloat(newValue))) {
    this.outputConnection.setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
  } else {
    // Remove the Number type from the output connection check if it exists.
    const check = Array.from(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    const numberIndex = check.indexOf('Number');
    if (numberIndex !== -1) {
      check.splice(numberIndex, 1);
    }
    this.outputConnection.setCheck(check);
  }
  maybeBumpBlockOnFinishEdit(this);
}

/**
 * Bumps the text block out of its connection iff it has an invalid type
 * @block Blockly.Block
 */
function maybeBumpBlockOnFinishEdit(block) {
  const outputConnection = block.outputConnection;
  const targetConnection = outputConnection.targetConnection;
  if (!targetConnection) {
    return;
  }
  // If the connections are no longer compatible.
  if (!(targetConnection.getConnectionChecker().canConnect(outputConnection, targetConnection, false))) {
    targetConnection.disconnect();
    targetConnection.sourceBlock_.bumpNeighbours();
  }
}

Blockly.Blocks['text_join'] = {
  // Create a string made up of any number of elements of any type.
  // TODO: (Andrew) Make this handle multiple arguments.
  category: 'Text',
  helpUrl: Blockly.Msg.LANG_TEXT_JOIN_HELPURL,
  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    this.appendValueInput('ADD0')
        .appendField(Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN);
    this.appendValueInput('ADD1');
    this.setTooltip(Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP);
    this.setMutator(new Blockly.icons.MutatorIcon(['text_join_item'], this));
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
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT));
    if (inputNum === 0) {
      input.appendField(Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN);
    }
    return input;
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN}]

};

AI.Blockly.Mixins.extend(Blockly.Blocks['text_join'], AI.Blockly.Mixins.DynamicConnections);

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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    this.appendValueInput('VALUE')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("boolean", AI.BlockUtils.OUTPUT));
    this.appendValueInput('VALUE')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("boolean", AI.BlockUtils.OUTPUT));
    this.appendValueInput('TEXT1')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE);
    this.appendValueInput('TEXT2')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.OUTPUT));
    var checkTypeText = AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT,
        ['TEXT', checkTypeText, Blockly.inputs.Align.RIGHT],
        ['PIECE', checkTypeText, Blockly.inputs.Align.RIGHT],
        Blockly.inputs.Align.RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP);
    this.setInputsInline(false);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT}]
};

Blockly.Blocks['text_contains'] = {
  category: 'Text',

  helpUrl: function() {
    return Blockly.Blocks.text_contains.HELPURLS()[this.getMode()];
  },

  init: function () {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);

    var utils = AI.BlockUtils;
    var getType = utils.YailTypeToBlocklyType;
    var dropdown = new Blockly.FieldDropdown(
        Blockly.Blocks.text_contains.OPERATORS(),
        Blockly.Blocks.text_contains.adjustToMode.bind(this));
    var text = new Blockly.FieldLabel(
        Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE);

    this.setOutput(true, getType("boolean", utils.OUTPUT));
    this.interpolateMsg(
        Blockly.Msg.LANG_TEXT_CONTAINS_INPUT,
        ['OP', dropdown],
        ['TEXT', getType('text', utils.INPUT), Blockly.inputs.Align.RIGHT],
        ['PIECE_TEXT', text],
        ['PIECE', getType('text', utils.INPUT), Blockly.inputs.Align.RIGHT],
        Blockly.inputs.Align.RIGHT);
    this.setInputsInline(false);

    this.setTooltip(function() {
      return Blockly.Blocks.text_contains.TOOLTIPS()[this.getMode()];
    }.bind(this));
  },

  // TODO: This can be removed after the blockly update b/c validators are
  // properly triggered on load from XML.
  domToMutation: function (xmlElement) {
    var mode = xmlElement.getAttribute('mode');
    Blockly.Blocks.text_contains.adjustToMode.call(this, mode);
  },

  mutationToDom: function () {
    var container = document.createElement('mutation');
    container.setAttribute('mode', this.getMode());
    return container;
  },

  getMode: function() {
    return this.getFieldValue('OP');
  },

  typeblock: [
    {
      translatedName: Blockly.Msg.LANG_TEXT_CONTAINS_OPERATOR_CONTAINS,
      dropDown: {
        titleName: 'OP',
        value: 'CONTAINS'
      }
    },
    {
      translatedName: Blockly.Msg.LANG_TEXT_CONTAINS_OPERATOR_CONTAINS_ANY,
      dropDown: {
        titleName: 'OP',
        value: 'CONTAINS_ANY'
      }
    },
    {
      translatedName: Blockly.Msg.LANG_TEXT_CONTAINS_OPERATOR_CONTAINS_ALL,
      dropDown: {
        titleName: 'OP',
        value: 'CONTAINS_ALL'
      }
    }
  ]
};

/**
 * Updates the block's PIECE input to reflect the current mode.
 * @param {string} mode
 * @this {!Blockly.BlockSvg}
 */
Blockly.Blocks.text_contains.adjustToMode = function (mode) {
  var utils = AI.BlockUtils;
  var getType = utils.YailTypeToBlocklyType;

  if (mode == 'CONTAINS') {
    this.getInput('PIECE')
        .setCheck(getType('text', utils.INPUT));
    this.setFieldValue(
        Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE,
        'PIECE_TEXT');
  } else {
    this.getInput('PIECE')
      .setCheck(getType('list', utils.INPUT));
    this.setFieldValue(
        Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE_LIST,
        'PIECE_TEXT');
  }
};

// The order here determines the order in the dropdown
Blockly.Blocks.text_contains.OPERATORS = function() {
  return [
    [Blockly.Msg.LANG_TEXT_CONTAINS_OPERATOR_CONTAINS, 'CONTAINS'],
    [Blockly.Msg.LANG_TEXT_CONTAINS_OPERATOR_CONTAINS_ANY, 'CONTAINS_ANY'],
    [Blockly.Msg.LANG_TEXT_CONTAINS_OPERATOR_CONTAINS_ALL, 'CONTAINS_ALL'],
  ]
};

Blockly.Blocks.text_contains.TOOLTIPS = function() {
  return {
    'CONTAINS': Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP_CONTAINS,
    'CONTAINS_ANY': Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP_CONTAINS_ANY,
    'CONTAINS_ALL': Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP_CONTAINS_ALL,
  }
};

Blockly.Blocks.text_contains.HELPURLS = function() {
  return {
    'CONTAINS': Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL_CONTAINS,
    'CONTAINS_ANY': Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL_CONTAINS_ANY,
    'CONTAINS_ALL': Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL_CONTAINS_ALL,
  }
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
        .appendField(new Blockly.FieldDropdown(this.OPERATORS, Blockly.Blocks.text_split.dropdown_onchange), 'OP')
        .appendField(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT);
    this.appendValueInput('AT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME')
        .setAlign(Blockly.inputs.Align.RIGHT);
  },
  // TODO: This can be removed after the blockly update b/c validators are
  // properly triggered on load from XML.
  // adjust for the mode when the block is read in
  domToMutation: function (xmlElement) {
    var mode = xmlElement.getAttribute('mode');
    Blockly.Blocks.text_split.adjustToMode(mode, this);
  },
  // put the mode in the DOM so it can be read in by domToMutation
  // Note: All attributes must be 100% lowercase because IE always writes
  // attributes as lowercase.
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
    block.getInput("AT").setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT));
    block.setFieldValue(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME');
  } else if (mode == 'SPLITATFIRSTOFANY' || mode == 'SPLITATANY') {
    block.getInput("AT").setCheck(AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.INPUT));
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.appendValueInput('TEXT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    var checkTypeText = AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT);
    var checkTypeNumber = AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_TEXT_SEGMENT_INPUT,
        ['TEXT', checkTypeText, Blockly.inputs.Align.RIGHT],
        ['START', checkTypeNumber, Blockly.inputs.Align.RIGHT],
        ['LENGTH', checkTypeNumber, Blockly.inputs.Align.RIGHT],
        Blockly.inputs.Align.RIGHT);
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    var checkTypeText = AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT);
    this.interpolateMsg(Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT,
        ['TEXT', checkTypeText, Blockly.inputs.Align.RIGHT],
        ['SEGMENT', checkTypeText, Blockly.inputs.Align.RIGHT],
        ['REPLACEMENT', checkTypeText, Blockly.inputs.Align.RIGHT],
        Blockly.inputs.Align.RIGHT);
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
    var textInput = new Blockly.FieldTextInput('', Blockly.Blocks['text'].inputValidator);
    this.appendDummyInput()
        .appendField(label)
        .appendField(textInput,'TEXT')
        .appendField(Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    this.setTooltip(Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_TOOLTIP);
    this.confounder = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 8);
  },
  domToMutation: function(xmlElement) {
    var confounder = xmlElement.getAttribute('confounder');
    this.confounder = confounder;
  },
  mutationToDom: function() {
    var container = document.createElement('mutation');
    container.setAttribute('confounder', this.confounder);
    return container;
  },
  onchange: Blockly.Blocks['text'].onchange,
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("boolean", AI.BlockUtils.OUTPUT));
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    this.appendValueInput('VALUE')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
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
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    var checkTypeText = AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT);
    var checkTypeMap = AI.BlockUtils.YailTypeToBlocklyType("dictionary", AI.BlockUtils.INPUT);

    this.appendValueInput('MAPPINGS')
      .setCheck(checkTypeMap)
      .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_TITLE)
      .setAlign(Blockly.inputs.Align.RIGHT)

    this.appendValueInput('TEXT')
      .setCheck(checkTypeText)
      .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_TEXT)
      .setAlign(Blockly.inputs.Align.RIGHT)

    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_ORDER_PREFIX)
        .appendField(new Blockly.FieldDropdown(this.OPERATORS, Blockly.Blocks.text_replace_mappings.onchange), 'OP')
        .appendField(Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_ORDER)
        .setAlign(Blockly.inputs.Align.RIGHT)

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
