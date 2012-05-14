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
Blockly.Mutator = function(block, toolbox) {
  this.block_ = block;
  this.toolbox_ = toolbox;
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
    <svg>
      <rect class="blocklyMutatorBackground" />
      <text class="blocklyHeader" y="30">Block Editor</text>
      [Help button]
      [Cancel button]
      [Change button]
      [Flyout]
      [Workspace]
    </g>
  </g>
  */
  var svgGroup = Blockly.createSvgElement('g', {'class': 'blocklyHidden'},
                                          null);
  Blockly.Mutator.svgGroup_ = svgGroup;
  Blockly.Mutator.svgShadow_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyScreenShadow'}, svgGroup);
  Blockly.Mutator.svgDialog_ = Blockly.createSvgElement('svg', {}, svgGroup);
  Blockly.Mutator.svgBackground_ = Blockly.createSvgElement('rect',
      {'class': 'blocklyMutatorBackground',
       height: '100%', width: '100%'}, Blockly.Mutator.svgDialog_);
  Blockly.Mutator.svgHeader_ = Blockly.createSvgElement('text',
      {'class': 'blocklyHeader', y: 30}, Blockly.Mutator.svgDialog_);
  var textNode = Blockly.svgDoc.createTextNode(Blockly.MSG_MUTATOR_HEADER);
  Blockly.Mutator.svgHeader_.appendChild(textNode);

  // Buttons
  Blockly.Mutator.helpButton_ =
      new Blockly.Mutator.Button(Blockly.MSG_HELP, false,
                                 Blockly.Mutator.showHelp_);
  Blockly.Mutator.cancelButton_ =
      new Blockly.Mutator.Button(Blockly.MSG_MUTATOR_CANCEL, false,
                                 Blockly.Mutator.closeDialog_);
  Blockly.Mutator.changeButton_ =
      new Blockly.Mutator.Button(Blockly.MSG_MUTATOR_CHANGE, true,
                                 Blockly.Mutator.saveDialog_);
  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.helpButton_.createDom());
  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.cancelButton_.createDom());
  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.changeButton_.createDom());

  // TODO: Move workspace and flyout instantiation into constructor, once
  // Mutator stops being a singleton.
  Blockly.Mutator.workspace_ = new Blockly.Workspace(true);
  Blockly.Mutator.flyout_ = new Blockly.Flyout();

  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.flyout_.createDom());
  Blockly.Mutator.svgDialog_.appendChild(
      Blockly.Mutator.workspace_.createDom());

  return svgGroup;
};

/**
 * Layout the buttons.  Only needs to be called once.
 */
