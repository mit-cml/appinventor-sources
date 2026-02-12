suite('Multiselect', function() {
  let workspace;

  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
    Blockly.common.setSelected(null);
  });

  test('copy/paste blocks', async function() {
    const block1 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 1 }
    }, workspace);
    const block2 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 2 }
    }, workspace);
    workspace.cleanUp();

    shift('keydown');
    select(block1);
    select(block2);
    await until(Blockly.Events.SELECTED, 3);
    shift('keyup');
    ctrl('c');
    ctrl('v');
    await until(Blockly.Events.BLOCK_CREATE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 4);
  });

  test('cut/paste blocks', async function() {
    const block1 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 1 }
    }, workspace);
    const block2 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 2 }
    }, workspace);
    workspace.cleanUp();

    shift('keydown');
    select(block1);
    select(block2);
    await until(Blockly.Events.SELECTED, 3);
    shift('keyup');
    ctrl('x');
    await until(Blockly.Events.BLOCK_DELETE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 0);
    ctrl('v');
    await until(Blockly.Events.BLOCK_CREATE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 2);
  });

  test('delete blocks', async function() {
    const block1 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 1 }
    }, workspace);
    const block2 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 2 }
    }, workspace);
    workspace.cleanUp();

    shift('keydown');
    select(block1);
    select(block2);
    await until(Blockly.Events.SELECTED, 3);
    shift('keyup');
    del();
    await until(Blockly.Events.BLOCK_DELETE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 0);
  });

  test('undo/redo blocks', async function() {
    const block1 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 1 }
    }, workspace);
    const block2 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 2 }
    }, workspace);
    workspace.cleanUp();

    shift('keydown');
    select(block1);
    select(block2);
    await until(Blockly.Events.SELECTED, 3);
    shift('keyup');
    del();
    await until(Blockly.Events.BLOCK_DELETE, 2);
    ctrl('z');
    await until(Blockly.Events.BLOCK_CREATE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 2);
    ctrl('y');
    await until(Blockly.Events.BLOCK_DELETE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 0);
  });

  test('select unselected block', async function() {
    const selectedBlock1 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 1 }
    }, workspace);
    const selectedBlock2 = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 2 }
    }, workspace);
    const unselectedBlock = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 3 }
    }, workspace);
    workspace.cleanUp();

    shift('keydown');
    select(selectedBlock1);
    select(selectedBlock2);
    await until(Blockly.Events.SELECTED, 3);
    shift('keyup');

    select(unselectedBlock);
    await until(Blockly.Events.SELECTED, 1);
    chai.assert.equal(Blockly.common.getSelected(), unselectedBlock);
  });

  test('select unselected child block', async function() {
    const selectedBlock = Blockly.serialization.blocks.append({
      type: 'math_compare',
      inputs: {
        A: { block: { type: 'math_number', fields: { NUM: 1 } } },
        B: { block: { type: 'math_number', fields: { NUM: 2 } } }
      }
    }, workspace);
    const unselectedChildBlock = selectedBlock.getInputTargetBlock('A');
    const otherSelectedBlock = Blockly.serialization.blocks.append({
      type: 'math_number', fields: { NUM: 3 }
    }, workspace);
    workspace.cleanUp();

    shift('keydown');
    select(selectedBlock);
    select(otherSelectedBlock);
    await until(Blockly.Events.SELECTED, 3);
    shift('keyup');

    select(unselectedChildBlock);
    await until(Blockly.Events.SELECTED, 1);
    chai.assert.equal(Blockly.common.getSelected(), unselectedChildBlock);
  });
});
