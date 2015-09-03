// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Procedure blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

/**
 * Lyn's Change History:
 * [lyn, written 11/16-17/13, added 07/01/14]
 *   + Added freeVariables, renameFree, and renameBound to procedure declarations
 *   + Added renameVars for procedure declarations, which allows renaming multiple parameters simultaneously
 *   + Modified updateParams_ to accept optional params argument
 *   + Introduced bodyInputName field for procedure declarations ('STACK' for procedures_defnoreturn;
 *     'RETURN' for procedures_return), and use this to share more methods between the two kinds
 *     of procedure declarations.
 *   + Replaced inlined string list equality tests by new Blockly.LexicalVariable.stringListsEqual
 * [lyn, 10/28/13]
 *   + Fixed a missing change of Blockly.Procedures.rename by Blockly.AIProcedure.renameProcedure
 *   + I was wrong about re-rendering not being needed in updatedParams_!
 *     Without it, changing horizontal -> vertical params doesn't handle body slot correctly.
 *     So added it back.
 * [lyn, 10/27/13]
 *   + Fix bug in list of callers in flyout by simplifying domToMutation for procedure callers.
 *     This should never look for associated declaration, but just take arguments from given xml.
 *   + Removed render() call from updateParams_. Seems unnecessary. <== I WAS WRONG. SEE 10/28/13 NOTE
 *   + Specify direction of flydowns
 *   + Replaced Blockly.Procedures.rename by Blockly.AIProcedure.renameProcedure in proc decls
 * [lyn, 10/26/13] Modify procedure parameter changeHandler to propagate name changes to caller arg labels
 *     and open mutator labels
 * [lyn, 10/25/13]
 *   + Modified procedures_defnoreturn compose method so that it doesn't call updateParams_
 *     if mutator hasn't changed parameter names. This helps avoid a situation where
 *     an attempt is made to update params of a collapsed declaration.
 *   + Modified collapsed decls to have 'to ' prefix and collapsed callers to have 'call ' prefix.
 * [lyn, 10/24/13] Allowed switching between horizontal and vertical display of arguments
 * [lyn, 10/23/13] Fixed bug in domToMutation for callers that was giving wrong args to caller blocks.
 * [lyn, 10/10-14/13]
 *   + Installed variable declaration flydowns in both types of procedure declarations.
 *   + Fixed bug: Modified onchange for procedure declarations to keep arguments_ instance
 *     variable updated when param is edited directly on declaration block.
 *   + Removed get block (still in Variable drawer; no longer needed with parameter flydowns)
 *   + Removed "do {} then-return []" block since (1) it's in Control drawer and
 *     (2) it will be superseded in the context by Michael Phox's proc_defnoreturn mutator
 *     that allows adding a DO statement.
 *   + TODO: Abstract over string labels on all blocks using constants defined in en/_messages.js
 *   + TODO: Clean up code, including refactoring to increase sharing between
 *     procedures_defnoreturn and procedures_defreturn.
 * [lyn, 11/29/12] Integrated into App Inventor blocks. Known bugs:
 *   + Reordering mutator_args in mutator_container changes references to ??? because it interprets it
 *     as removing and inserting rather than moving.
 * [lyn, 11/24/12] Implemented procedure parameter renaming:
 *   + changing a variable name in mutator_arg for procedure changes it immediately in references in body.
 *   + no duplicate names are allowed in mutator_args; alpha-renaming prevents this.
 *   + no variables can be captured by renaming; alpha-renaming prevents this.
 */
'use strict';

goog.provide('Blockly.Blocks.procedures');

goog.require('Blockly.Blocks.Utilities');
goog.require('goog.dom');

