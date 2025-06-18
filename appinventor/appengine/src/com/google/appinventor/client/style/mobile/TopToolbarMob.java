//<!-- Copyright 2025 MIT, All rights reserved -->
//<!-- Released under the Apache License, Version 2.0 -->
//<!-- http://www.apache.org/licenses/LICENSE-2.0 -->

package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

public class TopToolbarMob extends TopToolbar {
    interface TopToolbarMobUiBinder extends UiBinder<FlowPanel, TopToolbarMob> {}
    private static final TopToolbarMobUiBinder UIBINDER = GWT.create(TopToolbarMobUiBinder.class);

    @UiField(provided = true)
    protected DropDownButton fileDropDown;
    @UiField(provided = true)
    protected DropDownButton connectDropDown;
    @UiField(provided = true)
    protected DropDownButton buildDropDown;
    @UiField(provided = true)
    protected DropDownButton settingsDropDown;
    @UiField(provided = true)
    protected DropDownButton adminDropDown;

    @UiField Button hamburgerButton;
    @UiField PopupPanel menuPopup;
    @UiField Button closeButton;
    @UiField
    Button newProjectButton;

    @UiField(provided = true)
    protected Boolean hasWriteAccess;

    public TopToolbarMob() {
        fileDropDown = new DropDownButton();
        connectDropDown = new DropDownButton();
        buildDropDown = new DropDownButton();
        settingsDropDown = new DropDownButton();
        adminDropDown = new DropDownButton();
        hasWriteAccess = getHasWriteAccess();

        initWidget(UIBINDER.createAndBindUi(this));
    }

    @UiHandler("hamburgerButton")
    void onHamburgerClick(ClickEvent event) {
        menuPopup.show();
        centerPopup();
    }

    @UiHandler("closeButton")
    void onCloseClick(ClickEvent event) {
        menuPopup.hide();
    }

    private void centerPopup() {
        int left = (Window.getClientWidth() - menuPopup.getOffsetWidth()) / 2;
        int top = (Window.getClientHeight() - menuPopup.getOffsetHeight()) / 2;
        menuPopup.setPopupPosition(left, top);
    }
}