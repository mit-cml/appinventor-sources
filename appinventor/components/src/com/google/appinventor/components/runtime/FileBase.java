// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.Manifest;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.IOUtils;
import com.google.appinventor.components.runtime.util.QUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Base class for File-based components.
 * The class was refactored from the previous File component to
 * have the functionality from reading from file/input streams and for
 * getting the path, since some components can benefit as having
 * this functionality (DataFile is an example component that benefits
 * from this generalization)
 */
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public abstract class FileBase extends AndroidNonvisibleComponent implements Component {
  public static final String NO_ASSETS = "No_Assets";
  protected static final String LOG_TAG = "FileComponent";
  private static final int BUFFER_LENGTH = 4096;

  protected final Activity activity;
  protected boolean isRepl = false;
  protected boolean legacy = false;

  /**
   * Creates a new FileBase component.
   *
   * @param container the Form that this component is contained in.
   */
  protected FileBase(ComponentContainer container) {
    super(container.$form());
    if (form instanceof ReplForm) { // Note: form is defined in our superclass
      isRepl = true;
    }
    activity = (Activity) container.$context();
    LegacyMode(false);
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void LegacyMode(boolean legacy) {
    this.legacy = legacy;
  }

  /**
   * Allows app to access files from the root of the external storage directory (legacy mode).
   * Starting with Android 11, this will no longer be allowed and the behavior is strongly
   * discouraged on Android 10. Starting with Android 10, App Inventor by default will attempt to
   * store files relative to the app-specific private directory on external storage in accordance
   * with this security change.
   *
   *   **Note:** Apps that enable this property will likely stop working after upgrading to
   * Android 11, which strongly enforces that apps only write to app-private directories.
   */
  @SimpleProperty(description = "Allows app to access files from the root of the external storage "
      + "directory (legacy mode).")
  public boolean LegacyMode() {
    return legacy;
  }

  /**
   * Establishes the file path and reads the contents of the specified File
   * asynchronously.
   * <p>
   * Filename formats:
   * /file.txt - reads from SD card
   * //file.txt - reads from packaged application files
   * file.txt - application private storage (for packaged apps) or
   * /sdcard/AppInventor/data for companion
   *
   * @param fileName name of the file to read from
   */
  protected void readFromFile(final String fileName) {
    final boolean legacy = this.legacy;
    form.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionResultHandler() {
      @Override
      public void HandlePermissionResponse(String permission, boolean granted) {
        if (granted) {
          InputStream inputStream;
          try {
            if (fileName.startsWith("//")) {
              inputStream = form.openAsset(fileName.substring(2));
            } else {
              String filepath = AbsoluteFileName(fileName, legacy);
              Log.d(LOG_TAG, "filepath = " + filepath);
              inputStream = FileUtil.openFile(filepath);
            }

            // Read file contents asynchronously
            AsyncRead(inputStream, fileName);
          } catch (PermissionException e) {
            form.dispatchPermissionDeniedEvent(FileBase.this, "ReadFrom", e);
          } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "FileNotFoundException", e);
            form.dispatchErrorOccurredEvent(FileBase.this, "ReadFrom",
                ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
          } catch (IOException e) {
            Log.e(LOG_TAG, "IOException", e);
            form.dispatchErrorOccurredEvent(FileBase.this, "ReadFrom",
                ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
          }
        } else {
          form.dispatchPermissionDeniedEvent(FileBase.this, "ReadFrom", permission);
        }
      }
    });
  }

  /**
   * Returns absolute file path.
   *
   * @param filename the file used to construct the file path
   */
  protected String AbsoluteFileName(String filename, boolean legacy) {
    if (filename.startsWith("/")) {
      return QUtil.getExternalStoragePath(form, false, legacy) + filename;
    } else {
      java.io.File dirPath;
      if (form.isRepl()) {
        dirPath = new java.io.File(QUtil.getReplDataPath(form, false));
      } else {
        dirPath = form.getFilesDir();
      }
      if (!dirPath.exists()) {
        dirPath.mkdirs();           // Make sure it exists
      }
      return dirPath.getPath() + "/" + filename;
    }
  }

  /**
   * Replace Windows-style CRLF with Unix LF as String. This allows
   * end-user to treat Windows text files same as Unix or Mac. In
   * future, allowing user to choose to normalize new lines might also
   * be nice - in case someone really wants to detect Windows-style
   * line separators, or save a file which was read (and expect no
   * changes in size or checksum).
   *
   * @param s to convert
   */

  private String normalizeNewLines(String s) {
    return s.replaceAll("\r\n", "\n");
  }

  /**
   * Reads from the specified InputStream and returns the contents as a String.
   *
   * @param fileInput the stream to read from
   * @return Contents of the file (as a String)
   * @throws IOException when the system cannot read the file
   */
  public String readFromInputStream(InputStream fileInput) throws IOException {
    InputStreamReader input = null;
    try {
      input = new InputStreamReader(fileInput);
      StringWriter output = new StringWriter();
      char[] buffer = new char[BUFFER_LENGTH];
      int offset = 0;
      int length = 0;
      while ((length = input.read(buffer, offset, BUFFER_LENGTH)) > 0) {
        output.write(buffer, 0, length);
      }

      // Now that we have the file as a String,
      // normalize any line separators to avoid compatibility between Windows and Mac
      // text files. Users can expect \n to mean a line separator regardless of how
      // file was created. Currently only doing this for files opened locally - not files we pull
      // from other places like URLs.

      return normalizeNewLines(output.toString());
    } finally {
      IOUtils.closeQuietly(LOG_TAG, fileInput);
    }
  }

  /**
   * Asynchronously reads the contents of the specified Input Stream, the
   * content of which is expected to originate from the specified filename.
   *
   * @param inputStream Input Stream to read from
   * @param fileName    name of the file from which the input stream was generated (used for error reporting)
   */
  protected abstract void AsyncRead(final InputStream inputStream, final String fileName);
}
