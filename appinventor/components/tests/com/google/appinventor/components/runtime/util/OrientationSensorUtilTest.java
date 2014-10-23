// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import junit.framework.TestCase;

/**
 * Tests OrientationSensorUtil.
 *
 */
public class OrientationSensorUtilTest extends TestCase {
  private static final float DELTA = .00001f;  // floating point delta

  private static final float[] ANGLES = {
    -720f, -360f, -350f,  -100f, 181f, 0f, 1f,  30f,  180f, 360f, 400f, 710f };

  // Azimuth should be in the range [0, 360).
  private static final float[] AZIMUTHS = {
    0f,       0f,   10f,   260f, 181f, 0f, 1f,  30f,  180f,   0f,  40f, 350f };

  // Pitch should be in the range [-180, +180).
  private static final float[] PITCHES = {
    0f,       0f,   10f, -100f, -179f, 0f, 1f,  30f, -180f,   0f,  40f, -10f };

  // Roll should be in the range [-90, +90].
  // Output is only defined for inputs in the range [-180, +180].
  // Mark undefined outputs with Float.NaN.
  private static final float NAN = Float.NaN;
  private static final float[] rolls = {
    NAN,     NAN,   NAN,  -80f, NAN, 0f, 1f,  30f,    0f,   NAN,  NAN,  NAN };

  public void testNormalizeAzimuth() throws Exception {
    for (int i = 0; i < ANGLES.length; i++) {
      assertTrue(AZIMUTHS[i] >= 0 && AZIMUTHS[i] < 360);  // sanity check
      assertEquals(AZIMUTHS[i],
                   OrientationSensorUtil.normalizeAzimuth(ANGLES[i]),
                   DELTA);
    }
  }

  public void testNormalizePitch() {
    for (int i = 0; i < ANGLES.length; i++) {
      assertTrue(PITCHES[i] >= -180 && PITCHES[i] < 180);  // sanity check
      assertEquals(PITCHES[i],
                   OrientationSensorUtil.normalizePitch(ANGLES[i]),
                   DELTA);
    }
  }

  public void testNormalizeRoll() {
    for (int i = 0; i < ANGLES.length; i++) {
      // normalizeRoll is only guaranteed for inputs in [-180, +180].
      if (ANGLES[i] >= -180 && ANGLES[i] <= 180) {
        assertTrue(rolls[i] >= -90 && rolls[i] <= 90);  // sanity check
        assertEquals(rolls[i],
                     OrientationSensorUtil.normalizeRoll(ANGLES[i]),
                     DELTA);
      }
    }
  }

  public void testMod() {
    assertEquals(0f, OrientationSensorUtil.mod(720f, 360f), DELTA);
    assertEquals(0f, OrientationSensorUtil.mod(-720f, 360f), DELTA);

    // Cases on http://mindprod.com/jgloss/modulus.html
    assertEquals(3f, OrientationSensorUtil.mod(7f, 4f), DELTA);
    assertEquals(1f, OrientationSensorUtil.mod(-7f, 4f), DELTA);
    assertEquals(-1f, OrientationSensorUtil.mod(7f, -4f), DELTA);
    assertEquals(-3f, OrientationSensorUtil.mod(-7f, -4f), DELTA);
  }
}
