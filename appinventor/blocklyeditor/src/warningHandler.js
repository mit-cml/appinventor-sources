// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle warnings in the block editor.
 *
 */

Blockly.WarningHandler = {};
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
    Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors(false);
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

Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors = function() {
  var blockArray = Blockly.mainWorkspace.getAllBlocks();
  for(var i=0;i<blockArray.length;i++) {
    var blockErrorResult = Blockly.WarningHandler.checkErrors.call(blockArray[i]);
  }
}

//Takes a block as the context (this), puts
//the appropriate error or warning on the block,
//and returns the corresponding warning state
Blockly.WarningHandler.checkErrors = function() {
  if(typeof showWarnings == "undefined") {
    showWarnings = Blockly.WarningHandler.showWarningsToggle;
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
    var errorMessage = "This block cannot be in a definition";
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
    var dropDown = this.getTitle_(params.dropDowns[i]);
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
      var errorMessage = "Select a valid value in the drop down.";
      if(this.errorIcon){
        this.errorIcon.setText(errorMessage);
      } else {
        this.setErrorIconText(errorMessage);
      }
      return true;
    }
  }
  return false;
}

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

//Warnings

//Warnings indicate that there is a problem with the project, but it will not run
//Each function returns true if there is an warning, and sets the warning text on the block

//Check if the block contains any empty sockets
Blockly.WarningHandler.checkEmptySockets = function(params){
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
      var warningMessage = "You should fill all of the sockets with blocks";
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
Blockly.WarningHandler.checkBlockAtRoot = function(params){
  var rootBlock = this.getRootBlock();
  if(this == rootBlock && this.blockType != "event" && this.type !="global_declaration" &&
     this.type != "procedures_defnoreturn" && this.type != "procedures_defreturn"){
    if(Blockly.WarningHandler.showWarningsToggle) {
      var warningMessage = "This block should be connected to an event block or a procedure definition";
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
