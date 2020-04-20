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
      test('Matching Nesting - No Dupes', function() {
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
  suite('getNamesInScope', function() {
    setup(function() {
      this.assertNames = function(xml, expectedVars) {
        Blockly.Xml.domToWorkspace(xml, this.workspace);
        var block = this.workspace.getBlockById('a');
        var actualVars = Blockly.FieldLexicalVariable
            .getNamesInScope(block);
        chai.assert.sameDeepOrderedMembers(actualVars, expectedVars);
      }
    });
    test('Globals First', function() {
      var xml = Blockly.Xml.textToDom('<xml>' +
      '  <block type="global_declaration" y="-200">' +
      '    <field name="NAME">gName</field>' +
      '  </block>' +
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
      this.assertNames(xml, [
        ['global gName', 'global gName'],
        ['name', 'name'],
      ]);
    });
    test('Vars Sorted 1', function() {
      var xml = Blockly.Xml.textToDom('<xml>' +
      '  <block type="global_declaration" y="-200">' +
      '    <field name="NAME">gA</field>' +
      '  </block>' +
      '  <block type="global_declaration" y="-200">' +
      '    <field name="NAME">gB</field>' +
      '  </block>' +
      '  <block type="local_declaration_statement">' +
      '    <mutation>' +
      '      <localname name="lA"></localname>' +
      '    </mutation>' +
      '    <field name="VAR0">lA</field>' +
      '    <value name="STACK">' +
      '      <block type="local_declaration_statement">' +
      '        <mutation>' +
      '          <localname name="lB"></localname>' +
      '        </mutation>' +
      '        <field name="VAR0">lB</field>' +
      '        <value name="STACK">' +
      '          <block type="controls_if" id="a"/>' +
      '        </value>' +
      '      </block>' +
      '    </value>' +
      '  </block>' +
      '</xml>');
      this.assertNames(xml, [
        ['global gA', 'global gA'],
        ['global gB', 'global gB'],
        ['lA', 'lA'],
        ['lB', 'lB'],
      ]);
    });
    test('Vars Sorted 2', function() {
      var xml = Blockly.Xml.textToDom('<xml>' +
      '  <block type="global_declaration" y="-200">' +
      '    <field name="NAME">gB</field>' +
      '  </block>' +
      '  <block type="global_declaration" y="-200">' +
      '    <field name="NAME">gA</field>' +
      '  </block>' +
      '  <block type="local_declaration_statement">' +
      '    <mutation>' +
      '      <localname name="lB"></localname>' +
      '    </mutation>' +
      '    <field name="VAR0">lB</field>' +
      '    <value name="STACK">' +
      '      <block type="local_declaration_statement">' +
      '        <mutation>' +
      '          <localname name="lA"></localname>' +
      '        </mutation>' +
      '        <field name="VAR0">lA</field>' +
      '        <value name="STACK">' +
      '          <block type="controls_if" id="a"/>' +
      '        </value>' +
      '      </block>' +
      '    </value>' +
      '  </block>' +
      '</xml>');
      this.assertNames(xml, [
        ['global gA', 'global gA'],
        ['global gB', 'global gB'],
        ['lA', 'lA'],
        ['lB', 'lB'],
      ]);
    });
    test('Global Prefix is Translated', function() {
      Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX = 'testPrefix'
      var xml = Blockly.Xml.textToDom('<xml>' +
      '  <block type="global_declaration" y="-200">' +
      '    <field name="NAME">gName</field>' +
      '  </block>' +
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
      this.assertNames(xml, [
        ['testPrefix gName', 'global gName'],
        ['name', 'name'],
      ]);
    });
  })
  suite('prefixSuffix', function() {
    test('No Suffix', function() {
      var prefixSuffix = Blockly.FieldLexicalVariable.prefixSuffix('name');
      chai.assert.sameDeepOrderedMembers(prefixSuffix, ['name', '']);
    });
    test('Digit Suffix', function() {
      var prefixSuffix = Blockly.FieldLexicalVariable.prefixSuffix('name1');
      chai.assert.sameDeepOrderedMembers(prefixSuffix, ['name', '1']);
    }); 
    test('Letter Following Digit', function() {
      var prefixSuffix = Blockly.FieldLexicalVariable.prefixSuffix('name1a');
      chai.assert.sameDeepOrderedMembers(prefixSuffix, ['name1a', '']);
    }); 
  });
  suite('nameNotIn', function() {
    test('No Conflict', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['bar', 'cat', 'pupper']);
      chai.assert.equal(newName, 'foo');
    });
    test('Empty Not Used', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['foo1', 'foo2', 'foo3']);
      chai.assert.equal(newName, 'foo');
    });
    test('Empty & 0', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['foo', 'foo0']);
      chai.assert.equal(newName, 'foo2');
    });
    test('Empty & 1', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['foo', 'foo1']);
      chai.assert.equal(newName, 'foo2');
    });
    test('Empty & 2', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['foo', 'foo2']);
      chai.assert.equal(newName, 'foo3');
    });
    test('Empty, 2 & 4', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['foo', 'foo2', 'foo4']);
      chai.assert.equal(newName, 'foo3');
    });
    test('Empty, 2, 3 & 4', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['foo', 'foo2', 'foo3', 'foo4']);
      chai.assert.equal(newName, 'foo5');
    });
    test('Extra vars', function() {
      var newName = Blockly.FieldLexicalVariable
          .nameNotIn('foo', ['foo', 'foo2', 'foo', 'foo4', 'bar3', 'cats']);
      chai.assert.equal(newName, 'foo3');
    });
  });
  suite('setValue', function() {
    test('Global Prefix Incorrect', function() {
      var field = new Blockly.FieldLexicalVariable('notGlobal actualName');
      chai.assert.equal(field.getText(), 'global actualName');
    });
  });
  suite('checkIdentifier', function() {
    test('Spaces -> Underscores', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('test test');
      chai.assert.isTrue(result.isLegal);
      chai.assert.equal(result.transformed, 'test_test');
    });
    test('Trimming', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('   test   ');
      chai.assert.isTrue(result.isLegal);
      chai.assert.equal(result.transformed, 'test');
    });
    test('Trim to emtpy', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('   ');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, '');
    })
    test('Chinese Character', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('修改数值');
      chai.assert.isTrue(result.isLegal);
      chai.assert.equal(result.transformed, '修改数值');
    });
    // TODO: I thought this was supposed to be illegal, but it works.
    test.skip('@', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('@test');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, '@test');
    });
    test('.', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('.test');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, '.test');
    });
    test('-', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('-test');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, '-test');
    });
    test('\\', function() {  // Checks single slash.
      var result = Blockly.LexicalVariable.checkIdentifier('\\test');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, '\\test');
    });
    test('+', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('+test');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, '+test');
    });
    test('[', function() {
      var result = Blockly.LexicalVariable.checkIdentifier('[test');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, '[test');
    });
    test(']', function() {
      var result = Blockly.LexicalVariable.checkIdentifier(']test');
      chai.assert.isFalse(result.isLegal);
      chai.assert.equal(result.transformed, ']test');
    });
  });
  suite('makeLegalIdentifier', function() {
    test('Legal', function() {
      var name = Blockly.LexicalVariable.makeLegalIdentifier('test');
      chai.assert.equal(name, 'test');
    });
    test('Illegal, Empty', function() {
      var name = Blockly.LexicalVariable.makeLegalIdentifier('   ');
      chai.assert.equal(name, '_');
    });
    // TODO: See TODO in file.
    test.skip('Just Illegal', function() {})
  })
});
