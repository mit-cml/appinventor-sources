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
 * @fileoverview Blocks for lexically scoped variables in App Inventor
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */
'use strict';

/*
 Lyn's History: 
   [lyn, 11/18/12] Renaming for globals (still working on renaming of procedure and loop params)
   [lyn, 11/17/12] Integration of simple naming into App Inventor
   [lyn, 11/11/12] More work on onchange event. Allow invalid names for untethered getters/setters 
                   on workspace, but not when click in to other blocks. 
   [lyn, 11/08-10/12] Get dropdown list of names in scope to work for globals and params 
                      (including loops) in raw blockly. Pass along to Andrew for integration
                      into AI. Initial work on onchange event to change names when getters/setters
                      copied and moved.
   [lyn, 11/05-07/12] Add local variable declaration expressions. Get mutator working for local 
                      declaration statements and expressions. But these don't save/load properly from XML
                      Helpful 10/7 hangout with Andrew and Paul. 
   [lyn, 11/04/12] Created. Add global declarations. Work on local variable declaration statement. 

 Lyn's Todo List
 * Renaming of globals, procedure params, locals. 
 * Saving/restoring of local declaration statements and expressions
 * Correctly handle decl name changes with open mutator in local declaration stm/exp.
 * Handling of getters/setters in copies of non-top-level blocks on desktop. 
 * Effect system for global definition initializers and topologically sorting them by dependency. 
*/

/*
// For debugging only
function myStringify (obj) {
  var seen = [];
  return JSON.stringify(obj, function(key, val) {
   if (typeof val == "object") {
       if (seen.indexOf(val) >= 0) {
           return undefined;
       }
       seen.push(val);
   }
   return val
   });
}
*/

Blockly.Language.global_declaration = {
  // Global var defn
  category: Blockly.MSG_VARIABLE_CATEGORY,  
  helpUrl: "http://fakewebsite.com", // *** [lyn, 11/10/12] Fix this
  init: function() {
    this.setColour(330);
    this.appendValueInput('VALUE')
        .appendTitle("global")
        .appendTitle(new Blockly.FieldTextInput('name', Blockly.LexicalVariable.renameGlobal), 'NAME')
        .appendTitle("initially is");
    this.setTooltip(Blockly.LANG_VARIABLES_GET_TOOLTIP_1);
  },
  getVars: function() {
    return [this.getTitleValue('VAR')];
  },
  renameVar: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getTitleValue('VAR'))) {
      this.setTitleValue(newName, 'VAR');
    }
  }
};

Blockly.Language.lexical_variable_get = {
  // Variable getter.
  category: Blockly.MSG_VARIABLE_CATEGORY,  
  helpUrl: "http://fakewebsite.com", // *** [lyn, 11/10/12] Fix this
  init: function() {
    this.setColour(330);
    this.fieldVar_ = new Blockly.FieldLexicalVariable("???"); 
    this.fieldVar_.setBlock(this);
    this.appendDummyInput()
        .appendTitle("get")
        .appendTitle(this.fieldVar_, 'VAR');
    this.setOutput(true, null);
    this.setTooltip(Blockly.LANG_VARIABLES_GET_TOOLTIP_1);
  },
  getVars: function() {
    return [this.getTitleValue('VAR')];
  },
  onchange: function() {
     // [lyn, 11/10/12] Checks if parent has changed. If so, checks if curent variable name
     //    is still in scope. If so, keeps it as is; if not, changes to ???
     //    *** NEED TO MAKE THIS BEHAVIOR BETTER!
    if (this.fieldVar_) {
       var currentName = this.fieldVar_.getText();
       var nameList = this.fieldVar_.getNamesInScope();
       var cachedParent = this.fieldVar_.getCachedParent();
       var currentParent = this.fieldVar_.getBlock().getParent();
       // [lyn, 11/10/12] Allow current name to stay if block moved to workspace in "untethered" way. 
       //   Only changed to ??? if tether an untethered block. 
       if (currentParent != cachedParent) {
         this.fieldVar_.setCachedParent(currentParent);
         if  (currentParent != null) {
           for (var i = 0; i < nameList.length; i++ ) {
             if (nameList[i] === currentName) {
               return; // no change
             }
           }
           // Only get here if name not in list 
           this.fieldVar_.setText("???");
         }
       }
    }
  },
  renameLexicalVar: function(oldName, newName) {
    // console.log("Renaming lexical variable from " + oldName + " to " + newName);
    if (oldName === this.getTitleValue('VAR')) {
        this.setTitleValue(newName, 'VAR');
    }
  }
};

