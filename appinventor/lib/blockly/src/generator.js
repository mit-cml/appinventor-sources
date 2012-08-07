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

Blockly.Generator = {};

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
    var generator = {};
    /**
     * Generate code for the specified block (and attached blocks).
     * @param {Blockly.Block} block The block to generate code for.
     * @return {string|!Array} For statement blocks, the generated code.
     *     For value blocks, an array containing the generated code and an
     *     operator order value.  Returns '' if block is null.
     */
    generator.blockToCode = function(block) {
      if (!block) {
        return '';
      }
      var func = this[block.type];
      if (!func) {
        throw 'Language "' + name + '" does not know how to generate code ' +
            'for block type "' + block.type + '".';
      }
      var code = func.call(block);
      if (code instanceof Array) {
        // Value blocks return tuples of code and operator order.
        if (block.disabled) {
          code[0] = '';
        }
        return [this.scrub_(block, code[0]), code[1]];
      } else {
        if (block.disabled) {
          code = '';
        }
        return this.scrub_(block, code);
      }
    };

    /**
     * Generate code representing the specified value input.
     * @param {!Blockly.Block} block The block containing the input.
     * @param {string} name The name of the input.
     * @param {integer} order Order of operations rank of this input's context.
     * @return {string} Generated code or '' if no blocks are connected.
     */
    generator.valueToCode = function(block, name, order) {
      var input = block.getInputTargetBlock(name);
      if (!input) {
        return '';
      }
      var tuple = this.blockToCode(input);
      if (!(tuple instanceof Array)) {
        // Value blocks must return code and order of operations info.
        // Statement blocks must only return code.
        throw 'Expecting tuple from value block "' + input.type + '".';
      }
      var code = tuple[0];
      var innerOrder = tuple[1];
      if (code && order <= innerOrder) {
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
    generator.statementToCode = function(block, name) {
      var input = block.getInputTargetBlock(name);
      var code = this.blockToCode(input);
      if (typeof code != 'string') {
        // Value blocks must return code and order of operations info.
        // Statement blocks must only return code.
        throw 'Expecting code from statement block "' + input.type + '".';
      }
      if (code) {
        code = Blockly.Generator.prefixLines(code, '  ');
      }
      return code;
    };

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
    var line = generator.blockToCode(block, true);
    if (line instanceof Array) {
      // Value blocks return tuples of code and operator order.
      // Top-level blocks don't care about operator order.
      line = line[0];
    }
    if (block.outputConnection && generator.scrubNakedValue && line) {
      // This block is a naked value.  Ask the language's code generator if
      // it wants to append a semicolon, or something.
      line = generator.scrubNakedValue(line);
    }
    code.push(line);
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
