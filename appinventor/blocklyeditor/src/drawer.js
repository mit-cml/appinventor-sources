// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Set of drawers for holding factory blocks (blocks that create
 * other blocks when dragged onto the workspace). The set of drawers
 * includes the built-in drawers that we get from the blocks language, as
 * well as a drawer per component instance that was added to this workspace.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author Sharon Perl (sharon@google.com)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blockly.Drawer');

goog.require('AI.Blockly.Util.xml');
goog.require('goog.object');
goog.require('bd.toolbox.ctr');

// Some block drawers need to be initialized after all the javascript source is loaded because they
// use utility functions that may not yet be defined at the time their source is read in. They
// can do this by adding a field to Blockly.DrawerInit whose value is their initialization function.
// For example, see language/common/math.js.

Blockly.Drawer = function(parentWorkspace, opt_options) {
  if (opt_options instanceof Blockly.Options) {
    this.options = opt_options;
  } else {
    opt_options = opt_options || {};
    this.options = new Blockly.Options(opt_options);
  }
  this.workspace_ = parentWorkspace;
  this.flyout_ = this.workspace_.getFlyout();
  this.flyout_.setAutoClose(true);
  this.lastComponent = null;
};

/**
 * String to prefix on categories of each potential block in the drawer.
 * Used to prevent collisions with built-in properties like 'toString'.
 * @private
 */
Blockly.Drawer.PREFIX_ = 'cat_';

/**
 * Build the hierarchical tree of block types.
 * Note: taken from Blockly's toolbox.js
 * @return {!Object} Tree object.
 * @private
 */
Blockly.Drawer.buildTree_ = function() {
  var tree = {};
  var formName = Blockly.common.getMainWorkspace().formName;
  var screenName = formName.substring(formName.indexOf("_") + 1);

  // Check to see if a Blocks Toolkit is defined. If so, use that to build the tree.
  if (window.parent.BlocklyPanel_getComponentInstancePropertyValue) {
    var subsetJsonString = window.parent.BlocklyPanel_getComponentInstancePropertyValue(formName, screenName, "BlocksToolkit");
    if (subsetJsonString) {
      var toolkitTree = Blockly.Drawer.buildToolkitTree_(subsetJsonString);
      if (toolkitTree != undefined)
        return toolkitTree;
    }
  }

  // Populate the tree structure.
  for (var name in Blockly.Blocks) {
    var block = Blockly.Blocks[name];
    // Blocks without a category are fragments used by the mutator dialog.
    if (block.category) {
      var cat = Blockly.Drawer.PREFIX_ + window.encodeURI(block.category);
      if (cat in tree) {
        tree[cat].push(name);
      } else {
        tree[cat] = [name];
      }
    }
  }
  return tree;
};

/**
 * Build the hierarchical tree of built-in block types using the JSON property BlocksToolkit
 * @return {!Object} Tree object.
 * @private
 */
Blockly.Drawer.buildToolkitTree_ = function(jsonToolkit) {
  var tree = {};
  var subsetArray = JSON.parse(jsonToolkit);
  var subsetBlockArray = subsetArray["shownBlockTypes"];
  try {
    for (var key in subsetBlockArray) {
      if (key != 'ComponentBlocks') {
        var cat = "cat_" + key;
        var catBlocks = subsetBlockArray[key];
        for (var i = 0; i < catBlocks.length; i++) {
          var block = catBlocks[i];
          var name = block.type;
          if (cat in tree) {
            tree[cat].push(name);
          } else {
            tree[cat] = [name];
          }
        }
      }
    }
  } catch (err) {
    console.log(err);
    return undefined;
  }
  return tree;
};

/**
 * Show the contents of the built-in drawer named drawerName. drawerName
 * should be one of Blockly.Msg.VARIABLE_CATEGORY,
 * Blockly.Msg.PROCEDURE_CATEGORY, or one of the built-in block categories.
 * @param drawerName
 */
