/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
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
 * @fileoverview Text blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.text = {
  // Text value.
  category: 'Text',
  helpUrl: 'http://en.wikipedia.org/wiki/String_(computer_science)',
  init: function() {
    this.setColour(160);
    this.appendTitle('\u201C');
    this.appendTitle(new Blockly.FieldTextInput(''), 'TEXT');
    this.appendTitle('\u201D');
    this.setOutput(true);
    this.setTooltip('A letter, word, or line of text.');
  }
};

Blockly.Language.text_join = {
  // Create a string made up of any number of elements of any type.
  category: 'Text',
  helpUrl: '',
  init: function() {
    this.setColour(160);
    this.appendTitle('create text with');
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD0');
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD1');
    this.setOutput(true);
    this.setMutator(new Blockly.Mutator(this, ['text_create_join_item']));
    this.setTooltip('Create a piece of text by joining\n' +
                    'together any number of items.');
    this.itemCount_ = 2;
  },
  mutationToDom: function(workspace) {
    var container = document.createElement('mutation');
    container.setAttribute('items', this.itemCount_);
    return container;
  },
  domToMutation: function(container) {
    for (var x = 0; x < this.itemCount_; x++) {
      this.removeInput('ADD' + x);
    }
    this.itemCount_ = window.parseInt(container.getAttribute('items'), 10);
    for (var x = 0; x < this.itemCount_; x++) {
      this.appendInput('', Blockly.INPUT_VALUE, 'ADD' + x);
    }
  },
  decompose: function(workspace) {
    var listBlock = new Blockly.Block(workspace, 'text_create_join_container');
    listBlock.editable = false;
    listBlock.initSvg();
    var connection = listBlock.inputList[0];
    for (var x = 0; x < this.itemCount_; x++) {
      var itemBlock = new Blockly.Block(workspace, 'text_create_join_item');
      itemBlock.initSvg();
      // Store a pointer to any connected blocks.
      itemBlock.valueInput_ = this.getInput('ADD' + x).targetConnection;
      connection.connect(itemBlock.previousConnection);
      connection = itemBlock.nextConnection;
    }
    return listBlock;
  },
  compose: function(listBlock) {
    // Disconnect all input blocks and destroy all inputs.
    for (var x = 0; x < this.itemCount_; x++) {
      this.removeInput('ADD' + x);
    }
    this.itemCount_ = 0;
    // Rebuild the block's inputs.
    var itemBlock = listBlock.getInputTargetBlock('STACK');
    while (itemBlock) {
      var input =
          this.appendInput('', Blockly.INPUT_VALUE, 'ADD' + this.itemCount_);
      // Reconnect any child blocks.
      if (itemBlock.valueInput_) {
        input.connect(itemBlock.valueInput_);
      }
      this.itemCount_++;
      itemBlock = itemBlock.nextConnection &&
          itemBlock.nextConnection.targetBlock();
    }
  }
};

Blockly.Language.text_create_join_container = {
  // Container.
  init: function() {
    this.setColour(160);
    this.appendTitle('add');
    this.appendInput('', Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip('Add, remove, or reorder sections to reconfigure this text block.');
    this.contextMenu = false;
  }
};

Blockly.Language.text_create_join_item = {
  // Add items.
  init: function() {
    this.setColour(160);
    this.appendTitle('item');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Add an item to the text.');
    this.contextMenu = false;
  }
};

Blockly.Language.text_length = {
  // String length.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour(160);
    this.appendInput('length', Blockly.INPUT_VALUE, 'VALUE');
    this.setOutput(true);
    this.setTooltip('Returns number of letters (including spaces)\n' +
                    'in the provided text.');
  }
};

Blockly.Language.text_isEmpty = {
  // Is the string null?
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour(160);
    this.appendInput('is empty', Blockly.INPUT_VALUE, 'VALUE');
    this.setOutput(true);
    this.setTooltip('Returns true if the provided text is empty.');
  }
};

