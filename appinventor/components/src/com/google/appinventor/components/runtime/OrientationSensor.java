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
import com.google.appinventor.components.runtime.util.FroyoUtil;
import com.google.appinventor.components.runtime.util.OrientationSensorUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Sensor that can measure absolute orientation in 3 dimensions.
 *
 */
@DesignerComponent(version = YaVersion.ORIENTATIONSENSOR_COMPONENT_VERSION,
    description = "<p>Non-visible component providing information about the " +
    "device's physical orientation in three dimensions: <ul> " +
    "<li> <strong>Roll</strong>: 0 degrees when the device is level, increases to " +
    "     90 degrees as the device is tilted up on its left side, and " +
    "     decreases to -90 degrees when the device is tilted up on its right side. " +
    "     </li> " +
    "<li> <strong>Pitch</strong>: 0 degrees when the device is level, up to " +
    "     90 degrees as the device is tilted so its top is pointing down, " +
    "     up to 180 degrees as it gets turned over.  Similarly, as the device " +
    "     is tilted so its bottom points down, pitch decreases to -90 " +
    "     degrees, then further decreases to -180 degrees as it gets turned all the way " +
    "     over.</li> " +
    "<li> <strong>Azimuth</strong>: 0 degrees when the top of the device is " +
    "     pointing north, 90 degrees when it is pointing east, 180 degrees " +
    "     when it is pointing south, 270 degrees when it is pointing west, " +
    "     etc.</li></ul>" +
    "     These measurements assume that the device itself is not moving.</p>",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/orientationsensor.png")

@SimpleObject
public class OrientationSensor extends AndroidNonvisibleComponent
    implements SensorEventListener, Deleteable, OnPauseListener, OnResumeListener {
  // Constants
  private static final String LOG_TAG = "OrientationSensor";
  // offsets in array returned by SensorManager.getOrientation()
  private static final int AZIMUTH = 0;
  private static final int PITCH = 1;
  private static final int ROLL = 2;
  private static final int DIMENSIONS = 3;  // Warning: specific to our universe

  // Properties
  private boolean enabled;
  private float azimuth;    // degrees
  private float pitch;  // degrees
  private float roll;   // degrees
  private int accuracy;

  // Sensor information
  private final SensorManager sensorManager;
  private final Sensor accelerometerSensor;
  private final Sensor magneticFieldSensor;
  private boolean listening;

  // Pre-allocated arrays to hold sensor data so that we don't cause so many garbage collections
  // while processing sensor events. All are used only in onSensorChanged.
  private final float[] accels = new float[DIMENSIONS];  // acceleration vector
  private final float[] mags = new float[DIMENSIONS];    // magnetic field vector

  // Flags to tell whether the above arrays are filled. They are set in onSensorChanged and cleared
  // in stopListening.
  private boolean accelsFilled;
  private boolean magsFilled;

  // Pre-allocated matrixes used to compute orientation values from acceleration and magnetic
  // field data.
  private final float[] rotationMatrix = new float[DIMENSIONS * DIMENSIONS];
  private final float[] inclinationMatrix = new float[DIMENSIONS * DIMENSIONS];
  private final float[] values = new float[DIMENSIONS];

  /**
   * Creates a new OrientationSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public OrientationSensor(ComponentContainer container) {
    super(container.$form());

    // Get sensors, and start listening.
    sensorManager =
      (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    // Begin listening in onResume() and stop listening in onPause().
    form.registerForOnResume(this);
    form.registerForOnPause(this);

    // Set default property values.
    Enabled(true);
  }

  private void startListening() {
    if (!listening) {
      sensorManager.registerListener(this, accelerometerSensor,
                                     SensorManager.SENSOR_DELAY_NORMAL);
      sensorManager.registerListener(this, magneticFieldSensor,
                                     SensorManager.SENSOR_DELAY_NORMAL);
      listening = true;
    }
  }

  private void stopListening() {
    if (listening) {
      sensorManager.unregisterListener(this);
      listening = false;

      // Throw out sensor information that will go stale.
      accelsFilled = false;
      magsFilled = false;
    }
  }

  // Events

  /**
   * Default OrientationChanged event handler.
   *
   * <p>This event is signalled when the device's orientation has changed.  It
   * reports the new values of azimuth, pich, and roll, and it also sets the Azimuth, Pitch,
   * and roll properties.</p>
   * <p>Azimuth is the compass heading in degrees, pitch indicates how the device
   * is tilted from top to bottom, and roll indicates how much the device is tilted from
   * side to side.</p>
   */
  @SimpleEvent
  public void OrientationChanged(float azimuth, float pitch, float roll) {
    EventDispatcher.dispatchEvent(this, "OrientationChanged", azimuth, pitch, roll);
  }

  // Properties

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that an orientation sensor is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Available() {
    return sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() > 0
        && sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size() > 0;
  }

  /**
   * Enabled property getter method.
   *
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
  }

  /**
   * Enabled property setter method.
   *
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    if (this.enabled != enabled) {
      this.enabled = enabled;
      if (enabled) {
        startListening();
      } else {
        stopListening();
      }
    }
  }

  /**
   * Pitch property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return  current pitch
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public float Pitch() {
    return pitch;
  }

  /**
   * Roll property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return  current roll
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public float Roll() {
    return roll;
  }

  /**
   * Azimuth property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return  current azimuth
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public float Azimuth() {
    return azimuth;
  }

  /**
   * <p>Angle property getter method (read-only property).  Specifically, this
   * provides the angle in which the orientation sensor is tilted, treating
   * -{@link #Roll()} as the x-coordinate and {@link #Pitch()} as the
   * y-coordinate.  For the amount of the tilt, use {@link #Magnitude()}.</p>
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return the angle in degrees
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public float Angle() {
    return OrientationSensor.computeAngle(pitch, roll);
  }

  /**
   * Computes the angle the phone is tilted.  This has been lifted out
   * of {@link #Angle()} for ease of testing.
   *
   * @param pitch an angle indicating how far the device is tilted vertically,
   *              with a value of +90 degrees if the top is pointing straight
   *              down, +180 degrees if the phone is entirely turned over,
   *              -90/+270 degrees if the top is pointing straight up, etc.
   * @param roll an angle indicating how far the device is tilted horizontally,
   *             with a value of +90 degrees if it is tilted entirely on its
   *             left side, -90 degrees if it is tilted entirely on its right
   *             side; the maximum absolute value of roll is 90 degrees, after
   *             which it decreases back toward 0 (flat face-up or face-down).
   *
   * @returns the corresonding angle in the range [-180, +180] degrees
   */
  static float computeAngle(float pitch, float roll) {
    return (float) Math.toDegrees(Math.atan2(Math.toRadians(pitch),
                                             // invert roll to correct sign
                                             -Math.toRadians(roll)));
  }

  /**
   * Magnitude property getter method (read-only property).  Specifically, this
   * returns a number between 0 and 1, indicating how much the device
   * is tilted.  For the angle of tilt, use {@link #Angle()}.
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return the magnitude of the tilt, from 0 to 1
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public float Magnitude() {
    // Limit pitch and roll to 90; otherwise, the phone is upside down.
    // The official documentation falsely claims that the range of pitch and
    // roll is [-90, 90].  If the device is upside-down, it can range from
    // -180 to 180.  We restrict it to the range [-90, 90].
    // With that restriction, if the pitch and roll angles are P and R, then
    // the force is given by 1 - cos(P)cos(R).  I have found a truly wonderful
    // proof of this theorem, but the margin enforced by Lint is too small to
    // contain it.
    final int MAX_VALUE = 90;
    double npitch = Math.toRadians(Math.min(MAX_VALUE, Math.abs(pitch)));
    double nroll = Math.toRadians(Math.min(MAX_VALUE, Math.abs(roll)));
    return (float) (1.0 - Math.cos(npitch) * Math.cos(nroll));
  }

  // SensorListener implementation

  /*
   * Returns the rotation of the screen from its "natural" orientation.
   * Note that this is the angle of rotation of the drawn graphics on the
   * screen, which is the opposite direction of the physical rotation of the
   * device. For example, if the device is rotated 90 degrees counter-clockwise,
   * to compensate rendering will be rotated by 90 degrees clockwise and thus
   * the returned value here will be Surface.ROTATION_90.  Return values will
   * be in the set Surface.ROTATION_{0,90,180,270}.
   */
  private int getScreenRotation() {
    Display display =
        ((WindowManager) form.getSystemService(Context.WINDOW_SERVICE)).
        getDefaultDisplay();
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO) {
      return FroyoUtil.getRotation(display);
    } else {
      return display.getOrientation();
    }
  }

  /**
   * Responds to changes in the accelerometer or magnetic field sensors to
   * recompute orientation.  This only updates azimuth, pitch, and roll and
   * raises the OrientationChanged event if both sensors have reported in
   * at least once.
   *
   * @param sensorEvent an event from the accelerometer or magnetic field sensor
   */
  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled) {
      int eventType = sensorEvent.sensor.getType();

      // Save the new sensor information about acceleration or the magnetic field.
      switch (eventType) {
        case Sensor.TYPE_ACCELEROMETER:
          // Update acceleration array.
          System.arraycopy(sensorEvent.values, 0, accels, 0, DIMENSIONS);
          accelsFilled = true;
          // Only update the accuracy property for the accelerometer.
          accuracy = sensorEvent.accuracy;
          break;

        case Sensor.TYPE_MAGNETIC_FIELD:
          // Update magnetic field array.
          System.arraycopy(sensorEvent.values, 0, mags, 0, DIMENSIONS);
          magsFilled = true;
          break;

        default:
          Log.e(LOG_TAG, "Unexpected sensor type: " + eventType);
          return;
      }

      // If we have both acceleration and magnetic information, recompute values.
      if (accelsFilled && magsFilled) {
        SensorManager.getRotationMatrix(rotationMatrix,    // output
                                        inclinationMatrix, // output
                                        accels,
                                        mags);
        SensorManager.getOrientation(rotationMatrix, values);

        // Make sure values are in expected range.
        azimuth = OrientationSensorUtil.normalizeAzimuth(
            (float) Math.toDegrees(values[AZIMUTH]));
        pitch = OrientationSensorUtil.normalizePitch(
            (float) Math.toDegrees(values[PITCH]));
        // Sign change for roll is for compatibility with earlier versions
        // of App Inventor that got orientation sensor information differently.
        roll = OrientationSensorUtil.normalizeRoll(
            (float) -Math.toDegrees(values[ROLL]));

        // Adjust pitch and roll for phone rotation (e.g., landscape)
        int rotation = getScreenRotation();
        switch(rotation) {
          case Surface.ROTATION_0:  // normal rotation
            break;
          case Surface.ROTATION_90:  // phone is turned 90 degrees counter-clockwise
            float temp = -pitch;
            pitch = -roll;
            roll = temp;
            break;
          case Surface.ROTATION_180: // phone is rotated 180 degrees
            roll = -roll;
            break;
          case Surface.ROTATION_270:  // phone is turned 90 degrees clockwise
            temp = pitch;
            pitch = roll;
            roll = temp;
            break;
          default:
            Log.e(LOG_TAG, "Illegal value for getScreenRotation(): " +
                  rotation);
            break;
        }

        // Raise event.
        OrientationChanged(azimuth, pitch, roll);
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO(markf): Figure out if we actually need to do something here.
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    stopListening();
  }

  // OnPauseListener implementation

  public void onPause() {
    stopListening();
  }

  // OnResumeListener implementation

  public void onResume() {
    if (enabled) {
      startListening();
    }
  }
}
