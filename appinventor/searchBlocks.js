/**
 * Created by cecetsui on 6/30/15.
 * Modified by abhijit5893 7/30/16
 */
'use strict';

goog.provide('Blockly.SearchBlocks');

goog.require('Blockly.Workspace');
goog.require('Blockly.Block');
goog.require('goog.events');
goog.require('goog.events.KeyCodes');
goog.require('goog.events.KeyHandler');
goog.require('goog.window');


/**
 * Class for a search box.
 * @param {Object - Blockly.Workspace} workspace The workspace the search box will be corresponding with.
 * @param {String} frame The html configuration frame name.
 * @constructor
 */


/**
 * An array of the blocks that match the query written in.
 * Used when zooming to the next/previous block upon
 * typing in an arrow key.
 * @type {Array}
 * @private
 */
Blockly.SearchBlocks.matchBlocks = null;

/**
 * An array of the blocks that do not match the query written in.
 * Used when reverting the color of the blocks back to 
 * its original color, as these blocks' colors would be
 * gray.
 * @type {Array}
 * @private
 */
Blockly.SearchBlocks.notMatchBlocks = null;

/**
 * An object where the keys are the blocks' id matching the
 * criteria and the values are its parent block that 
 * is collapsed. This is used to collapse/un-collapse the
 * blocks when navigating through them with the arrow keys.
 * @type {Object}
 * @private
 */
Blockly.SearchBlocks.collapsedBlocks = null;

/**
 * Index correlating to which block in the matchBlocks list
 * is being viewed. With an up or right arrow key, the index
 * increases by one to view the next block. With a down or left
 * arrow key, the index decreases by one to view the previous block.
 * @type {number}
 * @private
 */
Blockly.SearchBlocks.currentBlockView = -1;



/**
* Search for the blocks that match with the query. Initialize
* all needed variables to take part.
* @param {String} query The phrase, word, or query that stands as the criteria of search.
**/
Blockly.SearchBlocks.start = function(query,filter) {
  if (Blockly.SearchBlocks.matchBlocks != null || Blockly.SearchBlocks.matchBlocks != undefined) {
    return;
  }
  if (Blockly.selected) {
    Blockly.selected.unselect();
    Blockly.selected = null;
  }
  if (filter == "None"){
    Blockly.SearchBlocks.getBlocks(query);
  } else if(filter == 'comments only'){
    Blockly.SearchBlocks.searchComment(query,filter);
  }
  else{
    Blockly.SearchBlocks.getSelectedBlocks(query,filter);
  }
  var matchList = Blockly.SearchBlocks.matchBlocks;
  Blockly.SearchBlocks.currentBlockView = -1;
  goog.events.unlisten(Blockly.TypeBlock.docKh_, 'key', Blockly.TypeBlock.handleKey);
  goog.events.unlisten(Blockly.TypeBlock.inputKh_, 'key', Blockly.TypeBlock.handleKey);
  goog.events.listen(Blockly.TypeBlock.docKh_, 'key', Blockly.SearchBlocks.handleKey);
  return matchList;
};

/**
* Search through comments
* Function is called only if advanced filters are enabled
* @param {String} query The phrase, word, or query that stands as the criteria of search.
* @param {String} Block type that need to be filtered
**/
Blockly.SearchBlocks.searchComment = function(query,filter){
    var splitQuery = query.split("+");
    var i;
    for( i = 0; i < splitQuery.length;i++ ){
      splitQuery[i] = splitQuery[i].trim();
    }
    var allBlocks = Blockly.mainWorkspace.getAllBlocks();
    var filteredBlocks = []; // Matching blocks
    var notAMatch = []; // Non-matching blocks
    var collapsed = {}; // Object storing any matching blocks within a collapsed block

    query = query.toLowerCase();

    for (var index in allBlocks) {
      var flag = 0;
      var block = allBlocks[index];
      var blockComment= block.comment;
      if(blockComment != null){
        var commentFlag =0;
        var blockCommentText = blockComment.getText();
        var commentText = blockCommentText.split(" ");
        for( i = 0; i < commentText.length;i++ ){
          commentText[i] = commentText[i].toLowerCase().trim();
          for( var j = 0; j < splitQuery.length;j++ ){
            if(commentText[i].indexOf(splitQuery[j]) != -1){
              commentFlag = 1;
            }
          } 
        }
        if(commentFlag == 1){
          filteredBlocks.push(block);
          flag = 1;
          blockComment.setVisible(true);
        }else{
          notAMatch.push(block);
          block.setNotMatchColour();
        }
      }else{
         notAMatch.push(block);
         block.setNotMatchColour();
      }
    }

    Blockly.SearchBlocks.notMatchBlocks = notAMatch;
    Blockly.SearchBlocks.matchBlocks = filteredBlocks;
    Blockly.SearchBlocks.collapsedBlocks = collapsed;
};

