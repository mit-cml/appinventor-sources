/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
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
 * @fileoverview Object representing a code comment.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a comment.
 * @param {!Blockly.Block} block The block associated with this comment.
 * @constructor
 */
Blockly.Comment = function(block) {
  this.block_ = block;
  this.createIcon_();

  this.setPinned(false);
};

/**
 * Radius of the comment icon.
 */
Blockly.Comment.ICON_RADIUS = 8;

/**
 * Bubble UI (if visible).
 * @type {Blockly.Bubble}
 * @private
 */
Blockly.Comment.prototype.bubble_ = null;

/**
 * Comment text (if bubble is not visible).
 * @private
 */
Blockly.Comment.prototype.text_ = '';

/**
 * Absolute X coordinate of icon's center.
 * @type {?number}
 * @private
 */
Blockly.Comment.prototype.iconX_ = null;

/**
 * Absolute Y coordinate of icon's centre.
 * @type {?number}
 * @private
 */
Blockly.Comment.prototype.iconY_ = null;

/**
 * Relative X coordinate of bubble with respect to the icon's centre.
 * In RTL mode the initial value is negated.
 * @private
 */
Blockly.Comment.prototype.relativeLeft_ = -100;

/**
 * Relative Y coordinate of bubble with respect to the icon's centre.
 * @private
 */
Blockly.Comment.prototype.relativeTop_ = -120;

/**
 * Width of bubble.
 * @private
 */
Blockly.Comment.prototype.width_ = 160;

/**
 * Height of bubble.
 * @private
 */
Blockly.Comment.prototype.height_ = 80;

/**
 * Is the comment always visible?
 * @private
 */
Blockly.Comment.prototype.isPinned_ = false;

/**
 * Create the icon on the block.
 * @private
 */
