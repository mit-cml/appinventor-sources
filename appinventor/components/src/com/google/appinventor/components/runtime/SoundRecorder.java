// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
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
import com.google.appinventor.components.runtime.util.FileUtil;
import android.Manifest;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/**
 * Multimedia component that records audio using
 * {@link android.media.MediaRecorder}.
 *
 */
@DesignerComponent(version = YaVersion.SOUND_RECORDER_COMPONENT_VERSION,
    description = "<p>Multimedia component that records audio.</p>",
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/soundRecorder.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO," +
  "android.permission.WRITE_EXTERNAL_STORAGE," +
  "android.permission.READ_EXTERNAL_STORAGE")
public final class SoundRecorder extends AndroidNonvisibleComponent
    implements Component, OnErrorListener, OnInfoListener {

  private static final String TAG = "SoundRecorder";

  // the path to the savedRecording
  // if it is the null string, the recorder will generate a path
  // note that this is also initialized to "" in the designer
  private String savedRecording = "";

  // Whether or not we have the RECORD_AUDIO permission
  private boolean havePermission = false;

  /**
   * This class encapsulates the required state during recording.
   */
  private class RecordingController {
    final MediaRecorder recorder;

    // file is the same as savedRecording, but we'll keep it local to the
    // RecordingController for future flexibility
     final String file;

    RecordingController(String savedRecording) throws IOException {
      // pick a pathname if none was specified
      file = (savedRecording.equals("")) ?
          FileUtil.getRecordingFile("3gp").getAbsolutePath() :
            savedRecording;

      recorder = new MediaRecorder();
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
      Log.i(TAG, "Setting output file to " + file);
      recorder.setOutputFile(file);
      Log.i(TAG, "preparing");
      recorder.prepare();
      recorder.setOnErrorListener(SoundRecorder.this);
      recorder.setOnInfoListener(SoundRecorder.this);
    }

    void start() throws IllegalStateException {
      Log.i(TAG, "starting");

      try {
        recorder.start();
      } catch (IllegalStateException e) {
        // This is the error produced when there are two recorders running.
        // There might be other causes, but we don't know them.
        // Using Log.e will log a stack trace, so we can investigate
        Log.e(TAG, "got IllegalStateException. Are there two recorders running?", e);
        // Pass back a message detail for dispatchErrorOccurred to
        // show at user level
        throw (new IllegalStateException("Is there another recording running?"));
      }
    }

    void stop() {
      recorder.setOnErrorListener(null);
      recorder.setOnInfoListener(null);
      recorder.stop();
      recorder.reset();
      recorder.release();
    }
  }

  /*
   * This is null when not recording, and contains the active RecordingState
   * when recording.
   */
  private RecordingController controller;

  public SoundRecorder(final ComponentContainer container) {
    super(container.$form());
  }


  /**
   * Returns the path to the saved recording
   *
   * @return  savedRecording path to recording
   */
  @SimpleProperty(
      description = "Specifies the path to the file where the recording should be stored. " +
          "If this proprety is the empty string, then starting a recording will create a file in " +
          "an appropriate location.  If the property is not the empty string, it should specify " +
          "a complete path to a file in an existing directory, including a file name with the " +
          "extension .3gp." ,
          category = PropertyCategory.BEHAVIOR)
  public String SavedRecording() {
    return savedRecording;
  }

  /**
   * Specifies the path to the saved recording displayed by the label.
   *
   * @param pathName  path to saved recording
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void SavedRecording(String pathName) {
    savedRecording = pathName;
  }

  /**
   * Starts recording.
   */
  @SimpleFunction
  public void Start() {
      // Need to check if we have RECORD_AUDIO permission
    if (!havePermission) {
      final SoundRecorder me = this;
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            form.askPermission(Manifest.permission.RECORD_AUDIO,
              new PermissionResultHandler() {
                @Override
                public void HandlePermissionResponse(String permission, boolean granted) {
                  if (granted) {
                    me.havePermission = true;
                    me.Start();
                  } else {
                    form.dispatchErrorOccurredEvent(me, "SoundRecorder",
                      ErrorMessages.ERROR_SOUND_NO_PERMISSION);
                  }
                }
              });
          }
        });
      return;
    }

    if (controller != null) {
      Log.i(TAG, "Start() called, but already recording to " + controller.file);
      return;
    }
    Log.i(TAG, "Start() called");
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      form.dispatchErrorOccurredEvent(
          this, "Start", ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE);
      return;
    }
    try {
      controller = new RecordingController(savedRecording);
    } catch (Throwable t) {
      form.dispatchErrorOccurredEvent(
          this, "Start", ErrorMessages.ERROR_SOUND_RECORDER_CANNOT_CREATE, t.getMessage());
      return;
    }
    try {
      controller.start();
    } catch (Throwable t) {
      // I'm commenting the next line out because stop can throw an error, and
      // it's not clear to me how to handle that.
      // controller.stop();
      controller = null;
      form.dispatchErrorOccurredEvent(
          this, "Start", ErrorMessages.ERROR_SOUND_RECORDER_CANNOT_CREATE, t.getMessage());
      return;
    }
    StartedRecording();
  }

  @Override
  public void onError(MediaRecorder affectedRecorder, int what, int extra) {
    if (controller == null || affectedRecorder != controller.recorder) {
      Log.w(TAG, "onError called with wrong recorder. Ignoring.");
      return;
    }
    form.dispatchErrorOccurredEvent(this, "onError", ErrorMessages.ERROR_SOUND_RECORDER);
    try {
      controller.stop();
    } catch (Throwable e) {
      Log.w(TAG, e.getMessage());
    } finally {
      controller = null;
      StoppedRecording();
    }
  }

  @Override
  public void onInfo(MediaRecorder affectedRecorder, int what, int extra) {
    if (controller == null || affectedRecorder != controller.recorder) {
      Log.w(TAG, "onInfo called with wrong recorder. Ignoring.");
      return;
    }
    switch (what) {
    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
      form.dispatchErrorOccurredEvent(this, "recording",
          ErrorMessages.ERROR_SOUND_RECORDER_MAX_DURATION_REACHED);
      break;
    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
      form.dispatchErrorOccurredEvent(this, "recording",
          ErrorMessages.ERROR_SOUND_RECORDER_MAX_FILESIZE_REACHED);
      break;
    case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
      form.dispatchErrorOccurredEvent(this, "recording", ErrorMessages.ERROR_SOUND_RECORDER);
      break;
    default:
      // value of `what` is not valid, probably device-specific debugging. escape early to prevent
      // stoppage until we see an Android-defined error. See also:
      // http://stackoverflow.com/questions/25785420/mediarecorder-oninfolistener-giving-an-895
      return;
    }
    try {
      Log.i(TAG, "Recoverable condition while recording. Will attempt to stop normally.");
      controller.recorder.stop();
    } catch(IllegalStateException e) {
      Log.i(TAG, "SoundRecorder was not in a recording state.", e);
      form.dispatchErrorOccurredEventDialog(this, "Stop",
          ErrorMessages.ERROR_SOUND_RECORDER_ILLEGAL_STOP);
    } finally {
      controller = null;
      StoppedRecording();
    }
  }

  /**
   * Stops recording.
   */
  @SimpleFunction
  public void Stop() {
    if (controller == null) {
      Log.i(TAG, "Stop() called, but already stopped.");
      return;
    }
    try {
      Log.i(TAG, "Stop() called");
      Log.i(TAG, "stopping");
      controller.stop();
      Log.i(TAG, "Firing AfterSoundRecorded with " + controller.file);
      AfterSoundRecorded(controller.file);
    } catch (Throwable t) {
      form.dispatchErrorOccurredEvent(this, "Stop", ErrorMessages.ERROR_SOUND_RECORDER);
    } finally {
      controller = null;
      StoppedRecording();
    }
  }

  @SimpleEvent(description = "Provides the location of the newly created sound.")
  public void AfterSoundRecorded(final String sound) {
    EventDispatcher.dispatchEvent(this, "AfterSoundRecorded", sound);
  }

  @SimpleEvent(description = "Indicates that the recorder has started, and can be stopped.")
  public void StartedRecording() {
    EventDispatcher.dispatchEvent(this, "StartedRecording");
  }

  @SimpleEvent(description = "Indicates that the recorder has stopped, and can be started again.")
  public void StoppedRecording() {
    EventDispatcher.dispatchEvent(this, "StoppedRecording");
  }
}
