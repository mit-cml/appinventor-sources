// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.runtime.Form;

/**
 * Error signaled by runtime.scm that triggers the Screen's Form.DispatchErrorOccurred
 * so that the developer can provide a handler for it.
 *
 * @author halabelson@mit.edu (Hal Abelson)
 */
public class YailFormRuntimeError {
   
  /**
   *
   * @param form the form where the error should be signaled
   */
  
  public YailFormRuntimeError(Form form, String functionName,  int errorNumber, String message) {
    form.formRuntimeErrorOccurredEvent(functionName, errorNumber, message);
  }


}
