// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
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

goog.require('Blockly.Flyout');
goog.require('Blockly.Options');
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

  //this.options.languageTree = Blockly.Drawer.buildTree_();
  //this.options.blockInfoArray = Blockly.Drawer.createBlockInfoArray_();
  this.options.blockInfoArray = Blockly.Drawer.createSubsetBlockInfoArray_();

  this.workspace_ = parentWorkspace;
  this.flyout_ = new Blockly.Flyout(this.options);
  var flyoutGroup = this.flyout_.createDom('g'),
      svg = this.workspace_.getParentSvg();
  if (this.workspace_.svgGroup_.nextSibling == null) {
    svg.appendChild(flyoutGroup);
  } else {
    svg.insertBefore(flyoutGroup, this.workspace_.svgGroup_.nextSibling);
  }
  this.flyout_.init(parentWorkspace);
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
  // Populate the tree structure.
  for (var name in Blockly.Blocks) {
    if (!Blockly.Blocks.hasOwnProperty(name)) continue;
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

Blockly.Drawer.createSubsetBlockInfoArray_ = function() {
  console.log("HELLO creating subset block info array here");
  try {
    console.log("trying to set subset string");
    var subsetJsonString;
    var formName = Blockly.mainWorkspace.formName;
    var screenName = formName.substring(formName.indexOf("_") + 1);
    if (window.parent.BlocklyPanel_getComponentInstancePropertyValue) {
      subsetJsonString = window.parent.BlocklyPanel_getComponentInstancePropertyValue(formName, screenName, "BlocksToolkit");
    } else {
      subsetJsonString = JSON.stringify(window.reactGetSubsetString());
    }  
    console.log("subsetJsonString");
    console.log(subsetJsonString);
    var subsetArray = JSON.parse(subsetJsonString);
    console.log("subsetArray");
    console.log(subsetArray);
    var subsetBlockArray = subsetArray["shownBlockTypes"];
    var drawerTypes = ["Logic", "Control", "Math", "Text", "Lists", "Colors", "Variables", "Procedures"];
    var fullBlockArray = Blockly.Drawer.createBlockInfoArray_();
    var blockArray = {};
    for (var key in subsetBlockArray) {
      if (key != 'ComponentBlocks') {
        var typeName = "cat_" + key;
        blockArray[typeName] = subsetBlockArray[key];
      }
    }
    console.log(blockArray);
    return blockArray;
  } catch (err) {
    console.log(err);
    return Blockly.Drawer.createBlockInfoArray_();
  }
};

/**
 * [Janice, 1/23/2017] 
 * Creates a dictionary mapping each category to an array of blocks 
 * in that category. The blocks are in JSON format, and this array
 * structure makes it easier to customize/manipulate block objects.
 * Note: procedures_callreturn and procedures_callnoreturn are under
 * 'list' property, not 'type'.
 */
Blockly.Drawer.createBlockInfoArray_ = function() {
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
        "TO":{inputType:"value", blockInfo:{type:"math_number", fieldNameToValue:{"NUM":"100"}}}
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
 * Show the contents of the built-in drawer named drawerName. drawerName
 * should be one of Blockly.Msg.VARIABLE_CATEGORY,
 * Blockly.Msg.PROCEDURE_CATEGORY, or one of the built-in block categories.
 * @param drawerName
 */
Blockly.Drawer.prototype.showBuiltin = function(drawerName) {
  drawerName = Blockly.Drawer.PREFIX_ + drawerName;
  //var blockInfoArray = this.options.blockInfoArray;
  var blockInfoArray = Blockly.Drawer.createSubsetBlockInfoArray_();
  var drawerArray = blockInfoArray[drawerName];

  // if (!blockSet) {
  //   throw "no such drawer: " + drawerName;
  // }

  if (drawerArray) {
    //var xmlList = this.blockListToXMLArray(blockSet);
    var xmlList = [];

    for (var i = 0; i < drawerArray.length; i++) {
      if (drawerArray[i].list == "procedures_callnoreturn" || drawerArray[i].list == "procedures_callreturn") {
        var returnBool = (drawerArray[i].list == "procedures_callreturn");
        var callerArray = Blockly.Drawer.procedureCallersBlockArray(returnBool);
        for (var k = 0; k < callerArray.length; k++) {
          xmlList.push(Blockly.Drawer.blockInfoToXML(callerArray[k]));
        }
      } else {
        xmlList.push(Blockly.Drawer.blockInfoToXML(drawerArray[i]));
      }
    }
    this.flyout_.show(xmlList);
  }
};

/**
 * Show the blocks drawer for the component with give instance name. If no
 * such component is found, currently we just log a message to the console
 * and do nothing.
 */
Blockly.Drawer.prototype.showComponent = function(instanceName) {
  var component = this.workspace_.getComponentDatabase().getInstance(instanceName);
  if (component) {
    this.flyout_.hide();
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
    this.flyout_.hide();

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
  for(var i=0;i<blockList.length;i++) {
    Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray(blockList[i],null));
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

  // I attempt to do properties
  var instanceName = instanceRecord.name;


  var formName = Blockly.mainWorkspace.formName;
  var screenName = formName.substring(formName.indexOf("_") + 1);
  var subsetJsonString;
  if (window.parent.BlocklyPanel_getComponentInstancePropertyValue) {
    subsetJsonString = window.parent.BlocklyPanel_getComponentInstancePropertyValue(formName, screenName, "BlocksToolkit");
  } else {
    subsetJsonString = JSON.stringify(window.reactGetSubsetString());
  }
  var subsetArray = [];
  var subsetBlocks = [];
  console.log("Subset json string");
  console.log(subsetJsonString);

  if (subsetJsonString.length > 0) {
    subsetArray = JSON.parse(subsetJsonString);
    console.log("subsetArray");
    console.log(subsetArray);
    var subsetBlockArray = subsetArray["shownBlockTypes"]["ComponentBlocks"][typeName];
    console.log("subsetBlockArray");
    console.log(subsetBlockArray);
    if (subsetBlockArray != undefined) {
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

    } else {
      //create event blocks
      goog.object.forEach(componentInfo.eventDictionary, function(event, name) {
        if (event.deprecated != 'true') {
          Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_event', {
            'component_type': typeName, 'instance_name': instanceRecord.name, 'event_name': name
          }));
        }
      }, this);

      //create non-generic method blocks
      goog.object.forEach(componentInfo.methodDictionary, function(method, name) {
        if (method.deprecated != 'true') {
          Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_method', {
            'component_type': typeName, 'instance_name': instanceRecord.name, 'method_name': name
          }));
        }
      }, this);

      //for each property
      goog.object.forEach(componentInfo.properties, function(property, name) {
        if (property.deprecated != 'true') {
          var params = {'component_type': typeName, 'instance_name': instanceRecord.name,
                        'property_name': name};
          if ((property.mutability & Blockly.PROPERTY_READABLE) == Blockly.PROPERTY_READABLE) {
            params['set_or_get'] = 'get';
            Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_set_get', params));
          }
          if ((property.mutability & Blockly.PROPERTY_WRITEABLE) == Blockly.PROPERTY_WRITEABLE) {
            params['set_or_get'] = 'set';
            Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_set_get', params));
          }
        }
      }, this);

      //create component literal block
      var mutatorAttributes = {component_type: typeName, instance_name: instanceRecord.name};
      Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray("component_component_block",mutatorAttributes));
    }
  } else {

    console.log("Using default blocks because no JSON specified");
    //create event blocks
      goog.object.forEach(componentInfo.eventDictionary, function(event, name) {
        if (event.deprecated != 'true') {
          Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_event', {
            'component_type': typeName, 'instance_name': instanceRecord.name, 'event_name': name
          }));
        }
      }, this);

      //create non-generic method blocks
      goog.object.forEach(componentInfo.methodDictionary, function(method, name) {
        if (method.deprecated != 'true') {
          Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_method', {
            'component_type': typeName, 'instance_name': instanceRecord.name, 'method_name': name
          }));
        }
      }, this);

      //for each property
      goog.object.forEach(componentInfo.properties, function(property, name) {
        if (property.deprecated != 'true') {
          var params = {'component_type': typeName, 'instance_name': instanceRecord.name,
                        'property_name': name};
          if ((property.mutability & Blockly.PROPERTY_READABLE) == Blockly.PROPERTY_READABLE) {
            params['set_or_get'] = 'get';
            Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_set_get', params));
          }
          if ((property.mutability & Blockly.PROPERTY_WRITEABLE) == Blockly.PROPERTY_WRITEABLE) {
            params['set_or_get'] = 'set';
            Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_set_get', params));
          }
        }
      }, this);

      //create component literal block
      var mutatorAttributes = {component_type: typeName, instance_name: instanceRecord.name};
      Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray("component_component_block",mutatorAttributes));
  }

  console.log("XML Array");
  console.log(xmlArray);
  return xmlArray;
};

