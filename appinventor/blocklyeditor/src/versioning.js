// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle converting apps from older versions to current
 *
 * @author wolber@usfca.edu (David Wolber)
 *
 * [lyn, 2014/10/31] Completely overhauled blocks upgrading architecture.
 * All the work is done in Blockly.Version.upgrade.
 * As in Liz Looney's AI1 BlocksSaveFile upgraders, all upgrades are based on mismatches
 * between component versions or language versions of the screen and those of the current system.
 * Unlike Liz's system, in which conditionals based on version numbers are used to express AI1 upgrades,
 * all AI2 upgrades are expressed via the dictionary structure in Blockly.Versioning.AllUpgradeMaps,
 * which is defined at the end of this file. This is a more declarative approach for expressing upgrades.
 */

'use strict';

goog.require('Blockly.Component');
goog.require('Blockly.ComponentTypes');
goog.require('goog.dom');
goog.require('goog.dom.xml');

goog.provide('Blockly.Versioning');

Blockly.Versioning.loggingFlag = true;

Blockly.Versioning.setLogging = function (bool) {
  Blockly.Versioning.loggingFlag = bool;
};

Blockly.Versioning.log = function log(string) { // Display feedback on upgrade if Blockly.Versioning.loggingFlag is on.
  if (Blockly.Versioning.loggingFlag) {
    console.log("Blockly.Versioning: " + string);
  }
};

/**
 * [lyn, 2014/11/04] Simplified version of Halloween AI2 upgrading architecture.
 *
 * @param preUpgradeFormJsonString: JSON String from pre-upgrade Form associated with these blocks
 * @param blocksContent: String with XML representation of blocks for a screen
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 *
 * Perform any upgrades on the blocks implied by mismatches (1) between component version numbers
 * in preUpgradeFormJsonString and current system component numbers or (2) between blocks language
 * version number in blocksContent and current system blocks language version.
 *
 * After any upgrades, load the blocks into Blockly.mainWorkspace.
 *
 * Upgrades may be performed either on the XML dom tree representation or the loaded blocks
 * representation in Blockly.mainWorkspace. As a consquence, the upgrading process may
 * ping-pong back and forth between these two representations. But in the end, the upgraded
 * blocks will be loaded into Blockly.mainWorkspace.
 *
 * All upgrades are described by the dictionary structure in Blockly.Versioning.AllUpgradeMaps,
 * which is defined at the end of this file.
 *
 */
Blockly.Versioning.upgrade = function (preUpgradeFormJsonString, blocksContent) {
  var preUpgradeFormJsonObject = JSON.parse(preUpgradeFormJsonString);
  var dom = Blockly.Xml.textToDom(blocksContent); // Initial blocks rep is dom for blocksContent

  /**
   * Upgrade the given componentType. If componentType is "Language", upgrades the blocks language.
   * The rep argument is either a dom or a workspace. It is passed explicitly only to indicate type
   * of current rep to determine when conversion needs to be done between dom and workspace (or vice versa).
   * @param componentType
   * @param preUpgradeVersion
   * @param systemVersion
   * @param rep: the current blocks representation (dom or Blockly.mainWorkspace).
   *        This is only passed explicitly to indicate type of current rep to determine
   *        when conversion needs to be done between dom and workspace (or vice versa).
   * @returns: resulting representation (dom or workspace), again only for dynamic type checking.
   */
  function upgradeComponentType (componentType, preUpgradeVersion, systemVersion, rep) {
    if (componentType == "Form") {
      componentType = "Screen"; // Treat Form as if it were Screen
    }
    Blockly.Versioning.log("In Blockly.Versioning.upgrade, upgradeComponentType("  + componentType + "," +
        preUpgradeVersion + ","  + systemVersion + "," + rep + ")");
    if (preUpgradeVersion > systemVersion) {
      // What to do in this case? Currently, throw an exception, but might want to do something else:
      // JIS: We simply ignore this situation. It happens when someone imports a project that was
      // touched by a newer version of App Inventor. By the time we are run here the user has already
      // been shown a warning that the project may not work as expected. However if we throw the
      // exception below we *guarantee* that the project will fail to load. Let's give them a
      // chance instead (so the lines below are commented out).
      // throw "Unexpected situation in Blockly.Versioning.upgrade: preUpgradeVersion of " + componentType +
      //     " = " + preUpgradeVersion + " > systemVersion = " + systemVersion;
    } else if (preUpgradeVersion < systemVersion) {
      // Need to upgrade this component
      Blockly.Versioning.log("upgrading component type " + componentType + " from version " +
          preUpgradeVersion + " to version " + systemVersion);
      var upgradeMap = Blockly.Versioning.AllUpgradeMaps[componentType];
      if (! upgradeMap) {
        throw "Blockly.Versioning.upgrade: no upgrade map for component type " + componentType;
      }
      for (var version = preUpgradeVersion + 1; version <= systemVersion; version++) {
        var versionUpgrader = upgradeMap[version];
        if (! versionUpgrader) {
          throw "Blockly.Versioning.upgrade: no upgrader to upgrade component type " + componentType +
              " to version " + version;
        }
        // Perform upgrade
        Blockly.Versioning.log("applying upgrader for upgrading component type " + componentType +
            " from version " + (version-1) + " to version " + version);
        // Apply upgrader, possibly mutating rep and changing its dynamic type.
        rep = Blockly.Versioning.applyUpgrader(versionUpgrader, rep);
      }
    } // otherwise, preUpgradeVersion and systemVersion are equal and no updgrade is necessary
    return rep; // Return final blocks representation, for dynamic typing purposes
  }

  // --------------------------------------------------------------------------------
  // Upgrade language based on language version

  var systemLanguageVersion = window.parent.BlocklyPanel_getBlocksLanguageVersion();
  var systemYoungAndroidVersion = window.parent.BlocklyPanel_getYaVersion();
  var versionTags = dom.getElementsByTagName('yacodeblocks');

  // if there is no version in the file, then this is an early ai2 project, prior to
  // 10/21/13, when the blocks internal xml structure was overhauled
  // with descriptive mutator tags. blocksOverhaul translates the blocks

  var preUpgradeLanguageVersion;
  if (versionTags.length===0) {
    Blockly.Versioning.v17_blocksOverhaul(dom);
    preUpgradeLanguageVersion = 17;  // default for oldest ai2
  }
  else {
    if (systemYoungAndroidVersion == parseInt(versionTags[0].getAttribute('ya-version'), 10)) {
      Blockly.Versioning.ensureWorkspace(dom);
      return;
    }
    preUpgradeLanguageVersion = parseInt(versionTags[0].getAttribute('language-version'), 10);
  }

  var blocksRep = dom; // Initial blocks rep is dom
  blocksRep = upgradeComponentType("Language", preUpgradeLanguageVersion, systemLanguageVersion, blocksRep);

  // --------------------------------------------------------------------------------
  // Upgrade components based on pre-upgrade version numbers
  var preUpgradeComponentVersionDict = Blockly.Versioning.makeComponentVersionDict(preUpgradeFormJsonObject);
  for (var componentType in preUpgradeComponentVersionDict) {
    var preUpgradeVersion = preUpgradeComponentVersionDict[componentType];
    var systemVersion = Blockly.Versioning.getSystemComponentVersion(componentType);
    blocksRep = upgradeComponentType(componentType, preUpgradeVersion, systemVersion, blocksRep);
  }

  // Ensure that final blocks rep is Blockly.mainWorkspace
  Blockly.Versioning.log("Blockly.Versioning.upgrade: Final conversion to Blockly.mainWorkspace");
  Blockly.Versioning.ensureWorkspace(blocksRep); // No need to use result; does work by side effect on Blockly.mainWorkspace

}

/**
 * [lyn, 2014/11/04]
 * Check that the given upgrader is a valid form for an upgrader. It must either be:
 * (1) a function
 * (2) an array of upgraders
 * (3) a special string (one of "noUpgrade" or "ai1CantDoUpgrade")
 *
 * Note: we don't check here in array case that all array elements are themselves upgraders.
 * That check will be performed dynamically later when array elements are applied
 * by Blockly.Versioning.applyUpgrader.
 */
Blockly.Versioning.checkUpgrader = function (upgrader) {

  var specialUpgradeStrings = ["noUpgrade", "ai1CantDoUpgrade"];

  if (typeof(upgrader) == "string") {
    if (specialUpgradeStrings.indexOf(upgrader) == -1) {
      throw "Blockly.Versioning.checkUpgrader: upgrader is unrecognized special string: " + upgrader;
    }
  } else if (! ((typeof(upgrader) == "function") || Array.isArray (upgrader))) {
    throw "Blockly.Versioning.checkUpgrader: upgrader is not a function, special string, or array of upgraders -- "
        + upgrader;
  }
}

/**
 * Returns true if blocksRep is a workspace; otherwise returns false
 */
Blockly.Versioning.isWorkspace = function (blocksRep) {
  return blocksRep instanceof Blockly.Workspace;
}

/*
Blockly.Versioning.isWorkspace =
    (function () {
      var workspaceInstance = new Blockly.Workspace(); // ignore get & set metrics
      return function (blocksRep) {
        return (typeof(blocksRep) == "object") && (blocksRep.constructor == workspaceInstance.constructor)
      };

    })();
 */

/**
 * Returns true if blocksRep is a dom; otherwise returns false
 */
