/**
 * Visual Blocks Editor
 *
 * Copyright 2011 Google Inc.
 * http://code.google.com/p/google-blockly/
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
 * @param {!Element} commentGroup The SVG group to append the comment bubble.
 * @constructor
 */
Blockly.Comment = function(block, commentGroup) {
  this.block_ = block;

  var angle = Blockly.Comment.ARROW_ANGLE;
  if (Blockly.RTL) {
    angle = -angle;
  }
  this.arrow_radians_ = angle / 360 * Math.PI * 2;

  this.createIcon_();
  this.createBubble_(commentGroup);

  this.setPinned(false);
  this.updateColour();
};

/**
 * Radius of the comment icon.
 */
Blockly.Comment.ICON_RADIUS = 8;

/**
 * Width of the border around the comment bubble.
 */
Blockly.Comment.BORDER_WIDTH = 6;

/**
 * Determines the thickness of the base of the arrow in relation to the size
 * of the comment bubble.  Higher numbers result in thinner arrows.
 */
Blockly.Comment.ARROW_THICKNESS = 10;

/**
 * The number of degrees that the arrow bends counter-clockwise.
 */
Blockly.Comment.ARROW_ANGLE = 20;

/**
 * The sharpness of the arrow's bend.  Higher numbers result in smoother arrows.
 */
Blockly.Comment.ARROW_BEND = 4;

/**
 * Wrapper function called when a mouseUp occurs during a drag operation.
 * @type {Function}
 * @private
 */
Blockly.Comment.onMouseUpWrapper_ = null;

/**
 * Wrapper function called when a mouseMove occurs during a drag operation.
 * @type {Function}
 * @private
 */
Blockly.Comment.onMouseMoveWrapper_ = null;

/**
 * Stop binding to the global mouseup and mousemove events.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Comment.unbindDragEvents_ = function(e) {
  if (Blockly.Comment.onMouseUpWrapper_) {
    Blockly.unbindEvent_(Blockly.svgDoc, 'mouseup',
                         Blockly.Comment.onMouseUpWrapper_);
    Blockly.Comment.onMouseUpWrapper_ = null;
  }
  if (Blockly.Comment.onMouseMoveWrapper_) {
    Blockly.unbindEvent_(Blockly.svgDoc, 'mousemove',
                         Blockly.Comment.onMouseMoveWrapper_);
    Blockly.Comment.onMouseMoveWrapper_ = null;
  }
};

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
    <text class="blocklyCommentMark" x="4" y="13">?</text>
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
      {'class': 'blocklyCommentMark',
      x: Blockly.Comment.ICON_RADIUS / 2,
      y: 2 * Blockly.Comment.ICON_RADIUS - 3}, this.iconGroup_);
  this.iconMark_.appendChild(Blockly.svgDoc.createTextNode('?'));
  this.block_.getSvgRoot().appendChild(this.iconGroup_);
  Blockly.bindEvent_(this.iconGroup_, 'click', this, this.iconClick_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseover', this, this.iconMouseOver_);
  Blockly.bindEvent_(this.iconGroup_, 'mouseout', this, this.iconMouseOut_);
};

/**
 * Create the icon's bubble.
 * @param {!Element} commentGroup The SVG group to append the comment bubble.
 * @private
 */
