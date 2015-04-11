// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.eventloop.EventLoopManager;
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
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcGamepad extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcRobotController.GamepadDevice {

  private static final float DEFAULT_JOYSTICK_DEADZONE = 0.2f;

  private volatile float joystickDeadzone = DEFAULT_JOYSTICK_DEADZONE;
  private volatile int gamepadIndex;
  private volatile EventLoopManager eventLoopManager;

  /**
   * Creates a new FtcGamepad component.
   */
  public FtcGamepad(ComponentContainer container) {
    super(container.$form());
    FtcRobotController.addGamepadDevice(form, this);
  }

  // Properties

  /**
   * GamepadNumber property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The gamepad number.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int GamepadNumber() {
    return gamepadIndex + 1;
  }

  /**
   * GamepadNumber property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_GAMEPAD_NUMBER,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void GamepadNumber(int gamepadNumber) {
    gamepadIndex = gamepadNumber - 1;
  }

  /**
   * JoystickDeadzone property getter.
   */
  @SimpleProperty(description = "The joystick deadzone, between 0.0 and 1.0.",
      category = PropertyCategory.BEHAVIOR)
  public float JoystickDeadzone() {
    return joystickDeadzone;
  }

  /**
   * JoystickDeadzone property setter.
   */
  @SimpleProperty
  public void JoystickDeadzone(float joystickDeadzone) {
    if (joystickDeadzone >= 0.0f && joystickDeadzone <= 1.0f) {
      this.joystickDeadzone = joystickDeadzone;
      try {
        Gamepad gamepad = getGamepad();
        if (gamepad != null) {
          gamepad.setJoystickDeadzone(joystickDeadzone);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "JoystickDeadzone",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * AtRest property getter.
   */
  @SimpleProperty(description = "Are all analog sticks and triggers in their rest position?",
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
   * LeftStickX property getter.
   */
  @SimpleProperty(description = "The left analog stick horizontal axis value.",
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
  @SimpleProperty(description = "The left analog stick vertical axis value.",
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
  @SimpleProperty(description = "The right analog stick horizontal axis value.",
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
  @SimpleProperty(description = "The right analog stick vertical axis value.",
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
  @SimpleProperty(description = "The dpad up value.",
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
  @SimpleProperty(description = "The dpad down value.",
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
  @SimpleProperty(description = "The dpad left value.",
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
  @SimpleProperty(description = "The dpad right value.",
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
  @SimpleProperty(description = "The value of the A button.",
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
  @SimpleProperty(description = "The value of the B button.",
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
  @SimpleProperty(description = "The value of the X button.",
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
  @SimpleProperty(description = "The value of the Y button.",
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
  @SimpleProperty(description = "The value of the Guide button. " +
      "The Guide button is often the large button in the middle of the controller.",
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
  @SimpleProperty(description = "The value of the Start button.",
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
  @SimpleProperty(description = "The value of the Back button.",
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
  @SimpleProperty(description = "The left bumper value.",
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
  @SimpleProperty(description = "The right bumper value.",
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
   * LeftTrigger property getter.
   */
  @SimpleProperty(description = "The left trigger value.",
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
  @SimpleProperty(description = "The right trigger value.",
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

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The status of the gamepad.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    try {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        return gamepad.toString();
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Status",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return "";
  }

  private Gamepad getGamepad() {
    if (eventLoopManager != null) {
      Gamepad[] gamepads = eventLoopManager.getGamepads();
      if (gamepads != null && gamepadIndex >= 0 && gamepadIndex < gamepads.length) {
        return gamepads[gamepadIndex];
      }
    }
    return null;
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    FtcRobotController.removeGamepadDevice(form, this);
    eventLoopManager = null;
  }

  // FtcRobotController.GamepadDevice implementation

  @Override
  public void setEventLoopManager(EventLoopManager eventLoopManager) {
    this.eventLoopManager = eventLoopManager;
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      gamepad.setJoystickDeadzone(joystickDeadzone);
    }
  }
}
