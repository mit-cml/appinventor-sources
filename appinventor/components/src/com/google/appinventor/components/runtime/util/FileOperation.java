// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;

import android.os.Build;
import android.util.Log;

import com.google.appinventor.components.common.FileScope;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;

import com.google.appinventor.components.runtime.errors.StopBlocksExecution;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * The FileOperation class encapsulates high level interactions between components that need to
 * interact with files, the Android permissions model, and threading.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class FileOperation implements Runnable, PermissionResultHandler {
  private static final String LOG_TAG = FileOperation.class.getSimpleName();
  protected final Form form;
  protected final Component component;
  protected final String method;
  protected final boolean async;
  protected volatile boolean askedForPermission = false;
  protected volatile boolean hasPermission = false;

  FileOperation(Form form, Component component, String method, boolean async) {
    this.form = form;
    this.component = component;
    this.method = method;
    this.async = async;
  }

  /**
   * Generate a list of the permissions needed by the operation.
   *
   * @return a list of Android permissions
   */
  public abstract List<String> getPermissions();

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
    List<String> neededPermissions = getNeededPermissions();
    if (AsynchUtil.isUiThread()) {
      // If run on the UI thread, check if we have permission. If we have permission, continue
      // with running the file operation. If we don't have permission, prompt the user.
      if (needsExternalStorage()) {
        FileUtil.checkExternalStorageWriteable();
      }
      if (!neededPermissions.isEmpty()) {
        if (!hasPermission) {
          if (askedForPermission) {
            form.dispatchPermissionDeniedEvent(component, method, neededPermissions.get(0));
            askedForPermission = false;
          } else {
            askedForPermission = true;
            form.askPermission(new BulkPermissionRequest(component, method,
                neededPermissions.toArray(new String[0])) {
                  @Override
                  public void onGranted() {
                    // This callback is performed on the UI thread.
                    hasPermission = true;
                    FileOperation.this.run();
                  }
                });
          }
          // We need to stop blocks execution at this point because work needs to be done on the
          // main thread by Android runtime.
          throw new StopBlocksExecution();
        } else if (async) {
          AsynchUtil.runAsynchronously(this);
        } else {
          performOperation();
        }
        return;
      } else {
        // If we are accessing an asset, a private data file, or an external file in the
        // app-specific data directory
        hasPermission = true;
      }
      if (async) {
        AsynchUtil.runAsynchronously(this);
      } else {
        performOperation();
      }
    } else if (!neededPermissions.isEmpty()) {
      // We don't have permission and aren't on the UI thread, so re-run the logic on the UI thread
      // in order to ask for permission first.
      hasPermission = false;
      askedForPermission = false;
      form.runOnUiThread(this);
    } else {
      performOperation();
    }
  }

  // endregion

  // region PermissionResultHandler implementation

  /**
   * Handle the user response for a permission request. This method is always run on the UI thread.
   *
   * @param permission the requested permission (as a string)
   * @param granted    true if permission granted, false otherwise
   */
  @Override
  public void HandlePermissionResponse(String permission, boolean granted) {
    this.askedForPermission = true;
    this.hasPermission = granted;
    this.run();
  }

  // endregion

  /**
   * Perform the operation on the file(s) associated with the operation.
   *
   * <p></p>Depending on the state of the async flag, the operation may be running on the UI thread.
   * It is strongly encouraged that any long-running operation be initialized with
   * {@code async = true} to run on a background thread.
   */
  protected abstract void performOperation();

  /**
   * Check whether any of the file(s) associated with the operation require that the external
   * storage partition be mounted.
   *
   * @return true if at least one file needs the external storage mounted, otherwise false
   */
  protected abstract boolean needsExternalStorage();

  /**
   * Tests whether the file(s) involved in the operation will require asking the user for
   * file access ({@code READ_EXTERNAL_STORAGE} or {@code WRITE_EXTERNAL_STORAGE}).
   *
   * @return true if at least one file needs read/write permissions, otherwise false
   */
  protected abstract boolean needsPermission();

  /**
   * Report an file operation error to the blocks code.
   *
   * @param errorNumber a number identifying the error
   * @param messageArgs any optional arguments used to format the error messages
   */
  protected void reportError(final int errorNumber, final Object... messageArgs) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        form.dispatchErrorOccurredEvent(component, method, errorNumber, messageArgs);
      }
    });
  }

  private List<String> getNeededPermissions() {
    if (hasPermission) {  // we've already gotten all permissions previously
      return Collections.emptyList();
    }
    List<String> permissions = getPermissions();
    Set<String> result = new HashSet<>();
    for (String permission : permissions) {
      if (form.isDeniedPermission(permission)) {
        result.add(permission);
      }
    }
    return new ArrayList<>(result);
  }

  public interface FileInvocation {
    void call(ScopedFile[] files) throws IOException;
  }

  /**
   * The FileOperation.Builder object can be used to programmatically construct a new FileOperation
   * object based on varying inputs.
   */
  public static class Builder {
    private Form form;
    private Component component;
    private String method;
    private final LinkedHashMap<ScopedFile, FileAccessMode> scopedFiles = new LinkedHashMap<>();
    private final Set<String> neededPermissions = new HashSet<>();
    private final List<FileInvocation> commands = new ArrayList<>();
    private boolean async = true;
    private boolean needsExternalStorage = false;
    private boolean askPermission = true;

    public Builder() {
    }

    /**
     * Create a new FileOperation builder for the given {@code form}, {@code component}, and
     * {@code method}.
     *
     * @param form the form responsible for performing permission operations
     * @param component the source component making the request
     * @param method the component method making the request
     */
    public Builder(Form form, Component component, String method) {
      this.form = form;
      this.component = component;
      this.method = method;
    }

    public Builder setForm(Form form) {
      this.form = form;
      return this;
    }

    public Builder setComponent(Component component) {
      this.component = component;
      return this;
    }

    public Builder setMethod(String method) {
      this.method = method;
      return this;
    }

    /**
     * Add a file to the list of files manipulated by the operation.
     *
     * @param scope the scope of the file
     * @param fileName the name of the file in the target scope
     * @param accessMode read, write, or append, based on what the operation needs
     * @return the Builder object to chain method calls
     */
    public Builder addFile(FileScope scope, String fileName, FileAccessMode accessMode) {
      ScopedFile file = new ScopedFile(scope, fileName);
      if (file.getScope() == FileScope.Asset && accessMode != FileAccessMode.READ) {
        form.dispatchErrorOccurredEvent(component, method, ErrorMessages.ERROR_CANNOT_WRITE_ASSET,
            file.getFileName());
        throw new StopBlocksExecution();
      }
      scopedFiles.put(file, accessMode);
      String resolvedFileName = FileUtil.resolveFileName(form, fileName, scope);
      Log.d(LOG_TAG, method + " resolved " + resolvedFileName);
      needsExternalStorage |= FileUtil.needsPermission(form, resolvedFileName);
      String permission = FileUtil.getNeededPermission(form, resolvedFileName, accessMode);
      if (permission != null) {
        neededPermissions.add(permission);
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
          && file.getScope() == FileScope.Shared && accessMode == FileAccessMode.READ) {
        // READ_EXTERNAL_STORAGE was migrated into finer-grained permissions in SDK 33
        neededPermissions.remove(READ_EXTERNAL_STORAGE);
        neededPermissions.add(READ_MEDIA_AUDIO);
        neededPermissions.add(READ_MEDIA_IMAGES);
        neededPermissions.add(READ_MEDIA_VIDEO);
      }
      return this;
    }

    public Builder addCommand(FileInvocation command) {
      commands.add(command);
      return this;
    }

    public Builder setAsynchronous(boolean async) {
      this.async = async;
      return this;
    }

    public Builder setAskPermission(boolean askPermission) {
      this.askPermission = askPermission;
      return this;
    }

    /**
     * Create a new {@link FileOperation} based on the configuration of the {@link Builder}.
     *
     * @return a fresh {@link FileOperation}
     */
    public FileOperation build() {
      return new FileOperation(form, component, method, async) {
        @Override
        public List<String> getPermissions() {
          return new ArrayList<>(neededPermissions);
        }

        @Override
        protected void performOperation() {
          if (askPermission && !neededPermissions.isEmpty()) {
            // Check whether file permissions are granted
            Iterator<String> i = neededPermissions.iterator();
            while (i.hasNext()) {
              String perm = i.next();
              if (!form.isDeniedPermission(perm)) {
                i.remove();
              }
            }

            if (needsPermission()) {
              Log.d(LOG_TAG, method + " need permissions: " + neededPermissions);
              form.askPermission(new BulkPermissionRequest(component, method,
                  neededPermissions.toArray(new String[0])) {
                @Override
                public void onGranted() {
                  // TODO(ewpatton): Once we have continuation passing, we would continue doing
                  // work here by invoking the command list.
                }
              });
              throw new StopBlocksExecution();
            }
          }

          // If granted, proceed to do the operation
          ScopedFile[] filesArray = scopedFiles.keySet().toArray(new ScopedFile[0]);
          for (FileInvocation command : commands) {
            try {
              command.call(filesArray);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        }

        @Override
        protected boolean needsPermission() {
          return !neededPermissions.isEmpty();
        }

        @Override
        protected boolean needsExternalStorage() {
          return needsExternalStorage;
        }
      };
    }
  }
}
