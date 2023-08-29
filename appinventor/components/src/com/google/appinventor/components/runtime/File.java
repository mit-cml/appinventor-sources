// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.errors.StopBlocksExecution;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.Continuation;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileAccessMode;
import com.google.appinventor.components.runtime.util.FileOperation;
import com.google.appinventor.components.runtime.util.FileStreamWriteOperation;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.FileWriteOperation;
import com.google.appinventor.components.runtime.util.IOUtils;
import com.google.appinventor.components.runtime.util.ScopedFile;
import com.google.appinventor.components.runtime.util.SingleFileOperation;
import com.google.appinventor.components.runtime.util.Synchronizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Non-visible component for storing and retrieving files. Use this component to write or read files
 * on the device.
 *
 * The exact location where external files are placed is a function of the value of the
 * [`Scope`](#File.Scope) property, whether the app is running in the Companion or compiled,
 * and which version of Android the app is running on.
 *
 *   Because newer versions of Android require files be stored in app-specific directories, the
 * `DefaultScope` is set to `App`. If you are using an older version of Android and need access to
 * the legacy public storage, change the `DefaultScope` property to `Legacy`. You can also change
 * the `Scope` using the blocks.
 *
 *   Below we briefly describe each scope type:
 *
 *   - App: Files will be read from and written to app-specific storage on Android 2.2 and
 *     higher. On earlier versions of Android, files will be written to legacy storage.
 *   - Asset: Files will be read from the app assets. It is an error to attempt to write to app
 *     assets as they are contained in read-only storage.
 *   - Cache: Files will be read from and written to the app's cache directory. Cache is useful for
 *     temporary files that can be recreated as it allows the user to clear temporary files to get
 *     back storage space.
 *   - Legacy: Files will be read from and written to the file system using the App Inventor rules
 *     prior to release nb187. That is, file names starting with a single `/` will be read from and
 *     written to the root of the external storage directory, e.g., `/sdcard/`. Legacy functionality
 *     ***will not work*** on Android 11 or later.
 *   - Private: Files will be read from and written to the app's private directory. Use this scope
 *     to store information that shouldn't be visible to other applications, such as file
 *     management apps.
 *   - Shared: Files will be read from and written to the device's shared media directories, such
 *     as `Pictures`.
 *
 * Note 1: In Legacy mode, file names can take one of three forms:
 *
 *  - Private files have no leading `/` and are written to app private storage (e.g., "file.txt")
 *  - External files have a single leading `/` and are written to public storage (e.g., "/file.txt")
 *  - Bundled app assets have two leading `//` and can only be read (e.g., "//file.txt")
 *
 * Note 2: In all scopes, a file name beginning with two slashes (`//`) will be interpreted as an
 * asset name.
 */
@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
    description = "Non-visible component for storing and retrieving files. Use this component to "
    + "write or read files on your device. The default behaviour is to write files to the "
    + "private data directory associated with your App. The Companion is special cased to write "
    + "files to a public directory for debugging. Use the More information link to read more about "
    + "how the File component uses paths and scopes to manage access to files.",
    category = ComponentCategory.STORAGE,
    nonVisible = true,
    iconName = "images/file.png")
@SimpleObject
@SuppressLint({"InlinedApi", "SdCardPath"})
public class File extends FileBase implements Component {
  private static final String LOG_TAG = "FileComponent";

  /**
   * Creates a new File component.
   * @param container the Form that this component is contained in.
   */
  public File(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * A designer-only property that can be used to enable read access to file storage outside of the
   * app-specific directories.
   *
   * @param required true if the permission is required
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false, category = PropertyCategory.BEHAVIOR)
  @UsesPermissions(READ_EXTERNAL_STORAGE)
  public void ReadPermission(boolean required) {
    // not used programmatically
  }

  /**
   * Indicates the current scope for operations such as ReadFrom and SaveFile.
   *
   * @param scope the target scope
   */
  @SimpleProperty
  public void Scope(FileScope scope) {
    this.scope = scope;
  }

  @SimpleProperty
  public FileScope Scope() {
    return scope;
  }

