// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

// BEGIN BOOTSTRAP
goog = { require: function() {}, provide: function() {} };
Blockly = { Msg: { en: { switch_blockly_language_to_en: { init: function() {} } } } };

/**
 * Processes any Blockly placeholders into Java format substitution placeholders.
 * For example, the Blockly placeholder %1 should become {0} in Java.
 * @param str
 * @returns {string}
 */
function percentsToBrackets(str) {
  let result = [], match;
  while (match = str.match(/%(\d+)/)) {
    result.push(str.substring(0, match.index), '{', match[1] - 1, '}');
    str = str.substr(match.index + match[0].length);
  }
  result.push(str);
  return result.join('');
}

/**
 * Processes any Java format substitution placeholders with Blockly placeholders.
 * For example, the Java placeholder {0} should become %1 in Blockly.
 * @param {string} str
 * @returns {string}
 */
function bracketsToPercents(str) {
  let result = [], match;
  while (match = str.match(/\{\d+\}/)) {
    result.push(str.substring(0, match.index), '%', match[1] + 1);
    str = str.substr(match.index + match[0].length);
  }
  result.push(str);
  return result.join('');
}

function js_to_prop(value) {
  // Make the following substitutions:
  // 1. Blockly strings have %1, %2, ... but properties use {0}, {1}, etc.
  // 2. MessageFormatter wants ' as ''.
  // 3. Properties files treat =, :, and newlines as terminating characters.
  return percentsToBrackets(value)
    .replace(/'/g, "''")
    .replace(/=/g, '\\=').replace(/:/g, '\\:').replace(/\n/g, '\\\n');
}

function prop_to_js(value) {
  // Make the following substitutions
  return bracketsToPercents(value)
    .replace(/''/g, "\\'")
    .replace(/\\=/g, '=').replace(/\\:/g, ':').replace(/\\n/g, '\n');
}

// END BOOTSTRAP

require('../../blocklyeditor/src/msg/en/_messages');

delete Blockly.Msg.en;

Object.keys(Blockly.Msg).forEach(key => {
  if (key.indexOf('HELPURL') > 0) {
    return;
  }
  let value = Blockly.Msg[key];
  console.log('blockseditor.' + key + '=' + js_to_prop(value));
});
