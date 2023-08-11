// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.ui.DialogBox;

public class Dialog extends DialogBox {

  private static Resources.DialogStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dialogStyleDark() : Resources.INSTANCE.dialogStyleLight();

  private Caption caption;

  public Dialog() {
    super(false, true, new CaptionImpl());
    caption = getCaption();
    style.ensureInjected();
    setStylePrimaryName(style.dialog());
    setGlassEnabled(true);
  }

  public void setCaption(String text) {
    caption.setText(text);
  }
}
