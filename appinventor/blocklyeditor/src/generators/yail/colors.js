// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Color blocks yail generators for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Yail.color');

AI.Yail.color = function() {
  // Convert hex value to numeric value
  var code = -1 * (window.Math.pow(16,6) - window.parseInt("0x" + this.getFieldValue('COLOR').substr(1)));
  return [code, AI.Yail.ORDER_ATOMIC];
};

AI.Yail['color_black'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_blue'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_cyan'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_dark_gray'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_gray'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_green'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_light_gray'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_magenta'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_pink'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_red'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_white'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_orange'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_yellow'] = function() {
  return AI.Yail.color.call(this);
};

AI.Yail['color_make_color'] = function() {
  var blackList = "(call-yail-primitive make-yail-list (*list-for-runtime* 0 0 0)  '( any any any)  \"make a list\")";
  var argument0 = AI.Yail.valueToCode(this, 'COLORLIST', AI.Yail.ORDER_NONE) || blackList;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "make-color" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "list"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "make-color"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};

AI.Yail['color_split_color'] = function() {
  var argument0 = AI.Yail.valueToCode(this, 'COLOR', AI.Yail.ORDER_NONE) || -1;
  var code = AI.Yail.YAIL_CALL_YAIL_PRIMITIVE + "split-color" + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_OPEN_COMBINATION
      + AI.Yail.YAIL_LIST_CONSTRUCTOR + AI.Yail.YAIL_SPACER
      + argument0 + AI.Yail.YAIL_CLOSE_COMBINATION;
  code += AI.Yail.YAIL_SPACER + AI.Yail.YAIL_QUOTE
      + AI.Yail.YAIL_OPEN_COMBINATION + "number"
      + AI.Yail.YAIL_CLOSE_COMBINATION + AI.Yail.YAIL_SPACER;
  code += AI.Yail.YAIL_DOUBLE_QUOTE + "split-color"
      + AI.Yail.YAIL_DOUBLE_QUOTE + AI.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, AI.Yail.ORDER_ATOMIC ];
};
