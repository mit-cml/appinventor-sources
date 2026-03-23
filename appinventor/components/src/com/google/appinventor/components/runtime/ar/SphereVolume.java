package com.google.appinventor.components.runtime.ar;


import android.util.Log;

public class SphereVolume extends CollisionVolume {
  public float radius;

  public SphereVolume(float radius) {
    this.radius = radius;
  }

  @Override
  public boolean intersects(CollisionVolume other, float[] posA, float[] posB) {
    float rA = radius;
    float rB = other.getEffectiveRadius();
    float minDist = rA + rB;
    float dist = distance(posA, posB);
    if (dist < minDist) {
      Log.d("SphereVolume", "CONTACT: dist=" + dist + " minDist=" + minDist);
      return true;
    }
    return false;
  }

  @Override
  public void separate(ARNodeBase a, ARNodeBase b) {
    float[] posA = a.getCurrentPosition();
    float[] posB = b.getCurrentPosition();
    float minDist = radius + ((SphereVolume) b.getCollisionVolume()).radius;
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
