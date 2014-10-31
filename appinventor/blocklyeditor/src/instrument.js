// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Instrumentation (timing and statistics) for core blockly functionality
 * that's useful for figuring out where time is being spent.
 * Lyn used this in conjunction with Chrome profiling to speed up
 * loading, dragging, and expanding collapsed blocks in large projecsts.
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */

'use strict';

goog.provide('Blockly.Instrument');

/** Is instrumentation turned on? */
// Blockly.Instrument.isOn = true;
Blockly.Instrument.isOn = false; // [lyn, 04/08/14] Turn off for production

/** Turn instrumentation on/off */
Blockly.Instrument.setOn = function (bool) {
  Blockly.Instrument.isOn = bool;
}

/** The following are global flags to control rendering.
 * The default settings give the best performance.
 * Other settings can be use to show slowdowns for different choices/algorithms.
*/

/**
 * [lyn, 04/01/14] Should we use the Blockly.Block.isRenderingOn flag?
 * Default value = true.
 */
Blockly.Instrument.useIsRenderingOn = true;

/**
 * [lyn, 04/01/14] Should we avoid workspace render in Blockly.Block.onMouseUp_?
 * Default value = true.
 */
Blockly.Instrument.avoidRenderWorkspaceInMouseUp = true;

/** [lyn, 04/01/14] Global flag to control rendering algorithm,
 * Used to show that renderDown() is better than render() in many situations.
 * Default value = true.
 */
Blockly.Instrument.useRenderDown = true;

/** [lyn, 04/01/14] Should we avoid renderDown on subblocks of collapsed blocks
 * Default value = true.
 */
Blockly.Instrument.avoidRenderDownOnCollapsedSubblocks = true;

/** [lyn, 04/01/14] Use Neil's fix to Blockly.Block.getHeightWidth, which sidesteps
 *    the inexplicable quadratic problem with getHeightWidth.
 * Default value = true.
 */
Blockly.Instrument.useNeilGetHeightWidthFix = true;

/** [lyn, 04/01/14] Use my fix to Blockly.Workspace.prototype.getAllBlocks,
 *  which avoids quadratic behavior in Neil's original version.
 */
Blockly.Instrument.useLynGetAllBlocksFix = true;

/** [lyn, 04/01/14] Use my fix to Blockly.FieldLexicalVariable.getGlobalNames,
 *  which just looks at top blocks in workspace, and not all blocks.
 */
Blockly.Instrument.useLynGetGlobalNamesFix = true;

/** [lyn, 04/01/14] In Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors,
 * compute Blockly.FieldLexicalVariable.getGlobalNames only once and cache it
 * so that it needn't be computed again.
 */
Blockly.Instrument.useLynCacheGlobalNames = true;

/** [lyn, 04/05/14] Stats to track improvements in slow removal */
Blockly.Instrument.stats = {}

Blockly.Instrument.statNames = [
  "totalTime",
  "topBlockCount",
  "blockCount",
  "domToBlockCalls",
  "domToBlockTime",
  "domToBlockInnerCalls",
  "domToWorkspaceCalls",
  "domToWorkspaceTime",
  "workspaceRenderCalls",
  "workspaceRenderTime",
  "renderCalls",
  // "renderTime",  Hard to track without double counting because of its recursive nature. Use renderHereTime instead.
  "renderDownCalls",
  "renderDownTime",
  "renderHereCalls",
  "renderHereTime",
  "getHeightWidthCalls",
  "getHeightWidthTime",
  "getTopBlocksCalls",
  "getTopBlocksTime",
  "getAllBlocksCalls",
  "getAllBlocksTime",
  "getAllBlocksAllocationCalls",
  "getAllBlocksAllocationSpace",
  "checkAllBlocksForWarningsAndErrorsCalls",
  "checkAllBlocksForWarningsAndErrorsTime",
  "scrollBarResizeCalls",
  "scrollBarResizeTime",
  "trashCanPositionCalls",
  "trashCanPositionTime",
  "expandCollapsedCalls",
  "expandCollapsedTime"
]

Blockly.Instrument.initializeStats = function (name) {
  if (Blockly.Instrument.isOn) {
    console.log("Initializing stats for " + name);
    var names = Blockly.Instrument.statNames;
    var stats = Blockly.Instrument.stats;
    for (var i = 0, name; name = names[i]; i++) {
      stats[name] = 0;
    }
  }
}

Blockly.Instrument.displayStats = function (name) {
  if (Blockly.Instrument.isOn) {
    var names = Blockly.Instrument.statNames;
    var stats = Blockly.Instrument.stats;
    console.log("Displaying stats for "  + name + ":");
    console.log("  Blockly.Instrument.useRenderDown=" + Blockly.Instrument.useRenderDown);
    console.log("  Blockly.Instrument.useIsRenderingOn=" + Blockly.Instrument.useIsRenderingOn);
    console.log("  Blockly.Instrument.avoidRenderWorkspaceInMouseUp=" + Blockly.Instrument.avoidRenderWorkspaceInMouseUp);
    console.log("  Blockly.Instrument.avoidRenderDownOnCollapsedSubblocks=" + Blockly.Instrument.avoidRenderDownOnCollapsedSubblocks);
    console.log("  Blockly.Instrument.useNeilGetHeightWidthFix=" + Blockly.Instrument.useNeilGetHeightWidthFix);
    console.log("  Blockly.Instrument.useLynGetAllBlocksFix=" + Blockly.Instrument.useLynGetAllBlocksFix);
    console.log("  Blockly.Instrument.useLynGetGlobalNamesFix=" + Blockly.Instrument.useLynGetGlobalNamesFix);
    console.log("  Blockly.Instrument.useLynCacheGlobalNames=" + Blockly.Instrument.useLynCacheGlobalNames);
    for  (var i = 0, name; name = names[i]; i++) {
      console.log("  " + name + "=" + stats[name]);
    }
  }
}

Blockly.Instrument.timer = function (thunk, callback) {
  if (Blockly.Instrument.isOn) {
    var start = new Date().getTime();
    var result = thunk();
    var stop = new Date().getTime();
    return callback(result, stop - start);
  } else {
    var result = thunk();
    return callback(result, 0);
  }
}
