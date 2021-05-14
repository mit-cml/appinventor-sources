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

goog.require('Blockly.BlockSvg');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Instrument');
goog.require('AI.Blockly.Error');

Blockly.BlockSvg.DISTANCE_45_INSIDE = (1 - Math.SQRT1_2) *
  (Blockly.BlockSvg.CORNER_RADIUS - 1) + 1;
Blockly.BlockSvg.DISTANCE_45_OUTSIDE = (1 - Math.SQRT1_2) *
  (Blockly.BlockSvg.CORNER_RADIUS + 1) - 1;

/**
 * Block's error icon (if any).
 * @type {Blockly.Error}
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
 * Returns a list of mutator, comment, and warning icons.
 * @return {!Array} List of icons.
 */
Blockly.BlockSvg.prototype.getIcons = (function(func) {
  return function() {
    var icons = func.call(this);
    if (this.error) {
      icons.push(this.error);
    }
    return icons;
  };
})(Blockly.BlockSvg.prototype.getIcons);

/**
 * Obtains starting coordinates so the block can return to spot after copy.
 *
 * @param {!Event} e Mouse down event.
 */
Blockly.BlockSvg.prototype.onMouseDown_ = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedFunc = function(e){
      var workspace = this.getTopWorkspace();
      if (workspace && workspace.getParentSvg() && workspace.getParentSvg().parentNode &&
          typeof workspace.getParentSvg().parentNode.focus === 'function') {  // Firefox 49 doesn't have focus function on SVG elements
        workspace.getParentSvg().parentNode.focus();
      }
      if (Blockly.FieldFlydown.openFieldFlydown_) {
        var flydown = Blockly.getMainWorkspace().getFlydown();
        if (flydown) {
          if (goog.dom.contains(flydown.svgGroup_, this.svgGroup_)) {
            //prevent hiding the flyout if a child block is the target
            flydown.shouldHide = false;
          }
        } else {
          console.warn('openFieldFlydown_ was set but flydown_ was undefined!');
        }
      }
      var retval = func.call(this, e);
      var xy = goog.style.getPageOffset(this.svgGroup_);
      this.startX = xy.x;
      this.startY = xy.y;
      if (workspace && workspace.getParentSvg() && workspace.getParentSvg().parentNode &&
        typeof workspace.getParentSvg().parentNode.focus === 'function') {  // Firefox 49 doesn't have focus function on SVG elements
        workspace.getParentSvg().parentNode.focus();
      }
      return retval;
    };
    wrappedFunc.isWrapped = true;
    return wrappedFunc;
  }
})(Blockly.BlockSvg.prototype.onMouseDown_);

// Adds backpack detection on mouse move
Blockly.BlockSvg.prototype.onMouseMove_ = (function(func) {
  if (func.isWrapped) {
    return func;
  } else {
    var wrappedFunc = function(e) {
      func.call(this, e);
      if (Blockly.dragMode_ == Blockly.DRAG_FREE) {
        if (Blockly.getMainWorkspace().hasBackpack()) {
          Blockly.getMainWorkspace().getBackpack().onMouseMove(e);
        }
      }
    };
    wrappedFunc.isWrapped = true;
    return wrappedFunc;
  }
})(Blockly.BlockSvg.prototype.onMouseMove_);

/**
 * Handle a mouse-up anywhere in the SVG pane.  Is only registered when a
 * block is clicked.  We can't use mouseUp on the block since a fast-moving
 * cursor can briefly escape the block before it catches up.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.BlockSvg.prototype.onMouseUp_ = (function(func) {
  if (func.isInstrumented) {
    return func;
  } else {
    var instrumentedFunc = function(e) {
      var start = new Date().getTime();
      Blockly.Instrument.initializeStats('onMouseUp');
      Blockly.getMainWorkspace().resetArrangements();
      try {
        var result = func.call(this, e);
        if (Blockly.getMainWorkspace().hasBackpack() &&
            Blockly.getMainWorkspace().getBackpack().isOpen) {
          var backpack = Blockly.getMainWorkspace().getBackpack();
          goog.Timer.callOnce(backpack.close, 100, backpack);
          backpack.addToBackpack(Blockly.selected, true);
          backpack.onMouseUp(e, Blockly.selected.dragStartXY_);
        }
        return result;
      } finally {
        if (! Blockly.Instrument.avoidRenderWorkspaceInMouseUp) {
          Blockly.mainWorkspace.render();
        }
        var stop = new Date().getTime();
        Blockly.Instrument.stats.totalTime = stop - start;
        Blockly.Instrument.displayStats('onMouseUp');
      }
    };
    instrumentedFunc.isInstrumented = true;
    return instrumentedFunc;
  }
})(Blockly.BlockSvg.prototype.onMouseUp_);

/**
 * Set this block's warning text.
 * @param {?string} text The text, or null to delete.
 */
