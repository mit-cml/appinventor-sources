/**
 * Visual Blocks Editor
 *
 * Copyright © 2011 Google Inc.
 * Copyright © 2011-2016 Massachusetts Institute of Technology
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
 * @fileoverview backpack flyout.
 * @author fraser@google.com (Neil Fraser)
 * @author vbrown@wellesley.edu (Tori Brown)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

 'use strict';

goog.provide('Blockly.BackpackFlyout');

goog.require('Blockly.Block');
goog.require('Blockly.Comment');
goog.require('Blockly.WorkspaceSvg');
goog.require('goog.userAgent');

/**
 * BackpackFlyout provides a Blockly flyout that presents opposite of the drawer and contains
 * the contents of the user's backpack.
 * @param workspaceOptions Options to control the look-and-feel of the flyout
 * @constructor
 */
Blockly.BackpackFlyout = function(workspaceOptions) {
  Blockly.BackpackFlyout.superClass_.constructor.call(this, workspaceOptions);
  // Backpack flyout is opposite the blocks flyout
  this.toolboxPosition_ = this.RTL ? Blockly.TOOLBOX_AT_LEFT : Blockly.TOOLBOX_AT_RIGHT;
};
goog.inherits(Blockly.BackpackFlyout, Blockly.Flyout);

/**
 * Creates the flyout's DOM.  Only needs to be called once.
 * @return {!Element} The flyout's SVG group.
 */
Blockly.BackpackFlyout.prototype.createDom = function(tagName) {
  Blockly.Flyout.prototype.createDom.call(this, tagName);
  this.svgBackground_.setAttribute('class', 'blocklybackpackFlyoutBackground');
  return this.svgGroup_;
};

/**
 * Dispose of this flyout.
 * Unlink from all DOM elements to prevent memory leaks.
 */
Blockly.BackpackFlyout.prototype.dispose = function() {
  this.hide();
  Blockly.unbindEvent_(this.eventWrappers_);
  this.eventWrappers_.length = 0;
  if (this.scrollbar_) {
    this.scrollbar_.dispose();
    this.scrollbar_ = null;
  }
  this.workspace_ = null;
  if (this.svgGroup_) {
    goog.dom.removeNode(this.svgGroup_);
    this.svgGroup_ = null;
  }
  this.svgBackground_ = null;
  this.targetWorkspace_ = null;
};

/**
 * Returns if the flyout allows a new instance of the given block to be created.
 * Always returns true to allow disabled blocks to be dragged out.
 * @param {!Blockly.BlockSvg} _block The block to copy from the flyout.
 * @return {boolean} True if the flyout allows the block to be instantiated.
 */
Blockly.BackpackFlyout.prototype.isBlockCreatable_ = function(_block) {
  return true;
}

/**
 * Stop binding to the global mouseup and mousemove events.
 * @private
 */
Blockly.BackpackFlyout.terminateDrag_ = function() {
  if (Blockly.BackpackFlyout.onMouseUpWrapper_) {
    Blockly.unbindEvent_(Blockly.BackpackFlyout.onMouseUpWrapper_);
    Blockly.BackpackFlyout.onMouseUpWrapper_ = null;
  }
  if (Blockly.BackpackFlyout.onMouseMoveWrapper_) {
    Blockly.unbindEvent_(Blockly.BackpackFlyout.onMouseMoveWrapper_);
    Blockly.BackpackFlyout.onMouseMoveWrapper_ = null;
  }
  Blockly.BackpackFlyout.startDownEvent_ = null;
  Blockly.BackpackFlyout.startBlock_ = null;
  Blockly.BackpackFlyout.startFlyout_ = null;
};
