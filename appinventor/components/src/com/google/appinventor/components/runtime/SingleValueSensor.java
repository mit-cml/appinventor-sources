// -*- mode: java; c-basic-offset: 2; -*-
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

import java.util.List;

/**
 * A sensors reporting a single value, such as temperature or humidity.
 */
@SimpleObject
public abstract class SingleValueSensor extends AndroidNonvisibleComponent
    implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {
  private Sensor sensor;
  protected float value;  // most recent value read
  protected final SensorManager sensorManager;
  protected boolean enabled;
  protected int sensorType;

  public SingleValueSensor(ComponentContainer container, int sensorType) {
    super(container.$form());
    this.sensorType = sensorType;
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    enabled = true;
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(sensorType);
    startListening();
  }

  protected void startListening() {
    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
  }

  protected void stopListening() {
    sensorManager.unregisterListener(this);
  }

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that a hygrometer is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
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
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
  }

  /**
   * Specifies whether the sensor should generate events.  If true,
   * the sensor will generate events.  Otherwise, no events are
   * generated.
   *
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    setEnabled(enabled);
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
  public void onResume() {
    if (enabled) {
      startListening();
    }
  }

  @Override
  public void onStop() {
    if (enabled) {
      stopListening();
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
