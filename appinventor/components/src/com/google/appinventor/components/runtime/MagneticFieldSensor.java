// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2019 MIT, All rights reserved
// Copyright 2017-2019 Kodular, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;


@DesignerComponent(
    category = ComponentCategory.SENSORS,
    description = "<p>Non-visible component that measures the ambient geomagnetic field for all three physical axes " +
        "(x, y, z) in Tesla https://en.wikipedia.org/wiki/Tesla_(unit).</p>",
    iconName = "images/magneticSensor.png",
    nonVisible = true,
    version = YaVersion.MAGNETICFIELDSENSOR_COMPONENT_VERSION)
@SimpleObject

public class MagneticFieldSensor extends AndroidNonvisibleComponent implements SensorEventListener, Deleteable, OnPauseListener, OnResumeListener, OnStopListener, SensorComponent {
  private double absoluteStrength;
  private boolean enabled = true;
  private boolean listening;
  private Sensor magneticSensor;
  private final SensorManager sensorManager;
  private float xStrength;
  private float yStrength;
  private float zStrength;

  public MagneticFieldSensor(ComponentContainer container) {
    super(container.$form());
    form.registerForOnResume(this);
    form.registerForOnStop(this);
    form.registerForOnPause(this);
    sensorManager = (SensorManager) container.$context().getSystemService("sensor");
    magneticSensor = sensorManager.getDefaultSensor(2);
    startListening();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates that there is a magnetic field " +
      "sensor in the device and it is available.")
  public boolean Available() {
    return sensorManager.getSensorList(2).size() > 0;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates the maximum range the magnetic " +
      "sensor can reach.")
  public float MaximumRange() {
    return magneticSensor.getMaximumRange();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates whether or not the magnetic field " +
      "sensor is enabled and working.")
  public boolean Enabled() {
    return enabled;
  }

  @DesignerProperty(defaultValue = "True", editorType = "boolean")
  @SimpleProperty
  public void Enabled(boolean localEnabled) {
    if (enabled != localEnabled) {
      enabled = localEnabled;
    }
    if (enabled) {
      startListening();
    } else {
      stopListening();
    }
  }

  @SimpleEvent(description = "Triggers when magnetic field has changed, setting the new values in parameters.")
  public void MagneticChanged(float xStrength, float yStrength, float zStrength, double absoluteStrength) {
    EventDispatcher.dispatchEvent(this, "MagneticChanged", xStrength, yStrength, zStrength, absoluteStrength);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates the absolute strength of the field.")
  public double AbsoluteStrength() {
    return absoluteStrength;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates the field's strength in the X-axis.")
  public float XStrength() {
    return xStrength;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates the field's strength in the Y-axis.")
  public float YStrength() {
    return yStrength;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Indicates the field's strength in the Z-axis.")
  public float ZStrength() {
    return zStrength;
  }

  private Sensor getMagneticSensor() {
    Sensor sensor = sensorManager.getDefaultSensor(2);
    return sensor != null ? sensor : sensorManager.getDefaultSensor(2);
  }

  public void onResume() {
    if (enabled) {
      startListening();
    }
  }

  public void onStop() {
    if (enabled) {
      stopListening();
    }
  }

  public void onDelete() {
    if (enabled) {
      stopListening();
    }
  }

  public void onPause() {
    stopListening();
  }

  private void startListening() {
    if (!listening && sensorManager != null && magneticSensor != null) {
      sensorManager.registerListener(this, magneticSensor, 3);
      listening = true;
    }
  }

  private void stopListening() {
    if (listening && sensorManager != null) {
      sensorManager.unregisterListener(this);
      listening = false;
      xStrength = 0.0f;
      yStrength = 0.0f;
      zStrength = 0.0f;
    }
  }

  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled && sensorEvent.sensor.getType() == 2) {
      float[] values = (float[]) sensorEvent.values.clone();
      xStrength = sensorEvent.values[0];
      yStrength = sensorEvent.values[1];
      zStrength = sensorEvent.values[2];
      absoluteStrength = Math.sqrt((double) (((xStrength * xStrength) + (yStrength * yStrength)) + (zStrength * zStrength)));
      MagneticChanged(xStrength, yStrength, zStrength, absoluteStrength);
    }
  }

  public void onAccuracyChanged(Sensor sensor, int i) {
  }
}
