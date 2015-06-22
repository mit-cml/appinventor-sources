// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ftc;

import android.content.Context;
import android.content.res.Resources;

/**
 * ResourceIds simulates R.<type>.<name> identifiers used in FtcRobotControllerActivity.java and
 * FtcRobotControllerSettingsActivity.java
 */
class ResourceIds {
  class RId {
    // from menu/ftc_robot_controller.xml
    final int action_settings;
    final int action_restart_robot;
    final int action_view_logs;
    final int action_about;
    final int action_exit_app;
    // from layout/activity_ftc_controller.xml
    final int entire_screen;
    final int included_header;
    final int textWifiDirectStatus;
    final int textRobotStatus;
    final int textOpMode;
    final int textErrorMessage;
    final int textGamepad1;
    final int textGamepad2;
    // from layout/device_name.xml
    final int textDeviceName;
    // from layout/header.xml
    final int active_filename;

    RId(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      action_settings =
          resources.getIdentifier("action_settings", "id", packageName);
      action_restart_robot =
          resources.getIdentifier("action_restart_robot", "id", packageName);
      action_view_logs =
          resources.getIdentifier("action_view_logs", "id", packageName);
      action_about =
          resources.getIdentifier("action_about", "id", packageName);
      action_exit_app =
          resources.getIdentifier("action_exit_app", "id", packageName);
      entire_screen =
          resources.getIdentifier("entire_screen", "id", packageName);
      included_header =
          resources.getIdentifier("included_header", "id", packageName);
      textWifiDirectStatus =
          resources.getIdentifier("textWifiDirectStatus", "id", packageName);
      textRobotStatus =
          resources.getIdentifier("textRobotStatus", "id", packageName);
      textOpMode =
          resources.getIdentifier("textOpMode", "id", packageName);
      textErrorMessage =
          resources.getIdentifier("textErrorMessage", "id", packageName);
      textGamepad1 =
          resources.getIdentifier("textGamepad1", "id", packageName);
      textGamepad2 =
          resources.getIdentifier("textGamepad2", "id", packageName);
      textDeviceName =
          resources.getIdentifier("textDeviceName", "id", packageName);
      active_filename =
          resources.getIdentifier("active_filename", "id", packageName);
    }
  }
  class RLayout {
    final int activity_ftc_controller;

    RLayout(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      activity_ftc_controller = 
          resources.getIdentifier("activity_ftc_controller", "layout", packageName);
    }
  }
  class RMenu {
    final int ftc_robot_controller;

    RMenu(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      ftc_robot_controller =
          resources.getIdentifier("ftc_robot_controller", "menu", packageName);
    }
  }
  class RString {
    // from values/strings.xml
    final int pref_launch_configure;
    final int pref_launch_autoconfigure;
    final int pref_launch_settings;
    final int pref_hardware_config_filename;

    RString(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      pref_launch_configure =
          resources.getIdentifier("pref_launch_configure", "string", packageName);
      pref_launch_autoconfigure =
          resources.getIdentifier("pref_launch_autoconfigure", "string", packageName);
      pref_launch_settings =
          resources.getIdentifier("pref_launch_settings", "string", packageName);
      pref_hardware_config_filename = 
          resources.getIdentifier("pref_hardware_config_filename", "string", packageName);
    }
  }
  class RXml {
    final int preferences;

    RXml(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      preferences = 
          resources.getIdentifier("preferences", "xml", packageName);
    }
  }

  final RId id;
  final RLayout layout;
  final RMenu menu;
  final RString string;
  final RXml xml;

  ResourceIds(Context context) {
    id = new RId(context);
    layout = new RLayout(context);
    menu = new RMenu(context);
    string = new RString(context);
    xml = new RXml(context);
  }

  private static int getIdentifier(Resources resources, String name, String defType, String defPackage) {
    int id = resources.getIdentifier(name, defType, defPackage);
    if (id == 0) {
      throw new IllegalStateException("Resource " + name + " not found");
    }
    return id;
  }
}
