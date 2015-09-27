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
  @SimpleProperty(description = "The Red values detected by the sensor as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int Red() {
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
  @SimpleProperty(description = "The Green values detected by the sensor as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int Green() {
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
  @SimpleProperty(description = "The Blue values detected by the sensor as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int Blue() {
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

  @SimpleFunction(description = "Convert an HSV (hue, saturation, value) to " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int HSVToColor(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return Color.HSVToColor(array);
        }
      }
      form.dispatchErrorOccurredEvent(this, "HSVToColor",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "HSVToColor",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a specified alpha value and " +
      "an HSV (hue, saturation, value) to an integer ARGB (alpha, red, green, blue) color.")
  public int HSVToColorWithAlpha(int alpha, Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return Color.HSVToColor(alpha, array);
        }
      }
      form.dispatchErrorOccurredEvent(this, "HSVToColorWithAlpha",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "HSVToColorWithAlpha",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert red, green, and blue values to " +
      "an HSV (hue, saturation, value).")
  public Object RGBToHSV(int red, int green, int blue) {
    try {
      float[] array = new float[3];
      Color.RGBToHSV(red, green, blue, array);
      return array;
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RGBToHSV",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new float[3];
  }

  @SimpleFunction(description = "Return the alpha component of an integer ARGB (alpha, red, green, blue) color.")
  public int Alpha(int color) {
    try {
      return Color.alpha(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Alpha",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Return an integer ARGB (alpha, red, green, blue) color from " +
      "alpha, red, green, blue values.")
  public int ARGB(int alpha, int red, int green, int blue) {
    try {
      return Color.argb(alpha, red, green, blue);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ARGB",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Return the blue component of " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int Blue(int color) {
    try {
      return Color.blue(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Blue",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert an integer ARGB (alpha, red, green, blue) color to " +
      "an HSV (hue, saturation, value).")
  public Object ColorToHSV(int color) {
    try {
      float[] array = new float[3];
      Color.colorToHSV(color, array);
      return array;
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ColorToHSV",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new float[3];
  }

  @SimpleFunction(description = "Return the green component of " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int Green(int color) {
    try {
      return Color.green(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Green",
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

  @SimpleFunction(description = "Return the red component of " +
      "an integer ARGB (alpha, red, green, blue) color.")
  public int Red(int color) {
    try {
      return Color.red(color);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Red",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Return an integer ARGB (alpha, red, green, blue) color from " +
      "red, green, blue values.")
  public int RGB(int red, int green, int blue) {
    try {
      return Color.rgb(red, green, blue);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RGB",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Create an HSV (hue, saturation, value)")
  public Object CreateHSV(float hue, float saturation, float value) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return array[0];
        }
      }
      form.dispatchErrorOccurredEvent(this, "Hue",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Hue",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Return the hue from the given HSV (hue, saturation, value)")
  public float Hue(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return array[0];
        }
      }
      form.dispatchErrorOccurredEvent(this, "Hue",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Hue",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Return the saturation from the given HSV (hue, saturation, value).")
  public float Saturation(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return array[0];
        }
      }
      form.dispatchErrorOccurredEvent(this, "Saturation",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Saturation",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Return the value from the given HSV (hue, saturation, value).")
  public float Value(Object hsv) {
    try {
      if (hsv instanceof float[]) {
        float[] array = (float[]) hsv;
        if (array.length == 3) {
          return array[0];
        }
      }
      form.dispatchErrorOccurredEvent(this, "Value",
          ErrorMessages.ERROR_FTC_INVALID_HSV);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Value",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      colorSensor = hardwareMap.colorSensor.get(getDeviceName());
      if (colorSensor == null) {
        deviceNotFound("ColorSensor", hardwareMap.colorSensor);
      }
    }
    return colorSensor;
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    colorSensor = null;
  }
}
