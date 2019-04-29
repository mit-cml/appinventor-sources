//Copyright © 2013-2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview File to handle 'Type Blocking'. When the user starts typing the
 * name of a Block in the workspace, a series of suggestions will appear. Upon
 * selecting one (enter key), the chosen block will be created in the workspace
 * This file needs additional configuration through the inject method.
 * @author josmasflores@gmail.com (Jose Dominguez)
 */
'use strict';

goog.provide('Blockly.TypeBlock');
goog.require('Blockly.Xml');

goog.require('goog.events');
goog.require('goog.events.KeyCodes');
goog.require('goog.events.KeyHandler');
goog.require('goog.ui.ac');
goog.require('goog.style');

goog.require('goog.ui.ac.ArrayMatcher');
goog.require('goog.ui.ac.AutoComplete');
goog.require('goog.ui.ac.InputHandler');
goog.require('goog.ui.ac.Renderer');

/**
 * Main Type Block function for configuration.
 * @param {Object} htmlConfig an object of the type:
     {
       frame: 'ai_frame',
       typeBlockDiv: 'ai_type_block',
       inputText: 'ac_input_text'
     }
 * @param {Blockly.WorkspaceSvg} workspace The workspace targeted by the TypeBlock
 * stating the ids of the attributes to be used in the html enclosing page
 * create a new block
 */
Blockly.TypeBlock = function( htmlConfig, workspace ){
  this.workspace_ = workspace;
  /**
   * Used as an optimisation trick to avoid reloading components and built-ins unless there is a real
   * need to do so. needsReload.components can be set to true when a component changes.
   * Defaults to true so that it loads the first time (set to null after loading in lazyLoadOfOptions_())
   * @type {{components: boolean}}
   */
  this.needsReload = {
    components: true
  };
  var frame = htmlConfig['frame'];
  this.typeBlockDiv_ = htmlConfig['typeBlockDiv'];
  this.inputText_ = htmlConfig['inputText'];

  this.docKh_ = new goog.events.KeyHandler(goog.dom.getElement(top.document.body));
  this.inputKh_ = new goog.events.KeyHandler(goog.dom.getElement(this.inputText_));
  this.handleKeyWrapper_ = this.handleKey.bind(this);
  goog.events.listen(this.docKh_, 'key', this.handleKeyWrapper_);
  // Create the auto-complete panel
  this.createAutoComplete_(this.inputText_);
};

/**
 * Div where the type block panel will be rendered
 * @private
 */
Blockly.TypeBlock.prototype.typeBlockDiv_ = null;

/**
 * input text contained in the type block panel used as input
 * @private
 */
Blockly.TypeBlock.prototype.inputText_ = null;

/**
 * Document key handler applied to the frame area, and used to catch keyboard
 * events. It is detached when the Type Block panel is shown, and
 * re-attached when the Panel is dismissed.
 * @private
 */
Blockly.TypeBlock.prototype.docKh_ = null;

/**
 * Input key handler applied to the Type Block Panel, and used to catch
 * keyboard events. It is attached when the Type Block panel is shown, and
 * dettached when the Panel is dismissed.
 * @private
 */
Blockly.TypeBlock.prototype.inputKh_ = null;

/**
 * Is the Type Block panel currently showing?
 */
Blockly.TypeBlock.prototype.visible = false;

/**
 * Mapping of options to show in the auto-complete panel. This maps the
 * canonical name of the block, needed to create a new Blockly.Block, with the
 * internationalised word or sentence used in typeblocks. Certain blocks do not only need the
 * canonical block representation, but also values for dropdowns (name and value)
 *   - No dropdowns:   this.typeblock: [{ translatedName: Blockly.LANG_VAR }]
 *   - With dropdowns: this.typeblock: [{ translatedName: Blockly.LANG_VAR },
 *                                        dropdown: {
 *                                          titleName: 'TITLE', value: 'value'
 *                                        }]
 *   - Additional types can be used to mark a block as isProcedure or isGlobalVar. These are only
 *   used to manage the loading of options in the auto-complete matcher.
 * @private
 */
Blockly.TypeBlock.prototype.TBOptions_ = {};

/**
 * This array contains only the Keys of Blockly.TypeBlock.TBOptions_ to be used
 * as options in the autocomplete widget.
 * @private
 */
