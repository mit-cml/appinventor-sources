// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * The FileScope enum identifies the set of scopes that App Inventor understands. Not all scopes
 * are relevant to every platform version App Inventor may run on.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public enum FileScope implements OptionList<String> {
  /**
   * Operations should occur in the app-specific directory on the "external" storage. This is the
   * default. Since app-specific storage is only supported on SDK 8 and later, this should behave
   * as App Inventor prior to release nb186.
   */
  App,

  /**
   * Operations should occur against app assets. This can only be used in read operations. Any
   * write operations attempted on assets should throw an error.
   */
  Asset,

  /**
   * Operations should occur on the files in the app's private cache directory.
   */
  Cache,

  /**
   * Operations should occur using legacy logic (pre-nb186). This may break on newer Android
   * versions and make apps incompatible with Google Play Store guidelines.
   */
  Legacy,

  /**
   * Operations should occur on files in the app's private data directory.
   */
  Private,

  /**
   * Operations should occur on files in the shared media directories. Examples of shared
   * directories include Downloads and Music.
   */
  Shared;

  private static final Map<String, FileScope> LOOKUP = new HashMap<>();

  static {
    for (FileScope scope : values()) {
      LOOKUP.put(scope.toUnderlyingValue(), scope);
    }
  }

  public static FileScope fromUnderlyingValue(String scope) {
    return LOOKUP.get(scope);
  }

  @Override
  public String toUnderlyingValue() {
    return name();
  }
}
