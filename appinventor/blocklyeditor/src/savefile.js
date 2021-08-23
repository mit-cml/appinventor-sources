// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle serialization of the blocks workspace
 * 
 * @author sharon@google.com (Sharon Perl)
 * @author ewpatton@mit.edu (Evan W. Patton)
 *
 * History:
 * [lyn, 2014/10/31] Completely overhauled blocks upgrading architecture.
 * All the work is done in Blockly.Version.upgrade.
 */

'use strict';

goog.provide('AI.Blockly.SaveFile');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Versioning');
goog.require('AI.Blockly.Instrument');

if (Blockly.SaveFile === undefined) Blockly.SaveFile = {};

Blockly.SaveFile.load = function(preUpgradeFormJson, blocksContent) {
  try {
    Blockly.Events.disable();
    Blockly.Instrument.initializeStats("Blockly.SaveFile.load");
    Blockly.Instrument.timer(
      function () {
        // We leave it to our caller to catch JavaScriptException and deal with
        // errors loading the block space.

        if (blocksContent.length != 0) {

          // Turn rendering off since we may go back and forth between
          // dom and blocks representations many time, and only want
          // to render once at the very end.
          try {
            Blockly.Block.isRenderingOn = false;
            // Perform language and component upgrades, and put blocks into Blockly.mainWorkspace
            Blockly.Versioning.upgrade(preUpgradeFormJson, blocksContent);
          } finally { // Guarantee that rendering is turned on going forward.
            Blockly.Block.isRenderingOn = true;
          }
        }
      },
      function (result, timeDiff) {
        Blockly.Instrument.stats.totalTime = timeDiff;
        Blockly.Instrument.stats.blockCount = Blockly.Instrument.stats.domToBlockInnerCalls;
        Blockly.Instrument.stats.topBlockCount = Blockly.Instrument.stats.domToBlockCalls;
        Blockly.Instrument.displayStats("Blockly.SaveFile.load");
        if (Blockly.mainWorkspace != null && Blockly.mainWorkspace.getCanvas() != null) {
          Blockly.mainWorkspace.render(); // Save the rendering of the workspace until the very end
        }
      }
    );
  } finally {
    Blockly.Events.enable();
  }
};

/**
 * get is called prior to writing out. Original ai2 apps had no versions in them
 *   so now we write out every time
 *
 * @param {boolean} prettify Specify true if the workspace should be pretty printed.
 * @param {?Blockly.WorkspaceSvg} opt_workspace The workspace to serialize. If none is given,
 *     Blockly.mainWorkspace will be serialized.
 * @return {string} XML serialization of the workspace
*/
Blockly.SaveFile.get = function(prettify, opt_workspace) {
  var workspace = opt_workspace || Blockly.mainWorkspace;
  var xml = Blockly.Xml.workspaceToDom(workspace, false);
  var element = goog.dom.createElement('yacodeblocks');
  element.setAttribute('ya-version',top.YA_VERSION);
  element.setAttribute('language-version',top.BLOCKS_VERSION);
  xml.appendChild(element);
  return prettify ? Blockly.Xml.domToPrettyText(xml) : Blockly.Xml.domToText(xml);
};
