// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.blocklyeditor;

import java.io.IOException;

import com.google.appinventor.common.testutils.TestUtils;

import junit.framework.TestCase;

/**
 * Tests the App Inventor Blockly blocks evaluation of various YAIL code.
 * 
 * TODO(andrew.f.mckinney): More tests needed!
 * 
 * @author andrew.f.mckinney@gmail.com (Andrew.F.McKinney)
 */

public class BlocklyEvalTest extends TestCase {
  
  public static final String testpath = TestUtils.APP_INVENTOR_ROOT_DIR + "/blocklyeditor";

  public void testButtonClick() throws Exception {

    String[] params = { "phantomjs", testpath + "/tests/com/google/appinventor/blocklyeditor/buttonClickTest.js" };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }
  
  public void testIfThenBlock() throws Exception {

    String[] params = { "phantomjs", testpath + "/tests/com/google/appinventor/blocklyeditor/ifThenBlockTest.js" };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }
}
