// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

/**
 * Encapsulates a {@link RawFile} and a count of the number of
 * files contained within.
 *
 */
public class ProjectSourceZip {
  private RawFile rawFile;
  private int fileCount;
  private String metadata;

  /**
   * Creates new raw zip file.
   *
   * @param rawFile encapsulation of the file name and contents
   * @param fileCount the number of files in the zip file
   */
  public ProjectSourceZip(RawFile rawFile, int fileCount) {
    this.rawFile = rawFile;
    this.fileCount = fileCount;
    this.metadata = "";
  }

  /**
   * Creates new raw zip file.
   *
   * @param fileName file name
   * @param content file content
   * @param fileCount number of files in the zip file
   */
  public ProjectSourceZip(String fileName, byte[] content, int fileCount) {
    this(new RawFile(fileName, content), fileCount);
  }

  /**
   * Returns the encapsulated file (name and content).
   *
   * @return the encapsulated file
   */
  public RawFile getRawFile() {
    return rawFile;
  }

  /**
   * Returns file name.
   *
   * @return file name
   */
  public String getFileName() {
    return rawFile.getFileName();
  }

  /**
   * Returns file content.
   *
   * @return file content
   */
  public byte[] getContent() {
    return rawFile.getContent();
  }

  /**
   * Returns count of number of files in this zip file
   *
   * @return file count
   */
  public int getFileCount() {
    return fileCount;
  }

  /**
   * Sets metadata for this zip file
   *
   */
  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  /**
   * Returns metadata for this zip file
   *
   * @return metadata
   */
  public String getMetadata() {
    return metadata;
  }
}
