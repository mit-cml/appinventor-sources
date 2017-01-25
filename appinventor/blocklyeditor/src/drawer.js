// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
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
 */

'use strict';

goog.provide('Blockly.Drawer');

goog.require('Blockly.Flyout');

// Some block drawers need to be initialized after all the javascript source is loaded because they
// use utility functions that may not yet be defined at the time their source is read in. They
// can do this by adding a field to Blockly.DrawerInit whose value is their initialization function.
// For example, see language/common/math.js.

/**
 * Create the dom for the drawer. Creates a flyout Blockly.Drawer.flyout_,
 * and initializes its dom.
 */
Blockly.Drawer.createDom = function() {
  Blockly.Drawer.flyout_ = new Blockly.Flyout();
  // insert the flyout after the main workspace (except, there's no
  // svg.insertAfter method, so we need to insert before the thing following
  // the main workspace. Neil Fraser says: this is "less hacky than it looks".
  var flyoutGroup = Blockly.Drawer.flyout_.createDom();
  Blockly.svg.insertBefore(flyoutGroup, Blockly.mainWorkspace.svgGroup_.nextSibling);
};

/**
 * Initializes the drawer by initializing the flyout and creating the
 * language tree. Call after calling createDom.
 */

 /* New edits 1/24/2017 - Janice
 * Instead of creating the language tree, this will now create
 * the category block dictionary - see createBlockInfoArray below.
 */
Blockly.Drawer.init = function() {
  Blockly.Drawer.flyout_.init(Blockly.mainWorkspace, true);
  for (var name in Blockly.DrawerInit) {
    Blockly.DrawerInit[name]();
  }
  Blockly.Drawer.blockInfoArray = Blockly.Drawer.createBlockInfoArray();
  // Blockly.Drawer.languageTree = Blockly.Drawer.buildTree_();
};

/**
 * String to prefix on categories of each potential block in the drawer.
 * Used to prevent collisions with built-in properties like 'toString'.
 * @private
 */
Blockly.Drawer.PREFIX_ = 'cat_';

/**
 * [Janice, 1/23/2017] 
 * Creates a dictionary mapping each category to an array of blocks 
 * in that category. The blocks are in JSON format, and this array
 * structure makes it easier to customize/manipulate block objects.
 * Note: procedures_callreturn and procedures_callnoreturn are under
 * 'list' property, not 'type'.
 */
