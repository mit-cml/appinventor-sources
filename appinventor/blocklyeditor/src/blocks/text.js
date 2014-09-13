// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
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
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_TEXT_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendDummyInput().appendField('\u201C').appendField(
        new Blockly.FieldTextBlockInput(''),
        'TEXT').appendField('\u201D');
    this.setOutput(true, [Blockly.Blocks.text.connectionCheck]);
    this.setTooltip(Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_CATEGORY_TEXT }]
};

Blockly.Blocks.text.connectionCheck = function(myConnection,otherConnection) {
  var block = myConnection.sourceBlock_;
  var otherTypeArray = otherConnection.check_;
  for(var i=0;i<otherTypeArray.length;i++) {
    if(otherTypeArray[i] == "String") {
      return true;
    } else if(otherTypeArray[i] == "Number" && !isNaN(parseFloat(block.getFieldValue('TEXT')))) {
      return true;
    }
  }
  return false;
};

Blockly.Blocks['text_join'] = {
  // Create a string made up of any number of elements of any type.
  // TODO: (Andrew) Make this handle multiple arguments.
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_JOIN_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('ADD0').appendField('join');
    this.appendValueInput('ADD1');
    this.setTooltip(Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP);
    this.setMutator(new Blockly.Mutator(['text_join_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'ADD';
    this.itemCount_ = 2;
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'text_join_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    this.appendDummyInput(this.emptyInputName)
      .appendField('join');
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT));
    if(inputNum === 0){
      input.appendField('join');
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.inputList[0].fieldRow[0].setText("join");
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN }]

};

Blockly.Blocks['text_join_item'] = {
  // Add items.
  init: function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField("string");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip("");
    this.contextMenu = false;
  }
};

Blockly.Blocks['text_length'] = {
  // String length.
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_LENGTH_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('VALUE')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('length');
    this.setTooltip(Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH }]
};

Blockly.Blocks['text_isEmpty'] = {
  // Is the string null?
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('VALUE')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('is empty');
    this.setTooltip(Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY }]
};

Blockly.Blocks['text_compare'] = {
  // Compare two texts
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_COMPARE_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT1')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('compare texts');
    this.appendValueInput('TEXT2')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.text_compare.TOOLTIPS[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE + ' <',
    dropDown: {
      titleName: 'OP',
      value: 'LT'
    }
  },{
    translatedName: Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE  + ' =',
    dropDown: {
      titleName: 'OP',
      value: 'EQUAL'
    }
  },{
    translatedName: Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE + ' >',
    dropDown: {
      titleName: 'OP',
      value: 'GT'
    }
  }]
};

Blockly.Blocks.text_compare.OPERATORS = [ [ '<', 'LT' ], [ '=', 'EQUAL' ], [ '>', 'GT' ] ];

Blockly.Blocks.text_compare.TOOLTIPS = {
  LT : 'Tests whether text1 is lexicographically less than text2.\n'
      + 'if one text is the prefix of the other, the shorter text is\n'
      + 'considered smaller. Uppercase characters precede lowercase characters.',
  EQUAL : 'Tests whether text strings are identical, ie., have the same\n'
      + 'characters in the same order. This is different from ordinary =\n'
      + 'in the case where the text strings are numbers: 123 and 0123 are =\n' + 'but not text =.',
  GT : 'Reports whether text1 is lexicographically greater than text2.\n'
      + 'if one text is the prefix of the other, the shorter text is considered smaller.\n'
      + 'Uppercase characters precede lowercase characters.'
};

Blockly.Blocks['text_trim'] = {
  // trim string
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_TRIM_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('trim');
    this.setTooltip(Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM }]
};

Blockly.Blocks['text_changeCase'] = {
  // Change capitalization.
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl: function() {
      var mode = this.getFieldValue('OP');
      return Blockly.Blocks.text_changeCase.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getFieldValue('OP');
      return Blockly.Blocks.text_changeCase.TOOLTIPS[mode];
    });
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE,
    dropDown: {
      titleName: 'OP',
      value: 'UPCASE'
    }
  },{
    translatedName: Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE,
    dropDown: {
      titleName: 'OP',
      value: 'DOWNCASE'
    }
  }]
};

Blockly.Blocks.text_changeCase.OPERATORS = [ [ 'upcase', 'UPCASE' ], [ 'downcase', 'DOWNCASE' ] ];

