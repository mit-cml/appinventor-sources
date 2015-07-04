// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Unit test for "split at any" block Yail generator
 *
 * Author: Hal Abelson (hal@mit.edu)
 */

////////////////////////////////////////
// These four variables are all you need to define to create a test
////////////////////////////////////////

var expected =
   "     \
   (call-yail-primitive string-split-at-any        \
          (*list-for-runtime* \"\" 1)             \
          '(text list)                            \
           \"split at any\")                             \
     ";

var delayedGenerator = function () { return Blockly.Yail.text_split ; } ;

var blockName = 'text_split' ;

var doesReturn = true ;

var dropdownOp = 'SPLITATANY' ;

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

