// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package net.qyv.binfile;

import android.app.Activity;

import android.os.Environment;

import android.util.Base64;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;

import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A Component for reading potentially binary files and returning
 * their contents as a base64 encoded string. It runs synchronously on
 * the UI thread and stores file contents in memory (up to two copies
 * simultaneously) but it is easy to use and should work reasonably
 * well for pictures taken with a device camera and small sound files.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */
@DesignerComponent(version = 1,
  description = "Non-visible component for reading and writing potentially binary files. " +
    "All I/O is done synchronously (on the UI Thread) and contents have to fit into memory. " +
    "We do things this way to make it easier on the App Inventor programmer, but it comes with " +
    "a trade-off in terms of the size of files that can be manipulated.",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/file.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class BinFile extends AndroidNonvisibleComponent implements Component {
  public static final String NO_ASSETS = "No_Assets";
  private final Activity activity;
  private boolean isRepl = false;
  private final int BUFFER_LENGTH = 4096;
  private static final String LOG_TAG = "BinFileComponent";
  private static final String BINFILE_DIR = "/AppInventorBinaries";

  /**
   * Creates a new BinFile component.
   * @param container the Form that this component is contained in.
   */
  public BinFile(ComponentContainer container) {
    super(container.$form());
    if (form instanceof ReplForm) { // Note: form is defined in our superclass
      isRepl = true;
    }
    activity = (Activity) container.$context();
  }

  /**
   * Accepts a base64 encoded string and a file extension (which must be three characters).
   * Decodes the string into a binary and saves it to a file on external storage and returns
   * the filename assigned.
   *
   * @param input Base64 input string
   * @param fileExtension three character file extension
   * @return the name of the created file
   */
  @SimpleFunction(description = "Saves the input to a temporary file. The file extension (last three " +
    "characters) are provided by the fileExtension argument. The input " +
    "should be a properly formatted base64 encoded string.")
  public String Write(String input, String fileExtension) {
    try {
      if (fileExtension.length() != 3) {
        signalError("Write", "File Extension must be three characters");
        return "";
      }
      byte [] content = Base64.decode(input, Base64.DEFAULT);
      String fullDirName = Environment.getExternalStorageDirectory() + BINFILE_DIR;
      File destDirectory = new File(fullDirName);
      destDirectory.mkdirs();
      File dest = File.createTempFile("BinFile", "." + fileExtension, destDirectory);
      FileOutputStream outStream = new FileOutputStream(dest);
      outStream.write(content);
      outStream.close();
      String retval = dest.toURI().toASCIIString();
      trimDirectory(20, destDirectory);
      return retval;
    } catch (Exception e) {
      signalError("Write", e.getMessage());
      return "";
    }
  }

  /**
   * Accepts a file name and returns a Yail List with two
   * elements. the first element is the file's extension (example:
   * jpg, gif, etc.). The second element is the base64 encoded
   * contents of the file. This function is suitable for reading
   * binary files such as sounds and images. The base64 contents can
   * then be stored with mechanisms oriented around text, such as
   * tinyDB, Fusion tables and Firebase.
   *
   * @param filename the filename to read
   * @returns YailList the list of the file extension and contents
   */
  @SimpleFunction(description = "Reads from a specified file and returns " +
    "a list containing the file’s extension and the base64 contents of the file.")
  public YailList Read(String fileName) {
    try {
      String originalFileName = fileName;
      // Trim off file:// part if present
      if (fileName.startsWith("file://")) {
        fileName = fileName.substring(7);
      }
      if (!fileName.startsWith("/")) {
        signalError("ReadFrom", "Invalid fileName, was " + originalFileName);
        return YailList.makeEmptyList();
      }
      File inputFile = new File(fileName);
      if (!inputFile.isFile()) {
        signalError("ReadFrom", "Cannot find file");
        return YailList.makeEmptyList();
      }
      String extension = getFileExtension(fileName);
      FileInputStream inputStream = new FileInputStream(inputFile);
      byte [] content = new byte[(int)inputFile.length()];
      int bytesRead = inputStream.read(content);
      if (bytesRead != inputFile.length()) {
        Log.w(LOG_TAG, "bytesRead != inputFile.length() (" + bytesRead + ") ("
          + inputFile.length() + ")");
        signalError("Read", "Did not read complete file!");
        return YailList.makeEmptyList();
      }
      inputStream.close();
      String encodedContent = Base64.encodeToString(content, Base64.DEFAULT);
      Object [] results = new Object[2];
      results[0] = extension;
      results[1] = encodedContent;
      return YailList.makeList(results);
    } catch (FileNotFoundException e) {
      Log.e(LOG_TAG, "FileNotFoundException", e);
      signalError("Read", e.getMessage());
    } catch (IOException e) {
      Log.e(LOG_TAG, "IOException", e);
      signalError("Read", e.getMessage());
    }
    return YailList.makeEmptyList();
  }

  /*
   * Signal that an error has occurred. We are an extension so we cannot directly
   * use the normal error handling used by builtin App Inventor components.
   */
  private void signalError(final String functionName, final String message) {
    activity.runOnUiThread(new Runnable() {
        public void run() {
          form.ErrorOccurred(BinFile.this, functionName, 9999, message);
        }
      });
  }

  private String getFileExtension(String fullName) {
    String fileName = new File(fullName).getName();
    int dotIndex = fileName.lastIndexOf(".");
    return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
  }

  // keep only the last N files, where N = maxSavedFiles
  private void trimDirectory(int maxSavedFiles, File directory) {

    File[] files = directory.listFiles();

    Arrays.sort(files, new Comparator<File>(){
      public int compare(File f1, File f2)
      {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      } });

    int excess = files.length - maxSavedFiles;
    for (int i = 0; i < excess; i++) {
      files[i].delete();
    }

  }

}
