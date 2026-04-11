// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Block factory helpers, color constant maps, asset helpers,
 * and utility functions for the YAIL-to-Blocks converter.
 *
 * @author anthropic
 */

'use strict';

goog.provide('AI.YailToBlocks.BlockHelpers');

goog.require('AI.YailToBlocks');

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

/**
 * Create a helpers_assets block for the given asset name.
 * @param {!Blockly.WorkspaceSvg} workspace The workspace.
 * @param {string} assetName The asset filename.
 * @return {!Blockly.Block} The new helpers_assets block.
 * @private
 */
AI.YailToBlocks.makeAssetBlock_ = function(workspace, assetName) {
  var block = workspace.newBlock('helpers_assets');
  block.initSvg();
  var mutation = document.createElement('mutation');
  mutation.setAttribute('value', assetName);
  block.domToMutation(mutation);
  return block;
};

/**
 * Try to create a helpers_assets block for a property value.  Returns the
 * asset block when the property has an ASSET helper key and the value is a
 * string that exists in the workspace's asset list.  Returns null otherwise,
 * so the caller can fall back to the normal expression conversion.
 *
 * @param {!Blockly.WorkspaceSvg} workspace The workspace.
 * @param {string} componentType Short component type name.
 * @param {string} propertyName The property being set.
 * @param {Object} valueNode The AST node for the value expression.
 * @return {?Blockly.Block} An asset block, or null.
 * @private
 */
AI.YailToBlocks.tryMakeAssetBlockForProperty_ = function(
    workspace, componentType, propertyName, valueNode) {
  if (!valueNode || valueNode.type !== 'string') {
    return null;
  }
  var componentDb = workspace.getComponentDatabase();
  if (!componentDb || !componentType) {
    return null;
  }

  var propDescriptor = componentDb.getPropertyForType(
      componentType, propertyName);
  if (!propDescriptor || !propDescriptor.helperKey ||
      propDescriptor.helperKey.type !== 'ASSET') {
    return null;
  }

  var assets = workspace.getAssetList();
  if (assets.indexOf(valueNode.value) === -1) {
    return null;
  }

  return AI.YailToBlocks.makeAssetBlock_(workspace, valueNode.value);
};

/**
 * Try to create a helpers_assets block for a method argument.  Returns the
 * asset block when the parameter has an ASSET helper key and the value is a
 * string that exists in the workspace's asset list.  Returns null otherwise.
 *
 * @param {!Blockly.WorkspaceSvg} workspace The workspace.
 * @param {string} componentType Short component type name.
 * @param {string} methodName The method being called.
 * @param {number} paramIndex Zero-based parameter index.
 * @param {Object} valueNode The AST node for the argument expression.
 * @return {?Blockly.Block} An asset block, or null.
 * @private
 */
AI.YailToBlocks.tryMakeAssetBlockForMethodArg_ = function(
    workspace, componentType, methodName, paramIndex, valueNode) {
  if (!valueNode || valueNode.type !== 'string') {
    return null;
  }
  var componentDb = workspace.getComponentDatabase();
  if (!componentDb || !componentType) {
    return null;
  }

  var methodDescriptor = componentDb.getMethodForType(
      componentType, methodName);
  if (!methodDescriptor || !methodDescriptor.parameters ||
      paramIndex >= methodDescriptor.parameters.length) {
    return null;
  }

  var param = methodDescriptor.parameters[paramIndex];
  if (!param.helperKey || param.helperKey.type !== 'ASSET') {
    return null;
  }

  var assets = workspace.getAssetList();
  if (assets.indexOf(valueNode.value) === -1) {
    return null;
  }

  return AI.YailToBlocks.makeAssetBlock_(workspace, valueNode.value);
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
 * Extract the short component type name from a possibly fully-qualified
 * Java class name (e.g. 'com.google.appinventor.components.runtime.Button'
 * becomes 'Button').  Returns the input unchanged when it contains no dots.
 * @param {string} typeName
 * @return {string}
 * @private
 */
AI.YailToBlocks.shortComponentType_ = function(typeName) {
  var dot = typeName.lastIndexOf('.');
  return dot >= 0 ? typeName.substring(dot + 1) : typeName;
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
