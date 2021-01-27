package com.google.appinventor.buildserver.context;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.util.AARLibraries;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@BuildType(apk = true, aab = true)
public class ComponentInfo {
  private final ConcurrentMap<String, Set<String>> assetsNeeded;
  private final ConcurrentMap<String, Set<String>> activitiesNeeded;
  private final ConcurrentMap<String, Set<String>> broadcastReceiversNeeded;
  private final ConcurrentMap<String, Set<String>> libsNeeded;
  private final ConcurrentMap<String, Set<String>> nativeLibsNeeded;
  private final ConcurrentMap<String, Set<String>> permissionsNeeded;
  private final ConcurrentMap<String, Set<String>> minSdksNeeded;
  private final ConcurrentMap<String, Set<String>> componentBroadcastReceiver;

  private Set<String> uniqueLibsNeeded;
  private AARLibraries explodedAarLibs;

  public ComponentInfo() {
    assetsNeeded = new ConcurrentHashMap<>();
    activitiesNeeded = new ConcurrentHashMap<>();
    broadcastReceiversNeeded = new ConcurrentHashMap<>();
    libsNeeded = new ConcurrentHashMap<>();
    nativeLibsNeeded = new ConcurrentHashMap<>();
    permissionsNeeded = new ConcurrentHashMap<>();
    minSdksNeeded = new ConcurrentHashMap<>();
    componentBroadcastReceiver = new ConcurrentHashMap<>();

    uniqueLibsNeeded = Sets.newHashSet();
  }

  public ConcurrentMap<String, Set<String>> getAssetsNeeded() {
    return assetsNeeded;
  }

  public ConcurrentMap<String, Set<String>> getActivitiesNeeded() {
    return activitiesNeeded;
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

  @Override
  public String toString() {
    return "JsonInfo{" +
        "assetsNeeded=" + assetsNeeded +
        ", activitiesNeeded=" + activitiesNeeded +
        ", broadcastReceiversNeeded=" + broadcastReceiversNeeded +
        ", libsNeeded=" + libsNeeded +
        ", nativeLibsNeeded=" + nativeLibsNeeded +
        ", permissionsNeeded=" + permissionsNeeded +
        ", minSdksNeeded=" + minSdksNeeded +
        ", componentBroadcastReceiver=" + componentBroadcastReceiver +
        ", uniqueLibsNeeded=" + uniqueLibsNeeded +
        '}';
  }
}
