// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Intent;

/**
 * Callback for receiving Activity results
 *
 * @author markf@google.com (Mark Friedman)
 */
public interface ActivityResultListener {

  /**
   * The callback method used to report Activity results back to the caller.
   * @param requestCode the originally passed in request code. Used to identify the call.
   * @param resultCode the returned result code: {@link android.app.Activity#RESULT_OK} or
   *                   {@link android.app.Activity#RESULT_CANCELED}
   * @param data the returned data, encapsulated as an {@link Intent}.
   */
  void resultReturned(int requestCode, int resultCode, Intent data);
}
