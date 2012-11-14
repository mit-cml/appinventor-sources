/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Object representing a mutator dialog.  A mutator allows the
 * user to change the shape of a block using a nested blocks editor.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

/**
 * Class for a mutator dialog.
 * @param {!Array.<string>} quarkNames List of names of sub-blocks for flyout.
 * @constructor
 */
Blockly.Mutator = function(quarkNames) {
  this.block_ = null;
  this.quarkNames_ = quarkNames;
};

/**
 * Height and width of the mutator icon.
 */
Blockly.Mutator.ICON_SIZE = 16;

/**
 * Bubble UI (if visible).
 * @type {Blockly.Bubble}
 * @private
 */
Blockly.Mutator.prototype.bubble_ = null;

/**
 * Absolute X coordinate of icon's center.
 * @private
 */
Blockly.Mutator.prototype.iconX_ = 0;

/**
 * Absolute Y coordinate of icon's centre.
 * @private
 */
Blockly.Mutator.prototype.iconY_ = 0;

/**
 * Width of workspace.
 * @private
 */
Blockly.Mutator.prototype.workspaceWidth_ = 0;

/**
 * Height of workspace.
 * @private
 */
Blockly.Mutator.prototype.workspaceHeight_ = 0;

/**
 * Create the icon on the block.
 */
Blockly.Mutator.prototype.createIcon = function() {
  /* Here's the markup that will be generated:
  <g class="blocklyIconGroup">
    <rect class="blocklyIconShield" width="16" height="16"/>
    <path class="blocklyMutatorMark" d="..."></path>
  </g>
  */
  var quantum = Blockly.Mutator.ICON_SIZE / 8;
  this.iconGroup_ = Blockly.createSvgElement('g', {}, null);
  if (this.block_.editable) {
    this.iconGroup_.setAttribute('class', 'blocklyIconGroup');
  }
  var iconShield = Blockly.createSvgElement('rect',
      {'class': 'blocklyIconShield',
       'width': 8 * quantum,
       'height': 8 * quantum,
       'rx': 2 * quantum,
       'ry': 2 * quantum}, this.iconGroup_);
  if (!Blockly.Mutator.crossPath_) {
    // Draw the cross once, and save it for future use.
    var path = [];
    path.push('M', (3.5 * quantum) + ',' + (3.5 * quantum));
    path.push('v', -2 * quantum, 'h', quantum);
    path.push('v', 2 * quantum, 'h', 2 * quantum);
    path.push('v', quantum, 'h', -2 * quantum);
    path.push('v', 2 * quantum, 'h', -quantum);
    path.push('v', -2 * quantum, 'h', -2 * quantum);
    path.push('v', -quantum, 'z');
    Blockly.Mutator.crossPath_ = path.join(' ');
  }
  this.iconMark_ = Blockly.createSvgElement('path',
      {'class': 'blocklyIconMark',
       'd': Blockly.Mutator.crossPath_}, this.iconGroup_);
  this.block_.getSvgRoot().appendChild(this.iconGroup_);
  if (this.block_.editable) {
    Blockly.bindEvent_(this.iconGroup_, 'mouseup', this, this.iconClick_);
  }
};

/**
 * Create the editor for the mutator's bubble.
 * @return {!Element} The top-level node of the editor.
 * @private
 */
Blockly.Mutator.prototype.createEditor_ = function() {
  /* Create the editor.  Here's the markup that will be generated:
  <svg>
    <rect class="blocklyMutatorBackground" />
    [Flyout]
    [Workspace]
  </svg>
  */
  this.svgDialog_ = Blockly.createSvgElement('svg',
      {'x': Blockly.Bubble.BORDER_WIDTH, 'y': Blockly.Bubble.BORDER_WIDTH},
      null);
  this.svgBackground_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyMutatorBackground',
       'height': '100%', 'width': '100%'}, this.svgDialog_);

  this.workspace_ = new Blockly.Workspace(true);
  this.flyout_ = new Blockly.Flyout();
  this.flyout_.autoClose = false;
  this.svgDialog_.appendChild(this.flyout_.createDom());
  this.svgDialog_.appendChild(this.workspace_.createDom());
  return this.svgDialog_;
};

/**
 * Callback function triggered when the bubble has resized.
 * Resize the workspace accordingly.
 * @private
 */
