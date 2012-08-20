// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

package com.google.appinventor.client.editor.youngandroid;

/**
 * Exception for error that occur when attempting to generate Yail code from blocks
 * 
 * @author sharon@google.com (Sharon Perl)
 */
public class YailGenerationException extends Exception {
  // The name of the form being built when an error occurred
  private final String formName;

  YailGenerationException(String message, String formName) {
    super(message);
    this.formName = formName;
  }

  /**
   * Return the name of the form that Yail generation failed on.
   */
  public String getFormName() {
    return formName;
  }

}
