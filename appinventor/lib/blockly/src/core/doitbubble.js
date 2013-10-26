/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
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
 * @fileoverview Object representing a code DoItBubble.
 * @author fraser@google.com (Neil Fraser)
 * @Author keertankini //Copied for second bubble
 */
'use strict';

goog.provide('Blockly.Comment');


/**
 * Class for a DoItBubble.
 * @param {!Blockly.Block} block The block associated with this DoItBubble.
 * @constructor
 */
Blockly.DoItBubble = function(block) {
  this.block_ = block;
  this.createIcon_();
};

/**
 * Radius of the DoItBubble icon.
 */
Blockly.DoItBubble.ICON_RADIUS = 8;

/**
 * Bubble UI (if visible).
 * @type {Blockly.Bubble}
 * @private
 */
Blockly.DoItBubble.prototype.bubble_ = null;

/**
 * DoItBubble text (if bubble is not visible).
 * @private
 */
Blockly.DoItBubble.prototype.text_ = '';

/**
 * Absolute X coordinate of icon's center.
 * @private
 */
Blockly.DoItBubble.prototype.iconX_ = 0;

/**
 * Absolute Y coordinate of icon's centre.
 * @private
 */
Blockly.DoItBubble.prototype.iconY_ = 0;

/**
 * Width of bubble.
 * @private
 */
Blockly.DoItBubble.prototype.width_ = 160;

/**
 * Height of bubble.
 * @private
 */
Blockly.DoItBubble.prototype.height_ = 80;

/**
 * Create the icon on the block.
 * @private
 */
