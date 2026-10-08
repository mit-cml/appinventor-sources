// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import org.junit.Test;
import org.robolectric.shadows.ShadowLocationManager;

/**
 * Tests for the {@link LocationSensor} component.
 */
public class LocationSensorTest extends RobolectricTestBase {

  // A fixed instant in the past, as the OS reports for a cached location.
  private static final long FIX_TIME = 1700000000000L;

  private LocationSensor sensor;
  private ShadowLocationManager shadowLocationManager;

  @Override
  public void setUp() {
    super.setUp();
    shadowOf(getForm()).grantPermissions(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION);
    LocationManager locationManager =
        (LocationManager) getForm().getSystemService(Context.LOCATION_SERVICE);
    shadowLocationManager = shadowOf(locationManager);
    shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
    sensor = new LocationSensor(getForm());
    sensor.ProviderLocked(true);
    sensor.ProviderName(LocationManager.GPS_PROVIDER);
    sensor.TimeInterval(0);
    sensor.DistanceInterval(0);
    sensor.Initialize();
    runAllEvents();
  }

  @Test
  public void testLastTimestampBeforeFirstFix() {
    assertEquals(0, sensor.LastTimestamp());
  }

  @Test
  public void testLastTimestampIsTimeOfFix() {
    simulateFix(42.36, -71.09, FIX_TIME);
    ShadowEventDispatcher.assertEventFiredAny(sensor, "LocationChanged");
    assertEquals(FIX_TIME, sensor.LastTimestamp());
  }

  @Test
  public void testLastTimestampUpdatesWithNewFix() {
    simulateFix(42.36, -71.09, FIX_TIME);
    simulateFix(42.37, -71.10, FIX_TIME + 5000);
    assertEquals(FIX_TIME + 5000, sensor.LastTimestamp());
  }

  private void simulateFix(double latitude, double longitude, long time) {
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    location.setTime(time);
    shadowLocationManager.simulateLocation(location);
    runAllEvents();
  }
}
