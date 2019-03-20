// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle warnings in the block editor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Blockly.WarningHandler');

Blockly.WarningHandler = function(workspace) {
  this.workspace = workspace;
  this.allBlockErrors = [{name:'checkReplErrors'}];
  this.allBlockWarnings = [{name:'checkBlockAtRoot'},{name:'checkEmptySockets'}];
  this.cachedGlobalNames = [];
  this.showWarningsToggle = false;
  this.warningIdHash = {};
  this.errorIdHash = {};
};

Blockly.WarningHandler.prototype.cacheGlobalNames = false;
Blockly.WarningHandler.prototype.errorCount = 0;
Blockly.WarningHandler.prototype.warningCount = 0;
Blockly.WarningHandler.WarningState = {
  NO_ERROR: 0,
  WARNING: 1,
  ERROR: 2
};
Blockly.WarningHandler.prototype.currentWarning = 0;
Blockly.WarningHandler.prototype.currentError = 0;

Blockly.WarningHandler.prototype.updateWarningErrorCount = function() {
  //update the error and warning count in the UI
  var indicator = this.workspace.getWarningIndicator();
  if (indicator) {
    // indicator is only available after the workspace has been drawn.
    indicator.updateWarningAndErrorCount();
  }
};

Blockly.WarningHandler.prototype.updateCurrentWarningAndError = function() {
  //update the error and warning count in the UI
  var indicator = this.workspace.getWarningIndicator();
  if (indicator) {
    // indicator is only available after the workspace has been drawn.
    indicator.updateCurrentWarningAndError(this.currentWarning, this.currentError)
  }
};

//noinspection JSUnusedGlobalSymbols
/**
 * Call to toggle the visibility of the warnings on the blocks.
 * Called from BlocklyPanel.java
 * @public
 */
Blockly.WarningHandler.prototype.toggleWarning = function() {
  if(this.showWarningsToggle) {
    this.showWarningsToggle = false;
    this.hideWarnings();
  } else {
    this.showWarningsToggle = true;
    this.checkAllBlocksForWarningsAndErrors(); // [lyn, 12/31/2013] Removed unnecessary false arg
  }
  var indicator = this.workspace.getWarningIndicator();
  if (indicator) {
    // indicator is only available after the workspace has been drawn.
    indicator.updateWarningToggleText();
  }
};

//Hide warnings on the blocks
Blockly.WarningHandler.prototype.hideWarnings = function() {
  var blockArray = this.workspace.getAllBlocks();
  for(var i=0;i<blockArray.length;i++) {
    if(blockArray[i].warning) {
      blockArray[i].setWarningText(null);
    }
  }
};

Blockly.WarningHandler.prototype.checkAllBlocksForWarningsAndErrors = function() {
  // Do not attempt to update blocks before they are rendered.
  if (!this.workspace.rendered) {
    return;
  }
  if (Blockly.Instrument.isOn) {
    var start = new Date().getTime();
    var topBlocks = this.workspace.getTopBlocks();
  }
  var allBlocks = this.workspace.getAllBlocks();
  try {
    if (Blockly.Instrument.useLynCacheGlobalNames) {
      // Compute and cache the list of global names once only
      // so that each call to checkDropDownContainsValidValue needn't recalculate this.
      this.cacheGlobalNames = false; // Set to false to actually compute names in next line.
      this.cachedGlobalNames = Blockly.FieldLexicalVariable.getGlobalNames();
      this.cacheGlobalNames = true;
    }
    for(var i=0;i<allBlocks.length;i++) {
      var blockErrorResult = this.checkErrors(allBlocks[i]);
    }
  } finally {
    // [lyn, 04/13/14] Ensure that these are reset no matter what:
    this.cacheGlobalNames = false;
    this.cachedGlobalNames = [];
  }
  if (Blockly.Instrument.isOn) {
    var stop = new Date().getTime();
    var timeDiff = stop - start;
    Blockly.Instrument.stats.topBlockCount = topBlocks.length;
    Blockly.Instrument.stats.blockCount = allBlocks.length;
    Blockly.Instrument.stats.checkAllBlocksForWarningsAndErrorsCalls++;
    Blockly.Instrument.stats.checkAllBlocksForWarningsAndErrorsTime += timeDiff;
  }
};

Blockly.WarningHandler.prototype.previousWarning = function() {
  var k = Object.keys(this.warningIdHash);
  if (this.currentWarning < k.length)
    this.workspace.getBlockById(k[this.currentWarning]).setHighlighted(false);
  if (this.currentWarning > 0) {
    this.currentWarning--;
  } else this.currentWarning = k.length - 1;
  this.workspace.centerOnBlock(k[this.currentWarning]);
  this.workspace.getBlockById(k[this.currentWarning]).setHighlighted(true);
  this.updateCurrentWarningAndError();
};

