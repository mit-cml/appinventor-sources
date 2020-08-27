// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.errors.RuntimeError;

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
@SuppressLint("InlinedApi")
public class FileUtil {
  /**
   * The minimum SDK version for which we enforce the use of app-specific directories on the
   * external storage. Starting with Android Q, apps cannot write to arbitrary locations on
   * external storage unless a specific attribute is set in the manifest. Starting with Android R,
   * this will only be allowed if the app is installed prior to the device being upgraded to
   * Android R and only if the app continues to be upgraded. A fresh install wipes the privileges
   * and apps must write to the app-specific directory.
   *
   * This must be 8 or more. App-specific directories are not supported on versions of Android
   * before SDK 8 (2.2 Froyo).
   */
  public static final int MIN_SDK_FOR_APP_SPECIFIC_DIRS = Build.VERSION_CODES.Q;

  private static final String LOG_TAG = FileUtil.class.getSimpleName();
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

  static {
    //noinspection ConstantConditions
    if (MIN_SDK_FOR_APP_SPECIFIC_DIRS < Build.VERSION_CODES.FROYO) {
      throw new IllegalStateException("MIN_SDK_FOR_APP_SPECIFIC_DIRS must be 8 or greater");
    }
  }

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
   * <p>This function is deprecated. Developers should use
   * {@link #readFile(Form, String)} instead.</p>
   *
   * @param inputFileName the name of the file to read from
   * @return the file's contents as a byte array
   */
  @Deprecated
  public static byte[] readFile(String inputFileName) throws IOException {
    Log.w(LOG_TAG, "Calling deprecated function readFile", new IllegalAccessException());
    return readFile(Form.getActiveForm(), inputFileName);
  }

  /**
   * Reads the given local file and returns the contents as a byte array.
   *
   * @param form the form to use as an Android context
   * @param inputFileName the name of the file to read from
   * @return the file's contents as a byte array
   */
  public static byte[] readFile(Form form, String inputFileName) throws IOException {
    File inputFile = new File(inputFileName);
    // There are cases where our caller will hand us a file to read that
    // doesn't exist and is expecting a FileNotFoundException if this is the
    // case. So we check if the file exists and throw the exception
    if (!inputFile.isFile()) {
      throw new FileNotFoundException("Cannot find file: " + inputFileName);
    }
    FileInputStream inputStream = null;
    byte[] content;
    try {
      inputStream = openFile(form, inputFileName);
      int fileLength = (int) inputFile.length();
      content = new byte[fileLength];
      int offset = 0;
      int bytesRead;
      do {
        bytesRead = inputStream.read(content, offset, fileLength - offset);
        if (bytesRead > 0) {
          offset += bytesRead;
        }
        if (offset == fileLength) {
          break;
        }
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
   * <p>
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   * </p>
   *
   * <p>
   * This function is deprecated. Developers should use {@link #openFile(Form, String)} instead.
   * </p>
   *
   * @param fileName The file path to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws PermissionException If the app does not have permission to read external files and the
   *     pathname looks to be external.
   */
  @Deprecated
  public static FileInputStream openFile(String fileName) throws IOException, PermissionException {
    Log.w(LOG_TAG, "Calling deprecated function openFile", new IllegalAccessException());
    return openFile(Form.getActiveForm(), fileName);
  }

  /**
   * Opens the file at the given file name.
   *
   * <p>
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   * </p>
   *
   * @param form the form to use as an Android context
   * @param fileName The file path to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws PermissionException If the app does not have permission to read external files and the
   *     pathname looks to be external.
   */
  public static FileInputStream openFile(Form form, String fileName) throws IOException,
      PermissionException {
    if (MediaUtil.isExternalFile(form, fileName)) {
      form.assertPermission(READ_EXTERNAL_STORAGE);
    }
    return new FileInputStream(fileName);
  }

  /**
   * Opens the file identified by the given File object.
   *
   * <p>
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   * </p>
   *
   * <p>
   * This function is deprecated. Developers should use {@link #openFile(Form, File)} instead.
   * </p>
   *
   * @param file The file to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws PermissionException If the app does not have permission to read external files and the
   *     pathname looks to be external.
   */
  @Deprecated
  public static FileInputStream openFile(File file) throws IOException, PermissionException {
    Log.w(LOG_TAG, "Calling deprecated function openFile", new IllegalAccessException());
    return openFile(Form.getActiveForm(), file.getAbsolutePath());
  }

  /**
   * Opens the file identified by the given File object.
   *
   * <p>
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   * </p>
   *
   * @param form the form to use as an Android context
   * @param file The file to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws PermissionException If the app does not have permission to read external files and the
   *     pathname looks to be external.
   */
  public static FileInputStream openFile(Form form, File file) throws IOException,
      PermissionException {
    return openFile(form, file.getAbsolutePath());
  }

  /**
   * Opens the file at the given file URI.
   *
   * <p>
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   * </p>
   *
   * <p>
   * This function is deprecated. Developers should use {@link #openFile(Form, URI)} instead.
   * </p>
   *
   * @param fileUri The file URI to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws PermissionException If the app does not have permission to read external files and the
   *     pathname looks to be external.
   */
  @Deprecated
  public static FileInputStream openFile(URI fileUri) throws IOException, PermissionException {
    Log.w(LOG_TAG, "Calling deprecated function openFile", new IllegalAccessException());
    return openFile(Form.getActiveForm(), fileUri);
  }

  /**
   * Opens the file at the given file URI.
   *
   * <p>
   * If the file is an external file and the app is running on a version of Android at SDK level 23
   * or higher, then the READ_EXTERNAL_STORAGE permission will be checked. If the app does not have
   * permission to read the external file, a PermissionException will be thrown.
   * </p>
   *
   * @param form the form to use as an Android context
   * @param fileUri The file URI to open.
   * @return An open FileInputStream on success
   * @throws IOException If the file cannot be opened
   * @throws PermissionException If the app does not have permission to read external files and the
   *     pathname looks to be external.
   */
  public static FileInputStream openFile(Form form, URI fileUri) throws IOException,
      PermissionException {
    if (MediaUtil.isExternalFileUrl(form, fileUri.toString())) {
      form.assertPermission(READ_EXTERNAL_STORAGE);
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
   * <p>
   * This function is deprecrated. Developers should use
   * {@link #getPictureFile(Form, String)} instead.
   * </p>
   *
   * @param extension file extension, such as "png" or "jpg"
   * @return the path to the file
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *     with the appropriate ErrorMessages error code.
   */
  @Deprecated
  public static File getPictureFile(String extension)
      throws IOException, FileException {
    Log.w(LOG_TAG, "Calling deprecated function getPictureFile", new IllegalAccessException());
    return getPictureFile(Form.getActiveForm(), extension);
  }

  /**
   * Creates a {@link File} representing the complete path for an image
   * file, creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * @param form the form to use as an Android context
   * @param extension file extension, such as "png" or "jpg"
   * @return the path to the file
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *     with the appropriate ErrorMessages error code.
   */
  public static File getPictureFile(Form form, String extension)
      throws IOException, FileException {
    return getFile(form, DIRECTORY_PICTURES, extension);
  }

  /**
   * Creates a {@link File} representing the complete path for a recording,
   * creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * <p>
   * This function is deprecated. Developers should use
   * {@link #getRecordingFile(Form, String)} instead.
   * </p>
   *
   * @return the path to the file
   * @param extension file extension, such as "3gp"
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *     with the appropriate ErrorMessages error code.
   */
  @Deprecated
  public static File getRecordingFile(String extension)
      throws IOException, FileException {
    return getRecordingFile(Form.getActiveForm(), extension);
  }

  /**
   * Creates a {@link File} representing the complete path for a recording,
   * creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * @param form the form to use as an Android context
   * @param extension file extension, such as "3gp"
   * @return the path to the file
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *     with the appropriate ErrorMessages error code.
   */
  public static File getRecordingFile(Form form, String extension)
      throws IOException, FileException {
    return getFile(form, DIRECTORY_RECORDINGS, extension);
  }

  /**
   * Creates a {@link File} representing the complete path for a downloaded file,
   * creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * <p>
   * This function is deprecated. Developers should use
   * {@link #getDownloadFile(Form, String)} instead.
   * </p>
   *
   * @return the path to the file
   * @param extension file extension, such as "tmp"
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *     with the appropriate ErrorMessages error code.
   */
  @Deprecated
  public static File getDownloadFile(String extension)
      throws IOException, FileException {
    Log.w(LOG_TAG, "Calling deprecated function getDownloadFile", new IllegalAccessException());
    return getDownloadFile(Form.getActiveForm(), extension);
  }

  /**
   * Creates a {@link File} representing the complete path for a downloaded file,
   * creating the enclosing directories if needed.  This does not actually
   * open the file.  Any component that calls this must have
   * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}.
   *
   * @param form the form to use as an Android context
   * @param extension file extension, such as "tmp"
   * @return the path to the file
   * @throws IOException if the enclosing directory cannot be created
   * @throws FileException if external storage is not accessible or not writable
   *     with the appropriate ErrorMessages error code.
   */
  public static File getDownloadFile(Form form, String extension)
      throws IOException, FileException {
    return getFile(form, DIRECTORY_DOWNLOADS, extension);
  }

  /**
   * Determines the best directory in which to store a file of the given type
   * and creates the directory if it does not exist, generating a full path.
   *
   * @param category a descriptive category, such as {@link #DIRECTORY_PICTURES}
   *        to include in the path
   * @param extension the extension for the end of the file, not including the
   *        period, such as "png"
   * @return the full path to the file
   * @throws IOException if the directory cannot be created
   */
  private static File getFile(Form form, String category, String extension)
      throws IOException, FileException {
    String fileName = DOCUMENT_DIRECTORY + category + "/" + FILENAME_PREFIX
        + System.currentTimeMillis() + "." + extension;
    return getExternalFile(form, fileName);
  }

  /**
   * Returns the File for fileName in the external storage directory in
   * preparation for writing the file. fileName may contain sub-directories.
   * Ensures that all subdirectories exist and that fileName does not exist
   * (deleting it if necessary).
   *
   * <p>
   * This function is deprecated. Developers should use
   * {@link #getExternalFile(Form, String)} instead.
   * </p>
   *
   * @param fileName The path name of the file relative to the external storage
   *     directory
   * @return the File object for creating fileName in the external storage
   * @throws IOException if we are unable to create necessary parent directories
   *     or delete an existing file
   * @throws FileException if the external storage is not writeable.
   */
  @Deprecated
  public static File getExternalFile(String fileName) throws IOException, FileException,
      SecurityException {
    return getExternalFile(Form.getActiveForm(), fileName);
  }

  /**
   * Returns the File for fileName in the external storage directory in
   * preparation for writing the file. fileName may contain sub-directories.
   *
   * @param form the form to use as an Android context
   * @param fileName The path name of the file relative to the external storage
   *     directory
   * @return the File object for creating fileName in the external storage
   * @throws FileException if the external storage is not writeable.
   */
  public static File getExternalFile(Form form, String fileName) throws FileException {
    checkExternalStorageWriteable();
    File file = new File(QUtil.getExternalStoragePath(form), fileName);
    if (form != null) {
      form.assertPermission(WRITE_EXTERNAL_STORAGE);
    }
    return file;
  }

  /**
   * Returns the File for fileName in the external storage directory in
   * preparation for writing the file. fileName may contain sub-directories.
   * Ensures that all subdirectories exist and that fileName does not exist
   * (deleting it if necessary).
   *
   * @param form the form to use as an Android context
   * @param fileName The path name of the file relative to the external storage
   *     directory
   * @param mkdirs true if the ancestor directories should be created, otherwise false
   * @param overwrite true if the file is going to be overwritten, otherwise false
   * @return the File object for creating fileName in the external storage
   * @throws IOException if we are unable to create necessary parent directories
   *     or delete an existing file
   * @throws FileException if the external storage is not writeable.
   */
  public static File getExternalFile(Form form, String fileName, boolean mkdirs, boolean overwrite)
      throws IOException, FileException {
    File file = getExternalFile(form, fileName);
    File directory = file.getParentFile();
    if (mkdirs && !directory.exists() && !directory.mkdirs()) {
      throw new IOException("Unable to create directory " + directory.getAbsolutePath());
    }
    if (overwrite && file.exists() && !file.delete()) {
      throw new IOException("Cannot overwrite existing file " + file.getAbsolutePath());
    }
    return file;
  }

  /**
   * Gets an external file {@code fileName} on the external storage.
   *
   * <p>The {@code form} is used as a context for asking for permissions and generating an
   * app-specific directory based on the {@code accessMode} provided.
   *
   * @param form The form to use as the context for the operation(s)
   * @param fileName The filename to read/write/append
   * @param permissionMode The permission model to apply to determine the file location
   * @return The File representing the filename on the external storage. The exact location depends
   *     on the value of {@code accessMode}.
   * @throws FileException if the external storage is not writeable
   * @throws PermissionException if the app doesn't have the necessary permissions to write the file
   */
  public static File getExternalFile(Form form, String fileName, FilePermissionMode permissionMode)
      throws FileException, PermissionException {

    checkExternalStorageWriteable();
    return new File(
        QUtil.getExternalStoragePath(form, permissionMode == FilePermissionMode.PRIVATE,
            permissionMode == FilePermissionMode.LEGACY),
        fileName);
  }

  /**
   * Gets an external file {@code fileName} on the external storage.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param permissionMode permission mode to use for locating the file and asking permissions
   * @param accessMode the direction of the access (read, write, append)
   * @param mkdirs true if any ancestor directories should be made if they don't exist
   * @return a new File object representing the external file
   * @throws IOException if mkdirs is true but the directories cannot be created
   * @throws FileException if the external storage is not writeable
   * @throws PermissionException if the app doesn't have the necessary permissions to write the file
   */
  public static File getExternalFile(Form form, String fileName, FilePermissionMode permissionMode,
      FileAccessMode accessMode, boolean mkdirs)
      throws IOException, FileException, PermissionException {
    File file = getExternalFile(form, fileName, permissionMode);
    if (mkdirs) {
      // Create intermediate directories, if needed.
      if (accessMode != FileAccessMode.READ) {
        File directory = file.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
          throw new IOException("Unable to create directory " + directory.getAbsolutePath());
        }
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
   * Given a file name of the form "name", "/name", or "//name", resolves the name to an absolute
   * file URI.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param permissionMode permission mode to use for locating the file and asking permissions
   * @return a String of the form "file://..." with the full path to the file based on the
   *     permission mode in effect
   */
  public static String resolveFileName(Form form, String fileName,
      FilePermissionMode permissionMode) {
    if (fileName.startsWith("//")) {  // Asset files
      return form.getAssetPath(fileName.substring(2));
    } else if (!fileName.startsWith("/")) {  // Private files
      return form.getPrivatePath(fileName);
    } else {
      /* Starting with nb186, files will be placed in different locations when the file name starts
       * with a single "/" character. For Android Q and later, this is the app-specific directory
       * on external storage. For Android versions prior to Q, it will be the root of the external
       * storage, such as /sdcard or /storage/external/0/.
       */
      return "file://" + getExternalFile(form, fileName, permissionMode).getAbsolutePath();
    }
  }

  /**
   * Checks whether the app will need permission to access {@code fileUri} due to it being in the
   * external storage directory. Starting with Android KitKat, READ/WRITE external storage
   * permissions are not required for files stored in the app-specific directory.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileUri the absolute URI to a file
   * @return true if the fileUri represents a file in external storage and permission will be
   *     needed to access it, otherwise false
   */
  public static boolean needsPermission(Form form, String fileUri) {
    if (isAssetUri(form, fileUri)) {
      return false;
    } else if (isPrivateUri(form, fileUri)) {
      return false;
    } else if (isAppSpecificExternalUri(form, fileUri)) {
      // App-specific directories don't need READ/WRITE permission on KitKat and higher.
      return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
    } else {
      return isExternalStorageUri(form, fileUri);
    }
  }

  /**
   * Checks whether the given {@code fileUri} represents an asset. In the case of a standard Form
   * object, this implies that the {@code fileUri} always starts with "file:///android_asset/".
   * For the REPL, the location will depend on the version of Android. For Android 10+, the REPL
   * stores its assets in the app-specific directory in external storage. Prior to Android 10, the
   * REPL stores its assets in AppInventor/assets in the root of the external storage.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileUri the absolute URI to a file
   * @return true if the fileUri represents an asset, otherwise false
   */
  public static boolean isAssetUri(Form form, String fileUri) {
    return fileUri.startsWith(form.getAssetPath(""));
  }

  /**
   * Checks whether the given {@code fileUri} represents a file in the app's private data
   * directory. For the REPL, "private" files are still stored on the external storage partition.
   * On Android 10 and later, the REPL places private files in a directory called data in the
   * app-specific directory. On Android versions prior to 10, the REPL places private files in
   * AppInventor/data in the root of the external storage.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileUri the absolute URI to a file
   * @return true if the fileUri represents a private data file, otherwise false
   */
  public static boolean isPrivateUri(Form form, String fileUri) {
    return fileUri.startsWith(form.getPrivatePath(""));
  }

  /**
   * Checks whether the given {@code fileUri} represents a file in the app-specific directory on
   * external storage. Because app-specific directories were introduced in SDK level 8
   * (Android 2.2 Froyo), this will always return false on earlier versions.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileUri the absolute URI to a file
   * @return true if the fileUri represents a file in app-specific external storage, otherwise false
   */
  public static boolean isAppSpecificExternalUri(Form form, String fileUri) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
      // App-specific directories were introduced in Android 2.2 Froyo; earlier versions don't have
      // an equivalent concept.
      return false;
    }
    return fileUri.startsWith(form.getExternalFilesDir("").toURI().toString());
  }

  /**
   * Checks whether the given {@code fileUri} represents a file in external storage.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileUri the absolute URI to a file
   * @return true if the fileUri represents a file in external storage, otherwise false
   */
  @SuppressWarnings({"unused", "deprecation"})
  public static boolean isExternalStorageUri(Form form, String fileUri) {
    return fileUri.startsWith(Environment.getExternalStorageDirectory().toURI().toString());
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
