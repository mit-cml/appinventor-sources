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
 * @fileoverview Flyout tray containing blocks which may be created.
 * In the interests of a consistent UI, the toolbox shares some functions and
 * properties with the context menu.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a flyout.
 * @constructor
 */
Blockly.Flyout = function() {
  this.workspace_ = new Blockly.Workspace(false);
};

/**
 * Corner radius of the flyout background.
 */
Blockly.Flyout.prototype.CORNER_RADIUS = 8;

/**
 * Creates the flyout's DOM.  Only needs to be called once.
 * @return {!Element} The flyout's SVG group.
 */
Blockly.Flyout.prototype.createDom = function() {
  /*
  <g>
    <path class="blocklyFlyoutBackground"/>
    <g></g>
  </g>
  */
  this.svgGroup_ = Blockly.createSvgElement('g', {}, null);
  this.svgBackground_ = Blockly.createSvgElement('path',
      {'class': 'blocklyFlyoutBackground'}, this.svgGroup_);
  this.svgOptions_ = Blockly.createSvgElement('g', {}, this.svgGroup_);
  this.svgOptions_.appendChild(this.workspace_.createDom());
  return this.svgGroup_;
};

/**
 * Return an object with all the metrics required to size scrollbars for the
 * flyout.  The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .viewWidth: Width of the visible rectangle,
 * .contentHeight: Height of the contents,
 * .viewTop: Offset of top edge of visible rectangle from parent,
 * .contentTop: Offset of the top-most content from the y=0 coordinate,
 * .absoluteTop: Top-edge of view.
 * .absoluteLeft: Left-edge of view.
 * @return {Object} Contains size and position metrics of the flyout.
 */
Blockly.Flyout.prototype.getMetrics = function() {
  if (this.svgGroup_.style.display != 'block') {
    // Flyout is hidden.
    return null;
  }
  var viewHeight = this.height_ - 2 * this.CORNER_RADIUS;
  var viewWidth = this.width_;
  var optionBox = this.svgOptions_.getBBox();
  return {
    viewHeight: viewHeight,
    viewWidth: viewWidth,
    contentHeight: optionBox.height + optionBox.y,
    viewTop: -this.svgOptions_.scrollY,
    contentTop: 0,
    absoluteTop: this.CORNER_RADIUS,
    absoluteLeft: 0
  };
};

/**
 * Sets the Y translation of the flyout to match the scrollbars.
 * @param {!Object} yRatio Contains a y property which is a float
 *     between 0 and 1 specifying the degree of scrolling.
 */
Blockly.Flyout.prototype.setMetrics = function(yRatio) {
  var metrics = this.getMetrics();
  if (typeof yRatio.y == 'number') {
    this.svgOptions_.scrollY =
        -metrics.contentHeight * yRatio.y - metrics.contentTop;
  }
  var y = this.svgOptions_.scrollY + metrics.absoluteTop;
  this.svgOptions_.setAttribute('transform', 'translate(0,' + y + ')');
};

/**
 * Initializes the flyout.
 * @param {!Blockly.Workspace} workspace The workspace in which to create new
 *     blocks.
 * @param {!Function} workspaceMetrics Function which returns size information
 *     regarding the flyout's target workspace.
 */
Blockly.Flyout.prototype.init = function(workspace, workspaceMetrics) {
  this.targetWorkspace_ = workspace;
  this.targetWorkspaceMetrics_ = workspaceMetrics;
  // Add scrollbars.
  this.width_ = 0;
  var flyout = this;
  new Blockly.Scrollbar(this.svgOptions_,
      function() {return flyout.getMetrics()},
      function(ratio) {return flyout.setMetrics(ratio)},
      false, false);

  // List of background buttons that lurk behind each block to catch clicks
  // landing in the blocks' lakes and bays.
  this.buttons_ = [];

  this.position_();

  // If the document resizes, reposition the toolbox.
  Blockly.bindEvent_(window, 'resize', this, this.position_);
};

