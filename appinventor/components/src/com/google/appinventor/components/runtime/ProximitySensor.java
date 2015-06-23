// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import java.util.List;

@DesignerComponent(version = YaVersion.PROXIMITYSENSOR_COMPONENT_VERSION,
        description = "<p>Non-visible component that can measures the proximity of an object in cm " +
                "relative to the view screen of a device. This sensor is typically used to determine " +
                "whether a handset is being held up to a persons ear; " +
                "i.e. lets you determine how far away an object is from a device. " +
                "Many devices return the absolute distance, in cm, but some return only near and far values. " +
                "In this case, the sensor usually reports its maximum range value in the far state " +
                "and a lesser value in the near state.</p>",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/proximitysensor.png")
@SimpleObject
public class ProximitySensor extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, SensorComponent, OnPauseListener,
        SensorEventListener, Deleteable {

    private Sensor proximitySensor;

    private final SensorManager sensorManager;

    // Indicates whether the sensor should generate events
    private boolean enabled;
    private float distance=0f;

    // Indicates if the sensor should be running when screen is off (on pause)
    private boolean keepRunningWhenOnPause;

    /**
     * Creates a new ProximitySensor component.
     *
     * @param container  ignored (because this is a non-visible component)
     */
    public ProximitySensor(ComponentContainer container) {
        super(container.$form());
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        form.registerForOnPause(this);

        enabled = true;
        sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        startListening();
    }

    /**
     * Used to determine if the device has ProximitySensor
     *
     * @return {@code true} indicates that an proximity sensor is available,
     *         {@code false} that it isn't
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Reports whether or not the device has a proximity sensor")
    public boolean Available() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        return (sensors.size() > 0);
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

    @Override
    public void onPause() {
        if (enabled && !keepRunningWhenOnPause) {
            stopListening();
        }
    }


    /**
     * Registers the sensor to start listening for proximity changes
     */
    private void startListening() {
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Stops the sensors from listening to the proximity changes
     */
    private void stopListening() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Called when sensor values have changed
     * @param sensorEvent holds information such as the sensor's type,
     *                    the time-stamp, accuracy and sensor's data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (enabled) {
            final float[] values = sensorEvent.values.clone();
            distance = values[0];
            ProximityChanged(distance);
        }
    }

    /**
     * Determines a sensor's maximum range. Some proximity sensors return binary values
     * that represent "near" or "far." In this case, the sensor usually reports
     * its maximum range value in the far state and a lesser value in the near state.
     * Typically, the far value is a value > 5 cm, but this can vary from sensor to sensor.
     *
     * @return Sensor's maximum range.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Reports the Maximum Range of the device's ProximitySensor")
    public float MaximumRange() {
        return proximitySensor.getMaximumRange();
    }

    /**
     * If true, the sensor will generate events.  Otherwise, no events
     * are generated .
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
     * the sensor will generate events.  Otherwise, no events are generated.
     *
     * @param enabled  {@code true} enables sensor event generation,
     *                 {@code false} disables it
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty (description = "If enabled, then device will listen for changes in proximity")
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
     * Returns value of keepRunningWhenOnPause
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean KeepRunningWhenOnPause() {
        return keepRunningWhenOnPause;
    }

    /**
     * Specifies if sensor should still be listening when activity is not active
     *
     * @param enabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty (description = "If set to true, it will keep sensing for proximity changes even when the app is not visible")
    public void KeepRunningWhenOnPause(boolean enabled) {

        this.keepRunningWhenOnPause = enabled;
    }

    @SimpleEvent(description = "Triggered when distance (in cm) of the object to the device changes. ")
    public void ProximityChanged(float distance) {
        this.distance = distance;
        EventDispatcher.dispatchEvent(this, "ProximityChanged", this.distance);
    }

    /**
     * Returns the distance.
     * The sensor must be enabled to return meaningful values.
     *
     * @return  distance
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the distance from the object to the device")
    public float Distance() {
        return distance;
    }

    /**
     * Called when the accuracy of the registered sensor has changed
     * @param sensor Sensor
     * @param accuracy the new accuracy of this sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
