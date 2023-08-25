// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.ElementParserToUse;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Image;
import com.google.appinventor.client.components.Icon;

/**
 * A PushButton that has a specific style.
 *
 * @author lizlooney@google.com (Liz Looney)
 */

@ElementParserToUse(className = "com.google.appinventor.client.widgets.TextButtonParser")
public class TextButton extends PushButton {

  public TextButton() {
    super();
    setStylePrimaryName("ode-TextButton");
  }

  public TextButton(String caption) {
    super(caption);
    setStylePrimaryName("ode-TextButton");
  }

  public TextButton(Image image) {
    super(image);
    setStylePrimaryName("ode-TextButton");
  }

  public TextButton(String caption, Icon icon) {
    super();
    super.setHTML(makeText(caption, icon));
    setStylePrimaryName("ode-TextButton");
  }

  public TextButton(String caption, Icon icon, String styleName) {
    super();
    super.setHTML(makeText(caption, icon));
    if (styleName == null || styleName.isEmpty()) {
      setStylePrimaryName("ode-TextButton");
    } else {
      setStylePrimaryName(styleName);
    }
  }

  public void setCommand(Command command) {
    this.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        command.execute();
      }
    });
  }

  protected String makeText(String caption, Icon icon) {
    String text = "";
    if (icon != null) {
      text += icon.toString();
    }
    text+= caption;
    return text;
  }
}
