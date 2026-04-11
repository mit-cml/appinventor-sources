// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Exports blocks-only YAIL from a workspace for the AI agent.
 * Unlike getFormYail(), this produces only the block-level YAIL (event handlers,
 * global variables, and procedures) without form scaffolding (define-form,
 * add-component, do-after-form-creation, init-runtime, etc.).
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.Yail.BlocksExport');

goog.require('AI.Yail');

/**
 * Generate YAIL for a block even if it is disabled.  Temporarily clears
 * the {@code disabled} flag (without firing events), calls the appropriate
 * code generator, and restores the flag.
 *
 * @param {!Blockly.Block} block The block to generate code for.
 * @param {boolean} thisOnly If true, uses {@code blockToCode1} (no
 *     attached blocks); otherwise uses {@code blockToCode}.
 * @return {string} Generated YAIL string, or empty string on failure.
 *     If the generator returns a [code, order] tuple, only the code
 *     part is returned.
 */
AI.Yail.blockToYailIgnoringDisabled = function(block, thisOnly) {
  var wasDisabled = block.disabled;
  if (wasDisabled) {
    block.disabled = false;
  }
  var code;
  try {
    code = thisOnly ? AI.Yail.blockToCode1(block) : AI.Yail.blockToCode(block);
    if (code instanceof Array) code = code[0];
  } catch (e) {
    code = '';
  } finally {
    if (wasDisabled) {
      block.disabled = true;
    }
  }
  return code || '';
};

/**
 * Generate YAIL for a single block, handling disabled blocks by temporarily
 * enabling them and prefixing the output with a ;;; DISABLED comment.
 *
 * @param {!Blockly.BlockSvg} block The block to generate YAIL for.
 * @return {string} YAIL string, or empty string if generation fails.
 * @private
 */
var blockToYailForExport_ = function(block) {
  var code = AI.Yail.blockToYailIgnoringDisabled(block, false);
  if (!code) {
    return '';
  }
  if (block.type === 'procedures_defreturn') {
    code = code.replace(/^\(def /, '(def-return ');
  }
  if (block.disabled) {
    code = ';;; DISABLED\n' + code;
  }
  return code;
};

/**
 * Generate YAIL for just the blocks in the workspace, organized by section.
 * This is used by the AI agent to get a code representation of the current
 * blocks without the form/component initialization boilerplate.
 *
 * Disabled blocks are included with a {@code ;;; DISABLED} comment prefix so
 * the LLM is aware of their existence.
 *
 * @return {string} YAIL string containing only block-level code.
 */
Blockly.WorkspaceSvg.prototype.getBlocksYail = function() {
  var componentMap = this.buildComponentMap([], [], false, false);
  var globals = [];
  var events = [];

  // Global variables and procedures
  for (var i = 0, block; block = componentMap.globals[i]; i++) {
    var code = blockToYailForExport_(block);
    if (code) {
      globals.push(code);
    }
  }

  // Event handlers, organized by component
  var componentNames = Object.keys(componentMap.components).sort();
  for (var c = 0; c < componentNames.length; c++) {
    var componentName = componentNames[c];
    var blocks = componentMap.components[componentName];
    for (var i = 0; i < blocks.length; i++) {
      var code = blockToYailForExport_(blocks[i]);
      if (code) {
        events.push(code);
      }
    }
  }

  var sections = [];
  if (globals.length > 0) {
    sections.push(globals.join('\n\n'));
  }
  if (events.length > 0) {
    sections.push(events.join('\n\n'));
  }

  return sections.join('\n\n');
};
