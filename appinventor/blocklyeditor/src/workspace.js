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
      if (block.workspace == Blockly.common.getMainWorkspace()) //Do not reset arrangements for the flyout
        Blockly.common.getMainWorkspace().resetArrangements();
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
      if (block.workspace == Blockly.common.getMainWorkspace())
        Blockly.common.getMainWorkspace().resetArrangements();
      func.call(this, block);
    };
    wrappedRemoveTopBlock.isWrapped = true;
    return wrappedRemoveTopBlock;
  }
})(Blockly.Workspace.prototype.removeTopBlock);

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