Blockly.Language.lexical_variable_set = {
  // Variable setter.
  category: Blockly.MSG_VARIABLE_CATEGORY,  
  helpUrl: "http://fakewebsite.com", // *** [lyn, 11/10/12] Fix this
  init: function() {
    this.setColour(330);
    this.fieldVar_ = new Blockly.FieldLexicalVariable("???"); 
    this.fieldVar_.setBlock(this);
    this.appendValueInput('VALUE')
        .appendTitle("set")
        .appendTitle(this.fieldVar_, 'VAR')
        .appendTitle("to");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.LANG_VARIABLES_SET_TOOLTIP_1);
  },
  getVars: function() {
    return [this.getTitleValue('VAR')];
  },
  onchange: Blockly.Language.lexical_variable_get.onchange,
  renameLexicalVar: Blockly.Language.lexical_variable_get.renameLexicalVar
};

Blockly.Language.local_declaration_statement = {
  // Define a procedure with no return value.
  // category: null,  // Procedures are handled specially.
  category: Blockly.MSG_VARIABLE_CATEGORY,  // *** [lyn, 11/07/12] Abstract over this
  helpUrl: "http://fakewebsite.com", // *** [lyn, 11/07/12] Fix this
  init: function() {
    this.setColour(330);
    this.localNames_ = ["name"]; // list of declared local variable names; has one initially 
    this.appendDummyInput('LOCAL_KEYWORD')
        .appendTitle("local"); // [lyn, 11/05/12] tried to put this on same line with first local name;
                               // Worked fine here, but not in compose function below.
    
    
    var declInput = this.appendValueInput('DUMMYDECL');
    declInput.appendTitle(new Blockly.FieldTextInput("name"))
            .appendTitle("initially is")
            .setAlign(Blockly.ALIGN_RIGHT);
    
    this.appendStatementInput('DUMMYSTACK') 
        .appendTitle("in do");

    // Add notch and nub for vertical statment composition 
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    // Add mutator for editing local variable names
    this.setMutator(new Blockly.Mutator(['local_mutatorarg']));

    this.setTooltip("Fix this tooltip!"); // *** [lyn, 11/07/12] Fix this
  },
  addDeclarationInputs_: function(names, inits) { 
    // Create all inputs (except for "local" keyword) from names list using exps in inits
    // If inits is undefined, treat all initial expressions as undefined. 
    for (var i = 0; i < names.length; i++) {
        var declInput = this.appendValueInput('DECL' + i);
        // [lyn, 11/06/12]
        //   This was for case where tried to put "local" keyword on same line with first local name.
        //   But even though alignment set to Blockly.ALIGN_RIGHT, the input was left justified
        //   and covered the plus sign for popping up the mutator. So I put the "local" keyword
        //   on it's own line even though this wastes vertical space. This should be fixed in the future. 
        // if (i == 0) {
        //  declInput.appendTitle("local"); // Only put keyword "local" on top line.
        // }
        declInput.appendTitle(new Blockly.FieldTextInput(names[i], Blockly.LexicalVariable.renameParam), 
                              'VAR' + i)
            .appendTitle("initially is")
            .setAlign(Blockly.ALIGN_RIGHT); 
        if (inits && inits[i]) { // If there is an initializer, connect it
            declInput.connection.connect(inits[i]);
        }
    }
  },
  mutationToDom: function() {
    // *** [lyn, 11/07/2012] Not sure what I'm doing here --- need to understand XML rep better. 
    var container = document.createElement('mutation');
    for (var i = 0; i< this.localNames_.length; i++) {
      var parameter = document.createElement('localname');
      parameter.setAttribute('name', this.localNames_[i]);
      container.appendChild(parameter);
    }
    return container;
  },
  domToMutation: function(xmlElement) {
    // *** [lyn, 11/07/2012] Not sure what I'm doing here --- need to understand XML rep better. 
    if (Object.keys(xmlElement).length > 0) { // Ensure xml element is nonempty
                                              // Else we'll overwrite initial list with "name" for new block
      this.localNames_ = [];
      for (var i = 0, childNode; childNode = xmlElement.childNodes[i]; i++) {
        if (childNode.nodeName.toLowerCase() == 'localname') {
          this.localNames_.push(childNode.getAttribute('name'));
        }
      }
    }
    // *** [lyn, 11/07/2012] What else needs to be done here? 
    // *** Perhaps something like the reconnection in procedure callers?
    
    this.removeInput('DUMMYDECL');
    this.removeInput('DUMMYSTACK');
    this.addDeclarationInputs_(this.localNames_); // add declarations; inits are undefined
        // Declaration body 
    this.appendStatementInput('STACK') 
        .appendTitle("in do");
  },
  decompose: function(workspace) {
    // Create "mutator" editor populated with name blocks with local variable names
    var containerBlock = new Blockly.Block(workspace,
                                           'local_mutatorcontainer');
    containerBlock.initSvg();
    containerBlock.setDefBlock(this);
    var connection = containerBlock.getInput('STACK').connection;
    for (var i = 0; i < this.localNames_.length; i++) {
      // alert("decompose local:getting input");
      // var decl = this.getInput('DECL' + i);
      var localName = this.getTitleValue('VAR' + i);
      // alert("field VAR" + i + ":" + JSON.stringify(field));
      // alert("nameInput:" + myStringify(nameInput));
      var nameBlock = new Blockly.Block(workspace, 'local_mutatorarg');
      nameBlock.initSvg();
      nameBlock.setTitleValue(localName, 'NAME');
      // Store the old location.
      nameBlock.oldLocation = i;
      connection.connect(nameBlock.previousConnection);
      connection = nameBlock.nextConnection;
    }
    return containerBlock;
  },
  compose: function(containerBlock) {
    // Modify this local-in-do block according to arrangement of name blocks in mutator editor. 

    // Remove all the local declaration inputs ...
    for (var i = 0; i < this.localNames_.length; i++) {
      this.removeInput('DECL' + i);
    }

    // ... and the body 
    if (this.getInput('STACK')) this.removeInput('STACK'); // for local variable declaration statement
    else if (this.getInput('RETURN')) this.removeInput('RETURN'); // for local variable declaration expression

    // Now rebuild the block from the mutator. 
    this.localNames_ = [];
    var initializers = [];
    var nameBlock = containerBlock.getInputTargetBlock('STACK');
    // First find the new names
    while (nameBlock) {
        var localNameFromEditor = nameBlock.getTitleValue('NAME');
        this.localNames_.push(localNameFromEditor);
        initializers.push(nameBlock.valueConnection_); // pushes undefined if doesn't exist
        nameBlock = nameBlock.nextConnection && nameBlock.nextConnection.targetBlock();
    }

    // Now add in the names inputs into the local var block
    this.addDeclarationInputs_(this.localNames_, initializers);

    // Finally add any body block remembers in the mutator container
    if (containerBlock.isStatement_) {
        // for local variable declaration statement
        var doInput = this.appendStatementInput('STACK') // add slot for body stack
                          .appendTitle("in do");
        if (containerBlock.bodyStatementConnection_) { 
            doInput.connection.connect(containerBlock.bodyStatementConnection_);
        }
    } else {
        // for local variable declaration expression
        var returnInput = this.appendValueInput('RETURN')
                              .appendTitle("in return")
                              .setAlign(Blockly.ALIGN_RIGHT);
        if (containerBlock.bodyExpressionConnection_) { // for local variable declaration expression
            returnInput.connection.connect(containerBlock.bodyExpressionConnection_);
        }
    }
  },
  dispose: function() {
    // *** [lyn, 11/07/12] Dunno if anything needs to be done here. 
    // Call parent's destructor.
    Blockly.Block.prototype.dispose.apply(this, arguments);
    // [lyn, 11/07/12] In above line, don't know where "arguments" param comes from, 
    // but if it's remove, there's no clicking sound upon deleting the block!
  },
  saveConnections: function(containerBlock) {
    // Store body block (in-do statement for local declaration statement, 
    //   in-return expression for local declaration expression) with containerBlock
    // containerBlock.isStatement_ remembers whether declaration is a statement or expression.
    if (this.getInput('STACK')) {
      containerBlock.isStatement_ = true;
      containerBlock.bodyStatementConnection_ = this.getInput('STACK').connection.targetConnection;
    }
    else if (this.getInput('RETURN')) {
      containerBlock.isStatement_ = false;
      containerBlock.bodyExpressionConnection_ = this.getInput('RETURN').connection.targetConnection;
    }

    // Store child initializer blocks for local name declarations with name blocks in mutator editor
    var nameBlock = containerBlock.getInputTargetBlock('STACK');
    var i = 0;
    while (nameBlock) {
      var localDecl = this.getInput('DECL' + i);
      nameBlock.valueConnection_ = 
        localDecl && localDecl.connection.targetConnection;
      i++;
      nameBlock = nameBlock.nextConnection &&
      nameBlock.nextConnection.targetBlock();
    }
  }, 
  getVars: function() {
    // New on [11/18/12]
    var varList = [];
    var i = 0; 
    var currentVar = this.getTitleValue('VAR' + i);
    while (currentVar) {
        varList.push(currentVar);
        i++;
    }
    return varList;
  },
  blocksInScope: function () {
    var doBody = this.getInputTargetBlock('STACK'); // *** [lyn, 11/24/12] This will go away with DO-AND-RETURN block
    var doBodyList = (doBody && [doBody]) || []; // List of non-null doBody or empty list for null doBody
    return doBodyList; // List of non-null body elements. 
  },
  declaredNames: function () { 
    var varNames = [];
    for(var i=0;this.getTitleValue('VAR' + i);i++){
      varNames.push(this.getTitleValue('VAR' + i));
    }
    return varNames;
  },
  renameVar: function(oldName, newName) {
    // *** [lyn, 11/07/12] Still need to handle renaming of local variable names
        /*
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
          if (block.type == 'local_mutatorarg' &&
              Blockly.Names.equals(oldName, block.getTitleValue('NAME'))) {
            block.setTitleValue(newName, 'NAME');
          }
        }
      }
    }
        */
    }
};

