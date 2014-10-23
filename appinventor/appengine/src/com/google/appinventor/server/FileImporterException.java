// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.UploadResponse;

/**
 * Thrown by FileImporter methods.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class FileImporterException extends Exception {
  final UploadResponse uploadResponse;

  /**
   * Creates a FileImporterException
   *
   * @param status the UploadResponse status
   */
  public FileImporterException(UploadResponse.Status status) {
    this.uploadResponse = new UploadResponse(status);
  }
}
