// -*- mode: Javascript; js-indent-level: 2; -*-
// Copyright © 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Blockly XML related utilities. Use by drawer.js and flydown
 * fields.
 */
'use strict';

goog.provide('AI.Blockly.Util.xml');

if (Blockly.Util === undefined) {
  Blockly.Util = {}
}
Blockly.Util.xml = {};

/**
 * Converts a group of nodes (the outer-most one being a <xml> node) into an
 * array of all of the child nodes.
 * @param {!Node} xml A group of nodes, the outer most one being an <xml> node.
 * @return {!Array<!Node>} An array of xml nodes.
 */
Blockly.Util.xml.XMLToArray = function(xml) {
  var xmlArray = [];
  var children = xml.children;
  for (var i = 0, child; child = children[i]; i++) {
    xmlArray.push(child);
  }
  return xmlArray;
}

/**
 * Returns XML defining the given block type & mutator attributes.
 * @param {string} blockType The type of block to create XML for.
 * @param {Object=} opt_attributes A map of keys and values (attributes) to be
 *     added to to a <mutation/> tag.
 */
Blockly.Util.xml.blockTypeToXML = function(blockType, opt_attributes) {
  var xmlString = '<xml><block type="' + blockType + '">';
  if (opt_attributes) {
    xmlString += Blockly.Util.xml.mutatorAttributesXmlString(opt_attributes);
  }
  xmlString += '</block></xml>';
  return Blockly.utils.xml.textToDom(xmlString);
}

/**
 * Converts the given map of keys and values to a <mutation/> element containing
 * those pairs.
 * @param {!Object} mutatorAttributes The map to convert to a <mutation/> element.
 */
Blockly.Util.xml.mutatorAttributesXmlString = function(mutatorAttributes){
  var xmlString = '<mutation ';
  for (var attributeName in mutatorAttributes) {
    if (!mutatorAttributes.hasOwnProperty(attributeName)) {
      continue;
    }
    xmlString += attributeName + '="' + mutatorAttributes[attributeName] + '" ';
  }
  xmlString += '></mutation>';
  return xmlString;
};

/**
 * Returns an XML string including one procedure caller for each procedure
 * declaration in the main workspace.
 * @param {boolean} returnsValue True if we want to create callers that return
 *     values. False otherwise.
 * @param {!Blockly.Workspace} workspace The workspace to search for declarations.
 */
Blockly.Util.xml.procedureCallersXMLString = function(returnsValue, workspace) {

  function compareDeclarationsByName(decl1, decl2) {
    var name1 = decl1.getFieldValue('NAME').toLocaleLowerCase();
    var name2 = decl2.getFieldValue('NAME').toLocaleLowerCase();
    return name1.localeCompare(name2);
  }

  var xmlString = '<xml>';
  var decls = workspace.getProcedureDatabase().getDeclarationBlocks(returnsValue);
  decls.sort(compareDeclarationsByName);
  for (var i = 0; i < decls.length; i++) {
    xmlString += Blockly.Util.xml.procedureCallerBlockString(decls[i]);
  }
  xmlString += '</xml>';
  return xmlString;
};

/**
 * Returns an XML string for a caller block for the given procedure declaration
 * block.
 * @param {!Blockly.Block} procDeclBlock The block for which we want to generate
 *     xml defining a caller.
 */
Blockly.Util.xml.procedureCallerBlockString = function(procDeclBlock) {
  /* Generates xml in the format:
   * <block type="procedures_callnoreturn" inline="false">
   *   <title name="PROCNAME">p2</title>
   *   <mutation name="p2">
   *     <arg name="b"></arg>
   *     <arg name="c"></arg>
   *  </mutation>
   * </block>
   * 
   */

  var declType = procDeclBlock.type;
  var callerType = (declType == 'procedures_defreturn') ?
      'procedures_callreturn' :
      'procedures_callnoreturn';
  var blockString = '<block type="' + callerType + '" inline="false">'
  var procName = procDeclBlock.getFieldValue('NAME');
  blockString += '<title name="PROCNAME">' + procName + '</title>';
  var mutationDom = procDeclBlock.mutationToDom();
  // Decl doesn't have name attribute, but caller does
  mutationDom.setAttribute('name', procName); 
  blockString += Blockly.Xml.domToText(mutationDom);
  blockString += '</block>';
  return blockString;
};

/**
 * Creates the xml for a value input with the given name.
 * @param {string} name The name of the value input.
 * @return {!Node} Two xml nodes defining the value input. The outer is an <xml>
 *     node and the inner is a <value> node.
 */
Blockly.Util.xml.valueInputXml = function(name) {
  var xmlString = '<xml><value name="' + name + '"></value></xml>';
  return Blockly.utils.xml.textToDom(xmlString);
}

/**
 * Creates the xml which defines the block for the given helper key.
 * @param {HelperKey} helperKey The helper key we want to get the block for.
 */
Blockly.Util.xml.helperKeyToXML= function(helperKey) {
  switch(helperKey.type) {
    case 'OPTION_LIST':
      return this.blockTypeToXML('helpers_dropdown', {key: helperKey.key});
    case 'ASSET':
      return this.blockTypeToXML('helpers_assets');
    case 'PROVIDER_MODEL':
      return this.blockTypeToXML('helpers_providermodel');
    case 'PROVIDER':
      return this.blockTypeToXML('helpers_provider');
  }
}

