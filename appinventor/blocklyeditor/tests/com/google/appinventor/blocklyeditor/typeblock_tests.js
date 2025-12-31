suite('TypeBlock', function() {
  let workspace;

  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
    Blockly.common.setSelected(null);
  });

  function input(text) {
    workspace.typeBlock_.controller.show();
    const input = Blockly.WidgetDiv.getDiv().querySelector('.fi-input');
    input.value = text;
    input.dispatchEvent(new InputEvent('input'));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
  }

  test('create number block', function() {
    input('42');

    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number');
    chai.assert.equal(numberBlock.getFieldValue('NUM'), '42');
  });

  test('create text block', function() {
    input('"hello"');

    const textBlock = workspace.getAllBlocks().find(b => b.type === 'text');
    chai.assert.equal(textBlock.getFieldValue('TEXT'), 'hello');
  });

  test('create global variable get block', function() {
    Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace);
    input('get global myVar');

    const variableGetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_get');
    chai.assert.equal(variableGetBlock.getFieldValue('VAR'), 'global myVar');
  });

  test('create global variable set block', function() {
    Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace);
    input('set global myVar');

    const variableSetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_set');
    chai.assert.equal(variableSetBlock.getFieldValue('VAR'), 'global myVar');
  });

  test('create local variable get block', function() {
    const initializeVariable = Blockly.serialization.blocks.append({
      type: 'local_declaration_statement',
      fields: { VAR0: 'myVar' }
    }, workspace);
    Blockly.common.setSelected(initializeVariable);
    input('get myVar');

    const variableGetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_get');
    chai.assert.equal(variableGetBlock.getFieldValue('VAR'), 'myVar');
  });

  test('create local variable set block', function() {
    const initializeVariable = Blockly.serialization.blocks.append({
      type: 'local_declaration_statement',
      fields: { VAR0: 'myVar' }
    }, workspace);
    Blockly.common.setSelected(initializeVariable);
    input('set myVar');

    const variableSetBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_set');
    chai.assert.equal(variableSetBlock.getFieldValue('VAR'), 'myVar');
  });

  test('create procedure call no return block', function() {
    Blockly.serialization.blocks.append({
      type: 'procedures_defnoreturn',
      fields: { NAME: 'myProcedure' }
    }, workspace);
    input('call myProcedure');

    const procedureCallBlock = workspace.getAllBlocks().find(b => b.type === 'procedures_callnoreturn');
    chai.assert.equal(procedureCallBlock.getFieldValue('PROCNAME'), 'myProcedure');
  });

  test('create procedure call return block', function() {
    Blockly.serialization.blocks.append({
      type: 'procedures_defreturn',
      fields: { NAME: 'myProcedure' }
    }, workspace);
    input('call myProcedure');

    const procedureCallBlock = workspace.getAllBlocks().find(b => b.type === 'procedures_callreturn');
    chai.assert.equal(procedureCallBlock.getFieldValue('PROCNAME'), 'myProcedure');
  });

  test('create split at first block', function() {
    input('split at first');

    const splitBlock = workspace.getAllBlocks().find(b => b.type === 'text_split');
    chai.assert.equal(splitBlock.getFieldValue('OP'), 'SPLITATFIRST');
    chai.assert.isNotNull(splitBlock.getInput('AT'));
    chai.assert.include(splitBlock.getInput('AT').connection.getCheck(), 'String');
  });

  test('create split at first of any block', function() {
    input('split at first of any');

    const splitBlock = workspace.getAllBlocks().find(b => b.type === 'text_split');
    chai.assert.equal(splitBlock.getFieldValue('OP'), 'SPLITATFIRSTOFANY');
    chai.assert.isNotNull(splitBlock.getInput('AT'));
    chai.assert.include(splitBlock.getInput('AT').connection.getCheck(), 'Array');
  });

  test('create button click block', function() {
    initComponentTypes();
    workspace.addComponent('-1048419043', 'Button1', 'Button');
    input('when Button1.Click');

    const eventBlock = workspace.getAllBlocks().find(b => b.type === 'component_event');
    chai.assert.equal(eventBlock.typeName, 'Button');
    chai.assert.equal(eventBlock.eventName, 'Click');
    chai.assert.equal(eventBlock.instanceName, 'Button1');
  });

  test('value block connects to first compatible empty input', function() {
    const ifBlock = Blockly.serialization.blocks.append({
      type: 'controls_if'
    }, workspace);
    Blockly.common.setSelected(ifBlock);
    input('true');

    const booleanBlock = workspace.getAllBlocks().find(b => b.type === 'logic_boolean');
    chai.assert.equal(booleanBlock.parentBlock_, ifBlock);
    const conditionInput = ifBlock.getInput('IF0');
    chai.assert.equal(conditionInput.connection.targetConnection, booleanBlock.outputConnection);
  });

  test('statement block connects to first empty statement input', function() {
    const ifBlock = Blockly.serialization.blocks.append({
      type: 'controls_if',
      inputs: {
        IF0: {
          block: { type: 'logic_boolean', fields: { BOOL: 'TRUE' } }
        }
      }
    }, workspace);
    Blockly.common.setSelected(ifBlock);
    Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace);
    input('set global myVar');

    const setBlock = workspace.getAllBlocks().find(b => b.type === 'lexical_variable_set');
    chai.assert.equal(setBlock.parentBlock_, ifBlock);
    const doInput = ifBlock.getInput('DO0');
    chai.assert.equal(doInput.connection.targetConnection, setBlock.previousConnection);
  });

  test('statement block connects below selected statement block', function() {
    Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace);
    const setBlock = Blockly.serialization.blocks.append({
      type: 'lexical_variable_set',
      fields: { VAR: 'global myVar' },
      inputs: {
        VALUE: {
          block: { type: 'math_number', fields: { NUM: 42 } }
        }
      }
    }, workspace);
    Blockly.common.setSelected(setBlock);
    input('if');

    const ifBlock = workspace.getAllBlocks().find(b => b.type === 'controls_if');
    chai.assert.equal(ifBlock.previousConnection.targetConnection, setBlock.nextConnection);
  });

  test('statement block connects below parent statement', function() {
    const parentIf = Blockly.serialization.blocks.append({
      type: 'controls_if',
      inputs: {
        IF0: {
          block: { type: 'logic_boolean', fields: { BOOL: 'TRUE' } }
        }
      }
    }, workspace);
    const booleanBlock = workspace.getAllBlocks().find(b => b.type === 'logic_boolean');
    Blockly.common.setSelected(booleanBlock);
    input('if');

    const newIfBlock = workspace.getAllBlocks().find(b => b.type === 'controls_if' && b !== parentIf);
    chai.assert.equal(newIfBlock.previousConnection.targetConnection, parentIf.nextConnection);
  });

  test('value block remains standalone when parent is statement', function() {
    Blockly.serialization.blocks.append({
      type: 'controls_if',
      inputs: {
        IF0: {
          block: { type: 'logic_boolean', fields: { BOOL: 'TRUE' } }
        }
      }
    }, workspace);
    const booleanBlock = workspace.getAllBlocks().find(b => b.type === 'logic_boolean');
    Blockly.common.setSelected(booleanBlock);
    input('42');

    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number');
    chai.assert.isNull(numberBlock.parentBlock_);
  });

  test('value block recursively connects to parent input', function() {
    const addBlock = Blockly.serialization.blocks.append({
      type: 'math_add',
      inputs: {
        NUM0: {
          block: { type: 'math_number', fields: { NUM: 1 } }
        }
      }
    }, workspace);
    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number');
    Blockly.common.setSelected(numberBlock);
    input('42');

    const newNumberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number' && b.getFieldValue('NUM') === '42');
    chai.assert.equal(newNumberBlock.parentBlock_, addBlock);
    const secondInput = addBlock.getInput('NUM1');
    chai.assert.equal(secondInput.connection.targetConnection, newNumberBlock.outputConnection);
  });

  test('block remains standalone when no compatible connection exists', function() {
    Blockly.serialization.blocks.append({
      type: 'global_declaration',
      fields: { NAME: 'myVar' }
    }, workspace);
    const setBlock = Blockly.serialization.blocks.append({
      type: 'lexical_variable_set',
      fields: { VAR: 'global myVar' },
      inputs: {
        VALUE: {
          block: { type: 'math_number', fields: { NUM: 42 } }
        }
      }
    }, workspace);
    Blockly.common.setSelected(setBlock);
    input('7');

    const numberBlock = workspace.getAllBlocks().find(b => b.type === 'math_number' && b.getFieldValue('NUM') === '7');
    chai.assert.isNull(numberBlock.parentBlock_);
  });
});
