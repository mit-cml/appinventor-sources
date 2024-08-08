// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

public class ProjectFolderNeo extends ProjectFolder {
  interface ProjectFolderUiBinderNeo extends UiBinder<FlowPanel, ProjectFolderNeo> { }

  @UiField protected FlowPanel container;
  @UiField protected FlowPanel childrenContainer;
  @UiField protected Label nameLabel;
  @UiField protected Label dateModifiedLabel;
  @UiField protected Label dateCreatedLabel;
  @UiField protected CheckBox checkBox;
  @UiField protected Icon expandButton;
  @UiField protected FocusPanel expandbuttonFocusPanel;

  public ProjectFolderNeo(String name, long dateCreated, long dateModified, ProjectFolder parent) {
    super(name, dateCreated, dateModified, parent);
  }

  public ProjectFolderNeo(String name, long dateCreated, ProjectFolder parent) {
    this(name, dateCreated, dateCreated, parent);
  }

  public ProjectFolderNeo(JSONObject json, ProjectFolder parent, UiStyleFactory styleFactory) {
    super(json, parent, styleFactory);
  }

  @Override
  public void bindUI() {
    ProjectFolderUiBinderNeo uibinder = GWT.create(ProjectFolderUiBinderNeo.class);
    initWidget(uibinder.createAndBindUi(this));
    super.container = container;
    super.childrenContainer = childrenContainer;
    super.nameLabel = nameLabel;
    super.dateModifiedLabel = dateModifiedLabel;
    super.dateCreatedLabel = dateCreatedLabel;
    super.checkBox = checkBox;
    super.expandButton = expandButton;
    super.expandbuttonFocusPanel = expandbuttonFocusPanel;
  }

  @Override
  public ProjectListItem createProjectListItem(Project p) {
    return new ProjectListItemNeo(p) ;
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
