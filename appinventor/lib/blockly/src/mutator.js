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
 * @type {?number}
 * @private
 */
Blockly.Mutator.prototype.iconX_ = null;

/**
 * Absolute Y coordinate of icon's centre.
 * @type {?number}
 * @private
 */
Blockly.Mutator.prototype.iconY_ = null;

/**
 * Relative X coordinate of bubble with respect to the icon's centre.
 * In RTL mode the initial value is negated.
 * @private
 */
Blockly.Mutator.prototype.relativeLeft_ = -180;

/**
 * Relative Y coordinate of bubble with respect to the icon's centre.
 * @private
 */
Blockly.Mutator.prototype.relativeTop_ = -230;

/**
 * Is the mutator always visible?
 * @private
 */
Blockly.Mutator.prototype.isPinned_ = false;

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
       width: 8 * quantum,
       height: 8 * quantum,
       rx: 2 * quantum,
       ry: 2 * quantum}, this.iconGroup_);
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
       d: Blockly.Mutator.crossPath_}, this.iconGroup_);
  this.block_.getSvgRoot().appendChild(this.iconGroup_);
  if (this.block_.editable) {
    Blockly.bindEvent_(this.iconGroup_, 'mouseup', this, this.iconClick_);
    Blockly.bindEvent_(this.iconGroup_, 'mouseover', this, this.iconMouseOver_);
    Blockly.bindEvent_(this.iconGroup_, 'mouseout', this, this.iconMouseOut_);
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
      {x: Blockly.Bubble.BORDER_WIDTH, y: Blockly.Bubble.BORDER_WIDTH}, null);
  this.svgBackground_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyMutatorBackground',
       height: '100%', width: '100%'}, this.svgDialog_);

  this.workspace_ = new Blockly.Workspace(true);
  this.flyout_ = new Blockly.Flyout();
  this.flyout_.autoClose = false;
  this.svgDialog_.appendChild(this.flyout_.createDom());
  this.svgDialog_.appendChild(this.workspace_.createDom());
  return this.svgDialog_;
};

/**
 * Callback function triggered when the bubble has resized.
 * Resize the text area accordingly.
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
                        flyoutMetrics.contentHeight);
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
 * Is the mutator bubble always visible?
 * @return {boolean} True if the bubble should be always visible.
 */
Blockly.Mutator.prototype.isPinned = function() {
  return this.isPinned_;
};

/**
 * Set whether the mutator bubble is always visible or not.
 * @param {boolean} pinned True if the bubble should be always visible.
 */
Blockly.Mutator.prototype.setPinned = function(pinned) {
  this.isPinned_ = pinned;
  this.iconMark_.style.fill = pinned ? '#fff' : '';
  this.setVisible_(pinned);
};

/**
 * Is the mutator bubble visible?
 * @return {boolean} True if the bubble is visible.
 * @private
 */
Blockly.Mutator.prototype.isVisible_ = function() {
  return !!this.bubble_;
};

/**
 * Show or hide the mutator bubble.
 * @param {boolean} visible True if the bubble should be visible.
 * @private
 */
Blockly.Mutator.prototype.setVisible_ = function(visible) {
  if (visible == this.isVisible_()) {
    // No change.
    return;
  }
  // Save the bubble location before the visibility switch.
  var relativeXY = this.getBubbleLocation();
  if (visible) {
    // Create the bubble.
    this.bubble_ = new Blockly.Bubble(this.block_.workspace.getBubbleCanvas(),
        this.createEditor_(), this.iconX_, this.iconY_,
        this.relativeLeft_, this.relativeTop_, null, null);
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
        this.block_, function() {
          if (thisObj.rootBlock_.workspace == thisObj.workspace_) {
            thisObj.resizeBubble_();
            thisObj.block_.compose(thisObj.rootBlock_)
          }
        });
    this.updateColour();
  } else {
    // Destroy the bubble.
    this.svgDialog_ = null;
    this.svgBackground_ = null;
    this.flyout_.destroy();
    this.flyout_ = null;
    this.workspace_.destroy();
    this.workspace_ = null;
    this.rootBlock_ = null;
    this.bubble_.destroy();
    this.bubble_ = null;
    this.workspaceWidth_ = 0;
    this.workspaceHeight_ = 0;
    if (this.sourceListener_) {
      Blockly.unbindEvent_(this.sourceListener_);
      this.sourceListener_ = null;
    }
  }
  // Restore the bubble location after the visibility switch.
  this.setBubbleLocation(relativeXY.x, relativeXY.y);
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
 * Clicking on the icon toggles if the bubble is pinned.
 * @param {!Event} e Mouse click event.
 * @private
 */