/**
 * Move the toolbox to the edge of the workspace.
 * @private
 */
Blockly.Flyout.prototype.position_ = function() {
  var metrics = this.targetWorkspaceMetrics_();
  if (!metrics) {
    // Hidden components will return null.
    return;
  }
  var edgeWidth = this.width_ - this.CORNER_RADIUS;
  if (Blockly.RTL) {
    edgeWidth *= -1;
  }
  var path = ['M ' + (Blockly.RTL ? this.width_ : 0) + ',0'];
  path.push('h', edgeWidth);
  path.push('a', this.CORNER_RADIUS, this.CORNER_RADIUS, 0, 0,
      Blockly.RTL ? 0 : 1,
      Blockly.RTL ? -this.CORNER_RADIUS : this.CORNER_RADIUS,
      this.CORNER_RADIUS);
  path.push('v', Math.max(0, metrics.viewHeight - this.CORNER_RADIUS * 2));
  path.push('a', this.CORNER_RADIUS, this.CORNER_RADIUS, 0, 0,
      Blockly.RTL ? 0 : 1,
      Blockly.RTL ? this.CORNER_RADIUS : -this.CORNER_RADIUS,
      this.CORNER_RADIUS);
  path.push('h', -edgeWidth);
  path.push('z');
  this.svgBackground_.setAttribute('d', path.join(' '));
  var x = metrics.absoluteLeft;
  if (Blockly.RTL) {
    x -= this.width_;
  }
  this.svgGroup_.setAttribute('transform',
      'translate(' + x + ',' + metrics.absoluteTop + ')');

  // Record the height for Blockly.Flyout.getMetrics.
  this.height_ = metrics.viewHeight;
};

/**
 * Hide and empty the flyout.
 */
Blockly.Flyout.prototype.hide = function() {
  this.svgGroup_.style.display = 'none';
  // Delete all the blocks.
  var blocks = this.workspace_.getTopBlocks();
  for (var x = 0, block; block = blocks[x]; x++) {
    block.destroy();
  }
  // Delete all the background buttons.
  for (var x = 0, rect; rect = this.buttons_[x];
       x++) {
    Blockly.unbindEvent_(rect, 'mousedown', rect.wrapper_);
    rect.parentNode.removeChild(rect);
  }
  this.buttons_ = [];
};

/**
 * Show and populate the flyout.
 * @param {!Array.<string>|string} names List of type names of blocks to show.
 *     Or 'variables' for a custom list of variables.
 */
