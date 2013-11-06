// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
