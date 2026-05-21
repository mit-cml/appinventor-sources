// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;

import java.io.Closeable;
import java.io.IOException;

/**
 * The FileStreamOperation class encapsulates high level interactions between components that need
 * to interact with files, the Android permissions model, and threading.
 *
 * @param <T> the underlying stream type, usually subclassed from InputStream or OutputStream
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class FileStreamOperation<T extends Closeable> extends SingleFileOperation {

  private static final String LOG_TAG = FileStreamOperation.class.getSimpleName();

  /**
   * Create a new file operation.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param scope permission mode to use for locating the file and asking permissions
   * @param accessMode access mode for the file
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   */
  protected FileStreamOperation(Form form, Component component, String method, String fileName,
      FileScope scope, FileAccessMode accessMode, boolean async) {
    super(form, component, method, fileName, scope, accessMode, async);
  }

  /**
   * Create a new file operation.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param file the ScopedFile to write to
   * @param accessMode access mode for the file
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   */
  protected FileStreamOperation(Form form, Component component, String method, ScopedFile file,
      FileAccessMode accessMode, boolean async) {
    super(form, component, method, file, accessMode, async);
  }

  @Override
  protected void processFile(ScopedFile file) {
    T stream = null;
    boolean shouldClose = true;
    try {
      stream = openFile();
      shouldClose = process(stream);
    } catch (IOException e) {
      e.printStackTrace();
      onError(e);
    } finally {
      if (shouldClose) {
        IOUtils.closeQuietly(component.getClass().getSimpleName(), stream);
      }
    }
  }

  /**
   * Handle errors during the file operation. The default implementation writes to the Android log.
   * Subclasses can override the behavior to provide alternative error handling logic.
   *
   * @param e the exception that occurred
   */
  public void onError(IOException e) {
    // Subclasses can override this to provide additional error handling.
    Log.e(LOG_TAG, "IO error in file operation", e);
  }

  /**
   * Process the contents of the stream.
   *
   * @param stream the stream
   * @return true if the stream should be closed by the caller, or false if the implementation will
   *     take ownership over closing the stream
   * @throws IOException if there is an underlying issue reading or writing the stream
   */
  protected boolean process(T stream) throws IOException {
    throw new UnsupportedOperationException("Subclasses must implement FileOperation#process.");
  }

  /**
   * Open the file for processing.
   *
   * @return an open stream
   * @throws IOException if there is an underlying I/O issue when attempting to open the stream
   */
  protected T openFile() throws IOException {
    throw new UnsupportedOperationException("Subclasses must implement FileOperation#openFile.");
  }
}