Blockly.Drawer.createBlockInfoArray = function() {
  var blockArray = {
    "cat_Logic": [
      {type:"logic_boolean", fieldNameToValue:{"BOOL":"TRUE"}},
      {type:"logic_boolean", fieldNameToValue:{"BOOL":"FALSE"}},
      {type:"logic_negate"},
      {type:"logic_compare",fieldNameToValue:{"OP":"EQ"}},
      {type:"logic_operation", fieldNameToValue:{"OP":"AND"}},
      {type:"logic_operation",fieldNameToValue:{"OP":"OR"}}
    ],
    "cat_Control": [
      {type:"controls_if"},
      {type:"controls_forRange", input:{
        "START":{inputType:"value",blockInfo:{type:"math_number",fieldNameToValue:{"NUM":"1"}}},
        "END":{inputType:"value",blockInfo:{type:"math_number",fieldNameToValue:{"NUM":"5"}}},
        "STEP":{inputType:"value",blockInfo:{type:"math_number",fieldNameToValue:{"NUM":"1"}}}
      }},
      {type:"controls_forEach"},
      {type:"controls_while"},
      {type:"controls_choose"},
      {type:"controls_do_then_return"},
      {type:"controls_eval_but_ignore"},
      {type:"controls_openAnotherScreen"},
      {type:"controls_openAnotherScreenWithStartValue"},
      {type:"controls_getStartValue"},
      {type:"controls_closeScreen"},
      {type:"controls_closeScreenWithValue"},
      {type:"controls_closeApplication"},
      {type:"controls_getPlainStartText"},
      {type:"controls_closeScreenWithPlainText"}
    ],
    "cat_Math": [
      {type:"math_number"},
      {type:"math_compare"},
      {type:"math_add"},
      {type:"math_subtract"},
      {type:"math_multiply"},
      {type:"math_division"},
      {type:"math_power"},
      {type:"math_random_int", input:{
        "FROM":{inputType:"value", blockInfo:{type:"math_number",fieldNameToValue:{"NUM":"1"}}},
        "TO":{inputType:"value", blockInfo:{type:"math_number", fieldNameToValue:{"NUM":"10"}}}
      }},
      {type:"math_random_float"},
      {type:"math_random_set_seed"},
      {type:"math_on_list"},
      {type:"math_single"},
      {type:"math_abs"},
      {type:"math_neg"},
      {type:"math_round"},
      {type:"math_ceiling"},
      {type:"math_floor"},
      {type:"math_divide"},
      {type:"math_trig"},
      {type:"math_cos"},
      {type:"math_tan"},
      {type:"math_atan2"},
      {type:"math_convert_angles"},
      {type:"math_format_as_decimal"},
      {type:"math_is_a_number"},
      {type:"math_convert_number"}
    ],
    "cat_Text": [
      {type:"text"},
      {type:"text_join"},
      {type:"text_length"},
      {type:"text_isEmpty"},
      {type:"text_compare"},
      {type:"text_trim"},
      {type:"text_changeCase"},
      {type:"text_starts_at"},
      {type:"text_contains"},
      {type:"text_split"},
      {type:"text_split_at_spaces"},
      {type:"text_segment"},
      {type:"text_replace_all"},
      {type:"obfuscated_text"}
    ],
    "cat_Lists": [
      {type:"lists_create_with", mutatorNameToValue:{"items":"0"}},
      {type:"lists_create_with"},
      {type:"lists_add_items"},
      {type:"lists_is_in"},
      {type:"lists_length"},
      {type:"lists_is_empty"},
      {type:"lists_pick_random_item"},
      {type:"lists_position_in"},
      {type:"lists_select_item"},
      {type:"lists_insert_item"},
      {type:"lists_replace_item"},
      {type:"lists_remove_item"},
      {type:"lists_append_list"},
      {type:"lists_copy"},
      {type:"lists_is_list"},
      {type:"lists_to_csv_row"},
      {type:"lists_to_csv_table"},
      {type:"lists_from_csv_row"},
      {type:"lists_from_csv_table"},
      {type:"lists_lookup_in_pairs", input:{
        "NOTFOUND":{inputType:"value", blockInfo:{type:"text", fieldNameToValue:{"TEXT":"not found"}}}
      }}
    ],
    "cat_Colors": [
      {type:"color_black"},
      {type:"color_white"},
      {type:"color_red"},
      {type:"color_pink"},
      {type:"color_orange"},
      {type:"color_yellow"},
      {type:"color_green"},
      {type:"color_cyan"},
      {type:"color_blue"},
      {type:"color_magenta"},
      {type:"color_light_gray"},
      {type:"color_gray"},
      {type:"color_dark_gray"},
      {type:"color_make_color", input:{
        "COLORLIST":{inputType:"value", blockInfo:{
          mutatorNameToValue:{"items":"3"},
          type:"lists_create_with", input:{
            "ADD0":{inputType:"value", blockInfo:{type:"math_number", fieldNameToValue:{"NUM":"255"}}},
            "ADD1":{inputType:"value", blockInfo:{type:"math_number", fieldNameToValue:{"NUM":"0"}}},
            "ADD2":{inputType:"value", blockInfo:{type:"math_number", fieldNameToValue:{"NUM":"0"}}}
          }
        }}
      }},
      {type:"color_split_color"}
    ],
    "cat_Variables": [
      {type:"global_declaration"},
      {type:"lexical_variable_get"},
      {type:"lexical_variable_set"},
      {type:"local_declaration_statement"},
      {type:"local_declaration_expression"}
    ],
    "cat_Procedures": [
      {type:"procedures_defnoreturn"},
      {type:"procedures_defreturn"},
      {list:"procedures_callnoreturn"},
      {list:"procedures_callreturn"}
    ]
  };
  return blockArray;
};

/**
 * Show the contents of the built-in drawer named drawerName. drawerName
 * should be one of Blockly.MSG_VARIABLE_CATEGORY,
 * Blockly.MSG_PROCEDURE_CATEGORY, or one of the built-in block categories.
 * @param drawerName
 */

 /**
 * New edits 1/20/2017 - Janice
 * Change to use of blockInfoArray - converts drawerName's 
 * array of JSON objects into xmlList using bd.toolbox.ctr
 * "If" statement handles blocks with property "list" (procedure callers)
 */
