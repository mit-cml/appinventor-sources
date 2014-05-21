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
 * @fileoverview Text blocks for Blockly, modified for App Inventor
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * @author fraser@google.com (Neil Fraser) Due to the frequency of long strings,
 *         the 80-column wrap rule need not apply to language files.
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.text = {
  // Text value.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_TEXT_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendDummyInput().appendTitle('\u201C').appendTitle(
        new Blockly.FieldTextBlockInput(''),
        'TEXT').appendTitle('\u201D');
    this.setOutput(true, [Blockly.Language.text.connectionCheck]);
    this.setTooltip(Blockly.LANG_TEXT_TEXT_TOOLTIP);
    this.appendCollapsedInput().appendTitle('', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_CATEGORY_TEXT }],
  prepareCollapsedText: function(){
    var textToDisplay = this.getTitleValue('TEXT');
    if (textToDisplay.length > 8 ) // 8 is a length of 5 plus 3 dots
        textToDisplay = textToDisplay.substring(0, 5) + '...';
    this.getTitle_('COLLAPSED_TEXT').setText(textToDisplay, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.text.connectionCheck = function(myConnection,otherConnection) {
  var block = myConnection.sourceBlock_;
  var otherTypeArray = otherConnection.check_;
  for(var i=0;i<otherTypeArray.length;i++) {
    if(otherTypeArray[i] == "String") {
      return true;
    } else if(otherTypeArray[i] == "Number" && !isNaN(parseFloat(block.getTitleValue('TEXT')))) {
      return true;
    }
  }
  return false;
};

Blockly.Language.text_join = {
  // Create a string made up of any number of elements of any type.
  // TODO: (Andrew) Make this handle multiple arguments.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_JOIN_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.OUTPUT));
    this.appendValueInput('ADD0').appendTitle('join');
    this.appendValueInput('ADD1');
    this.setTooltip(Blockly.LANG_TEXT_JOIN_TOOLTIP);
    this.setMutator(new Blockly.Mutator(['text_join_item']));
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'ADD';
    this.itemCount_ = 2;
    this.appendCollapsedInput().appendTitle('join', 'COLLAPSED_TEXT');
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
      .appendTitle('join');
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum).setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT));
    if(inputNum === 0){
      input.appendTitle('join');
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.inputList[0].titleRow[0].setText("join");
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_JOIN_TITLE_JOIN }]

};

Blockly.Language.text_join_item = {
  // Add items.
  init: function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.appendDummyInput()
        .appendTitle("string");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip("");
    this.contextMenu = false;
  }
};

Blockly.Language.text_length = {
  // String length.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_LENGTH_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('VALUE')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('length');
    this.setTooltip(Blockly.LANG_TEXT_LENGTH_TOOLTIP);
    this.appendCollapsedInput().appendTitle('length', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_LENGTH_INPUT_LENGTH }]
};

Blockly.Language.text_isEmpty = {
  // Is the string null?
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_ISEMPTY_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('VALUE')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('is empty');
    this.setTooltip(Blockly.LANG_TEXT_ISEMPTY_TOOLTIP);
    this.appendCollapsedInput().appendTitle('empty', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY }]
};

Blockly.Language.text_compare = {
  // Compare two texts
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_COMPARE_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT1')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('compare texts');
    this.appendValueInput('TEXT2')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.text_compare.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle('compare', 'COLLAPSED_TEXT');
  },
  typeblock: [{
    translatedName: Blockly.LANG_TEXT_COMPARE_INPUT_COMPARE + ' <',
    dropDown: {
      titleName: 'OP',
      value: 'LT'
    }
  },{
    translatedName: Blockly.LANG_TEXT_COMPARE_INPUT_COMPARE  + ' =',
    dropDown: {
      titleName: 'OP',
      value: 'EQUAL'
    }
  },{
    translatedName: Blockly.LANG_TEXT_COMPARE_INPUT_COMPARE + ' >',
    dropDown: {
      titleName: 'OP',
      value: 'GT'
    }
  }]
};

