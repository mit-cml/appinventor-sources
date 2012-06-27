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
 * @fileoverview List blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

Blockly.Language.lists_create_empty = {
  // Create an empty list.
  category: 'Lists',
  helpUrl: 'http://en.wikipedia.org/wiki/Linked_list#Empty_lists',
  init: function() {
    this.setColour(210);
    this.setOutput(true);
    this.appendTitle('create empty list');
    this.setTooltip('Returns a list, of length 0, containing no data records');
  }
};

Blockly.Language.lists_create_with = {
  // Create a list with any number of elements of any type.
  category: 'Lists',
  helpUrl: '',
  init: function() {
    this.setColour(210);
    this.appendTitle('create list with');
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD0');
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD1');
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD2');
    this.setOutput(true);
    this.setMutator(new Blockly.Mutator(this, ['lists_create_with_item']));
    this.setTooltip('Create a list with any number of items.');
    this.itemCount_ = 3;
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
    var listBlock = new Blockly.Block(workspace, 'lists_create_with_container');
    listBlock.editable = false;
    listBlock.initSvg();
    var connection = listBlock.inputList[0];
    for (var x = 0; x < this.itemCount_; x++) {
      var itemBlock = new Blockly.Block(workspace, 'lists_create_with_item');
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

Blockly.Language.lists_create_with_container = {
  // Container.
  init: function() {
    this.setColour(210);
    this.appendTitle('add');
    this.appendInput('', Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip('Add, remove, or reorder sections to reconfigure this list block.');
    this.contextMenu = false;
  }
};

Blockly.Language.lists_create_with_item = {
  // Add items.
  init: function() {
    this.setColour(210);
    this.appendTitle('item');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Add an item to the list.');
    this.contextMenu = false;
  }
};

Blockly.Language.lists_repeat = {
  // Create a list with one element repeated.
  category: 'Lists',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour(210);
    this.setOutput(true);
    this.appendTitle('create list');
    this.appendInput('with item', Blockly.INPUT_VALUE, 'ITEM');
    this.appendInput('repeated', Blockly.INPUT_VALUE, 'NUM');
    this.setInputsInline(true);
    this.setTooltip('Creates a list consisting of the given value\n' +
                    'repeated the specified number of times.');
  }
};

Blockly.Language.lists_length = {
  // List length.
  category: 'Lists',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour(210);
    this.appendInput('length', Blockly.INPUT_VALUE, 'VALUE');
    this.setOutput(true);
    this.setTooltip('Returns the length of a list.');
  }
};

Blockly.Language.lists_isEmpty = {
  // Is the list empty?
  category: 'Lists',
  helpUrl: 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html',
  init: function() {
    this.setColour(210);
    this.appendInput('is empty', Blockly.INPUT_VALUE, 'VALUE');
    this.setOutput(true);
    this.setTooltip('Returns true if the list is empty.');
  }
};

Blockly.Language.lists_indexOf = {
  // Find an item in the list.
  category: 'Lists',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    this.setColour(210);
    this.setOutput(true);
    this.appendTitle('find');
    var menu = new Blockly.FieldDropdown(function() {
      return Blockly.Language.lists_indexOf.OPERATORS;
    });
    this.appendTitle(menu, 'END');
    this.appendInput('occurrence of item', Blockly.INPUT_VALUE, 'FIND');
    this.appendInput('in list', Blockly.INPUT_VALUE, 'VALUE');
    this.setInputsInline(true);
    this.setTooltip('Returns the index of the first/last occurrence\n' +
                    'of the item in the list.\n' +
                    'Returns 0 if text is not found.');
  }
};

Blockly.Language.lists_indexOf.OPERATORS =
    [['first', 'FIRST'], ['last', 'LAST']];

Blockly.Language.lists_getIndex = {
  // Get element at index.
  category: 'Lists',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour(210);
    this.setOutput(true);
    this.appendTitle('get item');
    this.appendInput('at', Blockly.INPUT_VALUE, 'AT');
    this.appendInput('in list', Blockly.INPUT_VALUE, 'VALUE');
    this.setInputsInline(true);
    this.setTooltip('Returns the value at the specified position in a list.');
  }
};

Blockly.Language.lists_setIndex = {
  // Set element at index.
  category: 'Lists',
  helpUrl: 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm',
  init: function() {
    this.setColour(210);
    this.appendTitle('set item');
    this.appendInput('at', Blockly.INPUT_VALUE, 'AT');
    this.appendInput('in list', Blockly.INPUT_VALUE, 'LIST');
    this.appendInput('to', Blockly.INPUT_VALUE, 'TO');
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('Sets the value at the specified position in a list.');
  }
};

// this.setTooltip('Returns the position of the specified item in a list.\nReturns 0 if the item is not present.');
