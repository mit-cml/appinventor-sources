goog.provide('AI.Blockly.TypeBlockConnectionStrategy');

AI.Blockly.TypeBlockConnectionStrategy = function() {
  this.name = 'AppInventor';
  this.priority = 100;
};

AI.Blockly.TypeBlockConnectionStrategy.prototype.canConnect = function(newBlock, targetBlock, context) {
  return context.selectedBlock && context.selectedBlock === targetBlock;
};

AI.Blockly.TypeBlockConnectionStrategy.prototype.connect = function(newBlock, targetBlock, context) {
  try {
    this.connectIfPossible(targetBlock, newBlock);
  } catch(e) {
    //We can ignore these exceptions; they happen when connecting two blocks
    //that should not be connected.
  }
  const success = newBlock.parentBlock_ !== null;
  return {
    success: success,
    ...(!success && { reason: 'No compatible connection found' })
  };
};

/**
 * Blocks connect in different ways; a block with an outputConnection such as
 * a number will connect in one of its parent's input connection (inputLis).
 * A block with no outputConnection could be connected to its parent's next
 * connection.
 */
AI.Blockly.TypeBlockConnectionStrategy.prototype.connectIfPossible = function(blockSelected, createdBlock) {
  var i = 0,
    inputList = blockSelected.inputList,
    ilLength = inputList.length;
  const connectionChecker = blockSelected.workspace.connectionChecker;

  //If createdBlock has an output connection, we need to:
  //  connect to parent (eg: connect equals into if)
  //else we need to:
  //  connect its previousConnection to parent (eg: connect if to if)
  for (i = 0; i < ilLength; i++){
    try {
      if (createdBlock.outputConnection != null){
        //Check for type validity (connect does not do it)
        if (inputList[i].connection &&
            connectionChecker.canConnect(inputList[i].connection, createdBlock.outputConnection, false)){
            if (!inputList[i].connection.targetConnection){ // is connection empty?
              createdBlock.outputConnection.connect(inputList[i].connection);
              break;
            }
        }
      }
      // Only attempt a connection if the input is empty
      else if (!inputList[i].connection.isConnected()) {
        createdBlock.previousConnection.connect(inputList[i].connection);
        break;
      }
    } catch(e) {
      //We can ignore these exceptions; they happen when connecting two blocks
      //that should not be connected.
    }
  }
  if (createdBlock.parentBlock_ !== null) return; //Already connected --> return

  // Are both blocks statement blocks? If so, connect created block below the selected block
  if (blockSelected.outputConnection == null && createdBlock.outputConnection == null) {
      createdBlock.previousConnection.connect(blockSelected.nextConnection);
      return;
  }

  // No connections? Try the parent (if it exists)
  if (blockSelected.parentBlock_) {
    //Is the parent block a statement?
    if (blockSelected.parentBlock_.outputConnection == null) {
        //Is the created block a statment? If so, connect it below the parent block,
        // which is a statement
        if(createdBlock.outputConnection == null) {
          blockSelected.parentBlock_.nextConnection.connect(createdBlock.previousConnection);
          return;
        //If it's not, no connections should be made
        } else return;
      }
      else {
        //try the parent for other connections
        this.connectIfPossible(blockSelected.parentBlock_, createdBlock);
        //recursive call: creates the inner functions again, but should not be much
        //overhead; if it is, optimise!
      }
    }
  };
