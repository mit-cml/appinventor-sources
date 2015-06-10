// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

/**
 * The representation of database records
 * for .aix files uploaded to the author's directory
 */
public class ComponentRecord {

    private String name;
    private int version;

    public ComponentRecord(String name, int version) {
      this.name = name;
      this.version = version;
    }

    public String getName() {
      return name;
    }

    public int getVersion() {
      return version;
    }
  }