// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Methods for manipulating App Inventor components - adding, removing,
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
 * Object whose fields are names of component types and whose field values are the parsed JSON type
 * objects for the component type. Populated by the Blockly.Component.add method.
 */
Blockly.ComponentTypes = {};

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
  
  // TODO: figure out the type name before we reparse the json string so that we can avoid 
  // reparsing if we already have it.
  var typeDescription = JSON.parse(typeJsonString);
  if (!Blockly.ComponentTypes[typeDescription.name]) {
    Blockly.ComponentTypes[typeDescription.name] = typeDescription;
  }
  Blockly.ComponentInstances[name] = {};
  Blockly.ComponentInstances[name].uid = uid;
  Blockly.ComponentInstances[name].blocks = [];
  
  // Add event blocks
  for (var i = 0, eventType; eventType = typeDescription.events[i]; i++) {
    var elementName = name + '_' + eventType.name;
    Blockly.Language[elementName] = new Blockly.ComponentBlock.event(eventType, name);
    Blockly.Yail[elementName] = Blockly.Yail.event(name, eventType.name);
    Blockly.ComponentInstances[name].blocks.push(elementName);
  }
  
  // Add method blocks
  for (var i = 0, methodType; methodType = typeDescription.methods[i]; i++) {
    var elementName = name + '_' + methodType.name;
    Blockly.Language[elementName] = new Blockly.ComponentBlock.method(methodType, name);
    if (methodType.returnType) {
      Blockly.Yail[elementName] = Blockly.Yail.methodWithReturn(name, methodType.name);
    } else {
      Blockly.Yail[elementName] = Blockly.Yail.methodNoReturn(name, methodType.name);
    }
    Blockly.ComponentInstances[name].blocks.push(elementName);
  }
  // Add getter and setter blocks with drop-downs containing relevant property names
  var getters = [], setters = [];
  var propSetterName = name + '_setproperty'
  var propGetterName = name + '_getproperty'
  Blockly.Yail[propSetterName] = Blockly.Yail.setproperty(name);
  Blockly.Yail[propSetterName].propTypes = {};
  Blockly.Yail[propGetterName] = Blockly.Yail.getproperty(name);
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
    }  // some properites have rw = "invisible". ignore those.
    
    Blockly.Yail[propSetterName].propTypes[propType.name] = propType.type;
  }
  
  Blockly.Language[propGetterName] = new Blockly.ComponentBlock.getter(getters, name);
  Blockly.ComponentInstances[name].blocks.push(name + '_getproperty');
  Blockly.Language[propSetterName] = new Blockly.ComponentBlock.setter(setters, name);
  Blockly.ComponentInstances[name].blocks.push(name + '_setproperty');
  // Add "component" block
  Blockly.Language[name + '_component'] = new Blockly.ComponentBlock.component(name);
  Blockly.Yail[name + '_component'] = Blockly.Yail.componentObject(name);
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
    if (block.type == 'procedures_defnoreturn' || block.type == 'procedures_defreturn') {
      map.globals.push(block);
      // TODO: eventually deal with variable declarations, once we have them
    } else if (block.category == 'Component') {
      var instanceName = block.instanceName;
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
 * @param {String} componentTypeName (e.g., "Button")
 * @param {String} propertyName (e.g., "Text")
 * @returns {String} the type of the named property associated with the named component type
 *    (from the JSON component description passed to Blockly.Component.add).
 */
Blockly.Component.getPropertyType = function(componentTypeName, propertyName) {
  var componentType = Blockly.ComponentTypes[componentTypeName];
  if (!componentType) {
    throw "Can't find component type info for " + componentTypeName;
  }
  for (var i = 0, prop; prop = componentType.blockProperties[i]; i++) {
    if (prop.name == propertyName) {
      return prop.type;
    }
  }
  // TODO: note that the old Yail code generator puts up an error message in this case but then
  // forges ahead using a property type of "text"
  throw "Can't find property type for " + componentType + "." + propertyName;
}





