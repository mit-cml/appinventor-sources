// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Methods for manipulating App Inventor components - adding, removing,
 * renaming, etc.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 */

'use strict';

goog.provide('Blockly.Component');
goog.provide('Blockly.ComponentTypes');
goog.provide('Blockly.ComponentInstances');

goog.require('Blockly.TypeBlock');
goog.require('Blockly.TranslationProperties');
goog.require('Blockly.TranslationEvents');
goog.require('Blockly.TranslationMethods');
goog.require('Blockly.TranslationParams');

Blockly.Component.add = function(name, uid) {
  if (Blockly.ComponentInstances.haveInstance(name, uid)) {
    return;
  }
  Blockly.TypeBlock.needsReload.components = true;
  //get type name for instance
  var typeName = Blockly.Component.instanceNameToTypeName(name);
  Blockly.ComponentInstances.addInstance(name, uid, typeName);
};

/**
 * Rename component with given uid and instance name oldname to newname
 * @param oldname the Component's current name, e.g., Button1
 * @param newname the newname the component will be given, e.g., Button2
 * @param uid the component's unique id
 *
 * Here are the various places that a component's name must be changed, using Button1
 *  as an example name.
 * Blockly.ComponentInstances -- an index containing an entry for each Component used by the app
 *  keyed on its oldname, needs to change to the new name
 *  e.g., ComponentInstances['Button1'] --> ComponentInstances['Button2']
 *
 * Call rename on all component blocks
 */
Blockly.Component.rename = function(oldname, newname, uid) {
  console.log("Got call to Blockly.Component.rename(" + oldname + ", " + newname + ", " + uid + ")");
  Blockly.TypeBlock.needsReload.components = true;
  if (!Blockly.ComponentInstances.haveInstance(oldname, uid)) {
    console.log("Renaming, No such Component instance " + oldname + " aborting");
    return;
  }
  // Create an entry in Blockly.ComponentInstances for the block's newname and delete oldname (below)
  Blockly.ComponentInstances[newname] = {}
  Blockly.ComponentInstances[newname].uid = uid;
  Blockly.ComponentInstances[newname].typeName = Blockly.ComponentInstances[oldname].typeName;

  // Delete the index entry for the oldname
  Blockly.ComponentInstances[oldname] = null;
  delete Blockly.ComponentInstances[oldname];

  console.log("Revised Blockly.ComponentInstances, Blockly.Language, Blockly.Yail for " + newname);

  // Revise names, types, and block titles for all blocks containing newname in Blockly.mainWorkspace
  var blocks = Blockly.mainWorkspace.getAllBlocks();
  for (var x = 0, block; block = blocks[x]; x++) {
    if (!block.category) {
      continue;
    } else if (block.category == 'Component') {
      block.rename(oldname, newname);      // Changes block's instanceName, typeName, and current title
    }
  }

  console.log("Revised Blockly.mainWorkspace for " + newname);
};


/**
 * Remove component with given type and instance name and unique id uid
 * @param type, Component's type -- e.g., Button
 * @param name, Component's name == e.g., Buton1
 * @param uid, Component's unique id -- not currently used
 *
 * The component should be listed in the ComponentInstances list.
 *   - For each instance of the component's block in the Blockly.mainWorkspace
 *     -- Call its BlocklyBlock.destroy() method to remove the block
 *        from the workspace and adjust enclosed or enclosing blocks.
 * Remove the block's entry from ComponentInstances
 *
 */
Blockly.Component.remove = function(type, name, uid) {
  console.log("Got call to Blockly.Component.remove(" + type + ", " + name + ", " + uid + ")");
  Blockly.TypeBlock.needsReload.components = true;

  // Delete instances of this type of block from the workspace
  var allblocks = Blockly.mainWorkspace.getAllBlocks();
  for (var x = 0, block; block = allblocks[x]; x++) {
    if (!block.category) {
      continue;
    } else if (block.category == 'Component' && block.instanceName == name) {
      block.dispose(true);     // Destroy the block gently
    }
  }

  // Remove the component instance
  console.log("Deleting " + name + " from Blockly.ComponentInstances");
  delete Blockly.ComponentInstances[name];
};

