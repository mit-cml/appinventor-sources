// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle serialization of the blocks workspace
 * 
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.SaveFile = {};

Blockly.SaveFile.load = function(blocksContent) {
  // We leave it to our caller to catch JavaScriptException and deal with
  // errors loading the block space.
  if (blocksContent.length != 0) {
    blocksContent=Blockly.Versioning.translateVersion(blocksContent)
    var xml = Blockly.Xml.textToDom(blocksContent);
    Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
  }
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
