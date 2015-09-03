// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.blocklyeditor;
import java.io.IOException;
import com.google.appinventor.blocklyeditor.BlocklyTestUtils;
import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.components.common.YaVersion;
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

  public void testBackgroundColor() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/backgroundColorTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testMoleMash() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/moleMashTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testPaintPot() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/paintPotTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testHelloPurr() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/helloPurrTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testMakeQuiz() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/makeQuizTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testPictureCycle() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/pictureCycleTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testSensor() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/sensorTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testClock() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/clockTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testCamcorder() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/camcorderTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testCopyCat() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/copyCatTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testProductLookup() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/productLookupTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testfactorial() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/factorialTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

  public void testunderscore() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/underscoreTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }


  public void testmathsconvert() throws Exception {

    String[] params =
      { "phantomjs",
        testpath + "/tests/com/google/appinventor/blocklyeditor/mathsconvertTest.js",
        Integer.toString(YaVersion.BLOCKS_LANGUAGE_VERSION), Integer.toString(YaVersion.YOUNG_ANDROID_VERSION) };
    String result = "";

    try {
      result = CodeBlocksProcessHelper.exec(params, true).trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertEquals("true", result.toString());
  }

}

