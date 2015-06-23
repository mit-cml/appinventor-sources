// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle serialization of the blocks workspace
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 * History:
 * [lyn, 2014/10/31] Completely overhauled blocks upgrading architecture.
 * All the work is done in Blockly.Version.upgrade.
 */

'use strict';

goog.provide('Blockly.SaveFile');

goog.require('Blockly.Versioning');
goog.require('Blockly.Instrument');

Blockly.SaveFile.load = function(preUpgradeFormJson, blocksContent) {
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
        Blockly.Versioning.upgrade(preUpgradeFormJson,blocksContent);
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
    Blockly.mainWorkspace.render(); // Save the rendering of the workspace until the very end
  }
  );
};

/**
 * get is called prior to writing out. Original ai2 apps had no versions in them
 *   so now we write out every time
 *
*/
Blockly.SaveFile.get = function() {  
  var xml = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
  var element = goog.dom.createElement('yacodeblocks');
  var yaversion = window.parent.BlocklyPanel_getYaVersion();
  var languageVersion = window.parent.BlocklyPanel_getBlocksLanguageVersion();
  element.setAttribute('ya-version',yaversion);
  element.setAttribute('language-version',languageVersion);
  xml.appendChild(element);
  return Blockly.Xml.domToPrettyText(xml);
};
