// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.MapFactory.MapType;
import org.junit.Before;
import org.junit.Test;

/**
 * The only purpose this test serves is to ensure that we have 100% coverage of DummyMapController
 * so that in future extensions to Maps, if we add new methods, we test that the new methods also
 * throw UnsupportedOperationException.
 */
public class DummyMapControllerTest {

  private DummyMapController mapController;

  @Before
  public void setUp() {
    mapController = new DummyMapController();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetViewThrows() {
    mapController.getView();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetLatitude() {
    mapController.getLatitude();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetLongitude() {
    mapController.getLongitude();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetCenter() {
    mapController.setCenter(0.0, 0.0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetZoom() {
    mapController.setZoom(0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetZoom() {
    mapController.getZoom();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetMapType() {
    mapController.setMapType(MapType.ROADS);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetMapType() {
    mapController.getMapType();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetCompassEnabled() {
    mapController.setCompassEnabled(true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsCompassEnabled() {
    mapController.isCompassEnabled();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetZoomEnabled() {
    mapController.setZoomEnabled(true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsZoomEnabled() {
    mapController.isZoomEnabled();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetZoomControlEnabled() {
    mapController.setZoomControlEnabled(true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsZoomControlEnabled() {
    mapController.isZoomControlEnabled();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testShowUserEnabled() {
    mapController.setShowUserEnabled(true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsShowUserEnabled() {
    mapController.isShowUserEnabled();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetRotationEnabled() {
    mapController.setRotationEnabled(true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsRotationEnabled() {
    mapController.isRotationEnabled();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetPanEnabled() {
    mapController.setPanEnabled(true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsPanEnabled() {
    mapController.isPanEnabled();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPanTo() {
    mapController.panTo(0.0, 0.0, 0, 1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddEventListener() {
    mapController.addEventListener(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddFeatureMarker() {
    mapController.addFeature((MapMarker) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeaturePosition() {
    mapController.updateFeaturePosition((MapMarker) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeatureFill() {
    mapController.updateFeatureFill(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeatureImage() {
    mapController.updateFeatureImage(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeatureText() {
    mapController.updateFeatureText(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeatureDraggable() {
    mapController.updateFeatureDraggable(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetBoundingBox() {
    mapController.getBoundingBox();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetBoundingBox() {
    mapController.setBoundingBox(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddFeatureLineString() {
    mapController.addFeature((MapLineString) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddFeaturePolygon() {
    mapController.addFeature((MapPolygon) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddFeatureCircle() {
    mapController.addFeature((MapCircle) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAddFeatureRectangle() {
    mapController.addFeature((MapRectangle) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveFeature() {
    mapController.removeFeature(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testShowFeature() {
    mapController.showFeature(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testHideFeature() {
    mapController.hideFeature(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsFeatureVisible() {
    mapController.isFeatureVisible(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testShowInfobox() {
    mapController.showInfobox(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testHideInfobox() {
    mapController.hideInfobox(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsInfoboxVisible() {
    mapController.isInfoboxVisible(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeaturePositionLineString() {
    mapController.updateFeaturePosition((MapLineString) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeaturePositionPolygon() {
    mapController.updateFeaturePosition((MapPolygon) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeaturePositionCircle() {
    mapController.updateFeaturePosition((MapCircle) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeaturePositionRectangle() {
    mapController.updateFeaturePosition((MapRectangle) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeatureStroke() {
    mapController.updateFeatureStroke(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUpdateFeatureSize() {
    mapController.updateFeatureSize(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetLocationListener() {
    mapController.getLocationListener();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetOverlayCount() {
    mapController.getOverlayCount();
  }
}
