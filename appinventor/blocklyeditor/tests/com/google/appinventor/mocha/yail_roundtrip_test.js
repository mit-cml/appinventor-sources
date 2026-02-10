// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @fileoverview Round-trip tests for the YAIL <-> Blockly conversion system.
 * Tests that YAIL can be converted to blocks and back to equivalent YAIL,
 * and that blocks loaded from XML survive a YAIL round-trip.
 *
 * @author anthropic
 */

/**
 * Split a YAIL string into its top-level S-expression forms, smoosh each,
 * sort them, and join with '|'. This normalizes form ordering so that
 * round-trip outputs can be compared regardless of emission order.
 *
 * @param {string} yailStr The YAIL string to normalize.
 * @return {string} A normalized, order-independent representation.
 */
function normalizeYail(yailStr) {
  var forms = [];
  var depth = 0;
  var start = -1;
  for (var i = 0; i < yailStr.length; i++) {
    if (yailStr[i] === '(') {
      if (depth === 0) start = i;
      depth++;
    } else if (yailStr[i] === ')') {
      depth--;
      if (depth === 0 && start >= 0) {
        forms.push(smoosh(yailStr.substring(start, i + 1)));
        start = -1;
      }
    }
  }
  forms.sort();
  return forms.join('|');
}

suite('YAIL Round-Trip Tests', function() {

  suite('YAIL -> Blocks -> YAIL (Forward Round-Trip)', function() {
    setup(function() {
      this.workspace = Blockly.BlocklyEditor.create(document.body, '', false, false);
      Blockly.common.setMainWorkspace(this.workspace);
      initComponentTypes();
      this.workspace.addComponent('100', 'Button1', 'Button');
      this.workspace.addComponent('101', 'Label1', 'Label');
      this.workspace.addComponent('102', 'TextBox1', 'TextBox');
      this.workspace.addComponent('103', 'Notifier1', 'Notifier');
      this.workspace.addComponent('104', 'Clock1', 'Clock');
    });

    teardown(function() {
      this.workspace.dispose();
    });

    test('simple event handler', function() {
      var inputYail =
        '(define-event Button1 Click () (set-this-form)\n' +
        '  (call-component-method \'Notifier1 \'ShowAlert (*list-for-runtime* "Hello") \'(text)))';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('global variable (number)', function() {
      var inputYail = '(def g$score 0)';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('global variable (string)', function() {
      var inputYail = '(def g$name "hello")';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('global variable (boolean)', function() {
      var inputYail = '(def g$flag #t)';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('procedure (no return)', function() {
      var inputYail =
        '(def (p$greet $name)\n' +
        '  (call-component-method \'Notifier1 \'ShowAlert (*list-for-runtime* (lexical-value $name)) \'(text)))';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('procedure (with return - uses def-return)', function() {
      var inputYail =
        '(def-return (p$double $n)\n' +
        '  (call-yail-primitive * (*list-for-runtime* (lexical-value $n) 2) \'(number number) "*"))';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('event with if/else', function() {
      var inputYail =
        '(def g$score 0)\n\n' +
        '(define-event Button1 Click () (set-this-form)\n' +
        '  (if (call-yail-primitive yail-equal? (*list-for-runtime* (get-var g$score) 0) \'(any any) "=")\n' +
        '    (begin (set-and-coerce-property! \'Label1 \'Text "zero" \'text))\n' +
        '    (begin (set-and-coerce-property! \'Label1 \'Text "nonzero" \'text))))';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(normalizeYail(outputYail), normalizeYail(inputYail));
    });

    test('event with while loop', function() {
      var inputYail =
        '(def g$score 0)\n\n' +
        '(define-event Button1 Click () (set-this-form)\n' +
        '  (while (call-yail-primitive < (*list-for-runtime* (get-var g$score) 10) \'(number number) "<")\n' +
        '    (begin (set-var! g$score (call-yail-primitive + (*list-for-runtime* (get-var g$score) 1) \'(number number) "+")))))';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(normalizeYail(outputYail), normalizeYail(inputYail));
    });

    test('event with for-range', function() {
      var inputYail =
        '(define-event Button1 Click () (set-this-form)\n' +
        '  (forrange $i\n' +
        '    (begin (call-component-method \'Notifier1 \'ShowAlert (*list-for-runtime* "hi") \'(text)))\n' +
        '    1 10 1))';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('event with property get/set', function() {
      var inputYail =
        '(define-event Button1 Click () (set-this-form)\n' +
        '  (set-and-coerce-property! \'Label1 \'Text (get-property \'TextBox1 \'Text) \'text))';
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();
      chai.assert.equal(smoosh(outputYail), smoosh(inputYail));
    });

    test('multiple forms in one input', function() {
      var globalDef = '(def g$count 0)';
      var procDef =
        '(def (p$increment)\n' +
        '  (set-var! g$count (call-yail-primitive + (*list-for-runtime* (get-var g$count) 1) \'(number number) "+")))';
      var eventDef =
        '(define-event Button1 Click () (set-this-form)\n' +
        '  ((get-var p$increment)))';

      var inputYail = globalDef + '\n\n' + procDef + '\n\n' + eventDef;
      var result = AI.YailToBlocks.convert(this.workspace, inputYail);
      chai.assert.isTrue(result.success, result.error);
      var outputYail = this.workspace.getBlocksYail();

      // Use normalizeYail since form ordering may differ
      chai.assert.equal(normalizeYail(outputYail), normalizeYail(inputYail));
    });
  });

  suite('Blocks -> YAIL -> Blocks -> YAIL (Full Round-Trip from XML)', function() {
    setup(function() {
      this.workspace = Blockly.BlocklyEditor.create(document.body, '', false, false);
      Blockly.common.setMainWorkspace(this.workspace);
      initComponentTypes();
    });

    teardown(function() {
      this.workspace.dispose();
    });

    test('factorial project round-trip', function() {
      var formJson = '{"YaVersion":"80","Source":"Form","Properties":{"$Name":"Screen1","$Type":"Form","$Version":"11","Uuid":"0","Title":"Screen1","$Components":[{"$Name":"Button1","$Type":"Button","$Version":"5","Uuid":"597056068","Text":"Compute Factorial"},{"$Name":"TextBox1","$Type":"TextBox","$Version":"4","Uuid":"1173731358","Hint":"Hint for TextBox1"},{"$Name":"Label1","$Type":"Label","$Version":"2","Uuid":"-2024760173","Text":"Text for Label1"}]}}';

      var blocksXml = '<xml xmlns="http://www.w3.org/1999/xhtml">\n' +
        '  <block type="component_event" x="39" y="12">\n' +
        '    <mutation component_type="Button" instance_name="Button1" event_name="Click"></mutation>\n' +
        '    <title name="COMPONENT_SELECTOR">Button1</title>\n' +
        '    <statement name="DO">\n' +
        '      <block type="component_set_get" inline="false">\n' +
        '        <mutation component_type="Label" set_or_get="set" property_name="Text" is_generic="false" instance_name="Label1"></mutation>\n' +
        '        <title name="COMPONENT_SELECTOR">Label1</title>\n' +
        '        <title name="PROP">Text</title>\n' +
        '        <value name="VALUE">\n' +
        '          <block type="procedures_callreturn" inline="false">\n' +
        '            <mutation name="factorial">\n' +
        '              <arg name="x"></arg>\n' +
        '            </mutation>\n' +
        '            <title name="PROCNAME">factorial</title>\n' +
        '            <value name="ARG0">\n' +
        '              <block type="component_set_get">\n' +
        '                <mutation component_type="TextBox" set_or_get="get" property_name="Text" is_generic="false" instance_name="TextBox1"></mutation>\n' +
        '                <title name="COMPONENT_SELECTOR">TextBox1</title>\n' +
        '                <title name="PROP">Text</title>\n' +
        '              </block>\n' +
        '            </value>\n' +
        '          </block>\n' +
        '        </value>\n' +
        '      </block>\n' +
        '    </statement>\n' +
        '  </block>\n' +
        '  <block type="procedures_defreturn" inline="false" x="242" y="169">\n' +
        '    <mutation>\n' +
        '      <arg name="x"></arg>\n' +
        '    </mutation>\n' +
        '    <title name="NAME">factorial</title>\n' +
        '    <title name="VAR0">x</title>\n' +
        '    <value name="RETURN">\n' +
        '      <block type="local_declaration_expression" inline="false">\n' +
        '        <mutation>\n' +
        '          <localname name="acc"></localname>\n' +
        '        </mutation>\n' +
        '        <title name="VAR0">acc</title>\n' +
        '        <value name="DECL0">\n' +
        '          <block type="math_number">\n' +
        '            <title name="NUM">1</title>\n' +
        '          </block>\n' +
        '        </value>\n' +
        '        <value name="RETURN">\n' +
        '          <block type="controls_do_then_return" inline="false">\n' +
        '            <statement name="STM">\n' +
        '              <block type="controls_while" inline="false">\n' +
        '                <value name="TEST">\n' +
        '                  <block type="logic_compare" inline="true">\n' +
        '                    <title name="OP">NEQ</title>\n' +
        '                    <value name="A">\n' +
        '                      <block type="lexical_variable_get">\n' +
        '                        <title name="VAR">x</title>\n' +
        '                      </block>\n' +
        '                    </value>\n' +
        '                    <value name="B">\n' +
        '                      <block type="math_number">\n' +
        '                        <title name="NUM">0</title>\n' +
        '                      </block>\n' +
        '                    </value>\n' +
        '                  </block>\n' +
        '                </value>\n' +
        '                <statement name="DO">\n' +
        '                  <block type="lexical_variable_set" inline="false">\n' +
        '                    <title name="VAR">acc</title>\n' +
        '                    <value name="VALUE">\n' +
        '                      <block type="math_multiply" inline="true">\n' +
        '                        <mutation items="2"></mutation>\n' +
        '                        <value name="NUM0">\n' +
        '                          <block type="lexical_variable_get">\n' +
        '                            <title name="VAR">acc</title>\n' +
        '                          </block>\n' +
        '                        </value>\n' +
        '                        <value name="NUM1">\n' +
        '                          <block type="lexical_variable_get">\n' +
        '                            <title name="VAR">x</title>\n' +
        '                          </block>\n' +
        '                        </value>\n' +
        '                      </block>\n' +
        '                    </value>\n' +
        '                    <next>\n' +
        '                      <block type="lexical_variable_set" inline="false">\n' +
        '                        <title name="VAR">x</title>\n' +
        '                        <value name="VALUE">\n' +
        '                          <block type="math_subtract" inline="true">\n' +
        '                            <value name="A">\n' +
        '                              <block type="lexical_variable_get">\n' +
        '                                <title name="VAR">x</title>\n' +
        '                              </block>\n' +
        '                            </value>\n' +
        '                            <value name="B">\n' +
        '                              <block type="math_number">\n' +
        '                                <title name="NUM">1</title>\n' +
        '                              </block>\n' +
        '                            </value>\n' +
        '                          </block>\n' +
        '                        </value>\n' +
        '                      </block>\n' +
        '                    </next>\n' +
        '                  </block>\n' +
        '                </statement>\n' +
        '              </block>\n' +
        '            </statement>\n' +
        '            <value name="VALUE">\n' +
        '              <block type="lexical_variable_get">\n' +
        '                <title name="VAR">acc</title>\n' +
        '              </block>\n' +
        '            </value>\n' +
        '          </block>\n' +
        '        </value>\n' +
        '      </block>\n' +
        '    </value>\n' +
        '  </block>\n' +
        '  <yacodeblocks ya-version="80" language-version="17"></yacodeblocks>\n' +
        '</xml>';

      // Load form components and blocks
      processForm(formJson);
      processBlocks(formJson, blocksXml);

      // Get YAIL from loaded blocks
      var yail1 = this.workspace.getBlocksYail();
      chai.assert.isString(yail1);
      chai.assert.isNotEmpty(yail1);

      // Clear all blocks
      this.workspace.clear();

      // Convert YAIL back to blocks
      var result = AI.YailToBlocks.convert(this.workspace, yail1);
      chai.assert.isTrue(result.success, 'Convert failed: ' + result.error);

      // Get YAIL again
      var yail2 = this.workspace.getBlocksYail();

      // Compare (whitespace-insensitive, order-insensitive)
      chai.assert.equal(normalizeYail(yail1), normalizeYail(yail2),
        'Round-trip YAIL mismatch.\nOriginal:\n' + yail1 + '\nAfter round-trip:\n' + yail2);
    });
  });
});