/**
* Generate the query suggestions for given query.
* Function is called every time the user presses a button
* @param {String} query The phrase, word, or query that stands as the criteria of search.
* returns the list of query sugggestions for a given query
**/
Blockly.SearchBlocks.querySuggest = function(query){
   var allBlocks = Blockly.mainWorkspace.getAllBlocks();
   var suggestionList= new Array();
   for(var index in allBlocks){
      var block = allBlocks[index];
      var blockText = block.toString(null, false).toLowerCase().trim();
      if(blockText.indexOf(query)!= -1){
        var temp = blockText.replace(/[^\w\s]/g,"");
        temp = temp.replace(/[^\w\s]/g,"");
        temp = temp.trim();
        var tempList = temp.split(' ');
        if(tempList.length > 1){
            for(var i in tempList){
                suggestionList.push(tempList[i]);
            }
        } else {
          suggestionList.push(temp);
        }
     
     }
   }
   return suggestionList;
};

/**
* Generate the Block types of all the blocks in the workspace at a give point in time
* Function is called everytime search filters is used
* returns the list of blocktypes
**/
Blockly.SearchBlocks.getBlockTypes = function(query){
     var allBlocks = Blockly.mainWorkspace.getAllBlocks();
     var typesList= new Array();
     var typeSuggestList = new Array();
     for(var index in allBlocks){
        var block = allBlocks[index];
        if(typesList.indexOf(block.type) == -1){
          typesList.push(block.type);
        }
      }
      if(query == ""){
         return typesList;
      }
      
      for(var index in typesList){
        if(typesList[index].indexOf(query) != -1){
          typeSuggestList.push(typesList[index]);
        }
      }
    return typeSuggestList;
};


/**
* Helper function to search with the applied filter
* @param {String} query The phrase, word, or query that stands as the criteria of search.
* @param {List<Block>} The filtered list of blocks that need to be considered for search
**/
Blockly.SearchBlocks.getSelectedBlocks = function(query,filter) {
    var splitQuery = query.split("+");
    var i;
    for( i = 0; i < splitQuery.length;i++ ){
      splitQuery[i] = splitQuery[i].toLowerCase().trim();
    }
    var filteredBlocks = []; // Matching blocks
    var notAMatch = []; // Non-matching blocks
    var collapsed = {}; // Object storing any matching blocks within a collapsed block

    var allBlocks = Blockly.mainWorkspace.getAllBlocks();

    query = query.toLowerCase();

    for (var index in allBlocks) {
      var flag = 0;
      var block = allBlocks[index];
      var blockComment= block.comment;
      if(blockComment != null){
        var commentFlag =0;
        var blockCommentText = blockComment.getText();
        var commentText = blockCommentText.split(" ");
        for( i = 0; i < commentText.length;i++ ){
          commentText[i] = commentText[i].toLowerCase().trim();
          for( var j = 0; j < splitQuery.length;j++ ){
            if(commentText[i].indexOf(splitQuery[j]) != -1){
              commentFlag = 1;
            }
          } 
        }
        if(commentFlag == 1){
          filteredBlocks.push(block);
          flag = 1;
          blockComment.setVisible(true);
        }
      }
      var blockText = block.toString(null, false).toLowerCase().trim();
      for(i = 0; i < splitQuery.length;i++ ){
        if ((blockText.indexOf(splitQuery[i]) != -1)) { //If query is in block's text
          if(((block.getSurroundParent()!= null) && (block.getSurroundParent().type == filter)) || ((block.getParent()!= null) && (block.getParent().type == filter))){
            filteredBlocks.push(block);
            flag = 1;
         }
        } 
      }
      if(flag == 0){   
        block.setNotMatchColour();
        notAMatch.push(block);        
      }
      
      if (block.isCollapsed()) { //If collapsed
        var collapsedChildren = block.getDescendants(); //Inclusive of itself
        var match = [];
        var noMatch = [];
        for (var index in collapsedChildren) {
          var childBlock = collapsedChildren[index];
            var childBlockText = childBlock.toString(null, false).toLowerCase();
            if (childBlockText.indexOf(query) != -1) { //Check if query is in descendent's text
              match.push(childBlock);
            }
        }
        if (match.length > 0) { //If the collapsed block has blocks in it that have the query
          filteredBlocks.concat(match);
          block.revertColour();
          for (var index in match) {
            collapsed[match[index].id] = block;
          }
        }
      }
    }
    Blockly.SearchBlocks.notMatchBlocks = notAMatch;
    Blockly.SearchBlocks.matchBlocks = filteredBlocks;
    Blockly.SearchBlocks.collapsedBlocks = collapsed;
};

