// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import android.os.Looper;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.util.CameraVectors;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.ar.core.*;
import android.util.Log;
import android.graphics.PointF;
import android.view.MotionEvent;
import com.google.ar.core.exceptions.NotTrackingException;
import com.google.ar.core.exceptions.ResourceExhaustedException;

import java.util.List;
import java.util.Collection;
import java.util.UUID;


@SimpleObject
public abstract class ARNodeBase implements ARNode, FollowsMarker {
  // Core container and AR properties
  protected ARView3D arView = null;
  protected Anchor anchor = null;
  protected float[] fromPropertyPosition = {0f, 0f, 0f};
  protected float[] fromPropertyRotation = {0,0,0,1};
  protected float[] pendingPosition = null;
  protected float[] pendingRotation = {0,0,0,1};
  protected float scale = 1.0f;
  protected Session session = null;
  protected String texture = "";
  protected Trackable trackable = null;
  protected String name = "";
  protected String objectModel = Form.ASSETS_PREFIX + "";

  // Enhanced Physics Properties
  protected String collisionShape = "sphere";
  protected float staticFriction = 0.1f;
  protected float dynamicFriction = 0.1f;
  protected float restitution = 0.1f;
  protected float mass = 0.1f;
  protected float force = 0.1f;
  protected float dragSensitivity = 2.0f;  // Better default for responsiveness
  protected boolean enablePhysics = false;

  // Enhanced Gesture Properties
  protected boolean pinchToScale = false;
  protected boolean panToMove = false;
  protected boolean rotateWithGesture = false;
  protected boolean visible = true;
  // Enhanced Dragging
  protected volatile boolean isBeingDragged = false;
  protected PointF dragStartLocation = new PointF(0, 0);
  protected PointF lastDragLocation = new PointF(0, 0);
  protected Object originalMaterial = null;
  protected float[] lastFingerPosition ;

  protected float[] currentVelocity = {0, 0, 0};

  private boolean onGround = false;
  protected float GROUND_LEVEL = -1.2f; // Default ground level
  private static final float GRAVITY = -9.81f; // m/s²

  protected boolean isCurrentlyColliding = false;
  protected float linearDamping = 0.0f;
  protected float angularDamping = 0.0f;

  // Enhanced Marker and Positioning Properties
  protected ARImageMarker followingMarker = null;
  protected String worldOffset = "";
  protected Object geoAnchor = null;
  protected float[] previewPlacementSurface = null;
  protected boolean hasPreviewSurface = false;

  protected long dragStartTime;

  // The world matrix is the single source of truth during simulation
  public float[] currentWorldMatrix = null;

  protected NearestPlaneFinder planeFinder = null;

  public void setGroundLevel(float y) {
    GROUND_LEVEL = y;
  }

  public void setPlaneFinder(NearestPlaneFinder finder) {
    this.planeFinder = finder;
  }

 /* simple anchor init */
  public void initWorldMatrixFromAnchor() {
    if (anchor == null) return;
    if (currentWorldMatrix == null) currentWorldMatrix = new float[16];
    anchor.getPose().toMatrix(currentWorldMatrix, 0);
  }

  @SuppressWarnings("WeakerAccess")
  protected ARNodeBase(ARNodeContainer container) {
    // Enhanced initialization
    setupInitialProperties();
  }

  private void setupInitialProperties() {
    // Initialize enhanced properties
    currentVelocity = new float[]{0, 0, 0};
    dragStartLocation = new PointF(0, 0);
    lastDragLocation = new PointF(0, 0);
    isBeingDragged = false;
    isCurrentlyColliding = false;
  }

  public void setPendingPosition(float[] pendingPosition) {
    this.pendingPosition = pendingPosition;
  }

  public float[] getPendingPosition() {
    return this.pendingPosition;
  }

  // MARK: - Enhanced Basic Properties

  /* if the node has EnablePhysics, then make node must be on the ground and is subject to gravity
  except when being dragged
   */
  public void updateSimplePhysics(float deltaTime) {
    if (!EnablePhysics() || isBeingDragged) return;

    if (currentWorldMatrix == null) return;

    currentVelocity[1] += GRAVITY * deltaTime;
    currentVelocity[1] = Math.max(currentVelocity[1], -50f);

    float[] currentPos = getCurrentPosition(); // reads matrix now, not anchor

    if (currentPos[0] == 0 && currentPos[1] == 0 && currentPos[2] == 0) {
      if (anchor != null) initWorldMatrixFromAnchor();
      return;
    }

    currentPos[0] += currentVelocity[0] * deltaTime;
    currentPos[1] += currentVelocity[1] * deltaTime;
    currentPos[2] += currentVelocity[2] * deltaTime;

    float objectBottom = currentPos[1];
    if (this instanceof SphereNode) {
      objectBottom -= ((SphereNode) this).RadiusInCentimeters()
          * UnitHelper.centimetersToMeters(1f) * Scale();
    }

    if (objectBottom <= GROUND_LEVEL) {
      if (this instanceof SphereNode) {
        float r = ((SphereNode) this).RadiusInCentimeters()
            * UnitHelper.centimetersToMeters(1f) * Scale();
        currentPos[1] = GROUND_LEVEL + r;
      } else {
        currentPos[1] = GROUND_LEVEL;
      }

      if (currentVelocity[1] < 0) {
        currentVelocity[1] = -currentVelocity[1] * Restitution();
        if (Math.abs(currentVelocity[1]) < 0.05f) {
          currentVelocity[1] = 0;
        }
      }

      currentVelocity[0] *= (1.0f - StaticFriction() * deltaTime * 10f);
      currentVelocity[2] *= (1.0f - StaticFriction() * deltaTime * 10f);
    }

    setCurrentPosition(currentPos); // writes to matrix, no ARCore

    // Detect rest and sync anchor for drift correction
    float speed = vectorLength(currentVelocity);
    if (speed < 0.01f && objectBottom <= GROUND_LEVEL + 0.01f) {
      currentVelocity[0] = 0;
      currentVelocity[1] = 0;
      currentVelocity[2] = 0;

      // Only re-anchor if position has drifted from anchor
      // avoids unnecessary anchor creation when already at rest
      if (anchor != null && session != null) {
        float[] anchorPos = anchor.getPose().getTranslation();
        float[] matrixPos = getCurrentPosition();
        float drift = vectorDistance(anchorPos, matrixPos);

        if (drift > 0.01f) { // more than 1cm drift
          reanchorAtCurrentPosition(planeFinder);
        }
      }
    }

  }

  /* note, in meters */
  public float[] getCurrentPosition() {
    if (currentWorldMatrix != null) {
      return new float[]{
          currentWorldMatrix[12],
          currentWorldMatrix[13],
          currentWorldMatrix[14]
      };
    }
    if (anchor != null && anchor.getTrackingState() == TrackingState.TRACKING) {
      return anchor.getPose().getTranslation();
    }
    return new float[]{0, 0, 0};
  }

