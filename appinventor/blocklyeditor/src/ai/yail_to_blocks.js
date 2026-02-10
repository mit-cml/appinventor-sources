// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Converts YAIL S-expressions into Blockly blocks.
 * Parses YAIL code produced by the LLM and creates blocks programmatically
 * in the workspace using the Blockly API.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.YailToBlocks');

goog.require('AI.SExprParser');

/**
 * Position saved by deleteBlock so the replacement block can be placed
 * at the same location (upsert semantics).
 * @type {?{x: number, y: number}}
 * @private
 */
AI.YailToBlocks.lastDeletedPosition_ = null;

/**
 * Convert a YAIL string into Blockly blocks and add them to the workspace.
 *
 * @param {!Blockly.WorkspaceSvg} workspace The target workspace.
 * @param {string} yailString The YAIL S-expression string.
 * @return {{success: boolean, error: ?string, blockId: ?string}}
 */
AI.YailToBlocks.convert = function(workspace, yailString) {
  try {
    var ast = AI.SExprParser.parseAll(yailString);
    if (ast.length === 0) {
      return {success: false, error: 'Empty YAIL input', blockId: null};
    }

    // Disable events during the entire conversion to prevent workspace
    // saves from running on half-initialized blocks.  Block creation
    // (newBlock → domToMutation → initSvg) is not atomic, and
    // mutationToDom can crash if called between newBlock and domToMutation
    // (e.g. component_event.parameterNames is only set in domToMutation).
    Blockly.Events.disable();
    var createdBlocks = [];
    var upsertPositions = [];  // parallel: saved position or null per block
    try {
      var lastBlockId = null;
      for (var i = 0; i < ast.length; i++) {
        var node = ast[i];
        AI.YailToBlocks.lastDeletedPosition_ = null;
        var block = AI.YailToBlocks.convertTopLevel_(workspace, node);
        if (block) {
          createdBlocks.push(block);
          upsertPositions.push(AI.YailToBlocks.lastDeletedPosition_);
          lastBlockId = block.id;
          // Don't position here — dimensions aren't reliable until all
          // descendants are properly rendered (after events re-enabled).
        }
      }
    } catch (e) {
      // Clean up any blocks created before the error so broken
      // half-initialized blocks don't persist in the workspace.
      for (var j = 0; j < createdBlocks.length; j++) {
        try { createdBlocks[j].dispose(false); } catch (ignore) {}
      }
      Blockly.Events.enable();
      return {success: false, error: e.message, blockId: null};
    }
    Blockly.Events.enable();

    // Render ALL blocks including descendants, following the same pattern
    // as Blockly's domToBlockInternal.  This ensures every block's SVG
    // paths, fields, icons, and connections are properly computed — not
    // just the top-level roots.
    for (var k = 0; k < createdBlocks.length; k++) {
      var rootBlock = createdBlocks[k];
      var descendants = rootBlock.getDescendants(false);
      rootBlock.setConnectionTracking(false);
      for (var d = descendants.length - 1; d >= 0; d--) {
        descendants[d].initSvg();
        descendants[d].queueRender();
      }
    }
    Blockly.renderManagement.triggerQueuedRenders();

    // Position blocks — dimensions are now accurate after rendering.
    // New blocks are centered horizontally in the viewport.
    // Upsert blocks go back to their original position.
    var SPACING = 30;
    var metrics = workspace.getMetrics();
    var viewCenterX = metrics.viewLeft +
        metrics.viewWidth / (2 * workspace.scale);
    var viewTop = metrics.viewTop / workspace.scale;
    var nextY = viewTop + 20;

    for (var k = 0; k < createdBlocks.length; k++) {
      var block = createdBlocks[k];
      var upsertPos = upsertPositions[k];
      if (upsertPos) {
        block.moveTo(
            new Blockly.utils.Coordinate(upsertPos.x, upsertPos.y));
      } else {
        var bw = block.getHeightWidth().width;
        var x = viewCenterX - bw / 2;
        block.moveTo(new Blockly.utils.Coordinate(x, nextY));
        nextY += block.getHeightWidth().height + SPACING;
      }
    }

    // Re-enable connection tracking (deferred, like domToBlockInternal).
    for (var k = 0; k < createdBlocks.length; k++) {
      (function(b) {
        setTimeout(function() {
          if (!b.disposed) b.setConnectionTracking(true);
        }, 1);
      })(createdBlocks[k]);
    }
    workspace.resizeContents();

    return {success: true, error: null, blockId: lastBlockId};
  } catch (e) {
    // Ensure events are always re-enabled
    if (!Blockly.Events.isEnabled()) {
      Blockly.Events.enable();
    }
    return {success: false, error: e.message, blockId: null};
  }
};

/**
 * Delete a block identified by its YAIL head tokens.
 *
 * @param {!Blockly.WorkspaceSvg} workspace The workspace.
 * @param {string} identifier Block identifier (e.g., "define-event Button1 Click").
 * @return {{success: boolean, error: ?string}}
 */
AI.YailToBlocks.deleteBlock = function(workspace, identifier) {
  var tokens = identifier.trim().split(/\s+/);
  if (tokens.length === 0) {
    return {success: false, error: 'Empty block identifier'};
  }

  var formType = tokens[0];
  var topBlocks = workspace.getTopBlocks(false);
  var blockToDelete = null;

  switch (formType) {
    case 'define-event':
      if (tokens.length < 3) {
        return {success: false, error: 'define-event requires component and event name'};
      }
      var componentName = tokens[1];
      var eventName = tokens[2];
      for (var i = 0; i < topBlocks.length; i++) {
        var block = topBlocks[i];
        if (block.type === 'component_event' && block.instanceName === componentName
            && block.eventName === eventName) {
          blockToDelete = block;
          break;
        }
      }
      if (!blockToDelete) {
        return {success: false, error: 'Event handler not found: ' + componentName + '.' + eventName};
      }
      break;

    case 'def':
    case 'def-return':
      if (tokens.length < 2) {
        return {success: false, error: 'def requires a name'};
      }
      var name = tokens[1].replace(/[()]/g, '');
      if (name.startsWith('g$')) {
        var varName = name.substring(2);
        for (var i = 0; i < topBlocks.length; i++) {
          var block = topBlocks[i];
          if (block.type === 'global_declaration'
              && block.getFieldValue('NAME') === varName) {
            blockToDelete = block;
            break;
          }
        }
        if (!blockToDelete) {
          return {success: false, error: 'Global variable not found: ' + varName};
        }
      } else if (name.startsWith('p$')) {
        var procName = name.substring(2);
        for (var i = 0; i < topBlocks.length; i++) {
          var block = topBlocks[i];
          if ((block.type === 'procedures_defnoreturn' || block.type === 'procedures_defreturn')
              && block.getFieldValue('NAME') === procName) {
            blockToDelete = block;
            break;
          }
        }
        if (!blockToDelete) {
          return {success: false, error: 'Procedure not found: ' + procName};
        }
      } else {
        return {success: false, error: 'Unknown def target: ' + name};
      }
      break;

    case 'define-generic-event':
      if (tokens.length < 3) {
        return {success: false, error: 'define-generic-event requires type and event name'};
      }
      var typeName = tokens[1];
      var eventName = tokens[2];
      for (var i = 0; i < topBlocks.length; i++) {
        var block = topBlocks[i];
        if (block.type === 'component_event' && block.isGeneric
            && block.typeName === typeName && block.eventName === eventName) {
          blockToDelete = block;
          break;
        }
      }
      if (!blockToDelete) {
        return {success: false, error: 'Generic event handler not found: ' + typeName + '.' + eventName};
      }
      break;

    default:
      return {success: false, error: 'Unknown block type: ' + formType};
  }

  // Save position so the replacement block can be placed here (upsert).
  AI.YailToBlocks.lastDeletedPosition_ =
      blockToDelete.getRelativeToSurfaceXY();

  // Dispose with events disabled to prevent auto-save from serializing
  // the workspace while other blocks may be half-initialized.
  var eventsEnabled = Blockly.Events.isEnabled();
  if (eventsEnabled) {
    Blockly.Events.disable();
  }
  try {
    blockToDelete.dispose(true);
  } finally {
    if (eventsEnabled) {
      Blockly.Events.enable();
    }
  }
  return {success: true, error: null};
};

// ---- Internal: Top-level form dispatch ----

/**
 * Convert a top-level YAIL form into a block.
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {Object} node AST node
 * @return {?Blockly.Block} The created block, or null.
 * @private
 */
AI.YailToBlocks.convertTopLevel_ = function(workspace, node) {
  var head = AI.SExprParser.formHead(node);
  if (!head) {
    throw new Error('Top-level form must be a list starting with a symbol');
  }

  switch (head) {
    case 'define-event':
      return AI.YailToBlocks.convertEventHandler_(workspace, node);
    case 'def':
      return AI.YailToBlocks.convertDef_(workspace, node, false);
    case 'def-return':
      return AI.YailToBlocks.convertDef_(workspace, node, true);
    case 'define-generic-event':
      return AI.YailToBlocks.convertGenericEventHandler_(workspace, node);
    default:
      throw new Error('Unknown top-level form: ' + head);
  }
};

/**
 * Convert a (def ...) form — either a global variable or a procedure.
 * @private
 */
AI.YailToBlocks.convertDef_ = function(workspace, node, isReturn) {
  var els = node.elements;
  // (def g$name value) - global variable
  // (def (p$name $param1 ...) body) - procedure
  // (def-return (p$name $param1 ...) body) - procedure with return

  if (els.length < 3) {
    throw new Error('def form requires at least 3 elements');
  }

  var second = els[1];

  if (second.type === 'symbol' && second.name.startsWith('g$')) {
    return AI.YailToBlocks.convertGlobalVar_(workspace, node);
  } else if (second.type === 'list') {
    return AI.YailToBlocks.convertProcedure_(workspace, node, isReturn);
  } else {
    throw new Error('Unexpected def target: ' + JSON.stringify(second));
  }
};

// ---- Event Handler ----

/**
 * Convert (define-event ComponentName EventName ($params...) (set-this-form) body...)
 * @private
 */
