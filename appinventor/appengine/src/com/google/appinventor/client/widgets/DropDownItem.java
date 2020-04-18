// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.uibinder.client.ElementParserToUse;
import com.google.gwt.user.client.Command;

@ElementParserToUse(className = "com.google.appinventor.client.widgets.DropDownItemParser")
public class DropDownItem {
  String name;
  String caption;
  Command command;

  @SuppressWarnings("unused")  // invoked by GWT
  public DropDownItem() {
  }

  public DropDownItem(String name, String caption, Command command) {
    this.name = name;
    this.caption = caption;
    this.command = command;
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

}