Blockly.Language.local_declaration_expression = {
  // Define a procedure with a return value.
  category: Blockly.MSG_VARIABLE_CATEGORY,  // *** [lyn, 11/07/12] Abstract over this
  helpUrl: "http://fakewebsite.com", // *** [lyn, 11/07/12] Fix this
  init: function() {
    this.setColour(330);
    this.localNames_ = ["name"]; // list of declared local variable names; has one initially 
    this.appendDummyInput('LOCAL_KEYWORD')
        .appendTitle("local"); // [lyn, 11/05/12] See notes on this above in local_declaration_statement

    var declInput = this.appendValueInput('DUMMYDECL');
    declInput.appendTitle(new Blockly.FieldTextInput("name"))
            .appendTitle("initially is")
            .setAlign(Blockly.ALIGN_RIGHT);

	this.appendValueInput('DUMMYRETURN')
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle("in return");
        
    // Create plug for expression output
    this.setOutput(true, null);

    // Add mutator for editing local variable names
    this.setMutator(new Blockly.Mutator(['local_mutatorarg']));

    this.setTooltip("Fix this tooltip!"); // *** [lyn, 11/07/12] Fix this
  },
  addDeclarationInputs_: Blockly.Language.local_declaration_statement.addDeclarationInputs_,
  mutationToDom: Blockly.Language.local_declaration_statement.mutationToDom,
  domToMutation: function(xmlElement) {
    // *** [lyn, 11/07/2012] Not sure what I'm doing here --- need to understand XML rep better. 
    if (Object.keys(xmlElement).length > 0) { // Ensure xml element is nonempty
                                              // Else we'll overwrite initial list with "name" for new block
      this.localNames_ = [];
      for (var i = 0, childNode; childNode = xmlElement.childNodes[i]; i++) {
        if (childNode.nodeName.toLowerCase() == 'localname') {
          this.localNames_.push(childNode.getAttribute('name'));
        }
      }
    }
    this.removeInput('DUMMYDECL');
    this.removeInput('DUMMYRETURN');
    // *** [lyn, 11/07/2012] What else needs to be done here? 
    // *** Perhaps something like the reconnection in procedure callers?
    this.addDeclarationInputs_(this.localNames_); // add declarations; inits are undefined

    // Declaration body = return value of expression
    this.appendValueInput('RETURN')
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendTitle("in return");
  },
  blocksInScope: function () {
    var doBody = this.getInputTargetBlock('RETURN'); // *** [lyn, 11/24/12] This will go away with DO-AND-RETURN block
    var doBodyList = (doBody && [doBody]) || []; // List of non-null doBody or empty list for null doBody
    return doBodyList; // List of non-null body elements. 
  },
  decompose: Blockly.Language.local_declaration_statement.decompose,
  compose: Blockly.Language.local_declaration_statement.compose,
  dispose: Blockly.Language.local_declaration_statement.dispose,
  saveConnections: Blockly.Language.local_declaration_statement.saveConnections,
  declaredNames: Blockly.Language.local_declaration_statement.declaredNames,
  renameVar: Blockly.Language.local_declaration_statement.renameVar
};

