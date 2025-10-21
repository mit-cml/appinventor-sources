suite('ExportBlocksImage', function() {
  let workspace;

  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
  });

  test('export and import blocks png round-trip', async function() {
    const block = Blockly.serialization.blocks.append({
      type: 'math_add',
      inputs: {
        NUM0: { block: { type: 'math_number', fields: { NUM: 5 } } },
        NUM1: { block: { type: 'math_number', fields: { NUM: 10 } } }
      }
    }, workspace);
    const exportPromise = new Promise(resolve => {
      const original = URL.createObjectURL;
      URL.createObjectURL = png => {
        URL.createObjectURL = original;
        resolve(png);
        return original(png);
      };
    });
    Blockly.exportBlockAsPng(block);
    const png = await exportPromise;
    workspace.clear();
    const importPromise = new Promise(resolve => {
      const listener = (event) => {
        if (event.type === Blockly.Events.BLOCK_CREATE && workspace.getAllBlocks().length === 3) {
          workspace.removeChangeListener(listener);
          resolve();
        }
      };
      workspace.addChangeListener(listener);
    });
    Blockly.importPngAsBlock(workspace, { x: 0, y: 0 }, png);
    await importPromise;

    const blocks = workspace.getAllBlocks();
    const add = blocks.find(b => b.type === 'math_add');
    chai.assert.equal(blocks.length, 3);
    chai.assert.equal(add.getInputTargetBlock('NUM0').getFieldValue('NUM'), '5');
    chai.assert.equal(add.getInputTargetBlock('NUM1').getFieldValue('NUM'), '10');
  });
});
