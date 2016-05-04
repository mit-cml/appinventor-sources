// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.google.common.primitives.Longs;

/**
 * Component stored in the server
 */
public class Component implements IsSerializable {
  private long id;
  private String authorId;
  private String fullyQualifiedName;
  private String name;
  private long version;

  private static final String DELIM = "#DELIM#";

  public Component() {
    // no-op
  }

  public Component(long id, String authorId, String fullyQualifiedName, long version) {
    set(id, authorId, fullyQualifiedName, version);
  }

  public long getId() {
    return id;
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

  public void set(long id, String authorId, String fullyQualifiedName, long version) {
    this.id = id;
    this.authorId = authorId;
    this.fullyQualifiedName = fullyQualifiedName;
    this.name = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf(".") + 1);
    this.version = version;
  }

  public static Component valueOf(String text) {
    String[] parts = text.split(DELIM);
    if (parts.length != 4) {
      throw new IllegalArgumentException("text should have 4 parts.");
    }

    Long id = Longs.tryParse(parts[0]);
    String authorId = parts[1];
    String fullyQualifiedName = parts[2];
    Long version = Longs.tryParse(parts[3]);
    if (id == null) {
      throw new IllegalArgumentException("id is not parsable.");
    }
    if (version == null) {
      throw new IllegalArgumentException("version is not parsable.");
    }

    return new Component(id, authorId, fullyQualifiedName, version);
  }

  @Override
  public String toString() {
    return getId() + DELIM + getAuthorId() + DELIM + getFullyQualifiedName() +
        DELIM + getVersion();
  }
}
