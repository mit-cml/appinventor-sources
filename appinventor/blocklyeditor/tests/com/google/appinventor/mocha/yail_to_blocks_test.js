// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * @license
 * @fileoverview Unit tests for the YAIL-to-Blocks converter (AI.YailToBlocks).
 *
 * Tests the convert() and deleteBlock() APIs that parse YAIL S-expressions
 * and create corresponding Blockly blocks in the workspace.
 */

suite('YAIL to Blocks Converter', function() {
  setup(function() {
    this.workspace = Blockly.BlocklyEditor.create(document.body, '', false, false);
    Blockly.common.setMainWorkspace(this.workspace);
    initComponentTypes();
    // Add component instances needed for tests
    this.workspace.addComponent('100', 'Button1', 'Button');
    this.workspace.addComponent('101', 'Label1', 'Label');
    this.workspace.addComponent('102', 'TextBox1', 'TextBox');
    this.workspace.addComponent('103', 'Notifier1', 'Notifier');
    this.workspace.addComponent('104', 'Clock1', 'Clock');
  });

  teardown(function() {
    this.workspace.dispose();
  });

  /**
   * Helper: find the first top block of the given type.
   */
  function findTopBlock(workspace, type) {
    var topBlocks = workspace.getTopBlocks(false);
    for (var i = 0; i < topBlocks.length; i++) {
      if (topBlocks[i].type === type) {
        return topBlocks[i];
      }
    }
    return null;
  }

  /**
   * Helper: get the body (first statement in DO) of an event block.
   */
  function getEventBody(block) {
    var doInput = block.getInput('DO');
    return doInput && doInput.connection ? doInput.connection.targetBlock() : null;
  }

  // ================================================================
  // 1. Top-level forms — convert()
  // ================================================================
  suite('Top-level forms', function() {

    test('Event handler', function() {
      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  (call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "Hello") \'(text)))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var block = findTopBlock(this.workspace, 'component_event');
      chai.assert.isNotNull(block, 'should create a component_event block');
      chai.assert.equal(block.getFieldValue('COMPONENT_SELECTOR'), 'Button1');

      var mutation = block.mutationToDom();
      chai.assert.equal(mutation.getAttribute('event_name'), 'Click');
    });

    test('Generic event handler', function() {
      var yail = '(define-generic-event Button Click ($component) (set-this-form)\n' +
          '  (call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "clicked") \'(text)))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var block = findTopBlock(this.workspace, 'component_event');
      chai.assert.isNotNull(block, 'should create a component_event block');

      var mutation = block.mutationToDom();
      chai.assert.equal(mutation.getAttribute('is_generic'), 'true');
      chai.assert.equal(mutation.getAttribute('component_type'), 'Button');
    });

    test('Global variable (number)', function() {
      var yail = '(def g$score 0)';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var block = findTopBlock(this.workspace, 'global_declaration');
      chai.assert.isNotNull(block, 'should create a global_declaration block');
      chai.assert.equal(block.getFieldValue('NAME'), 'score');

      var valueBlock = block.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock, 'should have a value block');
      chai.assert.equal(valueBlock.type, 'math_number');
    });

    test('Global variable (string)', function() {
      var yail = '(def g$name "hello")';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var block = findTopBlock(this.workspace, 'global_declaration');
      chai.assert.isNotNull(block, 'should create a global_declaration block');
      chai.assert.equal(block.getFieldValue('NAME'), 'name');

      var valueBlock = block.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock, 'should have a value block');
      chai.assert.equal(valueBlock.type, 'text');
    });

    test('Procedure (no return)', function() {
      var yail = '(def (p$greet $name)\n' +
          '  (call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* (lexical-value $name)) \'(text)))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var block = findTopBlock(this.workspace, 'procedures_defnoreturn');
      chai.assert.isNotNull(block, 'should create a procedures_defnoreturn block');
      chai.assert.equal(block.getFieldValue('NAME'), 'greet');
      chai.assert.include(block.arguments_, 'name',
          'should have parameter "name"');
    });

    test('Procedure (with return)', function() {
      var yail = '(def-return (p$double $n)\n' +
          '  (call-yail-primitive * (*list-for-runtime* (lexical-value $n) 2) ' +
          '\'(number number) "*"))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var block = findTopBlock(this.workspace, 'procedures_defreturn');
      chai.assert.isNotNull(block, 'should create a procedures_defreturn block');
      chai.assert.equal(block.getFieldValue('NAME'), 'double');
      chai.assert.include(block.arguments_, 'n',
          'should have parameter "n"');

      var returnBlock = block.getInput('RETURN').connection.targetBlock();
      chai.assert.isNotNull(returnBlock, 'should have a return value block');
    });
  });

  // ================================================================
  // 2. Expressions (inside event handler body)
  // ================================================================
  suite('Expressions', function() {

    /**
     * Helper: convert an event wrapping a single body expression,
     * then return the first statement block in the event body.
     */
    function convertAndGetBody(workspace, bodyYail) {
      var yail = '(define-event Button1 Click () (set-this-form)\n  ' +
          bodyYail + ')';
      var result = AI.YailToBlocks.convert(workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');
      var event = findTopBlock(workspace, 'component_event');
      chai.assert.isNotNull(event, 'should have event block');
      return getEventBody(event);
    }

    test('Math addition', function() {
      var body = convertAndGetBody(this.workspace,
          '(call-yail-primitive + (*list-for-runtime* 1 2) \'(number number) "+")');
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'math_add');
    });

    test('Comparison (equal)', function() {
      var body = convertAndGetBody(this.workspace,
          '(call-yail-primitive yail-equal? (*list-for-runtime* 1 2) \'(any any) "=")');
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'math_compare');
      chai.assert.equal(body.getFieldValue('OP'), 'EQ');
    });

    test('Text join', function() {
      var body = convertAndGetBody(this.workspace,
          '(call-yail-primitive string-append (*list-for-runtime* "hello" " " "world") ' +
          '\'(text text text) "join")');
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'text_join');
    });

    test('Make a list', function() {
      var body = convertAndGetBody(this.workspace,
          '(call-yail-primitive make-yail-list (*list-for-runtime* 1 2 3) ' +
          '\'(any any any) "make a list")');
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'lists_create_with');
    });

    test('Number literal via set property', function() {
      var body = convertAndGetBody(this.workspace,
          '(set-and-coerce-property! \'Label1 \'Text 42 \'text)');
      chai.assert.isNotNull(body, 'should have body block');
      // The body is a set-property block; check its VALUE input
      var valueBlock = body.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock, 'should have value input');
      chai.assert.equal(valueBlock.type, 'math_number');
      chai.assert.equal(valueBlock.getFieldValue('NUM'), '42');
    });

    test('String literal via set property', function() {
      var body = convertAndGetBody(this.workspace,
          '(set-and-coerce-property! \'Label1 \'Text "hello" \'text)');
      chai.assert.isNotNull(body, 'should have body block');
      var valueBlock = body.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock, 'should have value input');
      chai.assert.equal(valueBlock.type, 'text');
    });

    test('Boolean literal (true)', function() {
      var yail = '(def g$flag #t)';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');
      var block = findTopBlock(this.workspace, 'global_declaration');
      var valueBlock = block.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock, 'should have value block');
      chai.assert.equal(valueBlock.type, 'logic_boolean');
      chai.assert.equal(valueBlock.getFieldValue('BOOL'), 'TRUE');
    });

    test('Variable get (global)', function() {
      // First define the variable
      var defResult = AI.YailToBlocks.convert(this.workspace, '(def g$score 0)');
      chai.assert.isTrue(defResult.success);

      // Now use it in an event body
      var body = convertAndGetBody(this.workspace,
          '(set-and-coerce-property! \'Label1 \'Text (get-var g$score) \'text)');
      chai.assert.isNotNull(body, 'should have body block');
      var valueBlock = body.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock, 'should have value input');
      chai.assert.equal(valueBlock.type, 'lexical_variable_get');
      chai.assert.include(valueBlock.getFieldValue('VAR'), 'score');
    });

    test('Variable set (global)', function() {
      // First define the variable
      var defResult = AI.YailToBlocks.convert(this.workspace, '(def g$score 0)');
      chai.assert.isTrue(defResult.success);

      var body = convertAndGetBody(this.workspace,
          '(set-var! g$score 10)');
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'lexical_variable_set');
    });

    test('Property get', function() {
      var body = convertAndGetBody(this.workspace,
          '(set-and-coerce-property! \'Label1 \'Text ' +
          '(get-property \'TextBox1 \'Text) \'text)');
      chai.assert.isNotNull(body, 'should have body block');
      var valueBlock = body.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock, 'should have value input');
      chai.assert.equal(valueBlock.type, 'component_set_get');
      var mutation = valueBlock.mutationToDom();
      chai.assert.equal(mutation.getAttribute('set_or_get'), 'get');
    });

    test('Method call', function() {
      var body = convertAndGetBody(this.workspace,
          '(call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "msg") \'(text))');
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'component_method');
    });
  });

  // ================================================================
  // 3. Control flow (inside event handler body)
  // ================================================================
  suite('Control flow', function() {

    test('If-else statement', function() {
      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  (if (call-yail-primitive yail-equal? (*list-for-runtime* 1 1) \'(any any) "=")\n' +
          '    (begin (set-and-coerce-property! \'Label1 \'Text "yes" \'text))\n' +
          '    (begin (set-and-coerce-property! \'Label1 \'Text "no" \'text))))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var event = findTopBlock(this.workspace, 'component_event');
      var body = getEventBody(event);
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'controls_if');
    });

    test('While loop', function() {
      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  (while (call-yail-primitive yail-equal? (*list-for-runtime* 1 1) \'(any any) "=")\n' +
          '    (begin (call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "loop") \'(text)))))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var event = findTopBlock(this.workspace, 'component_event');
      var body = getEventBody(event);
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'controls_while');
    });

    test('For range', function() {
      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  (forrange $i\n' +
          '    (begin (call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "hi") \'(text)))\n' +
          '    1 10 1))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var event = findTopBlock(this.workspace, 'component_event');
      var body = getEventBody(event);
      chai.assert.isNotNull(body, 'should have body block');
      chai.assert.equal(body.type, 'controls_forRange');
    });
  });

  // ================================================================
  // 4. deleteBlock()
  // ================================================================
  suite('deleteBlock', function() {

    test('Delete event handler', function() {
      var result = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form))');
      chai.assert.isTrue(result.success);
      chai.assert.isNotNull(findTopBlock(this.workspace, 'component_event'),
          'event block should exist before delete');

      var delResult = AI.YailToBlocks.deleteBlock(this.workspace,
          'define-event Button1 Click');
      chai.assert.isTrue(delResult.success, 'delete should succeed');
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 0,
          'no top blocks should remain');
    });

    test('Delete global variable', function() {
      var result = AI.YailToBlocks.convert(this.workspace, '(def g$score 0)');
      chai.assert.isTrue(result.success);

      var delResult = AI.YailToBlocks.deleteBlock(this.workspace,
          'def g$score');
      chai.assert.isTrue(delResult.success, 'delete should succeed');
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 0,
          'no top blocks should remain');
    });

    test('Delete procedure', function() {
      var result = AI.YailToBlocks.convert(this.workspace,
          '(def (p$greet $name) ' +
          '(call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "hi") \'(text)))');
      chai.assert.isTrue(result.success);

      var delResult = AI.YailToBlocks.deleteBlock(this.workspace,
          'def p$greet');
      chai.assert.isTrue(delResult.success, 'delete should succeed');
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 0,
          'no top blocks should remain');
    });

    test('Delete nonexistent block returns failure', function() {
      var delResult = AI.YailToBlocks.deleteBlock(this.workspace,
          'define-event NonExistent Click');
      chai.assert.isFalse(delResult.success,
          'should fail for nonexistent block');
    });
  });

  // ================================================================
  // 5. Error cases
  // ================================================================
  suite('Error cases', function() {

    test('Empty string', function() {
      var result = AI.YailToBlocks.convert(this.workspace, '');
      chai.assert.isFalse(result.success, 'should fail on empty input');
    });

    test('Malformed YAIL', function() {
      var result = AI.YailToBlocks.convert(this.workspace, '(define-event');
      chai.assert.isFalse(result.success, 'should fail on malformed YAIL');
      chai.assert.isNotNull(result.error, 'should include error message');
    });

    test('Unknown form', function() {
      var result = AI.YailToBlocks.convert(this.workspace, '(unknown-form)');
      chai.assert.isFalse(result.success, 'should fail on unknown form');
    });
  });

  // ================================================================
  // 6. Upsert semantics
  // ================================================================
  suite('Upsert semantics', function() {

    test('Re-converting a global variable replaces existing block', function() {
      var result1 = AI.YailToBlocks.convert(this.workspace, '(def g$score 0)');
      chai.assert.isTrue(result1.success);
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 1,
          'should have one top block');

      var result2 = AI.YailToBlocks.convert(this.workspace, '(def g$score 100)');
      chai.assert.isTrue(result2.success);
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 1,
          'should still have one top block after upsert');

      var block = findTopBlock(this.workspace, 'global_declaration');
      var valueBlock = block.getInput('VALUE').connection.targetBlock();
      chai.assert.equal(valueBlock.type, 'math_number');
      chai.assert.equal(valueBlock.getFieldValue('NUM'), '100',
          'value should be updated to 100');
    });

    test('Re-converting an event handler replaces existing block', function() {
      var result1 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form))');
      chai.assert.isTrue(result1.success);
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 1);

      var result2 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form)\n' +
          '  (call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "replaced") \'(text)))');
      chai.assert.isTrue(result2.success);
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 1,
          'should still have one top block after upsert');

      var event = findTopBlock(this.workspace, 'component_event');
      var body = getEventBody(event);
      chai.assert.isNotNull(body, 'replaced event should have body');
    });
  });
});
