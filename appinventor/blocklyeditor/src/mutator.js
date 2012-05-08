/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
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
 * @fileoverview Object representing a mutator dialog.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a mutator dialog.
 * @param {!Blockly.Block} block The block associated with this mutator.
 * @constructor
 */
Blockly.Mutator = function(block) {
  this.block_ = block;
  this.createIcon_(block);
};

/**
 * Height and width of the mutator icon.
 */
Blockly.Mutator.ICON_SIZE = 16;

/**
 * Is the mutator dialog open?
 */
Blockly.Mutator.isOpen = false;

/**
 * Disassemble the mutator icon to avoid memory leaks.
 */
Blockly.Mutator.prototype.destroy = function() {
  // Destroy and unlink the icon.
  this.iconGroup_.parentNode.removeChild(this.iconGroup_);
  this.iconGroup_ = null;
  // Disconnect links between the block and the mutator.
  this.block_.mutator = null;
  this.block_ = null;
};

/**
 * Create the icon on the block.
 * @param {!Blockly.Block} block The block associated with this comment.
 * @private
 */
Blockly.Mutator.prototype.createIcon_ = function(block) {
  /* Here's the markup that will be generated:
  <g class="blocklyIconGroup">
    <rect class="blocklyIconShield" width="16" height="16"/>
    <path class="blocklyMutatorMark" d="..."></path>
  </g>
  */
  var quantum = Blockly.Mutator.ICON_SIZE / 8;
  this.iconGroup_ = Blockly.createSvgElement('g', {}, null);
  if (block.editable) {
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
  var iconMark = Blockly.createSvgElement('path',
      {'class': 'blocklyMutatorMark',
      d: Blockly.Mutator.crossPath_}, this.iconGroup_);
  block.svg_.svgGroup_.appendChild(this.iconGroup_);
  if (block.editable) {
    Blockly.bindEvent_(this.iconGroup_, 'mouseup', this, this.onMouseUp_);
  }

  if (Blockly.Tooltip) {
    this.tooltip = Blockly.MSG_MUTATOR_TOOLTIP;
    iconShield.tooltip = this;
    iconMark.tooltip = this;
    Blockly.Tooltip.bindMouseEvents(iconShield);
    Blockly.Tooltip.bindMouseEvents(iconMark);
  }
};

/**
 * Render the icon for this comment.
 * @param {number} titleX Horizontal offset at which to position the icon.
 * @return {number} Width of icon.
 */
Blockly.Mutator.prototype.renderIcon = function(titleX) {
  var TOP_MARGIN = 5;
  var diameter = 2 * Blockly.Comment.ICON_RADIUS;
  if (Blockly.RTL) {
    titleX -= diameter;
  }
  this.iconGroup_.setAttribute('transform',
      'translate(' + titleX + ', ' + TOP_MARGIN + ')');
  return diameter;
};

/**
 * Clicking on the icon displays the dialog.
 * @param {!Event} e Mouse click event.
 * @private
 */
Blockly.Mutator.prototype.onMouseUp_ = function(e) {
  if (e.button == 2) {
    // Right-click.
    return;
  } else if (Blockly.Block.dragMode_ == 2) {
    // Drag operation is concluding.  Don't open the editor.
    return;
  }
  Blockly.Mutator.openDialog_(this.block_);
};


/**
 * Create the mutator dialog's elements.  Only needs to be called once.
 * @return {!Element} The dialog's SVG group.
 */
Blockly.Mutator.createDom = function() {
  /*
  <g class="blocklyHidden">
    <rect class="blocklyScreenShadow" />
    <g>
      <rect class="blocklyMutatorBackground" />
      <text class="blocklyHeader" y="30">Block Editor</text>
      [Cancel button]
      [Change button]
      [Workspace]
      [Flyout]
    </g>
  </g>
  */
  var svgGroup = Blockly.createSvgElement('g', {'class': 'blocklyHidden'},
                                          null);
  Blockly.Mutator.svgGroup_ = svgGroup;
  Blockly.Mutator.svgShadow_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyScreenShadow'}, svgGroup);
  Blockly.Mutator.svgDialog_ = Blockly.createSvgElement('g', {}, svgGroup);
  Blockly.Mutator.svgBackground_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyMutatorBackground'}, Blockly.Mutator.svgDialog_);
  Blockly.Mutator.svgHeader_ = Blockly.createSvgElement('text',
      {'class': 'blocklyHeader', y: 30}, Blockly.Mutator.svgDialog_);
  var textNode = Blockly.svgDoc.createTextNode(Blockly.MSG_MUTATOR_HEADER);
  Blockly.Mutator.svgHeader_.appendChild(textNode);

  // Buttons
  Blockly.Mutator.cancelButton_ =
      new Blockly.Mutator.Button(Blockly.MSG_MUTATOR_CANCEL, false,
                                 Blockly.Mutator.closeDialog_);
  Blockly.Mutator.changeButton_ =
      new Blockly.Mutator.Button(Blockly.MSG_MUTATOR_CHANGE, true,
                                 Blockly.Mutator.closeDialog_);
  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.cancelButton_.createDom());
  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.changeButton_.createDom());

  // TODO: Move workspace and flyout instantiation into constructor, once
  // Mutator stops being a singleton.
  Blockly.Mutator.workspace_ = new Blockly.Workspace(true);
  Blockly.Mutator.flyout_ = new Blockly.Flyout();

  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.workspace_.createDom());
  Blockly.Mutator.svgDialog_.appendChild(Blockly.Mutator.flyout_.createDom());

  return svgGroup;
};

