// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
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

goog.provide('Blockly.WarningHandler');

Blockly.WarningHandler.allBlockErrors = [{name:"checkReplErrors"}];
Blockly.WarningHandler.allBlockWarnings = [{name:"checkBlockAtRoot"},{name:"checkEmptySockets"}];
Blockly.WarningHandler.showWarningsToggle = false;

Blockly.WarningHandler.errorCount = 0;
Blockly.WarningHandler.warningCount = 0;

Blockly.WarningHandler.warningState = {
  NO_ERROR : 0,
  WARNING : 1,
  ERROR : 2
}

Blockly.WarningHandler.updateWarningErrorCount = function() {
  //update the error and warning count in the UI
  Blockly.mainWorkspace.warningIndicator.updateWarningAndErrorCount();
}

//Call to toggle the visibility of the warnings on the blocks
Blockly.WarningHandler.warningToggle = function() {
  if(Blockly.WarningHandler.showWarningsToggle) {
    Blockly.WarningHandler.showWarningsToggle = false;
    Blockly.WarningHandler.hideWarnings();
  } else {
    Blockly.WarningHandler.showWarningsToggle = true;
    Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors(); // [lyn, 12/31/2013] Removed unnecessary false arg
  }
  Blockly.mainWorkspace.warningIndicator.updateWarningToggleText();
}

//Hide warnings on the blocks
Blockly.WarningHandler.hideWarnings = function() {
  var blockArray = Blockly.mainWorkspace.getAllBlocks();
  for(var i=0;i<blockArray.length;i++) {
    if(blockArray[i].warning) {
      blockArray[i].setWarningText(null);
    }
  }
}

Blockly.WarningHandler.cacheGlobalNames = false;
Blockly.WarningHandler.cachedGlobalNames = [];

Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors = function() {
  var start = new Date().getTime();
  var topBlocks = Blockly.mainWorkspace.getTopBlocks();
  var allBlocks = Blockly.mainWorkspace.getAllBlocks();
  try {
    if (Blockly.Instrument.useLynCacheGlobalNames) {
      // Compute and cache the list of global names once only
      // so that each call to checkDropDownContainsValidValue needn't recalculate this.
      Blockly.WarningHandler.cacheGlobalNames = false; // Set to false to actually compute names in next line.
      Blockly.WarningHandler.cachedGlobalNames = Blockly.FieldLexicalVariable.getGlobalNames();
      Blockly.WarningHandler.cacheGlobalNames = true;
    }
    for(var i=0;i<allBlocks.length;i++) {
      var blockErrorResult = Blockly.WarningHandler.checkErrors.call(allBlocks[i]);
    }
  } finally {
    // [lyn, 04/13/14] Ensure that these are reset no matter what:
    Blockly.WarningHandler.cacheGlobalNames = false;
    Blockly.WarningHandler.cachedGlobalNames = [];
  }
  var stop = new Date().getTime();
  var timeDiff = stop - start;
  Blockly.Instrument.stats.topBlockCount = topBlocks.length;
  Blockly.Instrument.stats.blockCount = allBlocks.length;
  Blockly.Instrument.stats.checkAllBlocksForWarningsAndErrorsCalls++;
  Blockly.Instrument.stats.checkAllBlocksForWarningsAndErrorsTime += timeDiff;
}

