// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.appinventor.client.components.Icon;
import com.google.gwt.uibinder.client.ElementParserToUse;
import com.google.gwt.user.client.Command;

/**
 * A Toolbar item has a widgetName that is unique over all items in
 * the toolbar, a caption (displayed to the user)
 * and a command to run when the item is selected.
 */
@ElementParserToUse(className = "com.google.appinventor.client.widgets.ToolbarItemParser")
public class ToolbarItem {
  private String widgetName;
  private String caption;
  private Command command;
  private String align;
  private boolean visible = true;
  private boolean enabled = true;
  private Icon icon = null;
  private String styleName = null;

  private String tooltip = "";


  @SuppressWarnings("unused")  // Used by UiBinder
  public ToolbarItem() {
    widgetName = "";
    caption = "";
    command = null;
    align = "left";
    icon = null;
  }

  public ToolbarItem(String widgetName, String caption, Command command) {
    this.widgetName = widgetName;
    this.caption = caption;
    this.command = command;
    align = "left";
    icon = null;
  }

  public String getName() {
    return widgetName;
  }

  public String getCaption() {
    return caption;
  }

  public Command getCommand() {
    return command;
  }

  public Icon getIcon() {
    return icon;
  }

  public void setIcon(String iconName) {
    icon = new com.google.appinventor.client.components.Icon(iconName);
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  public String getStyleName() {
    return styleName;
  }

  public void setStyleName(String styleName) {
    this.styleName = styleName;
  }

  public void setName(String name) {
    widgetName = name;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

  public String getAlign() {
    return align;
  }

  public void setAlign(String align) {
    this.align = align;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean p_enabled) {
    this.enabled = p_enabled;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setTooltip (String text) {
    this.tooltip = text;
  }

  public String getTooltip () {
    return this.tooltip;
  }
}
