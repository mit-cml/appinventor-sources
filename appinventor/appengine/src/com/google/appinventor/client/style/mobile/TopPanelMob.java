// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.mobile;

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

public class TopPanelMob extends TopPanel {


    @UiTemplate("TopPanelMob.ui.xml")
    interface TopPanelUiBinderMob extends UiBinder<FlowPanel, TopPanelMob> {}


    @UiField TopToolbarMob topToolbar;
    @UiField Label readOnly;
    @UiField ImageElement logo;
    @UiField FlowPanel rightPanel;
    @UiField DropDownButton languageDropDown;
    @UiField DropDownButton accountButton;
    @UiField DropDownItem deleteAccountItem;
    @UiField FlowPanel links;

    @Override
    public void bindUI() {
        // Create and bind the mobile UI layout
        TopPanelUiBinderMob uibinder = GWT.create(TopPanelUiBinderMob.class);
        initWidget(uibinder.createAndBindUi(this));

        super.topToolbar = topToolbar;
        super.readOnly = readOnly;
        super.rightPanel = rightPanel;
        super.languageDropDown = languageDropDown;
        super.accountButton = accountButton;
        super.links = links;
        super.deleteAccountItem = deleteAccountItem;
        super.logo = logo;

    }
}