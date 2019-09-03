// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * A single-value sensor whose most recent values should be buffered
 * and averaged.
 */
@SimpleObject
public abstract class BufferedSingleValueSensor extends SingleValueSensor {
  private AveragingBuffer buffer;

  public BufferedSingleValueSensor(ComponentContainer container, 
    int sensorType, int bufferSize) {
    super(container.$form(), sensorType);
    buffer = new AveragingBuffer(bufferSize);
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled && sensorEvent.sensor.getType() == sensorType) {
      final float[] values = sensorEvent.values;
      buffer.insert(values[0]);
      super.onSensorChanged(sensorEvent);
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
