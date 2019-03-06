// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.repltest;

import com.google.appinventor.components.runtime.File;
import com.google.appinventor.components.runtime.FileTest;
import com.google.appinventor.components.runtime.ReplFormTest;
import org.junit.Before;

/**
 * Tests for the File Component, but run as if it is in the REPL rather than
 * in a compiled application. See {@link FileTest} for the test definitions.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class FileReplTest extends FileTest {
  @Before
  public void setUp() {
    setUpAsRepl();
    file = new File(getForm());
    ReplFormTest.copyAssetToReplAssets(TARGET_FILE, TARGET_FILE);
  }
}
