package com.google.appinventor.client.editor.youngandroid;

import java.io.IOException;

import com.google.gwt.core.client.JavaScriptException;

public class LoadBlocksException extends IOException {
  private static final long serialVersionUID = 5201254228683578731L;

  private final String formName;
  
  public LoadBlocksException(JavaScriptException e, String formName) {
    super(e);
    this.formName = formName;
  }

  public String getFormName() {
    return formName;
  }
}
