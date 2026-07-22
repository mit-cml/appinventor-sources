// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

/**
 * Optional capability for AR nodes that want "rolling ball" style dragging:
 * rotation derived from horizontal drag movement, a mass-scaled release
 * fling, and spin picked up from collisions.
 *
 * A node opts in by implementing this interface AND reporting
 * {@link #isRollingDragEnabled()} == true. When that flag is false — or the
 * node doesn't implement this interface at all — dragging and physics fall
 * back entirely to the plain {@link ARNodeBase} behavior (straight pickup,
 * no spin, no fling). The flag is checked at call time rather than baked in,
 * so a single node type (e.g. SphereNode) can toggle rolling on and off
 * dynamically via a behavior setting.
 *
 * All default methods here are expressed purely in terms of the accessor
 * methods below, so they have no dependency on any particular node's
 * internal fields — any future node type can adopt rolling by implementing
 * six small accessors.
 */
public interface RollingDraggable {

  /** Effective (scaled) radius, in meters, used for roll-angle math and ground clearance. */
  float getRollingRadius();

  /**
   * Whether rolling-specific behavior should run right now. Conformance to
   * this interface alone is not enough — this lets a node fall back to
   * default drag/physics dynamically (e.g. a "sticky" or "floating"
   * configuration that shouldn't roll or spin).
   */
  boolean isRollingDragEnabled();

  /** Current orientation quaternion [x, y, z, w]. */
  float[] getCurrentRotationForRolling();

  /** Apply a new orientation quaternion [x, y, z, w]. */
  void setCurrentRotationForRolling(float[] quaternion);

  /** Current horizontal drag velocity [x, y, z] in m/s. */
  float[] getDragVelocityForRolling();

  /** Current mass, used to scale release-fling speed. */
  float getMassForRolling();

  // ---- shared math; only ever touches the accessors above ----

  /**
   * Physics-accurate rolling: distance = radius × angle, with the spin axis
   * perpendicular to the horizontal movement direction. No-ops when rolling
   * is disabled.
   */
  default void applyRealisticRolling(float[] movement) {
    if (!isRollingDragEnabled()) return;

    float[] horizontal = {movement[0], 0, movement[2]};
    float distance = (float) Math.sqrt(
        horizontal[0] * horizontal[0] + horizontal[2] * horizontal[2]);
    float radius = getRollingRadius();
    if (distance <= 0.0001f || radius <= 0f) return;

    float rollAngle = distance / radius;
    float[] direction = {horizontal[0] / distance, 0, horizontal[2] / distance};
    float[] rollAxis = {direction[2], 0, -direction[0]};

    float[] rollRotation = quaternionFromAxisAngle(rollAxis, rollAngle);
    float[] current = getCurrentRotationForRolling();
    setCurrentRotationForRolling(multiplyQuaternions(rollRotation, current));
  }

  /**
   * Mass-scaled release velocity for a rolling fling, clamped to
   * {@code maxReleaseSpeed}. Returns {0,0,0} — i.e. no fling — when rolling
   * is disabled or the drag speed is below the fling threshold.
   */
  default float[] computeRollingReleaseVelocity(float baseMass, float maxReleaseSpeed) {
    if (!isRollingDragEnabled()) return new float[]{0, 0, 0};

    float[] dragVelocity = getDragVelocityForRolling();
    float speed = (float) Math.sqrt(
        dragVelocity[0] * dragVelocity[0] + dragVelocity[2] * dragVelocity[2]);
    if (speed < 0.1f) return new float[]{0, 0, 0};

    float mass = getMassForRolling();
    // heavier objects are harder to fling — scale by inverse mass
    float massScale = mass > 0f ? baseMass / mass : 1f;

    return new float[]{
        clampRolling(dragVelocity[0] * massScale, -maxReleaseSpeed, maxReleaseSpeed),
        0,
        clampRolling(dragVelocity[2] * massScale, -maxReleaseSpeed, maxReleaseSpeed)
    };
  }

  /**
   * Rolling-derived angular velocity (spin) from a linear velocity, ⟂ to the
   * direction of motion. Returns {0,0,0} when rolling is disabled.
   */
  default float[] computeRollingAngularVelocity(float[] linearVelocity) {
    if (!isRollingDragEnabled()) return new float[]{0, 0, 0};
    float radius = getRollingRadius();
    if (radius <= 0f) return new float[]{0, 0, 0};
    return new float[]{
        linearVelocity[2] / radius,
        0,
        -linearVelocity[0] / radius
    };
  }

  static float clampRolling(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  static float[] quaternionFromAxisAngle(float[] axis, float angle) {
    float half = angle * 0.5f;
    float sin = (float) Math.sin(half);
    float cos = (float) Math.cos(half);
    return new float[]{axis[0] * sin, axis[1] * sin, axis[2] * sin, cos};
  }

  static float[] multiplyQuaternions(float[] a, float[] b) {
    return new float[]{
        a[3] * b[0] + a[0] * b[3] + a[1] * b[2] - a[2] * b[1],
        a[3] * b[1] - a[0] * b[2] + a[1] * b[3] + a[2] * b[0],
        a[3] * b[2] + a[0] * b[1] - a[1] * b[0] + a[2] * b[3],
        a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2]
    };
  }
}