Blockly.TypeBlock.prototype.TBOptionsNames_ = [];

/**
 * pointer to the automcomplete widget to be able to change its contents when
 * the Language tree is modified (additions, renaming, or deletions)
 * @private
 */
Blockly.TypeBlock.prototype.ac_ = null;

/**
 * We keep a listener pointer in case of needing to unlisten to it. We only want
 * one listener at a time, and a reload could create a second one, so we
 * unlisten first and then listen back
 * @private
 */
Blockly.TypeBlock.prototype.currentListener_ = null;

/**
 *
 * @param {goog.events.KeyEvent} e
 */
Blockly.TypeBlock.prototype.handleKey = function(e){
    if (Blockly.mainWorkspace !== this.workspace_) return;  // not targeting this workspace
    // test blocks editor displayed/visible to user
    if (!this.workspace_.getParentSvg() ||
        this.workspace_.getParentSvg().parentElement.offsetParent == null) return;
    // Don't steal input from Blockly fields.
    if (e.target != this.ac_.getTarget() &&
      (e.target.tagName == 'INPUT' || e.target.tagName == 'TEXTAREA')) return;
    if (e.altKey || e.ctrlKey || e.metaKey || e.keyCode === 9) return; // 9 is tab
    //We need to duplicate delete handling here from blockly.js
    if (e.keyCode === 8 || e.keyCode === 46) {
      // Delete or backspace.
      // If the panel is showing the panel, just return to allow deletion in the panel itself
      if (goog.style.isElementShown(goog.dom.getElement(this.typeBlockDiv_))) return;
      // if id is empty, it is deleting inside a block title
      if (e.target.id === '') return;
      // only when selected and deletable, actually delete the block
      Blockly.onKeyDown_(e);
      return;
    }
    if (e.keyCode === 27){ //Dismiss the panel with esc
      this.hide();
      Blockly.mainWorkspace.getParentSvg().parentNode.focus();  // refocus workspace div
      Blockly.mainWorkspace.hideChaff();
      return;
    }
    switch (e.keyCode) {
      case 9:   // Tab
      case 16:  // Enter
      case 17:  // Ctrl
      case 18:  // Alt
      case 19:  // Home
      case 20:  // Caps Lock
      case 33:  // Page Up
      case 34:  // Page Down
      case 35:  // Shift
      case 36:  // End
      case 37:  // Left Arrow
      case 38:  // Up Arrow
      case 39:  // Right Arrow
      case 40:  // Down Arrow
      case 45:  // Ins
      case 91:  // Meta
      case 112: // F1
      case 113: // F2
      case 114: // F3
      case 115: // F4
      case 116: // F5
      case 117: // F6
      case 118: // F7
      case 119: // F8
      case 120: // F9
      case 121: // F10
      case 122: // F11
      case 123: // F12
        return;
    }
    if (goog.style.isElementShown(goog.dom.getElement(this.typeBlockDiv_))) {
      // Enter in the panel makes it select an option
      if (e.keyCode === 13) {
        this.hide();
      }
    }
    else if (e.keyCode != 13) {
      this.show();
      // Can't seem to make Firefox display first character, so keep all browsers from automatically
      // displaying the first character and add it manually.
      e.preventDefault();
      if (e.charCode == 0) {
        // Don't try to render a non-printing character.
        return;
      }
      goog.dom.getElement(this.inputText_).value =
        String.fromCharCode(e.charCode != null ? e.charCode : e.keyCode);
    }
  };

/**
 * function to hide the autocomplete panel. Also used from hideChaff in
 * Blockly.js
 */
Blockly.TypeBlock.prototype.hide = function(){
//  if (this.typeBlockDiv_ == null)
//    return;
  goog.style.showElement(goog.dom.getElement(this.typeBlockDiv_), false);
  goog.events.unlisten(this.docKh_, 'key', this.handleKeyWrapper_);
  goog.events.unlisten(this.inputKh_, 'key', this.handleKeyWrapper_);
  this.handleKeyWrapper_ = this.handleKey.bind(this);
  goog.events.listen(this.docKh_, 'key', this.handleKeyWrapper_);
  this.visible = false;
};

