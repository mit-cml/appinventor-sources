// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Color blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Yail.color');

Blockly.Yail.color = function() {
  // Convert hex value to numeric value
  var code = -1 * (window.Math.pow(16,6) - window.parseInt("0x" + this.getFieldValue('COLOR').substr(1)));
  return [code, Blockly.Yail.ORDER_ATOMIC];
};

Blockly.Yail['color_black'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_blue'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_cyan'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_dark_gray'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_gray'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_green'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_light_gray'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_magenta'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_pink'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_red'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_white'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_orange'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_yellow'] = function() {
  return Blockly.Yail.color.call(this);
};

Blockly.Yail['color_make_color'] = function() {
  var blackList = "(call-yail-primitive make-yail-list (*list-for-runtime* 0 0 0)  '( any any any)  \"make a list\")";
  var argument0 = Blockly.Yail.valueToCode(this, 'COLORLIST', Blockly.Yail.ORDER_NONE) || blackList;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-color" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "list"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_DOUBLE_QUOTE + "make-color"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};

Blockly.Yail['color_split_color'] = function() {
  var argument0 = Blockly.Yail.valueToCode(this, 'COLOR', Blockly.Yail.ORDER_NONE) || -1;
  var code = Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE + "split-color" + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_OPEN_COMBINATION
      + Blockly.Yail.YAIL_LIST_CONSTRUCTOR + Blockly.Yail.YAIL_SPACER
      + argument0 + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  code += Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE
      + Blockly.Yail.YAIL_OPEN_COMBINATION + "number"
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER;
  code += Blockly.Yail.YAIL_DOUBLE_QUOTE + "split-color"
      + Blockly.Yail.YAIL_DOUBLE_QUOTE + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};
