// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;

@SuppressLint("InlinedApi")
public enum FileAccessMode {
  READ(READ_EXTERNAL_STORAGE),
  WRITE(WRITE_EXTERNAL_STORAGE),
  APPEND(WRITE_EXTERNAL_STORAGE);

  private final String permission;

  FileAccessMode(String permission) {
    this.permission = permission;
  }

  public String getPermission() {
    return permission;
  }
}
