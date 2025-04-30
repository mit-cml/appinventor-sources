// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Instrumentation extensions to Blockly SVG
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.BlockSvg');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Block');
goog.require('AI.Blockly.FieldFlydown');
goog.require('AI.ErrorIcon');

/**
 * Block's error icon (if any).
 * @type {AI.ErrorIcon}
 */
Blockly.BlockSvg.prototype.error = null;

/**
 * Flag to indicate a bad block.
 * @type {boolean}
 */
Blockly.BlockSvg.prototype.isBad = false;

/**
 * This is to prevent any recursive render calls. It is not elegant in the
 * least, but it is pretty future proof.
 * @type {boolean}
 */
Blockly.BlockSvg.prototype.isRendering = false;

/**
 * The language-neutral id given to the collapsed input.
 * @const {string}
 */
Blockly.BlockSvg.COLLAPSED_INPUT_NAME = '_TEMP_COLLAPSED_INPUT';

/**
 * The language-neutral id given to the collapsed field.
 * @const {string}
 */
Blockly.BlockSvg.COLLAPSED_FIELD_NAME = '_TEMP_COLLAPSED_FIELD';

/**
 * Set this block's warning text.
 * @param {?string} text The text, or null to delete.
 */
Blockly.BlockSvg.prototype.setErrorIconText = function(text) {
  if (!AI.ErrorIcon) {
    throw 'Warnings not supported.';
  }
  if (this.isDeadOrDying()) {
    return;  // do not process errors if the block is being destroyed
  }
  var changedState = false;
  if (typeof text === 'string') {
    if (!this.error) {
      this.error = new AI.ErrorIcon(this);
      this.addIcon(this.error);
      changedState = true;
    }
    this.error.setText(/** @type {string} */ (text));
  } else {
    if (this.error) {
      this.removeIcon(this.error.getType());
      this.error = null;
      changedState = true;
    }
  }
  if (this.rendered && changedState) {
    // Adding or removing a warning icon will cause the block to change shape so we need to re-render.
    this.workspace.requestRender(this);
  }
};

/**
 * [lyn, 04/01/14] Global flag to control whether rendering is done.
 * There is no need to render blocks in Blockly.SaveFile.load.
 * We only need to render them when a Screen is loaded in the Blockly editor.
 * This flag is used to turn off rendering for first case and turn it on for the second.
 * @type {boolean}
 */
Blockly.BlockSvg.isRenderingOn = true;

/**
 * Mark this block as bad.  Highlight it visually in red.
 */
Blockly.BlockSvg.prototype.addBadBlock = function() {
  if (this.rendered) {
    Blockly.utils.dom.addClass(/** @type {!Element} */ (this.svgGroup_),
                           'badBlock');
    // Move the selected block to the top of the stack.
    this.svgGroup_.parentNode.appendChild(this.svgGroup_);
  }
};

/**
 * Unmark this block as bad.
 */
Blockly.BlockSvg.prototype.removeBadBlock = function() {
  if (this.rendered) {
    Blockly.utils.dom.removeClass(/** @type {!Element} */ (this.svgGroup_),
                              'badBlock');
    // Move the selected block to the top of the stack.
    this.svgGroup_.parentNode.appendChild(this.svgGroup_);
  }
};

/**
 * Check to see if the block is marked as bad.
 */
Blockly.BlockSvg.prototype.isBadBlock = function() {
  return Blockly.utils.dom.hasClass(/** @type {!Element} */ (this.svgGroup_),
    'badBlock');
};

/**
 * Mark this block as Bad.  Highlight it visually in Red.
 */
Blockly.BlockSvg.prototype.badBlock = function() {
  this.isBad = true;
  if (this.workspace == Blockly.common.getMainWorkspace()) {
    // mark a block bad only if it is on the main workspace
    goog.asserts.assertObject(this.svgGroup_, 'Block is not rendered.');
    this.addBadBlock();
  }
};

/**
 * Unmark this block as Bad.
 */
Blockly.BlockSvg.prototype.notBadBlock = function() {
  this.isBad = false;
  if (this.workspace == Blockly.common.getMainWorkspace()) {
    // mark a block not bad only if it is on the main workspace
    goog.asserts.assertObject(this.svgGroup_, 'Block is not rendered.');
    this.removeBadBlock();
  }
};

