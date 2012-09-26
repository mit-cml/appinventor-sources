// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.Date;

/**
 * Camcorder provides access to the phone's camcorder
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
public class Camcorder extends AndroidNonvisibleComponent
    implements ActivityResultListener, Component {

  private static final String CAMCORDER_INTENT = "android.media.action.VIDEO_CAPTURE";
  private static final String CAMCORDER_OUTPUT = "output";
  private final ComponentContainer container;
  private Uri clipFile;

  /* Used to identify the call to startActivityForResult. Will be passed back
     into the resultReturned() callback method. */
  private int requestCode;

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
   * Records a video, then raises the AfterRecoding event.
   */
  @SimpleFunction
  public void RecordVideo() {
    Date date = new Date();
    String state = Environment.getExternalStorageState();

    if (Environment.MEDIA_MOUNTED.equals(state)) {
      Log.i("CamcorderComponent", "External storage is available and writable");

      clipFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                                       "/Video/app_inventor_" + date.getTime()
                                       + ".3gp"));

      ContentValues values = new ContentValues();
      values.put(MediaStore.Video.Media.DATA, clipFile.getPath());
      values.put(MediaStore.Video.Media.MIME_TYPE, "clip/3gp");
      values.put(MediaStore.Video.Media.TITLE, clipFile.getLastPathSegment());

      if (requestCode == 0) {
        requestCode = form.registerForActivityResult(this);
      }

      Uri clipUri = container.$context().getContentResolver().insert(
                      MediaStore.Video.Media.INTERNAL_CONTENT_URI, values);
      Intent intent = new Intent(CAMCORDER_INTENT);
      intent.putExtra(CAMCORDER_OUTPUT, clipUri);
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
      File clip = new File(clipFile.getPath());
      if (clip.length() != 0) {
        AfterRecording(clipFile.toString());
      } else {
        deleteFile(clipFile);  // delete empty file
        // see if something useful got returned in the data
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
      }
    } else {
      // delete empty file
      deleteFile(clipFile);
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
   * the stored picture.
   */
  @SimpleEvent
    public void AfterRecording(String clip) {
    EventDispatcher.dispatchEvent(this, "AfterRecording", clip);
  }
}
