suite('Procedures', function() {
  let workspace;

  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
    Blockly.common.setSelected(null);
  });

  test('declared no-return procedure parameter in scope', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>',
      inputs: {
        STACK: {
          block: {
            type: 'lexical_variable_set',
            fields: { VAR: 'myParam' },
            inputs: {
              VALUE: {
                block: { type: 'lexical_variable_get', fields: { VAR: 'myParam' } }
              }
            }
          }
        }
      }
    }, workspace);
    workspace.getWarningHandler().checkAllBlocksForWarningsAndErrors();

    const myParamSetBlock = procedureBlock.getInputTargetBlock('STACK');
    const myParamGetBlock = myParamSetBlock.getInputTargetBlock('VALUE');
    chai.assert.isNotTrue(myParamGetBlock.hasError);
    chai.assert.isNotTrue(myParamSetBlock.hasError);
  });

  test('declared return procedure parameter in scope', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>',
      inputs: {
        RETURN: {
          block: {
            type: 'controls_do_then_return',
            inputs: {
              STM: {
                block: { type: 'lexical_variable_set', fields: { VAR: 'myParam' } }
              },
              VALUE: {
                block: { type: 'lexical_variable_get', fields: { VAR: 'myParam' } }
              }
            }
          }
        }
      }
    }, workspace);
    workspace.getWarningHandler().checkAllBlocksForWarningsAndErrors();

    const doThenReturnBlock = procedureBlock.getInputTargetBlock('RETURN');
    const myParamSetBlock = doThenReturnBlock.getInputTargetBlock('STM');
    const myParamGetBlock = doThenReturnBlock.getInputTargetBlock('VALUE');
    chai.assert.isNotTrue(myParamSetBlock.hasError);
    chai.assert.isNotTrue(myParamGetBlock.hasError);
  });

  test('declared no-return procedure parameter out of scope', function() {
    Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>'
    }, workspace);
    const myParamSetBlock = Blockly.serialization.blocks.append({
      type: 'lexical_variable_set',
      fields: { VAR: 'myParam' },
      inputs: {
        VALUE: {
          block: { type: 'lexical_variable_get', fields: { VAR: 'myParam' } }
        }
      }
    }, workspace);
    workspace.getWarningHandler().checkAllBlocksForWarningsAndErrors();

    const myParamGetBlock = myParamSetBlock.getInputTargetBlock('VALUE');
    chai.assert.isTrue(myParamGetBlock.hasError);
    chai.assert.isTrue(myParamSetBlock.hasError);
  });

  test('declared return procedure parameter out of scope', function() {
    Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>'
    }, workspace);
    const doThenReturnBlock = Blockly.serialization.blocks.append({
      type: 'controls_do_then_return',
      inputs: {
        STM: {
          block: { type: 'lexical_variable_set', fields: { VAR: 'myParam' } }
        },
        VALUE: {
          block: { type: 'lexical_variable_get', fields: { VAR: 'myParam' } }
        }
      }
    }, workspace);
    workspace.getWarningHandler().checkAllBlocksForWarningsAndErrors();

    const myParamSetBlock = doThenReturnBlock.getInputTargetBlock('STM');
    const myParamGetBlock = doThenReturnBlock.getInputTargetBlock('VALUE');
    chai.assert.isTrue(myParamSetBlock.hasError);
    chai.assert.isTrue(myParamGetBlock.hasError);
  });

  test('undeclared procedure parameter', function() {
    const myParamSetBlock = Blockly.serialization.blocks.append({
      type: 'lexical_variable_set',
      fields: { VAR: 'myParam' },
      inputs: {
        VALUE: {
          block: { type: 'lexical_variable_get', fields: { VAR: 'myParam' } }
        }
      }
    }, workspace);
    workspace.getWarningHandler().checkAllBlocksForWarningsAndErrors();

    const myParamGetBlock = myParamSetBlock.getInputTargetBlock('VALUE');
    chai.assert.isTrue(myParamGetBlock.hasError);
    chai.assert.isTrue(myParamSetBlock.hasError);
  });

  test('rename no-return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myOldParam"></arg></mutation>',
      inputs: {
        STACK: {
          block: {
            type: 'lexical_variable_set',
            fields: { VAR: 'myOldParam' },
            inputs: {
              VALUE: {
                block: { type: 'lexical_variable_get', fields: { VAR: 'myOldParam' } }
              }
            }
          }
        }
      }
    }, workspace);
    procedureBlock.setFieldValue('myNewParam', 'VAR0');

    const myParamSetBlock = procedureBlock.getInputTargetBlock('STACK');
    const myParamGetBlock = myParamSetBlock.getInputTargetBlock('VALUE');
    chai.assert.equal(myParamSetBlock.getFieldValue('VAR'), 'myNewParam');
    chai.assert.equal(myParamGetBlock.getFieldValue('VAR'), 'myNewParam');
  });

  test('rename return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myOldParam"></arg></mutation>',
      inputs: {
        RETURN: {
          block: {
            type: 'controls_do_then_return',
            inputs: {
              STM: {
                block: { type: 'lexical_variable_set', fields: { VAR: 'myOldParam' } }
              },
              VALUE: {
                block: { type: 'lexical_variable_get', fields: { VAR: 'myOldParam' } }
              }
            }
          }
        }
      }
    }, workspace);
    procedureBlock.setFieldValue('myNewParam', 'VAR0');

    const doThenReturnBlock = procedureBlock.getInputTargetBlock('RETURN');
    const myParamSetBlock = doThenReturnBlock.getInputTargetBlock('STM');
    const myParamGetBlock = doThenReturnBlock.getInputTargetBlock('VALUE');
    chai.assert.equal(myParamSetBlock.getFieldValue('VAR'), 'myNewParam');
    chai.assert.equal(myParamGetBlock.getFieldValue('VAR'), 'myNewParam');
  });

  test('rename no-return procedure parameter to duplicate name', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg><arg name="myOtherParam"></arg></mutation>',
      inputs: {
        STACK: {
          block: {
            type: 'lexical_variable_set',
            fields: { VAR: 'myOtherParam' },
            inputs: {
              VALUE: {
                block: { type: 'lexical_variable_get', fields: { VAR: 'myOtherParam' } }
              }
            }
          }
        }
      }
    }, workspace);
    procedureBlock.setFieldValue('myParam', 'VAR1');

    chai.assert.equal(procedureBlock.getFieldValue('VAR1'), 'myParam2');
    const myParamSetBlock = procedureBlock.getInputTargetBlock('STACK');
    const myParamGetBlock = myParamSetBlock.getInputTargetBlock('VALUE');
    chai.assert.equal(myParamSetBlock.getFieldValue('VAR'), 'myParam2');
    chai.assert.equal(myParamGetBlock.getFieldValue('VAR'), 'myParam2');
  });

  test('rename return procedure parameter to duplicate name', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg><arg name="myOtherParam"></arg></mutation>',
      inputs: {
        RETURN: {
          block: {
            type: 'controls_do_then_return',
            inputs: {
              STM: {
                block: { type: 'lexical_variable_set', fields: { VAR: 'myOtherParam' } }
              },
              VALUE: {
                block: { type: 'lexical_variable_get', fields: { VAR: 'myOtherParam' } }
              }
            }
          }
        }
      }
    }, workspace);
    procedureBlock.setFieldValue('myParam', 'VAR1');

    chai.assert.equal(procedureBlock.getFieldValue('VAR1'), 'myParam2');
    const doThenReturnBlock = procedureBlock.getInputTargetBlock('RETURN');
    const myParamSetBlock = doThenReturnBlock.getInputTargetBlock('STM');
    const myParamGetBlock = doThenReturnBlock.getInputTargetBlock('VALUE');
    chai.assert.equal(myParamSetBlock.getFieldValue('VAR'), 'myParam2');
    chai.assert.equal(myParamGetBlock.getFieldValue('VAR'), 'myParam2');
  });

  test('add no-return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn', fields: { NAME: 'myProcedure' }
    }, workspace);
    const mutatorDiv = document.createElement('div');
    const mutatorWorkspace = Blockly.inject(mutatorDiv, {});
    const mutatorRootBlock = procedureBlock.decompose(mutatorWorkspace);
    const myParamBlock = Blockly.serialization.blocks.append({
      type: 'procedures_mutatorarg', fields: { NAME: 'myParam' }
    }, mutatorWorkspace);
    myParamBlock.previousConnection.connect(mutatorRootBlock.getInput('STACK').connection);
    procedureBlock.compose(mutatorRootBlock);

    chai.assert.equal(procedureBlock.getFieldValue('VAR0'), 'myParam');
  });

  test('add return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defreturn', fields: { NAME: 'myProcedure' }
    }, workspace);
    const mutatorDiv = document.createElement('div');
    const mutatorWorkspace = Blockly.inject(mutatorDiv, {});
    const mutatorRootBlock = procedureBlock.decompose(mutatorWorkspace);
    const myParamBlock = Blockly.serialization.blocks.append({
      type: 'procedures_mutatorarg', fields: { NAME: 'myParam' }
    }, mutatorWorkspace);
    myParamBlock.previousConnection.connect(mutatorRootBlock.getInput('STACK').connection);
    procedureBlock.compose(mutatorRootBlock);

    chai.assert.equal(procedureBlock.getFieldValue('VAR0'), 'myParam');
  });

  test('reorder no-return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg><arg name="myOtherParam"></arg></mutation>'
    }, workspace);
    const mutatorDiv = document.createElement('div');
    const mutatorWorkspace = Blockly.inject(mutatorDiv, {});
    const mutatorRootBlock = procedureBlock.decompose(mutatorWorkspace);
    const myParamBlock = mutatorRootBlock.getInputTargetBlock('STACK');
    const myOtherParamBlock = myParamBlock.nextConnection.targetBlock();
    myParamBlock.nextConnection.disconnect();
    myOtherParamBlock.previousConnection.connect(mutatorRootBlock.getInput('STACK').connection);
    myOtherParamBlock.nextConnection.connect(myParamBlock.previousConnection);
    procedureBlock.compose(mutatorRootBlock);

    chai.assert.equal(procedureBlock.getFieldValue('VAR0'), 'myOtherParam');
    chai.assert.equal(procedureBlock.getFieldValue('VAR1'), 'myParam');
  });

  test('reorder return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg><arg name="myOtherParam"></arg></mutation>'
    }, workspace);
    const mutatorDiv = document.createElement('div');
    const mutatorWorkspace = Blockly.inject(mutatorDiv, {});
    const mutatorRootBlock = procedureBlock.decompose(mutatorWorkspace);
    const myParamBlock = mutatorRootBlock.getInputTargetBlock('STACK');
    const myOtherParamBlock = myParamBlock.nextConnection.targetBlock();
    myParamBlock.nextConnection.disconnect();
    myOtherParamBlock.previousConnection.connect(mutatorRootBlock.getInput('STACK').connection);
    myOtherParamBlock.nextConnection.connect(myParamBlock.previousConnection);
    procedureBlock.compose(mutatorRootBlock);

    chai.assert.equal(procedureBlock.getFieldValue('VAR0'), 'myOtherParam');
    chai.assert.equal(procedureBlock.getFieldValue('VAR1'), 'myParam');
  });

  test('remove no-return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>'
    }, workspace);
    const mutatorDiv = document.createElement('div');
    const mutatorWorkspace = Blockly.inject(mutatorDiv, {});
    const mutatorRootBlock = procedureBlock.decompose(mutatorWorkspace);
    const myParamBlock = mutatorRootBlock.getInputTargetBlock('STACK');
    myParamBlock.dispose();
    procedureBlock.compose(mutatorRootBlock);

    chai.assert.isNull(procedureBlock.getField('VAR0'));
  });

  test('remove return procedure parameter', function() {
    const procedureBlock = Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>'
    }, workspace);
    const mutatorDiv = document.createElement('div');
    const mutatorWorkspace = Blockly.inject(mutatorDiv, {});
    const mutatorRootBlock = procedureBlock.decompose(mutatorWorkspace);
    const myParamBlock = mutatorRootBlock.getInputTargetBlock('STACK');
    myParamBlock.dispose();
    procedureBlock.compose(mutatorRootBlock);

    chai.assert.isNull(procedureBlock.getField('VAR0'));
  });
});