Blockly.Drawer.prototype.showBuiltin = function(drawerName) {
  drawerName = Blockly.Drawer.PREFIX_ + drawerName;
  if (!this.options.languageTree) {
    this.options.languageTree = Blockly.Drawer.buildTree_();
  }
  var blockSet = this.options.languageTree[drawerName];
  if (drawerName == "cat_Procedures") {
    var newBlockSet = [];
    for (var i = 0; i < blockSet.length; i++) {
      if(!(blockSet[i] == "procedures_callnoreturn" // Include callnoreturn only if at least one defnoreturn declaration
           && this.workspace_.getProcedureDatabase().voidProcedures == 0)
         &&
         !(blockSet[i] == "procedures_callreturn" // Include callreturn only if at least one defreturn declaration
           && this.workspace_.getProcedureDatabase().returnProcedures == 0)){
        newBlockSet.push(blockSet[i]);
      }
    }
    blockSet = newBlockSet;
  }

  if (!blockSet) {
    throw "no such drawer: " + drawerName;
  }
  Blockly.hideChaff();
  var xmlList = this.blockListToXMLArray(blockSet);
  this.flyout_.show(xmlList);
};

/**
 * Show the blocks drawer for the component with give instance name. If no
 * such component is found, currently we just log a message to the console
 * and do nothing.
 */
Blockly.Drawer.prototype.showComponent = function(instanceName) {
  var component = this.workspace_.getComponentDatabase().getInstance(instanceName);
  if (component) {
    Blockly.hideChaff();
    this.flyout_.show(this.instanceRecordToXMLArray(component));
    this.lastComponent = instanceName;
  } else {
    console.log("Got call to Blockly.Drawer.showComponent(" +  instanceName +
                ") - unknown component name");
  }
};

/**
 * Show the contents of the generic component drawer named drawerName. (This is under the
 * "Any components" section in App Inventor). drawerName should be the name of a component type for
 * which we have at least one component instance in the blocks workspace. If no such component
 * type is found, currently we just log a message to the console and do nothing.
 * @param {!string} typeName
 */
Blockly.Drawer.prototype.showGeneric = function(typeName) {
  if (this.workspace_.getComponentDatabase().hasType(typeName)) {
    Blockly.hideChaff();
    var xmlList = this.componentTypeToXMLArray(typeName);
    this.flyout_.show(xmlList);
  } else {
    console.log("Got call to Blockly.Drawer.showGeneric(" +  typeName +
                ") - unknown component type name");
  }
};

/**
 * Hide the Drawer flyout
 */
Blockly.Drawer.prototype.hide = function() {
  this.lastComponent = null;
  this.flyout_.hide();
};

/**
 * @returns {boolean} true if the Drawer flyout is currently open, false otherwise.
 */
Blockly.Drawer.prototype.isShowing = function() {
  return this.flyout_.isVisible();
};

Blockly.Drawer.prototype.blockListToXMLArray = function(blockList) {
  var xmlArray = [];
  for (var i = 0; i < blockList.length; i++) {
    Array.prototype.push.apply(
        xmlArray, this.blockTypeToXMLArray(blockList[i], null));
  }
  return xmlArray;
};

/**
 * @param {{name: string, typeName: string}} instanceRecord
 */