/**
 * Builds a map of component name -> top level blocks for that component.
 * A special entry for "globals" maps to top-level global definitions.
 *
 * @param warnings a Map that will be filled with warnings for troublesome blocks
 * @param errors a list that will be filled with error messages
 * @param forRepl whether this is executed for REPL
 * @param compileUnattachedBlocks whether to compile unattached blocks
 * @returns object mapping component names to the top-level blocks for that component in the
 *            workspace. For each component C the object contains a field "component.C" whose
 *            value is an array of blocks. In addition, the object contains a field named "globals"
 *            whose value is an array of all valid top-level blocks not associated with a
 *            component (procedure and variable definitions)
 */
Blockly.Component.buildComponentMap = function(warnings, errors, forRepl, compileUnattachedBlocks) {
  var map = {};
  map.components = {};
  map.globals = [];

  // TODO: populate warnings, errors as we traverse the top-level blocks

  var blocks = Blockly.mainWorkspace.getTopBlocks(true);
  for (var x = 0, block; block = blocks[x]; x++) {

    // TODO: deal with unattached blocks that are not valid top-level definitions. Valid blocks
    // are events, variable definitions, or procedure definitions.

    if (!block.category) {
      continue;
    }
    if (block.type == 'procedures_defnoreturn' || block.type == 'procedures_defreturn' || block.type == 'global_declaration') {
      map.globals.push(block);
      // TODO: eventually deal with variable declarations, once we have them
    } else if (block.category == 'Component') {
      var instanceName = block.instanceName;
      if(block.blockType != "event") {
        continue;
      }
      if (!map.components[instanceName]) {
        map.components[instanceName] = [];  // first block we've found for this component
      }

      // TODO: check for duplicate top-level blocks (e.g., two event handlers with same name) -
      // or better yet, prevent these from happening!

      map.components[instanceName].push(block);
    }
  }
  return map;
};

/**
 * Verify all blocks after a Component upgrade
 */
Blockly.Component.verifyAllBlocks = function () {
  var allBlocks = Blockly.mainWorkspace.getAllBlocks();
  for (var x = 0, block; block = allBlocks[x]; ++x) {
    if (block.category != 'Component') {
      continue;
    }
    block.verify();
  }
}

/**
 * Blockly.ComponentTypes
 *
 * Object whose fields are names of component types. For a given component type object, the "componentInfo"
 * field is the parsed JSON type object for the component type and the "blocks" field is an array
 * of block names for the generic blocks for that type.
 * For example:
 *    Blockly.ComponentTypes['Canvas'].componentInfo = the JSON object from parsing the typeJsonString
 *
 * eventDictionary, methodDictionary, and properties take in the name of the event/method/property
 * and give the relevant object in from the componentInfo object.
 *
 * The componentInfo has the following format (where upper-case strings are
 * non-terminals and lower-case strings are literals):
 * { "type": "COMPONENT-TYPE",
 *   "name": "COMPONENT-TYPE-NAME",
 *   "external": "true"|"false",
 *   "version": "VERSION",
 *   "categoryString": "PALETTE-CATEGORY",
 *   "helpString": "DESCRIPTION",
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
 *   "methods": [
 *     { "name": "METHOD-NAME",
 *       "description": "DESCRIPTION",
 *       "params": [
 *         { "name": "PARAM-NAME",
 *       "type": "YAIL-TYPE"},*
 *     ]},+
 *   ]
 * }
 */

Blockly.ComponentTypes.haveType = function(typeName) {
  return Blockly.ComponentTypes[typeName] != undefined;
};

/**
 * Populate Blockly.ComponentTypes object
 *
 */
