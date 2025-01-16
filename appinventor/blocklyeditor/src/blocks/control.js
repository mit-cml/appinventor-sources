// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @fileoverview Control blocks for Blockly, modified for App Inventor
 * @author fraser@google.com (Neil Fraser)
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */

/**
 * Lyn's History:
 * [lyn, written 11/16-17/13, added 07/01/14] Added freeVariables, renameFree, and renameBound to forRange and forEach loops
 * [lyn, 10/27/13] Specify direction of flydowns
 * [lyn, 10/25/13] Made collapsed block labels more sensible.
 * [lyn, 10/10-14/13]
 *   + Installed flydown index variable declarations in forRange and forEach loops
 *   + Abstracted over string labels on all blocks using constants defined in en/_messages.js
 *   + Renamed "for <i> start [] end [] step []" block to "for each <number> from [] to [] by []"
 *   + Renamed "for each <i> in list []" block to "for each <item> in list []"
 *   + Renamed "choose test [] then-return [] else-return []" to "if [] then [] else []"
 *     (TODO: still needs to have a mutator like  the "if" statement blocks).
 *   + Renamed "evaluate" block to "evaluate but ignore result"
 *   + Renamed "do {} then-return []" block to "do {} result []" and re-added this block
 *     to the Control drawer (who removed it?)
 *   + Removed get block (still in Variable drawer; no longer needed with parameter flydowns)
 * [lyn, 11/29-30/12]
 *   + Change forEach and forRange loops to take name as input text rather than via plug.
 *   + For these blocks, add extra methods to support renaming.
 */

'use strict';

goog.provide('AI.Blocks.control');

goog.require('AI.Blockly.FieldFlydown');
goog.require('AI.BlockUtils');
goog.require('AI.Substitution');
goog.require('AI.Blockly.Mixins');

