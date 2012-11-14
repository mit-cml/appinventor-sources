/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Utility functions for generating executable code from
 * Blockly code.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

/**
 * Name space for the generator singleton.
 */
Blockly.Generator = {};

/**
 * Category to separate generated function names from variables and procedures.
 */
Blockly.Generator.NAME_TYPE = 'generated_function';

/**
 * Database of code generators, one for each language.
 */
Blockly.Generator.languages = {};

/**
 * Return the code generator for the specified language.  Create one if needed.
 * @param {string} name The language's name.
 * @return {!Object} Generator for this language.
 */
Blockly.Generator.get = function(name) {
  if (!(name in Blockly.Generator.languages)) {
    var generator = new Blockly.CodeGenerator(name);
    Blockly.Generator.languages[name] = generator;
  }
  return Blockly.Generator.languages[name];
};

/**
 * Generate code for all blocks in the workspace to the specified language.
 * @param {string} name Language name (e.g. 'JavaScript').
 * @return {string} Generated code.
 */
Blockly.Generator.workspaceToCode = function(name) {
  var code = [];
  var generator = Blockly.Generator.get(name);
  generator.init();
  var blocks = Blockly.mainWorkspace.getTopBlocks(true);
  for (var x = 0, block; block = blocks[x]; x++) {
    var line = generator.blockToCode(block);
    if (line instanceof Array) {
      // Value blocks return tuples of code and operator order.
      // Top-level blocks don't care about operator order.
      line = line[0];
    }
    if (line) {
      if (block.outputConnection && generator.scrubNakedValue) {
        // This block is a naked value.  Ask the language's code generator if
        // it wants to append a semicolon, or something.
        line = generator.scrubNakedValue(line);
      }
      code.push(line);
    }
  }
  code = code.join('\n');  // Blank line between each section.
  code = generator.finish(code);
  // Final scrubbing of whitespace.
  code = code.replace(/^\s+\n/, '');
  code = code.replace(/\n\s+$/, '\n');
  code = code.replace(/[ \t]+\n/g, '\n');
  return code;
};

// The following are some helpful functions which can be used by multiple
// languages.

/**
 * Prepend a common prefix onto each line of code.
 * @param {string} text The lines of code.
 * @param {string} prefix The common prefix.
 * @return {string} The prefixed lines of code.
 */
Blockly.Generator.prefixLines = function(text, prefix) {
  return prefix + text.replace(/\n(.)/g, '\n' + prefix + '$1');
};

/**
 * Recursively spider a tree of blocks, returning all their comments.
 * @param {!Blockly.Block} block The block from which to start spidering.
 * @return {string} Concatenated list of comments.
 */
Blockly.Generator.allNestedComments = function(block) {
  var comments = [];
  var blocks = block.getDescendants();
  for (var x = 0; x < blocks.length; x++) {
    var comment = blocks[x].getCommentText();
    if (comment) {
      comments.push(comment);
    }
  }
  // Append an empty string to create a trailing line break when joined.
  if (comments.length) {
    comments.push('');
  }
  return comments.join('\n');
};

/**
 * Class for a code generator that translates the blocks into a language.
 * @param {string} name Language name of this generator.
 * @constructor
 */
Blockly.CodeGenerator = function(name) {
  this.name_ = name;
};

/**
 * Generate code for the specified block (and attached blocks).
 * @param {Blockly.Block} block The block to generate code for.
 * @return {string|!Array} For statement blocks, the generated code.
 *     For value blocks, an array containing the generated code and an
 *     operator order value.  Returns '' if block is null.
 */
Blockly.CodeGenerator.prototype.blockToCode = function(block) {
  if (!block || block.disabled) {
    return '';
  }
  var func = this[block.type];
  if (!func) {
    throw 'Language "' + this.name_ + '" does not know how to generate code ' +
        'for block type "' + block.type + '".';
  }
  var code = func.call(block);
  if (code instanceof Array) {
    // Value blocks return tuples of code and operator order.
    return [this.scrub_(block, code[0]), code[1]];
  } else {
    return this.scrub_(block, code);
  }
};

/**
 * Generate code representing the specified value input.
 * @param {!Blockly.Block} block The block containing the input.
 * @param {string} name The name of the input.
 * @param {number} order The maximum binding strength (minimum order value)
 *     of any operators adjacent to "block".
 * @return {string} Generated code or '' if no blocks are connected.
 */
Blockly.CodeGenerator.prototype.valueToCode = function(block, name, order) {
  var targetBlock = block.getInputTargetBlock(name);
  if (!targetBlock) {
    return '';
  }
  var tuple = this.blockToCode(targetBlock);
  if (tuple === '') {
    // Disabled block.
    return '';
  }
  if (!(tuple instanceof Array)) {
    // Value blocks must return code and order of operations info.
    // Statement blocks must only return code.
    throw 'Expecting tuple from value block "' + targetBlock.type + '".';
  }
  var code = tuple[0];
  var innerOrder = tuple[1];
  if (code && order <= innerOrder) {
    // The operators outside this code are stonger than the operators
    // inside this code.  To prevent the code from being pulled apart,
    // wrap the code in parentheses.
    // Technically, this should be handled on a language-by-language basis.
    // However all known (sane) languages use parentheses for grouping.
    code = '(' + code + ')';
  }
  return code;
};

/**
 * Generate code representing the statement.  Indent the code.
 * @param {!Blockly.Block} block The block containing the input.
 * @param {string} name The name of the input.
 * @return {string} Generated code or '' if no blocks are connected.
 */
Blockly.CodeGenerator.prototype.statementToCode = function(block, name) {
  var targetBlock = block.getInputTargetBlock(name);
  var code = this.blockToCode(targetBlock);
  if (!goog.isString(code)) {
    // Value blocks must return code and order of operations info.
    // Statement blocks must only return code.
    throw 'Expecting code from statement block "' + targetBlock.type + '".';
  }
  if (code) {
    code = Blockly.Generator.prefixLines(/** @type {string} */ (code), '  ');
  }
  return code;
};
