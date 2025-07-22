// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.util.AARLibraries;
import com.google.appinventor.buildserver.util.PermissionConstraint;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@BuildType(apk = true, aab = true)
public class ComponentInfo {
  private final ConcurrentMap<String, Set<String>> assetsNeeded;
  private final ConcurrentMap<String, Set<String>> activitiesNeeded;
  private final ConcurrentMap<String, Set<String>> metadataNeeded;
  private final ConcurrentMap<String, Set<String>> activityMetadataNeeded;
  private final ConcurrentMap<String, Set<String>> broadcastReceiversNeeded;
  private final ConcurrentMap<String, Set<String>> libsNeeded;
  private final ConcurrentMap<String, Set<String>> nativeLibsNeeded;
  private final ConcurrentMap<String, Set<String>> permissionsNeeded;
  /**
   * Maps types to permissions to permission constraints.
   */
  private final ConcurrentMap<String, Map<String, Set<PermissionConstraint<?>>>>
      permissionConstraintsNeeded;
  private final ConcurrentMap<String, Set<String>> minSdksNeeded;
  private final ConcurrentMap<String, Set<String>> componentBroadcastReceiver;
  private final ConcurrentMap<String, Set<String>> queriesNeeded;
  private final ConcurrentMap<String, Set<String>> servicesNeeded;
  private final ConcurrentMap<String, Set<String>> contentProvidersNeeded;
  private final ConcurrentMap<String, Set<String>> xmlsNeeded;

  private Set<String> uniqueLibsNeeded;
  private AARLibraries explodedAarLibs;

  /**
   * Construct a new, uninitialized ComponentInfo.
   */
  public ComponentInfo() {
    assetsNeeded = new ConcurrentHashMap<>();
    activitiesNeeded = new ConcurrentHashMap<>();
    metadataNeeded = new ConcurrentHashMap<>();
    activityMetadataNeeded = new ConcurrentHashMap<>();
    broadcastReceiversNeeded = new ConcurrentHashMap<>();
    libsNeeded = new ConcurrentHashMap<>();
    nativeLibsNeeded = new ConcurrentHashMap<>();
    permissionsNeeded = new ConcurrentHashMap<>();
    permissionConstraintsNeeded = new ConcurrentHashMap<>();
    minSdksNeeded = new ConcurrentHashMap<>();
    componentBroadcastReceiver = new ConcurrentHashMap<>();
    queriesNeeded = new ConcurrentHashMap<>();
    servicesNeeded = new ConcurrentHashMap<>();
    contentProvidersNeeded = new ConcurrentHashMap<>();
    xmlsNeeded = new ConcurrentHashMap<>();

    uniqueLibsNeeded = Sets.newHashSet();
  }

  public ConcurrentMap<String, Set<String>> getAssetsNeeded() {
    return assetsNeeded;
  }

  public ConcurrentMap<String, Set<String>> getActivitiesNeeded() {
    return activitiesNeeded;
  }

  public ConcurrentMap<String, Set<String>> getMetadataNeeded() {
    return metadataNeeded;
  }

  public ConcurrentMap<String, Set<String>> getActivityMetadataNeeded() {
    return activityMetadataNeeded;
  }

  public ConcurrentMap<String, Set<String>> getBroadcastReceiversNeeded() {
    return broadcastReceiversNeeded;
  }

  public ConcurrentMap<String, Set<String>> getLibsNeeded() {
    return libsNeeded;
  }

  public ConcurrentMap<String, Set<String>> getNativeLibsNeeded() {
    return nativeLibsNeeded;
  }

  public ConcurrentMap<String, Set<String>> getPermissionsNeeded() {
    return permissionsNeeded;
  }

  public ConcurrentMap<String, Map<String, Set<PermissionConstraint<?>>>>
      getPermissionConstraintsNeeded() {
    return permissionConstraintsNeeded;
  }

  public ConcurrentMap<String, Set<String>> getMinSdksNeeded() {
    return minSdksNeeded;
  }

  public Set<String> getUniqueLibsNeeded() {
    return uniqueLibsNeeded;
  }

  public void setUniqueLibsNeeded(Set<String> uniqueLibsNeeded) {
    this.uniqueLibsNeeded = uniqueLibsNeeded;
  }

  public AARLibraries getExplodedAarLibs() {
    return explodedAarLibs;
  }

  public void setExplodedAarLibs(AARLibraries explodedAarLibs) {
    this.explodedAarLibs = explodedAarLibs;
  }

  public ConcurrentMap<String, Set<String>> getComponentBroadcastReceiver() {
    return componentBroadcastReceiver;
  }

  public ConcurrentMap<String, Set<String>> getQueriesNeeded() {
    return queriesNeeded;
  }

  public ConcurrentMap<String, Set<String>> getServicesNeeded() {
    return servicesNeeded;
  }

  public ConcurrentMap<String, Set<String>> getContentProvidersNeeded() {
    return contentProvidersNeeded;
  }

  public ConcurrentMap<String, Set<String>> getXmlsNeeded() {
    return xmlsNeeded;
  }

  @Override
  public String toString() {
    return "JsonInfo{"
        + "assetsNeeded=" + assetsNeeded
        + ", activitiesNeeded=" + activitiesNeeded
        + ", broadcastReceiversNeeded=" + broadcastReceiversNeeded
        + ", libsNeeded=" + libsNeeded
        + ", nativeLibsNeeded=" + nativeLibsNeeded
        + ", permissionsNeeded=" + permissionsNeeded
        + ", minSdksNeeded=" + minSdksNeeded
        + ", componentBroadcastReceiver=" + componentBroadcastReceiver
        + ", uniqueLibsNeeded=" + uniqueLibsNeeded
        + ", xmlsNeeded=" + xmlsNeeded
        + '}';
  }
}
