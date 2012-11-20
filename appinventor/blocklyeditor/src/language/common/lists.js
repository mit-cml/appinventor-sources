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
 * @fileoverview List blocks for Blockly, modified for App Inventor
 * @author fraser@google.com (Neil Fraser)
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

if (!Blockly.Language) Blockly.Language = {};

Blockly.Language.lists_create_with = {
  // Create a list with any number of elements of any type.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: '',
  init: function() {
    this.setColour(210);
    this.appendValueInput('ADD0')
        .appendTitle("make a list");
    this.appendValueInput('ADD1');
    this.setOutput(true, Array);
    this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
    this.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_TOOLTIP_1);
    this.itemCount_ = 2;
    this.emptyInputName = 'EMPTY';
    this.repeatingInputName = 'ADD';
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'lists_create_with_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){
    this.appendDummyInput(this.emptyInputName)
      .appendTitle(Blockly.LANG_LISTS_CREATE_EMPTY_TITLE_1);
  },
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum);
    if(inputNum == 0){
      input.appendTitle("make a list");
    }
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.inputList[0].titleRow[0].setText(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD);
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

Blockly.Language.lists_select_item = {
  // Select from list an item.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, null);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('select list item').appendTitle('list');
    this.appendValueInput('NUM').setCheck(Number).appendTitle('index').setAlign(Blockly.ALIGN_RIGHT);
    Blockly.Language.setTooltip(this, 'Get the nth item from a list.');
  }
};

Blockly.Language.lists_replace_item = {
  // Replace Item in list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('replace list item').appendTitle('list');
    this.appendValueInput('NUM').setCheck(Number).appendTitle('index').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('ITEM').appendTitle('replacement').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Replaces the nth item in a list.');
  }
};

Blockly.Language.lists_remove_item = {
  // Remove Item in list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('remove list item').appendTitle('list');
    this.appendValueInput('INDEX').setCheck(Number).appendTitle('index').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Removes the item at the specified position from the list.');
  }
};

Blockly.Language.lists_insert_item = {
  // Insert Item in list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('insert list item').appendTitle('list');
    this.appendValueInput('INDEX').setCheck(Number).appendTitle('index').setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('ITEM').setCheck(Number).appendTitle('item').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Insert an item into a list at the specified position.');
  }
};

Blockly.Language.lists_length = {
  // Length of list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Number);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('length of list').appendTitle('list');
    Blockly.Language.setTooltip(this, 'Counts the number of items in a list.');
  }
};

Blockly.Language.lists_append_list = {
  // Append to list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.appendValueInput('LIST0').setCheck(Array).appendTitle('append to list').appendTitle('list1');
    this.appendValueInput('LIST1').setCheck(Array).appendTitle('list2').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Appends all the items in list2 onto the end of list1. After '
        + 'the append, list1 will include these additional elements, but list2 will be unchanged.');
  }
};

Blockly.Language.lists_add_items = {
  // Create a list with any number of elements of any type.
  category: Blockly.LANG_CATEGORY_LISTS,
  helpUrl: '',
  init: function() {
    this.setColour(210);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('add items to list').appendTitle(' list');
    this.appendValueInput('ITEM0').appendTitle('item').setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    Blockly.Language.setTooltip(this, 'Adds items to the end of a list.');
    this.setMutator(new Blockly.Mutator(['lists_add_items_item']));
    this.itemCount_ = 1;
    this.emptyInputName = null;
    this.repeatingInputName = 'ITEM';
  },
  mutationToDom: Blockly.mutationToDom,
  domToMutation: Blockly.domToMutation,
  decompose: function(workspace){
    return Blockly.decompose(workspace,'lists_add_items_item',this);
  },
  compose: Blockly.compose,
  saveConnections: Blockly.saveConnections,
  addEmptyInput: function(){},
  addInput: function(inputNum){
    var input = this.appendValueInput(this.repeatingInputName + inputNum);
    input.appendTitle('item').setAlign(Blockly.ALIGN_RIGHT);
    return input;
  },
  updateContainerBlock: function(containerBlock) {
    containerBlock.inputList[0].titleRow[0].setText(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD);
    containerBlock.setTooltip(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP_1);
  }

};

