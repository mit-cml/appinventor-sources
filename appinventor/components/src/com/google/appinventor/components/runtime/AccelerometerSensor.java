// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.Sensitivity;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Non-visible component that can detect shaking and measure acceleration approximately in three
 * dimensions using SI units (m/s<sup>2</sup>). The components are:
 *
 * - **xAccel**: 0 when the phone is at rest on a flat surface, positive when the phone is tilted
 *   to the right (i.e., its left side is raised), and negative when the phone is tilted to the
 *   left (i.e., its right size is raised).
 * - **yAccel**: 0 when the phone is at rest on a flat surface, positive when its bottom is raised,
 *   and negative when its top is raised.
 * - **zAccel**: Equal to -9.8 (earth's gravity in meters per second per second when the device is
 *   at rest parallel to the ground with the display facing up, 0 when perpendicular to the ground,
 *   and +9.8 when facing down. The value can also be affected by accelerating it with or against
 *   gravity.
 *
 * @internaldoc
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
    "     0 when perpendicular to the ground, and +9.8 when facing down.  " +
    "     The value can also be affected by accelerating it with or against " +
    "     gravity. </li></ul>",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/accelerometersensor.png")
@SimpleObject
public class AccelerometerSensor extends AndroidNonvisibleComponent
    implements OnPauseListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable,
    RealTimeDataSource<String, Float> {

  // Logging and Debugging
  private final static String LOG_TAG = "AccelerometerSensor";
  private final static boolean DEBUG = true;

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
  private Sensitivity sensitivity;
  private volatile int deviceDefaultOrientation;

  private final SensorManager sensorManager;

  private final WindowManager windowManager;
  private final Resources resources;

  // Indicates whether the accelerometer should generate events
  private boolean enabled;

  //Specifies the minimum time interval between calls to Shaking()
  private int minimumInterval;

  //Specifies the time when Shaking() was last called
  private long timeLastShook;

  private Sensor accelerometerSensor;

  // Set to true to disable landscape mode tablet fix
  private boolean legacyMode = false;

  // Used to launch Runnables on the UI Thread after a delay
  private final Handler androidUIHandler;

  // Set of observers
  private final Set<DataSourceChangeListener> dataSourceObservers = new HashSet<>();

  /**
   * Creates a new AccelerometerSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public AccelerometerSensor(ComponentContainer container) {
    super(container.$form());
    form.registerForOnResume(this);
    form.registerForOnPause(this);

    enabled = true;
    resources = container.$context().getResources();
    windowManager = (WindowManager) container.$context().getSystemService(Context.WINDOW_SERVICE);
    sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    androidUIHandler = new Handler();
    startListening();
    MinimumInterval(400);
    SensitivityAbstract(Sensitivity.Moderate);
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
      description = "The minimum interval, in milliseconds, between phone shakes")
  public int MinimumInterval() {
    return minimumInterval;
  }

  /**
   * Specifies the minimum interval required between back-to-back {@link #Shaking()} events,
   * in milliseconds.
   * Once the phone starts being shaken, all further {@link #Shaking()} events will be ignored
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
  public @Options(Sensitivity.class) int Sensitivity() {
    return sensitivity.toUnderlyingValue();
  }

  /**
   * Returns the current sensitivity of this component.
   */
  @SuppressWarnings("RegularMethodName")
  public Sensitivity SensitivityAbstract() {
    return sensitivity;
  }

  /**
   * Sets the sensitivity of this sensor.
   */
  @SuppressWarnings("RegularMethodName")
  public void SensitivityAbstract(Sensitivity sensitivity) {
    this.sensitivity = sensitivity;
  }

  /**
   * Specifies the sensitivity of the accelerometer. Valid values are: `1` (weak), `2` (moderate),
   * and `3` (strong).
   *
   * @param sensitivity one of {@link Component#ACCELEROMETER_SENSITIVITY_WEAK},
   *          {@link Component#ACCELEROMETER_SENSITIVITY_MODERATE} or
   *          {@link Component#ACCELEROMETER_SENSITIVITY_STRONG}
   *
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ACCELEROMETER_SENSITIVITY,
      defaultValue = Component.ACCELEROMETER_SENSITIVITY_MODERATE + "")
  @SimpleProperty
  public void Sensitivity(@Options(Sensitivity.class) int sensitivity) {
    // Make sure sensitivity is a valid Sensitivity.
    Sensitivity level = Sensitivity.fromUnderlyingValue(sensitivity);
    if (level == null) {
      form.dispatchErrorOccurredEvent(this, "Sensitivity",
          ErrorMessages.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY, sensitivity);
      return;
    }
    SensitivityAbstract(level);
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

    // Notify the Data Source observers with the updated values
    notifyDataObservers("X", xAccel);
    notifyDataObservers("Y", yAccel);
    notifyDataObservers("Z", zAccel);

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

public int getDeviceDefaultOrientation() {
    if (Build.VERSION.SDK_INT < SdkLevel.LEVEL_FROYO) {
      // getRotation() is unavailable on versions of Android lower tha Froyo, so assume a default
      // orientation of PORTRAIT (which was the implied assumption before we added this check).
      return Configuration.ORIENTATION_PORTRAIT;
    }
    Configuration config = resources.getConfiguration();
    int rotation = windowManager.getDefaultDisplay().getRotation();
    if (DEBUG) {
      Log.d(LOG_TAG, "rotation = " + rotation);
      Log.d(LOG_TAG, "config.orientation = " + config.orientation);
    }
    // return config.orientation;

    if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
            config.orientation == Configuration.ORIENTATION_LANDSCAPE)
        || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
            config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
      return Configuration.ORIENTATION_LANDSCAPE;
    } else {
      return Configuration.ORIENTATION_PORTRAIT;
    }
}

  /**
   * Indicates the device started being shaken or continues to be shaken.
   */
  @SimpleEvent
  public void Shaking() {
    EventDispatcher.dispatchEvent(this, "Shaking");
  }

  /**
   * Returns whether the `AccelerometerSensor` hardware is available on the device.
   *
   * @return {@code true} indicates that an accelerometer sensor is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(
      description = "Returns whether the accelerometer is available on the device.",
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
    // save the device default orientation (portrait or landscape)
    androidUIHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          AccelerometerSensor.this.deviceDefaultOrientation = getDeviceDefaultOrientation();
          if (DEBUG) {
            Log.d(LOG_TAG, "deviceDefaultOrientation = " + AccelerometerSensor.this.deviceDefaultOrientation);
            Log.d(LOG_TAG, "Configuration.ORIENTATION_LANDSCAPE = " + Configuration.ORIENTATION_LANDSCAPE);
            Log.d(LOG_TAG, "Configuration.ORIENTATION_PORTRAIT = " + Configuration.ORIENTATION_PORTRAIT);
          }
        }
      }, 32);                   // Wait 32ms for the UI to settle down

    sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
  }

  // Assumes that sensorManager has been initialized, which happens in constructor
  private void stopListening() {
    sensorManager.unregisterListener(this);
  }

  /**
   * Specifies whether the sensor should generate events.  If `true`{:.logic.block},
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
   * Returns the acceleration in the X-dimension in SI units (m/s²).
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
   * Returns the acceleration in the Y-dimension in SI units (m/s²).
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
   * Returns the acceleration in the Z-dimension in SI units (m/s²).
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
    float delta = Math.abs(average - currentValue);

    switch (sensitivity) {
      case Weak:
        return delta > strongShakeThreshold;
      case Moderate:
        return delta > moderateShakeThreshold && delta < strongShakeThreshold;
      case Strong:
        return delta > weakShakeThreshold && delta < moderateShakeThreshold;
      default:
        return false;
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "False")
  @SimpleProperty(userVisible = false,
    description="Prior to the release that added this property the AccelerometerSensor " +
    "component passed through sensor values directly as received from the " +
    "Android system. However these values do not compensate for tablets " +
    "that default to Landscape mode, requiring the MIT App Inventor " +
    "programmer to compensate. However compensating would result in " +
    "incorrect results in Portrait mode devices such as phones. " +
    "We now detect Landscape mode tablets and perform the compensation. " +
    "However if your project is already compensating for the change, you " +
    "will now get incorrect results. Although our preferred solution is for " +
    "you to update your project, you can also just set this property to “true” " +
    "and our compensation code will be deactivated. Note: We recommend that " +
    "you update your project as we may remove this property in a future " +
    "release.",
    category = PropertyCategory.BEHAVIOR)
  public void LegacyMode(boolean legacyMode) {
    this.legacyMode = legacyMode;
  }

  public boolean LegacyMode() {
    return legacyMode;
  }

  // SensorListener implementation
  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled) {
      final float[] values = sensorEvent.values;
      // make landscapePrimary devices report acceleration as if they were
      // portraitPrimary
      if ((deviceDefaultOrientation == Configuration.ORIENTATION_LANDSCAPE) &&
          !legacyMode) {
        xAccel = values[1];
        yAccel = -values[0];
      } else {
        xAccel = values[0];
        yAccel = values[1];
      }
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

  // OnPauseListener implementation

  @Override
  public void onPause() {
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

  @Override
  public void addDataObserver(DataSourceChangeListener dataComponent) {
    dataSourceObservers.add(dataComponent);
  }

  @Override
  public void removeDataObserver(DataSourceChangeListener dataComponent) {
    dataSourceObservers.remove(dataComponent);
  }

  @Override
  public void notifyDataObservers(String key, Object value) {
    // Notify each Chart Data observer component of the Data value change
    for (DataSourceChangeListener dataComponent : dataSourceObservers) {
      dataComponent.onReceiveValue(this, key, value);
    }
  }

  /**
   * Returns a data value.
   *
   * <p>The key can be one of:</p>
   * <ul>
   * <li>X - x direction acceleration</li>
   * <li>Y - y direction acceleration</li>
   * <li>Z - z direction acceleration</li>
   * </ul>
   *
   * @param key identifier of the value
   * @return    Value corresponding to the key, or 0 if key is undefined.
   */
  @Override
  public Float getDataValue(String key) {
    switch (key) {
      case "X":
        return xAccel;

      case "Y":
        return yAccel;

      case "Z":
        return zAccel;

      default:
        return 0f;
    }
  }
}