Blockly.Versioning.isDom = function (blocksRep) {
  try {
    return (blocksRep instanceof Element
            || blocksRep instanceof HTMLElement
            || blocksRep instanceof HTMLUnknownElement);
  } catch (anyErr) {
    // In phantomJS testing context, HTMLUnknownElement is undefined and causes an error,
    // so handle it this way.
    return false;
  }
}

/**
 * If blocksRep is a dom, returns it; otherwise converts the workspace to a dom
 */
Blockly.Versioning.ensureDom = function (blocksRep) {
  if (Blockly.Versioning.isDom(blocksRep)) {
    return blocksRep; // already a dom
  } else if (Blockly.Versioning.isWorkspace(blocksRep)) {
    Blockly.Versioning.log("Blockly.Versioning.ensureDom: converting Blockly.mainWorkspace to dom");
    var dom = Blockly.Xml.workspaceToDom(blocksRep);
    return dom;
  } else {
    throw "Blockly.Versioning.ensureDom: blocksRep is neither dom nor workspace -- " + blocksRep;
  }
}

Blockly.Versioning.getBlockChildren = function (dom) {
    var result = [];
    var gdChildren = goog.dom.getChildren(dom);
    for (var gdi = 0, gdChild; gdChild = gdChildren[gdi]; gdi++) {
      result.push(gdChild);
    }
    return result;
}

/**
 * If blocksRep is a workspace, returns it; otherwise converts the workspace to a dom
 */
Blockly.Versioning.ensureWorkspace = function (blocksRep) {
  if (Blockly.Versioning.isWorkspace(blocksRep)) {
    return blocksRep; // already a workspace
  } else if (Blockly.Versioning.isDom(blocksRep)) {
    Blockly.Versioning.log("Blockly.Versioning.ensureWorkspace: converting dom to Blockly.mainWorkspace");
    Blockly.mainWorkspace.clear(); // Remove any existing blocks before we add new ones.
    Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, blocksRep);
    return Blockly.mainWorkspace;
  } else {
    throw "Blockly.Versioning.ensureWorkspace: blocksRep is neither workspace nor dom -- " + blocksRep;
  }
}

/**
 * Apply an upgrder to a blocksRepresentation, possibly (1) changing it by side effect and
 * (2) changing its representation (dom or workspace). Returns the final representation.
 * @param upgrader
 * @param blocksRep: an instance of an XML dom tree or a Blockly.Workspace
 */
Blockly.Versioning.applyUpgrader = function (upgrader, blocksRep) {
  Blockly.Versioning.checkUpgrader(upgrader); // ensure it has the correct form.
  // Perform upgrade
  if (upgrader == "ai1CantDoUpgrade") {
    throw "Blockly.Versioning.applyUpgrader: cannot perform an AI Classic upgrade on " + blocksRep;
  } else if (typeof(upgrader) == "function") {
    return upgrader(blocksRep); // Apply upgrader, possibly mutating rep and changing its dynamic type.
  } else if (Array.isArray (upgrader)) {
    // Treat array as sequential composition of upgraders
    Blockly.Versioning.log("Blockly.Versioning.applyUpgrader: treating list as sequential composition of upgraders");
    return (Blockly.Versioning.composeUpgraders(upgrader))(blocksRep);
  } else { // otherwise, versionUpgrader is "noUpgrade", and nothing is done, so acts like identity
    return blocksRep;
  }
}

/**
 * Return a single upgrader that sequentially composes the upgraders in upgraderList
 * @param upgraderList
 */
Blockly.Versioning.composeUpgraders = function (upgraderList) {
  return function (blocksRep) {
    for (var i = 0, upgrader; upgrader = upgraderList[i]; i++) {
      blocksRep = Blockly.Versioning.applyUpgrader(upgrader, blocksRep); // Applying upgrader may convert blocks rep from dom to workspace or vice versa.
    }
    return blocksRep; // Return the final blocks rep
  }
}

/******************************************************************************
 * Key functions for determining whether component upgrades are needed
 ******************************************************************************/

/*
 /** createUpgraders takes a pre-upgrade form JSON string and returns an object with two fields:
  * 1. The "XML" field denotes a function that upgrades the dom tree by side effect.
  * 2. The "blocks" field denotes a function that upgrades the main workspace by side effect.
  * /
Blockly.Versioning.createUpgraders = function (preUpgradeFormJsonString) {
  var xmlUpgraders = [];
  var blocksUpgraders = [];

  function collectUpgradersForComponentTypeToVersion(compType, oldVersion, newVersion) {
    var upgradeMap = Blockly.Versioning.AllUpgradeMaps[compType];
    for (var version = oldVersion; version < newVersion; version = version + 1) {
      var versionUpgraders = upgradeMap[version];
      var xmlUpgrader = versionUpgraders[0];
      xmlUpgraders.push(xmlUpgrader);
      var blocksUpgrader = versionUpgraders[1];
      blocksUpgraders.push(blocksUpgrader);
    }
  }

  var preUpgradeFormJsonObject = JSON.parse(preUpgradeFormJsonString);
  var preUpgradeComponentVersionDict = Blockly.Versioning.makeComponentVersionDict(preUpgradeFormJsonObject);
  for (var componentType in preUpgradeComponentVersionDict) {
    var preUpgradeVersion = preUpgradeComponentVersionDict[componentType];
    var currentVersion = Blockly.Versioning.getCurrentVersion(componentType);
    if (preUpgradeVersion > currentVersion) {
      // What to do in this case? Currently, do nothing, but at least might want a warning
      console.log("Unexpected: preUpgradeVersion of " + componentType + " = " + str(preUpgradVerision)
                   + " > currentVersion = " + str(currentVersion));
    } else if (preUpgradeVersion < currentVersion) {
      // Need to upgrade this component
      collectUpgradersForComponentTypeToVersion(componentType, preUpgradeVersion, currentVersion);
    } // else they're equal and we really do nothing
  }

  /**
   * Given a list of one-argument side-effecting functions, returns a one-argument
   * function that applies each function in the list to the argument, one by one.
   * @param functionList
   * @returns {Function}
   * /
  function curriedApplyAll (functionList) {
    return function (arg) {
      for (var i = 0, fun; fun = functionList[i]; i = i + 1) {
        fun(arg)
      }
    }
  }

  return {"XML": curriedApplyAll(xmlUpgraders), "blocks": curriedApplyAll(blocksUpgraders)}

}
*/

/**
 *  Create a dictionary mapping each component mentioned in formJsonObject
 *  to its version number. Complain if all version numbers for the same
 *  component are not the same.
 */
Blockly.Versioning.makeComponentVersionDict = function (formJsonObject) {
  var versionDict = {};

  /**
   * Walk over component tree, modifying versionDict to have version of each component encountered.
   * @param comps

   */
  function processComponents (comps) {
    for (var c = 0, comp; comp=comps[c]; c = c+ 1) {
      var compType = comp["$Type"];
      var compVersion = parseInt(comp["$Version"]);
      var versionAlreadyInDict = versionDict[compType];
      if (versionAlreadyInDict) {
        if (versionAlreadyInDict != compVersion) {
          throw "Blockly.Versioning.makeComponentVersionDict: inconsistent versions for component of type"
                + compType + "; [" + str(compVersion) + ", " + versionAlreadyInDict + "]"
        } // Otherwise version is same as before and we're happy
      } else { // No version yet in dict; add it
        versionDict[compType] = compVersion;
      }
      var subComponents = comp["$Components"];
      if (subComponents) { // recursively process any subcomponents
        processComponents(subComponents);
      }
    }
   };

   processComponents ([formJsonObject["Properties"]]); // Walk the component tree, updating versionDict along the way.
   return versionDict;
};

Blockly.Versioning.getSystemComponentVersion = function (componentType) {
  var versionString = Blockly.ComponentTypes[componentType].componentInfo.version;
  if (versionString) {
    return parseInt(versionString);
  } else {
    throw "Blockly.Versioning.getSystemComponentVersion: No version for component type " + componentType;
  }
}

/******************************************************************************
 * Details for specific upgrades go below, in reverse chronological order.
 * The code for each upgrade *MUST* be well-documented in order to help
 * those implementing similar upgrades in the future.
 ******************************************************************************/

/**----------------------------------------------------------------------------
 * Upgrade to Blocks Language version 17:
 * @author wolber@usfca.edu (David Wolber)
 *
 * Code for translating early ai2 blocks to 10/20/13 version
 * if there is no version # in the Blockly file, then it is an early ai2 project, prior to
 * 10/21/13, when the blocks internal xml structure was overhauled
 * with descriptive mutator tags. blocksOverhaul translates the blocks
 * Methods to handle serialization of the blocks workspace
 *
 * [lyn, 10/03/2014] Notes:
 *  * This code used to be in blocklyeditor/src/versioning/017_blocksOverhaul.js
 *    but I moved it here as part of consolidating all upgrade code in one file.
 *  * I prefixed all the function names with "v17_" to clarify which
 *    functions are used in the upgrade to YAVersion 17.
 ----------------------------------------------------------------------------*/