Blockly.Blocks['controls_if'] = {
  // If/elseif/else condition.
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_IF_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('IF0')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("boolean", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_IF_MSG_IF);
    this.appendStatementInput('DO0')
        .appendField(Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setMutator(new Blockly.icons.MutatorIcon(['controls_if_elseif',
      'controls_if_else'], this));
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function () {
      if (!thisBlock.elseifCount_ && !thisBlock.elseCount_) {
        return Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1;
      } else if (!thisBlock.elseifCount_ && thisBlock.elseCount_) {
        return Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2;
      } else if (thisBlock.elseifCount_ && !thisBlock.elseCount_) {
        return Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3;
      } else if (thisBlock.elseifCount_ && thisBlock.elseCount_) {
        return Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4;
      }
      return '';
    });
    this.elseifCount_ = 0;
    this.elseCount_ = 0;
    this.warnings = [{name: "checkEmptySockets", sockets: [{baseName: "IF"}, {baseName: "DO"}]}];
  },
  mutationToDom: function () {
    if (!this.elseifCount_ && !this.elseCount_) {
      return null;
    }
    var container = document.createElement('mutation');
    if (this.elseifCount_) {
      container.setAttribute('elseif', this.elseifCount_);
    }
    if (this.elseCount_) {
      container.setAttribute('else', 1);
    }
    return container;
  },
  domToMutation: function (xmlElement) {
    this.elseifCount_ = parseInt(xmlElement.getAttribute('elseif'), 10) || 0
    this.elseCount_ = window.parseInt(xmlElement.getAttribute('else'), 10) || 0;
    this.rebuildShape_();
  },
  decompose: function (workspace) {
    var containerBlock = workspace.newBlock('controls_if_if');
    containerBlock.initSvg();
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 1; x <= this.elseifCount_; x++) {
      var elseifBlock = workspace.newBlock('controls_if_elseif');
      elseifBlock.initSvg();
      connection.connect(elseifBlock.previousConnection);
      connection = elseifBlock.nextConnection;
    }
    if (this.elseCount_) {
      var elseBlock = workspace.newBlock('controls_if_else');
      elseBlock.initSvg();
      connection.connect(elseBlock.previousConnection);
    }
    return containerBlock;
  },
  compose: function (containerBlock) {
    var clauseBlock = containerBlock.getInputTargetBlock('STACK');
    this.elseifCount_ = 0;
    this.elseCount_ = 0;
    var valueConnections = [null];
    var statementConnections = [null];
    var elseStatementConnection = null;
    while (clauseBlock) {
      switch (clauseBlock.type) {
        case 'controls_if_elseif':
          this.elseifCount_++;
          valueConnections.push(clauseBlock.valueConnection_);
          statementConnections.push(clauseBlock.statementConnection_);
          break;
        case 'controls_if_else':
          this.elseCount_++;
          elseStatementConnection = clauseBlock.statementConnection_;
          break;
        default:
          throw 'Unknown block type.';
      }
      clauseBlock = clauseBlock.nextConnection &&
        clauseBlock.nextConnection.targetBlock();
    }
    this.rebuildShape_();
    this.reconnectChildBlocks_(valueConnections, statementConnections, elseStatementConnection);
  },
  saveConnections: function (containerBlock) {
    // Store a pointer to any connected child blocks.
    var inputDo;
    var clauseBlock = containerBlock.getInput('STACK').connection.targetBlock();
    var x = 1;
    while (clauseBlock) {
      switch (clauseBlock.type) {
        case 'controls_if_elseif':
          var inputIf = this.getInput('IF' + x);
          inputDo = this.getInput('DO' + x);
          clauseBlock.valueConnection_ =
              inputIf && inputIf.connection.targetConnection;
          clauseBlock.statementConnection_ =
              inputDo && inputDo.connection.targetConnection;
          x++;
          break;
        case 'controls_if_else':
          inputDo = this.getInput('ELSE');
          clauseBlock.statementConnection_ =
              inputDo && inputDo.connection.targetConnection;
          break;
        default:
          throw 'Unknown block type.';
      }
      clauseBlock = clauseBlock.nextConnection &&
      clauseBlock.nextConnection.targetBlock();
    }
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF}],
  rebuildShape_: function () {
    console.log('rebuidShape');
    var valueConnections = [null];
    var statementConnections = [null];
    var elseStatementConnection = null;

    if (this.getInput('ELSE')) {
      elseStatementConnection =
          this.getInput('ELSE').connection.targetConnection;
    }
    for (var i = 1; this.getInput('IF' + i); i++) {
      var inputIf = this.getInput('IF' + i);
      var inputDo = this.getInput('DO' + i);
      valueConnections.push(inputIf.connection.targetConnection);
      statementConnections.push(inputDo.connection.targetConnection);
    }
    this.updateShape_();
    this.reconnectChildBlocks_(valueConnections, statementConnections, elseStatementConnection);
  },
  reconnectChildBlocks_: function (valueConnections, statementConnections, elseStatementConnection) {
    for (let i = 1; i <= this.elseifCount_; i++) {
      if (valueConnections[i]) {
        valueConnections[i].reconnect(this, 'IF' + i);
      }
      if (statementConnections[i]) {
        statementConnections[i].reconnect(this, 'DO' + i);
      }
    }
    if (elseStatementConnection) {
      elseStatementConnection.reconnect(this, 'ELSE');
    }
   },
  updateShape_: function() {
    // Delete everything.
    if (this.getInput('ELSE')) {
      this.removeInput('ELSE');
    }
    var i = 1;
    while (this.getInput('IF' + i)) {
      this.removeInput('IF' + i);
      this.removeInput('DO' + i);
      i++;
    }
    // Rebuild block.
    for (var i = 1; i <= this.elseifCount_; i++) {
      this.appendValueInput('IF' + i)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType('boolean', AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF);
      this.appendStatementInput('DO' + i)
        .appendField(Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN);
    }
    if (this.elseCount_) {
      this.appendStatementInput('ELSE')
        .appendField(Blockly.Msg.CONTROLS_IF_MSG_ELSE);
    }
  }
};