Blockly.Comment.prototype.createBubble_ = function(commentGroup) {
  /* Create the editor.  Here's the markup that will be generated:
  <g>
    <g filter="url(#blocklyEmboss)">
      <path d="... Z" />
      <rect class="blocklyDraggable" rx="8" ry="8" width="180" height="180"/>
    </g>
    <g transform="translate(165, 165)" class="blocklyResizeSE">
      <polygon points="0,15 15,15 15,0"/>
      <line class="blocklyResizeLine" x1="5" y1="14" x2="14" y2="5"/>
      <line class="blocklyResizeLine" x1="10" y1="14" x2="14" y2="10"/>
    </g>
    <foreignObject x="8" y="8" width="164" height="164">
      <body xmlns="http://www.w3.org/1999/xhtml" class="blocklyMinimalBody">
        <textarea xmlns="http://www.w3.org/1999/xhtml"
            class="blocklyCommentTextarea"
            style="height: 164px; width: 164px;"></textarea>
      </body>
    </foreignObject>
  </g>
  */
  this.bubbleGroup_ = Blockly.createSvgElement('g', {}, null);
  var bubbleEmboss = Blockly.createSvgElement('g',
      {filter: 'url(#blocklyEmboss)'}, this.bubbleGroup_);
  this.bubbleArrow_ = Blockly.createSvgElement('path', {}, bubbleEmboss);
  this.bubbleBack_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyDraggable', x: 0, y: 0,
      rx: Blockly.Comment.BORDER_WIDTH, ry: Blockly.Comment.BORDER_WIDTH},
      bubbleEmboss);
  this.resizeGroup_ = Blockly.createSvgElement('g',
      {'class': Blockly.RTL ? 'blocklyResizeSW' : 'blocklyResizeSE'},
      this.bubbleGroup_);
  var resizeSize = 2 * Blockly.Comment.BORDER_WIDTH;
  Blockly.createSvgElement('polygon',
      {points: '0,x x,x x,0'.replace(/x/g, resizeSize)}, this.resizeGroup_);
  Blockly.createSvgElement('line',
      {'class': 'blocklyResizeLine',
      x1: resizeSize / 3, y1: resizeSize - 1,
      x2: resizeSize - 1, y2: resizeSize / 3}, this.resizeGroup_);
  Blockly.createSvgElement('line',
      {'class': 'blocklyResizeLine',
      x1: resizeSize * 2 / 3, y1: resizeSize - 1,
      x2: resizeSize - 1, y2: resizeSize * 2 / 3}, this.resizeGroup_);
  this.foreignObject_ = Blockly.createSvgElement('foreignObject',
      {x: Blockly.Comment.BORDER_WIDTH, y: Blockly.Comment.BORDER_WIDTH},
      this.bubbleGroup_);
  var body = Blockly.svgDoc.createElementNS(Blockly.HTML_NS, 'body');
  body.setAttribute('xmlns', Blockly.HTML_NS);
  body.className = 'blocklyMinimalBody';
  this.textarea_ = Blockly.svgDoc.createElementNS(Blockly.HTML_NS, 'textarea');
  this.textarea_.className = 'blocklyCommentTextarea';
  this.textarea_.setAttribute('dir', Blockly.RTL ? 'RTL' : 'LTR');
  body.appendChild(this.textarea_);
  this.foreignObject_.appendChild(body);

  this.setBubbleSize(this.width_, this.height_);

  commentGroup.appendChild(this.bubbleGroup_);

  Blockly.bindEvent_(this.bubbleBack_, 'mousedown', this,
                     this.bubbleMouseDown_);
  Blockly.bindEvent_(this.resizeGroup_, 'mousedown', this,
                     this.resizeMouseDown_);
  Blockly.bindEvent_(this.foreignObject_, 'mousedown', this,
                     Blockly.noEvent);
  Blockly.bindEvent_(this.textarea_, 'mouseup', this,
                     this.textareaFocus_);
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
  this.setVisible_(pinned);
};

/**
 * Is the comment bubble visible?
 * @return {boolean} True if the bubble is visible.
 * @private
 */
Blockly.Comment.prototype.isVisible_ = function() {
  return this.bubbleGroup_.style.display != 'none';
};

/**
 * Show or hide the comment bubble.
 * @param {boolean} visible True if the bubble should be visible.
 * @private
 */
Blockly.Comment.prototype.setVisible_ = function(visible) {
  this.bubbleGroup_.style.display = visible ? '' : 'none';
  if (visible) {
    // Rendering was disabled while it was invisible.
    // Rerender to pick up any changes.
    this.positionBubble_();
  }
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
 * Handle a mouse-down on comment bubble.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.Comment.prototype.bubbleMouseDown_ = function(e) {
  this.promote_();
  Blockly.Comment.unbindDragEvents_();
  if (e.button == 2) {
    // Right-click.
    return;
  } else if (Blockly.isTargetInput_(e)) {
    // When focused on an HTML text input widget, don't trap any events.
    return;
  }
  // Left-click (or middle click)
  Blockly.setCursorHand_(true);
  // Record the starting offset between the current location and the mouse.
  if (Blockly.RTL) {
    this.dragDeltaX = this.relativeLeft_ + e.clientX;
  } else {
    this.dragDeltaX = this.relativeLeft_ - e.clientX;
  }
  this.dragDeltaY = this.relativeTop_ - e.clientY;

  Blockly.Comment.onMouseUpWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
      'mouseup', this, Blockly.Comment.unbindDragEvents_);
  Blockly.Comment.onMouseMoveWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
      'mousemove', this, this.bubbleMouseMove_);
  Blockly.hideChaff();
  // This event has been handled.  No need to bubble up to the document.
  e.stopPropagation();
};

