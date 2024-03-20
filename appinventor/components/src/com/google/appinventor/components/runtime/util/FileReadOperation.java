// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.net.Uri;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;

import java.io.IOException;
import java.io.InputStream;

/**
 * The FileReadOperation class is responsible for opening files for read operations.
 *
 * <p>The default behavior is to open the file and read its entire contents into memory. This
 * behavior can be overridden by providing a custom implementation of {@link #process(InputStream)}.
 *
 * <p>If you only need to process the results of the byte stream, you can override
 * {@link #process(byte[])}
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class FileReadOperation extends FileStreamOperation<InputStream> {

  /**
   * Create a new FileReadOperation.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param scope permission mode to use for locating the file and asking permissions
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   */
  public FileReadOperation(Form form, Component component, String method, String fileName,
      FileScope scope, boolean async) {
    super(form, component, method, fileName, scope, FileAccessMode.READ, async);
  }

  @Override
  protected boolean process(InputStream stream) throws IOException {
    return process(IOUtils.readStream(stream));
  }

  /**
   * Process the contents of the file as a byte array. The default implementation does nothing.
   * Subclasses should override this method or {@link #process(InputStream)} if they need special
   * handling of the stream beyond reading the entire contents into a buffer.
   *
   * @param contents the contents of the file
   * @return true if the stream should be closed by the caller, or false if the implementation
   *     has taken ownership of the stream.
   */
  public boolean process(@SuppressWarnings("unused") byte[] contents) {
    return true;
  }

  @Override
  protected InputStream openFile() throws IOException {
    if (scopedFile.getFileName().startsWith("content:")) {
      return form.getContentResolver().openInputStream(Uri.parse(scopedFile.getFileName()));
    }
    return FileUtil.openForReading(form, scopedFile);
  }
}
