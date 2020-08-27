// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Looper;
import android.util.Log;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * The FileOperation class encapsulates high level interactions between components that need to
 * interact with files, the Android permissions model, and threading.
 *
 * @param <T> the underlying stream type, usually subclassed from InputStream or OutputStream
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class FileOperation<T extends Closeable>
    implements Runnable, PermissionResultHandler {

  private static final String LOG_TAG = FileOperation.class.getSimpleName();

  protected final Form form;
  protected final Component component;
  protected final String method;
  protected final FilePermissionMode permissionMode;
  protected final FileAccessMode accessMode;
  protected final String fileName;
  protected final boolean async;
  protected final String resolvedPath;
  protected final File file;
  private volatile boolean askedForPermission = false;
  private volatile boolean hasPermission = false;

  /**
   * Create a new file operation.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param component the Component requesting the file operation
   * @param method the method of {@code component} requesting the file operation
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param mode permission mode to use for locating the file and asking permissions
   * @param accessMode access mode for the file
   * @param async true if the operation should be performed on a separate thread to prevent
   *              blocking the UI thread
   */
  FileOperation(Form form, Component component, String method, String fileName,
      FilePermissionMode mode, FileAccessMode accessMode, boolean async) {
    this.form = form;
    this.component = component;
    this.method = method;
    this.permissionMode = mode;
    this.accessMode = accessMode;
    this.fileName = fileName;
    this.async = async;
    this.resolvedPath = FileUtil.resolveFileName(form, fileName, permissionMode);
    this.file = new File(URI.create(this.resolvedPath));
    Log.d(LOG_TAG, "resolvedPath = " + resolvedPath);
  }

  // region Runnable implementation

  /**
   * Run the FileOperation.
   *
   * <p>Note that this may be called multiple times from different threads and
   * so implementations should be thread safe and reentrant.
   *
   * <p>The logic flow is as follows:
   * 1. When called on the UI thread, we ask for permission if we need it
   *    - If permission is denied, we dispatch an error
   *    - If permission is granted:
   *      - If async is true, we run the operation using
   *        {@link AsynchUtil#runAsynchronously(Runnable)}
   *      - If async is false, we proceed to do the file operation on the UI thread
   * 2. When called not on the UI thread:
   *    - If we need permissions but they haven't been granted, schedule the operation to run on
   *      the UI thread (see step 1)
   *    - If we don't need permission or have the necessary permission, process the file
   */
  @Override
  public final void run() {
    if (isUiThread()) {
      // If run on the UI thread, check if we have permission. If we have permission, continue
      // with running the file operation asynchronously.
      if (fileName.startsWith("/") && (form.isRepl() || !fileName.startsWith("//"))) {
        FileUtil.checkExternalStorageWriteable();
      }
      if (needsPermissionForFile()) {
        hasPermission = !form.isDeniedPermission(accessMode.getPermission());
        if (!hasPermission) {
          if (this.askedForPermission) {
            form.dispatchPermissionDeniedEvent(component, method, accessMode.getPermission());
          } else {
            form.askPermission(accessMode.getPermission(), this);
          }
          return;
        }
      } else {
        // If we are accessing an asset, a private data file, or an external file in the
        // app-specific data directory
        hasPermission = true;
      }
      if (async) {
        AsynchUtil.runAsynchronously(this);
      } else {
        processFile(getFile());
      }
    } else if (needsPermissionForFile() && form.isDeniedPermission(accessMode.getPermission())) {
      // We don't have permission and aren't on the UI thread, so re-run the logic on the UI thread
      // in order to ask for permission first.
      hasPermission = false;
      form.runOnUiThread(this);
    } else {
      processFile(getFile());
    }
  }
  // endregion

  // region PermissionResultHandler implementation

  /**
   * Handle the user response for a permission request. This method is always run on the UI thread.
   *
   * @param permission - The requested permission (as a string)
   * @param granted    - boolean, true if permission granted, false otherwise
   */
  @Override
  public void HandlePermissionResponse(String permission, boolean granted) {
    this.askedForPermission = true;
    this.hasPermission = granted;
    this.run();
  }
  // endregion

  /**
   * Process the file. The default implementation opens the file, calls
   * {@link #process(Closeable)} with the stream, and closes the stream if
   * {@link #process(Closeable)} returns true.
   *
   * <p>Subclasses can override this method to provide additional behaviors, such as creating
   * the parent directories for a file if they don't exist, for example. It is recommended that
   * subclasses still call through to super to leverage the existing logic for file operations
   * in case the logic changes in future versions of App Inventor.
   *
   * @param file the file to be operated on
   */
  public void processFile(File file) {
    T stream = null;
    boolean shouldClose = true;
    try {
      stream = openFile();
      shouldClose = process(stream);
    } catch (IOException e) {
      onError(e);
    } finally {
      if (shouldClose) {
        IOUtils.closeQuietly(component.getClass().getSimpleName(), stream);
      }
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
   * Returns whether the requested file represents an asset or not.
   *
   * @return true if the file associated with this operation is an asset, otherwise false
   */
  public final boolean isAsset() {
    return fileName.startsWith("//");
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
  protected abstract boolean process(T stream) throws IOException;

  /**
   * Open the file for processing.
   *
   * @return an open stream
   * @throws IOException if there is an underlying I/O issue when attempting to open the stream
   */
  abstract T openFile() throws IOException;

  private boolean needsPermissionForFile() {
    return FileUtil.needsPermission(form, resolvedPath);
  }

  private static boolean isUiThread() {
    return Looper.getMainLooper().equals(Looper.myLooper());
  }
}