/**
 * Layout the buttons.  Only needs to be called once.
 */
Blockly.Mutator.init = function() {
  Blockly.Mutator.cancelButton_.init();
  Blockly.Mutator.changeButton_.init();
  // Save the size of the header so that calculations on its size may be
  // performed regardless of whether it is hidden or not.
  Blockly.Mutator.headerLength_ =
      Blockly.Mutator.svgHeader_.getComputedTextLength();
  Blockly.Mutator.workspace_.addTrashcan(Blockly.Mutator.getWorkspaceMetrics_);
  new Blockly.ScrollbarPair(Blockly.Mutator.workspace_.getCanvas(),
      Blockly.Mutator.getWorkspaceMetrics_,
      Blockly.Mutator.getWorkspaceMetrics_);
  Blockly.Mutator.flyout_.init(Blockly.Mutator.workspace_,
                               Blockly.Mutator.getFlyoutMetrics_);
};

/**
 * Lay out the dialog to fill the screen.
 * @private
 */
Blockly.Mutator.position_ = function() {
  var svgSize = Blockly.svgSize();
  Blockly.Mutator.svgShadow_.setAttribute('width', svgSize.width);
  Blockly.Mutator.svgShadow_.setAttribute('height', svgSize.height);
  var MARGIN = 40;
  var width = Math.max(0, svgSize.width - 2 * MARGIN);
  var height = Math.max(0, svgSize.height - 2 * MARGIN);
  Blockly.Mutator.svgDialog_.setAttribute('transform',
      'translate(' + MARGIN + ',' + MARGIN + ')');
  Blockly.Mutator.svgBackground_.setAttribute('width', width);
  Blockly.Mutator.svgBackground_.setAttribute('height', height);

  var headerX = Blockly.ContextMenu.X_PADDING;
  if (Blockly.RTL) {
    headerX = width - Blockly.Mutator.headerLength_ - headerX;
  }
  Blockly.Mutator.svgHeader_.setAttribute('x', headerX);

  var bBoxChange = Blockly.Mutator.changeButton_.getBBox();
  var bBoxCancel = Blockly.Mutator.cancelButton_.getBBox();
  var cursorX;
  if (Blockly.RTL) {
    cursorX = Blockly.ContextMenu.X_PADDING;
    Blockly.Mutator.changeButton_.setLocation(cursorX, 5);
    cursorX += bBoxChange.width + Blockly.ContextMenu.X_PADDING;
    Blockly.Mutator.cancelButton_.setLocation(cursorX, 5);
    cursorX += bBoxCancel.width;
    cursorX = headerX - cursorX;
  } else {
    var cursorX = width - Blockly.ContextMenu.X_PADDING - bBoxChange.width;
    Blockly.Mutator.changeButton_.setLocation(cursorX, 5);
    cursorX -= Blockly.ContextMenu.X_PADDING + bBoxCancel.width;
    Blockly.Mutator.cancelButton_.setLocation(cursorX, 5);
    cursorX -= headerX + Blockly.Mutator.headerLength_;
  }

  // Hide the header if the window is too small.
  Blockly.Mutator.svgHeader_.style.display = (cursorX > 0) ? 'block' : 'none';

  // Record some layout information for Blockly.Mutator.getWorkspaceMetrics_.
  Blockly.Mutator.workspaceWidth_ = width;
  Blockly.Mutator.workspaceHeight_ = height - bBoxChange.height - 10;
  Blockly.Mutator.workspaceTop_ = bBoxChange.height + 10;
  Blockly.Mutator.workspaceLeft_ = 0;
};

/**
 * Return an object with all the metrics required to size scrollbars for the
 * mutator flyout.  The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .absoluteTop: Top-edge of view.
 * .absoluteLeft: Left-edge of view.
 * @return {Object} Contains size and position metrics of mutator dialog's
 *     workspace.  Returns null if the dialog is hidden.
 * @private
 */
