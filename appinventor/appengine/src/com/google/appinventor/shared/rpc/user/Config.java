// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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

  private String environment = null;

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

  public String getEnvironmentValue() {
    return this.environment;
  }

  public void setEnvironmentValue(String value) {
    this.environment =  value;
  }

}
