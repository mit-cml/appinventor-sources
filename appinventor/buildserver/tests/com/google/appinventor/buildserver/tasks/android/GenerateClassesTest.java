// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the {@link GenerateClasses} class.
 */
public class GenerateClassesTest {
  private static final String PRELUDE = "#|\n$Source $Yail\n|#\n\n";

  @Test
  public void testRenameFormClass() {
    assertEquals(PRELUDE + "(define-form com.example.purr.Screen1 Screen1 #t)\n"
            + "(def appinventor.ai_ram8647.HelloPurr.Screen1 \"text\")\n",
        GenerateClasses.renameFormClass(PRELUDE
                + "(define-form appinventor.ai_ram8647.HelloPurr.Screen1 Screen1 #t)\n"
                + "(def appinventor.ai_ram8647.HelloPurr.Screen1 \"text\")\n",
            "appinventor.ai_ram8647.HelloPurr.Screen1", "com.example.purr.Screen1"));
  }

  @Test
  public void testRenameFormClassWithoutHeader() {
    String yail = PRELUDE + "(define-form appinventor.ai_ram8647.HelloPurr.Screen10 Screen10)\n";
    assertEquals(yail, GenerateClasses.renameFormClass(yail,
        "appinventor.ai_ram8647.HelloPurr.Screen1", "com.example.purr.Screen1"));
  }
}