  public float[] getCurrentRotation() {
    // Get current position from anchor or trackable
    if (anchor != null) {
      return anchor.getPose().getRotationQuaternion();
    }
    return new float[]{0, 0, 0};
  }

  /* note, in meters */
  public void setCurrentPosition(float[] position) {
    for (float v : position) {
      if (Float.isNaN(v) || Float.isInfinite(v)) return;
    }
    if (Math.abs(position[0]) > 100 || Math.abs(position[1]) > 100
        || Math.abs(position[2]) > 100) return;

    if (currentWorldMatrix == null) {
      currentWorldMatrix = new float[16];
      android.opengl.Matrix.setIdentityM(currentWorldMatrix, 0);
    }
    currentWorldMatrix[12] = position[0];
    currentWorldMatrix[13] = position[1];
    currentWorldMatrix[14] = position[2];
    Log.i("ARNODEBASE", "current position is " + currentWorldMatrix[12] + ", " + currentWorldMatrix[13] + ", " + currentWorldMatrix[14]);
  }

  // Called at drag end — single anchor creation, then done
  public void reanchorAtCurrentPosition(NearestPlaneFinder planeFinder) {
    if (currentWorldMatrix == null) return;
    if (session == null) return;

    float[] pos = getCurrentPosition();
    Log.i("ARNODEBASE", "reanchor position is " + pos[0] + ", " + pos[1] + ", " + pos[2]);
    try {
      Anchor newAnchor;
      Plane nearest = planeFinder != null
          ? planeFinder.find(pos[0], pos[2])
          : null;

      if (nearest != null) {
        newAnchor = nearest.createAnchor(
            new Pose(pos, new float[]{0, 0, 0, 1}));
      } else {
        newAnchor = session.createAnchor(
            new Pose(pos, new float[]{0, 0, 0, 1}));
      }

      // New anchor valid — now safe to detach old one
      if (anchor != null) anchor.detach();
      anchor = newAnchor;

    } catch (NotTrackingException e) {
      // Creation failed — old anchor untouched, node stays visible
      Log.w("ARNodeBase", "Re-anchor deferred — tracking lost during drag end");
    }
  }

  public void tryCreateAnchorIfNeeded(NearestPlaneFinder planeFinder) {
    if (Anchor() != null || pendingPosition == null) return;
    Log.w("Node", this.name + "has pending position " + pendingPosition[0] + " " + pendingPosition[1] + " " + pendingPosition[2] + " " );
    Plane bestDetectedPlane = planeFinder.find(pendingPosition[0], pendingPosition[2]);
    if (bestDetectedPlane == null) return;

    try {
        Pose newPose = new Pose(pendingPosition, pendingRotation);
        Anchor(bestDetectedPlane.createAnchor(newPose));

    } catch (com.google.ar.core.exceptions.NotTrackingException e) {
      // Normal during VIO reset — silent, no log
    } catch (com.google.ar.core.exceptions.ResourceExhaustedException e) {
      Log.w("Node", "Anchor limit reached");
    } catch (Exception e) {
      Log.e("Node", "Anchor creation failed: "
          + e.getClass().getSimpleName() + " " + e.getMessage());
    }
  }

  @Override
  @SimpleFunction(description = "Rotates the node to look at the DetectedPlane.")
  public void LookAtDetectedPlane(ARDetectedPlane detectedPlane) {
    // Implementation depends on ARDetectedPlane interface
    Log.d("ARNodeBase", "LookAtDetectedPlane - implement based on ARDetectedPlane interface");
  }

  @Override
  @SimpleFunction(description = "Rotates the node to look at the Spotlight.")
  public void LookAtSpotlight(ARSpotlight light) {
    // Implementation depends on ARSpotlight interface
    Log.d("ARNodeBase", "LookAtSpotlight - implement based on ARSpotlight interface");
  }

  @Override
  @SimpleFunction(description = "Rotates the node to look at the PointLight.")
  public void LookAtPointLight(ARPointLight light) {
    // Implementation depends on ARPointLight interface
    Log.d("ARNodeBase", "LookAtPointLight - implement based on ARPointLight interface");
  }

  @Override
  @SimpleFunction(description = "Rotates the node to look at the (x,y,z) position in centimeters.")
  public void LookAtPosition(float x, float y, float z) {
    if (anchor == null) {
      Log.w("ARNodeBase", "Cannot look at position - no anchor");
      return;
    }

    float[] myPos = anchor.getPose().getTranslation();
    float[] targetPos = {
        UnitHelper.centimetersToMeters(x),
        UnitHelper.centimetersToMeters(y),
        UnitHelper.centimetersToMeters(z)
    };

    // Calculate direction vector
    float[] direction = {
        targetPos[0] - myPos[0],
        targetPos[1] - myPos[1],
        targetPos[2] - myPos[2]
    };

    // Convert to rotation (simplified - full implementation would need proper quaternion math)
    float yaw = (float) Math.atan2(direction[0], direction[2]);
    float[] euler = {0, yaw, 0};
    float[] quaternion = eulerAnglesToQuaternion(euler);

    Pose newPose = new Pose(myPos, quaternion);

    if (trackable != null) {
      Anchor(trackable.createAnchor(newPose));
    } else if (session != null) {
      Anchor(session.createAnchor(newPose));
    }

    Log.i("ARNodeBase", "Looked at position (" + x + ", " + y + ", " + z + ")");
  }

  // MARK: - Enhanced Event Methods

  @Override
  @SimpleEvent(description = "The user clicked on the node.")
  public void Click() {
    Log.i("ARNodeBase", name + " was clicked");
    // Event dispatching handled by container
  }

  @Override
  @SimpleEvent(description = "The user long-pressed on the node.")
  public void LongClick() {
    Log.i("ARNodeBase", name + " was long-clicked");
    // Event dispatching handled by container
  }

  @Override
  @SimpleEvent(description = "The node stopped following an ImageMarker. This event will trigger after the StopFollowingImageMarker block is called.")
  public void StoppedFollowingMarker() {
    Log.i("ARNodeBase", name + " stopped following marker");
    // Event dispatching handled by container
  }

  @Override
  public float[] getVisualBounds() {
    // Get the model's bounding box - this depends on your 3D model system
    float[] modelBounds = getModelBounds(); // You'll need to implement this

    // Apply scale to bounds
    float scale = Scale();
    return new float[]{
        modelBounds[0] * scale, // width
        modelBounds[1] * scale, // height
        modelBounds[2] * scale  // depth
    };
  }

