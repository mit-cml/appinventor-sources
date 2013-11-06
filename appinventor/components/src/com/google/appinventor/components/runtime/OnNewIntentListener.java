// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

import android.content.Intent;

/**
 * Listener for distributing the Activity onStop() method to interested components.
 *
 * @author markf@google.com (Mark Friedman)
 */

public interface OnNewIntentListener {
  public void onNewIntent(Intent intent);
}
