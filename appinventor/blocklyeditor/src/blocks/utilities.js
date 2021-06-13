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
  },
  'enum': {
    'input': null,
    'output': ['Key']
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
  if (yail.indexOf('Enum') != -1) {
    return yail;
  }

  var type = Blockly.Blocks.Utilities
      .YailTypeToBlocklyTypeMap[yail][inputOrOutput];
  if (type === undefined) {
    throw new Error("Unknown Yail type: " + yail + " -- YailTypeToBlocklyType");
  }
  return type;
};

/**
 * Returns the blockly type associated with the given helper key, or null if
 * there is not one.
 * @param {!HelperKey} helperKey The helper key to find the equivalent blockly
 *     type of.
 * @param {!Blockly.Block} block The block which we will apply the type to. Used
 *     to access the component database etc.
 * @return {*} Something to add to the components array, or null/undefined.
 */
Blockly.Blocks.Utilities.helperKeyToBlocklyType = function(helperKey, block) {
  if (!helperKey) {
    return null;
  }
  var utils = Blockly.Blocks.Utilities;
  switch (helperKey.type) {
    case "OPTION_LIST":
      return utils.optionListKeyToBlocklyType(helperKey.key, block);
    case "ASSET":
      return utils.assetKeyToBlocklyType(helperKey.key, block);
  }
  return null;
}

/**
 * Returns the blockly type associated with the given option list helper key.
 * @param {HelperKey} key The key to find the equivalent blockly type of.
 * @param {!Blockly.Block} block The block which we will apply the type to. Used
 *     to access the component database etc.
 * @return {!string} The correct string representation of the type.
 */
Blockly.Blocks.Utilities.optionListKeyToBlocklyType = function(key, block) {
  var optionList = block.getTopWorkspace().getComponentDatabase()
      .getOptionList(key);
  return optionList.className + 'Enum';
}

/**
 * Returns a filter array associated with the given key. This can be added to
 * the connections type check. It causes any asset blocks attached to that
 * connection to filter their dropdowns.
 * @param {number=} key The key associated with a filter.
 * @param {!Blockly.Block} block The block to apply the filter to.
 * @return {Array<!string>=} An array of filters for use in filtering an
 *     attached assets block.
 */
Blockly.Blocks.Utilities.assetKeyToBlocklyType = function(key, block) {
  return block.getTopWorkspace().getComponentDatabase().getFilter(key);
}


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
