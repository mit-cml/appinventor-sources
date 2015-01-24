// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
   * JoystickDeadzone property setter.
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
   * AtRest property getter.
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
   * LeftStickX property getter.
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
   * LeftStickY property getter.
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
   * RightStickX property getter.
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
   * RightStickY property getter.
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
   * DpadUp property getter.
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
   * DpadDown property getter.
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
   * DpadLeft property getter.
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
   * DpadRight property getter.
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
   * A property getter.
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
   * B property getter.
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
   * X property getter.
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
   * Y property getter.
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
   * Guide property getter.
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
   * Start property getter.
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
   * Back property getter.
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
   * LeftBumper property getter.
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
   * RightBumper property getter.
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
   * LeftTrigger property getter.
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
   * RightTrigger property getter.
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

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The status of the gamepad.",
      category = PropertyCategory.BEHAVIOR)
  public String status() {
    Gamepad gamepad = getGamepad();
    if (gamepad != null) {
      return gamepad.toString();
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
  }
}
