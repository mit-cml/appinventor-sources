// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.ViewGroup;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.tileprovider.modules.ShadowMapTileModuleProviderBase;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.views.ShadowMapView;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.osmdroid.util.GeoPoint;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

import static com.google.appinventor.components.runtime.util.GeometryUtil.ONE_DEG_IN_METERS;

@Config(shadows = {ShadowMapView.class, ShadowMapTileModuleProviderBase.class})
public class MapTestBase extends RobolectricTestBase {

  /** Absolute tolerance allowed in degrees */
  static final double DEG_TOL = 1.0E-5;

  /** Absolute tolerance allowed in meters */
  static final double M_TOL = 1;

  static final double NORTH_LAT =  1.0;
  static final double SOUTH_LAT = -1.0;
  static final double EAST_LON  =  1.0;
  static final double WEST_LON  = -1.0;

  private Map map;

  public static LineString defaultLineNS(LineString line) {
    line.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] { 1.0, 0.0 }),
        YailList.makeList(new Object[] { -1.0, 0.0 })
    }));
    return line;
  }

  public static LineString defaultLineEW(LineString line) {
    line.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] { 0.0, 1.0 }),
        YailList.makeList(new Object[] { 0.0, -1.0 })
    }));
    return line;
  }

  public static Polygon defaultPolygon(Polygon polygon) {
    polygon.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] { 1.0, 1.0 }),
        YailList.makeList(new Object[] { 1.0, -1.0 }),
        YailList.makeList(new Object[] { -1.0, -1.0 }),
        YailList.makeList(new Object[] { -1.0, 1.0})
    }));
    return polygon;
  }

  public static Polygon defaultMultipolygon(Polygon polygon) {
    polygon.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] {
            GeometryUtil.asYailList(new GeoPoint(1., 1.)),
            GeometryUtil.asYailList(new GeoPoint(1., 0.)),
            GeometryUtil.asYailList(new GeoPoint(0., 0.))
        }),
        YailList.makeList(new Object[] {
            GeometryUtil.asYailList(new GeoPoint(-1., -1.)),
            GeometryUtil.asYailList(new GeoPoint(-1., 0.)),
            GeometryUtil.asYailList(new GeoPoint(0., 0.)),
            GeometryUtil.asYailList(new GeoPoint(0., -1.))
        })
    }));
    return polygon;
  }

  /**
   * Configures a rectangle with some default parameters for testing.
   *
   * @param rect  the rectangle to configure
   */
  public static Rectangle defaultRectangle(Rectangle rect) {
    rect.NorthLatitude(NORTH_LAT);
    rect.SouthLatitude(SOUTH_LAT);
    rect.WestLongitude(WEST_LON);
    rect.EastLongitude(EAST_LON);
    return rect;
  }

  public static Circle defaultCircle(Circle circle) {
    circle.SetLocation(0.0, 0.0);
    circle.Radius(ONE_DEG_IN_METERS);
    return circle;
  }

  public static LineString makeLineString(Map map, double lat1, double lon1, double lat2,
                                          double lon2) {
    LineString line = new LineString(map);
    line.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] { lat1, lon1 }),
        YailList.makeList(new Object[] { lat2, lon2 })
    }));
    return line;
  }

  public static Polygon makePolygon(Map map, double n, double w, double s, double e) {
    Polygon polygon = new Polygon(map);
    polygon.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] { n, w }),
        YailList.makeList(new Object[] { n, e }),
        YailList.makeList(new Object[] { s, e }),
        YailList.makeList(new Object[] { s, w })
    }));
    return polygon;
  }

  public static Rectangle makeRectangle(Map map, double n, double w, double s, double e) {
    Rectangle rect = new Rectangle(map);
    rect.NorthLatitude(n);
    rect.SouthLatitude(s);
    rect.WestLongitude(w);
    rect.EastLongitude(e);
    return rect;
  }

  public static Circle makeCircle(Map map, double lat, double lon, double radius) {
    Circle circle = new Circle(map);
    circle.SetLocation(lat, lon);
    circle.Radius(radius);
    return circle;
  }

  public Map getMap() {
    return map;
  }

  public ShadowMapView getMapShadow() {
    return Shadow.extract(((ViewGroup)map.getView()).getChildAt(0));
  }

  @Before
  public void setUp() {
    super.setUp();
    map = new Map(getForm());
    map.getView().requestLayout();
    map.getView().layout(0, 0, ComponentConstants.MAP_PREFERRED_WIDTH, ComponentConstants.MAP_PREFERRED_HEIGHT);
  }
}
