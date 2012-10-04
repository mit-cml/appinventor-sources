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
 * @fileoverview Object representing a warning.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a warning.
 * @param {!Blockly.Block} block The block associated with this warning.
 * @constructor
 */
Blockly.Warning = function(block) {
  this.block_ = block;
  this.createIcon_();

  this.setPinned(false);
};

/**
 * Radius of the warning icon.
 */
Blockly.Warning.ICON_RADIUS = 8;

/**
 * Bubble UI (if visible).
 * @type {Blockly.Bubble}
 * @private
 */
Blockly.Warning.prototype.bubble_ = null;

/**
 * Warning text (if bubble is not visible).
 * @private
 */
Blockly.Warning.prototype.text_ = '';

/**
 * Absolute X coordinate of icon's center.
 * @type {?number}
 * @private
 */
Blockly.Warning.prototype.iconX_ = null;

/**
 * Absolute Y coordinate of icon's centre.
 * @type {?number}
 * @private
 */
Blockly.Warning.prototype.iconY_ = null;

/**
 * Relative X coordinate of bubble with respect to the icon's centre.
 * In RTL mode the initial value is negated.
 * @private
 */
Blockly.Warning.prototype.relativeLeft_ = -100;

/**
 * Relative Y coordinate of bubble with respect to the icon's centre.
 * @private
 */
Blockly.Warning.prototype.relativeTop_ = -120;

/**
 * Is the warning always visible?
 * @private
 */
Blockly.Warning.prototype.isPinned_ = false;

/**
 * Create the icon on the block.
 * @private
 */
