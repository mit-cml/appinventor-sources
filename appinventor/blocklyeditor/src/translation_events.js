// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle Internationalization for AI2 Blockseditor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.TranslationEvents');

Blockly.TranslationEvents.map = {};

Blockly.TranslationEvents.haveEvent = function(eventName) {
    return Blockly.TranslationEvents.map[eventName] != undefined;
}

Blockly.TranslationEvents.addEvent = function(eventName, eventValue, eventDescription) {
    if (!Blockly.TranslationEvents.haveEvent(eventName)) {
        Blockly.TranslationEvents.map[eventName] = [eventValue, eventDescription];
    }
}

/**
 * eventList is an array list of event with format [eventValue, eventName, eventDescription]
 */
Blockly.TranslationEvents.addEvents = function(eventList) {
    for(var i = 0; i < eventList.length; i++) {
        var eventValue = eventList[i][0];
        var eventName = eventList[i][1];
        var eventDescription = eventList[i][2];
        Blockly.TranslationEvents.addEvent(eventName, eventValue, eventDescription);
    }
}

Blockly.TranslationEvents.updateEvent = function(eventName, eventValue, eventDescription) {
    if (Blockly.TranslationEvents.haveEvent(eventName)) {
        var decription = eventDescription;
        if (eventDescription == '') {
            decription = Blockly.TranslationEvents.map[eventName][1];
        }
        Blockly.TranslationEvents.map[eventName] = [eventValue, decription];
    } else {
        Blockly.TranslationEvents.addEvent(eventName, eventValue, eventDescription);
    }
}

/**
 * eventList is an array list of event with format [eventValue, eventName]
 */
Blockly.TranslationEvents.updateEvents = function(eventList) {
    for(var i = 0; i < eventList.length; i++) {
        var eventValue = eventList[i][0];
        var eventName = eventList[i][1];
        var eventDescription = eventList[i][2];
        Blockly.TranslationEvents.updateEvent(eventName, eventValue, eventDescription);
    }
}

Blockly.TranslationEvents.getEventValue = function(eventName) {
    if (!Blockly.TranslationEvents.haveEvent(eventName)) {
        return '';
    }
    return Blockly.TranslationEvents.map[eventName][0];
}

Blockly.TranslationEvents.getEventDescription = function(eventName) {
    if (!Blockly.TranslationEvents.haveEvent(eventName)) {
        return '';
    }
    return Blockly.TranslationEvents.map[eventName][1];
}


/**
 * Update event map 
 *
 * { "key": "EVENT-NAME",
 *   "value": "EVENT-VALUE",
 *   "decription": "EVENT-DESCRIPTION",
 * }
 *
 */
Blockly.TranslationEvents.updateEventMap = function(typeDescription) {
    var eventName = typeDescription.key;
    var eventValue = typeDescription.value;
    var eventDescription = typeDescription.description;
    Blockly.TranslationEvents.updateEvent(eventName, eventValue, eventDescription); 
}

/**
 * Update event map 
 *
 * typeJsonString has the following format (where upper-case strings are
 * non-terminals and lower-case strings are literals):
 * [
 *    { "key": "EVENT-NAME",
 *      "value": "EVENT-VALUE",
 *      "decription": "EVENT-DESCRIPTION",
 *    },+
 * ]
 */
Blockly.TranslationEvents.updateMap = function(jsonString) {
    var events = JSON.parse(jsonString);
  
    for (var i = 0, typeDescription; typeDescription = events[i]; i++) {
        Blockly.TranslationEvents.updateEventMap(typeDescription); 
    }
}