AI.YailToBlocks.convertEventHandler_ = function(workspace, node) {
  var els = node.elements;
  // [0]: define-event, [1]: componentName, [2]: eventName, [3]: params, [4..]: body
  if (els.length < 4) {
    throw new Error('define-event requires at least component, event, and params');
  }

  var componentName = els[1].name || String(els[1].value);
  var eventName = els[2].name || String(els[2].value);

  // Extract parameter names from the params list
  var params = [];
  var paramsNode = els[3];
  if (paramsNode.type === 'list') {
    for (var i = 0; i < paramsNode.elements.length; i++) {
      var p = paramsNode.elements[i];
      var pName = (p.name || String(p.value)).replace(/^\$(?:param_|local_)?/, '');
      params.push(pName);
    }
  }

  // Delete existing event handler if any
  AI.YailToBlocks.deleteBlock(workspace, 'define-event ' + componentName + ' ' + eventName);

  // Get the component type from the workspace's component database
  var componentDb = workspace.getComponentDatabase();
  var componentType = '';
  if (componentDb) {
    var typeInfo = componentDb.instanceNameToTypeName(componentName);
    if (typeInfo) {
      componentType = typeInfo;
    }
  }

  // Create the event block with mutation
  var block = workspace.newBlock('component_event');

  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  mutation.setAttribute('instance_name', componentName);
  mutation.setAttribute('event_name', eventName);
  mutation.setAttribute('is_generic', 'false');
  block.domToMutation(mutation);
  block.initSvg();

  // Convert body statements (skip set-this-form)
  var bodyStatements = [];
  for (var i = 4; i < els.length; i++) {
    if (AI.SExprParser.isForm(els[i], 'set-this-form')) {
      continue;
    }
    bodyStatements.push(els[i]);
  }

  if (bodyStatements.length > 0) {
    var firstStmt = AI.YailToBlocks.convertStatement_(workspace, bodyStatements[0]);
    if (firstStmt && block.getInput('DO')) {
      block.getInput('DO').connection.connect(firstStmt.previousConnection);
      // Chain remaining statements
      var prevBlock = firstStmt;
      for (var i = 1; i < bodyStatements.length; i++) {
        var nextStmt = AI.YailToBlocks.convertStatement_(workspace, bodyStatements[i]);
        if (nextStmt && prevBlock.nextConnection) {
          prevBlock.nextConnection.connect(nextStmt.previousConnection);
          prevBlock = nextStmt;
        }
      }
    }
  }

  block.render();
  return block;
};

// ---- Generic Event Handler ----

/**
 * Convert (define-generic-event TypeName EventName ($component $params...) (set-this-form) body...)
 * @private
 */
AI.YailToBlocks.convertGenericEventHandler_ = function(workspace, node) {
  var els = node.elements;
  // [0]: define-generic-event, [1]: typeName, [2]: eventName, [3]: params, [4..]: body
  if (els.length < 4) {
    throw new Error('define-generic-event requires type, event, and params');
  }

  var typeName = els[1].name || String(els[1].value);
  var eventName = els[2].name || String(els[2].value);

  // Extract parameter names (first param is always the component instance)
  var params = [];
  var paramsNode = els[3];
  if (paramsNode.type === 'list') {
    for (var i = 0; i < paramsNode.elements.length; i++) {
      var p = paramsNode.elements[i];
      var pName = (p.name || String(p.value)).replace(/^\$(?:param_|local_)?/, '');
      params.push(pName);
    }
  }

  // Delete existing generic event handler if any
  AI.YailToBlocks.deleteBlock(workspace,
      'define-generic-event ' + typeName + ' ' + eventName);

  // Create the event block with mutation (generic = true)
  var block = workspace.newBlock('component_event');

  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', typeName);
  mutation.setAttribute('instance_name', '');
  mutation.setAttribute('event_name', eventName);
  mutation.setAttribute('is_generic', 'true');
  block.domToMutation(mutation);
  block.initSvg();

  // Convert body statements (skip set-this-form)
  var bodyStatements = [];
  for (var i = 4; i < els.length; i++) {
    if (AI.SExprParser.isForm(els[i], 'set-this-form')) {
      continue;
    }
    bodyStatements.push(els[i]);
  }

  if (bodyStatements.length > 0) {
    var firstStmt = AI.YailToBlocks.convertStatement_(workspace, bodyStatements[0]);
    if (firstStmt && block.getInput('DO')) {
      block.getInput('DO').connection.connect(firstStmt.previousConnection);
      // Chain remaining statements
      var prevBlock = firstStmt;
      for (var i = 1; i < bodyStatements.length; i++) {
        var nextStmt = AI.YailToBlocks.convertStatement_(workspace, bodyStatements[i]);
        if (nextStmt && prevBlock.nextConnection) {
          prevBlock.nextConnection.connect(nextStmt.previousConnection);
          prevBlock = nextStmt;
        }
      }
    }
  }

  block.render();
  return block;
};

// ---- Global Variable ----

/**
 * Convert (def g$name initValue)
 * @private
 */
AI.YailToBlocks.convertGlobalVar_ = function(workspace, node) {
  var els = node.elements;
  var varName = els[1].name.substring(2); // strip g$
  var initValue = els[2];

  // Delete existing variable if any
  AI.YailToBlocks.deleteBlock(workspace, 'def g$' + varName);

  var block = workspace.newBlock('global_declaration');
  block.setFieldValue(varName, 'NAME');
  block.initSvg();

  // Convert initial value expression
  var valueBlock = AI.YailToBlocks.convertExpression_(workspace, initValue);
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }

  block.render();
  return block;
};

// ---- Procedure ----

/**
 * Convert (def (p$name $param1 ...) body...)
 * @private
 */
AI.YailToBlocks.convertProcedure_ = function(workspace, node, isReturn) {
  var els = node.elements;
  var nameList = els[1]; // (p$name $param1 ...)
  var procNameSym = nameList.elements[0];
  var procName = (procNameSym.name || String(procNameSym.value)).replace(/^p\$/, '');

  var params = [];
  for (var i = 1; i < nameList.elements.length; i++) {
    var p = nameList.elements[i];
    var pName = (p.name || String(p.value)).replace(/^\$(?:param_|local_)?/, '');
    params.push(pName);
  }

  // Delete existing procedure if any
  AI.YailToBlocks.deleteBlock(workspace, 'def p$' + procName);

  // Collect body forms (skip set-this-form)
  var bodyForms = [];
  for (var i = 2; i < els.length; i++) {
    if (AI.SExprParser.isForm(els[i], 'set-this-form')) continue;
    bodyForms.push(els[i]);
  }

  var blockType = isReturn ? 'procedures_defreturn' : 'procedures_defnoreturn';

  var block = workspace.newBlock(blockType);
  block.setFieldValue(procName, 'NAME');

  // Set parameters via mutation
  if (params.length > 0 && block.setProcedureParameters) {
    block.setProcedureParameters(params, params);
  }

  block.initSvg();

  // Connect body
  if (bodyForms.length > 0) {
    if (isReturn) {
      // For return procedures, the body is a single expression in the RETURN input
      // If multiple forms, wrap in a begin-like structure
      if (bodyForms.length === 1) {
        var retBlock = AI.YailToBlocks.convertExpression_(workspace, bodyForms[0]);
        if (retBlock && block.getInput('RETURN')) {
          block.getInput('RETURN').connection.connect(retBlock.outputConnection);
        }
      } else {
        // Multiple body forms — create a do_then_return wrapper
        var doReturn = workspace.newBlock('controls_do_then_return');
        doReturn.initSvg();
        var stmtNodes = bodyForms.slice(0, bodyForms.length - 1);
        var firstStmt = AI.YailToBlocks.convertStatement_(workspace, stmtNodes[0]);
        if (firstStmt && doReturn.getInput('STM')) {
          doReturn.getInput('STM').connection.connect(firstStmt.previousConnection);
          var prev = firstStmt;
          for (var i = 1; i < stmtNodes.length; i++) {
            var next = AI.YailToBlocks.convertStatement_(workspace, stmtNodes[i]);
            if (next && prev.nextConnection && next.previousConnection) {
              prev.nextConnection.connect(next.previousConnection);
              prev = next;
            }
          }
        }
        var retExpr = AI.YailToBlocks.convertExpression_(
            workspace, bodyForms[bodyForms.length - 1]);
        if (retExpr && doReturn.getInput('VALUE')) {
          doReturn.getInput('VALUE').connection.connect(retExpr.outputConnection);
        }
        if (block.getInput('RETURN')) {
          block.getInput('RETURN').connection.connect(doReturn.outputConnection);
        }
      }
    } else {
      // For no-return procedures, chain statements in STACK input
      var firstStmt = AI.YailToBlocks.convertStatement_(workspace, bodyForms[0]);
      if (firstStmt && block.getInput('STACK')) {
        block.getInput('STACK').connection.connect(
            firstStmt.previousConnection || firstStmt.outputConnection);
        var prevBlock = firstStmt;
        for (var i = 1; i < bodyForms.length; i++) {
          var nextStmt = AI.YailToBlocks.convertStatement_(workspace, bodyForms[i]);
          if (nextStmt && prevBlock.nextConnection) {
            prevBlock.nextConnection.connect(nextStmt.previousConnection);
            prevBlock = nextStmt;
          }
        }
      }
    }
  }

  block.render();
  return block;
};

// ---- Statement Conversion ----

/**
 * Convert a YAIL expression that is used in statement position.
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {Object} node AST node
 * @return {?Blockly.Block}
 * @private
 */
AI.YailToBlocks.convertStatement_ = function(workspace, node) {
  var head = AI.SExprParser.formHead(node);
  if (!head) {
    // Not a list form — try as expression and wrap if needed
    return null;
  }

  switch (head) {
    case 'set-var!':
      return AI.YailToBlocks.convertSetVar_(workspace, node, true);
    case 'set-lexical!':
      return AI.YailToBlocks.convertSetLexical_(workspace, node);
    case 'set-and-coerce-property!':
      return AI.YailToBlocks.convertSetProperty_(workspace, node);
    case 'set-and-coerce-property-and-check!':
      return AI.YailToBlocks.convertGenericSetProperty_(workspace, node);
    case 'call-component-method':
      return AI.YailToBlocks.convertMethodCall_(workspace, node, false);
    case 'call-component-type-method':
      return AI.YailToBlocks.convertGenericMethodCall_(workspace, node, false);
    case 'call-yail-primitive':
      return AI.YailToBlocks.convertPrimitive_(workspace, node, false);
    case 'if':
      return AI.YailToBlocks.convertIf_(workspace, node, false);
    case 'while':
      return AI.YailToBlocks.convertWhile_(workspace, node);
    case 'foreach':
      return AI.YailToBlocks.convertForEach_(workspace, node);
    case 'forrange':
      return AI.YailToBlocks.convertForRange_(workspace, node);
    case 'begin':
      return AI.YailToBlocks.convertBeginStatements_(workspace, node);
    case 'let':
      return AI.YailToBlocks.convertLet_(workspace, node, false);
    case 'break':
    case '*yail-break*':
      return AI.YailToBlocks.convertBreak_(workspace);
    case 'call-component-method-with-blocking-continuation':
      return AI.YailToBlocks.convertMethodCall_(workspace, node, false);
    case 'call-component-type-method-with-blocking-continuation':
      return AI.YailToBlocks.convertGenericMethodCall_(workspace, node, false);
    default:
      // Try as expression (some expressions can be used as statements)
      var exprBlock = AI.YailToBlocks.convertExpression_(workspace, node);
      return exprBlock;
  }
};

