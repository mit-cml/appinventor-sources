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
 * When true, new event-handler blocks are placed near existing blocks
 * for the same component instance.
 * @type {boolean}
 */
AI.YailToBlocks.GROUP_BY_COMPONENT = true;

/**
 * Set of Blockly block IDs that are scheduled for deletion later in the
 * current execution batch.
 * @type {?Object<string, boolean>}
 * @private
 */
AI.YailToBlocks.pendingDeletionIds_ = null;

/**
 * Mark blocks that will be deleted later in the current execution batch.
 *
 * @param {!Blockly.WorkspaceSvg} workspace The target workspace.
 * @param {!Array<string>} identifiers YAIL identifier strings for blocks
 *     that will be deleted (e.g. "define-event Button1 Click").
 */
AI.YailToBlocks.setPendingDeletions = function(workspace, identifiers) {
  AI.YailToBlocks.pendingDeletionIds_ = {};
  for (var i = 0; i < identifiers.length; i++) {
    var block = AI.YailToBlocks.findBlockByIdentifier_(
        workspace, identifiers[i]);
    if (block) {
      AI.YailToBlocks.pendingDeletionIds_[block.id] = true;
    }
  }
};

/**
 * Clear the pending deletion set.
 */
AI.YailToBlocks.clearPendingDeletions = function() {
  AI.YailToBlocks.pendingDeletionIds_ = null;
};

/**
 * Check whether a block with the given YAIL identifier exists on the
 * workspace.
 *
 * @param {!Blockly.WorkspaceSvg} workspace The workspace to search.
 * @param {string} identifier YAIL identifier (e.g. "define-event Button1 Click").
 * @return {boolean}
 */
AI.YailToBlocks.blockExists = function(workspace, identifier) {
  return AI.YailToBlocks.findBlockByIdentifier_(workspace, identifier) !== null;
};

/**
 * Mark existing blocks that will be replaced by WRITE_BLOCK upserts in
 * the current batch.
 *
 * @param {!Blockly.WorkspaceSvg} workspace The target workspace.
 * @param {!Array<string>} yailStrings YAIL S-expression strings from
 *     WRITE_BLOCK operations.
 */
AI.YailToBlocks.addPendingUpserts = function(workspace, yailStrings) {
  if (!AI.YailToBlocks.pendingDeletionIds_) {
    AI.YailToBlocks.pendingDeletionIds_ = {};
  }
  for (var i = 0; i < yailStrings.length; i++) {
    try {
      var asts = AI.SExprParser.parseAll(yailStrings[i]);
      for (var j = 0; j < asts.length; j++) {
        var id = AI.YailToBlocks.getUpsertIdentifier_(asts[j]);
        if (id) {
          var block = AI.YailToBlocks.findBlockByIdentifier_(workspace, id);
          if (block) {
            AI.YailToBlocks.pendingDeletionIds_[block.id] = true;
          }
        }
      }
    } catch (e) {
      // Parse error — skip this YAIL string.
    }
  }
};

/**
 * Extract the block identifier from a parsed YAIL AST node that would
 * be used for upsert matching.
 *
 * @param {Object} node Parsed AST node.
 * @return {?string} Identifier string compatible with findBlockByIdentifier_.
 * @private
 */