Blockly.Blocks['controls_if_if'] = {
  // If condition.
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF);
    this.appendStatementInput('STACK');
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP);
    this.contextMenu = false;
  }
};

Blockly.Blocks['controls_if_elseif'] = {
  // Else-If condition.
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP);
    this.contextMenu = false;
  }
};

Blockly.Blocks['controls_if_else'] = {
  // Else condition.
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP);
    this.contextMenu = false;
  }
};

Blockly.Blocks['controls_forRange'] = {
  // For range.
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    //this.setOutput(true, null);
    // Need to deal with variables here
    // [lyn, 11/30/12] Changed variable to be text input box that does renaming right (i.e., avoids variable capture)
    // Old code:
    // this.appendValueInput('VAR').appendField('for range').appendField('variable').setAlign(Blockly.inputs.Align.RIGHT);
    // this.appendValueInput('START').setCheck(Number).appendField('start').setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('START')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM)
        .appendField(new Blockly.FieldParameterFlydown(Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR, true, Blockly.FieldFlydown.DISPLAY_BELOW), 'VAR')
        .appendField(Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('END')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('STEP')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("number", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendStatementInput('DO')
        .appendField(Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP);
    this.lexicalVarPrefix = Blockly.localNamePrefix;
  },
  referenceResults: function(name, prefix, env) {
    let loopVar = this.getFieldValue('VAR');
    // Invariant: Blockly.showPrefixToUser must also be true!
    if (Blockly.usePrefixInCode) {
      loopVar =
          (Blockly.possiblyPrefixMenuNameWith(Blockly.loopRangeParameterPrefix))(
              loopVar);
    }
    const newEnv = env.concat([loopVar]);
    const startResults = Blockly.LexicalVariable.referenceResult(
        this.getInputTargetBlock('START'), name, prefix, env);
    const endResults = Blockly.LexicalVariable.referenceResult(
        this.getInputTargetBlock('END'), name, prefix, env);
    const stepResults = Blockly.LexicalVariable.referenceResult(
        this.getInputTargetBlock('STEP'), name, prefix, env);
    const doResults = Blockly.LexicalVariable.referenceResult(
        this.getInputTargetBlock('DO'), name, prefix, newEnv);
    const nextResults = Blockly.LexicalVariable.referenceResult(
        Blockly.LexicalVariable.getNextTargetBlock(this), name, prefix, env);
    return [startResults, endResults, stepResults, doResults, nextResults];
  },
  withLexicalVarsAndPrefix: function(child, proc) {
    if (this.getInputTargetBlock('DO') == child) {
      const lexVar = this.getFieldValue('VAR');
      proc(lexVar, this.lexicalVarPrefix);
    }
  },
  getVars: function () {
    return [this.getFieldValue('VAR')];
  },
  blocksInScope: function () {
    var doBlock = this.getInputTargetBlock('DO');
    if (doBlock) {
      return [doBlock];
    } else {
      return [];
    }
  },
  declaredNames: function () {
    return [this.getFieldValue('VAR')];
  },
  renameVar: function (oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getFieldValue('VAR'))) {
      this.setFieldValue(newName, 'VAR');
    }
  },
  renameBound: function (boundSubstitution, freeSubstitution) {
    Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('START'), freeSubstitution);
    Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('END'), freeSubstitution);
    Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('STEP'), freeSubstitution);
    var oldIndexVar = this.getFieldValue('VAR');
    var newIndexVar = boundSubstitution.apply(oldIndexVar);
    if (newIndexVar !== oldIndexVar) {
      this.renameVar(oldIndexVar, newIndexVar);
      var indexSubstitution = Blockly.Substitution.simpleSubstitution(oldIndexVar, newIndexVar);
      var extendedFreeSubstitution = freeSubstitution.extend(indexSubstitution);
      Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('DO'), extendedFreeSubstitution);
    } else {
      var removedFreeSubstitution = freeSubstitution.remove([oldIndexVar]);
      Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('DO'), removedFreeSubstitution);
    }
    if (this.nextConnection) {
      var nextBlock = this.nextConnection.targetBlock();
      Blockly.LexicalVariable.renameFree(nextBlock, freeSubstitution);
    }
  },
  renameFree: function (freeSubstitution) {
    var indexVar = this.getFieldValue('VAR');
    var bodyFreeVars = Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));
    bodyFreeVars.deleteName(indexVar);
    var renamedBodyFreeVars = bodyFreeVars.renamed(freeSubstitution);
    if (renamedBodyFreeVars.isMember(indexVar)) { // Variable capture!
      var newIndexVar = Blockly.FieldLexicalVariable.nameNotIn(indexVar, renamedBodyFreeVars.toList());
      var boundSubstitution = Blockly.Substitution.simpleSubstitution(indexVar, newIndexVar);
      this.renameBound(boundSubstitution, freeSubstitution);
    } else {
      this.renameBound(new Blockly.Substitution(), freeSubstitution);
    }
  },
  freeVariables: function () { // return the free variables of this block
    var result = Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));
    result.deleteName(this.getFieldValue('VAR')); // Remove bound index variable from body free vars
    result.unite(Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('START')));
    result.unite(Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('END')));
    result.unite(Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('STEP')));
    if (this.nextConnection) {
      var nextBlock = this.nextConnection.targetBlock();
      result.unite(Blockly.LexicalVariable.freeVariables(nextBlock));
    }
    return result;
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_FOREACH_NUMBER_TYPEBLOCK}]
};

