suite('TypeBlock', function() {
  let workspace;

  setup(async function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
  });

  function input(text) {
    workspace.typeBlock_.controller.show();
    const input = Blockly.WidgetDiv.getDiv().querySelector('.fi-input');
    input.value = text;
    input.dispatchEvent(new InputEvent('input'));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
  }

  test('create number block', async function() {
    await act(() => input('42'));

    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number');
    chai.assert.equal(numberBlock.getFieldValue('NUM'), '42');
    chai.assert.equal(Blockly.common.getSelected(), numberBlock);
  });

  test('create text block', async function() {
    await act(() => input('"hello"'));

    const textBlock = workspace.getAllBlocks().find(b => b.type === 'text');
    chai.assert.equal(textBlock.getFieldValue('TEXT'), 'hello');
    chai.assert.equal(Blockly.common.getSelected(), textBlock);
  });

  test('create global variable get block', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace));
    await act(() => input('get global myVar'));

    const variableGetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_get');
    chai.assert.equal(variableGetBlock.getFieldValue('VAR'), 'global myVar');
    chai.assert.equal(Blockly.common.getSelected(), variableGetBlock);
  });

  test('create global variable set block', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace));
    await act(() => input('set global myVar'));

    const variableSetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_set');
    chai.assert.equal(variableSetBlock.getFieldValue('VAR'), 'global myVar');
    chai.assert.equal(Blockly.common.getSelected(), variableSetBlock);
  });

  test('create local variable get block', async function() {
    const initializeVariable = await act(() => Blockly.serialization.blocks.append({
      type: 'local_declaration_statement',
      fields: { VAR0: 'myVar' }
    }, workspace));
    await act(() => Blockly.common.setSelected(initializeVariable));
    await act(() => input('get myVar'));

    const variableGetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_get');
    chai.assert.equal(variableGetBlock.getFieldValue('VAR'), 'myVar');
    chai.assert.equal(Blockly.common.getSelected(), variableGetBlock);
  });

  test('create local variable set block', async function() {
    const initializeVariable = await act(() => Blockly.serialization.blocks.append({
      type: 'local_declaration_statement',
      fields: { VAR0: 'myVar' }
    }, workspace));
    await act(() => Blockly.common.setSelected(initializeVariable));
    await act(() => input('set myVar'));

    const variableSetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_set');
    chai.assert.equal(variableSetBlock.getFieldValue('VAR'), 'myVar');
    chai.assert.equal(Blockly.common.getSelected(), variableSetBlock);
  });

  test('create procedure call no return block', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>'
    }, workspace));
    await act(() => input('call myProcedure'));

    const procedureCallBlock = workspace.getAllBlocks().find(b => b.type === 'procedures_callnoreturn');
    chai.assert.equal(procedureCallBlock.getFieldValue('PROCNAME'), 'myProcedure');
    chai.assert.isNotNull(procedureCallBlock.getInput('ARG0'));
    chai.assert.equal(Blockly.common.getSelected(), procedureCallBlock);
  });

  test('create procedure call return block', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' },
      extraState: '<mutation><arg name="myParam"></arg></mutation>'
    }, workspace));
    await act(() => input('call myProcedure'));

    const procedureCallBlock = workspace.getAllBlocks().find(b => b.type === 'procedures_callreturn');
    chai.assert.equal(procedureCallBlock.getFieldValue('PROCNAME'), 'myProcedure');
    chai.assert.isNotNull(procedureCallBlock.getInput('ARG0'));
    chai.assert.equal(Blockly.common.getSelected(), procedureCallBlock);
  });

  test('create split at first block', async function() {
    await act(() => input('split at first'));

    const splitBlock = workspace.getAllBlocks().find(b => b.type === 'text_split');
    chai.assert.equal(splitBlock.getFieldValue('OP'), 'SPLITATFIRST');
    chai.assert.isNotNull(splitBlock.getInput('AT'));
    chai.assert.include(splitBlock.getInput('AT').connection.getCheck(), 'String');
    chai.assert.equal(Blockly.common.getSelected(), splitBlock);
  });

  test('create split at first of any block', async function() {
    await act(() => input('split at first of any'));

    const splitBlock = workspace.getAllBlocks().find(b => b.type === 'text_split');
    chai.assert.equal(splitBlock.getFieldValue('OP'), 'SPLITATFIRSTOFANY');
    chai.assert.isNotNull(splitBlock.getInput('AT'));
    chai.assert.include(splitBlock.getInput('AT').connection.getCheck(), 'Array');
    chai.assert.equal(Blockly.common.getSelected(), splitBlock);
  });

  test('create button click block', async function() {
    initComponentTypes();
    await act(() => workspace.addComponent('-1048419043', 'Button1', 'Button'));
    await act(() => input('when Button1.Click'));

    const eventBlock = workspace.getAllBlocks().find(b => b.type === 'component_event');
    chai.assert.equal(eventBlock.typeName, 'Button');
    chai.assert.equal(eventBlock.eventName, 'Click');
    chai.assert.equal(eventBlock.instanceName, 'Button1');
    chai.assert.equal(Blockly.common.getSelected(), eventBlock);
  });

  test('value block connects to first compatible empty input', async function() {
    const ifBlock = await act(() => Blockly.serialization.blocks.append({
      type: 'controls_if'
    }, workspace));
    await act(() => Blockly.common.setSelected(ifBlock));
    await act(() => input('true'));

    const booleanBlock = workspace.getAllBlocks().find(b => b.type === 'logic_boolean');
    chai.assert.equal(booleanBlock.parentBlock_, ifBlock);
    const conditionInput = ifBlock.getInput('IF0');
    chai.assert.equal(conditionInput.connection.targetConnection, booleanBlock.outputConnection);
    chai.assert.equal(Blockly.common.getSelected(), booleanBlock);
  });

  test('statement block connects to first empty statement input', async function() {
    const ifBlock = await act(() => Blockly.serialization.blocks.append({
      type: 'controls_if',
      inputs: {
        IF0: {
          block: { type: 'logic_boolean', fields: { BOOL: 'TRUE' } }
        }
      }
    }, workspace));
    await act(() => Blockly.common.setSelected(ifBlock));
    await act(() => Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace));
    await act(() => input('set global myVar'));

    const setBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_set');
    chai.assert.equal(setBlock.parentBlock_, ifBlock);
    const doInput = ifBlock.getInput('DO0');
    chai.assert.equal(doInput.connection.targetConnection, setBlock.previousConnection);
    chai.assert.equal(Blockly.common.getSelected(), setBlock);
  });

  test('statement block connects below selected statement block', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace));
    const setBlock = await act(() => Blockly.serialization.blocks.append({
      type: 'lexical_variable_set',
      fields: { VAR: 'global myVar' },
      inputs: {
        VALUE: {
          block: { type: 'math_number', fields: { NUM: 42 } }
        }
      }
    }, workspace));
    await act(() => Blockly.common.setSelected(setBlock));
    await act(() => input('if'));

    const ifBlock = workspace.getAllBlocks().find(b => b.type === 'controls_if');
    chai.assert.equal(ifBlock.previousConnection.targetConnection, setBlock.nextConnection);
    chai.assert.equal(Blockly.common.getSelected(), ifBlock);
  });

  test('statement block connects below parent statement', async function() {
    const parentIf = await act(() => Blockly.serialization.blocks.append({
      type: 'controls_if',
      inputs: {
        IF0: {
          block: { type: 'logic_boolean', fields: { BOOL: 'TRUE' } }
        }
      }
    }, workspace));
    const booleanBlock = workspace.getAllBlocks().find(b => b.type === 'logic_boolean');
    await act(() => Blockly.common.setSelected(booleanBlock));
    await act(() => input('if'));

    const newIfBlock = workspace.getAllBlocks().find(b => b.type === 'controls_if' && b !== parentIf);
    chai.assert.equal(newIfBlock.previousConnection.targetConnection, parentIf.nextConnection);
    chai.assert.equal(Blockly.common.getSelected(), newIfBlock);
  });

  test('value block remains standalone when parent is statement', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'controls_if',
      inputs: {
        IF0: {
          block: { type: 'logic_boolean', fields: { BOOL: 'TRUE' } }
        }
      }
    }, workspace));
    const booleanBlock = workspace.getAllBlocks().find(b => b.type === 'logic_boolean');
    await act(() => Blockly.common.setSelected(booleanBlock));
    await act(() => input('42'));

    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number');
    chai.assert.isNull(numberBlock.parentBlock_);
    chai.assert.equal(Blockly.common.getSelected(), numberBlock);
  });

  test('value block recursively connects to parent input', async function() {
    const addBlock = await act(() => Blockly.serialization.blocks.append({
      type: 'math_add',
      inputs: {
        NUM0: {
          block: { type: 'math_number', fields: { NUM: 1 } }
        }
      }
    }, workspace));
    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number');
    await act(() => Blockly.common.setSelected(numberBlock));
    await act(() => input('42'));

    const newNumberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number' && b.getFieldValue('NUM') === '42');
    chai.assert.equal(newNumberBlock.parentBlock_, addBlock);
    const secondInput = addBlock.getInput('NUM1');
    chai.assert.equal(secondInput.connection.targetConnection, newNumberBlock.outputConnection);
    chai.assert.equal(Blockly.common.getSelected(), newNumberBlock);
  });

  test('block remains standalone when no compatible connection exists', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace));
    const setBlock = await act(() => Blockly.serialization.blocks.append({
      type: 'lexical_variable_set',
      fields: { VAR: 'global myVar' },
      inputs: {
        VALUE: {
          block: { type: 'math_number', fields: { NUM: 42 } }
        }
      }
    }, workspace));
    await act(() => Blockly.common.setSelected(setBlock));
    await act(() => input('7'));

    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number' && b.getFieldValue('NUM') === '7');
    chai.assert.isNull(numberBlock.parentBlock_);
    chai.assert.equal(Blockly.common.getSelected(), numberBlock);
  });

  test('undo create number block', async function() {
    await act(() => input('42'));

    await act(() => ctrl('z'));
    chai.assert.equal(workspace.getAllBlocks().length, 0);
  });

  test('undo create block with value connection', async function() {
    const ifBlock = await act(() => Blockly.serialization.blocks.append({
      type: 'controls_if'
    }, workspace));
    await act(() => Blockly.common.setSelected(ifBlock));
    await act(() => input('true'));

    await act(() => ctrl('z'));
    chai.assert.isNull(ifBlock.getInput('IF0').connection.targetBlock());
  });

  test('undo create block with statement connection', async function() {
    await act(() => Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace));
    const setBlock = await act(() => Blockly.serialization.blocks.append({
      type: 'lexical_variable_set',
      fields: { VAR: 'global myVar' }
    }, workspace));
    await act(() => Blockly.common.setSelected(setBlock));
    await act(() => input('if'));

    await act(() => ctrl('z'));
    chai.assert.isNull(setBlock.nextConnection.targetBlock());
  });
});