/**
 * function to show the auto-complete panel to start typing block names
 */
Blockly.TypeBlock.prototype.show = function(){
  this.lazyLoadOfOptions_();
  var panel = goog.dom.getElement(this.typeBlockDiv_);
  goog.style.setStyle(panel, 'top', this.workspace_.latestClick.y);
  goog.style.setStyle(panel, 'left', this.workspace_.latestClick.x);
  goog.style.showElement(panel, true);
  goog.dom.getElement(this.inputText_).focus();
  // If the input gets cleaned before adding the handler, all keys are read
  // correctly (at times it was missing the first char)
  goog.dom.getElement(this.inputText_).value = '';
  goog.events.unlisten(this.docKh_, 'key', this.handleKeyWrapper_);
  goog.events.unlisten(this.inputKh_, 'key', this.handleKeyWrapper_);
  this.handleKeyWrapper_ = this.handleKey.bind(this);
  goog.events.listen(this.inputKh_, 'key', this.handleKeyWrapper_);
  this.visible = true;
};

/**
 * Lazily loading options because some of them are not available during bootstrapping, and some
 * users will never use this functionality, so we avoid having to deal with changes such as handling
 * renaming of variables and procedures (leaving it until the moment they are used, if ever).
 * @private
 */
Blockly.TypeBlock.prototype.lazyLoadOfOptions_ = function () {

  // Optimisation to avoid reloading all components and built-in objects unless it is needed.
  // needsReload.components is setup when adding/renaming/removing a component in components.js
  if (this.needsReload.components){
    this.generateOptions();
    this.needsReload.components = null;
  }
  this.loadGlobalVariables_();
  this.loadProcedures_();
  this.reloadOptionsAfterChanges_();
};

/**
 * This function traverses the Language tree and re-creates all the options
 * available for type blocking. It's needed in the case of modifying the
 * Language tree after its creation (adding or renaming components, for instance).
 * It also loads all the built-in blocks.
 *
 * call 'reloadOptionsAfterChanges_' after calling this. The function lazyLoadOfOptions_ is an
 * example of how to call this function.
 */
Blockly.TypeBlock.prototype.generateOptions = function() {

  var buildListOfOptions = function() {
    var listOfOptions = {};
    var typeblockArray;
    for (var name in Blockly.Blocks) {
      var block = Blockly.Blocks[name];
      if(block.typeblock){
        typeblockArray = block.typeblock;
        if(typeof block.typeblock == "function") {
          typeblockArray = block.typeblock();
        }
        createOption(typeblockArray, name);
      }
    }

    function createOption(tb, canonicName){
      if (tb){
        goog.array.forEach(tb, function(dd){
          var dropDownValues = {};
          var mutatorAttributes = {};
          if (dd.dropDown){
            if (dd.dropDown.titleName && dd.dropDown.value){
              dropDownValues.titleName = dd.dropDown.titleName;
              dropDownValues.value = dd.dropDown.value;
            }
            else {
              throw new Error('TypeBlock not correctly set up for ' + canonicName);
            }
          }
          if(dd.mutatorAttributes) {
            mutatorAttributes = dd.mutatorAttributes;
          }
          listOfOptions[dd.translatedName] = {
            canonicName: canonicName,
            dropDown: dropDownValues,
            mutatorAttributes: mutatorAttributes
          };
        });
      }
    }

    return listOfOptions;
  };

  // This is called once on startup, and it will contain all built-in blocks. After that, it can
  // be called on demand (for instance in the function lazyLoadOfOptions_)
  this.TBOptions_ = buildListOfOptions();
};

/**
 * This function reloads all the latest changes that might have occurred in the language tree or
 * the structures containing procedures and variables. It only needs to be called once even if
 * different sources are being updated at the same time (call on load proc, load vars, and generate
 * options, only needs one call of this function; and example of that is lazyLoadOfOptions_
 * @private
 */
Blockly.TypeBlock.prototype.reloadOptionsAfterChanges_ = function () {
  this.TBOptionsNames_ = goog.object.getKeys(this.TBOptions_);
  goog.array.sort(this.TBOptionsNames_);
  this.ac_.matcher_.setRows(this.TBOptionsNames_);
};

