// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// Note: This file is a shim for the Lexical Variable Plugin

goog.provide('AI.NameSet');
goog.provide('AI.Substitution');

var lexVarPlugin = LexicalVariablesPlugin;

/**
 * Flydown
 */

goog.provide('AI.Blockly.Flydown');
Blockly.Flydown = lexVarPlugin.Flydown;

/**
 * FieldFlydown
 */

goog.provide('AI.Blockly.FieldFlydown');
Blockly.FieldFlydown = lexVarPlugin.FieldFlydown;

/**
 * FieldGlobalFlydown
 */

goog.provide('AI.Blockly.FieldGlobalFlydown');
// goog.require('AI.Blockly.FieldFlydown');
Blockly.FieldGlobalFlydown = lexVarPlugin.FieldGlobalFlydown;

/**
 * FieldParameterFlydown
 */
goog.provide('AI.Blockly.FieldLexicalVariable');
Blockly.FieldLexicalVariable = lexVarPlugin.FieldLexicalVariable;

/**
 * FieldParameterFlydown
 */

goog.provide('AI.Blockly.FieldParameterFlydown');
// goog.require('AI.Blockly.FieldFlydown');
Blockly.FieldParameterFlydown = lexVarPlugin.FieldParameterFlydown;

/**
 * FieldProcedureName
 */

goog.provide('AI.Blockly.FieldProcedureName');
AI.Blockly.FieldProcedureName = lexVarPlugin.FieldProcedureName;

/**
 * LexicalVariable
 */

Blockly.LexicalVariable = lexVarPlugin.LexicalVariable;

/**
 * ProcedureNameDropdown
 */

goog.provide('AI.Blockly.ProcedureNameDropdown');
AI.Blockly.ProcedureNameDropdown = lexVarPlugin.FieldNoCheckDropdown;

/**
 * FieldNoCheckDropdown
 */
goog.provide('AI.Blockly.FieldNoCheckDropdown');
AI.Blockly.FieldNoCheckDropdown = lexVarPlugin.FieldNoCheckDropdown;

// Add event methods for App Inventor

/**
 * [lyn, 07/03/14] Created
 * [jis, 09/18/15] Refactored into two procedures
 * @param {Blocky.BlockSvg} block a getter or setter block
 *
 * Creates the mutation for the eventparam attribute
 */
Blockly.LexicalVariable.eventParamMutationToDom = function (block) {
  if (!block.eventparam) {      // Newly created block won't have one
    Blockly.LexicalVariable.getEventParam(block);
  }
  // At this point, if the variable is an event parameter, the
  // eventparam field will be set. Otherwise it won't be and
  // we return null
  if (!block.eventparam) {
    return null;
  }
  var mutation = document.createElement('mutation');
  var eventParam = document.createElement('eventparam');
  eventParam.setAttribute('name', block.eventparam);
  mutation.appendChild(eventParam);
  return mutation;
}

/**
 * For getter or setter block of event parameters, sets the eventparam
 * field of the block which contains the default (untranslated =
 * English) name for event parameters.
 *
 * @param {Blockly.Block} block a getter or setter block
 */

Blockly.LexicalVariable.getEventParam = function (block) {
  // If it isn't undefined, then we have already computed it.
  if (block.eventparam !== undefined) {
    return block.eventparam;
  }
  block.eventparam = null;      // So if we leave without setting it to
                                // some value, we know we have already
                                // evaluated it.
  var prefixPair = Blockly.unprefixName(block.getFieldValue("VAR"));
  var prefix = prefixPair[0];
  if (prefix !== Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX) {
    var name = prefixPair[1];
    var child = block;
    var parent = block.getParent();
    while (parent) {
      var type = parent.type;
      // Walk up ancestor tree to determine if name is an event parameter name.
      if (type === "component_event") {
        var componentDb = block.getTopWorkspace().getComponentDatabase();
        var untranslatedEventParams = parent.getParameters().map(function (param) {
          return param.name;
        });
        var translatedEventParams = untranslatedEventParams.map(
            function (name) {
              return componentDb.getInternationalizedParameterName(name);
            }
        );
        var index = translatedEventParams.indexOf(name);
        if (index != -1) {
          block.eventparam = untranslatedEventParams[index];
          return null;         // return value is unimportant
        } else {
          return null;
        }
      } else if ((type === "local_declaration_expression"
              && parent.getInputTargetBlock('RETURN') == child) // only body is in scope of names
          || (type === "local_declaration_statement"
              && parent.getInputTargetBlock('STACK') == child) // only body is in scope of names
      ) {
        var params = parent.getVars(); // [lyn, 10/13/13] Names from block, not localNames_ instance var
        if (params.indexOf(name) != -1) {
          return null; // Name is locally bound, not an event parameter.
        }
      } else if ((type === "controls_forEach" || type === "controls_forRange")
          && (parent.getInputTargetBlock('DO') == child)) { // Only DO is in scope, not other inputs!
        var loopName = parent.getFieldValue('VAR');
        if (loopName == name) {
          return null; // Name is locally bound, not an event parameter.
        }
      } else if (type == 'controls_for_each_dict'
          && parent.getInputTargetBlock('DO') == child  // Only DO is in scope.
          && parent.getVars().indexOf(name) != -1) {  // is the name defined by the loop?
        // If the child is in the scope of the loop, and it is accessing a var
        // defined on the loop return null to say it is not an event param.
        return null;
      }
      child = parent;
      parent = parent.getParent(); // keep moving up the chain.
    }
    return null; // If get to this point, there is no mutation
  }
}

/**
 * [lyn, 07/03/14] Created
 * @param {Blockly.BlockSvg} block a getter or setter block
 * @param {Element} xmlElement an XML element
 * For getters and setters of event parameters, marks them specially
 * with a eventparam property to support i8n.
 * This is used only by Blockly.LexicalVariable.eventParameterDict
 */
Blockly.LexicalVariable.eventParamDomToMutation = function (block, xmlElement) {
  var children = goog.dom.getChildren(xmlElement);
  if (children.length == 1) { // Should be exactly one eventParam child
    var childNode = children[0];
    if (childNode.nodeName.toLowerCase() == 'eventparam') {
      var untranslatedEventName = childNode.getAttribute('name');
      block.eventparam = untranslatedEventName; // special property viewed by Blockly.LexicalVariable.eventParameterDict
    }
  }
}

/**
 * [lyn, 07/03/14] Created
 * @param {Blockly.BlockSvg} block a block
 * @returns a "dictionary" object that maps all default event parameter names
 *   used in the block to their translated names.
 */
Blockly.LexicalVariable.eventParameterDict = function (block) {
  var dict = {};
  var descendants = block.getDescendants();
  for (var i = 0, descendant; descendant = descendants[i]; i++) {
    if (descendant.eventparam) {
      // descendant.eventparam is the default event parameter name
      // descendant.getFieldValue('VAR') is the possibly translated name
      dict[descendant.eventparam] = descendant.getFieldValue('VAR');
    }
  }
  return dict;
}

/**
 * Shared
 */

// Copy the shared properties from the Shared object of the Lexical Variables Plugin to Blockly
Blockly = Object.assign(Blockly, lexVarPlugin.Shared);

// Add some properties to Blockly namespace that are defined by the plugin, possibly with different names.
Blockly.NameSet = lexVarPlugin.NameSet;
Blockly.Substitution = lexVarPlugin.Substitution;
Blockly.prefixSeparator = Blockly.yailSeparator;
Blockly.usePrefixInCode = Blockly.usePrefixInYail;
Blockly.possiblyPrefixYailNameWith = Blockly.possiblyPrefixGeneratedVarName;
