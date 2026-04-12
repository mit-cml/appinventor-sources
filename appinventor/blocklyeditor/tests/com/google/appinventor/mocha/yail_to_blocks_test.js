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

  // ================================================================
  // 7. Block positioning
  // ================================================================
  suite('Block positioning', function() {

    test('New block is placed below existing blocks in viewport', function() {
      var result1 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form))');
      chai.assert.isTrue(result1.success);

      var existingBlock = findTopBlock(this.workspace, 'component_event');
      chai.assert.isNotNull(existingBlock);
      var existingXY = existingBlock.getRelativeToSurfaceXY();
      var existingHeight = existingBlock.getHeightWidth().height;

      var result2 = AI.YailToBlocks.convert(this.workspace, '(def g$score 0)');
      chai.assert.isTrue(result2.success);

      var newBlock = findTopBlock(this.workspace, 'global_declaration');
      chai.assert.isNotNull(newBlock);
      var newXY = newBlock.getRelativeToSurfaceXY();

      chai.assert.isAtLeast(newXY.y, existingXY.y + existingHeight,
          'new block should be placed below existing block');
    });

    test('Multiple new blocks in one batch do not overlap each other', function() {
      var yail = '(def g$a 1)\n(def g$b 2)\n(def g$c 3)';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success);

      var topBlocks = this.workspace.getTopBlocks(false);
      chai.assert.lengthOf(topBlocks, 3);

      topBlocks.sort(function(a, b) {
        return a.getRelativeToSurfaceXY().y - b.getRelativeToSurfaceXY().y;
      });

      for (var i = 1; i < topBlocks.length; i++) {
        var prevBottom = topBlocks[i - 1].getRelativeToSurfaceXY().y +
            topBlocks[i - 1].getHeightWidth().height;
        var currTop = topBlocks[i].getRelativeToSurfaceXY().y;
        chai.assert.isAtLeast(currTop, prevBottom,
            'block ' + i + ' should not overlap block ' + (i - 1));
      }
    });

    test('Upsert preserves original position', function() {
      var result1 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form))');
      chai.assert.isTrue(result1.success);

      var block1 = findTopBlock(this.workspace, 'component_event');
      var origXY = block1.getRelativeToSurfaceXY();

      var result2 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form)\n' +
          '  (call-component-method \'Notifier1 \'ShowAlert ' +
          '(*list-for-runtime* "updated") \'(text)))');
      chai.assert.isTrue(result2.success);

      var block2 = findTopBlock(this.workspace, 'component_event');
      var newXY = block2.getRelativeToSurfaceXY();

      chai.assert.equal(newXY.x, origXY.x, 'upsert X should match original');
      chai.assert.equal(newXY.y, origXY.y, 'upsert Y should match original');
    });

  test('Component grouping: event placed near same component', function() {
    var result1 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form))');
    chai.assert.isTrue(result1.success);

    var clickBlock = findTopBlock(this.workspace, 'component_event');
    var clickXY = clickBlock.getRelativeToSurfaceXY();

    var result2 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Label1 Initialize () (set-this-form))');
    chai.assert.isTrue(result2.success);

    var result3 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 LongClick () (set-this-form))');
    chai.assert.isTrue(result3.success);

    var topBlocks = this.workspace.getTopBlocks(false);
    var longClickBlock = null;
    for (var i = 0; i < topBlocks.length; i++) {
      var m = topBlocks[i].mutationToDom ? topBlocks[i].mutationToDom() : null;
      if (m && m.getAttribute('instance_name') === 'Button1' &&
          m.getAttribute('event_name') === 'LongClick') {
        longClickBlock = topBlocks[i];
        break;
      }
    }
    chai.assert.isNotNull(longClickBlock, 'LongClick block should exist');

    var longClickXY = longClickBlock.getRelativeToSurfaceXY();
    chai.assert.equal(longClickXY.x, clickXY.x,
        'grouped block should align horizontally with group');

    var clickBottom = clickXY.y + clickBlock.getHeightWidth().height;
    chai.assert.isAtLeast(longClickXY.y, clickBottom,
        'grouped block should be below its group');
  });

  test('Component grouping: global var uses free-space (no grouping)', function() {
    var result1 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form))');
    chai.assert.isTrue(result1.success);

    var result2 = AI.YailToBlocks.convert(this.workspace, '(def g$counter 0)');
    chai.assert.isTrue(result2.success);

    var globalBlock = findTopBlock(this.workspace, 'global_declaration');
    chai.assert.isNotNull(globalBlock);

    var clickBlock = findTopBlock(this.workspace, 'component_event');
    var clickXY = clickBlock.getRelativeToSurfaceXY();
    var globalXY = globalBlock.getRelativeToSurfaceXY();

    var clickBottom = clickXY.y + clickBlock.getHeightWidth().height;
    chai.assert.isAtLeast(globalXY.y, clickBottom,
        'global should be below existing block');
  });

  test('Component grouping: toggle off disables grouping', function() {
    var origToggle = AI.YailToBlocks.GROUP_BY_COMPONENT;
    AI.YailToBlocks.GROUP_BY_COMPONENT = false;
    try {
      var result1 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form))');
      chai.assert.isTrue(result1.success);

      var clickBlock = findTopBlock(this.workspace, 'component_event');
      var clickXY = clickBlock.getRelativeToSurfaceXY();

      var result2 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 LongClick () (set-this-form))');
      chai.assert.isTrue(result2.success);

      var topBlocks = this.workspace.getTopBlocks(false);
      var longClickBlock = null;
      for (var i = 0; i < topBlocks.length; i++) {
        var m = topBlocks[i].mutationToDom ? topBlocks[i].mutationToDom() : null;
        if (m && m.getAttribute('instance_name') === 'Button1' &&
            m.getAttribute('event_name') === 'LongClick') {
          longClickBlock = topBlocks[i];
          break;
        }
      }
      chai.assert.isNotNull(longClickBlock);

      var longClickXY = longClickBlock.getRelativeToSurfaceXY();
      var clickBottom = clickXY.y + clickBlock.getHeightWidth().height;
      chai.assert.isAtLeast(longClickXY.y, clickBottom,
          'block should still not overlap even with grouping off');
    } finally {
      AI.YailToBlocks.GROUP_BY_COMPONENT = origToggle;
    }
  });

  test('Horizontal overlap avoidance: block shifts right when position is occupied', function() {
    // Create a Button1.Click handler
    var result1 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form))');
    chai.assert.isTrue(result1.success);

    // Create a Label1.Initialize handler — will go to free-space below Button1
    var result2 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Label1 Initialize () (set-this-form))');
    chai.assert.isTrue(result2.success);

    // Move Button1.Click so its bottom overlaps Label1's Y band
    var clickBlock = findTopBlock(this.workspace, 'component_event');
    var labelBlock = null;
    var topBlocks = this.workspace.getTopBlocks(false);
    for (var i = 0; i < topBlocks.length; i++) {
      var m = topBlocks[i].mutationToDom ? topBlocks[i].mutationToDom() : null;
      if (m && m.getAttribute('instance_name') === 'Label1') {
        labelBlock = topBlocks[i];
        break;
      }
    }
    chai.assert.isNotNull(labelBlock);

    var labelXY = labelBlock.getRelativeToSurfaceXY();
    var labelHW = labelBlock.getHeightWidth();

    // Move Button1.Click to overlap horizontally AND vertically with Label1
    clickBlock.moveTo(new Blockly.utils.Coordinate(labelXY.x, labelXY.y));

    // Now create Button1.LongClick — grouping will try to place below
    // Button1.Click, which is now at the same position as Label1.
    // avoidOverlap_ should shift it right to avoid Label1.
    var result3 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 LongClick () (set-this-form))');
    chai.assert.isTrue(result3.success);

    // Find the LongClick block
    topBlocks = this.workspace.getTopBlocks(false);
    var longClickBlock = null;
    for (var i = 0; i < topBlocks.length; i++) {
      var m = topBlocks[i].mutationToDom ? topBlocks[i].mutationToDom() : null;
      if (m && m.getAttribute('instance_name') === 'Button1' &&
          m.getAttribute('event_name') === 'LongClick') {
        longClickBlock = topBlocks[i];
        break;
      }
    }
    chai.assert.isNotNull(longClickBlock);

    var longClickXY = longClickBlock.getRelativeToSurfaceXY();
    var longClickHW = longClickBlock.getHeightWidth();

    // Verify no 2D overlap with Label1
    var noOverlap = longClickXY.x >= labelXY.x + labelHW.width ||
        longClickXY.x + longClickHW.width <= labelXY.x ||
        longClickXY.y >= labelXY.y + labelHW.height ||
        longClickXY.y + longClickHW.height <= labelXY.y;
    chai.assert.isTrue(noOverlap,
        'LongClick should not overlap with Label1 block');
  });
  });

  // ================================================================
  // 8. Procedure calls in expression position (regression: conv 0513883e)
  // ================================================================
  suite('Procedure calls', function() {

    test('Call defreturn in value socket resolves to procedures_callreturn',
        function() {
      // Define the procedure first so the workspace can resolve it.
      AI.YailToBlocks.convert(this.workspace,
          '(def-return (p$getMsg) "hi")');

      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  (set-and-coerce-property! \'Label1 \'Text ' +
          '((get-var p$getMsg)) \'text))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var event = findTopBlock(this.workspace, 'component_event');
      var setProp = getEventBody(event);
      var valueBlock = setProp.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock,
          'Text socket must hold the procedure call block');
      chai.assert.equal(valueBlock.type, 'procedures_callreturn');
      chai.assert.equal(valueBlock.getFieldValue('PROCNAME'), 'getMsg');
    });

    test('Call unresolved proc in value socket still builds callreturn',
        function() {
      // The def for p$getMsg will arrive in a LATER write_block within
      // the same batch. At conversion time it does not yet exist on the
      // workspace, but we must still produce a block with an output
      // connection so the value socket stays connected.
      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  (set-and-coerce-property! \'Label1 \'Text ' +
          '((get-var p$getMsg)) \'text))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var event = findTopBlock(this.workspace, 'component_event');
      var setProp = getEventBody(event);
      var valueBlock = setProp.getInput('VALUE').connection.targetBlock();
      chai.assert.isNotNull(valueBlock,
          'Text socket must hold the procedure call even when proc is undefined');
      chai.assert.equal(valueBlock.type, 'procedures_callreturn');
      chai.assert.equal(valueBlock.getFieldValue('PROCNAME'), 'getMsg');
    });

    test('Call unresolved proc as statement builds callnoreturn',
        function() {
      // Statement position needs a block with previousConnection.
      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  ((get-var p$doSomething) 1 2))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var event = findTopBlock(this.workspace, 'component_event');
      var body = getEventBody(event);
      chai.assert.isNotNull(body, 'event should have body statement');
      chai.assert.equal(body.type, 'procedures_callnoreturn');
      chai.assert.equal(body.getFieldValue('PROCNAME'), 'doSomething');
    });

    test('Call with argument subtree preserves nested expression', function() {
      // Nested call inside an arg — the inner call occupies a value
      // socket of the outer call. Both must end up as callreturn blocks.
      var yail = '(define-event Button1 Click () (set-this-form)\n' +
          '  (set-and-coerce-property! \'Label1 \'Text ' +
          '((get-var p$outer) ((get-var p$inner))) \'text))';
      var result = AI.YailToBlocks.convert(this.workspace, yail);
      chai.assert.isTrue(result.success, 'convert should succeed');

      var event = findTopBlock(this.workspace, 'component_event');
      var setProp = getEventBody(event);
      var outerCall = setProp.getInput('VALUE').connection.targetBlock();
      chai.assert.equal(outerCall.type, 'procedures_callreturn');
      chai.assert.equal(outerCall.getFieldValue('PROCNAME'), 'outer');

      var innerCall = outerCall.getInput('ARG0').connection.targetBlock();
      chai.assert.isNotNull(innerCall,
          'nested procedure call must occupy the outer ARG0 socket');
      chai.assert.equal(innerCall.type, 'procedures_callreturn');
      chai.assert.equal(innerCall.getFieldValue('PROCNAME'), 'inner');
    });
  });

  // ================================================================
  // 9. delete_block orphan-caller safety
  // ================================================================
  suite('deleteBlock orphan safety', function() {

    test('Delete procedure with live caller is refused', function() {
      AI.YailToBlocks.convert(this.workspace,
          '(def-return (p$compute) 42)');
      AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form)\n' +
          '  (set-and-coerce-property! \'Label1 \'Text ' +
          '((get-var p$compute)) \'text))');

      var delResult = AI.YailToBlocks.deleteBlock(this.workspace,
          'def p$compute');
      chai.assert.isFalse(delResult.success,
          'delete should be refused while caller is live');
      chai.assert.include(delResult.error, 'compute',
          'error should name the procedure');
      chai.assert.include(delResult.error, 'Button1.Click',
          'error should locate the caller for the LLM');

      // Procedure is still on the workspace.
      chai.assert.lengthOf(this.workspace.getTopBlocks(false), 2);
    });

    test('Delete procedure after caller rewritten succeeds', function() {
      AI.YailToBlocks.convert(this.workspace,
          '(def-return (p$compute) 42)');
      AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form)\n' +
          '  (set-and-coerce-property! \'Label1 \'Text ' +
          '((get-var p$compute)) \'text))');

      // First rewrite the caller to drop the reference, mimicking the
      // two-step pattern the grammar doc prescribes.
      AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form))');

      var delResult = AI.YailToBlocks.deleteBlock(this.workspace,
          'def p$compute');
      chai.assert.isTrue(delResult.success,
          'delete should succeed once callers are cleared');
    });

    test('Delete global with live reader is refused', function() {
      AI.YailToBlocks.convert(this.workspace, '(def g$count 0)');
      AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form)\n' +
          '  (set-and-coerce-property! \'Label1 \'Text (get-var g$count) \'text))');

      var delResult = AI.YailToBlocks.deleteBlock(this.workspace, 'def g$count');
      chai.assert.isFalse(delResult.success,
          'delete should be refused while a reader exists');
      chai.assert.include(delResult.error, 'count');
    });

    test('Delete procedure with no callers still succeeds', function() {
      AI.YailToBlocks.convert(this.workspace,
          '(def-return (p$unused) 0)');

      var delResult = AI.YailToBlocks.deleteBlock(this.workspace,
          'def p$unused');
      chai.assert.isTrue(delResult.success,
          'delete should succeed when no callers exist');
    });
  });

  // ================================================================
  // 10. validate() dry-run
  // ================================================================
  suite('validate()', function() {

    test('Single top-level form is accepted', function() {
      var r = AI.YailToBlocks.validate('(def g$a 1)');
      chai.assert.isTrue(r.valid, r.error || '');
    });

    test('Multiple top-level forms are rejected', function() {
      var r = AI.YailToBlocks.validate('(def g$a 1)\n(def g$b 2)');
      chai.assert.isFalse(r.valid);
      chai.assert.include(r.error, 'exactly one top-level form');
      chai.assert.include(r.error, 'Got 2');
    });

    test('Empty YAIL is rejected', function() {
      var r = AI.YailToBlocks.validate('');
      chai.assert.isFalse(r.valid);
    });

    test('Unbalanced parens are reported with deficit count', function() {
      var r = AI.YailToBlocks.validate('(def g$a 1');
      chai.assert.isFalse(r.valid);
    });
  });
});