Blockly.WarningHandler.prototype.nextWarning = function() {
  var k = Object.keys(this.warningIdHash);
  if (this.currentWarning < k.length)
    this.workspace.getBlockById(k[this.currentWarning]).setHighlighted(false);
  if (this.currentWarning < k.length - 1) {
    this.currentWarning++;
  } else this.currentWarning = 0;
  this.workspace.centerOnBlock(k[this.currentWarning]);
  this.workspace.getBlockById(k[this.currentWarning]).setHighlighted(true);
  this.updateCurrentWarningAndError();
};


Blockly.WarningHandler.prototype.previousError = function() {
  var k = Object.keys(this.errorIdHash);
  if (this.currentError < k.length)
    this.workspace.getBlockById(k[this.currentError]).setHighlighted(false);
  if (k.length > 0) {
    if (this.currentError > 0) {
      this.currentError--;
    } else this.currentError = k.length - 1;
    this.workspace.centerOnBlock(k[this.currentError]);
    this.workspace.getBlockById(k[this.currentError]).setHighlighted(true);
    this.updateCurrentWarningAndError();
  }
};

Blockly.WarningHandler.prototype.nextError = function() {
  var k = Object.keys(this.errorIdHash);
  if (this.currentError < k.length)
    this.workspace.getBlockById(k[this.currentError]).setHighlighted(false);
  if (k.length > 0) {
    if (this.currentError < k.length - 1) {
      this.currentError++;
    } else this.currentError = 0;
    this.workspace.centerOnBlock(k[this.currentError]);
    this.workspace.getBlockById(k[this.currentError]).setHighlighted(true);
    this.updateCurrentWarningAndError();
  }
};

//Takes a block as the context (this), puts
//the appropriate error or warning on the block,
//and returns the corresponding warning state
Blockly.WarningHandler.prototype.checkErrors = function(block) {
  // [lyn, 11/11/2013] Special case: ignore blocks in flyout for purposes of error handling
  //   Otherwise, blocks in drawer having connected subblocks (see Blockly.Drawer.defaultBlockXMLStrings)
  //   will increment warning indicator.
  block.setHighlighted(false);
  this.updateWarningErrorCount();
  if (block.isInFlyout) {
    return Blockly.WarningHandler.WarningState.NO_ERROR;
  }
  var showWarnings = this.showWarningsToggle;

  if(!block.getSvgRoot() || block.readOnly){
    //remove from error count
    if(block.hasWarning) {
      block.hasWarning = false;
      this.warningCount--;
      delete this.warningIdHash[block.id];
      this.updateWarningErrorCount();
    }
    if(block.hasError) {
      block.hasError = false;
      this.errorCount--;
      delete this.errorIdHash[block.id];
      this.updateWarningErrorCount();
    }
    return Blockly.WarningHandler.WarningState.NO_ERROR;
  }

  //give the block empty arrays of errors and warnings to check if they aren't defined.
  if(!block.errors){
    block.errors = [];
  }
  if(!block.warnings){
    block.warnings = [];
  }

  //add warnings and errors that are on every block
  var errorTestArray = block.errors.concat(this.allBlockErrors);
  var warningTestArray = block.warnings.concat(this.allBlockWarnings);

  //check if there are any errors
  for(var i=0;i<errorTestArray.length;i++){
    if(this[errorTestArray[i].name].call(this,block,errorTestArray[i])){

      //remove warning marker, if present
      if(block.warning) {
        block.setWarningText(null);
      }
      //If the block doesn't have an error already,
      //add one to the error count
      if(!block.hasError) {
        block.hasError = true;
        this.errorCount++;
        this.errorIdHash[block.id] = true;
        this.updateWarningErrorCount();
      }
      //If the block has a warning,
      //subtract from the error count
      if(block.hasWarning) {
        block.hasWarning = false;
        this.warningCount--;
        delete this.warningIdHash[block.id];
        this.updateWarningErrorCount();
      }

      return Blockly.WarningHandler.WarningState.ERROR;
    }
  }

  //remove the error icon, if there is one
  if(block.error) {
    block.setErrorIconText(null);
  }
  //If the block has an error,
  //subtract from the error count
  if(block.hasError) {
    block.hasError = false;
    this.errorCount--;
    delete this.errorIdHash[block.id];
    this.updateWarningErrorCount();
  }
  //if there are no errors, check for warnings
  for(var i=0;i<warningTestArray.length;i++){
    if(this[warningTestArray[i].name].call(this,block,warningTestArray[i])){
      if(!block.hasWarning) {
        block.hasWarning = true;
        this.warningCount++;
        this.warningIdHash[block.id] = true;
        this.updateWarningErrorCount();
      }
      return Blockly.WarningHandler.WarningState.WARNING;
    }
  }

  //remove the warning icon, if there is one
  if(block.warning) {
    block.setWarningText(null);
  }
  if(block.hasWarning) {
    block.hasWarning = false;
    this.warningCount--;
    delete this.warningIdHash[block.id];
    this.updateWarningErrorCount();
  }

  //return no error
  return Blockly.WarningHandler.WarningState.NO_ERROR;
};