Blockly.Drawer.showBuiltin = function(drawerName) {
  drawerName = Blockly.Drawer.PREFIX_ + drawerName;
  var blockInfoArray = Blockly.Drawer.blockInfoArray;
  var drawerArray = blockInfoArray[drawerName];

  if (!drawerArray) {
    throw "no such drawer: " + drawerName;
  }

  var xmlList = [];
  for(var i = 0; i < drawerArray.length; i++) {
    if (drawerArray[i].list == "procedures_callnoreturn" || drawerArray[i].list == "procedures_callreturn") {
      var returnBool = (drawerArray[i].list == "procedures_callreturn");
      var callerArray = Blockly.Drawer.procedureCallersBlockArray(returnBool);
      for (var k = 0; k < callerArray.length; k++) {
        xmlList.push(Blockly.Drawer.blockInfoToXML(callerArray[k]));
      }
    } 
    else {
      xmlList.push(Blockly.Drawer.blockInfoToXML(drawerArray[i]));
    }
  }
  Blockly.Drawer.flyout_.show(xmlList);
};

/**
 * Show the blocks drawer for the component with give instance name. If no
 * such component is found, currently we just log a message to the console
 * and do nothing.
 */

 /**
 * 1/23/2017 - Janice
 * Edited instanceNameToXMLArray, but no edits made here.
 */
Blockly.Drawer.showComponent = function(instanceName) {
  if (Blockly.ComponentInstances[instanceName]) {
    Blockly.Drawer.flyout_.hide();
    var xmlList = Blockly.Drawer.instanceNameToXMLArray(instanceName);
    Blockly.Drawer.flyout_.show(xmlList);
  }
  else {
    console.log("Got call to Blockly.Drawer.showComponent(" +  instanceName +
                ") - unknown component name");
  }
};

/**
 * Show the contents of the generic component drawer named drawerName. (This is under the
 * "Any components" section in App Inventor). drawerName should be the name of a component type for
 * which we have at least one component instance in the blocks workspace. If no such component
 * type is found, currently we just log a message to the console and do nothing.
 * @param drawerName
 */

 /**
 * 1/24/2017 - Janice
 * Edited componentTypeToXMLArray, but no edits made here.
 */
Blockly.Drawer.showGeneric = function(typeName) {
  if (Blockly.ComponentTypes[typeName]) {
    Blockly.Drawer.flyout_.hide();
    var xmlList = Blockly.Drawer.componentTypeToXMLArray(typeName);
    Blockly.Drawer.flyout_.show(xmlList);
  } else {
    console.log("Got call to Blockly.Drawer.showGeneric(" +  typeName +
                ") - unknown component type name");
  }
};

/** 
 * [Janice, 1/24/2017]
 * Adapted from procedureCallersXMLString
 * Creates array of procedure call blocks - one call block for each procedure 
 * declaration in main workspace.
 *
 * @param returnsValue (bool): true if procedure returns, false if no return
 * @return blockArray (array): JSON object array of procedure call blocks
 */
Blockly.Drawer.procedureCallersBlockArray = function(returnsValue, proc_name) {
  var decls = Blockly.AIProcedure.getProcedureDeclarationBlocks(returnsValue);
  var blockArray = [];
  // Used for typeblock
  if (proc_name) {
    for (var j = 0; j < decls.length; j++) {
      if (decls[j].getFieldValue('NAME').toLocaleLowerCase() == proc_name) {
        blockArray.push(Blockly.Drawer.proceduresBlockInfo(decls[j]))
        break;
      }
    }
  }
  // Used for showBuiltin
  else {
    // sort decls lexicographically by procedure name
    decls.sort(Blockly.Drawer.compareDeclarationsByName);
    for (var j = 0; j < decls.length; j++) {
      blockArray.push(Blockly.Drawer.procedureBlockInfo(decls[j]));
    }
  }
  return blockArray;
};

/** 
 * [Janice, 1/24/2017] 
 * Creates JSON object for the procedure caller block associated with 
 * the given procedure declaration block
 *
 * @param procBlock (object): procedure declaration block
 * @return blockInfo (object): JSON representation of procedure's caller block
 */
Blockly.Drawer.procedureBlockInfo = function(procBlock) {
  var declType = procBlock.type;
  var callerType = (declType == 'procedures_defreturn') ? 'procedures_callreturn' : 'procedures_callnoreturn';
  var blockInfo = {
    type: callerType,
    mutatorNameToValue: {"name":procBlock.getFieldValue('NAME')},
    fieldNameToValue: {"PROCNAME":procBlock.getFieldValue('NAME')}
  }
  return blockInfo;
}

