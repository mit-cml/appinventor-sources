// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.Date;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM;

public class ProjectFolderMob extends ProjectFolder {

    interface ProjectFolderUiBinderMob extends UiBinder<FlowPanel, ProjectFolderMob> { }

    private static final ProjectFolderUiBinderMob uibinder =
            GWT.create(ProjectFolderUiBinderMob.class);

    @UiField protected FlowPanel container;
    @UiField protected FlowPanel childrenContainer;
    @UiField protected Label nameLabel;
    @UiField protected Label dateModifiedLabel;
    @UiField protected Label dateCreatedLabel;
    @UiField protected CheckBox checkBox;
    @UiField protected Icon expandButton;
    @UiField protected FocusPanel expandbuttonFocusPanel;
    @UiField protected DropDownButton dateDropdown;

    public ProjectFolderMob(String name, long dateCreated, long dateModified, ProjectFolder parent) {
        super(name, dateCreated, dateModified, parent);
    }

    public ProjectFolderMob(String name, long dateCreated, ProjectFolder parent) {
        this(name, dateCreated, dateCreated, parent);
    }

    public ProjectFolderMob(JSONObject json, ProjectFolder parent, UiStyleFactory styleFactory) {
        super(json, parent, styleFactory);
    }

    @Override
    public void bindUI() {
        initWidget(uibinder.createAndBindUi(this));

        super.container = this.container;
        super.childrenContainer = this.childrenContainer;
        super.nameLabel = this.nameLabel;
        super.dateModifiedLabel = this.dateModifiedLabel;
        super.dateCreatedLabel = this.dateCreatedLabel;
        super.checkBox = this.checkBox;
        super.expandButton = this.expandButton;
        super.expandbuttonFocusPanel = this.expandbuttonFocusPanel;

        // Add click handler for dropdown styling
        dateDropdown.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addPopupStyling();
            }
        }, ClickEvent.getType());

        // Setup date dropdown after UI is bound
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                setupDateDropdown();
            }
        });
    }

    private void addPopupStyling() {
        NodeList<Element> allElements = Document.get().getElementsByTagName("div");
        for (int i = 0; i < allElements.getLength(); i++) {
            Element element = allElements.getItem(i);
            if (element.getClassName().contains("gwt-PopupPanel")) {
                String display = element.getStyle().getDisplay();

                // Toggle popup visibility
                if (display == null || display.equals("none")) {
                    // Show popup
                    element.addClassName("mobile-date-dropdown-popup");
                    element.getStyle().setProperty("display", "block");
                } else {
                    // Hide popup
                    element.removeClassName("mobile-date-dropdown-popup");
                    element.getStyle().setProperty("display", "none");
                }
            }
        }
    }

    /**
     * Configures the date dropdown menu with folder creation and modification timestamps.
     * Creates dropdown items displaying formatted dates for when the folder was created
     * and last modified.
     */
    private void setupDateDropdown() {
        try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DATE_TIME_MEDIUM);
            Date dateCreated = new Date(getDateCreated());
            Date dateModified = new Date(getDateModified());

            dateDropdown.clearAllItems();
            dateDropdown.setCaption("");

            DropDownItem dateCreatedItem = new DropDownItem("dateCreated",
                    "Created: " + dateTimeFormat.format(dateCreated),
                    new Command() {
                        @Override
                        public void execute() {
                            // No action needed
                        }
                    });
            dateDropdown.addItem(dateCreatedItem);

            DropDownItem dateModifiedItem = new DropDownItem("dateModified",
                    "Modified: " + dateTimeFormat.format(dateModified),
                    new Command() {
                        @Override
                        public void execute() {
                            // No action needed
                        }
                    });
            dateDropdown.addItem(dateModifiedItem);

        } catch (Exception e) {
            DropDownItem errorItem = new DropDownItem("error",
                    "Folder info",
                    new Command() {
                        @Override
                        public void execute() {
                            // No action needed
                        }
                    });
            dateDropdown.addItem(errorItem);
        }
    }

    @Override
    public ProjectListItem createProjectListItem(Project p) {
        ProjectListItem item = new ProjectListItemMob(p);
        item.addStyleName("ode-ProjectItemMobile");

        return item;
    }

    @SuppressWarnings("unused")
    @Override
    protected void toggleFolderSelection(ClickEvent e) {
        super.toggleFolderSelection(e);
    }

    @SuppressWarnings("unused")
    @Override
    protected void toggleExpandedState(KeyDownEvent e) {
        super.toggleExpandedState(e);
    }

    @SuppressWarnings("unused")
    @Override
    protected void toggleExpandedState(ClickEvent e) {
        super.toggleExpandedState(e);
    }
}
