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
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.SerialNumber;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A component that provides an interface to a DC motor controller of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_DC_MOTOR_CONTROLLER_COMPONENT_VERSION,
    description = "A component that provides an interface to a DC motor controller of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "hitechnic.jar,robotcore.jar")
public final class FtcDcMotorController extends FtcRobotControllerChild {

  public interface Child {
    void createChild();
    void debugChild(StringBuilder sb);
    void destroyChild();
  }

  private String serialNumber = "";
  private DcMotorController dcMotorController;
  private final List<Child> children = new ArrayList<Child>();

  /**
   * Creates a new FtcDcMotorController component.
   */
  public FtcDcMotorController(ComponentContainer container) {
    super(container, "FtcDcMotorController");
  }

  DcMotorController getDcMotorController() {
    return dcMotorController;
  }

  /**
   * SerialNumber property getter method.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The serial number of the DC motor controller.",
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
      destroyDcMotorController();
    }
    this.serialNumber = serialNumber;
    if (isAfterEventLoopInit()) {
      createDcMotorController();
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

  private void createDcMotorController() {
    try {
      // TODO: This creates a HiTechnicUsbDcMotorController. We may need a property
      // (set only in designer) that indicates what kind of DC motor controller to create.
      dcMotorController = getHiTechnicDeviceManager().createUsbDcMotorController(
          new SerialNumber(serialNumber));
    } catch (RobotCoreException e) {
      Log.w("FtcDcMotorController", "....HeyLiz caught " + e);
      form.dispatchErrorOccurredEvent(this, "",
          ErrorMessages.ERROR_FTC_FAILED_TO_CREATE_DC_MOTOR_CONTROLLER, serialNumber);
    }

    if (dcMotorController != null)  {
      for (Child child : children) {
        child.createChild();
      }
    }
  }

  private void destroyDcMotorController() {
    for (Child child : children) {
      child.destroyChild();
    }
    if (dcMotorController != null) {
      dcMotorController.close();
      dcMotorController = null;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyDcMotorController();
  }

  // FtcRobotController.Child implementation

  @Override
  public void createChild() {
    createDcMotorController();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("dcMotorController is ").append((dcMotorController == null) ? "null" : "not null").append("\n");
    for (Child child : children) {
      child.debugChild(sb);
    }
  }

  @Override
  public void destroyChild() {
    destroyDcMotorController();
  }
}
