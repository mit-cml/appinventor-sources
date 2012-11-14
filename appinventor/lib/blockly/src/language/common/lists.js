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
'use strict';

Blockly.Language.lists_create_empty = {
  // Create an empty list.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: Blockly.LANG_LISTS_CREATE_EMPTY_HELPURL,
  init: function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_LISTS_CREATE_EMPTY_TITLE_1);
    this.setTooltip(Blockly.LANG_LISTS_CREATE_EMPTY_TOOLTIP_1);
  }
};

Blockly.Language.lists_create_with = {
  // Create a list with any number of elements of any type.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: '',
  init: function() {
    this.setColour(210);
    this.appendValueInput('ADD0')
        .appendTitle(Blockly.LANG_LISTS_CREATE_WITH_INPUT_WITH);
    this.appendValueInput('ADD1');
    this.appendValueInput('ADD2');
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
      var input = this.appendValueInput('ADD' + x);
      if (x == 0) {
        input.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_INPUT_WITH);
      }
    }
    if (this.itemCount_ == 0) {
      this.appendDummyInput('EMPTY')
          .appendTitle(Blockly.LANG_LISTS_CREATE_EMPTY_TITLE_1);
    }
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace,
                                           'lists_create_with_container');
    containerBlock.initSvg();
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 0; x < this.itemCount_; x++) {
      var itemBlock = new Blockly.Block(workspace, 'lists_create_with_item');
      itemBlock.initSvg();
      connection.connect(itemBlock.previousConnection);
      connection = itemBlock.nextConnection;
    }
    return containerBlock;
  },
  compose: function(containerBlock) {
    // Disconnect all input blocks and remove all inputs.
    if (this.itemCount_ == 0) {
      this.removeInput('EMPTY');
    } else {
      for (var x = this.itemCount_ - 1; x >= 0; x--) {
        this.removeInput('ADD' + x);
      }
    }
    this.itemCount_ = 0;
    // Rebuild the block's inputs.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    while (itemBlock) {
      var input = this.appendValueInput('ADD' + this.itemCount_);
      if (this.itemCount_ == 0) {
        input.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_INPUT_WITH);
      }
      // Reconnect any child blocks.
      if (itemBlock.valueConnection_) {
        input.connection.connect(itemBlock.valueConnection_);
      }
      this.itemCount_++;
      itemBlock = itemBlock.nextConnection &&
          itemBlock.nextConnection.targetBlock();
    }
    if (this.itemCount_ == 0) {
      this.appendDummyInput('EMPTY')
          .appendTitle(Blockly.LANG_LISTS_CREATE_EMPTY_TITLE_1);
    }
  },
  saveConnections: function(containerBlock) {
    // Store a pointer to any connected child blocks.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    var x = 0;
    while (itemBlock) {
      var input = this.getInput('ADD' + x);
      itemBlock.valueConnection_ = input && input.connection.targetConnection;
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
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD);
    this.appendStatementInput('STACK');
    this.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP_1);
    this.contextMenu = false;
  }
};

Blockly.Language.lists_create_with_item = {
  // Add items.
  init: function() {
    this.setColour(210);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE);
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
    this.appendValueInput('ITEM')
        .appendTitle(Blockly.LANG_LISTS_REPEAT_INPUT_WITH);
    this.appendValueInput('NUM')
        .setCheck(Number)
        .appendTitle(Blockly.LANG_LISTS_REPEAT_INPUT_REPEATED);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_LISTS_REPEAT_INPUT_TIMES);
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
    this.appendValueInput('VALUE')
        .setCheck([Array, String])
        .appendTitle(Blockly.LANG_LISTS_LENGTH_INPUT_LENGTH);
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
    this.appendValueInput('VALUE')
        .setCheck([Array, String])
        .appendTitle(Blockly.LANG_LISTS_INPUT_IS_EMPTY);
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
    this.appendValueInput('FIND')
        .appendTitle(Blockly.LANG_LISTS_INDEX_OF_TITLE_FIND)
        .appendTitle(new Blockly.FieldDropdown(this.OPERATORS), 'END')
        .appendTitle(Blockly.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE);
    this.appendValueInput('VALUE')
        .setCheck(Array)
        .appendTitle(Blockly.LANG_LISTS_INDEX_OF_INPUT_IN_LIST);
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
    this.appendValueInput('AT')
        .setCheck(Number)
        .appendTitle(Blockly.LANG_LISTS_GET_INDEX_INPUT_AT);
    this.appendValueInput('VALUE')
        .setCheck(Array)
        .appendTitle(Blockly.LANG_LISTS_GET_INDEX_INPUT_IN_LIST);
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
    this.appendValueInput('AT')
        .setCheck(Number)
        .appendTitle(Blockly.LANG_LISTS_SET_INDEX_INPUT_AT);
    this.appendValueInput('LIST')
        .setCheck(Array)
        .appendTitle(Blockly.LANG_LISTS_SET_INDEX_INPUT_IN_LIST);
    this.appendValueInput('TO')
        .appendTitle(Blockly.LANG_LISTS_SET_INDEX_INPUT_TO);
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_LISTS_SET_INDEX_TOOLTIP_1);
  }
};
