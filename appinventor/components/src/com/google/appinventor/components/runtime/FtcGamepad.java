// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.hardware.Gamepad;

import android.util.Log;

/**
 * A component that provides an interface to a gamepad of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_GAMEPAD_COMPONENT_VERSION,
    description = "A component that provides an interface to a gamepad of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "robotcore.jar")
public final class FtcGamepad extends FtcRobotControllerChild {

  private int gamepadIndex; // Must be 0 or 1. 0 is the default.

  /**
   * Creates a new FtcGamepad component.
   */
  public FtcGamepad(ComponentContainer container) {
    super(container, "FtcGamepad");
  }

  private Gamepad getGamepad() {
    // TODO(4.0): Try to get the gamepad from the active OpMode, fallback to getting the gamepad
    // from the EventLoopManager if there is no active OpMode.
    EventLoopManager eventLoopManager = getEventLoopManager();
    if (eventLoopManager != null) {
      return eventLoopManager.getGamepad(gamepadIndex);
    }
    return null;
  }

  /**
   * The GamepadNumber property getter method.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The gamepad number, either 1 or 2.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int GamepadNumber() {
    return gamepadIndex + 1;
  }

  /**
   * The GamepadNumber property setter method.
   * Can only be set in designer; not visible in blocks.
   *
   * @param gamepadNumber either 1 or 2
   * @throws IllegalArgumentException if gamepadNumber is not 1 or 2.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_GAMEPAD_NUMBER,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void GamepadNumber(int gamepadNumber) {
    if (gamepadNumber != 1 && gamepadNumber != 2) {
      throw new IllegalArgumentException();
    }
    this.gamepadIndex = gamepadNumber - 1;
  }

  /**
   * The JoystickDeadzone property setter method
   */
  @SimpleProperty(description = "Set the joystick deadzone; must be between 0 and 1.")
  public void JoystickDeadzone(int joystickDeadzone) {
    if (joystickDeadzone >= 0.0f && joystickDeadzone <= 1.0f) {
      Gamepad gamepad = getGamepad();
      if (gamepad != null) {
        gamepad.setJoystickDeadzone(joystickDeadzone);
      }
    }
  }

  /**
   * The AtRest property getter method
   */
  @SimpleProperty(description = "Are all analog sticks and triggers in their rest position?",
      category = PropertyCategory.BEHAVIOR)
  public boolean AtRest() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.atRest();
    }
    return true;
  }

  /**
   * The LeftStickX property getter method
   */
  @SimpleProperty(description = "The left analog stick horizontal axis value.",
      category = PropertyCategory.BEHAVIOR)
  public float LeftStickX() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.left_stick_x;
    }
    return 0f;
  }

  /**
   * The LeftStickY property getter method
   */
  @SimpleProperty(description = "The left analog stick vertical axis value.",
      category = PropertyCategory.BEHAVIOR)
  public float LeftStickY() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.left_stick_y;
    }
    return 0f;
  }

  /**
   * The RightStickX property getter method
   */
  @SimpleProperty(description = "The right analog stick horizontal axis value.",
      category = PropertyCategory.BEHAVIOR)
  public float RightStickX() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.right_stick_x;
    }
    return 0f;
  }

  /**
   * The RightStickY property getter method
   */
  @SimpleProperty(description = "The right analog stick vertical axis value.",
      category = PropertyCategory.BEHAVIOR)
  public float RightStickY() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.right_stick_y;
    }
    return 0f;
  }

  /**
   * The DpadUp property getter method
   */
  @SimpleProperty(description = "The dpad up value.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadUp() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.dpad_up;
    }
    return false;
  }

  /**
   * The DpadDown property getter method
   */
  @SimpleProperty(description = "The dpad down value.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadDown() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.dpad_down;
    }
    return false;
  }

  /**
   * The DpadLeft property getter method
   */
  @SimpleProperty(description = "The dpad left value.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadLeft() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.dpad_left;
    }
    return false;
  }

  /**
   * The DpadRight property getter method
   */
  @SimpleProperty(description = "The dpad right value.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DpadRight() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.dpad_right;
    }
    return false;
  }

  /**
   * The A property getter method
   */
  @SimpleProperty(description = "The value of the A button.",
      category = PropertyCategory.BEHAVIOR)
  public boolean A() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.a;
    }
    return false;
  }

  /**
   * The B property getter method
   */
  @SimpleProperty(description = "The value of the B button.",
      category = PropertyCategory.BEHAVIOR)
  public boolean B() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.b;
    }
    return false;
  }

  /**
   * The X property getter method
   */
  @SimpleProperty(description = "The value of the X button.",
      category = PropertyCategory.BEHAVIOR)
  public boolean X() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.x;
    }
    return false;
  }

  /**
   * The Y property getter method
   */
  @SimpleProperty(description = "The value of the Y button.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Y() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.y;
    }
    return false;
  }

  /**
   * The Guide property getter method
   */
  @SimpleProperty(description = "The value of the Guide button. " +
      "The Guide button is often the large button in the middle of the controller.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Guide() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.guide;
    }
    return false;
  }

  /**
   * The Start property getter method
   */
  @SimpleProperty(description = "The value of the Start button.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Start() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.start;
    }
    return false;
  }

  /**
   * The Back property getter method
   */
  @SimpleProperty(description = "The value of the Back button.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Back() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.back;
    }
    return false;
  }

  /**
   * The LeftBumper property getter method
   */
  @SimpleProperty(description = "The left bumper value.",
      category = PropertyCategory.BEHAVIOR)
  public boolean LeftBumper() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.left_bumper;
    }
    return false;
  }

  /**
   * The RightBumper property getter method
   */
  @SimpleProperty(description = "The right bumper value.",
      category = PropertyCategory.BEHAVIOR)
  public boolean RightBumper() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.right_bumper;
    }
    return false;
  }

  /**
   * The LeftTrigger property getter method
   */
  @SimpleProperty(description = "The left trigger value.",
      category = PropertyCategory.BEHAVIOR)
  public float LeftTrigger() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.left_trigger;
    }
    return 0f;
  }

  /**
   * The RightTrigger property getter method
   */
  @SimpleProperty(description = "The right trigger value.",
      category = PropertyCategory.BEHAVIOR)
  public float RightTrigger() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.right_trigger;
    }
    return 0f;
  }
}
