// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.hitechnic.HiTechnicDeviceManager;
import com.qualcomm.robotcore.eventloop.EventLoopManager;

import android.util.Log;

/**
 * A base class for components that are children of the FtcRobotController.
 * etc.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SimpleObject
public abstract class FtcRobotControllerChild extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcRobotController.Child {

  protected final String logTag;

  protected FtcRobotController ftcRobotController;

  /**
   * Creates a new FtcRobotControllerChild.
   */
  protected FtcRobotControllerChild(ComponentContainer container, String logTag) {
    super(container.$form());
    this.logTag = logTag;
  }

  /**
   * Default Initialize
   */
  public final void Initialize() {
  }

  protected boolean isAfterEventLoopInit() {
    return (ftcRobotController != null)
        ? ftcRobotController.isAfterEventLoopInit()
        : false;
  }

  protected HiTechnicDeviceManager getHiTechnicDeviceManager() {
    return (ftcRobotController != null)
        ? ftcRobotController.getHiTechnicDeviceManager()
        : null;
  }

  protected EventLoopManager getEventLoopManager() {
    return (ftcRobotController != null)
        ? ftcRobotController.getEventLoopManager()
        : null;
  }

  // Properties

  /**
   * FtcRobotController property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The FtcRobotController component",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public FtcRobotController FtcRobotController() {
    return ftcRobotController;
  }

  /**
   * FtcRobotController property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_ROBOT_CONTROLLER,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void FtcRobotController(FtcRobotController ftcRobotController) {
    if (this.ftcRobotController != null) {
      if (isAfterEventLoopInit()) {
        destroyChild();
      }
      this.ftcRobotController.removeChild(this);
      this.ftcRobotController = null;
    }

    if (ftcRobotController != null) {
      this.ftcRobotController = ftcRobotController;
      this.ftcRobotController.addChild(this);
      if (isAfterEventLoopInit()) {
        createChild();
      }
    }
  }

  // Deleteable implementation
  @Override
  public void onDelete() {
    if (ftcRobotController != null) {
      if (isAfterEventLoopInit()) {
        destroyChild();
      }
      ftcRobotController.removeChild(this);
      ftcRobotController = null;
    }
  }

  // FtcRobotController.Child implementation

  @Override
  public void createChild() {
    // Overridden by subclasses if needed.
  }

  @Override
  public void debugChild(StringBuilder sb) {
    // Overridden by subclasses if needed.
  }

  @Override
  public void destroyChild() {
    // Overridden by subclasses if needed.
  }
}