//Errors

//Errors indicate that the project will not run (or get errors that we can detect at build time)
//Each function returns true if there is an error, and sets the error text on the block

//Check if the block is inside of a variable declaration block, if so, create an error
Blockly.WarningHandler.prototype["checkIsInDefinition"] = function(block){
  // Allow property getters as they should be pure.
  if (block.type === 'component_set_get' && block.setOrGet === 'get') {
    return false;
  }
  var rootBlock = block.getRootBlock();
  if(rootBlock.type == "global_declaration"){
    var errorMessage = Blockly.Msg.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION;
    block.setErrorIconText(errorMessage);
    return true;
  } else {
    return false;
  }
};

// Check if block is undefined and unplug
Blockly.WarningHandler.prototype['checkIfUndefinedBlock'] = function(block) {
  if (block.isBadBlock() === true) {
    var errorMessage = Blockly.Msg.ERROR_BLOCK_IS_NOT_DEFINED;
    var healStack = true;
    if (block.type == "component_event") {
      healStack = false; // unplug all blocks inside
    }
    block.isolate(healStack);
    block.setErrorIconText(errorMessage);
    return true;
  } else {
    return false;
  }
};


//Check if the block has an invalid drop down value, if so, create an error
Blockly.WarningHandler.prototype['checkDropDownContainsValidValue'] = function(block, params){
  if (Blockly.dragMode_ === Blockly.DRAG_FREE && Blockly.selected === block) {
    return false;  // wait until the user is done dragging to check validity.
  }
  for(var i=0;i<params.dropDowns.length;i++){
    var dropDown = block.getField(params.dropDowns[i]);
    var dropDownList = dropDown.menuGenerator_();
    var text = dropDown.text_;
    var textInDropDown = false;
    if (dropDown.updateMutation) {
      dropDown.updateMutation();
    }
    for(var k=0;k<dropDownList.length;k++) {
      if(dropDownList[k][0] == text && text != " "){
        textInDropDown = true;
        break;
      }
    }
    if(!textInDropDown) {
      var errorMessage = Blockly.Msg.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN;
      block.setErrorIconText(errorMessage);
      return true;
    }
  }
  return false;
};

// Check if the block is not within a loop block (used for checking break block)
// if so, create an error

Blockly.WarningHandler.prototype["checkIsNotInLoop"] = function(block) {
  if (Blockly.dragMode_ === Blockly.DRAG_FREE && Blockly.selected === block) {
    return false;  // wait until the user is done dragging to check validity.
  }
  if (Blockly_containedInLoop(block)) {
    return false;  // false means it is within a loop
  } else {
    var errorMessage = Blockly.Msg.ERROR_BREAK_ONLY_IN_LOOP;
    block.setErrorIconText(errorMessage);
    return true;  //true means it is not within a loop
  }
};

Blockly_loopBlockTypes =
  // add more later
  ["controls_forEach", "controls_forRange", "controls_while"] ;

Blockly_containedInLoop = function(block) {
  var enclosingBlock = block.getSurroundParent();
  if (enclosingBlock == null) {
    return false;
  }
  else if (Blockly_loopBlockTypes.indexOf(enclosingBlock.type) >= 0) {
    return true;
  } else {
    return Blockly_containedInLoop(enclosingBlock);
  }
};

// check if the component of the pasted block from the Backpack does not exist
// - originally written by @evanrthomas
// - added by @graceRyu

Blockly.WarningHandler.prototype['checkComponentNotExistsError'] = function(block) {
  if (block.isGeneric == true) { // Generic blocks take a component as an arg
    return false;               // So we cannot check for existence
  }
  var component_names = this.workspace.componentDb_.getInstanceNames();
  if (component_names.indexOf(block.instanceName) == -1) {
    var errorMessage = Blockly.Msg.ERROR_COMPONENT_DOES_NOT_EXIST;
    block.setErrorIconText(errorMessage);
    return true;
  }
  return false;
};

