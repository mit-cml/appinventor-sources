// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.Date;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM;

public class ProjectListItemMob extends ProjectListItem {

    interface ProjectListItemUiBinderMob extends UiBinder<FlowPanel, ProjectListItemMob> {}

    private static final ProjectListItemUiBinderMob uibinder =
            GWT.create(ProjectListItemUiBinderMob.class);

    @UiField FlowPanel container;
    @UiField Label nameLabel;
    @UiField Label dateModifiedLabel;
    @UiField Label dateCreatedLabel;
    @UiField CheckBox checkBox;
    @UiField FocusPanel projectnameFocusPanel;
    @UiField DropDownButton dateDropdown;

    public ProjectListItemMob(Project project) {
        super(project);
    }

    @Override
    public void bindUI() {
        initWidget(uibinder.createAndBindUi(this));

        super.container = container;
        super.checkBox = checkBox;
        super.dateCreatedLabel = dateCreatedLabel;
        super.dateModifiedLabel = dateModifiedLabel;
        super.nameLabel = nameLabel;
        super.projectnameFocusPanel = projectnameFocusPanel;

        // Setup date dropdown after all initialization is complete
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                setupDateDropdown();
            }
        });
    }

    /**
     * Configures the date dropdown menu with project creation and modification timestamps.
     * Creates dropdown items displaying formatted dates for when the folder was created
     * and last modified.
     */
    private void setupDateDropdown() {
        Project project = getProject();
        if (project == null) {
            return;
        }

        try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DATE_TIME_MEDIUM);
            Date dateCreated = new Date(project.getDateCreated());
            Date dateModified = new Date(project.getDateModified());

            dateDropdown.clearAllItems();
            dateDropdown.setCaption("");

            DropDownItem dateCreatedItem = new DropDownItem("dateCreated",
                    "Created: " + dateTimeFormat.format(dateCreated),
                    new Command() {
                        @Override
                        public void execute() {
                            // No action needed for date display
                        }
                    });
            dateDropdown.addItem(dateCreatedItem);

            DropDownItem dateModifiedItem = new DropDownItem("dateModified",
                    "Modified: " + dateTimeFormat.format(dateModified),
                    new Command() {
                        @Override
                        public void execute() {
                            // No action needed for date display
                        }
                    });
            dateDropdown.addItem(dateModifiedItem);

        } catch (Exception e) {
            // Fallback if date formatting fails
            DropDownItem errorItem = new DropDownItem("error",
                    "Date info unavailable",
                    new Command() {
                        @Override
                        public void execute() {
                            // No action needed
                        }
                    });
            dateDropdown.addItem(errorItem);
        }
    }

    @UiHandler("checkBox")
    protected void toggleItemSelection(ClickEvent e) {
        super.toggleItemSelection(e);
    }

    @UiHandler("projectnameFocusPanel")
    protected void openProject(KeyDownEvent e) {
        super.openProject(e);
    }

    @UiHandler("projectnameFocusPanel")
    protected void itemClicked(ClickEvent e) {
        super.itemClicked(e);
    }
}