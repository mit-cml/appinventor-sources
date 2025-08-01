// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage.remote;

import com.google.appinventor.server.flags.Flag;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Holds the singleton BuildOutputRemoteStorage subclass object. We will actually have
 *   multiple instances depending on the particular usage. This allows to for example
 *   store build outputs in a server/provider closer to the buildserver, but project
 *   exports in Google Cloud, for example.
 */
public class RemoteStorageInstanceHolder {
  public enum Usage {
    BUILD("build"),  // APK/AAB Artifacts
    EXPORT("export"),  // AIA Downloads
    ;

    private final String flagName;

    Usage(final String flagName) {
      this.flagName = flagName;
    }

    public String getFlagName() {
      return flagName;
    }
  }

  private static final Logger LOG = Logger.getLogger(RemoteStorageInstanceHolder.class.getName());

  // Here we store each provider for each specific usage
  private static final Map<Usage, RemoteStorage> INSTANCES_USAGE_MAP = new HashMap<>();

  private RemoteStorageInstanceHolder() {} // not to be instantiated

  public static RemoteStorage getInstance(final Usage usage) {
    if (INSTANCES_USAGE_MAP.containsKey(usage)) {
      return INSTANCES_USAGE_MAP.get(usage);
    }

    RemoteStorage instance = createRemoteInstance(usage);
    INSTANCES_USAGE_MAP.put(usage, instance);

    return instance;
  }

  public static boolean isRemoteConfigured(final Usage usage) {
    return getInstance(usage) != null;
  }

  private static RemoteStorage createRemoteInstance(final Usage usage) {
    final String flagName = "remotestorage." + usage.getFlagName();
    final String providerName = Flag.createFlag(flagName, null).get();
    if (providerName == null || providerName.isBlank()) {
      // If not configured, then use the default provider (aka none).
      return null;
    }

    if (providerName.equals("gcp")) {
      try {
        return RemoteStorageProviderGCS.getInstance();
      } catch (UnsupportedOperationException e) {
        LOG.severe("Could not initialize Remote GCP Storage in non-Production environment!");
        return null;
      }
    }

    if (providerName.equals("s3")) {
      return RemoteStorageProviderS3.getInstance();
    }

    throw new UnsupportedOperationException("Unknown remote storage provider: " + providerName);
  }
}
