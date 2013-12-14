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

'use strict';

goog.provide('Blockly.Zoom');

Blockly.Zoom = function(workspace) {
  this.workspace_ = workspace;
};

Blockly.Zoom.prototype.ZOOM_IN_URL_ = 'media/zoomin.png';

Blockly.Zoom.prototype.ZOOM_OUT_URL_ = 'media/zoomout.png';

Blockly.Zoom.prototype.WIDTH_ = 32;

Blockly.Zoom.prototype.HEIGHT_ = 32;

Blockly.Zoom.prototype.MARGIN_IN_OUT_ = 48;

Blockly.Zoom.prototype.MARGIN_TOP_ = 32;

Blockly.Zoom.prototype.MARGIN_SIDE_ = 96;

Blockly.Zoom.prototype.svgGroup_ = null;

Blockly.Zoom.prototype.zoomIn_ = null;

Blockly.Zoom.prototype.zoomOut_ = null;

Blockly.Zoom.prototype.left_ = 0;

Blockly.Zoom.prototype.top_ = 0;

Blockly.Zoom.prototype.createDom = function() {
  /*
  <g>
    <image width="32" height="32" href="media/zoomin.png"></image>
    <image width="32" height="32" x="64" href="media/zoomout.png"></image>
  </g>
  */
  this.svgGroup_ = Blockly.createSvgElement('g');
  this.zoomIn_ = Blockly.createSvgElement('image',
      {'width': this.WIDTH_, 'height': this.HEIGHT_},
      this.svgGroup_);
  this.zoomIn_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href',
      Blockly.pathToBlockly + this.ZOOM_IN_URL_);

  this.zoomOut_ = Blockly.createSvgElement('image',
      {'width': this.WIDTH_, 'height': this.HEIGHT_},
      this.svgGroup_);
  this.zoomOut_.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href',
      Blockly.pathToBlockly + this.ZOOM_OUT_URL_);
  this.zoomOut_.setAttribute('x', this.MARGIN_IN_OUT_);
  return this.svgGroup_;
};

/**
 * Initialize the zoom icons.
 */
Blockly.Zoom.prototype.init = function() {
  this.position_();
  // If the document resizes, reposition the zoom in and out icons.
  Blockly.bindEvent_(window, 'resize', this, this.position_);
  Blockly.bindEvent_(this.svgGroup_, 'mouseup', this, this.onClick_);
};

/**
 * Dispose of this zoom icons.
 * Unlink from all DOM elements to prevent memory leaks.
 */
Blockly.Zoom.prototype.dispose = function() {
  if (this.svgGroup_) {
    goog.dom.removeNode(this.svgGroup_);
    this.svgGroup_ = null;
  }
  this.zoomIn_ = null;
  this.zoomOut_ = null;
  this.workspace_ = null;
};

/**
 * Move the trash can to the bottom-right corner.
 * @private
 */
Blockly.Zoom.prototype.position_ = function() {
  var metrics = this.workspace_.getMetrics();
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
  this.top_ = metrics.absoluteTop + this.MARGIN_TOP_;
  this.svgGroup_.setAttribute('transform',
      'translate(' + this.left_ + ',' + this.top_ + ')');
};

/**
 * Determines if the mouse is currently over the trash can.
 * Opens/closes the lid and sets the isOpen flag.
 * @param {!Event} e Mouse move event.
 */
Blockly.Zoom.prototype.onClick_ = function(e) {
  if (!this.svgGroup_) {
    return;
  }
  var mouseXY = Blockly.mouseToSvg(e);
  var zoomXY = Blockly.getSvgXY_(this.svgGroup_);
  var overIn = (mouseXY.x > zoomXY.x) &&
             (mouseXY.x < zoomXY.x + this.WIDTH_) &&
             (mouseXY.y > zoomXY.y) &&
             (mouseXY.y < zoomXY.y + this.HEIGHT_);
  var overOut = (mouseXY.x > zoomXY.x + this.MARGIN_IN_OUT_) &&
             (mouseXY.x < zoomXY.x + this.WIDTH_ + this.MARGIN_IN_OUT_) &&
             (mouseXY.y > zoomXY.y) &&
             (mouseXY.y < zoomXY.y + this.HEIGHT_);
  var metrics = Blockly.getMainWorkspaceMetrics_();
  if (overIn && Blockly.WORKSPACE_SCALE < 3.5) {
    Blockly.WORKSPACE_SCALE *= 1.25;
  }
  if (overOut && Blockly.WORKSPACE_SCALE > 0.3) {
    Blockly.WORKSPACE_SCALE /= 1.25;
  }
  var translation = 'translate(' +
      (Blockly.mainWorkspace.scrollX + metrics.absoluteLeft /*- metrics.viewWidth*(1-Blockly.WORKSPACE_SCALE)/2*/) + ',' +
      (Blockly.mainWorkspace.scrollY + metrics.absoluteTop /*- metrics.viewHeight*(1-Blockly.WORKSPACE_SCALE)/2*/) + ') scale('+Blockly.WORKSPACE_SCALE+')';
  Blockly.mainWorkspace.getCanvas().setAttribute('transform', translation);
  Blockly.svgResize();
};