//Takes a block as the context (this), puts
//the appropriate error or warning on the block,
//and returns the corresponding warning state
Blockly.WarningHandler.checkErrors = function() {
  // [lyn, 11/11/2013] Special case: ignore blocks in flyout for purposes of error handling
  //   Otherwise, blocks in drawer having connected subblocks (see Blockly.Drawer.defaultBlockXMLStrings)
  //   will increment warning indicator.
  if (this.isInFlyout) {
    return Blockly.WarningHandler.warningState.NO_ERROR;
  }
  if(typeof showWarnings == "undefined") {
    var showWarnings = Blockly.WarningHandler.showWarningsToggle;
  }

  if(!this.getSvgRoot() || this.readOnly){
    //remove from error count
    if(this.hasWarning) {
      this.hasWarning = false;
      Blockly.WarningHandler.warningCount--;
      Blockly.WarningHandler.updateWarningErrorCount();
    }
    if(this.hasError) {
      this.hasError = false;
      Blockly.WarningHandler.errorCount--;
      Blockly.WarningHandler.updateWarningErrorCount();
    }
    return Blockly.WarningHandler.warningState.NO_ERROR;
  }

  //give the block empty arrays of errors and warnings to check if they aren't defined.
  if(!this.errors){
    this.errors = [];
  }
  if(!this.warnings){
    this.warnings = [];
  }

  //add warnings and errors that are on every block
  var errorTestArray = this.errors.concat(Blockly.WarningHandler.allBlockErrors);
  var warningTestArray = this.warnings.concat(Blockly.WarningHandler.allBlockWarnings);

  //check if there are any errors
  for(var i=0;i<errorTestArray.length;i++){
    if(Blockly.WarningHandler[errorTestArray[i].name].call(this,errorTestArray[i])){

      //remove warning marker, if present
      if(this.warning) {
        this.setWarningText(null);
      }
      //If the block doesn't have an error already,
      //add one to the error count
      if(!this.hasError) {
        this.hasError = true;
        Blockly.WarningHandler.errorCount++;
        Blockly.WarningHandler.updateWarningErrorCount();
      }
      //If the block has a warning,
      //subtract from the error count
      if(this.hasWarning) {
        this.hasWarning = false;
        Blockly.WarningHandler.warningCount--;
        Blockly.WarningHandler.updateWarningErrorCount();
      }

      return Blockly.WarningHandler.warningState.ERROR;
    }
  }

  //remove the error icon, if there is one
  if(this.errorIcon) {
    this.setErrorIconText(null);
  }
  //If the block has an error,
  //subtract from the error count
  if(this.hasError) {
    this.hasError = false;
    Blockly.WarningHandler.errorCount--;
    Blockly.WarningHandler.updateWarningErrorCount();
  }
  //if there are no errors, check for warnings
  for(var i=0;i<warningTestArray.length;i++){
    if(Blockly.WarningHandler[warningTestArray[i].name].call(this,warningTestArray[i])){
      if(!this.hasWarning) {
        this.hasWarning = true;
        Blockly.WarningHandler.warningCount++;
        Blockly.WarningHandler.updateWarningErrorCount();
      }
      return Blockly.WarningHandler.warningState.WARNING;
    }
  }

  //remove the warning icon, if there is one
  if(this.warning) {
    this.setWarningText(null);
  }
  if(this.hasWarning) {
    this.hasWarning = false;
    Blockly.WarningHandler.warningCount--;
    Blockly.WarningHandler.updateWarningErrorCount();
  }

  //return no error
  return Blockly.WarningHandler.warningState.NO_ERROR;

}


//Errors

//Errors indicate that the project will not run (or get errors that we can detect at build time)
//Each function returns true if there is an error, and sets the error text on the block

//Check if the block is inside of a variable declaration block, if so, create an error
Blockly.WarningHandler.checkIsInDefinition = function(){
  var rootBlock = this.getRootBlock();
  if(rootBlock.type == "global_declaration"){
    var errorMessage = Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION;
    if(this.errorIcon){
      this.errorIcon.setText(errorMessage);
    } else {
      this.setErrorIconText(errorMessage);
    }
    return true;
  } else {
    return false;
  }

}

//Check if the block has an invalid drop down value, if so, create an error
Blockly.WarningHandler.checkDropDownContainsValidValue = function(params){
  for(var i=0;i<params.dropDowns.length;i++){
    var dropDown = this.getField_(params.dropDowns[i]);
    var dropDownList = dropDown.menuGenerator_();
    var text = dropDown.text_;
    var textInDropDown = false;
    for(var k=0;k<dropDownList.length;k++) {
      if(dropDownList[k][0] == text && text != " "){
        textInDropDown = true;
        break;
      }
    }
    if(!textInDropDown) {
      var errorMessage = Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN;
      // [lyn, 12/23/2013] setErrorIconText already does this test, so don't repeat it here
//      if(this.errorIcon){
//        this.errorIcon.setText(errorMessage);
//      } else {
//        this.setErrorIconText(errorMessage);
//      }
      this.setErrorIconText(errorMessage);
      return true;
    }
  }
  return false;
}

// check if the component of the pasted block from the Backpack does not exist
// - originally written by @evanrthomas
// - added by @graceRyu