Blockly.BlockSvg.prototype.setErrorIconText = function(text) {
  if (!Blockly.Error) {
    throw 'Warnings not supported.';
  }
  var changedState = false;
  if (goog.isString(text)) {
    if (!this.error) {
      this.error = new Blockly.Error(this);
      changedState = true;
    }
    this.error.setText(/** @type {string} */ (text));
  } else {
    if (this.error) {
      this.error.dispose();
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
 * There is no need to render blocks in Blocky.SaveFile.load.
 * We only need to render them when a Screen is loaded in the Blockly editor.
 * This flag is used to turn off rendering for first case and turn it on for the second.
 * @type {boolean}
 */
Blockly.BlockSvg.isRenderingOn = true;
/*
// Instruments render
Blockly.BlockSvg.prototype.render = (function(func) {
  if (func.isInstrumented) {
    return func;
  } else {
    var f = function(opt_bubble) {
      if (Blockly.BlockSvg.isRenderingOn) {
        func.call(this, opt_bubble);
        // TODO(ewpatton): Realtime no longer in Blockly, consider removing
        //if (Blockly.Realtime.isEnabled() && !Blockly.Realtime.withinSync) {
        //  Blockly.Realtime.blockChanged(this);
        //}
        Blockly.Instrument.stats.renderCalls++;
        // [lyn, 04/08/14] Because render is recursive, doesn't make sense to track its time here.
      }
    };
    f.isInstrumented = true;
    return f;
  }
})(Blockly.BlockSvg.prototype.render);
*/

if (Blockly.Instrument.useRenderDown) {

Blockly.BlockSvg.prototype.render = function(opt_bubble) {
  if (opt_bubble !== false) {
    var root = this.getRootBlock();
    if (root != this) {  // If 'this' is not the top.
      root.render(false);
      return;
    }
  }
  if (this.isRendering || !Blockly.BlockSvg.isRenderingOn) {
    return;
  }
  goog.asserts.assertObject(this.svgGroup_,
    ' Uninitialized block cannot be rendered. Call block.initSvg()');

  this.isRendering = true;
  try {  // Make sure we set isRendering back to false if something goes wrong.
    this.renderDown_();
    if (!this.getParent()) {  // This is a top level block.
      // Fire an event so the scrollbars can resize.
      this.workspace.resizeContents();
    }
  } finally {
    this.isRendering = false;
  }
};

/**
 * [lyn, 04/01/14] Render a tree of blocks from top down rather than bottom up.
 * This is in contrast to render(), which renders a block and all its antecedents.
 * @private Should only be called from render.
 */
Blockly.BlockSvg.prototype.renderDown_ = function() {
  // Recursively renderDown all my children (as long as I'm not collapsed)
  if (!this.isCollapsed() || !Blockly.Instrument
      .avoidRenderDownOnCollapsedSubblocks) {
    var childBlocks = this.getChildren();  // Includes next block.
    for (var i = 0, childBlock; (childBlock = childBlocks[i]); i++) {
      childBlock.render(false);
    }
  } else {  // Always render next block.
    var nextBlock = this.getNextBlock();
    if (nextBlock) {
      nextBlock.render(false);
    }
  }
  this.renderHere_();
  Blockly.Instrument.stats.renderDownCalls++; //***lyn
  // [lyn, 04/08/14] Because renderDown is recursive, doesn't make sense to track its time here.
};

/**
 * Render this block. Assumes descendants have already been rendered.
 * @private Should only be called from renderDown_.
 */
Blockly.BlockSvg.prototype.renderHere_ = function() {
  var start = new Date().getTime();
  Blockly.Field.startCache();
  this.rendered = true;

  if (this.isCollapsed()) {
    this.updateCollapsed_();
  }

  // Now render me (even if I am collapsed, since still need to show collapsed block)
  var cursorX = Blockly.BlockSvg.SEP_SPACE_X;
  if (this.RTL) {
    cursorX = -cursorX;
  }
  // Move the icons into position.
  var icons = this.getIcons();
  for (var x = 0; x < icons.length; x++) {
    cursorX = icons[x].renderIcon(cursorX);
  }
  cursorX += this.RTL ?
      Blockly.BlockSvg.SEP_SPACE_X : -Blockly.BlockSvg.SEP_SPACE_X;
  // If there are no icons, cursorX will be 0, otherwise it will be the
  // width that the first label needs to move over by.

  var inputRows = this.renderCompute_(cursorX);
  this.renderDraw_(cursorX, inputRows);
  this.renderMoveConnections_();

  // correct bad block status if we weren't rendered.
  if (this.isBad && !this.isBadBlock()) {
    this.addBadBlock();
  } else if (!this.isBad && this.isBadBlock()) {
    this.removeBadBlock();
  }

  Blockly.Field.stopCache();
  var stop = new Date().getTime();
  var timeDiff = stop-start;
  Blockly.Instrument.stats.renderHereCalls++;
  Blockly.Instrument.stats.renderHereTime += timeDiff;
};

/**
 * Makes sure that when the block is collapsed, it is rendered correctly for
 * that state.
 * @private
 */
Blockly.BlockSvg.prototype.updateCollapsed_ = function() {
  var collapsed = this.isCollapsed();
  var collapsedInputName = Blockly.BlockSvg.COLLAPSED_INPUT_NAME;
  var collapsedFieldName = Blockly.BlockSvg.COLLAPSED_FIELD_NAME;

  for (var i = 0, input; (input = this.inputList[i]); i++) {
    if (input.name == collapsedInputName) {
      continue;
    }
    input.setVisible(!collapsed);
  }

  if (!collapsed) {
    this.removeInput(collapsedInputName);
    return;
  }

  var icons = this.getIcons();
  for (var i = 0, icon; (icon = icons[i]); i++) {
    icon.setVisible(false);
  }

  var text = this.toString(Blockly.COLLAPSE_CHARS);
  var field = this.getField(collapsedFieldName);
  if (field) {
    field.setValue(text);
    return;
  }
  var input = this.getInput(collapsedInputName) ||
      this.appendDummyInput(collapsedInputName);
  input.appendField(new Blockly.FieldLabel(text), collapsedFieldName);
};

  /**
   * Set whether the block is collapsed or not.
   * @param {boolean} collapsed True if collapsed.
   */
  Blockly.BlockSvg.prototype.setCollapsed = function(collapsed) {
    if (this.collapsed_ == collapsed) {
      return;
    }
    Blockly.BlockSvg.superClass_.setCollapsed.call(this, collapsed);
    if (!collapsed) {
      this.updateCollapsed_();
    } else if (this.rendered) {
      this.render();
    }
  };
}

// Instruments the real setCollapsed function
Blockly.BlockSvg.prototype.setCollapsed = (function(func) {
  if (func.isInstrumented) {
    return func;
  } else {
    var instrumentedFunc = function(collapsed) {
      var start = new Date().getTime();
      try {
        return func.call(this, collapsed);
      } finally {
        var stop = new Date().getTime();
        var timeDiff = stop - start;
        if (! collapsed) {
          Blockly.Instrument.stats.expandCollapsedCalls++;
          Blockly.Instrument.stats.expandCollapsedTime += timeDiff;
        }
      }
    };
    instrumentedFunc.isInstrumented = true;
    return instrumentedFunc;
  }
})(Blockly.BlockSvg.prototype.setCollapsed);

/**
 * Mark this block as bad.  Highlight it visually in red.
 */
Blockly.BlockSvg.prototype.addBadBlock = function() {
  if (this.rendered) {
    Blockly.utils.addClass(/** @type {!Element} */ (this.svgGroup_),
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
    Blockly.utils.removeClass(/** @type {!Element} */ (this.svgGroup_),
                              'badBlock');
    // Move the selected block to the top of the stack.
    this.svgGroup_.parentNode.appendChild(this.svgGroup_);
  }
};

/**
 * Check to see if the block is marked as bad.
 */
Blockly.BlockSvg.prototype.isBadBlock = function() {
  return Blockly.utils.hasClass(/** @type {!Element} */ (this.svgGroup_),
    'badBlock');
};

/**
 * Mark this block as Bad.  Highlight it visually in Red.
 */
Blockly.BlockSvg.prototype.badBlock = function() {
  this.isBad = true;
  if (this.workspace == Blockly.getMainWorkspace()) {
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
  if (this.workspace == Blockly.getMainWorkspace()) {
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
  if (Blockly.dragMode_ == Blockly.DRAG_FREE) {
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
      this.error = new Blockly.Error(this);
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
    this.bumpNeighbours_();
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
 * Add the selection highlight to the block.
 */
Blockly.BlockSvg.prototype.addSelect = function() {
  Blockly.utils.addClass(this.svgGroup_, 'blocklySelected');
  var block_0 = this;
  do {
    var root = block_0.getSvgRoot();
    if (!root.parentNode) break;  // not yet attached to DOM
    root.parentNode.appendChild(root);
    block_0 = block_0.getParent();
  } while (block_0);
};

/**
 * Load the block's help page in a new window. This version overrides the implementation in Blockly
 * in order to include the locale query parameter that the documentation page will use to redirect
 * the user if a translation exists for their language.
 *
 * @private
 */
Blockly.BlockSvg.prototype.showHelp_ = function() {
  var url = goog.isFunction(this.helpUrl) ? this.helpUrl() : this.helpUrl;
  if (url) {
    var parts = url.split('#');
    var hereparts = top.location.href.match('[&?]locale=([a-zA-Z-]*)');
    if (hereparts && hereparts[1].toLowerCase() !== 'en') {
      parts[0] += '?locale=' + hereparts[1];
    }
    window.open(parts.join('#'));
  }
};
