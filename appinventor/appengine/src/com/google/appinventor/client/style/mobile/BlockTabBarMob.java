package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.boxes.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

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

    private void handleTabClick(BlockTabBarMob.TabType tabType) {
        openPanel(tabType);

        // Notify listener if set
        if (tabClickListener != null) {
            tabClickListener.onTabClick(tabType);
        }
    }


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