Blockly.WarningHandler.checkComponentNotExistsError = function() {
  if (this.isGeneric == true) { // Generic blocks take a component as an arg
    return false;               // So we cannot check for existence
  }
  var component_names = Blockly.ComponentInstances.getInstanceNames();
  if (component_names.indexOf(this.instanceName) == -1) {
    var errorMessage = Blockly.ERROR_COMPONENT_DOES_NOT_EXIST;
    if(this.errorIcon){
      this.errorIcon.setText(errorMessage);
    } else {
      this.setErrorIconText(errorMessage);
    }
    return true;
  }
  return false;
}

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
Blockly.WarningHandler.determineDuplicateComponentEventHandlers = function(){
  var topBlocks = Blockly.mainWorkspace.getTopBlocks(false);
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
}

// [lyn, 12/31/2013] Function called by each component event handler to check
// if it's a duplicate, using the IAmADuplicate flag preiously set
// by determineDuplicateComponentEventHandlers
Blockly.WarningHandler.checkIfIAmADuplicateEventHandler = function() {
  if (this.IAmADuplicate) {
    this.setErrorIconText(Blockly.ERROR_DUPLICATE_EVENT_HANDLER);
    return true;
  } else {
    return false;
  }
}

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
Blockly.WarningHandler.setBlockError = function(message){
  if(this.warning) {
    this.setWarningText(null);
  }
  if(this.hasWarning) {
    this.hasWarning = false;
    Blockly.WarningHandler.warningCount--;
    Blockly.WarningHandler.updateWarningErrorCount();
  }
  if(!this.hasError) {
    this.hasError = true;
    Blockly.WarningHandler.errorCount++;
    Blockly.WarningHandler.updateWarningErrorCount();
  }
  this.setErrorIconText(message);
}

// Check a disposed block for any errors or warnings and update state accordingly.
Blockly.WarningHandler.checkDisposedBlock = function(){
  if(this.warning) {
    this.setWarningText(null);
  }
  if(this.errorIcon) {
    this.setErrorIconText(null);
  }
  if(this.hasWarning) {
    this.hasWarning = false;
    Blockly.WarningHandler.warningCount--;
    Blockly.WarningHandler.updateWarningErrorCount();
  }
  if(this.hasError) {
    this.hasError = false;
    Blockly.WarningHandler.errorCount--;
    Blockly.WarningHandler.updateWarningErrorCount();
  }
}

//Warnings

//Warnings indicate that there is a problem with the project, but it will not run
//Each function returns true if there is an warning, and sets the warning text on the block

//Check if the block contains any empty sockets
Blockly.WarningHandler.checkEmptySockets = function(){
  var containsEmptySockets = false;
  for(var i=0;i<this.inputList.length;i++){
    var inputName = this.inputList[i].name;
    if(this.inputList[i].type == Blockly.INPUT_VALUE && this.inputList[i].connection && !this.getInputTargetBlock(inputName)){
      containsEmptySockets = true;
      break;
    }
  }

  if(containsEmptySockets) {
    if(Blockly.WarningHandler.showWarningsToggle) {
      var warningMessage = Blockly.Msg.MISSING_SOCKETS_WARNINGS;
      if(this.warning){
        this.warning.setText(warningMessage);
      } else {
        this.setWarningText(warningMessage);
      }
    }
    return true;
  } else {
    return false;
  }
}

//Check if the block is a root block that isn't a procedure definition, variable declaration, or event
Blockly.WarningHandler.checkBlockAtRoot = function(){
  var rootBlock = this.getRootBlock();
  if(this == rootBlock && this.blockType != "event" && this.type !="global_declaration" &&
     this.type != "procedures_defnoreturn" && this.type != "procedures_defreturn"){
    if(Blockly.WarningHandler.showWarningsToggle) {
      var warningMessage = Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS;
      if(this.warning){
        this.warning.setText(warningMessage);
      } else {
        this.setWarningText(warningMessage);
      }
    }
    return true;
  } else {
    return false;
  }
}

//Check to see if the repl (Companion App) reported any errors.
Blockly.WarningHandler.checkReplErrors = function() {
    if (this.replError) {
        this.setErrorIconText(this.replError);
        return true;
    }
    return false;
}
