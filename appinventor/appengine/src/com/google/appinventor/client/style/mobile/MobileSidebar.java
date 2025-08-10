//<!-- Copyright 2025 MIT, All rights reserved -->
//<!-- Released under the Apache License, Version 2.0 -->
//<!-- http://www.apache.org/licenses/LICENSE-2.0 -->
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

public class MobileSidebar extends Composite implements HasWidgets {

    interface MobileSidebarUiBinder extends UiBinder<FlowPanel, MobileSidebar> {}
    private static MobileSidebarUiBinder uiBinder = GWT.create(MobileSidebarUiBinder.class);

    @UiField FocusPanel overlay;
    @UiField SimplePanel sidebar;
    @UiField SimplePanel content;

    private boolean isOpen = false;
    private Runnable syncCallback;

    public MobileSidebar() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void add(Widget w) {
        content.add(w);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return content.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return content.remove(w);
    }

    public void open() {
        if (!isOpen) {
            isOpen = true;
            getWidget().addStyleName("open");
        }
    }

    public void close() {
        if (isOpen) {
            isOpen = false;
            getWidget().removeStyleName("open");
            if (syncCallback != null) syncCallback.run();
        }
    }

    public void toggle() {
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setSyncCallback(Runnable syncCallback) {
        this.syncCallback = syncCallback;
    }

    @UiHandler("overlay")
    void onOverlayClick(ClickEvent event) {
        close();
    }

    public void openPanel(String panelName) {
        content.clear();
        Widget widgetToAdd = null;
        switch (panelName) {
            case "Palette":
                widgetToAdd = PaletteBox.getPaletteBox();
                break;
            case "Components":
                FlowPanel componentsPanel = new FlowPanel();
                componentsPanel.add(SourceStructureBox.getSourceStructureBox());
                componentsPanel.add(BlockSelectorBox.getBlockSelectorBox());
                componentsPanel.setWidth("100%");
                componentsPanel.setHeight("100%");
                widgetToAdd = componentsPanel;
                break;
            case "Properties":
                widgetToAdd = PropertiesBox.getPropertiesBox();
                widgetToAdd.setWidth("100%");
                break;
        }
        if (widgetToAdd != null) {
            if (panelName.equals("Palette") || panelName.equals("Properties")) {
                widgetToAdd.removeStyleName("ode-Hidden");
            }
            content.add(widgetToAdd);
        }
        open();
    }
}