Blockly.Comment.prototype.createIcon_ = function() {
  /* Here's the markup that will be generated:
  <g class="blocklyIconGroup">
    <circle class="blocklyIconShield" r="8" cx="8" cy="8"/>
    <text class="blocklyIconMark" x="4" y="13">?</text>
  </g>
  */
  this.iconGroup_ = Blockly.createSvgElement('g',
      {'class': 'blocklyIconGroup'}, null);
  var iconShield = Blockly.createSvgElement('circle',
      {'class': 'blocklyIconShield',
       r: Blockly.Comment.ICON_RADIUS,
       cx: Blockly.Comment.ICON_RADIUS,
       cy: Blockly.Comment.ICON_RADIUS}, this.iconGroup_);
  this.iconMark_ = Blockly.createSvgElement('text',
      {'class': 'blocklyIconMark',
       x: Blockly.Comment.ICON_RADIUS / 2 + 0.5,
       y: 2 * Blockly.Comment.ICON_RADIUS - 3}, this.iconGroup_);
  this.iconMark_.appendChild(Blockly.svgDoc.createTextNode('?'));
  this.block_.getSvgRoot().appendChild(this.iconGroup_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseup', this, this.iconClick_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseover', this, this.iconMouseOver_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseout', this, this.iconMouseOut_);
};

/**
 * Create the editor for the comment's bubble.
 * @return {!Element} The top-level node of the editor.
 * @private
 */
Blockly.Comment.prototype.createEditor_ = function() {
  /* Create the editor.  Here's the markup that will be generated:
    <foreignObject x="8" y="8" width="164" height="164">
      <body xmlns="http://www.w3.org/1999/xhtml" class="blocklyMinimalBody">
        <textarea xmlns="http://www.w3.org/1999/xhtml"
            class="blocklyCommentTextarea"
            style="height: 164px; width: 164px;"></textarea>
      </body>
    </foreignObject>
  */
  this.foreignObject_ = Blockly.createSvgElement('foreignObject',
      {x: Blockly.Bubble.BORDER_WIDTH, y: Blockly.Bubble.BORDER_WIDTH},
      null);
  var body = Blockly.svgDoc.createElementNS(Blockly.HTML_NS, 'body');
  body.setAttribute('xmlns', Blockly.HTML_NS);
  body.className = 'blocklyMinimalBody';
  this.textarea_ = Blockly.svgDoc.createElementNS(Blockly.HTML_NS, 'textarea');
  this.textarea_.className = 'blocklyCommentTextarea';
  this.textarea_.setAttribute('dir', Blockly.RTL ? 'RTL' : 'LTR');
  body.appendChild(this.textarea_);
  this.foreignObject_.appendChild(body);
  Blockly.bindEvent_(this.textarea_, 'mouseup', this, this.textareaFocus_);
  return this.foreignObject_;
};

/**
 * Callback function triggered when the bubble has resized.
 * Resize the text area accordingly.
 */
Blockly.Comment.prototype.resizeBubble_ = function() {
  var size = this.bubble_.getBubbleSize();
  var doubleBorderWidth = 2 * Blockly.Bubble.BORDER_WIDTH;
  this.foreignObject_.setAttribute('width', size.width - doubleBorderWidth);
  this.foreignObject_.setAttribute('height', size.height - doubleBorderWidth);
  this.textarea_.style.width = (size.width - doubleBorderWidth - 4) + 'px';
  this.textarea_.style.height = (size.height - doubleBorderWidth - 4) + 'px';
};

/**
 * Is the comment bubble always visible?
 * @return {boolean} True if the bubble should be always visible.
 */
Blockly.Comment.prototype.isPinned = function() {
  return this.isPinned_;
};

/**
 * Set whether the comment bubble is always visible or not.
 * @param {boolean} pinned True if the bubble should be always visible.
 */
Blockly.Comment.prototype.setPinned = function(pinned) {
  this.isPinned_ = pinned;
  this.iconMark_.style.fill = pinned ? '#fff' : '';
  if (this.bubble_) {
    this.bubble_.setDisabled(!this.isPinned_);
  }
};

/**
 * Is the comment bubble visible?
 * @return {boolean} True if the bubble is visible.
 * @private
 */
Blockly.Comment.prototype.isVisible_ = function() {
  return !!this.bubble_;
};

/**
 * Show or hide the comment bubble.
 * @param {boolean} visible True if the bubble should be visible.
 * @private
 */
Blockly.Comment.prototype.setVisible_ = function(visible) {
  if (visible == this.isVisible_()) {
    // No change.
    return;
  }
  // Save the bubble stats before the visibility switch.
  var text = this.getText();
  var relativeXY = this.getBubbleLocation();
  var size = this.getBubbleSize();
  if (visible) {
    // Create the bubble.
    this.bubble_ = new Blockly.Bubble(this.block_.workspace.getBubbleCanvas(),
        this.createEditor_(), this.iconX_, this.iconY_,
        this.relativeLeft_, this.relativeTop_, this.width_, this.height_);
    this.bubble_.registerResizeEvent(this, this.resizeBubble_);
    this.bubble_.setDisabled(!this.isPinned_);
    this.updateColour();
    this.text_ = null;
  } else {
    // Destroy the bubble.
    this.bubble_.destroy();
    this.bubble_ = null;
    this.textarea_ = null;
    this.foreignObject_ = null;
  }
  // Restore the bubble stats after the visibility switch.
  this.setText(text);
  this.setBubbleLocation(relativeXY.x, relativeXY.y);
  this.setBubbleSize(size.width, size.height);
};

/**
 * Clicking on the icon toggles if the bubble is pinned.
 * @param {!Event} e Mouse click event.
 * @private
 */
Blockly.Comment.prototype.iconClick_ = function(e) {
  this.setPinned(!this.isPinned_);
};

/**
 * Mousing over the icon makes the bubble visible.
 * @param {!Event} e Mouse over event.
 * @private
 */
Blockly.Comment.prototype.iconMouseOver_ = function(e) {
  if (!this.isPinned_ && Blockly.Block.dragMode_ == 0) {
    this.setVisible_(true);
  }
};

/**
 * Mousing off of the icon hides the bubble (unless it is pinned).
 * @param {!Event} e Mouse out event.
 * @private
 */
Blockly.Comment.prototype.iconMouseOut_ = function(e) {
  if (!this.isPinned_ && Blockly.Block.dragMode_ == 0) {
    this.setVisible_(false);
  }
};

/**
 * Bring the comment to the top of the stack when clicked on.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Comment.prototype.textareaFocus_ = function(e) {
  // Ideally this would be hooked to the focus event for the comment.
  // However doing so in Firefox swallows the cursor for unknown reasons.
  // So this is hooked to mouseup instead.  No big deal.
  this.bubble_.promote_();
  // Since the act of moving this node within the DOM causes a loss of focus,
  // we need to reapply the focus.
  this.textarea_.focus();
};

/**
 * Get the location of this comment's bubble.
 * @return {!Object} Object with x and y properties.
 */
Blockly.Comment.prototype.getBubbleLocation = function() {
  if (this.isVisible_()) {
    return this.bubble_.getBubbleLocation();
  } else {
    return {x: this.relativeLeft_, y: this.relativeTop_};
  }
};

/**
 * Set the location of this comment's bubble.
 * @param {number} x Horizontal offset from block.
 * @param {number} y Vertical offset from block.
 */
Blockly.Comment.prototype.setBubbleLocation = function(x, y) {
  if (this.isVisible_()) {
    this.bubble_.setBubbleLocation(x, y);
  } else {
    this.relativeLeft_ = x;
    this.relativeTop_ = y;
  }
};

/**
 * Get the dimensions of this comment's bubble.
 * @return {!Object} Object with width and height properties.
 */
Blockly.Comment.prototype.getBubbleSize = function() {
  if (this.isVisible_()) {
    return this.bubble_.getBubbleSize();
  } else {
    return {width: this.width_, height: this.height_};
  }
};

/**
 * Size this comment's bubble.
 * @param {number} width Width of the bubble.
 * @param {number} height Height of the bubble.
 */
Blockly.Comment.prototype.setBubbleSize = function(width, height) {
  if (this.isVisible_()) {
    this.bubble_.setBubbleSize(width, height);
  } else {
    this.width_ = width;
    this.height_ = height;
  }
};

/**
 * Returns this comment's text.
 * @return {string} Comment text.
 */
Blockly.Comment.prototype.getText = function() {
  return this.isVisible_() ? this.textarea_.value : this.text_;
};

/**
 * Set this comment's text.
 * @param {string} text Comment text.
 */
Blockly.Comment.prototype.setText = function(text) {
  if (this.isVisible_()) {
    this.textarea_.value = text;
  } else {
    this.text_ = text;
  }
};

/**
 * Change the colour of a comment to match its block.
 */
Blockly.Comment.prototype.updateColour = function() {
  if (this.isVisible_()) {
    var hexColour = Blockly.makeColour(this.block_.getColour());
    this.bubble_.setColour(hexColour);
  }
};

/**
 * Destroy this comment.
 */
Blockly.Comment.prototype.destroy = function() {
  // Destroy and unlink the icon.
  this.iconGroup_.parentNode.removeChild(this.iconGroup_);
  this.iconGroup_ = null;
  // Destroy and unlink the bubble.
  this.setVisible_(false);
  // Disconnect links between the block and the comment.
  this.block_.comment = null;
  this.block_ = null;
};

/**
 * Render the icon for this comment.
 * @param {number} titleX Horizontal offset at which to position the icon.
 * @return {number} Width of icon.
 */
Blockly.Comment.prototype.renderIcon = function(titleX) {
  if (this.block_.collapsed) {
    this.iconGroup_.setAttribute('display', 'none');
    return 0;
  }
  this.iconGroup_.setAttribute('display', 'block');

  var TOP_MARGIN = 5;
  var diameter = 2 * Blockly.Comment.ICON_RADIUS;
  if (Blockly.RTL) {
    titleX -= diameter;
  }
  this.iconGroup_.setAttribute('transform',
                               'translate(' + titleX + ', ' + TOP_MARGIN + ')');
  this.computeIconLocation();
  return diameter;
};

/**
 * Notification that the icon has moved.  Update the arrow accordingly.
 * @param {number} x Absolute horizontal location.
 * @param {number} y Absolute vertical location.
 */
Blockly.Comment.prototype.setIconLocation = function(x, y) {
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
Blockly.Comment.prototype.computeIconLocation = function() {
  // Find coordinates for the centre of the icon and update the arrow.
  var blockXY = this.block_.getRelativeToSurfaceXY();
  var iconXY = Blockly.getRelativeXY_(this.iconGroup_);
  var newX = blockXY.x + iconXY.x + Blockly.Comment.ICON_RADIUS;
  var newY = blockXY.y + iconXY.y + Blockly.Comment.ICON_RADIUS;
  if (newX !== this.iconX_ || newY !== this.iconY_) {
    this.setIconLocation(newX, newY);
  }
};

/**
 * Returns the center of the block's icon relative to the surface.
 * @return {!Object} Object with x and y properties.
 */
Blockly.Comment.prototype.getIconLocation = function() {
  return {x: this.iconX_, y: this.iconY_};
};
