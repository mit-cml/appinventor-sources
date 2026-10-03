suite('ExportBlocksImage', function() {
  let workspace;

  setup(async function() {
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
      const originalCreateObjectURL = URL.createObjectURL;
      const originalClick = HTMLAnchorElement.prototype.click;
      URL.createObjectURL = png => {
        URL.createObjectURL = originalCreateObjectURL;
        resolve(png);
        return originalCreateObjectURL(png);
      };
      HTMLAnchorElement.prototype.click = function() {
        HTMLAnchorElement.prototype.click = originalClick;
      };
    });
    Blockly.exportBlockAsPng(block);
    const png = await exportPromise;
    workspace.clear();
    await act(() => Blockly.importPngAsBlock(workspace, { x: 0, y: 0 }, png));

    const blocks = workspace.getAllBlocks();
    const add = blocks.find(b => b.type === 'math_add');
    chai.assert.equal(blocks.length, 3);
    chai.assert.equal(add.getInputTargetBlock('NUM0').getFieldValue('NUM'), '5');
    chai.assert.equal(add.getInputTargetBlock('NUM1').getFieldValue('NUM'), '10');
  });
});