/**
 * Loads all procedure names as options for TypeBlocking. It is used lazily from show().
 * Call 'reloadOptionsAfterChanges_' after calling this one. The function lazyLoadOfOptions_ is an
 * example of how to call this function.
 * @private
 */
Blockly.TypeBlock.prototype.loadProcedures_ = function(){
  // Clean up any previous procedures in the list.
  this.TBOptions_ = goog.object.filter(this.TBOptions_,
      function(opti){ return !opti.isProcedure;});

  var procsNoReturn = createTypeBlockForProcedures_.call(this, false);
  var self = this;
  goog.array.forEach(procsNoReturn, function(pro){
    self.TBOptions_[pro.translatedName] = {
      canonicName: 'procedures_callnoreturn',
      dropDown: pro.dropDown,
      isProcedure: true // this attribute is used to clean up before reloading
    };
  });

  var procsReturn = createTypeBlockForProcedures_.call(this, true);
  goog.array.forEach(procsReturn, function(pro){
    self.TBOptions_[pro.translatedName] = {
      canonicName: 'procedures_callreturn',
      dropDown: pro.dropDown,
      isProcedure: true
    };
  });

  /**
   * Procedure names can be collected for both 'with return' and 'no return' varieties from
   * getProcedureNames()
   * @param {boolean} withReturn indicates if the query us for 'with':true or 'no':false return
   * @returns {Array} array of the procedures requested
   * @private
   */
  function createTypeBlockForProcedures_(withReturn) {
    var options = [];
    var procNames = this.workspace_.getProcedureDatabase().getMenuItems(withReturn);
    if (procNames.length == 1 && procNames[0][0] == '') {
      procNames = [];
    }
    goog.array.forEach(procNames, function(proc){
      options.push(
          {
            translatedName: Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL + proc[0],
            dropDown: {
              titleName: 'PROCNAME',
              value: proc[1]
            }
          }
      );
    });
    return options;
  }
};

/**
 * Loads all global variable names as options for TypeBlocking. It is used lazily from show().
 * Call 'reloadOptionsAfterChanges_' after calling this one. The function lazyLoadOfOptions_ is an
 * example of how to call this function.
 */
Blockly.TypeBlock.prototype.loadGlobalVariables_ = function () {
  //clean up any previous procedures in the list
  this.TBOptions_ = goog.object.filter(this.TBOptions_,
      function(opti){ return !opti.isGlobalvar;});

  var globalVarNames = createTypeBlockForVariables_.call(this);
  var self = this;
  goog.array.forEach(globalVarNames, function(varName){
    var canonicalN;
    if (varName.translatedName.substring(0,3) === 'get')
      canonicalN = 'lexical_variable_get';
    else
      canonicalN = 'lexical_variable_set';
    self.TBOptions_[varName.translatedName] = {
      canonicName: canonicalN,
      dropDown: varName.dropDown,
      isGlobalvar: true
    };
  });

  /**
   * Create TypeBlock options for global variables (a setter and a getter for each).
   * @returns {Array} array of global var options
   */
  function createTypeBlockForVariables_() {
    var options = [];
    var varNames = Blockly.FieldLexicalVariable.getGlobalNames();
    // Make a setter and a getter for each of the names
    goog.array.forEach(varNames, function(varName){
      options.push(
          {
            translatedName: 'get global ' + varName,
            dropDown: {
              titleName: 'VAR',
              value: 'global ' + varName
            }
          }
      );
      options.push(
          {
            translatedName: 'set global ' + varName,
            dropDown: {
              titleName: 'VAR',
              value: 'global ' + varName
            }
          }
      );
    });
    return options;
  }
};

/**
 * Creates the auto-complete panel, powered by Google Closure's ac widget
 * @private
 */
