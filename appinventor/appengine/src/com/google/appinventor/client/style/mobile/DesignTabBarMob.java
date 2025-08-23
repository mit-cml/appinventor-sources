//<!-- Copyright 2025 MIT, All rights reserved -->
//<!-- Released under the Apache License, Version 2.0 -->
//<!-- http://www.apache.org/licenses/LICENSE-2.0 -->
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

public class DesignTabBarMob extends Composite {

    interface DesignTabBarMobUiBinder extends UiBinder<FlowPanel, DesignTabBarMob> {}
    private static DesignTabBarMobUiBinder uiBinder = GWT.create(DesignTabBarMobUiBinder.class);

    @UiField
    FocusPanel paletteTab;
    @UiField FocusPanel componentsTab;
    @UiField FocusPanel propertiesTab;

    public enum TabType {
        PALETTE, COMPONENTS, PROPERTIES
    }

    private MobileSidebar sidebar;
    private TabClickListener tabClickListener;

    public interface TabClickListener {
        void onTabClick(TabType tabType);
    }

    public DesignTabBarMob() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setSidebar(MobileSidebar sidebar) {
        this.sidebar = sidebar;
    }

    public void setTabClickListener(TabClickListener listener) {
        this.tabClickListener = listener;
    }

    @UiHandler("paletteTab")
    void onPaletteTabClick(ClickEvent event) {
        handleTabClick(TabType.PALETTE);
    }

    @UiHandler("componentsTab")
    void onComponentsTabClick(ClickEvent event) {
        handleTabClick(TabType.COMPONENTS);
    }

    @UiHandler("propertiesTab")
    void onPropertiesTabClick(ClickEvent event) {
        handleTabClick(TabType.PROPERTIES);
    }

    private void handleTabClick(TabType tabType) {
        openPanel(tabType);

        // Notify listener if set
        if (tabClickListener != null) {
            tabClickListener.onTabClick(tabType);
        }
    }


    private void openPanel(TabType tabType) {
        if (sidebar == null) return;

        sidebar.clear();
        Widget widgetToAdd = null;

        switch (tabType) {
            case PALETTE:
                widgetToAdd = PaletteBox.getPaletteBox();
                break;
            case COMPONENTS:
                FlowPanel componentsPanel = new FlowPanel();
                componentsPanel.add(SourceStructureBox.getSourceStructureBox());
                componentsPanel.setWidth("100%");
                componentsPanel.setHeight("100%");
                widgetToAdd = componentsPanel;
                break;
            case PROPERTIES:
                widgetToAdd = PropertiesBox.getPropertiesBox();
                widgetToAdd.setWidth("100%");
                break;
        }

        if (widgetToAdd != null) {
            if (tabType == TabType.PALETTE || tabType == TabType.PROPERTIES) {
                widgetToAdd.removeStyleName("ode-Hidden");
            }
            sidebar.add(widgetToAdd);
            sidebar.open();
        }
    }

}