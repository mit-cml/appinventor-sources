// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileOperation;
import com.google.appinventor.components.runtime.util.FilePermissionMode;
import com.google.appinventor.components.runtime.util.FileStreamReadOperation;
import com.google.appinventor.components.runtime.util.FileStreamWriteOperation;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.FileWriteOperation;
import com.google.appinventor.components.runtime.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Non-visible component for storing and retrieving files. Use this component to write or read files
 * on the device. File names can take one of three forms:
 *
 * - Private files have no leading `/` and are written to app private storage (e.g., "file.txt")
 * - External files have a single leading `/` and are written to public storage (e.g., "/file.txt")
 * - Bundled app assets have two leading `//` and can only be read (e.g., "//file.txt")
 *
 * The exact location where external files are placed is a function of the value of the
 * [`AccessMode`](#AccessMode) property, whether the app is running in the Companion or compiled,
 * and which version of Android the app is running on. The following table shows the different
 * combinations where files may be placed:
 *
 * <style>
 *   table.file-doc { margin: auto; font-size: 10pt; }
 *   table.file-doc th,
 *   table.file-doc td { border: 1px solid black; white-space: nowrap; padding: 4pt; }
 *   table.file-doc th { background-color: lightblue; }
 *   table.file-doc th[colspan] { background-color: lightgray; }
 * </style>
 * <table class="file-doc">
 *   <tr>
 *     <th style="text-align: center;">AccessMode</th>
 *     <th style="text-align: center;">Companion</th>
 *     <th style="text-align: center;">Compiled</th>
 *   </tr>
 *   <tr>
 *     <th colspan="3" style="text-align: center;">Prior to Android 10</th>
 *   </tr>
 *   <tr>
 *     <td>Default</td>
 *     <td>/sdcard/<i>filename</i></td>
 *     <td>/sdcard/<i>filename</i></td>
 *   </tr>
 *   <tr>
 *     <td>Legacy [Note&nbsp;2]</td>
 *     <td>/sdcard/<i>filename</i></td>
 *     <td>/sdcard/<i>filename</i></td>
 *   </tr>
 *   <tr>
 *     <td>Private [Note&nbsp;3]</td>
 *     <td>/sdcard/Android/data/edu.mit.appinventor.aicompanion3/<i>filename</i></td>
 *     <td>/sdcard/Android/data/<i>app package</i>/<i>filename</i></td>
 *   </tr>
 *   <tr>
 *     <th colspan="3" style="text-align: center;">Android 10 and Later</th>
 *   </tr>
 *   <tr>
 *     <td>Default</td>
 *     <td>/sdcard/Android/data/edu.mit.appinventor.aicompanion3/<i>filename</i></td>
 *     <td>/sdcard/Android/data/<i>app package</i>/<i>filename</i></td>
 *   </tr>
 *   <tr>
 *     <td>Legacy</td>
 *     <td>/sdcard/<i>filename</i></td>
 *     <td>/sdcard/<i>filename</i></td>
 *   </tr>
 *   <tr>
 *     <td>Private</td>
 *     <td>/sdcard/Android/data/edu.mit.appinventor.aicompanion3/<i>filename</i></td>
 *     <td>/sdcard/Android/data/<i>app package</i>/<i>filename</i></td>
 *   </tr>
 * </table>
 *
 * **Notes**
 *
 * Note 1: The exact location of the external storage depends on the particular device. We use
 * `/sdcard` above as a placeholder for the device-specific location.
 *
 * Note 2: Legacy mode only takes effect on Android 10 and later. On earlier versions of Android,
 * legacy mode is the same as Default mode. On Android 11 and later, Legacy mode may result in
 * errors due to changes in how Android manages file access.
 *
 * Note 3: Private mode only takes effect on Android 2.2 Froyo and later. On earlier versions of
 * Android private mode is the same as Default mode.
 *
 * Because newer versions of Android will require files to be stored in app-specific directories
 * on external storage, you may want to set `AccessMode` to `Private` wherever it makes sense in
 * your existing apps. Future versions of App Inventor may switch to using `Private` by default.
 */
@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
    description = "Non-visible component for storing and retrieving files. Use this component to "
    + "write or read files on your device. The default behaviour is to write files to the "
    + "private data directory associated with your App. The Companion is special cased to write "
    + "files to /sdcard/AppInventor/data to facilitate debugging. "
    + "If the file path starts with a slash (/), then the file is created relative to /sdcard. "
    + "For example writing a file to /myFile.txt will write the file in /sdcard/myFile.txt.",
    category = ComponentCategory.STORAGE,
    nonVisible = true,
    iconName = "images/file.png")
