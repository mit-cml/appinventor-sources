// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * The {@code SingleFileOperation} class encapsulates the behavior needed to interact with a single
 * file in the file system.
 */
public abstract class SingleFileOperation extends FileOperation {
  private static final String LOG_TAG = FileOperation.class.getSimpleName();

  protected final FileScope scope;
  protected final FileAccessMode accessMode;
  protected final String fileName;
  protected final ScopedFile scopedFile;
  protected final File file;
  protected final String resolvedPath;

  /**
   * Create a new {@code SingleFileOperation} to be executed under the given arguments. This
   * version of the constructor allows the caller to specify whether the operation is asynchronous.
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
  protected SingleFileOperation(Form form, Component component, String method, String fileName,
      FileScope scope, FileAccessMode accessMode, boolean async) {
    super(form, component, method, async);
    this.scope = scope;
    this.accessMode = accessMode;
    this.fileName = fileName;
    this.scopedFile = new ScopedFile(scope, fileName);
    if (fileName.startsWith("content:")) {
      this.file = null;
      this.resolvedPath = fileName;
    } else {
      this.file = scopedFile.resolve(form);
      this.resolvedPath = file.getAbsolutePath();
    }
    Log.d(LOG_TAG, "resolvedPath = " + resolvedPath);
  }

  /**
   * Create a new {@code SingleFileOperation} to be executed under the given arguments.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param file the ScopedFile to perform the operation on
   * @param accessMode access mode for the file
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   */
  protected SingleFileOperation(Form form, Component component, String method, ScopedFile file,
      FileAccessMode accessMode, boolean async) {
    super(form, component, method, async);
    this.scope = file.getScope();
    this.accessMode = accessMode;
    this.fileName = file.getFileName();
    this.scopedFile = file;
    if (fileName.startsWith("content:")) {
      this.file = null;
      this.resolvedPath = this.fileName;
    } else {
      this.file = scopedFile.resolve(form);
      this.resolvedPath = this.file.getAbsolutePath();
    }
    Log.d(LOG_TAG, "resolvedPath = " + resolvedPath);
  }

  /**
   * Create a new file operation.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param scope permission mode to use for locating the file and asking permissions
   * @param accessMode access mode for the file
   */
  protected SingleFileOperation(Form form, Component component, String method, String fileName,
      FileScope scope, FileAccessMode accessMode) {
    this(form, component, method, fileName, scope, accessMode, true);
  }

  @Override
  public List<String> getPermissions() {
    String permission = FileUtil.getNeededPermission(form, resolvedPath, accessMode);
    if (permission == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(permission);
    }
  }

  /**
   * Get the File object associated with this operation.
   *
   * @return the file object
   */
  public final File getFile() {
    return file;
  }

  /**
   * Get the ScopedFile object associated with this operation.
   *
   * @return the scoped file object
   */
  public final ScopedFile getScopedFile() {
    return scopedFile;
  }

  /**
   * Returns whether the requested file represents an asset or not.
   *
   * @return true if the file associated with this operation is an asset, otherwise false
   */
  public final boolean isAsset() {
    return fileName.startsWith("//") || scope == FileScope.Asset;
  }

  /**
   * Return the intended scope of the file operation.
   *
   * @return the file's scope
   */
  public final FileScope getScope() {
    return scope;
  }

  /**
   * Process the file.
   *
   * <p>Subclasses can override this method to provide additional behaviors, such as creating
   * the parent directories for a file if they don't exist, for example. It is recommended that
   * subclasses still call through to super to leverage the existing logic for file operations
   * in case the logic changes in future versions of App Inventor.
   *
   * @param file the file to be operated on
   */
  protected abstract void processFile(ScopedFile file);

  @Override
  protected void performOperation() {
    processFile(scopedFile);
  }

  @Override
  protected boolean needsExternalStorage() {
    return FileUtil.isExternalStorageUri(form, resolvedPath);
  }

  protected final boolean needsPermission() {
    return FileUtil.needsPermission(form, resolvedPath);
  }
}
