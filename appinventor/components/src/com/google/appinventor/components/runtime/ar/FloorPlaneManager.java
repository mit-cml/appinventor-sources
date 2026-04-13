package com.google.appinventor.components.runtime.ar;

import android.util.Log;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;

import java.util.Collection;

public class FloorPlaneManager {
  private static final String LOG_TAG = "FloorPlaneManager";
  private static final float FLOOR_CONFIDENCE_THRESHOLD = 0.7f;

  private Plane primaryFloorPlane = null;
  private float floorY = Float.NaN;  // Use NaN instead of -1
  private boolean floorDetected = false;

  /**
   * Update floor plane from ARCore tracking planes.
   * Call this every frame from onDrawFrame.
   */
  public void updateFloorPlane(Collection<Plane> planes, float[] cameraPos) {
    if (planes.isEmpty()) return;

    // Find the best horizontal plane (largest area, closest to camera)
    Plane bestPlane = null;
    float bestScore = 0f;

    for (Plane plane : planes) {
      // Only consider horizontal planes that are being tracked
      if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) continue;
      if (plane.getTrackingState() != TrackingState.TRACKING) continue;
      if (plane.getSubsumedBy() != null) continue;  // Skip merged planes

      // Score based on area and distance from camera
      float area = getPlaneArea(plane);
      float distance = getDistanceToCamera(plane, cameraPos);
      float score = area / (1f + distance);  // Prefer large, close planes

      if (score > bestScore) {
        bestScore = score;
        bestPlane = plane;
      }
    }

    if (bestPlane != null) {
      updateFloorHeight(bestPlane);
    }
  }

  private void updateFloorHeight(Plane plane) {
    Pose center = plane.getCenterPose();
    float newFloorY = center.ty();  // Y component of plane center

    if (!floorDetected) {
      // First detection
      floorY = newFloorY;
      primaryFloorPlane = plane;
      floorDetected = true;
      Log.d(LOG_TAG, "Floor detected at Y=" + floorY);
    } else {
      // Smooth floor height updates (prevents jitter)
      floorY = floorY * 0.9f + newFloorY * 0.1f;

      // Update primary plane if this one is significantly better
      if (getPlaneArea(plane) > getPlaneArea(primaryFloorPlane) * 1.5f) {
        primaryFloorPlane = plane;
        Log.d(LOG_TAG, "Primary floor plane updated");
      }
    }
  }

  private float getPlaneArea(Plane plane) {
    if (plane == null) return 0f;
    float width = plane.getExtentX();
    float height = plane.getExtentZ();
    return width * height;
  }

  private float getDistanceToCamera(Plane plane, float[] cameraPos) {
    Pose center = plane.getCenterPose();
    float dx = center.tx() - cameraPos[0];
    float dy = center.ty() - cameraPos[1];
    float dz = center.tz() - cameraPos[2];
    return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
  }

  public boolean isFloorDetected() {
    return floorDetected;
  }

  public float getFloorY() {
    return floorY;
  }

  public Plane getFloorPlane() {
    return primaryFloorPlane;
  }

  /**
   * Check if a point is on the floor (within tolerance).
   */
  public boolean isOnFloor(float y, float tolerance) {
    if (!floorDetected) return false;
    return Math.abs(y - floorY) < tolerance;
  }

  /**
   * Snap a Y coordinate to the floor.
   */
  public float snapToFloor(float y, float objectRadius) {
    if (!floorDetected) return y;
    return floorY + objectRadius;  // Place object with bottom touching floor
  }

  public void reset() {
    primaryFloorPlane = null;
    floorY = Float.NaN;
    floorDetected = false;
    Log.d(LOG_TAG, "Floor plane reset");
  }
}