Blockly.Mutator.init = function() {
  Blockly.Mutator.helpButton_.init();
  Blockly.Mutator.cancelButton_.init();
  Blockly.Mutator.changeButton_.init();
  // Save the size of the header and buttons so that calculations on their size
  // may be performed regardless of whether they are hidden or not.
  Blockly.Mutator.headerLength_ =
      Blockly.Mutator.svgHeader_.getComputedTextLength();
  Blockly.Mutator.helpLength_ = Blockly.Mutator.helpButton_.getBBox().width;
  Blockly.Mutator.cancelLength_ = Blockly.Mutator.cancelButton_.getBBox().width;
  var bBoxChange = Blockly.Mutator.changeButton_.getBBox();
  Blockly.Mutator.changeLength_ = bBoxChange.width;

  // Record some layout information for Blockly.Mutator.getWorkspaceMetrics_.
  Blockly.Mutator.workspaceLeft_ = 0;
  Blockly.Mutator.workspaceTop_ = bBoxChange.height + 10;

  Blockly.Mutator.workspace_.addTrashcan(Blockly.Mutator.getWorkspaceMetrics_);
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
  Blockly.Mutator.svgDialog_.setAttribute('x', MARGIN);
  Blockly.Mutator.svgDialog_.setAttribute('y', MARGIN);
  Blockly.Mutator.svgDialog_.setAttribute('width', width);
  Blockly.Mutator.svgDialog_.setAttribute('height', height);
  Blockly.Mutator.svgDialog_.setAttribute('viewBox',
      '0 0 ' + width + ' ' + height);

  var headerX = Blockly.ContextMenu.X_PADDING;
  if (Blockly.RTL) {
    headerX = width - Blockly.Mutator.headerLength_ - headerX;
  }
  Blockly.Mutator.svgHeader_.setAttribute('x', headerX);

  var cursorX;
  var cursorY = 5;
  if (Blockly.RTL) {
    cursorX = Blockly.ContextMenu.X_PADDING;
    Blockly.Mutator.changeButton_.setLocation(cursorX, cursorY);
    cursorX += Blockly.Mutator.changeLength_ + Blockly.ContextMenu.X_PADDING;
    Blockly.Mutator.cancelButton_.setLocation(cursorX, cursorY);
    cursorX += Blockly.Mutator.cancelLength_ + Blockly.ContextMenu.X_PADDING;
    Blockly.Mutator.helpButton_.setLocation(cursorX, cursorY);
    cursorX += Blockly.Mutator.helpLength_;
    cursorX = headerX - cursorX;
  } else {
    var cursorX = width - Blockly.ContextMenu.X_PADDING -
        Blockly.Mutator.changeLength_;
    Blockly.Mutator.changeButton_.setLocation(cursorX, cursorY);
    cursorX -= Blockly.ContextMenu.X_PADDING + Blockly.Mutator.cancelLength_;
    Blockly.Mutator.cancelButton_.setLocation(cursorX, cursorY);
    cursorX -= Blockly.ContextMenu.X_PADDING + Blockly.Mutator.helpLength_;
    Blockly.Mutator.helpButton_.setLocation(cursorX, cursorY);
    Blockly.Mutator.helpButton_.setVisible(cursorX > 0);
    cursorX -= headerX + Blockly.Mutator.headerLength_;
  }

  // Hide the header if the window is too small.
  Blockly.Mutator.svgHeader_.style.display = (cursorX > 0) ? 'block' : 'none';

  // Record some layout information for Blockly.Mutator.getWorkspaceMetrics_.
  Blockly.Mutator.workspaceWidth_ = width;
  Blockly.Mutator.workspaceHeight_ = height - Blockly.Mutator.workspaceTop_;
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
 * Return an object with the metrics required to position the trash can.
 * The following properties are computed:
 * .viewHeight: Height of the visible rectangle,
 * .viewWidth: Width of the visible rectangle,
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
  return {
    viewHeight: Blockly.Mutator.workspaceHeight_,
    viewWidth: Blockly.Mutator.workspaceWidth_,
    absoluteTop: Blockly.Mutator.workspaceTop_,
    absoluteLeft: Blockly.Mutator.workspaceLeft_
  };
};

/**
 * Open the dialog.
 * @param {!Blockly.Block} block Block to mutate.
 * @private
 */
Blockly.Mutator.showHelp_ = function() {
  Blockly.Mutator.sourceBlock_.showHelp_();
};

/**
 * Open the dialog.
 * @param {!Blockly.Block} block Block to mutate.
 * @private
 */
Blockly.Mutator.openDialog_ = function(block) {
  Blockly.Mutator.isOpen = true;
  Blockly.Mutator.sourceBlock_ = block;
  Blockly.Mutator.helpButton_.setVisible(!!block.helpUrl);
  Blockly.removeClass_(Blockly.Mutator.svgGroup_, 'blocklyHidden');
  Blockly.Mutator.position_();
  // Fire an event to allow the trashcan to position.
  Blockly.fireUiEvent(Blockly.svgDoc, window, 'resize');
  // If the document resizes, reposition the dialog.
  Blockly.Mutator.resizeWrapper_ =
      Blockly.bindEvent_(window, 'resize', null, Blockly.Mutator.position_);
  Blockly.Mutator.flyout_.show(block.mutator.toolbox_);

  Blockly.Mutator.rootBlock_ = block.decompose(Blockly.Mutator.workspace_);
  var blocks = Blockly.Mutator.rootBlock_.getDescendants();
  for (var i = 0, child; child = blocks[i]; i++) {
    child.render();
  }
  var x = 150;
  if (Blockly.RTL) {
    x = Blockly.Mutator.workspaceWidth_ - x;
  }
  Blockly.Mutator.rootBlock_.moveBy(x, 50);
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

  // Empty the dialog.
  Blockly.Mutator.flyout_.hide();
  var blocks = Blockly.Mutator.workspace_.getTopBlocks();
  for (var x = 0, block; block = blocks[x]; x++) {
    block.destroy();
  }
  Blockly.Mutator.sourceBlock_ = null;
  Blockly.Mutator.rootBlock_ = null;
};

/**
 * Save the mutation and close the dialog.
 * @private
 */
Blockly.Mutator.saveDialog_ = function() {
  Blockly.Mutator.sourceBlock_.compose(Blockly.Mutator.rootBlock_);
  Blockly.Mutator.closeDialog_();
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

/**
 * Show or hide this button.
 * @param {boolean} visible True if visible.
 */
Blockly.Mutator.Button.prototype.setVisible = function(visible) {
  this.svgGroup_.setAttribute('display', visible ? 'block' : 'none');
};