Blockly.Drawer.compareDeclarationsByName = function (decl1, decl2) {
  var name1 = decl1.getFieldValue('NAME').toLocaleLowerCase();
  var name2 = decl2.getFieldValue('NAME').toLocaleLowerCase();
  return name1.localeCompare(name2);
}

/** 
 * [Janice, 1/24/2017]
 * Creates all events, methods, properties, and literal blocks
 * for a specified instance Name 
 * Used for Blockly.Drawer.showComponents
 *
 * @param instanceName (String)
 * @return xmlArray: array of XML strings for all of the instance's blocks.
 */
Blockly.Drawer.instanceNameToXMLArray = function(instanceName) {
  var xmlArray = [];
  var typeName = Blockly.Component.instanceNameToTypeName(instanceName);

  var blockObjects = Blockly.ComponentTypes[typeName].componentInfo;

  // Create component event blocks
  var blockEvents = blockObjects.events;
  for (var i = 0; i < blockEvents.length; i++) {
    if (blockEvents[i].deprecated === "true") continue;
    var blockInfo = {
      type:"component_event",
      // typeName:typeName, 
      fieldNameToValue: {"COMPONENT_SELECTOR":instanceName}, 
      mutatorNameToValue: {
        "instance_name":instanceName, 
        "component_type":typeName, 
        "event_name":blockEvents[i].name
      }
    };
    xmlArray.push(Blockly.Drawer.blockInfoToXML(blockInfo));
  }

  // Create component method blocks
  var blockMethods = blockObjects.methods;
  for (var i = 0; i < blockMethods.length; i++) {
    if (blockMethods[i].deprecated === "true") continue;
    var inputObject = Blockly.Drawer.blockMethodSpecial(typeName, blockMethods[i].name);
    if (inputObject != null) {
      var blockInfo = {
        type:"component_method",
        fieldNameToValue: {"COMPONENT_SELECTOR":instanceName}, 
        mutatorNameToValue: {
          "instance_name":instanceName, 
          "component_type":typeName, 
          "method_name":blockMethods[i].name,
          "is_generic": "false"
        },
      input: inputObject
      };
    } 
    else {
      var blockInfo = {
        type:"component_method",
        fieldNameToValue: {"COMPONENT_SELECTOR":instanceName}, 
        mutatorNameToValue: {
          "instance_name":instanceName, 
          "component_type":typeName, 
          "method_name":blockMethods[i].name,
          "is_generic": "false"
        }
      };
    }
    xmlArray.push(Blockly.Drawer.blockInfoToXML(blockInfo));
  }

  // Create component property get and set blocks
  var blockProps = blockObjects.blockProperties;
  for (var i = 0; i < blockProps.length; i++) {
    if (blockProps[i].deprecated === "true") continue;
    for (var j = 0; j < 2; j++) {
      var makeBlock = false;
      if (j == 0 && (blockProps[i].rw == "read-write" || blockProps[i].rw == "read-only")) { 
        var getSet = "get";
        makeBlock = true;
      }
      if (j == 1 && (blockProps[i].rw == "read-write" || blockProps[i].rw == "write-only")) { 
        var getSet = "set";
        makeBlock = true;
      }

      if (makeBlock) {
        var blockInfo = {
          type:"component_set_get",
          fieldNameToValue: {"COMPONENT_SELECTOR":instanceName}, 
          fieldNameToValue: {"PROP":blockProps[i].name},
          mutatorNameToValue: {
            "instance_name":instanceName, 
            "component_type":typeName, 
            "property_name": blockProps[i].name,
            "set_or_get": getSet,
            "is_generic": "false",
          }
        };

        xmlArray.push(Blockly.Drawer.blockInfoToXML(blockInfo)); 
      }  
    }
  }

  // Create component literal block (component_component_block)
  var blockInfo = {
    type:"component_component_block",
    fieldNameToValue:{"COMPONENT_SELECTOR":instanceName},
    mutatorNameToValue: {"instance_name":instanceName}
  };
  xmlArray.push(Blockly.Drawer.blockInfoToXML(blockInfo));

  return xmlArray;
};

/** 
 * [Janice, 1/24/2017]
 * Creates all generic methods and property blocks for a component type
 * Used for Blockly.Drawer.showGeneric
 *
 * @param typeName (string): name of component type
 * @return xmlArray: array of XML strings for all of the component's blocks.
 */
