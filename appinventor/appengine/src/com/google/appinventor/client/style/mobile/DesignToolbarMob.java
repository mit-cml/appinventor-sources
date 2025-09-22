// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.actions.ProjectPropertiesAction;
import com.google.appinventor.client.editor.youngandroid.actions.SwitchToBlocksEditorAction;
import com.google.appinventor.client.editor.youngandroid.actions.SwitchToFormEditorAction;
import com.google.appinventor.client.editor.youngandroid.actions.ToggleTutorialAction;
import com.google.appinventor.client.actions.SendToGalleryAction;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.widgets.ToolbarItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import java.util.logging.Logger;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Mobile-specific implementation of the Design Toolbar for MIT App Inventor.
 * This class provides a responsive toolbar interface optimized for mobile devices,
 * including dropdown menus and touch-friendly navigation between design and blocks editors.
 *
 * @author Divyanshu kumar Nayak, kumardivyanshu118@gmail.com
 * @since 2025
 */
public class DesignToolbarMob extends DesignToolbar {

    private static final Logger LOG = Logger.getLogger(DesignToolbarMob.class.getName());
    interface DesignToolbarUiBinderMob extends UiBinder<Toolbar, DesignToolbarMob> {}

    @UiField protected DropDownButton pickFormItem;
    @UiField protected ToolbarItem addFormItem;
    @UiField protected ToolbarItem removeFormItem;
    @UiField protected ToolbarItem switchToDesign;
    @UiField protected ToolbarItem switchToBlocks;
    @UiField protected ToolbarItem sendToGalleryItem;
    @UiField protected DropDownButton designerActionDropdown;

    private static final String DESIGNER_ACTION_DROPDOWN = "DesignerAction";

    @Override
    public void bindUI() {
        DesignToolbarMob.DesignToolbarUiBinderMob uibinder = GWT.create(DesignToolbarMob.DesignToolbarUiBinderMob.class);
        populateToolbar(uibinder.createAndBindUi(this));

        // Set parent references
        super.pickFormItem = pickFormItem;
        super.addFormItem = addFormItem;
        super.removeFormItem = removeFormItem;
        super.switchToDesign = switchToDesign;
        super.switchToBlocks = switchToBlocks;
        super.sendToGalleryItem = sendToGalleryItem;

        designerActionDropdown.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addPopupStyling();
            }
        }, ClickEvent.getType());

        populateMobileDropdown();
    }

    // we need to refactor this method to use a more efficient method
    private void addPopupStyling() {
        NodeList<Element> allElements = Document.get().getElementsByTagName("div");
        for (int i = 0; i < allElements.getLength(); i++) {
            Element element = allElements.getItem(i);
            if (element.getClassName().contains("gwt-PopupPanel")) {
                String display = element.getStyle().getDisplay();

                // Toggle popup visibility
                if (display == null || display.equals("none")) {
                    // Show popup
                    element.addClassName("mobile-designer-popup");
                    element.getStyle().setProperty("display", "block");
                } else {
                    // Hide popup
                    element.removeClassName("mobile-designer-popup");
                    element.getStyle().setProperty("display", "none");
                }
            }
        }
    }

    /**
     * Populates the mobile designer action dropdown menu with various actions
     * including editor switching, project management, and tutorial controls.
     * Groups related actions together with visual separators for better UX.
     */
    private void populateMobileDropdown() {

        addDropDownButtonItem(DESIGNER_ACTION_DROPDOWN,
                new DropDownItem("SwitchToDesign", MESSAGES.switchToFormEditorButton(), new SwitchToFormEditorAction(), "mobile-dropdown-button"));
        addDropDownButtonItem(DESIGNER_ACTION_DROPDOWN,
                new DropDownItem("SwitchToBlocks", MESSAGES.switchToBlocksEditorButton(), new SwitchToBlocksEditorAction(), "mobile-dropdown-button"));

        // Project actions
        addDropDownButtonItem(DESIGNER_ACTION_DROPDOWN,
                new DropDownItem("ProjectSeparator", "─────────", null, "mobile-dropdown-separator"));
        addDropDownButtonItem(DESIGNER_ACTION_DROPDOWN,
                new DropDownItem("ProjectProperties", MESSAGES.projectPropertiesText(), new ProjectPropertiesAction(), "mobile-dropdown-button"));
        addDropDownButtonItem(DESIGNER_ACTION_DROPDOWN,
                new DropDownItem("SendToGallery", MESSAGES.publishToGalleryButton(), new SendToGalleryAction(), "mobile-dropdown-button"));
        addDropDownButtonItem(DESIGNER_ACTION_DROPDOWN,
                new DropDownItem("TutorialToggle", MESSAGES.toggleTutorialButton(), new ToggleTutorialAction(), "mobile-dropdown-button"));

    }

    /**
     * Toggles between design and blocks editor views with mobile-specific behavior.
     * Manages the visibility of tab bars and automatically closes sidebars
     * when switching between editors to optimize mobile screen space.
     *
     * @param blocks true to switch to blocks editor, false for design editor
     */
    @Override
    public void toggleEditor(boolean blocks) {
        super.toggleEditor(blocks);
        toggleDesignTabBar(!blocks);
        toggleBlockTabBar(blocks);
    }

    /**
     * Controls the visibility of the design tab bar and manages sidebar state.
     * Automatically closes the mobile sidebar when hiding the design tab bar
     * to prevent UI conflicts and optimize screen real estate.
     *
     * @param visible true to show the design tab bar, false to hide it
     */
    private void toggleDesignTabBar(boolean visible) {
        try {
            DesignTabBarMob tabBar = Ode.getInstance().getDesignTabBar();
            if (tabBar != null) {
                tabBar.setVisible(visible);
                // Close sidebar when switching to blocks view
                if (!visible) {
                    MobileSidebar sidebar = Ode.getInstance().getmobileSideBar();
                    if (sidebar != null) {
                        sidebar.close();
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Failed to toggle design tab bar visibility: " + e.getMessage());
        }
    }

    /**
     * Controls the visibility of the blocks tab bar and manages sidebar state.
     * Automatically closes the mobile sidebar when hiding the blocks tab bar
     * to prevent UI conflicts and optimize screen real estate.
     *
     * @param visible true to show the blocks tab bar, false to hide it
     */
    private void toggleBlockTabBar(boolean visible) {
        try {
            BlockTabBarMob tabBar = Ode.getInstance().getBlockTabBar();
            if (tabBar != null) {
                tabBar.setVisible(visible);
                // Close sidebar when switching to design view
                if (!visible) {
                    MobileSidebar sidebar = Ode.getInstance().getmobileSideBar();
                    if (sidebar != null) {
                        sidebar.close();
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Failed to toggle block tab bar visibility: " + e.getMessage());
        }
    }

}