AI.YailToBlocks.getUpsertIdentifier_ = function(node) {
  var head = AI.SExprParser.formHead(node);
  if (!head) return null;
  var els = node.elements;
  switch (head) {
    case 'define-event':
      if (els.length >= 3) {
        return 'define-event ' +
            (els[1].name || els[1].value) + ' ' +
            (els[2].name || els[2].value);
      }
      return null;
    case 'define-generic-event':
      if (els.length >= 3) {
        var typeName = AI.YailToBlocks.shortComponentType_(
            els[1].name || String(els[1].value));
        return 'define-generic-event ' + typeName + ' ' +
            (els[2].name || els[2].value);
      }
      return null;
    case 'def':
    case 'def-return':
      if (els.length < 2) return null;
      var second = els[1];
      if (second.type === 'symbol' && second.name &&
          second.name.startsWith('g$')) {
        return 'def ' + second.name;
      }
      if (second.type === 'list' && second.elements.length > 0) {
        var sym = second.elements[0];
        return 'def ' + (sym.name || String(sym.value));
      }
      return null;
    default:
      return null;
  }
};

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

    Blockly.Events.disable();
    var createdBlocks = [];
    var upsertPositions = [];

    var allNewBlocks = [];
    var origNewBlock = workspace.newBlock;
    workspace.newBlock = function(type, opt_id) {
      var b = origNewBlock.call(workspace, type, opt_id);
      allNewBlocks.push(b);
      return b;
    };

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
        }
      }
    } catch (e) {
      for (var j = allNewBlocks.length - 1; j >= 0; j--) {
        try {
          if (!allNewBlocks[j].disposed && !allNewBlocks[j].aiUpdatedInPlace_) {
            allNewBlocks[j].dispose(false);
          }
        } catch (ignore) {}
      }
      workspace.newBlock = origNewBlock;
      Blockly.Events.enable();
      return {success: false, error: e.message, blockId: null};
    }
    workspace.newBlock = origNewBlock;
    Blockly.Events.enable();

    // Render ALL blocks including descendants.
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

    AI.YailToBlocks.optimizeBlockWidths_(workspace, createdBlocks);

    // Position blocks.
    var SPACING = 30;
    var metrics = workspace.getMetrics();
    var viewCenterX = metrics.viewLeft +
        metrics.viewWidth / (2 * workspace.scale);
    var viewTop = metrics.viewTop / workspace.scale;
    var viewBottom = viewTop + metrics.viewHeight / workspace.scale;

    var existingBlocks = AI.YailToBlocks.getPositioningBlocks_(workspace);
    var freeSpaceY = viewTop + 20;
    for (var e = 0; e < existingBlocks.length; e++) {
      var eb = existingBlocks[e];
      var exy = eb.getRelativeToSurfaceXY();
      var ehw = eb.getHeightWidth();
      var eBottom = exy.y + ehw.height;
      if (exy.y < viewBottom && eBottom > viewTop) {
        if (eBottom + SPACING > freeSpaceY) {
          freeSpaceY = eBottom + SPACING;
        }
      }
    }

    for (var k = 0; k < createdBlocks.length; k++) {
      var block = createdBlocks[k];
      var upsertPos = upsertPositions[k];
      if (upsertPos) {
        block.moveTo(
            new Blockly.utils.Coordinate(upsertPos.x, upsertPos.y));
      } else {
        var hw = block.getHeightWidth();
        var placed = false;
        if (AI.YailToBlocks.GROUP_BY_COMPONENT) {
          var groupPos = AI.YailToBlocks.findGroupPosition_(
              workspace, block, SPACING);
          if (groupPos) {
            var pos = AI.YailToBlocks.avoidOverlap_(
                workspace, groupPos.x, groupPos.y,
                hw.width, hw.height, SPACING, block);
            block.moveTo(pos);
            placed = true;
            var groupBottom = pos.y + hw.height + SPACING;
            if (groupBottom > freeSpaceY) freeSpaceY = groupBottom;
          }
        }
        if (!placed) {
          var x = viewCenterX - hw.width / 2;
          var pos = AI.YailToBlocks.avoidOverlap_(
              workspace, x, freeSpaceY,
              hw.width, hw.height, SPACING, block);
          block.moveTo(pos);
          freeSpaceY = pos.y + hw.height + SPACING;
        }
      }
    }

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
  var blockToDelete = AI.YailToBlocks.findBlockByIdentifier_(
      workspace, identifier);
  if (!blockToDelete) {
    return {success: false, error: 'Block not found: ' + identifier};
  }

  AI.YailToBlocks.lastDeletedPosition_ =
      blockToDelete.getRelativeToSurfaceXY();

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

/**
 * Find an existing enabled top-level block by its YAIL identifier string.
 *
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {string} identifier e.g. "define-event Button1 Click"
 * @return {?Blockly.Block}
 * @private
 */
