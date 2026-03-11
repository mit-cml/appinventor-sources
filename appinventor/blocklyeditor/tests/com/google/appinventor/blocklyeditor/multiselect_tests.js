suite('Multiselect', function() {
  let workspace;

  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
    Blockly.common.setSelected(null);
  });

  test('copy/paste blocks', async function() {
    const block1 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 1 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);
    const block2 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 2 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);

    shift('keydown');
    select(block1);
    select(block2);
    shift('keyup');
    await until(Blockly.Events.SELECTED, 2);
    ctrl('c');
    ctrl('v');
    await until(Blockly.Events.BLOCK_CREATE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 4);
  });

  test('cut/paste blocks', async function() {
    const block1 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 1 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);
    const block2 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 2 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);

    shift('keydown');
    select(block1);
    select(block2);
    shift('keyup');
    await until(Blockly.Events.SELECTED, 2);
    ctrl('x');
    await until(Blockly.Events.BLOCK_DELETE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 0);
    ctrl('v');
    await until(Blockly.Events.BLOCK_CREATE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 2);
  });

  test('delete blocks', async function() {
    const block1 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 1 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);
    const block2 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 2 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);

    shift('keydown');
    select(block1);
    select(block2);
    shift('keyup');
    await until(Blockly.Events.SELECTED, 2);
    del();
    await until(Blockly.Events.BLOCK_DELETE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 0);
  });

  test('undo/redo blocks', async function() {
    const block1 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 1 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);
    const block2 = Blockly.clipboard.paste({
      paster: 'block',
      blockState: { type: 'math_number', fields: { NUM: 2 } },
      typeCounts: { 'math_number': 1 }
    }, workspace);

    shift('keydown');
    select(block1);
    select(block2);
    shift('keyup');
    await until(Blockly.Events.SELECTED, 2);
    del();
    await until(Blockly.Events.BLOCK_DELETE, 2);
    ctrl('z');
    await until(Blockly.Events.BLOCK_CREATE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 2);
    ctrl('y');
    await until(Blockly.Events.BLOCK_DELETE, 2);
    chai.assert.equal(workspace.getAllBlocks().length, 0);
  });
});