  @Override
  public float[] getModelBounds() {
      float radiusInMeters = 0.025f; // 2.5cm radius
      float heightInMeters = 0.1f;   // 10cm height
      return new float[]{radiusInMeters * 2, heightInMeters, radiusInMeters * 2};
  }



  @Override
  @SimpleEvent(description = "Collision event detected")
  public void CollisionDetected() {
    Log.i("ARNodeBase", name + " collision detected");
    // Event dispatching handled by container
  }



  @Override
  @SimpleEvent(description = "Collision event detected between object and scene")
  public void ObjectCollidedWithScene() {
    Log.i("ARNodeBase", name + " collided with scene");
    handleSceneCollision();
    // Event dispatching handled by container
  }

  @Override
  @SimpleEvent(description = "Collision event detected between object and another object")
  public void ObjectCollidedWithObject(ARNode otherNode) {
    Log.i("ARNodeBase", name + " collided with " + otherNode.NodeType());
    handleObjectCollision(otherNode);
    // Event dispatching handled by container
  }

  protected void handleSceneCollision() {
    showCollisionEffect("scene");

    // Restore appearance after delay
    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
      if (!isBeingDragged) {
        restoreOriginalMaterial();
      }
    }, 500);
  }

  protected void handleObjectCollision(ARNode otherNode) {
    showCollisionEffect("object");

    // Restore appearance after delay
    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
      if (!isBeingDragged) {
        //restoreOriginalAppearance();
      }
    }, 800);
  }

  protected void showCollisionEffect(String collisionType) {
    isCurrentlyColliding = true;
    // Override in subclasses to implement collision visual effects
    Log.i("ARNodeBase", "Showing " + collisionType + " collision effect - override in subclass");
  }

  // MARK: - Gesture Response Methods

  @SimpleFunction(description = "Handle pinch scaling if enabled")
  public void HandlePinchScale(float scaleFactor) {
    if (pinchToScale) {
      ScaleByPinch(scaleFactor);
      Log.d("ARNodeBase", "Pinch scaled by " + scaleFactor);
    }
  }

  @SimpleFunction(description = "Handle pan movement if enabled")
  public void HandlePanMove(float deltaX, float deltaY) {
    if (panToMove) {
      // Convert screen space to world space movement
      float worldX = UnitHelper.centimetersToMeters(deltaX);
      float worldZ = UnitHelper.centimetersToMeters(deltaY); // Y screen becomes Z world
      MoveBy(worldX, 0, worldZ);
      Log.d("ARNodeBase", "Pan moved by (" + deltaX + ", " + deltaY + ")");
    }
  }

  @SimpleFunction(description = "Handle rotation gesture if enabled")
  public void HandleRotationGesture(float rotationAngle) {
    if (rotateWithGesture) {
      RotateYBy((float) Math.toDegrees(rotationAngle));
      Log.d("ARNodeBase", "Rotation gesture: " + Math.toDegrees(rotationAngle) + " degrees");
    }
  }

  // MARK: - Utility Methods

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return arView != null ? arView.getDispatchDelegate() : null;
  }

  // Enhanced utility methods for coordinate conversions and math

  protected float[] quaternionToEulerAngles(float[] quaternion) {
    float w = quaternion[3];
    float x = quaternion[0];
    float y = quaternion[1];
    float z = quaternion[2];

    // Roll (x-axis rotation)
    float sinr_cosp = 2 * (w * x + y * z);
    float cosr_cosp = 1 - 2 * (x * x + y * y);
    float roll = (float) Math.atan2(sinr_cosp, cosr_cosp);

    // Pitch (y-axis rotation)
    float sinp = 2 * (w * y - z * x);
    float pitch;
    if (Math.abs(sinp) >= 1) {
      pitch = (float) Math.copySign(Math.PI / 2, sinp);
    } else {
      pitch = (float) Math.asin(sinp);
    }

    // Yaw (z-axis rotation)
    float siny_cosp = 2 * (w * z + x * y);
    float cosy_cosp = 1 - 2 * (y * y + z * z);
    float yaw = (float) Math.atan2(siny_cosp, cosy_cosp);

    return new float[]{roll, pitch, yaw};
  }

  protected float[] eulerAnglesToQuaternion(float[] euler) {
    float roll = euler[0];
    float pitch = euler[1];
    float yaw = euler[2];

    float cx = (float) Math.cos(roll * 0.5);
    float sx = (float) Math.sin(roll * 0.5);
    float cy = (float) Math.cos(pitch * 0.5);
    float sy = (float) Math.sin(pitch * 0.5);
    float cz = (float) Math.cos(yaw * 0.5);
    float sz = (float) Math.sin(yaw * 0.5);

    float w = cx * cy * cz + sx * sy * sz;
    float x = sx * cy * cz - cx * sy * sz;
    float y = cx * sy * cz + sx * cy * sz;
    float z = cx * cy * sz - sx * sy * cz;

    return new float[]{x, y, z, w};
  }

  protected PointF parsePointF(String pointStr) {
    try {
      String[] parts = pointStr.split(",");
      return new PointF(
          Float.parseFloat(parts[0].trim()),
          Float.parseFloat(parts[1].trim())
      );
    } catch (Exception e) {
      Log.e("ARNodeBase", "Error parsing PointF: " + pointStr, e);
      return new PointF(0, 0);
    }
  }

  protected float[] parseVector3(String vectorStr) {
    try {
      String[] parts = vectorStr.split(",");
      return new float[]{
          Float.parseFloat(parts[0].trim()),
          Float.parseFloat(parts[1].trim()),
          Float.parseFloat(parts[2].trim())
      };
    } catch (Exception e) {
      Log.e("SphereNode", "Error parsing vector3: " + vectorStr, e);
      return new float[]{0, 0, 0};
    }
  }

  protected float vectorDistance(float[] a, float[] b) {
    float dx = a[0] - b[0];
    float dy = a[1] - b[1];
    float dz = a[2] - b[2];
    return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  protected float[] vectorSubtract(float[] a, float[] b) {
    return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
  }

  protected float[] vectorAdd(float[] a, float[] b) {
    return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
  }

  protected float[] vectorMultiply(float[] vector, float scalar) {
    return new float[]{vector[0] * scalar, vector[1] * scalar, vector[2] * scalar};
  }

  protected float vectorLength(float[] vector) {
    return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
  }

  protected float[] vectorNormalize(float[] vector) {
    float length = vectorLength(vector);
    if (length > 0) {
      return vectorMultiply(vector, 1.0f / length);
    }
    return new float[]{0, 0, 0};
  }

  protected String arrayToString(float[] array) {
    if (array.length >= 3) {
      return "(" + String.format("%.3f", array[0]) + ", " +
          String.format("%.3f", array[1]) + ", " +
          String.format("%.3f", array[2]) + ")";
    }
    return java.util.Arrays.toString(array);
  }

  // MARK: - Enhanced Preview and Surface Methods

  @SimpleFunction(description = "Set preview placement surface for positioning")
  public void SetPreviewPlacementSurface(String surfacePosition) {
    previewPlacementSurface = parseVector3(surfacePosition);
    hasPreviewSurface = true;
    Log.i("ARNodeBase", "Set preview surface: " + arrayToString(previewPlacementSurface));
  }

  @SimpleFunction(description = "Clear preview placement surface")
  public void ClearPreviewPlacementSurface() {
    previewPlacementSurface = null;
    hasPreviewSurface = false;
    Log.i("ARNodeBase", "Cleared preview surface");
  }

  @SimpleProperty(description = "Whether the node has a preview placement surface")
  public boolean HasPreviewSurface() {
    return hasPreviewSurface;
  }

  public float[] getPreviewPlacementSurface() {
    return previewPlacementSurface;
  }

  // MARK: - Enhanced Lifecycle Methods

  @SimpleFunction(description = "Called when the AR session resumes")
  public void OnResume() {
    Log.d("ARNodeBase", name + " resuming");
    // Override in subclasses for resume behavior
  }

  @SimpleFunction(description = "Called when the AR session pauses")
  public void OnPause() {
    Log.d("ARNodeBase", name + " pausing");
    // Override in subclasses for pause behavior
  }

  @SimpleFunction(description = "Called when the node is being deleted")
  public void OnDelete() {
    Log.d("ARNodeBase", name + " being deleted");

    // Cleanup
    if (followingMarker != null) {
      StopFollowingImageMarker();
    }

    if (anchor != null) {
      anchor.detach();
      anchor = null;
    }

    // Override in subclasses for additional cleanup
  }

  @SimpleFunction(description = "Called when the node is being destroyed")
  public void OnDestroy() {
    Log.d("ARNodeBase", name + " being destroyed");
    OnDelete();
    // Override in subclasses for destruction behavior
  }

  // MARK: - Enhanced Debug Methods

  @SimpleFunction(description = "Print debug information about the node")
  public void DebugInfo() {
    Log.i("ARNodeBase", "=== DEBUG INFO FOR " + name + " ===");
    Log.i("ARNodeBase", "Type: " + NodeType());
    Log.i("ARNodeBase", "Position: " + XPosition() + ", " + YPosition() + ", " + ZPosition() + " cm");
    Log.i("ARNodeBase", "Rotation: " + XRotation() + ", " + YRotation() + ", " + ZRotation() + " deg");
    Log.i("ARNodeBase", "Scale: " + Scale());
    Log.i("ARNodeBase", "Has Anchor: " + (anchor != null));
    Log.i("ARNodeBase", "Has Physics: " + enablePhysics);
    Log.i("ARNodeBase", "Is Dragging: " + isBeingDragged);
    Log.i("ARNodeBase", "Following Marker: " + IsFollowingImageMarker());

    if (enablePhysics) {
      Log.i("ARNodeBase", "Mass: " + mass);
      Log.i("ARNodeBase", "Static Friction: " + staticFriction);
      Log.i("ARNodeBase", "Dynamic Friction: " + dynamicFriction);
      Log.i("ARNodeBase", "Restitution: " + restitution);
      Log.i("ARNodeBase", "Drag Sensitivity: " + dragSensitivity);
    }

    Log.i("ARNodeBase", "==============================");
  }

  // MARK: - Placeholder Unity Conversion Helper

  public static class UnitHelper {
    public static float metersToCentimeters(float meters) {
      return meters * 100.0f;
    }

    public static float centimetersToMeters(float centimeters) {
      return centimeters / 100.0f;
    }

    public static float[] metersToCentimeters(float[] metersArray) {
      float[] result = new float[metersArray.length];
      for (int i = 0; i < metersArray.length; i++) {
        result[i] = metersToCentimeters(metersArray[i]);
      }
      return result;
    }

    public static float[] centimetersToMeters(float[] centimetersArray) {
      float[] result = new float[centimetersArray.length];
      for (int i = 0; i < centimetersArray.length; i++) {
        result[i] = centimetersToMeters(centimetersArray[i]);
      }
      return result;
    }
  }

  public void setComponentName(String name) {
    this.name = name;
  }

  @Override
  public int Height() {
    return 0;
  }

  @Override
  public void Height(int height) {
  }

  @Override
  public int Width() {
    return 0;
  }

  @Override
  public void Width(int width) {
  }

  public Anchor Anchor() {
    return this.anchor;
  }

  public void Anchor(Anchor a) {
    this.anchor = a;
  }

// MARK: - Enhanced Pose Management

  @Override
  public Object Pose() {
    if (anchor != null) {
      return anchor.getPose();
    }
    return null;
  }

  @SimpleProperty(description = "Set the current pose of the object",
      category = PropertyCategory.APPEARANCE)
  @Override
  public void Pose(Object p) {
    Log.i("ARNodeBase", "Setting node pose with " + p);
    Pose pose = (Pose) p;

    float[] position = {pose.tx(), pose.ty(), pose.tz()};
    float[] rotation = {pose.qx(), pose.qy(), pose.qz(), pose.qw()};

    if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    } else if (session != null) {
      Anchor myAnchor = session.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    }
  }

  /* return inital position in centimeters (pose works with meters, so need to convert back */
  public float[] InitialPosition() {
    float[] positionFloatArray = fromPropertyPosition;
    float[] position = {0f, 0f, 0f};
    for (int i = 0; i < positionFloatArray.length; i++) {
      try {
        position[i] = UnitHelper.metersToCentimeters(positionFloatArray[i]);
        Log.i("ARNodeBase", "Initial position: " + positionFloatArray[i]);
      } catch (NumberFormatException e) {
        Log.w("ARNodeBase", "Invalid number in position: " + positionFloatArray[i]);
      }
    }
    return position;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Set the initial position pose of the object from property. Format is a comma-separated list of 3 coordinates: x, y, z such that 0, 0, 10 places the object at x of 0, y of 0 and z of 10 centimeters",
      category = PropertyCategory.APPEARANCE)
  public void InitialPosition(String positionFromProperty) {
    String[] positionArray = positionFromProperty.split(",");
    float[] position = {0f, 0f, 0f};

    for (int i = 0; i < positionArray.length; i++) {
      try {
        position[i] = UnitHelper.centimetersToMeters(Float.parseFloat(positionArray[i]));
        Log.i("ARNodeBase", "Initial position: " + position[i]);
      } catch (NumberFormatException e) {
        Log.w("ARNodeBase", "Invalid number in position: " + positionArray[i]);
      }
    }
    fromPropertyPosition = position;

   /* if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, fromPropertyRotation)); //CSB check
      Anchor(myAnchor);
    } else if (session != null) {
      Anchor myAnchor = session.createAnchor(new Pose(position, fromPropertyRotation));
      Anchor(myAnchor);
    }*/

    Log.i("ARNodeBase", "Stored pose with position " + positionFromProperty);
  }

