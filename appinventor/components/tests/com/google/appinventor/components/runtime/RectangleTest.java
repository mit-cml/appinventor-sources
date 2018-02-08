// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the Rectangle component.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class RectangleTest extends MapTestBase {

  private Rectangle rect;

  @Before
  public void setUp() {
    super.setUp();
    rect = new Rectangle(getMap());
    assertTrue(getMap().Features().contains(rect));
  }

  /**
   * Tests that the setter and getter properties defining the rectangle's bounds behave sanely.
   */
  @Test
  public void testGetSetBounds() {
    defaultRectangle(rect);
    assertEquals(NORTH_LAT, rect.NorthLatitude(), DEG_TOL);
    assertEquals(SOUTH_LAT, rect.SouthLatitude(), DEG_TOL);
    assertEquals(WEST_LON, rect.WestLongitude(), DEG_TOL);
    assertEquals(EAST_LON, rect.EastLongitude(), DEG_TOL);
    assertEquals(2, rect.Bounds().size());
    YailList nw = (YailList) rect.Bounds().get(1);
    YailList se = (YailList) rect.Bounds().get(2);
    assertEquals(NORTH_LAT, nw.get(1));
    assertEquals(WEST_LON, nw.get(2));
    assertEquals(SOUTH_LAT, se.get(1));
    assertEquals(EAST_LON, se.get(2));
  }

  /**
   * Tests that an error is raised when an out-of-bounds northern latitude is given.
   */
  @Test
  public void testInvalidCenterLatitudeNorth() {
    rect.SetCenter(100, 0);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_POINT);
  }

  /**
   * Tests that an error is raised when an out-of-bounds southern latitude is given.
   */
  @Test
  public void testInvalidCenterLatitudeSouth() {
    rect.SetCenter(-100, 0);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_POINT);
  }

  /**
   * Tests that an error is raised when an out-of-bounds eastern longitude is given.
   */
  @Test
  public void testInvalidCenterLongitudeEast() {
    rect.SetCenter(0, 200);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_POINT);
  }

  /**
   * Tests that an error is raised when an out-of-bounds western longitude is given.
   */
  @Test
  public void testInvalidCenterLongitudeWest() {
    rect.SetCenter(0, -200);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_POINT);
  }

  /**
   * Tests that setting the center of the rectangle shifts its bounds.
   */
  @Test
  public void testSetCenter() {
    defaultRectangle(rect);
    rect.SetCenter(0, 1);
    assertEquals(1.0, rect.NorthLatitude(), DEG_TOL);
    assertEquals(-1.0, rect.SouthLatitude(), DEG_TOL);
    assertEquals(2.0, rect.EastLongitude(), DEG_TOL);
    assertEquals(0.0, rect.WestLongitude(), DEG_TOL);
    assertEquals(0.0, ((Number) rect.Center().get(1)).doubleValue(), DEG_TOL);
    assertEquals(1.0, ((Number) rect.Center().get(2)).doubleValue(), DEG_TOL);
  }

  /**
   * Tests that the rectangle reports itself as a Rectangle type.
   *
   * This test may seem tautological, but given that when we write out the data using the Save
   * method of Map, we want to ensure future compatibility and breaking changes to the name should
   * be caught by testing.
   */
  @Test
  public void testRectangleIsRectangle() {
    assertEquals("Rectangle", rect.Type());
  }

  /**
   * This is really just intended as a sanity check. If this test fails then we've done something
   * horribly wrong.
   */
  @Test
  public void testDistanceToSelf() {
    defaultRectangle(rect);
    assertEquals(0.0, rect.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(rect, false), M_TOL);
  }

  /**
   * Tests that updating the bounds of the rectangle (as MapRectangle) affects the appropriate
   * properties.
   */
  @Test
  public void testUpdateBounds() {
    MapRectangle r = rect;
    r.updateBounds(NORTH_LAT, WEST_LON, SOUTH_LAT, EAST_LON);
    assertEquals(NORTH_LAT, r.NorthLatitude(), DEG_TOL);
    assertEquals(SOUTH_LAT, r.SouthLatitude(), DEG_TOL);
    assertEquals(WEST_LON, r.WestLongitude(), DEG_TOL);
    assertEquals(EAST_LON, r.EastLongitude(), DEG_TOL);
  }

  @Test
  public void testCentroid() {
    defaultRectangle(rect);
    YailList centroid = rect.Centroid();
    assertEquals(2, centroid.size());
    assertEquals(0.0, (double) centroid.get(1), DEG_TOL);
    assertEquals(0.0, (double) centroid.get(2), DEG_TOL);
  }

}
