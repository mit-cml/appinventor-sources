/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
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
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.lists_create_empty = {
  // Create an empty list.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_CREATE_EMPTY_HELPURL,
  init: function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendTitle(Blockly.LANG_LISTS_CREATE_EMPTY_TITLE_1);
    this.setTooltip(Blockly.LANG_LISTS_CREATE_EMPTY_TOOLTIP_1);
  }
};

Blockly.Language.lists_create_with = {
  // Create a list with any number of elements of any type.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: '',
  init: function() {
    this.setColour(210);
    this.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_INPUT_WITH);
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD0', null);
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD1', null);
    this.appendInput('', Blockly.INPUT_VALUE, 'ADD2', null);
    this.setOutput(true, Array);
    this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
    this.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_TOOLTIP_1);
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
      this.appendInput('', Blockly.INPUT_VALUE, 'ADD' + x, null);
    }
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace,
                                           'lists_create_with_container');
    containerBlock.initSvg();
    var connection = containerBlock.inputList[0];
    for (var x = 0; x < this.itemCount_; x++) {
      var itemBlock = new Blockly.Block(workspace, 'lists_create_with_item');
      itemBlock.initSvg();
      connection.connect(itemBlock.previousConnection);
      connection = itemBlock.nextConnection;
    }
    return containerBlock;
  },
  compose: function(containerBlock) {
    // Disconnect all input blocks and destroy all inputs.
    for (var x = this.itemCount_ - 1; x >= 0; x--) {
      this.removeInput('ADD' + x);
    }
    this.itemCount_ = 0;
    // Rebuild the block's inputs.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    while (itemBlock) {
      var input = this.appendInput('', Blockly.INPUT_VALUE,
                                   'ADD' + this.itemCount_, null);
      // Reconnect any child blocks.
      if (itemBlock.valueInput_) {
        input.connect(itemBlock.valueInput_);
      }
      this.itemCount_++;
      itemBlock = itemBlock.nextConnection &&
          itemBlock.nextConnection.targetBlock();
    }
  },
  saveConnections: function(containerBlock) {
    // Store a pointer to any connected child blocks.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    var x = 0;
    while (itemBlock) {
      var input = this.getInput('ADD' + x);
      itemBlock.valueInput_ = input && input.targetConnection;
      x++;
      itemBlock = itemBlock.nextConnection &&
          itemBlock.nextConnection.targetBlock();
    }
  }
};

Blockly.Language.lists_create_with_container = {
  // Container.
  init: function() {
    this.setColour(210);
    this.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD);
    this.appendInput('', Blockly.NEXT_STATEMENT, 'STACK');
    this.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.Language.lists_create_with_item = {
  // Add items.
  init: function() {
    this.setColour(210);
    this.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.Language.lists_repeat = {
  // Create a list with one element repeated.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_REPEAT_HELPURL,
  init: function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendTitle(Blockly.LANG_LISTS_REPEAT_TITLE_CREATELIST);
    this.appendInput(Blockly.LANG_LISTS_REPEAT_INPUT_WITH,
        Blockly.INPUT_VALUE, 'ITEM', null);
    this.appendInput(Blockly.LANG_LISTS_REPEAT_INPIT_REPEATED,
        Blockly.INPUT_VALUE, 'NUM', Number);
    this.appendInput(Blockly.LANG_LISTS_REPEAT_INPIT_TIMES,
        Blockly.DUMMY_INPUT, '', null);
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_LISTS_REPEAT_TOOLTIP_1);
  }
};

Blockly.Language.lists_length = {
  // List length.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_LENGTH_HELPURL,
  init: function() {
    this.setColour(210);
    this.appendInput(Blockly.LANG_LISTS_LENGTH_INPUT_LENGTH,
        Blockly.INPUT_VALUE, 'VALUE', [Array, String]);
    this.setOutput(true, Number);
    this.setTooltip(Blockly.LANG_LISTS_LENGTH_TOOLTIP_1);
  }
};

Blockly.Language.lists_isEmpty = {
  // Is the list empty?
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_IS_EMPTY_HELPURL,
  init: function() {
    this.setColour(210);
    this.appendInput(Blockly.LANG_LISTS_INPUT_IS_EMPTY,
        Blockly.INPUT_VALUE, 'VALUE', [Array, String]);
    this.setOutput(true, Boolean);
    this.setTooltip(Blockly.LANG_LISTS_TOOLTIP_1);
  }
};

Blockly.Language.lists_indexOf = {
  // Find an item in the list.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_INDEX_OF_HELPURL,
  init: function() {
    this.setColour(210);
    this.setOutput(true, Number);
    this.appendTitle(Blockly.LANG_LISTS_INDEX_OF_TITLE_FIND);
    var menu = new Blockly.FieldDropdown(this.OPERATORS);
    this.appendTitle(menu, 'END');
    this.appendInput(Blockly.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE,
        Blockly.INPUT_VALUE, 'FIND', null);
    this.appendInput(Blockly.LANG_LISTS_INDEX_OF_INPUT_IN_LIST,
        Blockly.INPUT_VALUE, 'VALUE', Array);
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_LISTS_INDEX_OF_TOOLTIP_1);
  }
};

Blockly.Language.lists_indexOf.OPERATORS =
    [[Blockly.LANG_LISTS_INDEX_OF_FIRST, 'FIRST'],
     [Blockly.LANG_LISTS_INDEX_OF_LAST, 'LAST']];

Blockly.Language.lists_getIndex = {
  // Get element at index.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_GET_INDEX_HELPURL,
  init: function() {
    this.setColour(210);
    this.setOutput(true, null);
    this.appendTitle(Blockly.LANG_LISTS_GET_INDEX_TITLE);
    this.appendInput(Blockly.LANG_LISTS_GET_INDEX_INPUT_AT,
        Blockly.INPUT_VALUE, 'AT', Number);
    this.appendInput(Blockly.LANG_LISTS_GET_INDEX_INPUT_IN_LIST,
        Blockly.INPUT_VALUE, 'VALUE', Array);
    this.setInputsInline(true);
    this.setTooltip(Blockly.LANG_LISTS_GET_INDEX_TOOLTIP_1);
  }
};

Blockly.Language.lists_setIndex = {
  // Set element at index.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_SET_INDEX_HELPURL,
  init: function() {
    this.setColour(210);
    this.appendTitle(Blockly.LANG_LISTS_SET_INDEX_TITLE);
    this.appendInput(Blockly.LANG_LISTS_SET_INDEX_INPUT_AT,
        Blockly.INPUT_VALUE, 'AT', Number);
    this.appendInput(Blockly.LANG_LISTS_SET_INDEX_INPUT_IN_LIST,
        Blockly.INPUT_VALUE, 'LIST', Array);
    this.appendInput(Blockly.LANG_LISTS_SET_INDEX_INPUT_TO,
        Blockly.INPUT_VALUE, 'TO', null);
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_LISTS_SET_INDEX_TOOLTIP_1);
  }
};
