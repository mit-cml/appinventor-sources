// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import android.util.Log;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.util.List;
import java.util.Collection;

@SimpleObject
public abstract class ARNodeBase implements ARNode, FollowsMarker {
  // Core container and AR properties
  protected ARView3D arView = null;
  protected Anchor anchor = null;
  protected float[] fromPropertyPosition = {0f, 0f, 0f};
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
  protected float gravityScale = 0.5f;
  protected float releaseForceMultiplier = 0.0f;
  protected boolean enablePhysics = false;

  // Enhanced Gesture Properties
  protected boolean pinchToScale = false;
  protected boolean panToMove = false;
  protected boolean rotateWithGesture = false;

  // Enhanced Drag System Properties
  protected boolean isBeingDragged = false;
  protected PointF dragStartLocation = new PointF(0, 0);
  protected PointF lastDragLocation = new PointF(0, 0);
  protected Object originalMaterial = null;

  // Enhanced Physics Simulation Properties
  protected float[] currentVelocity = {0, 0, 0};
  protected boolean isCurrentlyColliding = false;
  protected float linearDamping = 0.0f;
  protected float angularDamping = 0.0f;
  protected float rollingForce = 0.0f;
  protected float impulseScale = 0.0f;

  // Enhanced Collision Properties
  protected float collisionWidth = 0.0f;
  protected float collisionHeight = 0.0f;
  protected float collisionDepth = 0.0f;
  protected float collisionRadius = 0.0f;

  // Enhanced Marker and Positioning Properties
  protected ARImageMarker followingMarker = null;
  protected String worldOffset = "";
  protected Object geoAnchor = null;
  protected float[] previewPlacementSurface = null;
  protected boolean hasPreviewSurface = false;

  protected long dragStartTime;



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

  // MARK: - Enhanced Basic Properties

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

  // MARK: - Enhanced Collision System

  @Override
  @SimpleEvent(description = "Collision event detected")
  public void CollisionDetection() {
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
    new android.os.Handler().postDelayed(() -> {
      if (!isBeingDragged) {
        restoreOriginalMaterial();
      }
    }, 500);
  }

