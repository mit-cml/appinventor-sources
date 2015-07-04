// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle Internationalization for AI2 Blockseditor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.TranslationProperties');

Blockly.TranslationProperties.map = {};

Blockly.TranslationProperties.rwState = {
  READ_ONLY: 0,
  READ_WRITE: 1,
  WRITE_ONLY: 2
}

Blockly.TranslationProperties.haveType = function (typeName) {
  return Blockly.TranslationProperties.map[typeName] != undefined;
}

Blockly.TranslationProperties.haveProperty = function (typeName, propertyName) {
  if (!Blockly.TranslationProperties.haveType(typeName)) {
    return false;
  }
  return Blockly.TranslationProperties.map[typeName][propertyName] != undefined;
}

Blockly.TranslationProperties.addProperty = function (typeName, propertyName, propertyValue, rw) {
  if (!Blockly.TranslationProperties.haveType(typeName)) {
    Blockly.TranslationProperties.map[typeName] = {};
  }
  if (!Blockly.TranslationProperties.haveProperty(typeName, propertyName)) {
    Blockly.TranslationProperties.map[typeName][propertyName] = [propertyValue, rw];
  }
}

/**
 * propertyList is an array list of property with format [propertyValue, propertyName, rw]
 */
Blockly.TranslationProperties.addProperties = function (typeName, propertyList) {
  for (var i = 0; i < propertyList.length; i++) {
    var propertyValue = propertyList[i][0];
    var propertyName = propertyList[i][1];
    var rw = propertyList[i][2];
    Blockly.TranslationProperties.addProperty(typeName, propertyName, propertyValue, rw);
  }
}

Blockly.TranslationProperties.updateProperty = function (typeName, propertyName, propertyValue) {
  if (Blockly.TranslationProperties.haveProperty(typeName, propertyName)) {
    var rw = Blockly.TranslationProperties.map[typeName][propertyName][1];
    Blockly.TranslationProperties.map[typeName][propertyName] = [propertyValue, rw];
  }
}

/**
 * propertyList is an array list of property with format [propertyValue, propertyName, rw]
 */
Blockly.TranslationProperties.updateProperties = function (typeName, propertyList) {
  for (var i = 0; i < propertyList.length; i++) {
    var propertyValue = propertyList[i][0];
    var propertyName = propertyList[i][1];
    Blockly.TranslationProperties.updateProperty(typeName, propertyName, propertyValue);
  }
}

Blockly.TranslationProperties.getProperty = function (typeName, propertyName) {
  if (!Blockly.TranslationProperties.haveProperty(typeName, propertyName)) {
    return [];
  }
  var propItem = [Blockly.TranslationProperties.map[typeName][propertyName][0], propertyName];
  return propItem;
}

/**
 * Return all properties (getter + setter methods)
 */
Blockly.TranslationProperties.getAllProperties = function (typeName) {
  if (!Blockly.TranslationProperties.haveType(typeName)) {
    return [];
  }
  var propList = [];
  for (var propertyName in Blockly.TranslationProperties.map[typeName]) {
    var propItem = Blockly.TranslationProperties.getProperty(typeName, propertyName);
    propList.push(propItem);
  }
  return propList;
}

/**
 * Return properties of setter method
 */
Blockly.TranslationProperties.getAllPropertiesSetter = function (typeName) {
  if (!Blockly.TranslationProperties.haveType(typeName)) {
    return [];
  }
  var propList = [];
  for (var propertyName in Blockly.TranslationProperties.map[typeName]) {
    var propValues = Blockly.TranslationProperties.map[typeName][propertyName];
    if (propValues[1] == Blockly.TranslationProperties.rwState.READ_WRITE ||
        propValues[1] == Blockly.TranslationProperties.rwState.WRITE_ONLY) {
      var propItem = [propValues[0], propertyName];
      propList.push(propItem);
    }
  }
  return propList;
}

/**
 * Return properties of getter method
 */
Blockly.TranslationProperties.getAllPropertiesGetter = function (typeName) {
  if (!Blockly.TranslationProperties.haveType(typeName)) {
    return [];
  }
  var propList = [];
  for (var propertyName in Blockly.TranslationProperties.map[typeName]) {
    var propValues = Blockly.TranslationProperties.map[typeName][propertyName];
    if (propValues[1] == Blockly.TranslationProperties.rwState.READ_WRITE ||
        propValues[1] == Blockly.TranslationProperties.rwState.READ_ONLY) {
      var propItem = [propValues[0], propertyName];
      propList.push(propItem);
    }
  }
  return propList;
}

/**
 * Update property map for a component of the given type

 * { "name": "COMPONENT-TYPE-NAME",
 *   "blockProperties": [
 *     {  "key": "PROPERTY-NAME",
 *        "value": "PROPERTY-VALUE",
 *        "rw": "read-only"|"read-write"|"write-only"|"invisible"},*
 *   ]
 * }
 */
Blockly.TranslationProperties.updateComponentMap = function (typeDescription) {
  var typeName = typeDescription.name;

  for (var i = 0, propType; propType = typeDescription.blockProperties[i]; i++) {
    Blockly.TranslationProperties.updateProperty(typeName, propType.key, propType.value);
  }
}

/**
 * Update property map
 *
 * typeJsonString has the following format (where upper-case strings are
 * non-terminals and lower-case strings are literals):
 * [
 *   { "name": "COMPONENT-TYPE-NAME",
 *      "blockProperties": [
 *      {  "key": "PROPERTY-NAME",
 *         "value": "PROPERTY-VALUE",
 *         "rw": "read-only"|"read-write"|"write-only"|"invisible"},*
 *      ]
 *  },+
 * ]
 */
Blockly.TranslationProperties.updateMap = function (jsonString) {
  var components = JSON.parse(jsonString);

  for (var i = 0, typeDescription; typeDescription = components[i]; i++) {
    Blockly.TranslationProperties.updateComponentMap(typeDescription);
  }
}