Blockly.Language.local_mutatorcontainer = {
  // Local variable container (for mutator dialog).
  init: function() {
    this.setColour(330);
    this.appendDummyInput()
        .appendTitle("local names");
    this.appendStatementInput('STACK');
    this.setTooltip('');
    this.contextMenu = false;
  },
  // [lyn. 11/24/12] Set procBlock associated with this container. 
  setDefBlock: function (defBlock) {
    this.defBlock_ = defBlock;
  }, 
  // [lyn. 11/24/12] Set procBlock associated with this container. 
  // Invariant: should not be null, since only created as mutator for a particular proc block.
  getDefBlock: function () {
    return this.defBlock_;
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

Blockly.Language.local_mutatorarg = {
  // Procedure argument (for mutator dialog).
  init: function() {
    this.setColour(330);
    this.appendDummyInput()
        .appendTitle("name")
        .appendTitle(new Blockly.FieldTextInput('x', Blockly.LexicalVariable.renameParam), 'NAME');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
    this.contextMenu = false;
  },
  getContainerBlock: function () {
    var parent = this.getParent();
    while (parent && ! (parent.type === "local_mutatorcontainer")) {
      parent = parent.getParent();
    }
    // [lyn, 11/24/12] Cache most recent container block so can reference it upon removal from mutator arg stack
    this.cachedContainerBlock_ = (parent && (parent.type === "local_mutatorcontainer") && parent) || null;
    return this.cachedContainerBlock_;
  },
  getDefBlock: function () {
    var container = this.getContainerBlock();
    return (container && container.getDefBlock()) || null;
  },
  blocksInScope: function () { 
    var defBlock = this.getDefBlock(); 
    return (defBlock && defBlock.blocksInScope()) || [];
  },
  declaredNames: function () { 
    var container = this.getContainerBlock(); 
    return (container && container.declaredNames()) || [];
  },
  
  // [lyn, 11/24/12] Check for situation in which mutator arg has been removed from stack,
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
      }
    }
  }
};



