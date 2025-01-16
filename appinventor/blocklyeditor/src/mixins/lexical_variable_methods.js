// -*- mode: javascript;js-indent-level: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// noinspection JSUnusedGlobalSymbols

'use strict';

goog.provide('AI.Blockly.Mixins.LexicalVariableMethods');

/**
 * The block that this is mixed into needs to have the following methods defined on it:
 * - getDeclaredVarFieldNamesAndPrefixes(): a list of pairs of the names of the fields of the block's declared variables
 *     and the prefixes to use for those variables (e.g., [["KEY", "key"], ["VALUE", "value"]]) if we're using prefixes.
 * - getScopedInputName(): a list of the names of the input that defines the block's scope (e.g., "DO")
 * Note that users of this mixin do not need to define lexicalVarPrefix, since it is returned as part of
 * getDeclaredVarFieldNamesAndPrefixes().
 *
 * @mixin
 */
AI.Blockly.Mixins.LexicalVariableMethods = {
  getDeclaredVarFieldNames: function () {
    const thisBlock = this;
    return thisBlock.getDeclaredVarFieldNamesAndPrefixes().map(([varFieldName, _]) => varFieldName);
  },

  referenceResults: function (name, prefix, env) {
    const thisBlock = this;
    const possiblyPrefixedVarNames = thisBlock.getDeclaredVarFieldNamesAndPrefixes().map(([varFieldName, varPrefix]) => {
      // TODO: If this code moves into the lexvar plugin, then change the following to Shared.usePrefixInCode
      if (Blockly.usePrefixInYail) {
        const varName = thisBlock.getFieldValue(varFieldName);
        const nameFunc = Blockly.possiblyPrefixMenuNameWith(varPrefix);
        return nameFunc(varName);
      } else {
        return thisBlock.getFieldValue(varFieldName);
      }
    });
    const newEnv = env.concat(possiblyPrefixedVarNames);
    const inputResults = thisBlock.inputList.map((input) => {
      return Blockly.LexicalVariable.referenceResult(
          thisBlock.getInputTargetBlock(input.name),
          name,
          prefix,
          input.name === thisBlock.getScopedInputName() ? newEnv : env);
    });
    if (thisBlock.nextConnection) {
      inputResults.push(Blockly.LexicalVariable.referenceResult(
          thisBlock.getNextBlock(), name, prefix, env));
    }
    return inputResults;
  },

  withLexicalVarsAndPrefix: function (child, proc) {
    const thisBlock = this;
    if (child && thisBlock.getInputTargetBlock(thisBlock.getScopedInputName()) === child) {
      thisBlock.getDeclaredVarFieldNamesAndPrefixes().forEach(([fieldName, prefix]) => {
        const lexVar = thisBlock.getFieldValue(fieldName);
        proc(lexVar, thisBlock.prefix);
      });
    }
  },

  getVars: function () {
    const thisBlock = this;
    return thisBlock.getDeclaredVarFieldNames().map((fieldName) => {
      return thisBlock.getFieldValue(fieldName);
    });
  },

  blocksInScope: function () {
    const thisBlock = this;
    const blocks = thisBlock.getInputTargetBlock(thisBlock.getScopedInputName());
    return (blocks && [blocks]) || [];
  },

  declaredNames: function () { // TODO: Not sure why we have two different names for the same thing.
    return this.getVars()
  },

  renameVar: function (oldName, newName) {
    const thisBlock = this;
    thisBlock.getDeclaredVarFieldNames().forEach((fieldName) => {
      if (Blockly.Names.equals(oldName, thisBlock.getFieldValue(fieldName))) {
        thisBlock.setFieldValue(newName, fieldName);
      }
    });
  },

  renameBound: function (boundSubstitution, freeSubstitution) {
    const thisBlock = this;
    let modifiedSubstitution = freeSubstitution;
    thisBlock.inputList.forEach((input) => {
      if (input.name === thisBlock.getScopedInputName()) {
        thisBlock.getDeclaredVarFieldNames().forEach((fieldName) => {
          const oldVar = thisBlock.getFieldValue(fieldName);
          const newVar = boundSubstitution.apply(oldVar);
          if (newVar !== oldVar) {
            thisBlock.renameVar(oldVar, newVar);
            const varSubstitution = Blockly.Substitution.simpleSubstitution(
                oldVar, newVar);
            modifiedSubstitution = freeSubstitution.extend(varSubstitution);
          } else {
            modifiedSubstitution = freeSubstitution.remove([oldVar]);
          }
        });
        Blockly.LexicalVariable.renameFree(
            thisBlock.getInputTargetBlock(thisBlock.getScopedInputName()),
            modifiedSubstitution);
      } else {
        Blockly.LexicalVariable.renameFree(
            thisBlock.getInputTargetBlock(input.name), freeSubstitution);
      }
    });
    if (thisBlock.nextConnection) {
      const nextBlock = thisBlock.nextConnection.targetBlock();
      Blockly.LexicalVariable.renameFree(nextBlock, freeSubstitution);
    }
  },

  renameFree: function (freeSubstitution) {
    const thisBlock = this;
    const bodyFreeVars = Blockly.LexicalVariable.freeVariables(
        thisBlock.getInputTargetBlock(thisBlock.getScopedInputName()));

    const boundSubstitution = new Blockly.Substitution();
    thisBlock.getDeclaredVarFieldNames().forEach((fieldName) => {
      const oldVar = thisBlock.getFieldValue(fieldName);
      bodyFreeVars.deleteName(oldVar);
      const renamedBodyFreeVars = bodyFreeVars.renamed(freeSubstitution);
      if (renamedBodyFreeVars.isMember(oldVar)) { // Variable is bound in body.
        const newVar = Blockly.FieldLexicalVariable.nameNotIn(
            oldVar, renamedBodyFreeVars.toList());
        const substitution = Blockly.Substitution.simpleSubstitution(
            oldVar, newVar);
        boundSubstitution.extend(substitution);
      }
    });
    thisBlock.renameBound(boundSubstitution, freeSubstitution);
  },

  freeVariables: function () { // return the free variables of this block
    const thisBlock = this;
    const result = Blockly.LexicalVariable.freeVariables(
        thisBlock.getInputTargetBlock(thisBlock.getScopedInputName()));

    thisBlock.getDeclaredVarFieldNames().forEach((fieldName) => {
      result.deleteName(thisBlock.getFieldValue(fieldName));
    });

    // Add free variables from other inputs.
    thisBlock.inputList.forEach((input) => {
      if (input.name !== thisBlock.getScopedInputName()) {
        result.unite(Blockly.LexicalVariable.freeVariables(
            thisBlock.getInputTargetBlock(input.name)));
      }
    });

    // Add the free variables from the next block(s).
    if (thisBlock.nextConnection) {
      var nextBlock = thisBlock.nextConnection.targetBlock();
      result.unite(Blockly.LexicalVariable.freeVariables(nextBlock));
    }

    return result;
  },
}