/**
 * Extend Blockly.BlockSvg.prototype.dispose with AI2-specific functionality.
 */
Blockly.BlockSvg.prototype.dispose = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedFunc = function(healStack, animate) {
      var workspace = this.workspace;
      try {
        func.call(this, healStack, animate);
      } finally {
        // Remove any associated errors or warnings.
        if (workspace && workspace.getWarningHandler()) {  // Blocks in drawers do not have a workspace
          workspace.getWarningHandler().checkDisposedBlock(this);
        }
      }
    };
    wrappedFunc.isWrapped = true;
    return wrappedFunc;
  }
})(Blockly.BlockSvg.prototype.dispose);

/**
 * Set this block's error text.
 * @param {?string} text The text, or null to delete.
 * @param {string=} opt_id An optional ID for the warning text to be able to
 *     maintain multiple errors.
 */
Blockly.BlockSvg.prototype.setErrorText = function(text, opt_id) {
  if (!this.setErrorText.pid_) {
    // Create a database of warning PIDs.
    // Only runs once per block (and only those with warnings).
    this.setErrorText.pid_ = Object.create(null);
  }
  var id = opt_id || '';
  if (!id) {
    // Kill all previous pending processes, this edit supercedes them all.
    for (var n in this.setErrorText.pid_) {
      clearTimeout(this.setErrorText.pid_[n]);
      delete this.setErrorText.pid_[n];
    }
  } else if (this.setErrorText.pid_[id]) {
    // Only queue up the latest change.  Kill any earlier pending process.
    clearTimeout(this.setErrorText.pid_[id]);
    delete this.setErrorText.pid_[id];
  }
  if (this.workspace && this.workspace.isDragging()) {
    // Don't change the error text during a drag.
    // Wait until the drag finishes.
    var thisBlock = this;
    this.setErrorText.pid_[id] = setTimeout(function() {
      if (thisBlock.workspace) {  // Check block wasn't deleted.
        delete thisBlock.setErrorText.pid_[id];
        thisBlock.setErrorText(text, id);
      }
    }, 100);
    return;
  }
  if (this.isInFlyout) {
    text = null;
  }

  // Bubble up to add a error on top-most collapsed block.
  var parent = this.getSurroundParent();
  var collapsedParent = null;
  while (parent) {
    if (parent.isCollapsed()) {
      collapsedParent = parent;
    }
    parent = parent.getSurroundParent();
  }
  if (collapsedParent) {
    collapsedParent.setErrorText(text, 'collapsed ' + this.id + ' ' + id);
  }

  var changedState = false;
  if (goog.isString(text)) {
    if (!this.error) {
      this.error = new AI.ErrorIcon(this);
      changedState = true;
    }
    this.error.setText(/** @type {string} */ (text), id);
  } else {
    // Dispose all errors if no id is given.
    if (this.error && !id) {
      this.error.dispose();
      changedState = true;
    } else if (this.error) {
      var oldText = this.error.getText();
      this.error.setText('', id);
      var newText = this.error.getText();
      if (!newText) {
        this.error.dispose();
      }
      changedState = oldText == newText;
    }
  }
  if (changedState && this.rendered) {
    this.render();
    // Adding or removing a error icon will cause the block to change shape.
    this.bumpNeighbours();
  }
};

/**
 * Get the top-most workspace. Typically this is the current workspace except for flyout/flydowns.
 * @returns {!Blockly.WorkspaceSvg}
 */
Blockly.BlockSvg.prototype.getTopWorkspace = function() {
  var workspace = this.workspace;
  while (workspace.targetWorkspace) workspace = workspace.targetWorkspace;
  return workspace;
};

/**
 * Load the block's help page in a new window. This version overrides the implementation in Blockly
 * in order to include the locale query parameter that the documentation page will use to redirect
 * the user if a translation exists for their language.
 *
 * @private
 */
Blockly.BlockSvg.prototype.showHelp = function() {
  const url =
      typeof this.helpUrl === 'function' ? this.helpUrl() : this.helpUrl;
  if (url) {
    var parts = url.split('#');
    var hereparts = top.location.href.match('[&?]locale=([a-zA-Z-]*)');
    if (hereparts && hereparts[1].toLowerCase() !== 'en') {
      parts[0] += '?locale=' + hereparts[1];
    }
    window.open(parts.join('#'));
  }
};
