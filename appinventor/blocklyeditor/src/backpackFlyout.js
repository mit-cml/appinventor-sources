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

goog.provide('AI.Blockly.BackpackFlyout');

/**
 * BackpackFlyout provides a Blockly flyout that presents opposite of the drawer and contains
 * the contents of the user's backpack.
 * @param workspaceOptions Options to control the look-and-feel of the flyout
 * @constructor
 */
AI.Blockly.BackpackFlyout = class extends Blockly.VerticalFlyout {
  constructor(workspaceOptions) {
    super(workspaceOptions);
    // Backpack flyout is opposite the blocks flyout
    this.toolboxPosition_ = workspaceOptions.RTL ?
        Blockly.utils.toolbox.Position.LEFT : Blockly.utils.toolbox.Position.RIGHT;
  }
}

/**
 * Creates the flyout's DOM.  Only needs to be called once.
 * @return {!Element} The flyout's SVG group.
 */
AI.Blockly.BackpackFlyout.prototype.createDom = function(tagName) {
  Blockly.Flyout.prototype.createDom.call(this, tagName);
  this.svgBackground_.setAttribute('class', 'blocklybackpackFlyoutBackground');
  return this.svgGroup_;
};

/**
 * Dispose of this flyout.
 * Unlink from all DOM elements to prevent memory leaks.
 */
AI.Blockly.BackpackFlyout.prototype.dispose = function() {
  this.hide();
  Blockly.browserEvents.unbind(this.eventWrappers_);
  this.eventWrappers_.length = 0;
  if (this.scrollbar) {
    this.scrollbar.dispose();
    this.scrollbar = null;
  }
  this.workspace_ = null;
  if (this.svgGroup_) {
    goog.dom.removeNode(this.svgGroup_);
    this.svgGroup_ = null;
  }
  this.svgBackground_ = null;
  this.targetWorkspace = null;
};

/**
 * Returns if the flyout allows a new instance of the given block to be created.
 * Always returns true to allow disabled blocks to be dragged out.
 * @param {!Blockly.BlockSvg} _block The block to copy from the flyout.
 * @return {boolean} True if the flyout allows the block to be instantiated.
 */
AI.Blockly.BackpackFlyout.prototype.isBlockCreatable_ = function(_block) {
  return true;
}

/**
 * Stop binding to the global mouseup and mousemove events.
 * @private
 */
AI.Blockly.BackpackFlyout.terminateDrag_ = function() {
  if (AI.Blockly.BackpackFlyout.onMouseUpWrapper_) {
    Blockly.browserEvents.unbind(AI.Blockly.BackpackFlyout.onMouseUpWrapper_);
    AI.Blockly.BackpackFlyout.onMouseUpWrapper_ = null;
  }
  if (AI.Blockly.BackpackFlyout.onMouseMoveWrapper_) {
    Blockly.browserEvents.unbind(AI.Blockly.BackpackFlyout.onMouseMoveWrapper_);
    AI.Blockly.BackpackFlyout.onMouseMoveWrapper_ = null;
  }
  AI.Blockly.BackpackFlyout.startDownEvent_ = null;
  AI.Blockly.BackpackFlyout.startBlock_ = null;
  AI.Blockly.BackpackFlyout.startFlyout_ = null;
};
