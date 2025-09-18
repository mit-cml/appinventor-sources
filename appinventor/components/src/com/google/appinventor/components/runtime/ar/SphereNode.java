// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ARComponentConstants;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.util.CameraVectors;
import com.google.appinventor.components.runtime.*;

import com.google.appinventor.components.common.PropertyTypeConstants;

import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.Session;
import android.util.Log;
import android.graphics.PointF;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;

@UsesAssets(fileNames = "sphere.obj, Palette.png")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a 3D sphere with advanced physics behaviors in an ARView3D. " +
        "The sphere can roll, bounce, and respond to different surface types like wet, sticky, or slippery. " +
        "External model files can be uploaded but must be less than 5 MB.",
    category = ComponentCategory.AR)
@SimpleObject
public final class SphereNode extends ARNodeBase implements ARSphere {

  // Core sphere properties
  private String objectModel = Form.ASSETS_PREFIX + "sphere.obj";
  private String texture = Form.ASSETS_PREFIX + "Palette.png";
  private String behaviorName = "rolling";
  private ARNodeContainer _container;

  private float radius = 0.05f; // stored in meters

  // Drag system properties
  private DragMode currentDragMode = DragMode.ROLLING;
  private float[] dragStartPosition;
  private long dragStartTime;
  private float[] lastFingerPosition;
  private float pickupStartHeight = 0.0f;
  private boolean isBeingDragged = false;

  // Physics constants
  private float dragStartDamping = 0.1f;
  private float dragStartAngularDamping = 0.01f;

  // Speed thresholds for different behaviors
  private static final float NORMAL_ROLLING_SPEED = 1200f;
  private static final float FAST_ROLLING_SPEED = 2500f;
  private static final float FLING_THRESHOLD_SPEED = 3500f;
  private static final float PICKUP_FLING_SPEED = 2000f;

  private long lastUpdateTime = System.currentTimeMillis();
  private boolean isActivelyRolling = false;

  // Surface behavior system
  private EnumSet<SurfaceBehaviorFlags> behaviorFlags = EnumSet.of(SurfaceBehaviorFlags.ROLLING);

  // Collision effect tracking
  private boolean isCurrentlyColliding = false;
  private static final String LOG_TAG = "SphereNode";

  // Enums and inner classes
  public enum DragMode {
    ROLLING,    // Rolling ball along the floor
    PICKUP,     // Ball lifted off the floor
    FLINGING    // Ball thrown with velocity
  }

  public enum SurfaceBehaviorFlags {
    ROLLING(1 << 0),    // Rolls when on ground
    BOUNCY(1 << 1),     // High bounce
    FLOATING(1 << 2),   // Reduced gravity
    WET(1 << 3),        // High friction, low bounce
    STICKY(1 << 4),     // Extreme adherence
    SLIPPERY(1 << 5),   // Low friction
    HEAVY(1 << 6),      // High mass
    LIGHT(1 << 7);      // Low mass

    private final int value;

    SurfaceBehaviorFlags(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public SphereNode(final ARNodeContainer container) {
    super(container);
    Model(objectModel);
    Texture(texture);
    container.addNode(this);
    _container = container;

    // Set default rolling behavior
    updateBehaviorSettings();
  }

  @Override
  public void Session(Session s) {
    this.session = s;
  }

  // MARK: - Enhanced Radius Property

  @Override
  @SimpleProperty(description = "The radius of the sphere in centimeters.")
  public float RadiusInCentimeters() {
    return UnitHelper.metersToCentimeters(radius);
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void RadiusInCentimeters(float radiusInCentimeters) {
    this.radius = UnitHelper.centimetersToMeters(Math.abs(radiusInCentimeters));
    updateSphereMesh();
  }

  private void updateSphereMesh() {
    // Update the sphere mesh with new radius
    Log.i("SphereNode", "Updating sphere mesh with radius: " + radius + "m");
    // Implementation would depend on your 3D rendering system
  }

  // MARK: - Surface Behavior Properties

  @SimpleProperty(description = "Whether the sphere rolls when on the ground")
  public boolean IsRolling() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.ROLLING);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsRolling(boolean rolling) {
    if (rolling) {
      behaviorFlags.add(SurfaceBehaviorFlags.ROLLING);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.ROLLING);
    }
    //updateBehaviorSettings();
  }

