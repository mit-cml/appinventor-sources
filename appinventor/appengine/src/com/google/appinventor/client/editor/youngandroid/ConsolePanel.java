// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ConsolePanel extends FlowPanel {
  static {
    exportMethodsToJavascript();
  }

  public ConsolePanel() {
    getElement().setId("aiConsolePanel");
  }

  private static native void exportMethodsToJavascript() /*-{
    top.ConsolePanel_addLog = function(type, msg) {
      var container = top.document.getElementById('aiConsolePanel');
      var entry, category;
      entry = top.document.createElement('div');
      entry.classList.add(type);
      category = top.document.createElement('span');
      category.classList.add(type);
      category.innerText = type;
      entry.appendChild(category);
      entry.appendChild(top.document.createTextNode(": " + msg));
      container.appendChild(entry);
    }
   
  }-*/;
}
