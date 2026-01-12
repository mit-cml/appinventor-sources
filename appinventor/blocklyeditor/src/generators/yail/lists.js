// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Lists blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.lists');

AI.Yail.emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
AI.Yail.emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
AI.Yail.emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
AI.Yail.emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
AI.Yail.emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;


AI.Yail['lists_create_with'] = function() {

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  var itemsAdded = 0;
  for(var i=0;i<this.itemCount_;i++) {
    var argument = AI.Yail.valueToCode(this, 'ADD' + i, AI.Yail.ORDER_NONE) || null;
    if(argument != null){
      code += argument + AI.Yail.YAIL_SPACER;
      itemsAdded++;
    }
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  for(var i=0;i<itemsAdded;i++) {
    code += "any" + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];

};

AI.Yail['lists_select_item'] = function() {
  // Select from list an item.

  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = AI.Yail.valueToCode(this, 'NUM', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-get-item" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "select list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_replace_item'] = function() {
  // Replace Item in list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = AI.Yail.valueToCode(this, 'NUM', AI.Yail.ORDER_NONE) || 1;
  var argument2 = AI.Yail.valueToCode(this, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-set-item!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1
  code = code + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "replace list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['lists_remove_item'] = function() {
  // Remove Item in list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = AI.Yail.valueToCode(this, 'INDEX', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-remove-item!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "remove list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['lists_insert_item'] = function() {
  // Insert Item in list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = AI.Yail.valueToCode(this, 'INDEX', AI.Yail.ORDER_NONE) || 1;
  var argument2 = AI.Yail.valueToCode(this, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-insert-item!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1;
  code = code + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "insert list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['lists_length'] = function() {
  // Length of list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-length" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "length of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_append_list'] = function() {
  // Append to list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST0', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = AI.Yail.valueToCode(this, 'LIST1', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-append!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER;
  code = code + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "append to list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['lists_add_items'] = function() {
  // Add items to list.
  // TODO: (Andrew) Make this handle multiple items.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = AI.Yail.valueToCode(this, 'ITEM', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-add-to-list!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER;

  for(var i=0;i<this.itemCount_;i++) {
    var argument = AI.Yail.valueToCode(this, 'ITEM' + i, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
    code += argument + AI.Yail.YAIL_SPACER;
  }

  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list ";
  for(var i=0;i<this.itemCount_;i++) {
    code += "any" + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "add items to list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['lists_is_in'] = function() {
  // Is in list?.
  var argument0 = AI.Yail.valueToCode(this, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-member?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is in list?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_position_in'] = function() {
  // Postion of item in list.
  // This block is now called 'index in list"
  // It used to be called "position in list"
  var argument0 = AI.Yail.valueToCode(this, 'ITEM', AI.Yail.ORDER_NONE) || 1;
  var argument1 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-index" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "index in list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_pick_random_item'] = function() {
  // Pick random item
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-pick-random" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "pick random item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_is_empty'] = function() {
  // Is the list empty?.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-empty?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is list empty?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_copy'] = function() {
  // Make a copy of list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-copy" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "copy list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_is_list'] = function() {
  // Create an empty list.
  // TODO:(Andrew) test whether thing is var or text or number etc...
  var argument0 = AI.Yail.valueToCode(this, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is a list?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_reverse'] = function() {
  // Reverse the list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-reverse" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "reverse list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_to_csv_row'] = function() {
  // Make a csv row from list.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-to-csv-row" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list to csv row" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_to_csv_table'] = function() {
  // Make a csv table from list
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-to-csv-table" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list to csv table" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_from_csv_row'] = function() {
  // Make list from csv row.
  var argument0 = AI.Yail.valueToCode(this, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-from-csv-row" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list from csv row" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_from_csv_table'] = function() {
  // Make list from csv table.
  var argument0 = AI.Yail.valueToCode(this, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-from-csv-table" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list from csv table" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_lookup_in_pairs'] = function() {
  // Lookup in pairs in list of lists (key, value).
  var argument0 = AI.Yail.valueToCode(this, 'KEY', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument2 = AI.Yail.valueToCode(this, 'NOTFOUND', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-alist-lookup" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any list any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "lookup in pairs" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_join_with_separator'] = function() {
  // Joins list items into a string separated by specified separator
  var argument0 = AI.Yail.valueToCode(this, 'SEPARATOR', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-join-with-separator" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument1 + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + "list" + AI.Yail.YAIL_SPACER + "text" + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "join with separator" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_map'] = function() {
  // Map the list with given expression
  var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
  emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR');
  var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
  var bodyCode = AI.Yail.valueToCode(this, 'TO', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_MAP + loopIndexName + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
      + listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_filter'] = function() {
  // Filter the list
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR');
	var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var bodyCode = AI.Yail.valueToCode(this, 'TEST', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_FILTER + loopIndexName + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
    	+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_reduce'] = function() {
    // Reduce the list
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR1');
	var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR2');
	var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var initAnswerCode = AI.Yail.valueToCode(this, 'INITANSWER', AI.Yail.ORDER_NONE);
	var bodyCode = AI.Yail.valueToCode(this, 'COMBINE', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_REDUCE + initAnswerCode + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER
				+ loopIndexName1 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
				+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_sort'] = function() {
  // Sort the list in ascending order
	var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
	var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-sort";
	code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	code = code + argument0;
	code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
	code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
	code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "sort " + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_sort_comparator'] = function() {
  // Sort the list with specified comparator
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR1');
	var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR2');
	var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var bodyCode = AI.Yail.valueToCode(this, 'COMPARE', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_SORT_COMPARATOR_NONDEST + loopIndexName1 + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
    	+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_sort_key'] = function() {
  // Sorting the list using the key, a proxy value user creates with expressions.
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR');
	var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var bodyCode = AI.Yail.valueToCode(this, 'KEY', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_SORT_KEY_NONDEST + loopIndexName + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
    	+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_minimum_value'] = function() {
  // Minimum number in the list
  var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
  emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "minimum value of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR1');
  var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR2');
  var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
  var bodyCode = AI.Yail.valueToCode(this, 'COMPARE', AI.Yail.ORDER_NONE) ||
    ('(call-yail-primitive < (*list-for-runtime* (lexical-value ' + loopIndexName1 + ') (lexical-value ' + loopIndexName2 + ')) \'(number number) "<")');
  var code = "(mincomparator-nondest " + loopIndexName1 + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
      + listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_maximum_value'] = function() {
  // Maximum number in the list
  var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
  emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "maximum value of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR1');
  var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + this.getFieldValue('VAR2');
  var listCode = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
  var bodyCode = AI.Yail.valueToCode(this, 'COMPARE', AI.Yail.ORDER_NONE) ||
    ('(call-yail-primitive < (*list-for-runtime* (lexical-value ' + loopIndexName1 + ') (lexical-value ' + loopIndexName2 + ')) \'(number number) "<")');
  var code = "(maxcomparator-nondest " + loopIndexName1 + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
      + listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_but_first'] = function() {
  // Return the list without the first element
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-but-first";
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "butFirst of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_but_last'] = function() {
  // Return the list without the last element
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-but-last";
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "butLast of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['lists_slice'] = function() {
  // Slices list at the two given index.
  var argument0 = AI.Yail.valueToCode(this, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = AI.Yail.valueToCode(this, 'INDEX1', AI.Yail.ORDER_NONE) || 1;
  var argument2 = AI.Yail.valueToCode(this, 'INDEX2', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-slice";
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1;
  code = code + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "slice of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};