Blockly.TypeBlock.prototype.createAutoComplete_ = function(inputText){
  this.TBOptionsNames_ = goog.object.getKeys( this.TBOptions_ );
  goog.array.sort(this.TBOptionsNames_);
  goog.events.unlistenByKey(this.currentListener_); //if there is a key, unlisten
  if (this.ac_)
    this.ac_.dispose(); //Make sure we only have 1 at a time

  // 3 objects needed to create a goog.ui.ac.AutoComplete instance
  var matcher = new Blockly.TypeBlock.ac.AIArrayMatcher(this.TBOptionsNames_, false);
  var renderer = new goog.ui.ac.Renderer();
  var inputHandler = new goog.ui.ac.InputHandler(null, null, false);

  this.ac_ = new goog.ui.ac.AutoComplete(matcher, renderer, inputHandler);
  this.ac_.setMaxMatches(100); //Renderer has a set height of 294px and a scroll bar.
  inputHandler.attachAutoComplete(this.ac_);
  inputHandler.attachInputs(goog.dom.getElement(inputText));

  var self = this;
  this.currentListener_ = goog.events.listen(this.ac_,
      goog.ui.ac.AutoComplete.EventType.UPDATE,
    function() {
      var blockName = goog.dom.getElement(inputText).value;
      var blockToCreate = goog.object.get(self.TBOptions_, blockName);
      if (!blockToCreate) {
        //If the input passed is not a block, check if it is a number or a pre-populated text block
        var numberReg = new RegExp('^-?[0-9]\\d*(\.\\d+)?$', 'g');
        var numberMatch = numberReg.exec(blockName);
        var textReg = new RegExp('^[\"|\']+', 'g');
        var textMatch = textReg.exec(blockName);
        if (numberMatch && numberMatch.length > 0){
          blockToCreate = {
            canonicName: 'math_number',
            dropDown: {
              titleName: 'NUM',
              value: blockName
            }
          };
        }
        else if (textMatch && textMatch.length === 1){
          blockToCreate = {
            canonicName: 'text',
            dropDown: {
              titleName: 'TEXT',
              value: blockName.substring(1)
            }
          };
        }
        else
          return; // block does not exist: return
      }

      var blockToCreateName = '';
      var block;
      if (blockToCreate.dropDown){ //All blocks should have a dropDown property, even if empty
        blockToCreateName = blockToCreate.canonicName;
        // components have mutator attributes we need to deal with. We can also add these for special blocks
        //   e.g., this is done for create empty list
        if(!goog.object.isEmpty(blockToCreate.mutatorAttributes)) {
          //construct xml
          var xmlString = '<xml><block type="' + blockToCreateName + '"><mutation ';
          for(var attributeName in blockToCreate.mutatorAttributes) {
            xmlString += attributeName + '="' + blockToCreate.mutatorAttributes[attributeName] + '" ';
          }

          xmlString += '>';
          xmlString += '</mutation></block></xml>';
          var xml = Blockly.Xml.textToDom(xmlString);
          block = Blockly.Xml.domToBlock(xml.firstChild, self.workspace_);
        } else {
          block = self.workspace_.newBlock(blockToCreateName);
          block.initSvg(); //Need to init the block before doing anything else
          if (block.type && (block.type == "procedures_callnoreturn" || block.type == "procedures_callreturn")) {
            //Need to make sure Procedure Block inputs are updated
            Blockly.FieldProcedure.onChange.call(block.getField("PROCNAME"), blockToCreate.dropDown.value);
          }
        }

        if (blockToCreate.dropDown.titleName && blockToCreate.dropDown.value){
          block.setFieldValue(blockToCreate.dropDown.value, blockToCreate.dropDown.titleName);
          // change type checking for split blocks
          if(blockToCreate.dropDown.value == 'SPLITATFIRST' || blockToCreate.dropDown.value == 'SPLIT') {
            block.getInput("AT").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("text",Blockly.Blocks.Utilities.INPUT));
          } else if(blockToCreate.dropDown.value == 'SPLITATFIRSTOFANY' || blockToCreate.dropDown.value == 'SPLITATANY') {
            block.getInput("AT").setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType("list",Blockly.Blocks.Utilities.INPUT));
          }
        }
      } else {
        throw new Error('Type Block not correctly set up for: ' + blockToCreateName);
      }
      block.render();
      var blockSelected = Blockly.selected;
      var selectedX, selectedY, selectedXY;
      if (blockSelected) {
        selectedXY = blockSelected.getRelativeToSurfaceXY();
        selectedX = selectedXY.x;
        selectedY = selectedXY.y;
        self.connectIfPossible(blockSelected, block);
        if(!block.parentBlock_){
          //Place it close but a bit out of the way from the one we created.
          block.moveBy(Blockly.selected.getRelativeToSurfaceXY().x + 110,
              Blockly.selected.getRelativeToSurfaceXY().y + 50);
        }
        block.select();
      }
      else {
        //calculate positions relative to the view and the latest click
        var left = self.workspace_.latestClick.x;
        var top = self.workspace_.latestClick.y;
        block.moveBy(left, top);
        block.select();
      }
      self.workspace_.requestErrorChecking(block);
      self.hide();
      self.workspace_.getParentSvg().parentNode.focus();  // refocus workspace div
    }
  );
};

