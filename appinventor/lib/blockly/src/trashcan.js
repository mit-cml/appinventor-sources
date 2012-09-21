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
 * @fileoverview Object representing a trash can icon.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a trash can.
 * @param {!Function} getMetrics A function that returns workspace's metrics.
 * @constructor
 */
Blockly.Trashcan = function(getMetrics) {
  this.getMetrics_ = getMetrics;
};

Blockly.Trashcan.prototype.BODY_URL_ = 'media/trashbody.png';
Blockly.Trashcan.prototype.LID_URL_ = 'media/trashlid.png';
Blockly.Trashcan.prototype.WIDTH_ = 47;
Blockly.Trashcan.prototype.BODY_HEIGHT_ = 45;
Blockly.Trashcan.prototype.LID_HEIGHT_ = 15;
Blockly.Trashcan.prototype.MARGIN_BOTTOM_ = 35;
Blockly.Trashcan.prototype.MARGIN_SIDE_ = 35;

/**
 * Current open/close state of the lid.
 */
Blockly.Trashcan.prototype.isOpen = false;

/**
 * The SVG group containing the trash can.
 * @type {Element}
 * @private
 */
Blockly.Trashcan.prototype.svgGroup_ = null;

/**
 * The SVG image element of the trash can body.
 * @type {Element}
 * @private
 */
Blockly.Trashcan.prototype.svgBody_ = null;

/**
 * The SVG image element of the trash can lid.
 * @type {Element}
 * @private
 */
Blockly.Trashcan.prototype.svgLid_ = null;

/**
 * Task ID of opening/closing animation.
 * @private
 */
Blockly.Trashcan.prototype.lidTask_ = 0;

/**
 * Current angle of the lid.
 * @private
 */
Blockly.Trashcan.prototype.lidAngle_ = 0;

/**
 * Left coordinate of the trash can.
 * @private
 */
Blockly.Trashcan.prototype.left_ = 0;

/**
 * Top coordinate of the trash can.
 * @private
 */
Blockly.Trashcan.prototype.top_ = 0;

/**
 * Create the trash can elements.
 * @return {!Element} The trash can's SVG group.
 */
Blockly.Trashcan.prototype.createDom = function() {
  /*
  <g filter="url(#blocklyTrashcanShadowFilter)">
    <image width="47" height="45" y="15" href="media/trashbody.png"></image>
    <image width="47" height="15" href="media/trashlid.png"></image>
  </g>
  */
  this.svgGroup_ = Blockly.createSvgElement('g',
      {filter: 'url(#blocklyTrashcanShadowFilter)'}, null);
  this.svgBody_ = Blockly.createSvgElement('image',
      {width: this.WIDTH_, height: this.BODY_HEIGHT_},
      this.svgGroup_);
  this.svgBody_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href',
      Blockly.pathToBlockly + this.BODY_URL_);
  this.svgBody_.setAttribute('y', this.LID_HEIGHT_);
  this.svgLid_ = Blockly.createSvgElement('image',
      {width: this.WIDTH_, height: this.LID_HEIGHT_},
      this.svgGroup_);
  this.svgLid_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href',
      Blockly.pathToBlockly + this.LID_URL_);
  return this.svgGroup_;
};

/**
 * Initialize the trash can.
 */
Blockly.Trashcan.prototype.init = function() {
  this.setOpen_(false);
  this.position_();
  // If the document resizes, reposition the trash can.
  Blockly.bindEvent_(window, 'resize', this, this.position_);
};

/**
 * Destroy this trash can.
 * Unlink from all DOM elements to prevent memory leaks.
 */
Blockly.Trashcan.prototype.destroy = function() {
  if (this.svgGroup_) {
    this.svgGroup_.parentNode.removeChild(this.svgGroup_);
    this.svgGroup_ = null;
  }
  this.svgBody_ = null;
  this.svgLid_ = null;
  this.getMetrics_ = null;
  window.clearTimeout(this.lidTask_);
};

