// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.errors.RuntimeError;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import android.Manifest;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

/**
 * Utilities for reading and writing files to the external storage.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class FileUtil {
  // Note: Some phones come with a "Documents" directory rather than a
  // "My Documents" directory.  Should we check for this and try to be
  // consistent with the phone's directory structure or to be consistent
  // in directory names across phones?
  private static final String DOCUMENT_DIRECTORY = "My Documents/";

  private static final String DIRECTORY_RECORDINGS = "Recordings";

  private static final String FILENAME_PREFIX = "app_inventor_";

  // This has the same value as Environment.DIRECTORY_PICTURES, which was
  // not defined until API Level 8 (Android 2.2).
  // If we use Environment.DIRECTORY_PICTURES here, then this class (FileUtil)
  // will be rejected by the dalvikvm class verifier on older versions of
  // Android.
  private static final String DIRECTORY_PICTURES = "Pictures";

  private static final String DIRECTORY_DOWNLOADS = "Downloads";

  private FileUtil() {
  }

  /**
   * Returns an URL for the given local file.
   */
  public static String getFileUrl(String localFileName) {
    File file = new File(localFileName);
    return file.toURI().toString();
  }

  /**
   * Reads the given local file and returns the contents as a byte array.
   *
   * @param inputFileName the name of the file to read from
   * @return the file's contents as a byte array
   */
  public static byte[] readFile(String inputFileName) throws IOException {
    File inputFile = new File(inputFileName);
    // There are cases where our caller will hand us a file to read that
    // doesn't exist and is expecting a FileNotFoundException if this is the
    // case. So we check if the file exists and throw the exception
    if (!inputFile.isFile()) {
      throw new FileNotFoundException("Cannot find file: " + inputFileName);
    }
    FileInputStream inputStream = null;
    byte[] content = null;
    try {
      inputStream = openFile(inputFileName);
      int fileLength = (int) inputFile.length();
      content = new byte[fileLength];
      int offset = 0;
      int bytesRead = 0;
      do {
        bytesRead = inputStream.read(content, offset, fileLength-offset);
        if (bytesRead > 0) offset += bytesRead;
        if (offset == fileLength) break;
      } while (bytesRead >= 0);
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
    return content;
  }

  /**
   * Opens the file at the given file name.
   *
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   *
   * @param fileName The file path to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws com.google.appinventor.components.runtime.errors.PermissionException If the app does
   * not have permission to read external files and the pathname looks to be external.
   */
  public static FileInputStream openFile(String fileName) throws IOException {
    if (MediaUtil.isExternalFile(fileName)) {
      Form.getActiveForm().assertPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    return new FileInputStream(fileName);
  }

  /**
   * Opens the file identified by the given File object.
   *
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   *
   * @param file The file to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws com.google.appinventor.components.runtime.errors.PermissionException If the app does
   * not have permission to read external files and the pathname looks to be external.
   */
  public static FileInputStream openFile(File file) throws IOException {
    return openFile(file.getAbsolutePath());
  }

  /**
   * Opens the file at the given file URI.
   *
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   *
   * @param fileUri The file URI to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws com.google.appinventor.components.runtime.errors.PermissionException If the app does
   * not have permission to read external files and the pathname looks to be external.
   */
  public static FileInputStream openFile(URI fileUri) throws IOException {
    if (MediaUtil.isExternalFileUrl(fileUri.toString())) {
      Form.getActiveForm().assertPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    return new FileInputStream(new File(fileUri));
  }

  /**
   * Downloads the resource with the given URL and writes it as a local file.
   *
   * @param url the URL to read from
   * @param outputFileName the name of the file to write to
   * @return the URL for the local file
   */
  public static String downloadUrlToFile(String url, String outputFileName) throws IOException {
    InputStream in = new URL(url).openStream();
    try {
      return writeStreamToFile(in, outputFileName);
    } finally {
      in.close();
    }
  }

  /**
   * Writes the given byte array as a local file.
   *
   * @param array the byte array to read from
   * @param outputFileName the name of the file to write to
   * @return the URL for the local file
   */
  public static String writeFile(byte[] array, String outputFileName) throws IOException {
    InputStream in = new ByteArrayInputStream(array);
    try {
      return writeStreamToFile(in, outputFileName);
    } finally {
      in.close();
    }
  }

  /**
   * Copies the contents of one local file to another local file.
   *
   * @param inputFileName the name of the file to read to
   * @param outputFileName the name of the file to write to
   * @return the URL for the local file
   */
  public static String copyFile(String inputFileName, String outputFileName)
      throws IOException {
    InputStream in = new FileInputStream(inputFileName);
    try {
      return writeStreamToFile(in, outputFileName);
    } finally {
      in.close();
    }
  }

  /**
   * Writes the contents from the given input stream to the given file.
   *
   * @param in the InputStream to read from
   * @param outputFileName the name of the file to write to
   * @return the URL for the local file
   */
  public static String writeStreamToFile(InputStream in, String outputFileName) throws IOException {
    File file = new File(outputFileName);

    // Create the parent directory.
    file.getParentFile().mkdirs();

    OutputStream out = new FileOutputStream(file);
    try {
      copy(in, out);

      // Return the URL to the output file.
      return file.toURI().toString();
    } finally {
      out.flush();
      out.close();
    }
  }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    out = new BufferedOutputStream(out, 0x1000);
    in = new BufferedInputStream(in, 0x1000);

    // Copy the contents from the input stream to the output stream.
    while (true) {
      int b = in.read();
      if (b == -1) {
        break;
      }
      out.write(b);
    }
    out.flush();
  }

  /**
   * Creates a {@link File} representing the complete path for an image
   * file, creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * @param extension file extension, such as "png" or "jpg"
   * @return the path to the file
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *   with the appropriate ErrorMessages error code.
   */
  public static File getPictureFile(String extension)
    throws IOException, FileException {
    return getFile(DIRECTORY_PICTURES, extension);
  }

  /**
   * Creates a {@link File} representing the complete path for a recording,
   * creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * @return the path to the file
   * @param extension file extension, such as "3gp"
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *   with the appropriate ErrorMessages error code.
   */
  public static File getRecordingFile(String extension)
      throws IOException, FileException {
    return getFile(DIRECTORY_RECORDINGS, extension);
  }

  /**
   * Creates a {@link File} representing the complete path for a downloaded file,
   * creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * @return the path to the file
   * @param extension file extension, such as "tmp"
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *   with the appropriate ErrorMessages error code.
   */
  public static File getDownloadFile(String extension)
      throws IOException, FileException {
    return getFile(DIRECTORY_DOWNLOADS, extension);
  }

  /**
   * Determines the best directory in which to store a file of the given type
   * and creates the directory if it does not exist, generating a full path.
   *
   * @param category a descriptive category, such as {@link DIRECTORY_PICTURES}
   *        to include in the path
   * @param extension the extension for the end of the file, not including the
   *        period, such as "png"
   * @return the full path to the file
   * @throws IOException if the directory cannot be created
   */
  private static File getFile(String category, String extension)
    throws IOException, FileException {
    String fileName = DOCUMENT_DIRECTORY + category + "/" +
      FILENAME_PREFIX + System.currentTimeMillis() + "." + extension;
    return getExternalFile(fileName);
  }

  /**
   * Returns the File for fileName in the external storage directory in
   * preparation for writing the file. fileName may contain sub-directories.
   * Ensures that all subdirectories exist and that fileName does not exist
   * (deleting it if necessary).
   * @param fileName The path name of the file relative to the external storage
   *   directory
   * @return the File object for creating fileName in the external storage
   * @throws IOException if we are unable to create necessary parent directories
   *   or delete an existing file
   * @throws FileException if the external storage is not writeable.
   */
  public static File getExternalFile(String fileName)
    throws IOException, FileException, SecurityException {
    checkExternalStorageWriteable();
    File file = new File(Environment.getExternalStorageDirectory(), fileName);
    File directory = file.getParentFile();
    if (Form.getActiveForm() != null) {
      Form.getActiveForm().assertPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("Unable to create directory " + directory.getAbsolutePath());
    }
    if (file.exists()) {
      if (!file.delete()) {
        throw new IOException("Cannot overwrite existing file " + file.getAbsolutePath());
      }
    }
    return file;
  }

  /*
   *  Checks that external storage is mounted writeable. If it isn't,
   *  throws an exception.
   */
  public static void checkExternalStorageWriteable() throws FileException {
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      return;
    }
    if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      throw new FileException(ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_READONLY);
    } else {
      throw new FileException(ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE);
    }
  }

  /**
   * Exception class for reporting back media-related error numbers from
   * ErrorMessages, which the caller can in turn pass to
   * Form.dispatchErrorOccurredEvent if needed.
   */
  public static class FileException extends RuntimeError {
    private final int msgNumber;
    public FileException(int errorMsgNumber) {
      msgNumber = errorMsgNumber;
    }

    public int getErrorMessageNumber() {
      return msgNumber;
    }
  }
}
