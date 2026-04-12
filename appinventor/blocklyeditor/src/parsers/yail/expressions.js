// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Statement and expression dispatch, plus specific converters
 * for variables, properties, methods, components, and static fields in the
 * YAIL-to-Blocks converter.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.YailToBlocks.Expressions');

goog.require('AI.SExprParser');
goog.require('AI.YailToBlocks');

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
  var block;

  if (!head) {
    block = AI.YailToBlocks.convertExpression_(workspace, node);
  } else {
    switch (head) {
      case 'set-var!':
        block = AI.YailToBlocks.convertSetVar_(workspace, node, true);
        break;
      case 'set-lexical!':
        block = AI.YailToBlocks.convertSetLexical_(workspace, node);
        break;
      case 'set-and-coerce-property!':
        block = AI.YailToBlocks.convertSetProperty_(workspace, node);
        break;
      case 'set-and-coerce-property-and-check!':
        block = AI.YailToBlocks.convertGenericSetProperty_(workspace, node);
        break;
      case 'call-component-method':
        block = AI.YailToBlocks.convertMethodCall_(workspace, node, false);
        break;
      case 'call-component-type-method':
        block = AI.YailToBlocks.convertGenericMethodCall_(workspace, node, false);
        break;
      case 'call-yail-primitive':
        block = AI.YailToBlocks.convertPrimitive_(workspace, node, false);
        break;
      case 'if':
        block = AI.YailToBlocks.convertIf_(workspace, node, false);
        break;
      case 'while':
        block = AI.YailToBlocks.convertWhile_(workspace, node);
        break;
      case 'foreach':
        block = AI.YailToBlocks.convertForEach_(workspace, node);
        break;
      case 'forrange':
        block = AI.YailToBlocks.convertForRange_(workspace, node);
        break;
      case 'begin':
        block = AI.YailToBlocks.convertBeginStatements_(workspace, node);
        break;
      case 'let':
        block = AI.YailToBlocks.convertLet_(workspace, node, false);
        break;
      case 'break':
      case '*yail-break*':
        block = AI.YailToBlocks.convertBreak_(workspace);
        break;
      case 'call-component-method-with-blocking-continuation':
        block = AI.YailToBlocks.convertMethodCall_(workspace, node, false);
        break;
      case 'call-component-type-method-with-blocking-continuation':
        block = AI.YailToBlocks.convertGenericMethodCall_(workspace, node, false);
        break;
      default:
        block = AI.YailToBlocks.convertExpression_(workspace, node);
        break;
    }
  }

  // Ensure the result is actually a statement block (has previousConnection).
  if (block && !block.previousConnection) {
    var desc = head || (node && node.type) || 'unknown';
    block.dispose(false);
    throw new Error('Expression "' + desc + '" cannot be used in statement'
        + ' position. Use set-var! to store the result, or use def-return'
        + ' for procedures that return a value.');
  }

  return block;
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

  if (name === '#t') return AI.YailToBlocks.makeBoolBlock_(workspace, true);
  if (name === '#f') return AI.YailToBlocks.makeBoolBlock_(workspace, false);

  if (name === '*the-null-value*') {
    var block = workspace.newBlock('controls_nothing');
    block.initSvg();
    return block;
  }

  if (name.startsWith('g$')) {
    return AI.YailToBlocks.makeVarGetBlock_(workspace, 'global ' + name.substring(2));
  }

  var num = parseFloat(name);
  if (!isNaN(num)) {
    return AI.YailToBlocks.makeNumberBlock_(workspace, num);
  }

  // LLM tolerance: bare variable names without g$ prefix
  if (AI.YailToBlocks.isGlobalVariable_(workspace, name)) {
    return AI.YailToBlocks.makeVarGetBlock_(workspace, 'global ' + name);
  }

  // Fallback: treat unrecognized symbol as text literal
  return AI.YailToBlocks.makeTextBlock_(workspace, name);
};

/**
 * Convert a quoted expression.
 * @private
 */
AI.YailToBlocks.convertQuotedExpr_ = function(workspace, node) {
  var inner = node.inner;
  if (inner.type === 'list' && inner.elements.length === 0) {
    return AI.YailToBlocks.makeEmptyListBlock_(workspace);
  }
  if (inner.type === 'list' && inner.elements.length === 1
      && inner.elements[0].type === 'symbol' && inner.elements[0].name === '*list*') {
    return AI.YailToBlocks.makeEmptyListBlock_(workspace);
  }
  if (inner.type === 'symbol') {
    return AI.YailToBlocks.makeTextBlock_(workspace, inner.name);
  }
  return null;
};

/**
 * Convert a list expression (function call or special form).
 * @private
 */
AI.YailToBlocks.convertListExpr_ = function(workspace, node) {
  var head = AI.SExprParser.formHead(node);
  if (!head) {
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
      if (node.elements.length >= 2) {
        var colorBlock = AI.YailToBlocks.tryMakeColorBlock_(workspace, node.elements[1]);
        if (colorBlock) return colorBlock;
        return AI.YailToBlocks.convertExpression_(workspace, node.elements[1]);
      }
      return null;
    default:
      var primInfo = AI.YailToBlocks.PRIMITIVE_MAP_[head];
      if (primInfo) {
        return AI.YailToBlocks.convertShortPrimitive_(workspace, node, primInfo);
      }
      return null;
  }
};

// ---- Specific converters ----

