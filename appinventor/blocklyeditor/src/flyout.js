'use strict';

goog.provide('AI.Blockly.Flyout');

goog.require('Blockly.Flyout');
goog.require('Blockly.Block');
goog.require('Blockly.Comment');

/**
 * Copy a block from the flyout to the workspace and position it correctly.
 * @param {!Blockly.Block} originBlock The flyout block to copy..
 * @return {!Blockly.Block} The new block in the main workspace.
 * @private
 */
Blockly.Flyout.prototype.placeNewBlock_ = function(originBlock) {
  var targetWorkspace = this.targetWorkspace_;
  var svgRootOld = originBlock.getSvgRoot();
  if (!svgRootOld) {
    throw 'originBlock is not rendered.';
  }
  // Figure out where the original block is on the screen, relative to the upper
  // left corner of the main workspace.
  if (targetWorkspace.isMutator) {
    var xyOld = this.workspace_.getSvgXY(/** @type {!Element} */ (svgRootOld));
  } else {
    var xyOld = Blockly.utils.getInjectionDivXY_(svgRootOld);
  }

  // Take into account that the flyout might have been scrolled horizontally
  // (separately from the main workspace).
  // Generally a no-op in vertical mode but likely to happen in horizontal
  // mode.
  var scrollX = this.workspace_.scrollX;
  var scale = this.workspace_.scale;
  xyOld.x += scrollX / scale - scrollX;
  // If the flyout is on the right side, (0, 0) in the flyout is offset to
  // the right of (0, 0) in the main workspace.  Add an offset to take that
  // into account.
  if (this.toolboxPosition_ == Blockly.TOOLBOX_AT_RIGHT) {
    scrollX = targetWorkspace.getMetrics().viewWidth - this.width_;
    scale = targetWorkspace.scale;
    // Scale the scroll (getSvgXY_ did not do this).
    xyOld.x += scrollX / scale - scrollX;
  }


  // Take into account that the flyout might have been scrolled vertically
  // (separately from the main workspace).
  // Generally a no-op in horizontal mode but likely to happen in vertical
  // mode.
  var scrollY = this.workspace_.scrollY;
  scale = this.workspace_.scale;
  xyOld.y += scrollY / scale - scrollY;
  // If the flyout is on the bottom, (0, 0) in the flyout is offset to be below
  // (0, 0) in the main workspace.  Add an offset to take that into account.
  if (this.toolboxPosition_ == Blockly.TOOLBOX_AT_BOTTOM) {
    scrollY = targetWorkspace.getMetrics().viewHeight - this.height_;
    scale = targetWorkspace.scale;
    xyOld.y += scrollY / scale - scrollY;
  }

  // Create the new block by cloning the block in the flyout (via XML).
  var xml = Blockly.Xml.blockToDom(originBlock);
  var block = Blockly.Xml.domToBlock(xml, targetWorkspace);
  var svgRootNew = block.getSvgRoot();
  if (!svgRootNew) {
    throw 'block is not rendered.';
  }
  // Figure out where the new block got placed on the screen, relative to the
  // upper left corner of the workspace.  This may not be the same as the
  // original block because the flyout's origin may not be the same as the
  // main workspace's origin.
  if (targetWorkspace.isMutator) {
    var xyNew = targetWorkspace.getSvgXY(/* @type {!Element} */(svgRootNew));
  } else {
    var xyNew = Blockly.utils.getInjectionDivXY_(svgRootNew);
  }

  if (this.RTL) {
    xyNew.x -= (targetWorkspace.getMetrics().viewWidth - xyNew.x) / targetWorkspace.scale;
  }

  // Scale the scroll (getSvgXY_ did not do this).
  xyNew.x +=
    targetWorkspace.scrollX / targetWorkspace.scale - targetWorkspace.scrollX;
  xyNew.y +=
    targetWorkspace.scrollY / targetWorkspace.scale - targetWorkspace.scrollY;
  // If the flyout is collapsible and the workspace can't be scrolled.
  if (targetWorkspace.toolbox_ && !targetWorkspace.scrollbar) {
    xyNew.x += targetWorkspace.toolbox_.getWidth() / targetWorkspace.scale;
    xyNew.y += targetWorkspace.toolbox_.getHeight() / targetWorkspace.scale;
  }

  // Move the new block to where the old block is.
  block.moveBy(xyOld.x - xyNew.x, xyOld.y - xyNew.y);
  return block;
};