Blockly.Mutator.prototype.resizeBubble_ = function() {
  var doubleBorderWidth = 2 * Blockly.Bubble.BORDER_WIDTH;
  var workspaceSize = this.workspace_.getCanvas().getBBox();
  var flyoutMetrics = this.flyout_.getMetrics();
  var width;
  if (Blockly.RTL) {
    width = -workspaceSize.x;
  } else {
    width = workspaceSize.width + workspaceSize.x;
  }
  var height = Math.max(workspaceSize.height + doubleBorderWidth * 3,
                        flyoutMetrics.contentHeight + 20);
  width += doubleBorderWidth * 3;
  // Only resize if the size difference is significant.  Eliminates shuddering.
  if (Math.abs(this.workspaceWidth_ - width) > doubleBorderWidth ||
      Math.abs(this.workspaceHeight_ - height) > doubleBorderWidth) {
    // Record some layout information for getFlyoutMetrics_.
    this.workspaceWidth_ = width;
    this.workspaceHeight_ = height;
    // Resize the bubble.
    this.bubble_.setBubbleSize(width + doubleBorderWidth,
                               height + doubleBorderWidth);
    this.svgDialog_.setAttribute('width', this.workspaceWidth_);
    this.svgDialog_.setAttribute('height', this.workspaceHeight_);
  }

  if (Blockly.RTL) {
    // Scroll the workspace to always left-align.
    var translation = 'translate(' + this.workspaceWidth_ + ',0)';
    this.workspace_.getCanvas().setAttribute('transform', translation);
  }
};

/**
 * Is the mutator bubble visible?
 * @return {boolean} True if the bubble is visible.
 */
Blockly.Mutator.prototype.isVisible = function() {
  return !!this.bubble_;
};

/**
 * Show or hide the mutator bubble.
 * @param {boolean} visible True if the bubble should be visible.
 */
Blockly.Mutator.prototype.setVisible = function(visible) {
  if (visible == this.isVisible()) {
    // No change.
    return;
  }
  if (visible) {
    // Create the bubble.
    this.bubble_ = new Blockly.Bubble(this.block_.workspace,
        this.createEditor_(), this.block_.svg_.svgGroup_,
        this.iconX_, this.iconY_, null, null);
    var thisObj = this;
    this.flyout_.init(this.workspace_,
                      function() {return thisObj.getFlyoutMetrics_()}, false);
    this.flyout_.show(this.quarkNames_);

    this.rootBlock_ = this.block_.decompose(this.workspace_);
    var blocks = this.rootBlock_.getDescendants();
    for (var i = 0, child; child = blocks[i]; i++) {
      child.render();
    }
    // The root block should not be dragable or deletable.
    this.rootBlock_.editable = false;
    this.rootBlock_.deletable = false;
    var margin = this.flyout_.CORNER_RADIUS * 2;
    var x = this.flyout_.width_ + margin;
    if (Blockly.RTL) {
      x = -x;
    }
    this.rootBlock_.moveBy(x, margin);
    // Save the initial connections, then listen for further changes.
    if (this.block_.saveConnections) {
      this.block_.saveConnections(this.rootBlock_);
      this.sourceListener_ = Blockly.bindEvent_(
          this.block_.workspace.getCanvas(),
          'blocklyWorkspaceChange', this.block_,
          function() {thisObj.block_.saveConnections(thisObj.rootBlock_)});
    }
    this.resizeBubble_();
    // When the mutator's workspace changes, update the source block.
    Blockly.bindEvent_(this.workspace_.getCanvas(), 'blocklyWorkspaceChange',
        this.block_, function() {thisObj.workspaceChanged_();});
    this.updateColour();
  } else {
    // Dispose of the bubble.
    this.svgDialog_ = null;
    this.svgBackground_ = null;
    this.flyout_.dispose();
    this.flyout_ = null;
    this.workspace_.dispose();
    this.workspace_ = null;
    this.rootBlock_ = null;
    this.bubble_.dispose();
    this.bubble_ = null;
    this.workspaceWidth_ = 0;
    this.workspaceHeight_ = 0;
    if (this.sourceListener_) {
      Blockly.unbindEvent_(this.sourceListener_);
      this.sourceListener_ = null;
    }
  }
};

/**
 * Update the source block when the mutator's blocks are changed.
 * Delete any block that's out of bounds.
 * Fired whenever a change is made to the mutator's workspace.
 * @private
 */