Blockly.Drawer.prototype.instanceRecordToXMLArray = function(instanceRecord) {
  var xmlArray = [];
  var typeName = instanceRecord.typeName;
  var componentInfo = this.workspace_.getComponentDatabase().getType(typeName);

  var formName = Blockly.common.getMainWorkspace().formName;
  var screenName = formName.substring(formName.indexOf("_") + 1);
  var subsetJsonString = "";
  if (window.parent.BlocklyPanel_getComponentInstancePropertyValue) {
    subsetJsonString = window.parent.BlocklyPanel_getComponentInstancePropertyValue(
        formName, screenName, "BlocksToolkit");
  }
  // Create the subset of blocks.
  if (subsetJsonString.length > 0) {
    // TODO: All of this code should be cleaned up and moved into a separate
    //   function. Logs should be removed.

    var subsetArray = [];
    var subsetBlocks = [];
    subsetArray = JSON.parse(subsetJsonString);
    var subsetBlockArray = subsetArray["shownBlockTypes"]["ComponentBlocks"][typeName];
    // The component type might not be in the json string if it was removed from the blocks toolkit
    // after an instance was already created in the Designer. It's not entirely clear what behavior
    // one would expect in this situation. I'm going to leave the flyout blank.
    if (subsetBlockArray !== undefined) {
      for (var i = 0; i < subsetBlockArray.length; i++) {
        var obj = subsetBlockArray[i];
        obj['mutatorNameToValue']['instance_name'] = instanceRecord.name;
        obj['fieldNameToValue']['COMPONENT_SELECTOR'] = instanceRecord.name;
        console.log("added obj");
        console.log(obj);
        var xml = bd.toolbox.ctr.blockObjectToXML(bd.toolbox.ctr.blockInfoToBlockObject(obj));
        xmlArray.push(xml);
      }
      //create component literal block
      var obj = {type: "component_component_block"};
      var mutatorAttributes = {component_type: typeName, instance_name: instanceRecord.name};
      obj['mutatorNameToValue'] = mutatorAttributes;
      var xml = bd.toolbox.ctr.blockObjectToXML(bd.toolbox.ctr.blockInfoToBlockObject(obj));
      //console.log(xml);
      xmlArray.push(xml);
    }
  } else {
    xmlArray = this.createAllComponentBlocks(componentInfo, instanceRecord);
  }
  return xmlArray;
};

/**
 * Creates all of the blocks for a given component. -- These are actual blocks
 * not generic ones.
 * @param {!ComponentTypeDescriptor} componentInfo An object describing all of
 *     the information about a component. Eg events, methods, properties, etc.
 * @param {!{name: string, typeName: string}} instanceRecord An object
 *     describing all of the information about a component instance.
 */
