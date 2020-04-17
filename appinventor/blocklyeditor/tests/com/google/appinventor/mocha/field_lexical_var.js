// Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Field lexical variable behavior tests.
 */

suite ('FieldLexical', function() {
  setup(function() {
    this.workspace = Blockly.inject('blocklyDiv', {});

    this.createBlock = function (type) {
      var block = this.workspace.newBlock(type);
      block.initSvg();
      block.render();
      return block;
    }
  });
  teardown(function() {
    delete this.createBlock;
    this.workspace.dispose();
    delete this.workspace;
  })

  suite('getGlobalNames', function() {
    test('Simple', function() {
      // Uses XML so that names don't overlap.
      var xml = Blockly.Xml.textToDom('<xml>' +
      '  <block type="global_declaration">' +
      '    <field name="NAME">global</field>' +
      '  </block>' +
      '  <block type="global_declaration">' +
      '    <field name="NAME">global</field>' +
      '  </block>' +
      '  <block type="global_declaration">' +
      '    <field name="NAME">global</field>' +
      '  </block>' +
      '</xml>');
      Blockly.Xml.domToWorkspace(xml, this.workspace);
      var vars = Blockly.FieldLexicalVariable.getGlobalNames();
      chai.assert.sameOrderedMembers(vars, ['global', 'global2', 'global3']);
    });
    test('Top-Level Local', function() {
      var xml = Blockly.Xml.textToDom('<xml>' +
      '  <block type="global_declaration">' +
      '    <field name="NAME">global</field>' +
      '  </block>' +
      '  <block type="global_declaration">' +
      '    <field name="NAME">global</field>' +
      '  </block>' +
      '  <block type="global_declaration">' +
      '    <field name="NAME">global</field>' +
      '  </block>' +
      '  <block type="local_declaration_statement">' +
      '    <field name="VAR0">local</field>' +
      '  </block>' +
      '</xml>');
      Blockly.Xml.domToWorkspace(xml, this.workspace);
      var vars = Blockly.FieldLexicalVariable.getGlobalNames();
      chai.assert.sameOrderedMembers(vars, ['global', 'global2', 'global3']);
    });
  });
})