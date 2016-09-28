// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Instrumentation extensions to Blockly SVG
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI2.Blockly.Warning');

goog.require('Blockly.Warning');

Blockly.Warning.prototype.drawIcon_ = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedFunc = function(group) {
      func.call(this, group);
      // customize the warning icon style
      this.iconGroup_.setAttribute('class', 'blocklyWarningIconGroup');
    };
    wrappedFunc.isWrapped = true;
    return wrappedFunc;
  }
})(Blockly.Warning.prototype.drawIcon_);
