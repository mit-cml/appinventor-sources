// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info of a component stored in the server
 */
public class ComponentInfo implements IsSerializable {

  private long compId;
  private String authorId;
  private String name;
  private long version;

  private ComponentInfo() {
    // no-op
  }

  public ComponentInfo(long compId, String authorId, String name, long version) {
    this.compId = compId;
    this.authorId = authorId;
    this.name = name;
    this.version = version;
  }

  public long getId() {
    return compId;
  }

  public String getAuthorId() {
    return authorId;
  }

  public String getName() {
    return name;
  }

  public long getVersion() {
    return version;
  }
}
