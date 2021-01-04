package com.google.appinventor.client.widgets;

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

  @SuppressWarnings("unused")  // Used by UiBinder
  public ToolbarItem() {
    widgetName = "";
    caption = "";
    command = null;
    align = "left";
  }

  public ToolbarItem(String widgetName, String caption, Command command) {
    this.widgetName = widgetName;
    this.caption = caption;
    this.command = command;
    align = "left";
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

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return visible;
  }
}
