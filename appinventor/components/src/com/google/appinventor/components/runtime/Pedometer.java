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
import android.util.Log;

/**
 * This component keeps count of steps using the accelerometer.
 *
 */
@DesignerComponent(version = YaVersion.PEDOMETER_COMPONENT_VERSION,
                   description = "Component that can count steps.",
                   category = ComponentCategory.SENSORS,
                   nonVisible = true,
                   iconName = "images/pedometer.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.ACCESS_FINE_LOCATION")
public class Pedometer extends AndroidNonvisibleComponent
    implements Component, SensorEventListener, Deleteable {
  private static final String TAG = "Pedometer";
  private static final String PREFS_NAME = "PedometerPrefs";

  private static final int INTERVAL_VARIATION = 250;
  private static final int NUM_INTERVALS = 2;
  private static final int WIN_SIZE = 100;
  private static final float STRIDE_LENGTH = (float) 0.73;
  private static final float PEAK_VALLEY_RANGE = (float) 40.0;

  private final Context context;
  private final SensorManager sensorManager;

  private int       stopDetectionTimeout = 2000;
  private int       winPos = 0, intervalPos = 0;
  private int       numStepsWithFilter = 0, numStepsRaw = 0;
  private float     lastValley = 0;
  private float[]   lastValues = new float[WIN_SIZE];
  private float     strideLength = STRIDE_LENGTH;
  private float     totalDistance = 0;
  private long[]    stepInterval = new long[NUM_INTERVALS];
  private long      stepTimestamp = 0;
  private long      startTime = 0, prevStopClockTime = 0;
  private boolean   foundValley = false;
  private boolean   startPeaking = false;
  private boolean   foundNonStep = true;
  private boolean   pedometerPaused = true;

  private float[] avgWindow = new float[10];
  private int avgPos = 0;

  /** Constructor. */
  public Pedometer(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    // some initialization
    winPos = 0;
    startPeaking = false;
    numStepsWithFilter = 0;
    numStepsRaw = 0;

    foundValley = true;
    lastValley = 0;

    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

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
  }

  /**
   * Resets the step count, distance, and clock.
   */
  @SimpleFunction
  public void Reset() {
    numStepsWithFilter = 0;
    numStepsRaw = 0;
    totalDistance = 0;
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
<<<<<<< HEAD
   * Called whenever the accelerometer value updates.
   *
   * @param magnitude the smoothed magnitude value used for step detection
   */
  @SimpleEvent(description = "This event is run when the accelerometer provides a reading")
  public void MagnitudeChanged(float magnitude) {
    EventDispatcher.dispatchEvent(this, "MagnitudeChanged", magnitude);
=======
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
>>>>>>> parent of ffbb86d... Add MagnitudeChanged event to the pedometer for debugging.
  }

  // Properties

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
      category = PropertyCategory.BEHAVIOR, description = "stride length in meters")
  public void StrideLength(float length) {
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
      category = PropertyCategory.BEHAVIOR, description = "the duration in milliseconds of idleness (no steps detected) after which to go into a \"stopped\" state")
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
   * Returns the approximate distance traveled in meters.
   *
   * @return approximate distance traveled in meters.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR, description = "approximate distance traveled in meters.")
  public float Distance() {
    return totalDistance;
  }

  /**
   * Returns the time elapsed in milliseconds since the pedometer has started.
   *
   * @return time elapsed in milliseconds since the pedometer was started.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR, description = "time elapsed in milliseconds since the pedometer was started.")
  public long ElapsedTime() {
    if (pedometerPaused) {
      return prevStopClockTime;
    } else {
      return prevStopClockTime + (System.currentTimeMillis() - startTime);
    }
  }

  /**
   * Returns the number of simple steps taken since the pedometer has started.
   *
   * @return the number of simple steps since the pedometer was started.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "the number of simple steps taken since the pedometer has started.")
  public int SimpleSteps() {
    return numStepsRaw;
  }

  /**
   * Returns the number of walk steps taken since the pedometer has started.
   *
   * @return the number of walk steps since the pedometer was started.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "the number of walk steps taken since the pedometer has started.")
  public int WalkSteps() {
    return numStepsWithFilter;
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
   */
  private boolean isPeak() {
    int mid = (winPos + WIN_SIZE / 2) % WIN_SIZE;
    for (int i = 0; i < WIN_SIZE; i++) {
      if (i != mid && lastValues[i] > lastValues[mid]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the current middle of the window is the local peak.
   */
  private boolean isValley() {
    int mid = (winPos + WIN_SIZE / 2) % WIN_SIZE;
    for (int i = 0; i < WIN_SIZE; i++) {
      if (i != mid && lastValues[i] < lastValues[mid]) {
        return false;
      }
    }
    return true;
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
    float magnitude = 0;
    for (float v : values) magnitude += v * v;
    // Check if the middle reading within the current window represents
    // a peak/valley.
    int mid = (winPos + WIN_SIZE / 2) % WIN_SIZE;

    // Peak is detected
    if (startPeaking && isPeak()) {
      if (foundValley && lastValues[mid] - lastValley > PEAK_VALLEY_RANGE) {
        // Step detected on axis k with maximum peak-valley range.
        long timestamp = System.currentTimeMillis();
        stepInterval[intervalPos] = timestamp - stepTimestamp;
        intervalPos = (intervalPos + 1) % NUM_INTERVALS;
        stepTimestamp = timestamp;
        if (areStepsEquallySpaced()) {
          if (foundNonStep) {
            numStepsWithFilter += NUM_INTERVALS;
            totalDistance += strideLength * NUM_INTERVALS;
            foundNonStep = false;
          }
          numStepsWithFilter++;
          WalkStep(numStepsWithFilter, totalDistance);
          totalDistance += strideLength;
        } else {
          foundNonStep = true;
        }
        numStepsRaw++;
        SimpleStep(numStepsRaw, totalDistance);
        foundValley = false;
      }
    }
    // Valley is detected
    if (startPeaking && isValley()) {
      foundValley = true;
      lastValley = lastValues[mid];
    }
    // Store latest accelerometer reading in the window.
    avgWindow[avgPos] = magnitude;
    avgPos = (avgPos + 1) % avgWindow.length;
    lastValues[winPos] = 0;
    for (float m : avgWindow) lastValues[winPos] += m;
    lastValues[winPos] /= avgWindow.length;
    if (startPeaking || winPos > 1) {
      int i = winPos;
      if (--i < 0) i += WIN_SIZE;
      lastValues[winPos] += 2 * lastValues[i];
      if (--i < 0) i += WIN_SIZE;
      lastValues[winPos] += lastValues[i];
      lastValues[winPos] /= 4;
    } else if (!startPeaking && winPos == 1) {
      lastValues[1] = (lastValues[1] + lastValues[0]) / 2f;
    }

    long elapsedTimestamp = System.currentTimeMillis();
    if (elapsedTimestamp - stepTimestamp > stopDetectionTimeout) {
      stepTimestamp = elapsedTimestamp;
    }
    // Once the buffer is full, start peak/valley detection.
    if (winPos == WIN_SIZE - 1 && !startPeaking) {
      startPeaking = true;
    }
    // Increment position within the window.
    winPos = (winPos + 1) % WIN_SIZE;
  }

  // Deleteable implementation
  @Override
  public void onDelete() {
    sensorManager.unregisterListener(this);
  }
}
