// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Dictionaries blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author data1013@mit.edu (Danny Tang)
 */

'use strict';

goog.provide('AI.Yail.dictionaries');

AI.Yail['dictionaries_create_with'] = function() {
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-dictionary" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  var itemsAdded = 0;
  for (var i=0; i<this.itemCount_; i++) {
    var argument = AI.Yail.valueToCode(this, 'ADD' + i, AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
    if (argument != null) {
      code += argument + AI.Yail.YAIL_SPACER;
      itemsAdded++;
    }
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  for (i=0; i<itemsAdded; i++) {
    code += "pair" + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a dictionary" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['pair'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'KEY', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var argument1 = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-dictionary-pair" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "make a pair" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_set_pair'] = function() {
  // Set pairs in a dictionary
  var key = AI.Yail.valueToCode(this, 'KEY', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var dict = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var value = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-set-pair" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + key + AI.Yail.YAIL_SPACER + dict + AI.Yail.YAIL_SPACER;
  code = code + value + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key dictionary any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "set value for key in dictionary to value" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['dictionaries_delete_pair'] = function() {
  // Set pairs in a dictionary
  var argument0 = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var argument1 = AI.Yail.valueToCode(this, 'KEY', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;    //TODO: define empty dict code
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-delete-pair" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary key" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "delete dictionary pair" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['dictionaries_lookup'] = function() {
  // Lookup in pairs in list of lists (key, value).T
  var argument0 = AI.Yail.valueToCode(this, 'KEY', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;    //TODO: define empty dict code
  var argument2 = AI.Yail.valueToCode(this, 'NOTFOUND', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-lookup" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key dictionary any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "dictionary lookup" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_recursive_lookup'] = function() {
  // Lookup in pairs in list of lists (key, value).T
  var argument0 = AI.Yail.valueToCode(this, 'KEYS', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;    //TODO: define empty dict code
  var argument2 = AI.Yail.valueToCode(this, 'NOTFOUND', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_NULL;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-recursive-lookup" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list dictionary any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "dictionary recursive lookup" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_recursive_set'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'KEYS', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_LIST;
  var argument1 = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var argument2 = AI.Yail.valueToCode(this, 'VALUE', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-recursive-set" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code += argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + argument2 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list dictionary any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "dictionary recursive set" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['dictionaries_getters'] = function() {
  // Basic arithmetic operators.
  var mode = this.getFieldValue('OP');
  var tuple = AI.Yail.dictionaries_getters.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = AI.Yail.valueToCode(this, 'DICT', order) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "dictionary"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.dictionaries_getters.OPERATORS = {
  KEYS: ['yail-dictionary-get-keys', "get a dictionary's keys", AI.Yail.ORDER_NONE],
  VALUES: ['yail-dictionary-get-values', "get a dictionary's values", AI.Yail.ORDER_NONE]
};

AI.Yail['dictionaries_get_values'] = function() {
  return AI.Yail.dictionaries_getters.call(this);
};

AI.Yail['dictionaries_is_key_in'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'KEY', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-is-key-in" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key dictionary" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "is key in dict?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_length'] = function() {
  var argument = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-length" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "get a dictionary's length" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_alist_to_dict'] = function() {
  var argument = AI.Yail.valueToCode(this, 'PAIRS', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-alist-to-dict" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "convert an alist to a dictionary" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_dict_to_alist'] = function() {
  var argument = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-dict-to-alist" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "convert a dictionary to an alist" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_copy'] = function() {
  var argument = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-copy" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "get a shallow copy of a dict" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['dictionaries_combine_dicts'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'DICT1', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var argument1 = AI.Yail.valueToCode(this, 'DICT2', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-combine-dicts" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER + argument1 + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary dictionary" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "combine 2 dictionaries" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

AI.Yail['dictionaries_walk_tree'] = function() {
  var path = AI.Yail.valueToCode(this, 'PATH', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_YAIL_LIST;
  var dict = AI.Yail.valueToCode(this, 'DICT', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + 'yail-dictionary-walk' + AI.Yail.YAIL_SPACER +
      AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER +
      path + AI.Yail.YAIL_SPACER + dict + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION +
      AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION +
      'list any' + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER +
      AI.Yail.YAIL_DOUBLE_QUOTE + "list by walking key path in dictionary" +
      AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['dictionaries_walk_all'] = function() {
  return [AI.Yail.YAIL_CONSTANT_ALL, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['dictionaries_is_dict'] = function() {
  var argument = AI.Yail.valueToCode(this, 'THING', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_EMPTY_DICT;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "check if something is a dictionary" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};
