// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012-2017 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Text blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.text');

AI.Yail.forBlock['text'] = function(block, generator) {
  // Text value.
  var code = AI.Yail.quote_(block.getFieldValue('TEXT'));
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['text_join'] = function(block, generator) {
  // Create a string made up of elements of any type..
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-append"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;

  for(var i=0;i<block.itemCount_;i++) {
    var argument = generator.valueToCode(block, 'ADD' + i, AI.Yail.ORDER_NONE) || "\"\"";
    code += argument + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION;
  for(var i=0;i<block.itemCount_;i++) {
    code += "text" + AI.Yail.YAIL_SPACER;
  }
  code += AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "join"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_length'] = function(block, generator) {
  // // String length
  var argument = generator.valueToCode(block, 'VALUE', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-length"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "length"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_isEmpty'] = function(block, generator) {
  // Is the string null?
  var argument = generator.valueToCode(block, 'VALUE', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-empty?"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is text empty?"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_compare'] = function(block, generator) {
  // Basic compare operators
  var mode = block.getFieldValue('OP');
  var prim = AI.Yail.forBlock['text_compare'].OPERATORS[mode];
  var operator1 = prim[0];
  var operator2 = prim[1];
  var order = prim[2];
  var argument0 = generator.valueToCode(block, 'TEXT1', order) || "\"\"";
  var argument1 = generator.valueToCode(block, 'TEXT2', order) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  if (mode == 'NEQ') {
    code = '(not ' + code + ')';
  }
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_compare'].OPERATORS = {
  LT: ['string<?', 'text<', AI.Yail.ORDER_NONE],
  GT: ['string>?', 'text>', AI.Yail.ORDER_NONE],
  EQUAL: ['string=?', 'text=', AI.Yail.ORDER_NONE],
  NEQ: ['string=?', 'not =', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['text_trim'] = function(block, generator) {
  // String trim
  var argument = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-trim"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "trim"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_changeCase'] = function(block, generator) {
  // String change case.
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['text_changeCase'].OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var argument = generator.valueToCode(block, 'TEXT', order) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_changeCase'].OPERATORS = {
  UPCASE: ['string-to-upper-case', 'upcase', AI.Yail.ORDER_NONE],
  DOWNCASE: ['string-to-lower-case', 'downcase', AI.Yail.ORDER_NONE]
};

AI.Yail.forBlock['text_starts_at'] = function(block, generator) {
  // String starts at
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = generator.valueToCode(block, 'PIECE', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-starts-at"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "starts at"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_contains'] = function(block, generator) {
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = generator.valueToCode(block, 'PIECE', AI.Yail.ORDER_NONE) || "\"\"";
  var mode = AI.Yail.forBlock['text_contains'].OPERATORS[block.getMode()];

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + mode.operator
      + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text " + mode.type
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + mode.blockName
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, mode.order];
};

AI.Yail.forBlock['text_contains'].OPERATORS = {
  'CONTAINS': {
    operator: 'string-contains',
    blockName: 'string contains',
    order: AI.Yail.ORDER_ATOMIC,
    type: 'text'
  },
  'CONTAINS_ANY': {
    operator: 'string-contains-any',
    blockName: 'string contains any',
    order: AI.Yail.ORDER_ATOMIC,
    type: 'list'
  },
  'CONTAINS_ALL': {
    operator: 'string-contains-all',
    blockName: 'string contains all',
    order: AI.Yail.ORDER_ATOMIC,
    type: 'list'
  }
};

AI.Yail.forBlock['text_split'] = function(block, generator) {
  // String split operations.
  // Note that the type of arg2 might be text or list, depending on the dropdown selection
  var mode = block.getFieldValue('OP');
  var tuple = AI.Yail.forBlock['text_split'].OPERATORS[mode];
  var operator1 = tuple[0];
  var operator2 = tuple[1];
  var order = tuple[2];
  var arg2Type = tuple[3];
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = generator.valueToCode(block, 'AT', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + operator1
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text" +  AI.Yail.YAIL_SPACER + arg2Type
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + operator2
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, order ];
};

AI.Yail.forBlock['text_split'].OPERATORS = {
  SPLITATFIRST : [ 'string-split-at-first', 'split at first',
      AI.Yail.ORDER_ATOMIC, 'text' ],
  SPLITATFIRSTOFANY : [ 'string-split-at-first-of-any',
      'split at first of any', AI.Yail.ORDER_ATOMIC, 'list' ],
  SPLIT : [ 'string-split', 'split', AI.Yail.ORDER_ATOMIC, 'text' ],
  SPLITATANY : [ 'string-split-at-any', 'split at any', AI.Yail.ORDER_ATOMIC, 'list' ]
};

AI.Yail.forBlock['text_split_at_spaces'] = function(block, generator) {
  // split at spaces
  var argument = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-split-at-spaces"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "split at spaces"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_segment'] = function(block, generator) {
  // Create string segment
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = generator.valueToCode(block, 'START', AI.Yail.ORDER_NONE) || 1;
  var argument2 = generator.valueToCode(block, 'LENGTH', AI.Yail.ORDER_NONE) || 1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-substring"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_SPACER + argument2
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text number number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "segment"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_replace_all'] = function(block, generator) {
  // String replace with segment
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = generator.valueToCode(block, 'SEGMENT', AI.Yail.ORDER_NONE) || "\"\"";
  var argument2 = generator.valueToCode(block, 'REPLACEMENT', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-replace-all"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_SPACER + argument1
      + AI.Yail.YAIL_SPACER + argument2
      + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text text text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "replace all"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['obfuscated_text'] = function(block, generator) {
  // Deobfuscate the TEXT input argument
  var setupObfuscation = function(input, confounder) {
    // The algorithm below is also implemented in scheme in runtime.scm
    // If you change it here, you have to change it there!
    // Note: This algorithm is like xor, if applied to its output
    // it regenerates it input.
    var acc = [];
    // First make sure the confounder is long enough...
    while (confounder.length < input.length) {
      confounder += confounder;
    }
    for (var i = 0; i < input.length; i++) {
      var c = (input.charCodeAt(i) ^ confounder.charCodeAt(i)) & 0xFF;
      var b = (c ^ input.length - i) & 0xFF;
      var b2 = ((c >> 8) ^ i) & 0xFF;
      acc.push(String.fromCharCode((b2 << 8 | b) & 0xFF));
    }
    return acc.join('');
  }
  var input = block.getFieldValue('TEXT');
  var argument = AI.Yail.quote_(setupObfuscation(input, block.confounder));
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "text-deobfuscate"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_SPACER
      + AI.Yail.quote_(block.confounder) + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "deobfuscate text"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_is_string'] = function(block, generator) {
  // Check if the argument is a string
  var argument0 = generator.valueToCode(block, 'ITEM', AI.Yail.ORDER_NONE) || AI.Yail.YAIL_FALSE;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string?" + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE + AI.Yail.YAIL_OPEN_COMBINATION;
  code = code + "any" + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "is a string?" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail.forBlock['text_reverse'] = function (block, generator) {
  // String reverse.
  var argument = generator.valueToCode(block, 'VALUE', AI.Yail.ORDER_NONE) || "\"\"";
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "string-reverse"
      + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "text"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_DOUBLE_QUOTE + "reverse"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['text_replace_mappings'] = function (block, generator) {
  // Replace all occurrences in mappings with their corresponding replacement
  var argument0 = generator.valueToCode(block, 'TEXT', AI.Yail.ORDER_NONE) || "\"\"";
  var argument1 = generator.valueToCode(block, 'MAPPINGS', AI.Yail.ORDER_NONE) || "\"\"";
  var mode = block.getFieldValue('OP');
  var mode_function = AI.Yail.forBlock['text_replace_mappings'].OPERATORS[mode];

  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + mode_function + AI.Yail.YAIL_SPACER;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER;
  code = code + argument0 + AI.Yail.YAIL_SPACER;
  code = code + argument1 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE;
  code = code + AI.Yail.YAIL_OPEN_COMBINATION + "text" + AI.Yail.YAIL_SPACER;
  code = code + "dictionary" + AI.Yail.YAIL_CLOSE_COMBINATION;
  code = code + AI.Yail.YAIL_SPACER + AI.Yail.YAIL_DOUBLE_QUOTE + "replace with mappings" + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;

  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail.forBlock['text_replace_mappings'].OPERATORS = {
  LONGEST_STRING_FIRST: "string-replace-mappings-longest-string",
  DICTIONARY_ORDER: "string-replace-mappings-dictionary"
  //EARLIEST_OCCURRENCE: "string-replace-mappings-earliest-occurrence"
};
