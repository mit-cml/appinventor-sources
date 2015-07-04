// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * Functionality related to
 * {@link com.google.appinventor.components.runtime.OrientationSensor},
 * placed in this static utility class for easier testing.
 *
 */
public class OrientationSensorUtil {

  private OrientationSensorUtil() {
  }

  /**
   * Computes the modulo relationship.  This is not the same as
   * Java's remainder (%) operation, which always returns a
   * value with the same sign as the dividend or 0.
   *
   * @param dividend number to divide
   * @param quotient number to divide by
   * @return the number r with the smallest absolute value such
   *         that sign(r) == sign(quotient) and there exists an
   *         integer k such that k * quotient + r = dividend
   */
  //VisibleForTesting
  static float mod(float dividend, float quotient) {
    float result = dividend % quotient;
    if (result == 0 || Math.signum(dividend) == Math.signum(quotient)) {
      return result;
    } else {
      return result + quotient;
    }
  }

  /**
   * Normalizes azimuth to be in the range [0, 360).
   *
   * @param azimuth an angle in degrees, likely to be in (-360, +360)
   * @return an equivalent angle in the range [0, 360)
   */
  public static float normalizeAzimuth(float azimuth) {
    return mod(azimuth, 360f);
  }

  /**
   * Normalizes pitch to be in the range [-180, +180).
   *
   * @param pitch an angle in degrees, likely to be in (-360, +360)
   * @return an equivalent angle in the range [-180, +180)
   */
  public static float normalizePitch(float pitch) {
    return mod(pitch + 180f, 360f) - 180f;
  }

  /**
   * Normalizes roll to be in the range [-90, +90] degrees.
   * The App Inventor definition of Roll in the documentation is:
   * <blockquote>
   * 0 degrees when the device is level, increasing to 90 degrees as the
   * device is tilted up onto its left side, and decreasing to -90
   * degrees when the device is tilted up onto its right side.
   * </blockquote>
   * After rotating the phone more than 90 degrees, Roll decreased.
   * For compatibility, we are guaranteeing the same behavior.
   *
   * @param roll an angle likely to be in the range [-180, +180]
   *
   * @return the equivalent angle in the range [-90, +90], where angles
   *         with an absolute value greater than 90 are reflected over
   *         the x-axis; the value is not defined for inputs outside of
   *         [-180, +180]
   */
  public static float normalizeRoll(float roll) {
    // Guarantee that roll is in [-180, +180].  It could legitimately
    // be slightly outside due to floating point rounding issues.
    roll = Math.min(roll, 180f);
    roll = Math.max(roll, -180f);

    // If roll is in [-90, +90], we're done.
    if (roll >= -90 && roll <= 90) {
      return roll;
    }

    // Otherwise, reflect over x-axis to put in 1st or 4th quadrant.
    roll = 180 - roll;

    // Put in range [-90, +90].
    if (roll >= 270) {
      roll -= 360;
    }
    return roll;
  }
}