Blockly.Blocks['procedures_defnoreturn'] = {
  // Define a procedure with no return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL,
  bodyInputName: 'STACK',
  init: function() {
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    var name = Blockly.Procedures.findLegalName(
        Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE, this);
    this.appendDummyInput('HEADER')
        .appendField(Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE)
        // [lyn, 10/27/13] Replaced Blockly.Procedures.rename by Blockly.AIProcedure.renameProcedure
        .appendField(new Blockly.FieldTextInput(name, Blockly.AIProcedure.renameProcedure), 'NAME');
    this.horizontalParameters = true; // horizontal by default
    this.appendStatementInput('STACK')
        .appendField(Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO);
    this.setMutator(new Blockly.Mutator(['procedures_mutatorarg']));
    this.setTooltip(Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP);
    this.arguments_ = []; // List of declared local variable names; has one ("name") initially
                          // Other methods guarantee the invariant that this variable contains
                          // the list of names declared in the local declaration block.
    this.warnings = [{name:"checkEmptySockets",sockets:["STACK"]}];
  },
  onchange: function () {
    this.arguments_ = this.declaredNames(); // ensure arguments_ is in sync with paramFlydown fields
  },
  updateParams_: function(opt_params) {  // make rendered block reflect the parameter names currently in this.arguments_
    // [lyn, 11/17/13] Added optional opt_params argument:
    //    If its falsey (null or undefined), use the existing this.arguments_ list
    //    Otherwise, replace this.arguments_ by opt_params
    // In either case, make rendered block reflect the parameter names in this.arguments_
    if (opt_params) {
      this.arguments_ = opt_params;
    }
    // Check for duplicated arguments.
    // [lyn 10/10/13] Note that in blocks edited within AI2, duplicate parameter names should never occur
    //    because parameters are renamed to avoid duplication. But duplicates might show up
    //    in XML code hand-edited by user.
    // console.log("enter procedures_defnoreturn updateParams_()");
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
      this.setWarningText(Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING);
    } else {
      this.setWarningText(null);
    }

    var procName = this.getFieldValue('NAME');
    //save the first two input lines and the last input line
    //to be re added to the block later
    // var firstInput = this.inputList[0];  // [lyn, 10/24/13] need to reconstruct first input
    var bodyInput = this.inputList[this.inputList.length - 1]; // Body of procedure

    // stop rendering until block is recreated
    var savedRendered = this.rendered;
    this.rendered = false;

    // remove first input
    // console.log("updateParams_: remove input HEADER");
    var thisBlock = this; // Grab correct object for use in thunk below
    Blockly.FieldParameterFlydown.withChangeHanderDisabled(
        // [lyn, 07/02/14] Need to disable change handler, else this will try to rename params for horizontal arg fields!
        function() {thisBlock.removeInput('HEADER');}
    );

    // [lyn, 07/02/14 fixed logic] remove all old argument inputs (if they were vertical)
    if (! this.horizontalParameters) {
      var oldArgCount = this.inputList.length - 1; // Only args and body are left
      if (oldArgCount > 0) {
        var paramInput0 = this.getInput('VAR0');
        if (paramInput0) { // Yes, they were vertical
          for (var i = 0; i < oldArgCount; i++)
          {
            try
            {
              Blockly.FieldParameterFlydown.withChangeHanderDisabled(
                  // [lyn, 07/02/14] Need to disable change handler, else this will try to rename params for vertical arg fields!
                  function() {thisBlock.removeInput('VAR' + i);}
              );
            }
            catch(err)
            {
              console.log(err);
            }
          }
        }
      }
    }

    //empty the inputList then recreate it
    this.inputList = [];

    // console.log("updateParams_: create input HEADER");
    var headerInput =
        this.appendDummyInput('HEADER')
            .appendField(Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE)
            // [lyn, 10/28/13] Replaced Blockly.Procedures.rename by Blockly.AIProcedure.renameProcedure
            .appendField(new Blockly.FieldTextInput(procName, Blockly.AIProcedure.renameProcedure), 'NAME');

    //add an input title for each argument
    //name each input after the block and where it appears in the block to reference it later
    for (var i = 0; i < this.arguments_.length; i++) {
      if (this.horizontalParameters) { // horizontal case
        headerInput.appendField(' ')
                   .appendField(this.parameterFlydown(i), // [lyn, 10/10/13] Changed to param flydown
                                'VAR' + i); // Tag with param tag to make it easy to find later.
      } else { // vertical case
        this.appendDummyInput('VAR' + i)
             // .appendField(this.arguments_[i])
             .appendField(this.parameterFlydown(i), 'VAR' + i)
             .setAlign(Blockly.ALIGN_RIGHT);
      }
    }

    //put the last two arguments back
    this.inputList = this.inputList.concat(bodyInput);

    this.rendered = savedRendered;
    // [lyn, 10/28/13] I thought this rerendering was unnecessary. But I was wrong!
    // Without it, get bug noticed by Andrew in which toggling horizontal -> vertical params
    // in procedure decl doesn't handle body tag appropriately!
    if (this.rendered) {
       this.render();
    }
    // console.log("exit procedures_defnoreturn updateParams_()");
  },
  // [lyn, 10/26/13] Introduced this to correctly handle renaming of [(1) caller arg labels and
  // (2) mutatorarg in open mutator] when procedure parameter flydown name is edited.
  parameterFlydown: function (paramIndex) { // Return a new procedure parameter flydown
    var initialParamName = this.arguments_[paramIndex];
    var procDecl = this; // Here, "this" is the proc decl block. Name it to use in function below
    var procWorkspace = this.workspace;
    var procedureParameterChangeHandler = function (newParamName) {
      // console.log("enter procedureParameterChangeHandler");


      // Extra work that needs to be done when procedure param name is changed, in addition
      // to renaming lexical variables:
      //   1. Change all callers so label reflects new name
      //   2. If there's an open mutator, change the corresponding slot.
      // Note: this handler is invoked as method on field, so within the handler body,
      // "this" will be bound to that field and *not* the procedure declaration object!

      // Subtlety #1: within this changeHandler, procDecl.arguments_ has *not* yet been
      // updated to include newParamName. This only happens later. But since we know newParamName
      // *and* paramIndex, we know how to update procDecl.arguments_ ourselves!

      // Subtlety #2: I would have thought we would want to create local copy of
      // procedure arguments_ list rather than mutate that list, but I'd be wrong!
      // Turns out that *not* mutating list here causes trouble below in the line
      //
      //   Blockly.Field.prototype.setText.call(mutatorarg.getTitle_("NAME"), newParamName);
      //
      // The reason is that this fires a change event in mutator workspace, which causes
      // a call to the proc decl compose() method, and when it detects a difference in
      // the arguments it calls proc decl updateParams_. This removes proc decl inputs
      // before adding them back, and all hell breaks loose when the procedure name field
      // and previous parameter flydown fields are disposed before an attempt is made to
      // disposed this field. At this point, the SVG element associated with the procedure name
      // is gone but the field is still in the title list. Attempting to dispose this field
      // attempts to hide the open HTML editor widget, which attempts to re-render the
      // procedure declaration block. But the null SVG for the procedure name field
      // raises an exception.
      //
      // It turns out that by mutating proc decl arguments_, when compose() is called,
      // updateParams_() is *not* called, and this prevents the above scenario.
      //  So rather than doing
      //
      //    var newArguments = [].concat(procDecl.arguments_)
      //
      // we instead do:
      var newArguments = procDecl.arguments_;
      newArguments[paramIndex] = newParamName;

      var procName = procDecl.getFieldValue('NAME');

      // 1. Change all callers so label reflects new name
      Blockly.Procedures.mutateCallers(procName, procWorkspace, newArguments, procDecl.paramIds_);

      var callers = Blockly.Procedures.getCallers(procName, procWorkspace);
      for (var x = 0; x < callers.length; x++) {
        var block = callers[x];
        Blockly.Blocks.Utilities.renameCollapsed(block, 0);
      }

      // 2. If there's an open mutator, change the name in the corresponding slot.
      if (procDecl.mutator && procDecl.mutator.rootBlock_) {
        // Iterate through mutatorarg param blocks and change name of one at paramIndex
        var mutatorContainer = procDecl.mutator.rootBlock_;
        var mutatorargIndex = 0;
        var mutatorarg = mutatorContainer.getInputTargetBlock('STACK');
        while (mutatorarg && mutatorargIndex < paramIndex) {
            mutatorarg = mutatorarg.nextConnection && mutatorarg.nextConnection.targetBlock();
            mutatorargIndex++;
        }
        if (mutatorarg && mutatorargIndex == paramIndex) {
          // Subtlety #3: If call mutatorargs's setValue, its change handler will be invoked
          // several times, and on one of those times, it will find new param name in
          // the procedures arguments_ instance variable and will try to renumber it
          // (e.g. "a" -> "a2"). To avoid this, invoke the setText method of its Field s
          // superclass directly. I.e., can't do this:
          //   mutatorarg.getTitle_("NAME").setValue(newParamName);
          // so instead do this:
            Blockly.Field.prototype.setText.call(mutatorarg.getField_("NAME"), newParamName);
        }
      }
      // console.log("exit procedureParameterChangeHandler");
    }
    return new Blockly.FieldParameterFlydown(initialParamName,
                                             true, // name is editable
                                             // [lyn, 10/27/13] flydown location depends on parameter orientation
                                             this.horizontalParameters ? Blockly.FieldFlydown.DISPLAY_BELOW
                                                                       : Blockly.FieldFlydown.DISPLAY_RIGHT,
                                             procedureParameterChangeHandler);
  },
  setParameterOrientation: function(isHorizontal) {
    var params = this.getParameters();
    if (params.length != 0 && isHorizontal !== this.horizontalParameters) {
      this.horizontalParameters = isHorizontal;
      this.updateParams_();
    }
  },
  mutationToDom: function() {
    var container = document.createElement('mutation');
    if (!this.horizontalParameters) {
      container.setAttribute('vertical_parameters', "true"); // Only store an element for vertical
      // The absence of this attribute means horizontal.
    }
    for (var x = 0; x < this.arguments_.length; x++) {
      var parameter = document.createElement('arg');
      parameter.setAttribute('name', this.arguments_[x]);
      container.appendChild(parameter);
    }
    return container;
  },
  domToMutation: function(xmlElement) {
    var params = [];
    var children = goog.dom.getChildren(xmlElement);
    for (var x = 0, childNode; childNode = children[x]; x++) {
      if (childNode.nodeName.toLowerCase() == 'arg') {
        params.push(childNode.getAttribute('name'));
      }
    }
    this.horizontalParameters = xmlElement.getAttribute('vertical_parameters') !== "true";
    this.updateParams_(params);
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block.obtain(workspace, 'procedures_mutatorcontainer');
    containerBlock.initSvg();
    // [lyn, 11/24/12] Remember the associated procedure, so can
    // appropriately change body when update name in param block.
    containerBlock.setProcBlock(this);
    this.paramIds_ = [] // [lyn, 10/26/13] Added
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 0; x < this.arguments_.length; x++) {
      var paramBlock = new Blockly.Block.obtain(workspace, 'procedures_mutatorarg');
      this.paramIds_.push(paramBlock.id); // [lyn, 10/26/13] Added
      paramBlock.initSvg();
      paramBlock.setFieldValue(this.arguments_[x], 'NAME');
      // Store the old location.
      paramBlock.oldLocation = x;
      connection.connect(paramBlock.previousConnection);
      connection = paramBlock.nextConnection;
    }
    // [lyn, 10/26/13] Rather than passing null for paramIds, pass actual paramIds
    // and use true flag to initialize tracking.
    Blockly.Procedures.mutateCallers(this.getFieldValue('NAME'),
                                     this.workspace, this.arguments_, this.paramIds_, true);
    return containerBlock;
  },
  compose: function(containerBlock) {
    var params = [];
    this.paramIds_ = [];
    var paramBlock = containerBlock.getInputTargetBlock('STACK');
    while (paramBlock) {
      params.push(paramBlock.getFieldValue('NAME'));
      this.paramIds_.push(paramBlock.id);
      paramBlock = paramBlock.nextConnection &&
          paramBlock.nextConnection.targetBlock();
    }
    // console.log("enter procedures_defnoreturn compose(); prevArguments = "
    //    + prevArguments.join(',')
    //    + "; currentAguments = "
    //    + this.arguments_.join(',')
    //    + ";"
    // );
    // [lyn, 11/24/12] Note: update params updates param list in proc declaration,
    // but renameParam updates procedure body appropriately.
    if (!Blockly.LexicalVariable.stringListsEqual(params, this.arguments_)) { // Only need updates if param list has changed
      this.updateParams_(params);
      Blockly.Procedures.mutateCallers(this.getFieldValue('NAME'),
        this.workspace, this.arguments_, this.paramIds_);
    }
    // console.log("exit procedures_defnoreturn compose()");
  },
  dispose: function() {
    var name = this.getFieldValue('NAME');
    var editable = this.editable_;
    var workspace = this.workspace;

    // Call parent's destructor.
    Blockly.Block.prototype.dispose.apply(this, arguments);

    if (editable) {
      // Dispose of any callers.
      //Blockly.Procedures.disposeCallers(name, workspace);
      Blockly.AIProcedure.removeProcedureValues(name, workspace);
    }

  },
  getProcedureDef: function() {
    // Return the name of the defined procedure,
    // a list of all its arguments,
    // and that it DOES NOT have a return value.
    return [this.getFieldValue('NAME'),
            this.arguments_,
           this.bodyInputName === 'RETURN']; // true for procedures that return values.
  },
  getVars: function() {
    var names = []
    for (var i = 0, param; param = this.getFieldValue('VAR' + i); i++) {
      names.push(param);
    }
    return names;
  },
  declaredNames: function() { // [lyn, 10/11/13] return the names of all parameters of this procedure
     return this.getVars();
  },
  renameVar: function(oldName, newName) {
    this.renameVars(Blockly.Substitution.simpleSubstitution(oldName,newName));
  },
  renameVars: function(substitution) { // renaming is a dict (i.e., object) mapping old names to new ones
    var oldParams = this.getParameters();
    var newParams = substitution.map(oldParams);
    if (!Blockly.LexicalVariable.stringListsEqual(oldParams, newParams)) {
      this.updateParams_(newParams);
      // Update the mutator's variables if the mutator is open.
      if (this.mutator.isVisible()) {
        var blocks = this.mutator.workspace_.getAllBlocks();
        for (var x = 0, block; block = blocks[x]; x++) {
          if (block.type == 'procedures_mutatorarg') {
            var oldName = block.getFieldValue('NAME');
            var newName = substitution.apply(oldName);
            if (newName !== oldName) {
              block.setFieldValue(newName, 'NAME');
            }
          }
        }
      }
    }
  },
  renameBound: function (boundSubstitution, freeSubstitution) {
    var paramSubstitution = boundSubstitution.restrictDomain(this.declaredNames());
    this.renameVars(paramSubstitution);
    var newFreeSubstitution = freeSubstitution.extend(paramSubstitution);
    Blockly.LexicalVariable.renameFree(this.getInputTargetBlock(this.bodyInputName), newFreeSubstitution);
  },
  renameFree: function (freeSubstitution) { // Should have no effect since only top-level procedures.
    var freeVars = this.freeVariables(); // Calculate free variables, which should be empty,
                                         // throwing exception if not.
    // There should be no free variables, and so nothing to rename. Do nothing else.
  },
  freeVariables: function() { // return the free lexical variables of this block
                              // Should return the empty set: something is wrong if it doesn't!
    var result = Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock(this.bodyInputName));
    result.subtract(new Blockly.NameSet(this.declaredNames()));
    if (result.isEmpty()) {
      return result;
    } else {
      throw "Violation of invariant: procedure declaration has nonempty free variables: " + result.toString();
    }
  },
  // [lyn, 11/24/12] return list of procedure body (if there is one)
  blocksInScope: function () {
    var body = this.getInputTargetBlock(this.bodyInputName);
    return (body && [body]) || [];
  },
  typeblock: [{ translatedName: Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE +
      ' ' + Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO }],
  customContextMenu: function (options) {
    Blockly.FieldParameterFlydown.addHorizontalVerticalOption(this, options);
  },
  getParameters: function() {
    return this.arguments_;
  }
};

