// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012-2025 Massachusetts Institute of Technology. All Rights Reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

import com.google.gwt.core.client.JavaScriptException;
import java.io.IOException;

/**
 * LoadBlocksException is thrown by the BlocklyPanel if a blocks file fails to load correctly.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class LoadBlocksException extends IOException {
  private static final long serialVersionUID = 5201254228683578731L;

  private String formName;

  /**
   * No-arg constructor for serializable.
   */
  LoadBlocksException() {
  }

  public LoadBlocksException(JavaScriptException e, String formName) {
    super(e);
    this.formName = formName;
  }

  public String getFormName() {
    return formName;
  }
}
