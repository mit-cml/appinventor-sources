// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Main function for creating usit tests for yail generators
 *
 * Author: Hal Abelson (hal@mit.edu)
 */

// This is the main function for execution of generator tests; it can be 'require'd in other tests
// by using exports to make it available there.
// This function should be called in a context in which the arguments array exists.

exports.execute =  function (){

  // Open the actual page and load all the JavaScript in it
  // if success is true, all went well
  page.open('blocklyeditor/src/demos/yail/yail_testing_index.html', function(status) {
    if (status !== 'success') {
      console.log('load of yail_testing_index.html unsuccessful');
      phantom.exit();
      return false;
    }

    var passed = page.evaluate(
        function(){
          var expected = arguments[0];
          var generator = arguments[1]();
          var blockName = arguments[2];
          var doesReturn = arguments[3];
          // if dropdownOp is false, we test the simple block
          // otherwise set the block dropdown for the code generator to use
          var dropdownOp = arguments[4];

          var generatedYail = "";
          var yailForBlock;
          var testBlock = new Blockly.Block.obtain(Blockly.mainWorkspace, blockName);
          if (dropdownOp) {testBlock.setFieldValue(dropdownOp, 'OP');}
          yailForBlock = generator.call(testBlock);
          if (doesReturn) {
            if ((yailForBlock.length !== 2) || (yailForBlock[1] !== Blockly.Yail.ORDER_ATOMIC) ) {
              return false;
            } else {
              generatedYail = yailForBlock[0];
            }
          } else {
            generatedYail = yailForBlock;
          }
          // Uncomment these for debugging "expected" in making new tests
          // console.log(generatedYail);
          //  console.log(expected);
          return doesContain(generatedYail, expected);
          },
        expected,
        delayedGenerator,
        blockName,
        doesReturn,
        dropdownOp);

    //This is the actual result of the test
    console.log(passed);

    //Exit the phantom process
    phantom.exit();
  });
};