Blockly.Drawer.componentTypeToXMLArray = function(typeName) {
  var xmlArray = [];
  var blockObjects = Blockly.ComponentTypes[typeName].componentInfo;

  // Create generic method blocks
  var blockMethods = blockObjects.methods;
  for (var i = 0; i < blockMethods.length; i++) {
    if (blockMethods[i].deprecated === "true") continue;
    var inputObject = Blockly.Drawer.blockMethodSpecial(typeName, blockMethods[i].name);
    // Create special blocks (those with default values)
    if (inputObject != null) {
      var blockInfo = {
        type:"component_method",
        mutatorNameToValue: {
          "component_type":typeName, 
          "method_name":blockMethods[i].name,
          "is_generic": "true"
        },
      input: inputObject
      };
    } 
    else {
      // Create regular blocks
      var blockInfo = {
        type:"component_method",
        mutatorNameToValue: {
          "component_type":typeName, 
          "method_name":blockMethods[i].name,
          "is_generic": "true"
        }
      };
    }
    xmlArray.push(Blockly.Drawer.blockInfoToXML(blockInfo));
  }

  // Create generic get and set blocks for each property
  var blockProps = blockObjects.blockProperties;
  for (var i = 0; i < blockProps.length; i++) {
    if (blockProps[i].deprecated === "true") continue;
    for (var j = 0; j < 2; j++) {
      var makeBlock = false;
      if (j == 0 && (blockProps[i].rw == "read-write" || blockProps[i].rw == "read-only")) { 
        var getSet = "get";
        makeBlock = true;
      }
      if (j == 1 && (blockProps[i].rw == "read-write" || blockProps[i].rw == "write-only")) { 
        var getSet = "set";
        makeBlock = true;
      }

      if (makeBlock) {
        var blockInfo = {
          type:"component_set_get",
          fieldNameToValue: {"PROP":blockProps[i].name},
          mutatorNameToValue: {
            "component_type":typeName, 
            "property_name": blockProps[i].name,
            "set_or_get": getSet,
            "is_generic": "true",
          }
        };

        xmlArray.push(Blockly.Drawer.blockInfoToXML(blockInfo));      
      }  
    }
  }

  return xmlArray;
};

/**
 * [Janice, 1/24/2017] 
 * Deals with certain component method blocks that have default inputs
 * Creates object that will go under the 'input' property for those blocks
 * Used for showComponent and showGeneric
 * @return: object that will be input property of blockInfo
 *          null if component does not have method with special defaults
 */
Blockly.Drawer.blockMethodSpecial = function(componentType, methodName) {
  // COMPONENTS TinyDB or FirebaseDB with METHOD GetValue
  if (methodName == "GetValue" && (componentType == "TinyDB" || componentType == "FirebaseDB")) {
    var inputObj = {"ARG1":{
      inputType:"value", 
      blockInfo:{type:"text", fieldNameToValue:{"TEXT":""}}
    }};
  }
  // COMPONENT Notifier with METHODS ShowTextDialog or ShowChooseDialog
  else if (componentType == "Notifier") {
    if (methodName == "ShowTextDialog") {
      var inputObj = {"ARG2":{
        inputType:"value",
        blockInfo:{type:"logic_boolean", fieldNameToValue:{"BOOL":"TRUE"}}
      }};
    }
    else if (methodName == "ShowChooseDialog") {
      var inputObj = {"ARG4":{
        inputType:"value",
        blockInfo:{type:"logic_boolean", fieldNameToValue:{"BOOL":"TRUE"}}
      }};
    }
  }
  // COMPONENT Canvas with METHOD DrawCircle
  else if (componentType == "Canvas" && methodName == "DrawCircle") {
    var inputObj = {"ARG3":{
      inputType:"value",
      blockInfo:{type:"logic_boolean", fieldNameToValue:{"BOOL":"TRUE"}}
    }};
  }
  // COMPONENT Clock with METHODS FormatDate or FormatDateTime
  else if (componentType == "Clock") {
    if (methodName == "FormatDate") {
      var inputObj = {"ARG1":{
        inputType:"value",
        blockInfo:{type:"text", fieldNameToValue:{"TEXT":"MMM d, yyyy"}}
      }};
    }
    else if (methodName == "FormatDateTime") {
      var inputObj = {"ARG1":{
        inputType:"value",
        blockInfo:{type:"text", fieldNameToValue:{"TEXT":"MM/dd/yyyy hh:mm:ss a"}}
      }};

    }
  }
  return inputObj;
};

/**
 * [Janice, 1/23/2017]
 * Turn JSON object with block information into XML string
 * Combines bd.toolbox.ctr.blockInfoToBlockObject and
 *  bd.toolbox.ctr.blockObjectToXMLArray into one function
 * @param blockInfo - JSON object
 * @returns XMLString
 */
