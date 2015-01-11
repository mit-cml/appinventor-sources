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
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.hitechnic.HiTechnicDeviceManager.DeviceType;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.util.SerialNumber;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A component that provides an interface to a servo controller of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_SERVO_CONTROLLER_COMPONENT_VERSION,
    description = "A component that provides an interface to a servo controller of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "hitechnic.jar,robotcore.jar")
public final class FtcServoController extends FtcRobotControllerChild {

  public interface Child {
    void createChild();
    void debugChild(StringBuilder sb);
    void destroyChild();
  }

  private String serialNumber = "";
  private ServoController servoController;
  private final List<Child> children = new ArrayList<Child>();

  /**
   * Creates a new FtcServoController component.
   */
  public FtcServoController(ComponentContainer container) {
    super(container, "FtcServoController");
  }

  ServoController getServoController() {
    return servoController;
  }

  /**
   * SerialNumber property getter method.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The serial number of the servo controller.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String SerialNumber() {
    return serialNumber;
  }

  /**
   * SerialNumber property setter method.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void SerialNumber(String serialNumber) {
    if (isAfterEventLoopInit()) {
      destroyServoController();
    }
    this.serialNumber = serialNumber;
    if (isAfterEventLoopInit()) {
      createServoController();
    }
  }

  /**
   * Adds a {@link Child} to the children list.
   */
  void addChild(Child child) {
    children.add(child);
  }

  /**
   * Removes a {@link Child} from the children list.
   */
  void removeChild(Child child) {
    children.remove(child);
  }

  private void createServoController() {
    try {
      // TODO: This creates a HiTechnicUsbServoController. We may need a property
      // (set only in designer) that indicates what kind of servo controller to create.
      servoController = getHiTechnicDeviceManager().createUsbServoController(
          new SerialNumber(serialNumber));
    } catch (RobotCoreException e) {
      Log.w("FtcServoController", "....HeyLiz caught " + e);
      form.dispatchErrorOccurredEvent(this, "",
          ErrorMessages.ERROR_FTC_FAILED_TO_CREATE_SERVO_CONTROLLER, serialNumber);
    }

    if (servoController != null) {
      servoController.pwmEnable();
      for (Child child : children) {
        child.createChild();
      }
    }
  }

  private void destroyServoController() {
    for (Child child : children) {
      child.destroyChild();
    }
    if (servoController != null) {
      servoController.pwmDisable();
      servoController.close();
      servoController = null;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyServoController();
  }

  // FtcRobotController.Child implementation

  @Override
  public void createChild() {
    createServoController();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("servoController is ").append((servoController == null) ? "null" : "not null").append("\n");
    for (Child child: children) {
      child.debugChild(sb);
    }
  }

  @Override
  public void destroyChild() {
    destroyServoController();
  }
}
