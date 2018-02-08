// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Color;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.views.ShadowMapView;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;
import org.robolectric.shadow.api.Shadow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the Marker component.
 */
public class MarkerTest extends MapTestBase {

  private Marker marker;

  @Before
  public void setUp() {
    super.setUp();
    marker = new Marker(getMap());
  }

  @Test
  public void testDefaultLocation() {
    assertEquals(0.0, marker.Latitude(), DEG_TOL);
    assertEquals(0.0, marker.Longitude(), DEG_TOL);
  }

  @Test
  public void testUpdateLocation() {
    marker.updateLocation(1.0, -1.0);
    assertEquals(1.0, marker.Latitude(), DEG_TOL);
    assertEquals(-1.0, marker.Longitude(), DEG_TOL);
    assertEquals(new GeoPoint(1.0, -1.0), marker.getCentroid());
  }

  @Test
  public void testGetLocation() {
    GeoPoint gp = new GeoPoint(0.0, 0.0);
    assertEquals(gp, marker.getLocation());
  }

  @Test
  public void testInvalidNorthLatitude() {
    marker.Latitude(9000);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LATITUDE);
  }

  @Test
  public void testInvalidSouthLatitude() {
    marker.Latitude(-9000);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LATITUDE);
  }

  @Test
  public void testInvalidEastLongitude() {
    marker.Longitude(9000);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LONGITUDE);
  }

  @Test
  public void testInvalidWestLongitude() {
    marker.Longitude(-9000);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LONGITUDE);
  }

  /**
   * Tests that the marker class reports itself as the Marker type. This may seem trivial but
   * because we provide a Save feature, we want to test that a future change to how the Type is
   * reported breaks compatibility with the existing code.
   */
  @Test
  public void testMarkerIsMarker() {
    assertEquals("Marker", marker.Type());
  }

  /**
   * Tests that the latitude/longitude property definitions appear sane with valid input.
   */
  @Test
  public void testValidLatLon() {
    marker.Latitude(1.0);
    marker.Longitude(-1.0);
    assertEquals(1.0, marker.Latitude(), DEG_TOL);
    assertEquals(-1.0, marker.Longitude(), DEG_TOL);
  }

  @Test
  public void testEnableInfoboxFalseByDefault() {
    assertFalse(marker.EnableInfobox());
  }

  @Test
  public void testInfoboxFailsToOpen() {
    assertFalse(getMap().getController().isInfoboxVisible(marker));
  }

  @Test
  public void testImageAsset() {
    marker.ImageAsset("component/marker.svg");
    assertEquals("component/marker.svg", marker.ImageAsset());
  }

  @Test
  public void testAnchorHorizontal() {
    marker.AnchorHorizontal(1);
    assertEquals(1, marker.AnchorHorizontal());
  }

  @Test
  public void testAnchorHorizontalBadArgument1() {
    marker.AnchorHorizontal(-1);
    assertEquals(3, marker.AnchorHorizontal());
    ShadowEventDispatcher.assertErrorOccurred();
  }

  @Test
  public void testAnchorHorizontalBadArgument2() {
    marker.AnchorHorizontal(9000);
    assertEquals(3, marker.AnchorHorizontal());
    ShadowEventDispatcher.assertErrorOccurred();
  }

  @Test
  public void testAnchorVertical() {
    marker.AnchorVertical(1);
    assertEquals(1, marker.AnchorVertical());
  }

  @Test
  public void testAnchorVerticalBadArgument1() {
    marker.AnchorVertical(-1);
    assertEquals(3, marker.AnchorVertical());
    ShadowEventDispatcher.assertErrorOccurred();
  }

  @Test
  public void testAnchorVerticalBadArgument2() {
    marker.AnchorVertical(9000);
    assertEquals(3, marker.AnchorVertical());
    ShadowEventDispatcher.assertErrorOccurred();
  }

  @Test
  public void testGeometryOptimization() {
    assertEquals(marker.getGeometry(), marker.getGeometry());
    Geometry x = marker.getGeometry();
    marker.SetLocation(1.0, 1.0);
    assertFalse(marker.getGeometry().equals(x));
  }

  /**
   * Tests that the deprecated {@link Marker#ShowShadow()} property returns false.
   */
  @Test
  public void testShowShadow() {
    assertFalse(marker.ShowShadow());
  }

  @Test
  public void testEnableInfobox() {
    marker.EnableInfobox(true);
    assertTrue(marker.EnableInfobox());
  }

  @Test
  public void testInfoboxShows() {
    marker.ShowInfobox();
    assertTrue(getMap().getController().isInfoboxVisible(marker));
  }

  @Test
  public void testInfoboxHides() {
    marker.ShowInfobox();
    marker.HideInfobox();
    assertFalse(getMap().getController().isInfoboxVisible(marker));
  }

  @Test
  public void testClickEvent() {
    marker.Click();
    ShadowEventDispatcher.assertEventFired(marker, "Click");
    ShadowEventDispatcher.assertEventFired(getMap(), "FeatureClick", marker);
  }

  @Test
  public void testLongClickEvent() {
    marker.LongClick();
    ShadowEventDispatcher.assertEventFired(marker, "LongClick");
    ShadowEventDispatcher.assertEventFired(getMap(), "FeatureLongClick", marker);
  }

  @Test
  public void testDragStartEvent() {
    marker.StartDrag();
    ShadowEventDispatcher.assertEventFired(marker, "StartDrag");
    ShadowEventDispatcher.assertEventFired(getMap(), "FeatureStartDrag", marker);
  }

  @Test
  public void testDragEvent() {
    marker.Drag();
    ShadowEventDispatcher.assertEventFired(marker, "Drag");
    ShadowEventDispatcher.assertEventFired(getMap(), "FeatureDrag", marker);
  }

  @Test
  public void testDragStopEvent() {
    marker.StopDrag();
    ShadowEventDispatcher.assertEventFired(marker, "StopDrag");
    ShadowEventDispatcher.assertEventFired(getMap(), "FeatureStopDrag", marker);
  }

  @Test
  public void testWidthFill() {
    marker.Width(Component.LENGTH_FILL_PARENT);
    assertEquals(getMap().Width(), marker.Width());
  }

  @Test
  public void testWidthPercent() {
    marker.WidthPercent(50);
    assertEquals(getMap().Width() / 2.0, marker.Width(), 1.0);
  }

  @Test
  public void testHeightFill() {
    marker.Height(Component.LENGTH_FILL_PARENT);
    assertEquals(getMap().Height(), marker.Height());
  }

  @Test
  public void testHeightPercent() {
    marker.HeightPercent(50);
    assertEquals(getMap().Height() / 2.0, marker.Height(), 1.0);
  }

  @Test
  public void testDefaultFillColor() {
    assertEquals(Color.RED, marker.FillColor());
  }

  @Test
  public void testFillColor() {
    marker.FillColor(Color.BLUE);
    assertEquals(Color.BLUE, marker.FillColor());
  }

  @Test
  public void testDefaultStrokeColor() {
    assertEquals(Color.BLACK, marker.StrokeColor());
  }

  @Test
  public void testStrokeColor() {
    marker.StrokeColor(Color.BLUE);
    assertEquals(Color.BLUE, marker.StrokeColor());
  }

  @Test
  public void testVisibleNoInvalidate() {
    ShadowMapView mapView = Shadow.extract(getMap().getView());
    Marker marker = new Marker(getMap());
    int invalidateCalls = mapView.invalidateCalls;
    marker.Visible(true);
    assertTrue(getMap().getController().isFeatureVisible(marker));
    assertEquals(invalidateCalls, mapView.invalidateCalls);
  }

  @Test
  public void testVisibleInvalidate() {
    ShadowMapView mapView = Shadow.extract(getMap().getView());
    Marker marker = new Marker(getMap());
    int invalidateCalls = mapView.invalidateCalls;
    marker.Visible(true);
    marker.Visible(false);
    assertFalse(getMap().getController().isFeatureVisible(marker));
    assertEquals(invalidateCalls + 1, mapView.invalidateCalls);
  }

  @Test
  public void testVisible() {
    Marker marker = new Marker(getMap());
    marker.Visible(false);
    marker.Visible(true);
    assertTrue(getMap().getController().isFeatureVisible(marker));
  }

  @Test
  public void testDispatchDelegate() {
    Marker marker = new Marker(getMap());
    assertEquals(getForm(), marker.getDispatchDelegate());
  }

  @Test
  public void testDistanceToPointProxy() {
    assertEquals(marker.DistanceToPoint(0.0, 1.0, true), marker.DistanceToPoint(0.0, 1.0), M_TOL);
    assertEquals(marker.DistanceToPoint(0.0, 1.0, false), marker.DistanceToPoint(0.0, 1.0), M_TOL);
  }

  public static Marker createMarker(Map map, double latitude, double longitude) {
    Marker marker = new Marker(map);
    marker.SetLocation(latitude, longitude);
    return marker;
  }
}
