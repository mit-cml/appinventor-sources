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

/**
 * Decode an XML DOM and create blocks on the workspace.
 * @param {!Element} xml XML DOM.
 * @param {!Blockly.Workspace} workspace The workspace.
 */
Blockly.Xml.domToWorkspaceHeadless = function(xml, workspace) {
  Blockly.Events.disable();
  try {
    if (xml instanceof Blockly.Workspace) {
      var swap = xml;
      xml = workspace;
      workspace = swap;
      console.warn('Deprecated call to Blockly.Xml.domToWorkspace, ' +
                   'swap the arguments.');
    }
    var width;  // Not used in LTR.
    workspace.rendered = false;
    if (workspace.RTL) {
      width = workspace.getWidth();
    }
    Blockly.Field.startCache();
    // Safari 7.1.3 is known to provide node lists with extra references to
    // children beyond the lists' length.  Trust the length, do not use the
    // looping pattern of checking the index for an object.
    var childCount = xml.childNodes.length;
    var existingGroup = Blockly.Events.getGroup();
    if (!existingGroup) {
      Blockly.Events.setGroup(true);
    }
    for (var i = 0; i < childCount; i++) {
      var xmlChild = xml.childNodes[i];
      var name = xmlChild.nodeName.toLowerCase();
      if (name == 'block' ||
        (name == 'shadow' && !Blockly.Events.recordUndo)) {
        // Allow top-level shadow blocks if recordUndo is disabled since
        // that means an undo is in progress.  Such a block is expected
        // to be moved to a nested destination in the next operation.
        var block = Blockly.Xml.domToBlockHeadless_(xmlChild, workspace);
        block.x = parseInt(xmlChild.getAttribute('x'), 10);
        block.y = parseInt(xmlChild.getAttribute('y'), 10);
      } else if (name == 'shadow') {
        goog.asserts.fail('Shadow block cannot be a top-level block.');
      }
    }
    var block, blocks = workspace.getAllBlocks();
    for (i = 0; block = blocks[i]; i++) {
      if (block.eventparam) {
        block.setFieldValue(workspace.getComponentDatabase().getInternationalizedParameterName(block.eventparam), 'VAR');
      }
    }
    if (!existingGroup) {
      Blockly.Events.setGroup(false);
    }
    Blockly.Field.stopCache();
  } finally {
    Blockly.Events.enable();
  }

  workspace.updateVariableList(false);
};

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
