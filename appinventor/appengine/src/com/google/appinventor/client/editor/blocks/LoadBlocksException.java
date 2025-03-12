// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

package com.google.appinventor.client.editor.blocks;

import java.io.IOException;

import com.google.gwt.core.client.JavaScriptException;

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
