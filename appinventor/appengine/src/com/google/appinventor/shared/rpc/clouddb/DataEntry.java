// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.clouddb;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data transfer object representing a single tag/value pair from a CloudDB project.
 * Values are stored as JSON strings exactly as they appear in Redis.
 */
public final class DataEntry implements IsSerializable {

  private String tag;
  private String value;

  /** Required no-arg constructor for GWT serialization. */
  public DataEntry() {
  }

  public DataEntry(String tag, String value) {
    this.tag = tag;
    this.value = value;
  }

  public String getTag() {
    return tag;
  }

  public String getValue() {
    return value;
  }
}
