// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.common.PropertyTypeConstants;

import com.google.appinventor.components.runtime.errors.StopBlocksExecution;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileStreamReadOperation;
import com.google.appinventor.components.runtime.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Base class for File-based components.
 * The class was refactored from the previous File component to
 * have the functionality from reading from file/input streams and for
 * getting the path, since some components can benefit as having
 * this functionality (DataFile is an example component that benefits
 * from this generalization)
 */
@SimpleObject
public abstract class FileBase extends AndroidNonvisibleComponent implements Component {
  protected static final String LOG_TAG = "FileComponent";

  protected FileScope scope = FileScope.App;

  /**
   * Creates a new FileBase component.
   *
   * @param container the Form that this component is contained in.
   */
  protected FileBase(ComponentContainer container) {
    super(container.$form());
    DefaultScope(FileScope.App);
  }

  /**
   * Specifies the default scope for files accessed using the File component. The App scope should
   * work for most apps. Legacy mode can be used for apps that predate the newer constraints in
   * Android on app file access.
   *
   * @param scope the default file access scope
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FILESCOPE,
      defaultValue = "App")
  @SimpleProperty(userVisible = false, category = PropertyCategory.BEHAVIOR)
  public void DefaultScope(FileScope scope) {
    this.scope = scope;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  @Deprecated
  public void LegacyMode(boolean legacy) {
    this.scope = legacy ? FileScope.Legacy : FileScope.App;
  }

  /**
   * Allows app to access files from the root of the external storage directory (legacy mode).
   * Starting with Android 11, this will no longer be allowed and the behavior is strongly
   * discouraged on Android 10. Starting with Android 10, App Inventor by default will attempt to
   * store files relative to the app-specific private directory on external storage in accordance
   * with this security change.
   *
   *   <p><b>Note:</b> Apps that enable this property will likely stop working after upgrading to
   * Android 11, which strongly enforces that apps only write to app-private directories.
   */
  @SimpleProperty(description = "Allows app to access files from the root of the external storage "
      + "directory (legacy mode).")
  @Deprecated
  public boolean LegacyMode() {
    return scope == FileScope.Legacy;
  }

  /**
   * Establishes the file path and reads the contents of the specified File
   * asynchronously.
   *
   * <p>Filename formats:
   * /file.txt - reads from SD card
   * //file.txt - reads from packaged application files
   * file.txt - application private storage (for packaged apps) or
   * /sdcard/AppInventor/data for companion
   *
   * @param fileName name of the file to read from
   */
  protected void readFromFile(final String fileName) {
    try {
      new FileStreamReadOperation(form, this, "ReadFrom", fileName, scope, true) {
        @Override
        public boolean process(String contents) {
          final String text = IOUtils.normalizeNewLines(contents);
          afterRead(text);
          return true;
        }

        @Override
        public void onError(IOException e) {
          if (e instanceof FileNotFoundException) {
            Log.e(LOG_TAG, "FileNotFoundException", e);
            form.dispatchErrorOccurredEvent(FileBase.this, "ReadFrom",
                ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
          } else {
            Log.e(LOG_TAG, "IOException", e);
            form.dispatchErrorOccurredEvent(FileBase.this, "ReadFrom",
                ErrorMessages.ERROR_CANNOT_READ_FILE, fileName);
          }
        }
      }.run();
    } catch (StopBlocksExecution e) {
      // This is okay because the block is designed to be asynchronous.
    }
  }

  /**
   * Asynchronously reads the contents of the specified Input Stream, the
   * content of which is expected to originate from the specified filename.
   *
   * @param result  the contents of the file that was read
   */
  protected abstract void afterRead(String result);
}