Blockly.Drawer.prototype.createAllComponentBlocks =
  function(componentInfo, instanceRecord) {
    var xmlArray = [];
    // Collect helper keys used in events, methods, and properties so that we
    // can add all the helper blocks used by the component near the bottom of
    // its drawer.
    var helperKeys = [];
    var typeName = instanceRecord.typeName;
    var instanceName = instanceRecord.name;
    var xmlUtils = Blockly.Util.xml;
    var parent = Blockly.common.getMainWorkspace().getComponentDatabase().getContainer(
      Blockly.common.getMainWorkspace().formName, instanceName);
    var freePosition = parent && parent.typeName == 'AbsoluteArrangement';

    /**
     * Adds the feature's helper key to the list of HelperKeys if the key is
     * not currently in the list. This list is then used to add the helper keys
     * to the bottom of the drawer.
     * @param {{helperKey: Object}} feature The feature to check for a helperKey.
     */
    function getHelper(feature) {
      if (!feature.helperKey) {
        return;
      }

      function addToHelpers(curKey) {
        switch (curKey.type) {
          case "OPTION_LIST":
            return !helperKeys.some(function(altKey) {
              return altKey.key == curKey.key && altKey.type == curKey.type;
            });
          default:  // Most types probably only want one instance in the drawer.
            return !helperKeys.some(function(altKey) {
              return altKey.type == curKey.type;
            });
        }
      }

      if (addToHelpers(feature.helperKey)) {
        helperKeys.push(feature.helperKey);
      }
    }

    // Create event blocks.
    goog.object.forEach(componentInfo.eventDictionary, function (event, name) {
      if (event.deprecated) {
        return;
      }

      var eventObj = {
        component_type: typeName,
        instance_name: instanceName,
        event_name: name
      };
      var eventXml = this.blockTypeToXMLArray('component_event', eventObj);
      Array.prototype.push.apply(xmlArray, eventXml);

      // Determine if any parameters are associated with a helper which should
      // be added at the bottom of the drawer.
      event.parameters.forEach(getHelper);
    }, this);

    // Create method blocks.
    goog.object.forEach(componentInfo.methodDictionary, function (method, name) {
      if (method.deprecated) {
        return;
      }

      var methodObj = {
        component_type: typeName,
        instance_name: instanceName,
        method_name: name
      };
      var methodXml = this.blockTypeToXML('component_method', methodObj);

      method.parameters.forEach(function(param, index) {
        if (!param.helperKey) {
          return;
        }
        // Determine if any parameters are associated with a helper which should
        // be added at the bottom of the drawer.
        getHelper(param);
        // Adds dropdown blocks to inputs which expect them.
        var inputXml = xmlUtils.valueWithHelperXML('ARG' + index, param.helperKey);
        // First child b/c these are wrapped in an <xml/> node.
        methodXml.firstChild.appendChild(inputXml.firstChild);
      }.bind(this));

      Array.prototype.push.apply(xmlArray, xmlUtils.XMLToArray(methodXml));
    }, this);

    // Create getter and setter blocks.
    goog.object.forEach(componentInfo.properties, function (property, name) {
      if (property.deprecated) {
        return;
      }

      if ((name == 'Left' || name == 'Top') && !freePosition) {
        return;
      }

      var propertyObj = {
        component_type: typeName,
        instance_name: instanceName,
        property_name: name
      }
      var readable = Blockly.PROPERTY_READABLE;
      var writable = Blockly.PROPERTY_WRITEABLE;

      if ((property.mutability & readable) == readable) {
        propertyObj.set_or_get = 'get';
        var getXml = this.blockTypeToXMLArray('component_set_get', propertyObj);
        Array.prototype.push.apply(xmlArray, getXml);
      }
      if ((property.mutability & writable) == writable) {
        propertyObj.set_or_get = 'set';
        var setXml = this.blockTypeToXML('component_set_get', propertyObj);

        if (property.helperKey) {
          // Adds dropdown blocks to inputs which expect them.
          var inputXml = xmlUtils.valueWithHelperXML('VALUE', property.helperKey);
          // First child b/c these are wrapped in an <xml/> node.
          setXml.firstChild.appendChild(inputXml.firstChild);
        }

        Array.prototype.push.apply(xmlArray, xmlUtils.XMLToArray(setXml));
      }

      // Collects up helper blocks for properties which use them so they can
      // be added to the bottom of the drawer.
      getHelper(property);
    }, this);

    // Create helper blocks at the bottom of the drawer, right above the
    // component block.
    // Another option was to create a separate drawer for helper blocks, but it
    // was decided that it was better to keep helpers close to the components/
    // blocks that use them.
    helperKeys.forEach(function(helper) {
      var xml = xmlUtils.helperKeyToXML(helper);
      Array.prototype.push.apply(xmlArray, xmlUtils.XMLToArray(xml));
    }.bind(this));

    // Create component literal block.
    var componentObj = {
      component_type: typeName,
      instance_name: instanceName
    };
    var componentXml = this.blockTypeToXMLArray(
        'component_component_block', componentObj);
    Array.prototype.push.apply(xmlArray, componentXml);


    return xmlArray;
  }

