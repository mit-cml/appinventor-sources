// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Instrumentation extensions to Blockly XML parsing
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.Xml');

goog.require('Blockly.Xml');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Instrument');  // lyn's instrumentation code

if (Blockly.Instrument.isOn) {

Blockly.Xml.domToWorkspace = (function(func) {
  if (func.isInstrumented) {
    return func;
  } else {
    var f = function() {
      var args = Array.prototype.slice.call(arguments);
      Blockly.Instrument.timer (
        function() {
          func.apply(this, args);
        },
        function (result, timeDiff) {
          Blockly.Instrument.stats.domToWorkspaceCalls++;
          Blockly.Instrument.stats.domToWorkspaceTime = timeDiff;
        });
    };
    f.isInstrumented = true;
    return f;
  }
})(Blockly.Xml.domToWorkspace);

Blockly.Xml.domToBlock = (function(func) {
  if (func.isInstrumented) {
    return func;
  } else {
    var f = function() {
      var args = Array.prototype.slice.call(arguments);
      var target = this;
      return Blockly.Instrument.timer (
        function() {
          return func.apply(target, args);
        },
        function (result, timeDiff) {
          Blockly.Instrument.stats.domToBlockCalls++;
          Blockly.Instrument.stats.domToBlockTime += timeDiff;
          return result;
        });
    };
    f.isInstrumented = true;
    return f;
  }
})(Blockly.Xml.domToBlock);

}
