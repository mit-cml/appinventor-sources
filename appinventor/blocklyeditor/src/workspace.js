// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Instrumentation extensions to Blockly Workspace
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.Workspace');

goog.require('Blockly.Workspace');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Instrument'); // lyn's instrumentation code

Blockly.Workspace.prototype.arranged_type_ = null;
Blockly.Workspace.prototype.arranged_latest_position_ = null;  // used to default to (previous is used for menus)
Blockly.Workspace.prototype.arranged_position_ = null;

Blockly.Workspace.prototype.resetArrangements = function() {
  // reset the variables used for menus, but keep the latest position, so the current horizontal or
  // vertical state can be kept
  this.arranged_type_ = null;
  this.arranged_position_ = null;
};

/**
 * The workspace's warning indicator.
 * @type {Blockly.WarningIndicator}
 */
Blockly.Workspace.prototype.warningIndicator_ = null;

Blockly.Workspace.prototype.getWarningIndicator = function() {
  return this.warningIndicator_;
};

Blockly.Workspace.prototype.setWarningIndicator = function(indicator) {
  this.warningIndicator_ = indicator;
};

// Additional functionality when adding a top block
Blockly.Workspace.prototype.addTopBlock = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedAddTopBlock = function(block) {
      if (block.workspace == Blockly.getMainWorkspace()) //Do not reset arrangements for the flyout
        Blockly.getMainWorkspace().resetArrangements();
      func.call(this, block);
      if (this.warningIndicator) {
        this.warningIndicator.updateWarningAndErrorCount();
      }
    };
    wrappedAddTopBlock.isWrapped = true;
    return wrappedAddTopBlock;
  }
})(Blockly.Workspace.prototype.addTopBlock);

// Additional functionality when removing a top block
Blockly.Workspace.prototype.removeTopBlock = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedRemoveTopBlock = function(block) {
      if (block.workspace == Blockly.getMainWorkspace())
        Blockly.getMainWorkspace().resetArrangements();
      func.call(this, block);
    };
    wrappedRemoveTopBlock.isWrapped = true;
    return wrappedRemoveTopBlock;
  }
})(Blockly.Workspace.prototype.removeTopBlock);

Blockly.Workspace.prototype.getTopBlocks = (function(func) {
  if (func.isInstrumented) {
    return func;
  } else {
    var instrumentedFunc = function(ordered) {
      var start = new Date().getTime(); //*** lyn instrumentation
      try {
        return func.call(this, ordered);
      } finally {
        var stop = new Date().getTime(); //*** lyn instrumentation
        var timeDiff = stop - start; //*** lyn instrumentation
        Blockly.Instrument.stats.getTopBlocksCalls++;
        Blockly.Instrument.stats.getTopBlocksTime += timeDiff;
      }
    };
    instrumentedFunc.isInstrumented = true;
    return instrumentedFunc;
  }
})(Blockly.Workspace.prototype.getTopBlocks);

// Override Blockly's getAllBlocks with optimized version from lyn
Blockly.Workspace.prototype.getAllBlocks = function() {
  var start = new Date().getTime(); //*** lyn instrumentation
  var blocks = this.getTopBlocks(false);
  Blockly.Instrument.stats.getAllBlocksAllocationCalls++;
  if (Blockly.Instrument.useLynGetAllBlocksFix) {
    // Lyn's version of getAllBlocks that avoids quadratic times for large numbers of blocks
    // by mutating existing blocks array rather than creating new ones
    for (var x = 0; x < blocks.length; x++) {
      var children = blocks[x].getChildren();
      blocks.push.apply(blocks, children);
      Blockly.Instrument.stats.getAllBlocksAllocationSpace += children.length;
    }
  } else {
    // Neil's version that has quadratic time for large number of blocks
    // because each call to concat creates *new* array, and so this code does a *lot* of heap
    // allocation when there are a large number of blocks.
    for (var x = 0; x < blocks.length; x++) {
      blocks.push.apply(blocks, blocks[x].getChildren());
      Blockly.Instrument.stats.getAllBlocksAllocationCalls++;
      Blockly.Instrument.stats.getAllBlocksAllocationSpace += blocks.length;
    }
  }
  var stop = new Date().getTime(); //*** lyn instrumentation
  var timeDiff = stop - start; //*** lyn instrumentation
  Blockly.Instrument.stats.getAllBlocksCalls++;
  Blockly.Instrument.stats.getAllBlocksTime += timeDiff;
  return blocks;
};

Blockly.Workspace.prototype.dispose = (function(func) {
    if (func.isWrapped) {
      return func;
    } else {
      var wrappedFunc = function() {
        func.call(this);
        if (this.warningIndicator_) {
          this.warningIndicator_.dispose();
          this.warningIndicator_ = null;
        }
      };
      wrappedFunc.isWrapped = true;
      return wrappedFunc;
    }
  })(Blockly.Workspace.prototype.dispose);