Blockly.ComponentTypes.populateTypes = function() {

  var componentInfoArray = JSON.parse(window.parent.BlocklyPanel_getComponentsJSONString());
  for(var i=0;i<componentInfoArray.length;i++) {
    var componentInfo = componentInfoArray[i];
    var typeName = componentInfo.name;
    Blockly.ComponentTypes[typeName] = {};
    Blockly.ComponentTypes[typeName].type = componentInfo.type;
    Blockly.ComponentTypes[typeName].external = componentInfo.external;
    Blockly.ComponentTypes[typeName].componentInfo = componentInfo;
    Blockly.ComponentTypes[typeName].eventDictionary = {};
    Blockly.ComponentTypes[typeName].methodDictionary = {};
    Blockly.ComponentTypes[typeName].setPropertyList = [];
    Blockly.ComponentTypes[typeName].getPropertyList = [];
    Blockly.ComponentTypes[typeName].properties = {};

    //parse type description and fill in all of the fields
    for(var k=0;k<componentInfo.events.length;k++) {
      Blockly.ComponentTypes[typeName].eventDictionary[componentInfo.events[k].name] = componentInfo.events[k];
    }
    for(var k=0;k<componentInfo.methods.length;k++) {
      Blockly.ComponentTypes[typeName].methodDictionary[componentInfo.methods[k].name] = componentInfo.methods[k];
    }
    for(var k=0;k<componentInfo.blockProperties.length;k++) {
      Blockly.ComponentTypes[typeName].properties[componentInfo.blockProperties[k].name] = componentInfo.blockProperties[k];
      if (componentInfo.blockProperties[k].deprecated == "true") continue;
      if(componentInfo.blockProperties[k].rw == "read-write" || componentInfo.blockProperties[k].rw == "read-only") {
        Blockly.ComponentTypes[typeName].getPropertyList.push(componentInfo.blockProperties[k].name);
      }
      if(componentInfo.blockProperties[k].rw == "read-write" || componentInfo.blockProperties[k].rw == "write-only") {
        Blockly.ComponentTypes[typeName].setPropertyList.push(componentInfo.blockProperties[k].name);
      }

    }
  }
};

/**
 * Blockly.ComponentInstances
 *
 * Object whose fields are names of component instances and whose field values
 * are objects with a blocks field containing an array of block names for the
 * instance.
 * For example:
 *    Blockly.ComponentInstances['Canvas1'].blocks = ['Canvas1_Touched',
 *        'Canvas1_DrawCircle', 'Canvas1_getproperty', 'Canvas1_setproperty', ...]
 * Blockly.ComponentInstances is populated by the Blockly.Component.add method.
 */

Blockly.ComponentInstances.addInstance = function(name, uid, typeName) {
  Blockly.ComponentInstances[name] = {};
  Blockly.ComponentInstances[name].uid = uid;
  Blockly.ComponentInstances[name].typeName = typeName;
};

Blockly.ComponentInstances.haveInstance = function(name, uid) {
  return Blockly.ComponentInstances[name] != undefined
  && Blockly.ComponentInstances[name].uid == uid;
};

Blockly.ComponentInstances.getInstanceNames = function() {
  var instanceNames = [];
  for(var instanceName in Blockly.ComponentInstances) {
    if(typeof Blockly.ComponentInstances[instanceName] == "object" && Blockly.ComponentInstances[instanceName].uid != null){
      instanceNames.push(instanceName);
    }
  }
  return instanceNames;
}

Blockly.Component.instanceNameToTypeName = function(instanceName) {
  return window.parent.BlocklyPanel_getComponentInstanceTypeName(Blockly.BlocklyEditor.formName,instanceName);
}


Blockly.Component.getComponentNamesByType = function(componentType) {
  var componentNameArray = [];
  for(var componentName in Blockly.ComponentInstances) {
    if(Blockly.ComponentInstances[componentName].typeName == componentType) {
      componentNameArray.push([componentName,componentName]);
    }
  }
  return componentNameArray;
};
