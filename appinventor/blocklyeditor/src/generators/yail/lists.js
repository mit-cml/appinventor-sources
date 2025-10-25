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


AI.Yail.forBlock['lists_create_with'] = function(block, generator) {

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  var itemsAdded = 0;
  for(var i=0;i<block.itemCount_;i++) {
    var argument = generator.valueToCode(block, 'ADD' + i, AI.Yail.ORDER_NONE) || null;
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

AI.Yail.forBlock['lists_select_item'] = function(block, generator) {
  // Select from list an item.

  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = generator.valueToCode(block, 'NUM', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-get-item" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "select list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_replace_item'] = function(block, generator) {
  // Replace Item in list.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = generator.valueToCode(block, 'NUM', AI.Yail.ORDER_NONE) || 1;
  var argument2 = generator.valueToCode(block, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-set-item!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1
  code = code + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "replace list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail.forBlock['lists_remove_item'] = function(block, generator) {
  // Remove Item in list.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = generator.valueToCode(block, 'INDEX', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-remove-item!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "remove list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail.forBlock['lists_insert_item'] = function(block, generator) {
  // Insert Item in list.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = generator.valueToCode(block, 'INDEX', AI.Yail.ORDER_NONE) || 1;
  var argument2 = generator.valueToCode(block, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-insert-item!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1;
  code = code + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "insert list item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail.forBlock['lists_length'] = function(block, generator) {
  // Length of list.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-length" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "length of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_append_list'] = function(block, generator) {
  // Append to list.
  var argument0 = generator.valueToCode(block, 'LIST0', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = generator.valueToCode(block, 'LIST1', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
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

AI.Yail.forBlock['lists_add_items'] = function(block, generator) {
  // Add items to list.
  // TODO: (Andrew) Make this handle multiple items.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-add-to-list!" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER;

  for(var i=0;i<block.itemCount_;i++) {
    var argument = generator.valueToCode(block, 'ITEM' + i, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
    code += argument + AI.Yail.YAIL_SPACER;
  }

  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list ";
  for(var i=0;i<block.itemCount_;i++) {
    code += "any" + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "add items to list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail.forBlock['lists_is_in'] = function(block, generator) {
  // Is in list?.
  var argument0 = generator.valueToCode(block, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-member?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is in list?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_position_in'] = function(block, generator) {
  // Postion of item in list.
  // This block is now called 'index in list"
  // It used to be called "position in list"
  var argument0 = generator.valueToCode(block, 'ITEM', AI.Yail.ORDER_NONE) || 1;
  var argument1 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-index" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "index in list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_pick_random_item'] = function(block, generator) {
  // Pick random item
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-pick-random" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "pick random item" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_is_empty'] = function(block, generator) {
  // Is the list empty?.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-empty?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is list empty?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_copy'] = function(block, generator) {
  // Make a copy of list.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-copy" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "copy list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_is_list'] = function(block, generator) {
  // Create an empty list.
  // TODO:(Andrew) test whether thing is var or text or number etc...
  var argument0 = generator.valueToCode(block, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is a list?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_reverse'] = function(block, generator) {
  // Reverse the list.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-reverse" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "reverse list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_to_csv_row'] = function(block, generator) {
  // Make a csv row from list.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-to-csv-row" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list to csv row" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_to_csv_table'] = function(block, generator) {
  // Make a csv table from list
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-to-csv-table" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list to csv table" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_from_csv_row'] = function(block, generator) {
  // Make list from csv row.
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-from-csv-row" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list from csv row" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_from_csv_table'] = function(block, generator) {
  // Make list from csv table.
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-from-csv-table" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "text" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "list from csv table" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_lookup_in_pairs'] = function(block, generator) {
  // Lookup in pairs in list of lists (key, value).
  var argument0 = generator.valueToCode(block, 'KEY', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument2 = generator.valueToCode(block, 'NOTFOUND', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-alist-lookup" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any list any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "lookup in pairs" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_join_with_separator'] = function(block, generator) {
  // Joins list items into a string separated by specified separator
  var argument0 = generator.valueToCode(block, 'SEPARATOR', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-join-with-separator" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument1 + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + "list" + AI.Yail.YAIL_SPACER + "text" + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "join with separator" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_map'] = function(block, generator) {
  // Map the list with given expression
  var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
  emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR');
  var listCode = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
  var bodyCode = generator.valueToCode(block, 'TO', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_MAP + loopIndexName + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
      + listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_filter'] = function(block, generator) {
  // Filter the list
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR');
	var listCode = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var bodyCode = generator.valueToCode(block, 'TEST', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_FILTER + loopIndexName + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
    	+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_reduce'] = function(block, generator) {
    // Reduce the list
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR1');
	var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR2');
	var listCode = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var initAnswerCode = generator.valueToCode(block, 'INITANSWER', AI.Yail.ORDER_NONE);
	var bodyCode = generator.valueToCode(block, 'COMBINE', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_REDUCE + initAnswerCode + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER
				+ loopIndexName1 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
				+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_sort'] = function(block, generator) {
  // Sort the list in ascending order
	var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
	var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-sort";
	code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	code = code + argument0;
	code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
	code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
	code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "sort " + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_sort_comparator'] = function(block, generator) {
  // Sort the list with specified comparator
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR1');
	var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR2');
	var listCode = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var bodyCode = generator.valueToCode(block, 'COMPARE', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_SORT_COMPARATOR_NONDEST + loopIndexName1 + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
    	+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_sort_key'] = function(block, generator) {
  // Sorting the list using the key, a proxy value user creates with expressions.
	var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
	emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
	emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
	var loopIndexName = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR');
	var listCode = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
	var bodyCode = generator.valueToCode(block, 'KEY', AI.Yail.ORDER_NONE) ||  AI.Yail.YAIL_FALSE;
	var code = AI.Yail.YAIL_SORT_KEY_NONDEST + loopIndexName + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
    	+ listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_minimum_value'] = function(block, generator) {
  // Minimum number in the list
  var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
  emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "minimum value of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR1');
  var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR2');
  var listCode = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
  var bodyCode = generator.valueToCode(block, 'COMPARE', AI.Yail.ORDER_NONE) ||
    ('(call-yail-primitive < (*list-for-runtime* (lexical-value ' + loopIndexName1 + ') (lexical-value ' + loopIndexName2 + ')) \'(number number) "<")');
  var code = "(mincomparator-nondest " + loopIndexName1 + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
      + listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_maximum_value'] = function(block, generator) {
  // Maximum number in the list
  var emptyListCode = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-list" + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  emptyListCode += AI.Yail.YAIL_CLOSE_COMBINATION;
  emptyListCode += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "maximum value of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  var loopIndexName1 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR1');
  var loopIndexName2 = AI.Yail.YAIL_LOCAL_VAR_TAG + block.getFieldValue('VAR2');
  var listCode = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || emptyListCode;
  var bodyCode = generator.valueToCode(block, 'COMPARE', AI.Yail.ORDER_NONE) ||
    ('(call-yail-primitive < (*list-for-runtime* (lexical-value ' + loopIndexName1 + ') (lexical-value ' + loopIndexName2 + ')) \'(number number) "<")');
  var code = "(maxcomparator-nondest " + loopIndexName1 + AI.Yail.YAIL_SPACER + loopIndexName2 + AI.Yail.YAIL_SPACER + bodyCode + AI.Yail.YAIL_SPACER
      + listCode + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_but_first'] = function(block, generator) {
  // Return the list without the first element
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-but-first";
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "butFirst of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_but_last'] = function(block, generator) {
  // Return the list without the last element
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-but-last";
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "butLast of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['lists_slice'] = function(block, generator) {
  // Slices list at the two given index.
  var argument0 = generator.valueToCode(block, 'LIST', AI.Yail.ORDER_NONE) || AI.Yail.emptyListCode;
  var argument1 = generator.valueToCode(block, 'INDEX1', AI.Yail.ORDER_NONE) || 1;
  var argument2 = generator.valueToCode(block, 'INDEX2', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-list-slice";
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1;
  code = code + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list number number" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "slice of list" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};