Blockly.Language.lists_add_items_item = {
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

Blockly.Language.lists_is_in = {
  // Is in list?.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Boolean);
    this.appendValueInput('ITEM').appendTitle('is in list?').appendTitle('thing');
    this.appendValueInput('LIST').setCheck(Array).appendTitle('list').setAlign(Blockly.ALIGN_RIGHT);
    Blockly.Language.setTooltip(this, 'Retuns true if the the thing is an item in the list, and '
        + 'false if not.');
  }
};

Blockly.Language.lists_position_in = {
  // Postion of item in list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Number);
    
    this.appendValueInput('ITEM').appendTitle('position in list').appendTitle('thing');
    this.appendValueInput('LIST').setCheck(Array).appendTitle('list').setAlign(Blockly.ALIGN_RIGHT);
    Blockly.Language.setTooltip(this, 'Find the position of the thing in the list. If it\'s not in '
        + 'the list, return 0.');
  }
};

Blockly.Language.lists_pick_random_item = {
  // Length of list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, null);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('pick a random item').appendTitle('list');
    Blockly.Language.setTooltip(this, 'Pick an item at random from the list.');
  }
};

Blockly.Language.lists_is_empty = {
  // Is the list empty?.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Boolean);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('is list empty?').appendTitle('list');
    Blockly.Language.setTooltip(this, 'Tests if a list is empty \(has no items in it\)');
  }
};

Blockly.Language.lists_copy = {
  // Make a copy of list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('copy list').appendTitle('list');
    Blockly.Language.setTooltip(this, 'Makes a copy of a list, including copying all sublists');
  }
};

Blockly.Language.lists_is_list = {
  // Is a list?
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Boolean);
    this.appendValueInput('ITEM').appendTitle('is a list?').appendTitle('thing');
    Blockly.Language.setTooltip(this, 'Tests if something is a list.');
  }
};

Blockly.Language.lists_to_csv_row = {
  // Make a csv row from list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('list to csv row').appendTitle('list');
    Blockly.Language.setTooltip(this, 'Interprets the list as a row of a table and returns a CSV '
        + '\(comma-separated value\) text representing the row. Each item in the row list is '
        + 'considered to be a field, and is quoted with double-quotes in the resulting CSV text. '
        + 'Items are separated by commas. The returned row text does not have a line separator at '
        + 'the end.');
  }
};

Blockly.Language.lists_to_csv_table = {
  // Make a csv table from list.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendValueInput('LIST').setCheck(Array).appendTitle('list to csv table').appendTitle('list');
    Blockly.Language.setTooltip(this, 'Interprets the list as a table in row-major format and '
        + 'returns a CSV \(comma-separated value\) text representing the table. Each item in the '
        + 'list should itself be a list representing a row of the CSV table. Each item in the row '
        + 'list is considered to be a field, and is quoted with double-quotes in the resulting CSV '
        + 'text. In the returned text, items in rows are separated by commas and rows are '
        + 'separated by CRLF \(\\r\\n\).');
  }
};

Blockly.Language.lists_from_csv_row = {
  // Make list from csv row.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('list from csv row').appendTitle('text');
    Blockly.Language.setTooltip(this, 'Parses a text as a CSV \(comma-separated value\) formatted '
        + 'row to produce a list of fields. It is an error for the row text to contain unescaped '
        + 'newlines inside fields \(effectively, multiple lines\). It is okay for the row text to '
        + 'end in a single newline or CRLF.');
  }
};

Blockly.Language.lists_from_csv_table = {
  // Make list from csv table.
  category : Blockly.LANG_CATEGORY_LISTS,
  helpUrl : '',
  init : function() {
    this.setColour(210);
    this.setOutput(true, Array);
    this.appendValueInput('TEXT').setCheck(String).appendTitle('list from csv table').appendTitle('text');
    Blockly.Language.setTooltip(this, 'Parses a text as a CSV \(comma-separated value\) formatted '
        + 'table to produce a list of rows, each of which is a list of fields. Rows can be '
        + 'separated by newlines \(\\n\) or CRLF \(\\r\\n\).');
  }
};
