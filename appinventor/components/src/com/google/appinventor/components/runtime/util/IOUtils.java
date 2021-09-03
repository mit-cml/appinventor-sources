// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class IOUtils {
  private static final int BUFFER_LENGTH = 4096;

  /**
   * Closes the given {@code Closeable}. Suppresses any IO exceptions.
   */
  public static void closeQuietly(String tag, Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
      Log.w(tag, "Failed to close resource", e);
    }
  }

  /**
   * Read an InputStream object until the end of the stream.
   *
   * @param fis the input stream to read
   * @return a byte array with the contents of the stream
   * @throws IOException if there is an underlying problem reading the stream
   */
  public static byte[] readStream(InputStream fis) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_LENGTH];
    int read;
    while ((read = fis.read(buffer)) > 0) {
      baos.write(buffer, 0, read);
    }
    return baos.toByteArray();
  }

  /**
   * Read an InputStream object until the end of stream, interpreting the contents using the given
   * {@code encoding}.
   *
   * @param fis the input stream to read
   * @param encoding the encoding to use for interpreting the stream bytes
   * @return A new String containing the decoded content of the InputStream
   * @throws IOException if there is an underlying problem reading the stream or the given encoding
   *     is not supported on the platform
   */
  public static String readStreamAsString(InputStream fis, String encoding) throws IOException {
    return new String(readStream(fis), encoding);
  }

  /**
   * Read an InputStream object until the end of stream, interpreting the contents using the UTF-8
   * character set.
   *
   * @param fis the input stream to read
   * @return A new String containing the contents of the InputStream interpreted as UTF-8
   * @throws IOException if there is an underlying problem reading the stream
   */
  public static String readStreamAsString(InputStream fis) throws IOException {
    return readStreamAsString(fis, "UTF-8");
  }

  /**
   * Read an InputStreamReader object until the end of the stream.
   *
   * @param reader the input stream reader to use
   * @return a String containing the contents of the stream
   * @throws IOException if there is an underlying problem reading the stream
   */
  public static String readReader(InputStreamReader reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buffer = new char[BUFFER_LENGTH];
    int read;
    while ((read = reader.read(buffer)) > 0) {
      sb.append(buffer, 0, read);
    }
    return sb.toString();
  }

  /**
   * Replace Windows-style CRLF with Unix LF as String. This allows
   * end-user to treat Windows text files same as Unix or Mac. In
   * future, allowing user to choose to normalize new lines might also
   * be nice - in case someone really wants to detect Windows-style
   * line separators, or save a file which was read (and expect no
   * changes in size or checksum).
   * @param s to convert
   */
  public static String normalizeNewLines(String s) {
    return s.replaceAll("\r\n", "\n");
  }

  /**
   * Create the parent directory(-ies) of the given {@code file} if they don't exist. This does
   * not check for write permission, so callers are responsible for ensuring that the app has been
   * granted any required permissions by the user.
   *
   * @param file the file for which to create directories
   * @throws IOException if the directory(-ies) cannot be created
   */
  public static void mkdirs(File file) throws IOException {
    File directory = file.getParentFile();
    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("Unable to create directory for " + file);
    }
  }
}