Blockly.Flyout.prototype.show = function(names) {
  var margin = this.CORNER_RADIUS;
  this.svgGroup_.style.display = 'block';

  // Create the blocks to be shown in this flyout.
  var blocks = [];
  var gaps = [];
  if (names == Blockly.Toolbox.VARIABLE_CAT) {
    // Special category for variables.
    var variableList = Blockly.Variables.allVariables();
    variableList.sort(Blockly.caseInsensitiveComparator);
    // In addition to the user's variables, we also want to display the default
    // variable name at the top.  We also don't want this duplicated if the
    // user has created a variable of the same name.
    variableList.unshift(null);
    var defaultVariable = undefined;
    for (var i = 0; i < variableList.length; i++) {
      if (variableList[i] === defaultVariable) {
        continue;
      }
      var getBlock = new Blockly.Block(this.workspace_, 'variables_set');
      var setBlock = new Blockly.Block(this.workspace_, 'variables_get');
      if (variableList[i] === null) {
        defaultVariable = getBlock.getTitleText(1);
      } else {
        getBlock.setTitleText(variableList[i], 1);
        setBlock.setTitleText(variableList[i], 1);
      }
      blocks.push(getBlock, setBlock);
      gaps.push(margin, margin * 3);
    }
  } else {
    for (var i = 0, name; name = names[i]; i++) {
      var block = new Blockly.Block(this.workspace_, name);
      blocks[i] = block;
      gaps[i] = margin * 2;
    }
  }

  // Lay out the blocks vertically.
  var flyoutWidth = 0;
  var cursorY = margin;
  for (var i = 0, block; block = blocks[i]; i++) {
    // Mark blocks as being inside a flyout.  This is used to detect and prevent
    // the closure of the flyout if the user right-clicks on such a block.
    block.isInFlyout = true;
    // There is no good way to handle comment bubbles inside the flyout.
    // Blocks shouldn't come with predefined comments, but someone will
    // try this, I'm sure.  Kill the comment.
    Blockly.Comment && block.setCommentText(null);
    block.render();
    var bBox = block.svg_.svgGroup_.getBBox();
    var x = Blockly.RTL ? 0 : margin + Blockly.BlockSvg.TAB_WIDTH;
    block.moveBy(x, cursorY);
    flyoutWidth = Math.max(flyoutWidth, bBox.width);
    cursorY += bBox.height + gaps[i];
    Blockly.bindEvent_(block.svg_.svgGroup_, 'mousedown', null,
                       Blockly.Flyout.createBlockFunc_(this, block));
  }
  flyoutWidth += margin + Blockly.BlockSvg.TAB_WIDTH + margin / 2 +
                 Blockly.Scrollbar.scrollbarThickness;

  for (var i = 0, block; block = blocks[i]; i++) {
    if (Blockly.RTL) {
      // With the flyoutWidth known, reposition the blocks to the right-aligned.
      block.moveBy(flyoutWidth - margin - Blockly.BlockSvg.TAB_WIDTH, 0);
    }
    // Create an invisible rectangle over the block to act as a button.  Just
    // using the block as a button is poor, since blocks have holes in them.
    var bBox = block.svg_.svgGroup_.getBBox();
    var xy = block.getRelativeToSurfaceXY();
    var rect = Blockly.createSvgElement('rect',
        {width: bBox.width, height: bBox.height,
        x: xy.x + bBox.x, y: xy.y + bBox.y,
        'fill-opacity': 0}, null);
    // Add the rectangles under the blocks, so that the blocks' tooltips work.
    this.svgOptions_.insertBefore(rect, this.svgOptions_.firstChild);
    rect.wrapper_ = Blockly.bindEvent_(rect, 'mousedown', null,
        Blockly.Flyout.createBlockFunc_(this, block));
    this.buttons_[i] = rect;
  }
  // Record the width for .getMetrics and .position_.
  this.width_ = flyoutWidth;

  // Fire a resize event to update the flyout's scrollbar.
  Blockly.fireUiEvent(Blockly.svgDoc, window, 'resize');
};

/**
 * Create a copy of this block on the workspace.
 * @param {!Blockly.Flyout} flyout Instance of the flyout.
 * @param {!Blockly.Block} originBlock The toolbox block to copy.
 * @return {!Function} Function to call when block is clicked.
 * @private
 */
Blockly.Flyout.createBlockFunc_ = function(flyout, originBlock) {
  return function(e) {
    if (e.button == 2) {
      // Right-click.  Don't create a block, let the context menu show.
      return;
    }
    // Create the new block by cloning the block in the toolbox (via XML).
    var xml = Blockly.Xml.blockToDom_(originBlock);
    var block = Blockly.Xml.domToBlock_(flyout.targetWorkspace_, xml);
    // Place it in the same spot as the toolbox copy.
    var xyOld = Blockly.getAbsoluteXY_(originBlock.svg_.svgGroup_);
    var xyNew = Blockly.getAbsoluteXY_(flyout.targetWorkspace_.getCanvas());
    block.moveBy(xyOld.x - xyNew.x, xyOld.y - xyNew.y);
    block.render();
    // Start a dragging operation on the new block.
    block.onMouseDown_(e);
  };
};
