// Copyright 2011-2013 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/appinventor-sources/master/mitlicense.txt

package com.google.appinventor.blocklyeditor;
import java.io.IOException;
import com.google.appinventor.blocklyeditor.BlocklyTestUtils;
import com.google.appinventor.common.testutils.TestUtils;
import junit.framework.TestCase;

/**
 * Tests the App Inventor Blockly blocks evaluation of various YAIL code.
 *
 * TODO(andrew.f.mckinney): More tests needed!
 *
 * @author andrew.f.mckinney@gmail.com (Andrew.F.McKinney)
 */


public class BlocklyCodeGeneratorTest extends TestCase {

  public static final String testpath = TestUtils.APP_INVENTOR_ROOT_DIR + "/blocklyeditor";

    // The following tests are unit tests for the Yail generation of individual blocks.
    //  See the comments in
    // /tests/com/google/appinventor/generators_unit/listsCreateWithTest.js
    // for an explanation of how to create these tests

  public void testListsCreateList() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/listsCreateWithTest.js");
    assertEquals("true", result.toString());
  }

  public void testListsAddItems() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/listsAddItemsTest.js");
    assertEquals("true", result.toString());
  }

  public void testListsSelectItem() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/listsSelectItemTest.js");
    assertEquals("true", result.toString());
  }

  public void testTextSplit() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/textSplitTest.js");
    assertEquals("true", result.toString());
  }

  public void testTextSplitAtFirst() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/textSplitAtFirstTest.js");
    assertEquals("true", result.toString());
  }

  public void testTextSplitAtAny() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/textSplitAtAnyTest.js");
    assertEquals("true", result.toString());
  }

  public void testTextSplitAtFirstOfAny() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/textSplitAtFirstOfAnyTest.js");
    assertEquals("true", result.toString());
  }

  public void testControlsEvalButIgnore() throws Exception {
    String result = BlocklyTestUtils.generatorTest(
        testpath + "/tests/com/google/appinventor/generators_unit/controlsEvalButIgnoreTest.js");
    assertEquals("true", result.toString());
  }



    // add more unit tests here


}

