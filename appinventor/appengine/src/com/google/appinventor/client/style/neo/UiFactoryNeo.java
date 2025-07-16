// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.youngandroid.YaNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.youngandroid.YaVisibleComponentsPanel;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.json.client.JSONObject;

public class UiFactoryNeo extends UiStyleFactory {

  @Override
  public ProjectList createProjectList() {
    return new ProjectListNeo();
  }

  @Override
  public ProjectFolder createProjectFolder(String name, long dateCreated, long dateModified, ProjectFolder parent) {
    return new ProjectFolderNeo(name, dateCreated, dateModified, parent);
  }

  @Override
  public ProjectFolder createProjectFolder(String name, long dateCreated, ProjectFolder parent) {
    return new ProjectFolderNeo(name, dateCreated, parent);
  }

  @Override
  public ProjectFolder createProjectFolder(JSONObject json, ProjectFolder parent) {
    return new ProjectFolderNeo(json, parent, this);
  }

  @Override
  public YaVisibleComponentsPanel createSimpleVisibleComponentsPanel
      (ProjectEditor editor, YaNonVisibleComponentsPanel nonVisPanel) {
    return new YaVisibleComponentsPanelNeo(editor, nonVisPanel);
  }
}
