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
    final int action_about;
    final int action_restart_robot;
    final int action_settings;
    final int action_view_logs;
    final int active_filename;
    final int entire_screen;
    final int included_header;
    final int menu_buttons;
    final int textDeviceName;
    final int textErrorMessage;
    final int textGamepad1;
    final int textGamepad2;
    final int textOpMode;
    final int textRobotStatus;
    final int textWifiDirectStatus;

    RId(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      action_about = getIdentifier(resources, "action_about", "id", packageName);
      action_restart_robot = getIdentifier(resources, "action_restart_robot", "id", packageName);
      action_settings = getIdentifier(resources, "action_settings", "id", packageName);
      action_view_logs = getIdentifier(resources, "action_view_logs", "id", packageName);
      active_filename = getIdentifier(resources, "active_filename", "id", packageName);
      entire_screen = getIdentifier(resources, "entire_screen", "id", packageName);
      included_header = getIdentifier(resources, "included_header", "id", packageName);
      menu_buttons = getIdentifier(resources, "menu_buttons", "id", packageName);
      textDeviceName = getIdentifier(resources, "textDeviceName", "id", packageName);
      textErrorMessage = getIdentifier(resources, "textErrorMessage", "id", packageName);
      textGamepad1 = getIdentifier(resources, "textGamepad1", "id", packageName);
      textGamepad2 = getIdentifier(resources, "textGamepad2", "id", packageName);
      textOpMode = getIdentifier(resources, "textOpMode", "id", packageName);
      textRobotStatus = getIdentifier(resources, "textRobotStatus", "id", packageName);
      textWifiDirectStatus = getIdentifier(resources, "textWifiDirectStatus", "id", packageName);
    }
  }
  class RLayout {
    final int activity_ftc_controller;

    RLayout(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      activity_ftc_controller =  getIdentifier(resources, "activity_ftc_controller", "layout", packageName);
    }
  }
  class RMenu {
    final int ftc_robot_controller;

    RMenu(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      ftc_robot_controller = getIdentifier(resources, "ftc_robot_controller", "menu", packageName);
    }
  }
  class RString {
    final int pref_hardware_config_filename;
    final int pref_launch_autoconfigure;
    final int pref_launch_configure;
    final int pref_launch_settings;

    RString(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      pref_hardware_config_filename =  getIdentifier(resources, "pref_hardware_config_filename", "string", packageName);
      pref_launch_autoconfigure = getIdentifier(resources, "pref_launch_autoconfigure", "string", packageName);
      pref_launch_configure = getIdentifier(resources, "pref_launch_configure", "string", packageName);
      pref_launch_settings = getIdentifier(resources, "pref_launch_settings", "string", packageName);
    }
  }
  class RXml {
    final int preferences;

    RXml(Context context) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();
      preferences =  getIdentifier(resources, "preferences", "xml", packageName);
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
