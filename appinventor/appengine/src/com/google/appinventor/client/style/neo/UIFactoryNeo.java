package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.UIStyleFactory;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.SimpleVisibleComponentsPanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.json.client.JSONObject;

public class UIFactoryNeo extends UIStyleFactory {

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
    return new ProjectFolderNeo(json, parent);
  }

  @Override
  public SimpleVisibleComponentsPanel createSimpleVisibleComponentsPanel
      (YaFormEditor editor, SimpleNonVisibleComponentsPanel nonVisPanel) {
    return new SimpleVisibleComponentsPanelNeo(editor, nonVisPanel);
  }}


