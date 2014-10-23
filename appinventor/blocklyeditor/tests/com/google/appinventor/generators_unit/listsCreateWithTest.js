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
// blocklyeditor/tests/com/google/appinventor/blocklyeditor/BlocklyCodeGeneratorTest.java
// to include it in the suite of unit tests.

// You can also run this single test from the command line in the
// appinventor directory by executing the command
//     phantomJS blocklyeditor/tests/com/google/appinventor/generators_unit/listsCreateWithTest.js

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
// Blockly.Yail.BlocktoCode1(block)


// (3) Whether or not the block returns a value

// (4) If the block uses a dropdown to specify the operator,
// pass in the tag for that operator (as defined by the generator).
// If no dropdown, pass in false

////////////////////////////////////////
// These four variables are all you need to define to create a test
////////////////////////////////////////

var expected =
  "(call-yail-primitive make-yail-list (*list-for-runtime* ) '() \"make a list\")";

var delayedGenerator = function () { return Blockly.Yail.lists_create_with ; } ;

var blockName = 'lists_create_with';

var doesReturn = true;

var dropdownOp = false;




////////////////////////////////////////
// The rest of this page is common to all tests.
////////////////////////////////////////

// PhantomJS page object to open and load an URL - unfortunately we need to fully load Blockly
var page = require('webpage').create();
// Some debugging from PhantomJS
page.onConsoleMessage = function (msg) { console.log(msg); };
page.onError = function (msg, trace) {
  console.log(msg);
  trace.forEach(function(item) {
    console.log('  ', item.file, ':', item.line);
  });
};

var mainTest = require('./generator_test_mainRoutine.js');
mainTest.execute();

