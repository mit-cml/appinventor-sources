// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.net.Uri;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URI;

/**
 * The FileWriteOperation class is responsible for opening files for write operations.
 *
 * <p>The default behavior is to open the file and do nothing. Subclasses should override
 * {@link #process(OutputStream)} to manipulate the file contents.
 */
public class FileWriteOperation extends FileStreamOperation<OutputStream> {

  /**
   * Create a new FileWriteOperation.
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
  public FileWriteOperation(Form form, Component component, String method, String fileName,
      FileScope scope, boolean append, boolean async) {
    super(form, component, method, fileName, scope,
        append ? FileAccessMode.APPEND : FileAccessMode.WRITE, async);
    if (fileName.startsWith("//")) {
      throw new IllegalArgumentException("Cannot perform a write operation on an asset");
    }
  }

  /**
   * Create a new FileWriteOperation targeting a ScopedFile.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param file the ScopedFile to perform the operation on
   * @param append true if the file should be opened for appending, false if the file should be
   *               overwritten
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   */
  public FileWriteOperation(Form form, Component component, String method, ScopedFile file,
      boolean append, boolean async) {
    super(form, component, method, file,
        append ? FileAccessMode.APPEND : FileAccessMode.WRITE, async);
    if (file.getScope() == FileScope.Asset) {
      throw new IllegalArgumentException("Cannot perform a write operation on an asset");
    }
  }

  /**
   * Process the stream. The default implementation does nothing. Subclasses should override this
   * method to write content to the stream.
   *
   * @param stream the stream to write to
   * @return true if the stream should be closed by the caller, or false if the implementation will
   *     take ownership over closing the stream
   * @throws IOException if there is an underlying issue writing the stream
   */
  @Override
  protected boolean process(OutputStream stream) throws IOException {
    return true;
  }

  @Override
  protected OutputStream openFile() throws IOException {
    if (fileName.startsWith("content:")) {
      return form.getContentResolver().openOutputStream(Uri.parse(fileName),
          this.accessMode == FileAccessMode.WRITE ? "wt" : "wa");
    }
    String path = FileUtil.resolveFileName(form, fileName, scope);
    if (path.startsWith("file://")) {
      path = URI.create(path).getPath();
    } else if (path.startsWith("file:")) {
      path = URI.create(path).getPath();
    }
    File file = new File(path);
    IOUtils.mkdirs(file);
    return new FileOutputStream(file, FileAccessMode.APPEND.equals(accessMode));
  }

}
