// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.google.appinventor.server.flags.Flag;

/**
 * Holds the singleton StorageIo subclass object. We introduce this class
 * so that we can switch out the underlying StorageIo subclass without changing
 * the references in the code to the INSTANCE.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class StorageIoInstanceHolder {
  private static final Flag<String> storageBackend = Flag.createFlag("storage.backend", null);
  public static final StorageIo INSTANCE;

  static {
    String backend = storageBackend.get();

    if (backend == null || backend.equals("gcs")) {
      INSTANCE = new ObjectifyStorageIo();
    } else if (backend.equals("postgresql")) {
      INSTANCE = new PostgreSQLStorageIo();
    } else {
      throw new IllegalStateException("This storage.backend value \"" + backend + "\" is not supported");
    }
  }

  private StorageIoInstanceHolder() {  // not to be instantiated
    throw new IllegalStateException("StorageIoInstanceHolder is not meant to be instanced");
  }

}
