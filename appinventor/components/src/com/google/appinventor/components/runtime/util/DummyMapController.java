// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.LocationSensor;
import com.google.appinventor.components.runtime.util.MapFactory.MapScaleUnits;
import org.osmdroid.util.BoundingBox;

import com.google.appinventor.components.runtime.util.MapFactory.HasFill;
import com.google.appinventor.components.runtime.util.MapFactory.HasStroke;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapController;
import com.google.appinventor.components.runtime.util.MapFactory.MapEventListener;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapType;

import android.view.View;

class DummyMapController implements MapController {

  public View getView() {
    throw new UnsupportedOperationException();
  }

  public double getLatitude() {
    throw new UnsupportedOperationException();
  }

  public double getLongitude() {
    throw new UnsupportedOperationException();
  }

  public void setCenter(double latitude, double longitude) {
    throw new UnsupportedOperationException();
  }

  public void setZoom(int zoom) {
    throw new UnsupportedOperationException();
  }

  public void setRotation(float Rotation) {
    throw new UnsupportedOperationException();
  }

  public float getRotation() {
    throw new UnsupportedOperationException();
  }

  public int getZoom() {
    throw new UnsupportedOperationException();
  }

  public void setMapType(MapType type) {
    throw new UnsupportedOperationException();
  }

  public MapType getMapType() {
    throw new UnsupportedOperationException();
  }

  public void setCompassEnabled(boolean enabled) {
    throw new UnsupportedOperationException();
  }

  public boolean isCompassEnabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setZoomEnabled(boolean enabled) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isZoomEnabled() {
    throw new UnsupportedOperationException();
  }

  public void setZoomControlEnabled(boolean enabled) {
    throw new UnsupportedOperationException();
  }

  public boolean isZoomControlEnabled() {
    throw new UnsupportedOperationException();
  }

  public void setShowUserEnabled(boolean enable) {
    throw new UnsupportedOperationException();
  }

  public boolean isShowUserEnabled() {
    throw new UnsupportedOperationException();
  }

  public void setRotationEnabled(boolean enable) {
    throw new UnsupportedOperationException();
  }

  public boolean isRotationEnabled() {
    throw new UnsupportedOperationException();
  }

  public void setPanEnabled(boolean enable) {
    throw new UnsupportedOperationException();
  }

  public boolean isPanEnabled() {
    throw new UnsupportedOperationException();
  }

  public void panTo(double latitude, double longitude, int zoom, double seconds) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addEventListener(MapEventListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addFeature(MapMarker marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeaturePosition(MapMarker marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeatureFill(HasFill marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeatureImage(MapMarker marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeatureText(MapFeature marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeatureDraggable(MapFeature marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BoundingBox getBoundingBox() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBoundingBox(BoundingBox bbox) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addFeature(MapLineString polyline) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addFeature(MapPolygon polygon) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addFeature(MapCircle circle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addFeature(MapRectangle circle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeFeature(MapFeature feature) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void showFeature(MapFeature feature) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void hideFeature(MapFeature feature) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFeatureVisible(MapFeature feature) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void showInfobox(MapFeature feature) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void hideInfobox(MapFeature feature) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInfoboxVisible(MapFeature feature) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeaturePosition(MapLineString polyline) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeaturePosition(MapPolygon polygon) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeaturePosition(MapCircle circle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeaturePosition(MapRectangle rectangle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeatureStroke(HasStroke marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateFeatureSize(MapMarker marker) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocationSensor.LocationSensorListener getLocationListener() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getOverlayCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setScaleVisible(boolean show) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isScaleVisible() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setScaleUnits(MapScaleUnits units) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MapScaleUnits getScaleUnits() {
    throw new UnsupportedOperationException();
  }
}