// [lyn, 01/15/2013] Edited to remove STACK (no longer necessary with DO-THEN-RETURN)
Blockly.Blocks['procedures_defreturn'] = {
  // Define a procedure with a return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL,
  bodyInputName: 'RETURN',
  init: function() {
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    var name = Blockly.Procedures.findLegalName(
        Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE, this);
    this.appendDummyInput('HEADER')
        .appendField(Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE)
      // [lyn, 10/27/13] Replaced Blockly.Procedures.rename by Blockly.AIProcedure.renameProcedure
        .appendField(new Blockly.FieldTextInput(name, Blockly.AIProcedure.renameProcedure), 'NAME');
    this.horizontalParameters = true; // horizontal by default
    this.appendIndentedValueInput('RETURN')
        .appendField(Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN);
    this.setMutator(new Blockly.Mutator(['procedures_mutatorarg']));
    this.setTooltip(Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP);
    this.arguments_ = [];
    this.warnings = [{name:"checkEmptySockets",sockets:["RETURN"]}];
  },
  onchange: Blockly.Blocks.procedures_defnoreturn.onchange,
  // [lyn, 11/24/12] return list of procedure body (if there is one)
  updateParams_: Blockly.Blocks.procedures_defnoreturn.updateParams_,
  parameterFlydown: Blockly.Blocks.procedures_defnoreturn.parameterFlydown,
  setParameterOrientation: Blockly.Blocks.procedures_defnoreturn.setParameterOrientation,
  mutationToDom: Blockly.Blocks.procedures_defnoreturn.mutationToDom,
  domToMutation: Blockly.Blocks.procedures_defnoreturn.domToMutation,
  decompose: Blockly.Blocks.procedures_defnoreturn.decompose,
  compose: Blockly.Blocks.procedures_defnoreturn.compose,
  dispose: Blockly.Blocks.procedures_defnoreturn.dispose,
  getProcedureDef: Blockly.Blocks.getProcedureDef,
  getVars: Blockly.Blocks.procedures_defnoreturn.getVars,
  declaredNames: Blockly.Blocks.procedures_defnoreturn.declaredNames,
  renameVar: Blockly.Blocks.procedures_defnoreturn.renameVar,
  renameVars: Blockly.Blocks.procedures_defnoreturn.renameVars,
  renameBound: Blockly.Blocks.procedures_defnoreturn.renameBound,
  renameFree: Blockly.Blocks.procedures_defnoreturn.renameFree,
  freeVariables: Blockly.Blocks.procedures_defnoreturn.freeVariables,
  blocksInScope: Blockly.Blocks.procedures_defnoreturn.blocksInScope,
  typeblock: [{ translatedName: Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE +
      ' ' + Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN }],
  customContextMenu: Blockly.Blocks.procedures_defnoreturn.customContextMenu,
  getParameters: Blockly.Blocks.procedures_defnoreturn.getParameters
};

