// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.client.widgets;

import com.google.gwt.user.client.ui.Button;

/**
 * A Button that has a specific style.
 *
 */
public class TextButton extends Button {
  public TextButton(String caption) {
    super(caption);
    setStylePrimaryName("ode-TextButton");
  }
}
