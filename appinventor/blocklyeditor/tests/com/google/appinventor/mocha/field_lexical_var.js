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
  suite('getLexicalNamesInScope', function() {
    setup(function() {
      this.assertLexicalNames = function(xml, expectVars) {
        Blockly.Xml.domToWorkspace(xml, this.workspace);
        var block = this.workspace.getBlockById('a');
        var actualVars = Blockly.FieldLexicalVariable
            .getLexicalNamesInScope(block);
        chai.assert.sameDeepOrderedMembers(actualVars, expectVars);
      }
    })
    suite('Nesting', function() {
      test('Simple Nesting', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_statement">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <statement name="STACK">' +
        '      <block type="local_declaration_statement">' +
        '        <mutation>' +
        '          <localname name="name2"></localname>' +
        '        </mutation>' +
        '        <field name="VAR0">name2</field>' +
        '        <statement name="STACK">' +
        '          <block type="controls_if" id="a"/>' +
        '        </statement>' +
        '      </block>' +
        '    </statement>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['name', 'name'], ['name2', 'name2']]);
      });
      test('Matching Nesting', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_statement">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <statement name="STACK">' +
        '      <block type="local_declaration_statement">' +
        '        <mutation>' +
        '          <localname name="name"></localname>' +
        '        </mutation>' +
        '        <field name="VAR0">name</field>' +
        '        <statement name="STACK">' +
        '          <block type="controls_if" id="a"/>' +
        '        </statement>' +
        '      </block>' +
        '    </statement>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['name', 'name']]);

      });
      test('Weird Nesting 1', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_statement">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <value name="DECL0">' +
        '      <block type="local_declaration_expression">' +
        '        <mutation>' +
        '          <localname name="name2"></localname>' +
        '        </mutation>' +
        '        <field name="VAR0">name2</field>' +
        '        <value name="RETURN">' +
        '          <block type="controls_choose" id="a"/>' +
        '        </value>' +
        '      </block>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['name2', 'name2']]);
      });
      test('Weird Nesting 2', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_statement">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <statement name="STACK">' +
        '      <block type="local_declaration_statement">' +
        '        <mutation>' +
        '          <localname name="name2"></localname>' +
        '        </mutation>' +
        '        <field name="VAR0">name2</field>' +
        '        <value name="DECL0">' +
        '          <block type="controls_choose" id="a"/>' +
        '        </value>' +
        '      </block>' +
        '    </statement>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['name', 'name']]);
      })
    });
    suite('Procedures', function() {
      test('Stack Procedure', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="procedures_defnoreturn">' +
        '    <mutation>' +
        '      <arg name="x"></arg>' + 
        '      <arg name="y"></arg>' + 
        '    </mutation>' +
        '    <field name="NAME">procedure</field>' +
        '    <field name="VAR0">x</field>' +
        '    <field name="VAR1">y</field>' +
        '    <statement name="STACK">' +
        '      <block type="controls_if" id="a"/>' +
        '    </statement>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['x', 'x'], ['y', 'y']]);
      });
      test('Input Procedure', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="procedures_defreturn">' +
        '    <mutation>' +
        '      <arg name="x"></arg>' + 
        '      <arg name="y"></arg>' + 
        '    </mutation>' +
        '    <field name="NAME">procedure</field>' +
        '    <field name="VAR0">x</field>' +
        '    <field name="VAR1">y</field>' +
        '    <value name="RETURN">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['x', 'x'], ['y', 'y']]);
      });
    })
    suite('For Range', function() {
      test('From Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_forRange">' +
        '    <field name="VAR">number</field>' +
        '    <value name="START">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, []);
      });
      test('To Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_forRange">' +
        '    <field name="VAR">number</field>' +
        '    <value name="END">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, []);
      });
      test('By Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_forRange">' +
        '    <field name="VAR">number</field>' +
        '    <value name="STEP">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, []);
      });
      test('Do Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_forRange">' +
        '    <field name="VAR">number</field>' +
        '    <statement name="DO">' +
        '      <block type="controls_if" id="a"/>' +
        '    </statement>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['number', 'number']]);
      });
    })
    suite('For Each', function() {
      test('List Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_forEach">' +
        '    <field name="VAR">item</field>' +
        '    <value name="LIST">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, []);
      });
      test('Do Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_forEach">' +
        '    <field name="VAR">item</field>' +
        '    <value name="DO">' +
        '      <block type="controls_if" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['item', 'item']]);
      });
    });
    suite('For Each Dict', function() {
      test('Item Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_for_each_dict">' +
        '    <field name="KEY">key</field>' +
        '    <field name="VALUE">value</field>' +
        '    <value name="DICT">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, []);
      });
      test('Do Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="controls_for_each_dict">' +
        '    <field name="KEY">key</field>' +
        '    <field name="VALUE">value</field>' +
        '    <statement name="DO">' +
        '      <block type="controls_if" id="a"/>' +
        '    </statement>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['key', 'key'], ['value', 'value']]);
      });
    });
    suite('Local Expression Declaration', function() {
      test('Var Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_expression">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <value name="DECL0">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, []);
      });
      test('Expression Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_expression">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <value name="RETURN">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['name', 'name']]);
      });
    });
    suite('Local Statement Declaration', function() {
      test('Var Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_statement">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <value name="DECL0">' +
        '      <block type="controls_choose" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, []);
      });
      test('Statement Input', function() {
        var xml = Blockly.Xml.textToDom('<xml>' +
        '  <block type="local_declaration_statement">' +
        '    <mutation>' +
        '      <localname name="name"></localname>' +
        '    </mutation>' +
        '    <field name="VAR0">name</field>' +
        '    <value name="STACK">' +
        '      <block type="controls_if" id="a"/>' +
        '    </value>' +
        '  </block>' +
        '</xml>');
        this.assertLexicalNames(xml, [['name', 'name']]);
      });
    })
  })
})