// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.appinventor.server.storage;

import java.io.IOException;

/**
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class BlobWriteException extends IOException {

  public BlobWriteException(Exception e) {
    super();
    this.initCause(e);
  }
  
  public BlobWriteException(String msg) {
    super(msg);
  }

}
