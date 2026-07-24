// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;

import java.io.File;
import java.io.IOException;

// TODO: update the component version
@UsesAssets(fileNames = "quad.obj")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a video in an ARView3D.  The video is positioned " +
        "at a point, and the source, or video to be played, can be set." +
        "<p>App Inventor for Android only permits video files under 1 MB and " +
        "limits the total size of an application to 5 MB, not all of which is " +
        "available for media (video, audio, and sound) files.  If your media " +
        "files are too large, you may get errors when packaging or installing " +
        "your application, in which case you should reduce the number of media " +
        "files or their sizes.</p>",
    category = ComponentCategory.AR, iconName = "images/videoNode.png")
@SimpleObject
public final class VideoNode extends ARNodeBase implements ARVideo {

  private static final String LOG_TAG = "VideoNode";

  // ---------------------------------------------------------------------------
  // Rendering path: ObjectRenderer (GL layer), NOT Filament.
  //
  // Video frames arrive through a SurfaceTexture backed by a
  // GL_TEXTURE_EXTERNAL_OES texture — the exact mechanism BackgroundRenderer
  // already uses for the camera feed. Reuse that pattern: draw plane.obj with
  // a samplerExternalOES fragment shader, calling updateTexImage() on the GL
  // thread each frame. See class-level integration notes at the bottom.
  // ---------------------------------------------------------------------------

  // --- Model / geometry ---
  private final String objectModel = Form.ASSETS_PREFIX + "quad.obj";
  private float widthInCentimeters  = 50.0f;
  private float heightInCentimeters = 37.5f;
  private boolean matchVideoAspect  = true;   // auto-fit height to video aspect
  private boolean billboard = false;

  // --- Playback state (UI thread) ---
  private MediaPlayer mediaPlayer;
  private String videoSource = "";
  private String pendingSource = null;        // Source() called before init
  private int volume = 100;
  private int opacity = 100;           // 0..100, see Opacity property
  private int chromaKeyColor = COLOR_GREEN;  // default ON — matches iOS behavior
  private int chromaKeySensitivity = 65;
  // Default 65: the default key is idealized pure green (0,255,0), but real
  // compressed green-screen footage is far less saturated, so the default
  // net must be wide to catch it. Users keying on an exact sampled color
  // can lower this for precision.

  /** App Inventor green (0xFF00FF00). */
  private static final int COLOR_GREEN = 0xFF00FF00;
  private boolean shouldAutoPlay = false;
  private boolean looping = false;
  private volatile boolean isInitialized = false;   // MediaPlayer prepared

  // --- GL-side state (GL thread only, except the volatile flags) ---
  private int videoTextureId = 0;             // GL_TEXTURE_EXTERNAL_OES name
  private SurfaceTexture videoSurfaceTexture;
  private Surface videoSurface;
  private volatile boolean frameAvailable = false;
  private volatile boolean glInitialized = false;
  private volatile boolean firstFrameRendered = false;

  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final ARNodeContainer nodeContainer;

  public VideoNode(ARNodeContainer container) {
    super(container);
    this.nodeContainer = container;           // was `container = container;` — self-assignment bug
    Model(objectModel);
    container.addNode(this);
    // No Texture(...) call: the video texture is an external OES texture
    // created on the GL thread in initializeGlResources(), not an asset.
  }

  // ===========================================================================
  // Two-phase initialization
  //
  // Phase 1 (GL thread): create the OES texture + SurfaceTexture + Surface.
  //   Called by the renderer the first time it sees this node in the draw loop.
  // Phase 2 (main thread): create + configure MediaPlayer against that Surface.
  //   Posted from phase 1, because MediaPlayer callbacks need a Looper thread.
  // ===========================================================================

  /**
   * MUST be called on the GL thread (e.g. first time ObjectRenderer draws this
   * node). Safe to call every frame — it no-ops after the first call.
   */
  public void initializeGlResources() {
    if (glInitialized) {
      return;
    }
    glInitialized = true;

    int[] tex = new int[1];
    GLES30.glGenTextures(1, tex, 0);
    videoTextureId = tex[0];
    GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTextureId);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

    videoSurfaceTexture = new SurfaceTexture(videoTextureId);
    videoSurfaceTexture.setOnFrameAvailableListener(st -> frameAvailable = true);
    videoSurface = new Surface(videoSurfaceTexture);

    Log.i(LOG_TAG, "GL resources created (external texture id=" + videoTextureId + ")");

