package com.google.appinventor.components.runtime.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Note: This is forward-looking to when enum types are supported in the blocks language.
 */
public enum FilePermissionMode {
  DEFAULT(1),
  LEGACY(2),
  PRIVATE(3);

  private final int value;

  FilePermissionMode(int value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, FilePermissionMode> lookup = new HashMap<>();

  static {
    for (FilePermissionMode mode : FilePermissionMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static FilePermissionMode fromUnderlyingValue(Integer mode) {
    return lookup.get(mode);
  }
}
