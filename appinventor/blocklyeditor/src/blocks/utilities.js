// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @fileoverview Block utilities for Blockly, modified for App Inventor
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author hal@mit.edu (Hal Abelson)
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

'use strict';

goog.provide('Blockly.Blocks.Utilities');
goog.require('AI.Blockly.Msg');

/**
 * Checks that the given otherConnection is compatible with an InstantInTime
 * connection. If the workspace is currently loading (eg the blocks are not
 * yet rendered) this always returns true for backwards compatibility.
 * @param {!Blockly.Connection} myConn The parent connection.
 * @param {!Blockly.Connection} otherConn The child connection.
 */
Blockly.Blocks.Utilities.InstantInTime = function (myConn, otherConn) {
  if (!myConn.sourceBlock_.rendered ||
      !otherConn.sourceBlock_.rendered) {
    if (otherConn.check_ && !otherConn.check_.includes('InstantInTime')) {
      otherConn.sourceBlock_.badBlock();
    }
    return true;
  }
  return !otherConn.check_ || otherConn.check_.includes('InstantInTime');
};


// Convert Yail types to Blockly types
// Yail types are represented by strings: number, text, list, any, ...
// Blockly types are represented by objects: Number, String, ...
// and by the string "COMPONENT"
// The Yail type 'any' is repsented by Javascript null, to match
// Blockly's convention
Blockly.Blocks.Utilities.YailTypeToBlocklyTypeMap = {
  'number': {
    'input': ['Number'],
    'output': ['Number', 'String', 'Key']
  },
  'text': {
    'input': ['String'],
    'output': ['Number', 'String', 'Key']
  },
  'boolean': {
    'input': ['Boolean'],
    'output': ['Boolean', 'String']
  },
  'list': {
    'input': ['Array'],
    'output': ['Array', 'String']
  },
  'component': {
    'input': ['COMPONENT'],
    'output': ['COMPONENT', 'Key']
  },
  'InstantInTime': {
    'input': ['InstantInTime', Blockly.Blocks.Utilities.InstantInTime],
    'output': ['InstantInTime', Blockly.Blocks.Utilities.InstantInTime],
  },
  'any': {
    'input': null,
    'output': null
  },
  'dictionary': {
    'input': ['Dictionary'],
    'output': ['Dictionary', 'String', 'Array']
  },
  'pair': {
    'input': ['Pair'],
    'output': ['Pair', 'String', 'Array']
  },
  'key': {
    'input': ['Key'],
    'output': ['String', 'Key']
  }
};

Blockly.Blocks.Utilities.OUTPUT = 'output';
Blockly.Blocks.Utilities.INPUT = 'input';

/**
 * Gets the equivalent Blockly type for a given Yail type.
 * @param {string} yail The Yail type.
 * @param {!string} inputOrOutput Either Utilities.OUTPUT or Utilities.INPUT.
 * @param {Array<string>=} opt_currentType A type array to append, or null.
 */
Blockly.Blocks.Utilities.YailTypeToBlocklyType = function(yail, inputOrOutput) {
  var type = Blockly.Blocks.Utilities
      .YailTypeToBlocklyTypeMap[yail][inputOrOutput];
  if (type === undefined) {
    throw new Error("Unknown Yail type: " + yail + " -- YailTypeToBlocklyType");
  }
  return type;
};


// Blockly doesn't wrap tooltips, so these can get too wide.  We'll create our own tooltip setter
// that wraps to length 60.

Blockly.Blocks.Utilities.setTooltip = function(block, tooltip) {
    block.setTooltip(Blockly.Blocks.Utilities.wrapSentence(tooltip, 60));
};

// Wrap a string by splitting at spaces. Permit long chunks if there
// are no spaces.

Blockly.Blocks.Utilities.wrapSentence = function(str, len) {
  str = str.trim();
  if (str.length < len) return str;
  var place = (str.lastIndexOf(" ", len));
  if (place == -1) {
    return str.substring(0, len).trim() + Blockly.Blocks.Utilities.wrapSentence(str.substring(len), len);
  } else {
    return str.substring(0, place).trim() + "\n" +
           Blockly.Blocks.Utilities.wrapSentence(str.substring(place), len);
  }
};

// Change the text of collapsed blocks on rename
// Recurse to fix collapsed parents

Blockly.Blocks.Utilities.MAX_COLLAPSE = 4;

// unicode multiplication symbol
Blockly.Blocks.Utilities.times_symbol = '\u00D7';

/**
 * Regular expression for floating point numbers.
 *
 * @type {!RegExp}
 * @const
 */
Blockly.Blocks.Utilities.NUMBER_REGEX =
  new RegExp("^([-+]?[0-9]+)?(\\.[0-9]+)?([eE][-+]?[0-9]+)?$|" +
    "^[-+]?[0-9]+(\\.[0-9]*)?([eE][-+]?[0-9]+)?$");
