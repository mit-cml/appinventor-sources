// -*- mode: Javascript; js-indent-level: 2; -*-
// Copyright © 2020 MIT, All rights reserved
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
  // [lyn, 11/10/13] Use goog.dom.getChildren rather than .children or
  //   .childNodes to make this code work across browsers.
  var children = goog.dom.getChildren(xml);
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
