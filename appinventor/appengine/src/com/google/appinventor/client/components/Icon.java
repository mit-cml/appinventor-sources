// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.components;

import com.google.gwt.user.client.ui.InlineLabel;

public class Icon extends InlineLabel {

  private String iconName;

  public Icon() {
    addStyleName("material-icons");
  }

  public Icon(String iconName) {
    this();
    setIcon(iconName);
  }

  public void setIcon(String iconName) {
    this.iconName = iconName;
    setText(iconName);
  }

  public void setTooltip(String tooltip) {
    setTitle(tooltip);
  }

  @Override
  public void setStyleName(String styleName) {
    addStyleName(styleName);
  }
}
