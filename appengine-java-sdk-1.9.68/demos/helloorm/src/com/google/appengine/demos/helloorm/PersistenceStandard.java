// Copyright 2008 Google Inc. All Rights Reserved.
package com.google.appengine.demos.helloorm;

/**
 * @author Max Ross <maxr@google.com>
 */
public enum PersistenceStandard {

  JPA, JDO;

  private static final String SYS_PROP = "helloorm.persistence.standard";

  public static synchronized PersistenceStandard get() {
    return PersistenceStandard.valueOf(System.getProperty(SYS_PROP));
  }

  public static synchronized void set(PersistenceStandard ps) {
    System.setProperty(SYS_PROP, ps.name());
  }

  public PersistenceStandard getAlternate() {
    return PersistenceStandard.values()[(this.ordinal() + 1) % 2];
  }
}
