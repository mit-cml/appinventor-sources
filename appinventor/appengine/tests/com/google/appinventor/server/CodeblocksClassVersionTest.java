// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;

import junit.framework.TestCase;
import java.io.DataInputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Checks that all classes in BlocksEditor.jar are compatible with running on JDK 1.5.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class CodeblocksClassVersionTest extends TestCase {
  private static final int JDK5_MAJOR_CLASS_VERSION = 49;

  public void testClassVersions() throws Exception {
    String codeblocksJar = TestUtils.APP_INVENTOR_ROOT_DIR +
        "/build/blockseditor/BlocksEditor.jar";
    int countClassesChecked = 0;
    JarFile jarFile = new JarFile(codeblocksJar);
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        String filename = entry.getName();
        if (filename.endsWith(".class")) {
          DataInputStream in = new DataInputStream(jarFile.getInputStream(entry));
          try {
            checkClassVersion(filename, in);
            countClassesChecked++;
          } finally {
            in.close();
          }
        }
      }
    }
    assertTrue(countClassesChecked > 0);
  }

  private static void checkClassVersion(String filename, DataInputStream in)
      throws Exception {
    int magic = in.readInt();
    assertEquals(filename + " is not a valid class file.", 0xCAFEBABE, magic);

    int minor = in.readUnsignedShort();
    int major = in.readUnsignedShort();
    assertTrue("The major class version number for " + filename + " is " +
        major + ", which indicates that it will not run on JDK1.5.",
        major <= JDK5_MAJOR_CLASS_VERSION);
  }
}
