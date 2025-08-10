//<!-- Copyright 2025 MIT, All rights reserved -->
//<!-- Released under the Apache License, Version 2.0 -->
//<!-- http://www.apache.org/licenses/LICENSE-2.0 -->
package com.google.appinventor.client.style.mobile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class MobileBottomButton extends Composite implements HasClickHandlers {


    interface MobileBottomButtonUiBinder extends UiBinder<FlowPanel, MobileBottomButton> {}
    private static MobileBottomButtonUiBinder uiBinder = GWT.create(MobileBottomButtonUiBinder.class);

    @UiField Label button;
    @UiField Label label;
    @UiField FlowPanel buttonContainer;

    private String openText = "Designer Menu";
    private String closeText = "Close";
    private boolean isOpen = false;
    private MobileSidebar sidebar;

    public MobileBottomButton() {
        initWidget(uiBinder.createAndBindUi(this));
        updateButton();
        SetupButtonContainer();
    }
    public boolean isOpen() {
        return isOpen;
    }

    private void SetupButtonContainer(){
        buttonContainer.setStyleName("button-container");
        buttonContainer.setVisible( false);
        addSubButton("Palette");
        addSubButton("Components");
        addSubButton("Properties");
    }

    private void addSubButton(String text) {
        Button btn = new Button(text);
        btn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                sidebar.openPanel(text);
            }
        });
        buttonContainer.add(btn);
    }

    public void setSidebar(MobileSidebar sidebar) {
        this.sidebar = sidebar;
    }

    public void setText(String openText, String closeText) {
        this.openText = openText;
        this.closeText = closeText;
        updateButton();
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
        updateButton();
        buttonContainer.setVisible(open);
    }

    private void updateButton() {
        if (isOpen) {
            button.setText("✕");
            label.setText(closeText);
            addStyleName("open");
        } else {
            button.setText("☰");
            label.setText(openText);
            removeStyleName("open");
        }
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }
}