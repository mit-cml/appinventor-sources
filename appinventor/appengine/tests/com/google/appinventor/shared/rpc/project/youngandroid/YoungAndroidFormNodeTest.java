// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project.youngandroid;

import junit.framework.TestCase;

/**
 * Tests for YoungAndroidFormNodeTest.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidFormNodeTest extends TestCase {
  public void testGetQualifiedName() throws Exception {
    assertEquals("appinventor.ai_joeuser.HelloPurr.Screen1",
        YoungAndroidFormNode.getQualifiedName("src/appinventor/ai_joeuser/HelloPurr/Screen1.scm"));

    // Try calling getQualifiedForm with a file id not beginning with "src/". It should fail.
    try {
      YoungAndroidFormNode.getQualifiedName("appinventor/ai_joeuser/HelloPurr/Screen1.scm");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }
    try {
      YoungAndroidFormNode.getQualifiedName("srcappinventor/ai_joeuser/HelloPurr/Screen1.scm");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }

    // Try calling getQualifiedForm with a non-form file id. It should fail.
    try {
      YoungAndroidFormNode.getQualifiedName("src/appinventor/ai_joeuser/HelloPurr/Screen1.blk");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }
}
