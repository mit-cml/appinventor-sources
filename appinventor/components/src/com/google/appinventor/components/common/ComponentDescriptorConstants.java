// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

/**
 * Contains constants related to the description of Simple components.
 *
 */
public final class ComponentDescriptorConstants {
  private ComponentDescriptorConstants() {
    // nothing
  }
  // shared constants between ComponentListGenerator.java and Compiler.java
  public static final String ARMEABI_V7A_SUFFIX = "-v7a";
  public static final String ARM64_V8A_SUFFIX = "-v8a";
  public static final String X86_64_SUFFIX = "-x8a";
  public static final String ASSET_DIRECTORY = "component";
  public static final String ASSETS_TARGET = "assets";
  public static final String ACTIVITIES_TARGET = "activities";
  public static final String METADATA_TARGET = "metadata";
  public static final String ACTIVITY_METADATA_TARGET = "activityMetadata";
  public static final String LIBRARIES_TARGET = "libraries";
  public static final String NATIVE_TARGET = "native";
  public static final String PERMISSION_CONSTRAINTS_TARGET = "permissionConstraints";
  public static final String PERMISSIONS_TARGET = "permissions";
  public static final String BROADCAST_RECEIVERS_TARGET = "broadcastReceivers";
  public static final String QUERIES_TARGET = "queries";
  public static final String SERVICES_TARGET = "services";
  public static final String CONTENT_PROVIDERS_TARGET = "contentProviders";
  public static final String ANDROIDMINSDK_TARGET = "androidMinSdk";
  public static final String CONDITIONALS_TARGET = "conditionals";

  // TODO(Will): Remove the following target once the deprecated
  //             @SimpleBroadcastReceiver annotation is removed. It should
  //             should remain for the time being because otherwise we'll break
  //             extensions currently using @SimpleBroadcastReceiver.
  public static final String BROADCAST_RECEIVER_TARGET = "broadcastReceiver";
}
