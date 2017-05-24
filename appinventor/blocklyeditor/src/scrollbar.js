// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Extends Blockly.ScrollbarPair with Lyn's instrumentation code.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.ScrollbarPair');

goog.require('Blockly.ScrollbarPair');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Instrument');

Blockly.ScrollbarPair.prototype.resize = (function(func) {
  if (func.isInstrumented) {
    return func;
  } else {
    var instrumentedFunc = function() {
      var start = new Date().getTime();
      try {
        return func.call(this);
      } finally {
        var stop = new Date().getTime();
        var timeDiff = stop - start;
        Blockly.Instrument.stats.scrollBarResizeCalls++; //***lyn
        Blockly.Instrument.stats.scrollBarResizeTime += timeDiff; //***lyn
      }
    };
    instrumentedFunc.isInstrumented = true;
    return instrumentedFunc;
  }
})(Blockly.ScrollbarPair.prototype.resize);
