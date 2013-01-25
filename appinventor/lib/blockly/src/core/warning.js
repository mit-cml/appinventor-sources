/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * http://blockly.googlecode.com/
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
'use strict';

goog.provide('Blockly.Warning');


/**
 * Class for a warning.
 * @param {!Blockly.Block} block The block associated with this warning.
 * @constructor
 */
Blockly.Warning = function(block) {
  this.block_ = block;
  this.createIcon_();
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
 * @type {number}
 * @private
 */
Blockly.Warning.prototype.iconX_ = 0;

/**
 * Absolute Y coordinate of icon's centre.
 * @type {number}
 * @private
 */
Blockly.Warning.prototype.iconY_ = 0;

/**
 * Create the icon on the block.
 * @private
 */
Blockly.Warning.prototype.createIcon_ = function() {
  /* Here's the markup that will be generated:
  <g class="blocklyIconGroup">
    <path class="blocklyIconShield" d="..."/>
    <text class="blocklyIconMark" x="8" y="13">!</text>
  </g>
  */
  this.iconGroup_ = Blockly.createSvgElement('g',
      {'class': 'blocklyIconGroup'}, null);
  var iconShield = Blockly.createSvgElement('path',
      {'class': 'blocklyIconShield',
       'd': 'M 2,15 Q -1,15 0.5,12 L 6.5,1.7 Q 8,-1 9.5,1.7 L 15.5,12 ' +
       'Q 17,15 14,15 z'},
      this.iconGroup_);
  this.iconMark_ = Blockly.createSvgElement('text',
      {'class': 'blocklyIconMark',
       'x': Blockly.Warning.ICON_RADIUS,
       'y': 2 * Blockly.Warning.ICON_RADIUS - 3}, this.iconGroup_);
  this.iconMark_.appendChild(document.createTextNode('!'));
  this.block_.getSvgRoot().appendChild(this.iconGroup_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseup', this, this.iconClick_);
};

/**
 * Create the text for the warning's bubble.
 * @param {string} text The text to display.
 * @return {!Element} The top-level node of the text.
 * @private
 */
Blockly.Warning.prototype.textToDom_ = function(text) {
  var paragraph = Blockly.createSvgElement('text',
      {'class': 'blocklyText', 'y': Blockly.Bubble.BORDER_WIDTH}, null);
  var lines = text.split('\n');
  for (var i = 0; i < lines.length; i++) {
    var tspanElement = Blockly.createSvgElement('tspan',
        {'dy': '1em', 'x': Blockly.Bubble.BORDER_WIDTH}, paragraph);
    var textNode = document.createTextNode(lines[i]);
    tspanElement.appendChild(textNode);
  }
  return paragraph;
};

/**
 * Is the warning bubble visible?
 * @return {boolean} True if the bubble is visible.
 */
Blockly.Warning.prototype.isVisible = function() {
  return !!this.bubble_;
};

/**
 * Show or hide the warning bubble.
 * @param {boolean} visible True if the bubble should be visible.
 */
Blockly.Warning.prototype.setVisible = function(visible) {
  if (visible == this.isVisible()) {
    // No change.
    return;
  }
  if (visible) {
    // Create the bubble.
    var paragraph = this.textToDom_(this.text_);
    this.bubble_ = new Blockly.Bubble(
        /** @type {!Blockly.Workspace} */ (this.block_.workspace),
        paragraph, this.block_.svg_.svgGroup_,
        this.iconX_, this.iconY_, null, null);
    if (Blockly.RTL) {
      // Right-align the paragraph.
      // This cannot be done until the bubble is rendered on screen.
      var maxWidth = paragraph.getBBox().width;
      for (var x = 0, textElement; textElement = paragraph.childNodes[x]; x++) {
        textElement.setAttribute('text-anchor', 'end');
        textElement.setAttribute('x', maxWidth + Blockly.Bubble.BORDER_WIDTH);
      }
    }
    this.updateColour();
    // Bump the warning into the right location.
    var size = this.bubble_.getBubbleSize();
    this.bubble_.setBubbleSize(size.width, size.height);
  } else {
    // Dispose of the bubble.
    this.bubble_.dispose();
    this.bubble_ = null;
    this.body_ = null;
    this.foreignObject_ = null;
  }
};

/**
 * Clicking on the icon toggles if the bubble is visible.
 * @param {!Event} e Mouse click event.
 * @private
 */
Blockly.Warning.prototype.iconClick_ = function(e) {
  this.setVisible(!this.isVisible());
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
 * Set this warning's text.
 * @param {string} text Warning text.
 */
Blockly.Warning.prototype.setText = function(text) {
  this.text_ = text;
  if (this.isVisible()) {
    this.setVisible(false);
    this.setVisible(true);
  }
};

/**
 * Change the colour of a warning to match its block.
 */
Blockly.Warning.prototype.updateColour = function() {
  if (this.isVisible()) {
    var hexColour = Blockly.makeColour(this.block_.getColour());
    this.bubble_.setColour(hexColour);
  }
};

/**
 * Dispose of this warning.
 */
Blockly.Warning.prototype.dispose = function() {
  // Dispose of and unlink the icon.
  goog.dom.removeNode(this.iconGroup_);
  this.iconGroup_ = null;
  // Dispose of and unlink the bubble.
  this.setVisible(false);
  // Disconnect links between the block and the warning.
  this.block_.warning = null;
  this.block_ = null;
};

/**
 * Render the icon for this warning.
 * @param {number} cursorX Horizontal offset at which to position the icon.
 * @return {number} Horizontal offset for next item to draw.
 */
Blockly.Warning.prototype.renderIcon = function(cursorX) {
  if (this.block_.collapsed) {
    this.iconGroup_.setAttribute('display', 'none');
    return cursorX;
  }
  this.iconGroup_.setAttribute('display', 'block');

  var TOP_MARGIN = 5;
  var diameter = 2 * Blockly.Warning.ICON_RADIUS;
  if (Blockly.RTL) {
    cursorX -= diameter;
  }
  this.iconGroup_.setAttribute('transform',
      'translate(' + cursorX + ', ' + TOP_MARGIN + ')');
  this.computeIconLocation();
  if (Blockly.RTL) {
    cursorX -= Blockly.BlockSvg.SEP_SPACE_X;
  } else {
    cursorX += diameter + Blockly.BlockSvg.SEP_SPACE_X;
  }
  return cursorX;
};

/**
 * Notification that the icon has moved.  Update the arrow accordingly.
 * @param {number} x Absolute horizontal location.
 * @param {number} y Absolute vertical location.
 */
Blockly.Warning.prototype.setIconLocation = function(x, y) {
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
