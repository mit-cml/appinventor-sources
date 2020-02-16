// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * This interface contains a callback method onBeforeInitialize()
 * which will be invoked by the Form activity after the app is
 * initialized, but before the Screen Initialize event is dispatched.
 * Components can register for the callback by calling Form.registerForBeforeInitialize().
 */
public interface OnBeforeInitializeListener {
  void onBeforeInitialize();
}