Blockly.Blocks['controls_forEach'] = {
  // For each loop.
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    //this.setOutput(true, null);
    // [lyn, 10/07/13] Changed default name from "i" to "item"
    // [lyn, 11/29/12] Changed variable to be text input box that does renaming right (i.e., avoids variable capture)
    // Old code:
    // this.appendValueInput('VAR').appendField('for range').appendField('variable').setAlign(Blockly.inputs.Align.RIGHT);
    // this.appendValueInput('START').setCheck(Number).appendField('start').setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('LIST')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM)
        .appendField(new Blockly.FieldParameterFlydown(Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR,
            true, Blockly.FieldFlydown.DISPLAY_BELOW), 'VAR')
        .appendField(Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendStatementInput('DO').appendField(Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP);
  },
  referenceResults: function(name, prefix, env) {
    let loopVar = this.getFieldValue('VAR');
    // Invariant: Blockly.showPrefixToUser must also be true!
    if (Blockly.usePrefixInCode) {
      loopVar = (Blockly.possiblyPrefixMenuNameWith(Blockly.loopParameterPrefix))(
          loopVar);
    }
    const newEnv = env.concat([loopVar]);
    const listResults = Blockly.LexicalVariable.referenceResult(
        this.getInputTargetBlock('LIST'), name, prefix, env);
    const doResults = Blockly.LexicalVariable.referenceResult(
        this.getInputTargetBlock('DO'), name, prefix, newEnv);
    const nextResults = Blockly.LexicalVariable.referenceResult(
        Blockly.LexicalVariable.getNextTargetBlock(this), name, prefix, env);
    return [listResults, doResults, nextResults];
  },
  withLexicalVarsAndPrefix: Blockly.Blocks.controls_forRange.withLexicalVarsAndPrefix,
  getVars: function () {
    return [this.getFieldValue('VAR')];
  },
  blocksInScope: function () {
    var doBlock = this.getInputTargetBlock('DO');
    if (doBlock) {
      return [doBlock];
    } else {
      return [];
    }
  },
  declaredNames: function () {
    return [this.getFieldValue('VAR')];
  },
  renameVar: function (oldName, newName) {
    if (Blockly.Names.equals(oldName, this.getFieldValue('VAR'))) {
      this.setFieldValue(newName, 'VAR');
    }
  },
  renameBound: function (boundSubstitution, freeSubstitution) {
    Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('LIST'), freeSubstitution);
    var oldIndexVar = this.getFieldValue('VAR');
    var newIndexVar = boundSubstitution.apply(oldIndexVar);
    if (newIndexVar !== oldIndexVar) {
      this.renameVar(oldIndexVar, newIndexVar);
      var indexSubstitution = Blockly.Substitution.simpleSubstitution(oldIndexVar, newIndexVar);
      var extendedFreeSubstitution = freeSubstitution.extend(indexSubstitution);
      Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('DO'), extendedFreeSubstitution);
    } else {
      var removedFreeSubstitution = freeSubstitution.remove([oldIndexVar]);
      Blockly.LexicalVariable.renameFree(this.getInputTargetBlock('DO'), removedFreeSubstitution);
    }
    if (this.nextConnection) {
      var nextBlock = this.nextConnection.targetBlock();
      Blockly.LexicalVariable.renameFree(nextBlock, freeSubstitution);
    }
  },
  renameFree: function (freeSubstitution) {
    var indexVar = this.getFieldValue('VAR');
    var bodyFreeVars = Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));
    bodyFreeVars.deleteName(indexVar);
    var renamedBodyFreeVars = bodyFreeVars.renamed(freeSubstitution);
    if (renamedBodyFreeVars.isMember(indexVar)) { // Variable capture!
      var newIndexVar = Blockly.FieldLexicalVariable.nameNotIn(indexVar, renamedBodyFreeVars.toList());
      var boundSubstitution = Blockly.Substitution.simpleSubstitution(indexVar, newIndexVar);
      this.renameBound(boundSubstitution, freeSubstitution);
    } else {
      this.renameBound(new Blockly.Substitution(), freeSubstitution);
    }
  },
  freeVariables: function () { // return the free variables of this block
    var result = Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));
    result.deleteName(this.getFieldValue('VAR')); // Remove bound index variable from body free vars
    result.unite(Blockly.LexicalVariable.freeVariables(this.getInputTargetBlock('LIST')));
    if (this.nextConnection) {
      var nextBlock = this.nextConnection.targetBlock();
      result.unite(Blockly.LexicalVariable.freeVariables(nextBlock));
    }
    return result;
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_FOREACH_ITEM_TYPEBLOCK}]
};

