// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings;

import com.google.appinventor.client.utils.Promise;
import com.google.gwt.user.client.Command;

/**
 * Defines an interface for accessing settings.
 *
 */
public interface SettingsAccessProvider<T> {

  /**
   * Loads the associated settings.
   */
  Promise<T> loadSettings();

  /**
   * Saves the associated settings.
   *
   * @param command  command to execute after saving (may be {@code null})
   */
  void saveSettings(Command command);
}
