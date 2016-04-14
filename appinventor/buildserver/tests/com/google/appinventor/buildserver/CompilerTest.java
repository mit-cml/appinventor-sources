// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.common.collect.Sets;

import junit.framework.TestCase;

import java.util.Map;
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

    compiler.generatePermissions();
    Map<String,Set<String>> permissions = compiler.getPermissions();
    assertEquals(0, permissions.size());

    Set<String> componentTypes = Sets.newHashSet("com.google.appinventor.components.runtime.LocationSensor");
    compiler = new Compiler(null, componentTypes, System.out, System.err, System.err, false, 2048, null);
    compiler.generatePermissions();
    permissions = compiler.getPermissions();
    Set<String> flatPermissions = Sets.newHashSet();
    for (Set<String> compPermissions : permissions.values()) {
      flatPermissions.addAll(compPermissions);
    }

    assertEquals(4, flatPermissions.size());
    assertTrue(flatPermissions.contains(
        "android.permission.ACCESS_FINE_LOCATION"));
    assertTrue(flatPermissions.contains(
        "android.permission.ACCESS_COARSE_LOCATION"));
    assertTrue(flatPermissions.contains(
        "android.permission.ACCESS_MOCK_LOCATION"));
    assertTrue(flatPermissions.contains(
        "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"));
  }

  public void testGenerateBroadcastReceiver() throws Exception {
    Set<String> componentTypes = Sets.newHashSet("com.google.appinventor.components.runtime.Texting");
    Compiler compiler = new Compiler(null, componentTypes, System.out, System.err, System.err, false, 2048, null);
    Set<String> classNames = compiler.generateBroadcastReceiver();
    assertEquals(1, classNames.size());
    assertTrue(classNames.contains("com.google.appinventor.components.runtime.util.SmsBroadcastReceiver,android.provider.Telephony.SMS_RECEIVED,com.google.android.apps.googlevoice.SMS_RECEIVED"));

    componentTypes = Sets.newHashSet("com.google.appinventor.components.runtime.Texting",
      "com.google.appinventor.components.runtime.Label");
    compiler = new Compiler(null, componentTypes, System.out, System.err, System.err, false, 2048, null);
    classNames = compiler.generateBroadcastReceiver();
    assertEquals(1, classNames.size());
  }
}
