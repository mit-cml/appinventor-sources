// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods for manipulating App Inventor components - adding, removing,
 * renaming, etc.
 * 
 * @author sharon@google.com (Sharon Perl)
 */

if (!Blockly.Language) {
  Blockly.Language = {};
}

if (!Blockly.Component) {
  Blockly.Component = {};
}

/**
 * Object whose fields are names of component instances and whose field values
 * are objects with a blocks field containing an array of block names for the
 * instance.
 * For example:
 *    Blockly.ComponentInstances['Canvas1'].blocks = ['Canvas1_Touched', 
 *        'Canvas1_DrawCircle', 'Canvas1_getproperty', 'Canvas1_setproperty']
 * Blockly.ComponentInstances is populated by the Blockly.Component.add method.
 */
Blockly.ComponentInstances = {};

/** 
 * Add block prototypes for a component of the given type (described in 
 * typeJsonString) and instance name with given unique id uid to 
 * Blockly.Language and add the names of the blocks to ComponentInstances[name].
 * 
 * typeJsonString has the following format (where upper-case strings are
 * non-terminals and lower-case strings are literals):
 * { "name": "COMPONENT-TYPE-NAME",
 *   "version": "VERSION",
 *   "categoryString": "PALETTE-CATEGORY",
 *   "helpString": “DESCRIPTION”,
 *   "showOnPalette": "true"|"false",
 *   "nonVisible": "true"|"false",
 *   "iconName": "ICON-FILE-NAME",
 *   "properties": [
 *     { "name": "PROPERTY-NAME", 
 *        "editorType": "EDITOR-TYPE", 
 *        "defaultValue": "DEFAULT-VALUE"},*
 *    ],
 *   "blockProperties": [
 *     { "name": "PROPERTY-NAME", 
 *        "description": "DESCRIPTION", 
 *        "type": "YAIL-TYPE",
 *        "rw": "read-only"|"read-write"|"write-only"|"invisible"},*
 *   ],
 *   "events": [
 *     { "name": "EVENT-NAME", 
 *       "description": "DESCRIPTION", 
 *       "params": [
 *         { "name": "PARAM-NAME", 
 *           "type": "YAIL-TYPE"},*
 *       ]},+
 *   ],
 *   “methods”: [
 *     { "name": "METHOD-NAME", 
 *       "description": "DESCRIPTION", 
 *       "params": [
 *         { "name": "PARAM-NAME", 
 *       "type": "YAIL-TYPE"},*
 *     ]},+
 *   ]
 * }
 * 
 * TODO: May want to save (and return) result of parsing typeJsonString if it
 * is too inefficient to do this for each added component
 */
Blockly.Component.add = function(typeJsonString, name, uid) {
  if (Blockly.ComponentInstances[name] != undefined 
      && Blockly.ComponentInstances[name].uid == uid) {
    return;
  }
  // TODO(sharon): deal with case where name is there but with a different uid
  // what to do? (old component should have been renamed or removed first?)
  // Also, detect case where we have the uid but with a different name (component
  // should have been renamed).
  var typeDescription = JSON.parse(typeJsonString);
  Blockly.ComponentInstances[name] = {};
  Blockly.ComponentInstances[name].uid = uid;
  Blockly.ComponentInstances[name].blocks = [];
  
  // Add event blocks
  for (var i = 0, eventType; eventType = typeDescription.events[i]; i++) {
    var elementName = name + '_' + eventType.name;
    Blockly.Language[elementName] = new Blockly.ComponentBlock.event(eventType, name);
    Blockly.ComponentInstances[name].blocks.push(elementName);
  }
  // Add method blocks
  for (var i = 0, methodType; methodType = typeDescription.methods[i]; i++) {
    var elementName = name + '_' + methodType.name;
    Blockly.Language[elementName] = new Blockly.ComponentBlock.method(methodType, name);
    Blockly.ComponentInstances[name].blocks.push(elementName);
  }
  // Add getter and setter blocks with drop-downs containing relevant property names
  var getters = [], setters = [];
  for (var i = 0, propType; propType = typeDescription.blockProperties[i]; i++) {
    // Note: an items in the menu is a two-element array with a human-readable
    // text and a language-neutral value. For now we leave this the same,
    // but this might need attention with i18n.
    var propItem = [propType.name, propType.name];
    if (propType.rw == "read-only") {
      getters.push(propItem);
    } else if (propType.rw == "read-write") {
      getters.push(propItem);
      setters.push(propItem);
    } else if (propType.rw == "write-only") {
      setters.push(propItem);
    }
    // some properites have rw = "invisible". ignore those.
  }
  Blockly.Language[name + '_getproperty'] = new Blockly.ComponentBlock.getter(getters, name);
  Blockly.ComponentInstances[name].blocks.push(name + '_getproperty');
  Blockly.Language[name + '_setproperty'] = new Blockly.ComponentBlock.setter(setters, name);
  Blockly.ComponentInstances[name].blocks.push(name + '_setproperty');
  // Add "component" block
  Blockly.Language[name + '_component'] = new Blockly.ComponentBlock.component(name);
  Blockly.ComponentInstances[name].blocks.push(name + '_component');
};



/**
 * Rename component with given uid and instance name oldname to newname
 */
Blockly.Component.rename = function(oldname, newname, uid) {
  // TODO(sharon): implement this!
  console.log("Got call to Blockly.Component.rename(" + oldname + ", " + newname + 
    ", " + uid + ") - not yet implemented");
};

/**
 * Remove component with given type and instance name and unique id uid
 */
Blockly.Component.remove = function(type, name, uid) {
  // TODO(sharon): implement this!
  console.log("Got call to Blockly.Component.remove(" + type + ", " + name + 
    ", " + uid + ") - not yet implemented");
  
  /* TBD...ignore this code for now.
  // remove all blocks related to the component from the language
  if (!ComponentTypeElements[type]) {
    // throw 'Error: "' + type + '" is an unknown component type';
    console.log("Got call to Blockly.Component.remove(" + type + ", " + name + 
      ", " + uid + ") - unknown type");
    return;
  }
  for (var i in Blockly.ComponentTypeElements[type]) {
    // elements are events, methods, property setter/getter, component block
    // remove blocks for the elements for this component instance from the language
    var element = Blockly.ComponentTypeElements[type][i];
    var elementName = name + '_' + element;
    delete Blockly.Language[elementName];
  }
  
  // remove the component instance
  Blockly.ComponentInstances[name] = null;
  */
};





