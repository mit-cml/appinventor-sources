// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.hardware.Sensor;
import android.view.View;
import android.widget.RelativeLayout;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowSensorManager;
import org.robolectric.shadows.ShadowView;

import java.io.IOException;
import java.util.Collections;

import static com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher.assertErrorOccurred;
import static com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher.assertEventFired;
import static com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher.assertEventFiredAny;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the Map component.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class MapTest extends MapTestBase {

  private Map map;

  @Before
  public void setUp() {
    super.setUp();
    map = getMap();
  }

  @Test
  public void testViewNotNull() {
    assertNotNull(map.getView());
  }

  @Test
  public void testCenterFromString() {
    map.CenterFromString("1, 2");
    assertEquals(1.0, map.Latitude(), 1e-4);
    assertEquals(2.0, map.Longitude(), 1e-4);
  }

  @Test
  public void testIncorrectCenterFromStringLength() {
    map.CenterFromString("");
    assertEventFiredAny(map, "InvalidPoint");
  }

  @Test
  public void testCenterInvalidLatitude() {
    map.CenterFromString("bad, 0");
    assertEventFiredAny(map, "InvalidPoint");
  }

  @Test
  public void testCenterOutOfBoundsLatitude() {
    map.CenterFromString("9999, 0");
    map.CenterFromString("-9999, 0");
    assertEventFiredAny(map, "InvalidPoint");
  }

  @Test
  public void testCenterInvalidLongitude() {
    map.CenterFromString("0, bad");
    assertEventFiredAny(map, "InvalidPoint");
  }

  @Test
  public void testCenterOutOfBoundsLongitude() {
    map.CenterFromString("0, 9999");
    map.CenterFromString("0, -9999");
    assertEventFiredAny(map, "InvalidPoint");
  }

  @Test
  public void testZoom() {
    map.ZoomLevel(10);
    assertEquals(10, map.ZoomLevel());
  }

  @Test
  public void testDisableZoom() {
    map.EnableZoom(false);
    assertFalse(map.EnableZoom());
  }

  @Test
  public void testDisableRotation() {
    map.EnableRotation(false);
    assertFalse(map.EnableRotation());
  }

  @Test
  public void testDisablePan() {
    map.EnablePan(false);
    assertFalse(map.EnablePan());
  }

  @Test
  public void testMapType() {
    map.MapType(2);
    assertEquals(2, map.MapType());
  }

  @Test
  public void testShowCompass() {
    /* We need to create a sensor for the orientation type, otherwise compass will fail to enable */
    Context context = RuntimeEnvironment.application.getApplicationContext();
    ShadowSensorManager sensorManager = Shadow.extract(context.getSystemService(Context.SENSOR_SERVICE));
    Sensor s = Shadow.newInstanceOf(Sensor.class);
    sensorManager.addSensor(Sensor.TYPE_ORIENTATION, s);
    /* end setup */
    map.ShowCompass(true);
    assertTrue(map.ShowCompass());
  }

  @Test
  public void testShowZoom() {
    map.ShowZoom(true);
    assertTrue(map.ShowZoom());
  }

  @Test
  public void testShowUser() {
    map.ShowUser(true);
    assertTrue(map.ShowUser());
  }

  /**
   * Test that:
   * !) Showing the scale invalidates the view
   * 2) if we show the scale, the ShowScale getter will return true.
   */
  @Test
  public void testShowScale() {
    shadowOf(getMapView()).clearWasInvalidated();
    map.ShowScale(true);
    // Make sure that the view was invalidated
    assertTrue(shadowOf(getMapView()).wasInvalidated());
    assertTrue(map.ShowScale());
  }

  /**
   * Test that:
   * 1) Changing the scale invalidates the map view
   * 2) If we change the scale, the ScaleUnits getter will return the new value
   */
  @Test
  public void testScaleUnits() {
    shadowOf(getMapView()).clearWasInvalidated();
    map.ScaleUnits(2);
    // Make sure that the view was invalidated
    assertTrue(shadowOf(getMapView()).wasInvalidated());
    assertEquals(2, map.ScaleUnits());
  }

  /**
   * Test that, if we give an invalid unit system identifier, the system will
   * dispatch an error through the Form.
   */
  @Test
  public void testScaleUnitsInvalid() {
    map.ScaleUnits(-1);
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_UNIT_SYSTEM);
  }

  @Test
  public void testLocationSensor() {
    LocationSensor sensor = new LocationSensor(getForm());
    map.LocationSensor(sensor);
    assertEquals(sensor, map.LocationSensor());
  }

  @Test
  public void testLocationSensorNull() {
    map.LocationSensor(null);
    assertNull(map.LocationSensor());
  }

  @Test
  public void testBoundingBox() {
    YailList bounds = YailList.makeList(new Object[] {
        GeometryUtil.asYailList(new GeoPoint(1.0, -1.0)),
        GeometryUtil.asYailList(new GeoPoint(-1.0, 1.0))
    });
    map.BoundingBox(bounds);
    bounds = map.BoundingBox();
    assertEquals(2, bounds.size());
    YailList nw = (YailList) bounds.get(1), se = (YailList) bounds.get(2);
    assertEquals(2, nw.size());
    assertEquals(1.58183026, (Double) nw.get(1), DEG_TOL);
    assertEquals(-1.93359375, (Double) nw.get(2), DEG_TOL);
    assertEquals(-1.58183026, (Double) se.get(1), DEG_TOL);
    assertEquals(1.93359375, (Double) se.get(2), DEG_TOL);
  }

  @Test
  public void testUserLocationNoSensor() {
    map.LocationSensor(null);
    assertFalse(GeometryUtil.isValidLatitude(map.UserLatitude()));
    assertFalse(GeometryUtil.isValidLongitude(map.UserLongitude()));
  }

  @Test
  public void testUserLocationSensor() {
    LocationSensor mock = EasyMock.createMock(LocationSensor.class);
    map.LocationSensor(mock);
    expect(mock.Latitude()).andReturn(42.0);
    expect(mock.Longitude()).andReturn(-72.0);
    replay(mock);
    assertEquals(42.0, map.UserLatitude(), DEG_TOL);
    assertEquals(-72.0, map.UserLongitude(), DEG_TOL);
  }

  @Test
  public void testPanTo() throws InterruptedException {
    map.PanTo(30.0, -30.0, 18);
    runAllEvents();
    assertEquals(18, map.ZoomLevel());  // Zoom updates immediately.
    // Unfortunately, animations can't be unit tested, otherwise we could validate that the
    // Lat/Long also change, but they only reflect the first step in the animation, not the
    // final result.
  }

  @Test
  public void testSave() throws IOException {
    java.io.File file = java.io.File.createTempFile("test", "geojson");
    file.deleteOnExit();
    map.Save(file.getAbsolutePath());
    // TODO(ewpatton): How can we test that this is successful?
  }

  @Test
  public void testSaveBadPath() throws InterruptedException {
    map.Save("/bad/path/for/test.json");
    ShadowAsynchUtil.runAllPendingRunnables();
    runAllEvents();
    Thread.sleep(100);
    runAllEvents();
    assertErrorOccurred(ErrorMessages.ERROR_EXCEPTION_DURING_MAP_SAVE);
  }

  @Test
  public void testCreateMarker() {
    Marker marker = map.CreateMarker(4.0, -4.0);
    assertNotNull(marker);
    assertEquals(4.0, marker.Latitude(), DEG_TOL);
    assertEquals(-4.0, marker.Longitude(), DEG_TOL);
    assertTrue(map.Features().contains(marker));
  }

  @Test
  public void testOnReady() {
    map.onReady(map.getController());
    runAllEvents();
    assertEventFired(map, "Ready");
  }

  @Test
  public void testOnSingleTap() {
    map.onSingleTap(0.0, 0.0);
    runAllEvents();
    assertEventFired(map, "TapAtPoint", 0.0, 0.0);
  }

  @Test
  public void testOnDoubleTap() {
    map.onDoubleTap(0.0, 0.0);
    runAllEvents();
    assertEventFired(map, "DoubleTapAtPoint", 0.0, 0.0);
  }

  @Test
  public void testOnLongPress() {
    map.onLongPress(0.0, 0.0);
    runAllEvents();
    assertEventFired(map, "LongPressAtPoint", 0.0, 0.0);
  }

  @Test
  public void testOnFeatureClick() {
    Marker marker = new Marker(getMap());
    map.onFeatureClick(marker);
    runAllEvents();
    assertEventFired(marker, "Click");
    assertEventFired(map, "FeatureClick", marker);
  }

  @Test
  public void testOnFeatureLongPress() {
    Marker marker = new Marker(getMap());
    map.onFeatureLongPress(marker);
    runAllEvents();
    assertEventFired(marker, "LongClick");
    assertEventFired(map, "FeatureLongClick", marker);
  }

  @Test
  public void testOnFeatureStartDrag() {
    Marker marker = new Marker(getMap());
    map.onFeatureStartDrag(marker);
    runAllEvents();
    assertEventFired(marker, "StartDrag");
    assertEventFired(map, "FeatureStartDrag", marker);
  }

  @Test
  public void testOnFeatureDrag() {
    Marker marker = new Marker(getMap());
    map.onFeatureDrag(marker);
    runAllEvents();
    assertEventFired(marker, "Drag");
    assertEventFired(map, "FeatureDrag", marker);
  }

  @Test
  public void testOnFeatureStopDrag() {
    Marker marker = new Marker(getMap());
    map.onFeatureStopDrag(marker);
    runAllEvents();
    assertEventFired(marker, "StopDrag");
    assertEventFired(map, "FeatureStopDrag", marker);
  }

  @Test
  public void testFeatureList() {
    Marker marker = new Marker(map);
    assertEquals(1, map.Features().size());
    assertEquals(marker, map.Features().get(1));
  }

  @Test
  public void testFeatureListRemoval() {
    Marker marker1 = new Marker(map);
    Marker marker2 = new Marker(map);
    ((ShadowView) Shadow.extract(map.getView())).clearWasInvalidated();
    map.Features(YailList.makeList(Collections.singletonList(marker1)));
    assertTrue(((ShadowView) Shadow.extract(map.getView())).wasInvalidated());
    assertEquals(1, map.Features().size());
    assertTrue(map.Features().contains(marker1));
    assertFalse(map.Features().contains(marker2));
  }

  /**
   * Tests that removing a feature from the map results in the feature list going back to its
   * original size.
   */
  @Test
  public void testRemoveFeature() {
    Marker marker = new Marker(map);
    map.removeFeature(marker);
    assertEquals(0, map.Features().size());
  }

  /**
   * Tests that setting the feature list to an empty list results in clearing out of the overlays
   * in the map.
   */
  @Test
  public void testResetFeatureList() {
    int defaultFeatureListSize = map.getController().getOverlayCount();
    new Marker(map);
    assertEquals(defaultFeatureListSize + 1, map.getController().getOverlayCount());
    map.Features(YailList.makeEmptyList());
    assertEquals(0, map.Features().size());
    assertEquals(defaultFeatureListSize, map.getController().getOverlayCount());
  }

  private MapView getMapView() {
    RelativeLayout layout = (RelativeLayout) map.getView();
    return (MapView) layout.getChildAt(0);
  }

  private static ShadowView shadowOf(View view) {
    return (ShadowView) Shadow.extract(view);
  }
}
