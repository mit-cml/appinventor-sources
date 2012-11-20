/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://code.google.com/p/blockly/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Procedure blocks for Blockly.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

/*
 Lyn's Change History: 
   [lyn, 11/29/12] Integrated into App Inventor blocks. Known bugs:
   + Reordering mutator_args in mutator_container changes references to ??? because it interprets it
     as removing and inserting rather than moving. 
   [lyn, 11/24/12] Implemented procedure parameter renaming:
   + changing a variable name in mutator_arg for procedure changes it immediately in references in body. 
   + no duplicate names are allowed in mutator_args; alpha-renaming prevents this.
   + no variables can be captured by renaming; alpha-renaming prevents this.
*/

Blockly.Language.procedures_defnoreturn = {
  // Define a procedure with no return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.LANG_PROCEDURES_DEFNORETURN_HELPURL,
  init: function() {
    this.setColour(290);
    var name = Blockly.Procedures.findLegalName(
        Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE, this);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldTextInput(name,
        Blockly.Procedures.rename), 'NAME')
        .appendTitle('', 'PARAMS');
    this.appendStatementInput('STACK')
        .appendTitle(Blockly.LANG_PROCEDURES_DEFNORETURN_DO);
    this.setMutator(new Blockly.Mutator(['procedures_mutatorarg']));
    this.setTooltip(Blockly.LANG_PROCEDURES_DEFNORETURN_TOOLTIP_1);
    this.arguments_ = [];
  },
  updateParams_: function() {
    // Check for duplicated arguments.
    var badArg = false;
    var hash = {};
    for (var x = 0; x < this.arguments_.length; x++) {
      if (hash['arg_' + this.arguments_[x].toLowerCase()]) {
        badArg = true;
        break;
      }
      hash['arg_' + this.arguments_[x].toLowerCase()] = true;
    }
    if (badArg) {
      this.setWarningText(Blockly.LANG_PROCEDURES_DEF_DUPLICATE_WARNING);
    } else {
      this.setWarningText(null);
    }
    // Merge the arguments into a human-readable list.
    var paramString = this.arguments_.join(', ');
    this.setTitleValue(paramString, 'PARAMS');
  },
  mutationToDom: function() {
    var container = document.createElement('mutation');
    for (var x = 0; x < this.arguments_.length; x++) {
      var parameter = document.createElement('arg');
      parameter.setAttribute('name', this.arguments_[x]);
      container.appendChild(parameter);
    }
    return container;
  },
  domToMutation: function(xmlElement) {
    this.arguments_ = [];
    for (var x = 0, childNode; childNode = xmlElement.childNodes[x]; x++) {
      if (childNode.nodeName.toLowerCase() == 'arg') {
        this.arguments_.push(childNode.getAttribute('name'));
      }
    }
    this.updateParams_();
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace,
                                           'procedures_mutatorcontainer');
    containerBlock.initSvg();
    // [lyn, 11/24/12] Remember the associated procedure, so can 
    // appropriately change body when update name in param block. 
    containerBlock.setProcBlock(this);
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 0; x < this.arguments_.length; x++) {
      var paramBlock = new Blockly.Block(workspace, 'procedures_mutatorarg');
      paramBlock.initSvg();
      paramBlock.setTitleValue(this.arguments_[x], 'NAME');
      // Store the old location.
      paramBlock.oldLocation = x;
      connection.connect(paramBlock.previousConnection);
      connection = paramBlock.nextConnection;
    }
    // Initialize procedure's callers with blank IDs.
    Blockly.Procedures.mutateCallers(this.getTitleValue('NAME'),
                                     this.workspace, this.arguments_, null);
    return containerBlock;
  },
  compose: function(containerBlock) {
    this.arguments_ = [];
    this.paramIds_ = [];
    var paramBlock = containerBlock.getInputTargetBlock('STACK');
    while (paramBlock) {
      this.arguments_.push(paramBlock.getTitleValue('NAME'));
      this.paramIds_.push(paramBlock.id);
      paramBlock = paramBlock.nextConnection &&
          paramBlock.nextConnection.targetBlock();
    }
    // [lyn, 11/24/12] Note: update params updates param list in proc declaration,
    // but renameParam updates procedure body appropriately.
    this.updateParams_();
    Blockly.Procedures.mutateCallers(this.getTitleValue('NAME'),
        this.workspace, this.arguments_, this.paramIds_);
  },
  dispose: function() {
    var name = this.getTitleValue('NAME');
    var editable = this.editable;
    var workspace = this.workspace;

    if (editable) {
      // Dispose of any callers.
      //Blockly.Procedures.disposeCallers(name, workspace);
      Blockly.Language.removeProcedureValues(name, workspace);
    }
    // Call parent's destructor.
    Blockly.Block.prototype.dispose.apply(this, arguments);

  },
  getProcedureDef: function() {
    // Return the name of the defined procedure,
    // a list of all its arguments,
    // and that it DOES NOT have a return value.
    return [this.getTitleValue('NAME'), this.arguments_, false];
  },
  getVars: function() {
    return this.arguments_;
  },
  renameVar: function(oldName, newName) {
    var change = false;
    for (var x = 0; x < this.arguments_.length; x++) {
      if (Blockly.Names.equals(oldName, this.arguments_[x])) {
        this.arguments_[x] = newName;
        change = true;
      }
    }
    if (change) {
      this.updateParams_();
      // Update the mutator's variables if the mutator is open.
      if (this.mutator.isVisible_()) {
        var blocks = this.mutator.workspace_.getAllBlocks();
        for (var x = 0, block; block = blocks[x]; x++) {
          if (block.type == 'procedures_mutatorarg' &&
              Blockly.Names.equals(oldName, block.getTitleValue('NAME'))) {
            block.setTitleValue(newName, 'NAME');
          }
        }
      }
    }
  },
  // [lyn, 11/24/12] return list of procedure body (if there is one)
  blocksInScope: function () {
    var body = this.getInputTargetBlock('STACK');
    return (body && [body]) || []; 
  }
};

