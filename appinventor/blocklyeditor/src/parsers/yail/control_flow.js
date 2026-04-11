// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Control flow converters (if, while, foreach, forrange, let,
 * begin, logic ops), higher-order list forms, and protect-enum for the
 * YAIL-to-Blocks converter.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.YailToBlocks.ControlFlow');

goog.require('AI.SExprParser');
goog.require('AI.YailToBlocks');

// ---- Control flow ----

/** @private */
AI.YailToBlocks.convertIf_ = function(workspace, node, asExpression) {
  var els = node.elements;

  // In expression context, use controls_choose.
  if (asExpression && els.length > 3) {
    var block = workspace.newBlock('controls_choose');
    block.initSvg();

    var condBlock = AI.YailToBlocks.convertExpression_(workspace, els[1]);
    if (condBlock && block.getInput('TEST')) {
      block.getInput('TEST').connection.connect(condBlock.outputConnection);
    }
    var thenBlock = AI.YailToBlocks.convertExpression_(workspace, els[2]);
    if (thenBlock && block.getInput('THENRETURN')) {
      block.getInput('THENRETURN').connection.connect(thenBlock.outputConnection);
    }
    var elseBlock = AI.YailToBlocks.convertExpression_(workspace, els[3]);
    if (elseBlock && block.getInput('ELSERETURN')) {
      block.getInput('ELSERETURN').connection.connect(elseBlock.outputConnection);
    }
    return block;
  }

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

  // Statement context: use controls_if
  var block = workspace.newBlock('controls_if');
  var mutation = document.createElement('mutation');
  var elseifCount = conditions.length - 1;
  if (elseifCount > 0) mutation.setAttribute('elseif', String(elseifCount));
  if (elseBody) mutation.setAttribute('else', '1');
  block.domToMutation(mutation);
  block.initSvg();

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
 * @private
 */
AI.YailToBlocks.convertForEachDict_ = function(workspace, node, letNode) {
  var els = node.elements;
  var bindings = letNode.elements[1];

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

  if (els.length > 3) {
    var dictBlock = AI.YailToBlocks.convertExpression_(workspace, els[3]);
    if (dictBlock && block.getInput('DICT')) {
      block.getInput('DICT').connection.connect(dictBlock.outputConnection);
    }
  }

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
  var numOperands = els.length - 1;
  var block = workspace.newBlock('logic_operation');

  if (numOperands > 2 && block.domToMutation) {
    var mutation = document.createElement('mutation');
    mutation.setAttribute('items', String(numOperands));
    block.domToMutation(mutation);
  }
  block.initSvg();

  block.setFieldValue(op, 'OP');

  for (var i = 0; i < numOperands; i++) {
    var expr = els[i + 1];
    if (AI.SExprParser.isForm(expr, 'begin') && expr.elements.length >= 2) {
      expr = expr.elements[expr.elements.length - 1];
    }
    var inputName = i > 1 ? 'BOOL' + i : ['A', 'B'][i];
    var exprBlock = AI.YailToBlocks.convertExpression_(workspace, expr);
    if (exprBlock && block.getInput(inputName)) {
      block.getInput(inputName).connection.connect(exprBlock.outputConnection);
    }
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertLet_ = function(workspace, node, asExpression) {
  var els = node.elements;
  if (els.length < 3) return null;

  var bindingsList = els[1];
  if (bindingsList.type !== 'list') return null;

  var blockType = asExpression
      ? 'local_declaration_expression'
      : 'local_declaration_statement';
  var block = workspace.newBlock(blockType);

  var varNames = [];
  for (var i = 0; i < bindingsList.elements.length; i++) {
    var binding = bindingsList.elements[i];
    if (binding.type === 'list' && binding.elements.length >= 1) {
      var rawName = binding.elements[0].name || String(binding.elements[0].value);
      var name = rawName.replace(/^\$(?:param_|local_)?/, '').replace(/^local_/, '');
      varNames.push(name);
    }
  }

  var mutation = document.createElement('mutation');
  for (var i = 0; i < varNames.length; i++) {
    var localname = document.createElement('localname');
    localname.setAttribute('name', varNames[i]);
    mutation.appendChild(localname);
  }
  block.domToMutation(mutation);
  block.initSvg();

  for (var i = 0; i < bindingsList.elements.length; i++) {
    var binding = bindingsList.elements[i];
    if (binding.type === 'list' && binding.elements.length >= 2) {
      var valBlock = AI.YailToBlocks.convertExpression_(workspace, binding.elements[1]);
      if (valBlock && block.getInput('DECL' + i)) {
        block.getInput('DECL' + i).connection.connect(valBlock.outputConnection);
      }
    }
  }

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

// ---- Begin block helpers ----

/**
 * Convert a (begin ...) to chained statement blocks.
 * @private
 */
AI.YailToBlocks.convertBeginToStatements_ = function(workspace, node) {
  if (!AI.SExprParser.isForm(node, 'begin')) {
    return AI.YailToBlocks.convertStatement_(workspace, node);
  }

  var stmts = node.elements.slice(1);
  if (stmts.length === 0) return null;

  // Detect (begin <expr> "ignored") — the controls_eval_but_ignore block.
  if (stmts.length === 2
      && stmts[1].type === 'string' && stmts[1].value === 'ignored') {
    var block = workspace.newBlock('controls_eval_but_ignore');
    block.initSvg();
    var valueBlock = AI.YailToBlocks.convertExpression_(workspace, stmts[0]);
    if (valueBlock && block.getInput('VALUE')) {
      block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
    }
    return block;
  }

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

  if (els.length === 2) {
    return AI.YailToBlocks.convertExpression_(workspace, els[1]);
  }

  var block = workspace.newBlock('controls_do_then_return');
  block.initSvg();

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
  if (els.length >= 2 && AI.SExprParser.isForm(els[1], 'static-field')) {
    return AI.YailToBlocks.convertStaticField_(workspace, els[1]);
  }
  if (els.length >= 3) {
    return AI.YailToBlocks.convertExpression_(workspace, els[2]);
  }
  return null;
};

// ---- Higher-order list forms ----

/**
 * Configuration for higher-order list forms.
 * @private
 */
AI.YailToBlocks.HIGHER_ORDER_MAP_ = {
  'map_nondest': {
    block: 'lists_map',
    varFields: ['VAR'], varIndices: [1],
    bodyInput: 'TO', bodyIndex: 2,
    listInput: 'LIST', listIndex: 3
  },
  'filter_nondest': {
    block: 'lists_filter',
    varFields: ['VAR'], varIndices: [1],
    bodyInput: 'TEST', bodyIndex: 2,
    listInput: 'LIST', listIndex: 3
  },
  'sortkey_nondest': {
    block: 'lists_sort_key',
    varFields: ['VAR'], varIndices: [1],
    bodyInput: 'KEY', bodyIndex: 2,
    listInput: 'LIST', listIndex: 3
  },
  'sortcomparator_nondest': {
    block: 'lists_sort_comparator',
    varFields: ['VAR1', 'VAR2'], varIndices: [1, 2],
    bodyInput: 'COMPARE', bodyIndex: 3,
    listInput: 'LIST', listIndex: 4
  },
  'mincomparator-nondest': {
    block: 'lists_minimum_value',
    varFields: ['VAR1', 'VAR2'], varIndices: [1, 2],
    bodyInput: 'COMPARE', bodyIndex: 3,
    listInput: 'LIST', listIndex: 4
  },
  'maxcomparator-nondest': {
    block: 'lists_maximum_value',
    varFields: ['VAR1', 'VAR2'], varIndices: [1, 2],
    bodyInput: 'COMPARE', bodyIndex: 3,
    listInput: 'LIST', listIndex: 4
  },
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

  for (var v = 0; v < info.varFields.length; v++) {
    var varIdx = info.varIndices[v];
    if (varIdx < els.length) {
      var varName = (els[varIdx].name || String(els[varIdx].value))
          .replace(/^\$(?:param_|local_)?/, '');
      block.setFieldValue(varName, info.varFields[v]);
    }
  }

  block.initSvg();

  if (info.initInput && info.initIndex < els.length) {
    var initBlock = AI.YailToBlocks.convertExpression_(
        workspace, els[info.initIndex]);
    if (initBlock && block.getInput(info.initInput)) {
      block.getInput(info.initInput).connection.connect(
          initBlock.outputConnection);
    }
  }

  if (info.bodyIndex < els.length) {
    var bodyBlock = AI.YailToBlocks.convertExpression_(
        workspace, els[info.bodyIndex]);
    if (bodyBlock && block.getInput(info.bodyInput)) {
      block.getInput(info.bodyInput).connection.connect(
          bodyBlock.outputConnection);
    }
  }

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
