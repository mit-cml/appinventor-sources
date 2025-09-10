// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.boxes.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Mobile design tab bar component for App Inventor's design view.
 * Provides navigation tabs for Palette, Components, Properties, and Media panels.
 * Each tab opens its corresponding content in a mobile sidebar when clicked.
 */
public class DesignTabBarMob extends Composite {

    interface DesignTabBarMobUiBinder extends UiBinder<FlowPanel, DesignTabBarMob> {}
    private static DesignTabBarMobUiBinder uiBinder = GWT.create(DesignTabBarMobUiBinder.class);

    @UiField
    FocusPanel paletteTab;
    @UiField FocusPanel componentsTab;
    @UiField FocusPanel propertiesTab;
    @UiField FocusPanel mediaTab;
    public enum TabType {
        PALETTE, COMPONENTS, PROPERTIES, MEDIA
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

    @UiHandler("mediaTab")
    void onMediaTabClick(ClickEvent event) {
        handleTabClick(TabType.MEDIA);
    }

    /**
     * Common handler for all tab clicks.
     * Opens the appropriate panel and notifies listeners.
     * @param tabType The type of tab that was clicked
     */
    private void handleTabClick(TabType tabType) {
        openPanel(tabType);

        // Notify listener if set
        if (tabClickListener != null) {
            tabClickListener.onTabClick(tabType);
        }
    }

    /**
     * Opens the content panel corresponding to the selected tab type.
     * Clears the sidebar and loads the appropriate content widget.
     *
     * @param tabType The type of content to display in the sidebar
     */
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

            case MEDIA:
                widgetToAdd = AssetListBox.getAssetListBox();
                widgetToAdd.setWidth("100%");
                break;
        }

        if (widgetToAdd != null) {
            // Remove hidden style for all tab types
            widgetToAdd.removeStyleName("ode-Hidden");
            widgetToAdd.setVisible(true);
            if (tabType == TabType.COMPONENTS) {
                SourceStructureBox.getSourceStructureBox().removeStyleName("ode-Hidden");
                SourceStructureBox.getSourceStructureBox().setVisible(true);
            }
            sidebar.add(widgetToAdd);
            sidebar.open();
        }
    }

}