// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle serialization of the blocks workspace
 * 
 * @author sharon@google.com (Sharon Perl)
 */

'use strict';

goog.provide('Blockly.SaveFile');

goog.require('Blockly.Instrument');

Blockly.SaveFile.load = function(blocksContent) {
  Blockly.Instrument.initializeStats("Blockly.SaveFile.load");
  Blockly.Instrument.timer(
  function () {
  // We leave it to our caller to catch JavaScriptException and deal with
  // errors loading the block space.

  if (blocksContent.length != 0) {
    blocksContent = Blockly.Versioning.translateVersion(blocksContent);
    var xml = Blockly.Xml.textToDom(blocksContent);
    if (Blockly.Instrument.useIsRenderingOn) {
      try {
        Blockly.Block.isRenderingOn = false;
        Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
      } finally { // Guarantee that rendering is turned on going forward.
        Blockly.Block.isRenderingOn = true;
      }
    } else {
      Blockly.Block.isRenderingOn = true;
      Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
    }
  }
  },
  function (result, timeDiff) {
    Blockly.Instrument.stats.totalTime = timeDiff;
    Blockly.Instrument.stats.blockCount = Blockly.Instrument.stats.domToBlockInnerCalls;
    Blockly.Instrument.stats.topBlockCount = Blockly.Instrument.stats.domToBlockCalls;
    Blockly.Instrument.displayStats("Blockly.SaveFile.load");
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