// [lyn, 12/31/2013] Function that determines which component event handlers
// in the main workspace are duplicates. Sets the IAmADuplicate property of each
// duplicate event handler block to true; otherwise sets it to false.
// This property is later tested by the checkIfIAmADuplicateEventHandler function on
// each handler.
//
// This function is called once as a change handler on the main workspace every
// time there is a change to the space, before any error handlers are called.
// (via Blockly.bindEvent_(Blockly.mainWorkspace.getCanvas(), 'blocklyWorkspaceChange'
// in blocklyeditor.js). So the checkIfImADuplicateEventHandler for an event handler
// block can use the IAmADuplicate property set by this function.
//
// Separating the setting of the IAmADuplicate property from testing it is
// essential for making the time of the processing linear rather than quadratic
// in the number of event handler blocks in the workspace. If each event handler
// block independently determined the duplicate blocks, that behavior would be
// quadratic, and based on empirical tests could significantly slow down error
// checking for screens with lots (many dozens) of handlers.
Blockly.WarningHandler.prototype['determineDuplicateComponentEventHandlers'] = function(){
  var topBlocks = this.workspace.getTopBlocks(false);
  var len = topBlocks.length;
  var eventHandlers = {}; // Object for storing event handler info
  for (var i = 0; i < len; i++) {
    var topBlock = topBlocks[i];
    if (topBlock.type == "component_event") {
      topBlock.IAmADuplicate = false; // default value for this field; may be changed to true below
      var typeName = topBlock.typeName;
      var propertyName = typeName + ":" + topBlock.eventName + ":" + topBlock.instanceName + ":" + topBlock.disabled;
      /* [lyn, 01/04/2013] Notion of singleton component is not well-defined. Must think more about this!
         If adopt singleton component notion, will need the following code:
            // if (! Blockly.WarningHandler.isSingletonComponentType(typeName)) {
            //   propertyName = propertyName + ":" + topBlock.instanceName;
            //   // At this point, propertyName is something like AccelerometerSensor:AccelerationChanged
            //   // for singleton components.
            // }
            //
            //
      */
      // At this point, propertyName is something like Button:Click:Button2 for nonsingleton components
      var handler = eventHandlers[propertyName];
      if (! handler) {
        eventHandlers[propertyName] = {block: topBlock, isDuplicate: false};
      } else {
        if (!handler.isDuplicate) {
          handler.isDuplicate = true;
          handler.block.IAmADuplicate = true; // initial block is a duplicate, too
        }
        topBlock.IAmADuplicate = true; // this block is a duplicate
      }
    }
  }
};

// [lyn, 12/31/2013] Function called by each component event handler to check
// if it's a duplicate, using the IAmADuplicate flag preiously set
// by determineDuplicateComponentEventHandlers
Blockly.WarningHandler.prototype['checkIfIAmADuplicateEventHandler'] = function(block) {
  if (block.IAmADuplicate) {
    block.setErrorIconText(Blockly.Msg.ERROR_DUPLICATE_EVENT_HANDLER);
    return true;
  } else {
    return false;
  }
};


/* [lyn, 12/23/2013] Putting a change handler that determines duplicates
   on each AI2 event handler block leads to
   examining top-level blocks a quadratic number of times, which empirically is
   unnacceptable for screens with a large number of such blocks.

// [lyn, 12/23/2103]
// Check if the block is a duplicate error handler. If so, create an error
// Currently, this is an inefficient process that is called on each handler block.
// Should really only be called once on the whole workspace.
Blockly.WarningHandler.checkDuplicateErrorHandler = function(params){
  var topBlocks = Blockly.mainWorkspace.getTopBlocks(false);
  var len = topBlocks.length;
  Blockly.WarningHandler.outerCount++;
  console.log("outer checkDuplicateErrorHandler (topBlocks: " + len + "; outer: "
              + Blockly.WarningHandler.outerCount + "; inner: "
              + Blockly.WarningHandler.innerCount + ")" );
  for (var i = 0; i < topBlocks.length; i++) {
    Blockly.WarningHandler.innerCount++;
    console.log("inner checkDuplicateErrorHandler (topBlocks: " + len + "; outer: "
        + Blockly.WarningHandler.outerCount + "; inner: "
        + Blockly.WarningHandler.innerCount + ")" );
    var topBlock = topBlocks[i];
    if (topBlock != this
        && topBlock.type == "component_event"
        && topBlock.typeName == this.typeName
        && topBlock.eventName == this.eventName
        && (Blockly.WarningHandler.isSingletonComponentType(this.typeName) // For components for which there can be
                                                                           // only one conceptual instance
            || topBlock.instanceName == this.instanceName)) {
      this.setErrorIconText("This is a duplicate event handler for this component.");
      return true;
    }
  }
  return false;
}
*/