Blockly.Language.text_compare.OPERATORS = [ [ '<', 'LT' ], [ '=', 'EQUAL' ], [ '>', 'GT' ] ];

Blockly.Language.text_compare.TOOLTIPS = {
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

Blockly.Language.text_trim = {
  // trim string
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_TRIM_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('trim');
    this.setTooltip(Blockly.LANG_TEXT_TRIM_TOOLTIP);
    this.appendCollapsedInput().appendTitle('trim', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_TRIM_TITLE_TRIM }]
};

Blockly.Language.text_changeCase = {
  // Change capitalization.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl: function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.text_changeCase.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.text_changeCase.TOOLTIPS[mode];
    });
    this.appendCollapsedInput().appendTitle(this.getTitleValue('OP'), 'COLLAPSED_TEXT');
  },
  typeblock: [{
    translatedName: Blockly.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE,
    dropDown: {
      titleName: 'OP',
      value: 'UPCASE'
    }
  },{
    translatedName: Blockly.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE,
    dropDown: {
      titleName: 'OP',
      value: 'DOWNCASE'
    }
  }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

Blockly.Language.text_changeCase.OPERATORS = [ [ 'upcase', 'UPCASE' ], [ 'downcase', 'DOWNCASE' ] ];

Blockly.Language.text_changeCase.TOOLTIPS = {
  UPCASE : Blockly.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE,
  DOWNCASE : Blockly.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE
};

Blockly.Language.text_changeCase.HELPURLS = {
  UPCASE : Blockly.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE,
  DOWNCASE : Blockly.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE
};

Blockly.Language.text_starts_at = {
  // return index of first occurrence.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_STARTS_AT_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('starts at')
      .appendTitle('text');
    this.appendValueInput('PIECE')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('piece')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.LANG_TEXT_STARTS_AT_TOOLTIP);
    this.appendCollapsedInput().appendTitle('starts', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT }]
};

Blockly.Language.text_contains = {
  // Is text contained in
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_CONTAINS_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("boolean",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('contains')
      .appendTitle('text');
    this.appendValueInput('PIECE')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('piece')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.LANG_TEXT_CONTAINS_TOOLTIP);
    this.appendCollapsedInput().appendTitle('contains', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_CONTAINS_INPUT_CONTAINS }]
};

Blockly.Language.text_split = {
  // This includes all four split variants (modes). The name and type of the 'AT' arg
  // changes to match the selected mode.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : function() {
      var mode = this.getTitleValue('OP');
      return Blockly.Language.text_split.HELPURLS[mode];
    },
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("list",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle(new Blockly.FieldDropdown(this.OPERATORS,Blockly.Language.text_split.dropdown_onchange), 'OP')
      .appendTitle('text');
    this.appendValueInput('AT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle(Blockly.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME')
      .setAlign(Blockly.ALIGN_RIGHT);
   // This line seems to be necessary to create some kind of collapsed text, even if it's the
   // wrong mode.  (Why does it end up showing the correct title?)
   this.appendCollapsedInput().appendTitle(this.getTitleValue('OP'), 'COLLAPSED_TEXT');
  },
// adjust for the mode when the block is read in
domToMutation: function(xmlElement) {
    var mode = xmlElement.getAttribute('mode');
    Blockly.Language.text_split.adjustToMode(mode, this);
  },
  // put the mode in the DOM so it can be read in by domToMutation
  // WARNING:  Note that the 'mode' tag below is lowercase.  It would not work
  // to make it uppercase ('MODE').  There's a bug somewhere (in the browser?) that
  // writes tags as lowercase.  So writing the date with tag 'MODE' and attempting
  // to read with tag 'MODE' wil not work.
  mutationToDom: function() {
    var container = document.createElement('mutation');
    var savedMode = this.getTitleValue('OP');
    container.setAttribute('mode', savedMode);
    return container;
  },
  typeblock: [{
    translatedName: Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT,
    dropDown: {
      titleName: 'OP',
      value: 'SPLIT'
    }
  },{
    translatedName: Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATFIRST'
    }
  },{
    translatedName: Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATANY'
    }
  },{
    translatedName: Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY,
    dropDown: {
      titleName: 'OP',
      value: 'SPLITATFIRSTOFANY'
    }
  }],
  prepareCollapsedText: function(){
    var titleFromOperator = Blockly.FieldDropdown.lookupOperator(this.OPERATORS, this.getTitleValue('OP'));
    this.getTitle_('COLLAPSED_TEXT').setText(titleFromOperator, 'COLLAPSED_TEXT');
  }
};

