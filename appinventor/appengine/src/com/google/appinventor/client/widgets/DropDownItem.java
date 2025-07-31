// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.ElementParserToUse;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Image;

@ElementParserToUse(className = "com.google.appinventor.client.widgets.DropDownItemParser")
public class DropDownItem {
  String name;
  String caption;
  Command command;
  String styleName = null;
  String dependentStyleName = null;
  boolean visible = true;
  Image icon = null;

  @SuppressWarnings("unused")  // invoked by GWT
  public DropDownItem() {
  }

  public DropDownItem(String name, String caption, Command command) {
    this.name = name;
    this.caption = caption;
    this.command = command;
  }

  public DropDownItem(String name, String caption, Command command, String style) {
    this.name = name;
    this.caption = caption;
    this.command = command;
    this.styleName = style;
  }

  public DropDownItem(String widgetName, String caption, Command command, Image image) {
    this.name = widgetName;
    this.caption = caption;
    this.command = command;
    this.styleName = null;
    icon = image;
  }

  public DropDownItem(String name, String caption, Command command, String style, boolean visible) {
    this.name = name;
    this.caption = caption;
    this.command = command;
    this.styleName = style;
    this.visible = visible;
  }

  public void setName(String widgetName) {
    this.name = widgetName;
  }

  public String getName() {
    return name;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

  public void setStyleName(String styleName) {this.styleName = styleName;}

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean getVisible() {
    return visible;
  }

  public void setDependentStyleName(String dependentStyleName) {
    this.dependentStyleName = dependentStyleName;
  }

  public String getDependentStyleName() {
    return dependentStyleName;
  }

  public void setIcon(ImageResource icon) {
    this.icon = new Image(icon);
  }

  public Image getIcon() {
    return this.icon;
  }
}
