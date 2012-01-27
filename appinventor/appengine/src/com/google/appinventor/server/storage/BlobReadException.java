// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.appinventor.server.storage;

import java.io.IOException;

/**
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class BlobReadException extends IOException {

 public BlobReadException(Exception e) {
   super();
   this.initCause(e);
 }
 
 public BlobReadException(String msg) {
   super(msg);
 }
}