@SimpleObject
@UsesPermissions({ READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE })
@SuppressLint({"InlinedApi", "SdCardPath"})
public class File extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "FileComponent";
  private FilePermissionMode permissionMode = FilePermissionMode.DEFAULT;

  /**
   * Creates a new File component.
   * @param container the Form that this component is contained in.
   */
  public File(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Specifying the AccessMode allows you to control the scope of files read/written by the File
   * component when using a file name starting with a single `/` character.
   *
   *   1. Default stores files on Android 10 and higher in app-specific storage.
   *   2. Legacy will attempt to read/write files at the old locations and raise errors if this
   *      fails on newer versions of Android.
   *   3. Private will prefer app-specific directories on all versions of Android that support them
   *      (Android 2.2 Froyo and later).
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FILEACCESSMODE,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void AccessMode(int mode) {
    FilePermissionMode newMode = FilePermissionMode.fromUnderlyingValue(mode);
    if (newMode != null) {
      permissionMode = newMode;
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  @Deprecated
  public void LegacyMode(boolean legacy) {
    this.permissionMode = legacy ? FilePermissionMode.LEGACY : FilePermissionMode.DEFAULT;
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
  @Deprecated
  public boolean LegacyMode() {
    return permissionMode == FilePermissionMode.LEGACY;
  }

  /**
   * Saves text to a file. If the `fileName`{:.text.block} begins with a slash (`/`) the file is
   * written to the sdcard (for example, writing to `/myFile.txt` will write the file to
   * `/sdcard/myFile.txt`). If the `fileName`{:.text.block} does not start with a slash, it will be
   * written in the program's private data directory where it will not be accessible to other
   * programs on the phone. There is a special exception for the AI Companion where these files are
   * written to `/sdcard/AppInventor/data` to facilitate debugging.
   *
   *   Note that this block will overwrite a file if it already exists. If you want to add content
   * to an existing file use the {@link #AppendToFile(String, String)} method.
   *
   * @internaldoc
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction(description = "Saves text to a file. If the filename "
      + "begins with a slash (/) the file is written to the sdcard. For example writing to "
      + "/myFile.txt will write the file to /sdcard/myFile.txt. If the filename does not start "
      + "with a slash, it will be written in the programs private data directory where it will "
      + "not be accessible to other programs on the phone. There is a special exception for the "
      + "AI Companion where these files are written to /sdcard/AppInventor/data to facilitate "
      + "debugging. Note that this block will overwrite a file if it already exists."
      + "\n\nIf you want to add content to a file use the append block.")
  public void SaveFile(String text, String fileName) {
    write(fileName, "SaveFile", text, false);
  }

  /**
   * Appends text to the end of a file. Creates the file if it does not already exist. See the help
   * text under {@link #SaveFile(String, String)} for information about where files are written.
   * On success, the {@link #AfterFileSaved(String)} event will run.
   *
   * @internaldoc
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction(description = "Appends text to the end of a file storage, creating the file if it does not exist. "
      + "See the help text under SaveFile for information about where files are written.")
  public void AppendToFile(String text, String fileName) {
    write(fileName, "AppendToFile", text, true);
  }

  /**
   * Reads text from a file in storage. Prefix the `fileName`{:.text.block} with `/` to read from a
   * specific file on the SD card (for example, `/myFile.txt` will read the file
   * `/sdcard/myFile.txt`). To read assets packaged with an application (also works for the
   * Companion) start the `fileName`{:.text.block} with `//` (two slashes). If a
   * `fileName`{:.text.block} does not start with a slash, it will be read from the application's
   * private storage (for packaged apps) and from `/sdcard/AppInventor/data` for the Companion.
   *
   * @param fileName the file from which the text is read
   */
  @SimpleFunction(description = "Reads text from a file in storage. "
      + "Prefix the filename with / to read from a specific file on the SD card. "
      + "for instance /myFile.txt will read the file /sdcard/myFile.txt. To read "
      + "assets packaged with an application (also works for the Companion) start "
      + "the filename with // (two slashes). If a filename does not start with a "
      + "slash, it will be read from the applications private storage (for packaged "
      + "apps) and from /sdcard/AppInventor/data for the Companion.")
  public void ReadFrom(final String fileName) {
    run(new FileStreamReadOperation(form, this, "ReadFrom", fileName, permissionMode, true) {
      @Override
      public boolean process(String contents) {
        final String text = IOUtils.normalizeNewLines(contents);
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            GotText(text);
          }
        });
        return true;
      }

      @Override
      public void onError(IOException e) {
        if (e instanceof FileNotFoundException) {
          Log.e(LOG_TAG, "FileNotFoundException", e);
          form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
              ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
        } else {
          Log.e(LOG_TAG, "IOException", e);
          form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
              ErrorMessages.ERROR_CANNOT_READ_FILE, fileName);
        }
      }
    });
  }


  /**
   * Deletes a file from storage. Prefix the `fileName`{:.text.block} with `/` to delete a specific
   * file in the SD card (for example, `/myFile.txt` will delete the file `/sdcard/myFile.txt`).
   * If the `fileName`{:.text.block} does not begin with a `/`, then the file located in the
   * program's private storage will be deleted. Starting the `fileName`{:.text.block} with `//` is
   * an error because asset files cannot be deleted.
   *
   * @param fileName the file to be deleted
   */
  @SimpleFunction(description = "Deletes a file from storage. Prefix the filename with / to "
      + "delete a specific file in the SD card, for instance /myFile.txt. will delete the file "
      + "/sdcard/myFile.txt. If the file does not begin with a /, then the file located in the "
      + "programs private storage will be deleted. Starting the file with // is an error "
      + "because assets files cannot be deleted.")
  public void Delete(final String fileName) {
    if (fileName.startsWith("//")) {
      form.dispatchErrorOccurredEvent(this, "Delete",
          ErrorMessages.ERROR_CANNOT_DELETE_ASSET, fileName);
      return;
    }
    run(new FileWriteOperation(form, this, "Delete", fileName, permissionMode, false, true) {
      @Override
      public void processFile(java.io.File file) {
        // Invariant: After deleting, the file should not exist. If the file already
        // doesn't exist, mission accomplished!
        if (file.exists() && !file.delete()) {
          form.dispatchErrorOccurredEvent(File.this, "Delete",
              ErrorMessages.ERROR_CANNOT_DELETE_FILE, fileName);
        }
      }
    });
  }

  /**
   * Writes to the specified file.
   * @param filename the file to write
   * @param text to write to the file
   * @param append determines whether text should be appended to the file,
   *     or overwrite the file
   */
  private void write(final String filename, final String method, final String text,
      final boolean append) {
    if (filename.startsWith("//")) {
      form.dispatchErrorOccurredEvent(this, method, ErrorMessages.ERROR_CANNOT_WRITE_ASSET,
          filename);
      return;
    }
    if (filename.startsWith("/")) {
      FileUtil.checkExternalStorageWriteable(); // Only check if writing to sdcard
    }
    run(new FileStreamWriteOperation(form, this, method, filename, permissionMode, append, true) {
      @Override
      public void processFile(java.io.File file) {
        if (!file.exists()) {
          boolean success = false;
          try {
            IOUtils.mkdirs(file);
            success = file.createNewFile();
          } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to create file " + file.getAbsolutePath());
          }
          if (!success) {
            form.dispatchErrorOccurredEvent(File.this, method,
                ErrorMessages.ERROR_CANNOT_CREATE_FILE, file.getAbsolutePath());
            return;
          }
        }
        super.processFile(file);
      }

      @Override
      public boolean process(OutputStreamWriter out) throws IOException {
        out.write(text);
        out.flush();
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            AfterFileSaved(filename);
          }
        });
        return true;
      }

      @Override
      public void onError(IOException e) {
        super.onError(e);
        form.dispatchErrorOccurredEvent(File.this, method, ErrorMessages.ERROR_CANNOT_WRITE_TO_FILE,
            getFile().getAbsolutePath());
      }
    });
  }

  /**
   * Event indicating that the contents from the file have been read.
   *
   * @param text read from the file
   */
  @SimpleEvent (description = "Event indicating that the contents from the file have been read.")
  public void GotText(String text) {
    // invoke the application's "GotText" event handler.
    EventDispatcher.dispatchEvent(this, "GotText", text);
  }

  /**
   * Event indicating that the contents of the file have been written.
   *
   * @param fileName the name of the written file
   */
  @SimpleEvent (description = "Event indicating that the contents of the file have been written.")
  public void AfterFileSaved(String fileName) {
    // invoke the application's "AfterFileSaved" event handler.
    EventDispatcher.dispatchEvent(this, "AfterFileSaved", fileName);
  }

  private void run(FileOperation<?> operation) {
    operation.run();
  }
}
