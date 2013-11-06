// Copyright 2011-2013 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/appinventor-sources/master/mitlicense.txt

package com.google.appinventor.blocklyeditor;

import java.io.IOException;


/**
 * Utilities for Blockly tests in J-unit framework
 *
 * @author hal@mit.edu (Hal Abelson)
 */

public class BlocklyTestUtils {
// runs a unit test for a block code generator.
// testFile is s Javascript file that runs the test, using PhantomJS
  public static String generatorTest(String testFile) throws Exception {

    String[] params = { "phantomjs", testFile};
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

}

