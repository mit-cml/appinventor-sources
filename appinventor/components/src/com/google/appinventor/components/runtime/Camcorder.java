// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;

import android.content.Intent;

import android.net.Uri;

import android.os.Environment;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;

import java.io.File;

/**
 * ![Camcorder icon](images/camcorder.png)
 *
 * A component to record a video using the device's camcorder. After the video is recorded, the
 * name of the file on the phone containing the clip is available as an argument to the
 * {@link #AfterRecording(String)} event. The file name can be used, for example, to set the source
 * property of a {@link VideoPlayer} component.
 */

@DesignerComponent(version = YaVersion.CAMCORDER_COMPONENT_VERSION,
  description = "A component to record a video using the device's camcorder." +
  "After the video is recorded, the name of the file on the phone " +
  "containing the clip is available as an argument to the " +
  "AfterRecording event. The file name can be used, for example, to set " +
  "the source property of a VideoPlayer component.",
  category = ComponentCategory.MEDIA,
  nonVisible = true,
  iconName = "images/camcorder.png")
@SimpleObject
@UsesPermissions({CAMERA})
public class Camcorder extends AndroidNonvisibleComponent
  implements ActivityResultListener, Component {

  private static final String CAMCORDER_INTENT = "android.media.action.VIDEO_CAPTURE";
  private final ComponentContainer container;

  /* Used to identify the call to startActivityForResult. Will be passed back
     into the resultReturned() callback method. */
  private int requestCode;

  // Have camera permission
  private boolean havePermission = false;

  /**
   * Creates a Camcorder component.
   *
   * @param container container, component will be placed in
   */
  public Camcorder(ComponentContainer container) {
    super(container.$form());
    this.container = container;
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
   * Records a video, then raises the {@link #AfterRecording(String)} event.
   */
  @SimpleFunction
  public void RecordVideo() {
    String state = Environment.getExternalStorageState();
    if (!havePermission) {
      final Camcorder me = this;
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            form.askPermission(CAMERA,
                               new PermissionResultHandler() {
                                 @Override
                                 public void HandlePermissionResponse(String permission, boolean granted) {
                                   if (granted) {
                                     me.havePermission = true;
                                     me.RecordVideo();
                                   } else {
                                     form.dispatchPermissionDeniedEvent(me, "RecordVideo",
                                         CAMERA);
                                   }
                                 }
                               });
          }
        });
      return;
    }

    if (Environment.MEDIA_MOUNTED.equals(state)) {
      Log.i("CamcorderComponent", "External storage is available and writable");

      if (requestCode == 0) {
        requestCode = form.registerForActivityResult(this);
      }

      Intent intent = new Intent(CAMCORDER_INTENT);
      container.$context().startActivityForResult(intent, requestCode);
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      form.dispatchErrorOccurredEvent(this, "RecordVideo",
        ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_READONLY);
    } else {
      form.dispatchErrorOccurredEvent(this, "RecordVideo",
        ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE);
    }
  }

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    Log.i("CamcorderComponent",
      "Returning result. Request code = " + requestCode + ", result code = " + resultCode);
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      if (data != null && data.getData() != null) {
        Uri tryClipUri = data.getData();
        Log.i("CamcorderComponent", "Calling Camcorder.AfterPicture with clip path "
          + tryClipUri.toString());
        AfterRecording(tryClipUri.toString());
      } else {
        Log.i("CamcorderComponent", "Couldn't find a clip file from the Camcorder result");
        form.dispatchErrorOccurredEvent(this, "TakeVideo",
          ErrorMessages.ERROR_CAMCORDER_NO_CLIP_RETURNED);
      }
    } else {
      Log.i("CamcorderComponent", "No clip filed rerturn; request failed");
      form.dispatchErrorOccurredEvent(this, "TakeVideo",
        ErrorMessages.ERROR_CAMCORDER_NO_CLIP_RETURNED);
    }
  }

  private void deleteFile(Uri fileUri) {
    File fileToDelete = new File(fileUri.getPath());
    try {
      if (fileToDelete.delete()) {
        Log.i("CamcorderComponent", "Deleted file " + fileUri.toString());
      } else {
        Log.i("CamcorderComponent", "Could not delete file " + fileUri.toString());
      }
    } catch (SecurityException e) {
      Log.i("CamcorderComponent", "Got security exception trying to delete file "
        + fileUri.toString());
    }
  }

  /**
   * Indicates that a video was recorded with the camera and provides the path to
   * the stored video.
   */
  @SimpleEvent
  public void AfterRecording(String clip) {
    EventDispatcher.dispatchEvent(this, "AfterRecording", clip);
  }
}
