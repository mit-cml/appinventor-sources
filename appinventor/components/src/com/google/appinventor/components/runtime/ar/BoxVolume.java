// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import android.util.Log;

// NOTE: An AABBVolume already exists in CollisionVolume's instanceof chain.
// If AABBVolume has its own intersects/separate logic, consider whether
// BoxVolume should extend AABBVolume or simply be replaced by it.
// BoxVolume is kept separate here in case it needs TextNode-specific behaviour.
public class BoxVolume extends CollisionVolume {

  private static final String LOG_TAG = "BoxVolume";

  // Half-extents in meters along each axis
  public float halfWidth;   // X
  public float halfHeight;  // Y
  public float halfDepth;   // Z

  /**
   * @param width  Full width  in meters (X axis)
   * @param height Full height in meters (Y axis)
   * @param depth  Full depth  in meters (Z axis)
   */
  public BoxVolume(float width, float height, float depth) {
    this.halfWidth  = Math.abs(width)  / 2f;
    this.halfHeight = Math.abs(height) / 2f;
    this.halfDepth  = Math.abs(depth)  / 2f;
  }

  // -------------------------------------------------------------------------
  // CollisionVolume contract
  // -------------------------------------------------------------------------

  /**
   * MUST override: the base class getEffectiveRadius() is an instanceof chain
   * that does not include BoxVolume — without this, box vs. sphere falls
   * through to the 0.05f fallback and silently produces wrong collisions.
   *
   * Returns the circumscribed sphere radius (the smallest sphere containing
   * this box), which is the conservative correct answer for a single scalar
   * approximation of this volume.
   */
  @Override
  public float getEffectiveRadius() {
    return (float) Math.sqrt(
        halfWidth  * halfWidth  +
            halfHeight * halfHeight +
            halfDepth  * halfDepth);
  }

  // -------------------------------------------------------------------------
  // Intersection
  // -------------------------------------------------------------------------

  @Override
  public boolean intersects(CollisionVolume other, float[] posA, float[] posB) {
    if (other instanceof BoxVolume) {
      return intersectsBox((BoxVolume) other, posA, posB);
    } else {
      // Treat the other volume as a sphere (covers SphereVolume and unknowns)
      return intersectsSphere(other.getEffectiveRadius(), posA, posB);
    }
  }

  /**
   * AABB vs AABB — overlapping on all three axes means collision.
   */
  private boolean intersectsBox(BoxVolume other, float[] posA, float[] posB) {
    float dx = Math.abs(posA[0] - posB[0]);
    float dy = Math.abs(posA[1] - posB[1]);
    float dz = Math.abs(posA[2] - posB[2]);

    boolean overlap =
        dx < (halfWidth  + other.halfWidth)  &&
            dy < (halfHeight + other.halfHeight) &&
            dz < (halfDepth  + other.halfDepth);

    if (overlap) {
      Log.d(LOG_TAG, "CONTACT box-box: dx=" + dx + " dy=" + dy + " dz=" + dz);
    }
    return overlap;
  }

  /**
   * AABB vs Sphere — clamp the sphere centre to the box, then check whether
   * the clamped point is within the sphere radius. Geometrically exact.
   */
  private boolean intersectsSphere(float sphereRadius, float[] posBox, float[] posSphere) {
    float dx = posSphere[0] - posBox[0];
    float dy = posSphere[1] - posBox[1];
    float dz = posSphere[2] - posBox[2];

    // Closest point on the box to the sphere centre
    float cx = clamp(dx, -halfWidth,  halfWidth);
    float cy = clamp(dy, -halfHeight, halfHeight);
    float cz = clamp(dz, -halfDepth,  halfDepth);

    float dist2 = (dx - cx) * (dx - cx)
        + (dy - cy) * (dy - cy)
        + (dz - cz) * (dz - cz);

    boolean overlap = dist2 < (sphereRadius * sphereRadius);
    if (overlap) {
      Log.d(LOG_TAG, "CONTACT box-sphere: dist=" + (float) Math.sqrt(dist2)
          + " sphereRadius=" + sphereRadius);
    }
    return overlap;
  }