Blockly.Versioning.v17_blocksOverhaul = function(xmlFromFile) {
  // we loaded in something with no version, we need to translate
  var renameAlert = 0;
  var blocks = xmlFromFile.getElementsByTagName('block');
  for (var i = 0, im = blocks.length; i < im; i++)  {
    var blockElem = blocks[i];
    var blockType = blockElem.getAttribute('type');
    // all built-in blocks have an entry already in Blockly.Language
    //  we don't need to translate those, so if the following is non-null we ignore
    if (Blockly.Blocks[blockType] == null)
    {
      // add some translations for language changes...
      //   these should really be in a map or at procedure that checks and
      //   returns replacement...straight translations...we could also put
      //   lexical_variable_get and set here so that we don't have to have a special
      //   case below
      if (blockType == 'procedures_do_then_return')
        blockElem.setAttribute('type',"controls_do_then_return");
      else
      if (blockType == 'procedure_lexical_variable_get')
        blockElem.setAttribute('type',"lexical_variable_get");
      else
      if (blockType == 'for_lexical_variable_get')
        blockElem.setAttribute('type',"lexical_variable_get");
      else {
        var splitComponent = blockType.split('_');
        if (splitComponent.length > 2) {
          // This happens when someone puts an _ in a block name!
          splitComponent = [splitComponent.slice(0, -1).join('_'), splitComponent.pop()];
        }
        // there are some blocks that are not built-in but are not component based,
        //   we want to ignore them
        if (splitComponent[0] != 'lexical') {
          // methods on any (generics) have a blocktype of _any_someComponent_method
          //   so 1st check if type has 'any' in it, if so we have a generic method
          if (splitComponent[1] == 'any')  // we have a generic method call
            Blockly.Versioning.v17_translateAnyMethod(blockElem);
          else {
            // we have a set, get, component get, event, or method
            // check if the first thing is a component type. If so, the only
            // legal thing it could be is a (generic) component set/get
            //   but old programs allow instance names same as type names, so
            //   we can get a Accelerometer.Shaking which is really an instance event
            if ((Blockly.ComponentTypes[splitComponent[0]] != null) &&
                (splitComponent[1] == 'setproperty' || splitComponent[1] == 'getproperty'))
              Blockly.Versioning.v17_translateComponentSetGetProperty(blockElem);
            else {
              var instance = splitComponent[0];
              var componentType = Blockly.Component.instanceNameToTypeName(instance);
              if (componentType == instance && renameAlert === 0) {
                alert("Your app was created in an earlier version of App Inventor and may be loaded incorrectly."+
                    " The problem is that it names a component instance"+
                    " the same as the component type, which is longer allowed.");
                renameAlert = 1;
              }
              // we should really check for null here so if there are blocks that
              //   are not instance_ we can ignore. Right now the following ifs
              //   probably make sure of this-- if none of the questions about rightside
              //    are answered affirmatively, but this should be checked here as well
              var rightside = splitComponent[1];
              if (rightside == 'setproperty' || rightside == 'getproperty')
                Blockly.Versioning.v17_translateSetGetProperty(blockElem);
              else
              if (rightside == 'component')
                Blockly.Versioning.v17_translateComponentGet(blockElem);
              else
              if (Blockly.ComponentTypes[componentType].eventDictionary[rightside] != null)
                Blockly.Versioning.v17_translateEvent(blockElem);
              else
              if (Blockly.ComponentTypes[componentType].methodDictionary[rightside] != null)
                Blockly.Versioning.v17_translateMethod(blockElem);
            }
          }
        }
      }
    }
  }

};

/**
 * v17_translateEvent is called when we know we have an Event element that
 * needs to be translated.
 */
Blockly.Versioning.v17_translateEvent = function(blockElem) {
  //get the event type and instance name,
  // the type attribute is "component_event"
  var blockType = blockElem.getAttribute('type');
  var component_event = blockType;
  // event block types look like: <block type="Button1_Click" x="132" y="72">
  var splitComponent = component_event.split('_');
  if (splitComponent.length > 2) {
    // This happens when someone puts an _ in a block name!
    splitComponent = [splitComponent.slice(0, -1).join('_'), splitComponent.pop()];
  }
  var instance = splitComponent[0];
  var event=splitComponent[1];
  // Paul has a function to convert instance to type
  var componentType = Blockly.Component.instanceNameToTypeName(instance);
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_event');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('event_name', event);
  mutationElement.setAttribute('component_type',componentType);
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};
/**
 * v17_translateMethod is called when we know we have a component method element that
 * needs to be translated.
 */
Blockly.Versioning.v17_translateMethod = function(blockElem) {
  // the type attribute is "instance_method"
  var blockType = blockElem.getAttribute('type');
  // method block types look like: <block type="TinyDB_StoreValue" ...>
  var splitComponent = blockType.split('_');
  if (splitComponent.length > 2) {
    // This happens when someone puts an _ in a block name!
    splitComponent = [splitComponent.slice(0, -1).join('_'), splitComponent.pop()];
  }
  var instance = splitComponent[0];
  var method = splitComponent[1];
  // Paul has a function to convert instance to type
  var componentType = Blockly.Component.instanceNameToTypeName(instance);
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_method');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('method_name', method);
  mutationElement.setAttribute('component_type',componentType);
  mutationElement.setAttribute('is_generic','false');
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};
/**
 * v17_translateAnyMethod is called when we know we have a method on a generic (any)
 * component.
 */
Blockly.Versioning.v17_translateAnyMethod = function(blockElem) {
  // the type attribute is "instance_method"
  var blockType = blockElem.getAttribute('type');
  // any method block types look like: <block type="_any_ImageSprite_MoveTo" inline="false">
  var splitComponent = blockType.split('_');
  if (splitComponent.length > 3) {
    // This happens when someone puts an _ in a block name!
    var ctemp = splitComponent.slice(-2);
    splitComponent = [splitComponent.slice(0, -2).join('_'), ctemp.pop(), ctemp.pop()];
  }
  var componentType = splitComponent[2];
  var method=splitComponent[3];
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_method');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  mutationElement.setAttribute('method_name', method);
  mutationElement.setAttribute('component_type',componentType);
  mutationElement.setAttribute('is_generic','true');
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};
/**
 * v17_translateComponentGet is called when we know we have a component get, e.g.
 * TinyDB_component as the block
 */
Blockly.Versioning.v17_translateComponentGet = function(blockElem) {
  // the type attribute is "instance_method"
  var blockType = blockElem.getAttribute('type');
  // block type looks like: <block type="TinyDB1_component" ..> note an instance
  //    not a type as you'd expect
  var splitComponent = blockType.split('_');
  if (splitComponent.length > 2) {
    // This happens when someone puts an _ in a block name!
    splitComponent = [splitComponent.slice(0, -1).join('_'), splitComponent.pop()];
  }
  var instance = splitComponent[0];
  // if we got here splitComponent[1] must be "component"
  // Paul has a function to convert instance to type
  var componentType = Blockly.Component.instanceNameToTypeName(instance);
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_component_block');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = goog.dom.createElement('mutation');
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('component_type',componentType);
  blockElem.insertBefore(mutationElement,blockElem.firstChild);

};

/**
 * v17_translateSetGetProperty is called when we know we have a get or set on an instance
 */
Blockly.Versioning.v17_translateSetGetProperty = function(blockElem) {
  // the type attribute is "instance_setproperty" or "component_getproperty"
  var blockType = blockElem.getAttribute('type');
  // set block look like: <block type="Button1_setproperty" x="132" y="72">
  var splitComponent=blockType.split('_');
  if (splitComponent.length > 2) {
    // This happens when someone puts an _ in a block name!
    splitComponent = [splitComponent.slice(0, -1).join('_'), splitComponent.pop()];
  }
  var instance = splitComponent[0];
  var type=splitComponent[1]; //setproperty or getproperty
  // Paul has a function to convert instance to type
  var componentType = Blockly.Component.instanceNameToTypeName(instance);
  // grab titles to find the particular property. There is a title elem with
  //   a "PROP" attribute right under and within the block element itself
  //   There might be many titles, but we grab the first.
  var titles = blockElem.getElementsByTagName('title');
  var propName = 'unknown';
  for (var i = 0, len = titles.length; i < len; i++)
  {
    if (titles[i].getAttribute('name') == 'PROP') {
      propName = titles[i].textContent;
      break;
    }
  }
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_set_get');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = blockElem.getElementsByTagName('mutation')[0];
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('instance_name', instance);
  mutationElement.setAttribute('property_name', propName);
  if (type == 'setproperty') {
    mutationElement.setAttribute('set_or_get', 'set');
  } else {
    mutationElement.setAttribute('set_or_get','get');
  }
  mutationElement.setAttribute('component_type', componentType);
  mutationElement.setAttribute('is_generic','false');
  // old blocks had a 'yailtype' attribute in mutator, lets get rid of
  if (mutationElement.getAttribute('yailtype') != null)
    mutationElement.removeAttribute('yailtype');
};

/**
 * v17_translateComponentSetGetProperty is called when we know we have a get or set on a
 * generic component.
 */
