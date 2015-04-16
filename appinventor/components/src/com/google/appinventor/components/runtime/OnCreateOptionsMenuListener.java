// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.Menu;

/**
 * Listener for distributing the Activity onCreateOptionsMenu() method to interested
 * components.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface OnCreateOptionsMenuListener {
  public void onCreateOptionsMenu(Menu menu);
}
