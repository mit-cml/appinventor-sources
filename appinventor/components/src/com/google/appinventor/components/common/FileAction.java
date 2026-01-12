// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code FileAction} option list represents the different types of actions a user can take
 * with Android's Storage Access Framework. For more information see
 * <a href="https://developer.android.com/training/data-storage/shared/documents-files#use-cases">
 *   Use cases for accessing documents and other files</a>.
 */
public enum FileAction implements OptionList<String> {

  /**
   * Action to pick an existing file in the file system.
   */
  PickExistingFile("Pick Existing File"),

  /**
   * Action to pick a new file (or overwrite an existing file) in the file system.
   */
  PickNewFile("Pick New File"),

  /**
   * Action to pick a (possibly new) directory in the file system.
   */
  PickDirectory("Pick Directory");

  private static final Map<String, FileAction> LOOKUP = new HashMap<>();

  static {
    for (FileAction action : values()) {
      LOOKUP.put(action.value, action);
    }
  }

  private final String value;

  FileAction(String value) {
    this.value = value;
  }

  @Override
  public String toUnderlyingValue() {
    return value;
  }

  public static FileAction fromUnderlyingValue(String value) {
    return LOOKUP.get(value);
  }
}
