// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import android.graphics.Color;

/**
 * A component for a color sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_COLOR_SENSOR_COMPONENT_VERSION,
    description = "A component for a color sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcColorSensor extends FtcHardwareDevice {

  private volatile ColorSensor colorSensor;

  /**
   * Creates a new FtcColorSensor component.
   */
  public FtcColorSensor(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Red property getter.
   */
  @SimpleProperty(description = "The red value detected by the sensor as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int Red() {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        return colorSensor.red();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Red",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Green property getter.
   */
  @SimpleProperty(description = "The green value detected by the sensor as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int Green() {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        return colorSensor.green();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Green",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Blue property getter.
   */
  @SimpleProperty(description = "The blue value detected by the sensor as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int Blue() {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        return colorSensor.blue();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Blue",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Alpha property getter.
   */
  @SimpleProperty(description = "The amount of light detected by the sensor as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int Alpha() {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        return colorSensor.alpha();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Alpha",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * ARGB property getter.
   */
  @SimpleProperty(description = "The color detected by the sensor as " +
      "an integer ARGB (alpha, red, green, blue) color.",
      category = PropertyCategory.BEHAVIOR)
  public int ARGB() {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        return colorSensor.argb();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ARGB",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Enable the LED light.")
  public void EnableLed(boolean enable) {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        colorSensor.enableLed(enable);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableLed",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // Color functions

  @SimpleFunction(description = "Convert an integer ARGB (alpha, red, green, blue) color to " +
      "an HSV (hue, saturation, value).")
  public Object ConvertColorToHSV(int color) {
    try {
      float[] array = new float[3];
      Color.colorToHSV(color, array);
      return array;
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertColorToHSV",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new float[3];
  }

  @SimpleFunction(description = "Convert an HSV (hue, saturation, value) to " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int ConvertHSVToColor(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return Color.HSVToColor(array);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertHSVToColor",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertHSVToColor",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a specified alpha value and " +
      "an HSV (hue, saturation, value) to an integer ARGB (alpha, red, green, blue) color.")
  public int ConvertHSVToColorWithAlpha(int alpha, Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return Color.HSVToColor(alpha, array);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertHSVToColorWithAlpha",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertHSVToColorWithAlpha",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert red, green, and blue values to " +
      "an HSV (hue, saturation, value).")
  public Object ConvertRGBToHSV(int red, int green, int blue) {
    try {
      float[] array = new float[3];
      Color.RGBToHSV(red, green, blue, array);
      return array;
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertRGBToHSV",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new float[3];
  }

  @SimpleFunction(description = "Create an integer ARGB (alpha, red, green, blue) color from " +
      "alpha, red, green, blue values.")
  public int CreateARGB(int alpha, int red, int green, int blue) {
    try {
      return Color.argb(alpha, red, green, blue);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "CreateARGB",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Create an HSV (hue, saturation, value).")
  public Object CreateHSV(float hue, float saturation, float value) {
    try {
      float[] array = new float[3];
      array[0] = hue;
      array[1] = saturation;
      array[2] = value;
      return array;
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "CreateHSV",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Create an integer ARGB (alpha, red, green, blue) color from " +
      "red, green, blue values.")
  public int CreateRGB(int red, int green, int blue) {
    try {
      return Color.rgb(red, green, blue);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "CreateRGB",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Extract the alpha component of " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int ExtractAlpha(int color) {
    try {
      return Color.alpha(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ExtractAlpha",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Extract the red component of " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int ExtractRed(int color) {
    try {
      return Color.red(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ExtractRed",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Extract the green component of " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int ExtractGreen(int color) {
    try {
      return Color.green(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ExtractGreen",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Extract the blue component of " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int ExtractBlue(int color) {
    try {
      return Color.blue(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ExtractBlue",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Extract the hue from the given HSV (hue, saturation, value).")
  public float ExtractHue(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return array[0];
        }
      }
      form.dispatchErrorOccurredEvent(this, "ExtractHue",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ExtractHue",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Extract the saturation from the given " +
      "HSV (hue, saturation, value).")
  public float ExtractSaturation(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return array[1];
        }
      }
      form.dispatchErrorOccurredEvent(this, "ExtractSaturation",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ExtractSaturation",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Extract the value from the given HSV (hue, saturation, value).")
  public float ExtractValue(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return array[2];
        }
      }
      form.dispatchErrorOccurredEvent(this, "ExtractValue",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ExtractValue",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(
      description = "Parse the color string, and return the corresponding ARGB (alpha, red, green, blue) color.\n" +
      "Supported formats are: #RRGGBB #AARRGGBB or one of the following names: 'red',\n" +
      "'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray',\n" +
      "'darkgray', 'grey', 'lightgrey', 'darkgrey', 'aqua', 'fuchsia', 'lime', 'maroon',\n" +
      "'navy', 'olive', 'purple', 'silver', 'teal'.")
  public int ParseColor(String colorText) {
    try {
      return Color.parseColor(colorText);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ParseColor",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  /**
   * MAX_NEW_I2C_ADDRESS property getter.
   */
  @SimpleProperty(description = "The constant for MAX_NEW_I2C_ADDRESS.",
      category = PropertyCategory.BEHAVIOR)
  public int MAX_NEW_I2C_ADDRESS() {
    return ModernRoboticsUsbDeviceInterfaceModule.MAX_NEW_I2C_ADDRESS;
  }

  /**
   * MIN_NEW_I2C_ADDRESS property getter.
   */
  @SimpleProperty(description = "The constant for MIN_NEW_I2C_ADDRESS.",
      category = PropertyCategory.BEHAVIOR)
  public int MIN_NEW_I2C_ADDRESS() {
    return ModernRoboticsUsbDeviceInterfaceModule.MIN_NEW_I2C_ADDRESS;
  }

  /**
   * I2cAddress property setter.
   */
  @SimpleProperty
  public void I2cAddress(int newAddress) {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        colorSensor.setI2cAddress(newAddress);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "I2cAddress",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * I2cAddress property getter.
   */
  @SimpleProperty(description = "The I2C address of the color sensor. " + 
      "Not all color sensors support this feature.",
      category = PropertyCategory.BEHAVIOR)
  public int I2cAddress() {
    checkHardwareDevice();
    if (colorSensor != null) {
      try {
        return colorSensor.getI2cAddress();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "I2cAddress",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    colorSensor = hardwareMap.colorSensor.get(getDeviceName());
    return colorSensor;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("ColorSensor", hardwareMap.colorSensor);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    colorSensor = null;
  }
}
