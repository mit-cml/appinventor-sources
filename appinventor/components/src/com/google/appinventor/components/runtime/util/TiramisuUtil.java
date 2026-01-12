// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;

import android.os.Build;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;

public class TiramisuUtil {
  /**
   * Requests Android version-specific permissions for reading media.
   *
   * @param form the form used by for requesting the permission
   * @param path the path to the media
   * @param mediaPermission the media type related permission to request on Android 13+
   * @param continuation the code to execute after the permissions have been granted
   * @return true if a permission request is initiated or false if all permissions are granted
   */
  public static boolean requestFilePermissions(Form form, String path, String mediaPermission,
      PermissionResultHandler continuation) {
    String perm = null;
    if (path.startsWith("content:")
        || (path.startsWith("file:") && FileUtil.needsPermission(form, path))
        || FileUtil.needsReadPermission(new ScopedFile(form.DefaultFileScope(), path))) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        perm = mediaPermission;
      } else {
        // Prior to Android 13 Tiramisu, all file types were accessed with READ_EXTERNAL_STORAGE
        perm = READ_EXTERNAL_STORAGE;
      }
    }
    if (perm != null && form.isDeniedPermission(perm)) {
      form.askPermission(perm, continuation);
      return true;
    }
    return false;
  }

  public static boolean requestAudioPermissions(Form form, String path,
      PermissionResultHandler continuation) {
    return requestFilePermissions(form, path, READ_MEDIA_AUDIO, continuation);
  }

  public static boolean requestImagePermissions(Form form, String path,
      PermissionResultHandler continuation) {
    return requestFilePermissions(form, path, READ_MEDIA_IMAGES, continuation);
  }

  public static boolean requestVideoPermissions(Form form, String path,
      PermissionResultHandler continuation) {
    return requestFilePermissions(form, path, READ_MEDIA_VIDEO, continuation);
  }
}