Blockly.Versioning.v17_translateComponentSetGetProperty = function(blockElem) {
  // the type attribute is "component_setproperty" or "component_getproperty"
  //   where component is a type, e.g., Button
  var blockType = blockElem.getAttribute('type');
  // set block looks like: <block type="Button_setproperty" >
  var splitComponent = blockType.split('_');
  if (splitComponent.length > 2) {
    // This happens when someone puts an _ in a block name!
    splitComponent = [splitComponent.slice(0, -1).join('_'), splitComponent.pop()];
  }
  var type = splitComponent[1]; //setproperty or getproperty
  var componentType = splitComponent[0];
  // grab titles to find the particular property
  var titles = blockElem.getElementsByTagName('title');
  var propName = 'unknown';
  for (var i = 0, len = titles.length; i < len; i++)
  {
    if (titles[i].getAttribute('name') == 'PROP') {
      propName = titles[i].textContent;
      break;
    }
  }
  // ok, we have all the info, now we can override the old event attribute with 'event'
  blockElem.setAttribute('type','component_set_get');
  // <mutation component_type=​"Canvas" instance_name=​"Canvas1" event_name=​"Dragged">​</mutation>
  // add mutation tag
  var mutationElement = blockElem.getElementsByTagName('mutation')[0];
  //mutationElement.setAttribute('component_type',component);
  mutationElement.setAttribute('property_name', propName);
  if (type == 'setproperty') {
    mutationElement.setAttribute('set_or_get', 'set');
  } else {
    mutationElement.setAttribute('set_or_get','get');
  }
  mutationElement.setAttribute('component_type',componentType);
  mutationElement.setAttribute('is_generic','true');
  // old blocks had a 'yailtype' attribute in mutator, lets get rid of
  if (mutationElement.getAttribute('yailtype')!=null)
    mutationElement.removeAttribute('yailtype');
};

/******************************************************************************
 General helper methods for upgrades go in this section
 ******************************************************************************/

/**
 * @authors wolber@usfca.edu (David Wolber) & fturbak@wellesley.edu (Lyn Turbak)
 * changeEventParameterName changes any event handler parameter name. Note this code is
 * performed on Blockly blocks after xml has been loaded (when it's much easier
 * to correctly handle the subtleties of renaming involving lexical scoping).
 *
 * There are complications due to handling this renaming in the presence of i18n.
 * In particular, all references to the old localized parameter name within
 * the body must be renamed to new localized parameter names in a way that
 * avoids any accidental variable capture by local variables within the body.
 */
Blockly.Versioning.changeEventParameterName = function(componentType, eventName,
                                                       oldParamName, newParamName) {
  return function (blocksRep) {
    var mainWorkspace = Blockly.Versioning.ensureWorkspace(blocksRep);
    var blocks = mainWorkspace.getAllBlocks();
    for (var i = 0; i < blocks.length; i++) {
      var block = blocks[i];
      if (block.blockType === 'event') {
        if ((block.eventName === eventName) && (block.typeName === componentType)) {

          // Event params will have been saved previously in XML using oldParamName.
          // Find i18n translation of oldParamName within block
          var eventParamDict = Blockly.LexicalVariable.eventParameterDict(block);
          var oldParamTranslation = eventParamDict[oldParamName];
          if (oldParamTranslation) { // Is oldParamName referenced in event body?
            // If not, no further action is required.

            // For consistency, update .eventparam for blocks in which it's oldParamName to newParamName
            var descendants = block.getDescendants();
            for (var j = 0, descendant; descendant = descendants[j]; j++) {
              if (descendant.eventparam && (descendant.eventparam == oldParamName)) {
                descendant.eventparam = newParamName;
              }
            }

            // Find i18n translation of newParamName
            var newParamTranslation = window.parent.BlocklyPanel_getLocalizedParameterName(newParamName);

            // Event handler block will have been automatically created with newParamTranslation
            // So need to rename all occurrences of oldParamTranslation within its body
            // to newParamTranslation.
            var childBlocks = block.getChildren(); // should be at most one body block
            for (var k = 0, childBlock; childBlock = childBlocks[k]; k++) {
              var freeSubstitution = new Blockly.Substitution([oldParamTranslation], [newParamTranslation]);
              // renameFree does the translation.
              Blockly.LexicalVariable.renameFree(childBlock, freeSubstitution);
            }
          }
        }
      }
    }
    return mainWorkspace;
  }
};

/**
 * Rename all blocks with oldType to newType
 * @param oldBlockType: string name of old block type
 * @param newBlockType: string name of new block type
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */

Blockly.Versioning.renameBlockType = function(oldBlockType, newBlockType) {
  return function (blocksRep) {
    var dom = Blockly.Versioning.ensureDom(blocksRep);
    var allBlocks = dom.getElementsByTagName('block');
    for (var i = 0, im = allBlocks.length; i < im; i++) {
      var blockElem = allBlocks[i];
      var blockType = blockElem.getAttribute('type');
      if (blockType == oldBlockType) {
        blockElem.setAttribute('type', newBlockType);
      }
    }
    return dom; // Return the modified dom, as required by the upgrading structure.
  }
}

/**
 * @param componentType: name of component type for method
 * @param methodName: name of method
 * @argumentIndex: index of the default argument block
 * @defaultXMLArgumentBlockText: string with XML for argument block
 * @returns a function that maps a blocksRep (An XML DOM or workspace)
 *   to a modified DOM in which the default argument block has been added to every specified method call.
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 *
 */
Blockly.Versioning.addDefaultMethodArgument = function(componentType, methodName, argumentIndex, defaultXMLArgumentBlockText) {
  return function (blocksRep) {
    var dom = Blockly.Versioning.ensureDom(blocksRep);
    // For each matching method call block, change it to have new argument block for value child ARG<argumentIndex>
    var methodCallBlocks =  Blockly.Versioning.findAllMethodCalls(dom, componentType, methodName);
    for (var b = 0, methodCallBlock; methodCallBlock = methodCallBlocks[b]; b++) {
      var childBlocks = goog.dom.getChildren(methodCallBlock);
      var insertionChild = null; // Value with name ARG<N> we want to insert default before
      for (var c = 0, child; child = childBlocks[c]; c++) {
        if (child.tagName == "VALUE") {
          var name = child.getAttribute("name");
          if (name.indexOf("ARG") == 0) {
            var index = parseInt(name.split("ARG")[1]);
            if (index == argumentIndex) {
              // There is already an argument block at the given index.
              // Don't replace an existing block, but use console.log to report this situation.
              console.log("Already a child block at index  " + argumentIndex
                          + " in Blockly.Versioning.addDefaultMethodArgument(" + componentType
                          + ", " + methodName,
                          + ", " + argumentIndex,
                          + ", " + defaultXMLArgumentBlockText + ")");
            } else if (index > argumentIndex) {
              insertionChild = child;
              break; // exit loop once we find first bigger argument element
            }
          }
        }
      }
      // Create the new argument block for this method call
      // (careful: can't share one dom element across multiple calls!)
      var argumentElement = goog.dom.createElement('value');
      argumentElement.setAttribute('name', 'ARG' + argumentIndex);
      var argumentChild = Blockly.Versioning.xmlBlockTextToDom(defaultXMLArgumentBlockText);
      argumentElement.insertBefore(argumentChild, null); // The first and only child (a block)
      // Insert the new argument block
      methodCallBlock.insertBefore(argumentElement, insertionChild);
    }
    return dom; // Return the modified dom, as required by the upgrading structure.
  }
}

/**
 * Rename all method call blocks for a given component type and method name.
 * @param componentType: name of component type for method
 * @param oldMethodName: name of method
 * @param newMethodName: new name of method
 * @returns a function that maps a blocksRep (An XML DOM or workspace)
 *   to a modified DOM in which every specified method call has been renamed.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
Blockly.Versioning.changeMethodName = function(componentType, oldMethodName, newMethodName) {
  return function (blocksRep) {
    var dom = Blockly.Versioning.ensureDom(blocksRep);
    // For each matching method call block, change the method_name attribute.
    var methodCallBlocks =  Blockly.Versioning.findAllMethodCalls(dom, componentType, oldMethodName);
    for (var b = 0, methodCallBlock; methodCallBlock = methodCallBlocks[b]; b++) {
      var mutation = Blockly.Versioning.firstChildWithTagName(methodCallBlock, "mutation");
      mutation.setAttribute("method_name", newMethodName);
    }
    return dom; // Return the modified dom, as required by the upgrading structure.
  }
}

/**
 * Rename all property get/set blocks for a given component type and property name.
 * @param componentType: name of component type for property
 * @param oldPropertyName: name of property
 * @param newPropertyName: new name of property
 * @returns a function that maps a blocksRep (An XML DOM or workspace)
 *   to a modified DOM in which every specified property get/set has been renamed.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
Blockly.Versioning.changePropertyName = function(componentType, oldPropertyName, newPropertyName) {
  return function (blocksRep) {
    var dom = Blockly.Versioning.ensureDom(blocksRep);
    // For each matching property block, change the property_name attribute.
    var propertyBlocks =  Blockly.Versioning.findAllPropertyBlocks(dom, componentType, oldPropertyName);
    for (var b = 0, propertyBlock; propertyBlock = propertyBlocks[b]; b++) {
      var mutation = Blockly.Versioning.firstChildWithTagName(propertyBlock, "mutation");
      mutation.setAttribute("property_name", newPropertyName);
      var children = goog.dom.getChildren(propertyBlock);
      for (var c = 0, child; child = children[c]; c++) {
        if (child.tagName.toUpperCase() == "FIELD") {
          if (child.getAttribute("name") == "PROP") {
            if (child.textContent == oldPropertyName) {
              child.textContent = newPropertyName;
            }
          }
        }
      }
    }
    return dom; // Return the modified dom, as required by the upgrading structure.
  }
}

/**
 * @param dom: DOM for XML workspace
 * @param componentType: name of component type for method
 * @param methodName: name of method
 * @returns a list of HTML elements for the specfied method call blocks.
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 *
 */