/**
 * Drag this comment to follow the mouse.
 * @param {!Event} e Mouse move event.
 * @private
 */
Blockly.Comment.prototype.bubbleMouseMove_ = function(e) {
  if (Blockly.RTL) {
    this.relativeLeft_ = this.dragDeltaX - e.clientX;
  } else {
    this.relativeLeft_ = this.dragDeltaX + e.clientX;
  }
  this.relativeTop_ = this.dragDeltaY + e.clientY;
  this.positionBubble_();
  this.renderArrow_();
};

/**
 * Handle a mouse-down on comment bubble's resize corner.
 * @param {!Event} e Mouse down event.
 * @private
 */
Blockly.Comment.prototype.resizeMouseDown_ = function(e) {
  this.promote_();
  Blockly.Comment.unbindDragEvents_();
  if (e.button == 2) {
    // Right-click.
    return;
  }
  // Left-click (or middle click)
  Blockly.setCursorHand_(true);
  // Record the starting offset between the current location and the mouse.
  if (Blockly.RTL) {
    this.resizeDeltaWidth = this.width_ + e.clientX;
  } else {
    this.resizeDeltaWidth = this.width_ - e.clientX;
  }
  this.resizeDeltaHeight = this.height_ - e.clientY;

  Blockly.Comment.onMouseUpWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
      'mouseup', this, Blockly.Comment.unbindDragEvents_);
  Blockly.Comment.onMouseMoveWrapper_ = Blockly.bindEvent_(Blockly.svgDoc,
      'mousemove', this, this.resizeMouseMove_);
  Blockly.hideChaff();
  // This event has been handled.  No need to bubble up to the document.
  e.stopPropagation();
};

/**
 * Resize this comment to follow the mouse.
 * @param {!Event} e Mouse move event.
 * @private
 */
Blockly.Comment.prototype.resizeMouseMove_ = function(e) {
  var w = this.resizeDeltaWidth;
  var h = this.resizeDeltaHeight + e.clientY;
  if (Blockly.RTL) {
    // RTL drags the bottom-left corner.
    w -= e.clientX;
  } else {
    // LTR drags the bottom-right corner.
    w += e.clientX;
  }
  this.setBubbleSize(w, h);
  if (Blockly.RTL) {
    // RTL requires the bubble to move its left edge.
    this.positionBubble_();
  }
};

/**
 * Bring the comment to the top of the stack when clicked on.
 * @param {!Event} e Mouse up event.
 * @private
 */
Blockly.Comment.prototype.textareaFocus_ = function(e) {
  // Ideally this would be hooked to the focus event for the comment.
  // However doing so in Firefox swallows the cursor for unkown reasons.
  // So this is hooked to mouseup instead.  No big deal.
  this.promote_();
  // Since the act of moving this node within the DOM causes a loss of focus,
  // we need to reapply the focus.
  this.textarea_.focus();
};

/**
 * Move this comment to the top of the stack.
 * @private
 */
Blockly.Comment.prototype.promote_ = function() {
  var commentGroup = this.bubbleGroup_.parentNode;
  commentGroup.appendChild(this.bubbleGroup_);
};

/**
 * Get the location of this comment's bubble.
 * @return {!Object} Object with x and y properties.
 */
Blockly.Comment.prototype.getBubbleLocation = function() {
  return {x: this.relativeLeft_, y: this.relativeTop_};
};

/**
 * Set the location of this comment's bubble.
 * @param {number} x Horizontal offset from block.
 * @param {number} y Vertical offset from block.
 */
Blockly.Comment.prototype.setBubbleLocation = function(x, y) {
  this.relativeLeft_ = x;
  this.relativeTop_ = y;
  this.positionBubble_();
};

