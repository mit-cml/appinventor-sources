// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.blocklyeditor;

import static com.google.appinventor.components.common.YaVersion.BLOCKS_LANGUAGE_VERSION;
import static com.google.appinventor.components.common.YaVersion.YOUNG_ANDROID_VERSION;

import com.google.appinventor.common.testutils.TestUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests the App Inventor Blockly blocks evaluation of various YAIL code.
 *
 * TODO(andrew.f.mckinney): More tests needed!
 *
 * @author andrew.f.mckinney@gmail.com (Andrew.F.McKinney)
 */


public class BlocklyEvalTestBuilder {
  private static final Logger LOG = Logger.getLogger(BlocklyEvalTestBuilder.class.getName());

  public static final String testpath = TestUtils.APP_INVENTOR_ROOT_DIR
      + "/blocklyeditor/tests/com/google/appinventor/blocklyeditor/data";
  public static final String BUILT_TEST_PATH = TestUtils.APP_INVENTOR_ROOT_DIR
      + "/blocklyeditor/build/javascript/";

  public static final String OUTER_TEMPLATE_JS = "suite('Blockly Editor Tests', function() {\n"
      + "  setup(function() {\n"
      + "    Blockly.common.setMainWorkspace(Blockly.BlocklyEditor.create(document.body, '', /*readonly*/ false, /*rtl*/ false));\n"
      + "    initComponentTypes();\n"
      + "    processVersion(%d, %d);\n"
      + "  });\n";

  public static final String TEMPLATE_JS = "  test('%s Test', function() {\n"
      + "    var formJson = `%s`;\n"
      + "    var blocks = `%s`;\n"
      + "    var expected = `%s`;\n"
      + "    processForm(formJson);\n"
      + "    processBlocks(formJson, blocks);\n"
      + "    var newBlocks = toAppYail();\n"
      + "    chai.assert.isTrue(doTheyMatch(expected, newBlocks));\n"
      + "  });\n";

  private static final StringBuilder sb = new StringBuilder();

  public static void main(String[] args) {
    sb.append(String.format(OUTER_TEMPLATE_JS, BLOCKS_LANGUAGE_VERSION, YOUNG_ANDROID_VERSION));
    generateTest("backgroundColor");
    generateTest("camcorder");
    generateTest("clock");
    generateTest("copyCat");
    generateTest("factorial");
    generateTest("helloPurr");
    generateTest("makeQuiz");
    generateTest("mathsconvert");
    generateTest("moleMash");
    generateTest("paintPot");
    generateTest("pictureCycle");
    generateTest("productLookup");
    generateTest("sensor");
    generateTest("underscore");
    commit();
  }

  private static void generateTest(String testDir) {
    File dir = new File(testpath, testDir);
    String[] files = dir.list();
    if (files == null) {
      throw new IllegalStateException("No files found in " + dir);
    }
    String formJson = null, blocks = null, yail = null;
    for (String file : files) {
      if (file.endsWith(".scm")) {
        formJson = readForm(new File(dir, file));
      } else if (file.endsWith(".bky")) {
        blocks = readFile(new File(dir, file));
      } else if (file.endsWith(".yail")) {
        yail = readFile(new File(dir, file)).replaceAll("\\\\n", "\\\\\\\\u000a");
      }
    }
    sb.append(String.format(TEMPLATE_JS, testDir, formJson, blocks, yail));
  }

  private static String readFile(File file) {
    try (FileReader in = new FileReader(file)) {
      return IOUtils.toString(in);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Error reading file", e);
    }
    throw new IllegalStateException("Missing file in test harness. Aborting");
  }

  private static String readForm(File file) {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("{")) {
          return line;
        }
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Error reading form JSON", e);
    }
    // We should only get here if there is an IOException or the file doesn't contain JSON.
    throw new IllegalStateException("Bad SCM file in tests. Aborting.");
  }

  private static void commit() {
    sb.append("});\n");
    try (PrintStream out = new PrintStream(new File(BUILT_TEST_PATH, "BlocklyEvalTest.js"))) {
      out.print(sb);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Error writing test file", e);
    }
  }
}