// ---- Expression Conversion ----

/**
 * Convert a YAIL expression to a Blockly block with an output connection.
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {Object} node AST node
 * @return {?Blockly.Block}
 * @private
 */
AI.YailToBlocks.convertExpression_ = function(workspace, node) {
  if (!node) return null;

  switch (node.type) {
    case 'number':
      return AI.YailToBlocks.makeNumberBlock_(workspace, node.value);
    case 'string':
      return AI.YailToBlocks.makeTextBlock_(workspace, node.value);
    case 'boolean':
      return AI.YailToBlocks.makeBoolBlock_(workspace, node.value);
    case 'symbol':
      return AI.YailToBlocks.convertSymbolExpr_(workspace, node);
    case 'quoted':
      return AI.YailToBlocks.convertQuotedExpr_(workspace, node);
    case 'list':
      return AI.YailToBlocks.convertListExpr_(workspace, node);
    default:
      return null;
  }
};

/**
 * Convert a symbol used as an expression.
 * @private
 */
AI.YailToBlocks.convertSymbolExpr_ = function(workspace, node) {
  var name = node.name;

  // Boolean constants
  if (name === '#t') return AI.YailToBlocks.makeBoolBlock_(workspace, true);
  if (name === '#f') return AI.YailToBlocks.makeBoolBlock_(workspace, false);

  // Null value constant
  if (name === '*the-null-value*') {
    var block = workspace.newBlock('controls_nothing');
    block.initSvg();
    return block;
  }

  // Global variable reference (sometimes appears as bare symbol)
  if (name.startsWith('g$')) {
    return AI.YailToBlocks.makeVarGetBlock_(workspace, 'global ' + name.substring(2));
  }

  // Number if parseable
  var num = parseFloat(name);
  if (!isNaN(num)) {
    return AI.YailToBlocks.makeNumberBlock_(workspace, num);
  }

  // LLM tolerance: check if bare symbol matches a known global variable
  if (AI.YailToBlocks.isGlobalVariable_(workspace, name)) {
    return AI.YailToBlocks.makeVarGetBlock_(workspace, 'global ' + name);
  }

  // Fallback: treat as text
  return AI.YailToBlocks.makeTextBlock_(workspace, name);
};

/**
 * Convert a quoted expression.
 * @private
 */
AI.YailToBlocks.convertQuotedExpr_ = function(workspace, node) {
  var inner = node.inner;
  // '() — empty list
  if (inner.type === 'list' && inner.elements.length === 0) {
    return AI.YailToBlocks.makeEmptyListBlock_(workspace);
  }
  // '(*list*) — empty YAIL list
  if (inner.type === 'list' && inner.elements.length === 1
      && inner.elements[0].type === 'symbol' && inner.elements[0].name === '*list*') {
    return AI.YailToBlocks.makeEmptyListBlock_(workspace);
  }
  // Quoted symbol — treat as text literal
  if (inner.type === 'symbol') {
    return AI.YailToBlocks.makeTextBlock_(workspace, inner.name);
  }
  // Quoted list of types — ignore (type annotations)
  return null;
};

/**
 * Convert a list expression (function call or special form).
 * @private
 */
AI.YailToBlocks.convertListExpr_ = function(workspace, node) {
  var head = AI.SExprParser.formHead(node);
  if (!head) {
    // Nested list that starts with another list — procedure call: ((get-var p$X) args...)
    if (node.elements.length > 0 && node.elements[0].type === 'list') {
      return AI.YailToBlocks.convertProcedureCall_(workspace, node);
    }
    return null;
  }

  switch (head) {
    case 'get-var':
      return AI.YailToBlocks.convertGetVar_(workspace, node);
    case 'lexical-value':
      return AI.YailToBlocks.convertLexicalValue_(workspace, node);
    case 'get-property':
      return AI.YailToBlocks.convertGetProperty_(workspace, node);
    case 'get-property-and-check':
      return AI.YailToBlocks.convertGenericGetProperty_(workspace, node);
    case 'set-and-coerce-property-and-check!':
      return AI.YailToBlocks.convertGenericSetProperty_(workspace, node);
    case 'call-component-method':
      return AI.YailToBlocks.convertMethodCall_(workspace, node, true);
    case 'call-yail-primitive':
      return AI.YailToBlocks.convertPrimitive_(workspace, node, true);
    case 'if':
      return AI.YailToBlocks.convertIf_(workspace, node, true);
    case 'and-delayed':
      return AI.YailToBlocks.convertLogicOp_(workspace, node, 'AND');
    case 'or-delayed':
      return AI.YailToBlocks.convertLogicOp_(workspace, node, 'OR');
    case 'begin':
      return AI.YailToBlocks.convertBeginExpr_(workspace, node);
    case 'let':
      return AI.YailToBlocks.convertLet_(workspace, node, true);
    case 'get-component':
      return AI.YailToBlocks.convertGetComponent_(workspace, node);
    case 'get-all-components':
      return AI.YailToBlocks.convertGetAllComponents_(workspace, node);
    case 'call-component-type-method':
      return AI.YailToBlocks.convertGenericMethodCall_(workspace, node, true);
    case 'static-field':
      return AI.YailToBlocks.convertStaticField_(workspace, node);
    case 'set-var!':
      return AI.YailToBlocks.convertSetVar_(workspace, node, false);
    case 'set-and-coerce-property!':
      return AI.YailToBlocks.convertSetProperty_(workspace, node);
    case 'call-component-method-with-blocking-continuation':
      return AI.YailToBlocks.convertMethodCall_(workspace, node, true);
    case 'call-component-type-method-with-blocking-continuation':
      return AI.YailToBlocks.convertGenericMethodCall_(workspace, node, true);
    case 'protect-enum':
      return AI.YailToBlocks.convertProtectEnum_(workspace, node);
    case 'map_nondest':
    case 'filter_nondest':
    case 'sortkey_nondest':
    case 'sortcomparator_nondest':
    case 'mincomparator-nondest':
    case 'maxcomparator-nondest':
    case 'reduceovereach':
      return AI.YailToBlocks.convertHigherOrderForm_(workspace, node);
    case 'make-exact-yail-integer':
      // (make-exact-yail-integer N) — check for known color constants, else unwrap
      if (node.elements.length >= 2) {
        var colorBlock = AI.YailToBlocks.tryMakeColorBlock_(workspace, node.elements[1]);
        if (colorBlock) return colorBlock;
        return AI.YailToBlocks.convertExpression_(workspace, node.elements[1]);
      }
      return null;
    default:
      // LLM tolerance: check for shortened primitive forms like (+ a b)
      // instead of full (call-yail-primitive + (*list-for-runtime* a b) '(number number) "+")
      var primInfo = AI.YailToBlocks.PRIMITIVE_MAP_[head];
      if (primInfo) {
        return AI.YailToBlocks.convertShortPrimitive_(workspace, node, primInfo);
      }
      return null;
  }
};

/**
 * Convert a shortened primitive form like (+ a b) where the LLM omitted
 * the full call-yail-primitive wrapper.
 * @private
 */
AI.YailToBlocks.convertShortPrimitive_ = function(workspace, node, info) {
  var args = node.elements.slice(1);

  var block = workspace.newBlock(info.block);
  if (info.mode && block.getField && block.getField('OP')) {
    block.setFieldValue(info.mode, 'OP');
  }

  // For variadic blocks, set up mutation with the correct item count before initSvg
  if (info.arity === 'variadic' && block.domToMutation) {
    var mutation = document.createElement('mutation');
    mutation.setAttribute('items', String(args.length));
    block.domToMutation(mutation);
  }

  block.initSvg();

  // Connect arguments to inputs
  var inputNames = AI.YailToBlocks.getInputNames_(info, args.length, block);
  for (var i = 0; i < args.length && i < inputNames.length; i++) {
    var argBlock = AI.YailToBlocks.convertExpression_(workspace, args[i]);
    if (argBlock && block.getInput(inputNames[i])) {
      block.getInput(inputNames[i]).connection.connect(argBlock.outputConnection);
    }
  }

  return block;
};

// ---- Specific converters ----

/** @private */
AI.YailToBlocks.convertGetVar_ = function(workspace, node) {
  var varName = node.elements[1].name;
  if (varName.startsWith('g$')) {
    return AI.YailToBlocks.makeVarGetBlock_(workspace, 'global ' + varName.substring(2));
  }
  if (varName.startsWith('p$')) {
    // This is a procedure reference for call — handled by parent
    return null;
  }
  return AI.YailToBlocks.makeVarGetBlock_(workspace, varName);
};

/** @private */
AI.YailToBlocks.convertLexicalValue_ = function(workspace, node) {
  var varName = node.elements[1].name.replace(/^\$(?:param_|local_)?/, '');
  return AI.YailToBlocks.makeVarGetBlock_(workspace, varName);
};

/** @private */
AI.YailToBlocks.convertSetVar_ = function(workspace, node, isStatement) {
  var els = node.elements;
  var varName = els[1].name;
  var value = els[2];

  var block = workspace.newBlock('lexical_variable_set');
  if (varName.startsWith('g$')) {
    block.setFieldValue('global ' + varName.substring(2), 'VAR');
  } else {
    block.setFieldValue(varName, 'VAR');
  }
  block.initSvg();

  var valueBlock = AI.YailToBlocks.convertExpression_(workspace, value);
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }
  return block;
};

/** @private */
AI.YailToBlocks.convertSetLexical_ = function(workspace, node) {
  var els = node.elements;
  var varName = els[1].name.replace(/^\$(?:param_|local_)?/, '');
  var value = els[2];

  var block = workspace.newBlock('lexical_variable_set');
  block.setFieldValue(varName, 'VAR');
  block.initSvg();

  var valueBlock = AI.YailToBlocks.convertExpression_(workspace, value);
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }
  return block;
};