Blockly.Drawer.prototype.componentTypeToXMLArray = function(typeName) {
  var xmlArray = [];
  var componentInfo = this.workspace_.getComponentDatabase().getType(typeName);

  //create generic event blocks
  goog.object.forEach(componentInfo.eventDictionary, function(event, name){
    if(!event.deprecated){
      Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_event', {
        component_type: typeName, event_name: name, is_generic: 'true'
      }));
    }
  }, this);

  //create generic method blocks
  goog.object.forEach(componentInfo.methodDictionary, function(method, name) {
    if (!method.deprecated) {
      Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_method', {
        component_type: typeName, method_name: name, is_generic: "true"
      }));
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
        Array.prototype.push.apply(xmlArray, this.blockTypeToXMLArray('component_set_get', params));
      }
    }
  }, this);

  return xmlArray;
};

Blockly.Drawer.prototype.blockTypeToXMLArray = function(blockType,mutatorAttributes) {
  var xmlString = Blockly.Drawer.getDefaultXMLString(blockType,mutatorAttributes);
  if(xmlString == null) {
    // [lyn, 10/23/13] Handle procedure calls in drawers specially
    if (blockType == 'procedures_callnoreturn' || blockType == 'procedures_callreturn') {
      xmlString = this.procedureCallersXMLString(blockType == 'procedures_callreturn');
    } else {
      xmlString = '<xml><block type="' + blockType + '">';
      if(mutatorAttributes) {
        if (mutatorAttributes['is_generic'] === undefined) {
          mutatorAttributes['is_generic'] = !mutatorAttributes['instance_name']
        }
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
};

Blockly.Drawer.mutatorAttributesToXMLString = function(mutatorAttributes){
  var xmlString = '<mutation ';
  for(var attributeName in mutatorAttributes) {
    if (!mutatorAttributes.hasOwnProperty(attributeName)) continue;
    xmlString += attributeName + '="' + mutatorAttributes[attributeName] + '" ';
  }
  xmlString += '></mutation>';
  return xmlString;
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


// [lyn, 10/22/13] return an XML string including one procedure caller for each procedure declaration
// in main workspace.
// [jos, 10/18/15] if we pass a proc_name, we only want one procedure returned as xmlString
Blockly.Drawer.prototype.procedureCallersXMLString = function(returnsValue, proc_name) {
  var xmlString = '<xml>';  // Used to accumulate xml for each caller
  var decls = this.workspace_.getProcedureDatabase().getDeclarationBlocks(returnsValue);

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

Blockly.Drawer.compareDeclarationsByName = function (decl1, decl2) {
  var name1 = decl1.getFieldValue('NAME').toLocaleLowerCase();
  var name2 = decl2.getFieldValue('NAME').toLocaleLowerCase();
  return name1.localeCompare(name2);
};

// [lyn, 10/22/13] return an XML string for a caller block for the give procedure declaration block
// Here's an example:
//   <block type="procedures_callnoreturn" inline="false">
//     <title name="PROCNAME">p2</title>
//     <mutation name="p2">
//       <arg name="b"></arg>
//       <arg name="c"></arg>
//    </mutation>
//  </block>
Blockly.Drawer.procedureCallerBlockString = function(procDeclBlock) {
  var declType = procDeclBlock.type;
  var callerType = (declType == 'procedures_defreturn') ? 'procedures_callreturn' : 'procedures_callnoreturn';
  var blockString = '<block type="' + callerType + '" inline="false">'
  var procName = procDeclBlock.getFieldValue('NAME');
  blockString += '<title name="PROCNAME">' + procName + '</title>';
  var mutationDom = procDeclBlock.mutationToDom();
  mutationDom.setAttribute('name', procName); // Decl doesn't have name attribute, but caller does
  blockString += Blockly.Xml.domToText(mutationDom);
  blockString += '</block>';
  return blockString;
};

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
  lists_join_with_separator: {xmlString:
    '<xml>' +
      '<block type="lists_join_with_separator">' +
      '<value name="SEPARATOR"><block type="text"><title name="TEXT"></title></block></value>' +
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

    // Canvas.DrawShape has fill default to TRUE
    {matchingMutatorAttributes:{component_type:"Canvas", method_name:"DrawShape"},
     mutatorXMLStringFunction: function(mutatorAttributes) {
       return '' +
         '<xml>' +
         '<block type="component_method">' +
         //mutator generator
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
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
         Blockly.Drawer.mutatorAttributesToXMLString(mutatorAttributes) +
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
