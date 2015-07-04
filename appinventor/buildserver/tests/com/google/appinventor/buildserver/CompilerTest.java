// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.common.collect.Sets;

import junit.framework.TestCase;

import java.util.Set;

/**
 * Tests Compiler class.
 *
 */
public class CompilerTest extends TestCase {

  public void testGeneratePermissions() throws Exception {
    Set<String> noComponents = Sets.newHashSet();
    Compiler compiler = new Compiler(null, noComponents, System.out, System.err, System.err, false,
                                     2048, null);
    assertTrue("Permissions for no components not empty. (It should be empty!)",
        compiler.generatePermissions().isEmpty());

    Set<String> componentTypes = Sets.newHashSet("LocationSensor");
    compiler = new Compiler(null, componentTypes, System.out, System.err, System.err, false, 2048, null);
    Set<String> permissions = compiler.generatePermissions();
    assertEquals(4, permissions.size());
    assertTrue(permissions.contains(
        "android.permission.ACCESS_FINE_LOCATION"));
    assertTrue(permissions.contains(
        "android.permission.ACCESS_COARSE_LOCATION"));
    assertTrue(permissions.contains(
        "android.permission.ACCESS_MOCK_LOCATION"));
    assertTrue(permissions.contains(
        "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"));
  }
}
