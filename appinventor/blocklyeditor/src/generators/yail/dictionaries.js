// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Dictionaries blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author data1013@mit.edu (Danny Tang)
 */

'use strict';

goog.provide('Blockly.Yail.dictionaries');

Blockly.Yail['dictionaries_create_with'] = function() {
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-yail-dictionary" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  var itemsAdded = 0;
  for (var i=0; i<this.itemCount_; i++) {
    var argument = Blockly.Yail.valueToCode(this, 'ADD' + i, Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
    if (argument != null) {
      code += argument + Blockly.Yail.YAIL_SPACER;
      itemsAdded++;
    }
  }
  code += Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  for (i=0; i<itemsAdded; i++) {
    code += "pair" + Blockly.Yail.YAIL_SPACER;
  }
  code += Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "make a dictionary" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['pair'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'KEY', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var argument1 = Blockly.Yail.valueToCode(this, 'VALUE', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-dictionary-pair" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "make a pair" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_set_pair'] = function() {
  // Set pairs in a dictionary
  var key = Blockly.Yail.valueToCode(this, 'KEY', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var dict = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var value = Blockly.Yail.valueToCode(this, 'VALUE', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-set-pair" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + key + Blockly.Yail.YAIL_SPACER + dict + Blockly.Yail.YAIL_SPACER;
  code = code + value + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key dictionary any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "set value for key in dictionary to value" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail['dictionaries_delete_pair'] = function() {
  // Set pairs in a dictionary
  var argument0 = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var argument1 = Blockly.Yail.valueToCode(this, 'KEY', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;    //TODO: define empty dict code
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-delete-pair" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary key" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "delete dictionary pair" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail['dictionaries_lookup'] = function() {
  // Lookup in pairs in list of lists (key, value).T
  var argument0 = Blockly.Yail.valueToCode(this, 'KEY', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var argument1 = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;    //TODO: define empty dict code
  var argument2 = Blockly.Yail.valueToCode(this, 'NOTFOUND', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-lookup" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + argument2 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key any any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "dictionary lookup" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_recursive_lookup'] = function() {
  // Lookup in pairs in list of lists (key, value).T
  var argument0 = Blockly.Yail.valueToCode(this, 'KEYS', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var argument1 = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;    //TODO: define empty dict code
  var argument2 = Blockly.Yail.valueToCode(this, 'NOTFOUND', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_NULL;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-recursive-lookup" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + argument2 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list dictionary any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "dictionary recursive lookup" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_recursive_set'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'KEYS', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_LIST;
  var argument1 = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var argument2 = Blockly.Yail.valueToCode(this, 'VALUE', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-recursive-set" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code += argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + argument2 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list dictionary any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "dictionary recursive set" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail['dictionaries_getters'] = function() {
  // Basic arithmetic operators.
  var mode = this.getFieldValue('OP');
  var tuple = Blockly.Yail.dictionaries_getters.OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = Blockly.Yail.valueToCode(this, 'DICT', order) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "dictionary"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_DOUBLE_QUOTE + operator2
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail.dictionaries_getters.OPERATORS = {
  KEYS: ['yail-dictionary-get-keys', "get a dictionary's keys", Blockly.Yail.ORDER_NONE],
  VALUES: ['yail-dictionary-get-values', "get a dictionary's values", Blockly.Yail.ORDER_NONE]
};

Blockly.Yail['dictionaries_get_values'] = function() {
  return Blockly.Yail.dictionaries_getters.call(this);
};

Blockly.Yail['dictionaries_is_key_in'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'KEY', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var argument1 = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-is-key-in" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "key dictionary" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "is key in dict?" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_length'] = function() {
  var argument = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-length" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "get a dictionary's length" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_alist_to_dict'] = function() {
  var argument = Blockly.Yail.valueToCode(this, 'PAIRS', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-alist-to-dict" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "list" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "convert an alist to a dictionary" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_dict_to_alist'] = function() {
  var argument = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-dict-to-alist" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "convert a dictionary to an alist" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_copy'] = function() {
  var argument = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-copy" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "get a shallow copy of a dict" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['dictionaries_combine_dicts'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'DICT1', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var argument1 = Blockly.Yail.valueToCode(this, 'DICT2', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary-combine-dicts" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument0 + Blockly.Yail.YAIL_SPACER + argument1 + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "dictionary dictionary" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "combine 2 dictionaries" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail['dictionaries_walk_tree'] = function() {
  var path = Blockly.Yail.valueToCode(this, 'PATH', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_YAIL_LIST;
  var dict = Blockly.Yail.valueToCode(this, 'DICT', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + 'yail-dictionary-walk' + Blockly.Yail.YAIL_SPACER +
      Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER +
      path + Blockly.Yail.YAIL_SPACER + dict + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION +
      Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION +
      'list any' + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER +
      Blockly.Yail.YAIL_DOUBLE_QUOTE + "list by walking key path in dictionary" +
      Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['dictionaries_walk_all'] = function() {
  return [Blockly.Yail.YAIL_CONSTANT_ALL, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['dictionaries_is_dict'] = function() {
  var argument = Blockly.Yail.valueToCode(this, 'THING', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_EMPTY_DICT;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "yail-dictionary?" + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER;
  code = code + argument + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + Blockly.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any" + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code = code + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_DOUBLE_QUOTE + "check if something is a dictionary" + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};