  /**
   * A designer-only property that can be used to enable write access to file storage outside of the
   * app-specific directories.
   *
   * @param required true if the permission is required
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false, category = PropertyCategory.BEHAVIOR)
  @UsesPermissions(WRITE_EXTERNAL_STORAGE)
  public void WritePermission(boolean required) {
    // not used programmatically
  }

  /**
   * Create a new directory for storing files. The semantics of this method are such that it will
   * return true if the directory exists at its completion. This can mean that the directory already
   * existed prior to the call.
   *
   * @param scope the scope in which to create the directory
   * @param directoryName the name of the directory to create
   * @param continuation the code to run after making the directory
   */
  @SimpleFunction
  public void MakeDirectory(FileScope scope, String directoryName,
      final Continuation<Boolean> continuation) {
    if (scope == FileScope.Asset) {
      form.dispatchErrorOccurredEvent(this, "MakeDirectory",
          ErrorMessages.ERROR_CANNOT_MAKE_DIRECTORY, directoryName);
      return;
    }
    new SingleFileOperation(form, this, "MakeDirectory", directoryName, scope,
        FileAccessMode.WRITE, false) {
      @Override
      public void processFile(ScopedFile scopedFile) {
        java.io.File file = scopedFile.resolve(form);
        if (file.exists()) {
          if (file.isDirectory()) {
            onSuccess();
          } else {
            // cannot make a directory if there's a regular file there
            reportError(ErrorMessages.ERROR_FILE_EXISTS_AT_PATH, file.getAbsolutePath());
          }
        } else {
          if (file.mkdirs()) {
            onSuccess();
          } else {
            // cannot make directory, probably because an ancestor is read-only
            reportError(ErrorMessages.ERROR_CANNOT_MAKE_DIRECTORY, file.getAbsolutePath());
          }
        }
      }

      public void onSuccess() {
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            continuation.call(true);
          }
        });
      }
    }.run();
  }

  /**
   * Remove a directory from the file system. If recursive is true, then everything is removed. If
   * recursive is false, only the directory is removed and only if it is empty.
   *
   * @param scope the scope in which to find the directory
   * @param directoryName the name of the directory to remove
   * @param recursive true if the directory should be removed recursively
   * @param continuation the continuation to run after the operation completes
   */
  @SimpleFunction
  public void RemoveDirectory(FileScope scope, String directoryName, final boolean recursive,
      final Continuation<Boolean> continuation) {
    if (scope == FileScope.Asset) {
      form.dispatchErrorOccurredEvent(this, "RemoveDirectory",
          ErrorMessages.ERROR_CANNOT_REMOVE_DIRECTORY, directoryName);
      return;
    }
    // TODO(ewpatton): Restructure this when we have full continuation passing style.
    final Synchronizer<Boolean> result = new Synchronizer<>();
    new FileOperation.Builder(form, this, "RemoveDirectory")
        .addFile(scope, directoryName, FileAccessMode.WRITE)
        .addCommand(new FileOperation.FileInvocation() {
          @Override
          public void call(ScopedFile[] files) {
            try {
              ScopedFile file = files[0];
              result.wakeup(FileUtil.removeDirectory(file.resolve(form), recursive));
            } catch (Exception e) {
              result.caught(e);
            }
          }
        }).build().run();
    AsynchUtil.finish(result, continuation);
  }

  /**
   * Get a list of files and directories in the given directory.
   *
   * @param scope the scope to find the directory in
   * @param directoryName the name of the directory to list
   * @param continuation the continuation to run after the operation completes
   */
  @SimpleFunction
  public void ListDirectory(FileScope scope, String directoryName,
      final Continuation<List<String>> continuation) {
    if (scope == FileScope.Asset && !form.isRepl()) {
      try {
        continuation.call(FileUtil.listDirectory(form, new ScopedFile(scope, directoryName)));
      } catch (IOException e) {
        // test
        form.dispatchErrorOccurredEvent(this, "ListDirectory",
            ErrorMessages.ERROR_CANNOT_LIST_DIRECTORY, directoryName);
      }
      return;
    } else if (scope == FileScope.Shared && directoryName.startsWith("content:")
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      DocumentFile dir = DocumentFile.fromTreeUri(form, Uri.parse(directoryName));
      DocumentFile[] files = dir.listFiles();
      List<String> result = new ArrayList<>();
      for (DocumentFile file : files) {
        result.add(file.getName());
      }
      continuation.call(result);
      return;
    }
    if (!directoryName.contains("/")) {
      directoryName += "/";
    }
    final Synchronizer<List<String>> result = new Synchronizer<>();
    new FileOperation.Builder(form, this, "ListDirectory")
        .setAskPermission(true)
        .setAsynchronous(true)
        .addFile(scope, directoryName, FileAccessMode.READ)
        .addCommand(new FileOperation.FileInvocation() {
          @Override
          public void call(ScopedFile[] files) throws IOException {
            Log.d(LOG_TAG, "Listing directory " + files[0]);
            List<String> items = FileUtil.listDirectory(form, files[0]);
            if (items == null) {
              items = Collections.emptyList();
            }
            result.wakeup(items);
          }
        }).build().run();
    AsynchUtil.finish(result, continuation);
  }

  /**
   * Tests whether the path named in the given scope is a directory.
   *
   * @param scope the scope to find the path within
   * @param path the path to test to see if it is a directory
   * @param continuation the continuation to run after the operation completes
   */
  @SimpleFunction
  public void IsDirectory(FileScope scope, String path, final Continuation<Boolean> continuation) {
    if (scope == FileScope.Asset && !form.isRepl()) {
      AssetManager manager = form.getAssets();
      try {
        String[] files = manager.list(path);
        Log.d(LOG_TAG, "contents of " + path + " = " + Arrays.toString(files));
        continuation.call(files != null && files.length > 0);
      } catch (IOException e) {
        form.dispatchErrorOccurredEvent(this, "IsDirectory",
            ErrorMessages.ERROR_DIRECTORY_DOES_NOT_EXIST, path);
      }
      return;
    }
    // TODO(ewpatton): Restructure this when we have full continuation passing style.
    final Synchronizer<Boolean> result = new Synchronizer<>();
    new FileOperation.Builder(form, this, "IsDirectory")
        .addFile(scope, path, FileAccessMode.READ)
        .addCommand(new FileOperation.FileInvocation() {
          @Override
          public void call(ScopedFile[] files) {
            Log.d(LOG_TAG, "IsDirectory " + files[0]);
            result.wakeup(files[0].resolve(form).isDirectory());
          }
        }).build().run();
    AsynchUtil.finish(result, continuation);
  }

  /**
   * Copy the contents from the first file to the second file.
   *
   * @param fromScope the scope of the original file
   * @param fromFileName the name of the source file
   * @param toScope the scope for the target file
   * @param toFileName the name of the target file
   * @param continuation the continuation to run after the operation completes
   */
  @SimpleFunction
  public void CopyFile(FileScope fromScope, String fromFileName, final FileScope toScope,
      final String toFileName, final Continuation<Boolean> continuation) {
    final String method = "CopyFile";

    // We cannot copy to assets...
    if (toScope == FileScope.Asset) {
      form.dispatchErrorOccurredEvent(this, method, ErrorMessages.ERROR_CANNOT_WRITE_ASSET,
          toFileName);
      throw new StopBlocksExecution();
    }

    // TODO(ewpatton): Restructure this when we have full continuation passing style.
    final Synchronizer<Boolean> result = new Synchronizer<>();
    new FileOperation.Builder(form, this, method)
        .addFile(fromScope, fromFileName, FileAccessMode.READ)
        .addFile(toScope, toFileName, FileAccessMode.WRITE)
        .addCommand(new FileOperation.FileInvocation() {
          @Override
          public void call(ScopedFile[] files) {
            InputStream in = null;
            OutputStream out = null;
            if (!files[1].getFileName().startsWith("content:")) {
              // If we aren't using a content provider, try to ensure the parent dirs exist
              java.io.File parent = files[1].resolve(form).getParentFile();
              if (!parent.exists() && !parent.mkdirs()) {
                form.dispatchErrorOccurredEvent(File.this, method,
                    ErrorMessages.ERROR_CANNOT_MAKE_DIRECTORY, parent.getAbsolutePath());
                result.caught(new IOException());
                return;
              }
            }
            try {
              in = FileUtil.openForReading(form, files[0]);
              out = FileUtil.openForWriting(form, files[1]);
              FileUtil.copy(in, out);
            } catch (IOException e) {
              Log.w(LOG_TAG, "Unable to copy file", e);
              form.dispatchErrorOccurredEvent(File.this, method,
                  ErrorMessages.ERROR_CANNOT_COPY_MEDIA, files[0].getFileName());
              result.caught(e);
              return;
            } finally {
              IOUtils.closeQuietly(LOG_TAG, in);
              IOUtils.closeQuietly(LOG_TAG, out);
            }
            result.wakeup(true);
          }
        }).build().run();
    AsynchUtil.finish(result, continuation);
  }

  /**
   * Move a file from one location to another.
   *
   * @internaldoc The move will use the Java NIO interface to perform a file-system level move on
   *     Android O and higher. On earlier versions, the file will be copied and then the old file
   *     removed.
   *
   * @param fromScope The scope containing the file to be moved
   * @param fromFileName The name of the file to be moved
   * @param toScope The new scope to move the file into
   * @param toFileName The new name for the file
   * @param continuation A continuation to execute when the operation completes
   */
  @SimpleFunction
  public void MoveFile(final FileScope fromScope, final String fromFileName,
      final FileScope toScope, final String toFileName, final Continuation<Boolean> continuation) {
    final String method = "MoveFile";

    // We cannot move to/from assets as it would require either deleting an asset or writing one...
    if (fromScope == FileScope.Asset) {
      form.dispatchErrorOccurredEvent(this, method, ErrorMessages.ERROR_CANNOT_DELETE_ASSET,
          fromFileName);
      return;
    }
    if (toScope == FileScope.Asset) {
      form.dispatchErrorOccurredEvent(this, method, ErrorMessages.ERROR_CANNOT_WRITE_ASSET,
          toFileName);
      return;
    }

    // TODO(ewpatton): Restructure this when we have full continuation passing style.
    final Synchronizer<Boolean> result = new Synchronizer<>();
    new FileOperation.Builder(form, this, method)
        .addFile(fromScope, fromFileName, FileAccessMode.READ)
        .addFile(toScope, toFileName, FileAccessMode.WRITE)
        .addCommand(new FileOperation.FileInvocation() {
          @Override
          public void call(ScopedFile[] files) {
            try {
              result.wakeup(FileUtil.moveFile(form, files[0], files[1]));
            } catch (IOException e) {
              // The file was not moved, return false to the blocks
              result.wakeup(false);
            }
          }
        }).build().run();
    AsynchUtil.finish(result, continuation);
  }

  /**
   * Tests whether the path exists in the given scope.
   *
   * @param scope the scope in which to look for the path
   * @param path the path (such as a file or directory) to look for
   * @param continuation the continuation to pass the result
   */
  @SimpleFunction
  public void Exists(FileScope scope, String path, final Continuation<Boolean> continuation) {
    // TODO(ewpatton): Restructure this when we have full continuation passing style.
    final Synchronizer<Boolean> result = new Synchronizer<>();
    new FileOperation.Builder(form, this, "Exists")
        .addFile(scope, path, FileAccessMode.READ)
        .addCommand(new FileOperation.FileInvocation() {
          @Override
          public void call(ScopedFile[] files) {
            if (files[0].getScope() == FileScope.Asset && !form.isRepl()) {
              boolean success = false;
              try {
                String[] items = form.getAssets().list("");
                if (items != null) {
                  for (String item : items) {
                    if (item.equals(files[0].getFileName())) {
                      success = true;
                      break;
                    }
                  }
                }
              } catch (IOException e) {
                // This can happen if the file doesn't exist
              }
              result.wakeup(success);
            } else {
              result.wakeup(files[0].resolve(form).exists());
            }
          }
        }).build().run();
    AsynchUtil.finish(result, continuation);
  }

  /**
   * Converts the scope and path into a single string for other components.
   *
   * @param scope the scope in which to look for the file
   * @param path the path (such as a file or directory) to look for
   * @return a path that uniquely identifies a file in the file system
   */
  @SimpleFunction
  public String MakeFullPath(FileScope scope, String path) {
    return FileUtil.resolveFileName(form, path, scope);
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
    readFromFile(fileName);
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
    try {
      new FileWriteOperation(form, this, "Delete", fileName, scope, false, true) {
        @Override
        public void processFile(ScopedFile scopedFile) {
          java.io.File file = scopedFile.resolve(form);
          // Invariant: After deleting, the file should not exist. If the file already
          // doesn't exist, mission accomplished!
          if (file.exists() && !file.delete()) {
            form.dispatchErrorOccurredEvent(File.this, "Delete",
                ErrorMessages.ERROR_CANNOT_DELETE_FILE, fileName);
          }
        }
      }.run();
    } catch (StopBlocksExecution e) {
      // This is okay because the block is designed to be asynchronous.
    }
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
    try {
      new FileStreamWriteOperation(form, this, method, filename, scope, append, true) {
        @Override
        public void processFile(ScopedFile scopedFile) {
          java.io.File file = scopedFile.resolve(form);
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
          super.processFile(scopedFile);
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
          String fileName;
          if (getFile() == null) {
            fileName = getScopedFile().getFileName();
          } else {
            fileName = getFile().getAbsolutePath();
          }
          form.dispatchErrorOccurredEvent(File.this, method, ErrorMessages.ERROR_CANNOT_WRITE_TO_FILE,
              fileName);
        }
      }.run();
    } catch (StopBlocksExecution e) {
      // This is okay because the block is designed to be asynchronous.
    }
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

  @Override
  protected void afterRead(final String result) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        GotText(result);
      }
    });
  }
}