Blockly.Mutator.getFlyoutMetrics_ = function() {
  if (!Blockly.Mutator.isOpen) {
    return null;
  }
  var left = Blockly.Mutator.workspaceLeft_;
  if (Blockly.RTL) {
    left += Blockly.Mutator.workspaceWidth_;
  }
  return {
    viewHeight: Blockly.Mutator.workspaceHeight_,
    absoluteTop: Blockly.Mutator.workspaceTop_,
    absoluteLeft: left
  };
};

/**
 * Return an object with all the metrics required to size scrollbars for the
 * mutator dialog's workspace.  The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .viewWidth: Width of the visible rectangle,
 * .contentHeight: Height of the contents,
 * .contentWidth: Width of the content,
 * .viewTop: Offset of top edge of visible rectangle from parent,
 * .viewLeft: Offset of left edge of visible rectangle from parent,
 * .contentTop: Offset of the top-most content from the y=0 coordinate,
 * .contentLeft: Offset of the left-most content from the x=0 coordinate.
 * .absoluteTop: Top-edge of view.
 * .absoluteLeft: Left-edge of view.
 * @return {Object} Contains size and position metrics of mutator dialog's
 *     workspace.  Returns null if the dialog is hidden.
 * @private
 */
Blockly.Mutator.getWorkspaceMetrics_ = function() {
  if (!Blockly.Mutator.isOpen) {
    return null;
  }
  var viewWidth = Blockly.Mutator.workspaceWidth_ -
      Blockly.Scrollbar.scrollbarThickness;
  var viewHeight = Blockly.Mutator.workspaceHeight_ -
      Blockly.Scrollbar.scrollbarThickness;
  var blockBox = Blockly.Mutator.workspace_.getCanvas().getBBox();
  if (blockBox.width == -Infinity && blockBox.height == -Infinity) {
    // Opera has trouble with bounding boxes around empty objects.
    blockBox = {width: 0, height: 0, x: 0, y: 0};
  }
  // Add a border around the content that is at least half a screenful wide.
  var leftEdge = Math.min(blockBox.x - viewWidth / 2,
                          blockBox.x + blockBox.width - viewWidth);
  var rightEdge = Math.max(blockBox.x + blockBox.width + viewWidth / 2,
                           blockBox.x + viewWidth);
  var topEdge = Math.min(blockBox.y - viewHeight / 2,
                         blockBox.y + blockBox.height - viewHeight);
  var bottomEdge = Math.max(blockBox.y + blockBox.height + viewHeight / 2,
                            blockBox.y + viewHeight);
  return {
    viewHeight: Blockly.Mutator.workspaceHeight_,
    viewWidth: Blockly.Mutator.workspaceWidth_,
    contentHeight: bottomEdge - topEdge,
    contentWidth: rightEdge - leftEdge,
    viewTop: -Blockly.mainWorkspace.scrollY,
    viewLeft: -Blockly.mainWorkspace.scrollX,
    contentTop: topEdge,
    contentLeft: leftEdge,
    absoluteTop: Blockly.Mutator.workspaceTop_,
    absoluteLeft: Blockly.Mutator.workspaceLeft_
  };
};

/**
 * Sets the X/Y translations of the dialog's workspace to match the scrollbars.
 * @param {!Object} xyRatio Contains an x and/or y property which is a float
 *     between 0 and 1 specifying the degree of scrolling.
 * @private
 */
Blockly.Mutator.setWorkspaceMetrics_ = function(xyRatio) {
  var metrics = Blockly.Mutator.getWorkspaceMetrics_();
  if (typeof xyRatio.x == 'number') {
    Blockly.Mutator.workspace_.scrollX =
        -metrics.contentWidth * xyRatio.x - metrics.contentLeft;
  }
  if (typeof xyRatio.y == 'number') {
    Blockly.Mutator.workspace_.scrollY =
        -metrics.contentHeight * xyRatio.y - metrics.contentTop;
  }
  var translation = 'translate(' +
      (Blockly.Mutator.workspace_.scrollX + metrics.absoluteLeft) + ',' +
      (Blockly.Mutator.workspace_.scrollY + metrics.absoluteTop) + ')';
  Blockly.Mutator.workspace_.getCanvas().setAttribute('transform',
                                                          translation);
};

/**
 * Open the dialog.
 * @param {!Blockly.Block} block Block to mutate.
 * @private
 */