// [lyn, 01/15/2013] Edited to remove STACK (no longer necessary with DO-THEN-RETURN)
Blockly.Language.procedures_defreturn = {
  // Define a procedure with a return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.LANG_PROCEDURES_DEFRETURN_HELPURL,
  init: function() {
    this.setColour(290);
    var name = Blockly.Procedures.findLegalName(
        Blockly.LANG_PROCEDURES_DEFRETURN_PROCEDURE, this);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldTextInput(name,
        Blockly.Procedures.rename), 'NAME')
        .appendTitle('', 'PARAMS');
    /* this.appendStatementInput('STACK')
        .appendTitle(Blockly.LANG_PROCEDURES_DEFRETURN_DO); */
    this.appendValueInput('RETURN')
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle(Blockly.LANG_PROCEDURES_DEFRETURN_RETURN);
    this.setMutator(new Blockly.Mutator(['procedures_mutatorarg']));
    this.setTooltip(Blockly.LANG_PROCEDURES_DEFRETURN_TOOLTIP_1);
    this.arguments_ = [];
  },
  // [lyn, 11/24/12] return list of procedure body (if there is one)
  blocksInScope: function () {
    /* var doBody = this.getInputTargetBlock('STACK'); */ // *** [lyn, 11/24/12] This will go away with DO-AND-RETURN block
    var returnBody = this.getInputTargetBlock('RETURN');
    // var doBodyList = (doBody && [doBody]) || []; // List of non-null doBody or empty list for null doBody
    var returnBodyList = (returnBody && [returnBody]) || []; // List of non-null returnBody or empty list for null returnBody
    // return doBodyList.concat(returnBodyList); // List of non-null body elements. 
    return returnBodyList; // List of non-null body elements. 
  },
  updateParams_: Blockly.Language.procedures_defnoreturn.updateParams_,
  mutationToDom: Blockly.Language.procedures_defnoreturn.mutationToDom,
  domToMutation: Blockly.Language.procedures_defnoreturn.domToMutation,
  decompose: Blockly.Language.procedures_defnoreturn.decompose,
  compose: Blockly.Language.procedures_defnoreturn.compose,
  dispose: Blockly.Language.procedures_defnoreturn.dispose,
  getProcedureDef: function() {
    // Return the name of the defined procedure,
    // a list of all its arguments,
    // and that it DOES have a return value.
    return [this.getTitleValue('NAME'), this.arguments_, true];
  },
  getVars: Blockly.Language.procedures_defnoreturn.getVars,
  renameVar: Blockly.Language.procedures_defnoreturn.renameVar
};

