// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.google.common.primitives.Longs;

/**
 * Component stored in the server
 */
public class Component implements IsSerializable {

  private String authorId;
  private String fullyQualifiedName;
  private String name;
  private long version;

  private static final String DELIM = "#DELIM#";

  public Component() {
    // no-op
  }

  public Component(String authorId, String fullyQualifiedName, long version) {
    set(authorId, fullyQualifiedName, version);
  }

  public String getAuthorId() {
    return authorId;
  }

  public String getFullyQualifiedName() {
    return fullyQualifiedName;
  }

  public String getName() {
    return name;
  }

  public long getVersion() {
    return version;
  }

  public void set(String authorId, String fullyQualifiedName, long version) {
    this.authorId = authorId;
    this.fullyQualifiedName = fullyQualifiedName;
    this.name = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf(".") + 1);
    this.version = version;
  }

  public static Component valueOf(String text) {
    String[] parts = text.split(DELIM);
    if (parts.length != 3) {
      throw new IllegalArgumentException();
    }

    String authorId = parts[0];
    String fullyQualifiedName = parts[1];
    Long version = Longs.tryParse(parts[2]);
    if (version == null) {
      throw new IllegalArgumentException();
    }

    return new Component(authorId, fullyQualifiedName, version);
  }

  @Override
  public String toString() {
    return getAuthorId() + DELIM + getFullyQualifiedName() + DELIM + getVersion();
  }
}