Blockly.Warning.prototype.createIcon_ = function() {
  /* Here's the markup that will be generated:
  <g class="blocklyIconGroup">
    <circle class="blocklyIconShield" r="8" cx="8" cy="8"/>
    <text class="blocklyIconMark" x="4" y="13">!</text>
  </g>
  */
  this.iconGroup_ = Blockly.createSvgElement('g',
      {'class': 'blocklyIconGroup'}, null);
  var iconShield = Blockly.createSvgElement('path',
      {'class': 'blocklyIconShield',
       d: 'M 2,15 Q -1,15 0.5,12 L 6.5,1.7 Q 8,-1 9.5,1.7 L 15.5,12 ' +
       'Q 17,15 14,15 z'},
      this.iconGroup_);
  this.iconMark_ = Blockly.createSvgElement('text',
      {'class': 'blocklyIconMark',
       x: Blockly.Warning.ICON_RADIUS / 2 + 2,
       y: 2 * Blockly.Warning.ICON_RADIUS - 3}, this.iconGroup_);
  this.iconMark_.appendChild(Blockly.svgDoc.createTextNode('!'));
  this.block_.getSvgRoot().appendChild(this.iconGroup_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseup', this, this.iconClick_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseover', this, this.iconMouseOver_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseout', this, this.iconMouseOut_);
};

/**
 * Create the text for the warning's bubble.
 * @param {string} text The text to display.
 * @return {!Element} The top-level node of the text.
 * @private
 */
Blockly.Warning.prototype.textToDom_ = function(text) {
  var paragraph = Blockly.createSvgElement('text',
      {'class': 'blocklyText', y: Blockly.Bubble.BORDER_WIDTH}, null);
  var lines = text.split('\n');
  for (var i = 0; i < lines.length; i++) {
    var tspanElement = Blockly.createSvgElement('tspan',
        {dy: '1em', x: Blockly.Bubble.BORDER_WIDTH}, paragraph);
    var textNode = Blockly.svgDoc.createTextNode(lines[i]);
    tspanElement.appendChild(textNode);
  }
  return paragraph;
};

/**
 * Is the warning bubble always visible?
 * @return {boolean} True if the bubble should be always visible.
 */
Blockly.Warning.prototype.isPinned = function() {
  return this.isPinned_;
};

/**
 * Set whether the warning bubble is always visible or not.
 * @param {boolean} pinned True if the bubble should be always visible.
 */
Blockly.Warning.prototype.setPinned = function(pinned) {
  this.isPinned_ = pinned;
  this.iconMark_.style.fill = pinned ? '#fff' : '';
  if (this.bubble_) {
    this.bubble_.setDisabled(!this.isPinned_);
  }
};

/**
 * Is the warning bubble visible?
 * @return {boolean} True if the bubble is visible.
 * @private
 */
Blockly.Warning.prototype.isVisible_ = function() {
  return !!this.bubble_;
};

/**
 * Show or hide the warning bubble.
 * @param {boolean} visible True if the bubble should be visible.
 * @private
 */
Blockly.Warning.prototype.setVisible_ = function(visible) {
  if (visible == this.isVisible_()) {
    // No change.
    return;
  }
  if (visible) {
    // Create the bubble.
    var paragraph = this.textToDom_(this.text_);
    this.bubble_ = new Blockly.Bubble(this.block_.workspace.getBubbleCanvas(),
        paragraph, this.iconX_, this.iconY_,
        this.relativeLeft_, this.relativeTop_, null, null);
    if (Blockly.RTL) {
      // Right-align the paragraph.
      // This cannot be done until the bubble is rendered on screen.
      var maxWidth = paragraph.getBBox().width;
      for (var x = 0, textElement; textElement = paragraph.childNodes[x]; x++) {
        textElement.setAttribute('text-anchor', 'end');
        textElement.setAttribute('x', maxWidth + Blockly.Bubble.BORDER_WIDTH);
      }
    }
    this.bubble_.setDisabled(!this.isPinned_);
    this.updateColour();
  } else {
    // Destroy the bubble.
    this.bubble_.destroy();
    this.bubble_ = null;
    this.body_ = null;
    this.foreignObject_ = null;
  }
};

/**
 * Clicking on the icon toggles if the bubble is pinned.
 * @param {!Event} e Mouse click event.
 * @private
 */
Blockly.Warning.prototype.iconClick_ = function(e) {
  this.setPinned(!this.isPinned_);
};

/**
 * Mousing over the icon makes the bubble visible.
 * @param {!Event} e Mouse over event.
 * @private
 */
Blockly.Warning.prototype.iconMouseOver_ = function(e) {
  if (!this.isPinned_ && Blockly.Block.dragMode_ == 0) {
    this.setVisible_(true);
  }
};

/**
 * Mousing off of the icon hides the bubble (unless it is pinned).
 * @param {!Event} e Mouse out event.
 * @private
 */
Blockly.Warning.prototype.iconMouseOut_ = function(e) {
  if (!this.isPinned_ && Blockly.Block.dragMode_ == 0) {
    this.setVisible_(false);
  }
};

/**
 * Bring the warning to the top of the stack when clicked on.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Warning.prototype.bodyFocus_ = function(e) {
  this.bubble_.promote_();
};

/**
 * Set the location of this warning's bubble.
 * @param {number} x Horizontal offset from block.
 * @param {number} y Vertical offset from block.
 */
Blockly.Warning.prototype.setBubbleLocation = function(x, y) {
  if (this.isVisible_()) {
    this.bubble_.setBubbleLocation(x, y);
  } else {
    this.relativeLeft_ = x;
    this.relativeTop_ = y;
  }
};

/**
 * Set this warning's text.
 * @param {string} text Warning text.
 */
Blockly.Warning.prototype.setText = function(text) {
  this.text_ = text;
  if (this.isVisible_()) {
    this.setVisible_(false);
    this.setVisible_(true);
  }
};

/**
 * Change the colour of a warning to match its block.
 */
Blockly.Warning.prototype.updateColour = function() {
  if (this.isVisible_()) {
    var hexColour = Blockly.makeColour(this.block_.getColour());
    this.bubble_.setColour(hexColour);
  }
};

/**
 * Destroy this warning.
 */
Blockly.Warning.prototype.destroy = function() {
  // Destroy and unlink the icon.
  this.iconGroup_.parentNode.removeChild(this.iconGroup_);
  this.iconGroup_ = null;
  // Destroy and unlink the bubble.
  this.setVisible_(false);
  // Disconnect links between the block and the warning.
  this.block_.warning = null;
  this.block_ = null;
};

/**
 * Render the icon for this warning.
 * @param {number} titleX Horizontal offset at which to position the icon.
 * @return {Object} Height and width of icon, or null if not displayed.
 */
Blockly.Warning.prototype.renderIcon = function(titleX) {
  if (this.block_.collapsed) {
    this.iconGroup_.setAttribute('display', 'none');
    return null;
  }
  this.iconGroup_.setAttribute('display', 'block');

  var TOP_MARGIN = 5;
  var diameter = 2 * Blockly.Warning.ICON_RADIUS;
  if (Blockly.RTL) {
    titleX -= diameter;
  }
  this.iconGroup_.setAttribute('transform',
      'translate(' + titleX + ', ' + TOP_MARGIN + ')');
  this.computeIconLocation();
  return {x: diameter, y: diameter};
};

/**
 * Notification that the icon has moved.  Update the arrow accordingly.
 * @param {number} x Absolute horizontal location.
 * @param {number} y Absolute vertical location.
 */
Blockly.Warning.prototype.setIconLocation = function(x, y) {
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
Blockly.Warning.prototype.computeIconLocation = function() {
  // Find coordinates for the centre of the icon and update the arrow.
  var blockXY = this.block_.getRelativeToSurfaceXY();
  var iconXY = Blockly.getRelativeXY_(this.iconGroup_);
  var newX = blockXY.x + iconXY.x + Blockly.Warning.ICON_RADIUS;
  var newY = blockXY.y + iconXY.y + Blockly.Warning.ICON_RADIUS;
  if (newX !== this.iconX_ || newY !== this.iconY_) {
    this.setIconLocation(newX, newY);
  }
};

/**
 * Returns the center of the block's icon relative to the surface.
 * @return {!Object} Object with x and y properties.
 */
Blockly.Warning.prototype.getIconLocation = function() {
  return {x: this.iconX_, y: this.iconY_};
};
