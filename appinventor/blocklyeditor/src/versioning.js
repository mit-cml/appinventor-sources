// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle converting apps from older versions to current
 *
 * @author wolber@usfca.edu (David Wolber)
 */

'use strict';

goog.provide('Blockly.Versioning');
goog.require('goog.dom.xml');

/**
 * translateVersion is called from Blockly.Versioning.load to translate
 * old blocks into current version.
 * blocksContent, some xml, is sent in, its (possibly) modified value is returned.
 *
*/

Blockly.Versioning.translateVersion = function(blocksContent) {
  // get the text into a dom object xmlFromFile
  var parser = new DOMParser();
  var domFromFile = parser.parseFromString(blocksContent,"text/xml");
  var xmlFromFile = domFromFile.firstChild;  // get the xml element within doc tag
  // see if we have a version. If not, we need to translate
  var versionTags = xmlFromFile.getElementsByTagName('yacodeblocks');
  // if there is no version in the file, then this is an early ai2 project, prior to
  // 10/21/13, when the blocks internal xml structure was overhauled
  // with descriptive mutator tags. blocksOverhaul translates the blocks
  if (versionTags.length==0) // there is a version
  {
    Blockly.Versioning.blocksOverhaul(xmlFromFile);
  }

  // when we're done with all translation of xml object, write out to text and return
    // now write back dom object to a string and return it
  var serializer = new XMLSerializer();
  return serializer.serializeToString(xmlFromFile);

}
