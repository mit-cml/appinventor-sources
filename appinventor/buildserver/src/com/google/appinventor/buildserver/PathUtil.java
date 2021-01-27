// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.buildserver;

import java.util.logging.Logger;

/**
 * Constants and utility methods related to storage.
 *
 * Based largely on com.google.appinventor.shared.storage.StorageUtil
 *
 * @author markf@google.com (Herbert Czymontek)
 */

public class PathUtil {

  // Logging support
  private static final Logger LOG = Logger.getLogger(PathUtil.class.getName());
  
  private PathUtil() {}

  // Default character encoding
  public static final String DEFAULT_CHARSET = "Cp1252";

  /**
   * Gets the final component from a path.  This assumes that path components
   * are separated by forward slashes.
   *
   * @param path The path to apply the basename operation to.
   * @return path, with any leading directory elements removed
   */
  public static String basename(String path) {
    if (path.length() == 0) {
      return path;
    }

    int pos = path.lastIndexOf("/");
    if (pos == -1) {
      return path;
    } else {
      return path.substring(pos + 1);
    }
  }

  /**
   * <p>Determines the parent directory of the given path.  This is similar to
   * dirname(1).  This assumes that path components are separated by forward
   * slashes.</p>
   *
   * <p>The returned path omits the last slash and trailing component;
   * for example, "/foo/bar.txt" becomes "/foo".  There are a special cases:
   * <ul>
   *   <li> If the last slash is the first character in the input,
   *        the return value is "/".
   *   <li> If there are no slashes in the input, "." is returned.
   * </ul>
   * </p>
   *
   * @param path the path to strip
   * @return the parent path, as described above
   */
  public static String dirname(String path) {
    int lastSlash = path.lastIndexOf("/");

    if ("/".equals(path) || lastSlash == 0) {
      return "/";
    } else if (lastSlash == -1) {
      return ".";
    } else {
      return path.substring(0, lastSlash);
    }
  }

  /**
   * Returns a copy of the given path with the extension omitted.
   *
   * @param path the path
   * @return path, with the extension elements omitted.
   */
  public static String trimOffExtension(String path) {
    int lastSlash = path.lastIndexOf('/');
    int lastDot = path.lastIndexOf('.');
    return (lastDot > lastSlash) ? path.substring(0, lastDot) : path;
  }

  /**
   * Returns the package name part of an dot-qualified class name.
   *
   * @param qualifiedName  qualified class name
   * @return  package name
   */
  public static String getPackageName(String qualifiedName) {
    int index = qualifiedName.lastIndexOf('.');
    return index < 0 ? "" : qualifiedName.substring(0, index);
  }

  /**
   * Returns the content type for the given filePath.
   */
  public static String getContentTypeForFilePath(String filePath) {
    filePath = filePath.toLowerCase();

    if (filePath.endsWith(".gif")) {
      return "image/gif";
    }
    if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (filePath.endsWith(".png")) {
      return "image/png";
    }

    if (filePath.endsWith(".apk")) {
      return "application/vnd.android.package-archive; charset=utf-8";
    }

    if (filePath.endsWith(".zip")) {
      return "application/zip; charset=utf-8";
    }

    if (filePath.endsWith(".keystore")) {
      return "application/octet-stream";
    }

    // default
    return "text/plain; charset=utf-8";
  }

  /**
   * Returns true if the given filePath refers an image file.
   */
  public static boolean isImageFile(String filePath) {
    String contentType = getContentTypeForFilePath(filePath);
    return contentType.startsWith("image/");
  }

  /**
   * Returns true if the given filePath refers a text file.
   */
  public static boolean isTextFile(String filePath) {
    String contentType = getContentTypeForFilePath(filePath);
    return contentType.startsWith("text/");
  }
}
