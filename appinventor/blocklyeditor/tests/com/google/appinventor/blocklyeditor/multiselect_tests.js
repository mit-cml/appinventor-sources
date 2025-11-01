suite('Multiselect', function() {
  let workspace;

  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
    Blockly.common.setSelected(null);
  });

  function shift(type) {
    workspace.getCanvas().dispatchEvent(new KeyboardEvent(type, { key: 'shift', bubbles: true }));
  }

  function ctrl(key) {
    const keyCode = key.toUpperCase().charCodeAt(0);
    const options = { key: key, keyCode: keyCode, ctrlKey: true, bubbles: true };
    workspace.getCanvas().dispatchEvent(new KeyboardEvent('keydown', options));
    workspace.getCanvas().dispatchEvent(new KeyboardEvent('keyup', options));
  }

  function del() {
    workspace.getCanvas().dispatchEvent(new KeyboardEvent('keydown', { key: 'Delete', keyCode: 46, bubbles: true }));
    workspace.getCanvas().dispatchEvent(new KeyboardEvent('keyup', { key: 'Delete', keyCode: 46, bubbles: true }));
  }

  function select(block) {
    const blockPath = block.getSvgRoot().querySelector('path.blocklyPath');
    const blockRect = blockPath.getBoundingClientRect();
    const blockCenter = {
      clientX: blockRect.left + blockRect.width / 2,
      clientY: blockRect.top + blockRect.height / 2
    };

    blockPath.dispatchEvent(new PointerEvent('pointerdown', { ...blockCenter, bubbles: true }));
    blockPath.dispatchEvent(new PointerEvent('pointerup', { ...blockCenter, bubbles: true }));
  }

  function isSelected(block) {
    return block.getSvgRoot().classList.contains('blocklySelected');
  }

  async function events(type, count) {
    return new Promise(resolve => {
      let received = 0;
      const listener = (event) => {
        if (event.type === type) {
          received++;
          if (received === count) {
            workspace.removeChangeListener(listener);
            resolve();
          }
        }
      };
      workspace.addChangeListener(listener);
    });
  }

  test('select blocks', async function() {
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
    await events(Blockly.Events.SELECTED, 1);
    select(block2);
    await events(Blockly.Events.SELECTED, 1);
    shift('keyup');

    chai.assert.isTrue(isSelected(block1));
    chai.assert.isTrue(isSelected(block2));
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
    await events(Blockly.Events.SELECTED, 1);
    select(block2);
    await events(Blockly.Events.SELECTED, 1);
    shift('keyup');
    ctrl('c');

    chai.assert.equal(workspace.getAllBlocks().length, 2);

    ctrl('v');

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
    await events(Blockly.Events.SELECTED, 1);
    select(block2);
    await events(Blockly.Events.SELECTED, 1);
    shift('keyup');
    ctrl('x');

    chai.assert.equal(workspace.getAllBlocks().length, 0);

    ctrl('v');

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
    await events(Blockly.Events.SELECTED, 1);
    select(block2);
    await events(Blockly.Events.SELECTED, 1);
    shift('keyup');
    del();

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
    await events(Blockly.Events.SELECTED, 1);
    select(block2);
    await events(Blockly.Events.SELECTED, 1);
    shift('keyup');
    del();

    chai.assert.equal(workspace.getAllBlocks().length, 0);

    await events(Blockly.Events.BLOCK_DELETE, 2);
    ctrl('z');

    chai.assert.equal(workspace.getAllBlocks().length, 2);

    await events(Blockly.Events.BLOCK_CREATE, 2);
    ctrl('y');

    chai.assert.equal(workspace.getAllBlocks().length, 0);
  });
});
