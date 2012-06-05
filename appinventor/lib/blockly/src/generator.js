/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/google-blockly/
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
     * @param {?boolean} opt_dropParens If true, don't surround code with
     *     paretheses since the caller already has a safe container.
     * @return {string} Generated code, or '' if block is null.
     */
    generator.blockToCode = function(block, opt_dropParens) {
      if (!block) {
        return '';
      }
      var func = this[block.type];
      if (!func) {
        throw 'Language "' + name + '" does not know how to generate code ' +
            'for block type "' + block.type + '"';
      }
      var code = func.call(block, opt_dropParens);
      return this.scrub_(block, code);
    };

    /**
     * Generate Dart code representing the specified value input.
     * @param {!Blockly.Block} block The block containing the input.
     * @param {number} index The index of the input (0-based).
     * @param {?boolean} opt_dropParens If true, don't surround code with paretheses
     *     since the caller already has a safe container.
     * @return {string} Generated code or '' if no blocks are connected.
     */
    generator.valueToCode = function(block, index, opt_dropParens) {
      var input = block.getValueInput(index);
      return this.blockToCode(input, opt_dropParens);
    };
    
    /**
     * Generate Dart code representing the statement.  Indent the code.
     * @param {!Blockly.Block} block The block containing the input.
     * @param {number} index The index of the input (0-based).
     * @return {string} Generated code or '' if no blocks are connected.
     */
    generator.statementToCode = function(block, index) {
      var input = block.getStatementInput(index);
      var code = this.blockToCode(input);
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
    if (block.outputConnection && generator.scrubNakedValue) {
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
 * @return {string} Concatinated list of comments.
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
