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
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.appendDummyInput().appendTitle('\u201C').appendTitle(new Blockly.FieldTextInput(''),
        'TEXT').appendTitle('\u201D');
    this.setOutput(true, String);
    this.setTooltip('A text string.');
  }
};

Blockly.Language.text_join = {
  // Create a string made up of any number of elements of any type.
  // TODO: (Andrew) Make this handle multiple arguments.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, String);
    this.appendValueInput('ADD0').appendTitle('join');
    this.appendValueInput('ADD1');
    this.setTooltip('Appends all the inputs to form a single text string.\n'
        + 'If there are no inputs, makes an empty text.');
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
      .appendTitle('join');
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum);
    if(inputNum == 0){
      input.appendTitle('join');
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.inputList[0].titleRow[0].setText("join");
  }

};

Blockly.Language.text_join_item = {
  // Add items.
  init: function() {
    this.setColour(160);
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
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, Number);
    this.appendValueInput('VALUE').setCheck(String).appendTitle('length');
    this.setTooltip('Returns number of characters (including spaces)\n' + 'in the provided text.');
  }
};

Blockly.Language.text_isEmpty = {
  // Is the string null?
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, Boolean);
    this.appendValueInput('VALUE').setCheck(String).appendTitle('is empty');
    this.setTooltip('Returns true if the length of the\n' + 'text is 0, false otherwise.');
  }
};

Blockly.Language.text_compare = {
  // Compare two texts
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, Boolean);
    this.appendValueInput('TEXT1').setCheck(String).appendTitle('compare texts');
    this.appendValueInput('TEXT2').setCheck(String).appendTitle(
        new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    this.setInputsInline(true);
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.text_compare.TOOLTIPS[mode];
    });
  }
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
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, String);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('trim');
    this.setTooltip('Returns a copy of it text string arguments with any\n'
        + 'leading or trailing spaces removed.');
  }
};

Blockly.Language.text_changeCase = {
  // Change capitalization.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, String);
    this.appendValueInput('TEXT').setCheck(String).appendTitle(
        new Blockly.FieldDropdown(this.OPERATORS), 'OP');
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.text_changeCase.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.text_changeCase.OPERATORS = [ [ 'upcase', 'UPCASE' ], [ 'downcase', 'DOWNCASE' ] ];

Blockly.Language.text_changeCase.TOOLTIPS = {
  UPCASE : 'Returns a copy of its text string argument converted to uppercase.',
  DOWNCASE : 'Returns a copy of its text string argument converted to lowercase.'
};

Blockly.Language.text_starts_at = {
  // return index of first occurrence.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, Number);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('starts at').appendTitle('text');
    this.appendValueInput('PIECE').setCheck(String).appendTitle('piece');
    this.setTooltip('Returns the starting index of the piece in the text.\n'
        + 'where index 1 denotes the beginning of the text. Returns 0 if the\n'
        + 'piece is not in the text.');
  }
};

Blockly.Language.text_contains = {
  // Is text contained in
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, Boolean);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('contains').appendTitle('text');
    this.appendValueInput('PIECE').setCheck(String).appendTitle('piece');
    this.setTooltip('Tests whether the piece is contained in the text.');
  }
};

Blockly.Language.text_split = {
  // Splits at first
  // TODO: (Hal) Make this handle type change for the dropdown.
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, Array);
    this.appendValueInput('TEXT').setCheck(String).appendTitle(
        new Blockly.FieldDropdown(this.OPERATORS), 'OP').appendTitle('text');
    this.appendValueInput('AT').setCheck(String).appendTitle('at');
    var thisBlock = this;
    this.setTooltip(function() {
      var mode = thisBlock.getTitleValue('OP');
      return Blockly.Language.text_split.TOOLTIPS[mode];
    });
  }
};

Blockly.Language.text_split.OPERATORS = [ [ 'split at first', 'SPLITATFIRST' ],
    [ 'split at first of any', 'SPLITATFIRSTOFANY' ], [ 'split', 'SPLIT' ],
    [ 'split at any', 'SPLITATANY' ] ];

Blockly.Language.text_split.TOOLTIPS = {
  SPLITATFIRST : 'Splits the text into two pieces separated by the first occurrence of \'at\'.\n'
      + 'Returns a two-element list with the two pieces. Returns a one-element list with original\n'
      + 'text if \'at\' is not contained in the text.',
  SPLITATFIRSTOFANY : 'Splits the text into two pieces separated by the first\n'
      + 'occurrence of any of the elements in the list \'at\'\n'
      + 'and returns these pieces. Returns a one-element list with original\n'
      + 'text if \'at\' is not contained in the text.',
  SPLIT : 'Split the text into pieces separated by the\n'
      + 'occurrences of \'at\' and return the list of these pieces.\n'
      + 'Returns a one-element list with the original\n'
      + 'text if \'at\' is not contained in the text.',
  SPLITATANY : 'Split the text into pieces separated by the\n'
      + 'occurrences of any of the elements in the list \'at\' and\n'
      + 'return the list of these pieces. Returns a one-element list\n'
      + 'with the original text if \'at\' is not contained in the text.'
};

Blockly.Language.text_split_at_spaces = {
  // Split at spaces
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, Array);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('split at spaces');
    this.setTooltip('Split the text into pieces separated by spaces.');
  }
};

Blockly.Language.text_segment = {
  // Create text segment
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, String);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('segment').appendTitle('text');
    this.appendValueInput('START').setCheck(Number).appendTitle('start');
    this.appendValueInput('LENGTH').setCheck(Number).appendTitle('length');
    this.setTooltip('Extracts the segment of the given length from the given text\n'
        + 'starting from the given text starting from the given position. Position\n'
        + '1 denotes the beginning of the text.');
  }
};

Blockly.Language.text_replace_all = {
  // Replace all occurrences of text
  category : Blockly.LANG_CATEGORY_TEXT,
  helpUrl : '',
  init : function() {
    this.setColour(160);
    this.setOutput(true, String);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('replace all').appendTitle('text');
    this.appendValueInput('SEGMENT').setCheck(String).appendTitle('segment');
    this.appendValueInput('REPLACEMENT').setCheck(String).appendTitle('replacement');
    this.setTooltip('Returns a new text obtained by replacing all occurrences\n'
        + 'of the segment with the replacement.');
  }
};