Blockly.Versioning.findAllMethodCalls = function (dom, componentType, methodName) {
  var allBlocks = dom.getElementsByTagName('block');
  var callBlocks = [];
  for (var b = 0, block; block = allBlocks[b]; b++)  {
    if (block.getAttribute('type') == "component_method") {
      var mutation = Blockly.Versioning.firstChildWithTagName(block, "mutation");
      if (!mutation) {
        throw "Did not find expected mutation child in "
              + "Blockly.Versioning.findAllMethodCalls with componentType = " + componentType
              + "and methodName = " + methodName;
      } else {
        if ((mutation.getAttribute("component_type") == componentType)
            && (mutation.getAttribute("method_name") == methodName)) {
          callBlocks.push(block);
        }
      }
    }
  }
  return callBlocks;
}

/**
 * @param dom: DOM for XML workspace
 * @param componentType: name of component type for property
 * @param propertyName: name of property
 * @returns a list of HTML elements for the specfied property blocks.
 *
 * @author lizlooney@google.com (Liz Looney)
 *
 */
Blockly.Versioning.findAllPropertyBlocks = function (dom, componentType, propertyName) {
  var allBlocks = dom.getElementsByTagName('block');
  var propertyBlocks = [];
  for (var b = 0, block; block = allBlocks[b]; b++)  {
    if (block.getAttribute('type') == "component_set_get") {
      var mutation = Blockly.Versioning.firstChildWithTagName(block, "mutation");
      if (!mutation) {
        throw "Did not find expected mutation child in "
              + "Blockly.Versioning.findAllPropertyBlocks with componentType = " + componentType
              + "and propertyName = " + propertyName;
      } else {
        if ((mutation.getAttribute("component_type") == componentType)
            && (mutation.getAttribute("property_name") == propertyName)) {
          propertyBlocks.push(block);
        }
      }
    }
  }
  return propertyBlocks;
}

/**
 * @param elem: an HTML element
 * @param tag: string thats a tag name
 * @returns the first child of elem with the given tag name (case insensitive)
 *  or null if there is no such element.
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 *
 */
Blockly.Versioning.firstChildWithTagName = function (elem, tag) {
  var upcaseTag = tag.toUpperCase();
  var children = goog.dom.getChildren(elem);
  for (var c = 0, child; child = children[c]; c++) {
    if (child.tagName.toUpperCase() == upcaseTag) {
      return child;
    }
  }
  return null;
}

/**
 * @param xmlBlockText: string specifying the XML for a single block
 * @returns HTML element for the specified block
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 *
 */
Blockly.Versioning.xmlBlockTextToDom = function(xmlBlockText) {
  // To make Blockly.Xml.textToDom happy, must provide it with top-level XML tag
  var topLevelXmlString = "<xml>" + xmlBlockText + "</xml>";
  var topLevelDom = Blockly.Xml.textToDom(topLevelXmlString);
  // Now extract single block dom from top-level dom
  var children = goog.dom.getChildren(topLevelDom);
  if (children.length != 1) {
    throw "Unexpected number of childred in Blockly.Versioning.xmlBlockTextToDom: "
          + children.length;
  } else {
    return children[0];
  }
}

/******************************************************************************
 Define component upgrade maps here.
 ******************************************************************************/

/**
 * @author fturbak@wellesley.edu (Lyn Turbak)
 *
 * This is a dictionary of upgrade maps, one map per component type plus
 * one map for the special string "Language" (for upgrading blocks language version)
 * The component type "Form" is handled as "Screen".
 *
 * This dictionary expresses in a more declarative form the updating conditionals
 * in Liz Looney's BlockSave.java file from App Inventor Classic.
 *
 * In the upgrade map for a particular component type, an entry labeled with version number n
 * specifies what needs to be done to upgrade components at version n-1 to version n.
 * Each entry maps to either:
 *   (1) an upgrading function,
 *   (2) an array of upgraders,
 *   (3) a special upgrading string.
 *
 * An upgrading function is a single-argument function that takes and returns a
 * blocks program representation, which is either:
 *   (1) a top-level XML DOM element ,or
 *   (2) a Blockly.Workspace object
 * Modifications (by side effect) can be made to whichever of these representations is more convenient.
 * The utility functions Blockly.Versioning.ensureDom and Blockly.Versioning.ensureWorkspace
 * can be used to ensure that the representation is of a certain type, converting it
 * if it is not of that type. The framework allows the representation to ping-pong
 * back and forth between these representation types, and guaranteees that the
 * final representation is the workspace expected by Blockly.
 *
 * A list of upgraders is treated as the sequential composition of the functions
 * for each upgrader.
 *
 * The two special strings for blocks upgraders are:
 *
 *   "noUpgrade": No upgrade needs to be performed.
 *
 *   "ai1CantDoUpgrade": There was a nontrivial upgrader for AI1 in BlockSaveFile.java,
 *      but this upgrader cannot be replicated in AI2.
 *
 * Each upgrader is preceded by a comment that describes the upgrade.
 * For historical purposes, the upgrades from App Inventor Classic (from BlockSaveFile.java)
 * are documented here, even though the nontrival upgrades can no longer be performed.
 *
 */

