// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.ui.PopupPanel;

public class Dropdown extends PopupPanel {

  private static Resources.DropdownStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dropdownStyleDark() : Resources.INSTANCE.dropdownStyleLight();

  private Button dropdownButton;
  private boolean center;

  public Dropdown() {
    super(true);
    style.ensureInjected();
    addStyleName(style.dropdown());
    hide();
  }

  public Dropdown(boolean center) {
    this();
    setCentered(center);
  }

  public void setCentered(boolean center) {
    this.center = center;
    setGlassEnabled(center);
    setAnimationEnabled(center);
    setAnimationType(AnimationType.CENTER);
  }
}
