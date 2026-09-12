suite('ExportBlocksImage', function() {
  let workspace;

  setup(async function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
    workspace = Blockly.common.getMainWorkspace();
    await act(() => Blockly.common.setSelected(null));
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

  suite('comment bubbles', function() {
    let block;

    setup(async function() {
      block = Blockly.serialization.blocks.append({
        type: 'math_number', fields: { NUM: 42 }, x: 40, y: 40
      }, workspace);
      block.setCommentText('first line\nsecond line');
      await act(() => block.getIcon('comment').setBubbleVisible(true));
    });

    function liveBubble() {
      return workspace.getBubbleCanvas().querySelector('.blocklyTextInputBubble');
    }

    function decode(uri) {
      return decodeURIComponent(escape(window.atob(uri.split(',')[1])));
    }

    test('the export group includes the comment bubble', function() {
      chai.assert.isNotNull(liveBubble(), 'the bubble is shown on the workspace');
      const exported = workspaceExportGroup(workspace);
      chai.assert.isNotNull(exported.group.querySelector('.blocklyTextInputBubble'));
      chai.assert.isNotNull(exported.group.querySelector('.blocklyBubbleTail'));
      chai.assert.isNotNull(exported.group.querySelector('.blocklyBlockCanvas'));
    });

    test('comment text becomes SVG text, one tspan per line', function() {
      const exported = workspaceExportGroup(workspace);
      chai.assert.lengthOf(exported.group.querySelectorAll('foreignObject'), 0);
      const text = exported.group.querySelector('.blocklyTextInputBubble text');
      chai.assert.isNotNull(text);
      const lines = Array.from(text.querySelectorAll('tspan')).map(t => t.textContent);
      chai.assert.deepEqual(lines, ['first line', 'second line']);
      // The textarea's background is drawn as a rect behind the text, with an
      // inline fill (the theme colour is a CSS variable the export can't see).
      const background = text.previousElementSibling;
      chai.assert.equal(background.tagName.toLowerCase(), 'rect');
      chai.assert.match(background.getAttribute('style'), /fill: #[0-9a-fA-F]{6}/);
    });

    test('long comment lines wrap to the bubble width', function() {
      block.setCommentText('word '.repeat(40).trim());
      const exported = workspaceExportGroup(workspace);
      const tspans = exported.group.querySelectorAll('.blocklyTextInputBubble tspan');
      chai.assert.isAbove(tspans.length, 1, 'the text was wrapped');
      const fo = liveBubble().querySelector('foreignObject');
      const width = parseFloat(fo.getAttribute('width'));
      for (const tspan of tspans) {
        chai.assert.isAtMost(tspan.getComputedTextLength(), width);
      }
    });

    test('the export bounds cover the bubble and its tail', function() {
      const exported = workspaceExportGroup(workspace);
      const bubble = liveBubble();
      const xy = Blockly.utils.svgMath.getRelativeXY(bubble);
      const box = bubble.getBBox();
      const blocks = workspace.getCanvas().getBBox();
      chai.assert.isAtMost(exported.bbox.x, Math.min(blocks.x, xy.x + box.x));
      chai.assert.isAtMost(exported.bbox.y, Math.min(blocks.y, xy.y + box.y));
      chai.assert.isAtLeast(exported.bbox.x + exported.bbox.width,
          Math.max(blocks.x + blocks.width, xy.x + box.x + box.width));
      chai.assert.isAtLeast(exported.bbox.y + exported.bbox.height,
          Math.max(blocks.y + blocks.height, xy.y + box.y + box.height));
    });

    test('other bubbles on the bubble canvas are left out', function() {
      const stray = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      stray.setAttribute('class', 'blocklyBubble stray-mutator');
      workspace.getBubbleCanvas().appendChild(stray);
      try {
        const exported = workspaceExportGroup(workspace);
        chai.assert.isNull(exported.group.querySelector('.stray-mutator'));
        chai.assert.isNotNull(exported.group.querySelector('.blocklyTextInputBubble'));
      } finally {
        stray.remove();
      }
    });

    test('without comments the bounds are the block canvas bounds', async function() {
      await act(() => block.setCommentText(null));
      const exported = workspaceExportGroup(workspace);
      chai.assert.isNull(exported.group.querySelector('.blocklyTextInputBubble'));
      const blocks = workspace.getCanvas().getBBox();
      chai.assert.closeTo(exported.bbox.x, blocks.x, 0.01);
      chai.assert.closeTo(exported.bbox.y, blocks.y, 0.01);
      chai.assert.closeTo(exported.bbox.width, blocks.width, 0.01);
      chai.assert.closeTo(exported.bbox.height, blocks.height, 0.01);
    });

    test('the SVG data URI has the comment, no foreignObject and no dangling filter', function(done) {
      const exported = workspaceExportGroup(workspace);
      svgAsDataUri(exported.group, workspace.getMetrics(), { bbox: exported.bbox }, uri => {
        const svg = decode(uri);
        chai.assert.include(svg, 'second line');
        chai.assert.notInclude(svg, '<foreignObject');
        chai.assert.notInclude(svg, 'filter=');
        chai.assert.include(svg, 'blocklyTextInputBubble');
        done();
      });
    });

    test('a single block export includes the comment bubbles of the block and its children', function() {
      const outer = Blockly.serialization.blocks.append({
        type: 'math_add', x: 200, y: 200,
        inputs: { NUM0: { block: { type: 'math_number', fields: { NUM: 1 } } } }
      }, workspace);
      const inner = outer.getInputTargetBlock('NUM0');
      inner.setCommentText('child comment');
      return act(() => inner.getIcon('comment').setBubbleVisible(true)).then(() => {
        const exported = blockExportGroup(outer);
        const bubbles = exported.group.querySelectorAll('.blocklyTextInputBubble');
        chai.assert.lengthOf(bubbles, 1, "only the exported block's own bubbles, not the other block's");
        chai.assert.include(bubbles[0].textContent, 'child comment');
        chai.assert.lengthOf(exported.group.querySelectorAll('foreignObject'), 0);
        const root = inner.getIcon('comment').textInputBubble.getSvgRoot();
        const xy = Blockly.utils.svgMath.getRelativeXY(root);
        const box = root.getBBox();
        chai.assert.isAtMost(exported.bbox.x, xy.x + box.x);
        chai.assert.isAtLeast(exported.bbox.x + exported.bbox.width, xy.x + box.x + box.width);
        chai.assert.isAtLeast(exported.bbox.y + exported.bbox.height, xy.y + box.y + box.height);
      });
    });

    test('a single block export without comments keeps the block bounds', function() {
      const lone = Blockly.serialization.blocks.append({ type: 'math_number', x: 300, y: 300 }, workspace);
      const exported = blockExportGroup(lone);
      chai.assert.isNull(exported.group.querySelector('.blocklyTextInputBubble'));
      const box = lone.getSvgRoot().getBBox();
      const xy = lone.getRelativeToSurfaceXY();
      chai.assert.closeTo(exported.bbox.x, xy.x + box.x, 0.01);
      chai.assert.closeTo(exported.bbox.y, xy.y + box.y, 0.01);
      chai.assert.closeTo(exported.bbox.width, box.width, 0.01);
      chai.assert.closeTo(exported.bbox.height, box.height, 0.01);
    });

    test('the exported SVG rasterizes to an image', function(done) {
      // The reason foreignObject is replaced: an SVG containing one may not
      // draw into an <img>/<canvas>, which is how the PNG is produced.
      const exported = workspaceExportGroup(workspace);
      svgAsDataUri(exported.group, workspace.getMetrics(), { bbox: exported.bbox }, uri => {
        const image = new Image();
        image.onload = () => {
          chai.assert.isAbove(image.width, 0);
          chai.assert.isAbove(image.height, 0);
          done();
        };
        image.onerror = () => done(new Error('the exported SVG did not load as an image'));
        image.src = uri;
      });
    });
  });
});