/**
* Search for the blocks that match based on the criteria. Sets the 
* matchBlocks, notMatchBlocks, and collapsedBlocks.
* @param {String} query The phrase, word, or query that stands as the criteria of search.
**/
Blockly.SearchBlocks.getBlocks = function(query) {
    var splitQuery = query.split("+");
    var i;
    for( i = 0; i < splitQuery.length;i++ ){
      splitQuery[i] = splitQuery[i].toLowerCase().trim();
    }
    var filteredBlocks = []; // Matching blocks
    var notAMatch = []; // Non-matching blocks
    var collapsed = {}; // Object storing any matching blocks within a collapsed block

    var allBlocks = Blockly.mainWorkspace.getAllBlocks();

    query = query.toLowerCase();

    for (var index in allBlocks) {
      var flag = 0;
      var block = allBlocks[index];
      var blockComment= block.comment;
      if(blockComment != null){
        var commentFlag =0;
        var blockCommentText = blockComment.getText();
        var commentText = blockCommentText.split(" ");
        for( i = 0; i < commentText.length;i++ ){
          commentText[i] = commentText[i].toLowerCase().trim();
          for( var j = 0; j < splitQuery.length;j++ ){
            if(commentText[i].indexOf(splitQuery[j]) != -1){
              commentFlag = 1;
            }
          } 
        }
        if(commentFlag == 1){
          filteredBlocks.push(block);
          flag = 1;
          blockComment.setVisible(true);
        }
      }
      var blockText = block.getBlockString(null, false).toLowerCase().trim();
      for(i = 0; i < splitQuery.length;i++ ){
        if (blockText.indexOf(splitQuery[i]) != -1) { //If query is in block's text
          filteredBlocks.push(block);
          flag = 1;
        } 
      }
      if(flag == 0){   
        block.setNotMatchColour();
        notAMatch.push(block);        
      }
      
      if (block.isCollapsed()) { //If collapsed
        var collapsedChildren = block.getDescendants(); //Inclusive of itself
        var match = [];
        var noMatch = [];
        for (var index in collapsedChildren) {
          var childBlock = collapsedChildren[index];
            var childBlockText = childBlock.toString(null, false).toLowerCase();
            if (childBlockText.indexOf(query) != -1) { //Check if query is in descendent's text
              match.push(childBlock);
            }
        }
        if (match.length > 0) { //If the collapsed block has blocks in it that have the query
          filteredBlocks.concat(match);
          block.revertColour();
          for (var index in match) {
            collapsed[match[index].id] = block;
          }
        }
      }
    }
    Blockly.SearchBlocks.notMatchBlocks = notAMatch;
    Blockly.SearchBlocks.matchBlocks = filteredBlocks;
    Blockly.SearchBlocks.collapsedBlocks = collapsed;
};


