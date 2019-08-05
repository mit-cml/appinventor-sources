// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

public abstract class SingleValueSensor extends AndroidNonvisibleComponent
    implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {
  private static final int BUFFER_SIZE = 10;

  private AveragingBuffer buffer;
  private Sensor sensor;
  protected final SensorManager sensorManager;
  protected boolean enabled;
  protected int accuracy;
  protected int sensorType;

  public SingleValueSensor(ComponentContainer container, int sensorType) {
    super(container.$form());
    this.sensorType = sensorType;
    form.registerForOnResume(this);
    form.registerForOnStop(this);

    enabled = true;
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(sensorType);
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
    if (enabled && sensorEvent.sensor.getType() == sensorType) {
      accuracy = sensorEvent.accuracy;
      final float[] values = sensorEvent.values;
      buffer.insert(values[0]);
      onValueChanged(values[0]);
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
