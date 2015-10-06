// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ftc;

import android.content.Context;
import android.content.res.Resources;

import com.qualcomm.ftccommon.DbgLog;

import com.google.appinventor.components.runtime.collect.Maps;

import java.util.Map;

/**
 * R provides dynamic values for R.<type>.<name> identifiers used in
 * FtcRobotControllerActivity.java and FtcRobotControllerSettingsActivity.java.
 */
class R {
  static class Ids {
    final int action_about;
    final int action_exit_app;
    final int action_restart_robot;
    final int action_settings;
    final int action_view_logs;
    final int active_filename;
    final int entire_screen;
    final int included_header;
    final int RelativeLayout;
    final int menu_buttons;
    final int textDeviceName;
    final int textErrorMessage;
    final int textGamepad1;
    final int textGamepad2;
    final int textOpMode;
    final int textRobotStatus;
    final int textWifiDirectStatus;

    Ids(Resources resources, String packageName) {
      action_about = getIdentifier(resources, "action_about", "id", packageName);
      action_exit_app = getIdentifier(resources, "action_exit_app", "id", packageName);
      action_restart_robot = getIdentifier(resources, "action_restart_robot", "id", packageName);
      action_settings = getIdentifier(resources, "action_settings", "id", packageName);
      action_view_logs = getIdentifier(resources, "action_view_logs", "id", packageName);
      active_filename = getIdentifier(resources, "active_filename", "id", packageName);
      entire_screen = getIdentifier(resources, "entire_screen", "id", packageName);
      included_header = getIdentifier(resources, "included_header", "id", packageName);
      RelativeLayout = getIdentifier(resources, "RelativeLayout", "id", packageName);
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
  static class Layouts {
    final int activity_ftc_controller;

    Layouts(Resources resources, String packageName) {
      activity_ftc_controller = getIdentifier(resources, "activity_ftc_controller", "layout", packageName);
    }
  }
  static class Menus {
    final int ftc_robot_controller;

    Menus(Resources resources, String packageName) {
      ftc_robot_controller = getIdentifier(resources, "ftc_robot_controller", "menu", packageName);
    }
  }
  static class Strings {
    final int pref_hardware_config_filename;

    Strings(Resources resources, String packageName) {
      pref_hardware_config_filename = getIdentifier(resources, "AI_pref_hardware_config_filename", "string", packageName);
    }
  }
  static class Xmls {
    final int preferences;

    Xmls(Resources resources, String packageName) {
      preferences = getIdentifier(resources, "preferences", "xml", packageName);
    }
  }

  /**
   * Prevent instantiation.
   */
  private R() {
  }

  static final int id_action_about = 1;
  static final int id_action_exit_app = 2;
  static final int id_action_restart_robot = 3;
  static final int id_action_settings = 4;
  static final int id_action_view_logs = 5;
  private static final Map<Integer, Integer> actionIdToConstant = Maps.newHashMap();

  static Ids id;
  static Layouts layout;
  static Menus menu;
  static Strings string;
  static Xmls xml;

  static void init(Context context) {
    Resources resources = context.getResources();
    String packageName = context.getPackageName();
    id = new Ids(resources, packageName);
    layout = new Layouts(resources, packageName);
    menu = new Menus(resources, packageName);
    string = new Strings(resources, packageName);
    xml = new Xmls(resources, packageName);

    actionIdToConstant.put(id.action_about, id_action_about);
    actionIdToConstant.put(id.action_exit_app, id_action_exit_app);
    actionIdToConstant.put(id.action_restart_robot, id_action_restart_robot);
    actionIdToConstant.put(id.action_settings, id_action_settings);
    actionIdToConstant.put(id.action_view_logs, id_action_view_logs);
  }

  private static int getIdentifier(Resources resources, String name, String defType, String defPackage) {
    int id = resources.getIdentifier(name, defType, defPackage);
    if (id == 0) {
      throw new IllegalStateException("Resource " + name + " not found");
    }
    return id;
  }

  static int actionIdToConstant(int id) {
    try {
      return actionIdToConstant.get(id);
    } catch (Throwable e) {
      DbgLog.error("Could not handle action with id " + id);
      return 0;
    }
  }
}
