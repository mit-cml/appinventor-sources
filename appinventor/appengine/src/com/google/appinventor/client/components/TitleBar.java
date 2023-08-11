// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class TitleBar extends Composite {

  interface TitleBarUiBinder extends UiBinder<FlowPanel, TitleBar> {}
  private static final TitleBarUiBinder UI_BINDER = GWT.create(TitleBarUiBinder.class);

  @UiField(provided=true)
  Resources.TitleBarStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.titleBarStyleDark() : Resources.INSTANCE.titleBarStyleLight();

  public TitleBar() {
    style.ensureInjected();
    initWidget(UI_BINDER.createAndBindUi(this));
    // initWidget(new FlowPanel());
  }
}
