// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * A component for a gamepad of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_GAMEPAD_COMPONENT_VERSION,
    description = "A component for a gamepad of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcGamepad extends AndroidNonvisibleComponent
    implements Component, OnDestroyListener, Deleteable, FtcRobotController.GamepadDevice {

  private volatile int gamepadNumber = 1;
  private volatile OpMode opMode;

  /**
   * Creates a new FtcGamepad component.
   */
  public FtcGamepad(ComponentContainer container) {
    super(container.$form());
    FtcRobotController.addGamepadDevice(this);
    form.registerForOnDestroy(this);
  }

  private Gamepad getGamepad() {
    if (opMode != null) {
      return (gamepadNumber == 2) ? opMode.gamepad2 : opMode.gamepad1;
    }
    return null;
  }

  /**
   * GamepadNumber property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The gamepad number.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int GamepadNumber() {
    return gamepadNumber;
  }

  /**
   * GamepadNumber property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_GAMEPAD_NUMBER,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void GamepadNumber(int gamepadNumber) {
    this.gamepadNumber = gamepadNumber;
  }

  /**
   * LeftStickX property getter.
   */
  @SimpleProperty(description = "The left analog stick horizontal axis value, " +
      "as a numeric value between -1.0 and +1.0.",
      category = PropertyCategory.BEHAVIOR)
  public float LeftStickX() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.left_stick_x;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "LeftStickX",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0f;
  }

  /**
   * LeftStickY property getter.
   */
  @SimpleProperty(description = "The left analog stick vertical axis value, " +
      "as a numeric value between -1.0 and +1.0.",
      category = PropertyCategory.BEHAVIOR)
  public float LeftStickY() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.left_stick_y;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "LeftStickY",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0f;
  }

  /**
   * RightStickX property getter.
   */
  @SimpleProperty(description = "The right analog stick horizontal axis value, " +
      "as a numeric value between -1.0 and +1.0.",
      category = PropertyCategory.BEHAVIOR)
  public float RightStickX() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.right_stick_x;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RightStickX",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0f;
  }

  /**
   * RightStickY property getter.
   */
  @SimpleProperty(description = "The right analog stick vertical axis value, " +
      "as a numeric value between -1.0 and +1.0.",
      category = PropertyCategory.BEHAVIOR)
  public float RightStickY() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.right_stick_y;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RightStickY",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0f;
  }

  /**
   * DpadUp property getter.
   */
  @SimpleProperty(description = "The dpad up value: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadUp() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.dpad_up;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "DpadUp",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * DpadDown property getter.
   */
  @SimpleProperty(description = "The dpad down value: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadDown() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.dpad_down;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "DpadDown",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * DpadLeft property getter.
   */
  @SimpleProperty(description = "The dpad left value: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadLeft() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.dpad_left;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "DpadLeft",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * DpadRight property getter.
   */
  @SimpleProperty(description = "The dpad right value: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadRight() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.dpad_right;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "DpadRight",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * A property getter.
   */
  @SimpleProperty(description = "The value of the A button: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean A() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.a;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "A",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * B property getter.
   */
  @SimpleProperty(description = "The value of the B button: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean B() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.b;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "B",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * X property getter.
   */
  @SimpleProperty(description = "The value of the X button: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean X() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.x;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "X",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * Y property getter.
   */
  @SimpleProperty(description = "The value of the Y button: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Y() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.y;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Y",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * Guide property getter.
   */
  @SimpleProperty(description = "The value of the Guide button: true if pressed, false " +
      "otherwise. The Guide button is often the large button in the middle of the controller.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Guide() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.guide;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Guide",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * Start property getter.
   */
  @SimpleProperty(description = "The value of the Start button: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Start() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.start;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Start",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * Back property getter.
   */
  @SimpleProperty(description = "The value of the Back button: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Back() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.back;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Back",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * LeftBumper property getter.
   */
  @SimpleProperty(description = "The left bumper value: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean LeftBumper() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.left_bumper;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "LeftBumper",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * RightBumper property getter.
   */
  @SimpleProperty(description = "The right bumper value: true if pressed, false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean RightBumper() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.right_bumper;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RightBumper",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * LeftStickButton property getter.
   */
  @SimpleProperty(description = "The value of the left stick button: true if pressed, " +
      "false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean LeftStickButton() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.left_stick_button;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "LeftStickButton",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * RightStickButton property getter.
   */
  @SimpleProperty(description = "The value of the right stick button: true if pressed, " +
      "false otherwise.",
      category = PropertyCategory.BEHAVIOR)
  public boolean RightStickButton() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.right_stick_button;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RightStickButton",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return false;
  }

  /**
   * LeftTrigger property getter.
   */
  @SimpleProperty(description = "The left trigger value, as a numeric value " +
      "between 0.0 and +1.0.",
      category = PropertyCategory.BEHAVIOR)
  public float LeftTrigger() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.left_trigger;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "LeftTrigger",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0f;
  }

  /**
   * RightTrigger property getter.
   */
  @SimpleProperty(description = "The right trigger value, as a numeric value " +
      "between 0.0 and +1.0.",
      category = PropertyCategory.BEHAVIOR)
  public float RightTrigger() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.right_trigger;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RightTrigger",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0f;
  }

  @SimpleFunction(description = "Set the joystick deadzone. Must be between 0 and 1.")
  public void SetJoystickDeadzone(float joystickDeadzone) {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        gamepad.setJoystickDeadzone(joystickDeadzone);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "SetJoystickDeadzone",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  /**
   * AtRest property getter.
   */
  @SimpleProperty(description = "Are all analog sticks and triggers in their rest position? " +
      "True or false.",
      category = PropertyCategory.BEHAVIOR)
  public boolean AtRest() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.atRest();
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "AtRest",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return true;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The status of the gamepad.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        String status = gamepad.toString();
        if (status != null) {
          return status;
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Status",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return "";
  }

  /**
   * Type property getter.
   */
  @SimpleProperty(description = "The type of gamepad.",
      category = PropertyCategory.BEHAVIOR)
  public String Type() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        String type = gamepad.type();
        if (type != null) {
          return type;
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Type",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return "";
  }

  @SimpleFunction(description = "Return text representing the state of the gamepad.")
  public String ToString() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        String s = gamepad.toString();
        if (s != null) {
          return s;
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ToString",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return "";
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    FtcRobotController.removeGamepadDevice(this);
    opMode = null;
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    FtcRobotController.removeGamepadDevice(this);
    opMode = null;
  }

  // FtcRobotController.GamepadDevice implementation

  @Override
  public void initGamepadDevice(OpMode opMode) {
    this.opMode = opMode;
  }

  @Override
  public void clearGamepadDevice() {
    opMode = null;
  }
}
