// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.Manifest;

import java.io.File;
import java.util.Date;

/**
 * Camera provides access to the phone's camera
 *
 *
 */
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
   description = "A component to take a picture using the device's camera. " +
        "After the picture is taken, the name of the file on the phone " +
        "containing the picture is available as an argument to the " +
        "AfterPicture event. The file name can be used, for example, to set " +
        "the Picture property of an Image component.",
   category = ComponentCategory.MEDIA,
   nonVisible = true,
   iconName = "images/camera.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE," +
                 "android.permission.CAMERA")
public class Camera extends AndroidNonvisibleComponent
    implements ActivityResultListener, Component {

  private static final String CAMERA_INTENT = "android.media.action.IMAGE_CAPTURE";
  private static final String CAMERA_OUTPUT = "output";
  private final ComponentContainer container;
  private Uri imageFile;

  /* Used to identify the call to startActivityForResult. Will be passed back
  into the resultReturned() callback method. */
  private int requestCode;

  // whether to open into the front-facing camera
  private boolean useFront;

  // wether or not we have permission to use the camera

  private boolean havePermission = false;

  /**
   * Creates a Camera component.
   *
   * Camera has a boolean option to request the forward-facing camera via an intent extra.
   *
   * @param container container, component will be placed in
   */
  public Camera(ComponentContainer container) {
    super(container.$form());
    this.container = container;

    // Default property values
    UseFront(false);
  }

  /**
   * Returns true if the front-facing camera is to be used (when available)
   *
   * @return {@code true} indicates front-facing is to be used, {@code false} will open default
   */
  @Deprecated
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean UseFront() {
    return useFront;
  }

  /**
   * Specifies whether the front-facing camera should be used (when available)
   *
   * @param front
   *          {@code true} for front-facing camera, {@code false} for default
   */
  @Deprecated
  // Hide the deprecated property from the Designer
  //  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(description = "Specifies whether the front-facing camera should be used (when available). "
    + "If the device does not have a front-facing camera, this option will be ignored "
    + "and the camera will open normally.")
  public void UseFront(boolean front) {
    useFront = front;
  }

  /**
   * Takes a picture, then raises the AfterPicture event.
   * If useFront is true, adds an extra to the intent that requests the front-facing camera.
   */
  @SimpleFunction
  public void TakePicture() {
    Date date = new Date();
    String state = Environment.getExternalStorageState();
    if (!havePermission) {
      final Camera me = this;
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            form.askPermission(Manifest.permission.CAMERA,
                               new PermissionResultHandler() {
                                 @Override
                                 public void HandlePermissionResponse(String permission, boolean granted) {
                                   if (granted) {
                                     me.havePermission = true;
                                     me.TakePicture();
                                   } else {
                                     form.dispatchPermissionDeniedEvent(me, "TakePicture",
                                         Manifest.permission.CAMERA);
                                   }
                                 }
                               });
          }
        });
      return;
    }
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      Log.i("CameraComponent", "External storage is available and writable");

      imageFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
        "/Pictures/app_inventor_" + date.getTime()
        + ".jpg"));

      ContentValues values = new ContentValues();
      values.put(MediaStore.Images.Media.DATA, imageFile.getPath());
      values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
      values.put(MediaStore.Images.Media.TITLE, imageFile.getLastPathSegment());

      if (requestCode == 0) {
        requestCode = form.registerForActivityResult(this);
      }

      Uri imageUri = container.$context().getContentResolver().insert(
        MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
      Intent intent = new Intent(CAMERA_INTENT);
      intent.putExtra(CAMERA_OUTPUT, imageUri);

      // NOTE: This uses an undocumented, testing feature (CAMERA_FACING).
      // It may not work in the future.
      if (useFront) {
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
      }

      container.$context().startActivityForResult(intent, requestCode);
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      form.dispatchErrorOccurredEvent(this, "TakePicture",
          ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_READONLY);
    } else {
      form.dispatchErrorOccurredEvent(this, "TakePicture",
          ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE);
    }
  }

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    Log.i("CameraComponent",
      "Returning result. Request code = " + requestCode + ", result code = " + resultCode);
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      File image = new File(imageFile.getPath());
      if (image.length() != 0) {
        scanFileToAdd(image);
        AfterPicture(imageFile.toString());
      } else {
        deleteFile(imageFile);  // delete empty file
        // see if something useful got returned in the data
        if (data != null && data.getData() != null) {
          Uri tryImageUri = data.getData();
          Log.i("CameraComponent", "Calling Camera.AfterPicture with image path "
              + tryImageUri.toString());
          AfterPicture(tryImageUri.toString());
        } else {
          Log.i("CameraComponent", "Couldn't find an image file from the Camera result");
          form.dispatchErrorOccurredEvent(this, "TakePicture",
              ErrorMessages.ERROR_CAMERA_NO_IMAGE_RETURNED);
        }
      }
    } else {
      // delete empty file
      deleteFile(imageFile);
    }
  }

  /**
   * Scan the newly added picture to be displayed in a default media content provider
   * in a device (e.g. Gallery, Google Photo, etc..)
   *
   * @param image the picture taken by Camera component
   */
  private void scanFileToAdd(File image) {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    Uri contentUri = Uri.fromFile(image);
    mediaScanIntent.setData(contentUri);
    container.$context().getApplicationContext().sendBroadcast(mediaScanIntent);
  }

  private void deleteFile(Uri fileUri) {
    File fileToDelete = new File(fileUri.getPath());
    try {
      if (fileToDelete.delete()) {
        Log.i("CameraComponent", "Deleted file " + fileUri.toString());
      } else {
        Log.i("CameraComponent", "Could not delete file " + fileUri.toString());
      }
    } catch (SecurityException e) {
      Log.i("CameraComponent", "Got security exception trying to delete file "
          + fileUri.toString());
    }
  }

  /**
   * Indicates that a photo was taken with the camera and provides the path to
   * the stored picture.
   */
  @SimpleEvent
  public void AfterPicture(String image) {
    EventDispatcher.dispatchEvent(this, "AfterPicture", image);
  }
}
