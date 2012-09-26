// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