Blockly.Mutator.prototype.workspaceChanged_ = function() {
  // Delete any block that's sitting on top of the flyout, or above the window.
  if (Blockly.Block.dragMode_ == 0) {
    var blocks = this.workspace_.getTopBlocks(false);
    for (var b = 0, block; block = blocks[b]; b++) {
      var xy = block.getRelativeToSurfaceXY();
      if (xy.y < 0 || (Blockly.RTL ?
          xy.x > -this.flyout_.width_ : xy.x < this.flyout_.width_)) {
        block.dispose(false, false);
      }
    }
  }

  // When the mutator's workspace changes, update the source block.
  if (this.rootBlock_.workspace == this.workspace_) {
    // Switch off rendering while the source block is rebuilt.
    var savedRendered = this.block_.rendered;
    this.block_.rendered = false;
    // Allow the source block to rebuild itself.
    this.block_.compose(this.rootBlock_);
    // Restore rendering and show the changes.
    this.block_.rendered = savedRendered;
    if (this.block_.rendered) {
      this.block_.render();
    }
    this.resizeBubble_();
    // The source block may have changed, notify its workspace.
    this.block_.workspace.fireChangeEvent();
  }
};

/**
 * Return an object with all the metrics required to size scrollbars for the
 * mutator flyout.  The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .absoluteTop: Top-edge of view.
 * .absoluteLeft: Left-edge of view.
 * @return {!Object} Contains size and position metrics of mutator dialog's
 *     workspace.
 * @private
 */
Blockly.Mutator.prototype.getFlyoutMetrics_ = function() {
  var left = 0;
  if (Blockly.RTL) {
    left += this.workspaceWidth_;
  }
  return {
    viewHeight: this.workspaceHeight_,
    absoluteTop: 0,
    absoluteLeft: left
  };
};

/**
 * Clicking on the icon toggles if the bubble is visible.
 * @param {!Event} e Mouse click event.
 * @private
 */
Blockly.Mutator.prototype.iconClick_ = function(e) {
  this.setVisible(!this.isVisible());
};

/**
 * Change the colour of a mutator to match its block.
 */
Blockly.Mutator.prototype.updateColour = function() {
  if (this.isVisible()) {
    var hexColour = Blockly.makeColour(this.block_.getColour());
    this.bubble_.setColour(hexColour);
  }
};

/**
 * Dispose of this mutator.
 */
Blockly.Mutator.prototype.dispose = function() {
  // Dispose of and unlink the icon.
  goog.dom.removeNode(this.iconGroup_);
  this.iconGroup_ = null;
  // Dispose of and unlink the bubble.
  this.setVisible(false);
  // Disconnect links between the block and the mutator.
  this.block_.mutator = null;
  this.block_ = null;
};

/**
 * Render the icon for this mutator.
 * @param {number} cursorX Horizontal offset at which to position the icon.
 * @return {number} Horizontal offset for next item to draw.
 */
Blockly.Mutator.prototype.renderIcon = function(cursorX) {
  if (this.block_.collapsed) {
    this.iconGroup_.setAttribute('display', 'none');
    return cursorX;
  }
  this.iconGroup_.setAttribute('display', 'block');

  var TOP_MARGIN = 5;
  if (Blockly.RTL) {
    cursorX -= Blockly.Mutator.ICON_SIZE;
  }
  this.iconGroup_.setAttribute('transform',
      'translate(' + cursorX + ', ' + TOP_MARGIN + ')');
  this.computeIconLocation();
  if (Blockly.RTL) {
    cursorX -= Blockly.BlockSvg.SEP_SPACE_X;
  } else {
    cursorX += Blockly.Mutator.ICON_SIZE + Blockly.BlockSvg.SEP_SPACE_X;
  }
  return cursorX;
};

/**
 * Notification that the icon has moved.  Update the arrow accordingly.
 * @param {number} x Absolute horizontal location.
 * @param {number} y Absolute vertical location.
 */
Blockly.Mutator.prototype.setIconLocation = function(x, y) {
  this.iconX_ = x;
  this.iconY_ = y;
  if (this.isVisible()) {
    this.bubble_.setAnchorLocation(x, y);
  }
};

/**
 * Notification that the icon has moved, but we don't really know where.
 * Recompute the icon's location from scratch.
 */
Blockly.Mutator.prototype.computeIconLocation = function() {
  // Find coordinates for the centre of the icon and update the arrow.
  var blockXY = this.block_.getRelativeToSurfaceXY();
  var iconXY = Blockly.getRelativeXY_(this.iconGroup_);
  var newX = blockXY.x + iconXY.x + Blockly.Mutator.ICON_SIZE / 2;
  var newY = blockXY.y + iconXY.y + Blockly.Mutator.ICON_SIZE / 2;
  if (newX !== this.iconX_ || newY !== this.iconY_) {
    this.setIconLocation(newX, newY);
  }
};

/**
 * Returns the center of the block's icon relative to the surface.
 * @return {!Object} Object with x and y properties.
 */
Blockly.Mutator.prototype.getIconLocation = function() {
  return {x: this.iconX_, y: this.iconY_};
};
