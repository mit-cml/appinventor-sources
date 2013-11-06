// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;

/**
 * Holds the singleton StorageIo subclass object. We introduce this class
 * so that we can switch out the underlying StorageIo subclass without changing
 * the references in the code to the INSTANCE.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class StorageIoInstanceHolder {
  public static final StorageIo INSTANCE = new ObjectifyStorageIo();
  
  private StorageIoInstanceHolder() {} // not to be instantiated
    
}
