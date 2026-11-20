// Copyright © 2026 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Behavior tests for the radix number block.
 */

suite('Radix number block', function() {
  let workspace;

  setup(function() {
    workspace = Blockly.inject('blocklyDiv', {});
    // Set by AI.Blockly.BlocklyEditor.create in the editor; the dropdown uses it.
    AI.Blockly.multiselect = AI.Blockly.multiselect || Multiselect;
  });

  teardown(function() {
    workspace.dispose();
  });

  function radixBlock(base, text) {
    const block = workspace.newBlock('math_number_radix');
    block.setFieldValue(base, 'OP');
    block.setFieldValue(text, 'NUM');
    return block;
  }

  function yail(block) {
    return AI.Yail.forBlock['math_number_radix'](block, AI.Yail)[0];
  }

  test('Converts a whole number to the new base', function() {
    const block = radixBlock('DEC', '10');
    block.setFieldValue('BIN', 'OP');
    chai.assert.equal(block.getFieldValue('OP'), 'BIN');
    chai.assert.equal(block.getFieldValue('NUM'), '1010');
    chai.assert.equal(yail(block), 10);
    block.setFieldValue('HEX', 'OP');
    chai.assert.equal(block.getFieldValue('NUM'), 'a');
    chai.assert.equal(yail(block), 10);
  });

  // Binary, octal and hexadecimal fields accept only digits, so a negative
  // number or a fraction cannot be shown in them.
  ['-5', '1.5', '-0.25'].forEach(function(text) {
    ['BIN', 'OCT', 'HEX'].forEach(function(base) {
      test('Keeps decimal ' + text + ' when switching to ' + base, function() {
        const block = radixBlock('DEC', text);
        block.setFieldValue(base, 'OP');
        chai.assert.equal(block.getFieldValue('OP'), 'DEC');
        chai.assert.equal(block.getFieldValue('NUM'), text);
        chai.assert.equal(yail(block), Number(text));
        // The number field still accepts decimal input afterwards.
        block.setFieldValue('2.5', 'NUM');
        chai.assert.equal(block.getFieldValue('NUM'), '2.5');
      });
    });
  });

  test('Switches back to decimal from any base', function() {
    const block = radixBlock('HEX', 'ff');
    block.setFieldValue('DEC', 'OP');
    chai.assert.equal(block.getFieldValue('OP'), 'DEC');
    chai.assert.equal(block.getFieldValue('NUM'), '255');
    chai.assert.equal(yail(block), 255);
  });
});
