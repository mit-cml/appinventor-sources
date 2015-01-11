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
import com.qualcomm.hitechnic.HiTechnicUsbSensorMux;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.SerialNumber;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A component that provides an interface to a sensor mux of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_SENSOR_MUX_COMPONENT_VERSION,
    description = "A component that provides an interface to a sensor mux of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "hitechnic.jar,robotcore.jar")
public final class FtcSensorMux extends FtcRobotControllerChild {

  public interface Child {
    void createChild();
    void debugChild(StringBuilder sb);
    void destroyChild();
  }

  private String serialNumber = "";
  private HiTechnicUsbSensorMux sensorMux;
  private final List<Child> children = new ArrayList<Child>();

  /**
   * Creates a new FtcSensorMux component.
   */
  public FtcSensorMux(ComponentContainer container) {
    super(container, "FtcSensorMux");
  }

  HiTechnicUsbSensorMux getSensorMux() {
    return sensorMux;
  }

  /**
   * SerialNumber property getter method.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The serial number of the sensor mux.",
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
      destroySensorMux();
    }
    this.serialNumber = serialNumber;
    if (isAfterEventLoopInit()) {
      createSensorMux();
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

  private void createSensorMux() {
    try {
      // TODO: This creates a HiTechnicUsbSensorMux. We may need a property
      // (set only in designer) that indicates what kind of sensor mux to create.
      sensorMux = getHiTechnicDeviceManager().createUsbSensorMux(
          new SerialNumber(serialNumber));
    } catch (RobotCoreException e) {
      Log.w("FtcSensorMux", "....HeyLiz caught " + e);
      form.dispatchErrorOccurredEvent(this, "",
          ErrorMessages.ERROR_FTC_FAILED_TO_CREATE_SENSOR_MUX, serialNumber);
    }

    if (sensorMux != null)  {
      for (Child child : children) {
        child.createChild();
      }
    }
  }

  private void destroySensorMux() {
    for (Child child : children) {
      child.destroyChild();
    }
    if (sensorMux != null) {
      sensorMux.close();
      sensorMux = null;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroySensorMux();
  }

  // FtcRobotController.Child implementation

  @Override
  public void createChild() {
    createSensorMux();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("sensorMux is ").append((sensorMux == null) ? "null" : "not null").append("\n");
    for (Child child : children) {
      child.debugChild(sb);
    }
  }

  @Override
  public void destroyChild() {
    destroySensorMux();
  }
}
