// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * Listener for distributing the Activity onStop() method to interested components.
 * Listener for components that want to be notified when (clear-current-form) is called
 * This is only used in the Companion.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

public interface OnClearListener {
  public void onClear();
}
