// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.settings;

import com.google.gwt.user.client.Command;

/**
 * Defines an interface for accessing settings.
 *
 */
public interface SettingsAccessProvider {

  /**
   * Loads the associated settings.
   */
  void loadSettings();

  /**
   * Saves the associated settings.
   *
   * @param command  command to execute after saving (may be {@code null})
   */
  void saveSettings(Command command);
}