Blockly.Language.procedures_mutatorcontainer = {
  // Procedure container (for mutator dialog).
  init: function() {
    this.setColour(290);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_PROCEDURES_MUTATORCONTAINER_TITLE);
    this.appendStatementInput('STACK');
    this.setTooltip('');
    this.contextMenu = false;
  },
  // [lyn. 11/24/12] Set procBlock associated with this container. 
  setProcBlock: function (procBlock) {
    this.procBlock_ = procBlock;
  }, 
  // [lyn. 11/24/12] Set procBlock associated with this container. 
  // Invariant: should not be null, since only created as mutator for a particular proc block.
  getProcBlock: function () {
    return this.procBlock_;
  }, 
  // [lyn. 11/24/12] Return list of param names in this container
  // Invariant: there should be no duplicates!
  declaredNames: function () { 
    var paramNames = [];
    var paramBlock = this.getInputTargetBlock('STACK');
    while (paramBlock) {
      paramNames.push(paramBlock.getTitleValue('NAME')); 
      paramBlock = paramBlock.nextConnection &&
                   paramBlock.nextConnection.targetBlock();
    }
    return paramNames;
  }
};

Blockly.Language.procedures_mutatorarg = {
  // Procedure argument (for mutator dialog).
  init: function() {
    this.setColour(290);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_PROCEDURES_MUTATORARG_TITLE)
        .appendTitle(new Blockly.FieldTextInput('x',Blockly.LexicalVariable.renameParam), 'NAME');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
    this.contextMenu = false;
  }, 
  // [lyn, 11/24/12] Return the container this mutator arg is in, or null if it's not in one.
  // Dynamically calculate this by walking up chain, because mutator arg might or might not
  // be in container stack. 
  getContainerBlock: function () {
    var parent = this.getParent();
    while (parent && ! (parent.type === "procedures_mutatorcontainer")) {
      parent = parent.getParent();
    }
    // [lyn, 11/24/12] Cache most recent container block so can reference it upon removal from mutator arg stack
    this.cachedContainerBlock_ = (parent && (parent.type === "procedures_mutatorcontainer") && parent) || null;
    return this.cachedContainerBlock_;
  }, 
  // [lyn, 11/24/12] Return the procedure assocated with mutator arg is in, or null if there isn't one.
  // Dynamically calculate this by walking up chain, because mutator arg might or might not
  // be in container stack. 
  getProcBlock: function () {
    var container = this.getContainerBlock();
    return (container && container.getProcBlock()) || null;
  }, 
  // [lyn, 11/24/12] Return the declared names in the procedure assocoated with mutator arg, 
  // or the empty list if there isn't one.
  // Dynamically calculate this by walking up chain, because mutator arg might or might not
  // be in container stack. 
  declaredNames: function () { 
    var container = this.getContainerBlock(); 
    return (container && container.declaredNames()) || [];
  },
  // [lyn, 11/24/12] Return the blocks in scope of proc params in the the procedure associated with mutator arg, 
  // or the empty list if there isn't one.
  // Dynamically calculate this by walking up chain, because mutator arg might or might not
  // be in container stack. 
  blocksInScope: function () { 
    var proc = this.getProcBlock(); 
    return (proc && proc.blocksInScope()) || [];
  },
  // [lyn, 11/24/12] Check for situation in which mutator arg has been removed from stack,
  // and change all references to its name to ???.
  onchange: function() {
    var paramName = this.getTitleValue('NAME');
    if (paramName) { // paramName is null when delete from stack
      // console.log("Mutatorarg onchange: " + paramName);
      var cachedContainer = this.cachedContainerBlock_;
      var container = this.getContainerBlock(); // Order is important; this must come after cachedContainer
                                                // since it sets cachedContainerBlock_       
      // console.log("Mutatorarg onchange: " + paramName 
      //            + "; cachedContainer = " + JSON.stringify((cachedContainer && cachedContainer.type) || null)
      //            + "; container = " + JSON.stringify((container && container.type) || null));
      if ((! cachedContainer) && container) {
        // Event: added mutator arg to container stack
        // console.log("Mutatorarg onchange ADDED: " + paramName);        
        var declaredNames = this.declaredNames();
        var firstIndex = declaredNames.indexOf(paramName);
        if (firstIndex != -1) { 
          // Assertion: we should get here, since paramName should be among names
          var secondIndex = declaredNames.indexOf(paramName, firstIndex+1);
          if (secondIndex != -1) {
            // If we get here, there is a duplicate on insertion that must be resolved
            var newName = Blockly.FieldLexicalVariable.nameNotIn(paramName,declaredNames);
            this.setTitleValue(newName, 'NAME');
          }
        }
      } /* else if (cachedContainer && (! container)) {
        // Event: removed mutator arg from container stack
        // [lyn, 11/24/12] Mutator arg has been removed from stack. Change all references to its name to ???
        // console.log("Mutatorarg onchange REMOVED: " + paramName);
        var proc = cachedContainer.getProcBlock();
        var inScopeBlocks = (proc && proc.blocksInScope()) || [];
        var referenceResults = inScopeBlocks.map( function(blk) { return Blockly.LexicalVariable.referenceResult(blk, paramName, []); } );
        var blocksToRename = [];
        for (var r = 0; r < referenceResults.length; r++) {
          blocksToRename = blocksToRename.concat(referenceResults[r][0]);
          // ignore capturables, which are not relevant here.
        }
        // Rename getters and setters
        for (var i = 0; i < blocksToRename.length; i++) {
          var block = blocksToRename[i];
          var renamingFunction = block.renameLexicalVar;
          if (renamingFunction) {
            renamingFunction.call(block, "param " + paramName, "???");
          }
        }
      }*/
    }
  }
};

