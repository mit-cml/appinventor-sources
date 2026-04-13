package com.google.appinventor.components.runtime.util;


public class CameraVectors {
  public float[] right = new float[3];    // Camera's right vector
  public float[] forward = new float[3];  // Camera's forward vector
  public float[] up = new float[3];       // Camera's up vector

  public CameraVectors(float[] cameraViewMatrix) {
    // Extract camera vectors from view matrix
    // Right vector (first column)
    right[0] = cameraViewMatrix[0];
    right[1] = cameraViewMatrix[4];
    right[2] = cameraViewMatrix[8];

    // Up vector (second column)
    up[0] = cameraViewMatrix[1];
    up[1] = cameraViewMatrix[5];
    up[2] = cameraViewMatrix[9];

    // Forward vector (negative third column)
    forward[0] = -cameraViewMatrix[2];
    forward[1] = -cameraViewMatrix[6];
    forward[2] = -cameraViewMatrix[10];
  }
}