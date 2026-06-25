package com.google.appinventor.components.runtime.ar;

public class CapsuleVolume extends CollisionVolume {
  public float radius;
  public float halfHeight; // half distance between sphere centers

  public CapsuleVolume(float radius, float halfHeight) {
    this.radius = radius;
    this.halfHeight = halfHeight;
  }

  @Override
  public boolean intersects(CollisionVolume other,
                            float[] posA, float[] posB) {
    if (other instanceof SphereVolume) {
      return intersectsSphere(posA, posB,
          ((SphereVolume) other).radius);
    }
    if (other instanceof CapsuleVolume) {
      // Approximate as sphere-sphere using radius + halfHeight
      float rA = radius + halfHeight;
      float rB = ((CapsuleVolume) other).radius
          + ((CapsuleVolume) other).halfHeight;
      return distance(posA, posB) < (rA + rB);
    }
    if (other instanceof AABBVolume) {
      return ((AABBVolume) other).intersectsSphere(
          posB, posA, radius + halfHeight);
    }
    return false;
  }

  public boolean intersectsSphere(float[] capsulePos,
                                  float[] spherePos, float sphereR) {
    // Closest point on capsule axis to sphere center
    float closestY = Math.max(capsulePos[1] - halfHeight,
        Math.min(capsulePos[1] + halfHeight, spherePos[1]));
    float[] closest = {capsulePos[0], closestY, capsulePos[2]};
    return distance(closest, spherePos) < (radius + sphereR);
  }

  @Override
  public void separate(ARNodeBase a, ARNodeBase b) {
    // Treat as sphere separation using effective radius
    float[] posA = a.getCurrentPosition();
    float[] posB = b.getCurrentPosition();
    float rA = radius + halfHeight;
    float rB = (b.getCollisionVolume() instanceof CapsuleVolume)
        ? ((CapsuleVolume) b.getCollisionVolume()).radius
        + ((CapsuleVolume) b.getCollisionVolume()).halfHeight
        : ((SphereVolume) b.getCollisionVolume()).radius;

    float minDist = rA + rB;
    float dist = distance(posA, posB);
    float overlap = (minDist - dist) * 0.5f + 0.001f;
    float[] n = normal(posA, posB);

    a.setCurrentPosition(new float[]{
        posA[0] - n[0]*overlap,
        posA[1] - n[1]*overlap,
        posA[2] - n[2]*overlap
    });
    b.setCurrentPosition(new float[]{
        posB[0] + n[0]*overlap,
        posB[1] + n[1]*overlap,
        posB[2] + n[2]*overlap
    });
    applyImpulse(a, b, n);
  }
}
