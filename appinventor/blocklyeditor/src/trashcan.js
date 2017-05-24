// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Extends Blockly's trashcan with Lyn's instrumentation code
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.Trashcan');

goog.require('Blockly.Trashcan');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Instrument'); // lyn's instrumentation code

Blockly.Trashcan.prototype.position = (function(func) {
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
        Blockly.Instrument.stats.trashCanPositionCalls++; //***lyn
        Blockly.Instrument.stats.trashCanPositionTime += timeDiff; //***lyn
      }
    };
    instrumentedFunc.isInstrumented = true;
    return instrumentedFunc;
  }
})(Blockly.Trashcan.prototype.position);
