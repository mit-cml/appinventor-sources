// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CircleTest extends MapTestBase {

  private Circle circle;

  @Before
  public void setUp() {
    super.setUp();
    circle = new Circle(getMap());
  }

  @Test
  public void testDefaults() {
    assertEquals(0.0, circle.Latitude(), DEG_TOL);
    assertEquals(0.0, circle.Longitude(), DEG_TOL);
    assertEquals(0.0, circle.Radius(), M_TOL);
  }

  @Test
  public void testCircleIsCircle() {
    assertEquals("Circle", circle.Type());
  }

  @Test
  public void testLatitudeValid() {
    circle.Latitude(1.0);
    assertEquals(1.0, circle.Latitude(), DEG_TOL);
  }

  @Test
  public void testLatitudeInvalid() {
    double oldLatitude = circle.Latitude();
    circle.Latitude(9000);
    assertEquals(oldLatitude, circle.Latitude(), DEG_TOL);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LATITUDE);
  }

  @Test
  public void testLongitudeValid() {
    circle.Longitude(1.0);
    assertEquals(1.0, circle.Longitude(), DEG_TOL);
  }

  @Test
  public void testLongitudeInvalid() {
    double oldLongitude = circle.Longitude();
    circle.Longitude(9000);
    assertEquals(oldLongitude, circle.Longitude(), DEG_TOL);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LONGITUDE);
  }

  @Test
  public void testSetLocationValid() {
    circle.SetLocation(1.0, -1.0);
    assertEquals(1.0, circle.Latitude(), DEG_TOL);
    assertEquals(-1.0, circle.Longitude(), DEG_TOL);
  }

  @Test
  public void testSetLocationInvalidLatitude() {
    double oldLatitude = circle.Latitude();
    double oldLongitude = circle.Longitude();
    circle.SetLocation(9000, 1.0);
    assertEquals(oldLatitude, circle.Latitude(), DEG_TOL);
    assertEquals(oldLongitude, circle.Longitude(), DEG_TOL);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LATITUDE);
  }

  @Test
  public void testSetLocationInvalidLongitude() {
    double oldLatitude = circle.Latitude();
    double oldLongitude = circle.Longitude();
    circle.SetLocation(1.0, 9000);
    assertEquals(oldLatitude, circle.Latitude(), DEG_TOL);
    assertEquals(oldLongitude, circle.Longitude(), DEG_TOL);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LONGITUDE);
  }

  @Test
  public void testUpdateCenter() {
    circle.updateCenter(42, 42);
    assertEquals(42.0, circle.Latitude(), DEG_TOL);
    assertEquals(42.0, circle.Longitude(), DEG_TOL);
  }
}
