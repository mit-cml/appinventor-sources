// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The FileStreamReadOperation is an abstract class that wraps a read operation from a stream in
 * an InputStreamReader using the platform default encoding (UTF-8 on Android).
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class FileStreamReadOperation extends FileReadOperation {
  private static final String LOG_TAG = FileStreamReadOperation.class.getSimpleName();

  /**
   * Create a new FileStreamReadOperation.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param scope permission mode to use for locating the file and asking permissions
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   */
  public FileStreamReadOperation(Form form, Component component, String method, String fileName,
      FileScope scope, boolean async) {
    super(form, component, method, fileName, scope, async);
  }

  @Override
  protected boolean process(InputStream in) throws IOException {
    boolean close = true;
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(in);
      close = process(reader);
    } finally {
      if (close) {
        IOUtils.closeQuietly(LOG_TAG, reader);
      }
    }
    return close;
  }

  /**
   * Process the contents of the reader. The default implementation reads the contents into a
   * String, which is passed to {@link #process(String)}. Subclasses may override this function
   * if they need special stream processing logic beyond reading the whole file into a buffer.
   *
   * @param reader the reader to read from
   * @return true if the reader and the underlying stream should be closed by the caller, false if
   *     the implementation assumes responsibility for closing the reader and its stream
   * @throws IOException if an error occurs during the read operation
   */
  protected boolean process(InputStreamReader reader) throws IOException {
    return process(IOUtils.readReader(reader));
  }

  /**
   * Process the contents of the file.
   *
   * @param contents the contents of the file interpreted using the platform default encoding
   * @return true if the stream should be closed by the caller, false otherwise
   */
  protected abstract boolean process(String contents);
}