Blockly.Drawer.prototype.componentTypeToXMLArray = function(typeName) {
  var xmlArray = [];
  var componentInfo = this.workspace_.getComponentDatabase().getType(typeName);
  // Collect helper keys used in events, methods, and properties so that we
  // can add all the helper blocks used by the component near the bottom of
  // its drawer.
  var helperKeys = [];
  var xmlUtils = Blockly.Util.xml;

  function getHelper(feature) {
    if (!feature.helperKey) {
      return;
    }

    function addToHelpers(curKey) {
      switch (curKey.type) {
        case "OPTION_LIST":
          return !helperKeys.some(function(altKey) {
            return altKey.key == curKey.key && altKey.type == curKey.type;
          });
        default:  // Most types probably only want one instance in the drawer.
          return !helperKeys.some(function(altKey) {
            return altKey.type == curKey.type;
          });
      }
    }

    if (addToHelpers(feature.helperKey)) {
      helperKeys.push(feature.helperKey);
    }
  }

  //create generic event blocks
  goog.object.forEach(componentInfo.eventDictionary, function(event, name){
    if(!event.deprecated){
      Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_event', {
        component_type: typeName, event_name: name, is_generic: 'true'
      }));

      // Determine if any parameters are associated with a helper which should
      // be added at the bottom of the drawer.
      event.parameters.forEach(getHelper);
    }
  }, this);

  //create generic method blocks
  goog.object.forEach(componentInfo.methodDictionary, function(method, name) {
    if (!method.deprecated) {
      var methodXml = this.blockTypeToXML('component_method', {
        component_type: typeName, method_name: name, is_generic: "true"
      });


      method.parameters.forEach(function(param, index) {
        if (!param.helperKey) {
          return;
        }
        // Determine if any parameters are associated with a helper which should
        // be added at the bottom of the drawer.
        getHelper(param);
        // Adds dropdown blocks to inputs which expect them.
        var inputXml = xmlUtils.valueWithHelperXML('ARG' + index, param.helperKey);
        // First child b/c these are wrapped in an <xml/> node.
        methodXml.firstChild.appendChild(inputXml.firstChild);
      }.bind(this));

      Array.prototype.push.apply(xmlArray, xmlUtils.XMLToArray(methodXml));
    }
  }, this);

  //for each property
  goog.object.forEach(componentInfo.properties, function(property, name) {
    if (!property.deprecated) {
      var params = {component_type: typeName, property_name: name};
      if ((property.mutability & Blockly.PROPERTY_READABLE) == Blockly.PROPERTY_READABLE) {
        params.set_or_get = 'get';
        Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_set_get', params));
      }
      if ((property.mutability & Blockly.PROPERTY_WRITEABLE) == Blockly.PROPERTY_WRITEABLE) {
        params.set_or_get = 'set';
        var setXml = this.blockTypeToXML('component_set_get', params);

        if (property.helperKey) {
          // Adds dropdown blocks to inputs which expect them.
          var inputXml = xmlUtils.valueWithHelperXML('VALUE', property.helperKey);
          // First child b/c these are wrapped in an <xml/> node.
          setXml.firstChild.appendChild(inputXml.firstChild);
        }

        Array.prototype.push.apply(xmlArray, xmlUtils.XMLToArray(setXml));
      }

      // Collects up helper blocks for properties which use them so they can
      // be added to the bottom of the drawer.
      getHelper(property);
    }
  }, this);

  // Create helper blocks at the bottom of the drawer.
  helperKeys.forEach(function(helper) {
    var xml = xmlUtils.helperKeyToXML(helper);
    Array.prototype.push.apply(xmlArray, xmlUtils.XMLToArray(xml));
  }.bind(this));

  // add the all components getter
  Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_all_component_block', {
    component_type: typeName
  }));

  return xmlArray;
};

Blockly.Drawer.prototype.blockTypeToXMLArray =
  function(blockType, mutatorAttributes) {
    return Blockly.Util.xml.XMLToArray(
        this.blockTypeToXML(blockType, mutatorAttributes));
  };

Blockly.Drawer.prototype.blockTypeToXML = function(blockType, mutatorAttributes) {
    var utils = Blockly.Util.xml;

    if (mutatorAttributes && mutatorAttributes['is_generic'] === undefined) {
      mutatorAttributes['is_generic'] = !mutatorAttributes['instance_name']
    }

    switch (blockType) {
      case 'procedures_callnoreturn':
        return Blockly.utils.xml.textToDom(
            utils.procedureCallersXMLString(false, this.workspace_));
      case 'procedures_callreturn':
        return Blockly.utils.xml.textToDom(
            utils.procedureCallersXMLString(true, this.workspace_));
      default:
        var xmlString = Blockly.Drawer.getDefaultXMLString(
            blockType, mutatorAttributes);
        if (xmlString != null) {
          return Blockly.utils.xml.textToDom(xmlString);
        } else {
          return utils.blockTypeToXML(blockType, mutatorAttributes);
        }
    }
}

