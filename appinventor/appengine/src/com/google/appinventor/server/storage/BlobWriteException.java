// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;

import java.io.IOException;

/**
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class BlobWriteException extends IOException {

  public BlobWriteException(Exception e, String msg) {
    super(msg);
    this.initCause(e);
  }
  
  public BlobWriteException(String msg) {
    super(msg);
  }

}
