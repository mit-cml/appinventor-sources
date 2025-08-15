// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;

import android.content.ContentValues;
import android.content.Intent;

import android.net.Uri;

import android.os.Build;
import android.os.Environment;

import android.provider.MediaStore;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.BulkPermissionRequest;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.NougatUtil;
import com.google.appinventor.components.runtime.util.ScopedFile;

import java.io.File;

import java.net.URI;

/**
 * ![Camera icon](images/camera.png)
 *
 * Use a camera component to take a picture on the phone.
 *
 * `Camera` is a non-visible component that takes a picture using the device's camera. After the
 * picture is taken, the path to the file on the phone containing the picture is available as an
 * argument to the {@link #AfterPicture(String)} event. The path can be used, for example, as the
 * [`Picture`](userinterface.html#Image.Picture) property of an [`Image`](userinterface.html3Image)
 * component.
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
@UsesPermissions({CAMERA})
public class Camera extends AndroidNonvisibleComponent
    implements ActivityResultListener, Component {

  private static final String CAMERA_INTENT = MediaStore.ACTION_IMAGE_CAPTURE;
  private static final String CAMERA_OUTPUT = MediaStore.EXTRA_OUTPUT;
  private final ComponentContainer container;
  private Uri imageFile;

  /* Used to identify the call to startActivityForResult. Will be passed back
  into the resultReturned() callback method. */
  private int requestCode;

  // whether to open into the front-facing camera
  private boolean useFront;

  // whether or not we have permission to use the camera

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
   * Determine whether we have the right permissions during initialization.
   */
  public void Initialize() {
    havePermission = !form.isDeniedPermission(CAMERA);
    if (FileUtil.needsWritePermission(form.DefaultFileScope())) {
      havePermission &= !form.isDeniedPermission(WRITE_EXTERNAL_STORAGE);
    }
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
   * Takes a picture, then raises the {@link #AfterPicture(String)} event.
   *
   * @internaldoc
   * If useFront is true, adds an extra to the intent that requests the front-facing camera.
   */
  @SimpleFunction
  public void TakePicture() {
    final ScopedFile target = FileUtil.getScopedPictureFile(form, "jpg");
    if (!havePermission) {
      String[] permissions;
      if (FileUtil.needsWritePermission(target)) {
        permissions = new String[] { CAMERA, WRITE_EXTERNAL_STORAGE };
      } else {
        permissions = new String[] { CAMERA };
      }
      form.askPermission(new BulkPermissionRequest(this, "TakePicture", permissions) {
        @Override
        public void onGranted() {
          havePermission = true;
          Camera.this.TakePicture();
        }
      });
      return;
    }

    String state = Environment.getExternalStorageState();
    if (!FileUtil.needsExternalStorage(form, target) || Environment.MEDIA_MOUNTED.equals(state)) {
      Log.i("CameraComponent", "External storage is available and writable");
      capturePicture(target);
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      form.dispatchErrorOccurredEvent(this, "TakePicture",
          ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_READONLY);
    } else {
      form.dispatchErrorOccurredEvent(this, "TakePicture",
          ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE);
    }
  }

  private void capturePicture(final ScopedFile target) {
    File path = new File(URI.create(FileUtil.resolveFileName(form, target.getFileName(), target.getScope())));
    File directory = path.getParentFile();
    if (!directory.exists()) {
      directory.mkdirs();
    }
    imageFile = Uri.fromFile(path);

    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DATA, imageFile.getPath());
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.Images.Media.TITLE, imageFile.getLastPathSegment());

    if (requestCode == 0) {
      requestCode = form.registerForActivityResult(this);
    }

    Uri imageUri;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      imageUri = container.$context().getContentResolver().insert(
          MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
    } else {
      imageUri = NougatUtil.getPackageUri(form, path);
    }
    Intent intent = new Intent(CAMERA_INTENT);
    intent.putExtra(CAMERA_OUTPUT, imageUri);

    // NOTE: This uses an undocumented, testing feature (CAMERA_FACING).
    // It may not work in the future.
    if (useFront) {
      intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
    }

    container.$context().startActivityForResult(intent, requestCode);
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
    Uri contentUri = NougatUtil.getPackageUri(form, image);
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
   * Called after the picture is taken. The text argument `image` is the path that can be used to
   * locate the image on the phone.
   */
  @SimpleEvent
  public void AfterPicture(String image) {
    EventDispatcher.dispatchEvent(this, "AfterPicture", image);
  }
}