/**
 * Given the blockType and a dictionary of the mutator attributes
 * either return the xml string associated with the default block
 * or return null, since there are no default blocks associated with the blockType.
 */
Blockly.Drawer.getDefaultXMLString = function(blockType, mutatorAttributes) {
  //return null if the
  if(Blockly.Drawer.defaultBlockXMLStrings[blockType] == null) {
    return null;
  }

  if(Blockly.Drawer.defaultBlockXMLStrings[blockType].xmlString != null) {
    //return xml string associated with block type
    return Blockly.Drawer.defaultBlockXMLStrings[blockType].xmlString;
  } else if(Blockly.Drawer.defaultBlockXMLStrings[blockType].length != null){
    var possibleMutatorDefaults = Blockly.Drawer.defaultBlockXMLStrings[blockType];
    var matchingAttributes;
    var allMatch;
    //go through each of the possible matching cases
    for (var i=0;i<possibleMutatorDefaults.length;i++) {
      matchingAttributes = possibleMutatorDefaults[i].matchingMutatorAttributes;
      //if the object doesn't have a matchingAttributes object, then skip it
      if(!matchingAttributes) {
        continue;
      }
      //go through each of the mutator attributes.
      //if one attribute does not match then move to the next possibility
      allMatch = true;
      for (var mutatorAttribute in matchingAttributes) {
        if (!matchingAttributes.hasOwnProperty(mutatorAttribute)) continue;
        if(mutatorAttributes[mutatorAttribute] != matchingAttributes[mutatorAttribute]){
          allMatch = false;
          break;
        }
      }
      //if all of the attributes matched, return the xml string given the appropriate mutator
      if(allMatch) {
        return possibleMutatorDefaults[i].mutatorXMLStringFunction(mutatorAttributes);
      }
    }
    //if the mutator attributes did not match for all of the possibilities, return null
    return null;
  }

};

