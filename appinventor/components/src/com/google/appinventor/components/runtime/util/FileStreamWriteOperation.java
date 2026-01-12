// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * The FileStreamWriteOperation is an abstract class that wraps a write operation to a stream in
 * an OutputStreamWriter using the platform default character encoding (UTF-8 on Android).
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class FileStreamWriteOperation extends FileWriteOperation {
  private static final String LOG_TAG = FileStreamWriteOperation.class.getSimpleName();

  /**
   * Create a new FileStreamWriteOperation.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param scope permission mode to use for locating the file and asking permissions
   * @param append true if the file should be opened for appending, false if the file should be
   *               overwritten
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   * @throws IllegalArgumentException if the file given maps to an asset
   */
  public FileStreamWriteOperation(Form form, Component component, String method, String fileName,
      FileScope scope, boolean append, boolean async) {
    super(form, component, method, fileName, scope, append, async);
  }

  @Override
  protected final boolean process(OutputStream out) throws IOException {
    boolean close = true;
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(out);
      close = process(writer);
    } finally {
      if (close) {
        IOUtils.closeQuietly(LOG_TAG, writer);
      }
    }
    return close;
  }

  /**
   * Write to the stream.
   *
   * @param writer the writer to write to
   * @return true if the writer and the underlying stream should be closed by the caller, false if
   *     the implementation assumes responsibility for closing the writer and its stream
   * @throws IOException if an error occurs during the write operation
   */
  protected abstract boolean process(OutputStreamWriter writer) throws IOException;
}
