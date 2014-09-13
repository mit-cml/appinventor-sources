// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle Internationalization for AI2 Blockseditor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.TranslationParams');

Blockly.TranslationParams.map = {};

Blockly.TranslationParams.haveParam = function(paramName) {
    return Blockly.TranslationParams.map[paramName] != undefined;
}

Blockly.TranslationParams.addParam = function(paramName, paramValue) {
    if (!Blockly.TranslationParams.haveParam(paramName)) {
        Blockly.TranslationParams.map[paramName] = paramValue;
    }
}

/**
 * paramList is an array list of param with format [paramValue, paramName]
 */
Blockly.TranslationParams.addParams = function(paramList) {
    for(var i = 0; i < paramList.length; i++) {
        var paramValue = paramList[i][0];
        var paramName = paramList[i][1];
        Blockly.TranslationParams.addParam(paramName, paramValue);
    }
}

Blockly.TranslationParams.updateParam = function(paramName, paramValue) {
    if (Blockly.TranslationParams.haveParam(paramName)) {
        Blockly.TranslationParams.map[paramName] = paramValue;
    } else {
        Blockly.TranslationParams.addParam(paramName, paramValue);
    }
}

/**
 * paramList is an array list of param with format [paramValue, paramName]
 */
Blockly.TranslationParams.updateParams = function(paramList) {
    for(var i = 0; i < paramList.length; i++) {
        var paramValue = paramList[i][0];
        var paramName = paramList[i][1];
        Blockly.TranslationParams.updateParam(paramName, paramValue);
    }
}

Blockly.TranslationParams.getParam = function(paramName) {
    if (!Blockly.TranslationParams.haveParam(paramName)) {
        return '';
    }
    return Blockly.TranslationParams.map[paramName];
}


/**
 * Update parameter map 
 *
 * { "key": "PARAM-NAME",
 *   "value": "PARAM-VALUE",
 * }
 *
 */
Blockly.TranslationParams.updateParamMap = function(typeDescription) {
    var paramName = typeDescription.key;
    var paramValue = typeDescription.value;
    Blockly.TranslationParams.updateParam(paramName, paramValue); 
}

/**
 * Update param map 
 *
 * typeJsonString has the following format (where upper-case strings are
 * non-terminals and lower-case strings are literals):
 * [
 *    { "key": "PARAM-NAME",
 *      "value": "PARAM-VALUE",
 *    },+
 * ]
 */
Blockly.TranslationParams.updateMap = function(jsonString) {
    var params = JSON.parse(jsonString);
  
    for (var i = 0, typeDescription; typeDescription = params[i]; i++) {
        Blockly.TranslationParams.updateParamMap(typeDescription); 
    }
}