Blockly.Drawer.defaultBlockXMLStrings = {
  controls_if: {xmlString:
  '<xml>' +
    '<block type="controls_if">' +
    '</block>' +
    '<block type="controls_if">' +
      '<mutation else="1"></mutation>' +
    '</block>' +
    '<block type="controls_if">' +
      '<mutation elseif="1" else="1"></mutation>' +
    '</block>' +
  '</xml>' },

  controls_forRange: {xmlString:
  '<xml>' +
    '<block type="controls_forRange">' +
      '<value name="START"><block type="math_number"><title name="NUM">1</title></block></value>' +
      '<value name="END"><block type="math_number"><title name="NUM">5</title></block></value>' +
      '<value name="STEP"><block type="math_number"><title name="NUM">1</title></block></value>' +
    '</block>' +
  '</xml>' },

  controls_openAnotherScreen: {xmlString:
  '<xml>' +
    '<block type="controls_openAnotherScreen">' +
      '<value name="SCREEN">' +
        '<block type="helpers_screen_names"></block>' +
      '</value>' +
    '</block>' +
  '</xml>' },

  controls_openAnotherScreenWithStartValue: {xmlString:
  '<xml>' +
    '<block type="controls_openAnotherScreenWithStartValue">' +
      '<value name="SCREENNAME">' +
        '<block type="helpers_screen_names"></block>' +
      '</value>' +
    '</block>' +
  '</xml>' },

   math_random_int: {xmlString:
  '<xml>' +
    '<block type="math_random_int">' +
    '<value name="FROM"><block type="math_number"><title name="NUM">1</title></block></value>' +
    '<value name="TO"><block type="math_number"><title name="NUM">100</title></block></value>' +
    '</block>' +
  '</xml>'},
  color_make_color: {xmlString:
  '<xml>' +
    '<block type="color_make_color">' +
      '<value name="COLORLIST">' +
        '<block type="lists_create_with" inline="false">' +
          '<mutation items="3"></mutation>' +
          '<value name="ADD0"><block type="math_number"><title name="NUM">255</title></block></value>' +
          '<value name="ADD1"><block type="math_number"><title name="NUM">0</title></block></value>' +
          '<value name="ADD2"><block type="math_number"><title name="NUM">0</title></block></value>' +
        '</block>' +
      '</value>' +
    '</block>' +
  '</xml>'},
  lists_create_with: {xmlString:
  '<xml>' +
    '<block type="lists_create_with">' +
      '<mutation items="0"></mutation>' +
    '</block>' +
    '<block type="lists_create_with">' +
      '<mutation items="2"></mutation>' +
    '</block>' +
  '</xml>'},
  lists_lookup_in_pairs: {xmlString:
  '<xml>' +
    '<block type="lists_lookup_in_pairs">' +
    '<value name="NOTFOUND"><block type="text"><title name="TEXT">not found</title></block></value>' +
    '</block>' +
  '</xml>'},
  lists_join_with_separator: {xmlString:
    '<xml>' +
      '<block type="lists_join_with_separator">' +
      '<value name="SEPARATOR"><block type="text"><title name="TEXT"></title></block></value>' +
      '</block>' +
    '</xml>'},
  dictionaries_create_with: {xmlString:
  '<xml>' +
    '<block type="dictionaries_create_with">' +
      '<mutation items="0"></mutation>' +
    '</block>' +
    '<block type="dictionaries_create_with">' +
      '<mutation items="2"></mutation>' +
      '<value name="ADD0"><block type="pair"></block></value>' +
      '<value name="ADD1"><block type="pair"></block></value>' +
    '</block>' +
  '</xml>'},
  dictionaries_lookup: {xmlString:
  '<xml>' +
    '<block type="dictionaries_lookup">' +
    '<value name="NOTFOUND"><block type="text"><title name="TEXT">not found</title></block></value>' +
    '</block>' +
  '</xml>'},
  dictionaries_recursive_lookup: {xmlString:
  '<xml>' +
    '<block type="dictionaries_recursive_lookup">' +
    '<value name="NOTFOUND"><block type="text"><title name="TEXT">not found</title></block></value>' +
    '</block>' +
  '</xml>'},

  component_method: [
    {matchingMutatorAttributes:{component_type:"TinyDB", method_name:"GetValue"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG1"><block type="text"><title name="TEXT"></title></block></value>' +
         '</block>' +
         '</xml>';}},

    {matchingMutatorAttributes:{component_type:"FirebaseDB", method_name:"GetValue"},
      mutatorXMLStringFunction: function(mutatorAttributes) {
        return '' +
            '<xml>' +
            '<block type="component_method">' +
              //mutator generator
            Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
            '<value name="ARG1"><block type="text"><title name="TEXT"></title></block></value>' +
            '</block>' +
            '</xml>';}},

    // Notifer.ShowTextDialog has cancelable default to TRUE
    {matchingMutatorAttributes:{component_type:"Notifier", method_name:"ShowTextDialog"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG2"><block type="logic_boolean"><title name="BOOL">TRUE</title></block></value>' +
         '</block>' +
         '</xml>';}},

    // Notifer.ShowChooseDialog has cancelable default to TRUE
    {matchingMutatorAttributes:{component_type:"Notifier", method_name:"ShowChooseDialog"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG4"><block type="logic_boolean"><title name="BOOL">TRUE</title></block></value>' +
         '</block>' +
         '</xml>';}},

    // Canvas.DrawCircle has fill default to TRUE
    {matchingMutatorAttributes:{component_type:"Canvas", method_name:"DrawCircle"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG3"><block type="logic_boolean"><title name="BOOL">TRUE</title></block></value>' +
         '</block>' +
         '</xml>';}},

    // Canvas.DrawShape has fill default to TRUE
    {matchingMutatorAttributes:{component_type:"Canvas", method_name:"DrawShape"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG1"><block type="logic_boolean"><field name="BOOL">TRUE</field></block></value>' +
         '</block>' +
         '</xml>';}},

    // Canvas.DrawArc has useCenter default to FALSE and fill default to TRUE
    {matchingMutatorAttributes:{component_type:"Canvas", method_name:"DrawArc"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG6"><block type="logic_boolean"><field name="BOOL">FALSE</field></block></value>' +
         '<value name="ARG7"><block type="logic_boolean"><field name="BOOL">TRUE</field></block></value>' +
         '</block>' +
         '</xml>';}},

    // Clock.FormatDate has pattern default to MMM d, yyyy
    {matchingMutatorAttributes:{component_type:"Clock", method_name:"FormatDate"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG1"><block type="text"><field name="TEXT">MMM d, yyyy</field></block></value>' +
         '</block>' +
         '</xml>';}},

    // Clock.FormatDateTime has pattern default to MM/dd/yyyy hh:mm:ss a
    {matchingMutatorAttributes:{component_type:"Clock", method_name:"FormatDateTime"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
         '<value name="ARG1"><block type="text"><field name="TEXT">MM/dd/yyyy hh:mm:ss a</field></block></value>' +
         '</block>' +
         '</xml>';}},

    // Spreadsheet.Read methods default to reading from "Sheet1"
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadRow"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},

    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadCol"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadCell"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadRange"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadSheet"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    // {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadWithQuery"},
    // mutatorXMLStringFunction: function(mutatorAttributes) {
    //   return (
    //     '<xml>' +
    //     '<block type="component_method">' +
    //     // mutator generator
    //     Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
    //     '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
    //     '</block>' +
    //     '</xml>'
    //   );
    // }},

    // Spreadsheet.Read methods default to reading from "Sheet1"
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadWithExactFilter"},
      mutatorXMLStringFunction: function(mutatorAttributes) {
        return (
          '<xml>' +
          '<block type="component_method">' +
          // mutator generator
          Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
          '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
          '<value name="ARG1"><block type="math_number"><title name="NUM">0</title></block></value>' +
          '</block>' +
          '</xml>'
        );
      }},

    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ReadWithPartialFilter"},
      mutatorXMLStringFunction: function(mutatorAttributes) {
        return (
          '<xml>' +
          '<block type="component_method">' +
          // mutator generator
          Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
          '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
          '<value name="ARG1"><block type="math_number"><title name="NUM">0</title></block></value>' +
          '</block>' +
          '</xml>'
        );
      }},

    // Spreadsheet.Write methods default to writing to "Sheet1"
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"WriteRow"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"WriteCol"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"WriteCell"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"WriteRange"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},

    // Spreadsheet.ClearRange method default to Clearing from "Sheet1"
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"ClearRange"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},

    // Spreadsheet.Add methods default to adding to "Sheet1"
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"AddRow"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"AddCol"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},

    // Spreadsheet.Delete methods default to removing from Grid ID = 0
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"RemoveRow"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }},
    {matchingMutatorAttributes:{component_type:"AnomalyDetection", method_name:"DetectAnomalies"},
      mutatorXMLStringFunction: function(mutatorAttributes) {
        return (
            '<xml>' +
            '<block type="component_method">' +
            // mutator generator
            Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
            '<value name="ARG1"><block type="math_number"><title name="NUM">2</title></block></value>' +
            '</block>' +
            '</xml>'
        );
      }},
    {matchingMutatorAttributes:{component_type:"Regression", method_name:"CalculateLineOfBestFitValue"},
      mutatorXMLStringFunction: function(mutatorAttributes) {
        return (
            '<xml>' +
            '<block type="component_method">' +
            // mutator generator
            Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
            '<value name="ARG2"><block type="helpers_dropdown"><mutation key="LOBFValues"></mutation><field name="OPTION">Slope</field></block></value>' +
            '</block>' +
            '</xml>'
        );
      }},
    {matchingMutatorAttributes:{component_type:"Spreadsheet", method_name:"RemoveCol"},
    mutatorXMLStringFunction: function(mutatorAttributes) {
      return (
        '<xml>' +
        '<block type="component_method">' +
        // mutator generator
        Blockly.Util.xml.mutatorAttributesXmlString(mutatorAttributes) +
        '<value name="ARG0"><block type="text"><field name="TEXT">Sheet1</field></block></value>' +
        '</block>' +
        '</xml>'
      );
    }}
  ]
};