Blockly.Blocks.text_changeCase.TOOLTIPS = {
  UPCASE : Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE,
  DOWNCASE : Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE
};

Blockly.Blocks.text_changeCase.HELPURLS = {
  UPCASE : Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE,
  DOWNCASE : Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE
};

Blockly.Blocks['text_starts_at'] = {
  // return index of first occurrence.
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('starts at')
      .appendField('text');
    this.appendValueInput('PIECE')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('piece')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT }]
};

Blockly.Blocks['text_contains'] = {
  // Is text contained in
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("boolean",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('contains')
      .appendField('text');
    this.appendValueInput('PIECE')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('piece')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS }]
};

Blockly.Blocks['text_split'] = {
  // This includes all four split variants (modes). The name and type of the 'AT' arg
  // changes to match the selected mode.
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : function() {
    var mode = this.getFieldValue('OP');
    return Blockly.Blocks.text_split.HELPURLS[mode];
  },
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
      .appendField(new Blockly.FieldDropdown(this.OPERATORS, Blockly.Blocks.text_split.dropdown_onchange), 'OP')
      .appendField('text');
    this.appendValueInput('AT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT))
      .appendField(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME')
      .setAlign(Blockly.ALIGN_RIGHT);
  },
  // adjust for the mode when the block is read in
  domToMutation: function(xmlElement) {
    var mode = xmlElement.getAttribute('mode');
    Blockly.Blocks.text_split.adjustToMode(mode, this);
  },
  // put the mode in the DOM so it can be read in by domToMutation
  // WARNING:  Note that the 'mode' tag below is lowercase.  It would not work
  // to make it uppercase ('MODE').  There's a bug somewhere (in the browser?) that
  // writes tags as lowercase.  So writing the date with tag 'MODE' and attempting
  // to read with tag 'MODE' wil not work.
  mutationToDom: function() {
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
  },{
    translatedName: Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATFIRST'
    }
  },{
    translatedName: Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATANY'
    }
  },{
    translatedName: Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATFIRSTOFANY'
    }
  }]
};

// Change the name and type of ARG2 and set tooltop depending on mode
Blockly.Blocks.text_split.adjustToMode = function(mode, block) {
  if (mode == 'SPLITATFIRST' || mode == 'SPLIT') {
    block.getInput("AT").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text", Blockly.Blocks.Utilities.INPUT));
    block.setFieldValue(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME');
  } else if (mode == 'SPLITATFIRSTOFANY' || mode == 'SPLITATANY') {
    block.getInput("AT").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("list", Blockly.Blocks.Utilities.INPUT));
    block.setFieldValue(Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST, 'ARG2_NAME');
  };
  block.setTooltip(Blockly.Blocks.text_split.TOOLTIPS[mode]);
};

Blockly.Blocks.text_split.dropdown_onchange = function(mode) {
  Blockly.Blocks.text_split.adjustToMode(mode, this.sourceBlock_)
};

// The order here determines the order in the dropdown
Blockly.Blocks.text_split.OPERATORS = [
  [ 'split', 'SPLIT' ],
  [ 'split at first', 'SPLITATFIRST' ],
  [ 'split at any', 'SPLITATANY' ],
  [ 'split at first of any', 'SPLITATFIRSTOFANY' ]
];

Blockly.Blocks.text_split.TOOLTIPS = {
  SPLITATFIRST : Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST,
  SPLITATFIRSTOFANY : Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY ,
  SPLIT : Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT,
  SPLITATANY : Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY
};

Blockly.Blocks.text_split.HELPURLS = {
  SPLITATFIRST : Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST,
  SPLITATFIRSTOFANY : Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY ,
  SPLIT : Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT,
  SPLITATANY : Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY
};

Blockly.Blocks['text_split_at_spaces'] = {
  // Split at spaces
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("list",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT').setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT)).appendField('split at spaces');
    this.setTooltip(Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE }]
};

Blockly.Blocks['text_segment'] = {
  // Create text segment
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('segment')
      .appendField('text');
    this.appendValueInput('START')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT))
      .appendField('start')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('LENGTH')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("number",Blockly.Blocks.Utilities.INPUT))
      .appendField('length')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT }]
};

Blockly.Blocks['text_replace_all'] = {
  // Replace all occurrences of text
  category : Blockly.Msg.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('replace all')
      .appendField('text');
    this.appendValueInput('SEGMENT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('segment')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('REPLACEMENT')
      .setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT))
      .appendField('replacement')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP);
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL }]
};