Blockly.Drawer.blockInfoToXML = function(blockInfo) {
  var blockObj = bd.toolbox.ctr.blockInfoToBlockObject(blockInfo);
  var blockXMLString = bd.toolbox.ctr.blockObjectToXML(blockObj);
  return blockXMLString;
};

/**
 * Hide the Drawer flyout
 */
Blockly.Drawer.hide = function() {
  Blockly.Drawer.flyout_.hide();
};

/**
 * @returns  true if the Drawer flyout is currently open, false otherwise.
 */
Blockly.Drawer.isShowing = function() {
  return Blockly.Drawer.flyout_.isVisible();
};

/** #################################################################
 * The five functions below are not needed to show block drawers. 
 * However, they are used in typeblock.js (for typeblock-ing), 
 * so they could not be deleted.
 * 1/24/2017 - Janice
 */

Blockly.Drawer.mutatorAttributesToXMLString = function(mutatorAttributes){
  var xmlString = '<mutation ';
  for(var attributeName in mutatorAttributes) {
    xmlString += attributeName + '="' + mutatorAttributes[attributeName] + '" ';
  }
  xmlString += '></mutation>';
  return xmlString;
}

/**
 * [lyn, 10/22/13] return an XML string including one procedure caller for each procedure declaration
 * in main workspace.
 * [jos, 10/18/15] if we pass a proc_name, we only want one procedure returned as xmlString
 */
Blockly.Drawer.procedureCallersXMLString = function(returnsValue, proc_name) {
  var xmlString = '<xml>'  // Used to accumulate xml for each caller
  var decls = Blockly.AIProcedure.getProcedureDeclarationBlocks(returnsValue);

  if (proc_name) {
    for (var i = 0; i < decls.length; i++) {
      if (decls[i].getFieldValue('NAME').toLocaleLowerCase() == proc_name){
        xmlString += Blockly.Drawer.procedureCallerBlockString(decls[i]);
        break;
      }
    }
  }
  else {
    decls.sort(Blockly.Drawer.compareDeclarationsByName); // sort decls lexicographically by procedure name
    for (var i = 0; i < decls.length; i++) {
      xmlString += Blockly.Drawer.procedureCallerBlockString(decls[i]);
    }
  }
  xmlString += '</xml>';
  return xmlString;
};

/**
 * [lyn, 10/22/13] return an XML string for a caller block for the give procedure declaration block
 * Here's an example:
 *   <block type="procedures_callnoreturn" inline="false">
 *     <title name="PROCNAME">p2</title>
 *     <mutation name="p2">
 *       <arg name="b"></arg>
 *       <arg name="c"></arg>
 *    </mutation>
 *  </block>
 */
Blockly.Drawer.procedureCallerBlockString = function(procDeclBlock) {
  var declType = procDeclBlock.type;
  var callerType = (declType == 'procedures_defreturn') ? 'procedures_callreturn' : 'procedures_callnoreturn';
  var blockString = '<block type="' + callerType + '" inline="false">'
  var procName = procDeclBlock.getFieldValue('NAME');
  blockString += '<title name="PROCNAME">' + procName + '</title>';
  var mutationDom = procDeclBlock.mutationToDom();
  mutationDom.setAttribute('name', procName); // Decl doesn't have name attribute, but caller does
  var mutationXmlString = Blockly.Xml.domToText(mutationDom);
  blockString += mutationXmlString;
  blockString += '</block>';
  return blockString;
}

/**
 * Given the blockType and a dictionary of the mutator attributes
 * either return the xml string associated with the default block
 * or return null, since there are no default blocks associated with the blockType.
 */
