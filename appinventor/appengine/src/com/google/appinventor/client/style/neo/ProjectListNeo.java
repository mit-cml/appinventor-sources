// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineLabel;

public class ProjectListNeo extends ProjectList {
  interface ProjectListUiBinderNeo extends UiBinder<FlowPanel, ProjectListNeo> {}

  // UI elements
  @UiField CheckBox selectAllCheckBox;
  @UiField FlowPanel container;
  @UiField InlineLabel projectNameSortDec;
  @UiField InlineLabel projectNameSortAsc;
  @UiField InlineLabel createDateSortDec;
  @UiField InlineLabel createDateSortAsc;
  @UiField InlineLabel modDateSortDec;
  @UiField InlineLabel modDateSortAsc;
  @UiField FocusPanel nameFocusPanel;
  @UiField FocusPanel createdateFocusPanel;
  @UiField FocusPanel modDateFocusPanel;

  @Override
  public void bindIU() {
    ProjectListUiBinderNeo uibinder = GWT.create(ProjectListUiBinderNeo.class);
    initWidget(uibinder.createAndBindUi(this));
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

  @UiHandler("selectAllCheckBox")
  protected void toggleAllItemSelection(ClickEvent e) {
    super.toggleAllItemSelection(e);
  }

  @Override
  public ProjectListItemNeo createProjectListItem(Project p) {
    return new ProjectListItemNeo(p);
  }
}
