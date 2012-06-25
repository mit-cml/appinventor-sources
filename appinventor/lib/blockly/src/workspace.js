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
 * @fileoverview Object representing a workspace.
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class for a workspace.
 * @param {boolean} editable Is this workspace freely interactive?
 * @constructor
 */
Blockly.Workspace = function(editable) {
  this.editable = editable;
  this.topBlocks_ = [];
  Blockly.ConnectionDB.init(this);
};

Blockly.Workspace.prototype.dragMode = false;

// Add properties to control the current scrolling offset.
Blockly.Workspace.prototype.scrollX = 0;
Blockly.Workspace.prototype.scrollY = 0;

Blockly.Workspace.prototype.trashcan = null;

/**
 * Create the trash can elements.
 * @return {!Element} The trash can's SVG group.
 */
Blockly.Workspace.prototype.createDom = function() {
  /*
  <g>
    [Trashcan may go here]
    <g></g>
  </g>
  */
  this.svgGroup_ = Blockly.createSvgElement('g', {}, null);
  this.svgBlockCanvas_ = Blockly.createSvgElement('g', {}, this.svgGroup_);
  return this.svgGroup_;
};

/**
 * Add a trashcan.
 * @param {!Function} getMetrics A function that returns workspace's metrics.
 */
Blockly.Workspace.prototype.addTrashcan = function(getMetrics) {
  if (Blockly.Trashcan && this.editable) {
    this.trashcan = new Blockly.Trashcan(getMetrics);
    var svgTrashcan = this.trashcan.createDom();
    this.svgGroup_.insertBefore(svgTrashcan, this.svgBlockCanvas_);
    this.trashcan.init();
  }
};

/**
 * Get the SVG element that forms the drawing surface.
 * @return {!Element} SVG element.
 */
Blockly.Workspace.prototype.getCanvas = function() {
  return this.svgBlockCanvas_;
};

/**
 * Add a block to the list of top blocks.
 * @param {!Blockly.Block} block Block to remove.
 */
Blockly.Workspace.prototype.addTopBlock = function(block) {
  this.topBlocks_.push(block);
};

/**
 * Remove a block from the list of top blocks.
 * @param {!Blockly.Block} block Block to remove.
 */
Blockly.Workspace.prototype.removeTopBlock = function(block) {
  var found = false;
  for (var child, x = 0; child = this.topBlocks_[x]; x++) {
    if (child == block) {
      this.topBlocks_.splice(x, 1);
      found = true;
      break;
    }
  }
  if (!found) {
    throw 'Block not present in workspace\'s list of top-most blocks.';
  }
};

/**
 * Finds the top-level blocks and returns them.  Blocks are optionally sorted
 * by position; top to bottom.
 * @param {boolean} ordered Sort the list if true.
 * @return {!Array.<!Blockly.Block>} The top-level block objects.
 */
Blockly.Workspace.prototype.getTopBlocks = function(ordered) {
  // Copy the topBlocks_ list.
  var blocks = [].concat(this.topBlocks_);
  if (ordered && blocks.length > 1) {
    blocks.sort(function(a, b)
        {return a.getRelativeToSurfaceXY().y - b.getRelativeToSurfaceXY().y;});
  }
  return blocks;
};

/**
 * Find all blocks in workspace.  No particular order.
 * @return {!Array.<!Blockly.Block>} Array of blocks.
 */
Blockly.Workspace.prototype.getAllBlocks = function() {
  var blocks = this.getTopBlocks(false);
  for (var x = 0; x < blocks.length; x++) {
    blocks = blocks.concat(blocks[x].getChildren());
  }
  return blocks;
};

/**
 * Destroy all blocks in workspace.
 */
Blockly.Workspace.prototype.clear = function() {
  Blockly.hideChaff();
  while (this.topBlocks_.length) {
    this.topBlocks_[0].destroy();
  }
};

/**
 * Render all blocks in workspace.
 */
Blockly.Workspace.prototype.render = function() {
  var renderList = this.getAllBlocks();
  for (var x = 0, block; block = renderList[x]; x++) {
    if (!block.getChildren().length) {
      block.render();
    }
  }
};

/**
 * Finds the block with the specified ID in this workspace.
 * @param {string} id ID of block to find.
 * @return {Blockly.Block} The matching block, or null if not found.
 */
Blockly.Workspace.prototype.getBlockById = function(id) {
  // If this O(n) function fails to scale well, maintain a hash table of IDs.
  var blocks = this.getAllBlocks();
  for (var x = 0, block; block = blocks[x]; x++) {
    if (block.id == id) {
      return block;
    }
  }
  return null;
};

/**
 * Turn the visual trace functionality on or off.
 * @param {boolean} armed True if the trace should be on.
 */
Blockly.Workspace.prototype.traceOn = function(armed) {
  this.traceOn_ = armed;
  if (this.traceWrapper_) {
    Blockly.unbindEvent_(this.svgBlockCanvas_, 'blocklySelectChange',
                         this.traceWrapper_);
    this.traceWrapper_ = null;
  }
  if (armed) {
    this.traceWrapper_ = Blockly.bindEvent_(this.svgBlockCanvas_,
        'blocklySelectChange', this, function() {this.traceOn_ = false});
  }
};

/**
 * Highlight a block in the workspace.
 * @param {string} id ID of block to find.
 */
Blockly.Workspace.prototype.highlightBlock = function(id) {
  if (!this.traceOn_) {
    return;
  }
  var block = this.getBlockById(id);
  if (!block) {
    return;
  }
  // Temporary turn off the listener for selection changes, so that we don't
  // trip the monitor for detecting user activity.
  this.traceOn(false);
  // Select the curent block.
  block.select();
  // Restore the monitor for user activity.
  this.traceOn(true);
};

