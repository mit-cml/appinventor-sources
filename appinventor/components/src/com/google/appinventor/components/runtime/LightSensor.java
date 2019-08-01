// -*- mode: java; c-basic-offset: 2; -*-
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
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Handler;

import android.util.Log;

import android.view.Surface;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Physical world component that can measure the light level.
 * It is implemented using
 * android.hardware.SensorListener
 * (http://developer.android.com/reference/android/hardware/SensorListener.html).
 */
@DesignerComponent(version = 1,
                   description = "Non-visible component that can measure the light level.",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/lightsensor.png")
@SimpleObject
public class LightSensor extends AndroidNonvisibleComponent
    implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {

  // Logging and Debugging
  private final static String LOG_TAG = "LightSensor";
  private final static boolean DEBUG = true;

  // Backing for sensor values
  private float lux;

  private int accuracy;
  private int sensitivity;

  private final SensorManager sensorManager;

  private final WindowManager windowManager;
  private final Resources resources;

  // Indicates whether the sensor should generate events
  private boolean enabled;

  private Sensor sensor;

  // Used to launch Runnables on the UI Thread after a delay
  private final Handler androidUIHandler;

  /**
   * Creates a new LightSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public LightSensor(ComponentContainer container) {
    super(container.$form());
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    enabled = true;
    resources = container.$context().getResources();
    windowManager = (WindowManager) container.$context().getSystemService(Context.WINDOW_SERVICE);
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    androidUIHandler = new Handler();
    startListening();
  }


  /**
   * Indicates the light level changed.
   */
  @SimpleEvent
  public void LightChanged(float lux) {
    this.lux = lux;
    EventDispatcher.dispatchEvent(this, "LuxChanged", lux);
  }

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that a light sensor is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Available() {
    List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
    return sensors.size() > 0;
  }

  /**
   * If true, the sensor will generate events.  Otherwise, no events
   * are generated.
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
      sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
  }

  // Assumes that sensorManager has been initialized, which happens in constructor
  private void stopListening() {
    sensorManager.unregisterListener(this);
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
   * Returns the lux.
   * The sensor must be enabled to return meaningful values.
   *
   * @return lux
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public float Lux() {
      return lux;
  }

  // SensorListener implementation
  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
      if (enabled && sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
      accuracy = sensorEvent.accuracy;
      final float[] values = sensorEvent.values;
      LightChanged(values[0]);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
    
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
}