/** @private */
AI.YailToBlocks.convertGetVar_ = function(workspace, node) {
  var varName = node.elements[1].name;
  if (varName.startsWith('g$')) {
    return AI.YailToBlocks.makeVarGetBlock_(workspace, 'global ' + varName.substring(2));
  }
  if (varName.startsWith('p$')) {
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

  var valueBlock = AI.YailToBlocks.tryMakeAssetBlockForProperty_(
      workspace, componentType, propertyName, value);
  if (!valueBlock) {
    valueBlock = AI.YailToBlocks.tryMakeColorBlockForProperty_(
        workspace, componentType, propertyName, value);
  }
  if (!valueBlock) {
    valueBlock = AI.YailToBlocks.convertExpression_(workspace, value);
  }
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }
  return block;
};

/** @private */
AI.YailToBlocks.convertGenericGetProperty_ = function(workspace, node) {
  var els = node.elements;
  var componentExpr = els[1];
  var typeName = AI.YailToBlocks.shortComponentType_(
      AI.YailToBlocks.unquoteSymbol_(els[2]));
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

/** @private */
AI.YailToBlocks.convertGenericSetProperty_ = function(workspace, node) {
  var els = node.elements;
  var componentExpr = els[1];
  var typeName = AI.YailToBlocks.shortComponentType_(
      AI.YailToBlocks.unquoteSymbol_(els[2]));
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

  var valueBlock = AI.YailToBlocks.tryMakeAssetBlockForProperty_(
      workspace, typeName, propertyName, value);
  if (!valueBlock) {
    valueBlock = AI.YailToBlocks.tryMakeColorBlockForProperty_(
        workspace, typeName, propertyName, value);
  }
  if (!valueBlock) {
    valueBlock = AI.YailToBlocks.convertExpression_(workspace, value);
  }
  if (valueBlock && block.getInput('VALUE')) {
    block.getInput('VALUE').connection.connect(valueBlock.outputConnection);
  }
  return block;
};

/** @private */
AI.YailToBlocks.convertMethodCall_ = function(workspace, node, asExpression) {
  var els = node.elements;
  var componentName = AI.YailToBlocks.unquoteSymbol_(els[1]);
  var methodName = AI.YailToBlocks.unquoteSymbol_(els[2]);

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

  if (els.length > 3 && AI.SExprParser.isForm(els[3], '*list-for-runtime*')) {
    var argList = els[3].elements;
    for (var i = 1; i < argList.length; i++) {
      var paramIndex = i - 1;
      var argBlock = AI.YailToBlocks.tryMakeAssetBlockForMethodArg_(
          workspace, componentType, methodName, paramIndex, argList[i]);
      if (!argBlock) {
        argBlock = AI.YailToBlocks.convertExpression_(workspace, argList[i]);
      }
      if (argBlock) {
        var inputName = 'ARG' + paramIndex;
        if (block.getInput(inputName)) {
          block.getInput(inputName).connection.connect(argBlock.outputConnection);
        }
      }
    }
  }

  return block;
};

/** @private */
AI.YailToBlocks.convertGenericMethodCall_ = function(workspace, node, asExpression) {
  var els = node.elements;
  var componentExpr = els[1];
  var typeName = AI.YailToBlocks.shortComponentType_(
      AI.YailToBlocks.unquoteSymbol_(els[2]));
  var methodName = AI.YailToBlocks.unquoteSymbol_(els[3]);

  var block = workspace.newBlock('component_method');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', typeName);
  mutation.setAttribute('method_name', methodName);
  mutation.setAttribute('is_generic', 'true');
  block.domToMutation(mutation);
  block.initSvg();

  var compBlock = AI.YailToBlocks.convertExpression_(workspace, componentExpr);
  if (compBlock && block.getInput('COMPONENT')) {
    block.getInput('COMPONENT').connection.connect(compBlock.outputConnection);
  }

  if (els.length > 4 && AI.SExprParser.isForm(els[4], '*list-for-runtime*')) {
    var argList = els[4].elements;
    for (var i = 1; i < argList.length; i++) {
      var paramIndex = i - 1;
      var argBlock = AI.YailToBlocks.tryMakeAssetBlockForMethodArg_(
          workspace, typeName, methodName, paramIndex, argList[i]);
      if (!argBlock) {
        argBlock = AI.YailToBlocks.convertExpression_(workspace, argList[i]);
      }
      if (argBlock) {
        var inputName = 'ARG' + paramIndex;
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
  var getVarForm = els[0];
  if (!AI.SExprParser.isForm(getVarForm, 'get-var')) return null;
  var procRef = getVarForm.elements[1].name;
  if (!procRef.startsWith('p$')) return null;
  var procName = procRef.substring(2);

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

  var argCount = els.length - 1;
  var mutation = document.createElement('mutation');
  mutation.setAttribute('name', procName);
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
  var componentType = AI.YailToBlocks.shortComponentType_(
      els[1].name || String(els[1].value));

  var block = workspace.newBlock('component_all_component_block');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('component_type', componentType);
  block.domToMutation(mutation);
  block.initSvg();
  return block;
};

/**
 * Convert (static-field className "enumName") to helpers_dropdown.
 * @private
 */
AI.YailToBlocks.convertStaticField_ = function(workspace, node) {
  var els = node.elements;
  if (els.length < 3) return null;

  var className = els[1].name || String(els[1].value);
  var enumName = els[2].value || els[2].name || String(els[2].value);

  var componentDb = workspace.getComponentDatabase();
  var optionListKey = componentDb
      ? componentDb.getOptionListKeyByClassName(className)
      : null;
  if (!optionListKey) {
    return AI.YailToBlocks.makeTextBlock_(workspace, enumName);
  }

  var block = workspace.newBlock('helpers_dropdown');
  var mutation = document.createElement('mutation');
  mutation.setAttribute('key', optionListKey);
  block.domToMutation(mutation);
  block.initSvg();

  if (block.getField && block.getField('OPTION')) {
    block.setFieldValue(enumName, 'OPTION');
  }

  return block;
};