AI.YailToBlocks.findBlockByIdentifier_ = function(workspace, identifier) {
  var tokens = identifier.trim().split(/\s+/);
  if (tokens.length === 0) return null;

  var formType = tokens[0];
  var topBlocks = workspace.getTopBlocks(false);

  switch (formType) {
    case 'define-event':
      if (tokens.length < 3) return null;
      var componentName = tokens[1];
      var eventName = tokens[2];
      for (var i = 0; i < topBlocks.length; i++) {
        var block = topBlocks[i];
        if (!block.disabled && block.type === 'component_event'
            && block.instanceName === componentName
            && block.eventName === eventName) {
          return block;
        }
      }
      return null;

    case 'define-generic-event':
      if (tokens.length < 3) return null;
      var typeName = tokens[1];
      var eventName = tokens[2];
      for (var i = 0; i < topBlocks.length; i++) {
        var block = topBlocks[i];
        if (!block.disabled && block.type === 'component_event' && block.isGeneric
            && block.typeName === typeName && block.eventName === eventName) {
          return block;
        }
      }
      return null;

    case 'def':
    case 'def-return':
      if (tokens.length < 2) return null;
      var name = tokens[1].replace(/[()]/g, '');
      if (name.startsWith('g$')) {
        var varName = name.substring(2);
        for (var i = 0; i < topBlocks.length; i++) {
          var block = topBlocks[i];
          if (!block.disabled && block.type === 'global_declaration'
              && block.getFieldValue('NAME') === varName) {
            return block;
          }
        }
      } else if (name.startsWith('p$')) {
        var procName = name.substring(2);
        for (var i = 0; i < topBlocks.length; i++) {
          var block = topBlocks[i];
          if (!block.disabled
              && (block.type === 'procedures_defnoreturn'
                  || block.type === 'procedures_defreturn')
              && block.getFieldValue('NAME') === procName) {
            return block;
          }
        }
      }
      return null;

    default:
      return null;
  }
};

/**
 * Find an existing procedure definition block by name.
 * @private
 */
AI.YailToBlocks.findProcBlock_ = function(workspace, procName) {
  var topBlocks = workspace.getTopBlocks(false);
  for (var i = 0; i < topBlocks.length; i++) {
    var block = topBlocks[i];
    if (!block.disabled
        && (block.type === 'procedures_defnoreturn' || block.type === 'procedures_defreturn')
        && block.getFieldValue('NAME') === procName) {
      return block;
    }
  }
  return null;
};

/**
 * Disconnect and dispose all blocks connected to a given input.
 * @private
 */
AI.YailToBlocks.clearInputBlocks_ = function(block, inputName) {
  var input = block.getInput(inputName);
  if (!input || !input.connection) return;
  var target = input.connection.targetBlock();
  if (!target) return;

  input.connection.disconnect();

  if (input.type === Blockly.NEXT_STATEMENT) {
    var stmts = [];
    var current = target;
    while (current) {
      stmts.push(current);
      current = current.getNextBlock();
    }
    for (var i = stmts.length - 1; i >= 0; i--) {
      if (stmts[i].previousConnection && stmts[i].previousConnection.isConnected()) {
        stmts[i].previousConnection.disconnect();
      }
      if (stmts[i].nextConnection && stmts[i].nextConnection.isConnected()) {
        stmts[i].nextConnection.disconnect();
      }
      stmts[i].dispose(false);
    }
  } else {
    target.dispose(false);
  }
};

/**
 * Disconnect the block tree connected to a given input WITHOUT disposing it.
 * @private
 */
AI.YailToBlocks.detachInputBlocks_ = function(block, inputName) {
  var input = block.getInput(inputName);
  if (!input || !input.connection) return null;
  var target = input.connection.targetBlock();
  if (!target) return null;
  input.connection.disconnect();
  return target;
};

/**
 * Reconnect a previously detached block tree to an input.
 * @private
 */
AI.YailToBlocks.reattachInputBlocks_ = function(block, inputName, target) {
  var input = block.getInput(inputName);
  if (!input || !input.connection) return;
  if (target.previousConnection) {
    input.connection.connect(target.previousConnection);
  } else if (target.outputConnection) {
    input.connection.connect(target.outputConnection);
  }
};

/**
 * Dispose a previously detached block tree.
 * @private
 */
AI.YailToBlocks.disposeDetachedBlocks_ = function(target) {
  if (!target || target.disposed) return;
  if (target.previousConnection) {
    var stmts = [];
    var current = target;
    while (current) {
      stmts.push(current);
      current = current.getNextBlock();
    }
    for (var i = stmts.length - 1; i >= 0; i--) {
      if (stmts[i].previousConnection && stmts[i].previousConnection.isConnected()) {
        stmts[i].previousConnection.disconnect();
      }
      if (stmts[i].nextConnection && stmts[i].nextConnection.isConnected()) {
        stmts[i].nextConnection.disconnect();
      }
      stmts[i].dispose(false);
    }
  } else {
    target.dispose(false);
  }
};

