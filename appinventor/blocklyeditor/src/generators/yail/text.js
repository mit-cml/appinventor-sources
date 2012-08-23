// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Generating Yail for categories of blocks.
 * 
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 */

Blockly.Yail = Blockly.Generator.get('Yail');

if (!Blockly.Yail.yail_primitive_params) {
 Blockly.Yail.yail_primitive_params = {}
}

SPACE =  Blockly.Yail.YAIL_SPACER;
OPEN =  Blockly.Yail.YAIL_OPEN_COMBINATION;
CLOSE = Blockly.Yail.YAIL_CLOSE;

//Text Blocks

Blockly.Yail.text = function() {
  // Text value.
  var code = Blockly.Yail.quote_(this.getTitleText('TEXT'));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

/*
Blockly.Yail.generic_join = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.join = ["string-append", ["string", "string"], "join"];

Blockly.Yail.generic_length = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.length = ["string-length", ["string"], "length"];

Blockly.Yail.generic_isTextEmpty = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.isTextEmpty = ["string-empty?", ["string"], "is text empty?"];

// Note: problem with ? mark in string 
Blockly.Yail.generic_textEquals = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.lessThen = ["string<?", ["string", "string"], "text>"];

// Note: problem with ? mark in string 
Blockly.Yail.generic_textGreaterThen = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.greaterThen = ["string>?", ["string", "string"], "text>"];

Blockly.Yail.generic_trim = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.trim = ["string-trim", ["string"], "trim"];

Blockly.Yail.generic_upCase = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.upCase = ["string-to-upper-case", ["string"], "upcase"];

Blockly.Yail.generic_downCase = Blockly.Yail.yail_primitive;
Blockly.Yail.yail_primitive_params.downCase = ["string-to-lower-case", ["string"], "downcase"];
*/