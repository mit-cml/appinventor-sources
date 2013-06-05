//Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Methods for manipulating App Inventor components - adding, removing,
 * renaming, etc.
 *
 * @author sharon@google.com (Sharon Perl)
 */
'use strict';

goog.require('Blockly.TypeBlock');

if (!Blockly.Component) {
  Blockly.Component = {};
}

if (!Blockly.Language) {
  Blockly.Language = {};
}

/**
 * Add block prototypes for a component of the given type (described in
 * typeJsonString) and instance name with given unique id uid to
 * Blockly.Language and add the names of the blocks to ComponentInstances[name]. Also
 * add the generic blocks for this component's type if we don't already have them.
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
  if (Blockly.ComponentInstances.haveInstance(name, uid)) {
    return;
  }
  // TODO(sharon): deal with case where name is there but with a different uid
  // what to do? (old component should have been renamed or removed first?)
  // Also, detect case where we have the uid but with a different name (component
  // should have been renamed).

  Blockly.TypeBlock.needsReload.components = true;

  // TODO: figure out the type name before we reparse the json string so that we can avoid
  // reparsing if we already have it.
  var typeDescription = JSON.parse(typeJsonString);
  var typeName = typeDescription.name;
  var makeGenerics = false;  // make the generic blocks only if they don't already exist
  if (!Blockly.ComponentTypes.haveType(typeName)) {
    Blockly.ComponentTypes.addType(typeName, typeDescription);
    // TODO: reconsider whether we actually do want generic blocks for forms. Hal says:
    // It might be that even though there is only one form, a person might prefer to write code in
    // the generic style. It seems pointless for now, but it might become an issue when we
    // implement libraries. Similarly, it might be related to other possible "one of" components.
    // We don't have any of those now, but we might want to restrict to having only one TinyDB,
    // for example.
    if (typeName != "Form") {  // don't include generic blocks for the Form. There's only one!
      makeGenerics = true;
    }
  }

  Blockly.ComponentInstances.addInstance(name, uid);
  Blockly.ComponentInstances[name].typeName = typeName;
  // Add event blocks
  for (var i = 0, eventType; eventType = typeDescription.events[i]; i++) {
    Blockly.Component.addBlockAndGenerator(name, name + '_' + eventType.name,
        new Blockly.ComponentBlock.event(eventType, name, typeName),
        Blockly.Yail.event(name, eventType.name));
    // TODO: consider adding generic event blocks. We don't have them for now (since the original
    // App Inventor didn't have them).
  }

  // Add method blocks
  for (var i = 0, methodType; methodType = typeDescription.methods[i]; i++) {
    Blockly.Component.addBlockAndGenerator(name,
        name + '_' + methodType.name,
        new Blockly.ComponentBlock.method(methodType, name, typeName),
        (methodType.returnType
            ? Blockly.Yail.methodWithReturn(name, methodType.name)
                : Blockly.Yail.methodNoReturn(name, methodType.name)));
    if (makeGenerics) {
      // Need to distinguish names of generic blocks from regular component blocks. Since
      // a component instance name can be the same as a component type name, but it cannot start
      // with "_", we add a prefix to the type name that starts with "_" to make it unique (and
      // the "any" is to make it clearer when debugging).
      Blockly.Component.addGenericBlockAndGenerator(typeName,
          "_any_" + typeName + '_' + methodType.name,
          new Blockly.ComponentBlock.genericMethod(methodType, typeName),
          methodType.returnType
          ? Blockly.Yail.genericMethodWithReturn(typeName, methodType.name)
              : Blockly.Yail.genericMethodNoReturn(typeName, methodType.name));
    }
  }
  // Add getter and setter blocks with drop-downs containing relevant property names
  // and Yail type of the property
  var getters = [], setters = [] ;
  var propYailTypes = {};
  var propTooltips = {};
  for (var i = 0, propType; propType = typeDescription.blockProperties[i]; i++) {
    // Note: Each item in the menu is a two-element array with a human-readable
    // text and a language-neutral value. For now we leave these the same,
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

    // We pass the Yail types to the property block constructors.
    // The contructors will convert them to Blockly types to use in
    // the socket restrictions
    propYailTypes[propType.name] = propType.type;
    propTooltips[propType.name] = propType.description;
  }

  if (getters.length > 0) {
    Blockly.Component.addBlockAndGenerator(name,
        name + '_getproperty',
        new Blockly.ComponentBlock.getter(getters, propYailTypes, propTooltips, name, typeName),
        Blockly.Yail.getproperty(name));
  };

  if (setters.length > 0) {
    Blockly.Component.addBlockAndGenerator(name,
        name + '_setproperty',
        new Blockly.ComponentBlock.setter(setters, propYailTypes, propTooltips, name, typeName),
        Blockly.Yail.setproperty(name));
  };

  if (makeGenerics) {

    if (getters.length > 0) {
      Blockly.Component.addGenericBlockAndGenerator(typeName,
          typeName + '_getproperty',
          new Blockly.ComponentBlock.genericGetter(getters, propYailTypes, propTooltips, typeName),
          Blockly.Yail.genericGetproperty(typeName));
    }

    if (setters.length > 0) {
      Blockly.Component.addGenericBlockAndGenerator(typeName,
          typeName + '_setproperty',
          new Blockly.ComponentBlock.genericSetter(setters, propYailTypes, propTooltips, typeName),
          Blockly.Yail.genericSetproperty(typeName));
    }
  }

  Blockly.Component.addBlockAndGenerator(name,
      name + '_component',
      new Blockly.ComponentBlock.component(name,typeName),
      Blockly.Yail.componentObject(name));

  // For debugging purposes
  Blockly.Component.display(name);
};


/**
 * Displays a component's name in the various structures that contain it.
 * @param instanceName the component's current name, e.g., Button1
 */