/**
 * Connect body forms to a procedure definition block.
 * @private
 */
AI.YailToBlocks.connectProcedureBody_ = function(workspace, block, bodyForms, isReturn) {
  if (bodyForms.length === 0) return;

  if (isReturn) {
    if (bodyForms.length === 1) {
      var retBlock = AI.YailToBlocks.convertExpression_(workspace, bodyForms[0]);
      if (retBlock && block.getInput('RETURN')) {
        block.getInput('RETURN').connection.connect(retBlock.outputConnection);
      }
    } else {
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
    var firstStmt = AI.YailToBlocks.convertStatement_(workspace, bodyForms[0]);
    if (firstStmt && block.getInput('STACK')) {
      block.getInput('STACK').connection.connect(firstStmt.previousConnection);
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
};

// ---- Internal: Top-level form dispatch ----

/**
 * Convert a top-level YAIL form into a block.
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

/** @private */
AI.YailToBlocks.convertEventHandler_ = function(workspace, node) {
  var els = node.elements;
  if (els.length < 4) {
    throw new Error('define-event requires at least component, event, and params');
  }

  var componentName = els[1].name || String(els[1].value);
  var eventName = els[2].name || String(els[2].value);

  var params = [];
  var paramsNode = els[3];
  if (paramsNode.type === 'list') {
    for (var i = 0; i < paramsNode.elements.length; i++) {
      var p = paramsNode.elements[i];
      var pName = (p.name || String(p.value)).replace(/^\$(?:param_|local_)?/, '');
      params.push(pName);
    }
  }

  var existingId = 'define-event ' + componentName + ' ' + eventName;
  var existingBlock = AI.YailToBlocks.findBlockByIdentifier_(workspace, existingId);
  if (existingBlock) {
    AI.YailToBlocks.lastDeletedPosition_ = existingBlock.getRelativeToSurfaceXY();
  }

  var componentDb = workspace.getComponentDatabase();
  var componentType = '';
  if (componentDb) {
    var typeInfo = componentDb.instanceNameToTypeName(componentName);
    if (typeInfo) {
      componentType = typeInfo;
    }
  }

  var block = workspace.newBlock('component_event');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  mutation.setAttribute('instance_name', componentName);
  mutation.setAttribute('event_name', eventName);
  mutation.setAttribute('is_generic', 'false');
  block.domToMutation(mutation);
  block.initSvg();

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

  if (existingBlock) {
    existingBlock.dispose(true);
  }

  block.render();
  return block;
};

// ---- Generic Event Handler ----

/** @private */
AI.YailToBlocks.convertGenericEventHandler_ = function(workspace, node) {
  var els = node.elements;
  if (els.length < 4) {
    throw new Error('define-generic-event requires type, event, and params');
  }

  var typeName = AI.YailToBlocks.shortComponentType_(
      els[1].name || String(els[1].value));
  var eventName = els[2].name || String(els[2].value);

  var params = [];
  var paramsNode = els[3];
  if (paramsNode.type === 'list') {
    for (var i = 0; i < paramsNode.elements.length; i++) {
      var p = paramsNode.elements[i];
      var pName = (p.name || String(p.value)).replace(/^\$(?:param_|local_)?/, '');
      params.push(pName);
    }
  }

  var existingId = 'define-generic-event ' + typeName + ' ' + eventName;
  var existingBlock = AI.YailToBlocks.findBlockByIdentifier_(workspace, existingId);
  if (existingBlock) {
    AI.YailToBlocks.lastDeletedPosition_ = existingBlock.getRelativeToSurfaceXY();
  }

  var block = workspace.newBlock('component_event');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', typeName);
  mutation.setAttribute('instance_name', '');
  mutation.setAttribute('event_name', eventName);
  mutation.setAttribute('is_generic', 'true');
  block.domToMutation(mutation);
  block.initSvg();

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

  if (existingBlock) {
    existingBlock.dispose(true);
  }

  block.render();
  return block;
};

// ---- Global Variable ----

/** @private */
AI.YailToBlocks.convertGlobalVar_ = function(workspace, node) {
  var els = node.elements;
  var varName = els[1].name.substring(2); // strip g$
  var initValue = els[2];

  var existingBlock = AI.YailToBlocks.findBlockByIdentifier_(
      workspace, 'def g$' + varName);

  if (existingBlock) {
    // ===== UPDATE IN PLACE =====
    var block = existingBlock;
    AI.YailToBlocks.lastDeletedPosition_ = block.getRelativeToSurfaceXY();

    var savedValue = AI.YailToBlocks.detachInputBlocks_(block, 'VALUE');
    try {
      var valueBlock = AI.YailToBlocks.convertExpression_(workspace, initValue);
      if (valueBlock && block.getInput('VALUE')) {
        block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
      }
    } catch (e) {
      if (savedValue) {
        AI.YailToBlocks.reattachInputBlocks_(block, 'VALUE', savedValue);
      }
      throw e;
    }
    if (savedValue) {
      AI.YailToBlocks.disposeDetachedBlocks_(savedValue);
    }

    block.render();
    return block;
  }

  // ===== CREATE FROM SCRATCH =====
  var block = workspace.newBlock('global_declaration');
  block.setFieldValue(varName, 'NAME');
  block.initSvg();

  var valueBlock = AI.YailToBlocks.convertExpression_(workspace, initValue);
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }

  block.render();
  return block;
};

// ---- Procedure ----

/** @private */
AI.YailToBlocks.convertProcedure_ = function(workspace, node, isReturn) {
  var els = node.elements;
  var nameList = els[1];
  var procNameSym = nameList.elements[0];
  var procName = (procNameSym.name || String(procNameSym.value)).replace(/^p\$/, '');

  var params = [];
  for (var i = 1; i < nameList.elements.length; i++) {
    var p = nameList.elements[i];
    var pName = (p.name || String(p.value)).replace(/^\$(?:param_|local_)?/, '');
    params.push(pName);
  }

  var bodyForms = [];
  for (var i = 2; i < els.length; i++) {
    if (AI.SExprParser.isForm(els[i], 'set-this-form')) continue;
    bodyForms.push(els[i]);
  }

  var blockType = isReturn ? 'procedures_defreturn' : 'procedures_defnoreturn';
  var existingBlock = AI.YailToBlocks.findProcBlock_(workspace, procName);

  if (existingBlock && existingBlock.type === blockType) {
    // ===== SAME TYPE: UPDATE IN PLACE =====
    var block = existingBlock;
    AI.YailToBlocks.lastDeletedPosition_ = block.getRelativeToSurfaceXY();
    block.aiUpdatedInPlace_ = true;

    var oldParams = block.arguments_ ? block.arguments_.slice() : [];
    var paramsChanged =
        !Blockly.LexicalVariable.stringListsEqual(params, block.arguments_);

    if (paramsChanged) {
      block.updateParams_(params);
    }

    var bodyInputName = isReturn ? 'RETURN' : 'STACK';
    var savedBody = AI.YailToBlocks.detachInputBlocks_(block, bodyInputName);
    try {
      AI.YailToBlocks.connectProcedureBody_(workspace, block, bodyForms, isReturn);
    } catch (bodyError) {
      if (paramsChanged) {
        block.updateParams_(oldParams);
      }
      if (savedBody) {
        AI.YailToBlocks.reattachInputBlocks_(block, bodyInputName, savedBody);
      }
      throw bodyError;
    }

    if (paramsChanged) {
      Blockly.Procedures.mutateCallers(block);
    }
    if (savedBody) {
      AI.YailToBlocks.disposeDetachedBlocks_(savedBody);
    }

    block.render();
    return block;
  }

  if (existingBlock) {
    var existingKind = existingBlock.type === 'procedures_defreturn'
        ? 'returns a value' : 'does not return a value';
    var requestedKind = isReturn ? 'def-return' : 'def';
    throw new Error('Procedure "' + procName + '" already exists and '
        + existingKind + '. Cannot change to ' + requestedKind
        + '. Delete the procedure first or use the matching form.');
  }

  // ===== CREATE FROM SCRATCH =====
  var block = workspace.newBlock(blockType);
  block.setFieldValue(procName, 'NAME');

  if (params.length > 0) {
    var mutation = document.createElement('mutation');
    for (var j = 0; j < params.length; j++) {
      var arg = document.createElement('arg');
      arg.setAttribute('name', params[j]);
      mutation.appendChild(arg);
    }
    block.domToMutation(mutation);
  }

  block.initSvg();
  AI.YailToBlocks.connectProcedureBody_(workspace, block, bodyForms, isReturn);

  block.render();
  return block;
};
