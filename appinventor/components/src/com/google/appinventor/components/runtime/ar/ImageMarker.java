// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.util.MediaUtil;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ARCore-backed implementation of ARImageMarker for App Inventor.
 *
 * Lifecycle:
 *  1. The parent ARView3D collects all ImageMarker instances and calls
 *     {@link #registerWithDatabase(AugmentedImageDatabase, Form)} while
 *     building the AugmentedImageDatabase, before session.configure() is called.
 *  2. Each AR frame, the parent calls {@link #onFrameUpdate(Frame)} so this
 *     marker can query its own tracking state and fire the appropriate events.
 *  3. When ARView3D.ResetDetectedItems() is called, the parent calls
 *     {@link #reset()} on every marker.
 */
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that allows for image detection in an ARView3D.",
    category = ComponentCategory.AR, iconName = "images/imageMarker.png")
@SimpleObject
public final class ImageMarker implements ARImageMarker {

  private static final String TAG = "ImageMarker";

  // Thresholds so PositionChanged / RotationChanged don't fire at 30-60 Hz
  // from sub-millimeter pose jitter while the printed image sits still.
  private static final float POSITION_EPSILON_M = 0.01f;   // 1 cm
  private static final float ROTATION_EPSILON_DEG = 2.0f;  // 2 degrees

  // -------------------------------------------------------------------------
  // Component wiring
  // -------------------------------------------------------------------------

  /** The parent AR view that owns this marker. Set by ARView3D after construction. */
  protected ARView3D arView = null;

  // -------------------------------------------------------------------------
  // Designer / Simple properties
  // -------------------------------------------------------------------------

  private String name = "";
  private String imageUrl = "";
  private float physicalWidthInCentimeters = 0f;

  /**
   * Nodes that are attached to (following) this marker.
   * CopyOnWriteArrayList because it's written from the UI thread
   * (Follow blocks) and iterated on the GL thread (onFrameUpdate).
   */
  protected List<ARNode> arNodes = new CopyOnWriteArrayList<>();

  /** When true, attached nodes always face the camera (billboard mode). */
  protected boolean billboardNodes = false;

  // -------------------------------------------------------------------------
  // ARCore tracking state
  // -------------------------------------------------------------------------

  /**
   * Index assigned when this marker's image is added to the
   * {@link AugmentedImageDatabase}.  -1 means the image has not been
   * registered (missing URL, zero physical width, or load failure).
   */
  private int databaseIndex = -1;

  /** The most recently seen ARCore augmented image for this marker. */
  private volatile AugmentedImage trackedImage = null;

  /** Tracks whether {@link #FirstDetected()} has already been fired this session. */
  private boolean firstDetectionFired = false;

  /**
   * The tracking method observed during the previous frame, used to detect
   * transitions between FULL_TRACKING ↔ LAST_KNOWN_POSE so we can fire
   * {@link #AppearedInView()} / {@link #NoLongerInView()}.
   */
  private AugmentedImage.TrackingMethod lastTrackingMethod = null;

  /** Last pose values that were actually reported to the app's blocks. */
  private float[] lastReportedPosition = null;
  private float[] lastReportedEulerDeg = null;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  public ImageMarker(ARImageMarkerContainer container) {
    // NOTE: this requires ARView3D.ImageMarkers() to return its live,
    // persistent list — not a fresh ArrayList per call. See wiring notes.
    container.ImageMarkers().add(this);
    if (container instanceof ARView3D) {
      this.arView = (ARView3D) container;
    }
  }

  // -------------------------------------------------------------------------
  // ARCore integration — called by ARView3D
  // -------------------------------------------------------------------------

  /**
   * Registers this marker's image with the given {@link AugmentedImageDatabase}.
   *
   * <p>Must be called <em>before</em> {@code session.configure()} is invoked.
   * Returns {@code true} if the image was successfully added.
   *
   * <p>Loads the bitmap through {@link MediaUtil} first so this works in the
   * AI2 Companion (where assets live on external storage, and
   * {@code AssetManager.open()} would throw), then falls back to the APK's
   * AssetManager for compiled apps if MediaUtil fails.
   *
   * @param db   The database being built for the AR session.
   * @param form The form, used to resolve the image asset.
   * @return {@code true} on success; {@code false} if the image could not be
   *         loaded or if required properties (URL, physical width) are not set.
   */
  public boolean registerWithDatabase(AugmentedImageDatabase db, Form form) {
    if (imageUrl == null || imageUrl.isEmpty()) {
      Log.w(TAG, "ImageMarker '" + name + "': Image URL is not set; skipping registration.");
      return false;
    }
    if (physicalWidthInCentimeters <= 0f) {
      Log.w(TAG, "ImageMarker '" + name + "': PhysicalWidthInCentimeters must be > 0; skipping.");
      return false;
    }

    Bitmap bitmap = loadBitmap(form);
    if (bitmap == null) {
      Log.e(TAG, "ImageMarker '" + name + "': could not load '" + imageUrl + "'.");
      return false;
    }

    try {
      float widthInMeters = physicalWidthInCentimeters / 100f;
      databaseIndex = db.addImage(name, bitmap, widthInMeters);
      Log.i(TAG, "ImageMarker '" + name + "' registered at database index " + databaseIndex);
      return true;
    } catch (IllegalArgumentException e) {
      // ARCore throws this for unsupported bitmap formats AND for images with
      // too few trackable features (ImageInsufficientQualityException extends it).
      Log.e(TAG, "ImageMarker '" + name + "': ARCore rejected the image. "
          + "Check contrast/feature density with the arcoreimg tool.", e);
      return false;
    }
  }

  private Bitmap loadBitmap(Form form) {
    // Path 1: MediaUtil — resolves assets in both Companion and compiled apps.
    try {
      BitmapDrawable drawable = MediaUtil.getBitmapDrawable(form, imageUrl);
      if (drawable != null && drawable.getBitmap() != null) {
        return drawable.getBitmap();
      }
    } catch (IOException e) {
      Log.w(TAG, "MediaUtil could not load '" + imageUrl + "', trying AssetManager.", e);
    }
    // Path 2: raw AssetManager (compiled-app fallback).
    AssetManager assets = form.getAssets();
    try (InputStream is = assets.open(imageUrl)) {
      return BitmapFactory.decodeStream(is);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Processes a single AR frame for this marker.
   *
   * <p>The parent {@link ARView3D} must call this once per rendered frame from
   * the GL/render thread.  Events are posted to the UI thread here — the App
   * Inventor event dispatcher does NOT do that automatically.
   *
   * @param frame The current {@link Frame} from the AR session.
   */
  public void onFrameUpdate(Frame frame) {
    if (databaseIndex < 0) return; // not registered — nothing to track

    for (AugmentedImage img : frame.getUpdatedTrackables(AugmentedImage.class)) {
      if (img.getIndex() != databaseIndex) continue;

      TrackingState state = img.getTrackingState();
      AugmentedImage.TrackingMethod method = img.getTrackingMethod();

      switch (state) {
        case TRACKING:
          handleTracking(img, method);
          break;

        case PAUSED:
          // PAUSED means ARCore knows the image is in the database but hasn't
          // located it yet (or overall tracking is interrupted). Note: an
          // image *leaving the camera view* does NOT go PAUSED — it stays
          // TRACKING with method LAST_KNOWN_POSE, handled in handleTracking.
          if (lastTrackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            NoLongerInView();
          }
          break;

        case STOPPED:
          // ARCore has discarded all information about this image.
          reset();
          break;
      }

      lastTrackingMethod = method;
      trackedImage = img;
    }
  }

  /**
   * Handles a frame where this marker's image is actively tracked.
   */
  private void handleTracking(AugmentedImage img, AugmentedImage.TrackingMethod method) {
    boolean full = method == AugmentedImage.TrackingMethod.FULL_TRACKING;

    // Fire FirstDetected exactly once per session — only on a confident,
    // in-view detection.
    if (full && !firstDetectionFired) {
      firstDetectionFired = true;
      FirstDetected();
    }

    // View transitions. This is where NoLongerInView usually happens:
    // ARCore keeps state=TRACKING and flips the method to LAST_KNOWN_POSE
    // when the image leaves the frame.
    if (lastTrackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING && !full) {
      NoLongerInView();
    } else if (full
        && lastTrackingMethod != null
        && lastTrackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING) {
      AppearedInView();
    }

    // Only emit position/rotation events from a confident full-tracking pose,
    // and only when the pose has actually moved past the jitter threshold.
    if (full) {
      Pose centerPose = img.getCenterPose();

      float[] translation = new float[3];
      centerPose.getTranslation(translation, 0);
      if (lastReportedPosition == null
          || distance(translation, lastReportedPosition) > POSITION_EPSILON_M) {
        lastReportedPosition = translation.clone();
        PositionChanged(translation[0], translation[1], translation[2]);
      }

      float[] eulerDegrees = quaternionToEulerDegrees(centerPose.getRotationQuaternion());
      if (lastReportedEulerDeg == null
          || maxAbsDelta(eulerDegrees, lastReportedEulerDeg) > ROTATION_EPSILON_DEG) {
        lastReportedEulerDeg = eulerDegrees.clone();
        RotationChanged(eulerDegrees[0], eulerDegrees[1], eulerDegrees[2]);
      }

      // Move any nodes following this marker to the image's pose.
      updateAttachedNodes(centerPose);
    }
  }

  /**
   * Drives followers from the image's center pose. Runs on the GL thread —
   * writes go through ARNodeBase's world matrix, same as the drag system,
   * so no per-frame anchor churn.
   */
  private void updateAttachedNodes(Pose centerPose) {
    for (ARNode node : arNodes) {
      if (node instanceof ARNodeBase) {
        ((ARNodeBase) node).updateFromMarkerPose(centerPose);
      }
    }
  }

  /** Called by ARNodeBase.Follow / FollowWithOffset. */
  public void attachNode(ARNode node) {
    if (!arNodes.contains(node)) {
      arNodes.add(node);
    }
  }

  /** Called by ARNodeBase.StopFollowingImageMarker. */
  public void detachNode(ARNode node) {
    arNodes.remove(node);
  }

  /** Center pose of the detected image, or null if never tracked. */
  public Pose trackedPose() {
    AugmentedImage img = trackedImage;
    return img != null ? img.getCenterPose() : null;
  }

  /**
   * Resets all per-detection state so the marker can be re-detected.
   * Called both internally (on STOPPED) and externally (ARView3D.ResetDetectedItems).
   */
  public void reset() {
    firstDetectionFired = false;
    lastTrackingMethod = null;
    trackedImage = null;
    lastReportedPosition = null;
    lastReportedEulerDeg = null;
    Reset();
  }

  // -------------------------------------------------------------------------
  // Name property (set by App Inventor framework)
  // -------------------------------------------------------------------------

  @SimpleProperty(
      description = "The name of this ImageMarker component.",
      category = PropertyCategory.APPEARANCE)
  public String Name() {
    return this.name;
  }

  @Override
  public void setComponentName(String componentName) {
    this.name = componentName;
  }

  /* csb temp hack */
  @SimpleProperty(
      description = "The name of this ImageMarker component.",
      category = PropertyCategory.APPEARANCE)
  public void Name(String componentName) {
    this.name = componentName;
  }

  // -------------------------------------------------------------------------
  // Image property
  // -------------------------------------------------------------------------

  @Override
  @SimpleProperty(
      description = "The image file asset to be detected.",
      category = PropertyCategory.APPEARANCE)
  public String Image() {
    return this.imageUrl;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty(description = "The image asset used for AR image detection.")
  public void Image(String image) {
    this.imageUrl = image;
    // If the session is already configured, the database must be rebuilt for
    // this change to take effect. ARView3D handles that via a dirty flag.
    if (arView != null) {
      arView.imageMarkerDatabaseChanged();
    }
  }

  // -------------------------------------------------------------------------
  // PhysicalWidthInCentimeters property
  // -------------------------------------------------------------------------

  @Override
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "The width of the image in centimeters in the real world. "
          + "Must be greater than zero for detection to work. "
          + "Negative values are treated as their absolute value.")
  public float PhysicalWidthInCentimeters() {
    return this.physicalWidthInCentimeters;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "0")
  @SimpleProperty(description = "Sets the physical width of the image in centimeters.")
  public void PhysicalWidthInCentimeters(float width) {
    this.physicalWidthInCentimeters = Math.abs(width);
    if (arView != null) {
      arView.imageMarkerDatabaseChanged();
    }
  }

  // -------------------------------------------------------------------------
  // PhysicalHeightInCentimeters property
  //
  // ARCore derives height from the image's aspect ratio and the physical width,
  // so there is no setter — this is a read-only derived value.
  // -------------------------------------------------------------------------

  @Override
  @SimpleProperty(description = "The estimated physical height of the image in centimeters, "
      + "derived from the image's aspect ratio and PhysicalWidthInCentimeters.")
  public float PhysicalHeightInCentimeters() {
    AugmentedImage img = trackedImage;
    if (img != null) {
      // extentZ is the height of the detected image in metres.
      return img.getExtentZ() * 100f;
    }
    // Fallback: estimate from the asset bitmap's aspect ratio.
    if (arView != null && imageUrl != null && !imageUrl.isEmpty()
        && physicalWidthInCentimeters > 0f) {
      Bitmap bmp = loadBitmap(arView.$form());
      if (bmp != null && bmp.getWidth() > 0) {
        float aspectRatio = (float) bmp.getHeight() / (float) bmp.getWidth();
        return physicalWidthInCentimeters * aspectRatio;
      }
    }
    return 0f;
  }

  // -------------------------------------------------------------------------
  // Billboard property
  // -------------------------------------------------------------------------

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(description = "When true, nodes attached to this marker will always face the camera.",
      category = PropertyCategory.APPEARANCE)
  public void Billboard(boolean bd) {
    this.billboardNodes = bd;
    // Propagate to any already-attached nodes that respect billboard mode.
    for (ARNode node : arNodes) {
      if (node instanceof BillboardCapable) {
        ((BillboardCapable) node).Billboard(bd);
      }
    }
  }

  @SimpleProperty(description = "Whether attached nodes billboard toward the camera.",
      category = PropertyCategory.APPEARANCE)
  public boolean Billboard() {
    return this.billboardNodes;
  }

  // -------------------------------------------------------------------------
  // AttachedNodes
  // -------------------------------------------------------------------------

  @Override
  @SimpleProperty(description = "The list of nodes currently following this ImageMarker.")
  public List<ARNode> AttachedNodes() {
    return new java.util.ArrayList<>(arNodes);
  }

  // -------------------------------------------------------------------------
  // removeAllNodes
  // -------------------------------------------------------------------------

  @SimpleFunction(description = "Detach and remove all nodes following this marker.")
  public void removeAllNodes() {
    Log.i(TAG, "removeAllNodes() called for marker '" + name + "'");
    for (ARNode node : arNodes) {
      node.StopFollowingImageMarker();   // this calls detachNode(node)
    }
    arNodes.clear();
  }

  // -------------------------------------------------------------------------
  // HandlesEventDispatching
  // -------------------------------------------------------------------------

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return arView != null ? arView.getDispatchDelegate() : null;
  }

  // -------------------------------------------------------------------------
  // Events — onFrameUpdate runs on the GL render thread, so every event is
  // posted to the UI thread before dispatch. EventDispatcher does NOT do
  // this automatically (ARView3D wraps its own dispatches the same way).
  // -------------------------------------------------------------------------

  private void dispatch(final String eventName, final Object... args) {
    if (arView == null) {
      Log.w(TAG, "Cannot dispatch " + eventName + " — arView not set");
      return;
    }
    arView.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(ImageMarker.this, eventName, args);
      }
    });
  }

  @Override
  @SimpleEvent(description = "Fired the first time the image is detected. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void FirstDetected() {
    dispatch("FirstDetected");
  }

  @Override
  @SimpleEvent(description = "Fired when the detected image's position changes to (x, y, z). "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void PositionChanged(float x, float y, float z) {
    dispatch("PositionChanged", x, y, z);
  }

  @Override
  @SimpleEvent(description = "Fired when the detected image's rotation updates. "
      + "Rotation values are Euler angles in degrees. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void RotationChanged(float x, float y, float z) {
    dispatch("RotationChanged", x, y, z);
  }

  @Override
  @SimpleEvent(description = "Fired when the image leaves the camera's view after having been detected. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void NoLongerInView() {
    dispatch("NoLongerInView");
  }

  @Override
  @SimpleEvent(description = "Fired when the image re-enters the camera's view after having been lost. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void AppearedInView() {
    dispatch("AppearedInView");
  }

  @Override
  @SimpleEvent(description = "Fired when detection is reset, either via ARView3D.ResetDetectedItems() "
      + "or because ARCore discarded the tracking information.")
  public void Reset() {
    dispatch("Reset");
  }

  // -------------------------------------------------------------------------
  // Utility — quaternion to Euler angles (degrees, XYZ order)
  // -------------------------------------------------------------------------

  /**
   * Converts an ARCore rotation quaternion [qx, qy, qz, qw] to Euler angles
   * in degrees [pitch (X), yaw (Y), roll (Z)].
   */
  private static float[] quaternionToEulerDegrees(float[] q) {
    // q = [qx, qy, qz, qw]
    float qx = q[0], qy = q[1], qz = q[2], qw = q[3];

    // Pitch (rotation around X)
    double sinrCosp = 2.0 * (qw * qx + qy * qz);
    double cosrCosp = 1.0 - 2.0 * (qx * qx + qy * qy);
    double pitch = Math.atan2(sinrCosp, cosrCosp);

    // Yaw (rotation around Y) — clamped to avoid gimbal singularity
    double sinp = 2.0 * (qw * qy - qz * qx);
    double yaw;
    if (Math.abs(sinp) >= 1.0) {
      yaw = Math.copySign(Math.PI / 2, sinp);
    } else {
      yaw = Math.asin(sinp);
    }

    // Roll (rotation around Z)
    double sinyCosp = 2.0 * (qw * qz + qx * qy);
    double cosyCosp = 1.0 - 2.0 * (qy * qy + qz * qz);
    double roll = Math.atan2(sinyCosp, cosyCosp);

    return new float[]{
        (float) Math.toDegrees(pitch),
        (float) Math.toDegrees(yaw),
        (float) Math.toDegrees(roll)
    };
  }

  private static float distance(float[] a, float[] b) {
    float dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
    return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  private static float maxAbsDelta(float[] a, float[] b) {
    float m = 0f;
    for (int i = 0; i < 3; i++) {
      m = Math.max(m, Math.abs(a[i] - b[i]));
    }
    return m;
  }

  // -------------------------------------------------------------------------
  // Optional marker interface — implemented by nodes that support billboard mode
  // -------------------------------------------------------------------------

  /**
   * Nodes that support billboard mode should implement this interface so that
   * {@link #Billboard(boolean)} can propagate the setting to already-attached nodes.
   */
  public interface BillboardCapable {
    void Billboard(boolean billboard);
  }
}