Blockly.Mutator.openDialog_ = function(block) {
  Blockly.Mutator.isOpen = true;
  Blockly.removeClass_(Blockly.Mutator.svgGroup_, 'blocklyHidden');
  Blockly.Mutator.position_();
  // Fire an event to allow scrollbars to resize and the trashcan to position.
  Blockly.fireUiEvent(Blockly.svgDoc, window, 'resize');
  // If the document resizes, reposition the dialog.
  Blockly.Mutator.resizeWrapper_ =
      Blockly.bindEvent_(window, 'resize', null, Blockly.Mutator.position_);
  Blockly.Mutator.flyout_.show(block.toolbox);
};

/**
 * Close the dialog.
 * @private
 */
Blockly.Mutator.closeDialog_ = function() {
  Blockly.Mutator.isOpen = false;
  Blockly.addClass_(Blockly.Mutator.svgGroup_, 'blocklyHidden');
  Blockly.unbindEvent_(window, 'resize', Blockly.Mutator.resizeWrapper_);
  Blockly.Mutator.resizeWrapper_ = null;
};

// If Buttons get used for other things beyond the Mutator Dialog, then move
// this class to a separate file.

/**
 * Class for a styled button.
 * @param {string} caption Text to display on the button.
 * @param {boolean} launch True if the button should be the launch button (red).
 * @param {Function} action Function to call when the button is clicked.
 * @constructor
 */
Blockly.Mutator.Button = function(caption, launch, action) {
  this.caption_ = caption;
  this.launch_ = launch;
  this.action_ = action;
};

/**
 * Destroy this button and unlink everything cleanly.
 */
Blockly.Mutator.Button.prototype.destroy = function() {
  if (this.onClickWrapper_) {
    Blockly.unbindEvent_(this.svgGroup_, 'click', this.onClickWrapper_);
    this.onClickWrapper_ = null;
  }
  this.svgGroup_.parentNode.removeChild(this.svgGroup_);
  this.svgGroup_ = null;
  this.svgShadow_ = null;
  this.svgBackground_ = null;
  this.svgText_ = null;
};

/**
 * Create the button's elements.  Only needs to be called once.
 * @return {!Element} The button's SVG group.
 */
Blockly.Mutator.Button.prototype.createDom = function() {
  /*
  <g class="blocklyButton blocklyLaunchButton">
    <rect rx="5" ry="5" x="2" y="2" class="bocklyButtonShadow"/>
    <rect rx="5" ry="5" class="bocklyButtonBackground"/>
    <text class="bocklyButtonText">Caption</text>
  </g>
  */
  var className = 'blocklyButton';
  if (this.launch_) {
    className += ' blocklyLaunchButton';
  }
  this.svgGroup_ = Blockly.createSvgElement('g', {'class': className}, null);
  this.svgShadow_ = Blockly.createSvgElement('rect',
      {rx: 5, ry: 5, x: 2, y: 2, 'class': 'blocklyButtonShadow'},
      this.svgGroup_);
  this.svgBackground_ = Blockly.createSvgElement('rect',
      {rx: 5, ry: 5, 'class': 'blocklyButtonBackground'}, this.svgGroup_);
  this.svgText_ = Blockly.createSvgElement('text',
      {'class': 'blocklyButtonText'}, this.svgGroup_);
  this.svgText_.appendChild(Blockly.svgDoc.createTextNode(this.caption_));

  this.onClickWrapper_ = null;
  if (this.action_) {
    this.onClickWrapper_ =
      Blockly.bindEvent_(this.svgGroup_, 'click', this, this.action_);
  }
  return this.svgGroup_;
};

/**
 * Size the buttons to fit the text.  Only needs to be called once.
 */
Blockly.Mutator.Button.prototype.init = function() {
  var X_PADDING = Blockly.ContextMenu.X_PADDING;
  var bBox = this.svgText_.getBBox();
  this.svgShadow_.setAttribute('width', bBox.width + 2 * X_PADDING);
  this.svgShadow_.setAttribute('height', bBox.height + 10);
  this.svgBackground_.setAttribute('width', bBox.width + 2 * X_PADDING);
  this.svgBackground_.setAttribute('height', bBox.height + 10);
  this.svgText_.setAttribute('x', X_PADDING);
  this.svgText_.setAttribute('y', bBox.height);
};

/**
 * Returns the dimensions of this button.
 * @return {!Object} Bounding box with x, y, height and width properties.
 */
Blockly.Mutator.Button.prototype.getBBox = function() {
  return this.svgGroup_.getBBox();
};

/**
 * Move this button to a location relative to its parent.
 * @param {number} x Horizontal location.
 * @param {number} y Vertical location.
 */
Blockly.Mutator.Button.prototype.setLocation = function(x, y) {
  this.svgGroup_.setAttribute('transform', 'translate(' + x + ',' + y + ')');
};