Blockly.Blocks['procedures_mutatorcontainer'] = {
  // Procedure container (for mutator dialog).
  init: function() {
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE);
    this.appendStatementInput('STACK');
    this.setTooltip(Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP);
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
      paramNames.push(paramBlock.getFieldValue('NAME'));
      paramBlock = paramBlock.nextConnection &&
                   paramBlock.nextConnection.targetBlock();
    }
    return paramNames;
  }
};

Blockly.Blocks['procedures_mutatorarg'] = {
  // Procedure argument (for mutator dialog).
  init: function() {
//    var mutatorarg = this;
//    var mutatorargChangeHandler = function(newName) {
//      var proc = mutatorarg.getProcBlock();
//      var procArguments = proc ? proc.arguments_ : [];
//      console.log("mutatorargChangeHandler: newName = " + newName
//                  + " and proc argumnets = [" + procArguments.join(',') + "]");
//      return Blockly.LexicalVariable.renameParam.call(this,newName);
//    }
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE)
        .appendField(new Blockly.FieldTextInput('x',Blockly.LexicalVariable.renameParam), 'NAME');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP);
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
  // [lyn, 11/24/12] Return the procedure associated with mutator arg is in, or null if there isn't one.
  // Dynamically calculate this by walking up chain, because mutator arg might or might not
  // be in container stack.
  getProcBlock: function () {
    var container = this.getContainerBlock();
    return (container && container.getProcBlock()) || null;
  },
  // [lyn, 11/24/12] Return the declared names in the procedure associated with mutator arg,
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
    var paramName = this.getFieldValue('NAME');
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
            this.setFieldValue(newName, 'NAME');
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