// [lyn, 12/23/2103]
// Return true if typeName is a component type for which there can be only one instance on the phone,
// and false otherwise. E.g. there really is only one TinyDB, one Camera, one AccelerometerSensor, etc.
// even if the user tries to make more than one of them.
//
// TODO: There should be a more modular way to define the singleton components than to have a global list here!
// TODO: Should prevent more than one of a singleton component from being added to project!

// [lyn, 01/04/2013] Notion of singleton component is not well-defined, so commenting this code out.
// Must thing more about this!
//Blockly.WarningHandler.isSingletonComponentType = function(typeName) {
//  return Blockly.WarningHandler.singletonComponentTypes.indexOf(typeName) != -1;
//}
//
//Blockly.WarningHandler.singletonComponentTypes =
//    [// Storage
//     "TinyDB",
//     // Sensors
//     "AccelerometerSensor",
//     "BarcodeSensor",
//     "LocationSensor",
//     "NearField",
//     "OrientationSensor",
//     // Media
//     "Camcorder",
//     "Camera",
//     "ImagePicker",
//     "Player",
//     "Sound",
//     "SoundRecorder",
//     "SpeechRecognizer",
//     "TextToSpeech",
//     "VideoPlayer",
//     // Social
//     "ContactPicker",
//     "EmailPicker",
//     "PhoneCall",
//     "PhoneNumberPicker",
//     "Texting",
//     "Twitter"
//    ]

//This is the error that can be set from the REPL. It will be removed when the block changes.
Blockly.WarningHandler.prototype.setBlockError = function(block, message){
  if(block.warning) {
    block.setWarningText(null);
  }
  if(block.hasWarning) {
    block.hasWarning = false;
    this.warningCount--;
    this.updateWarningErrorCount();
  }
  if(!block.hasError) {
    block.hasError = true;
    this.errorCount++;
    this.updateWarningErrorCount();
  }
  block.setErrorIconText(message);
};

// Check a disposed block for any errors or warnings and update state accordingly.
Blockly.WarningHandler.prototype.checkDisposedBlock = function(block){
  if(block.warning) {
    block.setWarningText(null);
  }
  if(block.error) {
    block.setErrorIconText(null);
  }
  if(block.hasWarning) {
    block.hasWarning = false;
    this.warningCount--;
    delete this.warningIdHash[block.id];
    this.updateWarningErrorCount();
  }
  if(block.hasError) {
    block.hasError = false;
    this.errorCount--;
    delete this.errorIdHash[block.id];
    this.updateWarningErrorCount();
  }
};

//Warnings

//Warnings indicate that there is a problem with the project, but it will not run
//Each function returns true if there is an warning, and sets the warning text on the block

//Check if the block contains any empty sockets
Blockly.WarningHandler.prototype['checkEmptySockets'] = function(block){
  var containsEmptySockets = false;
  for(var i=0;i<block.inputList.length;i++){
    var inputName = block.inputList[i].name;
    if(block.inputList[i].type == Blockly.INPUT_VALUE && block.inputList[i].connection && !block.getInputTargetBlock(inputName)){
      containsEmptySockets = true;
      break;
    }
  }

  if(containsEmptySockets) {
    if(this.showWarningsToggle) {
      var warningMessage = Blockly.Msg.MISSING_SOCKETS_WARNINGS;
      block.setWarningText(warningMessage);
    }
    return true;
  } else {
    return false;
  }
};

//Check if the block is a root block that isn't a procedure definition, variable declaration, or event
Blockly.WarningHandler.prototype['checkBlockAtRoot'] = function(block){
  var rootBlock = block.getRootBlock();
  if(block == rootBlock && block.blockType != "event" && block.type !="global_declaration" &&
     block.type != "procedures_defnoreturn" && block.type != "procedures_defreturn"){
    if(this.showWarningsToggle) {
      var warningMessage = Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS;
      block.setWarningText(warningMessage);
    }
    return true;
  } else {
    return false;
  }
};

//Check to see if the repl (Companion App) reported any errors.
Blockly.WarningHandler.prototype['checkReplErrors'] = function(block) {
  if (block.replError) {
    block.setErrorIconText(block.replError);
    return true;
  }
  return false;
};
