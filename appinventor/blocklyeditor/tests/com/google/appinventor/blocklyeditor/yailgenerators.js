// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Unit test for "make a list" block Yail generator
 *
 * Author: Hal Abelson (hal@mit.edu)
 */

// This file runs a unit test for block Yail generation.  It generates the
// Yail for a block and checks wheither that contains the expected Yail
// (modulo whitespace).   This file illustrates how to test the "make a list"
// block (in the verison where there are no arguments).  Use this file (minus the
// comments), as a template for creating other generator unit tests.

// You can insert the test into
// blocklyeditor/tests/com/google/appinventor/blocklyeditor/BlocklyCodeGeneratorTestBuilder.java
// to include it in the suite of unit tests.

// You can also run this single test from the command line in the
// appinventor directory by executing the command
//     phantomJS blocklyeditor/tests/com/google/appinventor/generators_unit/yailgenerators.js

// To create a generator test, you need to define four values:

// (1) expected: the correct yail string for the block with all slots empty
// delayedGenerator: The block Yail generation function (delayed)
// blockName: name of the block
// doesReturn: true if the block returns a value, false otherwise

// (2) We pass the generator function delayed, because the symbol Blockly
// is not defined when the required page is loaded, but it _will_ be defined after
// load, which is why we can force the value inside page.evaluate
// evaluation function.  See the definition of generator_test_main_routine.js

// To find the correct value of expected, open the blocks pane, drag out the block
// and hightlight it (keep the sockets empty), then start the debugger and run in the console:

// bs = Blocklies['5629499534213120_Screen1'];      // or whatever the right index is
// bs.Yail.lists_create_with.call(bs.selected);

// Alternatively, you can get the array of blocks on the screen with
// Blocklies["5066549580791808_Screen1"].mainWorkspace.getTopBlocks();

// Get the appropraite block, and then run
// AI.Yail.BlocktoCode1(block)


// (3) Whether or not the block returns a value

// (4) If the block uses a dropdown to specify the operator,
// pass in the tag for that operator (as defined by the generator).
// If no dropdown, pass in false

////////////////////////////////////////
// These four variables are all you need to define to create a test
////////////////////////////////////////

suite('Blockly Code Generator Tests', function() {
  setup(function() {
    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));
  })
  suite('Control Blocks', function() {
    test('controls_eval_but_ignore', function() {
      let expected = "(begin #f \"ignored\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('controls_eval_but_ignore');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.typeOf(yailForBlock, 'string');
      chai.assert.equal(yailForBlock, expected);
    })
  })
  suite('Math Blocks', function() {
    test('math_atan2', function() {
      let expected = "(call-yail-primitive atan2-degrees (*list-for-runtime* 1 1) '(number number) \"atan2\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('math_atan2');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
  })
  suite('List Blocks', function() {
    test('lists_create_with', function() {
      let expected = "(call-yail-primitive make-yail-list (*list-for-runtime* ) '() \"make a list\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('lists_create_with');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
    test('lists_add_items', function() {
      let expected =    "(call-yail-primitive yail-list-add-to-list! (*list-for-runtime* (call-yail-primitive make-yail-list (*list-for-runtime* ) '() \"make a list\") #f ) '(list any ) \"add items to list\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('lists_add_items');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.typeOf(yailForBlock, 'string');
      chai.assert.equal(yailForBlock, expected);
    })
    test('lists_join_with_separator', function() {
      let expected = "(call-yail-primitive yail-list-join-with-separator (*list-for-runtime* (call-yail-primitive make-yail-list (*list-for-runtime* ) '() \"make a list\") \"\") '(list text) \"join with separator\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('lists_join_with_separator');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
    test('lists_select_item', function() {
      let expected = "(call-yail-primitive yail-list-get-item (*list-for-runtime* (call-yail-primitive make-yail-list (*list-for-runtime* ) '() \"make a list\") 1) '(list number) \"select list item\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('lists_select_item');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
  })
  suite('Text Blocks', function() {
    test('text_split:SPLITATANY', function() {
      let expected = "(call-yail-primitive string-split-at-any (*list-for-runtime* \"\" 1) '(text list) \"split at any\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('text_split');
      testBlock.setFieldValue('SPLITATANY', 'OP');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
    test('text_split:SPLITATFIRSTOFANY', function() {
      let expected = "(call-yail-primitive string-split-at-first-of-any (*list-for-runtime* \"\" 1) '(text list) \"split at first of any\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('text_split');
      testBlock.setFieldValue('SPLITATFIRSTOFANY', 'OP');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
    test('text_split:SPLITATFIRST', function() {
      let expected = "(call-yail-primitive string-split-at-first (*list-for-runtime* \"\" 1) '(text text) \"split at first\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('text_split');
      testBlock.setFieldValue('SPLITATFIRST', 'OP');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
    test('text_split:SPLIT', function() {
      let expected = "(call-yail-primitive string-split (*list-for-runtime* \"\" 1) '(text text) \"split\")";
      let testBlock = Blockly.common.getMainWorkspace().newBlock('text_split');
      testBlock.setFieldValue('SPLIT', 'OP');
      let yailForBlock = AI.Yail[testBlock.type].call(testBlock);
      chai.assert.lengthOf(yailForBlock, 2);
      chai.assert.equal(yailForBlock[0], expected);
    })
  })
})
