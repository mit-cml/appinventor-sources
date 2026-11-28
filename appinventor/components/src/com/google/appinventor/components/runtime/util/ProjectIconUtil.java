// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Reads the developer-selected icon from a saved project without extracting the project. */
public final class ProjectIconUtil {
  private static final int MAX_PROPERTIES_BYTES = 64 * 1024;
  private static final int MAX_ICON_BYTES = 5 * 1024 * 1024;

  private ProjectIconUtil() {
  }

  /**
   * Returns the selected icon bytes, or null if the project has no available icon.
   *
   * @throws IOException if the archive cannot be read or an entry exceeds the size limit
   * @throws IllegalArgumentException if the properties contain an invalid Unicode escape
   */
  public static byte[] readIcon(File project) throws IOException {
    try (ZipFile archive = new ZipFile(project)) {
      byte[] metadata = readEntry(archive, "youngandroidproject/project.properties",
          MAX_PROPERTIES_BYTES);
      if (metadata == null) {
        return null;
      }
      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(metadata));
      String icon = properties.getProperty("icon", "").trim();
      return icon.isEmpty() ? null : readEntry(archive, "assets/" + icon, MAX_ICON_BYTES);
    }
  }

  private static byte[] readEntry(ZipFile archive, String name, int limit) throws IOException {
    ZipEntry entry = archive.getEntry(name);
    if (entry == null || entry.isDirectory()) {
      return null;
    }
    if (entry.getSize() > limit) {
      throw new IOException("Project icon entry exceeds size limit: " + name);
    }
    try (InputStream input = archive.getInputStream(entry)) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int count;
      while ((count = input.read(buffer)) != -1) {
        if (count > limit - output.size()) {
          throw new IOException("Project icon entry exceeds size limit: " + name);
        }
        output.write(buffer, 0, count);
      }
      return output.toByteArray();
    }
  }
}