Blockly.Blocks['controls_for_each_dict'] = {
  category: 'Control',

  helpUrl: Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_HELPURL,

  init: function() {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    var checkTypeDict = AI.BlockUtils.YailTypeToBlocklyType(
        'dictionary', AI.BlockUtils.INPUT);
    var keyField = new Blockly.FieldParameterFlydown(
        Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_KEY,
        true, Blockly.FieldFlydown.DISPLAY_BELOW);
    var valueField = new Blockly.FieldParameterFlydown(
        Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_VALUE,
        true, Blockly.FieldFlydown.DISPLAY_BELOW);
    this.interpolateMsg(Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT,
        ['KEY', keyField],
        ['VALUE', valueField],
        ['DICT', checkTypeDict, Blockly.inputs.Align.LEFT],
        Blockly.inputs.Align.LEFT);
    this.appendStatementInput('DO')
        .appendField(Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_DO);
    this.setInputsInline(false);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_TOOLTIP);
    this.lexicalVarPrefix = Blockly.localNamePrefix;
  },

  getDeclaredVarFieldNamesAndPrefixes: function() {
    return [['KEY', 'key'], ['VALUE', 'value']];
  },

  getScopedInputName: function() {
    return 'DO';
  },

  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_TITLE}]
};
AI.Blockly.Mixins.extend(Blockly.Blocks['controls_for_each_dict'], AI.Blockly.Mixins.LexicalVariableMethods);

Blockly.Blocks['controls_while'] = {
  // While condition.
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('TEST')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("boolean", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_WHILE_TITLE)
        .appendField(Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendStatementInput('DO')
        .appendField(Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_WHILE_TITLE}]
};

