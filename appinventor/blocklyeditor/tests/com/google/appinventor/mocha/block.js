// Copyright © 2020 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Block behavior tests. Not related to code generation.
 *
 */

suite('Blocks', function() {
  setup(function() {
    this.workspace = Blockly.inject('blocklyDiv', {});
    Blockly.defineBlocksWithJsonArray([
      {
        "type": "empty_block",
        "message0": ""
      },
      {
        "type": "stack_block",
        "message0": "",
        "previousStatement": null,
        "nextStatement": null
      },
      {
        "type": "row_block",
        "message0": "%1",
        "args0": [
          {
            "type": "input_value",
            "name": "INPUT"
          }
        ],
        "output": null
      },
      {
        "type": "statement_block",
        "message0": "%1",
        "args0": [
          {
            "type": "input_statement",
            "name": "STATEMENT"
          }
        ],
        "previousStatement": null,
        "nextStatement": null
      }
    ]);

    // Because apparently Blockly can't give us a block that's actually rendered.
    this.createBlock = function (type) {
      var block = this.workspace.newBlock(type);
      block.initSvg();
      block.render();
      return block;
    }
  });
  teardown(function() {
    this.workspace.dispose();
    Blockly.mainWorkspace = null;
    delete Blockly.Blocks['empty_block'];
    delete Blockly.Blocks['stack_block'];
    delete Blockly.Blocks['row_block'];
    delete Blockly.Blocks['statement_block'];
  });
  suite('Collapsing and Expanding', function() {
    setup(function() {
      Blockly.Events.disable();
    });
    teardown(function() {
      Blockly.Events.enable();
    });
    function assertCollapsed(block, opt_string) {
      chai.assert.isTrue(block.isCollapsed());
      for (var i = 0, input; (input = block.inputList[i]); i++) {
        if (input.name == Blockly.BlockSvg.COLLAPSED_INPUT_NAME) {
          continue;
        }
        chai.assert.isFalse(input.isVisible());
        for (var j = 0, field; (field = input.fieldRow[j]); j++) {
          chai.assert.isFalse(field.isVisible());
        }
      }
      var icons = block.getIcons();
      for (var i = 0, icon; (icon = icons[i]); i++) {
        if (icon.bubbleIsVisible) {
          chai.assert.isFalse(icon.bubbleIsVisible());
        }
      }

      var input = block.getInput(Blockly.BlockSvg.COLLAPSED_INPUT_NAME);
      chai.assert.isNotNull(input);
      chai.assert.isTrue(input.isVisible());
      var field = block.getField(Blockly.BlockSvg.COLLAPSED_FIELD_NAME);
      chai.assert.isNotNull(field);
      chai.assert.isTrue(field.isVisible());

      if (opt_string) {
        chai.assert.equal(field.getText(), opt_string);
      }
    }
    function assertExpanded(block) {
      chai.assert.isFalse(block.isCollapsed());
      for (var i = 0, input; (input = block.inputList[i]); i++) {
        chai.assert.isTrue(input.isVisible());
        for (var j = 0, field; (field = input.fieldRow[j]); j++) {
          chai.assert.isTrue(field.isVisible());
        }
      }

      var input = block.getInput(Blockly.BlockSvg.COLLAPSED_INPUT_NAME);
      chai.assert.isNull(input);
      var field = block.getField(Blockly.BlockSvg.COLLAPSED_FIELD_NAME);
      chai.assert.isNull(field);
    }
    function assertHidden(block) {
      // Annoyingly, this is the only way I could find to test visibility.
      var node = block.getSvgRoot();
      do {
        var visible = node.style.display != 'none';
        if (!visible) {
          chai.assert(true);  // Succeed the test
          return;
        }
        node = node.parentNode;
      } while (node != document);
      chai.assert.fail();
    }
    function assertShown(block) {
      // Annoyingly, this is the only way I could find to test visibility.
      var node = block.getSvgRoot();
      do {
        var visible = node.style.display != 'none';
        if (!visible) {
          chai.assert.fail();
          return;
        }
        node = node.parentNode;
      } while (node != document);
      chai.assert(true);  // Succeed the test
    }
    suite('Connecting And Disconnecting', function() {
      test('Connect Block to Next', function() {
        var blockA = this.createBlock('stack_block');
        var blockB = this.createBlock('stack_block');

        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.nextConnection.connect(blockB.previousConnection);
        assertExpanded(blockB);
      });
      test('Connect Block to Value Input', function() {
        var blockA = this.createBlock('row_block');
        var blockB = this.createBlock('row_block');

        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
        assertHidden(blockB);
        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
      });
      test('Connect Block to Statement Input', function() {
        var blockA = this.createBlock('statement_block');
        var blockB = this.createBlock('stack_block');

        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.getInput('STATEMENT').connection
            .connect(blockB.previousConnection);
        assertHidden(blockB);
        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
      });
      test('Connect Block to Child of Collapsed - Input', function() {
        var blockA = this.createBlock('row_block');
        var blockB = this.createBlock('row_block');
        var blockC = this.createBlock('row_block');

        blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        blockB.getInput('INPUT').connection.connect(blockC.outputConnection);
        assertHidden(blockC);

        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
        assertShown(blockC);
      });
      test('Connect Block to Child of Collapsed - Next', function() {
        var blockA = this.createBlock('statement_block');
        var blockB = this.createBlock('stack_block');
        var blockC = this.createBlock('stack_block');

        blockA.getInput('STATEMENT').connection
            .connect(blockB.previousConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        blockB.nextConnection.connect(blockC.previousConnection);
        assertHidden(blockC);

        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
        assertShown(blockC);
      });
      test('Connect Block to Value Input Already Taken', function() {
        var blockA = this.createBlock('row_block');
        var blockB = this.createBlock('row_block');
        var blockC = this.createBlock('row_block');

        blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        blockA.getInput('INPUT').connection.connect(blockC.outputConnection);
        assertHidden(blockC);
        assertHidden(blockB);  // Still hidden after C is inserted between.

        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
        assertShown(blockC);
      });
      test('Connect Block to Statement Input Already Taken', function() {
        var blockA = this.createBlock('statement_block');
        var blockB = this.createBlock('stack_block');
        var blockC = this.createBlock('stack_block');

        blockA.getInput('STATEMENT').connection
            .connect(blockB.previousConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        blockA.getInput('STATEMENT').connection
            .connect(blockC.previousConnection);
        assertHidden(blockC);
        assertHidden(blockB);  // Still hidden after C is inserted between.

        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
        assertShown(blockC);
      });
      test('Connect Block with Child - Input', function() {
        var blockA = this.createBlock('row_block');
        var blockB = this.createBlock('row_block');
        var blockC = this.createBlock('row_block');

        blockB.getInput('INPUT').connection.connect(blockC.outputConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
        assertHidden(blockC);
        assertHidden(blockB);

        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
        assertShown(blockC);
      });
      test('Connect Block with Child - Statement', function() {
        var blockA = this.createBlock('statement_block');
        var blockB = this.createBlock('stack_block');
        var blockC = this.createBlock('stack_block');

        blockB.nextConnection.connect(blockC.previousConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.getInput('STATEMENT').connection
            .connect(blockB.previousConnection);
        assertHidden(blockC);
        assertHidden(blockB);

        blockA.setCollapsed(false);
        assertExpanded(blockA);
        assertShown(blockB);
        assertShown(blockC);
      });
      test('Disconnect Block from Value Input', function() {
        var blockA = this.createBlock('row_block');
        var blockB = this.createBlock('row_block');

        blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        blockB.outputConnection.disconnect();
        assertShown(blockB);
      });
      test('Disconnect Block from Statement Input', function() {
        var blockA = this.createBlock('statement_block');
        var blockB = this.createBlock('stack_block');

        blockA.getInput('STATEMENT').connection
            .connect(blockB.previousConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        blockB.previousConnection.disconnect();
        assertShown(blockB);
      });
      test('Disconnect Block from Child of Collapsed - Input', function() {
        var blockA = this.createBlock('row_block');
        var blockB = this.createBlock('row_block');
        var blockC = this.createBlock('row_block');

        blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
        blockB.getInput('INPUT').connection.connect(blockC.outputConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        assertHidden(blockC);

        blockC.outputConnection.disconnect();
        assertShown(blockC);
      });
      test('Disconnect Block from Child of Collapsed - Next', function() {
        var blockA = this.createBlock('statement_block');
        var blockB = this.createBlock('stack_block');
        var blockC = this.createBlock('stack_block');

        blockA.getInput('STATEMENT').connection
            .connect(blockB.previousConnection);
        blockB.nextConnection.connect(blockC.previousConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        assertHidden(blockC);

        blockC.previousConnection.disconnect();
        assertShown(blockC);
      });
      test('Disconnect Block with Child - Input', function() {
        var blockA = this.createBlock('row_block');
        var blockB = this.createBlock('row_block');
        var blockC = this.createBlock('row_block');

        blockB.getInput('INPUT').connection.connect(blockC.outputConnection);
        blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockB);
        assertHidden(blockC);

        blockB.outputConnection.disconnect();
        assertShown(blockB);
        assertShown(blockC);
      });
      test('Disconnect Block with Child - Statement', function() {
        var blockA = this.createBlock('statement_block');
        var blockB = this.createBlock('stack_block');
        var blockC = this.createBlock('stack_block');

        blockB.nextConnection.connect(blockC.previousConnection);
        blockA.getInput('STATEMENT').connection
            .connect(blockB.previousConnection);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        assertHidden(blockC);
        assertHidden(blockB);

        blockB.previousConnection.disconnect();
        assertShown(blockB);
        assertShown(blockC);
      });
    });
    suite('Adding and Removing Block Parts', function() {
      test('Add Previous Connection', function() {
        var blockA = this.createBlock('empty_block');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.setPreviousStatement(true);
        assertCollapsed(blockA);
        chai.assert.isNotNull(blockA.previousConnection);
      });
      test('Add Next Connection', function() {
        var blockA = this.createBlock('empty_block');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.setNextStatement(true);
        assertCollapsed(blockA);
        chai.assert.isNotNull(blockA.nextConnection);
      });
      test('Add Input', function() {
        var blockA = this.createBlock('empty_block');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.appendDummyInput('NAME');
        Blockly.renderManagement.triggerQueuedRenders();
        assertCollapsed(blockA);
        chai.assert.isNotNull(blockA.getInput('NAME'));
      });
      test('Add Field', function() {
        var blockA = this.createBlock('empty_block');
        var input = blockA.appendDummyInput('NAME');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        input.appendField(new Blockly.FieldLabel('test'), 'FIELD');
        assertCollapsed(blockA);
        var field = blockA.getField('FIELD');
        chai.assert.isNotNull(field);
        chai.assert.equal('test', field.getText());
      });
      test('Add Icon', function() {
        var blockA = this.createBlock('empty_block');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.setCommentText('test');
        assertCollapsed(blockA);
      });
      test('Remove Previous Connection', function() {
        var blockA = this.createBlock('empty_block');
        blockA.setPreviousStatement(true);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.setPreviousStatement(false);
        assertCollapsed(blockA);
        chai.assert.isNull(blockA.previousConnection);
      });
      test('Remove Next Connection', function() {
        var blockA = this.createBlock('empty_block');
        blockA.setNextStatement(true);
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.setNextStatement(false);
        assertCollapsed(blockA);
        chai.assert.isNull(blockA.nextConnection);
      });
      test('Remove Input', function() {
        var blockA = this.createBlock('empty_block');
        blockA.appendDummyInput('NAME');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.removeInput('NAME');
        assertCollapsed(blockA);
        chai.assert.isNull(blockA.getInput('NAME'));
      });
      test('Remove Field', function() {
        var blockA = this.createBlock('empty_block');
        var input = blockA.appendDummyInput('NAME');
        input.appendField(new Blockly.FieldLabel('test'), 'FIELD');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        input.removeField('FIELD');
        assertCollapsed(blockA);
        var field = blockA.getField('FIELD');
        chai.assert.isNull(field);
      });
      test('Remove Icon', function() {
        var blockA = this.createBlock('empty_block');
        blockA.setCommentText('test');
        blockA.setCollapsed(true);
        assertCollapsed(blockA);
        blockA.setCommentText(null);
        assertCollapsed(blockA);
      });
    });
  });
  suite('toString', function() {
    setup(function() {
      this.chars = 30;
    });
    test ('Block Less Than Blockly.CollapsedChars', function() {
      var string = '##########';  // 10
      var expectedString = '##########';  // 10

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(string), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('Block Exactly Blockly.CollapsedChars', function() {
      var string = '##############################';  // 30
      var expectedString = '##############################';  // 30

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(string), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('Block Blockly.CollapsedChars Minus 1', function() {
      var string = '#############################';  // 29
      var expectedString = '#############################'; // 29

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(string), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('Block Blockly.CollapsedChars Plus 1', function() {
      var string = '###############################';  // 31
      var expectedString = '###########################...';  // 27...

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(string), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('First Row Less Than Blockly.CollapsedChars', function() {
      var stringA = '##########';  // 10
      var stringB = '******************************';  // 30
      var expectedString = '########## ****************...';  // 10 16...

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL')
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('First Row Exactly Blockly.CollapsedChars', function() {
      var stringA = '##############################';  // 30
      var stringB = '******************************';  // 30
      var expectedString = '###########################...';  // 27...

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL')
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('First Row Blockly.CollapsedChars Minus 1', function() {
      var stringA = '#############################';  // 29
      var stringB = '******************************';  // 30
      var expectedString = '###########################...';  // 27...

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL')
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('First Row Blockly.CollapsedChars Minus 2', function() {
      var stringA = '############################';  // 28
      var stringB = '******************************';  // 30
      var expectedString = '###########################...';  // 27...

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL')
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('First Row Blockly.CollapsedChars Minus 3', function() {
      var stringA = '###########################';  // 27
      var stringB = '******************************';  // 30
      var expectedString = '###########################...';  // 27...

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL')
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('First Row Blockly.CollapsedChars Plus 1', function() {
      var stringA = '###############################';  // 31
      var stringB = '******************************';  // 30
      var expectedString = '###########################...';  // 27...

      var blockA = this.createBlock('empty_block');
      blockA.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL')
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('Block Empty', function() {
      var expectedString = '???';
      var blockA = this.createBlock('empty_block');
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('All Children Less Than Blockly.CollapsedChars', function() {
      var stringA = '##########';  // 10
      var stringB = '**********';  // 10
      var expectedString = '########## **********'; // 10 10

      var blockA = this.createBlock('row_block');
      var blockB = this.createBlock('empty_block');
      blockA.getInput('INPUT')
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL');
      blockB.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      blockB.setOutput(true);
      blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('All Children Exactly Blockly.CollapsedChars', function() {
      var stringA = '###############';  // 15
      var stringB = '**************';  // 14
      var expectedString = '############### **************';  // 15 14

      var blockA = this.createBlock('row_block');
      var blockB = this.createBlock('empty_block');
      blockA.getInput('INPUT')
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL');
      blockB.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      blockB.setOutput(true);
      blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('All Children Blockly.CollapsedChars Minus 1', function() {
      var stringA = '###############';  // 15
      var stringB = '*************';  // 13
      var expectedString = '############### *************';  // 15 13

      var blockA = this.createBlock('row_block');
      var blockB = this.createBlock('empty_block');
      blockA.getInput('INPUT')
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL');
      blockB.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      blockB.setOutput(true);
      blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
    test('All Children Blockly.CollapsedChars Plus 1', function() {
      var stringA = '###############';  // 15
      var stringB = '***************';  // 15
      var expectedString = '############### ***********...';  // 15 11...

      var blockA = this.createBlock('row_block');
      var blockB = this.createBlock('empty_block');
      blockA.getInput('INPUT')
          .appendField(new Blockly.FieldLabel(stringA), 'LABEL');
      blockB.appendDummyInput()
          .appendField(new Blockly.FieldLabel(stringB), 'LABEL');
      blockB.setOutput(true);
      blockA.getInput('INPUT').connection.connect(blockB.outputConnection);
      chai.assert.equal(blockA.toString(this.chars), expectedString)
    });
  });
});
