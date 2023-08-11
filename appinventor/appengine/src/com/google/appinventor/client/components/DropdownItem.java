// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.ui.Label;

public class DropdownItem extends Label {

  private static Resources.DropdownStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dropdownStyleDark() : Resources.INSTANCE.dropdownStyleLight();

  public DropdownItem() {
    super();
    style.ensureInjected();
    setStylePrimaryName(style.dropdownItem());
  }

  public void setHasDivider(boolean hasDivider) {
    if(hasDivider) {
      addStyleName(style.dropdownItemWithDivider());
    } else {
      removeStyleName(style.dropdownItemWithDivider());
    }
  }
}