/** @private */
AI.YailToBlocks.convertGetProperty_ = function(workspace, node) {
  var els = node.elements;
  // (get-property 'ComponentName 'PropertyName)
  var componentName = AI.YailToBlocks.unquoteSymbol_(els[1]);
  var propertyName = AI.YailToBlocks.unquoteSymbol_(els[2]);

  var componentDb = workspace.getComponentDatabase();
  var componentType = '';
  if (componentDb) {
    var typeInfo = componentDb.instanceNameToTypeName(componentName);
    if (typeInfo) componentType = typeInfo;
  }

  var block = workspace.newBlock('component_set_get');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  mutation.setAttribute('set_or_get', 'get');
  mutation.setAttribute('property_name', propertyName);
  mutation.setAttribute('instance_name', componentName);
  mutation.setAttribute('is_generic', 'false');
  block.domToMutation(mutation);
  block.initSvg();
  return block;
};

/** @private */
AI.YailToBlocks.convertSetProperty_ = function(workspace, node) {
  var els = node.elements;
  // (set-and-coerce-property! 'ComponentName 'PropertyName value 'type)
  var componentName = AI.YailToBlocks.unquoteSymbol_(els[1]);
  var propertyName = AI.YailToBlocks.unquoteSymbol_(els[2]);
  var value = els[3];

  var componentDb = workspace.getComponentDatabase();
  var componentType = '';
  if (componentDb) {
    var typeInfo = componentDb.instanceNameToTypeName(componentName);
    if (typeInfo) componentType = typeInfo;
  }

  var block = workspace.newBlock('component_set_get');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  mutation.setAttribute('set_or_get', 'set');
  mutation.setAttribute('property_name', propertyName);
  mutation.setAttribute('instance_name', componentName);
  mutation.setAttribute('is_generic', 'false');
  block.domToMutation(mutation);
  block.initSvg();

  var valueBlock = AI.YailToBlocks.convertExpression_(workspace, value);
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }
  return block;
};

/**
 * Convert (get-property-and-check componentExpr 'TypeName 'PropertyName)
 * to a generic component_set_get block with is_generic='true'.
 * @private
 */
AI.YailToBlocks.convertGenericGetProperty_ = function(workspace, node) {
  var els = node.elements;
  // (get-property-and-check componentExpr 'TypeName 'PropertyName)
  var componentExpr = els[1];
  var typeName = AI.YailToBlocks.unquoteSymbol_(els[2]);
  var propertyName = AI.YailToBlocks.unquoteSymbol_(els[3]);

  var block = workspace.newBlock('component_set_get');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', typeName);
  mutation.setAttribute('set_or_get', 'get');
  mutation.setAttribute('property_name', propertyName);
  mutation.setAttribute('is_generic', 'true');
  block.domToMutation(mutation);
  block.initSvg();

  var compBlock = AI.YailToBlocks.convertExpression_(workspace, componentExpr);
  if (compBlock && block.getInput('COMPONENT')) {
    block.getInput('COMPONENT').connection.connect(compBlock.outputConnection);
  }
  return block;
};

/**
 * Convert (set-and-coerce-property-and-check! componentExpr 'TypeName 'PropertyName value 'type)
 * to a generic component_set_get block with is_generic='true'.
 * @private
 */
AI.YailToBlocks.convertGenericSetProperty_ = function(workspace, node) {
  var els = node.elements;
  // (set-and-coerce-property-and-check! componentExpr 'TypeName 'PropertyName value 'type)
  var componentExpr = els[1];
  var typeName = AI.YailToBlocks.unquoteSymbol_(els[2]);
  var propertyName = AI.YailToBlocks.unquoteSymbol_(els[3]);
  var value = els[4];

  var block = workspace.newBlock('component_set_get');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', typeName);
  mutation.setAttribute('set_or_get', 'set');
  mutation.setAttribute('property_name', propertyName);
  mutation.setAttribute('is_generic', 'true');
  block.domToMutation(mutation);
  block.initSvg();

  var compBlock = AI.YailToBlocks.convertExpression_(workspace, componentExpr);
  if (compBlock && block.getInput('COMPONENT')) {
    block.getInput('COMPONENT').connection.connect(compBlock.outputConnection);
  }

  var valueBlock = AI.YailToBlocks.convertExpression_(workspace, value);
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }
  return block;
};

