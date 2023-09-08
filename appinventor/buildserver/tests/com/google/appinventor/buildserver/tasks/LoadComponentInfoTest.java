// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.appinventor.buildserver.Project;
import com.google.appinventor.buildserver.Reporter;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.tasks.common.LoadComponentInfo;
import com.google.appinventor.buildserver.tasks.common.ReadBuildInfo;
import com.google.appinventor.buildserver.util.ProjectUtils;
import com.google.appinventor.common.testutils.TestUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link LoadComponentInfo} class.
 */
public class LoadComponentInfoTest {
  private static final String HELLO_PURR_TEMPLATE =
      TestUtils.windowsToUnix(TestUtils.APP_INVENTOR_ROOT_DIR)
          + "/appengine/war/templates/HelloPurr/HelloPurr.zip";
  final ReadBuildInfo buildInfo = new ReadBuildInfo();
  static Project project;
  static File projectRoot;
  CompilerContext.Builder<?, ?> builder;

  @Before
  public void setUp() {
    builder = new CompilerContext.Builder<>(project, "apk")
        .withClass(AndroidCompilerContext.class)
        .withBlocks(Collections.<String, Set<String>>emptyMap())
        .withCache(null)
        .withCompanion(false)
        .withDangerousPermissions(false)
        .withEmulator(false)
        .withFormOrientations(Collections.<String, String>emptyMap())
        .withBlockPermissions(Collections.<String>emptySet())
        .withRam(2048)
        .withReporter(new Reporter(null))
        .withKeystore("test.keystore")
        .withTypes(Collections.<String>emptySet());
  }

  @BeforeClass
  public static void onlyOnce() throws IOException {
    projectRoot = ProjectUtils.createNewTempDir();
    ProjectUtils.extractProjectFiles(new ZipFile(HELLO_PURR_TEMPLATE), projectRoot);
    project = ProjectUtils.getProjectProperties(projectRoot);
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    FileUtils.deleteQuietly(new File(projectRoot.getCanonicalPath()));
  }

  @Test
  public void testGeneratePermissions() {
    CompilerContext<?> context = builder.build();
    LoadComponentInfo task = new LoadComponentInfo();
    buildInfo.execute(context);
    task.execute(context);

    Map<String,Set<String>> permissions = context.getComponentInfo().getPermissionsNeeded();
    assertEquals(0, permissions.size());

    Set<String> componentTypes = Sets.newHashSet(
        "com.google.appinventor.components.runtime.LocationSensor");
    context = builder.withTypes(componentTypes).build();
    task = new LoadComponentInfo();
    buildInfo.execute(context);
    task.execute(context);
    permissions = context.getComponentInfo().getPermissionsNeeded();
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

  @Test
  public void testGenerateBroadcastReceiver() {
    final String texting = "com.google.appinventor.components.runtime.Texting";
    final String label = "com.google.appinventor.components.runtime.Label";

    Set<String> componentTypes = Sets.newHashSet(texting);
    Map<String, Set<String>> blocks = Maps.newHashMap();
    blocks.put("Texting", Sets.newHashSet("ReceivingEnabled", "GoogleVoiceEnabled"));
    builder.withBlocks(blocks).withTypes(componentTypes);

    CompilerContext<?> context = builder.build();
    buildInfo.execute(context);
    new LoadComponentInfo().execute(context);

    Map<String, Set<String>> componentReceivers = context.getComponentInfo()
        .getBroadcastReceiversNeeded();
    Set<String> receivers = Sets.newHashSet();
    for (Set<String> receiverSubset : componentReceivers.values()) {
      receivers.addAll(receiverSubset);
    }
    assertEquals(2, receivers.size());
    boolean hasTelephony = false;
    boolean hasGoogleVoice = false;
    for (String receiverElementString : receivers) {
      assertTrue(receiverElementString.contains(
          "com.google.appinventor.components.runtime.util.SmsBroadcastReceiver"));
      hasTelephony = hasTelephony || receiverElementString.contains(
          "android.provider.Telephony.SMS_RECEIVED");
      hasGoogleVoice = hasGoogleVoice || receiverElementString.contains(
          "com.google.android.apps.googlevoice.SMS_RECEIVED");
    }
    assertTrue(hasTelephony);
    assertTrue(hasGoogleVoice);

    componentTypes = Sets.newHashSet(texting, label);
    context = builder.withTypes(componentTypes).build();
    buildInfo.execute(context);
    new LoadComponentInfo().execute(context);
    componentReceivers = context.getComponentInfo().getBroadcastReceiversNeeded();
    receivers.clear();
    for (Set<String> receiverSubset : componentReceivers.values()) {
      receivers.addAll(receiverSubset);
    }
    assertEquals(2, receivers.size());
    assertNull(componentReceivers.get(label));
  }

  @Test
  public void testGenerateActivities() {
    final String barcodeScanner = "com.google.appinventor.components.runtime.BarcodeScanner";
    final String listPicker = "com.google.appinventor.components.runtime.ListPicker";
    final String twitter = "com.google.appinventor.components.runtime.Twitter";

    Set<String> componentTypes = Sets.newHashSet(barcodeScanner);
    Map<String, Set<String>> blocks = Maps.newHashMap();

    builder.withBlocks(blocks).withTypes(componentTypes);

    CompilerContext<?> context = builder.build();

    buildInfo.execute(context);
    new LoadComponentInfo().execute(context);

    Map<String, Set<String>> componentActivities = context.getComponentInfo().getActivitiesNeeded();
    Set<String> activities = componentActivities.get(barcodeScanner);
    assertEquals(1, activities.size());
    String xml = activities.iterator().next();
    assertTrue(xml.contains("name=\"com.google.zxing.client.android.AppInvCaptureActivity\""));
    assertTrue(xml.contains("screenOrientation=\"landscape\""));
    assertTrue(xml.contains("stateNotNeeded=\"true\""));
    assertTrue(xml.contains("configChanges=\"orientation|keyboardHidden\""));
    assertTrue(xml.contains("theme=\"@android:style/Theme.NoTitleBar.Fullscreen\""));
    assertTrue(xml.contains("windowSoftInputMode=\"stateAlwaysHidden\""));

    componentTypes = Sets.newHashSet(listPicker);
    context = builder.withTypes(componentTypes).build();

    buildInfo.execute(context);
    new LoadComponentInfo().execute(context);

    componentActivities = context.getComponentInfo().getActivitiesNeeded();
    activities = componentActivities.get(listPicker);
    assertEquals(1, activities.size());
    xml = activities.iterator().next();
    assertTrue(xml.contains(
        "name=\"com.google.appinventor.components.runtime.ListPickerActivity\""));
    assertTrue(xml.contains("configChanges=\"orientation|keyboardHidden\""));
    assertTrue(xml.contains("screenOrientation=\"behind\""));

    componentTypes = Sets.newHashSet(twitter);
    context = builder.withTypes(componentTypes).build();


    buildInfo.execute(context);
    new LoadComponentInfo().execute(context);

    componentActivities = context.getComponentInfo().getActivitiesNeeded();
    activities = componentActivities.get(twitter);
    assertEquals(1, activities.size());
    xml = activities.iterator().next();
    assertTrue(xml.contains("name=\"com.google.appinventor.components.runtime.WebViewActivity\""));
    assertTrue(xml.contains("configChanges=\"orientation|keyboardHidden\""));
    assertTrue(xml.contains("screenOrientation=\"behind\""));
    // Finally, test for the name attribute of the <intent-filter>'s <action> subelement
    assertTrue(xml.contains("name=\"android.intent.action.MAIN\""));
  }
}