Blockly.Language.procedures_mutatorarg.validator = function(newVar) {
  // Merge runs of whitespace.  Strip leading and trailing whitespace.
  // Beyond this, all names are legal.
  newVar = newVar.replace(/[\s\xa0]+/g, ' ').replace(/^ | $/g, '');
  return newVar || null;
};

Blockly.Language.procedures_callnoreturn = {
  // Call a procedure with no return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.LANG_PROCEDURES_CALLNORETURN_HELPURL,
  init: function() {
    this.setColour(290);
    var procNamesFxn = function(){return Blockly.Language.getProcedureNames(false);};
    var onChangeDropDown = function(text) {
      var workspace = this.block.workspace;
      this.setText(text);
      if(text == "") {
        for(var i=0;this.block.getInput('ARG' + i) != null;i++){
          this.block.removeInput('ARG' + i)
        }
        return;
      }
      var def = Blockly.Procedures.getDefinition(text, workspace);
      if(def.paramIds_ == null){
        def.mutator.setVisible(true);
        def.mutator.shouldHide = true;
      }
      this.block.setProcedureParameters(def.arguments_, def.paramIds_,true);
    };
    this.procDropDown = new Blockly.FieldDropdown(procNamesFxn,onChangeDropDown);
    this.procDropDown.block = this;
    this.appendDummyInput()
        .appendTitle("call ")
        .appendTitle(this.procDropDown,"PROCNAME");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_PROCEDURES_CALLNORETURN_TOOLTIP_1);
    this.arguments_ = [];
    this.quarkConnections_ = null;
    this.quarkArguments_ = null;
  },
  getProcedureCall: function() {
    return this.getTitleValue('PROCNAME');
  },
  renameProcedure: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleValue('PROCNAME'))) {
      this.setTitleValue(newName, 'PROCNAME');
    }
  },
  setProcedureParameters: function(paramNames, paramIds,fromChange) {
    // Data structures for parameters on each call block:
    // this.arguments = ['x', 'y']
    //     Existing param names.
    // paramNames = ['x', 'y', 'z']
    //     New param names.
    // paramIds = ['piua', 'f8b_', 'oi.o']
    //     IDs of params (consistent for each parameter through the life of a
    //     mutator, regardless of param renaming).
    // this.quarkConnections_ {piua: null, f8b_: Blockly.Connection}
    //     Look-up of paramIds to connections plugged into the call block.
    // this.quarkArguments_ = ['piua', 'f8b_']
    //     Existing param IDs.
    // Note that quarkConnections_ may include IDs that no longer exist, but
    // which might reappear if a param is reattached in the mutator.
    if (!paramIds) {
      // Reset the quarks (a mutator is about to open).
      this.quarkConnections_ = {};
      this.quarkArguments_ = null;
      return;
    }
    if (paramIds.length != paramNames.length) {
      throw 'Error: paramNames and paramIds must be the same length.';
    }
    if(typeof fromChange == "undefined") {
      fromChange = null;
    }
    
    if (!this.quarkArguments_ || fromChange) {
      // Initialize tracking for this block.
      this.quarkConnections_ = {};
      if (paramNames.join('\n') == this.arguments_.join('\n') || fromChange) {
        // No change to the parameters, allow quarkConnections_ to be
        // populated with the existing connections.
        this.quarkArguments_ = paramIds;
      } else {
        this.quarkArguments_ = [];
      }
    }
    // Switch off rendering while the block is rebuilt.
    var savedRendered = this.rendered;
    this.rendered = false;
    // Update the quarkConnections_ with existing connections.
    for (var x = this.arguments_.length - 1; x >= 0; x--) {
      var input = this.getInput('ARG' + x);
      if (input) {
        var connection = input.connection.targetConnection;
        this.quarkConnections_[this.quarkArguments_[x]] = connection;
        // Disconnect all argument blocks and remove all inputs.
        this.removeInput('ARG' + x);
      }
    }
    // Rebuild the block's arguments.
    this.arguments_ = [].concat(paramNames);
    this.quarkArguments_ = paramIds;
    for (var x = 0; x < this.arguments_.length; x++) {
      var input = this.appendValueInput('ARG' + x)
          .setAlign(Blockly.ALIGN_RIGHT)
          .appendTitle(this.arguments_[x]);
      if (this.quarkArguments_) {
        // Reconnect any child blocks.
        var quarkName = this.quarkArguments_[x];
        if (quarkName in this.quarkConnections_) {
          var connection = this.quarkConnections_[quarkName];
          if (!connection || connection.targetConnection ||
              connection.sourceBlock_.workspace != this.workspace) {
            // Block no longer exists or has been attached elsewhere.
            delete this.quarkConnections_[quarkName];
          } else {
            input.connection.connect(connection);
          }
        }
      }
    }
    // Restore rendering and show the changes.
    this.rendered = savedRendered;
    if (this.rendered) {
      this.render();
    }
  },
  mutationToDom: function() {
    // Save the name and arguments (none of which are editable).
    var container = document.createElement('mutation');
    container.setAttribute('name', this.getTitleValue('PROCNAME'));
    for (var x = 0; x < this.arguments_.length; x++) {
      var parameter = document.createElement('arg');
      parameter.setAttribute('name', this.arguments_[x]);
      container.appendChild(parameter);
    }
    return container;
  },
  domToMutation: function(xmlElement) {
    // Restore the name and parameters.
    var name = xmlElement.getAttribute('name');
    this.setTitleValue(name, 'PROCNAME');
    var def = Blockly.Procedures.getDefinition(name, this.workspace);
    if (def && def.mutator.isVisible()) {
      // Initialize caller with the mutator's IDs.
      this.setProcedureParameters(def.arguments_, def.paramIds_);
    } else {
      this.arguments_ = [];
      for (var x = 0, childNode; childNode = xmlElement.childNodes[x]; x++) {
        if (childNode.nodeName.toLowerCase() == 'arg') {
          this.arguments_.push(childNode.getAttribute('name'));
        }
      }
      // For the second argument (paramIds) use the arguments list as a dummy
      // list.
      this.setProcedureParameters(this.arguments_, this.arguments_);
    }
  },
  renameVar: function(oldName, newName) {
    for (var x = 0; x < this.arguments_.length; x++) {
      if (Blockly.Names.equals(oldName, this.arguments_[x])) {
        this.arguments_[x] = newName;
        this.getInput('ARG' + x).titleRow[0].setText(newName);
      }
    }
  },
  procCustomContextMenu: function(options) {
    // Add option to find caller.
    var option = {enabled: true};
    option.text = Blockly.LANG_PROCEDURES_HIGHLIGHT_DEF;
    var name = this.getTitleValue('NAME');
    var workspace = this.workspace;
    option.callback = function() {
      var def = Blockly.Procedures.getDefinition(name, workspace);
      def && def.select();
    };
    options.push(option);
  },
  removeProcedureValue: function() {
    this.setTitleValue("none", 'PROCNAME');
    for(var i=0;this.getInput('ARG' + i) != null;i++) {
      this.removeInput('ARG' + i);
    }
  }
};