/**
 * Creates xml for a value input with the given name with a helper defined by
 * the given helper key attached.
 * @param {string} name The name of the value input.
 * @param {HelperKey} helperKey Key defining the helper block to create.
 * @return {!Node} XML nodes defining a value input with a helper block attached.
 */
Blockly.Util.xml.valueWithHelperXML = function(name, helperKey) {
  var inputXml= this.valueInputXml(name);
  var helperXml= this.helperKeyToXML(helperKey);
  // First child b/c these are wrapped in an <xml/> node.
  inputXml.firstChild.appendChild(helperXml.firstChild);
  return inputXml;
}

/**
 * Creates XML for a value input pre-filled with a default block.
 * Supported types: "text", "number", "boolean", "color", "list", "dictionary".
 * @param {string} inputName  The input slot name (e.g. 'ARG0', 'VALUE').
 * @param {Object} param The parameter object from components.json.
 * @return {Node} (Nullable) A <xml> node whose first child is a <value> node.
 */
Blockly.Util.xml.valueWithDefaultXML = function (inputName, param) {
  var xmlString = '<xml><value name="' + inputName + '">';

  var typeStr = 'unknown';
  if (param.editorType !== undefined) {
    // Accepts param type from DesignerProperty
    typeStr = param.editorType;
  } else if (param.type !== undefined) {
    // Not a DesignerProperty, take param type.
    typeStr = param.type;
  }
  if ((typeStr === 'any' || typeStr === 'unknown') && param.defaultValueType !== undefined) {
    // Take the optional defaultValueType if defined and only when the param type is 'any' or 'unknown'
    typeStr = param.defaultValueType;
  }

  var valueStr = String(param.defaultValue);
  var isNumericType = ['number', 'int', 'integer', 'float', 'double', 'short', 'long', 'non_negative_integer', 'non_negative_float'].includes(typeStr);

  if ((isNumericType || typeStr === 'color')) {
    if (valueStr.startsWith('&HFF')) {
      // Convert DesignerProperty color value to Hex code
      valueStr = valueStr.replace('&HFF', '#');
    } else if (valueStr.startsWith('&H00')) {
      // Convert DesignerProperty color value to Hex code
      valueStr = valueStr.replace('&H00', '#');
    }
  }

  if (valueStr.startsWith('#') && (isNumericType || typeStr === 'color')) {
    xmlString += '<block type="color_white">';
    xmlString += '<field name="COLOR">' + valueStr.toLowerCase() + '</field></block>';
  } else if (isNumericType) {
    xmlString += '<block type="math_number">';
    xmlString += '<field name="NUM">' + valueStr + '</field></block>';
  } else if (typeStr === 'boolean') {
    xmlString += '<block type="logic_boolean">';
    xmlString += '<field name="BOOL">' + valueStr.toUpperCase() + '</field></block>';
  } else if (typeStr === 'list') {
    xmlString += '<block type="lists_create_with">';
    if (!valueStr.trim()) {
      xmlString += '<mutation items="0"></mutation>';
    } else {
      var items = valueStr.split(',').map(item => item.trim());
      xmlString += '<mutation items="' + items.length + '"></mutation>';
      var blockType = 'text';
      var fieldName = 'TEXT';
      if (param.defaultValueType !== undefined && param.defaultValueType === 'number') {
        blockType = 'math_number';
        fieldName = 'NUM';
      } else if (param.defaultValueType !== undefined && param.defaultValueType === 'color') {
        blockType = 'color_white';
        fieldName = 'COLOR';
      }
      items.forEach((item, idx) => {
        xmlString += '<value name="ADD' + idx + '">' +
          '<block type="' + blockType + '"><field name="' + fieldName + '">' + this.escapeXml(item) + '</field></block></value>';
      });
    }
    xmlString += '</block>';
  } else if (typeStr === 'dictionary') {
    xmlString += '<block type="dictionaries_create_with">';
    if (!valueStr.trim()) {
      xmlString += '<mutation items="0"></mutation>';
    } else {
      var items = valueStr.split(',').map(item => item.trim());
      xmlString += '<mutation items="' + items.length + '"></mutation>';
      items.forEach((item, idx) => {
        var [key, value] = item.split(':');
        xmlString += '<value name="ADD' + idx + '"><block type="pair">';
        xmlString += '<value name="KEY"><block type="text"><field name="TEXT">' + this.escapeXml(key) + '</field></block></value>';
        xmlString += '<value name="VALUE"><block type="text"><field name="TEXT">' + this.escapeXml(value) + '</field></block></value>';
        xmlString += '</block></value>';
      });
    }
    xmlString += '</block>';
  } else if (typeStr === 'text' || typeStr === 'textArea' || typeStr === 'string') {
    xmlString += '<block type="text"><field name="TEXT">' + this.escapeXml(valueStr) + '</field></block>';
  } else {
    // Return null is the type is unknown
    return null;
  }

  xmlString += '</value></xml>';
  return Blockly.utils.xml.textToDom(xmlString);
};

/**
 * Escapes special characters for use in XML text nodes.
 * @param {string} unsafe The string to escape.
 * @return {string} The escaped string safe for XML.
 */
Blockly.Util.xml.escapeXml = function (unsafe) {
  return unsafe.replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
};