// MARK: - Enhanced Material and Texture System

  @Override
  @SimpleProperty(description = "Gets the 3D texture", category = PropertyCategory.APPEARANCE)
  public String Texture() {
    return this.texture;
  }

  @Override
  @SimpleProperty(description = "The 3D texture loaded.",
      category = PropertyCategory.APPEARANCE)
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  public void Texture(String texture) {
    Log.d("ARNodeBase", "Set texture on node: " + texture);
    this.texture = texture;
    updateMaterial();
  }

  protected void updateMaterial() {
    // Override in subclasses to implement material updates
    Log.d("ARNodeBase", "Material update requested - override in subclass");
  }

  public Trackable Trackable() {
    return this.trackable;
  }

  public void Trackable(Trackable t) {
    this.trackable = t;
  }

  @Override
  public Session Session() {
    return this.session;
  }

  @Override
  public void Session(Session s) {
    this.session = s;
  }

  @Override
  @SimpleProperty(description = "The 3D model file to be loaded.",
      category = PropertyCategory.APPEARANCE)
  public String Model() {
    return this.objectModel;
  }

  @Override
  @SimpleProperty(description = "The 3D model file to be loaded.",
      category = PropertyCategory.APPEARANCE)
  public void Model(String model) {
    this.objectModel = model;
    updateModel();
  }

  @SimpleProperty(description = "The node's unique Id.",
      category = PropertyCategory.APPEARANCE)
  public String ID() {
    return anchor.getCloudAnchorId();
  }

  protected void updateModel() {
    // Override in subclasses to implement model updates
    Log.d("ARNodeBase", "Model update requested - override in subclass");
  }

  @Override
  @SimpleProperty(description = "Returns the type of node as a String.")
  public String NodeType() {
    return getClass().getSimpleName();
  }

