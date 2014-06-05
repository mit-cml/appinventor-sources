/**
 * Author: Jos (josmasflores@gmail.com)
 */

// This could be read from a file.
var expected = "(call-yail-primitive atan2-degrees (*list-for-runtime* 1 1) '(number number) \"atan2\")";

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

// Open the actual page and load all the JavaScript in it
// if success is true, all went well
page.open('blocklyeditor/src/demos/yail/yail_testing_index.html', function(status) {
  //The evaluate function has arguments passed after the callback
  //in this case we are passing the expected value defined earlier
  if (status !== 'success') {
    console.log('load unsuccessful');
  }

  var passed = page.evaluate(function(){

    var expected = arguments[0];
    var atanTwo = new Blockly.Block.obtain(Blockly.mainWorkspace, 'math_atan2');
    var yailForBlock = Blockly.Yail.math_atan2.call(atanTwo);

    if (yailForBlock.length !== 2)
      return false;

    return yailForBlock[0] === expected && yailForBlock[1] === 0;
  }, expected);

  //This is the actual result of the test
  console.log(passed);

  //Exit the phantom process
  phantom.exit();
});