/**
 * Move the comment bubble to a location relative to the icon's centre.
 * @private
 */
Blockly.Comment.prototype.positionBubble_ = function() {
  if (!this.isVisible_() || this.iconX_ === null || this.iconY_ === null) {
    // Comment bubble invisible or host block hasn't rendered yet.
    return;
  }
  var left;
  if (Blockly.RTL) {
    left = this.iconX_ - this.relativeLeft_ - this.width_;
  } else {
    left = this.iconX_ + this.relativeLeft_;
  }
  var top = this.relativeTop_ + this.iconY_;
  this.bubbleGroup_.setAttribute('transform',
      'translate(' + left + ', ' + top + ')');
};

/**
 * Get the dimensions of this comment's bubble.
 * @return {!Object} Object with width and height properties.
 */
Blockly.Comment.prototype.getBubbleSize = function() {
  return {width: this.width_, height: this.height_};
};

/**
 * Size this comment's bubble.
 * @param {number} width Width of the bubble.
 * @param {number} height Height of the bubble.
 */
Blockly.Comment.prototype.setBubbleSize = function(width, height) {
  var doubleBorderWidth = 2 * Blockly.Comment.BORDER_WIDTH;
  width = Math.max(width, doubleBorderWidth + 45);
  height = Math.max(height, doubleBorderWidth + 18);
  this.width_ = width;
  this.height_ = height;
  this.bubbleBack_.setAttribute('width', width);
  this.bubbleBack_.setAttribute('height', height);
  if (Blockly.RTL) {
    // Mirror the resize group.
    var resizeSize = 2 * Blockly.Comment.BORDER_WIDTH;
    this.resizeGroup_.setAttribute('transform', 'translate(' +
        resizeSize + ', ' +
        (height - doubleBorderWidth) + ') scale(-1 1)');
  } else {
    this.resizeGroup_.setAttribute('transform', 'translate(' +
        (width - doubleBorderWidth) + ', ' +
        (height - doubleBorderWidth) + ')');
  }
  this.foreignObject_.setAttribute('width', width - doubleBorderWidth);
  this.foreignObject_.setAttribute('height', height - doubleBorderWidth);
  this.textarea_.style.width = (width - doubleBorderWidth - 4) + 'px';
  this.textarea_.style.height = (height - doubleBorderWidth - 4) + 'px';
  this.renderArrow_();
};

/**
 * Draw the arrow between the comment bubble and the block.
 * @private
 */
Blockly.Comment.prototype.renderArrow_ = function() {
  if (!this.isVisible_()) {
    return;
  }
  var steps = [];
  // Find the relative coordinates of the center of the bubble.
  var relBubbleX = this.width_ / 2;
  var relBubbleY = this.height_ / 2;
  // Find the relative coordinates of the center of the icon.
  var relIconX = -this.relativeLeft_;
  var relIconY = -this.relativeTop_;
  if (relBubbleX == relIconX && relBubbleY == relIconY) {
    // Null case.  Bubble is directly on top of the icon.
    // Short circuit this rather than wade through divide by zeros.
    steps.push('M ' + relBubbleX + ',' + relBubbleY);
  } else {
    // Compute the angle of the arrow's line.
    var rise = relIconY - relBubbleY;
    var run = relIconX - relBubbleX;
    if (Blockly.RTL) {
      run *= -1;
    }
    var hypotenuse = Math.sqrt(rise * rise + run * run);
    var angle = Math.acos(run / hypotenuse);
    if (rise < 0) {
      angle = 2 * Math.PI - angle;
    }
    // Compute a line perpendicular to the arrow.
    var rightAngle = angle + Math.PI / 2;
    if (rightAngle > Math.PI * 2) {
      rightAngle -= Math.PI * 2;
    }
    var rightRise = Math.sin(rightAngle);
    var rightRun = Math.cos(rightAngle);

    // Calculate the thickness of the base of the arrow.
    var bubbleSize = this.getBubbleSize();
    var thickness = (bubbleSize.width + bubbleSize.height) /
                    Blockly.Comment.ARROW_THICKNESS;
    thickness = Math.min(thickness, bubbleSize.width, bubbleSize.height) / 2;

    // Back the tip of the arrow off of the icon.
    var backoffRatio = 1 - Blockly.Comment.ICON_RADIUS / hypotenuse;
    relIconX = relBubbleX + backoffRatio * run;
    relIconY = relBubbleY + backoffRatio * rise;

    // Coordinates for the base of the arrow.
    var baseX1 = relBubbleX + thickness * rightRun;
    var baseY1 = relBubbleY + thickness * rightRise;
    var baseX2 = relBubbleX - thickness * rightRun;
    var baseY2 = relBubbleY - thickness * rightRise;

    // Distortion to curve the arrow.
    var swirlAngle = angle + this.arrow_radians_;
    if (swirlAngle > Math.PI * 2) {
      swirlAngle -= Math.PI * 2;
    }
    var swirlRise = Math.sin(swirlAngle) *
        hypotenuse / Blockly.Comment.ARROW_BEND;
    var swirlRun = Math.cos(swirlAngle) *
        hypotenuse / Blockly.Comment.ARROW_BEND;

    steps.push('M' + baseX1 + ',' + baseY1);
    steps.push('C' + (baseX1 + swirlRun) + ',' + (baseY1 + swirlRise) +
               ' ' + relIconX + ',' + relIconY +
               ' ' + relIconX + ',' + relIconY);
    steps.push('C' + relIconX + ',' + relIconY +
               ' ' + (baseX2 + swirlRun) + ',' + (baseY2 + swirlRise) +
               ' ' + baseX2 + ',' + baseY2);
  }
  steps.push('z');
  this.bubbleArrow_.setAttribute('d', steps.join(' '));
};