// [lyn, 01/15/2013] Remove DO C-sockets because now handled more modularly by DO-THEN-RETURN block.
Blockly.Blocks['controls_choose'] = {
  // Choose.
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.setOutput(true, null);
    this.appendValueInput('TEST')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("boolean", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE)
        .appendField(Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST)
        .setAlign(Blockly.inputs.Align.RIGHT);
    // this.appendStatementInput('DO0').appendField('then-do').setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('THENRETURN')
        .appendField(Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN)
        .setAlign(Blockly.inputs.Align.RIGHT);
    // this.appendStatementInput('ELSE').appendField('else-do').setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('ELSERETURN')
        .appendField(Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN)
        .setAlign(Blockly.inputs.Align.RIGHT);
    /* this.setTooltip('If the condition being tested is true, the agent will '
     + 'run all the blocks attached to the \'then-do\' section and return the value attached '
     + 'to the \'then-return\'slot. Otherwise, the agent will run all blocks attached to '
     + 'the \'else-do\' section and return the value in the \'else-return\' slot.');
     */
    // [lyn, 01/15/2013] Edit description to be consistent with changes to slots.
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP);
  },
  typeblock: [{
    translatedName: Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE + ' ' +
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN + ' ' +
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN
  }]
};

// [lyn, 10/10/13] This used to be in the control drawer as well as the procedure drawer
// but someone removed it from the control drawer. I think it still belongs here.
Blockly.Blocks['controls_do_then_return'] = {
  // String length.
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendStatementInput('STM')
        .appendField(Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO);
    this.appendValueInput('VALUE')
        .appendField(Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.setOutput(true, null);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE}]
};

// [lyn, 01/15/2013] Added
Blockly.Blocks['controls_eval_but_ignore'] = {
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('VALUE')
        .appendField(Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE}]
};

Blockly.Blocks['controls_openAnotherScreen'] = {
  // Open another screen
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('SCREEN')
        .appendField(Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE)
        .appendField(Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME)
        .setAlign(Blockly.inputs.Align.RIGHT)
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT));
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE}]
};

Blockly.Blocks['controls_openAnotherScreenWithStartValue'] = {
  // Open another screen with start value
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('SCREENNAME')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE)
        .appendField(Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.appendValueInput('STARTVALUE')
        .appendField(Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE}]
};

Blockly.Blocks['controls_getStartValue'] = {
  // Get start value
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.setOutput(true, null);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE}]
};

Blockly.Blocks['controls_closeScreen'] = {
  // Close screen
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE}]
};

Blockly.Blocks['controls_closeScreenWithValue'] = {
  // Close screen with value
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('SCREEN')
        .appendField(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE)
        .appendField(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE}]
};

Blockly.Blocks['controls_closeApplication'] = {
  // Close application
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput().appendField(Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE}]
};

Blockly.Blocks['controls_getPlainStartText'] = {
  // Get plain start text
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.OUTPUT));
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE}]
};

Blockly.Blocks['controls_closeScreenWithPlainText'] = {
  // Close screen with plain text
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendValueInput('TEXT')
        .setCheck(AI.BlockUtils.YailTypeToBlocklyType("text", AI.BlockUtils.INPUT))
        .appendField(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE)
        .appendField(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT)
        .setAlign(Blockly.inputs.Align.RIGHT);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP);
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE}]
};

Blockly.Blocks['controls_break'] = {
    // generate a call to break (the escape from loops)
  category: 'Control',
  helpUrl: Blockly.Msg.LANG_CONTROLS_BREAK_HELPURL,
  init: function () {
    this.setColour(Blockly.CONTROL_CATEGORY_HUE);
    this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_CONTROLS_BREAK_TITLE);
    this.setPreviousStatement(true);
    this.setTooltip(Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK);
    this.errors = [{name:"checkIsNotInLoop"}];
  },
  typeblock: [{translatedName: Blockly.Msg.LANG_CONTROLS_BREAK_TITLE}]
};

