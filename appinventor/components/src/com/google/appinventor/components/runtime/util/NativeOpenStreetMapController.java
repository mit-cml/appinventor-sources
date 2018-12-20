// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.LocationSensor;
import com.google.appinventor.components.runtime.util.MapFactory.HasFill;
import com.google.appinventor.components.runtime.util.MapFactory.HasStroke;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapController;
import com.google.appinventor.components.runtime.util.MapFactory.MapEventListener;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.MapFactory.MapScaleUnits;
import com.google.appinventor.components.runtime.util.MapFactory.MapType;
import com.google.appinventor.components.runtime.view.ZoomControlView;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.OnTapListener;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Marker.OnMarkerClickListener;
import org.osmdroid.views.overlay.Marker.OnMarkerDragListener;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.OverlayWithIWVisitor;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay.UnitsOfMeasure;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.OverlayInfoWindow;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class NativeOpenStreetMapController implements MapController, MapListener {
  /* copied from SVG */
  private static final long SPECIFIED_FILL                  =    1;
  private static final long SPECIFIED_FILL_OPACITY          = 1<<2;
  private static final long SPECIFIED_STROKE                = 1<<3;
  private static final long SPECIFIED_STROKE_OPACITY        = 1<<4;
  private static final long SPECIFIED_STROKE_WIDTH          = 1<<5;
  /* end copied from SVG */

  private static final String TAG = NativeOpenStreetMapController.class.getSimpleName();
  private boolean caches;
  private final Form form;
  private RelativeLayout containerView;
  private MapView view;
  private MapType tileType;
  private boolean zoomEnabled;
  private boolean zoomControlEnabled;
  private CompassOverlay compass = null;
  private final MyLocationNewOverlay userLocation;
  private RotationGestureOverlay rotation = null;
  private Set<MapEventListener> eventListeners = new HashSet<MapEventListener>();
  private Map<MapFeature, OverlayWithIW> featureOverlays = new HashMap<MapFeature, OverlayWithIW>();
  private SVG defaultMarkerSVG = null;
  private TouchOverlay touch = null;
  private OverlayInfoWindow defaultInfoWindow = null;
  private boolean ready = false;
  private ZoomControlView zoomControls = null;
  private float lastAzimuth = Float.NaN;
  private ScaleBarOverlay scaleBar;

  private static class AppInventorLocationSensorAdapter implements IMyLocationProvider,
      LocationSensor.LocationSensorListener {
    private LocationSensor source;
    private Location lastLocation;
    private IMyLocationConsumer consumer;
    private boolean enabled = false;

    @Override
    public void setSource(LocationSensor source) {
      if (this.source == source) {
        return;  // nothing to do here
      }
      if (this.source != null) {
        this.source.Enabled(false);
      }
      this.source = source;
      if (this.source != null) {
        this.source.Enabled(enabled);
      }
    }

    @Override
    public void onTimeIntervalChanged(int time) {
    }

    @Override
    public void onDistanceIntervalChanged(int distance) {
    }

    @Override
    public void onLocationChanged(Location location) {
      lastLocation = location;
      if (consumer != null) {
        consumer.onLocationChanged(location, this);
      }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer consumer) {
      this.consumer = consumer;
      if (source != null) {
        source.Enabled(true);
        enabled = true;
      }
      return enabled;
    }

    @Override
    public void stopLocationProvider() {
      if (source != null) {
        source.Enabled(false);
      }
      enabled = false;
    }

    @Override
    public Location getLastKnownLocation() {
      return lastLocation;
    }

    @Override
    public void destroy() {
      this.consumer = null;
    }
  }

  private class TouchOverlay extends Overlay {
    private boolean scrollEnabled = true;

    @Override
    public void draw(Canvas arg0, MapView arg1, boolean arg2) {}

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY, MapView mapView) {
      return !scrollEnabled;
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY, MapView mapView) {
      return !scrollEnabled;
    }

    @Override
    public boolean onLongPress(final MotionEvent pEvent, final MapView pMapView) {
      IGeoPoint p = pMapView.getProjection().fromPixels((int) pEvent.getX(), (int) pEvent.getY());
      final double lat = p.getLatitude();
      final double lng = p.getLongitude();
      for (MapEventListener l : eventListeners) {
        l.onLongPress(lat, lng);
      }
      return false;  // We don't want to cancel propagation to other overlays
    }
  }

  private class MapReadyHandler extends Handler {

    @Override
    public void handleMessage(final Message msg) {
      switch (msg.what) {
        case MapTile.MAPTILE_SUCCESS_ID:
          if (!ready && form.canDispatchEvent(null, "MapReady")) {
            ready = true;
            form.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                for (MapEventListener l : eventListeners) {
                  l.onReady(NativeOpenStreetMapController.this);
                }
              }
            });
          }
          view.invalidate();
          break;
      }
    }
  }

  private class CustomMapView extends MapView {
    public CustomMapView(Context context) {
      super(context, null, new MapReadyHandler());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      scrollTo(getScrollX() + (oldw - w) / 2, getScrollY() + (oldh - h) / 2);
      super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void onDetach() {
      // Suppress call to parent onDetach
    }
  }

  private final AppInventorLocationSensorAdapter locationProvider;

  NativeOpenStreetMapController(final Form form) {
    File osmdroid = new File(form.getCacheDir(), "osmdroid");
    if (osmdroid.exists() || osmdroid.mkdirs()) {
      Configuration.getInstance().setOsmdroidBasePath(osmdroid);
      File osmdroidTiles = new File(osmdroid, "tiles");
      if (osmdroidTiles.exists() || osmdroidTiles.mkdirs()) {
        Configuration.getInstance().setOsmdroidTileCache(osmdroidTiles);
        caches = true;
      }
    }
    this.form = form;
    this.touch = new TouchOverlay();
    view = new CustomMapView(form.getApplicationContext());
    locationProvider = new AppInventorLocationSensorAdapter();
    defaultInfoWindow = new OverlayInfoWindow(view);
    view.setTilesScaledToDpi(true);
    view.setMapListener(this);
    view.getOverlayManager().add(touch);
    view.addOnTapListener(new OnTapListener() {
      @Override
      public void onSingleTap(MapView view, double latitude, double longitude) {
        for (MapEventListener listener : eventListeners) {
          listener.onSingleTap(latitude, longitude);
        }
      }

      @Override
      public void onDoubleTap(MapView view, double latitude, double longitude) {
        for (MapEventListener listener : eventListeners) {
          listener.onDoubleTap(latitude, longitude);
        }
      }
    });
    zoomControls = new ZoomControlView(view);
    userLocation = new MyLocationNewOverlay(locationProvider, view);
    scaleBar = new ScaleBarOverlay(view);
    scaleBar.setAlignBottom(true);
    scaleBar.setAlignRight(true);
    scaleBar.disableScaleBar();
    view.getOverlayManager().add(scaleBar);

    containerView = new RelativeLayout(form);
    containerView.setClipChildren(true);
    containerView.addView(view, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    containerView.addView(zoomControls);
    zoomControls.setVisibility(View.GONE);  // not shown by default
  }

  @Override
  public View getView() {
    return containerView;
  }

  @Override
  public double getLatitude() {
    return view.getMapCenter().getLatitude();
  }

  @Override
  public double getLongitude() {
    return view.getMapCenter().getLongitude();
  }

  @Override
  public void setCenter(double latitude, double longitude) {
    view.getController().setCenter(new GeoPoint(latitude, longitude));
  }

  @Override
  public void setZoom(int zoom) {
    view.getController().setZoom((double) zoom);
    zoomControls.updateButtons();
  }

  @Override
  public int getZoom() {
    // We pass pending as true here so that when a user sets ZoomLevel
    // and then reads it back it should be reflected.
    return (int) view.getZoomLevel(true);
  }

  @Override
  public void setZoomEnabled(boolean enable) {
    this.zoomEnabled = enable;
    view.setMultiTouchControls(enable);
  }

  @Override
  public boolean isZoomEnabled() {
    return zoomEnabled;
  }

  @Override
  public void setMapType(MapType type) {
    switch (type) {
      case ROADS:
        tileType = type;
        view.setTileSource(TileSourceFactory.MAPNIK);
        break;
      case AERIAL:
        tileType = type;
        view.setTileSource(TileSourceFactory.USGS_SAT);
        break;
      case TERRAIN:
        tileType = type;
        view.setTileSource(TileSourceFactory.USGS_TOPO);
        break;
      case UNKNOWN:
      default:
        break;
    }
  }

  @Override
  public MapType getMapType() {
    return tileType;
  }

  @Override
  public void setCompassEnabled(boolean enabled) {
    if (enabled && compass == null) {
      compass = new CompassOverlay(view.getContext(), view);
      view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          float density = view.getContext().getResources().getDisplayMetrics().density;
          compass.setCompassCenter(view.getMeasuredWidth() / density - 35, 35);
          return true;
        }
      });
      view.getOverlayManager().add(compass);
    }
    if (compass != null) {
      if (enabled) {
        if (compass.getOrientationProvider() != null) {
          compass.enableCompass();
        } else {
          compass.enableCompass(new InternalCompassOrientationProvider(view.getContext()));
        }
        compass.onOrientationChanged(lastAzimuth, null);
      } else {
        lastAzimuth = compass.getOrientation();
        compass.disableCompass();
      }
    }
  }

  @Override
  public boolean isCompassEnabled() {
    return compass != null && compass.isCompassEnabled();
  }

  @Override
  public void setZoomControlEnabled(boolean enabled) {
    if (zoomControlEnabled != enabled) {
      zoomControls.setVisibility(enabled ? View.VISIBLE : View.GONE);
      zoomControlEnabled = enabled;
      containerView.invalidate();
    }
  }

  @Override
  public boolean isZoomControlEnabled() {
    return zoomControlEnabled;
  }

  @Override
  public void setShowUserEnabled(boolean enable) {
    userLocation.setEnabled(enable);
    if (enable) {
      userLocation.enableMyLocation();
      view.getOverlayManager().add( userLocation );
    } else {
      userLocation.disableMyLocation();
      view.getOverlayManager().remove( userLocation );
    }
  }

  @Override
  public boolean isShowUserEnabled() {
    return userLocation != null && userLocation.isEnabled();
  }

  @Override
  public void setRotationEnabled(boolean enabled) {
    if (enabled && rotation == null) {
      rotation = new RotationGestureOverlay(view);
    }
    if (rotation != null) {
      rotation.setEnabled(enabled);
      if (enabled) {
        view.getOverlayManager().add( rotation );
      } else {
        view.getOverlayManager().remove( rotation );
      }
    }
  }

  @Override
  public boolean isRotationEnabled() {
    return rotation != null && rotation.isEnabled();
  }

  @Override
  public void setPanEnabled(boolean enable) {
    touch.scrollEnabled = enable;
  }

  @Override
  public boolean isPanEnabled() {
    return touch.scrollEnabled;
  }

  @Override
  public void panTo(double latitude, double longitude, int zoom, double seconds) {
    view.getController().animateTo(new GeoPoint(latitude, longitude));
    if (view.getController().zoomTo((double) zoom)) {
      Animation animation = view.getAnimation();
      if (animation != null) {
        animation.setDuration((long) (1000 * seconds));
      }
    }
  }

  @Override
  public void addEventListener(MapEventListener listener) {
    eventListeners.add(listener);
    if ((ready || ViewCompat.isAttachedToWindow(view)) && form.canDispatchEvent(null, "MapReady")) {
      ready = true;
      listener.onReady(this);
    }
  }

  @Override
  public void addFeature(final MapMarker aiMarker) {
    createNativeMarker(aiMarker, new AsyncCallbackPair<Marker>() {
      @Override
      public void onFailure(String message) {
        Log.e(TAG, "Unable to create marker: " + message);
      }

      @Override
      public void onSuccess(Marker overlay) {
        overlay.setOnMarkerClickListener(new OnMarkerClickListener() {
          @Override
          public boolean onMarkerClick(Marker marker, MapView mapView) {
            for (MapEventListener listener : eventListeners) {
              listener.onFeatureClick(aiMarker);
            }
            if (aiMarker.EnableInfobox()) {
              marker.showInfoWindow();
            }
            return false;
          }
          @Override
          public boolean onMarkerLongPress(Marker marker, MapView mapView) {
            for (MapEventListener listener : eventListeners) {
              listener.onFeatureLongPress(aiMarker);
            }
            return false;
          }
        });
        overlay.setOnMarkerDragListener(new OnMarkerDragListener() {
          @Override
          public void onMarkerDrag(Marker marker) {
            for (MapEventListener listener : eventListeners) {
              listener.onFeatureDrag(aiMarker);
            }
          }

          @Override
          public void onMarkerDragEnd(Marker marker) {
            IGeoPoint point = marker.getPosition();
            aiMarker.updateLocation(point.getLatitude(), point.getLongitude());
            for (MapEventListener listener : eventListeners) {
              listener.onFeatureStopDrag(aiMarker);
            }
          }

          @Override
          public void onMarkerDragStart(Marker marker) {
            for (MapEventListener listener : eventListeners) {
              listener.onFeatureStartDrag(aiMarker);
            }
          }
        });
        if (aiMarker.Visible()) {
          showOverlay(overlay);
        } else {
          hideOverlay(overlay);
        }
      }

    });
  }

  @Override
  public void addFeature(final MapLineString aiPolyline) {
    Polyline polyline = createNativePolyline(aiPolyline);
    featureOverlays.put(aiPolyline, polyline);
    polyline.setOnClickListener(new Polyline.OnClickListener() {
      @Override
      public boolean onClick(Polyline arg0, MapView arg1, GeoPoint arg2) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureClick(aiPolyline);
        }
        if (aiPolyline.EnableInfobox()) {
          arg0.showInfoWindow(arg2);
        }
        return true;
      }

      @Override
      public boolean onLongClick(Polyline arg0, MapView arg1, GeoPoint arg2) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureLongPress(aiPolyline);
        }
        return true;
      }
    });
    polyline.setOnDragListener(new Polyline.OnDragListener() {
      @Override
      public void onDragStart(Polyline polyline) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureStartDrag(aiPolyline);
        }
      }

      @Override
      public void onDrag(Polyline polyline) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureDrag(aiPolyline);
        }
      }

      @Override
      public void onDragEnd(Polyline polyline) {
        aiPolyline.updatePoints(polyline.getPoints());
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureStopDrag(aiPolyline);
        }
      }
    });
    if (aiPolyline.Visible()) {
      showOverlay(polyline);
    } else {
      hideOverlay(polyline);
    }
  }

  private void configurePolygon(final MapFeature component, Polygon polygon) {
    featureOverlays.put(component, polygon);
    polygon.setOnClickListener(new Polygon.OnClickListener() {
      @Override
      public boolean onLongClick(Polygon arg0, MapView arg1, GeoPoint arg2) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureLongPress(component);
        }
        return true;
      }

      @Override
      public boolean onClick(Polygon arg0, MapView arg1, GeoPoint arg2) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureClick(component);
        }
        if (component.EnableInfobox()) {
          arg0.showInfoWindow(arg2);
        }
        return true;
      }
    });
    polygon.setOnDragListener(new Polygon.OnDragListener() {
      @Override
      public void onDragStart(Polygon polygon) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureStartDrag(component);
        }
      }

      @Override
      public void onDrag(Polygon polygon) {
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureDrag(component);
        }
      }

      @Override
      public void onDragEnd(Polygon polygon) {
        if (component instanceof MapCircle) {
          double latitude = 0, longitude = 0;
          int count = polygon.getPoints().size();
          // Note that this approximates the centroid of the Circle.
          for (GeoPoint p : polygon.getPoints()) {
            latitude += p.getLatitude();
            longitude += p.getLongitude();
          }
          if (count > 0) {
            ((MapCircle) component).updateCenter(latitude / count, longitude / count);
          } else {
            ((MapCircle) component).updateCenter(0, 0);
          }
        } else if (component instanceof MapRectangle) {
          double north = -90, east = -180, west = 180, south = 90;
          for (GeoPoint p : polygon.getPoints()) {
            double lat = p.getLatitude();
            double lng = p.getLongitude();
            north = Math.max(north, lat);
            south = Math.min(south, lat);
            east = Math.max(east, lng);
            west = Math.min(west, lng);
          }
          ((MapRectangle) component).updateBounds(north, west, south, east);
        } else {
          ((MapPolygon) component).updatePoints(Collections.singletonList(polygon.getPoints()));
          List<List<GeoPoint>> holes = new ArrayList<List<GeoPoint>>();
          holes.addAll(polygon.getHoles());
          ((MapPolygon) component).updateHolePoints(Collections.singletonList(holes));
        }
        for (MapEventListener listener : eventListeners) {
          listener.onFeatureStopDrag(component);
        }
      }
    });
    if (component.Visible()) {
      showOverlay(polygon);
    } else {
      hideOverlay(polygon);
    }
  }

  @Override
  public void addFeature(final MapPolygon aiPolygon) {
    configurePolygon(aiPolygon, createNativePolygon(aiPolygon));
  }

  @Override
  public void addFeature(MapCircle aiCircle) {
    configurePolygon(aiCircle, createNativeCircle(aiCircle));
  }

  @Override
  public void addFeature(MapRectangle aiRectangle) {
    configurePolygon(aiRectangle, createNativeRectangle(aiRectangle));
  }

  @Override
  public void removeFeature(MapFeature aiFeature) {
    view.getOverlayManager().remove(featureOverlays.get(aiFeature));
    featureOverlays.remove(aiFeature);
  }

  @Override
  public void updateFeaturePosition(MapMarker aiMarker) {
    Marker marker = (Marker)featureOverlays.get(aiMarker);
    if (marker != null) {
      marker.setPosition(new GeoPoint(aiMarker.Latitude(), aiMarker.Longitude()));
      view.invalidate();
    }
  }

  @Override
  public void updateFeaturePosition(MapLineString aiPolyline) {
    Polyline overlay = (Polyline) featureOverlays.get(aiPolyline);
    if (overlay != null) {
      overlay.setPoints(aiPolyline.getPoints());
      view.invalidate();
    }
  }

  @Override
  public void updateFeaturePosition(MapPolygon aiPolygon) {
    MultiPolygon polygon = (MultiPolygon) featureOverlays.get(aiPolygon);
    if (polygon != null) {
      polygon.setMultiPoints(aiPolygon.getPoints());
      polygon.setMultiHoles(aiPolygon.getHolePoints());
      view.invalidate();
    }
  }

  @Override
  public void updateFeaturePosition(MapCircle aiCircle) {
    GeoPoint center = new GeoPoint(aiCircle.Latitude(), aiCircle.Longitude());
    Polygon polygon = (Polygon) featureOverlays.get(aiCircle);
    if (polygon != null) {
      List<GeoPoint> geopoints = Polygon.pointsAsCircle(center, aiCircle.Radius());
      polygon.setPoints(geopoints);
      view.invalidate();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void updateFeaturePosition(MapRectangle aiRectangle) {
    Polygon polygon = (Polygon) featureOverlays.get(aiRectangle);
    if (polygon != null) {
      List<GeoPoint> geopoints = (List) Polygon.pointsAsRect(new BoundingBox(aiRectangle.NorthLatitude(),
          aiRectangle.EastLongitude(), aiRectangle.SouthLatitude(), aiRectangle.WestLongitude()));
      polygon.setPoints(geopoints);
      view.invalidate();
    }
  }

  @Override
  public void updateFeatureFill(final HasFill aiFeature) {
    OverlayWithIW overlay = featureOverlays.get(aiFeature);
    if (overlay == null) {
      return;  // not yet initialized
    }
    overlay.accept(new OverlayWithIWVisitor() {
      @Override
      public void visit(final Marker marker) {
        getMarkerDrawable((MapMarker) aiFeature, new AsyncCallbackPair<Drawable>() {
          @Override
          public void onFailure(String message) {
            Log.e(TAG, "Unable to update fill color for marker: " + message);
          }

          @Override
          public void onSuccess(Drawable result) {
            marker.setIcon(result);
            view.invalidate();
          }
        });
      }

      @Override
      public void visit(Polyline polyline) {
        // polylines do not have fills
      }

      @Override
      public void visit(Polygon polygon) {
        polygon.setFillColor(aiFeature.FillColor());
        view.invalidate();
      }

    });
  }

  @Override
  public void updateFeatureStroke(final HasStroke aiFeature) {
    OverlayWithIW overlay = featureOverlays.get(aiFeature);
    if (overlay == null) {
      return;  // not yet initialized
    }
    overlay.accept(new OverlayWithIWVisitor() {
      @Override
      public void visit(final Marker marker) {
        getMarkerDrawable((MapMarker) aiFeature, new AsyncCallbackPair<Drawable>() {
          @Override
          public void onFailure(String message) {
            Log.e(TAG, "Unable to update stroke color for marker: " + message);
          }

          @Override
          public void onSuccess(Drawable result) {
            marker.setIcon(result);
            view.invalidate();
          }
        });
      }

      @Override
      public void visit(Polyline polyline) {
        DisplayMetrics metrics = new DisplayMetrics();
        form.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        polyline.setColor(aiFeature.StrokeColor());
        polyline.setWidth(aiFeature.StrokeWidth() * metrics.density);
        view.invalidate();
      }

      @Override
      public void visit(Polygon polygon) {
        DisplayMetrics metrics = new DisplayMetrics();
        form.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        polygon.setStrokeColor(aiFeature.StrokeColor());
        polygon.setStrokeWidth(aiFeature.StrokeWidth() * metrics.density);
        view.invalidate();
      }
    });
  }

  @Override
  public void updateFeatureText(MapFeature aiFeature) {
    OverlayWithIW overlay = featureOverlays.get(aiFeature);
    if (overlay != null) {
      overlay.setTitle(aiFeature.Title());
      overlay.setSnippet(aiFeature.Description());
    }
  }

  @Override
  public void updateFeatureDraggable(MapFeature aiFeature) {
    OverlayWithIW overlay = featureOverlays.get(aiFeature);
    if (overlay != null) {
      overlay.setDraggable(aiFeature.Draggable());
    }
  }

  @Override
  public void updateFeatureImage(MapMarker aiMarker) {
    final Marker marker = (Marker)featureOverlays.get(aiMarker);
    if (marker == null) {
      return;  // not yet initialized
    }
    getMarkerDrawable(aiMarker, new AsyncCallbackPair<Drawable>() {
      @Override
      public void onFailure(String message) {
        Log.e(TAG, "Unable to update feature image: " + message);
      }

      @Override
      public void onSuccess(Drawable result) {
        marker.setIcon(result);
        view.invalidate();
      }
    });
  }

  @Override
  public void updateFeatureSize(MapMarker aiMarker) {
    final Marker marker = (Marker)featureOverlays.get(aiMarker);
    if (marker == null) {
      return;
    }
    getMarkerDrawable(aiMarker, new AsyncCallbackPair<Drawable>() {
      @Override
      public void onFailure(String message) {
        Log.wtf(TAG, "Cannot find default marker");
      }

      @Override
      public void onSuccess(Drawable result) {
        marker.setIcon(result);
        view.invalidate();
      }
    });
  }

  private void getMarkerDrawable(final MapMarker aiMarker,
      final AsyncCallbackPair<Drawable> callback) {
    final String assetPath = aiMarker.ImageAsset();
    if (assetPath == null || assetPath.length() == 0 || assetPath.endsWith(".svg")) {
      getMarkerDrawableVector(aiMarker, callback);
    } else {
      getMarkerDrawableRaster(aiMarker, callback);
    }
  }

  private void getMarkerDrawableVector(MapMarker aiMarker,
      AsyncCallbackPair<Drawable> callback) {
    SVG markerSvg = null;
    if (defaultMarkerSVG == null) {
      try {
        defaultMarkerSVG = SVG.getFromAsset(view.getContext().getAssets(), "marker.svg");
      } catch (SVGParseException e) {
        Log.e(TAG, "Invalid SVG in Marker asset", e);
      } catch (IOException e) {
        Log.e(TAG, "Unable to read Marker asset", e);
      }
      if (defaultMarkerSVG == null || defaultMarkerSVG.getRootElement() == null) {
        throw new IllegalStateException("Unable to load SVG from assets");
      }
    }
    final String markerAsset = aiMarker.ImageAsset();
    if (markerAsset != null && markerAsset.length() != 0) {
      try {
        markerSvg = SVG.getFromAsset(view.getContext().getAssets(), markerAsset);
      } catch (SVGParseException e) {
        Log.e(TAG, "Invalid SVG in Marker asset", e);
      } catch (IOException e) {
        Log.e(TAG, "Unable to read Marker asset", e);
      }
      if (markerSvg == null) {
        // Attempt to retrieve asset from ReplForm storage location
        InputStream is = null;
        try {
          is = MediaUtil.openMedia(form, markerAsset);
          markerSvg = SVG.getFromInputStream(is);
        } catch (SVGParseException e) {
          Log.e(TAG, "Invalid SVG in Marker asset", e);
        } catch (IOException e) {
          Log.e(TAG, "Unable to read Marker asset", e);
        } finally {
          IOUtils.closeQuietly(TAG, is);
        }
      }
    }
    if (markerSvg == null) {
      markerSvg = defaultMarkerSVG;
    }
    try {
      callback.onSuccess(rasterizeSVG(aiMarker, markerSvg));
    } catch(Exception e) {
      callback.onFailure(e.getMessage());
    }
  }

  private void getMarkerDrawableRaster(final MapMarker aiMarker,
      final AsyncCallbackPair<Drawable> callback) {
    MediaUtil.getBitmapDrawableAsync(form, aiMarker.ImageAsset(),
        new AsyncCallbackPair<BitmapDrawable>() {
      @Override
      public void onFailure(String message) {
        callback.onSuccess(getDefaultMarkerDrawable(aiMarker));
      }

      @Override
      public void onSuccess(BitmapDrawable result) {
        callback.onSuccess(result);
      }
    });
  }

  private Drawable getDefaultMarkerDrawable(MapMarker aiMarker) {
    return rasterizeSVG(aiMarker, defaultMarkerSVG);
  }

  private static float getBestGuessWidth(SVG.Svg svg) {
    if (svg.width != null) {
      return svg.width.floatValue();
    } else if (svg.viewBox != null) {
      return svg.viewBox.width;
    } else {
      return ComponentConstants.MARKER_PREFERRED_WIDTH;
    }
  }

  private static float getBestGuessHeight(SVG.Svg svg) {
    if (svg.height != null) {
      return svg.height.floatValue();
    } else if (svg.viewBox != null) {
      return svg.viewBox.height;
    } else {
      return ComponentConstants.MARKER_PREFERRED_HEIGHT;
    }
  }

  private Drawable rasterizeSVG(MapMarker aiMarker, SVG markerSvg) {
    SVG.Svg svg = markerSvg.getRootElement();
    final float density = view.getContext().getResources().getDisplayMetrics().density;
    float height = aiMarker.Height() <= 0 ? getBestGuessHeight(svg) : aiMarker.Height();
    float width = aiMarker.Width() <= 0 ? getBestGuessWidth(svg) : aiMarker.Width();
    float scaleH = height / getBestGuessHeight(svg);
    float scaleW = width / getBestGuessWidth(svg);
    float scale = (float) Math.sqrt(scaleH * scaleH + scaleW * scaleW);

    // update fill color of SVG <path>
    Paint fillPaint = new Paint();
    Paint strokePaint = new Paint();
    PaintUtil.changePaint(fillPaint, aiMarker.FillColor());
    PaintUtil.changePaint(strokePaint, aiMarker.StrokeColor());
    SVG.Length strokeWidth = new SVG.Length(aiMarker.StrokeWidth() / scale);
    for (SVG.SvgObject element : svg.getChildren()) {
      if (element instanceof SVG.SvgConditionalElement) {
        SVG.SvgConditionalElement path = (SVG.SvgConditionalElement) element;
        path.baseStyle.fill = new SVG.Colour(fillPaint.getColor());
        path.baseStyle.fillOpacity = fillPaint.getAlpha()/255.0f;
        path.baseStyle.stroke = new SVG.Colour(strokePaint.getColor());
        path.baseStyle.strokeOpacity = strokePaint.getAlpha()/255.0f;
        path.baseStyle.strokeWidth = strokeWidth;
        if (path.style != null) {
          if ((path.style.specifiedFlags & SPECIFIED_FILL) == 0) {
            path.style.fill = new SVG.Colour(fillPaint.getColor());
            path.style.specifiedFlags |= SPECIFIED_FILL;
          }
          if ((path.style.specifiedFlags & SPECIFIED_FILL_OPACITY) == 0) {
            path.style.fillOpacity = fillPaint.getAlpha()/255.0f;
            path.style.specifiedFlags |= SPECIFIED_FILL_OPACITY;
          }
          if ((path.style.specifiedFlags & SPECIFIED_STROKE) == 0) {
            path.style.stroke = new SVG.Colour(strokePaint.getColor());
            path.style.specifiedFlags |= SPECIFIED_STROKE;
          }
          if ((path.style.specifiedFlags & SPECIFIED_STROKE_OPACITY) == 0) {
            path.style.strokeOpacity = strokePaint.getAlpha()/255.0f;
            path.style.specifiedFlags |= SPECIFIED_STROKE_OPACITY;
          }
          if ((path.style.specifiedFlags & SPECIFIED_STROKE_WIDTH) == 0) {
            path.style.strokeWidth = strokeWidth;
            path.style.specifiedFlags |= SPECIFIED_STROKE_WIDTH;
          }
        }
      }
    }

    // draw SVG to Picture and create a BitmapDrawable for rendering
    Picture picture = markerSvg.renderToPicture();
    Picture scaledPicture = new Picture();
    Canvas canvas = scaledPicture.beginRecording((int)((width + 2.0f * aiMarker.StrokeWidth()) * density),
        (int)((height + 2.0f * aiMarker.StrokeWidth()) * density));
    canvas.scale(density * scaleW, density * scaleH);
    canvas.translate(strokeWidth.floatValue(), strokeWidth.floatValue());
    picture.draw(canvas);
    scaledPicture.endRecording();
    return new PictureDrawable(scaledPicture);
  }

  private void createNativeMarker(final MapMarker aiMarker,
      AsyncCallbackPair<Marker> callback) {
    final Marker osmMarker = new Marker(view);
    featureOverlays.put(aiMarker, osmMarker);
    osmMarker.setDraggable(aiMarker.Draggable());
    osmMarker.setTitle(aiMarker.Title());
    osmMarker.setSnippet(aiMarker.Description());
    osmMarker.setPosition(new GeoPoint(aiMarker.Latitude(), aiMarker.Longitude()));
    osmMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    getMarkerDrawable(aiMarker, new AsyncCallbackFacade<Drawable, Marker>(callback) {
      @Override
      public void onFailure(String message) {
        callback.onFailure(message);
      }

      @Override
      public void onSuccess(Drawable result) {
        osmMarker.setIcon(result);
        callback.onSuccess(osmMarker);
      }
    });
  }

  private Polyline createNativePolyline(final MapLineString aiLineString) {
    final Polyline osmLine = new Polyline();
    osmLine.setDraggable(aiLineString.Draggable());
    osmLine.setTitle(aiLineString.Title());
    osmLine.setSnippet(aiLineString.Description());
    osmLine.setPoints(aiLineString.getPoints());
    osmLine.setColor(aiLineString.StrokeColor());
    osmLine.setWidth(aiLineString.StrokeWidth());
    osmLine.setInfoWindow(defaultInfoWindow);
    return osmLine;
  }

  private void createPolygon(final Polygon osmPolygon, final MapFeature aiFeature) {
    osmPolygon.setDraggable(aiFeature.Draggable());
    osmPolygon.setTitle(aiFeature.Title());
    osmPolygon.setSnippet(aiFeature.Description());
    osmPolygon.setStrokeColor(((HasStroke) aiFeature).StrokeColor());
    osmPolygon.setStrokeWidth(((HasStroke) aiFeature).StrokeWidth());
    osmPolygon.setFillColor(((HasFill) aiFeature).FillColor());
    osmPolygon.setInfoWindow(defaultInfoWindow);
  }

  private MultiPolygon createNativePolygon(final MapPolygon aiPolygon) {
    final MultiPolygon osmPolygon = new MultiPolygon();
    createPolygon(osmPolygon, aiPolygon);
    osmPolygon.setMultiPoints(aiPolygon.getPoints());
    osmPolygon.setMultiHoles(aiPolygon.getHolePoints());
    return osmPolygon;
  }

  private Polygon createNativeCircle(final MapCircle aiCircle) {
    final Polygon osmPolygon = new Polygon();
    createPolygon(osmPolygon, aiCircle);
    osmPolygon.setPoints(Polygon.pointsAsCircle(new GeoPoint(aiCircle.Latitude(), aiCircle.Longitude()), aiCircle.Radius()));
    return osmPolygon;
  }

  private Polygon createNativeRectangle(final MapRectangle aiRectangle) {
    BoundingBox bbox = new BoundingBox(aiRectangle.NorthLatitude(), aiRectangle.EastLongitude(),
        aiRectangle.SouthLatitude(), aiRectangle.WestLongitude());
    final Polygon osmPolygon = new Polygon();
    createPolygon(osmPolygon, aiRectangle);
    osmPolygon.setPoints(new ArrayList<GeoPoint>((List) Polygon.pointsAsRect(bbox)));
    return osmPolygon;
  }

  @Override
  public void showFeature(MapFeature feature) {
    showOverlay(featureOverlays.get(feature));
  }

  protected void showOverlay(OverlayWithIW overlay) {
    view.getOverlayManager().add(overlay);
  }

  @Override
  public void hideFeature(MapFeature feature) {
    hideOverlay(featureOverlays.get(feature));
  }

  protected void hideOverlay(OverlayWithIW overlay) {
    view.getOverlayManager().remove(overlay);
  }

  @Override
  public boolean isFeatureVisible(MapFeature feature) {
    OverlayWithIW overlay = featureOverlays.get(feature);
    return overlay != null && view.getOverlayManager().contains(overlay);
  }

  @Override
  public void showInfobox(MapFeature feature) {
    OverlayWithIW overlay = featureOverlays.get(feature);
    overlay.showInfoWindow();
  }

  @Override
  public void hideInfobox(MapFeature feature) {
    OverlayWithIW overlay = featureOverlays.get(feature);
    overlay.closeInfoWindow();
  }

  @Override
  public boolean isInfoboxVisible(MapFeature feature) {
    OverlayWithIW overlay = featureOverlays.get(feature);
    return overlay != null && overlay.isInfoWindowOpen();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return view.getBoundingBox();
  }

  @Override
  public void setBoundingBox(BoundingBox bbox) {
    view.getController().setCenter(bbox.getCenter());
    view.getController().zoomToSpan(bbox.getLatitudeSpan(), bbox.getLongitudeSpan());
  }

  @Override
  public boolean onScroll(ScrollEvent event) {
    for (MapEventListener listener : eventListeners) {
      listener.onBoundsChanged();
    }
    return true;
  }

  @Override
  public boolean onZoom(ZoomEvent event) {
    zoomControls.updateButtons();
    for (MapEventListener listener : eventListeners) {
      listener.onZoom();
    }
    return true;
  }

  @Override
  public LocationSensor.LocationSensorListener getLocationListener() {
    return locationProvider;
  }

  @Override
  public int getOverlayCount() {
    System.err.println(view.getOverlays());
    return view.getOverlays().size();
  }

  @Override
  public void setRotation(float Rotation) {
    view.setMapOrientation(Rotation);
  }

  @Override
  public float getRotation() {
    return view.getMapOrientation();
  }

  @Override
  public void setScaleVisible(boolean show) {
    scaleBar.setEnabled(show);
    view.invalidate();
  }

  @Override
  public boolean isScaleVisible() {
    return scaleBar.isEnabled();
  }

  @Override
  public void setScaleUnits(MapScaleUnits units) {
    switch (units) {
      case METRIC:
        scaleBar.setUnitsOfMeasure(UnitsOfMeasure.metric);
        break;
      case IMPERIAL:
        scaleBar.setUnitsOfMeasure(UnitsOfMeasure.imperial);
        break;
      default:
        throw new IllegalArgumentException("Unallowable unit system: " + units);
    }
    view.invalidate();
  }

  @Override
  public MapScaleUnits getScaleUnits() {
    switch (scaleBar.getUnitsOfMeasure()) {
      case imperial:
        return MapScaleUnits.IMPERIAL;
      case metric:
        return MapScaleUnits.METRIC;
      default:
        throw new IllegalStateException("Somehow we have an unallowed unit system");
    }
  }

  static class MultiPolygon extends Polygon {

    private List<Polygon> children = new ArrayList<Polygon>();
    private boolean draggable;
    private OnClickListener clickListener;
    private OnDragListener dragListener;

    @Override
    public void showInfoWindow() {
      if (children.size() > 0) {
        children.get(0).showInfoWindow();
      }
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean b) {
      for (Polygon child : children) {
        child.draw(canvas, mapView, b);
      }
    }

    public void setMultiPoints(List<List<GeoPoint>> points) {
      Iterator<Polygon> polygonIterator = children.iterator();
      Iterator<List<GeoPoint>> pointIterator = points.iterator();
      while (polygonIterator.hasNext() && pointIterator.hasNext()) {
        polygonIterator.next().setPoints(pointIterator.next());
      }
      while (polygonIterator.hasNext()) {
        polygonIterator.next();
        polygonIterator.remove();
      }
      while (pointIterator.hasNext()) {
        Polygon p = new Polygon();
        p.setPoints(pointIterator.next());
        p.setStrokeColor(getStrokeColor());
        p.setFillColor(getFillColor());
        p.setStrokeWidth(getStrokeWidth());
        p.setInfoWindow(getInfoWindow());
        p.setDraggable(draggable);
        p.setOnClickListener(clickListener);
        p.setOnDragListener(dragListener);
        children.add(p);
      }
    }

    public void setMultiHoles(List<List<List<GeoPoint>>> holes) {
      if (holes == null || holes.isEmpty()) {
        for (Polygon child : children) {
          child.setHoles(Collections.<List<GeoPoint>>emptyList());
        }
      } else if (holes.size() != children.size()) {
        throw new IllegalArgumentException("Holes and points are not of the same arity.");
      } else {
        Iterator<Polygon> polygonIterator = children.iterator();
        Iterator<List<List<GeoPoint>>> holeIterator = holes.iterator();
        while (polygonIterator.hasNext() && holeIterator.hasNext()) {
          polygonIterator.next().setHoles(holeIterator.next());
        }
      }
    }

    @Override
    public void setDraggable(boolean draggable) {
      super.setDraggable(draggable);
      this.draggable = draggable;
      for (Polygon child : children) {
        child.setDraggable(draggable);
      }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
      super.setOnClickListener(listener);
      clickListener = listener;
      for (Polygon child : children) {
        child.setOnClickListener(listener);
      }
    }

    @Override
    public void setOnDragListener(OnDragListener listener) {
      super.setOnDragListener(listener);
      dragListener = listener;
      for (Polygon child : children) {
        child.setOnDragListener(listener);
      }
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
      super.setStrokeWidth(strokeWidth);
      for (Polygon child : children) {
        child.setStrokeWidth(strokeWidth);
      }
    }

    @Override
    public void setStrokeColor(int strokeColor) {
      super.setStrokeColor(strokeColor);
      for (Polygon child : children) {
        child.setStrokeColor(strokeColor);
      }
    }

    @Override
    public void setFillColor(int fillColor) {
      super.setFillColor(fillColor);
      for (Polygon child : children) {
        child.setFillColor(fillColor);
      }
    }

    @Override
    public void setTitle(String title) {
      super.setTitle(title);
      for (Polygon child : children) {
        child.setTitle(title);
      }
    }

    @Override
    public void setSnippet(String snippet) {
      super.setSnippet(snippet);
      for (Polygon child : children) {
        child.setSnippet(snippet);
      }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
      for (Polygon child : children) {
        if (child.onSingleTapConfirmed(event, mapView)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean contains(MotionEvent event) {
      for (Polygon child : children) {
        if (child.contains(event)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean onLongPress(MotionEvent event, MapView mapView) {
      boolean touched = contains(event);
      if (touched){
        if (mDraggable){
          mIsDragged = true;
          closeInfoWindow();
          mDragStartPoint = event;
          if (mOnDragListener != null) {
            mOnDragListener.onDragStart( this );
          }
          moveToEventPosition(event, mDragStartPoint, mapView);
        } else if (mOnClickListener != null) {
          mOnClickListener.onLongClick( this, mapView,
              (GeoPoint) mapView.getProjection().fromPixels( (int) event.getX(),
                  (int) event.getY() ) );
        }
      }
      return touched;
    }

    @Override
    public void moveToEventPosition(final MotionEvent event, final MotionEvent start,
        final MapView view) {
      for (Polygon child : children) {
        child.moveToEventPosition(event, start, view);
      }
    }

    @Override
    public void finishMove(final MotionEvent start, final MotionEvent end, final MapView view) {
      for (Polygon child : children) {
        child.finishMove(start, end, view);
      }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
      if (mDraggable && mIsDragged){
        if (event.getAction() == MotionEvent.ACTION_UP) {
          mIsDragged = false;
          finishMove(mDragStartPoint, event, mapView);
          if (mOnDragListener != null) {
            mOnDragListener.onDragEnd( this );
          }
          return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
          moveToEventPosition( event, mDragStartPoint, mapView );
          if (mOnDragListener != null) {
            mOnDragListener.onDrag( this );
          }
          return true;
        }
      }
      return false;
    }
  }
}
