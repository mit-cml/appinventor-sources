// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Flydown is an abstract class for a flyout-like dropdown containing blocks.
 *   Unlike a regular flyout, for simplicity it does not support scrolling.
 *   Any non-abstract subclass must provide a flydownBlocksXML_ () method that returns an
 *   XML element whose children are blocks that should appear in the flyout.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */
'use strict';

goog.provide('Blockly.Flydown');

goog.require('Blockly.Flyout');
goog.require('Blockly.Block');
goog.require('Blockly.Comment');

/**
 * Class for a flydown.
 * @constructor
 */
Blockly.Flydown = function() {
  Blockly.Flydown.superClass_.constructor.call(this);
 };
goog.inherits(Blockly.Flydown, Blockly.Flyout);

/**
 * Previous CSS class for this flydown
 * @type {number}
 * @const
 */
Blockly.Flydown.prototype.previousCSSClassName_ = '';

/**
 * Override flyout factor to be smaller for flydowns
 * @type {number}
 * @const
 */
Blockly.Flydown.prototype.VERTICAL_SEPARATION_FACTOR = 1;

/**
 * Creates the flydown's DOM.  Only needs to be called once.  Overrides the flyout createDom method.
 * @param {!String} cssClassName The name of the CSS class for this flydown. 
 * @return {!Element} The flydown's SVG group.
 */
Blockly.Flydown.prototype.createDom = function(cssClassName) {
  /*
  <g>
    <path class={cssClassName}/>
    <g></g>
  </g>
  */
  this.previousCSSClassName_ = cssClassName; // Remember class name for later
  this.svgGroup_ = Blockly.createSvgElement('g', {'class': cssClassName}, null);
  this.svgBackground_ = Blockly.createSvgElement('path', {}, this.svgGroup_);
  this.svgGroup_.appendChild(this.workspace_.createDom());
  return this.svgGroup_;
};

/**
 * Set the CSS class of the flydown SVG group. Need to remove previous class if there is one.
 * @param {!String} newCSSClassName The name of the new CSS class replacing the old one
 */
Blockly.Flydown.prototype.setCSSClass = function(newCSSClassName) {
  if (newCSSClassName !== this.previousCSSClassName_) {
    Blockly.removeClass_(this.svgGroup_, this.previousCSSClassName_);
    Blockly.addClass_(this.svgGroup_, newCSSClassName);
    this.previousCSSClassName_ = newCSSClassName;
  }
}

/**
 * Initializes the Flydown.
 * @param {!Blockly.Workspace} workspace The workspace in which to create new
 *     blocks.
 */
Blockly.Flydown.prototype.init = function(workspace) {
  Blockly.Flyout.prototype.init.call(this, workspace, false); // Flydowns have no scrollbar
}

/**
 * Override the flyout position_ method to do nothing instead
 * @private
 */
Blockly.Flydown.prototype.position_ = function() {
  return;
}

/**
 * Show and populate the flydown.
 * @param {!Array|string} xmlList List of blocks to show.
 * @param {!num} x x-position of upper-left corner of flydown
 * @param {!num} y y-position of upper-left corner of flydown
 */
Blockly.Flydown.prototype.showAt = function(xmlList,x,y) {
  this.show(xmlList); // invoke flyout method, which adds blocks to flydown and calculates width and height.
  // this.svgGroup_.setAttribute('transform', 'translate(' + x + ',' + y + ')');
  // Calculate path around flydown blocks. Based on code in flyout position_ method.

  // Start at bottom of top left arc and proceed clockwise
  // Flydown outline shape is symmetric about vertical axis, so no need to differentiate LTR and RTL paths.
  var margin = this.CORNER_RADIUS;
  var edgeWidth = this.width_ - 2*margin;
  var edgeHeight = this.height_ - 2*margin;
  var path = ['M 0,' + margin];
  path.push('a', margin, margin, 0, 0, 1, margin, -margin); // upper left arc
  path.push('h', edgeWidth);  // top edge
  path.push('a', margin, margin, 0, 0, 1, margin, margin); // upper right arc
  path.push('v', edgeHeight); // right edge
  path.push('a', margin, margin, 0, 0, 1, -margin, margin); // bottom right arc
  path.push('h', -edgeWidth); // bottom edge, drawn backwards
  path.push('a', margin, margin, 0, 0, 1, -margin, -margin); // bottom left arc
  path.push('z'); // complete path by drawing left edge
  this.svgBackground_.setAttribute('d', path.join(' '));
  this.svgGroup_.setAttribute('transform', 'translate(' + x + ', ' + y + ')');
}

/**
 * Compute width and height of Flydown.  Position button under each block.
 * Overrides the reflow method of flyout
 * For RTL: Lay out the blocks right-aligned.
 */
Blockly.Flydown.prototype.reflow = function() {
  var flydownWidth = 0;
  var flydownHeight = 0;
  var margin = this.CORNER_RADIUS;
  var blocks = this.workspace_.getTopBlocks(false);
  for (var i = 0, block; block = blocks[i]; i++) {
    var root = block.getSvgRoot();
    var blockHW = block.getHeightWidth();
    flydownWidth = Math.max(flydownWidth, blockHW.width);
    flydownHeight += blockHW.height;
  }
  flydownWidth += 2*margin + Blockly.BlockSvg.TAB_WIDTH; // TAB_WIDTH is with of plug
  flydownHeight += 2*margin + margin*this.VERTICAL_SEPARATION_FACTOR*(blocks.length - 1);
  if (this.width_ != flydownWidth) {
    for (var j = 0, block; block = blocks[j]; j++) {
      var blockHW = block.getHeightWidth();
      var blockXY = block.getRelativeToSurfaceXY();
      if (Blockly.RTL) {
        // With the FlydownWidth known, right-align the blocks.
        var dx = flydownWidth - margin - Blockly.BlockSvg.TAB_WIDTH - blockXY.x;
        block.moveBy(dx, 0);
        blockXY.x += dx;
      }
      if (block.FlyoutRect_) {
        block.FlyoutRect_.setAttribute('width', blockHW.width);
        block.FlyoutRect_.setAttribute('height', blockHW.height);
        block.FlyoutRect_.setAttribute('x',
            Blockly.RTL ? blockXY.x - blockHW.width : blockXY.x);
        block.FlyoutRect_.setAttribute('y', blockXY.y);
      }
    }
    // Record the width for us in showAt method
    this.width_ = flydownWidth;
    this.height_ = flydownHeight;
  }
};

// Note: nothing additional beyond flyout disposal needs to be done to dispose of a flydown.
