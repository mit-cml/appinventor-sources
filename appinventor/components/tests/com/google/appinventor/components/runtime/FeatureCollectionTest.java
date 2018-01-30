// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.views.ShadowMapView;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadow.api.Shadow;

import java.util.Collections;

import static com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher.assertEventFired;
import static com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher.assertEventFiredAny;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FeatureCollectionTest extends MapTestBase {

  private FeatureCollection collection;

  @Before
  public void setUp() {
    super.setUp();
    collection = new FeatureCollection(getMap());
  }

  @Test
  public void testFeaturesFromGeoJSON() {
    collection.FeaturesFromGeoJSON("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-71.0,42]}}]}");
    runAllEvents();
    ShadowEventDispatcher.assertEventFiredAny(collection, "GotFeatures");
  }

  @Test
  public void testFeaturesFromGeoJSONBadInput() {
    collection.FeaturesFromGeoJSON("[bad]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_GEOJSON);
  }

  @Test
  public void testFeaturesFromGeoJSONInvalidType() {
    collection.FeaturesFromGeoJSON("{\"type\": \"Garbage\"}");
    ShadowEventDispatcher.assertEventFiredAny(collection, "LoadError");
  }

  @Test
  public void testSourceSetter() {
    collection.Source("foo.geojson");
    assertEquals("foo.geojson", collection.Source());
  }

  @Test
  public void testLoadedFeatureCollection() {
    final String source = "http://example.com/foo.json";
    collection.GotFeatures(source, YailList.makeEmptyList());
    assertEquals(source, collection.Source());
    assertEventFiredAny(collection, "GotFeatures");
  }

  @Test
  public void testErrorLoadingFeatureCollection() {
    collection.LoadError("http://example.com/foo.json", 404, "Not Found");
    assertEventFiredAny(collection, "LoadError");
  }

  @Test
  public void testFeatureClick() {
    Marker marker = new Marker(collection);
    getMap().onFeatureClick(marker);
    assertEventFired(marker, "Click");
    assertEventFired(collection, "FeatureClick", marker);
    assertEventFired(getMap(), "FeatureClick", marker);
  }

  @Test
  public void testFeatureLongClick() {
    Marker marker = new Marker(collection);
    getMap().onFeatureLongPress(marker);
    assertEventFired(marker, "LongClick");
    assertEventFired(collection, "FeatureLongClick", marker);
    assertEventFired(getMap(), "FeatureLongClick", marker);
  }

  @Test
  public void testFeatureStartDrag() {
    Marker marker = new Marker(collection);
    getMap().onFeatureStartDrag(marker);
    assertEventFired(marker, "StartDrag");
    assertEventFired(collection, "FeatureStartDrag", marker);
    assertEventFired(getMap(), "FeatureStartDrag", marker);
  }

  @Test
  public void testFeatureDrag() {
    Marker marker = new Marker(collection);
    getMap().onFeatureDrag(marker);
    assertEventFired(marker, "Drag");
    assertEventFired(collection, "FeatureDrag", marker);
    assertEventFired(getMap(), "FeatureDrag", marker);
  }

  @Test
  public void testFeatureStopDrag() {
    Marker marker = new Marker(collection);
    getMap().onFeatureStopDrag(marker);
    assertEventFired(marker, "StopDrag");
    assertEventFired(collection, "FeatureStopDrag", marker);
    assertEventFired(getMap(), "FeatureStopDrag", marker);
  }

  @Test
  public void testContext() {
    assertEquals(getForm(), collection.$context());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAdd() {
    collection.$add(new Button(getForm()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetChildWidth() {
    collection.setChildWidth(new Button(getForm()), 0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetChildHeight() {
    collection.setChildHeight(new Button(getForm()), 0);
  }

  @Test
  public void testRemoveFeature() {
    Marker marker = new Marker(collection);
    assertEquals(1, collection.Features().size());
    assertTrue(collection.Features().contains(marker));
    collection.removeFeature(marker);
    assertEquals(0, collection.Features().size());
    assertFalse(collection.Features().contains(marker));
  }

  @Test
  public void testAddMarker() {
    testFeatureListSetter(new Marker(collection));
  }

  @Test
  public void testAddLineString() {
    testFeatureListSetter(new LineString(collection));
  }

  @Test
  public void testAddPolygon() {
    testFeatureListSetter(new Polygon(collection));
  }

  @Test
  public void testAddCircle() {
    testFeatureListSetter(new Circle(collection));
  }

  @Test
  public void testAddRectangle() {
    testFeatureListSetter(new Rectangle(collection));
  }

  @Test
  public void testFeatureSetter() {
    Marker marker = new Marker(getMap());
    collection.removeFeature(marker);
    assertEquals(0, collection.Features().size());
    ShadowMapView view = Shadow.extract(getMap().getView());
    view.clearWasInvalidated();
    collection.Features(YailList.makeList(Collections.singletonList(marker)));
    assertTrue(view.wasInvalidated());
    assertEquals(1, collection.Features().size());
    assertEquals(marker, collection.Features().get(1));
  }

  @Test
  public void testFeatureSetterBadInputs() {
    getMap().Features(YailList.makeList(Collections.singletonList("foo")));
    assertEquals(0, getMap().Features().size());
  }

  @Test
  public void testViewNull() {
    assertNull(collection.getView());
  }

  @Test
  public void testGetMap() {
    assertEquals(getMap(), collection.getMap());
  }

  private void testFeatureListSetter(MapFeature feature) {
    ShadowMapView view = Shadow.extract(getMap().getView());
    view.clearWasInvalidated();
    collection.Features(YailList.makeList(Collections.singletonList(feature)));
    assertTrue(view.wasInvalidated());
    assertEquals(1, collection.Features().size());
    assertEquals(feature, collection.Features().get(1));
  }
}
