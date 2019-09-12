// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.ViewGroup;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.views.ShadowMapView;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowView;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the Polygon component.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class PolygonTest extends MapTestBase {

  private Polygon polygon;

  @Before
  public void setUp() {
    super.setUp();
    polygon = new Polygon(getMap());
    polygon.Initialize();
  }

  @Test
  public void testPointsFromString() {
    polygon.PointsFromString("[[1, 1], [-1, 1], [-1, -1], [1, -1]]");
    YailList points = polygon.Points();
    assertEquals(4, points.size());
  }

  @Test
  public void testPointsFromStringMultipolygon() {
    polygon.PointsFromString("[[[1, 1], [1, 0], [0, 0]],[[0, 0], [-1, 0], [-1, -1]]]");
    YailList points = polygon.Points();
    assertEquals(2, points.size());
  }

  @Test
  public void testPointsFromStringGarbage() {
    polygon.PointsFromString("foo");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testPointsFromStringEmpty() {
    polygon.PointsFromString("");
    assertEquals(0, polygon.Points().size());
  }

  @Test
  public void testPointsFromStringEmptyArray() {
    polygon.PointsFromString("[]");
    assertEquals(0, polygon.Points().size());
  }

  @Test
  public void testPointsFromStringTooFewPoints() {
    polygon.PointsFromString("[[0, 0]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testPointsFromStringTooFewDimensions() {
    polygon.PointsFromString("[[0], [0,0], [1,1]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testPointsFromStringWithAltitude() {
    polygon.PointsFromString("[[1.0, 1.0, 10.0], [-1.0, 1.0, 0.0], [-1.0, -1.0, -10.0]]");
    assertEquals(3, polygon.Points().size());
    assertEquals(2, ((YailList) polygon.Points().get(1)).size());
  }

  @Test
  public void testPointsFromStringMalformedJSON() {
    polygon.PointsFromString("[[0, 0");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testPointsFromStringJSONButInvalidType() {
    polygon.PointsFromString("{\"foo\": \"bar\"}");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testPolygonIsPolygon() {
    assertEquals("Polygon", polygon.Type());
  }

  @Test
  public void testPointsPolygon() {
    defaultPolygon(polygon);
    YailList points = polygon.Points();
    assertEquals(4, points.size());
  }

  @Test
  public void testPointsInvalidatesView() {
    ShadowMapView view = getMapShadow();
    view.clearWasInvalidated();
    defaultPolygon(polygon);
    assertTrue(view.wasInvalidated());
  }

  @Test
  public void testPointsMultipolygon() {
    defaultMultipolygon(polygon);
    YailList points = polygon.Points();
    assertEquals(2, points.size());
    assertEquals(3, ((YailList)points.get(1)).size());
    assertEquals(4, ((YailList)points.get(2)).size());
  }

  @Test
  public void testPointsBad() {
    polygon.Points(YailList.makeList(new Object[] { "foo" }));
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_TYPE);
  }

  @Test
  public void testPointsBad2() {
    polygon.Points(YailList.makeList(new Object[] { YailList.makeList(new Object[] { "foo" })}));
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testPointsTooFewPoints() {
    polygon.Points(YailList.makeList(new Object[] {
        GeometryUtil.asYailList(new GeoPoint(0., 0.))
    }));
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testPointsTooFewDimensions() {
    polygon.Points(YailList.makeList(new Object[] { YailList.makeList(new Object[] { 0. })}));
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testHolePointsFromString() {
    defaultPolygon(polygon);
    polygon.HolePointsFromString("[[[0.5, 0.5], [0.5, -0.5], [-0.5, -0.5], [-0.5, 0.5]]]");
  }

  @Test
  public void testHolePointsFromStringMultipolygon() {
    defaultMultipolygon(polygon);
    polygon.HolePointsFromString("[[[[0.5, 0.5], [0.5, 0], [0, 0]]],[]]");
    assertEquals(polygon.Points().size(), polygon.HolePoints().size());
  }

  @Test
  public void testHolePointsFromStringGarbage() {
    polygon.HolePointsFromString("foo");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testHolePointsFromStringEmpty() {
    polygon.HolePointsFromString("");
    assertEquals(0, polygon.HolePoints().size());
  }

  @Test
  public void testHolePointsFromStringEmptyArray() {
    polygon.HolePointsFromString("[]");
    assertEquals(0, polygon.HolePoints().size());
  }

  @Test
  public void testHolePoints() {
    defaultPolygon(polygon);
    polygon.HolePoints(YailList.makeList(new Object[] {
        // First hole
        YailList.makeList(new Object[] {
            GeometryUtil.asYailList(new GeoPoint(0.5, 0.5)),
            GeometryUtil.asYailList(new GeoPoint(0.25, 0.5)),
            GeometryUtil.asYailList(new GeoPoint(0.5, 0.25))
        })
    }));
    YailList holes = polygon.HolePoints();
    assertEquals(1, holes.size());  // one hole in polygon
    assertEquals(3, ((YailList) holes.get(1)).size());  // three points in the hole
  }

  @Test
  public void testHolePointsInvalidatesView() {
    defaultPolygon(polygon);
    ShadowMapView view = getMapShadow();
    view.clearWasInvalidated();
    polygon.HolePoints(YailList.makeList(new Object[] {
        // First hole
        YailList.makeList(new Object[] {
            GeometryUtil.asYailList(new GeoPoint(0.5, 0.5)),
            GeometryUtil.asYailList(new GeoPoint(0.25, 0.5)),
            GeometryUtil.asYailList(new GeoPoint(0.5, 0.25))
        })
    }));
    assertTrue(view.wasInvalidated());
  }

  @Test
  public void testHolePointsMultipolygon() {
    defaultMultipolygon(polygon);
    polygon.HolePoints(YailList.makeList(new Object[] {
        // Holes for first polygon
        YailList.makeList(new Object[] {
            // First hole
            YailList.makeList(new Object[] {
                GeometryUtil.asYailList(new GeoPoint(0.5, 0.5)),
                GeometryUtil.asYailList(new GeoPoint(0.25, 0.5)),
                GeometryUtil.asYailList(new GeoPoint(0.5, 0.25))
            })
        }),
        // Second polygon has no holes
        YailList.makeEmptyList()
    }));
    YailList holes = polygon.HolePoints();
    assertEquals(2, holes.size());  // two polygons
    assertEquals(1, ((YailList) holes.get(1)).size());  // one hole in first polygon
    assertEquals(3, ((YailList)((YailList) holes.get(1)).get(1)).size());  // 3 points in hole
    assertEquals(0, ((YailList) holes.get(2)).size());  // no holes in second polygon
  }

  @Test
  public void testHolePointsEmpty() {
    defaultPolygon(polygon);
    polygon.HolePoints(YailList.makeEmptyList());
    assertEquals(0, polygon.HolePoints().size());
  }

  @Test
  public void testHolePointsBad() {
    defaultPolygon(polygon);
    polygon.HolePoints(YailList.makeList(new Object[] { "foo" }));
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_TYPE);
  }

  @Test
  public void testHolePointsChildBad() {
    defaultPolygon(polygon);
    polygon.HolePoints(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] {
            YailList.makeList(new Object[] { 0.0 })
        })
    }));
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_POLYGON_PARSE_ERROR);
  }

  @Test
  public void testCentroid() {
    defaultPolygon(polygon);
    YailList centroid = polygon.Centroid();
    assertEquals(2, centroid.size());
    assertEquals(0.0, (Double) centroid.get(1), DEG_TOL);
    assertEquals(0.0, (Double) centroid.get(2), DEG_TOL);
  }

  @Test
  public void testUpdatePoints() {
    defaultPolygon(polygon);
    Geometry oldGeo = polygon.getGeometry();
    polygon.updatePoints(Collections.singletonList(Arrays.asList(
        new GeoPoint(2.0, 2.0),
        new GeoPoint(2.0, -2.0),
        new GeoPoint(-2.0, -2.0),
        new GeoPoint(-2.0, 2.0)
    )));
    YailList points = polygon.Points();
    assertEquals(4, points.size());  // 4 points in polygon
    assertFalse(oldGeo == polygon.getGeometry());  // should be a fresh object
    assertFalse(oldGeo.equals(polygon.getGeometry()));  // should be not equal
  }

  @Test
  public void testUpdateHolePoints() {
    defaultPolygon(polygon);
    polygon.updateHolePoints(Collections.singletonList(Collections.singletonList(
        Arrays.asList(
            new GeoPoint(0.5, 0.5),
            new GeoPoint(-0.5, 0.5),
            new GeoPoint(-0.5, -0.5)))));
    YailList holePoints = polygon.HolePoints();
    assertEquals(1, holePoints.size());
    assertEquals(3, ((YailList) holePoints.get(1)).size());
  }

}
