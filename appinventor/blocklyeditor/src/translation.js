// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle Internationalization for AI2 Blockseditor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.Translation');

Blockly.Translation.map = {};

Blockly.Translation.haveType = function(typeName) {
  return Blockly.Translation.map[typeName] != undefined;
}

Blockly.Translation.haveProperty = function(typeName, propertyName) {
  if (!Blockly.Translation.haveType(typeName)) {
    return false;
  }
  return Blockly.Translation.map[typeName][propertyName] != undefined;
}

Blockly.Translation.addProperty = function(typeName, propertyName, propertyValue) {
  if (!Blockly.Translation.haveType(typeName)) {
    Blockly.Translation.map[typeName] = {};
  }
  if (!Blockly.Translation.haveProperty(typeName, propertyName)) {
    Blockly.Translation.map[typeName][propertyName] = propertyValue;
  }
}

/**
 * propertyList is an array list of property with format [propertyValue, propertyName]
 */
Blockly.Translation.addProperties = function(typeName, propertyList) {
  for(var i = 0; i < propertyList.length; i++) {
    var propertyValue = propertyList[i][0];
    var propertyName = propertyList[i][1];
    Blockly.Translation.addProperty(typeName, propertyName, propertyValue)
  }
}

Blockly.Translation.updateProperty = function(typeName, propertyName, newPropertyValue) {
  if (Blockly.Translation.haveProperty(typeName, propertyName)) {
    Blockly.Translation.map[typeName][propertyName] = newPropertyValue;
  }
}

Blockly.Translation.getProperty = function(typeName, propertyName) {
  if (!Blockly.Translation.haveProperty(typeName, propertyName)) {
    return [];
  }
  var propItem = [Blockly.Translation.map[typeName][propertyName], propertyName];
  return propItem;
}

Blockly.Translation.getAllProperties = function(typeName) {
  if (!Blockly.Translation.haveType(typeName)) {
    return [];
  }
  var propList = [];
  for(var propertyName in Blockly.Translation.map[typeName]) {
    var propItem = [Blockly.Translation.map[typeName][propertyName], propertyName];
    propList.push(propItem);
  }
  return propList;
}

//Blockly.Translation.addProperties('button',[['backgroundcolor','backgroundcolor'],['color','color']]);