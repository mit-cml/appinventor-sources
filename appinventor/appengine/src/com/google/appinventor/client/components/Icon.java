package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;

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

  @Override
  public void setStyleName(String styleName) {
    addStyleName(styleName);
  }
}
