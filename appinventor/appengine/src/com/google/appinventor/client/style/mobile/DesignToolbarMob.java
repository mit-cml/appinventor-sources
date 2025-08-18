package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.actions.*;
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

    private void addPopupStyling() {
        NodeList<Element> allElements = Document.get().getElementsByTagName("div");
        for (int i = 0; i < allElements.getLength(); i++) {
            Element element = allElements.getItem(i);
            if (element.getClassName().contains("gwt-PopupPanel")) {
                String display = element.getStyle().getDisplay();
                if (display == null || !display.equals("none")) {
                    element.addClassName("mobile-designer-popup");
                }
            }
        }
    }

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

    @Override
    public void toggleEditor(boolean blocks) {
        super.toggleEditor(blocks);

    }

}