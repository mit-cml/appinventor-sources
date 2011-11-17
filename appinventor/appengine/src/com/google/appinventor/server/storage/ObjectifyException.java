// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.server.storage;

/**
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class ObjectifyException extends Exception {

  public ObjectifyException(Exception e) {
    super();
    this.initCause(e);
  }
  
  public ObjectifyException(String msg) {
    super(msg);
  }
}