Blockly.Mutator.prototype.iconClick_ = function(e) {
  this.setPinned(!this.isPinned_);
};

/**
 * Mousing over the icon makes the bubble visible.
 * @param {!Event} e Mouse over event.
 * @private
 */
Blockly.Mutator.prototype.iconMouseOver_ = function(e) {
  if (!this.isPinned_ && Blockly.Block.dragMode_ == 0) {
    this.setVisible_(true);
  }
};

/**
 * Mousing off of the icon hides the bubble (unless it is pinned).
 * @param {!Event} e Mouse out event.
 * @private
 */
Blockly.Mutator.prototype.iconMouseOut_ = function(e) {
  if (!this.isPinned_ && Blockly.Block.dragMode_ == 0) {
    this.setVisible_(false);
  }
};

/**
 * Get the location of this mutator's bubble.
 * @return {!Object} Object with x and y properties.
 */
Blockly.Mutator.prototype.getBubbleLocation = function() {
  if (this.isVisible_()) {
    return this.bubble_.getBubbleLocation();
  } else {
    return {x: this.relativeLeft_, y: this.relativeTop_};
  }
};

/**
 * Set the location of this mutator's bubble.
 * @param {number} x Horizontal offset from block.
 * @param {number} y Vertical offset from block.
 */
Blockly.Mutator.prototype.setBubbleLocation = function(x, y) {
  if (this.isVisible_()) {
    this.bubble_.setBubbleLocation(x, y);
  } else {
    this.relativeLeft_ = x;
    this.relativeTop_ = y;
  }
};

/**
 * Change the colour of a mutator to match its block.
 */
Blockly.Mutator.prototype.updateColour = function() {
  if (this.isVisible_()) {
    var hexColour = Blockly.makeColour(this.block_.getColour());
    this.bubble_.setColour(hexColour);
  }
};

/**
 * Destroy this mutator.
 */
Blockly.Mutator.prototype.destroy = function() {
  // Destroy and unlink the icon.
  this.iconGroup_.parentNode.removeChild(this.iconGroup_);
  this.iconGroup_ = null;
  // Destroy and unlink the bubble.
  this.setVisible_(false);
  // Disconnect links between the block and the mutator.
  this.block_.mutator = null;
  this.block_ = null;
};

/**
 * Render the icon for this mutator.
 * @param {number} titleX Horizontal offset at which to position the icon.
 * @return {number} Width of icon.
 */
Blockly.Mutator.prototype.renderIcon = function(titleX) {
  if (this.block_.collapsed) {
    this.iconGroup_.setAttribute('display', 'none');
    return 0;
  }
  this.iconGroup_.setAttribute('display', 'block');

  var TOP_MARGIN = 5;
  if (Blockly.RTL) {
    titleX -= Blockly.Mutator.ICON_SIZE;
  }
  this.iconGroup_.setAttribute('transform',
                               'translate(' + titleX + ', ' + TOP_MARGIN + ')');
  this.computeIconLocation();
  return Blockly.Mutator.ICON_SIZE;
};

/**
 * Notification that the icon has moved.  Update the arrow accordingly.
 * @param {number} x Absolute horizontal location.
 * @param {number} y Absolute vertical location.
 */
Blockly.Mutator.prototype.setIconLocation = function(x, y) {
  this.iconX_ = x;
  this.iconY_ = y;
  if (this.isVisible_()) {
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