  @SimpleProperty(description = "Whether the sphere bounces with high restitution")
  public boolean IsBouncy() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.BOUNCY);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsBouncy(boolean bouncy) {
    if (bouncy) {
      behaviorFlags.add(SurfaceBehaviorFlags.BOUNCY);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.BOUNCY);
    }
    updateBehaviorSettings();
  }

  @SimpleProperty(description = "Whether the sphere floats with reduced gravity")
  public boolean IsFloating() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.FLOATING);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsFloating(boolean floating) {
    if (floating) {
      behaviorFlags.add(SurfaceBehaviorFlags.FLOATING);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.FLOATING);
    }
    updateBehaviorSettings();
  }

  @SimpleProperty(description = "Whether the sphere has wet surface properties (high friction, low bounce)")
  public boolean IsWet() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.WET);
  }


  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsWet(boolean wet) {
    if (wet) {
      behaviorFlags.add(SurfaceBehaviorFlags.WET);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.WET);
    }
    updateBehaviorSettings();
  }

  @SimpleProperty(description = "Whether the sphere sticks to surfaces (extreme friction)")
  public boolean IsSticky() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.STICKY);
  }


  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsSticky(boolean sticky) {
    if (sticky) {
      behaviorFlags.add(SurfaceBehaviorFlags.STICKY);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.STICKY);
    }
    updateBehaviorSettings();
  }

  @SimpleProperty(description = "Whether the sphere is slippery (low friction)")
  public boolean IsSlippery() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.SLIPPERY);
  }


  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsSlippery(boolean slippery) {
    if (slippery) {
      behaviorFlags.add(SurfaceBehaviorFlags.SLIPPERY);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.SLIPPERY);
    }
    updateBehaviorSettings();
  }

  @SimpleProperty(description = "Whether the sphere has heavy mass properties")
  public boolean IsHeavySphere() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.HEAVY);
  }


  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsHeavySphere(boolean heavy) {
    if (heavy) {
      behaviorFlags.add(SurfaceBehaviorFlags.HEAVY);
      behaviorFlags.remove(SurfaceBehaviorFlags.LIGHT);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.HEAVY);
    }
    updateBehaviorSettings();
  }

  @SimpleProperty(description = "Whether the sphere has light mass properties")
  public boolean IsLightSphere() {
    return behaviorFlags.contains(SurfaceBehaviorFlags.LIGHT);
  }


  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void IsLightSphere(boolean light) {
    if (light) {
      behaviorFlags.add(SurfaceBehaviorFlags.LIGHT);
      behaviorFlags.remove(SurfaceBehaviorFlags.HEAVY);
    } else {
      behaviorFlags.remove(SurfaceBehaviorFlags.LIGHT);
    }
    updateBehaviorSettings();
  }


  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_BEHAVIOR_TYPE, defaultValue = "default behavior")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Default behavior of sphere: rolling, heavy, light, bouncy, wet, sticky, slippery, floating. Use these settings to automatically configure Mass, StaticFriction, DynamicFriction, Restitution, and DragSensitivity, or tweak them individually afterward.")
  public void DefaultBehavior(String behavior) {
    this.behaviorName = behavior;
    addBehavior(behavior);

    Log.i("SphereNode", "Applied behavior: " + behavior +
        " - Mass: " + Mass() + ", Friction: " + StaticFriction() +
        ", Restitution: " + Restitution() + ", DragSensitivity: " + DragSensitivity());
  }

  @SimpleProperty(description = "Gets the current default behavior name")
  public String DefaultBehavior() {
    return behaviorName;
  }

  @SimpleFunction(description = "Add a behavior to the sphere (rolling, bouncy, floating, wet, sticky, slippery, heavy, light)")
  public void AddBehavior(String behaviorName) {
    addBehavior(behaviorName);
  }

  private void addBehavior(String behaviorName) {
    switch (behaviorName.toLowerCase()) {
      case "rolling":
        IsRolling(true);
        break;
      case "bouncy":
        IsBouncy(true);
        break;
      case "floating":
        IsFloating(true);
        break;
      case "wet":
        IsWet(true);
        break;
      case "sticky":
        IsSticky(true);
        break;
      case "slippery":
        IsSlippery(true);
        break;
      case "heavy":
        IsHeavySphere(true);
        break;
      case "light":
        IsLightSphere(true);
        break;
      default:
        IsRolling(true);
        break;
    }
  }

  public void RemoveBehavior(String behavior) {
    switch (behavior.toLowerCase()) {
      case "rolling":
        IsRolling(false);
        break;
      case "bouncy":
        IsBouncy(false);
        break;
      case "floating":
        IsFloating(false);
        break;
      case "wet":
        IsWet(false);
        break;
      case "sticky":
        IsSticky(false);
        break;
      case "slippery":
        IsSlippery(false);
        break;
      case "heavy":
        IsHeavySphere(false);
        break;
      case "light":
        IsLightSphere(false);
        break;
      default:
        Log.i("SphereNode", "Unknown behavior: " + behavior);
        break;
    }
  }

  @SimpleFunction(description = "Reset sphere to default rolling behavior")
  public void ResetToDefaultSphere() {
    behaviorFlags.clear();
    behaviorFlags.add(SurfaceBehaviorFlags.ROLLING);
    updateBehaviorSettings();
    Log.i("SphereNode", "Sphere reset to default behavior");
  }

  // MARK: - Behavior Settings Update

  private void updateBehaviorSettings() {
    float baseMass = 0.2f;
    float massRatio = Mass() / baseMass;

    float staticFriction = 0.5f;
    float dynamicFriction = 0.3f;
    float restitution = 0.6f;
    float gravityScale = 1.0f;
    float dragSensitivity = 1.0f;

    Log.i(LOG_TAG, "applying behavior for spherenode");
    // Apply mass effects
    MassEffect massEffect = calculateMassEffect(massRatio);

    // Apply behavior-specific defaults (last behavior wins if multiple)
    if (behaviorFlags.contains(SurfaceBehaviorFlags.HEAVY)) {
      Mass(1.0f);
      dragSensitivity = 1.0f;
      staticFriction = 0.7f;
      restitution = 0.3f;
    }

    if (behaviorFlags.contains(SurfaceBehaviorFlags.LIGHT)) {
      Mass(0.04f);
      dragSensitivity = 1.6f;
      restitution = 0.7f;
    }

    if (behaviorFlags.contains(SurfaceBehaviorFlags.BOUNCY)) {
      restitution = 0.9f;
      staticFriction = 0.2f;
      dynamicFriction = 0.12f;
    }

    if (behaviorFlags.contains(SurfaceBehaviorFlags.WET)) {
      staticFriction = 0.8f;
      dynamicFriction = 0.65f;
      restitution = 0.15f;
      dragSensitivity = 0.7f;
    }

    if (behaviorFlags.contains(SurfaceBehaviorFlags.STICKY)) {
      staticFriction = 0.95f;
      dynamicFriction = 0.85f;
      restitution = 0.05f;
      dragSensitivity = 0.4f;
    }

    if (behaviorFlags.contains(SurfaceBehaviorFlags.SLIPPERY)) {
      staticFriction = 0.05f;
      dynamicFriction = 0.02f;
      restitution = 0.8f;
      dragSensitivity = 1.4f;
    }

    if (behaviorFlags.contains(SurfaceBehaviorFlags.FLOATING)) {
      Mass(0.02f);
      gravityScale = 0.05f;
      staticFriction = 0.1f;
      dynamicFriction = 0.06f;
      dragSensitivity = 1.8f;
    }

    // Apply friction and damping effects
    staticFriction *= massEffect.friction;
    dynamicFriction *= massEffect.friction;

    // Set the calculated values
    StaticFriction(staticFriction);
    DynamicFriction(dynamicFriction);
    Restitution(restitution);
    DragSensitivity(dragSensitivity);

    String behaviorNames = getBehaviorNames();
    Log.i("SphereNode", "Applied " + behaviorNames +
        " defaults - Mass: " + Mass() +
        ", Friction: " + staticFriction +
        ", Restitution: " + restitution +
        ", DragSensitivity: " + dragSensitivity);
  }

  private String getBehaviorNames() {
    List<String> names = new ArrayList<>();
    for (SurfaceBehaviorFlags flag : behaviorFlags) {
      names.add(flag.name().toLowerCase());
    }
    return String.join("+", names);
  }

  private static class MassEffect {
    final float friction;
    final float damping;

    MassEffect(float friction, float damping) {
      this.friction = friction;
      this.damping = damping;
    }
  }

  private MassEffect calculateMassEffect(float massRatio) {
    // Mass affects friction and damping differently
    float frictionEffect = (float) Math.sqrt(massRatio);
    float dampingEffect = 1.0f / (float) Math.sqrt(massRatio);
    return new MassEffect(frictionEffect, dampingEffect);
  }

  // MARK: - Enhanced Collision Handling


  @Override
  @SimpleEvent(description = "This event is triggered when a sphereNode collides with another object. Note: physics must be enabled")
  public void ObjectCollidedWithObject(ARNode otherNode) {
    showCollisionEffect();

    // Restore color after delay
    new android.os.Handler().postDelayed(() -> {
      if (!isBeingDragged) {
        restoreOriginalAppearance();
      }
    }, 800);

    // Dispatch event to app level
    super.ObjectCollidedWithObject(otherNode);
    Log.i("SphereNode", name + " collided with " + otherNode.getClass().getSimpleName());
  }

  private void showCollisionEffect() {
    isCurrentlyColliding = true;
    // Store original material if not already stored
    // Change appearance to show collision - green for default
    // Implementation depends on your material/rendering system
    Log.i("SphereNode", "Showing collision effect");
  }

  private void restoreOriginalAppearance() {
    isCurrentlyColliding = false;
    // Restore original material
    Log.i("SphereNode", "Restored original appearance after collision");
  }

  // MARK: - Enhanced Scaling with Physics Correction

  @Override
  @SimpleFunction(description = "Changes the sphere's scale by the given scalar, maintaining bottom position if physics enabled.")
  public void ScaleBy(float scalar) {
    Log.i("SphereNode", "Scaling sphere " + name + " by " + scalar);

    float oldScale = Scale();
    float newScale = oldScale * Math.abs(scalar);

    // Update physics immediately if enabled to maintain bottom position
    if (EnablePhysics()) {
      float previousSize = radius * Scale();
      // Adjust Y position to maintain ground contact
      float[] currentPos = getCurrentPosition();
      currentPos[1] = currentPos[1] - previousSize + (radius * newScale);
      setCurrentPosition(currentPos);
    }

    Scale(newScale);
    Log.i("SphereNode", "Scale complete - bottom position maintained");
  }

  @SimpleFunction(description = "Scale sphere by pinch gesture while maintaining physics accuracy")
  public void ScaleByPinch(float scalar) {
    Log.i("SphereNode", "Pinch scaling sphere " + name + " by " + scalar);

    float oldScale = Scale();
    float newScale = oldScale * Math.abs(scalar);
    float newActualRadius = radius * newScale;

    float minRadius = 0.01f;
    float maxRadius = 20.0f;

    if (newActualRadius < minRadius || newActualRadius > maxRadius) {
      Log.i(LOG_TAG, "Pinch scale rejected - radius would be " + newActualRadius + "m");
      return;
    }

    boolean hadPhysics = EnablePhysics();
    Log.i(LOG_TAG, "has physics?" + hadPhysics);
    if (hadPhysics) {
      // Store physics properties
      float savedMass = Mass();
      float savedFriction = StaticFriction();
      float savedRestitution = Restitution();

      // Temporarily disable physics
      EnablePhysics(false);

      // Update position to maintain bottom contact
      float previousSize = radius * Scale();
      float[] currentPos = getCurrentPosition();
      currentPos[1] = currentPos[1] - previousSize + (radius * newScale);
      setCurrentPosition(currentPos);

      // Apply scaling
      Scale(newScale);

      // Restore physics
      Mass(savedMass);
      StaticFriction(savedFriction);
      Restitution(savedRestitution);
      EnablePhysics(true);

      Log.i("SphereNode", "Physics recreated with correct collision shape");
    } else {
      Scale(newScale);
    }

    Log.i("SphereNode", "Pinch scale complete: " + oldScale + " → " + newScale +
        ", collision radius: " + newActualRadius + "m");
    debugCollisionShape();
  }

  @SimpleFunction(description = "Debug information about the sphere's collision shape")
  public void DebugCollisionShape() {
    debugCollisionShape();
  }

  private void debugCollisionShape() {
    float visualScale = Scale();
    float calculatedRadius = radius * visualScale;

    Log.i("SphereNode", "=== COLLISION SHAPE DEBUG ===");
    Log.i("SphereNode", "Internal radius: " + radius + "m");
    Log.i("SphereNode", "Visual scale: " + visualScale);
    Log.i("SphereNode", "Calculated collision radius: " + calculatedRadius + "m");
    Log.i("SphereNode", "Has physics: " + EnablePhysics());
    Log.i("SphereNode", "==========================");
  }

  // MARK: - Enhanced Drag System

  @SimpleFunction(description = "Start dragging the sphere with enhanced physics control")
  public void StartDrag() {
    startDrag(new PointF());
  }

  @Override
  public void startDrag(PointF fingerLocation) {
    super.startDrag(fingerLocation);
    Log.i("SphereNode", "Starting optimized drag for " + name);

    isBeingDragged = true;
    //lastFingerPosition = null;

    // Store physics settings for restoration
    if (EnablePhysics()) {
      adjustPhysicsForDrag();
    }

    showDragEffect();
  }

  private void adjustPhysicsForDrag() {
    // Android-specific physics adjustment during drag
    // Reduce physics responsiveness for better control
    dragStartDamping = 0.85f;
    dragStartAngularDamping = 0.9f;
  }

 /* @SimpleFunction(description = "Update drag position based on finger world position")
  public void UpdateDrag(String fingerWorldPositionStr) {
    if (!isBeingDragged) return;

    float[] fingerWorldPosition = parseVector3(fingerWorldPositionStr);
    updateDrag(fingerWorldPosition);
  }*/

  @Override
  public void updateDrag(float[] groundProjection){
    if (!isBeingDragged) return;

    float[] currentAnchorPos = getCurrentPosition();
    Log.i("SphereNode", "Current anchor pos: " + arrayToString(currentAnchorPos));

    if (lastFingerPosition != null) {
      float[] movement = subtractVectors(groundProjection, lastFingerPosition);
      float distance = vectorLength(movement);

      if (distance > 0.5f) { // 50cm threshold
        Log.w("SphereNode", "Rejecting extreme jump: " + distance + "m - using previous position");
        return; // Skip this frame entirely
      }

      // Apply movement based on behavior
      updateDragPosition(currentAnchorPos, groundProjection, movement);


      Log.i("SphereNode", "Applied movement: " + arrayToString(movement) + " (distance: " + distance + ")");
    } else {
      Log.i("SphereNode", "DRAG UPDATE: Initial position " + arrayToString(groundProjection));
    }

    lastFingerPosition = groundProjection.clone();
  }

  private void updateDragPosition(float[] currentPos, float[] targetPos, float[] movement) {
    // Direct position control for immediate feedback
    float[] constrainedPos = {
        targetPos[0],
        currentPos[1],
        targetPos[2]
    };
    Log.i("SphereNode", "Current constrainedPos pos: " + arrayToString(constrainedPos));

    applyRealisticRolling(movement);
    setCurrentPosition(constrainedPos);

    // Add physics assistance if enabled
    if (EnablePhysics()) {
      float responsiveness = getResponsivenessForMass() * DragSensitivity();
      float forceScale = 2.0f;
      float[] assistForce = multiplyVector(movement, responsiveness * forceScale);

      // Apply horizontal force only
      applyForce(assistForce[0], 0, assistForce[2]);
    }
  }


  @SimpleFunction(description = "End drag with release velocity for momentum")
  public void endDrag(String pos) {
    PointF velocity = parsePointF(pos);
    //CSB todo
    endDrag(velocity, null);

  }

  @Override
  public void endDrag(PointF fingerVelocity, CameraVectors cameraVectors) {
    if (!isBeingDragged) return;

    Log.i("SphereNode", "Ending optimized drag for " + name);
    isBeingDragged = false;

    if (EnablePhysics()) {
      applyReleaseVelocity(fingerVelocity, cameraVectors);
    }

    // Restore physics settings
    restorePhysicsAfterDrag();

    // Restore visual appearance
    restoreOriginalAppearance();
  }

  private void applyRealisticRolling(float[] movement) {
    float[] horizontalMovement = {movement[0], 0, movement[2]};
    float distance = vectorLength(horizontalMovement);
    Log.d("SphereNode", "Going to roll: " + arrayToString(rotation));
    if (distance <= 0.0001f) return;

    // Physics-accurate rolling: distance = radius × angle
    float ballRadius = RadiusInCentimeters() * Scale();
    float rollAngle = distance / ballRadius;

    // Rotation axis perpendicular to movement
    float[] direction = normalizeVector(horizontalMovement);
    float[] rollAxis = {direction[2], 0, -direction[0]};

    // Apply incremental rotation
    float[] rollRotation = createQuaternionFromAxisAngle(rollAxis, rollAngle);

    Log.d("SphereNode", "Before rolling: " + arrayToString(rotation));
    rotation = multiplyQuaternions(rollRotation, rotation);
    Log.d("SphereNode", "After rolling: " + arrayToString(rotation));;



  }


  private float[] normalizeVector(float[] vector) {
    float length = vectorLength(vector);
    if (length == 0) return new float[]{0, 0, 0};
    return new float[]{vector[0] / length, vector[1] / length, vector[2] / length};
  }

  private float[] createQuaternionFromAxisAngle(float[] axis, float angle) {
    float halfAngle = angle * 0.5f;
    float sin = (float) Math.sin(halfAngle);
    float cos = (float) Math.cos(halfAngle);

    return new float[]{
        axis[0] * sin,  // x
        axis[1] * sin,  // y
        axis[2] * sin,  // z
        cos             // w
    };
  }

  private float[] multiplyQuaternions(float[] q1, float[] q2) {
    return new float[]{
        q1[3] * q2[0] + q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1], // x
        q1[3] * q2[1] - q1[0] * q2[2] + q1[1] * q2[3] + q1[2] * q2[0], // y
        q1[3] * q2[2] + q1[0] * q2[1] - q1[1] * q2[0] + q1[2] * q2[3], // z
        q1[3] * q2[3] - q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2]  // w
    };
  }
  /**
   * Apply camera-aware release velocity
   */
  @Override
  public void applyReleaseVelocity(PointF releaseVelocity, CameraVectors cameraVectors) {
    float releaseSpeed = (float) Math.sqrt(
        releaseVelocity.x * releaseVelocity.x + releaseVelocity.y * releaseVelocity.y
    );

    if (releaseSpeed <= NORMAL_ROLLING_SPEED) {
      Log.d("SphereNode", "Release speed too low: " + releaseSpeed);
      return;
    }

    //debugReleaseDirection(releaseVelocity, cameraVectors);

    // Use default camera vectors if none provided
    float[] right = (cameraVectors != null) ? cameraVectors.right : new float[]{1, 0, 0};
    float[] forward = (cameraVectors != null) ? cameraVectors.forward : new float[]{0, 0, -1};

    float baseScale = 0.002f;

    // CONSISTENT mapping: screen X → camera right, screen Y → camera forward
    // Screen Y is negative because screen coordinates have Y=0 at top
    float screenX = releaseVelocity.x * baseScale;
    float screenY = -releaseVelocity.y * baseScale; // Flip screen Y
    // let worldVelocity = (right * screenX) + (forward * screenY)
    // Calculate world velocity from camera-relative movement
    float[] worldVelocity = {
        (right[0] * screenX) + (forward[0] * screenY),
        0, // Keep Y velocity as is (no vertical component from horizontal drag)
        (right[2] * screenX) + (forward[2] * screenY)
    };

    Log.d("SphereNode", "Screen: (" + releaseVelocity.x + ", " + releaseVelocity.y +
        ") → World: (" + worldVelocity[0] + ", " + worldVelocity[1] + ", " + worldVelocity[2] + ")");
    Log.d("SphereNode", "Camera right: (" + right[0] + ", " + right[1] + ", " + right[2] +
        "), forward: (" + forward[0] + ", " + forward[1] + ", " + forward[2] + ")");

    // Apply the world velocity as impulse, preserving any existing Y velocity
    if (enablePhysics) {
      // Get current Y velocity to preserve it
      float currentYVelocity = 0;


      // Apply behavior-modified impulse
      float behaviorMultiplier = getBehaviorMomentumMultiplier();

      applyForce(
          UnitHelper.metersToCentimeters(worldVelocity[0] * behaviorMultiplier),
          UnitHelper.metersToCentimeters(currentYVelocity), // Keep existing Y velocity
          UnitHelper.metersToCentimeters(worldVelocity[2] * behaviorMultiplier)
      );
    }
  }

  private void applyForce(float x, float y, float z) {
    // Apply physics force - implementation depends on physics system
    Log.i("SphereNode", "Applying force: " + arrayToString(new float[]{x, y, z}));
  }

  private float[] subtractVectors(float[] a, float[] b) {
    return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
  }

  private float[] multiplyVector(float[] vector, float scalar) {
    return new float[]{vector[0] * scalar, vector[1] * scalar, vector[2] * scalar};
  }


  private void restorePhysicsAfterDrag() {
    // Restore original physics damping after drag
    new android.os.Handler().postDelayed(() -> {
      // Reset damping values to normal
      Log.i("SphereNode", "Physics restored after drag");
    }, 100);
  }


  @Override
  protected void showDragEffect() {
    // Color based on behavior for visual feedback
    String effectColor = "yellow"; // default

    if (behaviorFlags.contains(SurfaceBehaviorFlags.HEAVY)) {
      effectColor = "blue";
    } else if (behaviorFlags.contains(SurfaceBehaviorFlags.LIGHT)) {
      effectColor = "orange";
    } else if (behaviorFlags.contains(SurfaceBehaviorFlags.BOUNCY)) {
      effectColor = "white";
    } else if (behaviorFlags.contains(SurfaceBehaviorFlags.FLOATING)) {
      effectColor = "cyan";
    }

    Log.i("SphereNode", "Showing " + effectColor + " drag effect");
    // Implementation depends on your material/rendering system
  }

  // MARK: - Helper Methods

  private float getBehaviorMomentumMultiplier() {
    float multiplier = 1.0f;

    if (behaviorFlags.contains(SurfaceBehaviorFlags.HEAVY)) multiplier *= 0.7f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.LIGHT)) multiplier *= 1.0f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.STICKY)) multiplier *= 0.2f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.SLIPPERY)) multiplier *= 1.5f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.WET)) multiplier *= 0.6f;

    return multiplier;
  }

  private float getResponsivenessForMass() {
    float baseMass = 0.2f;
    float massRatio = Mass() / baseMass;
    float responsiveness = 1.0f / (float) Math.sqrt(massRatio);

    float finalResponsiveness = responsiveness;

    if (behaviorFlags.contains(SurfaceBehaviorFlags.HEAVY)) finalResponsiveness *= 0.6f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.LIGHT)) finalResponsiveness *= 1.4f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.STICKY)) finalResponsiveness *= 0.4f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.FLOATING)) finalResponsiveness *= 1.6f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.SLIPPERY)) finalResponsiveness *= 1.2f;
    if (behaviorFlags.contains(SurfaceBehaviorFlags.WET)) finalResponsiveness *= 0.8f;

    return finalResponsiveness;
  }

  // MARK: - Preset Sphere Configurations

  // MARK: - Enhanced Physics Methods

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void EnablePhysics(boolean isDynamic) {
    super.EnablePhysics(isDynamic);

    if (isDynamic) {
      setupSpherePhysics();
      Log.i("SphereNode", "Physics enabled - ARCore will handle all ground/floor collisions");
      debugPhysicsState();
    } else {
      Log.i("SphereNode", "Physics disabled");
    }
  }

  private void setupSpherePhysics() {
    // Create sphere collision shape with current radius and scale
    float actualRadius = radius * Scale();

    // Setup physics body with calculated properties
    // Implementation depends on your physics system (ARCore/Sceneform)

    Log.i("SphereNode", "Setup sphere physics - radius: " + actualRadius + "m, mass: " + Mass());
  }

  @SimpleFunction(description = "Debug current physics state of the sphere")
  public void DebugPhysicsState() {
    debugPhysicsState();
  }

  private void debugPhysicsState() {
    float[] currentPos = getCurrentPosition();

    Log.i("SphereNode", "=== PHYSICS STATE DEBUG ===");
    Log.i("SphereNode", "Position: " + arrayToString(currentPos));
    Log.i("SphereNode", "Has physics: " + EnablePhysics());
    Log.i("SphereNode", "Mass: " + Mass());
    Log.i("SphereNode", "Ball radius: " + radius);
    Log.i("SphereNode", "Ball radius * scale: " + (radius * Scale()));
    Log.i("SphereNode", "Scale: " + Scale());
    Log.i("SphereNode", "Static Friction: " + StaticFriction());
    Log.i("SphereNode", "Dynamic Friction: " + DynamicFriction());
    Log.i("SphereNode", "Restitution: " + Restitution());
    Log.i("SphereNode", "Drag Sensitivity: " + DragSensitivity());
    Log.i("SphereNode", "Behaviors: " + getBehaviorNames());
    Log.i("SphereNode", "==========================");
  }


  // MARK: - Enhanced Move Methods

  @Override
  @SimpleFunction(description = "Move sphere by offset, maintaining physics if enabled")
  public void MoveBy(float x, float y, float z) {
    float[] position = {0, 0, 0};
    float[] rotation = {0, 0, 0, 1};

    TrackingState trackingState = null;
    if (this.Anchor() != null) {
      float[] translations = this.Anchor().getPose().getTranslation();
      position = new float[]{translations[0] + x, translations[1] + y, translations[2] + z};
      trackingState = this.Anchor().getTrackingState();
    }

    // Maintain bottom contact for physics-enabled spheres
    if (EnablePhysics() && y != 0) {
      float ballRadius = radius * Scale();
      position[1] = Math.max(position[1], ballRadius); // Don't go below ground
    }

    Pose newPose = new Pose(position, rotation);
    if (this.trackable != null) {
      Anchor(this.trackable.createAnchor(newPose));
      Log.i("SphereNode", "Moved anchor BY " + newPose + " with physics correction");
    } else {
      if (trackingState == TrackingState.TRACKING) {
        if (session != null) {
          Log.i("SphereNode", "Moved anchor BY with SESSION");
          Anchor(session.createAnchor(newPose));
        }
      }
    }
  }

  @Override
  @SimpleFunction(description = "Move sphere to absolute position")
  public void MoveTo(float x, float y, float z) {

    float xMeters = x / 100.0f;
    float yMeters = y / 100.0f;
    float zMeters = z / 100.0f;

    float[] position = {xMeters, yMeters, zMeters};
    float[] rotation = {0, 0, 0, 1};

    TrackingState trackingState = null;
    if (this.Anchor() != null) {
      float[] translations = this.Anchor().getPose().getTranslation();
      position = new float[]{translations[0] + xMeters, translations[1] + yMeters, translations[2] + zMeters};
      trackingState = this.Anchor().getTrackingState();
    }

    // Maintain physics constraints
    if (EnablePhysics()) {
      //float ballRadius = radius * Scale();
      //position[1] = Math.max(position[1], ballRadius); // Don't go below ground
    }

    Pose newPose = new Pose(position, rotation);
    if (this.trackable != null) {
      Log.i("SphereNode", "Moving anchor to pose: " + newPose + " with physics correction");
      Anchor(this.trackable.createAnchor(newPose));
    } else {
      if (trackingState == TrackingState.TRACKING) {
        if (session != null) {
          Log.i("SphereNode", "Moved anchor to with SESSION " + xMeters + " " + yMeters + " " + zMeters);
          Anchor(session.createAnchor(newPose));
        }
      }
    }
  }

  @Override
  @SimpleFunction(description = "Move sphere to detected plane with physics considerations")
  public void MoveToDetectedPlane(ARDetectedPlane targetPlane, Object p) {
    this.trackable = (Trackable) targetPlane.DetectedPlane();
    if (this.anchor != null) {
      this.anchor.detach();
    }

    Pose targetPose = (Pose) p;

    // Adjust Y position for sphere physics
    if (EnablePhysics()) {
      float ballRadius = radius * Scale();
      float[] translation = targetPose.getTranslation();
      translation[1] += ballRadius; // Place sphere on surface, not embedded
      targetPose = new Pose(translation, targetPose.getRotationQuaternion());
    }

    Anchor(this.trackable.createAnchor(targetPose));
    Log.i("SphereNode", "Moved to detected plane with physics adjustment");
  }

  // MARK: - Existing Functions (keep unchanged)

  @SimpleFunction(description = "Sets the color of all nodes with the given opacity.")
  public void SetFillColorForAllNodes(int color, int opacity) {
    // Implementation depends on your rendering system
  }

  @SimpleFunction(description = "Sets the color of a node named \"name\" with the given opacity.")
  public void SetFillColorForNode(String name, int color, int opacity, boolean shouldColorChildNodes) {
    // Implementation depends on your rendering system
  }

  @SimpleFunction(description = "Sets the texture of a node named \"name\" with the given opacity.")
  public void SetTextureForNode(String name, String texture, int opacity, boolean shouldTexturizeChildNodes) {
    this.texture = texture;
    // Implementation depends on your rendering system
  }

  @SimpleFunction(description = "Sets the texture of all nodes with the given opacity.")
  public void SetTextureForAllNodes(String texture, int opacity) {
    // Implementation depends on your rendering system
  }

  @SimpleFunction(description = "Sets whether the shadow is shown for a node named \"name\".")
  public void SetShowShadowForNode(String name, boolean showShadow, boolean shouldShadowChildNodes) {
    // Implementation depends on your rendering system
  }

  @SimpleFunction(description = "Sets if all nodes show a shadow.")
  public void SetShowShadowForAllNodes(boolean showShadow) {
    // Implementation depends on your rendering system
  }

  @SimpleFunction(description = "Plays all animations in the model, if it has animations.")
  public void PlayAnimationsForAllNodes() {
    // Implementation depends on your animation system
  }

  @SimpleFunction(description = "Plays animations attached to a node named \"name\".")
  public void PlayAnimationsForNode(String name, boolean shouldPlayChildNodes) {
    // Implementation depends on your animation system
  }

  @SimpleFunction(description = "Stops all animations in the model, if it has animations.")
  public void StopAnimationsForAllNodes() {
    // Implementation depends on your animation system
  }

  @SimpleFunction(description = "Stops animations attached to a node named \"name\".")
  public void StopAnimationsForNode(String name, boolean shouldStopChildNodes) {
    // Implementation depends on your animation system
  }

  @SimpleFunction(description = "Renames a node named \"oldName\" to \"newName\".")
  public void RenameNode(String oldName, String newName) {
    if (oldName.equals(this.name)) {
      this.name = newName;
    }
  }

  @SimpleEvent(description = "This event is triggered when the user tries to access a node named \"name\", but a node with that \"name\" does not exist.")
  public void NodeNotFound(String name) {
    // Event handling
  }

  // MARK: - Hidden/Override Properties (keep existing)

  @Override
  @SimpleProperty(userVisible = false)
  public boolean ShowShadow() {
    return false;
  }

  @Override
  @SimpleProperty(userVisible = false)
  public void ShowShadow(boolean showShadow) {
    // Implementation depends on your rendering system
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Get the color of the sphere")
  public int FillColor() {
    return 0;
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Set the color of the sphere")
  public void FillColor(int color) {
    // Implementation depends on your material system
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Get the opacity of the sphere")
  public int FillColorOpacity() {
    return 100;
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Set the opacity of the sphere")
  public void FillColorOpacity(int fillColorOpacity) {
    // Implementation depends on your material system
  }

  @Override
  @SimpleProperty(userVisible = false)
  public int TextureOpacity() {
    return 100;
  }

  @Override
  @SimpleProperty(userVisible = false)
  public void TextureOpacity(int textureOpacity) {
    // Implementation depends on your material system
  }
}