Blockly.Blocks.procedures_mutatorarg.validator = function(newVar) {
  // Merge runs of whitespace.  Strip leading and trailing whitespace.
  // Beyond this, all names are legal.
  newVar = newVar.replace(/[\s\xa0]+/g, ' ').replace(/^ | $/g, '');
  return newVar || null;
};

/* [lyn 10/10/13] With parameter flydown changes,
 * I don't think a special GET block in the Procedure drawer is necesssary
Blockly.Blocks.procedure_lexical_variable_get = {
  // Variable getter.
  category: 'Procedures',
  helpUrl: Blockly.Msg.LANG_PROCEDURES_GET_HELPURL, // *** [lyn, 11/10/12] Fix this
  init: function() {
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    this.fieldVar_ = new Blockly.FieldLexicalVariable(" ");
    this.fieldVar_.setBlock(this);
    this.appendDummyInput()
        .appendField("get")
        .appendField(this.fieldVar_, 'VAR');
    this.setOutput(true, null);
    this.setTooltip(Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP);
    this.errors = [{name:"checkIsInDefinition"},{name:"checkDropDownContainsValidValue",dropDowns:["VAR"]}];
  },
  getVars: function() {
    return [this.getFieldValue('VAR')];
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
         if  (currentParent !== null) {
           for (var i = 0; i < nameList.length; i++ ) {
             if (nameList[i] === currentName) {
               return; // no change
             }
           }
           // Only get here if name not in list
           this.fieldVar_.setText(" ");
         }
       }
    }
    Blockly.WarningHandler.checkErrors.call(this);
  },
  renameLexicalVar: function(oldName, newName) {
    // console.log("Renaming lexical variable from " + oldName + " to " + newName);
    if (oldName === this.getFieldValue('VAR')) {
        this.setFieldValue(newName, 'VAR');
    }
  }
};
*/

