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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Handler;

import java.util.List;

public abstract class EnvironmentSensor extends AndroidNonvisibleComponent
    implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {
  private static final int BUFFER_SIZE = 10;

  private AveragingBuffer buffer;
  private Sensor sensor;
  protected final SensorManager sensorManager;
  protected boolean enabled;
  protected int accuracy;

  public EnvironmentSensor(ComponentContainer container) {
    super(container.$form());
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    enabled = true;
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    buffer = new AveragingBuffer(BUFFER_SIZE);
    startListening();
  }

  protected void startListening() {
    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
  }

  protected void stopListening() {
    sensorManager.unregisterListener(this);
  }

  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled && sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
      accuracy = sensorEvent.accuracy;
      final float[] values = sensorEvent.values;
      buffer.insert(values[0]);
      onValueChanged(values[0]);
    }
  }
  protected abstract void onValueChanged(float value);

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

  protected float getAverageValue() {
    return buffer.getAverage();
  }

  private class AveragingBuffer {
    private Float[] data;
    private int next;

    private AveragingBuffer(int size) {
      data = new Float[size];
      next = 0;
    }

    private void insert(Float datum) {
      data[next++] = datum;
      if (next == data.length) {
        next = 0;
      }
    }

    private float getAverage() {
      double sum = 0;
      int count = 0;

      for (int i = 0; i < data.length; i++) {
        if (data[i] != null) {
          sum += data[i];
          count++;
        }
      }

      return (float) (count == 0 ? sum : sum / count);
    }
  }
}
