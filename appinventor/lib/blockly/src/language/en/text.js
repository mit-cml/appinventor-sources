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
    this.addTitle('\u201C');
    this.addTitle(new Blockly.FieldTextInput(''));
    this.addTitle('\u201D');
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
    this.addTitle('create text with');
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
    this.setMutator(new Blockly.Mutator(this, ['text_create_join_item']));
    this.setTooltip('Create a piece of text by joining\ntogether any number of items.');
    this.itemCount_ = 2;
  },
  mutationToDom: function(workspace) {
    var container = document.createElement('mutation');
    container.setAttribute('items', this.itemCount_);
    return container;
  },
  domToMutation: function(container) {
    while (this.inputList.length) {
      this.removeInput(0);
    }
    this.itemCount_ = window.parseInt(container.getAttribute('items'), 10);
    for (var x = 0; x < this.itemCount_; x++) {
      this.addInput('', '', Blockly.INPUT_VALUE);
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
      itemBlock.valueInput_ = this.inputList[x].targetConnection;
      connection.connect(itemBlock.previousConnection);
      connection = itemBlock.nextConnection;
    }
    return listBlock;
  },
  compose: function(listBlock) {
    // Disconnect all input blocks.
    for (var x = 0; x < this.inputList.length; x++) {
      var child = this.inputList[x].targetBlock();
      if (child) {
        child.setParent(null);
      }
    }
    // Destroy all inputs.
    while (this.inputList.length) {
      this.removeInput(0);
    }
    this.itemCount_ = 0;
    // Rebuild the block's inputs.
    var itemBlock = listBlock.getStatementInput(0);
    while (itemBlock) {
      this.addInput('', '', Blockly.INPUT_VALUE);
      // Reconnect any child blocks.
      if (itemBlock.valueInput_) {
        this.inputList[this.itemCount_].connect(itemBlock.valueInput_);
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
    this.addTitle('add');
    this.addInput('', '', Blockly.NEXT_STATEMENT);
    this.setTooltip('Add, remove, or reorder sections to reconfigure this text block.');
    this.contextMenu = false;
  }
};

Blockly.Language.text_create_join_item = {
  // Add items.
  init: function() {
    this.setColour(160);
    this.addTitle('item');
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
    this.addInput('length', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
    this.setTooltip('Returns number of letters (including spaces)\nin the provided text.');
  }
};

Blockly.Language.text_isEmpty = {
  // Is the string null?
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour(160);
    this.addInput('is empty', '', Blockly.INPUT_VALUE);
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
      return [thisBlock.MSG_FIRST, thisBlock.MSG_LAST];
    });
    this.addInput(menu, '', Blockly.INPUT_VALUE);
    this.addInput('letters in text', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
    this.setTooltip('Returns specified number of letters at the beginning or end of the text.');
  },
  MSG_FIRST: 'first',
  MSG_LAST: 'last'
};

Blockly.Language.text_indexOf = {
  // Find a substring in the text.
  category: 'Text',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(160);
    this.setOutput(true);
    this.addTitle('find');
    var menu = new Blockly.FieldDropdown(function() {
      return [thisBlock.MSG_FIRST, thisBlock.MSG_LAST];
    });
    this.addTitle(menu);
    this.addInput('occurrence of text', '', Blockly.INPUT_VALUE);
    this.addInput('in text', '', Blockly.INPUT_VALUE);
    this.setInputsInline(true);
    this.setTooltip('Returns the index of the first/last occurrence\nof first text in the second text.\nReturns 0 if text is not found.');
  },
  MSG_FIRST: 'first',
  MSG_LAST: 'last'
};

Blockly.Language.text_charAt = {
  // Get a character from the string.
  category: 'Text',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour(160);
    this.addTitle('letter');
    this.setOutput(true);
    this.addInput('at', '', Blockly.INPUT_VALUE);
    this.addInput('in text', '', Blockly.INPUT_VALUE);
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
    this.addTitle('to');
    var menu = new Blockly.FieldDropdown(function() {
      return [thisBlock.MSG_UPPERCASE, thisBlock.MSG_LOWERCASE, thisBlock.MSG_TITLECASE];
    });
    this.addInput(menu, '', Blockly.INPUT_VALUE);
    this.setOutput(true);
    this.setTooltip('Return a copy of the text in a different case.');
  },
  MSG_UPPERCASE: 'UPPER CASE',
  MSG_LOWERCASE: 'lower case',
  MSG_TITLECASE: 'Title Case'
};

Blockly.Language.text_trim = {
  // Trim spaces.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(160);
    this.addTitle('trim spaces from');
    var menu = new Blockly.FieldDropdown(function() {
      return [thisBlock.MSG_BOTH, thisBlock.MSG_LEFT, thisBlock.MSG_RIGHT];
    }, function(text) {
      var newTitle = (text == thisBlock.MSG_BOTH) ? 'sides' : 'side';
      sideTitle.setText(newTitle);
      this.setText(text);
    });
    this.addTitle(menu);
    var sideTitle = this.addTitle('sides');
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.setOutput(true);
    this.setTooltip('Return a copy of the text with spaces\nremoved from one or both ends.');
  },
  mutationToDom: function(workspace) {
    // Save whether the third title should be plural or singular.
    var container = document.createElement('mutation');
    var plural = (this.getTitleText(1) == this.MSG_BOTH);
    container.setAttribute('plural', plural);
    return container;
  },
  domToMutation: function(container) {
    // Restore the third title as plural or singular.
    var plural = (container.getAttribute('plural') == 'true')
    this.setTitleText(plural ? 'sides' : 'side', 2);
  },
  MSG_BOTH: 'both',
  MSG_LEFT: 'left',
  MSG_RIGHT: 'right'
};

Blockly.Language.text_print = {
  // Print statement.
  category: 'Text',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour(160);
    this.addTitle('print');
    this.addInput('', '', Blockly.INPUT_VALUE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Print the specified text, number or other value.');
  }
};
