// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import android.annotation.SuppressLint;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;

import android.content.res.AssetManager;

import android.database.Cursor;

import android.net.Uri;

import android.os.Build;
import android.os.Environment;

import android.provider.MediaStore;

import android.util.Log;

import com.google.appinventor.components.common.FileScope;

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

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
   * <p>This must be 8 or more. App-specific directories are not supported on versions of Android
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
    if (inputFileName.startsWith("file://")) {
      // The remainder of this function expects Unix-like paths, not URIs
      inputFileName = inputFileName.substring(7);
    }
    InputStream inputStream = null;
    byte[] content;
    try {
      if (inputFileName.startsWith("/android_asset/")) {
        // Assets don't live in the file system, so need to be handled separately.
        inputStream = form.openAsset(inputFileName.substring(inputFileName.lastIndexOf('/') + 1));
      } else {
        File inputFile = new File(inputFileName);
        // There are cases where our caller will hand us a file to read that
        // doesn't exist and is expecting a FileNotFoundException if this is the
        // case. So we check if the file exists and throw the exception
        if (!inputFile.isFile()) {
          throw new FileNotFoundException("Cannot find file: " + inputFileName);
        }
        inputStream = openFile(form, inputFileName);
      }
      content = IOUtils.readStream(inputStream);
    } finally {
      IOUtils.closeQuietly(LOG_TAG, inputStream);
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
    // Make fileName a URI for testing permissions
    String fileUri = fileName.startsWith("/") ? "file://" + fileName : fileName;
    if (needsReadPermission(form, fileUri)) {
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
    if (needsPermission(form, fileUri.toString())) {
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
   * Copy a file from one scope to another.
   *
   * @param form the form to serve as a context for Android operations
   * @param src the source file
   * @param dest the destination file
   * @return true if the file was copied successfully, false otherwise
   * @throws IOException if an I/O exception occurs while copying the original file
   */
  public static boolean copyFile(Form form, ScopedFile src, ScopedFile dest) throws IOException {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        && src.getScope() != FileScope.Shared && dest.getScope() != FileScope.Shared) {
      // Because Shared files go through the MediaStore, we cannot use the NIO API for copying.
      // New style. Use Java NIO to move the file, potentially between file system providers
      Files.copy(Paths.get(src.resolve(form).toURI()), Paths.get(dest.resolve(form).toURI()),
          REPLACE_EXISTING);
    } else {
      InputStream in = null;
      OutputStream out = null;
      try {
        in = openForReading(form, src);
        out = openForWriting(form, src);
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) > 0) {
          out.write(buffer, 0, read);
        }
      } finally {
        IOUtils.closeQuietly(LOG_TAG, in);
        IOUtils.closeQuietly(LOG_TAG, out);
      }
    }
    return true;
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

  /**
   * Copy the contents of the input stream {@code in} to the output stream {@code out}.
   *
   * @param in the stream to read
   * @param out the stream to write
   * @throws IOException when the stream(s) cannot be accessed
   */
  public static void copy(InputStream in, OutputStream out) throws IOException {
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
   * Creates a {@link ScopedFile} for a picture file with the given {@code extension} relative to
   * the Picture directory in the {@code form}'s {@link Form#DefaultFileScope()}.
   *
   * @param form the Form object to provide the scope
   * @param extension the image extension (e.g., "png")
   * @return a fresh file, scoped to the form's default scope
   */
  public static ScopedFile getScopedPictureFile(Form form, String extension) {
    return getScopedFile(form, DIRECTORY_PICTURES, extension);
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
    File target = getExternalFile(form, fileName);
    File parent = target.getParentFile();
    if (!parent.exists() && !parent.mkdirs()) {
      throw new IOException("Unable to create directory: " + parent.getAbsolutePath());
    }
    return target;
  }

  /**
   * Determines the best directory in which to store a file of the given type
   * and generates the corresponding ScopedFile object using the default scope
   * provided by the {@code form}. Note that since {@link FileScope#Asset} scope
   * is read-only, calls to this method will use {@link FileScope#Private} as
   * a fallback.
   *
   * @param form the form to use as an Android context
   * @param category a descriptive category, such as {@link #DIRECTORY_PICTURES}
   *                 to include in the path
   * @param extension the extension for the end of the file, not including the
   *                  period, such as "png"
   * @return the ScopedFile referencing
   */
  private static ScopedFile getScopedFile(Form form, String category, String extension) {
    String fullPath;
    FileScope scope = form.DefaultFileScope();
    if (scope == FileScope.Legacy) {
      // NB: we need the first slash here when using Legacy mode so that the file will appear in the
      // external storage rather than internal storage.
      fullPath = "/" + DOCUMENT_DIRECTORY + category;
    } else {
      fullPath = category;
      if (scope == FileScope.Asset) {
        // This method is called to create new files, but assets are read-only.
        // We fall back to private storage in this case
        scope = FileScope.Private;
      }
    }
    fullPath += "/" + FILENAME_PREFIX + System.currentTimeMillis() + "." + extension;
    return new ScopedFile(scope, fullPath);
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
    if (form.DefaultFileScope() == FileScope.Legacy && !fileName.startsWith("/")) {
      // The legacy File component used a single slash as a prefix to indicate external storage.
      fileName = "/" + fileName;
    }
    String uri = resolveFileName(form, fileName, form.DefaultFileScope());
    if (isExternalStorageUri(form, uri)) {
      checkExternalStorageWriteable();
    }
    if (needsPermission(form, uri)) {
      form.assertPermission(WRITE_EXTERNAL_STORAGE);
    }
    return new File(URI.create(uri));
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
   * @param scope The permission model to apply to determine the file location
   * @return The File representing the filename on the external storage. The exact location depends
   *     on the value of {@code accessMode}.
   * @throws FileException if the external storage is not writeable
   * @throws PermissionException if the app doesn't have the necessary permissions to write the file
   */
  public static File getExternalFile(Form form, String fileName, FileScope scope)
      throws FileException, PermissionException {
    return new File(URI.create(resolveFileName(form, fileName, scope)));
  }

  /**
   * Gets an external file {@code fileName} on the external storage.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param scope permission mode to use for locating the file and asking permissions
   * @param accessMode the direction of the access (read, write, append)
   * @param mkdirs true if any ancestor directories should be made if they don't exist
   * @return a new File object representing the external file
   * @throws IOException if mkdirs is true but the directories cannot be created
   * @throws FileException if the external storage is not writeable
   * @throws PermissionException if the app doesn't have the necessary permissions to write the file
   */
  public static File getExternalFile(Form form, String fileName, FileScope scope,
      FileAccessMode accessMode, boolean mkdirs)
      throws IOException, FileException, PermissionException {
    File file = getExternalFile(form, fileName, scope);
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
   * @param scope permission mode to use for locating the file and asking permissions
   * @return a String of the form "file://..." with the full path to the file based on the
   *     permission mode in effect
   */
  public static String resolveFileName(Form form, String fileName, FileScope scope) {
    File path;
    if (fileName.startsWith("//")) {  // Asset files in legacy mode
      path = new File(form.getAssetPath(fileName.substring(2)).substring(7));
    } else if (fileName.startsWith("content:")) {
      return fileName;
    } else if (scope == FileScope.App && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
      path = new File(form.getExternalFilesDir(""), fileName);
    } else if (scope == FileScope.Asset) {
      path = new File(form.getAssetPath(fileName).substring(7));
    } else if (scope == FileScope.Cache) {
      path = new File(form.getCachePath(fileName).substring(7));
    } else if ((scope == FileScope.Legacy || scope == null) && fileName.startsWith("/")) {
      path = new File(QUtil.getExternalStorageDir(form, false, true), fileName.substring(1));
    } else if (scope == FileScope.Private) {
      path = new File(form.getPrivatePath(fileName).substring(7));
    } else if (scope == FileScope.Shared) {
      path = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
    } else if (!fileName.startsWith("/")) {  // Private files in legacy mode
      path = new File(form.getPrivatePath(fileName).substring(7));
    } else {
      /* Starting with nb186, files will be placed in different locations when the file name starts
       * with a single "/" character. For Android Q and later, this is the app-specific directory
       * on external storage. For Android versions prior to Q, it will be the root of the external
       * storage, such as /sdcard or /storage/external/0/.
       */
      path = getExternalFile(form, fileName.substring(1), scope);
    }
    return Uri.fromFile(path).toString();
  }

  /**
   * Given a scoped file, resolves the name to an absolute file URI.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param file the ScopedFile for which to obtain an absolute URI
   * @return a String of the form "file://..." with the full path to the file based on the
   *     permission mode in effect
   */
  public static String resolveFileName(Form form, ScopedFile file) {
    return resolveFileName(form, file.getFileName(), file.getScope());
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
   * Check if the given {@code fileUri} will need READ permission to be accessed.
   *
   * @param form the Form to serve as an Android context
   * @param fileUri the file that will be accessed
   * @return true if the READ_EXTERNAL_STORAGE will need to be granted before the file can be
   *     accessed
   */
  public static boolean needsReadPermission(Form form, String fileUri) {
    return needsPermission(form, fileUri);
  }

  /**
   * Check whether the given scoped file needs the READ_EXTERNAL_STORAGE permission to be
   * granted to the app before the file can be read.
   *
   * @param scopedFile the scoped file to check for permission
   * @return true if the file will need READ_EXTERNAL_STORAGE permission, otherwise false
   */
  public static boolean needsReadPermission(ScopedFile scopedFile) {
    switch (scopedFile.getScope()) {
      case Legacy:
      case Shared:
        return true;
      default:
        return false;
    }
  }

  /**
   * Check if the given {@code fileUri} will need WRITE permission to be accessed.
   *
   * @param form the Form to serve as an Android context
   * @param fileUri the file that will be accessed
   * @return true if the WRITE_EXTERNAL_STORAGE will need to be granted before the file can be
   *     accessed
   */
  public static boolean needsWritePermission(Form form, String fileUri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      // WRITE_EXTERNAL_STORAGE is removed starting with Android R
      // For files the app can write to, write operations will proceed as expected
      // For file the app cannot write to, write operations will fail
      return false;
    } else {
      return needsPermission(form, fileUri);
    }
  }

  /**
   * Check whether the given scoped file needs the WRITE_EXTERNAL_STORAGE permission to be
   * granted to the app before the file can be written. Note: WRITE_EXTERNAL_STORAGE was removed
   * starting in Android 11 (SDK 30).
   *
   * @param scopedFile the scoped file to check for permission
   * @return true if the file will need WRITE_EXTERNAL_STORAGE permission, otherwise false
   */
  public static boolean needsWritePermission(ScopedFile scopedFile) {
    return needsWritePermission(scopedFile.getScope());
  }

  /**
   * Check whether the given file scope needs the WRITE_EXTERNAL_STORAGE permisison to be
   * granted to the app before files can be written to that scope. Note: WRITE_EXTERNAL_STORAGE was
   * removed starting in Android 11 (SDK 30).
   *
   * @param scope the file scope
   * @return true if the file scope needs WRITE_EXTERNAL_STORAGE permission, otherwise false
   */
  public static boolean needsWritePermission(FileScope scope) {
    switch (scope) {
      case Legacy:
      case Shared:
        // Write permission no longer exists in Android R
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R;
      default:
        return false;
    }
  }

  /**
   * Check whether the given scoped file will cause access to external storage. This can be used to
   * determine whether a check for the mount state of the external storage is also needed. Note that
   * the file need not exist since we may want to create it.
   *
   * @param form the Form object to use as the Android context
   * @param scopedFile the scoped file to check against the external storage
   * @return true if operations on the scoped file will involve external storage, otherwise false
   */
  public static boolean needsExternalStorage(Form form, ScopedFile scopedFile) {
    return isExternalStorageUri(form,
        resolveFileName(form, scopedFile.getFileName(), scopedFile.getScope()));
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
    return fileUri.startsWith("file://" + form.getExternalFilesDir("").getAbsolutePath());
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
    if (fileUri.startsWith("file:///sdcard/") || fileUri.startsWith("file:///storage")) {
      return true;
    }
    return fileUri.startsWith("file://"
        + Environment.getExternalStorageDirectory().getAbsolutePath())
        || fileUri.startsWith("file://" + form.getExternalFilesDir("").getAbsolutePath());
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

  public static String getNeededPermission(Form form, String path, FileAccessMode mode) {
    if (path == null) {
      throw new NullPointerException("path cannot be null");
    } else if (path.startsWith("file:") || path.startsWith("/")) {
      if (path.startsWith("/")) {
        path = "file://" + path;
      }
      if (isExternalStorageUri(form, path)) {
        if (isAppSpecificExternalUri(form, path)
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          // On Android 4.4 (API level 19) or higher, your app doesn't need to request any
          // storage-related permissions to access app-specific directories within external storage.
          // https://developer.android.com/training/data-storage/app-specific?hl=en#external
          return null;
        } else if (mode == FileAccessMode.READ) {
          return READ_EXTERNAL_STORAGE;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
          // According to the Android documentation:
          // > Note: If your app targets Build.VERSION_CODES.R or higher,
          // > this permission has no effect.
          return WRITE_EXTERNAL_STORAGE;
        } else {
          return null;
        }
      }
    } else if (!path.contains(":")) {
      throw new IllegalArgumentException("path cannot be relative");
    }
    return null;
  }

  /**
   * Move a file from one scope to another. The move will be attempted in an atomic way if
   * supported by the underlying system. IF not, the file will be copied and the original will be
   * deleted. It may be the case that the source file is read-only but isn't reported as such, in
   * which case the move operation will attempt to roll back by deleting the new copy.
   *
   * @param form the form to serve as a context for Android operations
   * @param src the source file
   * @param dest the destination file
   * @return true if the file was moved successfully, false otherwise
   * @throws IOException if an I/O exception occurs while copying or deleting the original file
   */
  public static boolean moveFile(Form form, ScopedFile src, ScopedFile dest) throws IOException {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && src.getScope() != FileScope.Shared && dest.getScope() != FileScope.Shared) {
      // Because Shared files go through the MediaStore, we cannot use the NIO API for moving.
      // New style. Use Java NIO to move the file, potentially between file system providers
      Path source = Paths.get(src.resolve(form).toURI());
      Path destination = Paths.get(dest.resolve(form).toURI());
      Files.move(source, destination, REPLACE_EXISTING);
      return true;
    } else {
      // Old style. Copy the file and then delete the original.
      byte[] buffer = new byte[4096];
      int read;
      InputStream in = null;
      OutputStream out = null;
      try {
        in = openForReading(form, src);
        out = openForWriting(form, dest);
        while ((read = in.read(buffer)) > 0) {
          out.write(buffer, 0, read);
        }
      } finally {
        IOUtils.closeQuietly(LOG_TAG, in);
        IOUtils.closeQuietly(LOG_TAG, out);
      }
      File original = src.resolve(form);
      File copy = dest.resolve(form);
      if (original.delete()) {
        // The file has been "moved"
        return true;
      } else if (copy.delete()) {
        // Deleted the copy since we couldn't remove the original
        return false;
      } else {
        // Made the copy but couldn't clean it up during rollback
        throw new IOException("Unable to delete fresh file");
      }
    }
  }

  /**
   * Remove a directory.
   *
   * <p>This method will recursively remove a directory if {@code recursive} is {@code true}.
   * However, this operation is not atomic, so it is possible that an exception can be thrown
   * and some files will have been deleted and others not.
   *
   * @param directory the directory to remove
   * @param recursive true if the directory's contents should be removed recursively, otherwise
   *                  false, in which case the directory must be empty
   * @return true if the directory was successfully removed
   * @throws IOException if an IO error occurs that prevents removal of the directory
   * @throws NullPointerException if {@code directory} is null
   * @throws IllegalArgumentException if {@code directory} does not represent a directory
   */
  public static boolean removeDirectory(File directory, boolean recursive) throws IOException {
    if (directory == null) {
      throw new NullPointerException();
    }
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException();
    }

    File[] files = directory.listFiles();
    if (files == null) {
      return directory.delete();
    } else if (!recursive && files.length > 0) {
      return false;
    } else {
      boolean success = true;

      for (File child : files) {
        if (child.isDirectory()) {
          success &= removeDirectory(directory, recursive);
        } else {
          success &= child.delete();
        }
      }

      return success && directory.delete();
    }
  }

  /**
   * Open a {@link ScopedFile} for reading as an {@link InputStream}. The caller is responsible for
   * closing the stream returned by this method.
   *
   * @param form the form to use as an Android context
   * @param file the file to open for reading
   * @return a new input stream.
   * @throws IOException if the file cannot be opened
   */
  @SuppressWarnings("deprecation")
  public static InputStream openForReading(Form form, ScopedFile file) throws IOException {
    switch (file.getScope()) {
      case Asset:
        return form.openAsset(file.getFileName());
      case App:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
          return new FileInputStream(new File(Environment.getExternalStorageDirectory(),
              file.getFileName()));
        }
        return new FileInputStream(new File(form.getExternalFilesDir(""), file.getFileName()));
      case Cache:
        return new FileInputStream(new File(URI.create(form.getCachePath(file.getFileName()))));
      case Legacy:
        return new FileInputStream(new File(Environment.getExternalStorageDirectory(),
            file.getFileName()));
      case Private:
        return new FileInputStream(new File(URI.create(form.getPrivatePath(file.getFileName()))));
      case Shared:
        Uri targetUri;
        if (file.getFileName().startsWith("content:")) {
          targetUri = Uri.parse(file.getFileName());
        } else {
          String[] parts = file.getFileName().split("/", 2);
          Uri contentUri = getContentUriForPath(parts[0]);
          String[] projection = new String[]{
              MediaStore.Files.FileColumns._ID,
              MediaStore.Files.FileColumns.DISPLAY_NAME
          };
          Cursor cursor = null;
          try {
            cursor = form.getContentResolver().query(contentUri, projection,
                MediaStore.Files.FileColumns.DISPLAY_NAME + " = ?", new String[]{parts[1]}, null);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            if (cursor.moveToFirst()) {
              long id = cursor.getLong(idColumn);
              targetUri = ContentUris.withAppendedId(contentUri, id);
            } else {
              throw new FileNotFoundException("Unable to find shared file: " + file.getFileName());
            }
          } finally {
            IOUtils.closeQuietly(LOG_TAG, cursor);
          }
        }
        return form.getContentResolver().openInputStream(targetUri);
      default:
        break;
    }
    throw new IOException("Unsupported file scope: " + file.getScope());
  }

  /**
   * Open a {@link ScopedFile} for writing as an {@link OutputStream}. The caller is responsible
   * for closing the stream returned by this method.
   *
   * @param form the form to use as an Android context
   * @param file the file to open for reading
   * @return a new input stream.
   * @throws IOException if the file cannot be opened
   */
  @SuppressWarnings("deprecation")
  public static OutputStream openForWriting(Form form, ScopedFile file) throws IOException {
    switch (file.getScope()) {
      case Asset:
        throw new IOException("Assets are read-only.");
      case App:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
          return new FileOutputStream(new File(Environment.getExternalStorageDirectory(),
              file.getFileName()));
        }
        return new FileOutputStream(new File(form.getExternalFilesDir(""), file.getFileName()));
      case Cache:
        return new FileOutputStream(new File(URI.create(form.getCachePath(file.getFileName()))));
      case Legacy:
        return new FileOutputStream(new File(Environment.getExternalStorageDirectory(),
            file.getFileName()));
      case Private:
        return new FileOutputStream(new File(URI.create(form.getPrivatePath(file.getFileName()))));
      case Shared:
        final ContentResolver resolver = form.getContentResolver();
        if (file.getFileName().startsWith("content:")) {
          Log.d(LOG_TAG, "Opening content URI: " + file.getFileName());
          return resolver.openOutputStream(Uri.parse(file.getFileName()));
        }
        String[] parts = file.getFileName().split("/", 2);
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, parts[1]);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, parts[0]);
        Uri contentUri = getContentUriForPath(parts[0]);
        if (contentUri == null) {
          throw new IOException("Unrecognized shared folder: " + parts[0]);
        }
        Uri uri = resolver.insert(contentUri, values);

        if (uri == null) {
          throw new IOException("Unable to insert MediaStore entry for shared content");
        }

        OutputStream out = resolver.openOutputStream(uri);
        if (out == null) {
          throw new IOException("Unable to open stream for writing");
        }
        return out;
      default:
        break;
    }
    throw new IOException("Unsupported file scope: " + file.getScope());
  }

  /**
   * List the contents of a directory from a given scope.
   *
   * @param form the form to use as an Android context
   * @param file the directory to list
   * @return a list of files in the directory, or null if the given pathname was not a directory
   * @throws IOException if an I/O error occurs while reading the directory
   */
  @SuppressWarnings("checkstyle:FallThrough")
  public static List<String> listDirectory(Form form, ScopedFile file) throws IOException {
    switch (file.getScope()) {
      case Asset:
        if (!form.isRepl()) {
          // Assets are a special case since they are part of the APK, not on disk as individual files
          // ...except in the REPL, where they are stored in the app-specific directory.
          AssetManager manager = form.getAssets();
          String[] files = manager.list(file.getFileName());
          if (files != null) {
            return Arrays.asList(files);
          } else {
            return Collections.emptyList();
          }
        }
        // At this point, the processing logic is the same as all other types because of how the
        // REPL processes assets (which are typically stored in the App directory).
      case App:
      case Cache:
      case Legacy:
      case Private:
        File directory = new File(URI.create(resolveFileName(form, file.getFileName(), file.getScope())));
        String[] files = directory.list();
        if (files != null) {
          return Arrays.asList(files);
        }
        return null;
      case Shared:
        String dirname = file.getFileName();
        if (dirname.startsWith("/")) {
          dirname = dirname.substring(1);
        }
        String[] parts = dirname.split("/", 2);
        if (!dirname.endsWith("/")) {
          dirname += "/";
        }
        final ContentResolver resolver = form.getContentResolver();
        Uri contentUri = getContentUriForPath(parts[0]);
        if (contentUri == null) {
          contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }
        Cursor cursor = null;
        try {
          String dataColumnName = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
              ? MediaStore.Files.FileColumns.DATA : MediaStore.Files.FileColumns.RELATIVE_PATH;
          final String[] columns = new String[] {
              MediaStore.Files.FileColumns.DISPLAY_NAME,
              dataColumnName
          };
          cursor = resolver.query(contentUri, columns, null, null, null);
          final int nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
          final int pathColumn = cursor.getColumnIndex(dataColumnName);
          final List<String> results = new ArrayList<>();
          final String rootPath = QUtil.getExternalStoragePath(form, false, true) + "/";
          while (cursor.moveToNext()) {
            String name = cursor.getString(nameColumn);
            String path = cursor.getString(pathColumn);
            String pathname;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
              pathname = path.replace(rootPath, "");
            } else {
              pathname = path + name;
            }
            if (pathname.startsWith(dirname)) {
              results.add(pathname.substring(dirname.length()));
            }
          }
          return results;
        } finally {
          IOUtils.closeQuietly(LOG_TAG, cursor);
        }
      default:
        throw new IOException("Unsupported file scope: " + file.getScope());
    }
  }

  private static Uri getContentUriForPath(String path) {
    if ("DCIM".equals(path) || "Pictures".equals(path) || "Screenshots".equals(path)) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
      }
      return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    } else if ("Videos".equals(path) || "Movies".equals(path)) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
      }
      return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    } else if ("Audio".equals(path) || "Music".equals(path)) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
      }
      return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        && ("Download".equals(path) || "Downloads".equals(path))) {
      return MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      return MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
    }
    return null;
  }
}