/**
 * Blocks connect in different ways; a block with an outputConnection such as
 * a number will connect in one of its parent's input connection (inputLis).                          .
 * A block with no outputConnection could be connected to its parent's next
 * connection.
 */
Blockly.TypeBlock.prototype.connectIfPossible = function(blockSelected, createdBlock) {
  var i = 0,
    inputList = blockSelected.inputList,
    ilLength = inputList.length;

  //If createdBlock has an output connection, we need to:
  //  connect to parent (eg: connect equals into if)
  //else we need to:
  //  connect its previousConnection to parent (eg: connect if to if)
  for (i = 0; i < ilLength; i++){
    try {
      if (createdBlock.outputConnection != null){
        //Check for type validity (connect does not do it)
        if ( inputList[i].connection &&
             inputList[i].connection.checkType_(createdBlock.outputConnection) ){
            if (!inputList[i].connection.targetConnection){ // is connection empty?
              createdBlock.outputConnection.connect(inputList[i].connection);
              break;
            }
        }
      }
      else {
        createdBlock.previousConnection.connect(inputList[i].connection);
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

//--------------------------------------
// A custom matcher for the auto-complete widget that can handle numbers as well as the default
// functionality of goog.ui.ac.ArrayMatcher
goog.provide('Blockly.TypeBlock.ac.AIArrayMatcher');

goog.require('goog.iter');
goog.require('goog.string');

/**
 * Extension of goog.ui.ac.ArrayMatcher so that it can handle any number typed in.
 * @constructor
 * @param {Array} rows Dictionary of items to match.  Can be objects if they
 * have a toString method that returns the value to match against.
 * @param {boolean=} opt_noSimilar if true, do not do similarity matches for the
 * input token against the dictionary.
 * @extends {goog.ui.ac.ArrayMatcher}
 */
Blockly.TypeBlock.ac.AIArrayMatcher = function(rows, opt_noSimilar) {
  goog.ui.ac.ArrayMatcher.call(rows, opt_noSimilar);
  this.rows_ = rows;
  this.useSimilar_ = !opt_noSimilar;
};
goog.inherits(Blockly.TypeBlock.ac.AIArrayMatcher, goog.ui.ac.ArrayMatcher);

/**
 * @inheritDoc
 */
Blockly.TypeBlock.ac.AIArrayMatcher.prototype.requestMatchingRows = function(token, maxMatches,
    matchHandler, opt_fullString) {

  var matches = this.getPrefixMatches(token, maxMatches);

  //Because we allow for similar matches, Button.Text will always appear before Text
  //So we handle the 'text' case as a special case here
  if (token === 'text' || token === 'Text'){
    goog.array.remove(matches, 'Text');
    goog.array.insertAt(matches, 'Text', 0);
  }

  // Added code to handle any number typed in the widget (including negatives and decimals)
  var reg = new RegExp('^-?[0-9]\\d*(\.\\d+)?$', 'g');
  var match = reg.exec(token);
  if (match && match.length > 0){
    matches.push(token);
  }

  // Added code to handle default values for text fields (they start with " or ')
  var textReg = new RegExp('^[\"|\']+', 'g');
  var textMatch = textReg.exec(token);
  if (textMatch && textMatch.length === 1){
    matches.push(token);
  }

  if (matches.length === 0 && this.useSimilar_) {
    matches = this.getSimilarRows(token, maxMatches);
  }

  matchHandler(token, matches);
};

Blockly.TypeBlock.hide = function() {
  if (Blockly.mainWorkspace && Blockly.mainWorkspace.typeBlock_)
    Blockly.mainWorkspace.typeBlock_.hide();
};