/* // [lyn, 10/14/13] This will become unnecessary with Michael Phox's
 * mutator on procedure_defreturn that allows adding a DO statement.
 */
/*
Blockly.Blocks.procedures_do_then_return = {
  // String length.
  category: 'Procedures',
  helpUrl: Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL,
  init: function() {
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    this.appendStatementInput('STM')
        .appendField(Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO);
    this.appendValueInput('VALUE')
        .appendField(Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN)
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setOutput(true, null);
    this.setTooltip(Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP);
  },
  onchange: Blockly.WarningHandler.checkErrors
};
*/

Blockly.Blocks['procedures_callnoreturn'] = {
  // Call a procedure with no return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL,
  init: function() {
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    this.procNamesFxn = function(){return Blockly.AIProcedure.getProcedureNames(false);};

    this.procDropDown = new Blockly.FieldDropdown(this.procNamesFxn,Blockly.FieldProcedure.onChange);
    this.procDropDown.block = this;
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL)
        .appendField(this.procDropDown,"PROCNAME");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP);
    this.arguments_ = [];
    this.quarkConnections_ = null;
    this.quarkArguments_ = null;
    this.errors = [{name:"checkIsInDefinition"},{name:"checkDropDownContainsValidValue",dropDowns:["PROCNAME"]}];
    //Blockly.FieldProcedure.onChange.call(this.getField_("PROCNAME"),this.procNamesFxn(false)[0][0]);
    Blockly.FieldProcedure.onChange.call(this.getField_("PROCNAME"),this.getField_("PROCNAME").getValue());
  },
  getProcedureCall: function() {
    return this.getFieldValue('PROCNAME');
  },
  renameProcedure: function(oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getFieldValue('PROCNAME'))) {
      this.setFieldValue(newName, 'PROCNAME');
      Blockly.Blocks.Utilities.renameCollapsed(this, 0);
    }
  },
  // [lyn, 10/27/13] Renamed "fromChange" parameter to "startTracking", because it should be true in any situation
  // where we want caller to start tracking connections associated with paramIds. This includes when a mutator
  // is opened on a procedure declaration.
  setProcedureParameters: function(paramNames, paramIds, startTracking) {
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

    var input;
    var connection;
    var x;

    //fixed parameter alignment see ticket 465
    if (!paramIds) {
      // Reset the quarks (a mutator is about to open).
      this.quarkConnections_ = {};
      this.quarkArguments_ = null;
      // return;  // [lyn, 10/27/13] No, don't return yet. We still want to add paramNames to block!
      // For now, create dummy list of param ids. This needs to be cleaned up further!
      paramIds = [].concat(paramNames); // create a dummy list that's a copy of paramNames.
    }
    if (paramIds.length != paramNames.length) {
      throw 'Error: paramNames and paramIds must be the same length.';
    }
    var paramIdToParamName = {};
    for(var i=0;i<paramNames.length;i++) {
      paramIdToParamName[paramIds[i]] = paramNames[i];
    }
    if(typeof startTracking == "undefined") {
      startTracking = null;
    }

    if (!this.quarkArguments_ || startTracking) {
      // Initialize tracking for this block.
      this.quarkConnections_ = {};
      if (Blockly.LexicalVariable.stringListsEqual(paramNames, this.arguments_) || startTracking) {
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
    for (x = 0;this.getInput('ARG' + x); x++) {
      input = this.getInput('ARG' + x);
      if (input) {
        connection = input.connection.targetConnection;
        this.quarkConnections_[this.quarkArguments_[x]] = connection;
        // Disconnect all argument blocks and remove all inputs.
        this.removeInput('ARG' + x);
      }
    }
    // Rebuild the block's arguments.
    this.arguments_ = [].concat(paramNames);
    this.quarkArguments_ = paramIds;
    for (x = 0; x < this.arguments_.length; x++) {
      input = this.appendValueInput('ARG' + x)
          .setAlign(Blockly.ALIGN_RIGHT)
          .appendField(this.arguments_[x]);
      if (this.quarkArguments_) {
        // Reconnect any child blocks.
        var quarkName = this.quarkArguments_[x];
        if (quarkName in this.quarkConnections_) {
          connection = this.quarkConnections_[quarkName];
          if (!connection || connection.targetConnection ||
              connection.sourceBlock_.workspace != this.workspace) {
            // Block no longer exists or has been attached elsewhere.
            delete this.quarkConnections_[quarkName];
          } else {
            input.connection.connect(connection);
          }
        } else if(paramIdToParamName[quarkName]){
          connection = this.quarkConnections_[paramIdToParamName[quarkName]];
          if (connection){
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
    container.setAttribute('name', this.getFieldValue('PROCNAME'));
    for (var x = 0; this.getInput("ARG" + x); x++) {
      var parameter = document.createElement('arg');
      parameter.setAttribute('name', this.getInput("ARG" + x).fieldRow[0].text_);
      container.appendChild(parameter);
    }
    return container;
  },
  domToMutation: function(xmlElement) {
    // Restore the name and parameters.
    var name = xmlElement.getAttribute('name');
    this.setFieldValue(name, 'PROCNAME');
    // [lyn, 10/27/13] Significantly cleaned up this code. Always take arg names from xmlElement.
    // Do not attempt to find definition.
    this.arguments_ = [];
    var children = goog.dom.getChildren(xmlElement);
    for (var x = 0, childNode; childNode = children[x]; x++) {
      if (childNode.nodeName.toLowerCase() == 'arg') {
        this.arguments_.push(childNode.getAttribute('name'));
      }
    }
    this.setProcedureParameters(this.arguments_, null, true);
      // [lyn, 10/27/13] Above. set tracking to true in case this is a block with argument subblocks.
      // and there's an open mutator.
  },
  renameVar: function(oldName, newName) {
    for (var x = 0; x < this.arguments_.length; x++) {
      if (Blockly.Names.equals(oldName, this.arguments_[x])) {
        this.arguments_[x] = newName;
        this.getInput('ARG' + x).fieldRow[0].setText(newName);
      }
    }
  },
  procCustomContextMenu: function(options) {
    // Add option to find caller.
    var option = {enabled: true};
    option.text = Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF;
    var name = this.getFieldValue('PROCNAME');
    var workspace = this.workspace;
    option.callback = function() {
      var def = Blockly.Procedures.getDefinition(name, workspace);
      def && def.select();
    };
    options.push(option);
  },
  removeProcedureValue: function() {
    this.setFieldValue("none", 'PROCNAME');
    for(var i=0;this.getInput('ARG' + i) !== null;i++) {
      this.removeInput('ARG' + i);
    }
  },
  // This generates a single generic call to 'call no return' defaulting its value
  // to the first procedure in the list. Calls for each procedure cannot be done here because the
  // blocks have not been loaded yet (they are loaded in typeblock.js)
  typeblock: [{ translatedName: Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME}]
};


Blockly.Blocks['procedures_callreturn'] = {
  // Call a procedure with a return value.
  category: 'Procedures',  // Procedures are handled specially.
  helpUrl: Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL,
  init: function() {
    this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
    this.procNamesFxn = function(){return Blockly.AIProcedure.getProcedureNames(true);};

    this.procDropDown = new Blockly.FieldDropdown(this.procNamesFxn,Blockly.FieldProcedure.onChange);
    this.procDropDown.block = this;
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL)
        .appendField(this.procDropDown,"PROCNAME");
    this.setOutput(true, null);
    this.setTooltip(Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP);
    this.arguments_ = [];
    this.quarkConnections_ = null;
    this.quarkArguments_ = null;
    this.errors = [{name:"checkIsInDefinition"},{name:"checkDropDownContainsValidValue",dropDowns:["PROCNAME"]}];
    //Blockly.FieldProcedure.onChange.call(this.getField_("PROCNAME"),this.procNamesFxn()[0][0]);
    Blockly.FieldProcedure.onChange.call(this.getField_("PROCNAME"),this.getField_("PROCNAME").getValue());
  },
  getProcedureCall: Blockly.Blocks.procedures_callnoreturn.getProcedureCall,
  renameProcedure: Blockly.Blocks.procedures_callnoreturn.renameProcedure,
  setProcedureParameters:
      Blockly.Blocks.procedures_callnoreturn.setProcedureParameters,
  mutationToDom: Blockly.Blocks.procedures_callnoreturn.mutationToDom,
  domToMutation: Blockly.Blocks.procedures_callnoreturn.domToMutation,
  renameVar: Blockly.Blocks.procedures_callnoreturn.renameVar,
  procCustomContextMenu: Blockly.Blocks.procedures_callnoreturn.procCustomContextMenu,
  removeProcedureValue: Blockly.Blocks.procedures_callnoreturn.removeProcedureValue,
  // This generates a single generic call to 'call return' defaulting its value
  // to the first procedure in the list. Calls for each procedure cannot be done here because the
  // blocks have not been loaded yet (they are loaded in typeblock.js)
  typeblock: [{ translatedName: Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME}]
};