Blockly.Component.display = function(instanceName) {
  var blocks = Blockly.ComponentInstances[instanceName].blocks;
  for (var i = 0; i < blocks.length; i++) { 
    var blockname = blocks[i];
    var blocklyblock = Blockly.Language[blockname];
  }
}

/**
 * Rename component with given uid and instance name oldname to newname
 * @param oldname the Component's current name, e.g., Button1
 * @param newname the newname the component will be given, e.g., Button2
 * @param uid the component's unique id
 * 
 * Here are the various places that a component's name must be changed, using Button1
 *  as an example name.
 * Blockly.ComponentInstances -- an index containing an entry for each Component used by the app
 *  keyed on its oldname and containing the names of all of the Component's blocks
 *  e.g., ComponentInstances['Button1'].blocks: ['Button1_Click', 'Button1_LongClick',...]
 *  >>    ComponentInstances['Button2'].blocks: ['Button2_Click', 'Button2_LongClick',...]

 * Blockly.Language: a dictionary that contains definitions of all Component blocks used by the 
 *  app keyed by their block name and pointing to objects that represent the various block types
 *  e.g., Blockly.Language['Button1_Click'], Blockly.Language['Button1_LongClick'], ...
 *  where Blockly.Language['Button1_Click'] --> ComponentBlock.instanceName='Button1_Click'
 *  >>    Blockly.Language['Button2_Click'], Blockly.Language['Button2_LongClick'], ...
 *  >>    Blockly.Language['Button2_Click'] --> ComponentBlock.instanceName='Button2_Click'
 *
 * Blockly.Yail: a dictionary of code generators for each type of block used by the app
 *  e.g., Blockly.Yail['Button1_Click'], Blockly.Yail['Button1_LongClick'], ...
 *  >>    Blockly.Yail['Button2_Click'], Blockly.Yail['Button2_LongClick'], ...
 *  >>    Blockly.Yail['Button2_Click'] = Blackly.Yail.<codegenerator>  -- revise the code generator

 * Blockly.mainWorkspace: a list of all the actual BlocklyBlocks created and currently in the workspace
 *  e.g., Blockly.mainWorkspace[].instanceName='Button1' 
 *        Blockly.mainWorkspace[].type='Button1_Click' 
 *        Blockly.mainWorkspace[].inputList[0].titleRow[0].text_ = 'when Button1.Click'
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
  Blockly.ComponentInstances[newname].blocks = [];
  Blockly.ComponentInstances[newname].typeName = Blockly.ComponentInstances[oldname].typeName;

  // Construct new names for each of the Component's block types -- e.g., Button1_Click
  var blocks = Blockly.ComponentInstances[oldname].blocks;
  for (var i = 0; i < blocks.length; i++) { 
    var oldblockname = blocks[i];                       // E.g. Button1_Click
    var blocklyblock = Blockly.Language[oldblockname];  // Get the block's definition from Blockly.Language

    // Construct the new name and push it onto the ComponentInstances[newname].blocks list
    var newblockname = newname + "_" + oldblockname.substring(oldblockname.indexOf('_') + 1);
    console.log("Renaming " + oldblockname + " to " + newblockname);
    Blockly.ComponentInstances[newname].blocks.push(newblockname);

    // Modify the block type's entry in Blockly.Language
    Blockly.Language[newblockname] = {}
    var temp = Blockly.Language[oldblockname];
    Blockly.Language[oldblockname] = null;
    delete Blockly.Language[oldblockname]; 
    Blockly.Language[newblockname] = temp;
    Blockly.Language[newblockname].instanceName = newname;

    // Modify the block type's entry in Blockly.Yail
    temp = Blockly.Yail[oldblockname];
    Blockly.Yail[oldblockname] = null;
    delete Blockly.Yail[oldblockname];
    Blockly.Yail[newblockname] = temp;

    // Regenerate the code
    console.log("Regenerating code for " + newname + " blocks");

    var blocktype = Blockly.Language[newblockname].blockType;
    if (blocktype === 'event') {
      Blockly.Yail[newblockname] = Blockly.Yail.event(newname, Blockly.Language[newblockname].eventType.name);
      Blockly.Language[newblockname].typeblock = [{ translatedName: newname + '.' + Blockly.Language[newblockname].eventType.name }];
    }
    else if (blocktype === 'methodwithreturn') {
      Blockly.Yail[newblockname] = Blockly.Yail.methodWithReturn(newname, Blockly.Language[newblockname].methodType.name);
      Blockly.Language[newblockname].typeblock = [{ translatedName: newname + '.' + Blockly.Language[newblockname].methodType.name }];
    }
    else if (blocktype === 'methodnoreturn') {
      Blockly.Yail[newblockname] = Blockly.Yail.methodNoReturn(newname, Blockly.Language[newblockname].methodType.name);
      Blockly.Language[newblockname].typeblock = [{ translatedName: newname + '.' + Blockly.Language[newblockname].methodType.name }];
    }
    else if (blocktype === 'getter') {
      Blockly.Yail[newblockname] = Blockly.Yail.getproperty(newname);
      var tbOptions = Blockly.Language[newblockname].typeblock;
      goog.array.forEach(tbOptions, function(option){
        option.translatedName = 'get ' + newname + '.' + option.dropDown.value;
      });
    }
    else if (blocktype === 'setter') {
      Blockly.Yail[newblockname] = Blockly.Yail.setproperty(newname);
      var tbOptions = Blockly.Language[newblockname].typeblock;
      goog.array.forEach(tbOptions, function(option){
        option.translatedName = 'set ' + newname + '.' + option.dropDown.value;
      });
    }
    else if (blocktype === 'component') {
      console.log("Generating component block code for " + newname);
      Blockly.Yail[newblockname] = Blockly.Yail.componentObject(newname);
      Blockly.Language[newblockname].typeblock = [{ translatedName: newname }];
    }
    else {// This is here for good measure, should never happen.
      throw new Error('BlockType: ' + blocktype + ' not recognisable?');
    }
  }

  // Delete the index entry for the oldname
  Blockly.ComponentInstances[oldname] = null;
  delete Blockly.ComponentInstances[oldname];

  console.log("Revised Blockly.ComponentInstances, Blockly.Language, Blockly.Yail for " + newname);

  // Revise names, types, and block titles for all blocks containing newname in Blockly.mainWorkspace
  blocks = Blockly.mainWorkspace.getAllBlocks();
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
 * For each of its Component (prototype) blocks -- e.g., Button1_OnClick, Button1_OnLongClick, etc.
 *   - delete the block's  structure from Blockly.Language
 *   - delete the block's structure from Blockly.Yail
 *   - For each instance of the block in the Blockly.mainWorkspace
 *     -- Call its BlocklyBlock.destroy() method to remove the block
 *        from the workspace and adjust enclosed or enclosing blocks.
 * Remove the block's entry from ComponentInstances
 *
 */
