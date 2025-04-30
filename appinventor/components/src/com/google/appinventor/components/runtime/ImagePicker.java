// -*- Mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;

import android.content.ContentResolver;
import android.content.Intent;

import android.net.Uri;

import android.provider.MediaStore;

import android.util.Log;

import android.webkit.MimeTypeMap;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.QUtil;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A special-purpose button. When the user taps an `ImagePicker`, the device's image gallery
 * appears, and the user can choose an image. After an image is picked, it is saved, and the
 * {@link #Selection()} property will be the name of the file where the image is stored. In order
 * to not fill up storage, a maximum of 10 images will be stored. Picking more images will delete
 * previous images, in order from oldest to newest.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
@DesignerComponent(version = YaVersion.IMAGEPICKER_COMPONENT_VERSION,
    description = "A special-purpose button. When the user taps an image picker, the " +
          "device's image gallery appears, and the user can choose an image. After an image is " +
          "picked, it is saved, and the <code>Selected</code> " +
          "property will be the name of the file where the image is stored. In order to not " +
          "fill up storage, a maximum of 10 images will be stored.  Picking more images " +
          "will delete previous images, in order from oldest to newest.",
    category = ComponentCategory.MEDIA)
@SimpleObject
public class ImagePicker extends Picker implements ActivityResultListener {

  private static final String LOG_TAG = "ImagePicker";

  // directory on external storage for storing the files for the saved images
  private static final String imagePickerDirectoryName = "/Pictures/_app_inventor_image_picker";

  // prefix for image file names
  private static final String FILE_PREFIX = "picked_image";

 // max number of files to save in image directory
  private static final int maxSavedFiles = 10;

  // The media path (URI) for the selected image file created by MediaUtil
  private String selectionURI;

  // The path to the saved image
  private String selectionSavedImage = "";

  // Flag to indicate whether we have permission to write imgaes to external storage
  private boolean havePermission = false;

  /**
   * Create a new ImagePicker component.
   *
   * @param container the parent container.
   */
  public ImagePicker(ComponentContainer container) {
    super(container);
  }

  /**
   * Path to the file containing the image that was selected.
   */
  @SimpleProperty(description = "Path to the file containing the image that was selected.",
      category = PropertyCategory.BEHAVIOR)
  public String Selection() {
    return selectionSavedImage;
  }

  @Override
  protected Intent getIntent() {
    return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
  }

  @Override
  public void click() {
    if (!havePermission && FileUtil.needsWritePermission(container.$form().DefaultFileScope())) {
      container.$form().askPermission(WRITE_EXTERNAL_STORAGE,
          new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
              if (granted) {
                havePermission = true;
                click();
              } else {
                container.$form().dispatchPermissionDeniedEvent(ImagePicker.this, "Click",
                    permission);
              }
            }
          });
      return;
    }
    super.click();
  }

  /**
   * Callback method to get the result returned by the image picker activity
   *
   * @param requestCode a code identifying the request.
   * @param resultCode a code specifying success or failure of the activity
   * @param data the returned data, in this case an Intent whose data field
   *        contains the image's content URI.
   */
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      Uri selectedImage = data.getData();
      selectionURI = selectedImage.toString();
      Log.i(LOG_TAG, "selectionURI = " + selectionURI);

      // get the file type extension from the intent data Uri
      ContentResolver cR = container.$context().getContentResolver();
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      String extension = "." + mime.getExtensionFromMimeType(cR.getType(selectedImage));
      Log.i(LOG_TAG, "extension = " + extension);

      // save the image to a temp file in external storage, using a name
      // that includes the extension
      saveSelectedImageToExternalStorage(extension);
      AfterPicking();
    }
  }

  private void saveSelectedImageToExternalStorage(String extension) {
    // clear imageFile for new save attempt
    // This will be the stored picture
    selectionSavedImage = "";
    // create a temp file for holding the image that was picked
    // This is not the external stored file: This is in the internal directory used by MediaUtil
    File tempFile = null;

    // copy the picture at the image URI to the temp file
    try {
      tempFile = MediaUtil.copyMediaToTempFile(container.$form(), selectionURI);
    } catch (IOException e) {
      Log.i(LOG_TAG, "copyMediaToTempFile failed: " + e.getMessage());
      container.$form().dispatchErrorOccurredEvent(this, "ImagePicker",
          ErrorMessages.ERROR_CANNOT_COPY_MEDIA, e.getMessage());
      return;
    }

    // copy the temp file to external storage
    Log.i(LOG_TAG, "temp file path is: " + tempFile.getPath());
    // Copy file will signal a screen error if the copy fails.
    copyToExternalStorageAndDeleteSource(tempFile, extension);
  }

  private void copyToExternalStorageAndDeleteSource(File source, String extension) {

    File dest = null;

    String fullDirname = QUtil.getExternalStoragePath(container.$form()) + imagePickerDirectoryName;
    File destDirectory = new File(fullDirname);

    try {
      destDirectory.mkdirs();
      dest = File.createTempFile (FILE_PREFIX, extension,  destDirectory);

      selectionSavedImage = dest.getPath();
      // Uncomment this to delete imageFile when the application stops
      // dest.deleteOnExit();
      Log.i(LOG_TAG, "saved file path is: " + selectionSavedImage);

      FileUtil.copyFile(source.getAbsolutePath(), dest.getAbsolutePath());

      Log.i(LOG_TAG, "Image was copied to " + selectionSavedImage);
      // this can be uncommented to show the alert, but the alert
      // is pretty annoying
      // new (container.$form()).ShowAlert("Image was copied to " + selectedImage);

    } catch(IOException e) {
      String err =  "destination is " + selectionSavedImage + ": " + "error is "  + e.getMessage();
      Log.i(LOG_TAG, "copyFile failed. " + err);
      container.$form().dispatchErrorOccurredEvent(this, "SaveImage",
          ErrorMessages.ERROR_CANNOT_SAVE_IMAGE, err);
      selectionSavedImage = "";
      dest.delete();
    }

    // clean up the temp file.  This isn't critical because MudiaUtil.copyMediaToTempFile
    // marks this with deleteOnExit, but it's nice to clean up here.
    source.delete();
    trimDirectory(maxSavedFiles, destDirectory);
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
