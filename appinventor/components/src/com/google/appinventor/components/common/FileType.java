// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code FileType} option list represents high level MIME types that can be used for
 * controlling what files are requested by components such as the FilePicker. This is not meant as
 * an exhaustive list so when adopting FileType for a component it is recommended to accept and
 * return String values instead and use the
 * {@link com.google.appinventor.components.annotations.Options} annotation to indicate that this
 * enumeration is acceptable (but not required).
 */
public enum FileType implements OptionList<String> {
  /**
   * Accept any file type.
   */
  Any("*/*"),

  /**
   * Files associated with audio MIME types.
   */
  Audio("audio/*"),

  /**
   * Files associated with image MIME types.
   */
  Image("image/*"),

  /**
   * Files associated with video MIME types.
   */
  Video("video/*");

  private static final Map<String, FileType> LOOKUP = new HashMap<>();

  static {
    for (FileType type : values()) {
      LOOKUP.put(type.toUnderlyingValue(), type);
    }
  }

  private final String mimeType;

  FileType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public String toUnderlyingValue() {
    return mimeType;
  }

  public static FileType fromUnderlyingValue(String value) {
    return LOOKUP.get(value);
  }
}
