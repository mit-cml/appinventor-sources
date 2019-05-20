// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.google.common.annotations.VisibleForTesting;

/**
 * Holds the singleton StorageIo subclass object. We introduce this class
 * so that we can switch out the underlying StorageIo subclass without changing
 * the references in the code to the INSTANCE.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class StorageIoInstanceHolder {
  private static StorageIo INSTANCE;
  
  private StorageIoInstanceHolder() {} // not to be instantiated

  public static StorageIo getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ObjectifyStorageIo();
    }
    return INSTANCE;
  }

  @VisibleForTesting
  public static void setInstance(StorageIo instance) {
    INSTANCE = instance;
  }
}