  // -------------------------------------------------------------------------
  // Separation
  // -------------------------------------------------------------------------

  @Override
  public void separate(ARNodeBase a, ARNodeBase b) {
    CollisionVolume volB = b.getCollisionVolume();
    if (volB instanceof BoxVolume) {
      separateBoxBox(a, b, (BoxVolume) volB);
    } else {
      separateBoxSphere(a, b, volB.getEffectiveRadius());
    }
  }

  /**
   * Box-box: separate along the axis of minimum penetration.
   */
  private void separateBoxBox(ARNodeBase a, ARNodeBase b, BoxVolume other) {
    float[] posA = a.getCurrentPosition();
    float[] posB = b.getCurrentPosition();

    float dx = posB[0] - posA[0];
    float dy = posB[1] - posA[1];
    float dz = posB[2] - posA[2];

    float overlapX = (halfWidth  + other.halfWidth)  - Math.abs(dx);
    float overlapY = (halfHeight + other.halfHeight) - Math.abs(dy);
    float overlapZ = (halfDepth  + other.halfDepth)  - Math.abs(dz);

    float[] n;
    float overlap;

    if (overlapX <= overlapY && overlapX <= overlapZ) {
      n       = new float[]{ dx < 0 ? -1f : 1f, 0f, 0f };
      overlap = overlapX;
    } else if (overlapY <= overlapX && overlapY <= overlapZ) {
      n       = new float[]{ 0f, dy < 0 ? -1f : 1f, 0f };
      overlap = overlapY;
    } else {
      n       = new float[]{ 0f, 0f, dz < 0 ? -1f : 1f };
      overlap = overlapZ;
    }

    float push = overlap * 0.5f + 0.001f;   // epsilon matches SphereVolume

    a.setCurrentPosition(new float[]{
        posA[0] - n[0] * push,
        posA[1] - n[1] * push,
        posA[2] - n[2] * push
    });
    b.setCurrentPosition(new float[]{
        posB[0] + n[0] * push,
        posB[1] + n[1] * push,
        posB[2] + n[2] * push
    });

    applyImpulse(a, b, n);  // inherited from CollisionVolume
  }

  /**
   * Box-sphere: push apart along the contact normal.
   */
  private void separateBoxSphere(ARNodeBase a, ARNodeBase b, float sphereRadius) {
    float[] posBox    = a.getCurrentPosition();
    float[] posSphere = b.getCurrentPosition();

    float dx = posSphere[0] - posBox[0];
    float dy = posSphere[1] - posBox[1];
    float dz = posSphere[2] - posBox[2];

    // Closest point on box to sphere centre
    float cx = clamp(dx, -halfWidth,  halfWidth);
    float cy = clamp(dy, -halfHeight, halfHeight);
    float cz = clamp(dz, -halfDepth,  halfDepth);

    // Contact normal: from closest box point toward sphere centre
    float nx   = dx - cx;
    float ny   = dy - cy;
    float nz   = dz - cz;
    float dist = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

    if (dist < 0.0001f) {
      // Sphere centre is fully inside the box — push straight up as fallback
      nx = 0; ny = 1; nz = 0; dist = 1f;
    }

    float[] n   = new float[]{ nx / dist, ny / dist, nz / dist };
    float overlap = (sphereRadius - dist) * 0.5f + 0.001f;

    a.setCurrentPosition(new float[]{
        posBox[0]    - n[0] * overlap,
        posBox[1]    - n[1] * overlap,
        posBox[2]    - n[2] * overlap
    });
    b.setCurrentPosition(new float[]{
        posSphere[0] + n[0] * overlap,
        posSphere[1] + n[1] * overlap,
        posSphere[2] + n[2] * overlap
    });

    applyImpulse(a, b, n);  // inherited from CollisionVolume
  }

  // -------------------------------------------------------------------------
  // Local utility
  // -------------------------------------------------------------------------

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }
}