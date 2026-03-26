// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ARCore-backed implementation of ARImageMarker for App Inventor.
 *
 * Lifecycle:
 *  1. The parent ARView3D collects all ImageMarker instances and calls
 *     {@link #registerWithDatabase(AugmentedImageDatabase, AssetManager)} while
 *     building the AugmentedImageDatabase, before session.configure() is called.
 *  2. Each AR frame, the parent calls {@link #onFrameUpdate(Frame)} so this
 *     marker can query its own tracking state and fire the appropriate events.
 *  3. When ARView3D.ResetDetectedItems() is called, the parent calls
 *     {@link #reset()} on every marker.
 */
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that allows for image detection in an ARView3D.",
    category = ComponentCategory.AR)
@SimpleObject
public final class ImageMarker implements ARImageMarker {

  private static final String TAG = "ImageMarker";

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

  /** Nodes that are attached to (following) this marker. */
  protected List<ARNode> arNodes = new ArrayList<>();

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
  private AugmentedImage trackedImage = null;

  /** Tracks whether {@link #FirstDetected()} has already been fired this session. */
  private boolean firstDetectionFired = false;

  /**
   * The tracking method observed during the previous frame, used to detect
   * transitions between FULL_TRACKING ↔ LAST_KNOWN_POSE so we can fire
   * {@link #AppearedInView()} / {@link #NoLongerInView()}.
   */
  private AugmentedImage.TrackingMethod lastTrackingMethod = null;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  public ImageMarker(ARImageMarkerContainer container) {
    container.ImageMarkers().add(this);
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
   * @param db     The database being built for the AR session.
   * @param assets The app's {@link AssetManager} used to open the image asset.
   * @return {@code true} on success; {@code false} if the image could not be loaded
   *         or if required properties (URL, physical width) are not set.
   */
  public boolean registerWithDatabase(AugmentedImageDatabase db, AssetManager assets) {
    if (imageUrl == null || imageUrl.isEmpty()) {
      Log.w(TAG, "ImageMarker '" + name + "': Image URL is not set; skipping registration.");
      return false;
    }
    if (physicalWidthInCentimeters <= 0f) {
      Log.w(TAG, "ImageMarker '" + name + "': PhysicalWidthInCentimeters must be > 0; skipping.");
      return false;
    }

    InputStream is = null;
    try {
      is = assets.open(imageUrl);
      Bitmap bitmap = BitmapFactory.decodeStream(is);
      if (bitmap == null) {
        Log.e(TAG, "ImageMarker '" + name + "': BitmapFactory returned null for '" + imageUrl + "'.");
        return false;
      }
      float widthInMeters = physicalWidthInCentimeters / 100f;
      databaseIndex = db.addImage(name, bitmap, widthInMeters);
      Log.i(TAG, "ImageMarker '" + name + "' registered at database index " + databaseIndex);
      return true;
    } catch (IOException e) {
      Log.e(TAG, "ImageMarker '" + name + "': Failed to open asset '" + imageUrl + "'.", e);
      return false;
    } catch (IllegalArgumentException e) {
      // ARCore throws this if the bitmap format is unsupported.
      Log.e(TAG, "ImageMarker '" + name + "': ARCore rejected the image.", e);
      return false;
    } finally {
      if (is != null) {
        try { is.close(); } catch (IOException ignored) {}
      }
    }
  }

  /**
   * Processes a single AR frame for this marker.
   *
   * <p>The parent {@link ARView3D} must call this once per rendered frame from
   * the GL/render thread.  Event dispatch is forwarded to the App Inventor
   * event dispatch delegate, which posts to the UI thread automatically.
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
          // PAUSED + LAST_KNOWN_POSE means ARCore remembers where the image
          // was but can no longer see it in the current frame.
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
    // Fire FirstDetected exactly once per session.
    if (!firstDetectionFired) {
      firstDetectionFired = true;
      FirstDetected();
    }

    // Fire AppearedInView when we transition back to full tracking from lost.
    if (lastTrackingMethod != null
        && lastTrackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING
        && method == AugmentedImage.TrackingMethod.FULL_TRACKING) {
      AppearedInView();
    }

    // Only emit position/rotation events when we have a confident full-tracking pose.
    if (method == AugmentedImage.TrackingMethod.FULL_TRACKING) {
      float[] translation = new float[3];
      img.getCenterPose().getTranslation(translation, 0);
      PositionChanged(translation[0], translation[1], translation[2]);

      float[] eulerDegrees = quaternionToEulerDegrees(img.getCenterPose().getRotationQuaternion());
      RotationChanged(eulerDegrees[0], eulerDegrees[1], eulerDegrees[2]);
    }
  }

  /**
   * Resets all per-detection state so the marker can be re-detected.
   * Called both internally (on STOPPED) and externally (ARView3D.ResetDetectedItems).
   */
  public void reset() {
    firstDetectionFired = false;
    lastTrackingMethod = null;
    trackedImage = null;
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
  @SimpleProperty(
      description = "The name of this ImageMarker component.",
      category = PropertyCategory.APPEARANCE)
  public void setComponentName(String componentName) {
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
    if (trackedImage != null) {
      // extentZ is the height of the detected image in metres.
      return trackedImage.getExtentZ() * 100f;
    }
    // Fallback: estimate from the asset bitmap's aspect ratio.
    if (arView != null && imageUrl != null && !imageUrl.isEmpty()
        && physicalWidthInCentimeters > 0f) {
      try (InputStream is = arView.$context().getAssets().open(imageUrl)) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, opts);
        if (opts.outWidth > 0 && opts.outHeight > 0) {
          float aspectRatio = (float) opts.outHeight / (float) opts.outWidth;
          return physicalWidthInCentimeters * aspectRatio;
        }
      } catch (IOException e) {
        Log.w(TAG, "Could not read image dimensions for height estimate.", e);
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
        ((BillboardCapable) node).setBillboard(bd);
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
    return new ArrayList<>(arNodes);
  }

  // -------------------------------------------------------------------------
  // removeAllNodes
  // -------------------------------------------------------------------------

  @SimpleFunction(description = "Detach and remove all nodes following this marker.")
  public void removeAllNodes() {
    Log.i(TAG, "removeAllNodes() called for marker '" + name + "'");
    for (ARNode node : arNodes) {
      node.StopFollowingImageMarker();
    }
    arNodes.clear();
  }

  // -------------------------------------------------------------------------
  // HandlesEventDispatching
  // -------------------------------------------------------------------------

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return arView.getDispatchDelegate();
  }

  // -------------------------------------------------------------------------
  // Events
  // -------------------------------------------------------------------------

  @Override
  @SimpleEvent(description = "Fired the first time the image is detected. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void FirstDetected() {
    EventDispatcher.dispatchEvent(this, "FirstDetected");
  }

  @Override
  @SimpleEvent(description = "Fired when the detected image's position changes to (x, y, z). "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void PositionChanged(float x, float y, float z) {
    EventDispatcher.dispatchEvent(this, "PositionChanged", x, y, z);
  }

  @Override
  @SimpleEvent(description = "Fired when the detected image's rotation updates. "
      + "Rotation values are Euler angles in degrees. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void RotationChanged(float x, float y, float z) {
    EventDispatcher.dispatchEvent(this, "RotationChanged", x, y, z);
  }

  @Override
  @SimpleEvent(description = "Fired when the image leaves the camera's view after having been detected. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void NoLongerInView() {
    EventDispatcher.dispatchEvent(this, "NoLongerInView");
  }

  @Override
  @SimpleEvent(description = "Fired when the image re-enters the camera's view after having been lost. "
      + "Requires PhysicalWidthInCentimeters > 0 and a valid Image asset.")
  public void AppearedInView() {
    EventDispatcher.dispatchEvent(this, "AppearedInView");
  }

  @Override
  @SimpleEvent(description = "Fired when detection is reset, either via ARView3D.ResetDetectedItems() "
      + "or because ARCore discarded the tracking information.")
  public void Reset() {
    EventDispatcher.dispatchEvent(this, "Reset");
  }

  // -------------------------------------------------------------------------
  // Utility — quaternion to Euler angles (degrees, XYZ order)
  // -------------------------------------------------------------------------

  /**
   * Converts an ARCore rotation quaternion [qx, qy, qz, qw] to Euler angles
   * in degrees [pitch (X), yaw (Y), roll (Z)].
   *
   * <p>ARCore's {@code Pose#getRotationQuaternion(float[], int)} fills the
   * array as [qx, qy, qz, qw].
   */
  private static float[] quaternionToEulerDegrees() {
    // Overloaded helper; the real work is below.
    return new float[]{0f, 0f, 0f};
  }

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

  // -------------------------------------------------------------------------
  // Optional marker interface — implemented by nodes that support billboard mode
  // -------------------------------------------------------------------------

  /**
   * Nodes that support billboard mode should implement this interface so that
   * {@link #Billboard(boolean)} can propagate the setting to already-attached nodes.
   */
  public interface BillboardCapable {
    void setBillboard(boolean billboard);
  }
}