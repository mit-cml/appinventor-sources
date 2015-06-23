// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import junit.framework.TestCase;

/**
 */
public class OrientationSensorTest extends TestCase {
  private static final float DELTA = .0001f;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testComputeAngle() throws Exception {
    // due right (0 degrees)
    assertEquals(+0f, OrientationSensor.computeAngle(0f, -90f), DELTA);

    // straight up (90 degrees)
    assertEquals(+90f, OrientationSensor.computeAngle(+90f, 0), DELTA);

    // due left (180 degrees)
    assertEquals(+180f, OrientationSensor.computeAngle(0f, +90f), DELTA);

    // straight down (270 degrees)
    assertEquals(-90f, OrientationSensor.computeAngle(-90f, 0), DELTA);

    // Cases where pitch and roll have equal absolute value
    assertEquals(+45f, OrientationSensor.computeAngle(+5f, -5f), DELTA);
    assertEquals(+135f, OrientationSensor.computeAngle(+10f, +10f), DELTA);
    assertEquals(-135f, OrientationSensor.computeAngle(-15f, +15f), DELTA);
    assertEquals(-45f, OrientationSensor.computeAngle(-20f, -20f), DELTA);
  }
}
