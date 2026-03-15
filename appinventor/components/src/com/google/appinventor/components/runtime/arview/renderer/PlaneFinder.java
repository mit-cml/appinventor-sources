package com.google.appinventor.components.runtime.arview.renderer;

@FunctionalInterface
public interface PlaneFinder {
  com.google.ar.core.Plane findOccludingPlane(float[] sphereWorldPos, float[] cameraWorldPos);
}