/**
* Called when the user presses an arrow key. Highlights the block being viewed in a 
* pink color and brings the block to the center of the viewer.
* @param {number} upOrDown A number (-1 or 1) that would either view the next or previous block.
**/
Blockly.SearchBlocks.zoomToSearchedBlock = function(upOrDown) { //called when user presses up/down arrow key?
  if (Blockly.SearchBlocks.matchBlocks == null || Blockly.SearchBlocks.matchBlocks.length <= 0) {
    return;
  }
  Blockly.TypeBlock.hide();
  goog.events.unlisten(Blockly.TypeBlock.docKh_, 'key', Blockly.TypeBlock.handleKey);
    var metrics = Blockly.mainWorkspace.getMetrics();
    //Figure out the next block (either viewing the next or previous block)
    if (Blockly.SearchBlocks.currentBlockView <= 0 && upOrDown  < 0) { //Going to the last block from the first
        Blockly.SearchBlocks.currentBlockView = Blockly.SearchBlocks.matchBlocks.length-1;
    } else { //Loop through
        Blockly.SearchBlocks.currentBlockView = (Blockly.SearchBlocks.currentBlockView + upOrDown) % Blockly.SearchBlocks.matchBlocks.length;
    }

    //Unselect any selected block (so the highlighting does not override)
    if (Blockly.selected) {
        Blockly.selected.unselect();
        Blockly.selected = null;
    }

    //Un-highlight the previous block
    Blockly.SearchBlocks.unHighlightSearchedBlock();

    // Get new block to view
    var blockToView = Blockly.SearchBlocks.matchBlocks[Blockly.SearchBlocks.currentBlockView];
    
    var isCollapsedBlock = Blockly.SearchBlocks.collapsedBlocks[blockToView.id];
    //If the block is in a collapsed block (meaning it has a value in collapsedBlocks)
    if (isCollapsedBlock != undefined) {
      //uncollapse block to view
      isCollapsedBlock.searchHighlight();
      isCollapsedBlock.setCollapsed(false);
      var isCollapsed = true;
      if (isCollapsedBlock != blockToView) {
        //If the block is not the block being viewed, grey it out. (Initially non-gray so user could 
          //see that the collapsed block also has a matching criteria)
        isCollapsedBlock.setNotMatchColour();
      }
    } else {
      var isCollapsed = false;
    }

    //Highlight block
    blockToView.searchHighlight();
    //Center block in the viewer
    Blockly.mainWorkspace.scrollbar.centerScrolls(blockToView, isCollapsed);
};

/**
* Unhighlight the current searched block.
**/
Blockly.SearchBlocks.unHighlightSearchedBlock = function() {
    if (Blockly.searched) {
        Blockly.searched.unSearchHighlight();
        if (Blockly.SearchBlocks.collapsedBlocks[Blockly.searched.id] != undefined) { // If block was originally collapsed
          Blockly.SearchBlocks.collapsedBlocks[Blockly.searched.id].revertColour(); // Revert colour
          Blockly.SearchBlocks.collapsedBlocks[Blockly.searched.id].setCollapsed(true); // Re-collapse block
        }
        Blockly.searched = null;
    }
};

/**
* Stop the search process. Unhighlight searched blocks. Re-collapse any blocks. Revert the colors of
* all blocks.
**/
Blockly.SearchBlocks.stop = function() {
    Blockly.SearchBlocks.unHighlightSearchedBlock();
    for(var index in Blockly.SearchBlocks.matchBlocks){
      var block= Blockly.SearchBlocks.matchBlocks[index];
      if(block.comment != null){
        block.comment.setVisible(false);
      }
    }
    for (var index in Blockly.SearchBlocks.notMatchBlocks) {
        var block = Blockly.SearchBlocks.notMatchBlocks[index];
        block.revertColour();
    }
    Blockly.SearchBlocks.notMatchBlocks = null;
    Blockly.SearchBlocks.matchBlocks = null;
    Blockly.SearchBlocks.collapsedBlocks = null;
    goog.events.unlisten(Blockly.TypeBlock.docKh_, 'key', Blockly.SearchBlocks.handleKey);
    goog.events.listen(Blockly.TypeBlock.docKh_, 'key', Blockly.TypeBlock.handleKey);
};

/**
* Key Handler for the workspace after user has searched for the blocks. (Outside of the search box.)
* @param {Event} e Event being listened for.
**/
Blockly.SearchBlocks.handleKey = function(e) {
  if (e.keyCode === 8 || e.keyCode === 46 || e.keyCode === 27) {
    Blockly.SearchBlocks.stop();
  } else if (e.keyCode === 37 || e.keyCode === 40){
    Blockly.SearchBlocks.zoomToSearchedBlock(-1);
  } else if (e.keyCode === 38 || e.keyCode === 39) {
    Blockly.SearchBlocks.zoomToSearchedBlock(1);
  } else {
    return;
  }

}