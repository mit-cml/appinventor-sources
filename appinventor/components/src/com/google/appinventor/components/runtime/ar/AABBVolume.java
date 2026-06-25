package com.google.appinventor.components.runtime.ar;

public class AABBVolume extends CollisionVolume {
  public float[] halfExtents; // {x, y, z}

  public AABBVolume(float x, float y, float z) {
    halfExtents = new float[]{x, y, z};
  }

  @Override
  public boolean intersects(CollisionVolume other,
                            float[] posA, float[] posB) {
    if (other instanceof AABBVolume) {
      float[] hB = ((AABBVolume) other).halfExtents;
      return Math.abs(posA[0]-posB[0]) < halfExtents[0]+hB[0]
          && Math.abs(posA[1]-posB[1]) < halfExtents[1]+hB[1]
          && Math.abs(posA[2]-posB[2]) < halfExtents[2]+hB[2];
    }
    if (other instanceof SphereVolume) {
      return intersectsSphere(posA, posB,
          ((SphereVolume) other).radius);
    }
    if (other instanceof CapsuleVolume) {
      // Approximate capsule as sphere using radius
      return intersectsSphere(posA, posB,
          ((CapsuleVolume) other).radius);
    }
    return false;
  }

  // Sphere vs AABB — finds closest point on box to sphere center
  public boolean intersectsSphere(float[] boxPos,
                                  float[] spherePos, float r) {
    float dx = Math.max(0, Math.abs(spherePos[0]-boxPos[0]) - halfExtents[0]);
    float dy = Math.max(0, Math.abs(spherePos[1]-boxPos[1]) - halfExtents[1]);
    float dz = Math.max(0, Math.abs(spherePos[2]-boxPos[2]) - halfExtents[2]);
    return (dx*dx + dy*dy + dz*dz) < (r*r);
  }

  @Override
  public void separate(ARNodeBase a, ARNodeBase b) {
    float[] posA = a.getCurrentPosition();
    float[] posB = b.getCurrentPosition();
    float[] hA = halfExtents;
    float[] hB = (b.getCollisionVolume() instanceof AABBVolume)
        ? ((AABBVolume) b.getCollisionVolume()).halfExtents
        : new float[]{0.05f, 0.05f, 0.05f};

    // Find minimum overlap axis
    float[] overlap = {
        hA[0]+hB[0] - Math.abs(posA[0]-posB[0]),
        hA[1]+hB[1] - Math.abs(posA[1]-posB[1]),
        hA[2]+hB[2] - Math.abs(posA[2]-posB[2])
    };
    int axis = 0;
    if (overlap[1] < overlap[axis]) axis = 1;
    if (overlap[2] < overlap[axis]) axis = 2;

    float[] n = {0, 0, 0};
    n[axis] = posA[axis] < posB[axis] ? -1f : 1f;
    float push = (overlap[axis] * 0.5f) + 0.001f;

    posA[axis] += n[axis] * push;
    posB[axis] -= n[axis] * push;

    a.setCurrentPosition(posA);
    b.setCurrentPosition(posB);
    applyImpulse(a, b, n);
  }
}