Blockly.Language.text_endString = {
  // Return a leading or trailing substring.
  category: 'Text',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(160);
    this.setOutput(true);
    var menu = new Blockly.FieldDropdown(function() {
      return Blockly.Language.text_endString.OPERATORS;
    });
    this.appendInput(menu, Blockly.INPUT_VALUE, 'NUM');
    this.appendInput('letters in text', Blockly.INPUT_VALUE, 'TEXT');
    this.setInputsInline(true);
    this.setTooltip('Returns specified number of letters at the beginning or end of the text.');
  }
};

Blockly.Language.text_endString.OPERATORS =
    [['first', 'FIRST'], ['last', 'LAST']];

Blockly.Language.text_indexOf = {
  // Find a substring in the text.
  category: 'Text',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(160);
    this.setOutput(true);
    this.appendTitle('find');
    var menu = new Blockly.FieldDropdown(function() {
      return Blockly.Language.text_indexOf.OPERATORS;
    });
    this.appendTitle(menu, 'END');
    this.appendInput('occurrence of text', Blockly.INPUT_VALUE, 'FIND');
    this.appendInput('in text', Blockly.INPUT_VALUE, 'VALUE');
    this.setInputsInline(true);
    this.setTooltip('Returns the index of the first/last occurrence\n' +
                    'of first text in the second text.\n' +
                    'Returns 0 if text is not found.');
  }
};

Blockly.Language.text_indexOf.OPERATORS =
    [['first', 'FIRST'], ['last', 'LAST']];

Blockly.Language.text_charAt = {
  // Get a character from the string.
  category: 'Text',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour(160);
    this.appendTitle('letter');
    this.setOutput(true);
    this.appendInput('at', Blockly.INPUT_VALUE, 'AT');
    this.appendInput('in text', Blockly.INPUT_VALUE, 'VALUE');
    this.setInputsInline(true);
    this.setTooltip('Returns the letter at the specified position.');
  }
};

Blockly.Language.text_changeCase = {
  // Change capitalization.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(160);
    this.appendTitle('to');
    var menu = new Blockly.FieldDropdown(function() {
      return Blockly.Language.text_changeCase.OPERATORS;
    });
    this.appendInput(menu, Blockly.INPUT_VALUE, 'TEXT');
    this.setOutput(true);
    this.setTooltip('Return a copy of the text in a different case.');
  },
};

Blockly.Language.text_changeCase.OPERATORS =
    [['UPPER CASE', 'UPPERCASE'],
     ['lower case', 'LOWERCASE'],
     ['Title Case', 'TITLECASE']];

Blockly.Language.text_trim = {
  // Trim spaces.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(160);
    this.appendTitle('trim spaces from');
    var menu = new Blockly.FieldDropdown(function() {
      return Blockly.Language.text_trim.OPERATORS;
    }, function(text) {
      var newTitle = (text == 'both') ? 'sides' : 'side';
      this.setTitleText(newTitle, 'SIDES');
      this.setText(text);
    });
    this.appendTitle(menu, 'MODE');
    this.appendTitle('sides', 'SIDES');
    this.appendInput('', Blockly.INPUT_VALUE, 'TEXT');
    this.setOutput(true);
    this.setTooltip('Return a copy of the text with spaces\n' +
                    'removed from one or both ends.');
  },
  mutationToDom: function(workspace) {
    // Save whether the 'sides' title should be plural or singular.
    var container = document.createElement('mutation');
    var plural = (this.getTitleValue('MODE') == 'BOTH');
    container.setAttribute('plural', plural);
    return container;
  },
  domToMutation: function(container) {
    // Restore the 'sides' title as plural or singular.
    var plural = (container.getAttribute('plural') == 'true');
    this.setTitleText(plural ? 'sides' : 'side', 'SIDES');
  }
};

Blockly.Language.text_trim.OPERATORS =
    [['both', 'BOTH'], ['left', 'LEFT'], ['right', 'RIGHT']];

Blockly.Language.text_print = {
  // Print statement.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour(160);
    this.appendTitle('print');
    this.appendInput('', Blockly.INPUT_VALUE, 'TEXT');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Print the specified text, number or other value.');
  }
};
