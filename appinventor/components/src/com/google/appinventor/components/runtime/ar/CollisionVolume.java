package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.ar.ARNodeBase;

public abstract class CollisionVolume {

  public abstract boolean intersects(CollisionVolume other,
                                     float[] posA, float[] posB);

  public abstract void separate(ARNodeBase a, ARNodeBase b);

  // -------------------------------------------------------------------------
  // Shared math utilities
  // -------------------------------------------------------------------------

  protected float distance(float[] posA, float[] posB) {
    float dx = posB[0] - posA[0];
    float dy = posB[1] - posA[1];
    float dz = posB[2] - posA[2];
    return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  protected float[] normal(float[] posA, float[] posB) {
    float d = distance(posA, posB);
    if (d < 0.0001f) return new float[]{1, 0, 0};
    return new float[]{
        (posB[0] - posA[0]) / d,
        (posB[1] - posA[1]) / d,
        (posB[2] - posA[2]) / d
    };
  }

  protected void applyImpulse(ARNodeBase a, ARNodeBase b, float[] n) {
    float restitution = (a.Restitution() + b.Restitution()) * 0.5f;
    float relVelN =
        (b.currentVelocity[0] - a.currentVelocity[0]) * n[0] +
            (b.currentVelocity[1] - a.currentVelocity[1]) * n[1] +
            (b.currentVelocity[2] - a.currentVelocity[2]) * n[2];

    if (relVelN > 0) return; // already separating

    float impulse = -(1 + restitution) * relVelN
        / (1f / a.Mass() + 1f / b.Mass());

    a.currentVelocity[0] -= (impulse / a.Mass()) * n[0];
    a.currentVelocity[1] -= (impulse / a.Mass()) * n[1];
    a.currentVelocity[2] -= (impulse / a.Mass()) * n[2];

    b.currentVelocity[0] += (impulse / b.Mass()) * n[0];
    b.currentVelocity[1] += (impulse / b.Mass()) * n[1];
    b.currentVelocity[2] += (impulse / b.Mass()) * n[2];
  }

  public float getEffectiveRadius() {
    if (this instanceof SphereVolume) {
      return ((SphereVolume) this).radius;
    } else if (this instanceof AABBVolume) {
      float[] h = ((AABBVolume) this).halfExtents;
      return Math.max(h[0], Math.max(h[1], h[2]));
    } else if (this instanceof CapsuleVolume) {
      return ((CapsuleVolume) this).radius + ((CapsuleVolume) this).halfHeight;
    }
    return 0.05f;
  }
}