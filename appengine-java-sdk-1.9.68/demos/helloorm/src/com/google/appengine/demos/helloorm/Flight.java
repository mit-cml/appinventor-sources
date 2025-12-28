// Copyright 2008 Google Inc. All Rights Reserved.
package com.google.appengine.demos.helloorm;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author Max Ross <maxr@google.com>
 */
@PersistenceCapable(detachable = "true")
public class Flight {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String orig;

  @Persistent
  private String dest;

  public Flight(String orig, String dest) {
    this.orig = orig;
    this.dest = dest;
  }

  public Long getId() {
    return id;
  }

  public String getOrig() {
    return orig;
  }

  public String getDest() {
    return dest;
  }

  public void setOrig(String orig) {
    this.orig = orig;
  }

  public void setDest(String dest) {
    this.dest = dest;
  }
}