/** @private */
AI.YailToBlocks.convertMethodCall_ = function(workspace, node, asExpression) {
  var els = node.elements;
  // (call-component-method 'ComponentName 'MethodName (*list-for-runtime* args...) '(types...))
  var componentName = AI.YailToBlocks.unquoteSymbol_(els[1]);
  var methodName = AI.YailToBlocks.unquoteSymbol_(els[2]);

  // Look up the component type from the workspace's component database
  var componentDb = workspace.getComponentDatabase();
  var componentType = '';
  if (componentDb) {
    var typeInfo = componentDb.instanceNameToTypeName(componentName);
    if (typeInfo) {
      componentType = typeInfo;
    }
  }

  var block = workspace.newBlock('component_method');

  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  mutation.setAttribute('method_name', methodName);
  mutation.setAttribute('instance_name', componentName);
  mutation.setAttribute('is_generic', 'false');
  block.domToMutation(mutation);
  block.initSvg();

  // Connect arguments
  if (els.length > 3 && AI.SExprParser.isForm(els[3], '*list-for-runtime*')) {
    var argList = els[3].elements;
    for (var i = 1; i < argList.length; i++) {
      var argBlock = AI.YailToBlocks.convertExpression_(workspace, argList[i]);
      if (argBlock) {
        var inputName = 'ARG' + (i - 1);
        if (block.getInput(inputName)) {
          block.getInput(inputName).connection.connect(argBlock.outputConnection);
        }
      }
    }
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertProcedureCall_ = function(workspace, node) {
  var els = node.elements;
  // ((get-var p$name) arg1 arg2 ...)
  var getVarForm = els[0];
  if (!AI.SExprParser.isForm(getVarForm, 'get-var')) return null;
  var procRef = getVarForm.elements[1].name;
  if (!procRef.startsWith('p$')) return null;
  var procName = procRef.substring(2);

  // Try to find if this procedure returns a value
  var blockType = 'procedures_callnoreturn';
  var topBlocks = workspace.getTopBlocks(false);
  for (var i = 0; i < topBlocks.length; i++) {
    if (topBlocks[i].type === 'procedures_defreturn'
        && topBlocks[i].getFieldValue('NAME') === procName) {
      blockType = 'procedures_callreturn';
      break;
    }
  }

  var block = workspace.newBlock(blockType);

  // Build mutation to ensure ARG inputs are created.
  // FieldProcedure.onChange may not find the definition if it hasn't been
  // registered in the procedure database yet, so we set up the mutation
  // explicitly with the correct argument count.
  var argCount = els.length - 1;
  var mutation = document.createElement('mutation');
  mutation.setAttribute('name', procName);
  // Try to get parameter names from the procedure definition block
  var paramNames = [];
  for (var i = 0; i < topBlocks.length; i++) {
    if ((topBlocks[i].type === 'procedures_defreturn' ||
         topBlocks[i].type === 'procedures_defnoreturn') &&
        topBlocks[i].getFieldValue('NAME') === procName &&
        topBlocks[i].arguments_) {
      paramNames = topBlocks[i].arguments_;
      break;
    }
  }
  for (var i = 0; i < argCount; i++) {
    var arg = document.createElement('arg');
    arg.setAttribute('name', paramNames[i] || ('x' + i));
    mutation.appendChild(arg);
  }
  block.domToMutation(mutation);
  block.initSvg();

  // Connect arguments
  for (var i = 1; i < els.length; i++) {
    var argBlock = AI.YailToBlocks.convertExpression_(workspace, els[i]);
    if (argBlock) {
      var inputName = 'ARG' + (i - 1);
      if (block.getInput(inputName)) {
        block.getInput(inputName).connection.connect(argBlock.outputConnection);
      }
    }
  }

  return block;
};

// ---- Primitive dispatch ----

/**
 * Primitive name to Blockly block type mapping.
 * @private
 */
AI.YailToBlocks.PRIMITIVE_MAP_ = {
  // Math — basic arithmetic
  '+': {block: 'math_add', arity: 'variadic'},
  '-': {block: 'math_subtract', arity: 2},
  '*': {block: 'math_multiply', arity: 'variadic'},
  'yail-divide': {block: 'math_division', arity: 2},
  '/': {block: 'math_division', arity: 2},
  'expt': {block: 'math_power', arity: 2},
  'modulo': {block: 'math_modulo', arity: 2},
  'remainder': {block: 'math_remainder', arity: 2},
  'quotient': {block: 'math_quotient', arity: 2},

  // Math — single-argument operations
  'sqrt': {block: 'math_single', mode: 'ROOT', arity: 1},
  'abs': {block: 'math_single', mode: 'ABS', arity: 1},
  'log': {block: 'math_single', mode: 'LN', arity: 1},
  'exp': {block: 'math_single', mode: 'EXP', arity: 1},
  'yail-round': {block: 'math_single', mode: 'ROUND', arity: 1},
  'yail-ceiling': {block: 'math_single', mode: 'CEILING', arity: 1},
  'yail-floor': {block: 'math_single', mode: 'FLOOR', arity: 1},

  // Math — bitwise operations
  'bitwise-and': {block: 'math_bitwise', mode: 'BITAND', arity: 'variadic'},
  'bitwise-ior': {block: 'math_bitwise', mode: 'BITIOR', arity: 'variadic'},
  'bitwise-xor': {block: 'math_bitwise', mode: 'BITXOR', arity: 'variadic'},

  // Math — trigonometry
  'sin-degrees': {block: 'math_trig', mode: 'SIN', arity: 1},
  'cos-degrees': {block: 'math_trig', mode: 'COS', arity: 1},
  'tan-degrees': {block: 'math_trig', mode: 'TAN', arity: 1},
  'asin-degrees': {block: 'math_trig', mode: 'ASIN', arity: 1},
  'acos-degrees': {block: 'math_trig', mode: 'ACOS', arity: 1},
  'atan-degrees': {block: 'math_trig', mode: 'ATAN', arity: 1},
  'atan2-degrees': {block: 'math_atan2', arity: 2},

  // Math — random
  'random-integer': {block: 'math_random_int', arity: 2},
  'random-fraction': {block: 'math_random_float', arity: 0},
  'random-set-seed': {block: 'math_random_seed', arity: 1},

  // Math — min/max (variadic, two-input version)
  'min': {block: 'math_on_list', mode: 'MIN', arity: 'variadic'},
  'max': {block: 'math_on_list', mode: 'MAX', arity: 'variadic'},
  'yail-min': {block: 'math_on_list', mode: 'MIN', arity: 'variadic'},  // alias
  'yail-max': {block: 'math_on_list', mode: 'MAX', arity: 'variadic'},  // alias

  // Math — list statistics (single list input)
  'avg': {block: 'math_on_list2', mode: 'AVG', arity: 1},
  'minl': {block: 'math_on_list2', mode: 'MIN', arity: 1},
  'maxl': {block: 'math_on_list2', mode: 'MAX', arity: 1},
  'gm': {block: 'math_on_list2', mode: 'GM', arity: 1},
  'std-dev': {block: 'math_on_list2', mode: 'SD', arity: 1},
  'std-err': {block: 'math_on_list2', mode: 'SE', arity: 1},
  'mode': {block: 'math_mode_of_list', arity: 1},

  // Math — angle conversion
  'radians->degrees': {block: 'math_convert_angles', mode: 'RADIANS_TO_DEGREES', arity: 1},
  'degrees->radians': {block: 'math_convert_angles', mode: 'DEGREES_TO_RADIANS', arity: 1},

  // Math — number/base conversion
  'math-convert-dec-hex': {block: 'math_convert_number', mode: 'DEC_TO_HEX', arity: 1},
  'math-convert-hex-dec': {block: 'math_convert_number', mode: 'HEX_TO_DEC', arity: 1},
  'math-convert-dec-bin': {block: 'math_convert_number', mode: 'DEC_TO_BIN', arity: 1},
  'math-convert-bin-dec': {block: 'math_convert_number', mode: 'BIN_TO_DEC', arity: 1},
  'format-as-decimal': {block: 'math_format_as_decimal', arity: 2},

  // Math — number type checks
  'is-number?': {block: 'math_is_a_number', mode: 'NUMBER', arity: 1},
  'is-base10?': {block: 'math_is_a_number', mode: 'BASE10', arity: 1},
  'is-hexadecimal?': {block: 'math_is_a_number', mode: 'HEXADECIMAL', arity: 1},
  'is-binary?': {block: 'math_is_a_number', mode: 'BINARY', arity: 1},

  // Comparison
  'yail-equal?': {block: 'math_compare', mode: 'EQ', arity: 2},
  'yail-not-equal?': {block: 'math_compare', mode: 'NEQ', arity: 2},
  '<': {block: 'math_compare', mode: 'LT', arity: 2},
  '<=': {block: 'math_compare', mode: 'LTE', arity: 2},
  '>': {block: 'math_compare', mode: 'GT', arity: 2},
  '>=': {block: 'math_compare', mode: 'GTE', arity: 2},

  // Logic
  'yail-not': {block: 'logic_negate', arity: 1},
  'not': {block: 'logic_negate', arity: 1},          // alias — used in text NEQ wrapping

  // Text
  'string-append': {block: 'text_join', arity: 'variadic'},
  'string-length': {block: 'text_length', arity: 1},
  'string-empty?': {block: 'text_isEmpty', arity: 1},
  'string-contains': {block: 'text_contains', mode: 'CONTAINS', arity: 2},
  'string-contains-any': {block: 'text_contains', mode: 'CONTAINS_ANY', arity: 2},
  'string-contains-all': {block: 'text_contains', mode: 'CONTAINS_ALL', arity: 2},
  'string-starts-at': {block: 'text_starts_at', arity: 2},
  'string-replace-all': {block: 'text_replace_all', arity: 3},
  'string-replace-mappings-longest-string': {block: 'text_replace_mappings', mode: 'LONGEST_STRING_FIRST', arity: 2},
  'string-replace-mappings-dictionary': {block: 'text_replace_mappings', mode: 'DICTIONARY_ORDER', arity: 2},
  'string-split': {block: 'text_split', mode: 'SPLIT', arity: 2},
  'string-split-at-first': {block: 'text_split', mode: 'SPLITATFIRST', arity: 2},
  'string-split-at-first-of-any': {block: 'text_split', mode: 'SPLITATFIRSTOFANY', arity: 2},
  'string-split-at-any': {block: 'text_split', mode: 'SPLITATANY', arity: 2},
  'string-split-at-spaces': {block: 'text_split_at_spaces', arity: 1},
  'string-trim': {block: 'text_trim', arity: 1},
  'string-substring': {block: 'text_segment', arity: 3},
  'string-reverse': {block: 'text_reverse', arity: 1},
  'string-to-upper-case': {block: 'text_changeCase', mode: 'UPCASE', arity: 1},
  'string-to-lower-case': {block: 'text_changeCase', mode: 'DOWNCASE', arity: 1},
  'upcase': {block: 'text_changeCase', mode: 'UPCASE', arity: 1},      // alias
  'downcase': {block: 'text_changeCase', mode: 'DOWNCASE', arity: 1},  // alias

  // Text — comparison
  'string<?': {block: 'text_compare', mode: 'LT', arity: 2},
  'string>?': {block: 'text_compare', mode: 'GT', arity: 2},
  'string=?': {block: 'text_compare', mode: 'EQUAL', arity: 2},

  // Text — type check and obfuscation
  'string?': {block: 'text_is_string', arity: 1},
  'text-deobfuscate': {block: 'obfuscated_text', arity: 2},

  // Lists
  'make-yail-list': {block: 'lists_create_with', arity: 'variadic'},
  'yail-list-get-item': {block: 'lists_select_item', arity: 2},
  'yail-list-set-item!': {block: 'lists_replace_item', arity: 3},
  'yail-list-length': {block: 'lists_length', arity: 1},
  'yail-list-empty?': {block: 'lists_is_empty', arity: 1},
  'yail-list-add-to-list!': {block: 'lists_add_items', arity: 'variadic'},
  'yail-list-remove-item!': {block: 'lists_remove_item', arity: 2},
  'yail-list-insert-item!': {block: 'lists_insert_item', arity: 3},
  'yail-list-append!': {block: 'lists_append_list', arity: 2},
  'yail-list-append': {block: 'lists_append_list', arity: 2},   // alias (without bang)
  'yail-list-copy': {block: 'lists_copy', arity: 1},
  'yail-list-member?': {block: 'lists_is_in', arity: 2},
  'yail-list-index': {block: 'lists_position_in', arity: 2},
  'yail-list-pick-random': {block: 'lists_pick_random_item', arity: 1},
  'yail-list?': {block: 'lists_is_list', arity: 1},
  'yail-list-is-list?': {block: 'lists_is_list', arity: 1},     // alias
  'yail-list-reverse': {block: 'lists_reverse', arity: 1},
  'yail-list-join-with-separator': {block: 'lists_join_with_separator', arity: 2},
  'yail-list-to-csv-row': {block: 'lists_to_csv_row', arity: 1},
  'yail-list-to-csv-table': {block: 'lists_to_csv_table', arity: 1},
  'yail-list-from-csv-row': {block: 'lists_from_csv_row', arity: 1},
  'yail-list-from-csv-table': {block: 'lists_from_csv_table', arity: 1},
  'yail-alist-lookup': {block: 'lists_lookup_in_pairs', arity: 3},
  'yail-list-sort': {block: 'lists_sort', arity: 1},
  'yail-list-but-first': {block: 'lists_but_first', arity: 1},
  'yail-list-but-last': {block: 'lists_but_last', arity: 1},
  'yail-list-slice': {block: 'lists_slice', arity: 3},

  // Dictionaries
  'make-yail-dictionary': {block: 'dictionaries_create_with', arity: 'variadic'},
  'make-dictionary-pair': {block: 'pair', arity: 2},
  'yail-dictionary-lookup': {block: 'dictionaries_lookup', arity: 3},
  'yail-dictionary-recursive-lookup': {block: 'dictionaries_recursive_lookup', arity: 3},
  'yail-dictionary-set-pair': {block: 'dictionaries_set_pair', arity: 3},
  'yail-dictionary-recursive-set': {block: 'dictionaries_recursive_set', arity: 3},
  'yail-dictionary-delete-pair': {block: 'dictionaries_delete_pair', arity: 2},
  'yail-dictionary-get-keys': {block: 'dictionaries_getters', mode: 'KEYS', arity: 1},
  'yail-dictionary-get-values': {block: 'dictionaries_getters', mode: 'VALUES', arity: 1},
  'yail-dictionary-is-key-in': {block: 'dictionaries_is_key_in', arity: 2},
  'yail-dictionary-is-key-in?': {block: 'dictionaries_is_key_in', arity: 2},  // alias
  'yail-dictionary-length': {block: 'dictionaries_length', arity: 1},
  'yail-dictionary?': {block: 'dictionaries_is_dict', arity: 1},
  'yail-dictionary-is-dict?': {block: 'dictionaries_is_dict', arity: 1},      // alias
  'yail-dictionary-alist-to-dict': {block: 'dictionaries_alist_to_dict', arity: 1},
  'yail-dictionary-dict-to-alist': {block: 'dictionaries_dict_to_alist', arity: 1},
  'yail-dictionary-copy': {block: 'dictionaries_copy', arity: 1},
  'yail-dictionary-combine-dicts': {block: 'dictionaries_combine_dicts', arity: 2},
  'yail-dictionary-walk': {block: 'dictionaries_walk_tree', arity: 2},

  // Colors
  'make-color': {block: 'color_make_color', arity: 1},
  'split-color': {block: 'color_split_color', arity: 1},

  // Screen control
  'open-another-screen': {block: 'controls_openAnotherScreen', arity: 1},
  'open-another-screen-with-start-value': {block: 'controls_openAnotherScreenWithStartValue', arity: 2},
  'close-screen': {block: 'controls_closeScreen', arity: 0},
  'close-screen-with-value': {block: 'controls_closeScreenWithValue', arity: 1},
  'close-screen-with-plain-text': {block: 'controls_closeScreenWithPlainText', arity: 1},
  'close-application': {block: 'controls_closeApplication', arity: 0},
  'get-start-value': {block: 'controls_getStartValue', arity: 0},
  'get-plain-start-text': {block: 'controls_getPlainStartText', arity: 0}
};

/** @private */
AI.YailToBlocks.convertPrimitive_ = function(workspace, node, asExpression) {
  var els = node.elements;
  // (call-yail-primitive name (*list-for-runtime* args...) '(types...) "desc")
  if (els.length < 2) return null;

  var primName = els[1].name;

  // make-exact-yail-integer is a type-coercion wrapper, not a real block.
  // Unwrap it: extract the single argument and return it as a number or color.
  if (primName === 'make-exact-yail-integer' || primName === 'make-exact-yail-real-number') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    if (args.length > 0) {
      var colorBlock = AI.YailToBlocks.tryMakeColorBlock_(workspace, args[0]);
      if (colorBlock) return colorBlock;
      return AI.YailToBlocks.convertExpression_(workspace, args[0]);
    }
    return AI.YailToBlocks.makeNumberBlock_(workspace, 0);
  }

  var info = AI.YailToBlocks.PRIMITIVE_MAP_[primName];
  if (!info) {
    throw new Error('Unknown YAIL primitive: ' + primName);
  }

  // Extract arguments from *list-for-runtime* first (needed for variadic mutation)
  var args = [];
  if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
    var argList = els[2].elements;
    for (var i = 1; i < argList.length; i++) {
      args.push(argList[i]);
    }
  }

  var block = workspace.newBlock(info.block);
  if (info.mode && block.getField && block.getField('OP')) {
    block.setFieldValue(info.mode, 'OP');
  }

  // For variadic blocks, set up mutation with the correct item count before initSvg
  if (info.arity === 'variadic' && block.domToMutation) {
    var mutation = document.createElement('mutation');
    mutation.setAttribute('items', String(args.length));
    block.domToMutation(mutation);
  }

  block.initSvg();

  // Connect arguments to inputs
  // After domToMutation, the block knows its repeatingInputName
  var inputNames = AI.YailToBlocks.getInputNames_(info, args.length, block);
  for (var i = 0; i < args.length && i < inputNames.length; i++) {
    var argBlock = AI.YailToBlocks.convertExpression_(workspace, args[i]);
    if (argBlock && block.getInput(inputNames[i])) {
      block.getInput(inputNames[i]).connection.connect(argBlock.outputConnection);
    }
  }

  return block;
};

