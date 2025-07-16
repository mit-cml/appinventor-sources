// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.youngandroid.YaNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.youngandroid.YaVisibleComponentsPanel;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;

public class UiStyleFactory {

  @UiTemplate("Ode.ui.xml")
  interface OdeUiBinder extends UiBinder<FlowPanel, Ode> {}
  @UiTemplate("style/neo/Ode.ui.xml")
  interface OdeUiBinderNeo extends UiBinder<FlowPanel, Ode> {}

  public FlowPanel createOde(Ode target, String style) {
    if (style.equals("modern")) {
      OdeUiBinderNeo uibinder = GWT.create(OdeUiBinderNeo.class);
      return uibinder.createAndBindUi(target);
    }
    OdeUiBinder uibinder = GWT.create(OdeUiBinder.class);
    return uibinder.createAndBindUi(target);
  }

  public ProjectList createProjectList() {
    return new ProjectList();
  }

  public ProjectFolder createProjectFolder(String name, long dateCreated, long dateModified, ProjectFolder parent) {
    return new ProjectFolder(name, dateCreated, dateModified, parent);
  }

  public ProjectFolder createProjectFolder(String name, long dateCreated, ProjectFolder parent) {
    return new ProjectFolder(name, dateCreated, parent);
  }

  public ProjectFolder createProjectFolder(JSONObject json, ProjectFolder parent) {
    return new ProjectFolder(json, parent, this);
  }

  public NewYoungAndroidProjectWizard createNewYoungAndroidProjectWizard() {
    return new NewYoungAndroidProjectWizard();
  }

  public YaVisibleComponentsPanel createSimpleVisibleComponentsPanel
      (ProjectEditor editor, YaNonVisibleComponentsPanel nonVisPanel) {
    return new YaVisibleComponentsPanel(editor, nonVisPanel);
  }
}
