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
 * Generate YAIL for just the blocks in the workspace, organized by section.
 * This is used by the AI agent to get a code representation of the current
 * blocks without the form/component initialization boilerplate.
 *
 * @return {string} YAIL string containing only block-level code.
 */
Blockly.WorkspaceSvg.prototype.getBlocksYail = function() {
  var componentMap = this.buildComponentMap([], [], false, false);
  var globals = [];
  var events = [];

  // Global variables and procedures
  for (var i = 0, block; block = componentMap.globals[i]; i++) {
    var code = AI.Yail.blockToCode(block);
    if (code) {
      if (block.type === 'procedures_defreturn') {
        code = code.replace(/^\(def /, '(def-return ');
      }
      globals.push(code);
    }
  }

  // Event handlers, organized by component
  var componentNames = Object.keys(componentMap.components).sort();
  for (var c = 0; c < componentNames.length; c++) {
    var componentName = componentNames[c];
    var blocks = componentMap.components[componentName];
    for (var i = 0; i < blocks.length; i++) {
      var code = AI.Yail.blockToCode(blocks[i]);
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