// Change the name and type of ARG2 and set tooltop depending on mode
Blockly.Language.text_split.adjustToMode = function(mode, block) {
  if (mode == 'SPLITATFIRST' || mode == 'SPLIT') {
    block.getInput("AT").setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT));
    block.setTitleValue(Blockly.LANG_TEXT_SPLIT_INPUT_AT, 'ARG2_NAME');
  } else if (mode == 'SPLITATFIRSTOFANY' || mode == 'SPLITATANY') {
    block.getInput("AT").setCheck(Blockly.Language.YailTypeToBlocklyType("list",Blockly.Language.INPUT));
    block.setTitleValue(Blockly.LANG_TEXT_SPLIT_INPUT_AT_LIST, 'ARG2_NAME');
  };
  block.setTooltip(Blockly.Language.text_split.TOOLTIPS[mode]);
};

Blockly.Language.text_split.dropdown_onchange = function(mode) {
  Blockly.Language.text_split.adjustToMode(mode, this.sourceBlock_)
};

// The order here determines the order in the dropdown
Blockly.Language.text_split.OPERATORS = [
  [ 'split', 'SPLIT' ],
  [ 'split at first', 'SPLITATFIRST' ],
  [ 'split at any', 'SPLITATANY' ],
  [ 'split at first of any', 'SPLITATFIRSTOFANY' ]
];

Blockly.Language.text_split.TOOLTIPS = {
  SPLITATFIRST : Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST,
  SPLITATFIRSTOFANY : Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY ,
  SPLIT : Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT,
  SPLITATANY : Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY
};

Blockly.Language.text_split.HELPURLS = {
  SPLITATFIRST : Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST,
  SPLITATFIRSTOFANY : Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY ,
  SPLIT : Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT,
  SPLITATANY : Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY
};

Blockly.Language.text_split_at_spaces = {
  // Split at spaces
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_SPLIT_AT_SPACES_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("list",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT').setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT)).appendTitle('split at spaces');
    this.setTooltip(Blockly.LANG_TEXT_SPLIT_AT_TOOLTIP);
    this.appendCollapsedInput().appendTitle('split at spaces', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_SPLIT_AT_SPACES_TITLE }]
};

Blockly.Language.text_segment = {
  // Create text segment
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_SEGMENT_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('segment')
      .appendTitle('text');
    this.appendValueInput('START')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
      .appendTitle('start')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('LENGTH')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("number",Blockly.Language.INPUT))
      .appendTitle('length')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.LANG_TEXT_SEGMENT_AT_TOOLTIP);
    this.appendCollapsedInput().appendTitle('segment', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_SEGMENT_TITLE_SEGMENT }]
};

Blockly.Language.text_replace_all = {
  // Replace all occurrences of text
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : Blockly.LANG_TEXT_REPLACE_ALL_HELPURL,
  init : function() {
    this.setColour(Blockly.TEXT_CATEGORY_HUE);
    this.setOutput(true, Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.OUTPUT));
    this.appendValueInput('TEXT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('replace all')
      .appendTitle('text');
    this.appendValueInput('SEGMENT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('segment')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('REPLACEMENT')
      .setCheck(Blockly.Language.YailTypeToBlocklyType("text",Blockly.Language.INPUT))
      .appendTitle('replacement')
      .setAlign(Blockly.ALIGN_RIGHT);
    this.setTooltip(Blockly.LANG_TEXT_REPLACE_ALL_TOOLTIP);
    this.appendCollapsedInput().appendTitle('replace all', 'COLLAPSED_TEXT');
  },
  typeblock: [{ translatedName: Blockly.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL }]
};