Blockly.Language.procedures_callreturn = {
  // Call a procedure with a return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.LANG_PROCEDURES_CALLRETURN_HELPURL,
  init: function() {
    this.setColour(290);
    var procNamesFxn = function(){return Blockly.Language.getProcedureNames(true);};
    var onChangeDropDown = function(text) {
      var workspace = this.block.workspace;
      this.setText(text);
      if(text == "") {
        for(var i=0;this.block.getInput('ARG' + i) != null;i++){
          this.block.removeInput('ARG' + i);
        }
        return;
      }
      var def = Blockly.Procedures.getDefinition(text, workspace);
      if(def.paramIds_ == null){
        def.mutator.setVisible(true);
        def.mutator.shouldHide = true;
      }
      this.block.setProcedureParameters(def.arguments_, def.paramIds_);
    };
    this.procDropDown = new Blockly.FieldDropdown(procNamesFxn,onChangeDropDown);
    this.procDropDown.block = this;
    this.appendDummyInput()
        .appendTitle("call ")
        .appendTitle(this.procDropDown,"PROCNAME");
    this.setOutput(true, null);
    this.setTooltip(Blockly.LANG_PROCEDURES_CALLRETURN_TOOLTIP_1);
    this.arguments_ = [];
    this.quarkConnections_ = null;
    this.quarkArguments_ = null;
  },
  getProcedureCall: Blockly.Language.procedures_callnoreturn.getProcedureCall,
  renameProcedure: Blockly.Language.procedures_callnoreturn.renameProcedure,
  setProcedureParameters:
      Blockly.Language.procedures_callnoreturn.setProcedureParameters,
  mutationToDom: Blockly.Language.procedures_callnoreturn.mutationToDom,
  domToMutation: Blockly.Language.procedures_callnoreturn.domToMutation,
  renameVar: Blockly.Language.procedures_callnoreturn.renameVar,
  procCustomContextMenu: Blockly.Language.procedures_callnoreturn.procCustomContextMenu,
  removeProcedureValue: Blockly.Language.procedures_callnoreturn.removeProcedureValue
};

Blockly.Language.getProcedureNames = function(returnValue) {
  var topBlocks = Blockly.mainWorkspace.getTopBlocks();
  var procNameArray = [["","none"]];
  for(var i=0;i<topBlocks.length;i++){
    var procName = topBlocks[i].getTitleValue('NAME');
    if(topBlocks[i].type == "procedures_defnoreturn" && !returnValue) {
      procNameArray.push([procName,procName]);
    } else if (topBlocks[i].type == "procedures_defreturn" && returnValue) {
      procNameArray.push([procName,procName]);
    }
  }
  return procNameArray;

}


Blockly.Language.removeProcedureValues = function(name, workspace) {
  var blockArray = Blockly.mainWorkspace.getAllBlocks();
  for(var i=0;i<blockArray.length;i++){
    var block = blockArray[i];
    if(block.type == "procedures_callreturn" || block.type == "procedures_callnoreturn") {
      if(block.getTitleValue('PROCNAME') == name) {
        block.removeProcedureValue();
      }
    }
  }
}
