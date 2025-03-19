// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.BulkPermissionRequest;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.Manifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Non-visible component providing location information, including {@link #Latitude()},
 * {@link #Longitude()}, {@link #Altitude()} (if supported by the device), speed (if supported by
 * the device), and address. This can also perform "geocoding", converting a given address (not
 * necessarily the current one) to a latitude (with the {@link #LatitudeFromAddress(String)}
 * method) and a longitude (with the {@link #LongitudeFromAddress(String)} method).
 *
 * In order to function, the component must have its {@link #Enabled(boolean)} property set to
 * `true`{:.logic.block}, and the device must have location sensing enabled through wireless
 * networks or GPS satellites (if outdoors).
 *
 * Location information might not be immediately available when an app starts. You'll have to wait
 * a short time for a location provider to be found and used, or wait for the
 * {@link #LocationChanged(double, double, double, float)} event.
 *
 * The emulator does not emulate sensors on all devices. Code should be tested on a physical device.
 */
@DesignerComponent(version = YaVersion.LOCATIONSENSOR_COMPONENT_VERSION,
    description = "Non-visible component providing location information, " +
    "including longitude, latitude, altitude (if supported by the device), " +
    "speed (if supported by the device), " +
    "and address.  This can also perform \"geocoding\", converting a given " +
    "address (not necessarily the current one) to a latitude (with the " +
    "<code>LatitudeFromAddress</code> method) and a longitude (with the " +
    "<code>LongitudeFromAddress</code> method).</p>\n" +
    "<p>In order to function, the component must have its " +
    "<code>Enabled</code> property set to True, and the device must have " +
    "location sensing enabled through wireless networks or GPS " +
    "satellites (if outdoors).</p>\n" +
    "Location information might not be immediately available when an app starts.  You'll have to wait a short time for " +
    "a location provider to be found and used, or wait for the LocationChanged event",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/locationSensor.png")
@SimpleObject
@UsesPermissions(permissionNames =
                 "android.permission.ACCESS_FINE_LOCATION," +
                 "android.permission.ACCESS_COARSE_LOCATION," +
                 "android.permission.ACCESS_MOCK_LOCATION," +
                 "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS")
public class LocationSensor extends AndroidNonvisibleComponent
    implements Component, OnStopListener, OnResumeListener, Deleteable,
    RealTimeDataSource<String, Float> {

  // Set of observers
  private Set<DataSourceChangeListener> dataSourceObservers = new HashSet<>();

  public interface LocationSensorListener extends LocationListener {
    void onTimeIntervalChanged(int time);
    void onDistanceIntervalChanged(int distance);
    void setSource(LocationSensor provider);
  }

  /**
   * Class that listens for changes in location, raises appropriate events,
   * and provides properties.
   *
   */
  private class MyLocationListener implements LocationListener {
    @Override
    // This sets fields longitude, latitude, altitude, hasLocationData, and
    // hasAltitude, then calls LocationSensor.LocationChanged(), all in the
    // enclosing class LocationSensor.
    public void onLocationChanged(final Location location) {
      lastLocation = location;
      longitude = location.getLongitude();
      latitude = location.getLatitude();
      speed = location.getSpeed();
      // If the current location doesn't have altitude information, the prior
      // altitude reading is retained.
      if (location.hasAltitude()) {
        hasAltitude = true;
        altitude = location.getAltitude();
      }

      // By default Location.latitude == Location.longitude == 0.
      // So we want to ignore that case rather than generating a changed event.
      if (longitude != UNKNOWN_VALUE || latitude != UNKNOWN_VALUE) {
        hasLocationData = true;
        final double argLatitude = latitude;
        final double argLongitude = longitude;
        final double argAltitude = altitude;
        final float argSpeed = speed;
        androidUIHandler.post(new Runnable() {
            @Override
            public void run() {
              LocationChanged(argLatitude, argLongitude, argAltitude, argSpeed);
              for (LocationSensorListener listener : listeners) {
                listener.onLocationChanged(location);
              }
            }
          });
      }
    }

    @Override
    public void onProviderDisabled(String provider) {
      StatusChanged(provider, "Disabled");
      stopListening();
      if (enabled) {
        RefreshProvider("onProviderDisabled");
      }
    }

    @Override
    public void onProviderEnabled(String provider) {
      StatusChanged(provider, "Enabled");
      RefreshProvider("onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      switch (status) {
        // Ignore TEMPORARILY_UNAVAILABLE, because service usually returns quickly.
        case LocationProvider.TEMPORARILY_UNAVAILABLE:
          StatusChanged(provider, "TEMPORARILY_UNAVAILABLE");
          break;
        case LocationProvider.OUT_OF_SERVICE:
          // If the provider we were listening to is no longer available,
          // find another.
          StatusChanged(provider, "OUT_OF_SERVICE");

          if (provider.equals(providerName)) {
            stopListening();
            RefreshProvider("onStatusChanged");
          }
          break;
        case LocationProvider.AVAILABLE:
          // If another provider becomes available and is one we hadn't known
          // about see if it is better than the one we're currently using.
          StatusChanged(provider, "AVAILABLE");
          if (!provider.equals(providerName) &&
              !allProviders.contains(provider)) {
            RefreshProvider("onStatusChanged");
          }
          break;
      }
    }
  }

  /**
   * Constant returned by {@link #Longitude()}, {@link #Latitude()}, and
   * {@link #Altitude()} if no value could be obtained for them.  The client
   * can find this out directly by calling {@link #HasLongitudeLatitude()} or
   * {@link #HasAltitude()}.
   */
  public static final int UNKNOWN_VALUE = 0;

  // These variables contain information related to the LocationProvider.
  private final Criteria locationCriteria;
  private final Handler handler;
  private final LocationManager locationManager;

  private final Set<LocationSensorListener> listeners = new HashSet<LocationSensorListener>();

  private boolean providerLocked = false; // if true we can't change providerName
  private String providerName;
  // Invariant: providerLocked => providerName is non-empty

  private boolean initialized = false;

  private int timeInterval;
  private int distanceInterval;

  private MyLocationListener myLocationListener;

  private LocationProvider locationProvider;
  private boolean listening = false;
    // Invariant: listening <=> a myLocationListener is registered with locationManager
    // Invariant: !listening <=> locationProvider == null

  //This holds all the providers available when we last chose providerName.
  //The reported best provider is first, possibly duplicated.
  private List<String> allProviders;

  // These location-related values are set in MyLocationListener.onLocationChanged().
  private Location lastLocation;
  private double longitude = UNKNOWN_VALUE;
  private double latitude = UNKNOWN_VALUE;
  private double altitude = UNKNOWN_VALUE;
  private float speed = UNKNOWN_VALUE;
  private boolean hasLocationData = false;
  private boolean hasAltitude = false;

  // For posting events on the UI thread
  private final Handler androidUIHandler = new Handler();

  // This is used in reverse geocoding.
  private Geocoder geocoder;

  // User-settable properties
  private boolean enabled = true;  // the default value is true

  private boolean havePermission = false; // Do we have the necessary permission
  private static final String LOG_TAG = LocationSensor.class.getSimpleName();

  /**
   * Creates a new LocationSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public LocationSensor(ComponentContainer container) {
    this(container, true);
  }

  /**
   * Creates a new LocationSensor component with a default state of <code>enabled</code>.
   *
   * @param container  ignored (because this is a non-visible component)
   * @param enabled  true if the LocationSensor is enabled by default, otherwise false.
   */
  public LocationSensor(ComponentContainer container, boolean enabled) {
    super(container.$form());
    this.enabled = enabled;
    handler = new Handler();
    // Set up listener
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    // Initialize sensor properties (60 seconds; 5 meters)
    timeInterval = 60000;
    distanceInterval = 5;

    // Initialize location-related fields
    Context context = container.$context();
    geocoder = new Geocoder(context);
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    locationCriteria = new Criteria();
    myLocationListener = new MyLocationListener();
    allProviders = new ArrayList<String>();
    // Do some initialization depending on the initial enabled state
    Enabled(enabled);
  }

  @SuppressWarnings({"unused"})  // Called from Scheme
  public void Initialize() {
    initialized = true;
    Enabled(enabled);
  }

  // Events

  /**
   * Indicates that a new location has been detected. Speed is reported in meters/second
   * Other values match their properties.
   */
  @SimpleEvent(description = "Indicates that a new location has been detected.")
  public void LocationChanged(double latitude, double longitude, double altitude, float speed) {
    notifyDataObservers("latitude", latitude);
    notifyDataObservers("longitude", longitude);
    notifyDataObservers("altitude", altitude);
    notifyDataObservers("speed", speed);

    EventDispatcher.dispatchEvent(this, "LocationChanged", latitude, longitude, altitude, speed);
  }

  /**
   * Indicates that the status of the location provider service has changed, such as when a
   * provider is lost or a new provider starts being used.
   */
  @SimpleEvent
  public void StatusChanged(String provider, String status) {
    if (enabled) {
      EventDispatcher.dispatchEvent(this, "StatusChanged", provider, status);
    }
  }

  // Properties

  /**
   * Indicates the source of the location information.  If there is no provider, the
   * string "NO PROVIDER" is returned.  This is useful primarily for debugging.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String ProviderName() {
    if (providerName == null) {
      return "NO PROVIDER";
    } else {
      return providerName;
    }
  }

  /**
   * The current service provider. The provider will most likely be either GPS or network.
   *
   * @internaldoc
   * Change the location provider.
   * If the blocks program changes the name, try to change the provider.
   * Whatever happens now, the provider and the reported name may be switched to
   * Android's preferred provider later. This is primarily for debugging.
   */
  @SimpleProperty
  public void ProviderName(String providerName) {
    this.providerName = providerName;
    if (!empty(providerName) && startProvider(providerName)) {
      return;
    } else {
      RefreshProvider("ProviderName");
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean ProviderLocked() {
    return providerLocked;
  }

  /**
   * The device will not change the service provider.
   *
   *   It is possible for a device to switch service providers when the current provider is unable
   * to provide adequate location information. `ProviderLocked` is a Boolean value: true/false.
   * Set to `true`{:.logic.block} to prevent providers from changing. Set to `false`{:.logic.block}
   * to allow for automatic switching when necessary.
   *
   * @internaldoc
   * Indicates whether the sensor should allow the developer to
   * manually change the provider (GPS, GSM, Wifi, etc.)
   * from which location updates are received.
   */
  @SimpleProperty
  public void ProviderLocked(boolean lock) {
      providerLocked = lock;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SENSOR_TIME_INTERVAL,
      defaultValue = "60000")
  @SimpleProperty
  public void TimeInterval(int interval) {

      // make sure that the provided value is a valid one.
      // choose 1000000 miliseconds to be the upper limit
      if (interval < 0 || interval > 1000000)
          return;

      timeInterval = interval;

      // restart listening for location updates, using the new time interval
      if (enabled) {
          RefreshProvider("TimeInterval");
      }

      for (LocationSensorListener listener : listeners) {
        listener.onTimeIntervalChanged(timeInterval);
      }
  }

  /**
   * Determines the minimum time interval, in milliseconds, that the sensor will try to use for
   * sending out location updates. However, location updates will only be received when the
   * location of the phone actually changes, and use of the specified time interval is not
   * guaranteed. For example, if 30000 is used as the time interval, location updates will never
   * be fired sooner than 30000ms, but they may be fired anytime after.
   *
   *   Values smaller than 30000ms (30 seconds) are not practical for most devices. Small values
   * may drain battery and overwork the GPS.
   */
  @SimpleProperty(
      description = "Determines the minimum time interval, in milliseconds, that the sensor will try " +
          "to use for sending out location updates. However, location updates will only be received " +
          "when the location of the phone actually changes, and use of the specified time interval " +
          "is not guaranteed. For example, if 1000 is used as the time interval, location updates will " +
          "never be fired sooner than 1000ms, but they may be fired anytime after.",
          category = PropertyCategory.BEHAVIOR)
  public int TimeInterval() {
      return timeInterval;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SENSOR_DIST_INTERVAL,
      defaultValue = "5")
  @SimpleProperty
  public void DistanceInterval(int interval) {

      // make sure that the provided value is a valid one.
      // choose 1000 meters to be the upper limit
      if (interval < 0 || interval > 1000)
          return;

      distanceInterval = interval;

      // restart listening for location updates, using the new distance interval
      if (enabled) {
          RefreshProvider("DistanceInterval");
      }

      for (LocationSensorListener listener : listeners) {
        listener.onDistanceIntervalChanged(distanceInterval);
      }
  }

  /**
   * Determines the minimum distance interval, in meters, that the sensor will try to use for
   * sending out location updates. For example, if this is set to 50, then the sensor will fire a
   * {@link #LocationChanged(double, double, double, float)} event only after 50 meters have been
   * traversed. However, the sensor does not guarantee that an update will be received at exactly
   * the distance interval. It may take more than 5 meters to fire an event, for instance.
   *
   *   It is also useful to check against {@link #Accuracy()} when using this property. When your
   * device is moving, the accuracy of the detected location is constantly changing.
   */
  @SimpleProperty(
      description = "Determines the minimum distance interval, in meters, that the sensor will try " +
      "to use for sending out location updates. For example, if this is set to 5, then the sensor will " +
      "fire a LocationChanged event only after 5 meters have been traversed. However, the sensor does " +
      "not guarantee that an update will be received at exactly the distance interval. It may take more " +
      "than 5 meters to fire an event, for instance.",
      category = PropertyCategory.BEHAVIOR)
  public int DistanceInterval() {
      return distanceInterval;
  }

  /**
   * If `true`{:.logic.block}, the device can report longitude and latitude.  It is
   * always the case that either both or neither are.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean HasLongitudeLatitude() {
    return hasLocationData && enabled;
  }

  /**
   * If `true`{:.logic.block}, the device can report its altitude.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean HasAltitude() {
    return hasAltitude && enabled;
  }

  /**
   * If `true`{:.logic.block}, the device can report its accuracy level.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean HasAccuracy() {
    return Accuracy() != UNKNOWN_VALUE && enabled;
  }

  /**
   * The most recent available longitude value in degrees reported to 5 decimal places.
   * If no value is available, 0 will be returned.
   * Longitude is a value between 180 (east) and -180 (west), where 0 marks the Prime Meridian.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public double Longitude() {
    return longitude;
  }

  /**
   * The most recently available latitude value in degrees reported to 5 decimal places.
   * If no value is available, 0 will be returned.
   * Latitude is a value between 90 (north) and -90 (south), where 0 marks the Equator.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public double Latitude() {
      return latitude;
  }

  /**
   * Altitude of the device measured in meters, if available.
   *
   *   Altitude is measured from the
   * [World Geodetic System 84 reference ellipsoid](https://gisgeography.com/wgs84-world-geodetic-system/),
   * not sea level.
   *
   *   Note that it is difficult for devices to accurately sense altitude. Altitude reported on a
   * phone/tablet can easily be off by 30 meters or more.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The most recently available altitude value, in meters.  If no value is "
          + "available, 0 will be returned.")
  public double Altitude() {
    return altitude;
  }

  /**
   * The `LocationSensor` will be able to locate the device with a varying degree of confidence,
   * based on the quality of satellite, cell towers, and other data used to estimate location.
   * The `Accuracy` value is the radius in meters around the sensor's detected location. The device
   * has a 68% chance to be located within this radius. More precise location detection will result
   * in a smaller accuracy number, which allows the app to have more confidence where the device
   * is actually located.
   *
   *   If the accuracy is not known, the return value is 0.0
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The most recent measure of accuracy, in meters.  If no value is available, "
          + "0 will be returned.")
  public double Accuracy() {
    if (lastLocation != null && lastLocation.hasAccuracy()) {
      return lastLocation.getAccuracy();
    } else if (locationProvider != null) {
      return locationProvider.getAccuracy();
    } else {
      return UNKNOWN_VALUE;
    }
  }

  /**
   * Indicates whether the user has specified that the sensor should
   * listen for location changes and raise the corresponding events.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
  }

  /**
   * If `true`{:.logic.block}, the `LocationSensor` will attempt to read location information from
   * GPS, WiFi location, or other means available on the device. This setting does not control
   * whether location information is actually available. Device location must be enabled or
   * disabled in the device settings.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    this.enabled = enabled;
    if (!initialized) {
      return;
    }
    if (!enabled) {
      stopListening();
    } else {
      RefreshProvider("Enabled");
    }
  }

  /**
   * Physical street address of the device from Google's map database.
   *
   *   The address might not always be available from the provider, and the address reported may not
   * always be of the building where the device is located.
   *
   *   If Google has no address information available for a particular location, this will return
   * `No address available`.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Provides a textual representation of the current address or \"No address "
          + "available\".")
  public String CurrentAddress() {
    if (hasLocationData &&
        latitude <= 90 && latitude >= -90 &&
        longitude <= 180 || longitude >= -180) {
      try {
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if (addresses != null && addresses.size() == 1) {
          Address address = addresses.get(0);
          if (address != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
              sb.append(address.getAddressLine(i));
              sb.append("\n");
            }
            return sb.toString();
          }
        }

      } catch (Exception e) {
        // getFromLocation can throw an IOException or an IllegalArgumentException
        // a bad result can give an indexOutOfBoundsException
        // are there others?
        if (e instanceof IllegalArgumentException
            || e instanceof IOException
            || e instanceof IndexOutOfBoundsException ) {
          Log.e(LOG_TAG, "Exception thrown by getting current address " + e.getMessage());
        } else {
          // what other exceptions can happen here?
          Log.e(LOG_TAG,
              "Unexpected exception thrown by getting current address " + e.getMessage());
        }
      }
    }
    return "No address available";
  }

  /**
   * Derives latitude from the given `locationName`.
   *
   * @param locationName  human-readable address
   *
   * @return latitude in degrees, 0 if not found.
   */
  @SimpleFunction(description = "Derives latitude of given address")
  public double LatitudeFromAddress(String locationName) {
    try {
      List<Address> addressObjs = geocoder.getFromLocationName(locationName, 1);
      Log.i(LOG_TAG, "latitude addressObjs size is " + addressObjs.size() + " for " + locationName);
      if ( (addressObjs == null) || (addressObjs.size() == 0) ){
        throw new IOException("");
      }
      return addressObjs.get(0).getLatitude();
    } catch (IOException e) {
      form.dispatchErrorOccurredEvent(this, "LatitudeFromAddress",
          ErrorMessages.ERROR_LOCATION_SENSOR_LATITUDE_NOT_FOUND, locationName);
      return 0;
    }
  }

  /**
   * Derives longitude from the given `locationName`.
   *
   * @param locationName  human-readable address
   *
   * @return longitude in degrees, 0 if not found.
   */
  @SimpleFunction(description = "Derives longitude of given address")
  public double LongitudeFromAddress(String locationName) {
    try {
      List<Address> addressObjs = geocoder.getFromLocationName(locationName, 1);
      Log.i(LOG_TAG, "longitude addressObjs size is " + addressObjs.size() + " for " + locationName);
      if ( (addressObjs == null) || (addressObjs.size() == 0) ){
        throw new IOException("");
      }
      return addressObjs.get(0).getLongitude();
    } catch (IOException e) {
      form.dispatchErrorOccurredEvent(this, "LongitudeFromAddress",
          ErrorMessages.ERROR_LOCATION_SENSOR_LONGITUDE_NOT_FOUND, locationName);
      return 0;
    }
  }

  /**
   * List of available service providers, such as gps or network. This information is provided
   * as a list and in text form.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public List<String> AvailableProviders () {
    return allProviders;
  }

  // Methods to stop and start listening to LocationProviders

  /**
   * Refresh provider attempts to choose and start the best provider unless
   * someone has set and locked the provider. Currently, blocks programmers
   * cannot do that because the relevant methods are not declared as properties.
   *
   */

  // @SimpleFunction(description = "Find and start listening to a location provider.")
  public void RefreshProvider(final String caller) {
    if (!initialized) return;    // Not yet ready to start...
    stopListening();             // In case another provider is active.
    final LocationSensor me = this;
    if (!havePermission) {
      // Make sure we do this on the UI thread
      androidUIHandler.post(new Runnable() {
          @Override
          public void run() {
            me.form.askPermission(new BulkPermissionRequest(me, "RefreshProvider", Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION) {
                @Override
                public void onGranted() {
                  me.havePermission = true;
                  me.RefreshProvider(caller);
                  Log.d(LOG_TAG, "Permission Granted");
                }
              });
          }
        });
    }

    if (providerLocked && !empty(providerName)) {
      listening = startProvider(providerName);
      return;
    }
    allProviders = locationManager.getProviders(true);  // Typically it's ("network" "gps")
    String bProviderName = locationManager.getBestProvider(locationCriteria, true);
    if (bProviderName != null && !bProviderName.equals(allProviders.get(0))) {
      allProviders.add(0, bProviderName);
    }
    // We'll now try the best first and stop as soon as one successfully starts.
    for (String providerN : allProviders) {
      listening = startProvider(providerN);
      if (listening) {
        if (!providerLocked) {
          providerName = providerN;
        }
        return;
      }
    }
  }

  /* Start listening to ProviderName.
   * Return true iff successful.
   */
  private boolean startProvider(final String providerName) {
    this.providerName = providerName;
    LocationProvider tLocationProvider = locationManager.getProvider(providerName);
    if (tLocationProvider == null) {
      Log.d(LOG_TAG, "getProvider(" + providerName + ") returned null");
      return false;
    }
    stopListening();
    locationProvider = tLocationProvider;
    locationManager.requestLocationUpdates(providerName, timeInterval,
          distanceInterval, myLocationListener);
    listening = true;
    return true;
  }

  /**
   * This unregisters {@link #myLocationListener} as a listener to location
   * updates.  It is safe to call this even if no listener had been registered,
   * in which case it has no effect.  This also sets the value of
   * {@link #locationProvider} to {@code null} and sets {@link #listening}
   * to {@code false}.
   */
  private void stopListening() {
    if (listening) {
      locationManager.removeUpdates(myLocationListener);
      locationProvider = null;
      listening = false;
    }
  }


  // OnResumeListener implementation

  @Override
  public void onResume() {
    if (enabled) {
      RefreshProvider("onResume");
    }
  }

  // OnStopListener implementation

  @Override
  public void onStop() {
    stopListening();
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    stopListening();
  }

  public void addListener(LocationSensorListener listener) {
    listener.setSource(this);
    listeners.add(listener);
  }

  public void removeListener(LocationSensorListener listener) {
    listeners.remove(listener);
    listener.setSource(null);
  }

  private boolean empty(String s) {
    return s == null || s.length() == 0;
  }

  @Override
  public void addDataObserver(DataSourceChangeListener dataComponent) {
    dataSourceObservers.add(dataComponent);
  }

  @Override
  public void removeDataObserver(DataSourceChangeListener dataComponent) {
    dataSourceObservers.remove(dataComponent);
  }

  @Override
  public void notifyDataObservers(String key, Object value) {
    // Notify each Chart Data observer component of the Data value change
    for (DataSourceChangeListener dataComponent : dataSourceObservers) {
      dataComponent.onReceiveValue(this, key, value);
    }
  }

  /**
   * Returns a data value for a given key. Possible keys include:
   * <ul>
   *   <li>latitude  - latitude value</li>
   *   <li>longitude - longitude value</li>
   *   <li>altitude  - altitude value</li>
   *   <li>speed     - speed value</li>
   * </ul>
   *
   * @param key identifier of the value
   * @return    Value corresponding to the key, or 0 if key is undefined.
   */
  @Override
  public Float getDataValue(String key) {
    switch (key) {
      case "latitude":
        return (float) latitude;

      case "longitude":
        return (float )longitude;

      case "altitude":
        return (float) altitude;

      case "speed":
        return speed;

      default:
        return 0f;
    }
  }
}