Blockly.Versioning.AllUpgradeMaps =
{

  "AccelerometerSensor": {

    // AI1: The AccelerometerSensor.MinimumInterval property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI2: AccelerometerSensor.Sensitivty property was added.
    3: "noUpgrade"

  }, // End Accelerometer upgraders

  "ActivityStarter": {

    // AI1: The ActivityStarter.DataType, ActivityStarter.ResultType, and ActivityStarter.ResultUri
    // properties were added.
    // The ActivityStarter.ResolveActivity method was added.
    // The ActivityStarter.ActivityError event was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The ActivityStarter.ActivityError event was marked userVisible false and is no longer
    // used.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "ActivityStarter", "ActivityError")) {
        markBlockBad(block, "The ActivityStarter.ActivityError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The ActivityStarter.StartActivity method was modified to pull the parent Form's
    // screen animation type. No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI2: The ActivityStarter.ActivityCanceled event was added.
    // No blocks need to be modified to upgrade to version 5.
    5: "noUpgrade",

    // Extras property was added
    6: "noUpgrade"

  }, // End ActivityStarter upgraders

  "Ball": {

    // AI1: The PointTowards method was added (for all sprites).
    // The Heading property was changed from int to double (for all srites).
    // No blocks need to be modified to upgrade to version 2.
    // Blocks related to this component have now been upgraded to version 2.
    2: "noUpgrade",

    // AI1: The Z property was added (also for ImageSprite)
    3: "noUpgrade",

    // AI1: The TouchUp, TouchDown, and Flung events were added. (for all sprites)
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI1: speed and hearing were added to the Flung event (for all sprites)
    // speed and heading were added to the Flung event
    /* From BlockSaveFile.java:
          final String CHANGED_FLUNG_WARNING = "The %s block has been changed to " +
          "include speed and heading. Please change your program " +
          "by deleting this old version of the block and pick a new Flung block" +
          "from the drawer";
      for (Element block : getAllMatchingGenusBlocks("Ball-Flung")) {
        markBlockBad(block, String.format(CHANGED_FLUNG_WARNING, "Flung"));
    */
    5: "ai1CantDoUpgrade" // Just indicates we couldn't do upgrade even if we wanted to

  }, // End Ball upgraders

  "BarcodeScanner": {

    // AI1: No changes required
    // The UseExternalScanner property was added.
    2: "noUpgrade"

  }, // End BarcodeScanner upgraders

  "BluetoothClient": {

    // AI1: The BluetoothClient.Enabled property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The BluetoothClient.BluetoothError event was marked userVisible false and is no longer
    // used.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "BluetoothClient", "BluetoothError")) {
        markBlockBad(block, "The BluetoothClient.BluetoothError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // The BluetoothClient.DelimiterByte property was added.
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // The BluetoothClient.Secure property was added.
    // No blocks need to be modified to upgrade to version 5.
    5: "noUpgrade"

  }, // End BluetoothClient upgraders

  "BluetoothServer": {

    // AI1: The BluetoothServer.Enabled property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The BluetoothServer.BluetoothError event was marked userVisible false and is no longer
    // used.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "BluetoothServer", "BluetoothError")) {
        markBlockBad(block, "The BluetoothServer.BluetoothError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // A11: The BluetoothServer.DelimiterByte property was added.
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // The BluetoothServer.Secure property was added.
    // No blocks need to be modified to upgrade to version 5.
    5: "noUpgrade"

  }, // End BluetoothServer upgraders

  "Button": {

    // A1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
          handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The LongClick event was added.
    // No blocks need to be modified to upgrade to version 3.
    3: "noUpgrade",

    // AI1: The Shape property was added.
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI1: The ShowFeedback property was added.
    // No properties need to be modified to upgrade to version 5.
    5: "noUpgrade",

    // AI2: Added TouchUp and TouchDown events;
    // FontSize, FontBold, FontItalic properties made visible in block editor
    6: "noUpgrade"

  }, // End BarcodeScanner upgraders

  "Camcorder": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Camcorder upgraders

  "Camera": {

    // AI2: The UseFront property was added.
    2: "noUpgrade",

    // AI2: The UseFront property was removed
    3: "noUpgrade"

  }, // End Camera upgraders

  "Canvas": {

    // AI1: The LineWidth property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The FontSize and TextAlignment properties and
    // the DrawText and DrawTextAtAngle methods were added.
    // No blocks need to be modified to upgrade to version 3.
    3: "noUpgrade",

    // AI1: No blocks need to be modified to upgrade to version 4.
    // The Save and SaveAs methods were added.
    4: "noUpgrade",

    // AI1: No blocks need to be modified to upgrade to version 5.
    // The GetBackgroundPixelColor, GetPixelColor, and
    // SetBackgroundPixelColor methods were added.
    5: "noUpgrade",

    // AI1: No blocks need to be modified to upgrade to version 6.
    // The TouchUp and TouchDown events were added.
    6: "noUpgrade",

    // AI1: speed and heading were added to the Flung event
    // Previous instances of the Flung event were marked bad.
    /* From BlockSaveFile.java:
      final String CHANGED_FLUNG_WARNING = "The %s block has been changed to " +
          "include speed and heading. Please change your program " +
          "by deleting this old version of the block and pick a new Flung block" +
          "from the drawer";
      for (Element block : getAllMatchingGenusBlocks("Canvas-Flung")) {
        markBlockBad(block, String.format(CHANGED_FLUNG_WARNING, "Flung"));
      }
    */
    7: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI2: Dave Wolber's canvas naming changes
    // * In Dragged event, parameter draggedSprite renamed to draggedAnySprite
    // * In Touched event, parameter touchedSprite renamed to touchedAnySprite
    // * In Canvas.DrawCircle method, parameters x, y, and r changed to
    //   xCenter, yCemter, and radius, respectively.
    // The method parameter changes require no upgrade, but the event parameter
    //   changes require consistent renaming of references to those parameters.
    8:
       [ // Blocks upgrader for Canvas version is the sequential composition of these two upgraders:
          Blockly.Versioning.changeEventParameterName("Canvas","Dragged","draggedSprite","draggedAnySprite"),
          Blockly.Versioning.changeEventParameterName("Canvas","Touched","touchedSprite","touchedAnySprite")
       ],

    9: Blockly.Versioning.addDefaultMethodArgument("Canvas", "DrawCircle", 3, // Since this will be ARG3
        '<block type="logic_boolean">' +
        '  <field name="BOOL">TRUE</field>' +
        '</block>'),

    // AI2: No blocks need to be modified to upgrade to version 10
    // The default value of TextAlignment was changed from Normal (left) to Center
    10: "noUpgrade"

  }, // End Canvas upgraders

  "CheckBox": {

    // AI2: The Value property was renamed to Checked.
    2: "noUpgrade"

  }, // End CheckBox upgraders

  "Clock": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade",

    // AI2: The patterm pattermeter was added to FormatDate and FormatDateTime.
    // * FormatDate(instant) to FormatDate(instant, pattern)
    // * FormatDateTime(instant) to FormatDateTime(instant, pattern)
    2:
      [  // Set the default argument for parameter to be an empty string.
         Blockly.Versioning.addDefaultMethodArgument("Clock", "FormatDateTime", 1,
         '<block type="text">' +
         '  <field name="TEXT">MMM d, yyyy HH:mm:ss a</field>' +
         '</block>'),
         Blockly.Versioning.addDefaultMethodArgument("Clock", "FormatDate", 1,
         '<block type="text">' +
         '  <field name="TEXT">MMM d, yyyy</field>' +
         '</block>')
      ],

    // Duration Support was added.
    3: "noUpgrade"

  }, // End Clock upgraders

  "ContactPicker": {

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The Open method was added, which does not require changes.
    3: "noUpgrade",

    // AI1: The Shape property was added.
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI2:  Added PhoneNumber, PhoneNumberList, and EmailAddressList to ContactPicker.
    // - For Eclair and up, we now use ContactsContract instead of the deprecated Contacts.
    5: "noUpgrade",

    // AI2:  Added ContactUri
    6: "noUpgrade"

  }, // End ContactPicker upgraders


  "DatePicker": {

    // AI2: The datepicker dialog was updated to show the current date
    // instead of the last set date by default.
    // The SetDateToDisplay and LaunchPicker methods were added to
    // give the user more control of what time is displayed in the
    // datepicker dialog.
    2: "noUpgrade",

    // AI2: SetDateToDisplayFromInstant method and Instant property are added.
    3: "noUpgrade"

  }, // End DatePicker upgraders

  "EmailPicker": {

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // RequestFocus was added
    3: "noUpgrade"

  }, // End EmailPicker upgraders

  "File": {

    // AI2: The AfterFileSaved event was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade"

  }, // End File upgraders

  // Form is renamed to Screen. See below.

  "FirebaseDB": {

    1: "noUpgrade",
    // AI2 Added AppendValue, RemoveFirst and FirstRemoved
    2: "noUpgrade",
    // AI2 Added ClearTag function, GetTagList and Persist
    3: "noUpgrade"

  },

  "FusiontablesControl": {

    // AI1: No changes required
    // The ApiKey property and the SendQuery and ForgetLogin methods were added.
    2: "noUpgrade",

    // AI2: - InsertRow, GetRows and GetRowsWithConditions was added.
    // - KeyFile, UseServiceAuthentication and ServiceAccountEmail
    //   were added.
    3: "noUpgrade"

  }, // End FusiontablesControl upgraders

  "GameClient": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End GameClient upgraders

  "GyroscopeSensor": {

    // This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End GyroscopeSensor upgraders

  "HorizontalArrangement": {

    // AI1: The AlignHorizontal and AlignVertical properties were added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // - Added background color & image
    3: "noUpgrade"

  }, // End HorizontalArrangement upgraders

  "HorizontalScrollArrangement": {

    // This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End HorizontalScrollArrangement upgraders

  "Image": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade",

    // AI2: The RotationAngle property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // Scaling property was added (but not in use yet)
    3: "noUpgrade"

  }, // End Image upgraders

  "ImagePicker": {

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The Open method was added, which does not require changes.
    3: "noUpgrade",

    // AI1: The Shape property was added.
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI1: The ImagePath property was renamed to Selection.
    // Blocks related to this component have now been upgraded to version 5.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "ImagePath", "Selection");
    */
    5: "noUpgrade"

  }, // End ImagePicker upgraders

  "ImageSprite": {

    // AI1: The ImageSprite.Rotates property was added
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The PointTowards method was added.
    // The Heading property was changed from int to double
    // No blocks need to be modified to upgrade to version 3.
    3: "noUpgrade",

    // AI1: The Z property was added (also for Ball)
    4: "noUpgrade",

    // AI1: The TouchUp, TouchDown, and Flung events were added. (for all sprites)
    // No blocks need to be modified to upgrade to version 5.
    5: "noUpgrade",

    // AI1: speed and hearing were added to the Flung event (for all sprites)
    // speed and heading were added to the Flung event
    /* From BlockSaveFile.java:
        final String CHANGED_FLUNG_WARNING = "The %s block has been changed to " +
            "include speed and heading. Please change your program " +
            "by deleting this old version of the block and pick a new Flung block" +
            "from the drawer";
        for (Element block : getAllMatchingGenusBlocks("ImageSprite-Flung")) {
          markBlockBad(block, String.format(CHANGED_FLUNG_WARNING, "Flung"));
        }
    */
    6: "ai1CantDoUpgrade" // Just indicates we couldn't do upgrade even if we wanted to

  }, // End ImageSprite upgraders

  "Label": {

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI2: For LABEL_COMPONENT_VERSION 3:
    // - The HasMargins property was added
    3: "noUpgrade",

    // AI2: Add HTMLFormat property
    4: "noUpgrade"

  }, // End Label upgraders

  // Special upgrading map for language version.
  // All dictionary keys except this one are component names.
  "Language": {

    // AI1: In BLOCKS_LANGUAGE_VERSION 2, we allow arguments of different procedures and events
    // to have the same names.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: In BLOCKS_LANGUAGE_VERSION 3, we added some string operations
    // No blocks need to be modified to upgrade to version 3.
    3: "noUpgrade",

    // AI1: In BLOCKS_LANGUAGE_VERSION 4, we added replace all, copy list,
    // insert list item, for range
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI1: In BLOCKS_LANGUAGE_VERSION 5, we changed some Math functions' formal parameter names.
    /* From BlockSaveFile.java:
        upgradeTrigBlocks();
        // Blocks have now been upgraded to language version 5.
    */
    5: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: Beginning in BLOCKS_LANGUAGE_VERSION 6, text blocks, comments, and complaints
    // are encoded on save and decoded on load to preserve international characters.
    /* From BlockSaveFile.java:
        encodeInternationalCharacters();
        // Blocks have now been upgraded to language version 6.
    */
    6: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: In BLOCKS_LANGUAGE_VERSION 7, corrupted character sequences in comments are replaced
    // with * when .blk files are upgraded.
    /* From BlockSaveFile.java:
        fixCorruptedComments();
        // Blocks have now been upgraded to language version 7.
    */
    7: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: In BLOCKS_LANGUAGE_VERSION 8, socket labels of some text blocks were changed.
    /* From BlockSaveFile.java:
        upgradeTextBlockSocketLabels();
        // Blocks have now been upgraded to language version 8.
    */
    8: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: In BLOCKS_LANGUAGE_VERSION 9, radian conversion blocks were fixed
    /* From BlockSaveFile.java:
        fixRadiansConversionBlocks();
        // Blocks have now been upgraded to language version 9.
    */
    9: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: In BLOCKS_LANGUAGE_VERSION 10, "as" label was added to def blocks
    /* From BlockSaveFile.java:
        addAsLabelToDefBlocks();
        // Blocks have now been upgraded to language version 10.
    */
    10: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: In BLOCKS_LANGUAGE_VERSION 11, we added csv-related list operations.
    // No blocks need to be modified to upgrade to version 11.
    11: "noUpgrade",

    // In BLOCKS_LANGUAGE_VERSION 12, we changed the multiply
    // symbol from * star to times, and the subtract symbol
    // from hyphen to minus
    /* From BlockSaveFile.java:
        changeStarAndHyphenToTimesAndMinusForMultiplyAndSubtractBlocks();
        // Blocks have now been upgraded to language version 12.
    */
    12: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: In BLOCKS_LANGUAGE_VERSION 13, we added open-screen and open-screen-with-start-text.
    // No blocks need to be modified to upgrade to version 13.
    13: "noUpgrade",

    // AI1: In BLOCKS_LANGUAGE_VERSION 14, we added property and method blocks for component objects.
    // No language blocks need to be modified to upgrade to version 14.
    14: "noUpgrade",

    // AI1: In BLOCKS_LANGUAGE_VERSION 15, we added "is text empty?" to
    // Text drawer.
    // No language blocks need to be modified to upgrade to version 15.
    15: "noUpgrade",

    // AI1: In BLOCKS_LANGUAGE_VERSION 16, we added make-color and split-color to the Color drawer.
    // No language blocks need to be modified to upgrade to version 16.
    16: "noUpgrade",

    // AI1: In BLOCKS_LANGUAGE_VERSION 17.
    // Changed open-screen to open-another-screen
    // Changed open-screen-with-start-text to open-another-screen-with-start-value
    // Marked get-startup-text as a bad block
    // Added get-start-value
    // Added get-plain-start-text
    // Marked close-screen-with-result as a bad block
    // Added close-screen-with-value
    // Added close-screen-with-plain-text
    17: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI2: Jeff Schiller's new Obfuscate Text block added
    18: "noUpgrade",

    // AI2: In BLOCKS_LANGUAGE_VERSION 19
    // is-number?, was extended with a dropdown to include base10, bin, and hex
    // The existing block from an old project apparently does not need to be modified to
    // see these new options.  (Hal is not sure why not, but it seems to work.)
    // The math convert block was added
    // No language blocks need to be modified to upgrade to version 16.
    19: "noUpgrade",


    // AI2: In BLOCKS_LANGUAGE_VERSION 20// Rename 'obsufcated_text' text block to 'obfuscated_text'
    20: Blockly.Versioning.renameBlockType('obsufcated_text', 'obfuscated_text')


  }, // End Language upgraders


  "ListPicker": {

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The SelectionIndex property was added, which does not require changes.
    3: "noUpgrade",

    // AI1: The Open method was added, which does not require changes.
    4: "noUpgrade",

    // AI1: The Shape property was added.
    // No blocks need to be modified to upgrade to version 5.
    5: "noUpgrade",

    // AI1: The getIntent method was modified to add the parent Form's screen
    // animation type. No blocks need to be modified to upgrade to version 6.
    6: "noUpgrade",

    // AI2: Added ShowFilterBar property
    7: "noUpgrade",

    // AI2: Added title property
    8: "noUpgrade",

    // AI2: Added  ItemTextColor and ItemBackgroundColor
    9: "noUpgrade"

  }, // End ListPicker upgraders

  "ListView": {

    // AI2: Added Elements property
    2: "noUpgrade",

    // AI2:
    // - Added BackgroundColor Property
    // - Added TextColor Property
    3: "noUpgrade",
    // AI2:
    // - Added TextSize Property
    4: "noUpgrade",
    // AI2:
    // - Added SelectionColor Property
    5: "noUpgrade"

  }, // End ListView upgraders

  "LocationSensor": {

    // AI1: The TimeInterval and DistanceInterval properties were added.
    // No changes required.
    2: "noUpgrade",
    // AI2:
    // The speed parameter to the LocationChanged event
    3: "noUpgrade"

  }, // End LocationSensor upgraders

  "NearField": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NearField upgraders

  "Notifier": {

    // AI1:
    /* From BlockSaveFile.java:
      final String NEW_ARG_WARNING = "The %s block has been changed to " +
          "expect a new cancelable argument. Please replace this block by a new one from the Notifier drawer.";

    if (blkCompVersion < 2) {
        // Look for ShowChooseDialog method block for this component.
        for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Notifier",
          "ShowChooseDialog")) {
          // Mark the block bad.
          markBlockBad(block,NEW_ARG_WARNING );
      }

      // Look for ShowTextDialog method block for this component.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Notifier",
          "ShowTextDialog")) {
          // Mark the block bad.
          markBlockBad(block,NEW_ARG_WARNING );
      }
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI2: Added NotifierColor, TextColor and NotifierLength options
    3: "noUpgrade",

    // Added a ProgressDialog, a dialog that cannot be dismissed by the user.
    // The ShowProgressDialog will show the dialog, and DismissProgressDialog is the only way to dismiss it
    4: "noUpgrade"

  }, // End Notifier upgraders

  "NxtColorSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NxtColorSensor upgraders

  "NxtDirectCommands": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NxtDirectCommands upgraders

  "NxtDrive": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NxtDrive upgraders

  "NxtLightSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NxtLightSensor upgraders

  "NxtSoundSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NxtSoundSensor upgraders

  "NxtTouchSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NxtTouchSensor upgraders

  "NxtUltrasonicSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End NxtUltrasonicSensor upgraders

  "Ev3Motors": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3Motors upgraders

  "Ev3ColorSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3ColorSensor upgraders

  "Ev3GyroSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3GyroSensor upgraders

  "Ev3TouchSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3TouchSensor upgraders

  "Ev3UltrasonicSensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3UltrasonicSensor upgraders

  "Ev3Sound": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3Sound upgraders

  "Ev3UI": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3UI upgraders

  "Ev3Commands": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Ev3Commands upgraders

  "OrientationSensor": {

    // AI1: The Yaw property was renamed to Azimuth.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Yaw", "Azimuth");
      // The yaw parameter to OrientationChanged was renamed to azimuth.
      for (Element block : getAllMatchingMethodOrEventBlocks(
               componentName, "OrientationSensor", "OrientationChanged")) {
        changeFirstMatchingSocketBlockConnectorLabel(block, "yaw", "azimuth");
      }
    */
    2: "ai1CantDoUpgrade" // Just indicates we couldn't do upgrade even if we wanted to

  }, // End OrientationSensor upgraders

  "PasswordTextBox": {

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // RequestFocus was added
    3: "noUpgrade"

  }, // End PasswordTextBox upgraders

  "Pedometer": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade",

    // AI2: The step sensing algorithm was updated to be more accurate.
    // The GPS related functionality was removed.
    2: "noUpgrade"

  }, // End PhoneCall upgraders

  "PhoneCall": {

    // AI2: - The PhoneCallStarted event was added.
    // - The PhoneCallEnded event was added.
    // - The IncomingCallAnswered event was added.
    2: "noUpgrade"

  }, // End PhoneCall upgraders

  "PhoneNumberPicker": {

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The Open method was added, which does not require changes.
    3: "noUpgrade",

    // AI1: The Shape property was added.
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade"

  }, // End PhoneNumberPicker upgraders

  "Player": {

    // AI1: The Player.PlayerError event was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The Player.PlayerError event was marked userVisible false and is no longer used.
    /* From BlockSaveFile.java:
        for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Player", "PlayerError")) {
        markBlockBad(block, "The Player.PlayerError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The Looping and Volume properties were added.
    // The Completed Event was added.
    // The IsPlaying method was added.
    // No properties need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI1: The IsLooping method was renamed to Loop.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "IsLooping", "Loop");
    */
    5: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI2: - The PlayInForeground property was added.
    // - The OtherPlayerStarted event was added.
    6: "noUpgrade"

  }, // End Player upgraders

  "ProximitySensor": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End ProximitySensor upgraders

  // Screen is renamed from Form
  "Screen": {

    // AI1: The Screen.Scrollable property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // The Screen.Icon property was added.
    // No blocks need to be modified to upgrade to version 3.
    3: "noUpgrade",

    // AI1: The Screen.ErrorOccurred event was added.
    // No blocks need to be modified to upgrade to version 4.
    4: "noUpgrade",

    // AI1: The Screen.ScreenOrientation property and Screen.ScreenOrientationChanged event were
    // added.
    // No blocks need to be modified to upgrade to version 5.
    5: "noUpgrade",

    // AI1: The SwitchForm and SwitchFormWithArgs methods were removed and the OtherScreenClosed event
    // was added.
    6: "noUpgrade",

    // AI1: The VersionCode and VersionName properties were added. No blocks need to be modified
    // to update to version 7.
    7: "noUpgrade",

    // AI1: The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
    // to upgrade to version 8.
    8: "noUpgrade",

    // AI1: The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
    // to upgrade to version 8.
    9: "noUpgrade",

    // AI1: The BackPressed event was added. No blocks need to be modified to upgrade to version 10.
    10: "noUpgrade",

    // *** AI2: NEED TO ACTUALLY HANDLE THIS!**
    /*
    if (blkCompVersion < 11) {
      final String CHANGED_SCREENANIMATIONS_WARNING =
        "The %s block has been changed to a property. Please change your program " +
        "by deleting this old version of the block and pick a new Property Changing block.";
      for (Element block : getAllMatchingGenusBlocks("Screen-OpenScreenAnimation")) {
        changeBlockGenusName(block, "FusiontablesControl-DoQuery"); // HACK!!!! We need a block that is "like" we once were
        markBlockBad(block, String.format(CHANGED_SCREENANIMATIONS_WARNING, "OpenScreenAnimation"));
      }
      for (Element block : getAllMatchingGenusBlocks("Screen-CloseScreenAnimation")) {
        changeBlockGenusName(block, "FusiontablesControl-DoQuery"); // HACK!!!
        markBlockBad(block, String.format(CHANGED_SCREENANIMATIONS_WARNING, "CloseScreenAnimation"));
      }
      blkCompVersion = 11;
    }
    */
    11: "noUpgrade",

    // For FORM_COMPONENT_VERSION 12:
    // - AboutScreen property was added
    12: "noUpgrade",

    // For FORM_COMPONENT_VERSION 13:
    // - The Screen.Scrollable property was set to False by default
    13: "noUpgrade",

    // For FORM_COMPONENT_VERSION 14:
    // - The Screen1.AppName was added and no block need to be changed.
    14: "noUpgrade",

    // For FORM_COMPONENT_VERSION 15:
    // - The Screen1.ShowStatusBar was added and no block needs to be changed.
    15: "noUpgrade",

    // For FORM_COMPONENT_VERSION 16:
    // - The Screen1.TitleVisible was added and no block needs to be changed.
    16: "noUpgrade",

    // For FORM_COMPONENT_VERSION 17:
    // - Screen.CompatibilityMode property was added no block needs to be changed.
    17: "noUpgrade",

    // FOR FORM_COMPONENT_VERSION 18:
    // Screen.CompatibililtyMode replaced with Screen.Sizing no blocks need to be
    // changed.
    18: "noUpgrade",

    // For FORM_COMPONENT_VERSION 19:
    // - The Screen1.HideKeyboard method was added and no block needs to be changed.
    19: "noUpgrade",

    // For FORM_COMPONENT_VERSION 20:
    // - The Screen1.ShowListsAsJson property was added and no block needs to be changed.
    20: "noUpgrade"


  }, // End Screen

  "Sharing": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Sharing upgraders

  "Slider": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade",

    // Added the property to allow for the removal of the Thumb Slider
    2: "noUpgrade"

  }, // End Slider upgraders

  "Sound": {

    // AI1: The Sound.SoundError event was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The Sound.SoundError event was marked userVisible false and is no longer used.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Sound", "SoundError")) {
        markBlockBad(block, "The Sound.SoundError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
    */
    3: "ai1CantDoUpgrade" // Just indicates we couldn't do upgrade even if we wanted to

  }, // End Sound upgraders

  "SoundRecorder": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade",
    // AI2: The Sound.SavedRecording property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade"

  }, // End SoundRecorder upgraders

  "SpeechRecognizer": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End SpeechRecognizer upgraders

  "Spinner": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Spinner upgraders

  "TableArrangement": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End TableArrangementupgraders

  "TextBox": {

    // AI1: The TextBox.NumbersOnly property was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: The TextBox.HideKeyboard method was added
    // The MultiLine property was added,
    // No blocks need to be modified to upgrade to version 4, although old
    // block need to have MultiLine explicitly set to true, since the new default
    // is false (see YoungAndroidFormUpgrade).
    4: "noUpgrade",

    // AI2: Added RequestFocus method
    5: "noUpgrade"

  }, // End TextBox upgraders

  "Texting": {

    // AI1: No changes required
    // The GoogleVoiceEnabled property was added.
    2: "noUpgrade",

    // AI1: The Alignment property was renamed to TextAlignment.
    // Blocks related to this component have now been upgraded to version 2.
    /* From BlockSaveFile.java:
      handlePropertyTypeChange(componentName, "ReceivingEnabled", "receivingEnabled is now an integer in the range 1-3 instead of a boolean");
    */
    3: "ai1CantDoUpgrade" // Just indicates we couldn't do upgrade even if we wanted to

  }, // End Texting

  "TextToSpeech": {

    // AI2:  added speech pitch and rate
    2: "noUpgrade",

    // the AvailableLanguages property was added
    // the AvailableCountries property was added
    3: "noUpgrade",

    // the Country designer property was changed to use a ChoicePropertyEditor
    // the Language designer property was changed to use a ChoicePropertyEditor
    4: "noUpgrade",

    // default value was added to the Country designer property
    // default value was added to the Language designer property
    5: "noUpgrade"

  }, // End TextToSpeech upgraders

  "TimePicker": {

    // AI2: After feedback from the forum, the timepicker dialog was updated
    // to show the current time instead of the last set time by default.
    // The SetTimeToDisplay and LaunchPicker methods were added to
    // give the user more control of what time is displayed in the
    // timepicker dialog.
    2: "noUpgrade",

    // AI2: SetTimeToDisplayFromInstant method and Instant property are added.
    3: "noUpgrade"

  }, // End TimePicker upgraders

  "TinyDB": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End TinyDB upgraders

  "TinyWebDB": {

    // AI1: Look for TinyWebDB-ShowAlert method blocks for this component.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "TinyWebDB",
          "ShowAlert")) {
        // Change the genus-name because TinyWebDB-ShowAlert doesn't exist anymore.
        changeBlockGenusName(block, "Notifier-ShowAlert");
        // Mark the block bad.
        markBlockBad(block,
            "TinyWebDB.ShowAlert has been removed. Please use Notifier.ShowAlert instead.");
      }
      // Blocks related to this component have now been upgraded to version 2.
    */
    2: "ai1CantDoUpgrade" // Just indicates we couldn't do upgrade even if we wanted to

  }, // End TinyWebDB upgraders

  "Twitter": {

    // AI1: Change IsLoggedIn handlers to IsAuthorized. They are close enough
    // that this will probably work for most apps
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Twitter", "IsLoggedIn")) {
        changeBlockGenusName(block, "Twitter-IsAuthorized");
        Node labelChild = getBlockLabelChild(block);
        String newLabel = componentName + ".IsAuthorized";
        labelChild.setNodeValue(newLabel);
      }
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Twitter", "Login")) {
        markBlockBad(block, "Twitter.Login no longer works due to a change in " +
            "Twitter's APIs. Please use Authorize instead.");
      }
      // Blocks related to this component have now been upgraded to version 2.
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: // SetStatus has been changed to Tweet because it's more intuitive.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Twitter", "SetStatus")) {
        changeBlockGenusName(block, "Twitter-Tweet");
        Node labelChild = getBlockLabelChild(block);
        String newLabel = componentName + ".Tweet";
        labelChild.setNodeValue(newLabel);
      }
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI2: - Modified 'TweetWithImage' to upload images to Twitter directly because of the shutdown of
    //   TwitPic. The TwitPic_API_Key property is now deprecated and hidden.
    // *** This really should involve markBadBlock or something similar ***
    4: "noUpgrade"

  }, // End Twitter upgraders

  "VerticalArrangement": {

    // AI1: The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
    // to upgrade to version 2.
    2: "noUpgrade",

    // - Added background color & image
    3: "noUpgrade"

  }, // End VerticalArrangement upgraders

  "VerticalScrollArrangement": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End VerticalScrollArrangement upgraders

  "VideoPlayer": {

    // AI1: The VideoPlayer.VideoPlayerError event was added.
    // No blocks need to be modified to upgrade to version 2.
    2: "noUpgrade",

    // AI1: The VideoPlayer.VideoPlayerError event was marked userVisible false and is no longer used.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "VideoPlayer", "VideoPlayerError")) {
        markBlockBad(block, "The VideoPlayer.VideoPlayerError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // A1: The VideoPlayer.height and VideoPlayer.width getter and setters were marked as
    // visible to the user
    4: "noUpgrade",

    // AI2: The Volume property (setter only) was added to the VideoPlayer.
    5: "noUpgrade"

  }, // End VideoPlayer upgraders

  "Voting": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  }, // End Voting upgraders

  "Web": {

    // AI1: The RequestHeaders and AllowCookies properties were added.
    // The BuildPostData and ClearCookies methods were added.
    // The existing PostText method was renamed to PostTextWithEncoding, and a new PostText
    // method was added.
    /* From BlockSaveFile.java:
      // Look for Web-PostText method blocks for this component.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Web", "PostText")) {
        // Change the method from PostText to PostTextWithEncoding.
        changeBlockGenusName(block, "Web-PostTextWithEncoding");
        changeBlockLabel(block, componentName + ".PostText",
            componentName + ".PostTextWithEncoding");
      }
    */
    2: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI1: Change BuildPostData function to BuildRequestData.
    /* From BlockSaveFile.java:
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Web", "BuildPostData")) {
        changeBlockGenusName(block, "Web-BuildRequestData");
        Node labelChild = getBlockLabelChild(block);
        String newLabel = componentName + ".BuildRequestData";
        labelChild.setNodeValue(newLabel);
      }
    */
    3: "ai1CantDoUpgrade", // Just indicates we couldn't do upgrade even if we wanted to

    // AI2: Added method XMLTextDecode
    4: "noUpgrade"

  }, // End Web upgraders

  "WebViewer": {

    // AI1: The CanGoForward and CanGoBack methods were added
    // nothing needs to be changed to upgrade to version 2
    2: "noUpgrade",

    // UsesLocation property added.
    // No properties need to be modified to upgrade to version 3.
    3: "noUpgrade",

    // AI2: Add WebViewString
    4: "noUpgrade",

    // AI2: IgnoreSslError property added
    5: "noUpgrade",

    // AI2: Added ClearCaches method
    6: "noUpgrade"

  }, // End WebViewer upgraders

  "YandexTranslate": {

    //This is initial version. Placeholder for future upgrades
    1: "noUpgrade"

  } // End YandexTranslate upgraders

}
