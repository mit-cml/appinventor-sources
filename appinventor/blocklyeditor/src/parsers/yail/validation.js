// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Dry-run validation for YAIL S-expressions.
 * Checks structure without creating any Blockly blocks.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.YailToBlocks.Validation');

goog.require('AI.SExprParser');
goog.require('AI.YailToBlocks');

// ---- Dry-run validation (no block creation) ----

/**
 * Validate a YAIL string without creating any blocks.
 * Parses the S-expression and checks that each top-level form has a
 * recognized head and the minimum required structure.  This is used
 * by the client to pre-validate LLM output before showing the
 * operation preview to the user.
 *
 * @param {string} yailString The YAIL S-expression string.
 * @return {{valid: boolean, error: ?string}}
 */
AI.YailToBlocks.validate = function(yailString) {
  try {
    var ast = AI.SExprParser.parseAll(yailString);
    if (ast.length === 0) {
      return {valid: false, error: 'Empty YAIL input'};
    }

    // A write_block tool call must carry exactly one top-level form.
    // Packing multiple defs into a single yail string collapses upsert
    // identity onto the first form only, so a later single-proc rewrite
    // can't replace the others. Require one tool call per block.
    if (ast.length > 1) {
      return {valid: false,
          error: 'write_block.yail must contain exactly one top-level form '
              + '(define-event, define-generic-event, def, or def-return). '
              + 'Got ' + ast.length + '. Split into '
              + ast.length + ' separate write_block calls.'};
    }

    for (var i = 0; i < ast.length; i++) {
      var node = ast[i];
      var result = AI.YailToBlocks.validateTopLevel_(node);
      if (!result.valid) {
        return result;
      }
    }
    return {valid: true, error: null};
  } catch (e) {
    var msg = e.message;
    if (msg.indexOf('Unterminated list') !== -1 ||
        msg.indexOf('missing closing parenthesis') !== -1) {
      var deficit = AI.SExprParser.countParenDeficit(yailString);
      if (deficit > 0) {
        msg += '. You are missing ' + deficit + ' closing parenthes'
            + (deficit === 1 ? 'is' : 'es')
            + ' — add ' + deficit + ' more ) at the end.';
      }
    }
    return {valid: false, error: msg};
  }
};

/**
 * Validate a DELETE_BLOCK identifier string without touching the workspace.
 * Checks that the identifier has the right format (form type + names).
 *
 * @param {string} identifier Block identifier (e.g., "define-event Button1 Click").
 * @return {{valid: boolean, error: ?string}}
 */
AI.YailToBlocks.validateDeleteId = function(identifier) {
  var tokens = identifier.trim().split(/\s+/);
  if (tokens.length === 0) {
    return {valid: false, error: 'Empty block identifier'};
  }

  var formType = tokens[0];
  switch (formType) {
    case 'define-event':
      if (tokens.length < 3) {
        return {valid: false,
            error: 'define-event requires component and event name'};
      }
      break;
    case 'define-generic-event':
      if (tokens.length < 3) {
        return {valid: false,
            error: 'define-generic-event requires type and event name'};
      }
      break;
    case 'def':
    case 'def-return':
      if (tokens.length < 2) {
        return {valid: false,
            error: formType + ' requires a variable or procedure name'};
      }
      var name = tokens[1].replace(/[()]/g, '');
      if (!name.startsWith('g$') && !name.startsWith('p$')) {
        return {valid: false,
            error: formType + ': name must start with g$ or p$, got: ' + name};
      }
      break;
    default:
      return {valid: false, error: 'Unknown block type: ' + formType};
  }
  return {valid: true, error: null};
};

/**
 * Validate a single top-level S-expression form.
 * @param {Object} node AST node from the parser.
 * @return {{valid: boolean, error: ?string}}
 * @private
 */
AI.YailToBlocks.validateTopLevel_ = function(node) {
  var head = AI.SExprParser.formHead(node);
  if (!head) {
    return {valid: false,
        error: 'Top-level form must be a list starting with a symbol'};
  }

  switch (head) {
    case 'define-event':
      return AI.YailToBlocks.validateEventHead_(node);
    case 'def':
      return AI.YailToBlocks.validateDefHead_(node, false);
    case 'def-return':
      return AI.YailToBlocks.validateDefHead_(node, true);
    case 'define-generic-event':
      return AI.YailToBlocks.validateGenericEventHead_(node);
    default:
      return {valid: false,
          error: 'Unknown top-level form: ' + head
              + ". Expected 'define-event', 'define-generic-event',"
              + " 'def', or 'def-return'."};
  }
};

/**
 * Validate (define-event ComponentName EventName ($params...) ...) structure.
 * @private
 */
AI.YailToBlocks.validateEventHead_ = function(node) {
  var els = node.elements;
  if (els.length < 4) {
    return {valid: false,
        error: 'define-event requires at least component, event, and params'};
  }
  var comp = els[1];
  var evt = els[2];
  if (comp.type !== 'symbol' || !comp.name) {
    return {valid: false,
        error: 'define-event: component name must be a symbol'};
  }
  if (evt.type !== 'symbol' || !evt.name) {
    return {valid: false,
        error: 'define-event: event name must be a symbol'};
  }
  return {valid: true, error: null};
};

/**
 * Validate (define-generic-event TypeName EventName ($params...) ...) structure.
 * @private
 */
AI.YailToBlocks.validateGenericEventHead_ = function(node) {
  var els = node.elements;
  if (els.length < 4) {
    return {valid: false,
        error: 'define-generic-event requires at least type, event, and params'};
  }
  var typeName = els[1];
  var evt = els[2];
  if (typeName.type !== 'symbol' || !typeName.name) {
    return {valid: false,
        error: 'define-generic-event: type name must be a symbol'};
  }
  if (evt.type !== 'symbol' || !evt.name) {
    return {valid: false,
        error: 'define-generic-event: event name must be a symbol'};
  }
  return {valid: true, error: null};
};

/**
 * Validate (def g$name value) or (def (p$name ...) body) structure.
 * @private
 */
AI.YailToBlocks.validateDefHead_ = function(node, isReturn) {
  var formName = isReturn ? 'def-return' : 'def';
  var els = node.elements;
  if (els.length < 3) {
    return {valid: false,
        error: formName + ' requires at least 3 elements'};
  }

  var second = els[1];
  if (second.type === 'symbol') {
    if (!second.name.startsWith('g$')) {
      return {valid: false,
          error: formName + ': global variable name must start with g$, got: '
              + second.name};
    }
    if (isReturn) {
      return {valid: false,
          error: 'def-return cannot be used for global variables;'
              + ' use def instead'};
    }
    return {valid: true, error: null};
  } else if (second.type === 'list') {
    if (second.elements.length < 1) {
      return {valid: false,
          error: formName + ': procedure parameter list is empty'};
    }
    var procNameNode = second.elements[0];
    if (procNameNode.type !== 'symbol' || !procNameNode.name) {
      return {valid: false,
          error: formName + ': procedure name must be a symbol'};
    }
    if (!procNameNode.name.startsWith('p$')) {
      return {valid: false,
          error: formName + ': procedure name must start with p$, got: '
              + procNameNode.name};
    }
    return {valid: true, error: null};
  } else {
    return {valid: false,
        error: formName + ': unexpected second element type: '
            + second.type};
  }
};
