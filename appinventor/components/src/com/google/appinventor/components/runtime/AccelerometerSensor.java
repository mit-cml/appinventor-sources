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
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Physical world component that can detect shaking and measure
 * acceleration in three dimensions.  It is implemented using
 * android.hardware.SensorListener
 * (http://developer.android.com/reference/android/hardware/SensorListener.html).
 *
 * <p>From the Android documentation:
 * "Sensor values are acceleration in the X, Y and Z axis, where the X axis
 * has positive direction toward the right side of the device, the Y axis has
 * positive direction toward the top of the device and the Z axis has
 * positive direction toward the front of the device. The direction of the
 * force of gravity is indicated by acceleration values in the X, Y and Z
 * axes. The typical case where the device is flat relative to the surface of
 * the Earth appears as -STANDARD_GRAVITY in the Z axis and X and Y values
 * close to zero. Acceleration values are given in SI units (m/s^2)."
 *
 */
// TODO(user): ideas - event for knocking
@DesignerComponent(version = YaVersion.ACCELEROMETERSENSOR_COMPONENT_VERSION,
    description = "Non-visible component that can detect shaking and " +
    "measure acceleration approximately in three dimensions using SI units " +
    "(m/s<sup>2</sup>).  The components are: <ul>\n" +
    "<li> <strong>xAccel</strong>: 0 when the phone is at rest on a flat " +
    "     surface, positive when the phone is tilted to the right (i.e., " +
    "     its left side is raised), and negative when the phone is tilted " +
    "     to the left (i.e., its right size is raised).</li>\n " +
    "<li> <strong>yAccel</strong>: 0 when the phone is at rest on a flat " +
    "     surface, positive when its bottom is raised, and negative when " +
    "     its top is raised. </li>\n " +
    "<li> <strong>zAccel</strong>: Equal to -9.8 (earth's gravity in meters per " +
    "     second per second when the device is at rest parallel to the ground " +
    "     with the display facing up, " +
    "     0 when perpindicular to the ground, and +9.8 when facing down.  " +
    "     The value can also be affected by accelerating it with or against " +
    "     gravity. </li></ul>",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/accelerometersensor.png")
@SimpleObject
public class AccelerometerSensor extends AndroidNonvisibleComponent
    implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {

  // Shake thresholds - derived by trial
  private static final double weakShakeThreshold = 5.0;
  private static final double moderateShakeThreshold = 13.0;
  private static final double strongShakeThreshold = 20.0;

  // Cache for shake detection
  private static final int SENSOR_CACHE_SIZE = 10;
  private final Queue<Float> X_CACHE = new LinkedList<Float>();
  private final Queue<Float> Y_CACHE = new LinkedList<Float>();
  private final Queue<Float> Z_CACHE = new LinkedList<Float>();

  // Backing for sensor values
  private float xAccel;
  private float yAccel;
  private float zAccel;

  private int accuracy;

  private int sensitivity;

  // Sensor manager
  private final SensorManager sensorManager;

  // Indicates whether the accelerometer should generate events
  private boolean enabled;

  //Specifies the minimum time interval between calls to Shaking()
  private int minimumInterval;

  //Specifies the time when Shaking() was last called
  private long timeLastShook;

  private Sensor accelerometerSensor;

  /**
   * Creates a new AccelerometerSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public AccelerometerSensor(ComponentContainer container) {
    super(container.$form());
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    enabled = true;
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    startListening();
    MinimumInterval(400);
    Sensitivity(Component.ACCELEROMETER_SENSITIVITY_MODERATE);
  }


  /**
   * Returns the minimum interval required between calls to Shaking(),
   * in milliseconds.
   * Once the phone starts being shaken, all further Shaking() calls will be ignored
   * until the interval has elapsed.
   * @return  minimum interval in ms
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "The minimum interval between phone shakes")
  public int MinimumInterval() {
    return minimumInterval;
  }

  /**
   * Specifies the minimum interval required between calls to Shaking(),
   * in milliseconds.
   * Once the phone starts being shaken, all further Shaking() calls will be ignored
   * until the interval has elapsed.
   * @param interval  minimum interval in ms
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "400") //Default value derived by trial of 12 people on 3 different devices
  @SimpleProperty
  public void MinimumInterval(int interval) {
    minimumInterval = interval;
  }

  /**
   * Returns a number that encodes how sensitive the AccelerometerSensor is.
   * The choices are: 1 = weak, 2 = moderate, 3 = strong.
   *
   * @return  one of {@link Component#ACCELEROMETER_SENSITIVITY_WEAK},
   *          {@link Component#ACCELEROMETER_SENSITIVITY_MODERATE} or
   *          {@link Component#ACCELEROMETER_SENSITIVITY_STRONG}
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how sensitive the accelerometer is. " +
              "The choices are: 1 = weak, 2 = moderate, " +
              " 3 = strong.")
  public int Sensitivity() {
    return sensitivity;
  }

  /**
   * Specifies the sensitivity of the accelerometer
   * and checks that the argument is a legal value.
   *
   * @param sensitivity one of {@link Component#ACCELEROMETER_SENSITIVITY_WEAK},
   *          {@link Component#ACCELEROMETER_SENSITIVITY_MODERATE} or
   *          {@link Component#ACCELEROMETER_SENSITIVITY_STRONG}
   *
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ACCELEROMETER_SENSITIVITY,
      defaultValue = Component.ACCELEROMETER_SENSITIVITY_MODERATE + "")
  @SimpleProperty
  public void Sensitivity(int sensitivity) {
    if ((sensitivity == 1) || (sensitivity == 2) || (sensitivity == 3)) {
      this.sensitivity = sensitivity;
    } else {
      form.dispatchErrorOccurredEvent(this, "Sensitivity",
          ErrorMessages.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY, sensitivity);
    }
  }

  /**
   * Indicates the acceleration changed in the X, Y, and/or Z dimensions.
   */
  @SimpleEvent
  public void AccelerationChanged(float xAccel, float yAccel, float zAccel) {
    this.xAccel = xAccel;
    this.yAccel = yAccel;
    this.zAccel = zAccel;

    addToSensorCache(X_CACHE, xAccel);
    addToSensorCache(Y_CACHE, yAccel);
    addToSensorCache(Z_CACHE, zAccel);

    long currentTime = System.currentTimeMillis();

    //Checks whether the phone is shaking and the minimum interval
    //has elapsed since the last registered a shaking event.
    if ((isShaking(X_CACHE, xAccel) || isShaking(Y_CACHE, yAccel) || isShaking(Z_CACHE, zAccel))
        && (timeLastShook == 0 || currentTime >= timeLastShook + minimumInterval)){
      timeLastShook = currentTime;
      Shaking();
    }

    EventDispatcher.dispatchEvent(this, "AccelerationChanged", xAccel, yAccel, zAccel);
  }

  /**
   * Indicates the device started being shaken or continues to be shaken.
   */
  @SimpleEvent
  public void Shaking() {
    EventDispatcher.dispatchEvent(this, "Shaking");
  }

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that an accelerometer sensor is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Available() {
    List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
    return (sensors.size() > 0);
  }

  /**
   * If true, the sensor will generate events.  Otherwise, no events
   * are generated even if the device is accelerated or shaken.
   *
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
  }

  // Assumes that sensorManager has been initialized, which happens in constructor
  private void startListening() {
    sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
  }

  // Assumes that sensorManager has been initialized, which happens in constructor
  private void stopListening() {
    sensorManager.unregisterListener(this);
  }

  /**
   * Specifies whether the sensor should generate events.  If true,
   * the sensor will generate events.  Otherwise, no events are
   * generated even if the device is accelerated or shaken.
   *
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    if (this.enabled == enabled) {
      return;
    }
    this.enabled = enabled;
    if (enabled) {
      startListening();
    } else {
      stopListening();
    }
  }

  /**
   * Returns the acceleration in the X-dimension in SI units (m/s^2).
   * The sensor must be enabled to return meaningful values.
   *
   * @return  X acceleration
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public float XAccel() {
    return xAccel;
  }

  /**
   * Returns the acceleration in the Y-dimension in SI units (m/s^2).
   * The sensor must be enabled to return meaningful values.
   *
   * @return  Y acceleration
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public float YAccel() {
    return yAccel;
  }

  /**
   * Returns the acceleration in the Z-dimension in SI units (m/s^2).
   * The sensor must be enabled to return meaningful values.
   *
   * @return  Z acceleration
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public float ZAccel() {
    return zAccel;
  }

  /*
   * Updating sensor cache, replacing oldest values.
   */
  private void addToSensorCache(Queue<Float> cache, float value) {
    if (cache.size() >= SENSOR_CACHE_SIZE) {
      cache.remove();
    }
    cache.add(value);
  }

  /*
   * Indicates whether there was a sudden, unusual movement.
   */
  // TODO(user): Maybe this can be improved.
  // See http://www.utdallas.edu/~rxb023100/pubs/Accelerometer_WBSN.pdf.
  private boolean isShaking(Queue<Float> cache, float currentValue) {
    float average = 0;
    for (float value : cache) {
      average += value;
    }

    average /= cache.size();

    if (Sensitivity() == 1) { //sensitivity is weak
      return Math.abs(average - currentValue) > strongShakeThreshold;
    } else if (Sensitivity() == 2) { //sensitivity is moderate
      return ((Math.abs(average - currentValue) > moderateShakeThreshold)
        && (Math.abs(average - currentValue) < strongShakeThreshold));
    } else { //sensitivity is strong
      return ((Math.abs(average - currentValue) > weakShakeThreshold)
        && (Math.abs(average - currentValue) < moderateShakeThreshold));
    }
  }

  // SensorListener implementation
  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled) {
      final float[] values = sensorEvent.values;
      xAccel = values[0];
      yAccel = values[1];
      zAccel = values[2];
      accuracy = sensorEvent.accuracy;
      AccelerationChanged(xAccel, yAccel, zAccel);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO(markf): Figure out if we actually need to do something here.
  }

  // OnResumeListener implementation

  @Override
  public void onResume() {
    if (enabled) {
      startListening();
    }
  }

  // OnStopListener implementation

  @Override
  public void onStop() {
    if (enabled) {
      stopListening();
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    if (enabled) {
      stopListening();
    }
  }
}
