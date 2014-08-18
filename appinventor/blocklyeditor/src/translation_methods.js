// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle Internationalization for AI2 Blockseditor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.TranslationMethods');

Blockly.TranslationMethods.map = {};

Blockly.TranslationMethods.haveMethod = function (methodName) {
  return Blockly.TranslationMethods.map[methodName] != undefined;
}

Blockly.TranslationMethods.addMethod = function (methodName, methodValue, methodDescription) {
  if (!Blockly.TranslationMethods.haveMethod(methodName)) {
    Blockly.TranslationMethods.map[methodName] = [methodValue, methodDescription];
  }
}

/**
 * methodList is an array list of method with format [methodValue, methodName, methodDescription]
 */
Blockly.TranslationMethods.addMethods = function (methodList) {
  for (var i = 0; i < methodList.length; i++) {
    var methodValue = methodList[i][0];
    var methodName = methodList[i][1];
    var methodDescription = methodList[i][2];
    Blockly.TranslationMethods.addMethod(methodName, methodValue, methodDescription);
  }
}

Blockly.TranslationMethods.updateMethod = function (methodName, methodValue, methodDescription) {
  if (Blockly.TranslationMethods.haveMethod(methodName)) {
    var decription = methodDescription;
    if (methodDescription == '') {
      decription = Blockly.TranslationMethods.map[methodName][1];
    }
    Blockly.TranslationMethods.map[methodName] = [methodValue, decription];
  } else {
    Blockly.TranslationMethods.addMethod(methodName, methodValue, methodDescription);
  }
}

/**
 * methodList is an array list of method with format [methodValue, methodName]
 */
Blockly.TranslationMethods.updateMethods = function (methodList) {
  for (var i = 0; i < methodList.length; i++) {
    var methodValue = methodList[i][0];
    var methodName = methodList[i][1];
    var methodDescription = methodList[i][2];
    Blockly.TranslationMethods.updateMethod(methodName, methodValue, methodDescription);
  }
}

Blockly.TranslationMethods.getMethodValue = function (methodName) {
  if (!Blockly.TranslationMethods.haveMethod(methodName)) {
    return '';
  }
  return Blockly.TranslationMethods.map[methodName][0];
}

Blockly.TranslationMethods.getMethodDescription = function (methodName) {
  if (!Blockly.TranslationMethods.haveMethod(methodName)) {
    return '';
  }
  return Blockly.TranslationMethods.map[methodName][1];
}

/**
 * Update method map
 *
 * { "key": "METHOD-NAME",
 *   "value": "METHOD-VALUE",
 *   "decription": "METHOD-DESCRIPTION",
 * }
 *
 */
Blockly.TranslationMethods.updateMethodMap = function (typeDescription) {
  var methodName = typeDescription.key;
  var methodValue = typeDescription.value;
  var methodDescription = typeDescription.decription;
  Blockly.TranslationMethods.updateMethod(methodName, methodValue);
}

/**
 * Update method map
 *
 * typeJsonString has the following format (where upper-case strings are
 * non-terminals and lower-case strings are literals):
 * [
 *    { "key": "METHOD-NAME",
 *      "value": "METHOD-VALUE",
 *      "decription": "METHOD-DESCRIPTION",
 *    },+
 * ]
 */
Blockly.TranslationMethods.updateMap = function (jsonString) {
  var methods = JSON.parse(jsonString);

  for (var i = 0, typeDescription; typeDescription = methods[i]; i++) {
    Blockly.TranslationMethods.updateMethodMap(typeDescription);
  }
}