  protected void handleObjectCollision(ARNode otherNode) {
    showCollisionEffect("object");

    // Restore appearance after delay
    new android.os.Handler().postDelayed(() -> {
      if (!isBeingDragged) {
        restoreOriginalMaterial();
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

  public float[] PoseFromPropertyPosition() {
    return fromPropertyPosition;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Set the current pose of the object from property. Format is a comma-separated list of 3 coordinates: x, y, z such that 0, 0, 1 places the object at x of 0, y of 0 and z of 1",
      category = PropertyCategory.APPEARANCE)
  public void PoseFromPropertyPosition(String positionFromProperty) {
    String[] positionArray = positionFromProperty.split(",");
    float[] position = {0f, 0f, 0f};

    for (int i = 0; i < positionArray.length; i++) { //&& i < 3 why?
      try {
        position[i] = Float.parseFloat(positionArray[i]); // why .trim());
      } catch (NumberFormatException e) {
        Log.w("ARNodeBase", "Invalid number in position: " + positionArray[i]);
      }
    }

    this.fromPropertyPosition = position;
    float[] rotation = {0f, 0f, 0f, 1f}; // no rotation by default

    if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    } else if (session != null) {
      Anchor myAnchor = session.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    }

    Log.i("ARNodeBase", "Stored pose with position " + positionFromProperty);
  }

  @Override
  public void PoseFromPropertyPositions(String x, String y, String z) {
    try {
      float xPos = Float.parseFloat(x.trim());
      float yPos = Float.parseFloat(y.trim());
      float zPos = Float.parseFloat(z.trim());
      PoseFromPropertyPosition(xPos + "," + yPos + "," + zPos);
    } catch (NumberFormatException e) {
      Log.e("ARNodeBase", "Invalid position values: " + x + "," + y + "," + z, e);
    }
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
    return true;
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
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void StaticFriction(float friction) {
    this.staticFriction = Math.max(0.0f, Math.min(1.0f, friction));
    updatePhysicsMaterial();
  }

  public float StaticFriction() {
    return staticFriction;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.1")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void DynamicFriction(float friction) {
    this.dynamicFriction = Math.max(0.0f, Math.min(1.0f, friction));
    updatePhysicsMaterial();
  }

  public float DynamicFriction() {
    return dynamicFriction;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.5")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Restitution(float rest) {
    this.restitution = Math.max(0.0f, Math.min(1.0f, rest));
    updatePhysicsMaterial();
  }

  public float Restitution() {
    return restitution;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "1.0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
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
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void DragSensitivity(float d) {
    this.dragSensitivity = Math.max(0.1f, d); // Minimum sensitivity
  }

  public float DragSensitivity() {
    return dragSensitivity;
  }

// New enhanced physics properties

  /*@SimpleProperty(description = "The gravity scale affecting this node. 1.0 = normal gravity, 0.0 = no gravity")
  public float GravityScale() {
    return gravityScale;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.5")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void GravityScale(float gravityScale) {
    this.gravityScale = Math.max(0.0f, gravityScale);
    updatePhysicsBody();
  }*/

  /* @SimpleProperty(description = "Multiplier for release force when drag ends")
  public float ReleaseForceMultiplier() {
    return releaseForceMultiplier;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void ReleaseForceMultiplier(float multiplier) {
    this.releaseForceMultiplier = multiplier;
  }
  */


  @SimpleProperty(description = "Linear damping for physics body (air resistance)")
  public float LinearDamping() {
    return linearDamping;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void LinearDamping(float damping) {
    this.linearDamping = Math.max(0.0f, Math.min(1.0f, damping));
    updatePhysicsBody();
  }

  @SimpleProperty(description = "Angular damping for physics body (rotational air resistance)")
  public float AngularDamping() {
    return angularDamping;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
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
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
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
    fromPropertyPosition[0] = xMeters;
    updatePositionFromProperties();
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
    updatePositionFromProperties();
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
    updatePositionFromProperties();
  }

  private void updatePositionFromProperties() {
    float[] rotation = {0f, 0f, 0f, 1f};
    Pose newPose = new Pose(fromPropertyPosition, rotation);

    if (trackable != null) {
      Anchor(trackable.createAnchor(newPose));
    } else if (session != null) {
      Anchor(session.createAnchor(newPose));
    }
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

  private void setRotationComponent(int component, float radians) {
    if (anchor != null) {
      float[] euler = quaternionToEulerAngles(anchor.getPose().getRotationQuaternion());
      euler[component] = radians;
      float[] quaternion = eulerAnglesToQuaternion(euler);

      float[] position = anchor.getPose().getTranslation();
      Pose newPose = new Pose(position, quaternion);

      if (trackable != null) {
        Anchor(trackable.createAnchor(newPose));
      } else if (session != null) {
        Anchor(session.createAnchor(newPose));
      }
    }
  }

// MARK: - Enhanced Scale Property

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
  public void RotateXBy(float degrees) {
    float currentX = XRotation();
    XRotation(currentX + degrees);
  }

  @Override
  @SimpleFunction(description = "Changes the node's y rotation by the given degrees.")
  public void RotateYBy(float degrees) {
    float currentY = YRotation();
    YRotation(currentY + degrees);
  }

  @Override
  @SimpleFunction(description = "Changes the node's z rotation by the given degrees.")
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
    Log.i("arnodebase","scale by pinch");
    Scale(Scale() * Math.abs(scalar));
  }


  @Override
  @SimpleFunction(description = "Changes the node's position by (x,y,z) in centimeters.")
  public void MoveBy(float x, float y, float z) {
    float xMeters = UnitHelper.centimetersToMeters(x);
    float yMeters = UnitHelper.centimetersToMeters(y);
    float zMeters = UnitHelper.centimetersToMeters(z);

    float[] currentPos;
    if (anchor != null) {
      currentPos = anchor.getPose().getTranslation();
    } else {
      currentPos = fromPropertyPosition;
    }

    float[] newPosition = {
        currentPos[0] + xMeters,
        currentPos[1] + yMeters,
        currentPos[2] + zMeters
    };

    float[] rotation = {0f, 0f, 0f, 1f};
    if (anchor != null) {
      rotation = anchor.getPose().getRotationQuaternion();
    }

    Pose newPose = new Pose(newPosition, rotation);

    if (trackable != null) {
      Anchor(trackable.createAnchor(newPose));
    } else if (session != null) {
      Anchor(session.createAnchor(newPose));
    }

    Log.i("ARNodeBase", "Moved by (" + x + ", " + y + ", " + z + ") cm");
  }

  @Override
  @SimpleFunction(description = "Changes the node's position to (x,y,z) in centimeters.")
  public void MoveTo(float x, float y, float z) {
    float xMeters = UnitHelper.centimetersToMeters(x);
    float yMeters = UnitHelper.centimetersToMeters(y);
    float zMeters = UnitHelper.centimetersToMeters(z);

    float[] position = {xMeters, yMeters, zMeters};
    float[] rotation = {0f, 0f, 0f, 1f};

    if (anchor != null) {
      rotation = anchor.getPose().getRotationQuaternion();
    }

    Pose newPose = new Pose(position, rotation);

    if (trackable != null) {
      Anchor(trackable.createAnchor(newPose));
    } else if (session != null) {
      Anchor(session.createAnchor(newPose));
    }

    Log.i("ARNodeBase", "Moved to (" + x + ", " + y + ", " + z + ") cm");
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
    if (anchor == null) return 0f;

    // Get positions and calculate distance
    float[] myPos = anchor.getPose().getTranslation();
    // Implementation would need access to other node's position
    // This is a placeholder - actual implementation depends on ARNode interface

    return UnitHelper.metersToCentimeters(0f); // Placeholder
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
                                          float[] camera3DProjection, String gesturePhase) {
    if ("began".equals(gesturePhase)) {
      startDrag(fingerLocation);
    } else if ("changed".equals(gesturePhase)) {
      updateDrag(fingerLocation, fingerMovement, groundProjection, camera3DProjection);
    } else if ("ended".equals(gesturePhase)) {
      endDrag(fingerVelocity, groundProjection);
    }
  }

  @Override
  public void startDrag(PointF fingerLocation) {
    isBeingDragged = true;
    dragStartLocation.set(fingerLocation.x, fingerLocation.y);
    dragStartTime = System.currentTimeMillis();
    if (originalMaterial == null) {
      originalMaterial = getCurrentMaterial();
    }
    showDragEffect();
    Log.d("ARNodeBase", "Started advanced drag for " + NodeType());
  }

  @Override
  public void updateDrag(PointF fingerLocation, PointF fingerMovement,
                                    float[] groundProjection, float[] camera3DProjection) {
    // Default implementation - override in subclasses
    if (groundProjection != null) {
      // Convert meters to centimeters for MoveTo
      MoveTo(groundProjection[0] * 100, groundProjection[1] * 100, groundProjection[2] * 100);
    }
  }

  @Override
  public void endDrag(PointF fingerVelocity, float[] finalPosition) {
    isBeingDragged = false;
    restoreOriginalMaterial();

    // Apply momentum if velocity is high enough
    float velocityMagnitude = (float) Math.sqrt(
        fingerVelocity.x * fingerVelocity.x + fingerVelocity.y * fingerVelocity.y
    );

    if (velocityMagnitude > 100 && EnablePhysics()) {
      applyReleaseForce(fingerVelocity.x, fingerVelocity.y);
    }

    Log.d("ARNodeBase", "Ended advanced drag for " + NodeType());
  }
  // Fix the method signature to match usage
  protected void applyReleaseForce(PointF fingerVelocity) {
    // Convert PointF to individual components for consistency
    applyReleaseForce(fingerVelocity.x, fingerVelocity.y);
  }

  protected void applyReleaseForce(float velocityX, float velocityY) {
    // Override in physics-enabled subclasses
    Log.d("ARNodeBase", "Apply release force - override in subclass");
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
    Opacity((int)(opacity * 100)); // Convert back to 0-100 range
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