// MARK: - Enhanced Serialization

  @SimpleProperty(description = "Convert current pose to yail",
      category = PropertyCategory.APPEARANCE)
  public YailDictionary PoseToYailDictionary() {
    Log.i("ARNodeBase", "Converting pose to YailDict");

    if (this.Anchor() == null) {
      Log.w("ARNodeBase", "No anchor available for pose conversion");
      return null;
    }

    Pose p = this.Anchor().getPose();
    Log.i("ARNodeBase", "Pose is " + p);

    if (p != null) {
      YailDictionary translationDict = new YailDictionary();
      YailDictionary rotationDict = new YailDictionary();
      YailDictionary yailDictSave = new YailDictionary();

      // Translation
      translationDict.put("x", p.tx());
      translationDict.put("y", p.ty());
      translationDict.put("z", p.tz());
      yailDictSave.put("t", translationDict);

      // Rotation (quaternion)
      rotationDict.put("x", p.qx());
      rotationDict.put("y", p.qy());
      rotationDict.put("z", p.qz());
      rotationDict.put("w", p.qw());
      yailDictSave.put("q", rotationDict);

      // Add geo coordinates if available
      if (geoAnchor != null) {
        // Add geo anchor data - implementation depends on geo anchor type
        yailDictSave.put("lat", 0.0);
        yailDictSave.put("lng", 0.0);
        yailDictSave.put("alt", 0.0);
      }

      Log.i("ARNodeBase", "Exporting pose as YailDict with " + yailDictSave);
      return yailDictSave;
    }
    return null;
  }

  @SimpleProperty(description = "Serialize the ARNode to yail",
      category = PropertyCategory.APPEARANCE)
  public YailDictionary ARNodeToYail() {
    Log.i("ARNodeBase", "Exporting ARNode as yail");
    YailDictionary yailDict = new YailDictionary();

    yailDict.put("model", this.Model());
    yailDict.put("texture", this.Texture());
    yailDict.put("scale", this.Scale());
    yailDict.put("pose", this.PoseToYailDictionary());
    yailDict.put("type", this.NodeType());
    yailDict.put("name", this.name);

    try {
      Log.i("ARNodeBase", "Exporting ARNode as Yail: " + yailDict);
      return yailDict;
    } catch (Exception e) {
      Log.e("ARNodeBase", "Failed to export as yail", e);
    }
    return null;
  }

