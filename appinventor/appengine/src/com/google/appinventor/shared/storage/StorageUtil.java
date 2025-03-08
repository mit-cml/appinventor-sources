// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.storage;

import com.google.appinventor.shared.rpc.ServerLayout;

/**
 * Constants and utility methods related to storage.
 *
 * @author lizlooney@google.com (Liz Looney)
 */

public class StorageUtil {
  private StorageUtil() {}

  // Default character encoding
  public static final String DEFAULT_CHARSET = "utf-8";

  // The initial Motd Id (which will contain a pointer to the last one).
  // Note that it is an error for there to be no record with this ID.  Use Odetool to
  // create the initial record.
  public static final long INITIAL_MOTD_ID = 1;

  public static final String ANDROID_KEYSTORE_FILENAME = "android.keystore";
  public static final String USER_BACKPACK_FILENAME = "backpack.xml";

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
   * References support media files listed at http://developer.android.com/guide/appendix/media-formats.html
   */
  public static String getContentTypeForFilePath(String filePath) {
    filePath = filePath.toLowerCase();

    // Image File Types
    if (filePath.endsWith(".gif")) {
      return "image/gif";
    }
    if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (filePath.endsWith(".png")) {
      return "image/png";
    }
    if (filePath.endsWith(".bmp")) {
      return "image/bmp";
    }
    if (filePath.endsWith(".webp")) {
      return "image/webp";
    }
    if (filePath.endsWith(".svg")) {
      return "image/svg+xml";
    }

    // Audio File Types
    if (filePath.endsWith(".mp3")) {
      return "audio/mp3";
    }
    if (filePath.endsWith(".m4a")) {
      return "audio/m4a";
    }
    if (filePath.endsWith(".aac")) {
      return "audio/aac";
    }
    if (filePath.endsWith(".wav")) {
      return "audio/wav";
    }
    if (filePath.endsWith(".flac")) {
      return "audio/flac";
    }
    if (filePath.endsWith(".mid") || filePath.endsWith(".xmf") || filePath.endsWith(".mxmf")) {
      return "audio/midi";
    }
    if (filePath.endsWith(".ota")) {
      return "audio/ota";
    }
    if (filePath.endsWith(".rtttl")) {
      return "audio/rtttl";
    }
    if (filePath.endsWith(".rtx")) {
      return "audio/rtx";
    }
    if (filePath.endsWith(".imy")) {
      return "audio/imy";
    }
    if (filePath.endsWith(".ogg")) {
      return "audio/ogg";
    }

    // Video File Types
    if (filePath.endsWith(".avi")) {
      return "video/avi";
    }
    if (filePath.endsWith(".mp4")) {
      return "video/mp4";
    }
    if (filePath.endsWith(".3gp")) {
      return "video/3gp";
    }
    if (filePath.endsWith(".ts")) {
      return "video/ts";
    }
    if (filePath.endsWith(".webm")) {
      return "video/webm";
    }
    if (filePath.endsWith(".mkv")) {
      return "video/mkv";
    }

    // Other File Types
    if (filePath.endsWith(".apk")) {
      return "application/vnd.android.package-archive";
    }

    if (filePath.endsWith(".aia")) {
      return "application/zip; charset=utf-8";
    }

    if (filePath.endsWith(".keystore")) {
      return "application/octet-stream";
    }
    
    if (filePath.endsWith(".ttf")) {
      return "font/ttf";
    }
    if (filePath.endsWith(".otf"))  {
      return "font/otf";
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

  /**
   * Returns true if the given filePath refers an audio file.
   */
  public static boolean isAudioFile(String filePath) {
    String contentType = getContentTypeForFilePath(filePath);
    return contentType.startsWith("audio/");
  }

  /**
   * Returns true if the given filePath refers a video file.
   */
  public static boolean isVideoFile(String filePath) {
    String contentType = getContentTypeForFilePath(filePath);
    return contentType.startsWith("video/");
  }

  /**
   * Returns true if the given filePath refers a font file.
   */
  public static boolean isFontFile(String filePath)  {
    String contentType = getContentTypeForFilePath(filePath);
    return contentType.startsWith("font/");
  }

  /**
   * Returns the URL for the given project file.
   */
  public static String getFileUrl(long projectId, String fileId) {
    // Note: Cannot call ode.GetModuleBaseURL() here because it is shared
    // and Ode is not in scope on the server side.
    return (ServerLayout.getModuleBaseURL() + getFilePath(projectId, fileId));
  }

  /**
   * Returns the relative path within the GWT module for the given project file.
   */
  public static String getFilePath(long projectId, String fileId) {
    // Add "?t=<current time>" so that Firefox won't cache the image.
    // Firefix's caching behavior is buggily aggressive - it caches images even if the server said
    // not to.
    return ServerLayout.DOWNLOAD_SERVLET_BASE + ServerLayout.DOWNLOAD_FILE + "/" +
        projectId + '/' + fileId + "?t=" + System.currentTimeMillis();
  }
}