/**
 * Get the input names for a primitive block based on its arity.
 * Uses the block's actual inputs to discover names when possible.
 * @private
 */
AI.YailToBlocks.getInputNames_ = function(info, argCount, block) {
  if (info.arity === 'variadic') {
    var prefix = (block && block.repeatingInputName) || 'NUM';
    var names = [];
    for (var i = 0; i < argCount; i++) {
      names.push(prefix + i);
    }
    return names;
  }

  // For fixed-arity blocks, discover input names from the block itself.
  // Different block types use different naming conventions (NUM, TEXT, A/B, ITEM, LIST, etc.)
  if (block && block.inputList) {
    var valueInputs = [];
    for (var i = 0; i < block.inputList.length; i++) {
      var input = block.inputList[i];
      if (input.type === Blockly.INPUT_VALUE) {
        valueInputs.push(input.name);
      }
    }
    if (valueInputs.length >= argCount) {
      return valueInputs.slice(0, argCount);
    }
  }

  // Fallback to common naming patterns
  if (info.arity === 1) {
    return ['NUM'];
  }
  if (info.arity === 2) {
    return ['A', 'B'];
  }
  if (info.arity === 3) {
    return ['A', 'B', 'C'];
  }
  return [];
};

// ---- Control flow ----

/** @private */
AI.YailToBlocks.convertIf_ = function(workspace, node, asExpression) {
  var els = node.elements;
  // (if condition (begin then...) (begin else...))
  // Unwrap nested if chains into elseif

  var conditions = [];
  var bodies = [];
  var elseBody = null;

  var current = node;
  while (AI.SExprParser.isForm(current, 'if')) {
    var curEls = current.elements;
    conditions.push(curEls[1]);
    bodies.push(curEls[2]);
    if (curEls.length > 3) {
      var elsePart = curEls[3];
      if (AI.SExprParser.isForm(elsePart, 'if')) {
        current = elsePart;
      } else {
        elseBody = elsePart;
        break;
      }
    } else {
      break;
    }
  }

  // In expression context with simple if/then/else, use controls_choose
  if (asExpression && conditions.length === 1 && elseBody) {
    var block = workspace.newBlock('controls_choose');
    block.initSvg();

    var condBlock = AI.YailToBlocks.convertExpression_(workspace, conditions[0]);
    if (condBlock && block.getInput('TEST')) {
      block.getInput('TEST').connection.connect(condBlock.outputConnection);
    }
    var thenBlock = AI.YailToBlocks.convertExpression_(workspace, bodies[0]);
    if (thenBlock && block.getInput('THENRETURN')) {
      block.getInput('THENRETURN').connection.connect(thenBlock.outputConnection);
    }
    var elseBlock = AI.YailToBlocks.convertExpression_(workspace, elseBody);
    if (elseBlock && block.getInput('ELSERETURN')) {
      block.getInput('ELSERETURN').connection.connect(elseBlock.outputConnection);
    }
    return block;
  }

  // Statement context or complex if/elseif chains: use controls_if
  var block = workspace.newBlock('controls_if');
  var mutation = document.createElement('mutation');
  var elseifCount = conditions.length - 1;
  if (elseifCount > 0) mutation.setAttribute('elseif', String(elseifCount));
  if (elseBody) mutation.setAttribute('else', '1');
  block.domToMutation(mutation);
  block.initSvg();

  // Connect conditions and bodies
  for (var i = 0; i < conditions.length; i++) {
    var condBlock = AI.YailToBlocks.convertExpression_(workspace, conditions[i]);
    if (condBlock && block.getInput('IF' + i)) {
      block.getInput('IF' + i).connection.connect(condBlock.outputConnection);
    }

    var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(workspace, bodies[i]);
    if (bodyBlock && block.getInput('DO' + i)) {
      block.getInput('DO' + i).connection.connect(bodyBlock.previousConnection);
    }
  }

  if (elseBody) {
    var elseBlock = AI.YailToBlocks.convertBeginToStatements_(workspace, elseBody);
    if (elseBlock && block.getInput('ELSE')) {
      block.getInput('ELSE').connection.connect(elseBlock.previousConnection);
    }
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertWhile_ = function(workspace, node) {
  var els = node.elements;
  // (while (begin test) (begin body))
  var block = workspace.newBlock('controls_while');
  block.initSvg();

  var testExpr = els[1];
  if (AI.SExprParser.isForm(testExpr, 'begin') && testExpr.elements.length >= 2) {
    testExpr = testExpr.elements[testExpr.elements.length - 1];
  }
  var testBlock = AI.YailToBlocks.convertExpression_(workspace, testExpr);
  if (testBlock && block.getInput('TEST')) {
    block.getInput('TEST').connection.connect(testBlock.outputConnection);
  }

  var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(workspace, els[2]);
  if (bodyBlock && block.getInput('DO')) {
    block.getInput('DO').connection.connect(bodyBlock.previousConnection);
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertForEach_ = function(workspace, node) {
  var els = node.elements;
  // Dictionary foreach: (foreach $item (let (($key ...) ($value ...)) body) dictExpr)
  // List foreach:       (foreach $item (begin body) listExpr)

  // Detect dictionary foreach by checking if body is a let with two bindings
  var bodyNode = els[2];
  if (AI.SExprParser.isForm(bodyNode, 'let') && bodyNode.elements.length >= 3) {
    var bindings = bodyNode.elements[1];
    if (bindings.type === 'list' && bindings.elements.length === 2) {
      return AI.YailToBlocks.convertForEachDict_(workspace, node, bodyNode);
    }
  }

  var block = workspace.newBlock('controls_forEach');
  var itemName = els[1].name.replace(/^\$(?:param_|local_)?/, '');
  block.setFieldValue(itemName, 'VAR');
  block.initSvg();

  if (els.length > 3) {
    var listBlock = AI.YailToBlocks.convertExpression_(workspace, els[3]);
    if (listBlock && block.getInput('LIST')) {
      block.getInput('LIST').connection.connect(listBlock.outputConnection);
    }
  }

  var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(workspace, bodyNode);
  if (bodyBlock && block.getInput('DO')) {
    block.getInput('DO').connection.connect(bodyBlock.previousConnection);
  }

  return block;
};

/**
 * Convert a dictionary foreach.
 * YAIL: (foreach $item (let (($key getKey) ($value getValue)) body...) dictExpr)
 * Block: controls_for_each_dict with fields KEY, VALUE and inputs DICT, DO
 * @private
 */
AI.YailToBlocks.convertForEachDict_ = function(workspace, node, letNode) {
  var els = node.elements;
  var bindings = letNode.elements[1];

  // Extract key and value names from the let bindings
  var keyName = 'key';
  var valueName = 'value';
  if (bindings.elements[0].type === 'list' && bindings.elements[0].elements.length >= 1) {
    keyName = bindings.elements[0].elements[0].name.replace(/^\$(?:param_|local_)?/, '').replace(/^local_/, '');
  }
  if (bindings.elements[1].type === 'list' && bindings.elements[1].elements.length >= 1) {
    valueName = bindings.elements[1].elements[0].name.replace(/^\$(?:param_|local_)?/, '').replace(/^local_/, '');
  }

  var block = workspace.newBlock('controls_for_each_dict');
  block.setFieldValue(keyName, 'KEY');
  block.setFieldValue(valueName, 'VALUE');
  block.initSvg();

  // Connect dictionary expression
  if (els.length > 3) {
    var dictBlock = AI.YailToBlocks.convertExpression_(workspace, els[3]);
    if (dictBlock && block.getInput('DICT')) {
      block.getInput('DICT').connection.connect(dictBlock.outputConnection);
    }
  }

  // Connect body — the let's body is letNode.elements[2]
  var innerBody = letNode.elements[2];
  var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(workspace, innerBody);
  if (bodyBlock && block.getInput('DO')) {
    block.getInput('DO').connection.connect(bodyBlock.previousConnection);
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertForRange_ = function(workspace, node) {
  var els = node.elements;
  // (forrange $i (begin body) start end step)
  var block = workspace.newBlock('controls_forRange');
  var varName = els[1].name.replace(/^\$(?:param_|local_)?/, '');
  block.setFieldValue(varName, 'VAR');
  block.initSvg();

  if (els.length > 4) {
    var startBlock = AI.YailToBlocks.convertExpression_(workspace, els[3]);
    if (startBlock && block.getInput('START')) {
      block.getInput('START').connection.connect(startBlock.outputConnection);
    }
    var endBlock = AI.YailToBlocks.convertExpression_(workspace, els[4]);
    if (endBlock && block.getInput('END')) {
      block.getInput('END').connection.connect(endBlock.outputConnection);
    }
  }
  if (els.length > 5) {
    var stepBlock = AI.YailToBlocks.convertExpression_(workspace, els[5]);
    if (stepBlock && block.getInput('STEP')) {
      block.getInput('STEP').connection.connect(stepBlock.outputConnection);
    }
  }

  var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(workspace, els[2]);
  if (bodyBlock && block.getInput('DO')) {
    block.getInput('DO').connection.connect(bodyBlock.previousConnection);
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertBreak_ = function(workspace) {
  var block = workspace.newBlock('controls_break');
  block.initSvg();
  return block;
};

/** @private */
AI.YailToBlocks.convertLogicOp_ = function(workspace, node, op) {
  var els = node.elements;
  var block = workspace.newBlock('logic_operation');
  block.setFieldValue(op, 'OP');
  block.initSvg();

  if (els.length > 1) {
    var aBlock = AI.YailToBlocks.convertExpression_(workspace, els[1]);
    if (aBlock && block.getInput('A')) {
      block.getInput('A').connection.connect(aBlock.outputConnection);
    }
  }
  if (els.length > 2) {
    // The second arg is often wrapped in (begin ...)
    var bExpr = els[2];
    if (AI.SExprParser.isForm(bExpr, 'begin') && bExpr.elements.length >= 2) {
      bExpr = bExpr.elements[bExpr.elements.length - 1];
    }
    var bBlock = AI.YailToBlocks.convertExpression_(workspace, bExpr);
    if (bBlock && block.getInput('B')) {
      block.getInput('B').connection.connect(bBlock.outputConnection);
    }
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertLet_ = function(workspace, node, asExpression) {
  // (let (($var1 val1) ($var2 val2) ...) body)
  var els = node.elements;
  if (els.length < 3) return null;

  var bindingsList = els[1]; // list of (name value) pairs
  if (bindingsList.type !== 'list') return null;

  var blockType = asExpression
      ? 'local_declaration_expression'
      : 'local_declaration_statement';
  var block = workspace.newBlock(blockType);

  // Extract variable names for mutation
  var varNames = [];
  for (var i = 0; i < bindingsList.elements.length; i++) {
    var binding = bindingsList.elements[i];
    if (binding.type === 'list' && binding.elements.length >= 1) {
      var rawName = binding.elements[0].name || String(binding.elements[0].value);
      // Strip $ prefix and optional local_ prefix
      var name = rawName.replace(/^\$(?:param_|local_)?/, '').replace(/^local_/, '');
      varNames.push(name);
    }
  }

  // Set mutation with localname children
  var mutation = document.createElement('mutation');
  for (var i = 0; i < varNames.length; i++) {
    var localname = document.createElement('localname');
    localname.setAttribute('name', varNames[i]);
    mutation.appendChild(localname);
  }
  block.domToMutation(mutation);
  block.initSvg();

  // Connect initializer values to DECL0, DECL1, ...
  for (var i = 0; i < bindingsList.elements.length; i++) {
    var binding = bindingsList.elements[i];
    if (binding.type === 'list' && binding.elements.length >= 2) {
      var valBlock = AI.YailToBlocks.convertExpression_(workspace, binding.elements[1]);
      if (valBlock && block.getInput('DECL' + i)) {
        block.getInput('DECL' + i).connection.connect(valBlock.outputConnection);
      }
    }
  }

  // Connect body
  var bodyNode = els[2];
  if (asExpression) {
    var bodyBlock = AI.YailToBlocks.convertExpression_(workspace, bodyNode);
    if (bodyBlock && block.getInput('RETURN')) {
      block.getInput('RETURN').connection.connect(bodyBlock.outputConnection);
    }
  } else {
    var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(workspace, bodyNode);
    if (bodyBlock && block.getInput('STACK')) {
      block.getInput('STACK').connection.connect(bodyBlock.previousConnection);
    }
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertGetComponent_ = function(workspace, node) {
  var els = node.elements;
  var componentName = els[1].name || String(els[1].value);

  var componentDb = workspace.getComponentDatabase();
  var componentType = '';
  if (componentDb) {
    var typeInfo = componentDb.instanceNameToTypeName(componentName);
    if (typeInfo) componentType = typeInfo;
  }

  var block = workspace.newBlock('component_component_block');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  mutation.setAttribute('instance_name', componentName);
  block.domToMutation(mutation);
  block.initSvg();
  return block;
};

/**
 * Convert (get-all-components FQCN) to component_all_component_block.
 * @private
 */
AI.YailToBlocks.convertGetAllComponents_ = function(workspace, node) {
  var els = node.elements;
  // (get-all-components com.google.appinventor.components.runtime.Button)
  var fqcn = els[1].name || String(els[1].value);
  // Extract short type name from FQCN
  var componentType = fqcn.substring(fqcn.lastIndexOf('.') + 1);

  var block = workspace.newBlock('component_all_component_block');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  block.domToMutation(mutation);
  block.initSvg();
  return block;
};

/**
 * Convert (call-component-type-method component 'typeName 'methodName args types)
 * to a generic component_method block.
 * @private
 */
AI.YailToBlocks.convertGenericMethodCall_ = function(workspace, node, asExpression) {
  var els = node.elements;
  // (call-component-type-method <componentExpr> 'TypeName 'MethodName (*list-for-runtime* args...) '(types...))
  var componentExpr = els[1];
  var typeName = AI.YailToBlocks.unquoteSymbol_(els[2]);
  var methodName = AI.YailToBlocks.unquoteSymbol_(els[3]);

  var block = workspace.newBlock('component_method');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', typeName);
  mutation.setAttribute('method_name', methodName);
  mutation.setAttribute('is_generic', 'true');
  block.domToMutation(mutation);
  block.initSvg();

  // Connect the component expression to the COMPONENT input
  var compBlock = AI.YailToBlocks.convertExpression_(workspace, componentExpr);
  if (compBlock && block.getInput('COMPONENT')) {
    block.getInput('COMPONENT').connection.connect(compBlock.outputConnection);
  }

  // Connect arguments
  if (els.length > 4 && AI.SExprParser.isForm(els[4], '*list-for-runtime*')) {
    var argList = els[4].elements;
    for (var i = 1; i < argList.length; i++) {
      var argBlock = AI.YailToBlocks.convertExpression_(workspace, argList[i]);
      if (argBlock) {
        var inputName = 'ARG' + (i - 1);
        if (block.getInput(inputName)) {
          block.getInput(inputName).connection.connect(argBlock.outputConnection);
        }
      }
    }
  }

  return block;
};

/**
 * Convert (static-field className "enumName") to helpers_dropdown.
 * @private
 */
AI.YailToBlocks.convertStaticField_ = function(workspace, node) {
  var els = node.elements;
  // (static-field com.google.appinventor.components.runtime.util.LineOfBestFit "Slope")
  if (els.length < 3) return null;

  var className = els[1].name || String(els[1].value);
  var enumName = els[2].value || els[2].name || String(els[2].value);

  // Try to find the OptionList key from the component database
  var block = workspace.newBlock('helpers_dropdown');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('key', className);
  block.domToMutation(mutation);
  block.initSvg();

  // Set the OPTION field value
  if (block.getField && block.getField('OPTION')) {
    block.setFieldValue(enumName, 'OPTION');
  }

  return block;
};

// ---- Begin block helpers ----

/**
 * Convert a (begin ...) to chained statement blocks.
 * @private
 */
AI.YailToBlocks.convertBeginToStatements_ = function(workspace, node) {
  if (!AI.SExprParser.isForm(node, 'begin')) {
    // Single expression used as statement
    return AI.YailToBlocks.convertStatement_(workspace, node);
  }

  var stmts = node.elements.slice(1); // skip 'begin' symbol
  if (stmts.length === 0) return null;

  var firstBlock = AI.YailToBlocks.convertStatement_(workspace, stmts[0]);
  if (!firstBlock) return null;

  var prevBlock = firstBlock;
  for (var i = 1; i < stmts.length; i++) {
    var nextBlock = AI.YailToBlocks.convertStatement_(workspace, stmts[i]);
    if (nextBlock && prevBlock.nextConnection && nextBlock.previousConnection) {
      prevBlock.nextConnection.connect(nextBlock.previousConnection);
      prevBlock = nextBlock;
    }
  }

  return firstBlock;
};

/** @private */
AI.YailToBlocks.convertBeginStatements_ = function(workspace, node) {
  return AI.YailToBlocks.convertBeginToStatements_(workspace, node);
};

/** @private */
AI.YailToBlocks.convertBeginExpr_ = function(workspace, node) {
  var els = node.elements;
  if (els.length < 2) return null;

  // Single element: just return it as expression
  if (els.length === 2) {
    return AI.YailToBlocks.convertExpression_(workspace, els[1]);
  }

  // Multiple elements: create controls_do_then_return
  // Statement input STM gets all but last; value input VALUE gets last
  var block = workspace.newBlock('controls_do_then_return');
  block.initSvg();

  // Chain statements (all elements except first 'begin' and last return value)
  var stmtNodes = els.slice(1, els.length - 1);
  if (stmtNodes.length > 0) {
    var firstStmt = AI.YailToBlocks.convertStatement_(workspace, stmtNodes[0]);
    if (firstStmt && block.getInput('STM')) {
      block.getInput('STM').connection.connect(firstStmt.previousConnection);
      var prevStmt = firstStmt;
      for (var i = 1; i < stmtNodes.length; i++) {
        var nextStmt = AI.YailToBlocks.convertStatement_(workspace, stmtNodes[i]);
        if (nextStmt && prevStmt.nextConnection && nextStmt.previousConnection) {
          prevStmt.nextConnection.connect(nextStmt.previousConnection);
          prevStmt = nextStmt;
        }
      }
    }
  }

  // Return value expression
  var returnNode = els[els.length - 1];
  var returnBlock = AI.YailToBlocks.convertExpression_(workspace, returnNode);
  if (returnBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(returnBlock.outputConnection);
  }

  return block;
};

// ---- Protect-enum ----

/**
 * Convert (protect-enum (static-field ClassName "EnumName") concreteValue)
 * @private
 */
AI.YailToBlocks.convertProtectEnum_ = function(workspace, node) {
  var els = node.elements;
  // Prefer the static-field form if present
  if (els.length >= 2 && AI.SExprParser.isForm(els[1], 'static-field')) {
    return AI.YailToBlocks.convertStaticField_(workspace, els[1]);
  }
  // Fallback: use the concrete value
  if (els.length >= 3) {
    return AI.YailToBlocks.convertExpression_(workspace, els[2]);
  }
  return null;
};

// ---- Higher-order list forms ----

/**
 * Configuration for higher-order list forms.
 * Maps YAIL form name to block type and argument structure.
 * @private
 */
AI.YailToBlocks.HIGHER_ORDER_MAP_ = {
  // (map_nondest $var bodyExpr listExpr)
  'map_nondest': {
    block: 'lists_map',
    varFields: ['VAR'], varIndices: [1],
    bodyInput: 'TO', bodyIndex: 2,
    listInput: 'LIST', listIndex: 3
  },
  // (filter_nondest $var testExpr listExpr)
  'filter_nondest': {
    block: 'lists_filter',
    varFields: ['VAR'], varIndices: [1],
    bodyInput: 'TEST', bodyIndex: 2,
    listInput: 'LIST', listIndex: 3
  },
  // (sortkey_nondest $var keyExpr listExpr)
  'sortkey_nondest': {
    block: 'lists_sort_key',
    varFields: ['VAR'], varIndices: [1],
    bodyInput: 'KEY', bodyIndex: 2,
    listInput: 'LIST', listIndex: 3
  },
  // (sortcomparator_nondest $var1 $var2 compareExpr listExpr)
  'sortcomparator_nondest': {
    block: 'lists_sort_comparator',
    varFields: ['VAR1', 'VAR2'], varIndices: [1, 2],
    bodyInput: 'COMPARE', bodyIndex: 3,
    listInput: 'LIST', listIndex: 4
  },
  // (mincomparator-nondest $var1 $var2 compareExpr listExpr)
  'mincomparator-nondest': {
    block: 'lists_minimum_value',
    varFields: ['VAR1', 'VAR2'], varIndices: [1, 2],
    bodyInput: 'COMPARE', bodyIndex: 3,
    listInput: 'LIST', listIndex: 4
  },
  // (maxcomparator-nondest $var1 $var2 compareExpr listExpr)
  'maxcomparator-nondest': {
    block: 'lists_maximum_value',
    varFields: ['VAR1', 'VAR2'], varIndices: [1, 2],
    bodyInput: 'COMPARE', bodyIndex: 3,
    listInput: 'LIST', listIndex: 4
  },
  // (reduceovereach initValue $var2 $var1 combineExpr listExpr)
  // Note: VAR2 (accumulator) precedes VAR1 (item) in YAIL
  'reduceovereach': {
    block: 'lists_reduce',
    varFields: ['VAR2', 'VAR1'], varIndices: [2, 3],
    initInput: 'INITANSWER', initIndex: 1,
    bodyInput: 'COMBINE', bodyIndex: 4,
    listInput: 'LIST', listIndex: 5
  }
};

/**
 * Convert a higher-order list form to its corresponding block.
 * @private
 */
AI.YailToBlocks.convertHigherOrderForm_ = function(workspace, node) {
  var head = AI.SExprParser.formHead(node);
  var info = AI.YailToBlocks.HIGHER_ORDER_MAP_[head];
  if (!info) return null;

  var els = node.elements;
  var block = workspace.newBlock(info.block);

  // Set variable field(s)
  for (var v = 0; v < info.varFields.length; v++) {
    var varIdx = info.varIndices[v];
    if (varIdx < els.length) {
      var varName = (els[varIdx].name || String(els[varIdx].value))
          .replace(/^\$(?:param_|local_)?/, '');
      block.setFieldValue(varName, info.varFields[v]);
    }
  }

  block.initSvg();

  // Connect init value (reduce only)
  if (info.initInput && info.initIndex < els.length) {
    var initBlock = AI.YailToBlocks.convertExpression_(
        workspace, els[info.initIndex]);
    if (initBlock && block.getInput(info.initInput)) {
      block.getInput(info.initInput).connection.connect(
          initBlock.outputConnection);
    }
  }

  // Connect body/compare/key expression
  if (info.bodyIndex < els.length) {
    var bodyBlock = AI.YailToBlocks.convertExpression_(
        workspace, els[info.bodyIndex]);
    if (bodyBlock && block.getInput(info.bodyInput)) {
      block.getInput(info.bodyInput).connection.connect(
          bodyBlock.outputConnection);
    }
  }

  // Connect list expression
  if (info.listIndex < els.length) {
    var listBlock = AI.YailToBlocks.convertExpression_(
        workspace, els[info.listIndex]);
    if (listBlock && block.getInput(info.listInput)) {
      block.getInput(info.listInput).connection.connect(
          listBlock.outputConnection);
    }
  }

  return block;
};

// ---- Block factory helpers ----

/** @private */
AI.YailToBlocks.makeNumberBlock_ = function(workspace, value) {
  var block = workspace.newBlock('math_number');
  block.setFieldValue(String(value), 'NUM');
  block.initSvg();
  return block;
};

/** @private */
AI.YailToBlocks.makeTextBlock_ = function(workspace, value) {
  var block = workspace.newBlock('text');
  block.setFieldValue(value, 'TEXT');
  block.initSvg();
  return block;
};

/** @private */
AI.YailToBlocks.makeBoolBlock_ = function(workspace, value) {
  var block = workspace.newBlock('logic_boolean');
  block.setFieldValue(value ? 'TRUE' : 'FALSE', 'BOOL');
  block.initSvg();
  return block;
};

/** @private */
AI.YailToBlocks.makeVarGetBlock_ = function(workspace, name) {
  var block = workspace.newBlock('lexical_variable_get');
  block.setFieldValue(name, 'VAR');
  block.initSvg();
  return block;
};

/** @private */
AI.YailToBlocks.makeEmptyListBlock_ = function(workspace) {
  var block = workspace.newBlock('lists_create_with');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('items', '0');
  block.domToMutation(mutation);
  block.initSvg();
  return block;
};

// ---- Color constants ----

/**
 * Map of numeric color values to Blockly color block types.
 * Values are computed as: -1 * (16^6 - parseInt(hex6, 16))
 * matching the formula in colors.js YAIL generator.
 * @private
 */
AI.YailToBlocks.COLOR_VALUE_MAP_ = {
  '-16777216': 'color_black',      // #000000
  '-1': 'color_white',             // #ffffff
  '-65536': 'color_red',           // #ff0000
  '-20561': 'color_pink',          // #ffafaf
  '-14336': 'color_orange',        // #ffc800
  '-256': 'color_yellow',          // #ffff00
  '-16711936': 'color_green',      // #00ff00
  '-16711681': 'color_cyan',       // #00ffff
  '-16776961': 'color_blue',       // #0000ff
  '-65281': 'color_magenta',       // #ff00ff
  '-3355444': 'color_light_gray',  // #cccccc
  '-7829368': 'color_gray',        // #888888
  '-12303292': 'color_dark_gray'   // #444444
};

/**
 * Map of 32-bit unsigned ARGB hex values (alpha=0xFF) to color block types.
 * The S-expression parser produces these as unsigned integers from #xFFRRGGBB.
 * @private
 */
AI.YailToBlocks.COLOR_HEX_MAP_ = {
  '4278190080': 'color_black',      // 0xFF000000
  '4294967295': 'color_white',      // 0xFFFFFFFF
  '4294901760': 'color_red',        // 0xFFFF0000
  '4294946735': 'color_pink',       // 0xFFFFAFAF
  '4294952960': 'color_orange',     // 0xFFFFC800
  '4294967040': 'color_yellow',     // 0xFFFFFF00
  '4278255360': 'color_green',      // 0xFF00FF00
  '4278255615': 'color_cyan',       // 0xFF00FFFF
  '4278190335': 'color_blue',       // 0xFF0000FF
  '4294902015': 'color_magenta',    // 0xFFFF00FF
  '4291611852': 'color_light_gray', // 0xFFCCCCCC
  '4287137928': 'color_gray',       // 0xFF888888
  '4282664004': 'color_dark_gray'   // 0xFF444444
};

/**
 * Try to create a color constant block from a number node.
 * Returns null if the value doesn't match a known color.
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {Object} node  The inner argument of make-exact-yail-integer.
 * @return {?Blockly.Block}
 * @private
 */
AI.YailToBlocks.tryMakeColorBlock_ = function(workspace, node) {
  if (node.type !== 'number') return null;

  var val = String(node.value);
  var blockType = AI.YailToBlocks.COLOR_VALUE_MAP_[val]
      || AI.YailToBlocks.COLOR_HEX_MAP_[val];
  if (!blockType) return null;

  var block = workspace.newBlock(blockType);
  block.initSvg();
  return block;
};

// ---- Utility ----

/**
 * Extract a symbol name from a quoted or bare symbol.
 * Handles both 'Symbol (quoted) and Symbol (bare).
 * @private
 */
AI.YailToBlocks.unquoteSymbol_ = function(node) {
  if (node.type === 'quoted' && node.inner.type === 'symbol') {
    return node.inner.name;
  }
  if (node.type === 'symbol') {
    return node.name;
  }
  if (node.type === 'string') {
    return node.value;
  }
  return String(node.value || node.name || '');
};

/**
 * Check if a bare name matches a known global variable in the workspace.
 * Used for LLM tolerance when the g$ prefix is omitted.
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {string} name Variable name without prefix.
 * @return {boolean}
 * @private
 */
AI.YailToBlocks.isGlobalVariable_ = function(workspace, name) {
  var topBlocks = workspace.getTopBlocks(false);
  for (var i = 0; i < topBlocks.length; i++) {
    if (topBlocks[i].type === 'global_declaration'
        && topBlocks[i].getFieldValue('NAME') === name) {
      return true;
    }
  }
  return false;
};

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

    for (var i = 0; i < ast.length; i++) {
      var node = ast[i];
      var result = AI.YailToBlocks.validateTopLevel_(node);
      if (!result.valid) {
        return result;
      }
    }
    return {valid: true, error: null};
  } catch (e) {
    return {valid: false, error: e.message};
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
    // Global variable: (def g$name value)
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
    // Procedure: (def (p$name $param1 ...) body)
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
