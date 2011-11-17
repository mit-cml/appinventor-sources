// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project;

/**
 * Represents project raw file.
 *
 */
public class RawFile {
  private final String fileName;
  private final byte[] content;

  /**
   * Creates new raw file.
   *
   * @param fileName file name
   * @param content file content
   */
  public RawFile(String fileName, byte[] content) {
    this.fileName = fileName;
    this.content = content;
  }

  /**
   * Returns file name.
   *
   * @return file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns file content.
   *
   * @return file content
   */
  public byte[] getContent() {
    return content;
  }
}
