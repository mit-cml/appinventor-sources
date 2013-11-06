// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project.youngandroid;

import junit.framework.TestCase;

/**
 * Tests for YoungAndroidSourceNodeTest.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidSourceNodeTest extends TestCase {
  public void testGetQualifiedName() throws Exception {
    //
    assertEquals("appinventor.ai_joeuser.HelloPurr.Screen1",
        YoungAndroidSourceNode.getQualifiedName("src/appinventor/ai_joeuser/HelloPurr/Screen1.scm"));
    assertEquals("appinventor.ai_joeuser.HelloPurr.Screen1",
        YoungAndroidSourceNode.getQualifiedName("src/appinventor/ai_joeuser/HelloPurr/Screen1.blk"));

    // Try calling getQualifiedName with a file id not beginning with "src/". It should fail.
    try {
      YoungAndroidSourceNode.getQualifiedName("appinventor/ai_joeuser/HelloPurr/Screen1.scm");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }
    try {
      YoungAndroidSourceNode.getQualifiedName("srcappinventor/ai_joeuser/HelloPurr/Screen1.scm");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }

    // Try calling getQualifiedName with an invalid kind file. It should fail.
    try {
      YoungAndroidSourceNode.getQualifiedName("src/appinventor/ai_joeuser/HelloPurr/Screen1.foo");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }
}