/**
 * Move the trash can to the bottom-right corner.
 * @private
 */
Blockly.Trashcan.prototype.position_ = function() {
  var metrics = this.getMetrics_();
  if (!metrics) {
    // There are no metrics available (workspace is probably not visible).
    return;
  }
  if (Blockly.RTL) {
    this.left_ = this.MARGIN_SIDE_;
  } else {
    this.left_ = metrics.viewWidth + metrics.absoluteLeft -
        this.WIDTH_ - this.MARGIN_SIDE_;
  }
  this.top_ = metrics.viewHeight + metrics.absoluteTop -
      (this.BODY_HEIGHT_ + this.LID_HEIGHT_) - this.MARGIN_BOTTOM_;
  this.svgGroup_.setAttribute('transform',
      'translate(' + this.left_ + ',' + this.top_ + ')');
};

/**
 * Determines if the mouse is currently over the trash can.
 * Opens/closes the lid and sets the isOpen flag.
 * @param {!Event} e Mouse move event.
 */
Blockly.Trashcan.prototype.onMouseMove = function(e) {
  /*
  An alternative approach would be to use onMouseOver and onMouseOut events.
  However the selected block will be between the mouse and the trash can,
  thus these events won't fire.
  Another approach is to use HTML5's drag & drop API, but it's widely hated.
  Instead, we'll just have the block's drag_ function call us.
  */
  if (!this.svgGroup_) {
    return;
  }
  var xy = Blockly.getAbsoluteXY_(this.svgGroup_);
  var left = xy.x;
  var top = xy.y;
  // Measure the offset of the SVG.
  var node = Blockly.svg.parentNode;
  while (node) {
    left += node.offsetLeft;
    top += node.offsetTop;
    node = node.offsetParent;
  }
  var over = (e.clientX > left) &&
             (e.clientX < left + this.WIDTH_) &&
             (e.clientY > top) &&
             (e.clientY < top + this.BODY_HEIGHT_ + this.LID_HEIGHT_);
  // For bonus points we might want to match the trapezoidal outline.
  if (this.isOpen != over) {
    this.setOpen_(over);
  }
};

/**
 * Flip the lid open or shut.
 * @param {boolean} state True if open.
 * @private
 */
Blockly.Trashcan.prototype.setOpen_ = function(state) {
  if (this.isOpen == state) {
    return;
  }
  window.clearTimeout(this.lidTask_);
  this.isOpen = state;
  Blockly.Trashcan.animateLid_(this);
};

/**
 * Rotate the lid open or closed by one step.  Then wait and recurse.
 * @param {!Blockly.Trashcan} trashcan The instance of a trashcan to animate.
 * @private
 */
Blockly.Trashcan.animateLid_ = function(trashcan) {
  trashcan.lidAngle_ += trashcan.isOpen ? 10 : -10;
  trashcan.lidAngle_ = Math.max(0, trashcan.lidAngle_);
  trashcan.svgLid_.setAttribute('transform', 'rotate(' +
      (Blockly.RTL ? -trashcan.lidAngle_ : trashcan.lidAngle_) + ', ' +
      (Blockly.RTL ? 4 : trashcan.WIDTH_ - 4) + ', ' +
      (trashcan.LID_HEIGHT_ - 2) + ')');
  if (trashcan.isOpen ? (trashcan.lidAngle_ < 45) : (trashcan.lidAngle_ > 0)) {
    var closure = function() {
      Blockly.Trashcan.animateLid_(trashcan);
    };
    this.lidTask_ = window.setTimeout(closure, 5);
  }
};

/**
 * Flip the lid shut.
 * @param {!Blockly.Trashcan} trashcan The instance of a trashcan to animate.
 * Called externally after a drag.
 */
Blockly.Trashcan.close = function(trashcan) {
  trashcan.setOpen_(false);
};
