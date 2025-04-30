// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.TopPanel;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class TopPanelNeo extends TopPanel {

  @UiTemplate("TopPanelNeo.ui.xml")
  interface TopPanelUiBinderNeo extends UiBinder<FlowPanel, TopPanelNeo> {}

  @UiField
  TopToolbarNeo topToolbar;
  @UiField ImageElement logo;
  @UiField Label readOnly;
  @UiField FlowPanel rightPanel;
  @UiField
  DropDownButton languageDropDown;
  @UiField
  DropDownButton accountButton;
  @UiField
  DropDownItem deleteAccountItem;
  @UiField
  FlowPanel links;

  @Override
  public void bindUI() {
    TopPanelUiBinderNeo uibinder = GWT.create(TopPanelUiBinderNeo.class);
    initWidget(uibinder.createAndBindUi(this));
    super.topToolbar = topToolbar;
    super.logo = logo;
    super.readOnly = readOnly;
    super.rightPanel = rightPanel;
    super.languageDropDown = languageDropDown;
    super.accountButton = accountButton;
    super.deleteAccountItem = deleteAccountItem;
    super.links = links;
  }
}