/**
 * Returns this comment's text.
 * @return {string} Comment text.
 */
Blockly.Comment.prototype.getText = function() {
  return this.textarea_.value;
};

/**
 * Set this comment's text.
 * @param {string} text Comment text.
 */
Blockly.Comment.prototype.setText = function(text) {
  this.textarea_.value = text;
};

/**
 * Change the colour of a comment to match its block.
 */
Blockly.Comment.prototype.updateColour = function() {
  var hexColour = Blockly.makeColour(this.block_.getColour());
  this.bubbleBack_.setAttribute('fill', hexColour);
  this.bubbleArrow_.setAttribute('fill', hexColour);
};

/**
 * Destroy this comment.
 */
Blockly.Comment.prototype.destroy = function() {
  Blockly.Comment.unbindDragEvents_();
  // Destroy and unlink the icon.
  this.iconGroup_.parentNode.removeChild(this.iconGroup_);
  this.iconGroup_ = null;
  // Destroy and unlink the bubble.
  this.bubbleGroup_.parentNode.removeChild(this.bubbleGroup_);
  this.textarea_ = null;
  this.bubbleGroup_ = null;
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

  if (window.navigator.userAgent.indexOf('Chrome/') != -1) {
    /* HACK:
     The current version of Chrome (16.0) has a redraw bug which fails to update
     changes to the comment bubble's colour or changes to the arrow's geometry.
     Needlessly calling positionBubble_ solves this.
     If Chrome starts behaving properly with the following line commented out,
     then delete this entire hack.
    */
    this.positionBubble_();
  }
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
  this.positionBubble_();
};

/**
 * Notification that the icon has moved, but we don't really know where.
 * Recompute the icon's location from sratch.
 */
Blockly.Comment.prototype.computeIconLocation = function() {
  // Find coordinates for the centre of the icon and update the arrow.
  var blockXY = this.block_.getRelativeToSurfaceXY();
  var iconXY = Blockly.getRelativeXY_(this.iconGroup_);
  var newX = blockXY.x + iconXY.x + Blockly.Comment.ICON_RADIUS;
  var newY = blockXY.y + iconXY.y + Blockly.Comment.ICON_RADIUS;
  if (newX !== this.iconX_ || newY !== this.iconY_) {
    this.iconX_ = newX;
    this.iconY_ = newY;
    this.positionBubble_();
  }
};

/**
 * Returns the center of the block's icon relative to the surface.
 * @return {!Object} Object with x and y properties.
 */
Blockly.Comment.prototype.getIconLocation = function() {
  return {x: this.iconX_, y: this.iconY_};
};
