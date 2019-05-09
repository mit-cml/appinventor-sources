// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.common.collect.Maps;
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
    Map<String, Set<String>> noComponentBlocks = Maps.newHashMap();
    Compiler compiler = new Compiler(null, noComponents, noComponentBlocks, System.out, System.err, System.err, false,
                                     false, false, 2048, null, null);

    compiler.generatePermissions();
    Map<String,Set<String>> permissions = compiler.getPermissions();
    assertEquals(0, permissions.size());

    Set<String> componentTypes = Sets.newHashSet("com.google.appinventor.components.runtime.LocationSensor");
    compiler = new Compiler(null, componentTypes, noComponentBlocks, System.out, System.err, System.err, false, false, false, 2048, null, null);
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
    String texting = "com.google.appinventor.components.runtime.Texting";
    String label = "com.google.appinventor.components.runtime.Label";
    
    Set<String> componentTypes = Sets.newHashSet(texting);
    Map<String, Set<String>> blocks = Maps.newHashMap();
    blocks.put("Texting", Sets.newHashSet("ReceivingEnabled", "GoogleVoiceEnabled"));
    Compiler compiler = new Compiler(null, componentTypes, blocks, System.out, System.err, System.err, false, false, false, 2048, null, null);
    compiler.generateBroadcastReceivers();
    Map<String, Set<String>> componentReceivers = compiler.getBroadcastReceivers();
    Set<String> receivers = Sets.newHashSet();
    for (Set<String> receiverSubset : componentReceivers.values()) {
      receivers.addAll(receiverSubset);
    }
    assertEquals(2, receivers.size());
    boolean hasTelephony = false, hasGoogleVoice = false;
    for (String receiverElementString : receivers) {
      assertTrue(receiverElementString.contains("com.google.appinventor.components.runtime.util.SmsBroadcastReceiver"));
      hasTelephony = hasTelephony || receiverElementString.contains("android.provider.Telephony.SMS_RECEIVED");
      hasGoogleVoice = hasGoogleVoice || receiverElementString.contains("com.google.android.apps.googlevoice.SMS_RECEIVED");
    }
    assertTrue(hasTelephony);
    assertTrue(hasGoogleVoice);

    componentTypes = Sets.newHashSet(texting, label);
    compiler = new Compiler(null, componentTypes, blocks, System.out, System.err, System.err, false, false, false, 2048, null, null);
    compiler.generateBroadcastReceivers();
    componentReceivers = compiler.getBroadcastReceivers();
    receivers.clear();
    for (Set<String> receiverSubset : componentReceivers.values()) {
      receivers.addAll(receiverSubset);
    }
    assertEquals(2, receivers.size());
    assertTrue(componentReceivers.get(label) == null);
  }
  
  public void testGenerateActivities() throws Exception {
    String barcodeScanner = "com.google.appinventor.components.runtime.BarcodeScanner";
    String listPicker = "com.google.appinventor.components.runtime.ListPicker";
    String twitter = "com.google.appinventor.components.runtime.Twitter";
    
    Set<String> componentTypes = Sets.newHashSet(barcodeScanner);
    Map<String, Set<String>> blocks = Maps.newHashMap();
    Compiler compiler = new Compiler(null, componentTypes, blocks, System.out, System.err, System.err, false, false, false, 2048, null, null);
    compiler.generateActivities();
    Map<String, Set<String>> componentActivities = compiler.getActivities();
    Set<String> activities = componentActivities.get(barcodeScanner);
    assertEquals(1, activities.size());
    String activityElementString = activities.iterator().next();
    assertTrue(activityElementString.contains("name=\"com.google.zxing.client.android.AppInvCaptureActivity\""));
    assertTrue(activityElementString.contains("screenOrientation=\"landscape\""));
    assertTrue(activityElementString.contains("stateNotNeeded=\"true\""));
    assertTrue(activityElementString.contains("configChanges=\"orientation|keyboardHidden\""));
    assertTrue(activityElementString.contains("theme=\"@android:style/Theme.NoTitleBar.Fullscreen\""));
    assertTrue(activityElementString.contains("windowSoftInputMode=\"stateAlwaysHidden\""));
  
    componentTypes = Sets.newHashSet(listPicker);
    compiler = new Compiler(null, componentTypes, blocks, System.out, System.err, System.err, false, false, false, 2048, null, null);
    compiler.generateActivities();
    componentActivities = compiler.getActivities();
    activities = componentActivities.get(listPicker);
    assertEquals(1, activities.size());
    activityElementString = activities.iterator().next();
    assertTrue(activityElementString.contains("name=\"com.google.appinventor.components.runtime.ListPickerActivity\""));
    assertTrue(activityElementString.contains("configChanges=\"orientation|keyboardHidden\""));
    assertTrue(activityElementString.contains("screenOrientation=\"behind\""));
  
    componentTypes = Sets.newHashSet(twitter);
    compiler = new Compiler(null, componentTypes, blocks, System.out, System.err, System.err, false, false, false, 2048, null, null);
    compiler.generateActivities();
    componentActivities = compiler.getActivities();
    activities = componentActivities.get(twitter);
    assertEquals(1, activities.size());
    activityElementString = activities.iterator().next();
    assertTrue(activityElementString.contains("name=\"com.google.appinventor.components.runtime.WebViewActivity\""));
    assertTrue(activityElementString.contains("configChanges=\"orientation|keyboardHidden\""));
    assertTrue(activityElementString.contains("screenOrientation=\"behind\""));
    // Finally, test for the name attribute of the <intent-filter>'s <action> subelement
    assertTrue(activityElementString.contains("name=\"android.intent.action.MAIN\""));
  }
}
