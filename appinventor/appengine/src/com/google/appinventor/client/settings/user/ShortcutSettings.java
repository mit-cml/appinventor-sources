// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings.user;

import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.settings.SettingsConstants;

/**
 * ShortcutSettings encapsulates user settings related to keyboard shortcut remapping.
 */
public final class ShortcutSettings extends Settings {

  /**
   * Create a new ShortcutSettings instance with the default values.
   * @param user
   */
  public ShortcutSettings(UserInfoProvider user) {
    super(SettingsConstants.SHORTCUTS_SETTINGS);

    addProperty(new EditableProperty(this, SettingsConstants.SHORTCUTS_KEY_MAP,
        "", EditableProperty.TYPE_INVISIBLE));
  }
}
