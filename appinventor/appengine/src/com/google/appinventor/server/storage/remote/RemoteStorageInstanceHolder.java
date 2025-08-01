// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage.remote;

import com.google.appinventor.server.flags.Flag;
import com.google.common.annotations.VisibleForTesting;

import java.util.logging.Logger;


/**
 * Holds the singleton BuildOutputRemoteStorage subclass object. We introduce this class
 * so that we can switch out the underlying BuildOutputRemoteStorage subclass without changing
 * the references in the code to the INSTANCE.
 */
public class RemoteStorageInstanceHolder {
  private static final Logger LOG = Logger.getLogger(RemoteStorageInstanceHolder.class.getName());

  private static final Flag<String> PROVIDER_NAME = Flag.createFlag("remotestorage", null);

  private static Boolean IS_LOADED = false;
  private static RemoteStorage INSTANCE;

  private RemoteStorageInstanceHolder() {} // not to be instantiated

  public static RemoteStorage getInstance() {
    if (!IS_LOADED) {
      INSTANCE = createRemoteInstance();
      IS_LOADED = true;
    }

    return INSTANCE;
  }

  public static boolean isRemoteConfigured() {
    return getInstance() != null;
  }

  @VisibleForTesting
  public static void setInstance(RemoteStorage instance) {
    INSTANCE = instance;
  }

  private static RemoteStorage createRemoteInstance() {
    final String providerName = PROVIDER_NAME.get();
    if (providerName == null || providerName.isBlank()) {
      // If not configured, then use the default provider (aka none).
      return null;
    }

    if (providerName.equals("gcp")) {
      try {
        return new RemoteStorageProviderGCS();
      } catch (UnsupportedOperationException e) {
        LOG.severe("Could not initialize Remote GCP Storage in non-Production environment!");
        return null;
      }
    }

    if (providerName.equals("s3")) {
      return new RemoteStorageProviderS3();
    }

    throw new UnsupportedOperationException("Unknown remote storage provider: " + providerName);
  }
}
