// Copyright 2010 Google Inc. All Rights Reserved.

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