Blockly.Drawer.getDefaultXMLString = function(blockType,mutatorAttributes) {
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
    for(var i=0;i<possibleMutatorDefaults.length;i++) {
      matchingAttributes = possibleMutatorDefaults[i].matchingMutatorAttributes;
      //if the object doesn't have a matchingAttributes object, then skip it
      if(!matchingAttributes) {
        continue;
      }
      //go through each of the mutator attributes.
      //if one attribute does not match then move to the next possibility
      allMatch = true;
      for(var mutatorAttribute in matchingAttributes) {
        if(mutatorAttributes[mutatorAttribute] != matchingAttributes[mutatorAttribute]){
          allMatch = false
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

}

Blockly.Drawer.defaultBlockXMLStrings = {
  controls_forRange: {xmlString:
  '<xml>' +
    '<block type="controls_forRange">' +
      '<value name="START"><block type="math_number"><title name="NUM">1</title></block></value>' +
      '<value name="END"><block type="math_number"><title name="NUM">5</title></block></value>' +
      '<value name="STEP"><block type="math_number"><title name="NUM">1</title></block></value>' +
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

  component_method: [
    {matchingMutatorAttributes:{component_type:"TinyDB", method_name:"GetValue"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
         '<value name="ARG1"><block type="text"><title name="TEXT"></title></block></value>' +
         '</block>' +
         '</xml>';}},

    {matchingMutatorAttributes:{component_type:"FirebaseDB", method_name:"GetValue"},
      mutatorXMLStringFunction: function(mutatorAttributes) {
        return '' +
            '<xml>' +
            '<block type="component_method">' +
              //mutator generator
            Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
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
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
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
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
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
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
         '<value name="ARG3"><block type="logic_boolean"><title name="BOOL">TRUE</title></block></value>' +
         '</block>' +
         '</xml>';}},

    // Clock.FormatDate has pattern default to MMM d, yyyy
    {matchingMutatorAttributes:{component_type:"Clock", method_name:"FormatDate"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
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
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
         '<value name="ARG1"><block type="text"><field name="TEXT">MM/dd/yyyy hh:mm:ss a</field></block></value>' +
         '</block>' +
         '</xml>';}}
  ]
};

// #################################################################
// Below functions were part of original drawer.js code, but
// are no longer needed.
// 1/24/2017 - Janice

/**
 * Build the hierarchical tree of block types.
 * Note: taken from Blockly's toolbox.js
 * @return {!Object} Tree object.
 * @private
 */
/*
Blockly.Drawer.buildTree_ = function() {
  var tree = {};
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
*/

/**
 * [Janice, 1/23/2017] 
 * If drawer blocks are shown using bd.toolbox.ctr, 
 * then return True.  Else return False.
 */
/*
Blockly.Drawer.isDrawer = function(name) {
  var drawers = ["cat_Logic", "cat_Control", "cat_Math", "cat_Text", "cat_Lists", "cat_Colors", "cat_Variables", "cat_Procedures"];
  for (var n = 0; n < drawers.length; n++) {
    if (name == drawers[n]) {
      return true;
    }
  }
  return false;
};
*/

/**
 * [Janice, 1/24/2017] 
 * Return JSON object Array of all Procedure blocks that need to be displayed
 */
/*
Blockly.Drawer.procedureBlockArray = function(procArray) {
  // Creates array of TYPES of blocks that should be in drawer
  var newArray = [];
  for(var i=0;i<procArray.length;i++) {
    if(!(procArray[i].type == "procedures_callnoreturn" 
      // Include callnoreturn only if at least one defnoreturn declaration
         && JSON.stringify(Blockly.AIProcedure.getProcedureNames(false))
            == JSON.stringify([Blockly.FieldProcedure.defaultValue]))
      &&
       !(procArray[i].type == "procedures_callreturn"
       // Include callreturn only if at least one defreturn declaration
         && JSON.stringify(Blockly.AIProcedure.getProcedureNames(true))
            == JSON.stringify([Blockly.FieldProcedure.defaultValue]))){
      newArray.push(procArray[i]);
    }
  }
  procArray = newArray;

  // Gets specific blocks for procedures
  var blockArray = [];
  for (var i = 0; i < procArray.length; i++) {
    // for the call blocks
    if (procArray[i].type == 'procedures_callnoreturn' || procArray[i].type == 'procedures_callreturn') {
      // Blockly.Drawer.procedureCallersBlockArray(blockType == 'procedures_callreturn')
      blockArray = blockArray.concat(Blockly.Drawer.procedureCallersBlockArray(procArray[i].type == 'procedures_callreturn'));
    }
    // for the default procedures def blocks
    else {
      blockArray.push(procArray[i]);
    }
  }
  return blockArray;
}
*/

/*
Blockly.Drawer.blockListToXMLArray = function(blockList) {
  var xmlArray = [];
  for(var i=0;i<blockList.length;i++) {
    xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray(blockList[i],null));
  }
  return xmlArray;
};

Blockly.Drawer.instanceNameToXMLArray = function(instanceName) {
  var xmlArray = [];
  var typeName = Blockly.Component.instanceNameToTypeName(instanceName);
  var mutatorAttributes;

  //create event blocks
  var eventObjects = Blockly.ComponentTypes[typeName].componentInfo.events;
  for(var i=0;i<eventObjects.length;i++) {
    if (eventObjects[i].deprecated === "true") continue;
    mutatorAttributes = {component_type: typeName, instance_name: instanceName, event_name : eventObjects[i].name};
    xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_event",mutatorAttributes));
  }
  //create non-generic method blocks
  var methodObjects = Blockly.ComponentTypes[typeName].componentInfo.methods;
  for(var i=0;i<methodObjects.length;i++) {
    if (methodObjects[i].deprecated === "true") continue;
    mutatorAttributes = {component_type: typeName, instance_name: instanceName, method_name: methodObjects[i].name, is_generic:"false"};
    xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_method",mutatorAttributes));
  }

  //for each property
  var propertyObjects = Blockly.ComponentTypes[typeName].componentInfo.blockProperties;
  for(var i=0;i<propertyObjects.length;i++) {
    //create non-generic get block
    if (propertyObjects[i].deprecated === "true") continue;
    if(propertyObjects[i].rw == "read-write" || propertyObjects[i].rw == "read-only") {
      mutatorAttributes = {set_or_get:"get", component_type: typeName, instance_name: instanceName, property_name: propertyObjects[i].name, is_generic: "false"};
      xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_set_get",mutatorAttributes));
    }
    //create non-generic set block
    if(propertyObjects[i].rw == "read-write" || propertyObjects[i].rw == "write-only") {
      mutatorAttributes = {set_or_get:"set", component_type: typeName, instance_name: instanceName, property_name: propertyObjects[i].name, is_generic: "false"};
      xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_set_get",mutatorAttributes));
    }
  }

  //create component literal block
  mutatorAttributes = {component_type: typeName, instance_name: instanceName};
  xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_component_block",mutatorAttributes));

  return xmlArray;
};

Blockly.Drawer.componentTypeToXMLArray = function(typeName) {
  var xmlArray = [];
  var mutatorAttributes;

  //create generic method blocks
  var methodObjects = Blockly.ComponentTypes[typeName].componentInfo.methods;
  for(var i=0;i<methodObjects.length;i++) {
    if (methodObjects[i].deprecated === "true") continue;
    mutatorAttributes = {component_type: typeName, method_name: methodObjects[i].name, is_generic:"true"};
    xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_method",mutatorAttributes));
  }

  //for each property
  var propertyObjects = Blockly.ComponentTypes[typeName].componentInfo.blockProperties;
  for(var i=0;i<propertyObjects.length;i++) {
    //create generic get block
    if (propertyObjects[i].deprecated === "true") continue;
    if(propertyObjects[i].rw == "read-write" || propertyObjects[i].rw == "read-only") {
      mutatorAttributes = {set_or_get: "get", component_type: typeName, property_name: propertyObjects[i].name, is_generic: "true"};
      xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_set_get",mutatorAttributes));
    }
    //create generic set block
    if(propertyObjects[i].rw == "read-write" || propertyObjects[i].rw == "write-only") {
      mutatorAttributes = {set_or_get: "set", component_type: typeName, property_name: propertyObjects[i].name, is_generic: "true"};
      xmlArray = xmlArray.concat(Blockly.Drawer.blockTypeToXMLArray("component_set_get",mutatorAttributes));
    }
  }
  return xmlArray;
};

Blockly.Drawer.blockTypeToXMLArray = function(blockType,mutatorAttributes) {
  var xmlString = Blockly.Drawer.getDefaultXMLString(blockType,mutatorAttributes);
  if(xmlString == null) {
    // [lyn, 10/23/13] Handle procedure calls in drawers specially
    if (blockType == 'procedures_callnoreturn' || blockType == 'procedures_callreturn') {
      xmlString = Blockly.Drawer.procedureCallersXMLString(blockType == 'procedures_callreturn');
    } else {
      xmlString = '<xml><block type="' + blockType + '">';
      if(mutatorAttributes) {
        xmlString += Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes);
      }
      xmlString += '</block></xml>';
    }
  }
  var xmlBlockArray = [];
  var xmlFromString = Blockly.Xml.textToDom(xmlString);
  // [lyn, 11/10/13] Use goog.dom.getChildren rather than .children or .childNodes
  //   to make this code work across browsers.
  var children = goog.dom.getChildren(xmlFromString);
  for(var i=0;i<children.length;i++) {
    xmlBlockArray.push(children[i]);
  }
  return xmlBlockArray;
}
*/
