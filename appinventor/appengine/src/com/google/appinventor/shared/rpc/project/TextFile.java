// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

/**
 * Represents project file whose content is text.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class TextFile {
  private final String fileName;
  private final String content;

  /**
   * Creates new file.
   *
   * @param fileName file name
   * @param content file content
   */
  public TextFile(String fileName, String content) {
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
  public String getContent() {
    return content;
  }
}