    // Phase 2 on the main thread:
    mainHandler.post(this::initializeMediaPlayer);
  }

  /** Main thread. Creates the MediaPlayer and wires its callbacks. */
  private void initializeMediaPlayer() {
    if (mediaPlayer != null) {
      return;
    }
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setSurface(videoSurface);

    mediaPlayer.setOnPreparedListener(mp -> {
      isInitialized = true;
      applyVolume();
      mp.setLooping(looping);
      if (matchVideoAspect && mp.getVideoWidth() > 0 && mp.getVideoHeight() > 0) {
        heightInCentimeters = widthInCentimeters
            * mp.getVideoHeight() / (float) mp.getVideoWidth();
        updateCollisionShape();
        Log.i(LOG_TAG, "Aspect-fit height: " + heightInCentimeters + "cm");
      }
      Log.i(LOG_TAG, "MediaPlayer prepared (" + mp.getVideoWidth() + "x"
          + mp.getVideoHeight() + ")");
      if (shouldAutoPlay) {
        shouldAutoPlay = false;
        mp.start();
        Log.i(LOG_TAG, "Auto-play started");
      }
    });

    mediaPlayer.setOnCompletionListener(mp -> Completed());

    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
      Log.e(LOG_TAG, "MediaPlayer error what=" + what + " extra=" + extra);
      isInitialized = false;
      return true; // consume — avoid double-dispatch through completion
    });

    Log.i(LOG_TAG, "MediaPlayer created");

    // A Source() set before we were ready? Load it now.
    if (pendingSource != null) {
      String src = pendingSource;
      pendingSource = null;
      loadVideoSource(src);
    }
  }

  // ===========================================================================
  // GL-thread per-frame hook — call from ObjectRenderer before drawing this node
  // ===========================================================================

  /**
   * MUST be called on the GL thread every frame before this node is drawn.
   * Latches the newest decoded video frame into the external texture.
   */
  public void updateVideoFrameIfAvailable() {
    if (frameAvailable && videoSurfaceTexture != null) {
      frameAvailable = false;
      videoSurfaceTexture.updateTexImage();
      firstFrameRendered = true;
    }
  }

  /** External OES texture the renderer binds when drawing the quad. */
  public int getVideoTextureId() { return videoTextureId; }

  /** The asset backing this node's mesh. */
  public String getObjectModel() { return objectModel; }

  /**
   * Predicate for ObjectRenderer's node filter: draw only once GL resources
   * exist, at least one real frame has been latched (prevents a black/garbage
   * quad before decoding starts), and dimensions are visible per the API docs.
   */
  public boolean isReadyToRender() {
    return glInitialized && firstFrameRendered
        && widthInCentimeters > 0f && heightInCentimeters > 0f;
  }

  /** Per-axis scale (meters) for the unit quad: (width, height, 1). */
  public float[] getGeometryScale() {
    return new float[]{ widthInCentimeters / 100f, heightInCentimeters / 100f, 1f };
  }

  // ===========================================================================
  // Source & playback
  // ===========================================================================

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty(
      description = "The path to the video file, which should be added in the Designer.",
      category = PropertyCategory.BEHAVIOR)
  public void Source(String path) {
    videoSource = path == null ? "" : path;
    if (mediaPlayer == null) {
      // Init hasn't happened yet (renderer hasn't drawn us). Defer.
      pendingSource = videoSource;
      Log.i(LOG_TAG, "Source deferred until MediaPlayer ready: " + path);
      return;
    }
    loadVideoSource(videoSource);
  }

  @SimpleProperty(description = "The path to the video file.")
  public String Source() { return videoSource; }

  /** Main thread. */
  private void loadVideoSource(String path) {
    if (path == null || path.isEmpty()) {
      return;
    }
    try {
      isInitialized = false;
      firstFrameRendered = false;
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.stop();
      }
      mediaPlayer.reset();
      mediaPlayer.setSurface(videoSurface);   // reset() detaches the surface

      File videoFile = new File(path);
      if (videoFile.exists()) {
        // Absolute path (e.g. file picker, external storage)
        mediaPlayer.setDataSource(videoFile.getAbsolutePath());
        Log.i(LOG_TAG, "Loading video from file: " + path);
      } else {
        // App Inventor asset: resolve through MediaUtil, which knows the
        // asset layout in both companion and compiled apps. Raw
        // AssetManager.openFd() throws FileNotFoundException here because
        // App Inventor assets are not at the raw asset root (and openFd
        // fails on compressed assets regardless).
        File resolved = MediaUtil.copyMediaToTempFile(
            nodeContainer.$form(), path);
        mediaPlayer.setDataSource(resolved.getAbsolutePath());
        Log.i(LOG_TAG, "Loading video via MediaUtil: " + path
            + " -> " + resolved.getAbsolutePath());
      }
      mediaPlayer.prepareAsync();             // completes in OnPreparedListener
    } catch (IOException e) {
      Log.e(LOG_TAG, "Failed to load video source: " + path, e);
    }
  }

  @Override
  @SimpleProperty(description = "Returns true if the video is currently playing.")
  public boolean IsPlaying() {
    return mediaPlayer != null && isInitialized && mediaPlayer.isPlaying();
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Sets the volume to a number between 0 and 100.")
  public void Volume(int vol) {
    volume = Math.max(0, Math.min(100, vol));
    applyVolume();
  }

  private void applyVolume() {
    if (mediaPlayer != null && isInitialized) {
      float v = volume / 100.0f;
      mediaPlayer.setVolume(v, v);
      Log.i(LOG_TAG, "Volume set to: " + volume);
    }
    // Otherwise: applied in OnPreparedListener via applyVolume()
  }

  @SimpleProperty(description = "The opacity of the video, from 0 (fully " +
      "transparent) to 100 (fully opaque).")
  public int Opacity() { return opacity; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Opacity(int op) {
    opacity = Math.max(0, Math.min(100, op));
    Log.i(LOG_TAG, "Opacity set to: " + opacity);
  }

  @SimpleProperty(description = "The green-screen (chroma key) color. Pixels " +
      "matching this color become transparent, letting green-screen footage " +
      "float in AR. Defaults to green. Set to None to disable and show the " +
      "video opaque.")
  public int ChromaKeyColor() { return chromaKeyColor; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_GREEN)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ChromaKeyColor(int color) {
    chromaKeyColor = color;
    Log.i(LOG_TAG, "ChromaKeyColor set to: " + Integer.toHexString(color));
  }

  @SimpleProperty(description = "How aggressively pixels near the chroma key " +
      "color are made transparent, 0-100. Higher values key out a wider range " +
      "of shades. Default 65, tuned for the default pure-green key against " +
      "typical footage; lower it when keying on an exact sampled color.")
  public int ChromaKeySensitivity() { return chromaKeySensitivity; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "65")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void ChromaKeySensitivity(int s) {
    chromaKeySensitivity = Math.max(0, Math.min(100, s));
  }

  /**
   * True when chroma keying is active. App Inventor's "None" color is
   * 0x00FFFFFF (alpha 0), and 0 is also treated as unset — so "enabled"
   * means a color with a nonzero alpha byte was chosen. Checking != 0
   * alone would misread None as "key on white".
   */
  public boolean isChromaKeyEnabled() {
    return (chromaKeyColor >>> 24) != 0;
  }

  /** Key color as RGB floats 0..1 for the shader. */
  public float[] getChromaKeyRgb() {
    return new float[]{
        ((chromaKeyColor >> 16) & 0xFF) / 255f,
        ((chromaKeyColor >> 8) & 0xFF) / 255f,
        (chromaKeyColor & 0xFF) / 255f
    };
  }

  /** Chroma distance threshold for the shader, derived from sensitivity. */
  public float getChromaKeyThreshold() {
    // Sensitivity 0..100 -> CbCr distance 0..0.4 (0.4 is a very wide net;
    // typical green screens key well around 0.10-0.15, i.e. sensitivity ~30)
    return chromaKeySensitivity / 250f;
  }

  /** Opacity as 0..1 float for the renderer. */
  public float getOpacityFloat() { return opacity / 100f; }

  /** True when this node needs alpha blending (drawn in the transparent pass). */
  public boolean isTranslucent() { return opacity < 100 || isChromaKeyEnabled(); }

  @SimpleProperty(description = "Whether the video restarts from the beginning " +
      "when it reaches the end.")
  public boolean Loop() { return looping; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Loop(boolean loop) {
    looping = loop;
    if (mediaPlayer != null && isInitialized) {
      mediaPlayer.setLooping(loop);
    }
  }

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
    shouldAutoPlay = false;
    if (mediaPlayer != null && isInitialized && mediaPlayer.isPlaying()) {
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
        return Math.max(duration, 0);
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

  // ===========================================================================
  // Events
  // ===========================================================================

  @Override
  @SimpleEvent(description = "Indicates that the video has reached the end.")
  public void Completed() {
    EventDispatcher.dispatchEvent(this, "Completed");
  }

  // ===========================================================================
  // Size, aspect, billboard
  // ===========================================================================

  @SimpleProperty(description = "How far, in centimeters, the VideoNode extends along the x-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the VideoNode " +
      "will not be shown.")
  public float WidthInCentimeters() { return widthInCentimeters; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "50")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthInCentimeters(float width) {
    widthInCentimeters = Math.abs(width);    // now honors the documented contract
    if (matchVideoAspect && mediaPlayer != null && isInitialized
        && mediaPlayer.getVideoWidth() > 0) {
      heightInCentimeters = widthInCentimeters
          * mediaPlayer.getVideoHeight() / (float) mediaPlayer.getVideoWidth();
    }
    updateCollisionShape();
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
    heightInCentimeters = Math.abs(height);
    matchVideoAspect = false;                // explicit height overrides aspect-fit
    updateCollisionShape();
  }

  @SimpleProperty(description = "When true (default), the node's height is " +
      "automatically fit to the video's aspect ratio based on its width. " +
      "Setting HeightInCentimeters disables this.")
  public boolean MatchVideoAspect() { return matchVideoAspect; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void MatchVideoAspect(boolean match) { matchVideoAspect = match; }

  @Override
  public void Billboard(boolean billboard) {
    this.billboard = billboard;              // was an empty override that swallowed the property
  }

  public boolean Billboard() { return billboard; }

  // ===========================================================================
  // Collision — thin box matching the visible quad
  // ===========================================================================

  @Override
  public void updateCollisionShape() {
    float w = (widthInCentimeters  / 100f) * Scale();
    float h = (heightInCentimeters / 100f) * Scale();
    collisionVolume = new BoxVolume(w, h, 0.01f);  // 1 cm slab, never zero-thickness
    Log.i(LOG_TAG, "Collision shape updated — " + w + " x " + h + " m");
  }

  @Override
  public float[] getModelBounds() {
    return new float[]{
        (widthInCentimeters  / 100f) * Scale(),
        (heightInCentimeters / 100f) * Scale(),
        0.01f
    };
  }

  // ===========================================================================
  // Lifecycle / cleanup
  // ===========================================================================

  /** Main thread. Call from the container's onPause. */
  public void onPause() {
    if (mediaPlayer != null && isInitialized && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      shouldAutoPlay = true;                 // resume on return
    }
  }

  /** Main thread. Call from the container's onResume. */
  public void onResume() {
    if (shouldAutoPlay && mediaPlayer != null && isInitialized) {
      shouldAutoPlay = false;
      mediaPlayer.start();
    }
  }

  /**
   * Releases the MediaPlayer and surfaces. GL texture deletion must happen on
   * the GL thread — see releaseGlResources(). Call from onDestroy / node removal.
   */
  public void release() {
    if (mediaPlayer != null) {
      try {
        mediaPlayer.stop();
      } catch (IllegalStateException ignored) { }
      mediaPlayer.release();
      mediaPlayer = null;
    }
    isInitialized = false;
    if (videoSurface != null) {
      videoSurface.release();
      videoSurface = null;
    }
    if (videoSurfaceTexture != null) {
      videoSurfaceTexture.release();
      videoSurfaceTexture = null;
    }
  }

  /** GL thread. Deletes the external texture. */
  public void releaseGlResources() {
    if (videoTextureId != 0) {
      GLES30.glDeleteTextures(1, new int[]{videoTextureId}, 0);
      videoTextureId = 0;
    }
    glInitialized = false;
    firstFrameRendered = false;
  }

  // ---------------------------------------------------------------------------
  // ObjectRenderer integration notes
  //
  // 1. Filter: include this node when videoNode.isReadyToRender().
  // 2. First draw: call initializeGlResources() (idempotent).
  // 3. Every frame, on the GL thread, before drawing:
  //      videoNode.updateVideoFrameIfAvailable();
  // 4. Draw plane.obj with a fragment shader sampling
  //      uniform samplerExternalOES — copy the pattern from
  //      background_show_camera.frag / BackgroundRenderer, which does exactly
  //      this for the camera feed. Bind getVideoTextureId() to
  //      GL_TEXTURE_EXTERNAL_OES.
  // 5. Multiply getGeometryScale() into the model matrix before the node's
  //      Scale()/rotation/translation. If Billboard() is true, replace the
  //      rotation with the inverse camera rotation (translation from anchor).
  // 6. On node removal / surface destroyed: release() on the main thread,
  //      releaseGlResources() on the GL thread.
  // ---------------------------------------------------------------------------
}