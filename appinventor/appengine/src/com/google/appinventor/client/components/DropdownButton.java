// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;

public class DropdownButton extends Button {

  private static Resources.DropdownStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dropdownStyleDark() : Resources.INSTANCE.dropdownStyleLight();

  public DropdownButton() {
    super();
    style.ensureInjected();
  }

  @Override
  protected String makeText() {
    Icon arrowIcon = new Icon("arrow_drop_down");
    arrowIcon.addStyleName(style.buttonIcon());
    return super.makeText() + arrowIcon.toString();
  }
}
