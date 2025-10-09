// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2012-2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Methods for manipulating App Inventor components - adding, removing,
 * renaming, etc.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.Component');
goog.provide('AI.Blockly.ComponentTypes');
goog.provide('AI.Blockly.ComponentInstances');

if (Blockly.Component === undefined) Blockly.Component = {};
if (Blockly.ComponentTypes === undefined) Blockly.ComponentTypes = {};
if (Blockly.ComponentInstances === undefined) Blockly.ComponentInstances = {};


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

  var blocks = Blockly.common.getMainWorkspace().getTopBlocks(true);
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
      if (block.isGeneric) {
        map.globals.push(block);
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
  // We can only verify blocks once the workspace has been injected...
  if (Blockly.common.getMainWorkspace() != null) {
    var allBlocks = Blockly.common.getMainWorkspace().getAllBlocks();
    for (var x = 0, block; block = allBlocks[x]; ++x) {
      if (block.category != 'Component') {
        continue;
      }
      block.verify();
    }
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
 * @param projectId the projectid whose types we are loading. Note: projectId is
 *        a string at this point. We will convert it to a long in Java code we call
 *        later.
 */
Blockly.ComponentTypes.populateTypes = function(projectId) {
  var componentInfoArray = JSON.parse(window.parent.BlocklyPanel_getComponentsJSONString(projectId));
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
