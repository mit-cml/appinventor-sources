// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import android.content.res.AssetFileDescriptor;
import com.google.android.filament.MaterialInstance;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.android.filament.Material;
import com.google.android.filament.Stream;
import android.media.MediaPlayer;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import java.io.File;

import android.util.Log;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;

import java.io.IOException;


// TODO: either supply a simple quad or make one
@UsesAssets(fileNames = "plane.obj, Palette.png")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a video in an ARView3D.  The video is positioned " +
        "at a point, and the source, or video to be played, can be set." +
        "<p>App Inventor for Android only permits video files under 1 MB and " +
        "limits the total size of an application to 5 MB, not all of which is " +
        "available for media (video, audio, and sound) files.  If your media " +
        "files are too large, you may get errors when packaging or installing " +
        "your application, in which case you should reduce the number of media " +
        "files or their sizes.  Most video editing software, such as Windows " +
        "Movie Maker and Apple iMovie, can help you decrease the size of videos " +
        "by shortening them or re-encoding the video into a more compact format.</p>",
    category = ComponentCategory.AR)
@SimpleObject
public final class VideoNode extends ARNodeBase implements ARVideo {

  private String objectModel = Form.ASSETS_PREFIX + "plane.obj";
  private String texture = Form.ASSETS_PREFIX + "Palette.png";
  private MediaPlayer mediaPlayer;
  private SurfaceTexture videoSurfaceTexture;
  private Surface videoSurface;
  private Stream videoStream;
  private Texture videoTexture;

  private static final String LOG_TAG = "VideoNode";

  private float widthInCentimeters = 50.0f;
  private float heightInCentimeters = 37.5f;
  private String videoSource = "";
  private int volume = 100;

  private boolean isInitialized = false;
  private boolean shouldAutoPlay = false;
  private boolean isFirstFrameReady = false;

  private Material videoMaterial;
  private MaterialInstance videoMaterialInstance;
  private int videoEntity;
  private ARNodeContainer container;

  public VideoNode(ARNodeContainer container) {
    super(container);
    // Additional updates
      Model( objectModel);
      Texture(texture);
    container.addNode(this);
    container = container;
  }

  /**
   * Create plane geometry for video
   */
  /*private void createVideoPlaneGeometry() {
    updateGeometry
  }*/


  private void updateVideoPlaneSize() {
    if (!isInitialized || videoEntity == 0) {
      return;
    }
    // Recreate geometry with new dimensions
    //createVideoPlaneGeometry();
    Log.i(LOG_TAG, "Updated video size: " + widthInCentimeters + "x" + heightInCentimeters + " cm");
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty(
      description = "The path to the video file, which should be added in the Designer.",
      category = PropertyCategory.BEHAVIOR)
  public void Source(String path) {
    videoSource = path;
    loadVideoSource(path);
  }

  private void loadVideoSource(String path) {
    if (mediaPlayer == null) {
      Log.e("VideoNode", "MediaPlayer not initialized");
      return;
    }

    try {
      // Reset media player
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.stop();
      }
      mediaPlayer.reset();

      // Re-attach surface after reset
      mediaPlayer.setSurface(videoSurface);

      // Load video source
      File videoFile = new java.io.File(path);
      if (videoFile.exists()) {
        mediaPlayer.setDataSource(videoFile.getAbsolutePath());
        Log.i(LOG_TAG, "Loading video from file: " + path);
      } else {
        // Try as asset
        AssetFileDescriptor afd = container.$context().getAssets().openFd(path);
        mediaPlayer.setDataSource(afd.getFileDescriptor(),
            afd.getStartOffset(), afd.getLength());
        afd.close();
        Log.i(LOG_TAG, "Loading video from assets: " + path);
      }

      // Prepare asynchronously
      mediaPlayer.prepareAsync();

    } catch (IOException e) {
      Log.e(LOG_TAG, "Failed to load video source: " + path, e);
      //container.$context().dispatchErrorOccurredEvent(
      //    this, "Source", ErrorMessages.ERROR_UNABLE_TO_LOAD_MEDIA, path);
    }
  }

  @Override
  @SimpleProperty(description = "Returns true if the video is currently playing.")
  public boolean IsPlaying() {
    return mediaPlayer != null && mediaPlayer.isPlaying();
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Sets the volume to a number between 0 and 100.")
  public void Volume(int vol) {
    volume = Math.max(0, Math.min(100, vol));
    if (mediaPlayer != null && isInitialized) {
      float volumeFloat = volume / 100.0f;
      mediaPlayer.setVolume(volumeFloat, volumeFloat);
      Log.i(LOG_TAG, "Volume set to: " + volume);
    }
  }

  // Playback Methods

  @Override
  @SimpleFunction(description = "Starts playback of the video.")
  public void Play() {
    if (mediaPlayer != null && isInitialized) {
      try {
        if (!mediaPlayer.isPlaying()) {
          mediaPlayer.start();
          Log.i(LOG_TAG, "Video playback started");
        }
      } catch (IllegalStateException e) {
        Log.e(LOG_TAG, "Cannot start playback", e);
      }
    } else {
      shouldAutoPlay = true;
      Log.i(LOG_TAG, "Video will auto-play when ready");
    }
  }

  @Override
  @SimpleFunction(description = "Pauses playback of the video.")
  public void Pause() {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      Log.i(LOG_TAG, "Video playback paused");
    }
  }

  @Override
  @SimpleFunction(description = "Returns duration of the video in milliseconds.")
  public int GetDuration() {
    if (mediaPlayer != null && isInitialized) {
      try {
        int duration = mediaPlayer.getDuration();
        return duration > 0 ? duration : 0;
      } catch (IllegalStateException e) {
        Log.e(LOG_TAG, "Cannot get duration", e);
      }
    }
    return 0;
  }

  @Override
  @SimpleFunction(description = "Seeks to the requested time (specified in milliseconds).")
  public void SeekTo(int ms) {
    if (mediaPlayer != null && isInitialized) {
      try {
        mediaPlayer.seekTo(ms);
        Log.i(LOG_TAG, "Seeking to: " + ms + "ms");
      } catch (IllegalStateException e) {
        Log.e(LOG_TAG, "Cannot seek", e);
      }
    }
  }

  // Events

  @Override
  @SimpleEvent(description = "Indicates that the video has reached the end.")
  public void Completed() {
    EventDispatcher.dispatchEvent(this, "Completed");
  }


  @SimpleProperty(description = "How far, in centimeters, the VideoNode extends along the x-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the VideoNode " +
      "will not be shown.")
  public float WidthInCentimeters() { return widthInCentimeters; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "50")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthInCentimeters(float width) {
    widthInCentimeters = width;
  }

  @Override
  @SimpleProperty(description = "How far, in centimeters, the VideoNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the VideoNode " +
      "will not be shown.")
  public float HeightInCentimeters() { return heightInCentimeters; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "37.5")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightInCentimeters(float height) {
    heightInCentimeters=height;
  }
}
