// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Block positioning and width optimization for the
 * YAIL-to-Blocks converter.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.YailToBlocks.Positioning');

goog.require('AI.YailToBlocks');

/**
 * Get top-level blocks for positioning, excluding any blocks that are
 * scheduled for deletion in the current batch.
 *
 * @param {!Blockly.WorkspaceSvg} workspace
 * @return {!Array<!Blockly.Block>}
 * @private
 */
AI.YailToBlocks.getPositioningBlocks_ = function(workspace) {
  var blocks = workspace.getTopBlocks(false);
  var pending = AI.YailToBlocks.pendingDeletionIds_;
  if (!pending) return blocks;
  var filtered = [];
  for (var i = 0; i < blocks.length; i++) {
    if (!pending[blocks[i].id]) {
      filtered.push(blocks[i]);
    }
  }
  return filtered;
};

/**
 * Find the deepest block eligible for inline-to-external flipping.
 * Eligibility: (1) currently inline, (2) at least 2 value inputs have
 * blocks connected, (3) at least one connected child is non-leaf.
 * Depth counts only value-input connections (not statement/next).
 *
 * @param {!Blockly.Block} block The block to search from.
 * @param {number} valueDepth Current depth along value-input chain.
 * @param {number} maxDepth Maximum value-depth to consider. Candidates
 *     deeper than this are skipped (used to escalate to shallower blocks
 *     when deep flips are ineffective).
 * @return {?{block: !Blockly.Block, depth: number, width: number}}
 * @private
 */
AI.YailToBlocks.findDeepestEligibleBlock_ = function(block, valueDepth, maxDepth) {
  var best = null;

  if (block.inputList) {
    for (var i = 0; i < block.inputList.length; i++) {
      var input = block.inputList[i];
      var child = input.connection && input.connection.targetBlock();
      if (!child) continue;
      var childDepth = (input.type === Blockly.INPUT_VALUE)
          ? valueDepth + 1
          : valueDepth;
      var current = child;
      while (current) {
        var result = AI.YailToBlocks.findDeepestEligibleBlock_(
            current, childDepth, maxDepth);
        if (result && (!best || result.depth > best.depth ||
            (result.depth === best.depth &&
             result.width > best.width))) {
          best = result;
        }
        current = current.getNextBlock();
      }
    }
  }

  if (valueDepth <= maxDepth && block.getInputsInline()) {
    var connectedValueCount = 0;
    var hasNonLeafChild = false;
    if (block.inputList) {
      for (var i = 0; i < block.inputList.length; i++) {
        var input = block.inputList[i];
        if (input.type === Blockly.INPUT_VALUE) {
          var child = input.connection && input.connection.targetBlock();
          if (child) {
            connectedValueCount++;
            if (child.getChildren(false).length > 0) {
              hasNonLeafChild = true;
            }
          }
        }
      }
    }
    if (connectedValueCount >= 2 && hasNonLeafChild) {
      var hw = block.getHeightWidth();
      if (!best || valueDepth > best.depth ||
          (valueDepth === best.depth && hw.width > best.width)) {
        best = {block: block, depth: valueDepth, width: hw.width};
      }
    }
  }

  return best;
};

/**
 * Optimize block widths by flipping deep inline blocks to external inputs.
 * Called after initial render and before positioning. Only affects blocks
 * in the provided array (AI-generated blocks from the current conversion).
 *
 * @param {!Blockly.WorkspaceSvg} workspace The workspace.
 * @param {!Array<!Blockly.Block>} blocks The newly created top-level blocks.
 * @private
 */
AI.YailToBlocks.optimizeBlockWidths_ = function(workspace, blocks) {
  var metrics = workspace.getMetrics();
  var maxWidth = metrics.viewWidth / workspace.scale;
  var MAX_FLIPS = 3;
  var MIN_REDUCTION = 0.2;  // minimum 20% width reduction per flip

  for (var i = 0; i < blocks.length; i++) {
    var block = blocks[i];
    var flips = 0;
    var maxCandidateDepth = Infinity;
    while (flips < MAX_FLIPS) {
      var hw = block.getHeightWidth();
      if (hw.width <= maxWidth) break;
      var prevWidth = hw.width;
      var candidate = AI.YailToBlocks.findDeepestEligibleBlock_(
          block, 0, maxCandidateDepth);
      if (!candidate) break;
      candidate.block.setInputsInline(false);
      candidate.block.queueRender();
      Blockly.renderManagement.triggerQueuedRenders();
      flips++;
      var newWidth = block.getHeightWidth().width;
      if (newWidth > prevWidth * (1 - MIN_REDUCTION)) {
        maxCandidateDepth = candidate.depth - 1;
        if (maxCandidateDepth < 0) break;
      }
    }
  }
};

/**
 * Find a position near existing blocks for the same component.
 * Returns null if the block has no component association or no
 * group members exist on the workspace.
 *
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {!Blockly.Block} newBlock The block to position.
 * @param {number} spacing Vertical gap between blocks.
 * @return {?Blockly.utils.Coordinate} Position, or null if no group found.
 * @private
 */
AI.YailToBlocks.findGroupPosition_ = function(workspace, newBlock, spacing) {
  var mutation = newBlock.mutationToDom ? newBlock.mutationToDom() : null;
  if (!mutation) return null;
  var instanceName = mutation.getAttribute('instance_name');
  if (!instanceName) return null;

  var topBlocks = AI.YailToBlocks.getPositioningBlocks_(workspace);
  var maxBottom = -Infinity;
  var groupX = null;

  for (var i = 0; i < topBlocks.length; i++) {
    var eb = topBlocks[i];
    if (eb === newBlock || eb.id === newBlock.id) continue;
    var ebMutation = eb.mutationToDom ? eb.mutationToDom() : null;
    if (!ebMutation) continue;
    if (ebMutation.getAttribute('instance_name') !== instanceName) continue;

    var xy = eb.getRelativeToSurfaceXY();
    var hw = eb.getHeightWidth();
    var bottom = xy.y + hw.height;
    if (bottom > maxBottom) {
      maxBottom = bottom;
      groupX = xy.x;
    }
  }

  if (groupX === null) return null;
  return new Blockly.utils.Coordinate(groupX, maxBottom + spacing);
};

/**
 * Adjust a proposed block position to avoid overlapping existing blocks.
 * Shifts the block rightward past any overlapping blocks.
 *
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {number} x Proposed X coordinate.
 * @param {number} y Proposed Y coordinate.
 * @param {number} width Block width.
 * @param {number} height Block height.
 * @param {number} spacing Gap to leave between blocks.
 * @param {!Blockly.Block} currentBlock Block being positioned (excluded).
 * @return {!Blockly.utils.Coordinate} Adjusted position.
 * @private
 */
AI.YailToBlocks.avoidOverlap_ = function(
    workspace, x, y, width, height, spacing, currentBlock) {
  var topBlocks = AI.YailToBlocks.getPositioningBlocks_(workspace);
  for (var attempt = 0; attempt < 20; attempt++) {
    var overlap = false;
    for (var i = 0; i < topBlocks.length; i++) {
      var eb = topBlocks[i];
      if (eb === currentBlock || eb.id === currentBlock.id) continue;
      var exy = eb.getRelativeToSurfaceXY();
      var ehw = eb.getHeightWidth();
      if (x < exy.x + ehw.width && x + width > exy.x &&
          y < exy.y + ehw.height && y + height > exy.y) {
        x = exy.x + ehw.width + spacing;
        overlap = true;
        break;
      }
    }
    if (!overlap) break;
  }
  return new Blockly.utils.Coordinate(x, y);
};
