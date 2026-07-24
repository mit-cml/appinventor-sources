// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineLabel;

public class ProjectListMob extends ProjectList {

    interface ProjectListUiBinderMob extends UiBinder<FlowPanel, ProjectListMob> {}

    @UiField CheckBox selectAllCheckBox;
    @UiField FlowPanel container;
    @UiField InlineLabel projectNameSortDec;
    @UiField InlineLabel projectNameSortAsc;
    @UiField DropDownButton sortDropdown;
    @UiField InlineLabel createDateSortDec;
    @UiField InlineLabel createDateSortAsc;
    @UiField InlineLabel modDateSortDec;
    @UiField InlineLabel modDateSortAsc;
    @UiField FocusPanel nameFocusPanel;
    @UiField FocusPanel createdateFocusPanel;
    @UiField FocusPanel modDateFocusPanel;

    public void bindIU() {
        ProjectListUiBinderMob uibinder = GWT.create(ProjectListUiBinderMob.class);
        initWidget(uibinder.createAndBindUi(this));

        setupSortDropdown();

        super.selectAllCheckBox = selectAllCheckBox;
        super.container = container;
        super.projectNameSortAsc = projectNameSortAsc;
        super.projectNameSortDec = projectNameSortDec;
        super.createDateSortAsc = createDateSortAsc;
        super.createDateSortDec = createDateSortDec;
        super.modDateSortAsc = modDateSortAsc;
        super.modDateSortDec = modDateSortDec;
        super.nameFocusPanel = nameFocusPanel;
        super.createdateFocusPanel = createdateFocusPanel;
        super.modDateFocusPanel = modDateFocusPanel;

        // Add click handler for dropdown styling
        sortDropdown.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addPopupStyling();
            }
        }, ClickEvent.getType());

        Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);

        // It is important to listen to project manager events as soon as possible.
        Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
    }

    /**
     * Initializes the sort dropdown menu with project sorting options.
     *
     * Creates dropdown items that allow users to sort projects by creation date
     * or modification date. Each item triggers a sort order change when selected.
     * The dropdown displays localized headers for the sorting options.
     */
    private void setupSortDropdown() {
        DropDownItem dateCreatedItem = new DropDownItem("sortByDateCreated",
                Ode.MESSAGES.projectDateCreatedHeader(),
                new Command() {
                    @Override
                    public void execute() {
                        changeSortOrder(SortField.DATE_CREATED);
                    }
                });
        sortDropdown.addItem(dateCreatedItem);

        DropDownItem dateModifiedItem = new DropDownItem("sortByDateModified",
                Ode.MESSAGES.projectDateModifiedHeader(),
                new Command() {
                    @Override
                    public void execute() {
                        changeSortOrder(SortField.DATE_MODIFIED);
                    }
                });
        sortDropdown.addItem(dateModifiedItem);


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
                    element.addClassName("mobile-sort-dropdown-popup");
                    element.getStyle().setProperty("display", "block");
                } else {
                    // Hide popup
                    element.removeClassName("mobile-sort-dropdown-popup");
                    element.getStyle().setProperty("display", "none");
                }
            }
        }
    }


    @UiHandler("selectAllCheckBox")
    protected void toggleAllItemSelection(ClickEvent e) {
        super.toggleAllItemSelection(e);
    }

    @Override
    public ProjectListItemMob createProjectListItem(Project p) {
        return new ProjectListItemMob(p);
    }

}