Blockly.Component.remove = function(type, name, uid) {
  console.log("Got call to Blockly.Component.remove(" + type + ", " + name + ", " + uid + ")");
  Blockly.TypeBlock.needsReload.components = true;
  for (var i=0; i < Blockly.ComponentInstances[name].blocks.length; i++) {
    var elementName = Blockly.ComponentInstances[name].blocks[i]
    console.log("Deleting " + elementName + " from Blockly.Lanaguage");
    delete Blockly.Language[elementName];
    console.log("Deleting " + elementName + " from Blockly.Yail");
    delete Blockly.Yail[elementName];

  }
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
 * @param {String} componentTypeName (e.g., "Button")
 * @param {String} propertyName (e.g., "Text")
 * @returns {String} the type of the named property associated with the named component type
 *    (from the JSON component description passed to Blockly.Component.add).
 */
Blockly.Component.getPropertyType = function(componentTypeName, propertyName) {
  var componentType = Blockly.ComponentTypes.getType(componentTypeName);
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
};

/**
 * Blockly.ComponentTypes
 *
 * Object whose fields are names of component types. For a given component type object, the "type"
 * field is the parsed JSON type object for the component type and the "blocks" field is an array
 * of block names for the generic blocks for that type.
 * For example:
 *    Blockly.ComponentTypes['Canvas'].type = the JSON object from parsing the typeJsonString
 *      argument passed to Blockly.Component.add for a Canvas component instance
 *    Blockly.ComponentTypes['Canvas'].blocks = ['Canvas_Touched', 'Canvas_DrawCircle', ...]
 * Populated by Blockly.Component.add.
 */
Blockly.ComponentTypes = {};

Blockly.ComponentTypes.haveType = function(typeName) {
  return Blockly.ComponentTypes[typeName] != undefined;
};

Blockly.ComponentTypes.addType = function(typeName, typeDescription) {
  Blockly.ComponentTypes[typeName] = {};
  Blockly.ComponentTypes[typeName].type = typeDescription;
  Blockly.ComponentTypes[typeName].blocks = [];
};

Blockly.ComponentTypes.addBlockName = function(typeName, blockName) {
  Blockly.ComponentTypes[typeName].blocks.push(blockName);
};

Blockly.ComponentTypes.getType = function(typeName) {
  return Blockly.ComponentTypes[typeName].type;
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
Blockly.ComponentInstances = {};

Blockly.ComponentInstances.addInstance = function(name, uid) {
  Blockly.ComponentInstances[name] = {};
  Blockly.ComponentInstances[name].uid = uid;
  Blockly.ComponentInstances[name].blocks = [];
};

Blockly.ComponentInstances.haveInstance = function(name, uid) {
  return Blockly.ComponentInstances[name] != undefined
  && Blockly.ComponentInstances[name].uid == uid;
};

Blockly.ComponentInstances.addBlockName = function(name, blockName) {
  Blockly.ComponentInstances[name].blocks.push(blockName);
};

/**
 * Add a component-related block to the language
 * @param instanceName component instance name
 * @param langName the name that identifies this block in Blockly.Language and Blockly.Yail
 * @param langBlock the language block (a Blockly.Block)
 * @param generator the Yail generation function for this block
 */
Blockly.Component.addBlockAndGenerator = function(instanceName, langName, langBlock, generator) {
  Blockly.Language[langName] = langBlock;
  Blockly.Yail[langName] = generator;
  Blockly.ComponentInstances.addBlockName(instanceName, langName);
};

/**
 * Add a generic component-related block to the language
 * @param typeName component type name
 * @param langName the name that identifies this block in Blockly.Language and Blockly.Yail
 * @param langBlock the language block (a Blockly.Block)
 * @param generator the Yail generation function for this block
 */
Blockly.Component.addGenericBlockAndGenerator = function(typeName, langName, langBlock, generator) {
  Blockly.Language[langName] = langBlock;
  Blockly.Yail[langName] = generator;
  Blockly.ComponentTypes.addBlockName(typeName, langName);
};

Blockly.Component.getComponentNamesByType = function(componentType) {
  var componentNameArray = [];
  for(var componentName in Blockly.ComponentInstances) {
    if(Blockly.ComponentInstances[componentName].typeName == componentType) {
      componentNameArray.push([componentName,componentName]);
    }
  }
  return componentNameArray;
};