Blockly.DoItBubble.prototype.createIcon_ = function() {
  /* Here's the markup that will be generated:
  <g class="blocklyIconGroup">
    <circle class="blocklyIconShield" r="8" cx="8" cy="8"/>
    <text class="blocklyIconMark" x="8" y="13">?</text>
  </g>
  */
  this.iconGroup_ = Blockly.createSvgElement('g',
      {'class': 'blocklyIconGroup'}, null);
  var iconShield = Blockly.createSvgElement('circle',
      {'class': 'blocklyIconShield',
       'r': Blockly.DoItBubble.ICON_RADIUS,
       'cx': Blockly.DoItBubble.ICON_RADIUS,
       'cy': Blockly.DoItBubble.ICON_RADIUS}, this.iconGroup_);
  this.iconMark_ = Blockly.createSvgElement('text',
      {'class': 'blocklyIconMark',
       'x': Blockly.DoItBubble.ICON_RADIUS,
       'y': 2 * Blockly.DoItBubble.ICON_RADIUS - 3}, this.iconGroup_);
  this.iconMark_.appendChild(document.createTextNode('?'));
  this.block_.getSvgRoot().appendChild(this.iconGroup_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseup', this, this.iconClick_);
};

/**
 * Create the editor for the DoItBubble's bubble.
 * @return {!Element} The top-level node of the editor.
 * @private
 */
Blockly.DoItBubble.prototype.createEditor_ = function() {
  /* Create the editor.  Here's the markup that will be generated:
    <foreignObject x="8" y="8" width="164" height="164">
      <body xmlns="http://www.w3.org/1999/xhtml" class="blocklyMinimalBody">
        <textarea xmlns="http://www.w3.org/1999/xhtml"
            class="blocklyDoItBubbleTextarea"
            style="height: 164px; width: 164px;"></textarea>
      </body>
    </foreignObject>
  */
  this.foreignObject_ = Blockly.createSvgElement('foreignObject',
      {'x': Blockly.Bubble.BORDER_WIDTH, 'y': Blockly.Bubble.BORDER_WIDTH},
      null);
  var body = document.createElementNS(Blockly.HTML_NS, 'body');
  body.setAttribute('xmlns', Blockly.HTML_NS);
  body.className = 'blocklyMinimalBody';
  this.textarea_ = document.createElementNS(Blockly.HTML_NS, 'textarea');
  this.textarea_.className = 'blocklyDoItBubbleTextarea';
  this.textarea_.setAttribute('dir', Blockly.RTL ? 'RTL' : 'LTR');
  body.appendChild(this.textarea_);
  this.foreignObject_.appendChild(body);
  Blockly.bindEvent_(this.textarea_, 'mouseup', this, this.textareaFocus_);
  return this.foreignObject_;
};

/**
 * Callback function triggered when the bubble has resized.
 * Resize the text area accordingly.
 * @private
 */
Blockly.DoItBubble.prototype.resizeBubble_ = function() {
  var size = this.bubble_.getBubbleSize();
  var doubleBorderWidth = 2 * Blockly.Bubble.BORDER_WIDTH;
  this.foreignObject_.setAttribute('width', size.width - doubleBorderWidth);
  this.foreignObject_.setAttribute('height', size.height - doubleBorderWidth);
  this.textarea_.style.width = (size.width - doubleBorderWidth - 4) + 'px';
  this.textarea_.style.height = (size.height - doubleBorderWidth - 4) + 'px';
};

/**
 * Is the DoItBubble bubble visible?
 * @return {boolean} True if the bubble is visible.
 */
Blockly.DoItBubble.prototype.isVisible = function() {
  return !!this.bubble_;
};

/**
 * Show or hide the DoItBubble bubble.
 * @param {boolean} visible True if the bubble should be visible.
 */
Blockly.DoItBubble.prototype.setVisible = function(visible) {
  if (visible == this.isVisible()) {
    // No change.
    return;
  }
  // Save the bubble stats before the visibility switch.
  var text = this.getText();
  var size = this.getBubbleSize();
  if (visible) {
    // Create the bubble.
    this.bubble_ = new Blockly.Bubble(
        /** @type {!Blockly.Workspace} */ (this.block_.workspace),
        this.createEditor_(), this.block_.svg_.svgGroup_,
        this.iconX_, this.iconY_,
        this.width_, this.height_);
    this.bubble_.registerResizeEvent(this, this.resizeBubble_);
    this.updateColour();
    this.text_ = null;
  } else {
    // Dispose of the bubble.
    this.bubble_.dispose();
    this.bubble_ = null;
    this.textarea_ = null;
    this.foreignObject_ = null;
  }
  // Restore the bubble stats after the visibility switch.
  this.setText(text);
  this.setBubbleSize(size.width, size.height);
};

/**
 * Clicking on the icon toggles if the bubble is visible.
 * @param {!Event} e Mouse click event.
 * @private
 */
Blockly.DoItBubble.prototype.iconClick_ = function(e) {
  this.setVisible(!this.isVisible());
};


/**
 * Bring the DoItBubble to the top of the stack when clicked on.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.DoItBubble.prototype.textareaFocus_ = function(e) {
  // Ideally this would be hooked to the focus event for the DoItBubble.
  // However doing so in Firefox swallows the cursor for unknown reasons.
  // So this is hooked to mouseup instead.  No big deal.
  this.bubble_.promote_();
  // Since the act of moving this node within the DOM causes a loss of focus,
  // we need to reapply the focus.
  this.textarea_.focus();
};

/**
 * Get the dimensions of this DoItBubble's bubble.
 * @return {!Object} Object with width and height properties.
 */
Blockly.DoItBubble.prototype.getBubbleSize = function() {
  if (this.isVisible()) {
    return this.bubble_.getBubbleSize();
  } else {
    return {width: this.width_, height: this.height_};
  }
};

/**
 * Size this DoItBubble's bubble.
 * @param {number} width Width of the bubble.
 * @param {number} height Height of the bubble.
 */
Blockly.DoItBubble.prototype.setBubbleSize = function(width, height) {
  if (this.isVisible()) {
    this.bubble_.setBubbleSize(width, height);
  } else {
    this.width_ = width;
    this.height_ = height;
  }
};

/**
 * Returns this DoItBubble's text.
 * @return {string} DoItBubble text.
 */
Blockly.DoItBubble.prototype.getText = function() {
  return this.isVisible() ? this.textarea_.value : this.text_;
};

/**
 * Set this DoItBubble's text.
 * @param {string} text DoItBubble text.
 */
Blockly.DoItBubble.prototype.setText = function(text) {
  if (this.isVisible()) {
    this.textarea_.value = text;
  } else {
    this.text_ = text;
  }
};

/**
 * Change the colour of a DoItBubble to match its block.
 */
Blockly.DoItBubble.prototype.updateColour = function() {
  if (this.isVisible()) {
    var hexColour = Blockly.makeColour(this.block_.getColour());
    this.bubble_.setColour(hexColour);
  }
};

/**
 * Dispose of this DoItBubble.
 */
Blockly.DoItBubble.prototype.dispose = function() {
  // Dispose of and unlink the icon.
  goog.dom.removeNode(this.iconGroup_);
  this.iconGroup_ = null;
  // Dispose of and unlink the bubble.
  this.setVisible(false);
  // Disconnect links between the block and the DoItBubble.
  this.block_.DoItBubble = null;
  this.block_ = null;
};

/**
 * Render the icon for this DoItBubble.
 * @param {number} cursorX Horizontal offset at which to position the icon.
 * @return {number} Horizontal offset for next item to draw.
 */
Blockly.DoItBubble.prototype.renderIcon = function(cursorX) {
  if (this.block_.collapsed) {
    this.iconGroup_.setAttribute('display', 'none');
    return cursorX;
  }
  this.iconGroup_.setAttribute('display', 'block');

  var TOP_MARGIN = 5;
  var diameter = 2 * Blockly.DoItBubble.ICON_RADIUS;
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
Blockly.DoItBubble.prototype.setIconLocation = function(x, y) {
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
Blockly.DoItBubble.prototype.computeIconLocation = function() {
  // Find coordinates for the centre of the icon and update the arrow.
  var blockXY = this.block_.getRelativeToSurfaceXY();
  var iconXY = Blockly.getRelativeXY_(this.iconGroup_);
  var newX = blockXY.x + iconXY.x + Blockly.DoItBubble.ICON_RADIUS;
  var newY = blockXY.y + iconXY.y + Blockly.DoItBubble.ICON_RADIUS;
  if (newX !== this.iconX_ || newY !== this.iconY_) {
    this.setIconLocation(newX, newY);
  }
};

/**
 * Returns the center of the block's icon relative to the surface.
 * @return {!Object} Object with x and y properties.
 */
Blockly.DoItBubble.prototype.getIconLocation = function() {
  return {x: this.iconX_, y: this.iconY_};
};
