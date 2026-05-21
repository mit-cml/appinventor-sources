// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import java.util.List;

/**
 * A sensors reporting a single value, such as temperature or humidity.
 */
@SimpleObject
public abstract class SingleValueSensor extends AndroidNonvisibleComponent
    implements OnPauseListener, OnResumeListener, SensorComponent,
    SensorEventListener, Deleteable {
  private static final int DEFAULT_REFRESH_TIME = 1000; // ms
  private Sensor sensor;
  protected int sensorType;
  protected float value;  // most recent value read
  protected final SensorManager sensorManager;
  protected boolean enabled;
  protected int refreshTime;

  public SingleValueSensor(ComponentContainer container, int sensorType) {
    super(container.$form());
    this.sensorType = sensorType;
    form.registerForOnResume(this);
    form.registerForOnPause(this);

    refreshTime = DEFAULT_REFRESH_TIME;
    enabled = true;
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(sensorType);
    startListening();
  }

  protected void startListening() {
    // Before Gingerbread, the only legal values for the third argument
    // to registerListener() were SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI,
    // SENSOR_DELAY_GAME, or SENSOR_DELAY_FASTEST. From Gingerbread,
    // the refresh rate can be requested in microseconds.
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      // App Inventor users specify time in milliseconds.
      int timeInMicroseconds = refreshTime * 1000;
      sensorManager.registerListener(this, sensor, timeInMicroseconds);
    } else {
      sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }
  }

  protected void stopListening() {
    sensorManager.unregisterListener(this);
  }

  /**
   * Specifies whether or not the device has the hardware to support the `%type%` component.
   *
   * @return {@code true} indicates that the sensor is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(description = "Specifies whether or not the device has the "
      + "hardware to support the %type% component.")
  public boolean Available() {
    return isAvailable();
  }

/**
   * If true, the sensor will generate events.  Otherwise, no events
   * are generated.
   *
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty(description = "If enabled, then device will listen for changes.")
  public boolean Enabled() {
    return enabled;
  }

  /**
   * Specifies whether the sensor should generate events.  If `true`{:.logic.block},
   * the sensor will generate events.  Otherwise, no events are
   * generated.
   *
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Enabled(boolean enabled) {
    setEnabled(enabled);
  }

  /**
   * RefreshTime property getter method.
   *
   * @suppressdoc
   * @return time in ms between updates
   */
  @SimpleProperty(
      description = "The requested minimum time in milliseconds between " +
      "changes in readings being reported. Android is not guaranteed to honor the request. " +
      "Setting this property has no effect on pre-Gingerbread devices.")
  public int RefreshTime() {
    return refreshTime;
  }

  /**
   * RefreshTime property setter method.
   *
   * @suppressdoc
   * @param time in ms between updates
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_REFRESH_TIME + "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void RefreshTime(int time) {
    refreshTime = time;
    if (enabled) {
      stopListening();
      startListening();
    }
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled && sensorEvent.sensor.getType() == sensorType) {
      final float[] values = sensorEvent.values;
      value = values[0];
      onValueChanged(value);
    }
  }

  protected abstract void onValueChanged(float value);

  protected boolean isAvailable() {
    return sensorManager.getSensorList(sensorType).size() > 0;
  }

  protected void setEnabled(boolean enabled) {
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

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onPause() {
    if (enabled) {
      stopListening();
    }
  }

  @Override
  public void onResume() {
    if (enabled) {
      startListening();
    }
  }

  @Override
  public void onDelete() {
    if (enabled) {
      stopListening();
    }
  }

  protected float getValue() {
    return value;
  }
}
