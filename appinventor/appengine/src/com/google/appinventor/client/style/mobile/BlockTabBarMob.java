// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mobile block editor tab bar component for App Inventor's blocks view.
 * Provides navigation tabs for Blocks and Media panels in the blocks editor.
 * Each tab opens its corresponding content in a mobile sidebar when clicked.
 */
public class BlockTabBarMob extends Composite {

    interface BlockTabBarMobUiBinder extends UiBinder<FlowPanel, BlockTabBarMob> {}
    private static BlockTabBarMob.BlockTabBarMobUiBinder uiBinder = GWT.create(BlockTabBarMob.BlockTabBarMobUiBinder.class);

   @UiField FocusPanel BlockTab;
    @UiField FocusPanel mediaTab;
    public enum TabType {
        BLOCKS, MEDIA
    }

    private MobileSidebar sidebar;
    private BlockTabBarMob.TabClickListener tabClickListener;
    private TabType currentActiveTab = null;

    public interface TabClickListener {
        void onTabClick(BlockTabBarMob.TabType tabType);
    }

    public BlockTabBarMob() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setSidebar(MobileSidebar sidebar) {
        this.sidebar = sidebar;
    }

    public void setTabClickListener(BlockTabBarMob.TabClickListener listener) {
        this.tabClickListener = listener;
    }

    @UiHandler("BlockTab")
    void onPaletteTabClick(ClickEvent event) {
        handleTabClick(BlockTabBarMob.TabType.BLOCKS);
    }


    @UiHandler("mediaTab")
    void onMediaTabClick(ClickEvent event) {
        handleTabClick(BlockTabBarMob.TabType.MEDIA);
    }

    /**
     * Common handler for all tab clicks.
     * Opens the appropriate panel and notifies listeners.
     * @param tabType The type of tab that was clicked
     */
    private void handleTabClick(BlockTabBarMob.TabType tabType) {

        // If clicking the same tab that's already active, close the sidebar
        if (currentActiveTab == tabType && sidebar != null && sidebar.isOpen()) {
            sidebar.close();
            currentActiveTab = null;
        } else {
            // Open the new panel
            openPanel(tabType);
            currentActiveTab = tabType;
        }

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
    private void openPanel(BlockTabBarMob.TabType tabType) {
        if (sidebar == null) return;

        sidebar.clear();
        Widget widgetToAdd = null;

        switch (tabType) {
            case BLOCKS:
                BlockSelectorBox blockSelector = BlockSelectorBox.getBlockSelectorBox();
                // Set the sidebar reference so it can close itself
                blockSelector.setMobileSidebar(sidebar);
                widgetToAdd = blockSelector;
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
            sidebar.add(widgetToAdd);
            sidebar.open();
        }
    }
}
