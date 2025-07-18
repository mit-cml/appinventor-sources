//<!-- Copyright 2025 MIT, All rights reserved -->
//<!-- Released under the Apache License, Version 2.0 -->
//<!-- http://www.apache.org/licenses/LICENSE-2.0 -->
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;

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

        Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);

        // It is important to listen to project manager events as soon as possible.
        Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
    }

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


    @UiHandler("selectAllCheckBox")
    protected void toggleAllItemSelection(ClickEvent e) {
        super.toggleAllItemSelection(e);
    }

    @Override
    public ProjectListItemMob createProjectListItem(Project p) {
        return new ProjectListItemMob(p);
    }

}

