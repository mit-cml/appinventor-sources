// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Add additional functions to Blockly namespace
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.TypeBlock');
goog.require('AI.Blockly.WorkspaceSvg');

/**
 * Constant indicating a horizontal arrangement.
 *
 * @const {number}
 */
AI.Blockly.BLKS_HORIZONTAL = 0;

/**
 * Constant indicating a vertical arrangement.
 *
 * @const {number}
 */
AI.Blockly.BLKS_VERTICAL = 1;

/**
 * Constant indicating arrangement by cateogry.
 *
 * @const {number}
 */
AI.Blockly.BLKS_CATEGORY = 2;

/**
 * ENUM for an indented value input.  Similar to next_statement but with value
 * input shape.
 * @const {number}
 */
Blockly.INDENTED_VALUE = 6;

/**
 * Path to Blockly's directory.  Can be relative, absolute, or remote.
 * Used for loading additional resources.
 * @const {string}
 */
Blockly.pathToBlockly = './';

/**
 * Extend Blockly's hideChaff method with AI2-specific behaviors.
 */
Blockly.hideChaff = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var f = function() {
      var argCopy = Array.prototype.slice.call(arguments);
      func.apply(this, argCopy);
      // [lyn, 10/06/13] for handling parameter & procedure flydowns
      Blockly.WorkspaceSvg.prototype.hideChaff.call(Blockly.common.getMainWorkspace(), argCopy);
    };
    f.isWrapped = true;
    return f;
  }
})(Blockly.hideChaff);

/**
 * Present a [HTML] deletion dialog to the user. The callback will be called with <code>true</code>
 * if the user confirms deletion, otherwise it will be called with <code>false</code>.
 * @param {!function(confirmed: boolean)} callback Function to call with the user's selection.
 */
Blockly.confirmDeletion = function(callback) {
  var DELETION_THRESHOLD = 3;

  var descendantCount = Blockly.common.getMainWorkspace().getAllBlocks().length;
  if (Blockly.common.getSelected() != null) {
    descendantCount = Blockly.common.getSelected().getDescendants().length;
    if (Blockly.common.getSelected().nextConnection && Blockly.common.getSelected().nextConnection.targetConnection) {
      descendantCount -= Blockly.common.getSelected().nextConnection.targetBlock().getDescendants().length;
    }
  }


  if (descendantCount >= DELETION_THRESHOLD) {
    if (Blockly.Util && Blockly.Util.Dialog) {
      var msg = Blockly.Msg.WARNING_DELETE_X_BLOCKS.replace('%1', String(descendantCount));
      var cancelButton = top.BlocklyPanel_getOdeMessage('cancelButton');
      var deleteButton = top.BlocklyPanel_getOdeMessage('deleteButton');
      var dialog = new Blockly.Util.Dialog(Blockly.Msg.CONFIRM_DELETE, msg, deleteButton, true, cancelButton, 0, function(button) {
        dialog.hide();
        if (button == deleteButton) {
          Blockly.common.getMainWorkspace().getAudioManager().play('delete');
          callback(true);
        } else {
          callback(false);
        }
      });
    } else {
      var response = confirm(Blockly.Msg.WARNING_DELETE_X_BLOCKS.replace('%1', String(descendantCount)));
      if (response) {
        Blockly.common.getMainWorkspace().getAudioManager().play('delete');
      }
      callback(response);
    }
  }
  else {
    Blockly.common.getMainWorkspace().getAudioManager().play('delete');
    callback(true);
  }
};