// MARK: - Enhanced Visibility and Appearance

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY, defaultValue = "True")
  @SimpleProperty
  public boolean Visible() {
    return visible;
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.")
  public void Visible(boolean visibility) {
    // Override in subclasses to implement visibility
    Log.d("ARNodeBase", "Visibility change requested: " + visibility);
  }

  @Override
  @SimpleProperty(description = "Specifies whether the node should show a shadow when it is lit by Lights.")
  public boolean ShowShadow() {
    return false;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShowShadow(boolean showShadow) {
    // Override in subclasses to implement shadow control
    Log.d("ARNodeBase", "Shadow change requested: " + showShadow);
  }

  @Override
  @SimpleProperty(description = "Sets the opacity of the node. Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.")
  public int Opacity() {
    return 90;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "90")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Opacity(int opacity) {
    int clampedOpacity = Math.max(0, Math.min(100, opacity));
    // Override in subclasses to implement opacity
    Log.d("ARNodeBase", "Opacity change requested: " + clampedOpacity);
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The color of the node. If the Texture is set, the color is not shown.")
  public int FillColor() {
    return 0;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_RED)
  @SimpleProperty()
  public void FillColor(int color) {
    // Override in subclasses to implement color changes
    Log.d("ARNodeBase", "Fill color change requested: " + color);
    updateMaterial();
  }

  @Override
  @SimpleProperty(description = "The opacity of the node's FillColor. Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.")
  public int FillColorOpacity() {
    return 100;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FillColorOpacity(int colorOpacity) {
    int clampedOpacity = Math.max(0, Math.min(100, colorOpacity));
    // Override in subclasses to implement color opacity
    Log.d("ARNodeBase", "Fill color opacity change requested: " + clampedOpacity);
    updateMaterial();
  }

  @Override
  @SimpleProperty(description = "The opacity of the node's Texture. Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.")
  public int TextureOpacity() {
    return 100;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void TextureOpacity(int textureOpacity) {
    int clampedOpacity = Math.max(0, Math.min(100, textureOpacity));
    // Override in subclasses to implement texture opacity
    Log.d("ARNodeBase", "Texture opacity change requested: " + clampedOpacity);
    updateMaterial();
  }

// MARK: - Enhanced Physics Properties

  @Override
  @SimpleProperty(description = "If the property is set to true, physics apply")
  public boolean EnablePhysics() {
    return enablePhysics;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void EnablePhysics(boolean enablePhysics) {
    this.enablePhysics = enablePhysics;
    if (enablePhysics) {
      setupPhysics();
    } else {
      disablePhysics();
    }
  }

  protected void setupPhysics() {
    // Override in subclasses to implement physics setup
    Log.d("ARNodeBase", "Physics setup requested - override in subclass");
  }

  protected void disablePhysics() {
    // Override in subclasses to implement physics cleanup
    Log.d("ARNodeBase", "Physics disable requested - override in subclass");
  }

// Enhanced Physics Properties with proper validation

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.1")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void StaticFriction(float friction) {
    this.staticFriction = Math.max(0.0f, Math.min(1.0f, friction));
    updatePhysicsMaterial();
  }

  public float StaticFriction() {
    return staticFriction;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.1")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void DynamicFriction(float friction) {
    this.dynamicFriction = Math.max(0.0f, Math.min(1.0f, friction));
    updatePhysicsMaterial();
  }

  public float DynamicFriction() {
    return dynamicFriction;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.5")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void Restitution(float rest) {
    this.restitution = Math.max(0.0f, Math.min(1.0f, rest));
    updatePhysicsMaterial();
  }

  public float Restitution() {
    return restitution;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "1.0")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void Mass(float m) {
    this.mass = Math.max(0.001f, m); // Minimum mass to prevent physics issues
    updateMassProperties();
  }

  public float Mass() {
    return mass;
  }

  /*
  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.5")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void RollingForce(float f) {
    this.rollingForce = f;
  }

  public float RollingForce() {
    return rollingForce;
  }*/

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "2.0")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void DragSensitivity(float d) {
    this.dragSensitivity = Math.max(0.1f, d); // Minimum sensitivity
  }

  public float DragSensitivity() {
    return dragSensitivity;
  }

  @SimpleProperty(description = "Linear damping for physics body (air resistance)")
  public float LinearDamping() {
    return linearDamping;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void LinearDamping(float damping) {
    this.linearDamping = Math.max(0.0f, Math.min(1.0f, damping));
    updatePhysicsBody();
  }

  @SimpleProperty(description = "Angular damping for physics body (rotational air resistance)")
  public float AngularDamping() {
    return angularDamping;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void AngularDamping(float damping) {
    this.angularDamping = Math.max(0.0f, Math.min(1.0f, damping));
    updatePhysicsBody();
  }

// Physics update methods

  protected void updatePhysicsMaterial() {
    if (enablePhysics) {
      Log.d("ARNodeBase", "Updating physics material - override in subclass");
    }
  }

  protected void updateMassProperties() {
    if (enablePhysics) {
      Log.d("ARNodeBase", "Updating mass properties - override in subclass");
    }
  }

  protected void updatePhysicsBody() {
    if (enablePhysics) {
      Log.d("ARNodeBase", "Updating physics body - override in subclass");
    }
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "sphere")
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void CollisionShape(String shape) {
    this.collisionShape = shape;
    updateCollisionShape();
  }

  public String CollisionShape() {
    return collisionShape;
  }

  protected void updateCollisionShape() {
    if (enablePhysics) {
      Log.d("ARNodeBase", "Updating collision shape - override in subclass");
    }
  }

// MARK: - Enhanced Gesture Properties

  @Override
  @SimpleProperty(description = "If the property is set to true, then the node can be scaled using the pinch gesture. Otherwise, a node's Scale cannot be changed by the pinch gesture.")
  public boolean PinchToScale() {
    return pinchToScale;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void PinchToScale(boolean pinchToScale) {
    this.pinchToScale = pinchToScale;
  }

  @Override
  @SimpleProperty(description = "If the property is set to true, then the node can be moved using a one finger pan gesture. Otherwise, a node's x,y-position cannot be changed by the pan gesture.")
  public boolean PanToMove() {
    return panToMove;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void PanToMove(boolean panToMove) {
    this.panToMove = panToMove;
  }

  @Override
  @SimpleProperty(description = "If the property is set to true, the node can be rotated around its y-axis using a two finger rotation gesture. Clockwise increases the angle, and counter clockwise decreases the angle. Otherwise, the node's rotation cannot be changed with a rotation gesture.")
  public boolean RotateWithGesture() {
    return rotateWithGesture;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void RotateWithGesture(boolean r) {
    this.rotateWithGesture = r;
  }

// MARK: - Enhanced Position Properties with Unit Conversion

  @Override
  @SimpleProperty(description = "The x position in centimeters of the node.")
  public float XPosition() {
    if (anchor != null) {
      return UnitHelper.metersToCentimeters(anchor.getPose().tx());
    }
    return UnitHelper.metersToCentimeters(fromPropertyPosition[0]);
  }

  @Override
  @SimpleProperty
  public void XPosition(float xPosition) {
    float xMeters = UnitHelper.centimetersToMeters(xPosition);
    fromPropertyPosition[0] = xMeters; // keep in sync for serialization
    float[] currentPos = getCurrentPosition();
    currentPos[0] = xMeters;
    setCurrentPosition(currentPos);
    reanchorAtCurrentPosition(planeFinder);
  }

  @Override
  @SimpleProperty(description = "The y position in centimeters of the node.")
  public float YPosition() {
    if (anchor != null) {
      return UnitHelper.metersToCentimeters(anchor.getPose().ty());
    }
    return UnitHelper.metersToCentimeters(fromPropertyPosition[1]);
  }

  @Override
  @SimpleProperty
  public void YPosition(float yPosition) {
    float yMeters = UnitHelper.centimetersToMeters(yPosition);
    fromPropertyPosition[1] = yMeters;
    float[] currentPos = getCurrentPosition();
    currentPos[1] = yMeters;
    setCurrentPosition(currentPos);
    reanchorAtCurrentPosition(planeFinder);
  }
  @Override
  @SimpleProperty(description = "The z position in centimeters of the node.")
  public float ZPosition() {
    if (anchor != null) {
      return UnitHelper.metersToCentimeters(anchor.getPose().tz());
    }
    return UnitHelper.metersToCentimeters(fromPropertyPosition[2]);
  }

  @Override
  @SimpleProperty
  public void ZPosition(float zPosition) {
    float zMeters = UnitHelper.centimetersToMeters(zPosition);
    fromPropertyPosition[2] = zMeters;
    float[] currentPos = getCurrentPosition();
    currentPos[2] = zMeters;
    setCurrentPosition(currentPos);
    reanchorAtCurrentPosition(planeFinder);
  }

  private void updatePositionFromProperties() {
    setCurrentPosition(fromPropertyPosition);
    reanchorAtCurrentPosition(planeFinder);
  }

// MARK: - Enhanced Rotation Properties

  @Override
  @SimpleProperty(description = "The x rotation of the node in degrees.")
  public float XRotation() {
    if (anchor != null) {
      float[] euler = quaternionToEulerAngles(anchor.getPose().getRotationQuaternion());
      return (float) Math.toDegrees(euler[0]);
    }
    return 0f;
  }

  @Override
  @SimpleProperty
  public void XRotation(float xRotation) {
    setRotationComponent(0, (float) Math.toRadians(xRotation));
  }

  @Override
  @SimpleProperty(description = "The y rotation of the node in degrees.")
  public float YRotation() {
    if (anchor != null) {
      float[] euler = quaternionToEulerAngles(anchor.getPose().getRotationQuaternion());
      return (float) Math.toDegrees(euler[1]);
    }
    return 0f;
  }

  @Override
  @SimpleProperty
  public void YRotation(float yRotation) {
    setRotationComponent(1, (float) Math.toRadians(yRotation));
  }

  @Override
  @SimpleProperty(description = "The z rotation of the node in degrees.")
  public float ZRotation() {
    if (anchor != null) {
      float[] euler = quaternionToEulerAngles(anchor.getPose().getRotationQuaternion());
      return (float) Math.toDegrees(euler[2]);
    }
    return 0f;
  }

  @Override
  @SimpleProperty
  public void ZRotation(float zRotation) {
    setRotationComponent(2, (float) Math.toRadians(zRotation));
  }


  public void setRotationComponent(int component, float radians) {
    if (anchor == null) return;
    float[] euler = quaternionToEulerAngles(anchor.getPose().getRotationQuaternion());
    euler[component] = radians;
    float[] quaternion = eulerAnglesToQuaternion(euler);
    float[] position = getCurrentPosition(); // from matrix
    Pose newPose = new Pose(position, quaternion);
    try {
      Anchor newAnchor = trackable != null
          ? trackable.createAnchor(newPose)
          : session.createAnchor(newPose);
      if (anchor != null) anchor.detach();
      anchor = newAnchor;
      initWorldMatrixFromAnchor(); // sync matrix
    } catch (Exception e) {
      Log.w("ARNodeBase", "setRotationComponent failed: " + e.getMessage());
    }
  }

  public float[] InitialRotation() { return fromPropertyRotation; }

  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Set the initial rotation of the object from property. The rotation of the node in the form of rotation of degrees eg 45,0,0.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  public void InitialRotation(String rFromProperty) {
    String[] rotationAray = rFromProperty.split(",");
    float[] rotation = {0f, 0f, 0f};

    for (int i = 0; i < rotationAray.length; i++) { //&& i < 3 why?
      try {
        rotation[i] = Float.parseFloat(rotationAray[i]); // why .trim());
      } catch (NumberFormatException e) {
        Log.w("ARNodeBase", "Invalid number in position: " + rotationAray[i]);
      }
    }
    fromPropertyRotation = rotation; // Ensure positive scale
  }

  @Override
  @SimpleProperty(description = "The scale of the node. This is used to multiply its sizing properties. Values less than zero will be treated as their absolute value.")
  public float Scale() {
    return this.scale;
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "1")
  public void Scale(float s) {
    this.scale = Math.abs(s); // Ensure positive scale
    updateScale();
  }

  protected void updateScale() {
    // Override in subclasses to implement scaling
    Log.d("ARNodeBase", "Scale update requested: " + scale);
    if (enablePhysics) {
      updateCollisionShape();
    }
  }

  @Override
  @SimpleProperty(description = "Specifies whether a node is following an ImageMarker. Returns true if it is and false otherwise.")
  public boolean IsFollowingImageMarker() {
    return followingMarker != null;
  }

// MARK: - Enhanced Drag System Properties

  @SimpleProperty(description = "Whether the node is currently being dragged")
  public boolean IsBeingDragged() {
    return isBeingDragged;
  }

  public void setBeingDragged(boolean dragged) {
    this.isBeingDragged = dragged;
  }

  public Object getOriginalMaterial() {
    return originalMaterial;
  }

  public void setOriginalMaterial(Object material) {
    this.originalMaterial = material;
  }

// MARK: - Enhanced Movement Functions

  @Override
  @SimpleFunction(description = "Changes the node's x rotation by the given degrees.")
  @SimpleProperty(description = "Changes the node's x rotation by the given degrees.")
  public void RotateXBy(float degrees) {
    float currentX = XRotation();
    XRotation(currentX + degrees);
  }

  @Override
  @SimpleFunction(description = "Changes the node's y rotation by the given degrees.")
  @SimpleProperty(description = "Changes the node's y rotation by the given degrees.")
  public void RotateYBy(float degrees) {
    float currentY = YRotation();
    YRotation(currentY + degrees);
  }

  @Override
  @SimpleFunction(description = "Changes the node's z rotation by the given degrees.")
  @SimpleProperty(description = "Changes the node's z rotation by the given degrees.")
  public void RotateZBy(float degrees) {
    float currentZ = ZRotation();
    ZRotation(currentZ + degrees);
  }

  @Override
  @SimpleFunction(description = "Changes the node's scale by the given scalar.")
  public void ScaleBy(float scalar) {
    Scale(Scale() * Math.abs(scalar));
  }


  @SimpleFunction(description = "Changes the node's scale by the given scalar.")
  public void ScaleByPinch(float scalar) {
    Log.i("arnodebase", "scale by pinch");
    Scale(Scale() * Math.abs(scalar));
  }


  @Override
  public void MoveBy(float x, float y, float z) {
    float[] currentPos = getCurrentPosition();
    setCurrentPosition(new float[]{
        currentPos[0] + UnitHelper.centimetersToMeters(x),
        currentPos[1] + UnitHelper.centimetersToMeters(y),
        currentPos[2] + UnitHelper.centimetersToMeters(z)
    });
    reanchorAtCurrentPosition(planeFinder);
  }

  @Override
  public void MoveTo(float x, float y, float z) {
    setCurrentPosition(new float[]{
        UnitHelper.centimetersToMeters(x),
        UnitHelper.centimetersToMeters(y),
        UnitHelper.centimetersToMeters(z)
    });
    reanchorAtCurrentPosition(planeFinder);
  }

  @Override
  @SimpleFunction(description = "Moves the node to a detected plane.")
  public void MoveToDetectedPlane(ARDetectedPlane targetPlane, Object p) {
    // Override in subclasses for specific plane placement logic
    Log.d("ARNodeBase", "MoveToDetectedPlane - override in subclass for specific behavior");
  }

// MARK: - Enhanced Distance Calculations

  @Override
  @SimpleFunction(description = "Calculates the distance, in centimeters, between two nodes.")
  public float DistanceToNode(ARNode node) {
    float[] myPos = getCurrentPosition();
    float[] otherPos = ((ARNodeBase) node).getCurrentPosition();
    return UnitHelper.metersToCentimeters(vectorDistance(myPos, otherPos));
  }

  @Override
  @SimpleFunction(description = "Calculates the distance, in centimeters, between a node and a Spotlight.")
  public float DistanceToSpotlight(ARSpotlight light) {
    // Placeholder - implementation depends on ARSpotlight interface
    return UnitHelper.metersToCentimeters(0f);
  }

  @Override
  @SimpleFunction(description = "Calculates the distance, in centimeters, between a node and a Pointlight.")
  public float DistanceToPointLight(ARPointLight light) {
    // Placeholder - implementation depends on ARPointLight interface
    return UnitHelper.metersToCentimeters(0f);
  }

  @Override
  @SimpleFunction(description = "Calculates the distance, in centimeters, between a node and a DetectedPlane.")
  public float DistanceToDetectedPlane(ARDetectedPlane detectedPlane) {
    // Placeholder - implementation depends on ARDetectedPlane interface
    return UnitHelper.metersToCentimeters(0f);
  }

// MARK: - Enhanced Drag System Methods


  // Primary gesture system - all camera info comes from ARView3D
  public void handleAdvancedGestureUpdate(PointF fingerLocation, PointF fingerMovement,
                                          PointF fingerVelocity, float[] groundProjection,
                                          CameraVectors camera3DProjection, String gesturePhase) {
    if ("began".equals(gesturePhase)) {
      startDrag(fingerLocation);
    } else if ("changed".equals(gesturePhase)) {
      updateDrag(groundProjection);
    } else if ("ended".equals(gesturePhase)) {
      endDrag(fingerVelocity, camera3DProjection);
    }
  }

  // ARNodeBase — complete version
  @Override
  public void startDrag(PointF fingerLocation) {
    isBeingDragged = true;
    lastFingerPosition = null;  // reset so first updateDrag has no delta
    dragStartLocation.set(fingerLocation.x, fingerLocation.y);
    dragStartTime = System.currentTimeMillis();

    // Zero velocity so physics doesn't fight drag on any node type
    currentVelocity[0] = 0;
    currentVelocity[1] = 0;
    currentVelocity[2] = 0;

    if (originalMaterial == null) {
      originalMaterial = getCurrentMaterial();
    }
    showDragEffect();
    Log.d("ARNodeBase", "startDrag for " + NodeType()
        + " at " + arrayToString(getCurrentPosition())
        + " groundLevel=" + GROUND_LEVEL);
  }


  // ARNodeBase — handles all node types
  @Override
  public void updateDrag(float[] groundProjection) {
    if (!isBeingDragged || groundProjection == null) return;

    if (lastFingerPosition != null) {
      float[] movement = subtractVectors(groundProjection, lastFingerPosition);
      float distance = vectorLength(movement);

      if (distance > 0.75f) {
        lastFingerPosition = groundProjection.clone();
        return;
      }

      float[] currentPos = getCurrentPosition();
      float[] newPos = {
          groundProjection[0],
          currentPos[1],  // preserve Y by default
          groundProjection[2]
      };

      setCurrentPosition(newPos);
    }
    Log.d("ARNodeBase", "updating drag for " + NodeType()
        + " at " + arrayToString(getCurrentPosition())
        + " groundLevel=" + GROUND_LEVEL);
    lastFingerPosition = groundProjection.clone();
  }


  protected float[] subtractVectors(float[] a, float[] b) {
    return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
  }


  public void endDrag(PointF fingerVelocity, CameraVectors cameraVectors) {
    if (!isBeingDragged) return;

    isBeingDragged = false;          // once, here
    lastFingerPosition = null;

    reanchorAtCurrentPosition(planeFinder);

    if (EnablePhysics() && fingerVelocity != null) {
      applyReleaseVelocity(fingerVelocity, cameraVectors);
    } else {
      currentVelocity[0] = 0;
      currentVelocity[1] = 0;
      currentVelocity[2] = 0;
    }

    restoreOriginalMaterial();
    Log.d("ARNodeBase", "endDrag for " + NodeType());
  }


  public void applyReleaseVelocity(PointF releaseVelocity, CameraVectors cameraVectors) {

  }

  protected Object getCurrentMaterial() {
    // Just store the opacity since that's what most drag effects change
    return getCurrentOpacity();
  }

  protected void setMaterial(Object material) {
    if (material instanceof Float) {
      setOpacity((Float) material);
    }
  }

  protected void restoreOriginalMaterial() {
    setOpacity(1.0f);
  }

  protected void showDragEffect() {
    if (originalMaterial == null) {
      originalMaterial = getCurrentOpacity();
    }
    setOpacity(0.7f);
  }

  private float getCurrentOpacity() {
    return Opacity() / 100.0f; // Convert from 0-100 to 0.0-1.0
  }

  private void setOpacity(float opacity) {
    Opacity((int) (opacity * 100)); // Convert back to 0-100 range
  }
// MARK: - Enhanced Image Marker Following

  @Override
  @SimpleFunction(description = "Makes the node follow an ImageMarker and sets its position to be the center of the detected image.")
  public void Follow(ARImageMarker imageMarker) {
    if (followingMarker != null) {
      Log.w("ARNodeBase", "Node already following a marker");
      return;
    }

    followingMarker = imageMarker;
    // Implementation depends on ARImageMarker interface
    Log.i("ARNodeBase", "Started following image marker");
  }

  @Override
  @SimpleFunction(description = "Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).")
  public void FollowWithOffset(ARImageMarker imageMarker, float x, float y, float z) {
    if (followingMarker != null) {
      Log.w("ARNodeBase", "Node already following a marker");
      return;
    }

    followingMarker = imageMarker;
    // Set offset position
    float[] offsetMeters = {
        UnitHelper.centimetersToMeters(x),
        UnitHelper.centimetersToMeters(y),
        UnitHelper.centimetersToMeters(z)
    };
    // Implementation depends on ARImageMarker interface
    Log.i("ARNodeBase", "Started following image marker with offset");
  }

  @Override
  @SimpleFunction(description = "Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.")
  public void StopFollowingImageMarker() {
    if (followingMarker == null) {
      Log.w("ARNodeBase", "Node not following any marker");
      return;
    }

    // Get current world position before stopping
    if (anchor != null) {
      fromPropertyPosition = anchor.getPose().getTranslation();
    }

    followingMarker = null;
    StoppedFollowingMarker();
    Log.i("ARNodeBase", "Stopped following image marker");
  }

// MARK: - Enhanced Look-At Functions

  @Override
  @SimpleFunction(description = "Rotates the node to look at the given node.")
  public void LookAtNode(ARNode node) {
    // Implementation depends on getting other node's position
    Log.d("ARNodeBase", "LookAtNode - implement based on ARNode interface");
  }
}
