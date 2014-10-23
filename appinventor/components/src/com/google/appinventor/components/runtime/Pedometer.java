// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
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

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * This component keeps count of steps using the accelerometer.
 *
 */
@DesignerComponent(version = YaVersion.PEDOMETER_COMPONENT_VERSION,
                   description = "Component that can count steps.",
                   category = ComponentCategory.INTERNAL,
                   nonVisible = true,
                   iconName = "images/pedometer.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.ACCESS_FINE_LOCATION")
public class Pedometer extends AndroidNonvisibleComponent
    implements Component, LocationListener, SensorEventListener, Deleteable {
  private static final String TAG = "Pedometer";
  private static final String PREFS_NAME = "PedometerPrefs";

  private static final int DIMENSIONS = 3;
  private static final int INTERVAL_VARIATION = 250;
  private static final int NUM_INTERVALS = 2;
  private static final int WIN_SIZE = 20;
  private static final int MIN_SATELLITES = 4;
  private static final float STRIDE_LENGTH = (float) 0.73;
  private static final float PEAK_VALLEY_RANGE = (float) 4.0;

  private final Context context;;
  private final SensorManager sensorManager;
  private final LocationManager locationManager;

  private Location prevLocation;
  private Location currentLocation;
  private Location locationWhenGPSLost;

  private int       stopDetectionTimeout = 2000;
  private int       winPos = 0, intervalPos = 0;
  private int       numStepsWithFilter = 0, numStepsRaw = 0;
  private int       lastNumSteps = 0;
  private int[]     peak = new int[DIMENSIONS];
  private int[]     valley = new int[DIMENSIONS];
  private float[]   lastValley = new float[DIMENSIONS];
  private float[][] lastValues = new float[WIN_SIZE][DIMENSIONS];
  private float[]   prevDiff = new float[DIMENSIONS];
  private float     strideLength = STRIDE_LENGTH;
  private float     totalDistance = 0;
  private float     distWhenGPSLost = 0;
  private float     gpsDistance = 0;
  private long[]    stepInterval = new long[NUM_INTERVALS];
  private long      stepTimestamp = 0;
  private long      elapsedTimestamp = 0;
  private long      startTime = 0, prevStopClockTime = 0;
  private long      gpsStepTime = 0;
  private boolean[] foundValley = new boolean[DIMENSIONS];
  private boolean   startPeaking = false;
  private boolean   foundNonStep = true;
  private boolean   gpsAvailable = false;
  private boolean   calibrateSteps = true;
  private boolean   pedometerPaused = true;
  private boolean   useGps = true;
  private boolean   statusMoving = false;
  private boolean   firstGpsReading = true;

  /** Constructor. */
  public Pedometer(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    // some initialization
    winPos = 0;
    startPeaking = false;
    numStepsWithFilter = 0;
    numStepsRaw = 0;

    firstGpsReading = true;
    gpsDistance = 0;

    for (int i = 0; i < DIMENSIONS; i++) {
      foundValley[i] = true;
      lastValley[i] = 0;
    }
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    // Restore preferences
    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    strideLength = settings.getFloat("Pedometer.stridelength", STRIDE_LENGTH);
    totalDistance = settings.getFloat("Pedometer.distance", 0);
    numStepsRaw = settings.getInt("Pedometer.prevStepCount", 0);
    prevStopClockTime = settings.getLong("Pedometer.clockTime", 0);
    numStepsWithFilter = numStepsRaw;
    startTime = System.currentTimeMillis();
    Log.d(TAG, "Pedometer Created");
  }

  // Simple functions

  /**
   * Starts the pedometer.
   */
  @SimpleFunction
  public void Start() {
    if (pedometerPaused) {
      pedometerPaused = false;
      sensorManager.registerListener(this,
          sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
          SensorManager.SENSOR_DELAY_FASTEST);
      startTime = System.currentTimeMillis();
    }
  }

  /**
   * Stops the pedometer.
   */
  @SimpleFunction
  public void Stop() {
    Pause();
    locationManager.removeUpdates(this);
    useGps = false;
    calibrateSteps = false;
    setGpsAvailable(false);
  }

  /**
   * Resets the step count, distance, and clock.
   */
  @SimpleFunction
  public void Reset() {
    numStepsWithFilter = 0;
    numStepsRaw = 0;
    totalDistance = 0;
    calibrateSteps = false;
    prevStopClockTime = 0;
    startTime = System.currentTimeMillis();
  }

  /**
   * Resumes the counting of steps.
   */
  @SimpleFunction
  public void Resume() {
    Start();
  }

  /**
   * Pauses the counting of steps.
   */
  @SimpleFunction
  public void Pause() {
    if (!pedometerPaused) {
      pedometerPaused = true;
      statusMoving = false;
      sensorManager.unregisterListener(this);
      Log.d(TAG, "Unregistered listener on pause");
      prevStopClockTime += (System.currentTimeMillis() - startTime);
    }
  }

  /**
   * Saves the pedometer state to shared preferences.
   */
  @SimpleFunction(description = "Saves the pedometer state to the phone")
  public void Save() {
    // Store preferences
    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putFloat("Pedometer.stridelength", strideLength);
    editor.putFloat("Pedometer.distance", totalDistance);
    editor.putInt("Pedometer.prevStepCount", numStepsRaw);
    if (pedometerPaused) {
      editor.putLong("Pedometer.clockTime", prevStopClockTime);
    } else {
      editor.putLong("Pedometer.clockTime", prevStopClockTime +
          (System.currentTimeMillis() - startTime));
    }
    editor.putLong("Pedometer.closeTime", System.currentTimeMillis());
    editor.commit();
    Log.d(TAG, "Pedometer state saved.");
  }

  // Events

  /**
   * Indicates that a step was taken.
   *
   * @param simpleSteps number of raw steps detected
   * @param distance approximate distance covered by number of simpleSteps in meters
   */
  @SimpleEvent(description = "This event is run when a raw step is detected")
  public void SimpleStep(int simpleSteps, float distance) {
    EventDispatcher.dispatchEvent(this, "SimpleStep", simpleSteps, distance);
  }

  /**
   * Indicates that a step was taken while walking. This will not be called if
   * a single step is taken while standing still.
   *
   * @param walkSteps number of walking steps detected
   * @param distance approximate distance covered by the number of walkSteps in meters
   */
  @SimpleEvent(description = "This event is run when a walking step is detected")
  public void WalkStep(int walkSteps, float distance) {
    EventDispatcher.dispatchEvent(this, "WalkStep", walkSteps, distance);
  }

  /**
   * Indicates that the device is moving.
   */
  @SimpleEvent
  public void StartedMoving() {
    EventDispatcher.dispatchEvent(this, "StartedMoving");
  }

  /**
   * Indicates that the device has stopped.
   */
  @SimpleEvent
  public void StoppedMoving() {
    EventDispatcher.dispatchEvent(this, "StoppedMoving");
  }

  /**
   * Indicates that the calibration has failed. This could happen is the GPS
   * is not active, or if the client has set UseGps to false.
   */
  @SimpleEvent
  public void CalibrationFailed() {
    EventDispatcher.dispatchEvent(this, "CalibrationFailed");
  }

  /**
   * Indicates that the GPS is now available to use for distance measurement, and that
   * calibration is now possible.
   */
  @SimpleEvent
  public void GPSAvailable() {
    EventDispatcher.dispatchEvent(this, "GPSAvailable");
  }

  /**
   * Indicates that the GPS signal is lost.
   */
  @SimpleEvent
  public void GPSLost() {
    EventDispatcher.dispatchEvent(this, "GPSLost");
  }

  // Properties

  /**
   * Starts the process of calibrating the stride length by comparing number
   * of steps with the ditance covered (using the GPS).
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "true")
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public void CalibrateStrideLength(boolean cal) {
    if (!gpsAvailable && cal) {
      CalibrationFailed();
    } else {
      if (cal) {
        useGps = true;
      }
      calibrateSteps = cal;
    }
  }

  /**
   * Tells Whether stride length calibration is currently going on.
   *
   * @return {@code true} if stride length calibration is currently going on,
   *     {@code false} otherwise.
   */
  @SimpleProperty
  public boolean CalibrateStrideLength() {
    return calibrateSteps;
  }

  /**
   * Specifies the stride length in meters. The application can use this to explicitly set
   * stride length to override the one calculated by the pedometer's calibration mechanism.
   * As a side effect, this method turns off calibration of stride length using the GPS.
   *
   * @param length is the stride length in meters.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "0.73")
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public void StrideLength(float length) {
    CalibrateStrideLength(false);
    strideLength = length;
  }

  /**
   * Returns the current estimate of stride length in meters, if calibrated, or returns the
   * default (0.73 m) otherwise.
   *
   * @return length of the stride in meters.
   */
  @SimpleProperty
  public float StrideLength() {
    return strideLength;
  }

  /**
   * Sets the duration of idleness (no steps detected) after which to go into a "stopped" state.
   *
   * @param timeout the timeout in milliseconds.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "2000")
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public void StopDetectionTimeout(int timeout) {
    stopDetectionTimeout = timeout;
  }

  /**
   * Returns the duration of idleness (no steps detected) after which to go into a "stopped" state.
   *
   * @return the timeout in milliseconds.
   */
  @SimpleProperty
  public int StopDetectionTimeout() {
    return stopDetectionTimeout;
  }

  /**
   * Specifies whether to use GPS, if signal is available, to compute distance. This is set to
   * {@code true} by default.
   *
   * @param gps {@code true} enables use of GPS,
   *            {@code false} disables GPS
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "true")
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public void UseGPS(boolean gps) {
    if (!gps && useGps) {
      locationManager.removeUpdates(this);
    } else if (gps && !useGps) {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          0, 0, this);
    }
    useGps = gps;
  }

  /**
   * Returns whether the GPS is being used to measure distance.
   *
   * @return {@code true} if GPS is being used to measure distance, {@code false} otherwise.
   */
  @SimpleProperty
  public boolean UseGPS() {
    return useGps;
  }

  /**
   * Returns the approximate distance traveled in meters.
   *
   * @return approximate distance traveled in meters.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public float Distance() {
    return totalDistance;
  }

  /**
   * Returns the current status of motion.
   *
   * @return {@code true} if moving, {@code false} otherwise.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Moving() {
    return statusMoving;
  }

  /**
   * Returns the time elapsed in milliseconds since the pedometer has started.
   *
   * @return time elapsed in milliseconds since the pedometer was started.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public long ElapsedTime() {
    if (pedometerPaused) {
      return prevStopClockTime;
    } else {
      return prevStopClockTime + (System.currentTimeMillis() - startTime);
    }
  }

  /**
   * Finds average of the last NUM_INTERVALS number of step intervals
   * and checks if they are roughly equally spaced.
   */
  private boolean areStepsEquallySpaced() {
    float avg = 0;
    int num = 0;
    for (long interval : stepInterval) {
      if (interval > 0) {
        num++;
        avg += interval;
      }
    }
    avg = avg / num;
    for (long interval : stepInterval) {
      if (Math.abs(interval - avg) > INTERVAL_VARIATION) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the current middle of the window is the local peak.
   * TODO(user): Combine getPeak and getValley into one method.
   */
  private void getPeak() {
    int mid = (winPos + WIN_SIZE / 2) % WIN_SIZE;
    for (int k = 0; k < DIMENSIONS; k++) {
      peak[k] = mid;
      for (int i = 0; i < WIN_SIZE; i++) {
        if (i != mid && lastValues[i][k] >= lastValues[mid][k]) {
          peak[k] = -1;
          break;
        }
      }
    }
  }

  /**
   * Checks if the current middle of the window is the local valley.
   */
  private void getValley() {
    int mid = (winPos + WIN_SIZE / 2) % WIN_SIZE;
    for (int k = 0; k < DIMENSIONS; k++) {
      valley[k] = mid;
      for (int i = 0; i < WIN_SIZE; i++) {
        if (i != mid && lastValues[i][k] <= lastValues[mid][k]) {
          valley[k] = -1;
          break;
        }
      }
    }
  }

  private void setGpsAvailable(boolean available) {
    if (!gpsAvailable && available) {
      gpsAvailable = true;
      GPSAvailable();
    } else if (gpsAvailable && !available) {
      gpsAvailable = false;
      GPSLost();
    }
  }

  // SensorEventListener implementation

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    Log.d(TAG, "Accelerometer accuracy changed.");
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
      return;
    }
    float[] values = event.values;
    // Check if the middle reading within the current window represents
    // a peak/valley.
    if (startPeaking) {
      getPeak();
      getValley();
    }
    // Find largest peak-valley range amongst the three
    // accelerometer axis
    int argmax = prevDiff[0] > prevDiff[1] ? 0 : 1;
    argmax = prevDiff[2] > prevDiff[argmax] ? 2 : argmax;
    // Process each of the X, Y and Z accelerometer axis values
    for (int k = 0; k < DIMENSIONS; k++) {
      // Peak is detected
      if (startPeaking && peak[k] >= 0) {
        if (foundValley[k] &&
            lastValues[peak[k]][k] - lastValley[k] > PEAK_VALLEY_RANGE) {
          // Step detected on axis k with maximum peak-valley range.
          if (argmax == k) {
            long timestamp = System.currentTimeMillis();
            stepInterval[intervalPos] = timestamp - stepTimestamp;
            intervalPos = (intervalPos + 1) % NUM_INTERVALS;
            stepTimestamp = timestamp;
            if (areStepsEquallySpaced()) {
              if (foundNonStep) {
                numStepsWithFilter += NUM_INTERVALS;
                if (!gpsAvailable) {
                  totalDistance += strideLength * NUM_INTERVALS;
                }
                foundNonStep = false;
              }
              numStepsWithFilter++;
              WalkStep(numStepsWithFilter, totalDistance);
              if (!gpsAvailable) {
                totalDistance += strideLength;
              }
            } else {
              foundNonStep = true;
            }
            numStepsRaw++;
            SimpleStep(numStepsRaw, totalDistance);
            if (!statusMoving) {
              statusMoving = true;
              StartedMoving();
            }
          }
          foundValley[k] = false;
          prevDiff[k] = lastValues[peak[k]][k] - lastValley[k];
        } else {
          prevDiff[k] = 0;
        }
      }
      // Valley is detected
      if (startPeaking && valley[k] >= 0) {
        foundValley[k] = true;
        lastValley[k] = lastValues[valley[k]][k];
      }
      // Store latest accelerometer reading in the window.
      lastValues[winPos][k] = values[k];
    }
    elapsedTimestamp = System.currentTimeMillis();
    if (elapsedTimestamp - stepTimestamp > stopDetectionTimeout) {
      if (statusMoving) {
        statusMoving = false;
        StoppedMoving();
      }
      stepTimestamp = elapsedTimestamp;
    }
    // Force inequality with previous value. This helps with better
    // peak/valley detection.
    int prev = winPos - 1 < 0 ? WIN_SIZE - 1 : winPos - 1;
    for (int k = 0; k < DIMENSIONS; k++) {
      if (lastValues[prev][k] == lastValues[winPos][k]) {
        lastValues[winPos][k] += 0.001;
      }
    }
    // Once the buffer is full, start peak/valley detection.
    if (winPos == WIN_SIZE - 1 && !startPeaking) {
      startPeaking = true;
    }
    // Increment position within the window.
    winPos = (winPos + 1) % WIN_SIZE;
  }

  // LocationListener implementation

  @Override
  public void onLocationChanged(Location loc) {
    // If pedometer says stopped, return
    if (!statusMoving || pedometerPaused || !useGps) {
      return;
    }
    float distDelta = 0;
    currentLocation = loc;
    if (currentLocation.getAccuracy() > 10) {
      setGpsAvailable(false);
      return;
    } else {
      setGpsAvailable(true);
    }
    if (prevLocation != null) {
      distDelta = currentLocation.distanceTo(prevLocation);
      if (distDelta > 0.1 && distDelta < 10) {
        totalDistance += distDelta;
        prevLocation = currentLocation;
      }
    } else {
      if (locationWhenGPSLost != null) {
        float distDarkness =
            currentLocation.distanceTo(locationWhenGPSLost);
        totalDistance = distWhenGPSLost +
            (distDarkness + (totalDistance - distWhenGPSLost)) / 2;
      }
      gpsStepTime = System.currentTimeMillis();
      prevLocation = currentLocation;
    }
    if (calibrateSteps) {
      if (!firstGpsReading) {
        gpsDistance += distDelta;
        int stepsTaken = numStepsRaw - lastNumSteps;
        strideLength = gpsDistance / stepsTaken;
      } else {
        firstGpsReading = false;
        lastNumSteps = numStepsRaw;
      }
    } else {
      firstGpsReading = true;
      gpsDistance = 0;
    }
  }

  @Override
  public void onProviderDisabled(String provider) {
    distWhenGPSLost = totalDistance;
    locationWhenGPSLost = currentLocation;
    firstGpsReading = true;
    prevLocation = null;
    setGpsAvailable(false);
  }

  @Override
  public void onProviderEnabled(String provider) {
    setGpsAvailable(true);
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle data) {
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    sensorManager.unregisterListener(this);
    locationManager.removeUpdates(this);
  }
}
