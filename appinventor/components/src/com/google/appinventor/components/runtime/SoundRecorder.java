// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;

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
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    iconName = "images/soundRecorder.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO")
public final class SoundRecorder extends AndroidNonvisibleComponent
    implements Component, OnErrorListener, OnInfoListener {

  private static final String TAG = "SoundRecorder";

  /**
   * This class encapsulates the required state during recording.
   */
  private class RecordingController {
    final MediaRecorder recorder;
    final String file;

    RecordingController() throws IOException {
      file = FileUtil.getRecordingFile("3gp").getAbsolutePath();
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

    void start() {
      Log.i(TAG, "starting");
      recorder.start();
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
   * Starts recording.
   */
  @SimpleFunction
  public void Start() {
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
      controller = new RecordingController();
    } catch (Throwable t) {
      form.dispatchErrorOccurredEvent(
          this, "Start", ErrorMessages.ERROR_SOUND_RECORDER_CANNOT_CREATE, t.getMessage());
      return;
    }
    try {
      controller.start();
    } catch (Throwable t) {
      controller.stop();
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
    Log.i(TAG, "Recoverable condition while recording. Will attempt to stop normally.");
    controller.recorder.stop();
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
