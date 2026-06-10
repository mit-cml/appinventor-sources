// Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Tests for backpack collapsed-state preservation (issue #3396).
 *
 * Verifies that when a collapsed block is added to the backpack via
 * addToBackpack(), the resulting XML contains collapsed="true" so that
 * the block is correctly restored as collapsed when dragged back out.
 */

suite('Backpack', function() {
  setup(function() {
    this.workspace = Blockly.inject('blocklyDiv', {});
    Blockly.defineBlocksWithJsonArray([
      {
        'type': 'test_block',
        'message0': 'test %1',
        'args0': [
          {
            'type': 'input_value',
            'name': 'INPUT'
          }
        ],
        'previousStatement': null,
        'nextStatement': null
      }
    ]);

    // Helper: create a rendered block on the workspace.
    this.createBlock = function(type) {
      var block = this.workspace.newBlock(type);
      block.initSvg();
      block.render();
      return block;
    };

    /**
     * Minimal stub for AI.Blockly.Backpack that lets us test addToBackpack()
     * without needing the full backpack UI (flyout, SVG, server calls, etc.).
     * Only the logic under test (XML generation + collapsed attribute) is live.
     */
    this.makeStubBackpack = function(workspace) {
      var backpack = Object.create(AI.Blockly.Backpack.prototype);
      backpack.workspace_ = workspace;
      backpack.capturedXml_ = null;   // set by addToBackpack during the test

      // Override getContents() so it resolves immediately with an empty array.
      backpack.getContents = function() {
        return Promise.resolve([]);
      };

      // Override setContents() to capture what would be stored.
      backpack.setContents = function(contents /*, store */) {
        this.capturedXml_ = contents[0] || null;
      };

      // Stub out UI side-effects that require a full DOM.
      backpack.grow = function() {};
      backpack.flyout_ = { isVisible: function() { return false; } };

      return backpack;
    };
  });

  teardown(function() {
    this.workspace.dispose();
    Blockly.mainWorkspace = null;
    delete Blockly.Blocks['test_block'];
  });

  suite('addToBackpack XML', function() {

    test('Collapsed block: XML must contain collapsed="true"', function(done) {
      var block = this.createBlock('test_block');
      block.setCollapsed(true);
      chai.assert.isTrue(block.isCollapsed(),
          'Pre-condition: block should be collapsed before adding to backpack');

      var backpack = this.makeStubBackpack(this.workspace);

      // addToBackpack is async (Promise-based), so we use done().
      backpack.addToBackpack(block, false);

      // Wait one microtask tick for the Promise to resolve.
      setTimeout(function() {
        chai.assert.isNotNull(backpack.capturedXml_,
            'addToBackpack should have stored XML');
        chai.assert.include(backpack.capturedXml_, 'collapsed="true"',
            'Stored XML must contain collapsed="true" for a collapsed block');
        done();
      }, 50);
    });

    test('Expanded block: XML must NOT contain collapsed="true"', function(done) {
      var block = this.createBlock('test_block');
      // Block is expanded by default — do NOT collapse it.
      chai.assert.isFalse(block.isCollapsed(),
          'Pre-condition: block should be expanded before adding to backpack');

      var backpack = this.makeStubBackpack(this.workspace);
      backpack.addToBackpack(block, false);

      setTimeout(function() {
        chai.assert.isNotNull(backpack.capturedXml_,
            'addToBackpack should have stored XML');
        chai.assert.notInclude(backpack.capturedXml_, 'collapsed="true"',
            'Stored XML must NOT contain collapsed="true" for an expanded block');
        done();
      }, 50);
    });

    test('Collapsed block: original block stays collapsed after addToBackpack', function(done) {
      var block = this.createBlock('test_block');
      block.setCollapsed(true);

      var backpack = this.makeStubBackpack(this.workspace);
      backpack.addToBackpack(block, false);

      setTimeout(function() {
        chai.assert.isTrue(block.isCollapsed(),
            'The original block must remain collapsed after being added to backpack');
        done();
      }, 50);
    });

    test('Collapsed block: domToBlock restores collapsed state from stored XML', function(done) {
      var block = this.createBlock('test_block');
      block.setCollapsed(true);

      var backpack = this.makeStubBackpack(this.workspace);
      backpack.addToBackpack(block, false);

      var self = this;
      setTimeout(function() {
        // Parse the stored XML string back and instantiate the block.
        var xmlDom = Blockly.utils.xml.textToDom(backpack.capturedXml_);
        var blockXml = xmlDom.firstElementChild;

        // domToBlock is the same function used when dragging out of backpack.
        var restoredBlock = Blockly.Xml.domToBlock(blockXml, self.workspace);

        chai.assert.isTrue(restoredBlock.isCollapsed(),
            'Block restored from backpack XML must be collapsed');

        restoredBlock.dispose();
        done();
      }, 50);
    });
  });
});
