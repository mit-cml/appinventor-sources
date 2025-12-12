goog.provide('AI.Blockly.TypeBlockOptionGenerator');

AI.Blockly.TypeBlockOptionGenerator = function(workspace) {
  this.workspace_ = workspace;
};

AI.Blockly.TypeBlockOptionGenerator.prototype.generateOptions = function() {
  const options = [
    ...this.getVariableOptions(),
    ...this.getProcedureOptions(),
    ...this.getBuiltinBlockAndComponentOptions()
  ];

  options.sort((a, b) => a.displayText > b.displayText ? 1 : a.displayText < b.displayText ? -1 : 0);

  return options;
};

AI.Blockly.TypeBlockOptionGenerator.prototype.getVariableOptions = function() {
  const createVarOption = (blockType, scope, varName) => {
    const displayTextByBlockType = {
      lexical_variable_get: Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET,
      lexical_variable_set: Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET
    };

    const varNameByScope = {
      global: Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX + ' ' + varName,
      local: varName
    };

    return {
      blockType: blockType,
      displayText: displayTextByBlockType[blockType] + ' ' + varNameByScope[scope],
      fieldValues: { VAR: varNameByScope[scope] }
    };
  };

  const options = [];

  const globalVarNames = Blockly.FieldLexicalVariable.getGlobalNames();
  for (const globalVarName of globalVarNames) {
    options.push(createVarOption('lexical_variable_get', 'global', globalVarName));
    options.push(createVarOption('lexical_variable_set', 'global', globalVarName));
  }

  const selected = Blockly.common.getSelected();
  if (selected) {
    // Index 0 should be the translated name.
    let localVarNames = Blockly.FieldLexicalVariable.getLexicalNamesInScope(selected)
      .map(varNameArray => varNameArray[0]);

    // getLexicalNamesInScope does not include names declared on the block passed.
    if (selected.getVars) {
      // TODO: This doesn't currently support variable prefixes, but I don't want
      //  to duplicate all of the logic inside getLexicalNamesInScope(). If the
      //  suggestion for #2033 gets accepted this will be an easy fix.
      localVarNames = localVarNames.concat(
        selected.getVars().map(varName =>
          this.workspace_.getTopWorkspace().getComponentDatabase()
            .getInternationalizedParameterName(varName)
        ));
    }

    for (const localVarName of localVarNames) {
      options.push(createVarOption('lexical_variable_get', 'local', localVarName));
      options.push(createVarOption('lexical_variable_set', 'local', localVarName));
    }
  }

  return options;
};

AI.Blockly.TypeBlockOptionGenerator.prototype.getProcedureOptions = function() {
  const options = [];

  const createProcedureOption = (blockType, displayProcedureName, procedureName) => {
    const displayTextByBlockType = {
      procedures_callnoreturn: Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL,
      procedures_callreturn: Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL
    };

    return {
      blockType: blockType,
      displayText: displayTextByBlockType[blockType] + displayProcedureName,
      fieldValues: {
        PROCNAME: procedureName
      }
    };
  };

  const addProcedureOptions = (blockType, withReturn) => {
    const procedures = this.workspace_.getProcedureDatabase().getMenuItems(withReturn);
    // Skip the empty placeholder entry that's returned when there are no procedures
    if (!(procedures.length === 1 && procedures[0][0] === '')) {
      for (const [displayProcedureName, procedureName] of procedures) {
        options.push(createProcedureOption(blockType, displayProcedureName, procedureName));
      }
    }
  };

  addProcedureOptions('procedures_callnoreturn', false);
  addProcedureOptions('procedures_callreturn', true);

  return options;
};

AI.Blockly.TypeBlockOptionGenerator.prototype.getBuiltinBlockAndComponentOptions = function() {
  const options = [];

  for (const blockType of Object.keys(Blockly.Blocks)) {
    const block = Blockly.Blocks[blockType];
    if (block.typeblock) {
      const typeblockOptions = typeof block.typeblock === 'function'
        ? block.typeblock()
        : block.typeblock;

      for (const typeblockOption of typeblockOptions) {
        const displayText = typeblockOption.translatedName.replace(/%[0-9]+/g, '');
        const option = {
          blockType: blockType,
          displayText: displayText
        };

        if (typeblockOption.mutatorAttributes) {
          const mutation = document.createElement('mutation');
          for (const [attributeName, attributeValue] of Object.entries(typeblockOption.mutatorAttributes)) {
            mutation.setAttribute(attributeName, attributeValue);
          }
          option.extraState = Blockly.utils.xml.domToText(mutation);
        }

        if (typeblockOption.dropDown) {
          option.fieldValues = {};
          option.fieldValues[typeblockOption.dropDown.titleName] = typeblockOption.dropDown.value;
        }

        options.push(option);
      }
    }
  }

  return options;
};
