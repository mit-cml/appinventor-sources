// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Data Transfer Object representing user data.
 *
 */
public class Config implements IsSerializable, Serializable {
  // Unique identifier for the user
  private User user;

  private String rendezvousServer = null;

  public Config() {
  }

  public User getUser() {
    return this.user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getRendezvousServer() {
    return this.rendezvousServer; // might be null
  }

  public void setRendezvousServer(String value) {
    this.rendezvousServer = value;
  }

}
